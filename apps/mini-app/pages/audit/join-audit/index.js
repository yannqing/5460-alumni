// pages/audit/join-audit/index.js
const { joinApplicationApi, userApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    selectedAlumniAssociationLogo: '',
    showAlumniAssociationPicker: false,
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限,
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份,
    defaultUserAvatarUrl: config.defaultAvatar,
    // 校友会列表分页相关
    organizationCurrentPage: 1,
    organizationPageSize: 20,
    organizationTotal: 0,
    organizationLoading: false,
    organizationHasMore: true,
    // 审核列表相关
    applicationList: [],
    applicationLoading: false,
    currentPage: 1,
    pageSize: 10,
    total: 0,
    applicationStatus: 0, // 0-待审核 1-已通过 2-已拒绝 3-已撤销
    hasMore: true,
    // 状态列表
    statusList: [
      { value: 0, label: '待审核' },
      { value: 1, label: '已通过' },
      { value: 2, label: '已拒绝' },
      { value: 3, label: '已撤销' },
    ],
    // 审核相关
    reviewing: false,
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadAlumniAssociationList()
  },

  // 加载审核列表
  async loadApplicationList(refresh = false) {
    if (this.data.applicationLoading) {
      return
    }

    if (refresh) {
      this.setData({
        currentPage: 1,
        applicationList: [],
        hasMore: true,
      })
    }

    if (!this.data.hasMore) {
      return
    }

    try {
      this.setData({ applicationLoading: true })

      const params = {
        current: this.data.currentPage,
        pageSize: this.data.pageSize,
        alumniAssociationId: this.data.selectedAlumniAssociationId || 0,
        applicationStatus: this.data.applicationStatus,
      }

      console.log('[Debug] 加载审核列表参数:', params)

      const res = await this.getApplicationList(params)

      console.log('[Debug] 加载审核列表结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        // 处理数据：格式化时间（去掉T）
        const processedRecords = (res.data.data.records || []).map(item => {
          if (item.applyTime) {
            item.applyTime = item.applyTime.replace('T', ' ')
          }
          if (item.applicantInfo && item.applicantInfo.avatarUrl) {
            item.applicantInfo.avatarUrl = config.getImageUrl(item.applicantInfo.avatarUrl)
          }
          return item
        })

        const newList = refresh
          ? processedRecords
          : [...this.data.applicationList, ...processedRecords]

        this.setData({
          applicationList: newList,
          total: res.data.data.total || 0,
          hasMore: newList.length < (res.data.data.total || 0),
          currentPage: refresh ? 2 : this.data.currentPage + 1,
        })
      } else {
        console.error('[Debug] 加载审核列表失败:', res)
        this.setData({ hasMore: false })
      }
    } catch (error) {
      console.error('[Debug] 加载审核列表异常:', error)
      this.setData({ hasMore: false })
    } finally {
      this.setData({ applicationLoading: false })
    }
  },

  // 调用审核列表接口
  getApplicationList(params) {
    return joinApplicationApi.getApplicationPage(params)
  },

  // 加载校友会列表
  async loadAlumniAssociationList(refresh = false) {
    if (this.data.organizationLoading) return
    if (!refresh && !this.data.organizationHasMore) return

    const currentPage = refresh ? 1 : this.data.organizationCurrentPage

    try {
      this.setData({ organizationLoading: true })

      const res = await userApi.getManagedOrganizationsPage({
        type: 0,
        current: currentPage,
        pageSize: this.data.organizationPageSize,
      })

      if (!(res.data && res.data.code === 200)) {
        console.error('[Debug] 获取校友会列表接口调用失败:', res)
        this.setData({ hasAlumniAdminPermission: false })
        return
      }

      const pageData = res.data.data
      const records = pageData.records || []

      const alumniAssociationList = records.map(org => {
        let logo = org.logo || config.defaultAvatar
        if (logo && !logo.startsWith('http://') && !logo.startsWith('https://')) {
          logo = config.getImageUrl(logo)
        }
        return {
          id: org.id,
          alumniAssociationId: org.id,
          alumniAssociationName: org.name || '校友会',
          organizeId: org.id,
          logo,
          location: org.location || '',
          type: org.type,
        }
      })

      const newList = refresh
        ? alumniAssociationList
        : [...this.data.alumniAssociationList, ...alumniAssociationList]

      this.setData({
        alumniAssociationList: newList,
        organizationCurrentPage: currentPage + 1,
        organizationTotal: pageData.total || 0,
        organizationHasMore: newList.length < (pageData.total || 0),
        hasAlumniAdminPermission: newList.length > 0,
      })

      // 刷新时，如果只有1个校友会结果（已加载完所有数据），自动选择并隐藏选择器
      if (refresh && newList.length === 1 && !this.data.organizationHasMore) {
        this.handleAlumniAssociationSelection(newList)
      }
    } catch (error) {
      console.error('[Debug] 加载校友会列表失败:', error)
    } finally {
      this.setData({ organizationLoading: false })
    }
  },

  // 加载更多校友会
  loadMoreAlumniAssociations() {
    this.loadAlumniAssociationList(false)
  },

  // 处理校友会选择逻辑
  async handleAlumniAssociationSelection(alumniAssociationList) {
    if (alumniAssociationList.length === 1) {
      // 只有一个校友会权限，自动选择并禁用选择器
      const singleAlumni = alumniAssociationList[0]
      this.setData({
        selectedAlumniAssociationId: singleAlumni.alumniAssociationId,
        selectedAlumniAssociationName: singleAlumni.alumniAssociationName,
        selectedAlumniAssociationLogo: singleAlumni.logo || '',
        hasSingleAlumniAssociation: true,
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      // 加载审核列表
      await this.loadApplicationList(true)
    } else if (alumniAssociationList.length > 1) {
      // 多个校友会权限，正常显示选择器
      this.setData({
        hasSingleAlumniAssociation: false,
      })
      console.log('[Debug] 有多个校友会权限，正常显示选择器')
    } else {
      // 没有校友会权限
      this.setData({
        hasSingleAlumniAssociation: false,
      })
      console.log('[Debug] 没有校友会权限')
    }
  },

  // 显示校友会选择器
  showAlumniAssociationSelector() {
    this.setData({ showAlumniAssociationPicker: true })
  },

  // 校友会选择器选择事件
  onAlumniAssociationSelect(e) {
    const { item } = e.detail
    this.setData({
      selectedAlumniAssociationId: item.alumniAssociationId,
      selectedAlumniAssociationName: item.alumniAssociationName,
      selectedAlumniAssociationLogo: item.logo || '',
      showAlumniAssociationPicker: false,
    })
    this.loadApplicationList(true)
  },

  // 校友会选择器取消事件
  onAlumniAssociationCancel() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 校友会选择器加载更多
  onOrganizationPickerLoadMore() {
    this.loadAlumniAssociationList(false)
  },

  // 选择状态
  async selectStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status)
    if (status === this.data.applicationStatus) {
      return
    }

    this.setData({ applicationStatus: status })
    await this.loadApplicationList(true)
  },

  // 滚动触底加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.applicationLoading) {
      return
    }
    this.loadApplicationList()
  },

  // 通过申请
  async passApplication(e) {
    const applicationId = e.currentTarget.dataset.applicationId
    wx.showModal({
      title: '通过审核',
      content: '确定要通过该申请吗？',
      success: async res => {
        if (res.confirm) {
          await this.submitReview(applicationId, 1)
        }
      },
    })
  },

  // 拒绝申请
  async rejectApplication(e) {
    const applicationId = e.currentTarget.dataset.applicationId
    wx.showModal({
      title: '拒绝审核',
      placeholderText: '请输入拒绝理由',
      editable: true,
      success: async res => {
        if (res.confirm) {
          const reviewComment = res.content.trim()
          if (!reviewComment) {
            wx.showToast({
              title: '拒绝理由不能为空',
              icon: 'none',
            })
            return
          }
          await this.submitReview(applicationId, 2, reviewComment)
        }
      },
    })
  },

  // 提交审核
  async submitReview(applicationId, reviewResult, reviewComment = '') {
    if (this.data.reviewing) {
      return
    }

    try {
      this.setData({ reviewing: true })

      const params = {
        applicationId: applicationId,
        reviewResult: reviewResult,
        reviewComment: reviewComment,
      }

      console.log('[Debug] 提交审核参数:', params)

      const res = await this.reviewApplication(params)

      console.log('[Debug] 提交审核结果:', res)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewResult === 1 ? '通过成功' : '拒绝成功',
          icon: 'success',
          duration: 2000,
        })
        await this.loadApplicationList(true)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败',
          icon: 'none',
          duration: 2000,
        })
      }
    } catch (error) {
      console.error('[Debug] 提交审核异常:', error)
      wx.showToast({
        title: '网络异常，请重试',
        icon: 'none',
        duration: 2000,
      })
    } finally {
      this.setData({ reviewing: false })
    }
  },

  // 调用审核接口
  reviewApplication(params) {
    return joinApplicationApi.reviewApplication(params)
  },

  // 跳转到申请详情页
  goToDetail(e) {
    const applicationId = e.currentTarget.dataset.applicationId
    wx.navigateTo({
      url: `/pages/audit/join-audit/detail?applicationId=${applicationId}`,
    })
  },
})
