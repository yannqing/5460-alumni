// pages/audit/alumni-association/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { associationApi } = require('../../../../api/api.js')

Page({
  data: {
    applicationList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝'],
    pageParams: {
      current: 1,
      pageSize: 10
    },
    hasMore: true,
    total: 0
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

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.data.pageParams.current++
      this.loadApplicationList(true)
    }
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: parseInt(index),
      'pageParams.current': 1
    })
    this.loadApplicationList()
  },

  // 加载申请列表
  async loadApplicationList(append = false) {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const status = this.getStatusByTab()
      const params = {
        ...this.data.pageParams,
        auditStatus: status
      }

      const res = await associationApi.getAssociationApplicationList(params)

      if (res.data && res.data.code === 200) {
        const records = res.data.data.records || []
        const total = res.data.data.total || 0

        this.setData({
          applicationList: append ? [...this.data.applicationList, ...records] : records,
          hasMore: records.length >= this.data.pageParams.pageSize,
          total: total
        })
      }
    } catch (error) {
      console.error('加载申请列表失败:', error)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 根据标签获取状态
  getStatusByTab() {
    const tabMap = {
      0: undefined, // 全部
      1: 0,         // 待审核
      2: 1,         // 已通过
      3: 2          // 已拒绝
    }
    return tabMap[this.data.currentTab]
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/alumni-association/detail/detail?id=${id}`
    })
  },

  // 审核操作
  handleAudit(e) {
    const { id, action } = e.currentTarget.dataset
    const actionText = action === 'approve' ? '通过' : '拒绝'

    wx.showModal({
      title: '确认审核',
      content: `确定要${actionText}该申请吗？`,
      success: (res) => {
        if (res.confirm) {
          this.submitAudit(id, action)
        }
      }
    })
  },

  // 提交审核
  async submitAudit(id, action) {
    try {
      wx.showLoading({ title: '提交中...' })

      const auditStatus = action === 'approve' ? 1 : 2
      const res = await associationApi.auditAssociationApplication({
        applicationId: id,
        auditStatus: auditStatus
      })

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '审核成功',
          icon: 'success'
        })
        this.loadApplicationList()
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('审核失败:', error)
      wx.showToast({
        title: '审核失败',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 格式化状态文本
  formatStatus(status) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝'
    }
    return statusMap[status] || '未知'
  },

  // 格式化时间
  formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
})
