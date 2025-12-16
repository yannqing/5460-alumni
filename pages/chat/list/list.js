// pages/chat/list/list.js
const config = require('../../../utils/config.js')
const { chatApi } = require('../../../api/api.js')

Page({
  data: {
    chatList: [],
    allChatList: [],
    refreshing: false,
    loading: false,
    showSidebar: false,
    socketConnected: false,
    onlineUsers: [],
    unreadTotal: 0
  },

  // WebSocket 事件监听器引用（用于移除监听）
  messageListener: null,
  onlineStatusListener: null,
  connectListener: null,
  disconnectListener: null,

  onLoad() {
    this.initWebSocket()
  },

  onShow() {
    // 每次显示页面时刷新列表
    this.loadChatList()
    this.loadUnreadTotal()
    // 刷新在线状态
    this.refreshOnlineStatus()
  },

  onUnload() {
    // 页面卸载时移除 WebSocket 监听
    this.removeWebSocketListeners()
  },

  onHide() {
    // 页面隐藏时不移除监听，保持连接
  },

  /**
   * 初始化 WebSocket 监听
   */
  initWebSocket() {
    const app = getApp()
    const socketManager = app.globalData.socketManager

    if (!socketManager) {
      console.error('[ChatList] WebSocket 管理器未初始化')
      return
    }

    // 监听新消息
    this.messageListener = (data) => {
      if (data.type === 'message') {
        this.handleNewMessage(data)
      }
    }
    socketManager.on('onMessage', this.messageListener)

    // 监听在线状态变化
    this.onlineStatusListener = (data) => {
      this.handleOnlineStatusChange(data)
    }
    socketManager.on('onOnlineStatus', this.onlineStatusListener)

    // 监听连接状态
    this.connectListener = () => {
      this.setData({ socketConnected: true })
      this.refreshOnlineStatus()
    }
    socketManager.on('onConnect', this.connectListener)

    this.disconnectListener = () => {
      this.setData({ socketConnected: false })
    }
    socketManager.on('onDisconnect', this.disconnectListener)

    // 获取当前连接状态
    const status = socketManager.getStatus()
    this.setData({ 
      socketConnected: status.isConnected,
      onlineUsers: socketManager.getOnlineUsers()
    })
  },

  /**
   * 移除 WebSocket 监听
   */
  removeWebSocketListeners() {
    const app = getApp()
    const socketManager = app.globalData.socketManager

    if (socketManager) {
      if (this.messageListener) {
        socketManager.off('onMessage', this.messageListener)
      }
      if (this.onlineStatusListener) {
        socketManager.off('onOnlineStatus', this.onlineStatusListener)
      }
      if (this.connectListener) {
        socketManager.off('onConnect', this.connectListener)
      }
      if (this.disconnectListener) {
        socketManager.off('onDisconnect', this.disconnectListener)
      }
    }
  },

  /**
   * 处理新消息
   */
  handleNewMessage(data) {
    console.log('[ChatList] 收到新消息:', data)
    
    const messageData = data.data || {}
    const { fromUserId, content, timestamp } = messageData

    // 更新聊天列表
    const chatList = this.data.chatList
    const existingChat = chatList.find(chat => chat.userId === fromUserId)

    if (existingChat) {
      // 更新现有聊天
      existingChat.lastMessage = content
      existingChat.lastTime = this.formatTime(timestamp)
      existingChat.unreadCount = (existingChat.unreadCount || 0) + 1
      
      // 移到最前面
      const index = chatList.indexOf(existingChat)
      chatList.splice(index, 1)
      chatList.unshift(existingChat)
    } else {
      // 添加新聊天
      chatList.unshift({
        id: fromUserId,
        userId: fromUserId,
        name: messageData.fromUserName || '新消息',
        avatar: messageData.fromUserAvatar ? config.getImageUrl(messageData.fromUserAvatar) : '',
        lastMessage: content,
        lastTime: this.formatTime(timestamp),
        unreadCount: 1,
        isMuted: false
      })
    }

    this.setData({ chatList })

    // 显示新消息提示（如果页面在前台）
    if (wx.getStorageSync('currentPage') === 'chat-list') {
      wx.showToast({
        title: '收到新消息',
        icon: 'none',
        duration: 1500
      })
    }
  },

  /**
   * 处理在线状态变化
   */
  handleOnlineStatusChange(data) {
    console.log('[ChatList] 在线状态变化:', data)
    
    const { onlineUsers } = data
    if (onlineUsers) {
      this.setData({ onlineUsers })
    }
  },

  /**
   * 刷新在线状态
   */
  refreshOnlineStatus() {
    const app = getApp()
    const socketManager = app.globalData.socketManager
    
    if (socketManager && socketManager.isConnected) {
      socketManager.requestOnlineUsers()
    }
  },

  /**
   * 检查用户是否在线
   */
  isUserOnline(userId) {
    return this.data.onlineUsers.includes(String(userId))
  },

  /**
   * 格式化时间
   */
  formatTime(timestamp) {
    const now = new Date()
    const date = new Date(timestamp)
    const diff = now.getTime() - date.getTime()
    
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
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    if (diff < 7 * 24 * 3600000) {
      return weekDays[date.getDay()]
    }
    // 更早
    return `${date.getMonth() + 1}/${date.getDate()}`
  },

  async loadChatList() {
    this.setData({ loading: true })
    
    try {
      // 从后端获取会话列表
      const res = await chatApi.getConversations({
      })
      
      if (res.data && res.data.code === 200) {
        const chatList = res.data.data?.records || res.data.data || []
        
        // 映射聊天列表数据
        const mappedChatList = chatList.map(chat => ({
          id: chat.conversationId || chat.id || chat.userId,
          userId: chat.userId || chat.targetId,
          peerId: chat.peerId || chat.userId || chat.targetId, // 确保获取 peerId
          conversationId: chat.conversationId || chat.id, // 保存会话ID
          draftContent: chat.draftContent || '', // 草稿内容
          // 优先使用后端返回的 peerNickname
          name: chat.peerNickname || chat.userName || chat.targetName || chat.name || '未知用户',
          // 优先使用后端返回的 peerAvatar
          avatar: chat.peerAvatar ? config.getImageUrl(chat.peerAvatar) : ((chat.userAvatar || chat.targetAvatar || chat.avatar) ? config.getImageUrl(chat.userAvatar || chat.targetAvatar || chat.avatar) : ''),
          // 优先使用草稿，否则使用最后一条消息
          lastMessage: (chat.draftContent) ? ('[草稿] ' + chat.draftContent) : (chat.lastMessageContent || chat.lastMessage || chat.lastMsg || ''),
          lastTime: this.formatTime(chat.lastMessageTime || chat.lastMsgTime || chat.updateTime),
          unreadCount: chat.unreadCount || 0,
          isMuted: chat.isMuted || false,
          isOnline: this.isUserOnline(chat.userId || chat.targetId)
        }))
        
        this.setData({
          chatList: mappedChatList,
          allChatList: mappedChatList,
          loading: false
        })
        
        return
      }
    } catch (error) {
      console.error('[ChatList] 加载聊天列表失败:', error)
      wx.showToast({ title: '加载会话失败', icon: 'none' })
    }
    
    this.setData({ loading: false })
  },

  /**
   * 获取未读总数
   */
  async loadUnreadTotal() {
    try {
      const res = await chatApi.getUnreadCount()
      if (res.data && res.data.code === 200) {
        const total = res.data.data || 0
        this.setData({ unreadTotal: total })

        // 更新底部 TabBar 未读角标（假设“5460消息”在第 3 个 tab，索引 2）
        if (typeof wx.setTabBarBadge === 'function') {
          if (total > 0) {
            wx.setTabBarBadge({
              index: 2,
              text: String(total > 99 ? '99+' : total)
            })
          } else {
            wx.removeTabBarBadge({ index: 2 })
          }
        }
      }
    } catch (error) {
      console.error('[ChatList] 获取未读总数失败:', error)
    }
  },

  onRefresh() {
    this.setData({ refreshing: true })
    this.loadChatList()
    setTimeout(() => {
      this.setData({ refreshing: false })
    }, 1000)
  },

  openChat(e) {
    const { id, type, peerid } = e.currentTarget.dataset
    
    // 查找当前聊天对象以获取更多信息
    const chat = this.data.chatList.find(c => c.id === id)
    
    if (chat && chat.unreadCount > 0) {
      // 重置未读数
      chat.unreadCount = 0
      this.setData({ chatList: this.data.chatList })
    }
    
    // 优先使用 peerId 跳转，因为详情页需要 peerId (User ID) 来获取信息和发送消息
    // 如果没有 peerId (极少情况)，则降级使用 id (conversationId)
    const targetId = peerid || id
    
    // 获取昵称和头像传递给详情页
    const name = chat ? (chat.name || '') : ''
    const avatar = chat ? (chat.avatar || '') : ''
    const conversationId = chat ? (chat.conversationId || '') : ''
    const draftContent = chat ? (chat.draftContent || '') : ''
    
    wx.navigateTo({
      url: `/pages/chat/detail/detail?id=${targetId}&type=${type || 'chat'}&name=${encodeURIComponent(name)}&avatar=${encodeURIComponent(avatar)}&conversationId=${conversationId}&draftContent=${encodeURIComponent(draftContent)}`
    })
  },

  toggleSidebar() {
    this.setData({
      showSidebar: !this.data.showSidebar
    })
  },

  navigateToMyAssociations() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-association/my-association'
    })
  },

  navigateToMyFollows() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-follow/my-follow'
    })
  },

  navigateToMyFavorites() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-favorites/my-favorites'
    })
  }
})

