// pages/profile/privacy/privacy.js
const app = getApp()

Page({
  data: {
    privacySettings: {
      showRealName: true,
      showPhone: false,
      showWxNum: false,
      showEmail: false,
      showLocation: true,
      showSchoolInfo: true,
      allowSearch: true,
      allowFootprint: true,
      allowMessage: true
    }
  },

  onLoad() {
    this.loadPrivacySettings()
  },

  onShow() {
    this.loadPrivacySettings()
  },

  // 加载隐私设置
  loadPrivacySettings() {
    // 从全局数据或本地存储获取隐私设置
    const userData = app.globalData.userData || {}
    const savedSettings = wx.getStorageSync('privacySettings') || {}
    
    // 合并设置，优先使用保存的设置
    const privacySettings = {
      showRealName: savedSettings.showRealName !== undefined ? savedSettings.showRealName : (userData.showRealName !== undefined ? userData.showRealName : true),
      showPhone: savedSettings.showPhone !== undefined ? savedSettings.showPhone : (userData.showPhone !== undefined ? userData.showPhone : false),
      showWxNum: savedSettings.showWxNum !== undefined ? savedSettings.showWxNum : (userData.showWxNum !== undefined ? userData.showWxNum : false),
      showEmail: savedSettings.showEmail !== undefined ? savedSettings.showEmail : (userData.showEmail !== undefined ? userData.showEmail : false),
      showLocation: savedSettings.showLocation !== undefined ? savedSettings.showLocation : (userData.showLocation !== undefined ? userData.showLocation : true),
      showSchoolInfo: savedSettings.showSchoolInfo !== undefined ? savedSettings.showSchoolInfo : (userData.showSchoolInfo !== undefined ? userData.showSchoolInfo : true),
      allowSearch: savedSettings.allowSearch !== undefined ? savedSettings.allowSearch : (userData.allowSearch !== undefined ? userData.allowSearch : true),
      allowFootprint: savedSettings.allowFootprint !== undefined ? savedSettings.allowFootprint : (userData.allowFootprint !== undefined ? userData.allowFootprint : true),
      allowMessage: savedSettings.allowMessage !== undefined ? savedSettings.allowMessage : (userData.allowMessage !== undefined ? userData.allowMessage : true)
    }

    this.setData({ privacySettings })
  },

  // 切换隐私设置
  togglePrivacy(e) {
    const { key } = e.currentTarget.dataset
    const value = e.detail.value
    
    this.setData({
      [`privacySettings.${key}`]: value
    })

    // 保存到本地存储
    const privacySettings = this.data.privacySettings
    wx.setStorageSync('privacySettings', privacySettings)

    // TODO: 调用后端接口保存隐私设置
    // 这里可以调用API保存到服务器
    // api.updatePrivacySettings(privacySettings).then(() => {
    //   wx.showToast({
    //     title: value ? '已开启' : '已关闭',
    //     icon: 'success'
    //   })
    // }).catch(err => {
    //   console.error('保存失败', err)
    //   wx.showToast({
    //     title: '保存失败',
    //     icon: 'none'
    //   })
    // })

    wx.showToast({
      title: value ? '已开启' : '已关闭',
      icon: 'success',
      duration: 1500
    })
  }
})


