const { merchantApi, associationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const { uploadImage } = require('../../../utils/fileUpload.js')

Page({
  data: {
    detailLoading: true,
    submitting: false,
    merchantIdStr: '',
    associationDisplayName: '',
    associationLogoUrl: '',
    associationNameFirstChar: '校',
    logoPreview: '',
    licensePreview: '',
    backgroundImageList: [],
    backgroundImagePreviewUrls: [],
    categoryOptions: [],
    serviceOptions: [],
    categoryIndex: -1,
    serviceIndex: -1,
    formData: {
      merchantName: '',
      merchantType: 1,
      businessLicense: '',
      unifiedSocialCreditCode: '',
      legalPerson: '',
      legalPersonId: '',
      contactPhone: '',
      phone: '',
      contactEmail: '',
      businessScope: '',
      businessCategory: '',
      businessCategoryId: '',
      businessServiceId: '',
      logo: '',
      alumniAssociationId: '',
    },
  },

  onLoad(options) {
    const merchantIdStr = options.merchantId ? String(options.merchantId) : ''
    if (!merchantIdStr) {
      wx.showToast({ title: '缺少商户信息', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ merchantIdStr })
    this.loadCategoryOptions().finally(() => {
      this.loadMerchantDetail(merchantIdStr)
    })
  },

  async loadCategoryOptions() {
    try {
      const res = await merchantApi.getCategoryTree()
      const body = res.data || {}
      if (body.code !== 200 || !Array.isArray(body.data)) {
        return
      }
      const options = body.data.map(item => ({
        id: item.id != null ? String(item.id) : '',
        name: item.name || ''
      })).filter(item => item.id && item.name)
      this.setData({ categoryOptions: options })
    } catch (e) {
      console.warn('加载经营类目失败', e)
    }
  },

  async loadServiceOptionsByCategoryId(categoryId, selectedServiceName = '') {
    if (!categoryId) {
      this.setData({
        serviceOptions: [],
        serviceIndex: -1,
        'formData.businessServiceId': '',
        'formData.businessScope': ''
      })
      return
    }
    try {
      const res = await merchantApi.getCategoryServices(categoryId)
      const body = res.data || {}
      if (body.code !== 200 || !Array.isArray(body.data)) {
        this.setData({ serviceOptions: [], serviceIndex: -1, 'formData.businessServiceId': '' })
        return
      }
      const serviceOptions = body.data.map(item => ({
        id: item.id != null ? String(item.id) : '',
        name: item.name || ''
      })).filter(item => item.id && item.name)

      let serviceIndex = -1
      let businessServiceId = ''
      let businessScope = ''
      if (selectedServiceName) {
        const idx = serviceOptions.findIndex(item => item.name === selectedServiceName)
        if (idx >= 0) {
          serviceIndex = idx
          businessServiceId = serviceOptions[idx].id
          businessScope = serviceOptions[idx].name
        }
      }
      this.setData({
        serviceOptions,
        serviceIndex,
        'formData.businessServiceId': businessServiceId,
        'formData.businessScope': businessScope || (selectedServiceName || '')
      })
    } catch (e) {
      console.warn('加载经营范围失败', e)
      this.setData({ serviceOptions: [], serviceIndex: -1, 'formData.businessServiceId': '' })
    }
  },

  parseBackgroundImage(raw) {
    if (!raw) return []
    if (Array.isArray(raw)) return raw.filter(Boolean).map(String)
    if (typeof raw === 'string') {
      try {
        const parsed = JSON.parse(raw)
        return Array.isArray(parsed) ? parsed.filter(Boolean).map(String) : []
      } catch (e) {
        return []
      }
    }
    return []
  },

  syncBackgroundImagePreviews() {
    const list = this.data.backgroundImageList || []
    const urls = list.map(u => {
      const s = String(u).trim()
      if (!s) return ''
      return /^https?:\/\//i.test(s) ? s : config.getImageUrl(s)
    })
    this.setData({ backgroundImagePreviewUrls: urls })
  },

  updateImagePreviews() {
    const f = this.data.formData
    this.setData({
      logoPreview: f.logo ? config.getImageUrl(String(f.logo).trim()) : '',
      licensePreview: f.businessLicense ? config.getImageUrl(String(f.businessLicense).trim()) : '',
    })
  },

  async loadMerchantDetail(merchantIdStr) {
    this.setData({ detailLoading: true })
    try {
      const res = await merchantApi.getMerchantInfo(merchantIdStr)
      const body = res.data || {}
      if (body.code !== 200 || !body.data) {
        wx.showToast({ title: body.message || '加载失败', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
      const data = body.data
      
      const assoc = data.alumniAssociation
      let alumniIdStr = ''
      if (assoc && assoc.alumniAssociationId != null) {
        alumniIdStr = String(assoc.alumniAssociationId)
      } else if (data.alumniAssociationId != null) {
        alumniIdStr = String(data.alumniAssociationId)
      }
      let assocName = (assoc && assoc.associationName) || ''
      let logoRaw = (assoc && assoc.logo) || ''

      if (alumniIdStr && (!assocName || !logoRaw)) {
        try {
          const res = await associationApi.getAssociationDetail(alumniIdStr)
          const inner = res.data && res.data.data
          if (inner) {
            if (!assocName) assocName = inner.associationName || ''
            if (!logoRaw) logoRaw = inner.logo || ''
          }
        } catch (e) { console.warn(e) }
      }

      const nameTrim = (assocName || '').trim()
      const associationLogoUrl = logoRaw ? config.getImageUrl(String(logoRaw).trim()) : ''
      const associationNameFirstChar = nameTrim ? nameTrim.slice(0, 1) : '校'

      this.setData({
        formData: {
          merchantName: data.merchantName || '',
          merchantType: data.merchantType || 1,
          businessLicense: data.businessLicense || '',
          unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
          legalPerson: data.legalPerson || '',
          legalPersonId: data.legalPersonId ? String(data.legalPersonId).trim() : '',
          contactPhone: data.contactPhone || '',
          phone: data.phone || '',
          contactEmail: data.contactEmail || '',
          businessScope: data.businessScope || '',
          businessCategory: data.businessCategory || '',
          businessCategoryId: '',
          businessServiceId: '',
          logo: data.logo ? String(data.logo).trim() : '',
          alumniAssociationId: alumniIdStr,
        },
        backgroundImageList: this.parseBackgroundImage(data.backgroundImage),
        associationDisplayName: nameTrim,
        associationLogoUrl,
        associationNameFirstChar,
        detailLoading: false,
      })
      const categoryOptions = this.data.categoryOptions || []
      const categoryName = (data.businessCategory || '').trim()
      const serviceName = (data.businessScope || '').trim()
      const categoryIndex = categoryOptions.findIndex(item => item.name === categoryName)
      if (categoryIndex >= 0) {
        const category = categoryOptions[categoryIndex]
        this.setData({
          categoryIndex,
          'formData.businessCategoryId': category.id,
          'formData.businessCategory': category.name
        })
        await this.loadServiceOptionsByCategoryId(category.id, serviceName)
      }
      this.updateImagePreviews()
      this.syncBackgroundImagePreviews()
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ detailLoading: false })
      setTimeout(() => wx.navigateBack(), 1500)
    }
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({ [`formData.${field}`]: value }, () => {
      if (field === 'logo' || field === 'businessLicense') {
        this.updateImagePreviews()
      }
    })
  },

  onCategoryChange(e) {
    const index = Number(e.detail.value)
    const option = this.data.categoryOptions[index]
    if (!option) return
    this.setData({
      categoryIndex: index,
      serviceIndex: -1,
      'formData.businessCategoryId': option.id,
      'formData.businessCategory': option.name,
      'formData.businessServiceId': '',
      'formData.businessScope': ''
    })
    this.loadServiceOptionsByCategoryId(option.id)
  },

  onServiceChange(e) {
    const index = Number(e.detail.value)
    const option = this.data.serviceOptions[index]
    if (!option) return
    this.setData({
      serviceIndex: index,
      'formData.businessServiceId': option.id,
      'formData.businessScope': option.name
    })
  },

  clearLogo() { this.setData({ 'formData.logo': '' }, () => this.updateImagePreviews()) },
  clearLicense() { this.setData({ 'formData.businessLicense': '' }, () => this.updateImagePreviews()) },

  async uploadFile(path) {
    wx.showLoading({ title: '上传中...', mask: true })
    try {
      const res = await uploadImage(path, '/file/upload/images', 'image')
      if (res.code === 200 && res.data && res.data.fileUrl) {
        return res.data.fileUrl
      }
      throw new Error(res.msg || '上传失败')
    } finally {
      wx.hideLoading()
    }
  },

  chooseLogo() {
    wx.chooseImage({
      count: 1,
      success: async res => {
        const path = await this.uploadFile(res.tempFilePaths[0])
        this.setData({ 'formData.logo': path }, () => this.updateImagePreviews())
      }
    })
  },

  chooseLicense() {
    wx.chooseImage({
      count: 1,
      success: async res => {
        const path = await this.uploadFile(res.tempFilePaths[0])
        this.setData({ 'formData.businessLicense': path }, () => this.updateImagePreviews())
      }
    })
  },

  chooseBackgroundImage() {
    const remain = 9 - this.data.backgroundImageList.length
    if (remain <= 0) return
    wx.chooseImage({
      count: remain,
      success: async res => {
        const next = [...this.data.backgroundImageList]
        for (const p of res.tempFilePaths) {
          const url = await this.uploadFile(p)
          next.push(url)
        }
        this.setData({ backgroundImageList: next }, () => this.syncBackgroundImagePreviews())
      }
    })
  },

  removeBackgroundImage(e) {
    const { index } = e.currentTarget.dataset
    const list = [...this.data.backgroundImageList]
    list.splice(index, 1)
    this.setData({ backgroundImageList: list }, () => this.syncBackgroundImagePreviews())
  },

  async submitForm() {
    const { merchantIdStr, formData } = this.data
    const normalizedCreditCode = String(formData.unifiedSocialCreditCode || '').trim().toUpperCase()
    const legalPersonId = (formData.legalPersonId || '').trim()
    const contactPhone = (formData.contactPhone || '').trim()
    const legalPhone = (formData.phone || '').trim()
    const idCardReg = /^\d{17}[\dXx]$/
    const mobileReg = /^1[3-9]\d{9}$/
    // 基础校验
    if (!formData.merchantName.trim()) return wx.showToast({ title: '请输入商家名称', icon: 'none' })
    if (!normalizedCreditCode) return wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
    if (normalizedCreditCode.length !== 18) return wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' })
    if (!/^[0-9A-Z]{18}$/.test(normalizedCreditCode)) return wx.showToast({ title: '统一社会信用代码格式不正确', icon: 'none' })
    if (!formData.legalPerson.trim()) return wx.showToast({ title: '请输入法人姓名', icon: 'none' })
    if (!legalPersonId) return wx.showToast({ title: '请输入法人身份证号', icon: 'none' })
    if (legalPersonId.length !== 18) return wx.showToast({ title: '法人身份证号须为18位', icon: 'none' })
    if (!idCardReg.test(legalPersonId)) return wx.showToast({ title: '法人身份证号格式不正确', icon: 'none' })
    if (!contactPhone) return wx.showToast({ title: '请输入商家联系电话', icon: 'none' })
    if (!mobileReg.test(contactPhone)) return wx.showToast({ title: '商家联系电话格式不正确', icon: 'none' })
    if (!legalPhone) return wx.showToast({ title: '请输入法人个人联系电话', icon: 'none' })
    if (!mobileReg.test(legalPhone)) return wx.showToast({ title: '法人联系电话格式不正确', icon: 'none' })
    if (formData.contactEmail && formData.contactEmail.trim()) {
      const email = formData.contactEmail.trim()
      if (!/^[\w.%+-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(email)) {
        return wx.showToast({ title: '联系邮箱格式不正确', icon: 'none' })
      }
    }
    if (!formData.businessLicense.trim()) return wx.showToast({ title: '请上传营业执照', icon: 'none' })

    const payload = {
      merchantId: merchantIdStr,
      merchantName: formData.merchantName.trim(),
      merchantType: formData.merchantType,
      unifiedSocialCreditCode: normalizedCreditCode,
      legalPerson: formData.legalPerson.trim(),
      legalPersonId: legalPersonId.toUpperCase(),
      contactPhone,
      phone: legalPhone,
      businessLicense: formData.businessLicense.trim(),
      contactEmail: formData.contactEmail.trim(),
      businessScope: formData.businessScope,
      businessCategory: formData.businessCategory.trim(),
      logo: formData.logo.trim(),
      alumniAssociationId: formData.alumniAssociationId,
      backgroundImage: JSON.stringify(this.data.backgroundImageList)
    }

    this.setData({ submitting: true })
    try {
      const res = await merchantApi.publishMerchant(payload)
      const body = res.data || {}
      if (body.code === 200) {
        wx.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1000)
      } else {
        wx.showToast({ title: body.message || '发布失败', icon: 'none' })
      }
    } catch (err) {
      wx.showToast({ title: '系统错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
