// pages/merchant/edit-apply/edit-apply.js
const app = getApp()
const { fileApi, merchantApi } = require('../../../api/api.js')

Page({
  data: {
    form: {
      merchantName: '',
      merchantType: 1,
      businessLicense: '',
      logo: '',
      backgroundImageList: [],
      unifiedSocialCreditCode: '',
      legalPerson: '',
      legalPersonId: '',
      contactPhone: '',
      contactEmail: '',
      businessScope: '',
      businessCategory: '',
      alumniAssociationId: null
    },
    submitting: false,
    uploadType: 'license',
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
            merchantType: data.merchantType || 1,
            businessLicense: data.businessLicense || '',
            logo: data.logo || '',
            backgroundImageList: this.parseBackgroundImage(data.backgroundImage),
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            legalPersonId: data.legalPersonId || '',
            contactPhone: data.contactPhone || '',
            contactEmail: data.contactEmail || '',
            businessScope: data.businessScope || '',
            businessCategory: data.businessCategory || '',
            alumniAssociationId: data.alumniAssociation?.alumniAssociationId || data.alumniAssociationId || null
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

  chooseBusinessLicense() {
    this.setData({ uploadType: 'license' })
    this.chooseImage()
  },

  chooseLogo() {
    this.setData({ uploadType: 'logo' })
    this.chooseImage()
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles?.[0]?.tempFilePath
        if (tempFilePath) {
          const field = this.data.uploadType === 'logo' ? 'logo' : 'businessLicense'
          this.uploadImage(tempFilePath, field)
        }
      }
    })
  },

  chooseBackgroundImage() {
    wx.chooseMedia({
      count: 9,
      mediaType: ['image'],
      success: (res) => {
        const files = res.tempFiles || []
        if (!files.length) return
        const uploadTasks = files.map(file => this.uploadImage(file.tempFilePath, 'backgroundImageList', false))
        Promise.all(uploadTasks).then(urls => {
          const validUrls = urls.filter(Boolean)
          if (!validUrls.length) return
          this.setData({
            'form.backgroundImageList': [...(this.data.form.backgroundImageList || []), ...validUrls]
          })
        })
      }
    })
  },

  uploadImage(filePath, field, showLoading = true) {
    if (showLoading) wx.showLoading({ title: '上传中...' })
    return fileApi.uploadImage(filePath).then((res) => {
      if (showLoading) wx.hideLoading()
      if (res.code === 200 && res.data && res.data.fileUrl) {
        if (field !== 'backgroundImageList') {
          this.setData({ [`form.${field}`]: res.data.fileUrl })
        }
        return res.data.fileUrl
      }
      return ''
    }).catch(() => {
      if (showLoading) wx.hideLoading()
      return ''
    })
  },

  removeImage(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [`form.${field}`]: '' })
  },

  removeBackgroundImage(e) {
    const { index } = e.currentTarget.dataset
    const list = [...(this.data.form.backgroundImageList || [])]
    list.splice(index, 1)
    this.setData({ 'form.backgroundImageList': list })
  },

  parseBackgroundImage(backgroundImage) {
    if (!backgroundImage) return []
    if (Array.isArray(backgroundImage)) return backgroundImage
    if (typeof backgroundImage === 'string') {
      try {
        const parsed = JSON.parse(backgroundImage)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return [backgroundImage]
      }
    }
    return []
  },

  validateForm() {
    const { merchantName, businessLicense, unifiedSocialCreditCode, legalPerson, legalPersonId, contactPhone } = this.data.form
    if (!merchantName?.trim()) return this.toast('请输入商户名称')
    if (!businessLicense) return this.toast('请上传营业执照')
    if (!unifiedSocialCreditCode?.trim() || unifiedSocialCreditCode.length !== 18) return this.toast('请输入18位信用代码')
    if (!legalPerson?.trim()) return this.toast('请输入法人姓名')
    if (!legalPersonId?.trim() || legalPersonId.length !== 18) return this.toast('请输入18位身份证号')
    if (!/^\d{11}$/.test(contactPhone)) return this.toast('请输入11位联系电话')
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
      ...form,
      backgroundImage: form.backgroundImageList?.length ? JSON.stringify(form.backgroundImageList) : undefined
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
