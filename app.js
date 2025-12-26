// app.js
import lib from './utils/lib.js'
import auth from './utils/auth'
const { socketManager } = require('./utils/socketManager.js')

App({
  // 全局数据
  kVersionNum: '1.0',

  async onLaunch(opt) {
    this.globalData.urlOpt = opt

    // 从配置文件读取环境配置
    const config = require('./utils/config.js')
    
    // 完全手动切换环境，从本地存储读取
    const manualEnv = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    
    // 获取当前环境的配置
    const envConfig = config.getEnvConfig(manualEnv)
    
    console.log('=== 环境配置（手动切换） ===')
    console.log('手动设置的环境:', manualEnv)
    console.log('当前API地址:', envConfig.apiBaseUrl)
    
    // 设置全局配置（完整的API基础地址，包含前缀）
    this.globalData.baseUrl = envConfig.apiBaseUrl

    // 获取系统信息
    this.getSystemInfo();

    // 获取用户位置信息
    this.getUserLocation();

    // 自动初始化登录
    try {
      await this.initApp()
      console.log('登录初始化成功')
      
      // 登录成功后初始化 WebSocket
      // 延迟一点时间确保用户数据已经加载
      setTimeout(() => {
        this.initWebSocket()
      }, 1000)
    } catch (error) {
      console.error('登录初始化失败:', error)
    }
  },

  onShow(options) {
    // 如果有邀请人参数，触发登录
    if (options.query && options.query.scene) {
      this.login(options.query.scene)
    }
  },

  userInfo: {
    token: ''
  },

  globalData: {
    baseUrl: '',
    apploaded: false,
    userData: {},      // 存储用户数据
    userConfig: {
      roles: {},
      is_apply_acard: 0
    },
    userInfo: null,
    token: '',
    systemInfo: null,
    statusBarHeight: 0,
    urlOpt: null,
    socketManager: socketManager,  // WebSocket 管理器实例
    location: null,  // 存储用户位置信息
    isShowingMessageNotification: false  // 标记是否正在显示消息提示
  },

  getSystemInfo() {
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res;
        this.globalData.statusBarHeight = res.statusBarHeight;
      }
    });
  },

  /**
   * 获取用户位置信息（小程序启动时调用）
   */
  getUserLocation() {
    wx.getLocation({
      type: 'gcj02', // 返回可以用于wx.openLocation的经纬度
      altitude: false, // 传入 true 会返回高度信息，由于获取高度需要较高精度，会减慢接口返回速度
      success: (res) => {
        console.log('[App] 获取到用户位置:', res.latitude, res.longitude)
        this.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        }
      },
      fail: (err) => {
        console.error('[App] 获取位置失败:', err)
        // 获取位置失败，不设置默认值
        this.globalData.location = null
      }
    })
  },

  /**
   * 初始化 WebSocket 连接
   */
  initWebSocket() {
    // 优先使用内存中的 token / userId；其次使用本地缓存
    const token = this.globalData.token || wx.getStorageSync('token')
    const userId =
      this.globalData.userData?.wxId ||
      this.globalData.userData?.userId ||
      wx.getStorageSync('userId')
    
    if (!token || !userId) {
      console.log('[App] WebSocket 初始化失败：缺少 token 或 userId')
      console.log('[App] 调试信息:', {
        hasGlobalToken: !!this.globalData.token,
        hasStorageToken: !!wx.getStorageSync('token'),
        hasGlobalUserId: !!this.globalData.userData?.wxId,
        hasStorageUserId: !!wx.getStorageSync('userId')
      })
      return
    }

    // 获取当前环境的 WebSocket 配置
    const config = require('./utils/config.js')
    const envConfig = config.getEnvConfig()
    const wsUrl = envConfig.wsUrl

    console.log('[App] 初始化 WebSocket:', { 
      wsUrl, 
      userId,
      hasToken: !!token,
      tokenLength: token ? token.length : 0
    })

    // 初始化 WebSocket 管理器
    socketManager.init(wsUrl, userId, token, {
      heartbeatInterval: 30000,   // 30秒心跳
      reconnectInterval: 3000,    // 3秒重连间隔
      reconnectMaxTimes: 5        // 最多重连5次
    })

    // 连接 WebSocket
    socketManager.connect()

    // 监听连接成功事件
    socketManager.on('onConnect', (data) => {
      console.log('[App] WebSocket 连接成功:', data)
      // 连接成功时不显示提示（已注释）
      // wx.showToast({
      //   title: '消息服务已连接',
      //   icon: 'none',
      //   duration: 1500
      // })
    })

    // 监听连接断开事件
    socketManager.on('onDisconnect', (data) => {
      console.log('[App] WebSocket 连接断开:', data)
    })

    // 监听连接错误事件
    socketManager.on('onError', (error) => {
      console.error('[App] WebSocket 错误:', error)
      // 网络错误提示已在 socketManager 中显示，这里不需要重复显示
    })

    // 监听重连事件
    socketManager.on('onReconnect', (data) => {
      console.log('[App] WebSocket 重连中...', data)
    })

    // 监听在线状态变化
    socketManager.on('onOnlineStatus', (data) => {
      console.log('[App] 在线状态变化:', data)
      // 可以在这里更新全局在线用户列表
    })

    // 监听全局消息（用于显示消息提示弹窗）
    socketManager.on('onMessage', (data) => {
      if (data.type === 'message') {
        this.handleGlobalMessage(data)
      }
    })
  },

  /**
   * 处理全局消息（显示消息提示弹窗）
   */
  async handleGlobalMessage(data) {
    try {
      // 如果正在显示消息提示，则忽略新消息（避免重复弹窗）
      if (this.globalData.isShowingMessageNotification) {
        console.log('[App] 正在显示消息提示，忽略新消息')
        return
      }

      const messageData = data.data || {}
      const { fromUserId, content, messageType } = messageData

      // 获取当前用户ID，排除自己发送的消息
      const myUserId = this.globalData.userData?.wxId || wx.getStorageSync('userId')
      if (String(fromUserId) === String(myUserId)) {
        console.log('[App] 是自己发送的消息，不显示提示')
        return
      }

      // 获取当前页面信息
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const currentRoute = currentPage ? currentPage.route : ''

      // 如果当前在聊天详情页，且是当前聊天的消息，则不显示弹窗
      if (currentRoute === 'pages/chat/detail/detail') {
        const chatId = currentPage.data?.chatId
        if (chatId && String(fromUserId) === String(chatId)) {
          console.log('[App] 当前在聊天详情页，不显示消息提示')
          return
        }
      }

      // 获取发送者信息
      let senderName = '未知用户'
      let senderAvatar = ''
      
      try {
        const { alumniApi } = require('./api/api.js')
        const res = await alumniApi.getAlumniInfo(fromUserId)
        if (res.data && res.data.code === 200 && res.data.data) {
          const userInfo = res.data.data
          senderName = userInfo.name || userInfo.nickname || '未知用户'
          const config = require('./utils/config.js')
          senderAvatar = userInfo.avatarUrl ? config.getImageUrl(userInfo.avatarUrl) : ''
        }
      } catch (error) {
        console.error('[App] 获取发送者信息失败:', error)
      }

      // 格式化消息内容（图片消息显示特殊提示）
      let messageContent = content
      if (messageType === 'image') {
        messageContent = '[图片]'
      } else if (messageType === 'location') {
        messageContent = '[位置]'
      } else if (messageType === 'contact') {
        messageContent = '[名片]'
      } else if (!messageContent || messageContent.trim() === '') {
        messageContent = '[消息]'
      }

      // 限制消息内容长度
      if (messageContent.length > 30) {
        messageContent = messageContent.substring(0, 30) + '...'
      }

      // 显示消息提示弹窗
      this.showMessageNotification({
        senderId: fromUserId,
        senderName: senderName,
        senderAvatar: senderAvatar,
        messageContent: messageContent
      })
    } catch (error) {
      console.error('[App] 处理全局消息失败:', error)
    }
  },

  /**
   * 显示消息通知弹窗
   */
  showMessageNotification({ senderId, senderName, senderAvatar, messageContent }) {
    // 标记正在显示消息提示
    this.globalData.isShowingMessageNotification = true

    // 使用自定义弹窗显示消息提示
    wx.showModal({
      title: senderName,
      content: messageContent,
      showCancel: true,
      cancelText: '忽略',
      confirmText: '查看',
      confirmColor: '#4a90e2',
      success: (res) => {
        // 重置标记
        this.globalData.isShowingMessageNotification = false
        
        if (res.confirm) {
          // 点击"查看"，跳转到聊天详情页
          wx.navigateTo({
            url: `/pages/chat/detail/detail?id=${senderId}&type=chat&name=${encodeURIComponent(senderName)}&avatar=${encodeURIComponent(senderAvatar || '')}`
          })
        }
      },
      fail: () => {
        // 重置标记
        this.globalData.isShowingMessageNotification = false
      }
    })
  },

  /**
   * 关闭 WebSocket 连接
   */
  closeWebSocket() {
    console.log('[App] 关闭 WebSocket 连接')
    socketManager.close()
  },

  // 将 auth 模块的方法混入 App 实例
  ...auth,
  ...lib
})
