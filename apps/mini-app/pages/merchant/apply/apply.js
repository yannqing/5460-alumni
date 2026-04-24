// pages/merchant/apply/apply.js
const app = getApp()
const { associationApi, fileApi, merchantApi } = require('../../../api/api.js')
const { post } = require('../../../utils/request.js')

Page({
  data: {
    form: {
      merchantName: '',
      merchantType: 2,
      unifiedSocialCreditCode: '',
      legalPerson: '',
      phone: ''
    },
    submitting: false,
    merchantStatus: 'none', // none, pending, approved, rejected
    /** 编辑待审核申请时存在（与 onLoad options.merchantId 一致） */
    merchantId: ''
  },

  onLoad(options) {
    this.loadMerchantStatus()
    this.loadUserData()
    
    // 如果有merchantId参数，获取待审核商家详情
    if (options.merchantId) {
      this.setData({
        merchantId: options.merchantId
      })
      this.loadPendingMerchantDetail(options.merchantId)
    }
  },

  loadUserData() {
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || {}
    
    const formData = {
      merchantName: '',
      merchantType: 2,
      unifiedSocialCreditCode: '',
      legalPerson: userInfo.nickName || userData.nickName || userData.name || '',
      phone: userInfo.mobile || userData.mobile || userData.phone || ''
    }
    
    this.setData({ form: formData })
  },

  loadMerchantStatus() {
    const userData = app.globalData.userData || {}
    // 假设后端返回 merchantStatus 字段
    const status = userData.merchantStatus || 'none'
    this.setData({ merchantStatus: status })
  },

  // 加载待审核商家详情
  loadPendingMerchantDetail(merchantId) {
    wx.showLoading({ title: '加载中...' })
    
    merchantApi.getPendingMerchantDetail(merchantId).then((res) => {
      wx.hideLoading()
      
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        // 预填充表单数据
        this.setData({
          form: {
            merchantName: data.merchantName || '',
            merchantType: data.merchantType || 2,
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            phone: data.phone || ''
          },
          merchantStatus: this.getMerchantStatusText(data.reviewStatus) // 设置审核状态
        })
      } else {
        wx.showToast({
          title: msg || '加载失败，请稍后重试',
          icon: 'none'
        })
      }
    }).catch((err) => {
      wx.hideLoading()
      console.error('加载待审核商家详情失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    })
  },

  // 获取商家状态文本
  getMerchantStatusText(reviewStatus) {
    switch (reviewStatus) {
      case 0: // 待审核
        return 'pending'
      case 1: // 已通过
        return 'approved'
      case 2: // 已拒绝
        return 'rejected'
      default:
        return 'none'
    }
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  validateForm() {
    const { merchantName, unifiedSocialCreditCode, legalPerson, phone } = this.data.form
    
    if (!merchantName || !merchantName.trim()) {
      wx.showToast({ title: '请输入商户名称', icon: 'none' })
      return false
    }
    
    if (merchantName.length > 100) {
      wx.showToast({ title: '商户名称不能超过100个字符', icon: 'none' })
      return false
    }
    
    if (!unifiedSocialCreditCode || !unifiedSocialCreditCode.trim()) {
      wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
      return false
    }
    
    const normalizedCreditCode = String(unifiedSocialCreditCode).trim().toUpperCase()
    if (normalizedCreditCode.length !== 18) {
      wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' })
      return false
    }
    if (!/^[0-9A-Z]{18}$/.test(normalizedCreditCode)) {
      wx.showToast({ title: '统一社会信用代码格式不正确', icon: 'none' })
      return false
    }
    
    if (!legalPerson || !legalPerson.trim()) {
      wx.showToast({ title: '请输入法人姓名', icon: 'none' })
      return false
    }
    
    if (legalPerson.length > 50) {
      wx.showToast({ title: '法人姓名不能超过50个字符', icon: 'none' })
      return false
    }
    
    if (!phone || !phone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return false
    }
    
    if (!/^\d{11}$/.test(String(phone).trim())) {
      wx.showToast({ title: '联系电话须为11位数字', icon: 'none' })
      return false
    }
    
    return true
  },

  submitApply() {
    if (this.data.merchantStatus === 'approved') {
      wx.showToast({
        title: '您已是认证商家',
        icon: 'none'
      })
      return
    }
    
    if (!this.validateForm()) {return}
    
    this.setData({ submitting: true })
    
    const { form } = this.data
    const normalizedCreditCode = String(form.unifiedSocialCreditCode || '').trim().toUpperCase()

    const applyPayload = {
      merchantName: form.merchantName,
      merchantType: form.merchantType,
      unifiedSocialCreditCode: normalizedCreditCode,
      legalPerson: form.legalPerson,
      phone: form.phone,
      businessCategory: '',
      alumniAssociationId: 0
    }

    const mid = this.data.merchantId
    const useUpdatePending = mid && this.data.merchantStatus === 'pending'
    const req = useUpdatePending
      ? merchantApi.updatePendingMerchantApplication(mid, applyPayload)
      : post('/merchant/apply', applyPayload)

    req.then((res) => {
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        wx.showToast({
          title: msg || '申请提交成功，请等待审核',
          icon: 'success',
          duration: 2000
        })
        this.setData({
          submitting: false,
          merchantStatus: 'pending'
        })
        
        // 更新全局数据
        if (app.globalData.userData) {
          app.globalData.userData.merchantStatus = 'pending'
        }
        
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: msg || '提交失败，请稍后重试',
          icon: 'none'
        })
        this.setData({ submitting: false })
      }
    }).catch((err) => {
      console.error('提交申请失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
      this.setData({ submitting: false })
    })
  }
})



