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
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 0,
    iconBack: '',
    backgroundImage: ''
  },

  attached() {
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync()
    const statusBarHeight = systemInfo.statusBarHeight || 0
    const navBarHeight = 44 // 导航栏高度固定为44px
    
    // 获取返回按钮图标
    const iconBack = config.getIconUrl('back@3x.png')
    
    this.setData({
      statusBarHeight: statusBarHeight,
      navBarHeight: navBarHeight,
      iconBack: iconBack,
      backgroundImage: ''
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
