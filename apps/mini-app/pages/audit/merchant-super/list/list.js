// pages/audit/merchant-super/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { merchantApi } = require('../../../../api/api.js')
const request = require('../../../../utils/request.js')

Page({
  data: {
    applyList: [],
    loading: false,
    loadingMore: false,
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    total: 0,
    currentTab: 0, // 0: 全部, 1: 待审核, 2: 已通过, 3: 已拒绝
    // 拒绝弹窗相关
    showRejectModal: false,
    rejectMerchantId: null,
    rejectComment: ''
  },

  onLoad(options) {
    this.loadApplyList(false)
  },

  onShow() {
    // 每次显示页面时刷新列表，确保状态最新
    if (this.data.applyList.length > 0) {
      this.loadApplyList(false)
    }
  },

  onPullDownRefresh() {
    this.loadApplyList(false)
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (!this.data.loadingMore && this.data.hasMore) {
      this.loadApplyList(true)
    }
  },

  // 切换标签
  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    if (this.data.currentTab !== index) {
      this.setData({
        currentTab: index,
        applyList: [],
        currentPage: 1,
        hasMore: true
      })
      this.loadApplyList(false)
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

  // 加载申请列表
  async loadApplyList(isLoadMore = false) {
    const { loading, loadingMore, hasMore, currentPage, pageSize } = this.data

    if (loading || loadingMore) return
    if (isLoadMore && !hasMore) return

    if (isLoadMore) {
      this.setData({ loadingMore: true })
    } else {
      this.setData({
        loading: true,
        currentPage: 1,
        hasMore: true
      })
    }

    const page = isLoadMore ? currentPage + 1 : 1

    try {
      const status = this.getStatusByTab()
      const params = {
        current: page,
        pageSize: pageSize
      }

      // 添加审核状态筛选参数
      if (status !== undefined) {
        params.reviewStatus = status
      }

      // 使用新生成的超级管理员查看接口
      const res = await merchantApi.getSuperAdminApprovalRecords(params)

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || []
        const total = res.data.data?.total || 0

        const currentCount = isLoadMore ? this.data.applyList.length + records.length : records.length
        const totalNum = parseInt(total)
        const newHasMore = currentCount < totalNum

        // 映射列表，格式化时间并处理 logo
        const mappedList = records.map(item => ({
          ...item,
          displayLogo: item.logo ? config.getImageUrl(item.logo) : config.defaultAvatar,
          createTime: this.formatDisplayTime(item.createTime)
        }))
        const finalList = isLoadMore ? [...this.data.applyList, ...mappedList] : mappedList

        this.setData({
          applyList: finalList,
          currentPage: page,
          total: totalNum,
          hasMore: newHasMore,
          loading: false,
          loadingMore: false
        })
      } else {
        this.setData({
          loading: false,
          loadingMore: false
        })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载商户申请列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({
        loading: false,
        loadingMore: false
      })
    }
  },

  formatDisplayTime(timeStr) {
    if (!timeStr) return ''
    return String(timeStr).replace('T', ' ').substring(0, 16)
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    // 跳转到现有的商户审核详情页
    wx.navigateTo({
      url: `/pages/audit/merchant/apply-detail/apply-detail?merchantId=${id}`
    })
  },

  // 审核操作 - 通过
  handleApprove(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return

    wx.showModal({
      title: '确认审核',
      content: '确定要通过该商户申请吗？',
      success: (res) => {
        if (res.confirm) {
          this.submitReview(id, 1, '')
        }
      }
    })
  },

  // 审核操作 - 拒绝（打开弹窗）
  handleReject(e) {
    const { id } = e.currentTarget.dataset
    this.setData({
      showRejectModal: true,
      rejectMerchantId: id,
      rejectComment: ''
    })
  },

  // 关闭拒绝弹窗
  closeRejectModal() {
    this.setData({
      showRejectModal: false,
      rejectMerchantId: null,
      rejectComment: ''
    })
  },

  // 输入拒绝意见
  onRejectCommentInput(e) {
    this.setData({
      rejectComment: e.detail.value
    })
  },

  // 确认拒绝
  confirmReject() {
    const { rejectMerchantId, rejectComment } = this.data

    if (!rejectComment.trim()) {
      wx.showToast({
        title: '请输入拒绝理由',
        icon: 'none'
      })
      return
    }

    this.submitReview(rejectMerchantId, 2, rejectComment)
    this.closeRejectModal()
  },

  // 提交审核
  async submitReview(merchantId, reviewStatus, reviewReason) {
    try {
      wx.showLoading({ title: '提交中...' })

      const res = await request.post('/merchant-management/approve', {
        merchantId: merchantId,
        reviewStatus: reviewStatus,
        reviewReason: reviewReason
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewStatus === 1 ? '审核通过成功' : '已拒绝申请',
          icon: 'success'
        })
        // 重新加载列表
        this.loadApplyList(false)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('审核失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
  },

  // 阻止冒泡
  preventBubble() {}
})
