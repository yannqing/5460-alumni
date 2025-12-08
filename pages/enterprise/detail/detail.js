// pages/enterprise/detail/detail.js
const config = require('../../../utils/config.js')

Page({
  data: {
    enterpriseId: '',
    enterpriseInfo: null,
    loading: true
  },

  onLoad(options) {
    const { id } = options
    this.setData({ enterpriseId: id })
    this.loadEnterpriseDetail()
  },

  loadEnterpriseDetail() {
    // TODO: 对接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/enterprise/detail`,
    //   data: { id: this.data.enterpriseId },
    //   success: (res) => {
    //     this.setData({ enterpriseInfo: res.data, loading: false })
    //   }
    // })

    // 模拟数据
    const mockData = {
      id: this.data.enterpriseId,
      name: '腾讯科技',
      logo: config.defaultAvatar,
      industry: '互联网科技',
      founder: '张三',
      description: '腾讯科技是一家专注于互联网科技的公司，致力于为用户提供优质的产品和服务。公司成立于2000年，经过多年的发展，已经成为行业内的领先企业。',
      location: '深圳市南山区科技园',
      scale: '1000-5000人',
      website: 'https://www.tencent.com',
      phone: '0755-86013388',
      email: 'contact@tencent.com'
    }

    this.setData({
      enterpriseInfo: mockData,
      loading: false
    })
  },

  copyWebsite() {
    const { enterpriseInfo } = this.data
    if (enterpriseInfo && enterpriseInfo.website) {
      wx.setClipboardData({
        data: enterpriseInfo.website,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  makeCall(e) {
    const { phone } = e.currentTarget.dataset
    if (phone) {
      wx.makePhoneCall({
        phoneNumber: phone
      })
    }
  },

  copyEmail(e) {
    const { email } = e.currentTarget.dataset
    if (email) {
      wx.setClipboardData({
        data: email,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  }
})

