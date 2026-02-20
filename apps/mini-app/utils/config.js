// API配置文件 - 统一管理所有配置
const config = {
  // ==================== 环境配置 ====================
  // 开发环境
  dev: {
    baseUrl: 'http://localhost:8086',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: '/upload/images', // 图标路径前缀，如 'dev' 或 'dev/upload'，如果不需要前缀则留空
    wsUrl: 'wss://${API_DOMAIN}/ws', // WebSocket 地址
  },
  
  // 测试环境
  test: {
    baseUrl: 'https://${API_DOMAIN}',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: 'upload/images', // 图标路径前缀，如 'test' 或 'test/upload'，如果不需要前缀则留空
    wsUrl: 'wss://${API_DOMAIN}/ws', // WebSocket 地址
  },
  
  // 生产环境
  prod: {
    baseUrl: 'https://api.example.com',
    apiPrefix: '', // API前缀路径，如 '/api/v1'，如果不需要前缀则留空
    iconPathPrefix: '/upload/images', // 图标路径前缀，如 'prod' 或 'prod/upload'，如果不需要前缀则留空
    wsUrl: 'wss://api.example.com/ws', // WebSocket 地址（生产环境使用 wss 加密）
  },

  // ==================== 通用配置 ====================
  // 超时时间
  timeout: 10000,

  // 默认个人头像
  defaultAvatar: '/assets/avatar/avatar.jpg',

  // 默认母校头像
  defaultSchoolAvatar: 'https://${API_DOMAIN}/upload/images/assets/logo/njdx.jpg',

  // 默认校友会头像
  defaultAlumniAvatar: 'https://${API_DOMAIN}/upload/images/assets/logo/njdxxyh.jpg',
  
  // 默认背景图
  defaultCover: 'https://${API_DOMAIN}/upload/images/assets/images/njdxbjt.jpg',

  // 默认商品图
  defaultGoods: 'https://${API_DOMAIN}/upload/images/assets/images/bread.jpg',

  // 默认个人页背景图
  defaultGoods: 'https://${API_DOMAIN}/upload/images/assets/images/person_bg.jpg',

  // 默认空状态图片（使用 data URI，避免网络请求）
  defaultEmptyImage: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5OTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5pqC5peg5pWw5o2uPC90ZXh0Pjwvc3ZnPg==',

  // ==================== 图标路径配置 ====================
  // 图标固定路径（assets/icons/ 这个路径不会变）
  iconFixedPath: 'assets/icons',
  
  // 图片固定路径（assets/images/ 这个路径不会变）
  imageFixedPath: 'assets/images',

  /**
   * 获取图标完整路径
   * @param {string} iconName - 图标文件名，如 '母校.png' 或 'home.png'
   * @param {string} env - 可选，环境名称，如果不传则从本地存储读取
   * @returns {string} 完整的图标URL路径
   * @example
   * // 如果 test 环境的 iconPathPrefix 为空
   * config.getIconUrl('母校.png') // 返回: 'http://127.0.0.1:8000/images/assets/icons/母校.png'
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

  /**
   * 获取 assets/images 文件夹下的图片完整路径
   * @param {string} imageName - 图片文件名，如 'grjrt@2x.png' 或 'grdbt@2x.png'
   * @param {string} env - 可选，环境名称，如果不传则从本地存储读取
   * @returns {string} 完整的图片URL路径
   * @example
   * config.getAssetImageUrl('grjrt@2x.png') // 返回: 'https://${API_DOMAIN}/upload/images/assets/images/grjrt@2x.png'
   */
  getAssetImageUrl(imageName, env) {
    if (!imageName) {
      return ''
    }
    
    // 如果没有传入环境参数，从本地存储读取
    if (!env) {
      env = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    }
    
    const envConfig = this[env] || this.test // 默认使用测试环境
    const baseUrl = envConfig.baseUrl
    const iconPathPrefix = envConfig.iconPathPrefix || '' // 获取环境配置的图标路径前缀
    
    // 拼接完整路径：baseUrl + iconPathPrefix + imageFixedPath + imageName
    if (iconPathPrefix) {
      return `${baseUrl}/${iconPathPrefix}/${this.imageFixedPath}/${imageName}`
    } else {
      return `${baseUrl}/${this.imageFixedPath}/${imageName}`
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
      // WebSocket 地址
      wsUrl: envConfig.wsUrl || 'wss://${API_DOMAIN}/ws',
    }
  },

  // ==================== 图片URL处理 ====================
  /**
   * 处理图片URL，确保返回完整的URL
   * 如果传入的是相对路径，会拼接当前环境的 baseUrl
   * 如果传入的是完整URL，会检查并修正为当前环境的 baseUrl
   * @param {string} imageUrl - 图片URL（可能是相对路径或完整URL）
   * @param {string} env - 可选，环境名称，如果不传则从本地存储读取
   * @returns {string} 完整的图片URL
   * @example
   * // 相对路径
   * config.getImageUrl('/upload/images/avatar.png') // 返回: 'https://${API_DOMAIN}/upload/images/avatar.png'
   * 
   * // 完整URL（旧地址）
   * config.getImageUrl('http://127.0.0.1:8000/upload/images/avatar.png') // 返回: 'https://${API_DOMAIN}/upload/images/avatar.png'
   * 
   * // 完整URL（当前环境）
   * config.getImageUrl('https://${API_DOMAIN}/upload/images/avatar.png') // 返回: 'https://${API_DOMAIN}/upload/images/avatar.png'
   */
  getImageUrl(imageUrl, env) {
    if (!imageUrl || typeof imageUrl !== 'string') {
      return ''
    }

    // 如果没有传入环境参数，从本地存储读取
    if (!env) {
      env = wx.getStorageSync('manual_env') || 'test' // 默认使用测试环境
    }
    
    const envConfig = this[env] || this.test // 默认使用测试环境
    const baseUrl = envConfig.baseUrl

    // 如果已经是完整URL（以 http:// 或 https:// 开头）
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      // 检查是否是旧地址，如果是则替换为当前环境的 baseUrl
      const oldBaseUrls = [
        'http://127.0.0.1:8000',
        'https://127.0.0.1:8000',
        'http://localhost:8086'
      ]
      
      for (const oldBaseUrl of oldBaseUrls) {
        if (imageUrl.startsWith(oldBaseUrl)) {
          // 提取路径部分（去掉旧 baseUrl）
          const path = imageUrl.replace(oldBaseUrl, '')
          // 拼接新的 baseUrl
          return baseUrl + path
        }
      }
      
      // 如果已经是当前环境的 baseUrl，直接返回
      if (imageUrl.startsWith(baseUrl)) {
        return imageUrl
      }
      
      // 其他完整URL，直接返回（可能是外部图片）
      return imageUrl
    }

    // 相对路径，拼接 baseUrl
    // 确保路径以 / 开头
    const path = imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl
    return baseUrl + path
  }
}

module.exports = config
