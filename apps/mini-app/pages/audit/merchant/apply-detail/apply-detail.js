// pages/audit/merchant/apply-detail/apply-detail.js
const request = require('../../../../utils/request.js')
const config = require('../../../../utils/config.js')

Page({
  data: {
    applyInfo: {},
    loading: true,
    defaultLogo: config.defaultAvatar,
    defaultBackground: config.defaultCover
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

        const formatTime = (t) => {
          if (!t) { return '' }
          const s = typeof t === 'string' ? t.replace('T', ' ') : String(t)
          return s.length > 19 ? s.slice(0, 19) : s
        }

        // 接口可能返回字符串或数字，统一成数字再分支
        const rs = Number(applyInfo.reviewStatus)

        // 格式化状态文本
        let statusText = ''
        let status = ''
        switch (rs) {
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

        const mt = applyInfo.merchantType
        let merchantTypeText = ''
        if (mt === 1 || mt === '1') { merchantTypeText = '校友商铺' }
        else if (mt === 2 || mt === '2') { merchantTypeText = '普通商铺' }
        else { merchantTypeText = mt != null ? String(mt) : '' }

        let alumniAssociation = applyInfo.alumniAssociation
        if (alumniAssociation && typeof alumniAssociation === 'object') {
          alumniAssociation = {
            ...alumniAssociation,
            logoUrl: config.getImageUrl(alumniAssociation.logo || '')
          }
        } else {
          alumniAssociation = null
        }

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
          alumniAssociation,
          status: status,
          statusText: statusText,
          merchantTypeText,
          logoUrl: config.getImageUrl(applyInfo.logo || ''),
          backgroundImageUrl: normalizeBgImage(applyInfo.backgroundImage),
          submitTime: formatTime(applyInfo.createTime),
          businessLicenseUrl: config.getImageUrl(applyInfo.businessLicense || ''),
          // 调整字段名称以匹配页面模板
          businessLicenseCode: applyInfo.unifiedSocialCreditCode || '',
          legalRepresentative: applyInfo.legalPerson || '',
          businessAddress: applyInfo.merchantAddress || '',
          businessScope: applyInfo.businessScope || '',
          registrationDate: applyInfo.establishmentDate || ''
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
    this.submitAudit('approved')
  },

  handleReject() {
    this.submitAudit('rejected')
  },

  async submitAudit(status) {
    try {
      // 映射状态值：approved->1, rejected->2
      const reviewStatus = status === 'approved' ? 1 : 2
      // 后端拒绝时要求非空原因；页面无备注输入时使用默认文案
      let reviewReason = ''
      if (reviewStatus === 2) {
        reviewReason = '审核未通过'
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
