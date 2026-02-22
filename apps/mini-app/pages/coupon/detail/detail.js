// pages/coupon/detail/detail.js
const { couponApi } = require('../../../api/api.js')

Page({
  data: {
    userCouponId: '',
    couponInfo: null,
    loading: true,
    qrCodeImage: '', // 二维码图片（base64解码后）
    formattedValidStartTime: '',
    formattedValidEndTime: '',
    formattedVerificationExpireTime: ''
  },

  onLoad(options) {
    // 从URL参数获取userCouponId
    const userCouponId = options.id || options.userCouponId
    if (!userCouponId) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    this.setData({ userCouponId })
    this.loadCouponDetail()
  },

  // 加载优惠券详情
  async loadCouponDetail() {
    try {
      this.setData({ loading: true })
      const res = await couponApi.getUserCouponDetail(this.data.userCouponId)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        // 处理base64二维码图片，后端返回的base64CodeImg已经是完整的data URI格式
        const qrCodeImage = data.base64CodeImg || ''
        
        // 格式化时间，去掉T
        const formattedValidStartTime = data.validStartTime ? data.validStartTime.replace('T', ' ') : ''
        const formattedValidEndTime = data.validEndTime ? data.validEndTime.replace('T', ' ') : ''
        const formattedVerificationExpireTime = data.verificationExpireTime ? data.verificationExpireTime.replace('T', ' ') : ''
        
        // 从 coupon 对象中提取 couponDesc 到顶层（如果存在）
        if (data.coupon && data.coupon.couponDesc) {
          data.couponDesc = data.coupon.couponDesc;
        }
        // 兼容驼峰和下划线命名
        if (data.couponDesc === undefined && data.coupon_desc !== undefined) {
          data.couponDesc = data.coupon_desc;
        }
        // 如果 coupon 对象中有 coupon_desc
        if (data.couponDesc === undefined && data.coupon && data.coupon.coupon_desc) {
          data.couponDesc = data.coupon.coupon_desc;
        }
        
        this.setData({
          couponInfo: data,
          qrCodeImage: qrCodeImage,
          formattedValidStartTime: formattedValidStartTime,
          formattedValidEndTime: formattedValidEndTime,
          formattedVerificationExpireTime: formattedVerificationExpireTime,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('加载优惠券详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 刷新核销码
  async refreshCode() {
    try {
      wx.showLoading({ title: '刷新中...' })
      const res = await couponApi.refreshCouponCode(this.data.userCouponId)
      
      wx.hideLoading()
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        // 处理base64二维码图片，后端返回的base64CodeImg已经是完整的data URI格式
        const qrCodeImage = data.base64CodeImg || ''
        
        // 格式化时间，去掉T
        const formattedValidStartTime = data.validStartTime ? data.validStartTime.replace('T', ' ') : ''
        const formattedValidEndTime = data.validEndTime ? data.validEndTime.replace('T', ' ') : ''
        const formattedVerificationExpireTime = data.verificationExpireTime ? data.verificationExpireTime.replace('T', ' ') : ''
        
        // 从 coupon 对象中提取 couponDesc 到顶层（如果存在）
        if (data.coupon && data.coupon.couponDesc) {
          data.couponDesc = data.coupon.couponDesc;
        }
        // 兼容驼峰和下划线命名
        if (data.couponDesc === undefined && data.coupon_desc !== undefined) {
          data.couponDesc = data.coupon_desc;
        }
        // 如果 coupon 对象中有 coupon_desc
        if (data.couponDesc === undefined && data.coupon && data.coupon.coupon_desc) {
          data.couponDesc = data.coupon.coupon_desc;
        }
        
        this.setData({
          couponInfo: data,
          qrCodeImage: qrCodeImage,
          formattedValidStartTime: formattedValidStartTime,
          formattedValidEndTime: formattedValidEndTime,
          formattedVerificationExpireTime: formattedVerificationExpireTime
        })
        
        wx.showToast({
          title: '刷新成功',
          icon: 'success'
      })
    } else {
            wx.showToast({
          title: res.data?.msg || '刷新失败',
          icon: 'none'
            })
          }
    } catch (error) {
      wx.hideLoading()
      console.error('刷新核销码失败:', error)
      wx.showToast({
        title: '刷新失败，请重试',
        icon: 'none'
      })
    }
  },

  callMerchant() {
    // 如果优惠券信息中有商家电话，可以调用
    const phone = this.data.couponInfo?.merchantPhone
    if (phone) {
    wx.makePhoneCall({
        phoneNumber: phone
    })
    }
  }
})
