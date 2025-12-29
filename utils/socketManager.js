/**
 * WebSocket 管理器 - 企业级实现
 * 功能：聊天消息、在线状态、断线重连、心跳检测、消息队列
 */

class SocketManager {
  constructor() {
    // WebSocket 实例
    this.socket = null
    
    // 连接状态
    this.isConnected = false
    this.isConnecting = false
    
    // 配置
    this.config = {
      url: '',                    // WebSocket 服务器地址
      heartbeatInterval: 30000,   // 心跳间隔（30秒）
      reconnectInterval: 3000,    // 重连间隔（3秒）
      reconnectMaxTimes: 5,       // 最大重连次数
      timeout: 10000,             // 连接超时时间（10秒）
    }
    
    // 重连相关
    this.reconnectTimer = null
    this.reconnectCount = 0
    
    // 心跳相关
    this.heartbeatTimer = null
    this.heartbeatTimeoutTimer = null
    
    // 消息队列（离线时缓存消息）
    this.messageQueue = []
    
    // 事件监听器
    this.listeners = {
      onMessage: [],      // 收到消息
      onConnect: [],      // 连接成功
      onDisconnect: [],   // 连接断开
      onError: [],        // 发生错误
      onReconnect: [],    // 重连中
      onOnlineStatus: [], // 在线状态变化
    }
    
    // 用户信息
    this.userId = null
    this.token = null
    
    // 在线用户列表
    this.onlineUsers = new Set()
  }

  /**
   * 初始化配置
   */
  init(url, userId, token, options = {}) {
    this.config.url = url
    this.userId = userId
    this.token = token
    
    // 合并自定义配置
    Object.assign(this.config, options)
    
    console.log('[WebSocket] 初始化配置:', {
      url: this.config.url,
      userId: this.userId
    })
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    if (this.isConnected || this.isConnecting) {
      console.log('[WebSocket] 已连接或正在连接中')
      return
    }

    this.isConnecting = true
    console.log('[WebSocket] 开始连接...', {
      url: this.config.url,
      userId: this.userId,
      hasToken: !!this.token,
      tokenLength: this.token ? this.token.length : 0
    })

    try {
      // 构建连接URL（使用 x-token 作为参数名，进行 URL 编码）
      const encodedToken = encodeURIComponent(this.token || '')
      const url = `${this.config.url}?x-token=${encodedToken}`
      
      console.log('[WebSocket] ==================== 开始连接 ====================')
      console.log('[WebSocket] 连接地址:', this.config.url)
      console.log('[WebSocket] 用户ID:', this.userId)
      console.log('[WebSocket] Token长度:', this.token ? this.token.length : 0)
      console.log('[WebSocket] 完整URL:', url.replace(encodedToken, '***TOKEN***'))
      console.log('[WebSocket] =================================================')
      
      this.socket = wx.connectSocket({
        url: url,
        header: {
          'x-token': this.token,                     // 使用 x-token 字段
          'Authorization': `Bearer ${this.token}`,   // 同时保留标准格式
          'Content-Type': 'application/json'
        },
        success: () => {
          console.log('[WebSocket] ✅ 连接请求已发送，等待握手...', {
            url,
            header: {
              'x-token': this.token ? '***TOKEN***' : '',
              'Authorization': this.token ? 'Bearer ***TOKEN***' : '',
              'Content-Type': 'application/json'
            }
          })
        },
        fail: (err) => {
          console.error('[WebSocket] ❌ 连接请求失败:', err)
          this.isConnecting = false
          this.handleError(err)
          
          // 显示网络错误提示
          wx.showToast({
            title: '网络错误',
            icon: 'none',
            duration: 2000
          })
          
          this.tryReconnect()
        }
      })

      // 监听连接打开
      this.socket.onOpen(() => {
        this.onOpen()
      })

      // 监听消息接收
      this.socket.onMessage((res) => {
        this.onMessage(res)
      })

      // 监听连接错误
      this.socket.onError((err) => {
        this.onError(err)
      })

      // 监听连接关闭
      this.socket.onClose((res) => {
        this.onClose(res)
      })

    } catch (error) {
      console.error('[WebSocket] 连接异常:', error)
      this.isConnecting = false
      this.handleError(error)
    }
  }

