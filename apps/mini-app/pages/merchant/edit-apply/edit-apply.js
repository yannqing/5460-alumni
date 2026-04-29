// pages/merchant/edit-apply/edit-apply.js
const { merchantApi } = require('../../../api/api.js')

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
    merchantId: '',
  },

  onLoad(options) {
    const merchantId = options.merchantId
    if (!merchantId) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ merchantId })
    this.loadPendingMerchantDetail(merchantId)
  },

  // 加载待审核商家详情
  loadPendingMerchantDetail(merchantId) {
    wx.showLoading({ title: '加载中...' })
    
    merchantApi.getPendingMerchantDetail(merchantId).then((res) => {
      wx.hideLoading()
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        this.setData({
          form: {
            merchantName: data.merchantName || '',
            merchantType: data.merchantType === 1 || data.merchantType === 2 ? data.merchantType : 2,
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            phone: data.phone || data.contactPhone || ''
          }
        })
      } else {
        wx.showToast({ title: msg || '加载失败', icon: 'none' })
      }
    }).catch((err) => {
      wx.hideLoading()
      console.error('加载详情失败:', err)
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  validateForm() {
    const { merchantName, unifiedSocialCreditCode, legalPerson, phone } = this.data.form
    if (!merchantName?.trim()) return this.toast('请输入商户名称')
    if (!unifiedSocialCreditCode?.trim() || unifiedSocialCreditCode.length !== 18) return this.toast('请输入18位信用代码')
    if (!legalPerson?.trim()) return this.toast('请输入法人姓名')
    if (!phone?.trim()) return this.toast('请输入法人个人联系方式')
    if (!/^\d{11}$/.test(phone)) return this.toast('请输入11位法人联系电话')
    return true
  },

  toast(title) {
    wx.showToast({ title, icon: 'none' })
    return false
  },

  submitEdit() {
    if (!this.validateForm()) return
    this.setData({ submitting: true })
    
    const { form, merchantId } = this.data
    const payload = {
      merchantName: (form.merchantName || '').trim(),
      merchantType: 2,
      unifiedSocialCreditCode: (form.unifiedSocialCreditCode || '').trim().toUpperCase(),
      legalPerson: (form.legalPerson || '').trim(),
      phone: (form.phone || '').trim()
    }

    merchantApi.updatePendingMerchantApplication(merchantId, payload).then((res) => {
      this.setData({ submitting: false })
      if (res.data?.code === 200) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '保存失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ submitting: false })
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  }
})
