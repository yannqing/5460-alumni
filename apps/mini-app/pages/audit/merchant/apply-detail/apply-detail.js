// pages/audit/merchant/apply-detail/apply-detail.js
const request = require('../../../../utils/request.js')
const config = require('../../../../utils/config.js')
const { merchantApi } = require('../../../../api/api.js')

Page({
  data: {
    applyInfo: {},
    loading: true,
    defaultLogo: config.defaultAvatar,
    defaultAlumniLogo: config.defaultAvatar,
    defaultBackground: config.defaultCover
  },

  onLoad(options) {
    this.applyId = options.id ? decodeURIComponent(String(options.id)) : ''
    this.merchantId = options.merchantId ? decodeURIComponent(String(options.merchantId)) : ''
    this.setData({ 
      canAudit: !!options.id,
      fromSuperAdmin: !options.id && !!options.merchantId 
    }) // 只有从审核列表进入的才显示审核按钮
    this.loadApplyDetail()
  },

  async loadApplyDetail() {
    this.setData({ loading: true })
    
    try {
      const params = {}
      if (this.applyId) {
        params.id = String(this.applyId)
      }
      if (this.merchantId) {
        params.merchantId = String(this.merchantId)
      }
      
      const res = await merchantApi.getMerchantJoinApplyDetail(params)
      
      if (res.data && res.data.code === 200) {
        let applyInfo = res.data.data || {}
        this.applyId = applyInfo.id != null ? String(applyInfo.id) : this.applyId

        const formatTime = (t) => {
          if (!t) { return '' }
          const s = typeof t === 'string' ? t.replace('T', ' ') : String(t)
          return s.length > 19 ? s.slice(0, 19) : s
        }

        // 格式化状态文本
        let statusText = ''
        let status = ''
        switch (applyInfo.status) {
          case 0:
            statusText = '待审核'
            status = 'pending'
            break
          case 1:
            statusText = '已发布'
            status = 'approved'
            break
          case 4:
            statusText = '待发布'
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

        let merchantTypeText = ''
        if (applyInfo.merchantType === 1) { merchantTypeText = '校友商铺' }
        else if (applyInfo.merchantType === 2) { merchantTypeText = '普通商铺' }

        const normalizeBgImage = (rawBg) => {
          if (!rawBg || typeof rawBg !== 'string') {
            return ''
          }
          const bg = rawBg.trim()
          if (!bg) {
            return ''
          }
          try {
            const parsed = JSON.parse(bg)
            if (Array.isArray(parsed) && parsed.length > 0) {
              return config.getImageUrl(parsed[0] || '')
            }
          } catch (e) {
            // 非 JSON 字符串时按普通图片路径处理
          }
          return config.getImageUrl(bg)
        }

        // 格式化数据
        applyInfo = {
          ...applyInfo,
          status: status,
          statusText: statusText,
          merchantTypeText,
          logoUrl: config.getImageUrl(applyInfo.logo || ''),
          backgroundImageUrl: normalizeBgImage(applyInfo.backgroundImage),
          submitTime: formatTime(applyInfo.createTime),
          phone: applyInfo.phone || '',
          contactPhone: applyInfo.contactPhone || '',
          businessLicenseUrl: config.getImageUrl(applyInfo.businessLicense || ''),
          businessLicenseCode: applyInfo.unifiedSocialCreditCode || '',
          legalRepresentative: applyInfo.legalPerson || '',
          businessScope: applyInfo.businessScope || '',
          reviewReason: applyInfo.reviewComment
        }

        // 格式化校友会信息
        if (applyInfo.alumniAssociation) {
          applyInfo.alumniAssociation = {
            ...applyInfo.alumniAssociation,
            logoUrl: applyInfo.alumniAssociation.logo ? config.getImageUrl(applyInfo.alumniAssociation.logo) : config.defaultAvatar
          }
        }

        // 格式化已加入的校友会列表
        if (applyInfo.joinedAssociations && Array.isArray(applyInfo.joinedAssociations)) {
          applyInfo.joinedAssociations = applyInfo.joinedAssociations.map(item => ({
            ...item,
            logoUrl: item.logo ? config.getImageUrl(item.logo) : config.defaultAvatar
          }))
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

  handleApprove() {
    wx.showModal({
      title: '确认审核',
      content: '确定通过该商户的加入申请吗？',
      success: (res) => {
        if (res.confirm) {
          this.submitAudit('approved')
        }
      }
    })
  },

  handleReject() {
    // 这里简单弹窗输入拒绝原因
    wx.showModal({
      title: '拒绝申请',
      editable: true,
      placeholderText: '请输入拒绝原因',
      success: (res) => {
        if (res.confirm) {
          this.submitAudit('rejected', res.content)
        }
      }
    })
  },

  async submitAudit(status, reason = '') {
    try {
      const reviewStatus = status === 'approved' ? 1 : 2
      
      const res = await merchantApi.reviewMerchantJoinApply({
        id: String(this.applyId),
        status: reviewStatus,
        reviewComment: reason || (reviewStatus === 2 ? '审核未通过' : '')
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
              if (prevPage && prevPage.reloadApplyList) {
                prevPage.reloadApplyList()
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
