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
    unreadTotal: 0,
    
    // 滑动操作相关
    startX: 0,
    startY: 0
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

  handleNewMessage(data) {
    // 收到新消息，重新加载列表
    // 优化：可以直接更新列表状态而不是重新加载
    this.loadChatList()
    this.loadUnreadTotal()
  },

  handleOnlineStatusChange(data) {
    this.setData({ onlineUsers: data })
    this.refreshOnlineStatus()
  },

  refreshOnlineStatus() {
    const { allChatList, onlineUsers } = this.data
    if (!allChatList || allChatList.length === 0) return

    const newList = allChatList.map(item => ({
      ...item,
      isOnline: this.isUserOnline(item.userId)
    }))

    this.setData({ 
      allChatList: newList,
      chatList: newList
    })
  },

  isUserOnline(userId) {
    if (!userId) return false
    return this.data.onlineUsers.some(u => String(u.userId) === String(userId))
  },

  formatTime(timeStr) {
    if (!timeStr) return ''
    
    let date;
    
    try {
      // 1. 如果是数组形式 [2023, 12, 17, 10, 0, 0]
      if (Array.isArray(timeStr)) {
        // new Date(year, monthIndex, day, hour, minute, second)
        // 注意 monthIndex 从 0 开始，所以需要减 1
        if (timeStr.length >= 3) {
           date = new Date(timeStr[0], timeStr[1] - 1, timeStr[2], 
                           timeStr[3] || 0, timeStr[4] || 0, timeStr[5] || 0);
        }
      } 
      // 2. 如果是数字（时间戳）
      else if (typeof timeStr === 'number') {
        date = new Date(timeStr);
      }
      // 3. 如果是字符串
      else if (typeof timeStr === 'string') {
        // 纯数字字符串 -> 转数字
        if (/^\d+$/.test(timeStr)) {
          date = new Date(Number(timeStr));
        } else {
          // 尝试标准解析
          date = new Date(timeStr);
          // 如果失败，尝试替换 - 为 / (兼容 iOS)
          if (isNaN(date.getTime())) {
            date = new Date(timeStr.replace(/-/g, '/'));
          }
          // 如果还失败，尝试替换 T 为空格 (兼容某些 ISO 变体)
          if (isNaN(date.getTime())) {
            date = new Date(timeStr.replace(/T/g, ' ').replace(/-/g, '/'));
          }
        }
      }
    } catch (e) {
      console.error('Date parse error:', e);
    }

    // 检查日期是否有效
    if (!date || isNaN(date.getTime())) {
      // 如果解析失败，尽量返回原始值（如果看起来像时间）或者空
      // 避免直接返回空导致用户以为没时间
      return String(timeStr).substring(0, 16); // 截取一部分作为兜底显示
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
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    if (diff < 7 * 24 * 3600000 && diff > 0) {
      return weekDays[date.getDay()]
    }
    // 更早
    return `${date.getMonth() + 1}/${date.getDate()}`
  },

  async loadChatList() {
    this.setData({ loading: true })
    
    try {
      // 从后端获取会话列表
      const res = await chatApi.getConversations({})
      
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
          isPinned: chat.isPinned || false, // 添加置顶状态
          isOnline: this.isUserOnline(chat.userId || chat.targetId),
          isTouchMove: false // 初始不显示滑动菜单
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
      // 如果报错，可能是接口问题，不弹窗，避免刷屏
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

  onPullDownRefresh() {
    this.loadChatList().then(() => {
      wx.stopPullDownRefresh()
    })
    this.loadUnreadTotal()
  },

  openChat(e) {
    const { id, type, peerid } = e.currentTarget.dataset
    
    // 查找当前聊天对象以获取更多信息
    const chat = this.data.allChatList.find(c => c.id === id)
    
    if (chat && chat.unreadCount > 0) {
      // 重置未读数
      chat.unreadCount = 0
      this.setData({ allChatList: this.data.allChatList })
    }
    
    // 优先使用 peerId 跳转
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
  },

  // ================= 手势滑动逻辑 =================

  touchstart: function (e) {
    // 开始触摸时 重置所有删除状态（除了当前点击的）
    // 或者简单点，每次触摸都只记录起点
    this.setData({
      startX: e.changedTouches[0].clientX,
      startY: e.changedTouches[0].clientY
    })
  },

  touchmove: function (e) {
    let index = e.currentTarget.dataset.index,
      startX = this.data.startX,
      startY = this.data.startY,
      touchMoveX = e.changedTouches[0].clientX,
      touchMoveY = e.changedTouches[0].clientY,
      
      // 计算角度
      angle = this.angle({ X: startX, Y: startY }, { X: touchMoveX, Y: touchMoveY });

    this.data.allChatList.forEach(function (v, i) {
      v.isTouchMove = false
      // 滑动超过30度角 return
      if (Math.abs(angle) > 30) return;
      if (i == index) {
        if (touchMoveX > startX) // 右滑
          v.isTouchMove = false
        else // 左滑
          v.isTouchMove = true
      }
    })

    // 更新数据
    this.setData({
      allChatList: this.data.allChatList
    })
  },

  /**
   * 计算滑动角度
   * @param {Object} start 起点
   * @param {Object} end 终点
   */
  angle: function (start, end) {
    var _X = end.X - start.X,
      _Y = end.Y - start.Y
    // 返回角度 /Math.atan()返回数字的反正切值
    return 360 * Math.atan(_Y / _X) / (2 * Math.PI);
  },

  // ================= 列表操作逻辑 =================

  /**
   * 标记已读
   */
  async markRead(e) {
    const { peerid, index } = e.currentTarget.dataset
    
    if (!peerid) {
      console.error('[ChatList] 标记已读失败：缺少 peerid')
      wx.showToast({ title: '操作失败', icon: 'none' })
      return
    }
    
    // 乐观更新
    const list = this.data.allChatList
    const originalUnreadCount = list[index].unreadCount
    list[index].unreadCount = 0
    list[index].isTouchMove = false // 关闭滑动菜单
    this.setData({ allChatList: list })

    try {
      // 调用后端接口标记已读
      const res = await chatApi.markConversationRead(peerid)
      
      if (res.data && res.data.code === 200) {
        // 更新未读总数
        this.loadUnreadTotal()
      } else {
        // 回滚
        list[index].unreadCount = originalUnreadCount
        this.setData({ allChatList: list })
        wx.showToast({ title: res.data?.msg || '操作失败', icon: 'none' })
      }
    } catch (error) {
      console.error('[ChatList] 标记已读失败:', error)
      // 回滚
      list[index].unreadCount = originalUnreadCount
      this.setData({ allChatList: list })
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  /**
   * 置顶/取消置顶
   */
  async pinConversation(e) {
    const { id, index, ispinned } = e.currentTarget.dataset
    // id 即 conversationId
    if (!id) return

    // dataset 中的值是字符串，需要正确转换为布尔值
    // ispinned 可能是 "true"、"false"、true、false 或 undefined
    const currentIsPinned = ispinned === true || ispinned === 'true'
    const newIsPinned = !currentIsPinned
    
    try {
      // 调用 API，注意传递 isPinned 参数
      const res = await chatApi.pinConversation(id, newIsPinned)
      
      if (res.data && res.data.code === 200) {
        const list = this.data.allChatList
        list[index].isPinned = newIsPinned
        list[index].isTouchMove = false
        
        // 重新排序：置顶的在前面
        list.sort((a, b) => {
          if (a.isPinned && !b.isPinned) return -1
          if (!a.isPinned && b.isPinned) return 1
          // 如果置顶状态相同，按时间倒序（假设时间已处理好，或者用原始时间戳）
          // 这里简化，假设列表本来就是按时间排好的，只是置顶改变了顺序
          return 0 
        })
        
        this.setData({ allChatList: list })
      } else {
        wx.showToast({ title: '操作失败', icon: 'none' })
      }
    } catch (error) {
      console.error(error)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  /**
   * 删除会话
   */
  async deleteConversation(e) {
    const { id, index } = e.currentTarget.dataset
    // id 是 conversationId
    
    if (!id) {
      console.error('[ChatList] 删除会话失败：缺少 conversationId')
      wx.showToast({ title: '操作失败', icon: 'none' })
      return
    }
    
    wx.showModal({
      title: '提示',
      content: '确定要删除该会话吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const apiRes = await chatApi.deleteConversation(id)
            if (apiRes.data && apiRes.data.code === 200) {
              const list = this.data.allChatList
              list.splice(index, 1)
              this.setData({ allChatList: list, chatList: list })
              wx.showToast({ title: '删除成功', icon: 'success' })
              // 更新未读总数
              this.loadUnreadTotal()
            } else {
              wx.showToast({ title: apiRes.data?.msg || '删除失败', icon: 'none' })
            }
          } catch (error) {
            console.error('[ChatList] 删除会话失败:', error)
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        } else {
           // 取消删除，关闭滑动
           const list = this.data.allChatList
           list[index].isTouchMove = false
           this.setData({ allChatList: list })
        }
      }
    })
  }
})