  /**
   * 连接成功回调
   */
  onOpen() {
    console.log('[WebSocket] ==================== 连接成功 ====================')
    console.log('[WebSocket] ✅ WebSocket 握手成功')
    console.log('[WebSocket] 连接状态: 已连接')
    console.log('[WebSocket] 用户ID:', this.userId)
    console.log('[WebSocket] 服务器地址:', this.config.url)
    console.log('[WebSocket] 重连次数已重置: 0')
    console.log('[WebSocket] ====================================================')
    
    this.isConnected = true
    this.isConnecting = false
    this.reconnectCount = 0

    // 发送认证消息
    console.log('[WebSocket] 📤 发送认证消息...')
    this.sendAuth()

    // 开始心跳检测
    console.log('[WebSocket] 💓 启动心跳检测 (间隔: 30秒)')
    this.startHeartbeat()

    // 发送缓存的消息
    if (this.messageQueue.length > 0) {
      console.log(`[WebSocket] 📨 发送缓存消息 (${this.messageQueue.length} 条)`)
      this.flushMessageQueue()
    }

    // 触发连接成功事件
    this.emit('onConnect', { userId: this.userId })
    
    // 显示成功提示（已注释，连接成功时不显示提示）
    // wx.showToast({
    //   title: 'WebSocket 已连接',
    //   icon: 'success',
    //   duration: 2000
    // })
  }

  /**
   * 接收消息回调
   */
  onMessage(res) {
    try {
      const data = JSON.parse(res.data)
      console.log('[WebSocket] 收到消息:', data)

      // 处理不同类型的消息
      switch (data.type) {
        case 'auth':
          // 认证结果
          this.handleAuthResponse(data)
          break
        case 'heartbeat':
          // 心跳响应
          this.handleHeartbeatResponse(data)
          break
        case 'msg':
          // 聊天消息
          this.handleChatMessage(data)
          break
        case 'online_status':
          // 在线状态变化
          this.handleOnlineStatus(data)
          break
        case 'system':
          // 系统消息
          this.handleSystemMessage(data)
          break
        default:
          console.warn('[WebSocket] 未知消息类型:', data.type)
      }

      // 触发消息接收事件
      this.emit('onMessage', data)

    } catch (error) {
      console.error('[WebSocket] 消息解析失败:', error)
    }
  }

  /**
   * 连接错误回调
   */
  onError(err) {
    console.error('[WebSocket] ==================== 连接错误 ====================')
    console.error('[WebSocket] ❌ 发生错误')
    console.error('[WebSocket] 错误信息:', err)
    console.error('[WebSocket] 错误代码:', err.errCode)
    console.error('[WebSocket] 错误消息:', err.errMsg)
    console.error('[WebSocket] ====================================================')
    
    this.isConnecting = false
    this.handleError(err)
    this.emit('onError', err)
    
    // 显示网络错误提示
    wx.showToast({
      title: '网络错误',
      icon: 'none',
      duration: 2000
    })
  }

  /**
   * 连接关闭回调
   */
  onClose(res) {
    console.log('[WebSocket] 连接关闭:', res)
    console.log('[WebSocket] 当前状态 => isConnected:', this.isConnected, ' isConnecting:', this.isConnecting, ' reconnectCount:', this.reconnectCount)
    this.isConnected = false
    this.isConnecting = false

    // 停止心跳
    this.stopHeartbeat()

    // 触发断开事件
    this.emit('onDisconnect', res)

    // 尝试重连
    if (res.code !== 1000) { // 非正常关闭
      this.tryReconnect()
    }
  }

  /**
   * 发送认证消息
   */
  sendAuth() {
    const authMessage = {
      type: 'auth',
      data: {
        userId: this.userId,
        token: this.token,
        timestamp: Date.now()
      }
    }
    this.send(authMessage)
  }

  /**
   * 处理认证响应
   */
  handleAuthResponse(data) {
    if (data.success) {
      console.log('[WebSocket] 认证成功')
      // 可以在这里请求在线用户列表等
      this.requestOnlineUsers()
    } else {
      console.error('[WebSocket] 认证失败:', data.message)
      this.close()
    }
  }

