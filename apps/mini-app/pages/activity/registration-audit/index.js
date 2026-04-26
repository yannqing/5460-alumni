// pages/activity/registration-audit/index.js
const { alumniAssociationManagementApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    activityId: 0,
    activityTitle: '',
    // 列表
    registrationList: [],
    listLoading: false,
    currentPage: 1,
    pageSize: 10,
    total: 0,
    hasMore: true,
    // 筛选
    registrationStatus: 0, // 0-待审核 1-已通过 2-已拒绝 3-已取消
    statusList: [
      { value: 0, label: '待审核' },
      { value: 1, label: '已通过' },
      { value: 2, label: '已拒绝' },
      { value: 3, label: '已取消' },
    ],
    keyword: '',
    // 审核中
    reviewing: false,
    defaultUserAvatarUrl: config.defaultAvatar,
  },

  onLoad(options) {
    const activityId = options.activityId
    const activityTitle = options.activityTitle ? decodeURIComponent(options.activityTitle) : ''
    if (!activityId) {
      wx.showToast({ title: '活动ID缺失', icon: 'none' })
      return
    }
    this.setData({ activityId, activityTitle })
    this.loadList(true)
  },

  onShow() {
    if (this.data.activityId) {
      this.loadList(true)
    }
  },

  // 加载报名列表
  async loadList(refresh = false) {
    if (this.data.listLoading) return
    if (refresh) {
      this.setData({ currentPage: 1, registrationList: [], hasMore: true })
    }
    if (!this.data.hasMore) return

    try {
      this.setData({ listLoading: true })
      const params = {
        current: this.data.currentPage,
        pageSize: this.data.pageSize,
        activityId: this.data.activityId,
        registrationStatus: this.data.registrationStatus,
        keyword: this.data.keyword || '',
      }
      const res = await alumniAssociationManagementApi.getActivityRegistrationPage(params)
      if (res.data && res.data.code === 200 && res.data.data) {
        const records = (res.data.data.records || []).map(item => {
          const formatted = { ...item }
          if (item.registrationTime) {
            formatted.registrationTime = String(item.registrationTime).replace('T', ' ')
          }
          if (item.auditTime) {
            formatted.auditTime = String(item.auditTime).replace('T', ' ')
          }
          if (item.userAvatar) {
            formatted.userAvatar = config.getImageUrl(item.userAvatar)
          }
          return formatted
        })
        const newList = refresh
          ? records
          : [...this.data.registrationList, ...records]
        this.setData({
          registrationList: newList,
          total: res.data.data.total || 0,
          hasMore: newList.length < (res.data.data.total || 0),
          currentPage: refresh ? 2 : this.data.currentPage + 1,
        })
      } else {
        this.setData({ hasMore: false })
      }
    } catch (error) {
      console.error('[RegistrationAudit] 加载列表异常:', error)
      this.setData({ hasMore: false })
    } finally {
      this.setData({ listLoading: false })
    }
  },

  // 触底加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.listLoading) return
    this.loadList()
  },

  // 切换状态
  selectStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status)
    if (status === this.data.registrationStatus) return
    this.setData({ registrationStatus: status })
    this.loadList(true)
  },

  // 关键词输入（防抖：失焦/确认时触发）
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onKeywordConfirm() {
    this.loadList(true)
  },

  // 通过
  passRegistration(e) {
    const registrationId = e.currentTarget.dataset.id
    wx.showModal({
      title: '通过审核',
      content: '确定要通过该报名申请吗？',
      success: async res => {
        if (res.confirm) {
          await this.submitReview(registrationId, 1)
        }
      },
    })
  },

  // 拒绝
  rejectRegistration(e) {
    const registrationId = e.currentTarget.dataset.id
    wx.showModal({
      title: '拒绝审核',
      placeholderText: '请输入拒绝理由',
      editable: true,
      success: async res => {
        if (res.confirm) {
          const reason = (res.content || '').trim()
          if (!reason) {
            wx.showToast({ title: '拒绝理由不能为空', icon: 'none' })
            return
          }
          await this.submitReview(registrationId, 2, reason)
        }
      },
    })
  },

  async submitReview(registrationId, reviewResult, auditReason = '') {
    if (this.data.reviewing) return
    try {
      this.setData({ reviewing: true })
      const res = await alumniAssociationManagementApi.reviewActivityRegistration({
        registrationId,
        reviewResult,
        auditReason,
      })
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewResult === 1 ? '通过成功' : '拒绝成功',
          icon: 'success',
        })
        this.loadList(true)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '审核失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('[RegistrationAudit] 提交审核异常:', error)
      wx.showToast({ title: '网络异常，请重试', icon: 'none' })
    } finally {
      this.setData({ reviewing: false })
    }
  },
})
