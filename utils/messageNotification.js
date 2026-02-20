/**
 * 消息通知工具
 * 在小程序任何页面顶部显示消息通知
 */

/**
 * 显示消息通知
 * @param {Object} options - 通知选项
 * @param {string} options.senderName - 发送者昵称
 * @param {string} options.senderAvatar - 发送者头像
 * @param {string} options.messageContent - 消息内容
 * @param {number} options.duration - 显示时长（毫秒），默认2000
 */
function showMessageNotification(options) {
  const { senderName, senderAvatar, messageContent, duration = 2000 } = options

  console.log('[MessageNotification] 显示通知:', { senderName, senderAvatar, messageContent })

  // 获取当前页面栈
  const pages = getCurrentPages()
  if (pages.length === 0) {
    console.warn('[MessageNotification] 没有可用的页面栈')
    return
  }

  const currentPage = pages[pages.length - 1]
  console.log('[MessageNotification] 当前页面:', currentPage.route)
  
  // 确保页面数据已初始化
  if (!currentPage.data) {
    currentPage.data = {}
  }

  // 初始化通知相关字段（如果不存在）
  if (currentPage.data.showMessageNotification === undefined) {
    currentPage.data.showMessageNotification = false
  }
  if (!currentPage.data.messageNotificationData) {
    currentPage.data.messageNotificationData = {
      senderName: '',
      senderAvatar: '',
      messageContent: ''
    }
  }

  // 如果页面已经有通知数据，先隐藏之前的
  if (currentPage.data.showMessageNotification) {
    console.log('[MessageNotification] 隐藏之前的通知')
    currentPage.setData({
      showMessageNotification: false
    })
    // 等待动画完成
    setTimeout(() => {
      showNewNotification()
    }, 300)
  } else {
    showNewNotification()
  }

  function showNewNotification() {
    console.log('[MessageNotification] 显示新通知')
    // 显示新通知
    currentPage.setData({
      showMessageNotification: true,
      messageNotificationData: {
        senderName: senderName || '未知用户',
        senderAvatar: senderAvatar || '',
        messageContent: messageContent || ''
      }
    }, () => {
      console.log('[MessageNotification] 通知已显示，页面数据:', currentPage.data.showMessageNotification)
    })

    // 自动隐藏
    setTimeout(() => {
      if (currentPage.data && currentPage.data.showMessageNotification) {
        console.log('[MessageNotification] 自动隐藏通知')
        currentPage.setData({
          showMessageNotification: false
        })
      }
    }, duration)
  }
}

/**
 * 隐藏消息通知
 */
function hideMessageNotification() {
  const pages = getCurrentPages()
  if (pages.length === 0) return

  const currentPage = pages[pages.length - 1]
  if (currentPage.data && currentPage.data.showMessageNotification) {
    currentPage.setData({
      showMessageNotification: false
    })
  }
}

module.exports = {
  showMessageNotification,
  hideMessageNotification
}

