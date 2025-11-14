// pages/benefit/list/list.js
Page({
  data: {
    associationId: '',
    benefitList: [],
    loading: false
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.loadBenefitList()
  },

  loadBenefitList() {
    this.setData({ loading: true })

    const mockData = [
      {
        id: 1,
        title: '星巴克校友专属优惠',
        merchant: '星巴克咖啡',
        discount: '8折',
        distance: '500m',
        isNew: true,
        expireDate: '2025-12-31',
        description: '凭校友身份可享受8折优惠，适用于所有饮品和食品。'
      },
      {
        id: 2,
        title: '海底捞校友专享',
        merchant: '海底捞火锅',
        discount: '9折',
        distance: '1.2km',
        isNew: false,
        expireDate: '2025-12-31',
        description: '校友专享9折优惠，需提前预约。'
      },
      {
        id: 3,
        title: '电影院校友优惠',
        merchant: 'XX影城',
        discount: '7折',
        distance: '800m',
        isNew: true,
        expireDate: '2025-12-31',
        description: '校友观影享受7折优惠，节假日除外。'
      }
    ]

    setTimeout(() => {
      this.setData({
        benefitList: mockData,
        loading: false
      })
    }, 500)
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/benefit/detail/detail?id=${id}`
    })
  }
})

