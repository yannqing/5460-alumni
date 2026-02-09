// components/custom-nav-bar/custom-nav-bar.js
const config = require('../../utils/config.js')

Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    showBack: {
      type: Boolean,
      value: true
    },
    backgroundColor: {
      type: String,
      value: '#fff'
    },
    textColor: {
      type: String,
      value: '#000'
    },
    /** 是否使用管理入口样式（渐变背景 + 中间 Logo） */
    useAuditStyle: {
      type: Boolean,
      value: false
    },
    /** 管理入口样式的标题文字 */
    auditTitle: {
      type: String,
      value: '管理'
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 0,
    iconBack: '',
    iconBackAudit: '',
    backgroundImage: '',
    centerLogo: ''
  },

  attached() {
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync()
    const statusBarHeight = systemInfo.statusBarHeight || 0
    const navBarHeight = 44 // 导航栏高度固定为44px
    
    // 获取返回按钮图标
    const iconBack = '/assets/icons/back.png'
    // 管理入口样式：返回按钮图标（使用本地图片）
    const iconBackAudit = '/assets/avatar/back7@2x.png'
    
    // 管理入口样式：中间 Logo（使用本地图片）
    const centerLogo = '/assets/avatar/dbdh@2x.png'
    // 默认样式：背景图
    const backgroundImage = config.getIconUrl('tljb@3x.png')
    
    this.setData({
      statusBarHeight: statusBarHeight,
      navBarHeight: navBarHeight,
      iconBack: iconBack,
      iconBackAudit: iconBackAudit,
      centerLogo: centerLogo,
      backgroundImage: backgroundImage
    })
  },

  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        wx.navigateBack()
      } else {
        // 如果没有上一页，跳转到首页
        wx.switchTab({
          url: '/pages/index/index'
        })
      }
    }
  }
})
