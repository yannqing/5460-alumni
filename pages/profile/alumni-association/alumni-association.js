// pages/profile/alumni-association/alumni-association.js
const { associationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    associations: [],
    loading: true,
    error: false,
    errorMsg: ''
  },

  onLoad() {
    this.loadMyAssociations()
  },

  onShow() {
  },

  // 加载我的校友会列表
  async loadMyAssociations() {
    this.setData({ loading: true, error: false })
    
    try {
      const res = await associationApi.getMyAssociations()
      console.log('获取我的校友会列表:', res)
      
      if (res.code === 200 || (res.data && res.data.code === 200)) {
        const data = res.data || res
        this.setData({
          associations: data.data || [],
          loading: false
        })
      } else {
        this.setData({
          error: true,
          errorMsg: res.msg || '获取校友会列表失败',
          loading: false
        })
      }
    } catch (error) {
      console.error('获取我的校友会列表失败:', error)
      this.setData({
        error: true,
        errorMsg: '网络错误，请稍后重试',
        loading: false
      })
    }
  },

  // 跳转到校友会详情页
  goToAssociationDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 跳转到校友会列表页
  goToAssociationList() {
    wx.navigateTo({
      url: '/pages/alumni-association/list/list'
    })
  }
})