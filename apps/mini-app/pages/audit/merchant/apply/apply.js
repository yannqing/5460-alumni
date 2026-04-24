// pages/audit/merchant/apply/apply.js
const app = getApp()
const request = require('../../../../utils/request.js')
const { userApi, merchantApi } = require('../../../../api/api.js')

Page({
  data: {
    applyList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝'],
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    alumniAssociationList: [],
    selectedAlumniAssociationId: '',
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    hasSingleAlumniAssociation: false,
    hasAlumniAdminPermission: false,
    initialized: false,
    isRejectModalVisible: false,
    currentRejectId: '',
    rejectReason: ''
  },

  onLoad(options) {
    this.initPage()
  },

  loadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ pageNum: this.data.pageNum + 1 })
      this.loadApplyList()
    }
  },

  onShow() {
    if (!this.data.initialized) {return}
    this.reloadApplyList()
  },

  onPullDownRefresh() {
    this.reloadApplyList()
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({
        pageNum: this.data.pageNum + 1
      })
      this.loadApplyList()
    }
  },

  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index,
      pageNum: 1,
      applyList: [],
      hasMore: true
    })
    this.loadApplyList()
  },

  async initPage() {
    await this.loadAlumniAssociationList()
    this.setData({ initialized: true })
  },

  async loadAlumniAssociationList() {
    try {
      const res = await userApi.getManagedOrganizations({ type: 0 })
      if (!(res.data && res.data.code === 200)) {
        this.setData({
          alumniAssociationList: [],
          hasAlumniAdminPermission: false,
          hasSingleAlumniAssociation: false
        })
        return
      }

      const organizationList = res.data.data || []
      const alumniAssociationList = organizationList.map(org => ({
        alumniAssociationId: org.id,
        alumniAssociationName: org.name || '校友会'
      }))

      this.setData({
        alumniAssociationList,
        hasAlumniAdminPermission: alumniAssociationList.length > 0
      })

      if (alumniAssociationList.length === 1) {
        const singleAlumni = alumniAssociationList[0]
        this.setData({
        selectedAlumniAssociationId: String(singleAlumni.alumniAssociationId),
          selectedAlumniAssociationName: singleAlumni.alumniAssociationName,
          hasSingleAlumniAssociation: true
        })
        this.reloadApplyList()
        return
      }

      this.setData({
        hasSingleAlumniAssociation: false,
        selectedAlumniAssociationId: '',
        selectedAlumniAssociationName: ''
      })
    } catch (error) {
      console.error('加载校友会列表失败:', error)
      this.setData({
        alumniAssociationList: [],
        hasAlumniAdminPermission: false,
        hasSingleAlumniAssociation: false
      })
    }
  },

  showAlumniAssociationSelector() {
    if (this.data.hasSingleAlumniAssociation || !this.data.hasAlumniAdminPermission) {return}
    this.setData({ showAlumniAssociationPicker: true })
  },

  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  selectAlumniAssociation(e) {
    const alumniAssociationId = String(e.currentTarget.dataset.alumniAssociationId || '')
    const alumniAssociationName = e.currentTarget.dataset.alumniAssociationName || ''

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false
    })

    this.reloadApplyList()
  },

  reloadApplyList() {
    this.setData({
      pageNum: 1,
      applyList: [],
      hasMore: true
    })
    this.loadApplyList()
  },

  async loadApplyList() {
    if (this.data.loading) {return}
    if (
      this.data.hasAlumniAdminPermission &&
      !this.data.hasSingleAlumniAssociation &&
      !this.data.selectedAlumniAssociationId
    ) {
      return
    }
    
    this.setData({ loading: true })
    
    try {
      // 映射标签页到审核状态
      let status = null
      if (this.data.currentTab === 1) {
        status = 0 // 待审核
      } else if (this.data.currentTab === 2) {
        status = 1 // 已通过
      } else if (this.data.currentTab === 3) {
        status = 2 // 已拒绝
      }

      // 构建请求参数
      const params = {
        current: this.data.pageNum,
        pageSize: this.data.pageSize,
        alumniAssociationId: this.data.selectedAlumniAssociationId
      }
      
      if (status !== null) {
        params.status = status
      }

      // 调用新写的商户加入校友会申请列表接口
      const res = await merchantApi.getMerchantJoinApplyPage(params)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const records = res.data.data.records || []
        
        // 转换数据格式以适配页面展示
        const formattedList = records.map(item => {
          let statusText = ''
          let statusTag = ''
          switch (item.status) {
            case 0:
              statusText = '待审核'
              statusTag = 'pending'
              break
            case 1:
              statusText = '已通过'
              statusTag = 'approved'
              break
            case 2:
              statusText = '已拒绝'
              statusTag = 'rejected'
              break
            default:
              statusText = '未知状态'
              statusTag = 'unknown'
          }
          
          return {
            id: item.id, // 申请记录ID
            merchantId: item.merchantId,
            merchantName: item.merchantName,
            logo: item.logo,
            contactPhone: item.applicantPhone,
            applicantName: item.applicantName,
            status: statusTag,
            statusText: statusText,
            submitTime: this.formatDisplayTime(item.createTime),
            reviewStatus: item.status,
            reviewReason: item.reviewComment
          }
        })
        
        const newList = this.data.pageNum === 1 ? formattedList : [...this.data.applyList, ...formattedList]
        const total = parseInt(res.data.data.total) || 0
        const hasMore = newList.length < total
        
        this.setData({
          applyList: newList,
          hasMore: hasMore,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data.msg || '加载失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('加载商户加入申请列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  formatDisplayTime(timeStr) {
    if (!timeStr) {return ''}
    return String(timeStr).replace('T', ' ')
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/merchant/apply-detail/apply-detail?id=${id}`
    })
  },

  async handleAudit(e) {
    const { id, status } = e.currentTarget.dataset

    if (status === 'rejected') {
      this.setData({
        isRejectModalVisible: true,
        currentRejectId: id,
        rejectReason: ''
      })
      return
    }

    wx.showModal({
      title: '确认审核',
      content: '确定通过该商户申请吗？',
      success: async (res) => {
        if (res.confirm) {
          await this.submitAudit(id, status)
        }
      }
    })
  },

  async submitAudit(id, status, reason = '') {
    try {
      // 映射状态值：approved->1, rejected->2
      const reviewStatus = status === 'approved' ? 1 : 2

      let reviewComment = ''
      if (reviewStatus === 2) {
        reviewComment = String(reason || '').trim()
      }
      
      const res = await merchantApi.reviewMerchantJoinApply({
        id: id,
        status: reviewStatus,
        reviewComment: reviewComment
      })
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: status === 'approved' ? '审核通过' : '已拒绝',
          icon: 'success'
        })
        
        // 重置列表并重新加载
        this.setData({
          pageNum: 1,
          applyList: [],
          hasMore: true
        })
        this.loadApplyList()
      } else {
        wx.showToast({
          title: res.data.msg || '审核失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('提交审核失败:', error)
      wx.showToast({
        title: '审核失败，请重试',
        icon: 'none'
      })
    }
  },

  onRejectReasonInput(e) {
    this.setData({
      rejectReason: e.detail.value
    })
  },

  hideRejectModal() {
    this.setData({
      isRejectModalVisible: false,
      currentRejectId: '',
      rejectReason: ''
    })
  },

  async submitRejectAudit() {
    const reason = String(this.data.rejectReason || '').trim()
    if (!reason) {
      wx.showToast({
        title: '请填写拒绝原因',
        icon: 'none'
      })
      return
    }

    await this.submitAudit(this.data.currentRejectId, 'rejected', reason)
    this.hideRejectModal()
  }
})
