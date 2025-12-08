// app.js
import lib from './utils/lib.js'
import auth from './utils/auth'

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
    urlOpt: null
  },

  getSystemInfo() {
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res;
        this.globalData.statusBarHeight = res.statusBarHeight;
      }
    });
  },

  // 将 auth 模块的方法混入 App 实例
  ...auth,
  ...lib
})
