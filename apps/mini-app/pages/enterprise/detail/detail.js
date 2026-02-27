// pages/enterprise/detail/detail.js
const { placeApi } = require('../../../api/api.js')
const app = getApp()

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
    placeApi.getPlaceManagementDetail(this.data.enterpriseId)
      .then((res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.setData({ 
            enterpriseInfo: res.data.data,
            loading: false 
          })
        } else {
          console.error('获取企业详情失败:', res.data && res.data.msg || '接口调用失败')
          this.setData({ loading: false })
          wx.showToast({ title: '获取企业详情失败', icon: 'none' })
        }
      })
      .catch((error) => {
        console.error('获取企业详情异常:', error)
        this.setData({ loading: false })
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      })
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

