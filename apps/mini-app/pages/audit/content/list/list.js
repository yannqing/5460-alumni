// pages/audit/content/list/list.js
const app = getApp()

Page({
  data: {
    contentList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝']
  },

  onLoad(options) {
    this.loadContentList()
  },

  onShow() {
    this.loadContentList()
  },

  onPullDownRefresh() {
    this.loadContentList()
    wx.stopPullDownRefresh()
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index
    })
    this.loadContentList()
  },

  // 加载内容审核列表
  async loadContentList() {
    this.setData({ loading: true })
    
    try {
      // TODO: 调用后端接口获取内容审核列表
      // const res = await auditApi.getContentAuditList({ status: this.data.currentTab })
      
      // 模拟数据
      const mockData = [
        {
          id: 1,
          contentId: 2001,
          type: 'post',
          title: '校友聚会活动',
          author: '王五',
          submitTime: '2024-01-15 10:30:00',
          status: 'pending',
          statusText: '待审核'
        },
        {
          id: 2,
          contentId: 2002,
          type: 'comment',
          title: '评论内容',
          author: '赵六',
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
        contentList: filteredData,
        loading: false
      })
    } catch (error) {
      console.error('加载内容审核列表失败:', error)
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
      url: `/pages/audit/content/detail/detail?id=${id}`
    })
  }
})


