// pages/audit/merchant/info-maintenance/edit/edit.js
const { merchantApi, associationApi } = require('../../../../../api/api.js')
const config = require('../../../../../utils/config.js')
const { uploadImage } = require('../../../../../utils/fileUpload.js')

Page({
  data: {
    detailLoading: true,
    submitting: false,
    merchantIdStr: '',
    /** 关联校友会展示 */
    associationDisplayName: '',
    associationLogoUrl: '',
    associationNameFirstChar: '校',
    logoPreview: '',
    licensePreview: '',
    /** 背景图 URL 列表（相对路径或完整 URL，提交时 JSON.stringify） */
    backgroundImageList: [],
    /** 背景图预览完整地址（含 getImageUrl） */
    backgroundImagePreviewUrls: [],
    formData: {
      merchantName: '',
      merchantType: 1,
      businessLicense: '',
      unifiedSocialCreditCode: '',
      legalPerson: '',
      legalPersonId: '',
      contactPhone: '',
      contactEmail: '',
      businessScope: '',
      businessCategory: '',
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
    this.loadMerchantDetail(merchantIdStr)
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
        wx.showToast({
          title: body.message || body.msg || '加载失败',
          icon: 'none',
        })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
      const data = body.data
      if (data.merchantType !== 1) {
        wx.showToast({
          title: '仅校友商铺可在此编辑',
          icon: 'none',
        })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
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
        } catch (e) {
          console.warn('[edit] 拉取校友会详情失败', e)
        }
      }

      const nameTrim = (assocName || '').trim()
      const associationLogoUrl = logoRaw ? config.getImageUrl(String(logoRaw).trim()) : ''
      const associationNameFirstChar = nameTrim ? nameTrim.slice(0, 1) : '校'

      const bgList = this.parseBackgroundImage(data.backgroundImage)

      this.setData({
        formData: {
          merchantName: data.merchantName || '',
          merchantType: 1,
          businessLicense: data.businessLicense || '',
          unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
          legalPerson: data.legalPerson || '',
          legalPersonId:
            data.legalPersonId != null && data.legalPersonId !== undefined
              ? String(data.legalPersonId).trim()
              : '',
          contactPhone: data.contactPhone || '',
          contactEmail: data.contactEmail || '',
          businessScope: data.businessScope || '',
          businessCategory: data.businessCategory || '',
          logo: data.logo ? String(data.logo).trim() : '',
          alumniAssociationId: alumniIdStr,
        },
        backgroundImageList: bgList,
        associationDisplayName: nameTrim,
        associationLogoUrl,
        associationNameFirstChar,
        detailLoading: false,
      })
      this.updateImagePreviews()
      this.syncBackgroundImagePreviews()
    } catch (e) {
      console.error(e)
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

  clearLogo() {
    this.setData({ 'formData.logo': '' }, () => this.updateImagePreviews())
  },

  clearLicense() {
    this.setData({ 'formData.businessLicense': '' }, () => this.updateImagePreviews())
  },

  chooseLogo() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async res => {
        const paths = res.tempFilePaths || []
        if (!paths.length) return
        try {
          wx.showLoading({ title: '上传中...', mask: true })
          const uploadRes = await uploadImage(paths[0], '/file/upload/images', 'image')
          if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.fileUrl) {
            this.setData({ 'formData.logo': uploadRes.data.fileUrl }, () => this.updateImagePreviews())
            wx.showToast({ title: '上传成功', icon: 'success' })
          } else {
            throw new Error(uploadRes.msg || '上传失败')
          }
        } catch (err) {
          console.error(err)
          wx.showToast({ title: (err && err.message) || '上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      },
    })
  },

  chooseLicense() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async res => {
        const paths = res.tempFilePaths || []
        if (!paths.length) return
        try {
          wx.showLoading({ title: '上传中...', mask: true })
          const uploadRes = await uploadImage(paths[0], '/file/upload/images', 'image')
          if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.fileUrl) {
            this.setData({ 'formData.businessLicense': uploadRes.data.fileUrl }, () => this.updateImagePreviews())
            wx.showToast({ title: '上传成功', icon: 'success' })
          } else {
            throw new Error(uploadRes.msg || '上传失败')
          }
        } catch (err) {
          console.error(err)
          wx.showToast({ title: (err && err.message) || '上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      },
    })
  },

  chooseBackgroundImage() {
    const remain = 9 - (this.data.backgroundImageList || []).length
    if (remain <= 0) {
      wx.showToast({ title: '最多上传9张背景图', icon: 'none' })
      return
    }
    wx.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async res => {
        const paths = res.tempFilePaths || []
        if (!paths.length) return
        try {
          wx.showLoading({ title: '上传中...', mask: true })
          const next = [...(this.data.backgroundImageList || [])]
          for (const p of paths) {
            const uploadRes = await uploadImage(p, '/file/upload/images', 'image')
            if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.fileUrl) {
              next.push(uploadRes.data.fileUrl)
            } else {
              throw new Error(uploadRes.msg || '上传失败')
            }
          }
          this.setData({ backgroundImageList: next }, () => this.syncBackgroundImagePreviews())
          wx.showToast({ title: '上传成功', icon: 'success' })
        } catch (err) {
          console.error(err)
          wx.showToast({ title: (err && err.message) || '上传失败', icon: 'none' })
        } finally {
          wx.hideLoading()
        }
      },
    })
  },

  removeBackgroundImage(e) {
    const { index } = e.currentTarget.dataset
    const i = parseInt(index, 10)
    if (Number.isNaN(i)) return
    const list = [...(this.data.backgroundImageList || [])]
    list.splice(i, 1)
    this.setData({ backgroundImageList: list }, () => this.syncBackgroundImagePreviews())
  },

  async submitForm() {
    const { merchantIdStr, formData } = this.data
    const name = (formData.merchantName || '').trim()
    if (!name) {
      wx.showToast({ title: '请填写商家名称', icon: 'none' })
      return
    }

    const aidStr = String(formData.alumniAssociationId || '').trim()
    if (!aidStr) {
      wx.showToast({ title: '请填写关联校友会ID', icon: 'none' })
      return
    }
    if (!/^\d+$/.test(aidStr)) {
      wx.showToast({ title: '校友会ID须为数字', icon: 'none' })
      return
    }

    const creditRaw = (formData.unifiedSocialCreditCode || '').trim()
    if (!creditRaw) {
      wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
      return
    }
    const normalizedCreditCode = creditRaw.toUpperCase()
    if (normalizedCreditCode.length !== 18) {
      wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' })
      return
    }
    if (!/^[0-9]{18}$/.test(normalizedCreditCode)) {
      wx.showToast({ title: '统一社会信用代码须为18位数字', icon: 'none' })
      return
    }

    const legalPerson = (formData.legalPerson || '').trim()
    if (!legalPerson) {
      wx.showToast({ title: '请输入法人姓名', icon: 'none' })
      return
    }
    if (legalPerson.length > 50) {
      wx.showToast({ title: '法人姓名不能超过50个字符', icon: 'none' })
      return
    }

    const legalPersonIdRaw = (formData.legalPersonId || '').trim()
    if (!legalPersonIdRaw) {
      wx.showToast({ title: '请输入法人身份证号', icon: 'none' })
      return
    }
    if (legalPersonIdRaw.length !== 18) {
      wx.showToast({ title: '法人身份证号须为18位', icon: 'none' })
      return
    }
    if (!/^\d{17}[\dXx]$/.test(legalPersonIdRaw)) {
      wx.showToast({ title: '法人身份证号格式不正确', icon: 'none' })
      return
    }
    const normalizedLegalPersonId = legalPersonIdRaw.toUpperCase()

    const contactPhone = String(formData.contactPhone || '').trim()
    if (!contactPhone) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return
    }
    if (!/^\d{11}$/.test(contactPhone)) {
      wx.showToast({ title: '联系电话须为11位数字', icon: 'none' })
      return
    }

    const businessLicense = String(formData.businessLicense || '').trim()
    if (!businessLicense) {
      wx.showToast({ title: '请上传营业执照', icon: 'none' })
      return
    }

    const payload = {
      merchantId: merchantIdStr,
      merchantName: name,
      merchantType: 1,
      unifiedSocialCreditCode: normalizedCreditCode,
      legalPerson,
      legalPersonId: normalizedLegalPersonId,
      contactPhone,
      businessLicense,
    }

    const addStr = (key, val) => {
      if (val !== undefined && val !== null && String(val).trim() !== '') {
        payload[key] = typeof val === 'string' ? val.trim() : val
      }
    }
    addStr('contactEmail', formData.contactEmail)
    if (formData.businessScope) {
      payload.businessScope = formData.businessScope
    }
    addStr('businessCategory', formData.businessCategory)
    addStr('logo', formData.logo)
    payload.backgroundImage = JSON.stringify(this.data.backgroundImageList || [])

    // 雪花 ID 超过 JS 安全整数，必须用字符串提交，避免 parseInt/Number 丢精度
    payload.alumniAssociationId = aidStr

    this.setData({ submitting: true })
    try {
      const res = await merchantApi.updateMerchantInfo(payload)
      const body = res.data || {}
      if (body.code === 200) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 500)
      } else {
        wx.showToast({
          title: body.message || body.msg || '保存失败',
          icon: 'none',
        })
      }
    } catch (err) {
      console.error(err)
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
})
