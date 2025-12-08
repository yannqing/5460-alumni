// pages/promotion/detail/detail.js
const config = require('../../../utils/config.js')

Page({
  data: {
    promotionId: '',
    promotionInfo: null,
    loading: true
  },

  onLoad(options) {
    const { id = '' } = options
    this.setData({ promotionId: id })
    this.fetchPromotionDetail()
  },

  fetchPromotionDetail() {
    this.setData({ loading: true })

    // TODO: 接入真实接口，返回对应的地方校促会详情
    const mock = {
      id: this.data.promotionId || 'promotion-001',
      name: '上海市校友企业促进会',
      city: '上海市',
      coverage: '长三角地区校企合作与校友资源对接',
      icon: config.defaultSchoolAvatar,
      cover: config.defaultCover,
      description: '致力于联动各地校友企业资源，帮助校友项目落地，提供产业交流平台。',
      contactPhone: '021-8888 1234',
      contactEmail: 'support@shxch.org',
      address: '上海市浦东新区张江科创大道88号'
    }

    this.setData({
      promotionInfo: mock,
      loading: false
    })
  },

  handleCall() {
    const { promotionInfo } = this.data
    if (!promotionInfo?.contactPhone) return
    wx.makePhoneCall({
      phoneNumber: promotionInfo.contactPhone
    })
  },

  copyValue(e) {
    const value = e.currentTarget.dataset.value
    if (!value) return
    wx.setClipboardData({
      data: value,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' })
      }
    })
  }
})

