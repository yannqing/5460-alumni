// pages/shop/detail/detail.js
Page({
  data: {
    shopId: '',
    shopInfo: null,
    loading: true
  },

  onLoad(options) {
    const { id } = options
    this.setData({ shopId: id })
    this.loadShopDetail()
  },

  loadShopDetail() {
    // TODO: 对接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/shop/detail`,
    //   data: { id: this.data.shopId },
    //   success: (res) => {
    //     this.setData({ shopInfo: res.data, loading: false })
    //   }
    // })

    // 模拟数据
    const mockData = {
      id: this.data.shopId,
      name: '校友咖啡厅',
      cover: '/assets/images/头像.png',
      owner: '王五',
      category: '餐饮',
      location: '上海市浦东新区世纪大道123号',
      distance: '800m',
      rating: 4.8,
      description: '温馨的校友聚会场所，提供优质咖啡和简餐。店内环境优雅，是校友们交流聚会的好去处。',
      phone: '021-12345678',
      wechat: 'alumni_cafe',
      businessHours: '周一至周日 08:00-22:00'
    }

    this.setData({
      shopInfo: mockData,
      loading: false
    })
  },

  makeCall(e) {
    const { phone } = e.currentTarget.dataset
    if (phone) {
      wx.makePhoneCall({
        phoneNumber: phone
      })
    }
  }
})

