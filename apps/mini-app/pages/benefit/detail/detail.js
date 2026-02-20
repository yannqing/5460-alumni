// pages/benefit/detail/detail.js
Page({
  data: {
    benefitId: '',
    benefitInfo: null,
    loading: false
  },

  onLoad(options) {
    const { id } = options
    this.setData({ benefitId: id })
    this.loadBenefitDetail()
  },

  loadBenefitDetail() {
    this.setData({ loading: true })

    const mockData = {
      id: this.data.benefitId,
      title: '星巴克校友专属优惠',
      merchant: '星巴克咖啡',
      discount: '8折',
      distance: '500m',
      expireDate: '2025-12-31',
      description: '凭校友身份可享受8折优惠，适用于所有饮品和食品。',
      address: '上海市浦东新区世纪大道XXX号',
      phone: '021-12345678',
      rules: ['需出示校友身份证明', '不可与其他优惠同享', '仅限本人使用', '有效期至2025年12月31日']
    }

    setTimeout(() => {
      this.setData({
        benefitInfo: mockData,
        loading: false
      })
    }, 500)
  },

  makeCall() {
    wx.makePhoneCall({
      phoneNumber: this.data.benefitInfo.phone
    })
  }
})

