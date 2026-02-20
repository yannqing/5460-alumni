// pages/audit/merchant/apply/apply.js
const app = getApp()
const request = require('../../../../utils/request.js')

Page({
  data: {
    applyList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝'],
    hasMore: true,
    pageNum: 1,
    pageSize: 10
  },

  onLoad(options) {
    this.loadApplyList()
  },

  onShow() {
    this.loadApplyList()
  },

  onPullDownRefresh() {
    this.setData({
      pageNum: 1,
      applyList: [],
      hasMore: true
    })
    this.loadApplyList()
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

  async loadApplyList() {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    
    try {
      // 映射标签页到审核状态
      let reviewStatus = ''
      if (this.data.currentTab === 1) {
        reviewStatus = 0 // 待审核
      } else if (this.data.currentTab === 2) {
        reviewStatus = 1 // 已通过
      } else if (this.data.currentTab === 3) {
        reviewStatus = 2 // 已拒绝
      }

      // 构建请求参数
      const params = {
        current: this.data.pageNum,
        pageSize: this.data.pageSize
      }
      
      if (reviewStatus !== '') {
        params.reviewStatus = reviewStatus
      }

      // 调用真实API
      const res = await request.get('/merchant-management/approval/records', params)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const records = res.data.data.records || []
        
        // 转换数据格式
        const formattedList = records.map(item => {
          let statusText = ''
          let status = ''
          switch (item.reviewStatus) {
            case 0:
              statusText = '待审核'
              status = 'pending'
              break
            case 1:
              statusText = '已通过'
              status = 'approved'
              break
            case 2:
              statusText = '已拒绝'
              status = 'rejected'
              break
            default:
              statusText = '未知状态'
              status = 'unknown'
          }
          
          return {
            id: item.merchantId,
            merchantId: item.merchantId,
            merchantName: item.merchantName,
            contactPhone: item.contactPhone,
            applicantName: item.legalPerson,
            status: status,
            statusText: statusText,
            submitTime: item.createTime,
            reviewStatus: item.reviewStatus,
            merchantType: item.merchantType === 1 ? '校友商铺' : '普通商铺',
            reviewReason: item.reviewReason
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
      console.error('加载商户申请列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/merchant/apply-detail/apply-detail?id=${id}`
    })
  },

  async handleAudit(e) {
    const { id, status } = e.currentTarget.dataset
    
    wx.showModal({
      title: '确认审核',
      content: status === 'approved' ? '确定通过该商户申请吗？' : '确定拒绝该商户申请吗？',
      success: async (res) => {
        if (res.confirm) {
          await this.submitAudit(id, status)
        }
      }
    })
  },

  async submitAudit(id, status) {
    try {
      // 映射状态值：approved->1, rejected->2
      const reviewStatus = status === 'approved' ? 1 : 2
      
      // 准备审核原因
      let reviewReason = ''
      if (reviewStatus === 2) {
        // 审核失败时，提示用户输入原因
        await this.showReasonInput().then(reason => {
          reviewReason = reason
        }).catch(() => {
          // 用户取消输入，终止审核
          return
        })
      }
      
      const res = await request.post('/merchant-management/approve', {
        merchantId: id,
        reviewStatus: reviewStatus,
        reviewReason: reviewReason
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
  
  // 显示审核原因输入框
  showReasonInput() {
    return new Promise((resolve, reject) => {
      wx.showModal({
        title: '审核失败原因',
        editable: true,
        placeholderText: '请输入审核失败原因',
        success: (res) => {
          if (res.confirm) {
            if (!res.content || res.content.trim() === '') {
              wx.showToast({
                title: '请输入审核失败原因',
                icon: 'none'
              })
              reject()
            } else {
              resolve(res.content.trim())
            }
          } else {
            reject()
          }
        },
        fail: () => {
          reject()
        }
      })
    })
  }
})
