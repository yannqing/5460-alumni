// pages/audit/schooloffice/detail/detail.js
const app = getApp()

Page({
  data: {
    applicationInfo: {
      id: '',
      associationName: '',
      chargeName: '',
      location: '',
      createTime: '',
      applicationStatus: 0
    }
  },

  onLoad(options) {
    const { id } = options
    this.loadApplicationDetail(id)
  },

  // 加载校处会审核详情
  async loadApplicationDetail(id) {
    try {
      // 调用后端接口获取校处会审核详情
      const res = await app.api.localPlatformApi.getAssociationApplicationDetail(id)
      
      this.setData({
        applicationInfo: res.data
      })
    } catch (error) {
      console.error('加载校处会审核详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
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

  // 通过申请
  async approveApplication() {
    try {
      // 调用后端接口通过申请
      const res = await app.api.localPlatformApi.approveAssociationApplication(this.data.applicationInfo.id)
      
      wx.showToast({
        title: '审核通过',
        icon: 'success'
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      console.error('审核通过失败:', error)
      wx.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      })
    }
  },

  // 拒绝申请
  async rejectApplication() {
    try {
      // 调用后端接口拒绝申请
      const res = await app.api.localPlatformApi.rejectAssociationApplication(this.data.applicationInfo.id)
      
      wx.showToast({
        title: '审核拒绝',
        icon: 'success'
      })
      
      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      console.error('审核拒绝失败:', error)
      wx.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      })
    }
  }
})