// pages/audit/list/list.js
const app = getApp()

Page({
  data: {
    auditList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝']
  },

  onLoad(options) {
    this.loadAuditList()
  },

  onShow() {
    this.loadAuditList()
  },

  onPullDownRefresh() {
    this.loadAuditList()
    wx.stopPullDownRefresh()
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index
    })
    this.loadAuditList()
  },

  // 加载审核列表
  async loadAuditList() {
    this.setData({ loading: true })
    
    try {
      // TODO: 调用后端接口获取审核列表
      // const res = await auditApi.getAuditList({ status: this.data.currentTab })
      
      // 模拟数据
      const mockData = [
        {
          id: 1,
          type: 'user',
          title: '用户认证审核',
          applicant: '张三',
          submitTime: '2024-01-15 10:30:00',
          status: 'pending', // pending: 待审核, approved: 已通过, rejected: 已拒绝
          statusText: '待审核'
        },
        {
          id: 2,
          type: 'content',
          title: '内容审核',
          applicant: '李四',
          submitTime: '2024-01-14 15:20:00',
          status: 'approved',
          statusText: '已通过'
        },
        {
          id: 3,
          type: 'merchant',
          title: '商家入驻审核',
          applicant: '王五',
          submitTime: '2024-01-13 09:10:00',
          status: 'rejected',
          statusText: '已拒绝'
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
        auditList: filteredData,
        loading: false
      })
    } catch (error) {
      console.error('加载审核列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 查看详情
  viewDetail(e) {
    const { id, type } = e.currentTarget.dataset
    let url = ''
    
    if (type === 'user') {
      url = `/pages/audit/user/detail/detail?id=${id}`
    } else if (type === 'content') {
      url = `/pages/audit/content/detail/detail?id=${id}`
    } else if (type === 'merchant') {
      url = `/pages/audit/merchant/detail/detail?id=${id}`
    }
    
    if (url) {
      wx.navigateTo({ url })
    }
  }
})


