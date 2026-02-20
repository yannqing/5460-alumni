// pages/enterprise/detail-new/detail-new.js
const { placeApi } = require('../../../api/api.js')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    placeId: '',
    place: {},
    loading: true,
    error: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      this.setData({ placeId: options.id })
      this.loadPlaceDetail()
    } else {
      this.setData({ error: '缺少企业ID参数', loading: false })
    }
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
    this.loadPlaceDetail()
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
   * 加载企业详情
   */
  async loadPlaceDetail() {
    const { placeId } = this.data
    if (!placeId) {
      this.setData({ error: '缺少企业ID参数', loading: false })
      return
    }

    this.setData({ loading: true, error: '' })
    try {
      const res = await placeApi.getPlaceDetail(placeId)
      console.log('获取企业详情结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        let placeData = res.data && res.data.data ? res.data.data : res.data
        
        // 解析 images 字段（JSON字符串转数组）
        if (placeData.images) {
          try {
            // 解析 JSON 字符串
            let imagesArray = JSON.parse(placeData.images)
            
            // 清理每个 URL，移除反引号和多余的空格
            if (Array.isArray(imagesArray)) {
              placeData.images = imagesArray.map(img => {
                // 移除反引号和前后空格
                return img.replace(/[`\s]/g, '')
              })
            } else {
              placeData.images = []
            }
          } catch (error) {
            console.error('解析 images 字段失败:', error)
            placeData.images = []
          }
        } else {
          placeData.images = []
        }
        
        this.setData({ place: placeData, loading: false })
        
        // 设置导航栏标题
        wx.setNavigationBarTitle({
          title: placeData.placeName || '企业详情'
        })
      } else {
        this.setData({ error: '获取企业详情失败', loading: false })
      }
    } catch (error) {
      console.error('获取企业详情失败:', error)
      this.setData({ error: '获取企业详情失败', loading: false })
    }
  },

  /**
   * 跳转到编辑页面
   */
  navigateToEdit() {
    const { placeId } = this.data
    if (!placeId) {
      wx.showToast({
        title: '缺少企业ID参数',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/enterprise/edit/edit?id=${placeId}`
    })
  }
})
