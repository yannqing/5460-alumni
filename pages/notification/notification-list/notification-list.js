// pages/notification/notification-list/notification-list.js
const { chatApi } = require('../../../api/api.js')

Page({
  data: {
    notificationList: [],
    loading: false,
    refreshing: false,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,

    // 筛选条件
    activeTab: 'all', // all, unread
    messageType: '', // 空表示全部类型

    tabs: [
      { key: 'all', label: '全部' },
      { key: 'unread', label: '未读' }
    ]
  },

  onLoad() {
    this.loadNotificationList()
  },

  onShow() {
    // 每次显示时刷新列表
    this.refreshList()
  },

  onPullDownRefresh() {
    this.refreshList().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore()
    }
  },

  // 切换Tab
  switchTab(e) {
    const { key } = e.currentTarget.dataset
    this.setData({
      activeTab: key,
      pageNum: 1,
      notificationList: [],
      hasMore: true
    })
    this.loadNotificationList()
  },

  // 刷新列表
  async refreshList() {
    this.setData({
      refreshing: true,
      pageNum: 1,
      notificationList: [],
      hasMore: true
    })
    await this.loadNotificationList()
    this.setData({ refreshing: false })
  },

  // 加载更多
  async loadMore() {
    const nextPage = this.data.pageNum + 1
    this.setData({ pageNum: nextPage })
    await this.loadNotificationList()
  },

  // 加载通知列表
  async loadNotificationList() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const params = {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      }

      // 根据Tab设置筛选条件
      if (this.data.activeTab === 'unread') {
        params.readStatus = 0 // 0-未读
      }

      // 如果有消息类型筛选
      if (this.data.messageType) {
        params.messageType = this.data.messageType
      }

      const res = await chatApi.getNotificationList(params)

      if (res.data && res.data.code === 200) {
        const pageData = res.data.data || {}
        const records = pageData.records || []

        // 格式化通知数据
        const formattedList = records.map(item => ({
          notificationId: item.notificationId,
          messageType: item.messageType,
          messageTypeText: this.getMessageTypeText(item.messageType),
          fromUserId: item.fromUserId,
          fromUsername: item.fromUsername || '系统',
          title: item.title || '',
          content: item.content || '',
          relatedId: item.relatedId,
          relatedType: item.relatedType,
          readStatus: item.readStatus,
          readTime: item.readTime,
          extraData: item.extraData,
          createdTime: this.formatTime(item.createdTime),
          rawCreatedTime: item.createdTime
        }))

        // 追加或替换列表
        const newList = this.data.pageNum === 1
          ? formattedList
          : [...this.data.notificationList, ...formattedList]

        this.setData({
          notificationList: newList,
          hasMore: records.length >= this.data.pageSize,
          loading: false
        })
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.message || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载通知列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 获取消息类型文本
  getMessageTypeText(type) {
    const typeMap = {
      'USER_FOLLOW': '关注',
      'SYSTEM_NOTICE': '系统通知',
      'COMMENT': '评论',
      'LIKE': '点赞',
      'REPLY': '回复',
      'MENTION': '提及'
    }
    return typeMap[type] || type
  },

  // 格式化时间
  formatTime(timeStr) {
    if (!timeStr) return ''

    let date

    try {
      // 处理数组形式
      if (Array.isArray(timeStr)) {
        if (timeStr.length >= 3) {
          date = new Date(timeStr[0], timeStr[1] - 1, timeStr[2],
            timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0)
        }
      }
      // 处理数字（时间戳）
      else if (typeof timeStr === 'number') {
        date = new Date(timeStr)
      }
      // 处理字符串
      else if (typeof timeStr === 'string') {
        if (/^\d+$/.test(timeStr)) {
          date = new Date(Number(timeStr))
        } else {
          date = new Date(timeStr.replace(/-/g, '/').replace(/T/g, ' '))
        }
      }
    } catch (e) {
      console.error('Date parse error:', e)
    }

    if (!date || isNaN(date.getTime())) {
      return String(timeStr).substring(0, 16)
    }

    const now = new Date()
    const diff = now - date

    // 小于1分钟
    if (diff < 60000) {
      return '刚刚'
    }
    // 小于1小时
    if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`
    }
    // 今天
    if (date.toDateString() === now.toDateString()) {
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
    // 昨天
    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)
    if (date.toDateString() === yesterday.toDateString()) {
      return '昨天'
    }
    // 本周
    if (diff < 7 * 24 * 3600000 && diff > 0) {
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekDays[date.getDay()]
    }
    // 更早
    return `${date.getMonth() + 1}月${date.getDate()}日`
  },

  // 点击通知项
  async handleNotificationClick(e) {
    const { item, index } = e.currentTarget.dataset

    // 如果是未读状态，先标记为已读
    if (item.readStatus === 0) {
      await this.markNotificationAsRead(item.notificationId, index)
    }

    // 根据通知类型和相关业务类型跳转到对应页面
    const { relatedType, relatedId, messageType } = item

    // 如果是用户类型（如关注通知），跳转到用户主页
    if (relatedType === 'USER' && relatedId) {
      wx.navigateTo({
        url: `/pages/alumni/detail/detail?id=${relatedId}`,
        fail: (err) => {
          console.error('[Notification] 跳转用户主页失败:', err)
          // 跳转失败时显示详情
          wx.showModal({
            title: item.title || '通知详情',
            content: item.content || '暂无内容',
            showCancel: false
          })
        }
      })
      return
    }

    // 其他类型的通知保持原有逻辑（显示详情）
    // 如果是商户入驻申请未通过，显示查看详情按钮
    if (item.title === '商户入驻申请未通过') {
      wx.showModal({
        title: item.title || '通知详情',
        content: item.content || '暂无内容',
        showCancel: true,
        cancelText: '确定',
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            // 查看详情按钮点击事件，暂时不做功能处理
            console.log('查看详情按钮被点击')
          }
        }
      })
    } else {
      wx.showModal({
        title: item.title || '通知详情',
        content: item.content || '暂无内容',
        showCancel: false
      })
    }
  },

  // 标记单条通知为已读
  async markNotificationAsRead(notificationId, index) {
    try {
      const res = await chatApi.markNotificationRead(notificationId)

      if (res.data && res.data.code === 200) {
        // 更新列表中的已读状态
        const list = this.data.notificationList
        if (list[index]) {
          list[index].readStatus = 1
          this.setData({ notificationList: list })
        }

        // 更新全局未读数
        const app = getApp()
        if (app && app.updateUnreadCount) {
          app.updateUnreadCount()
        }
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  },

  // 全部已读
  async markAllAsRead() {
    wx.showModal({
      title: '提示',
      content: '确定要标记所有通知为已读吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '处理中...', mask: true })

            const result = await chatApi.markNotificationRead()

            wx.hideLoading()

            if (result.data && result.data.code === 200) {
              const count = result.data.data || 0
              wx.showToast({
                title: `已标记${count}条通知为已读`,
                icon: 'success'
              })

              // 刷新列表
              this.refreshList()

              // 更新全局未读数
              const app = getApp()
              if (app && app.updateUnreadCount) {
                app.updateUnreadCount()
              }
            } else {
              wx.showToast({
                title: result.data?.message || '操作失败',
                icon: 'none'
              })
            }
          } catch (error) {
            wx.hideLoading()
            console.error('全部已读失败:', error)
            wx.showToast({
              title: '操作失败，请重试',
              icon: 'none'
            })
          }
        }
      }
    })
  }
})
