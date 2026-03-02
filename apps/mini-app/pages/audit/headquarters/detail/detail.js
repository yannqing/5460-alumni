// pages/audit/headquarters/detail/detail.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { unionApi } = require('../../../../api/api.js')

Page({
  data: {
    headquarters: null,
    loading: true,
    statusClass: '',
    approvalStatusText: ''
  },

  onLoad(options) {
    const { headquartersId } = options
    if (headquartersId) {
      this.loadHeadquartersDetail(headquartersId)
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  async loadHeadquartersDetail(headquartersId) {
    try {
      this.setData({ loading: true })
      
      // 调用获取校友总会申请详情的接口
      const res = await unionApi.getApplyDetail(headquartersId)
      
      if (res.data && res.data.code === 200) {
        const headquarters = res.data.data
        
        // 处理状态文本和样式
        const statusClass = this.getStatusClass(headquarters.approvalStatus)
        const approvalStatusText = this.getApprovalStatusText(headquarters.approvalStatus)
        
        this.setData({
          headquarters,
          statusClass,
          approvalStatusText,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '获取详情失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('获取校友总会申请详情失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 获取审核状态样式类
  getStatusClass(status) {
    switch (status) {
      case 0:
        return 'status-pending'
      case 1:
        return 'status-approved'
      case 2:
        return 'status-rejected'
      default:
        return ''
    }
  },

  // 获取审核状态文本
  getApprovalStatusText(status) {
    switch (status) {
      case 0:
        return '待审核'
      case 1:
        return '已通过'
      case 2:
        return '已驳回'
      default:
        return '未知'
    }
  },

  // 打开链接
  openLink(e) {
    const { link } = e.currentTarget.dataset
    if (link) {
      wx.navigateTo({
        url: `/pages/article/web-view/web-view?url=${encodeURIComponent(link)}`
      })
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack({ delta: 1 })
  }
})
