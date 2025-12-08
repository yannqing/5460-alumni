// API配置文件 - 统一管理所有配置
const config = {
  // ==================== 环境配置 ====================
  // 开发环境
  dev: {
    baseUrl: 'http://localhost:8086',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: '/upload/images', // 图标路径前缀，如 'dev' 或 'dev/upload'，如果不需要前缀则留空
  },
  
  // 测试环境
  test: {
    baseUrl: 'http://222.191.253.58:8000',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: 'upload/images', // 图标路径前缀，如 'test' 或 'test/upload'，如果不需要前缀则留空
  },
  
  // 生产环境
  prod: {
    baseUrl: 'https://api.example.com',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: '/upload/images', // 图标路径前缀，如 'prod' 或 'prod/upload'，如果不需要前缀则留空
  },

  // ==================== 通用配置 ====================
  // 超时时间
  timeout: 10000,

  // 默认个人头像
  defaultAvatar: 'http://222.191.253.58:8000/upload/images/assets/images/avatar.png',

  // 默认母校头像
  defaultSchoolAvatar: 'http://222.191.253.58:8000/upload/images/assets/logo/njdx.jpg',

  // 默认校友会头像
  defaultAlumniAvatar: 'http://222.191.253.58:8000/upload/images/assets/logo/njdxxyh.jpg',
  
  // 默认背景图
  defaultCover: 'http://222.191.253.58:8000/upload/images/assets/images/njdxbjt.jpg',

  // 默认商品图
  defaultGoods: 'http://222.191.253.58:8000/upload/images/assets/images/bread.jpg',

  // ==================== 图标路径配置 ====================
  // 图标固定路径（assets/icons/ 这个路径不会变）
  iconFixedPath: 'assets/icons',

  /**
   * 获取图标完整路径
   * @param {string} iconName - 图标文件名，如 '母校.png' 或 'home.png'
   * @param {string} env - 可选，环境名称，如果不传则从本地存储读取
   * @returns {string} 完整的图标URL路径
   * @example
   * // 如果 test 环境的 iconPathPrefix 为空
   * config.getIconUrl('母校.png') // 返回: 'http://222.191.253.58:8000/images/assets/icons/母校.png'
   * 
   * // 如果 prod 环境的 iconPathPrefix 为 'prod'
   * config.getIconUrl('home.png', 'prod') // 返回: 'https://api.example.com/prod/images/assets/icons/home.png'
   * 
   * // 如果 dev 环境的 iconPathPrefix 为 'dev/upload'
   * config.getIconUrl('icon.png', 'dev') // 返回: 'http://localhost:8086/dev/upload/images/assets/icons/icon.png'
   */
  getIconUrl(iconName, env) {
    if (!iconName) {
      return ''
    }
    
    // 如果没有传入环境参数，从本地存储读取
    if (!env) {
      env = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    }
    
    const envConfig = this[env] || this.test // 默认使用测试环境
    const baseUrl = envConfig.baseUrl
    const iconPathPrefix = envConfig.iconPathPrefix || '' // 获取环境配置的图标路径前缀
    
    // 拼接完整路径
    // 如果有前缀：baseUrl + iconPathPrefix + iconFixedPath + iconName
    // 如果没有前缀：baseUrl + iconFixedPath + iconName
    if (iconPathPrefix) {
      return `${baseUrl}/${iconPathPrefix}/${this.iconFixedPath}/${iconName}`
    } else {
      return `${baseUrl}/${this.iconFixedPath}/${iconName}`
    }
  },

  // ==================== 获取当前环境配置 ====================
  /**
   * 获取当前环境的配置
   * @param {string} env - 环境名称：'dev' | 'test' | 'prod'，如果不传则从本地存储读取
   * @returns {object} 当前环境的配置对象
   */
  getEnvConfig(env) {
    // 如果没有传入环境参数，从本地存储读取
    if (!env) {
      env = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    }
    
    const envConfig = this[env] || this.test // 默认使用测试环境
    const apiPrefix = envConfig.apiPrefix || ''
    
    return {
      baseUrl: envConfig.baseUrl,
      apiPrefix: apiPrefix,
      // 完整的API基础地址（baseUrl + apiPrefix）
      apiBaseUrl: envConfig.baseUrl + apiPrefix,
    }
  }
}

module.exports = config
