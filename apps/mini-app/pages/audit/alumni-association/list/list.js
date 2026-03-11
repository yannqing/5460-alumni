// pages/audit/alumni-association/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { associationApi } = require('../../../../api/api.js')

Page({
  data: {
    applicationList: [],
    loading: false,
    loadingMore: false,
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    total: 0,
    currentTab: 0, // 0: 全部, 1: 待审核, 2: 已通过, 3: 已拒绝, 4: 已撤销
    // 拒绝弹窗相关
    showRejectModal: false,
    rejectApplicationId: null,
    rejectComment: ''
  },

  onLoad(options) {
    this.loadApplicationList(false)
  },

  onShow() {
    this.loadApplicationList(false)
  },

  onPullDownRefresh() {
    this.loadApplicationList(false)
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (!this.data.loadingMore && this.data.hasMore) {
      this.loadApplicationList(true)
    }
  },

  // 切换标签
  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    if (this.data.currentTab !== index) {
      this.setData({ currentTab: index })
      this.loadApplicationList(false)
    }
  },

  // 根据标签获取状态
  getStatusByTab() {
    const tabMap = {
      0: undefined, // 全部
      1: 0,         // 待审核
      2: 1,         // 已通过
      3: 2,         // 已拒绝
      4: 3          // 已撤销
    }
    return tabMap[this.data.currentTab]
  },

  // 加载申请列表
  async loadApplicationList(isLoadMore = false) {
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

      // 添加申请状态筛选参数
      if (status !== undefined) {
        params.applicationStatus = status
      }

      // 使用新接口：系统管理员分页查询所有校友会创建申请列表
      const res = await associationApi.querySystemAdminApplicationPage(params)

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || []
        const total = res.data.data?.total || 0

        const currentCount = isLoadMore ? this.data.applicationList.length + records.length : records.length
        const totalNum = parseInt(total)
        const newHasMore = currentCount < totalNum

        // 映射列表，无 logo 时使用默认头像（与 pages/alumni-association/list 保持一致：config.defaultAvatar）
        const mappedList = records.map(item => ({
          ...item,
          displayLogo: item.logo ? config.getImageUrl(item.logo) : config.defaultAvatar
        }))
        const finalList = isLoadMore ? [...this.data.applicationList, ...mappedList] : mappedList

        this.setData({
          applicationList: finalList,
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
      }
    } catch (error) {
      console.error('加载申请列表失败:', error)
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

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/alumni-association/detail/detail?id=${id}`
    })
  },

  // 审核操作 - 通过
  handleApprove(e) {
    const { id } = e.currentTarget.dataset
    console.log('handleApprove - applicationId:', id)

    if (!id) {
      wx.showToast({
        title: '申请ID不存在',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认审核',
      content: '确定要通过该申请吗？',
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
      rejectApplicationId: id,
      rejectComment: ''
    })
  },

  // 关闭拒绝弹窗
  closeRejectModal() {
    this.setData({
      showRejectModal: false,
      rejectApplicationId: null,
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
    const { rejectApplicationId, rejectComment } = this.data

    if (!rejectComment.trim()) {
      wx.showToast({
        title: '请输入审核意见',
        icon: 'none'
      })
      return
    }

    this.submitReview(rejectApplicationId, 2, rejectComment)
    this.closeRejectModal()
  },

  // 提交审核
  async submitReview(applicationId, reviewResult, reviewComment) {
    console.log('submitReview - params:', { applicationId, reviewResult, reviewComment })

    if (!applicationId) {
      wx.showToast({
        title: '申请ID不能为空',
        icon: 'none'
      })
      return
    }

    try {
      wx.showLoading({ title: '提交中...' })

      const params = {
        applicationId: applicationId,
        reviewResult: reviewResult,
        reviewComment: reviewComment || undefined
      }
      console.log('submitReview - API params:', params)

      // 使用新接口：系统管理员审核校友会创建申请
      const res = await associationApi.reviewApplication(params)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewResult === 1 ? '审核通过成功' : '已拒绝申请',
          icon: 'success'
        })
        // 重新加载列表
        this.loadApplicationList(false)
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
