// pages/profile/profile.js
const app = getApp()
const { userApi, followApi } = require('../../api/api.js')

Page({
  data: {
    userInfo: null,
    isLogin: false,
    certificationStatus: 'none', // none: 未认证, pending: 认证中, verified: 已认证
    stats: {
      coupons: 0,
      followedShops: 0,
      footprints: 0
    },
    couponTab: 'unused', // unused, used, expired
    couponStats: {
      unused: 0,
      used: 0,
      expired: 0
    },
    privacySettings: {
      allowSearch: true,
      allowFootprint: true
    },
    showCardModal: false,
    alumniCardQrcode: '',
    alumniCardNumber: '',
    // 关注和粉丝统计
    followStats: {
      followingCount: 0,
      fansCount: 0
    }
  },

  onLoad() {
    this.loadUserInfo()
  },

  onShow() {
    // 每次显示页面时都重新加载用户信息，确保数据实时更新
    this.loadUserInfo()
    this.loadUserData()
  },

  onPullDownRefresh() {
    this.loadUserData()
    wx.stopPullDownRefresh()
  },

  checkLogin() {
    // 静默登录，始终从全局数据获取用户信息
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || userData
    
    // 格式化用户信息
    const formattedUserInfo = {
      nickname: userInfo.nickname || '用户',
      avatarUrl: userInfo.avatarUrl || '',
      school: userInfo.school || userInfo.schoolName || '',
      major: userInfo.major || '',
      graduateYear: userInfo.graduateYear || userInfo.enrollYear || ''
    }
    
    this.setData({
      userInfo: formattedUserInfo,
      isLogin: true // 静默登录始终为已登录状态
    })
  },

  // 加载用户信息（从全局数据读取最新值，如果为空则从后端获取）
  async loadUserInfo() {
    // 从全局数据获取用户信息，优先使用 userInfo，其次使用 userData
    let userData = app.globalData.userData || {}
    let userInfo = app.globalData.userInfo || userData
    
    // 如果全局数据为空或没有关键字段（nickname），尝试从后端重新获取
    // 注意：登录接口可能只返回 token 和 roles，不包含用户详细信息
    const hasUserInfo = userData && userData.nickname
    if (!hasUserInfo) {
      console.log('用户详细信息缺失，尝试从后端获取...')
      try {
        // 检查是否已登录
        const isLogin = app.checkHasLogined()
        if (isLogin) {
          // 从后端获取用户详细信息
          try {
            const res = await userApi.getUserInfo()
            console.log('getUserInfo 接口返回:', res)
            
            // 检查返回数据结构：res.data.code === 200 且 res.data.data 包含用户信息
            if (res && res.data && res.data.code === 200 && res.data.data) {
              // 更新全局数据，保留原有的 token 和 roles
              const userInfoData = res.data.data
              app.globalData.userData = {
                ...(app.globalData.userData || {}),
                ...userInfoData
              }
              app.globalData.userInfo = {
                ...(app.globalData.userInfo || {}),
                ...userInfoData
              }
              userData = app.globalData.userData
              userInfo = app.globalData.userInfo
              console.log('从后端获取用户信息成功:', userData)
            } else if (res && res.code === 200 && res.data) {
              // 兼容另一种数据结构
              app.globalData.userData = {
                ...(app.globalData.userData || {}),
                ...res.data
              }
              app.globalData.userInfo = {
                ...(app.globalData.userInfo || {}),
                ...res.data
              }
              userData = app.globalData.userData
              userInfo = app.globalData.userInfo
              console.log('从后端获取用户信息成功（兼容格式）:', userData)
            } else {
              console.warn('从后端获取用户信息失败，返回数据格式不正确:', res)
              console.warn('返回数据结构:', {
                hasRes: !!res,
                hasData: !!(res && res.data),
                code: res && res.data ? res.data.code : 'N/A',
                hasDataData: !!(res && res.data && res.data.data)
              })
            }
          } catch (error) {
            console.error('调用 getUserInfo 接口失败:', error)
            console.error('错误详情:', error.message || error)
          }
        } else {
          // 未登录，尝试初始化登录
          console.log('未登录，尝试初始化登录...')
          try {
            await app.initApp()
            userData = app.globalData.userData || {}
            userInfo = app.globalData.userInfo || userData
            console.log('登录初始化后的用户数据:', userData)
            
            // 登录后如果还是没有用户详细信息，再次尝试获取
            if (!userData.nickname) {
              console.log('登录后仍无用户详细信息，再次尝试获取...')
              try {
                const res = await userApi.getUserInfo()
                if (res && res.data && res.data.code === 200 && res.data.data) {
                  const userInfoData = res.data.data
                  app.globalData.userData = {
                    ...(app.globalData.userData || {}),
                    ...userInfoData
                  }
                  app.globalData.userInfo = {
                    ...(app.globalData.userInfo || {}),
                    ...userInfoData
                  }
                  userData = app.globalData.userData
                  userInfo = app.globalData.userInfo
                  console.log('登录后获取用户信息成功:', userData)
                }
              } catch (error) {
                console.error('登录后获取用户信息失败:', error)
              }
            }
          } catch (error) {
            console.error('登录初始化失败:', error)
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
    
    // 兼容多种头像字段名：avatarUrl, avatar, headImg
    const rawAvatarUrl = userInfo.avatarUrl || userInfo.avatar || userInfo.headImg || userData.avatarUrl || userData.avatar || userData.headImg || ''
    
    // 使用 config.getImageUrl 处理图片URL，确保使用正确的 baseUrl
    const config = require('../../utils/config.js')
    const avatarUrl = rawAvatarUrl ? config.getImageUrl(rawAvatarUrl) : ''
    
    // 调试信息（开发时使用）
    console.log('加载用户信息 - userData:', userData)
    console.log('加载用户信息 - userInfo:', userInfo)
    console.log('加载用户信息 - rawAvatarUrl:', rawAvatarUrl)
    console.log('加载用户信息 - avatarUrl (处理后):', avatarUrl)
    console.log('加载用户信息 - nickname:', userInfo.nickname || userData.nickname)
    
    // 格式化用户信息
    const formattedUserInfo = {
      nickname: userInfo.nickname || userData.nickname || '用户',
      avatarUrl: avatarUrl,
      school: userInfo.school || userInfo.schoolName || userData.school || userData.schoolName || '',
      major: userInfo.major || userData.major || '',
      graduateYear: userInfo.graduateYear || userInfo.enrollYear || userData.graduateYear || userData.enrollYear || ''
    }
    
    this.setData({
      userInfo: formattedUserInfo,
      isLogin: true
    })
  },

  loadUserData() {
    // 从全局数据获取用户信息
    const userData = app.globalData.userData || {}

    // 设置认证状态（从后端数据获取）
    const certificationStatus = userData.certificationStatus || userData.is_apply_acard === 1 ? 'verified' : 'none'

    // 设置统计数据（从后端接口获取，这里先设为0，后续对接接口）
    this.setData({
      certificationStatus,
      stats: {
        coupons: userData.couponCount || 0,
        followedShops: userData.followedShopCount || 0,
        footprints: userData.footprintCount || 0
      },
      couponStats: {
        unused: userData.unusedCouponCount || 0,
        used: userData.usedCouponCount || 0,
        expired: userData.expiredCouponCount || 0
      },
      alumniCardQrcode: userData.alumniCardQrcode || '',
      alumniCardNumber: userData.alumniCardNumber || ''
    })

    // 加载关注和粉丝统计
    this.loadFollowStats()
  },

  // 加载关注和粉丝统计
  async loadFollowStats() {
    try {
      const res = await followApi.getCurrentUserStats()
      console.log('关注统计接口返回:', res)

      if (res.data && res.data.code === 200) {
        const stats = res.data.data || {}
        this.setData({
          followStats: {
            followingCount: stats.followingCount || 0,
            fansCount: stats.fansCount || stats.followerCount || 0
          }
        })
      }
    } catch (error) {
      console.error('加载关注统计失败:', error)
    }
  },

  // 跳转到我的关注页面
  goToMyFollow() {
    wx.navigateTo({
      url: '/pages/my-follow/my-follow?tab=follow'
    })
  },

  // 跳转到我的粉丝页面
  goToMyFans() {
    wx.navigateTo({
      url: '/pages/my-follow/my-follow?tab=fans'
    })
  },


  navigateTo(e) {
    const { url } = e.currentTarget.dataset
    if (!url) return
    
    wx.navigateTo({ url })
  },

  editProfile() {
    wx.navigateTo({
      url: '/pages/profile/edit/edit'
    })
  },

  // 去认证
  goToCertification() {
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
    wx.navigateTo({
      url: '/pages/merchant/apply/apply'
    })
  }
})