  /**
   * 开始心跳检测
   */
  startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.sendHeartbeat()
      }
    }, this.config.heartbeatInterval)
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
    if (this.heartbeatTimeoutTimer) {
      clearTimeout(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }
  }

  /**
   * 发送心跳
   */
  sendHeartbeat() {
    const heartbeatMessage = {
      type: 'heartbeat',
      data: {
        userId: this.userId,
        timestamp: Date.now()
      }
    }
    this.send(heartbeatMessage)
    // 仅记录心跳发送，不再本地强制断开，避免自触发重连
  }

  /**
   * 处理心跳响应
   */
  handleHeartbeatResponse(data) {
    // 清除心跳超时定时器
    if (this.heartbeatTimeoutTimer) {
      clearTimeout(this.heartbeatTimeoutTimer)
      this.heartbeatTimeoutTimer = null
    }
    // console.log('[WebSocket] 心跳正常')
  }

  /**
   * 处理聊天消息
   */
  handleChatMessage(data) {
    console.log('[WebSocket] 收到聊天消息:', data)
    // 触发消息事件，由页面监听处理
  }

  /**
   * 处理在线状态变化
   */
  handleOnlineStatus(data) {
    console.log('[WebSocket] 在线状态变化:', data)
    
    const { userId, status, onlineUsers } = data.data || {}
    
    // 更新在线用户列表
    if (onlineUsers && Array.isArray(onlineUsers)) {
      this.onlineUsers = new Set(onlineUsers)
    } else if (userId) {
      // 单个用户状态变化
      if (status === 'online') {
        this.onlineUsers.add(userId)
      } else if (status === 'offline') {
        this.onlineUsers.delete(userId)
      }
    }
    
    // 触发在线状态事件
    this.emit('onOnlineStatus', {
      userId,
      status,
      onlineUsers: Array.from(this.onlineUsers)
    })
  }

  /**
   * 处理系统消息
   */
  handleSystemMessage(data) {
    console.log('[WebSocket] 系统消息:', data)
    // 可以显示系统通知等
  }

  /**
   * 请求在线用户列表
   */
  requestOnlineUsers() {
    const message = {
      type: 'get_online_users',
      data: {
        timestamp: Date.now()
      }
    }
    this.send(message)
  }

  /**
   * 发送消息
   */
  send(message) {
    if (!this.isConnected) {
      console.warn('[WebSocket] 未连接，消息已加入队列')
      this.messageQueue.push(message)
      return false
    }

    try {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      this.socket.send({
        data: data,
        success: () => {
          console.log('[WebSocket] 消息发送成功')
        },
        fail: (err) => {
          console.error('[WebSocket] 消息发送失败:', err)
          // 发送失败，加入队列
          this.messageQueue.push(message)
        }
      })
      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息异常:', error)
      this.messageQueue.push(message)
      return false
    }
  }

  /**
   * 发送聊天消息
   */
  sendChatMessage(toUserId, content, messageType = 'text', extra = {}) {
    const message = {
      type: 'message',
      data: {
        fromUserId: this.userId,
        toUserId: toUserId,
        messageType: messageType, // text, image, voice, video, location, etc.
        content: content,
        timestamp: Date.now(),
        ...extra
      }
    }
    return this.send(message)
  }

  /**
   * 刷新消息队列（发送缓存的消息）
   */
  flushMessageQueue() {
    if (this.messageQueue.length === 0) return

    console.log(`[WebSocket] 发送缓存消息，共 ${this.messageQueue.length} 条`)
    
    const queue = [...this.messageQueue]
    this.messageQueue = []
    
    queue.forEach(message => {
      this.send(message)
    })
  }

  /**
   * 尝试重连
   */
  tryReconnect() {
    if (this.reconnectCount >= this.config.reconnectMaxTimes) {
      console.error('[WebSocket] 达到最大重连次数，停止重连')
      this.emit('onError', { message: '连接失败，请稍后重试' })
      
      // 显示网络错误提示
      wx.showToast({
        title: '网络错误',
        icon: 'none',
        duration: 2000
      })
      
      return
    }

    if (this.reconnectTimer) {
      return
    }

    this.reconnectCount++
    console.log(`[WebSocket] 准备重连，第 ${this.reconnectCount} 次...`)
    
    this.emit('onReconnect', { count: this.reconnectCount })

    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, this.config.reconnectInterval)
  }

  /**
   * 关闭连接
   */
  close() {
    console.log('[WebSocket] 主动关闭连接')
    
    this.stopHeartbeat()
    
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.socket) {
      this.socket.close({
        code: 1000,
        reason: '正常关闭'
      })
      this.socket = null
    }

    this.isConnected = false
    this.isConnecting = false
    this.reconnectCount = 0
  }

  /**
   * 重置连接（完全重新开始）
   */
  reset() {
    this.close()
    this.messageQueue = []
    this.onlineUsers.clear()
  }

  /**
   * 错误处理
   */
  handleError(error) {
    console.error('[WebSocket] 错误:', error)
  }

  /**
   * 检查用户是否在线
   */
  isUserOnline(userId) {
    return this.onlineUsers.has(String(userId))
  }

  /**
   * 获取所有在线用户
   */
  getOnlineUsers() {
    return Array.from(this.onlineUsers)
  }

  /**
   * 添加事件监听
   */
  on(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event].push(callback)
    }
  }

  /**
   * 移除事件监听
   */
  off(event, callback) {
    if (this.listeners[event]) {
      const index = this.listeners[event].indexOf(callback)
      if (index > -1) {
        this.listeners[event].splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`[WebSocket] 事件回调执行失败 [${event}]:`, error)
        }
      })
    }
  }

  /**
   * 获取连接状态
   */
  getStatus() {
    return {
      isConnected: this.isConnected,
      isConnecting: this.isConnecting,
      reconnectCount: this.reconnectCount,
      messageQueueLength: this.messageQueue.length,
      onlineUserCount: this.onlineUsers.size
    }
  }
}

// 创建单例实例
const socketManager = new SocketManager()

module.exports = {
  socketManager,
  SocketManager
}

