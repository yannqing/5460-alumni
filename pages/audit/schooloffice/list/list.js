// pages/audit/schooloffice/list/list.js
const app = getApp()

Page({
  data: {
    applicationList: [],
    loading: false,
    pageParams: {
      current: 1,
      pageSize: 10,
      sortField: 'createTime',
      sortOrder: 'ascend',
      platformId: 0,
      associationName: '',
      chargeName: '',
      location: '',
      applicationStatus: 0
    }
  },

  onLoad(options) {
    this.loadApplicationList()
  },

  onShow() {
    this.loadApplicationList()
  },

  onPullDownRefresh() {
    this.data.pageParams.current = 1
    this.loadApplicationList()
    wx.stopPullDownRefresh()
  },

  // 加载校处会审核列表
  async loadApplicationList() {
    this.setData({ loading: true })
    
    try {
      // 调用后端接口获取校处会审核列表
      const res = await this.queryAssociationApplicationPage(this.data.pageParams)
      
      // 假设接口返回格式如下
      // {
      //   code: 0,
      //   data: {
      //     list: [...],
      //     total: 100
      //   }
      // }
      
      this.setData({
        applicationList: res.data.list,
        loading: false
      })
    } catch (error) {
      console.error('加载校处会审核列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 调用API接口
  async queryAssociationApplicationPage(params) {
    // 调用真实的API
    return await app.api.localPlatformApi.queryAssociationApplicationPage(params)
  },

  // 获取状态文本
  getStatusText(status) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      3: '已撤销'
    }
    return statusMap[status] || '未知'
  },

  // 获取状态样式类
  getStatusClass(status) {
    const classMap = {
      0: 'pending',
      1: 'approved',
      2: 'rejected',
      3: 'withdrawn'
    }
    return classMap[status] || ''
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/schooloffice/detail/detail?id=${id}`
    })
  }
})