// pages/enterprise/my-enterprise/my-enterprise.js
const { placeApi } = require('../../../api/api.js')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    places: [],
    loading: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadMyPlaces()
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadMyPlaces()
    wx.stopPullDownRefresh()
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 加载我的企业列表
   */
  async loadMyPlaces() {
    this.setData({ loading: true })
    try {
      const res = await placeApi.getMyPlaces()
      console.log('获取我的企业列表结果:', res)
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        const data = res.data && res.data.data ? res.data.data : res.data
        this.setData({ places: data || [] })
      } else {
        wx.showToast({
          title: '获取企业列表失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('获取我的企业列表失败:', error)
      wx.showToast({
        title: '获取企业列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 跳转到企业详情页面
   */
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id) {
      wx.navigateTo({
        url: `/pages/enterprise/detail-new/detail-new?id=${id}`
      })
    }
  },

  /**
   * 跳转到申请企业页面
   */
  goToApply() {
    wx.navigateTo({
      url: '/pages/enterprise/apply-enterprise/apply-enterprise'
    })
  }
})