// pages/audit/headquarters/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { unionApi } = require('../../../../api/api.js')

Page({
  data: {
    headquartersList: [],
    loading: false,
    loadingMore: false,
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    total: 0,
    filterStatus: -1, // -1: 全部, 0: 待审核, 1: 已审核
    // 审核弹框相关
    isApprovalModalVisible: false,
    approvalAction: 1, // 1-通过，2-拒绝
    currentApprovalId: null,
    approvalForm: {
      headquartersId: '',
      approvalStatus: 1,
      approvalRemark: ''
    }
  },

  onLoad(options) {
    this.loadHeadquartersList(false)
  },

  onShow() {
    this.loadHeadquartersList(false)
  },

  onPullDownRefresh() {
    this.loadHeadquartersList(false)
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (!this.data.loadingMore && this.data.hasMore) {
      this.loadHeadquartersList(true)
    }
  },

  // 设置筛选状态
  setFilterStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status)
    if (this.data.filterStatus !== status) {
      this.setData({ filterStatus: status })
      this.loadHeadquartersList(false)
    }
  },

  async loadHeadquartersList(isLoadMore = false) {
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
      const params = {
        current: page,
        pageSize: pageSize
      }

      // 添加审核状态筛选参数
      if (this.data.filterStatus !== -1) {
        params.approvalStatus = this.data.filterStatus
      }

      // 调用真实接口获取待审核的校友总会列表
      const res = await unionApi.getPendingUnionPage(params)

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || []
        const total = res.data.data?.total || 0
        
        const newList = records.map(item => ({
          id: item.headquartersId,
          name: item.headquartersName,
          logo: item.logo,
          description: item.description,
          contactInfo: item.contactInfo,
          address: item.address,
          website: item.website,
          wechatPublicAccount: item.wechatPublicAccount,
          email: item.email,
          phone: item.phone,
          establishedDate: item.establishedDate,
          memberCount: item.memberCount,
          activeStatus: item.activeStatus,
          activeStatusText: item.activeStatus === 1 ? '活跃' : '不活跃',
          approvalStatus: item.approvalStatus,
          approvalStatusText: item.approvalStatus === 0 ? '待审核' : item.approvalStatus === 1 ? '已通过' : '已驳回',
          level: item.level,
          levelText: this.getLevelText(item.level),
          createCode: item.createCode
        }))

        const currentCount = isLoadMore ? this.data.headquartersList.length + newList.length : newList.length
        const totalNum = parseInt(total)
        const newHasMore = currentCount < totalNum

        const finalList = isLoadMore ? [...this.data.headquartersList, ...newList] : newList

        this.setData({
          headquartersList: finalList,
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
      console.error('加载校友总会列表失败:', error)
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

  getLevelText(level) {
    const levelMap = {
      1: '校级',
      2: '省级',
      3: '国家级',
      4: '国际级'
    }
    return levelMap[level] || '未知'
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/headquarters/detail/detail?headquartersId=${id}`
    })
  },

  // 点击通过按钮
  onApproveTap(e) {
    const { id } = e.currentTarget.dataset
    this.showApprovalModal(id, 1) // 1表示通过
  },

  // 点击拒绝按钮
  onRejectTap(e) {
    const { id } = e.currentTarget.dataset
    this.showApprovalModal(id, 2) // 2表示拒绝
  },

  // 显示审核弹框
  showApprovalModal(id, action) {
    this.setData({
      isApprovalModalVisible: true,
      approvalAction: action,
      currentApprovalId: id,
      approvalForm: {
        headquartersId: id,
        approvalStatus: action,
        approvalRemark: ''
      }
    })
  },

  // 隐藏审核弹框
  hideApprovalModal() {
    this.setData({
      isApprovalModalVisible: false,
      currentApprovalId: null,
      approvalForm: {
        headquartersId: '',
        approvalStatus: 1,
        approvalRemark: ''
      }
    })
  },

  // 处理审核表单输入
  onApprovalInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    this.setData({
      [`approvalForm.${field}`]: value
    })
  },

  // 提交审核
  async submitApproval() {
    try {
      wx.showLoading({ title: '提交中...' })

      const { approvalForm } = this.data

      // 调用审核API
      const res = await unionApi.auditUnion(approvalForm)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: approvalForm.approvalStatus === 1 ? '审核通过成功' : '审核拒绝成功',
          icon: 'success'
        })

        // 隐藏弹框
        this.hideApprovalModal()

        // 重新加载列表
        this.loadHeadquartersList(false)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('提交审核失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
  }
})
