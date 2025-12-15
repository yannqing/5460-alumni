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
    socketManager: socketManager  // WebSocket 管理器实例
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
      wx.showToast({
        title: '消息服务已连接',
        icon: 'none',
        duration: 1500
      })
    })

    // 监听连接断开事件
    socketManager.on('onDisconnect', (data) => {
      console.log('[App] WebSocket 连接断开:', data)
    })

    // 监听连接错误事件
    socketManager.on('onError', (error) => {
      console.error('[App] WebSocket 错误:', error)
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
