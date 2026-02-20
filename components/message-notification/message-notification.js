// components/message-notification/message-notification.js
Component({
  /**
   * 组件的属性列表
   */
  properties: {},

  /**
   * 组件的初始数据
   */
  data: {
    showMessageNotification: false,
    messageNotificationData: {
      senderName: '',
      senderAvatar: '',
      messageContent: ''
    }
  },

  /**
   * 组件生命周期
   */
  lifetimes: {
    attached() {
      // 组件挂载时，立即同步页面数据
      this.syncPageData()
      
      // 定期监听页面数据变化（用于同步 messageNotification.js 工具函数更新的数据）
      this._checkInterval = setInterval(() => {
        this.syncPageData()
      }, 100)
    },
    detached() {
      // 清理定时器
      if (this._hideTimer) {
        clearTimeout(this._hideTimer)
        this._hideTimer = null
      }
      if (this._checkInterval) {
        clearInterval(this._checkInterval)
        this._checkInterval = null
      }
    }
  },

  /**
   * 组件的方法列表
   */
  methods: {
    // 同步页面数据
    syncPageData() {
      try {
        const pages = getCurrentPages()
        if (pages.length > 0) {
          const currentPage = pages[pages.length - 1]
          if (currentPage && currentPage.data) {
            const pageData = currentPage.data
            // 同步页面的通知数据到组件
            const newShow = pageData.showMessageNotification || false
            const newData = pageData.messageNotificationData || {
              senderName: '',
              senderAvatar: '',
              messageContent: ''
            }
            
            // 只有当数据发生变化时才更新，避免不必要的 setData
            if (this.data.showMessageNotification !== newShow || 
                JSON.stringify(this.data.messageNotificationData) !== JSON.stringify(newData)) {
              this.setData({
                showMessageNotification: newShow,
                messageNotificationData: newData
              })
              
              // 如果显示通知，设置自动隐藏定时器
              if (newShow) {
                if (this._hideTimer) {
                  clearTimeout(this._hideTimer)
                }
                // 注意：实际的隐藏逻辑由 messageNotification.js 工具函数控制
                // 这里不需要设置定时器，因为工具函数已经处理了
              } else {
                if (this._hideTimer) {
                  clearTimeout(this._hideTimer)
                  this._hideTimer = null
                }
              }
            }
          }
        }
      } catch (error) {
        console.error('[MessageNotification Component] 同步页面数据失败:', error)
      }
    }
  }
})

