// pages/merchant/my-merchant/my-merchant.js
const { merchantApi } = require('../../../api/api.js')

Page({
  data: {
    loading: false,
    merchantList: []
  },

  onLoad() {
    this.loadMerchants()
  },

  onShow() {
    this.loadMerchants()
  },

  loadMerchants() {
    this.setData({ loading: true })
    
    merchantApi.getMyMerchants({
      current: 1,
      size: 100
    }).then(res => {
      if (res && res.data && res.data.code === 200) {
        const merchantList = res.data.data?.records || []
        this.setData({
          merchantList: merchantList
        })
      }
    }).catch(err => {
      console.error('获取商户列表失败:', err)
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  goToApplyMerchant() {
    wx.navigateTo({
      url: '/pages/merchant/apply/apply'
    })
  }
})
