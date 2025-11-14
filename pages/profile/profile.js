// pages/profile/profile.js
const app = getApp()

Page({
  data: {
    userInfo: null,
    isLogin: false,
    stats: {
      followSchools: 5,
      joinedAssociations: 3,
      followAlumni: 28,
      coupons: 12
    },
    menuItems: [
      { id: 1, icon: '🏫', name: '我的母校', url: '/pages/my-follow/my-follow?type=school' },
      { id: 2, icon: '👥', name: '我的校友会', url: '/pages/my-association/my-association' },
      { id: 3, icon: '⭐', name: '我的关注', url: '/pages/my-follow/my-follow?type=alumni' },
      { id: 4, icon: '🎫', name: '我的优惠券', url: '/pages/coupon/list/list?type=my' },
      { id: 5, icon: '💬', name: '我的圈子', url: '/pages/circle/list/list?type=my' },
      { id: 6, icon: '⚙️', name: '设置', url: '/pages/settings/settings' }
    ]
  },

  onLoad() {
    this.checkLogin()
  },

  onShow() {
    this.checkLogin()
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
    if (!this.data.isLogin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    if (url) {
      wx.navigateTo({ url })
    } else {
      wx.showToast({
        title: '功能开发中',
        icon: 'none'
      })
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
  }
})
