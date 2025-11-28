// pages/profile/profile.js
const app = getApp()

Page({
  data: {
    userInfo: null,
    isLogin: false,
    certificationStatus: 'none', // none: 未认证, pending: 认证中, verified: 已认证
    stats: {
      coupons: 12,
      followedShops: 5,
      footprints: 28
    },
    couponTab: 'unused', // unused, used, expired
    couponStats: {
      unused: 8,
      used: 3,
      expired: 1
    },
    privacySettings: {
      allowSearch: true,
      allowFootprint: true
    },
    showCardModal: false,
    alumniCardQrcode: '/assets/images/头像.png', // TODO: 替换为真实二维码
    alumniCardNumber: 'AL202500123456'
  },

  onLoad() {
    this.checkLogin()
  },

  onShow() {
    this.checkLogin()
    this.loadUserData()
  },

  onPullDownRefresh() {
    this.loadUserData()
    wx.stopPullDownRefresh()
  },

  checkLogin() {
    const userInfo = app.globalData.userInfo
    if (userInfo) {
      this.setData({
        userInfo,
        isLogin: true
      })
    } else {
      // 模拟登录用户数据
      const mockUser = {
        nickName: '张三',
        avatarUrl: '/assets/images/头像.png',
        school: '南京大学',
        major: '计算机科学',
        graduateYear: 2015
      }
      this.setData({
        userInfo: mockUser,
        isLogin: true
      })
    }
  },

  loadUserData() {
    // TODO: 对接后端接口获取用户数据
    // 模拟数据
    this.setData({
      certificationStatus: 'none', // 可以改为 'pending' 或 'verified' 测试不同状态
      stats: {
        coupons: 12,
        followedShops: 5,
        footprints: 28
      },
      couponStats: {
        unused: 8,
        used: 3,
        expired: 1
      }
    })
  },

  handleLogin() {
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (res) => {
        const userInfo = res.userInfo
        this.setData({
          userInfo,
          isLogin: true
        })
        app.setUserInfo(userInfo)
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        })
      },
      fail: () => {
        wx.showToast({
          title: '登录失败',
          icon: 'none'
        })
      }
    })
  },

  navigateTo(e) {
    const { url } = e.currentTarget.dataset
    if (!url) return
    
    if (!this.data.isLogin && url.includes('type=my')) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    if (url) {
      wx.navigateTo({ url })
    }
  },

  editProfile() {
    if (!this.data.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: '/pages/profile/edit/edit'
    })
  },

  // 去认证
  goToCertification() {
    if (!this.data.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: '/pages/certification/certification'
    })
  },

  // 切换优惠券标签
  switchCouponTab(e) {
    const { tab } = e.currentTarget.dataset
    this.setData({
      couponTab: tab
    })
  },

  // 显示校友卡
  showAlumniCard() {
    if (this.data.certificationStatus !== 'verified') {
      return
    }
    this.setData({
      showCardModal: true
    })
  },

  // 隐藏校友卡
  hideAlumniCard() {
    this.setData({
      showCardModal: false
    })
  },

  // 切换隐私设置
  togglePrivacy(e) {
    const { key } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({
      [`privacySettings.${key}`]: value
    })
    
    // TODO: 调用后端接口保存隐私设置
    wx.showToast({
      title: value ? '已开启' : '已关闭',
      icon: 'success'
    })
  },

  // 申请商家
  applyMerchant() {
    if (!this.data.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: '/pages/merchant/apply/apply'
    })
  }
})
