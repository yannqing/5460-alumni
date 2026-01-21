// pages/audit/user/list/list.js
const app = getApp()

Page({
  data: {
    articleList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝']
  },

  onLoad(options) {
    this.loadArticleList()
  },

  onShow() {
    this.loadArticleList()
  },

  onPullDownRefresh() {
    this.loadArticleList()
    wx.stopPullDownRefresh()
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index
    })
    this.loadArticleList()
  },

  // 加载文章审核列表
  async loadArticleList() {
    this.setData({ loading: true })
    
    try {
      // TODO: 调用后端接口获取文章审核列表
      // const res = await auditApi.getArticleAuditList({ status: this.data.currentTab })
      
      // 模拟数据
      const mockData = [
        {
          id: 1,
          articleId: 2001,
          nickname: '南京大学举办2024年校友年会',
          avatar: '',
          school: '南京大学',
          submitTime: '2024-01-15 10:30:00',
          status: 'pending',
          statusText: '待审核'
        },
        {
          id: 2,
          articleId: 2002,
          nickname: '东南大学校友创业分享会成功举办',
          avatar: '',
          school: '东南大学',
          submitTime: '2024-01-14 15:20:00',
          status: 'approved',
          statusText: '已通过'
        }
      ]
      
      // 根据当前标签过滤数据
      let filteredData = mockData
      if (this.data.currentTab === 1) {
        filteredData = mockData.filter(item => item.status === 'pending')
      } else if (this.data.currentTab === 2) {
        filteredData = mockData.filter(item => item.status === 'approved')
      } else if (this.data.currentTab === 3) {
        filteredData = mockData.filter(item => item.status === 'rejected')
      }
      
      this.setData({
        articleList: filteredData,
        loading: false
      })
    } catch (error) {
      console.error('加载文章审核列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/user/detail/detail?id=${id}`
    })
  }
})


