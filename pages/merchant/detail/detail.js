// pages/merchant/detail/detail.js
Page({
  data: {
    merchantId: '',
    merchantInfo: {
      id: 1,
      name: '星巴克咖啡',
      cover: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Starbucks',
      category: '餐饮美食',
      discount: '全场8折',
      description: '全球知名咖啡连锁品牌，为校友提供专属优惠',
      address: '上海市浦东新区世纪大道XXX号',
      phone: '021-12345678',
      businessHours: '09:00-22:00',
      tags: ['校友优惠', '连锁品牌', '咖啡茶饮']
    }
  },

  onLoad(options) {
    this.setData({ merchantId: options.id })
  },

  callMerchant() {
    wx.makePhoneCall({
      phoneNumber: this.data.merchantInfo.phone
    })
  }
})
