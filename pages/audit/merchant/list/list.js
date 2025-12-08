// pages/audit/merchant/list/list.js
const app = getApp()

Page({
  data: {
    merchantList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝']
  },

  onLoad(options) {
    this.loadMerchantList()
  },

  onShow() {
    this.loadMerchantList()
  },

  onPullDownRefresh() {
    this.loadMerchantList()
    wx.stopPullDownRefresh()
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index
    })
    this.loadMerchantList()
  },

  // 加载商家审核列表
  async loadMerchantList() {
    this.setData({ loading: true })
    
    try {
      // TODO: 调用后端接口获取商家审核列表
      // const res = await auditApi.getMerchantAuditList({ status: this.data.currentTab })
      
      // 模拟数据
      const mockData = [
        {
          id: 1,
          merchantId: 3001,
          name: '校友餐厅',
          owner: '孙七',
          phone: '137****7777',
          address: '南京市鼓楼区',
          submitTime: '2024-01-15 10:30:00',
          status: 'pending',
          statusText: '待审核'
        },
        {
          id: 2,
          merchantId: 3002,
          name: '校友咖啡',
          owner: '周八',
          phone: '136****6666',
          address: '南京市玄武区',
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
        merchantList: filteredData,
        loading: false
      })
    } catch (error) {
      console.error('加载商家审核列表失败:', error)
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
      url: `/pages/audit/merchant/detail/detail?id=${id}`
    })
  }
})

