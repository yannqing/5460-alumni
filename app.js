// app.js
import lib from './utils/lib.js'
import auth from './utils/auth'

App({
  // 全局数据
  kVersionNum: '1.0',

  async onLaunch(opt) {
    this.globalData.urlOpt = opt

    // 根据环境配置 baseUrl（完全手动切换，不自动判断）
    // 开发环境
    const DEV_BASE_URL = 'http://localhost:8086' // 开发环境API地址
    // 测试环境
    const TEST_BASE_URL = 'http://222.191.253.58:8000' // 测试环境API地址
    // 生产环境
    const PROD_BASE_URL = 'https://api.example.com' // 生产环境API地址
    
    // 完全手动切换环境，从本地存储读取
    const manualEnv = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    
    console.log('=== 环境配置（手动切换） ===')
    console.log('手动设置的环境:', manualEnv)
    
    switch (manualEnv) {
      case 'dev':
        this.globalData.baseUrl = DEV_BASE_URL
        console.log('使用开发环境')
        break;
      case 'test':
        this.globalData.baseUrl = TEST_BASE_URL
        console.log('使用测试环境')
        break;
      case 'prod':
        this.globalData.baseUrl = PROD_BASE_URL
        console.log('使用生产环境')
        break;
      default:
        // 默认使用测试环境
        this.globalData.baseUrl = DEV_BASE_URL
        console.log('使用默认环境（测试环境）')
        break;
    }
    
    console.log('当前API地址:', this.globalData.baseUrl)

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
