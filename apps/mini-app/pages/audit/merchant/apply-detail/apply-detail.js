// pages/audit/merchant/apply-detail/apply-detail.js
const app = getApp()
const request = require('../../../../utils/request.js')

Page({
  data: {
    applyInfo: {},
    loading: true,
    remark: ''
  },

  onLoad(options) {
    this.applyId = options.id
    this.loadApplyDetail()
  },

  async loadApplyDetail() {
    this.setData({ loading: true })
    
    try {
      const res = await request.get(`/merchant-management/approval/record?merchantId=${this.applyId}`)
      
      if (res.data && res.data.code === 200) {
        let applyInfo = res.data.data || {}
        
        // 格式化状态文本
        let statusText = ''
        let status = ''
        switch (applyInfo.reviewStatus) {
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
        
        // 格式化数据
        applyInfo = {
          ...applyInfo,
          status: status,
          statusText: statusText,
          // 调整字段名称以匹配页面模板
          businessLicenseCode: applyInfo.uniformSocialCreditCode || '',
          businessLicenseName: applyInfo.merchantName || '',
          legalRepresentative: applyInfo.legalPerson || '',
          businessAddress: applyInfo.merchantAddress || '',
          businessScope: applyInfo.businessScope || '',
          registrationDate: applyInfo.establishmentDate || '',
          auditorName: applyInfo.reviewerName || ''
        }
        
        this.setData({
          applyInfo: applyInfo,
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
      console.error('加载详情失败:', error)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  onRemarkInput(e) {
    this.setData({
      remark: e.detail.value
    })
  },

  handleApprove() {
    this.submitAudit('approved')
  },

  handleReject() {
    this.submitAudit('rejected')
  },

  async submitAudit(status) {
    try {
      // 映射状态值：approved->1, rejected->2
      const reviewStatus = status === 'approved' ? 1 : 2
      
      let reviewReason = this.data.remark
      
      // 审核失败时，确保有审核原因
      if (reviewStatus === 2) {
        if (!reviewReason || reviewReason.trim() === '') {
          wx.showToast({
            title: '审核失败原因不能为空',
            icon: 'none'
          })
          return
        }
        reviewReason = reviewReason.trim()
      }
      
      const res = await request.post('/merchant-management/approve', {
        merchantId: this.applyId,
        reviewStatus: reviewStatus,
        reviewReason: reviewReason
      })
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: status === 'approved' ? '审核通过' : '审核拒绝',
          icon: 'success'
        })
        
        // 返回上一页并刷新列表
        setTimeout(() => {
          wx.navigateBack({
            delta: 1,
            success: () => {
              const pages = getCurrentPages()
              const prevPage = pages[pages.length - 2]
              if (prevPage && prevPage.loadApplyList) {
                prevPage.loadApplyList()
              }
            }
          })
        }, 1500)
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
  }
})
