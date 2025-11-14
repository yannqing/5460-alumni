// pages/coupon/detail/detail.js
Page({
  data: {
    couponId: '',
    couponInfo: null
  },

  onLoad(options) {
    this.setData({ couponId: options.id })
    this.loadCouponDetail()
  },

  loadCouponDetail() {
    const mockData = {
      id: this.data.couponId,
      title: '星巴克100元优惠券',
      merchant: '星巴克咖啡',
      originalPrice: 100,
      discountPrice: 59,
      image: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Starbucks',
      stock: 50,
      totalStock: 100,
      startTime: '2025-11-13 10:00:00',
      endTime: '2025-11-20 23:59:59',
      type: 'rush',
      description: '全场通用，无门槛使用',
      rules: ['本券仅限线下门店使用', '不可与其他优惠同享', '不找零不兑现', '逾期作废'],
      merchantAddress: '上海市浦东新区世纪大道XXX号',
      merchantPhone: '021-12345678'
    }

    this.setData({ couponInfo: mockData })
  },

  handleCoupon() {
    const { couponInfo } = this.data

    if (couponInfo.type === 'rush') {
      wx.navigateTo({
        url: `/pages/coupon/rush/rush?id=${couponInfo.id}`
      })
    } else {
      wx.showModal({
        title: '提示',
        content: '确认领取该优惠券吗？',
        success: (res) => {
          if (res.confirm) {
            wx.showToast({
              title: '领取成功',
              icon: 'success'
            })
          }
        }
      })
    }
  },

  callMerchant() {
    wx.makePhoneCall({
      phoneNumber: this.data.couponInfo.merchantPhone
    })
  }
})
