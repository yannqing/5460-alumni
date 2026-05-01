const { associationApi, fileApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    detailLoading: true,
    submitting: false,
    associationIdStr: '',
    pageMode: 'perfect',
    formData: {
      associationName: '',
      schoolName: '',
      location: '',
      zhName: '',
      zhRole: '',
      zhPhone: '',
      zhSocialAffiliation: '',
      associationProfile: '',
      bgImg: '',
    },
    bgImageList: [],
    bgImagePreviewUrls: [],
  },

  onLoad(options) {
    const associationIdStr = options.associationId ? String(options.associationId) : ''
    this.setData({ associationIdStr })
    this.loadAssociationDetail(associationIdStr)
  },

  parseBgImages(raw) {
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

  syncBgImagePreviews() {
    const list = this.data.bgImageList || []
    const urls = list.map(u => {
      const s = String(u).trim()
      if (!s) return ''
      return /^https?:\/\//i.test(s) ? s : config.getImageUrl(s)
    })
    this.setData({ bgImagePreviewUrls: urls })
  },

  async loadAssociationDetail(associationIdStr) {
    this.setData({ detailLoading: true })
    try {
      const res = await associationApi.getAssociationDetail(associationIdStr)
      const body = res.data || {}
      if (body.code !== 200 || !body.data) {
        wx.showToast({ title: body.message || '加载失败', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
      const data = body.data
      const bgList = this.parseBgImages(data.bgImg)

      this.setData({
        formData: {
          associationName: data.associationName || '',
          schoolName: data.schoolInfo?.schoolName || '',
          location: data.location || '',
          zhName: data.zhName || '',
          zhRole: data.zhRole || '',
          zhPhone: data.zhPhone || '',
          zhSocialAffiliation: data.zhSocialAffiliation || '',
          associationProfile: data.associationProfile || '',
          bgImg: data.bgImg || '',
        },
        bgImageList: bgList,
        detailLoading: false,
      })
      this.syncBgImagePreviews()
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ detailLoading: false })
      setTimeout(() => wx.navigateBack(), 1500)
    }
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({ [`formData.${field}`]: value })
  },

  async chooseBgImage() {
    const remain = 9 - this.data.bgImageList.length
    if (remain <= 0) return

    try {
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: remain,
          mediaType: ['image'],
          sizeType: ['compressed'],
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) return

      wx.showLoading({ title: '上传中...', mask: true })
      const next = [...this.data.bgImageList]

      for (const file of tempFiles) {
        const uploadRes = await fileApi.uploadImage(
          file.tempFilePath,
          file.name || 'bg.jpg',
          file.size
        )
        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          const rawUrl = uploadRes.data.fileUrl || ''
          if (rawUrl) {
            next.push(rawUrl)
          }
        }
      }

      this.setData({ bgImageList: next }, () => this.syncBgImagePreviews())
      wx.showToast({ title: '上传完成', icon: 'success' })
    } catch (error) {
      if (error?.errMsg !== 'chooseMedia:fail cancel') {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    } finally {
      wx.hideLoading()
    }
  },

  removeBgImage(e) {
    const { index } = e.currentTarget.dataset
    const list = [...this.data.bgImageList]
    list.splice(index, 1)
    this.setData({ bgImageList: list }, () => this.syncBgImagePreviews())
  },

  async submitForm() {
    const { associationIdStr, formData, bgImageList } = this.data

    if (!formData.associationProfile?.trim()) {
      return wx.showToast({ title: '请输入校友会简介', icon: 'none' })
    }

    const payload = {
      associationId: associationIdStr,
      associationProfile: formData.associationProfile.trim(),
      bgImg: JSON.stringify(bgImageList),
    }

    if (formData.zhName) payload.zhName = formData.zhName.trim()
    if (formData.zhRole) payload.zhRole = formData.zhRole.trim()
    if (formData.zhPhone) payload.zhPhone = formData.zhPhone.trim()
    if (formData.zhSocialAffiliation) payload.zhSocialAffiliation = formData.zhSocialAffiliation.trim()

    this.setData({ submitting: true })
    try {
      const updateRes = await associationApi.updateAssociationInfo(payload)
      if (updateRes.data?.code !== 200) {
        wx.showToast({ title: updateRes.data?.message || '保存失败', icon: 'none' })
        return
      }

      const publishRes = await associationApi.publishAssociation(associationIdStr)
      if (publishRes.data?.code === 200) {
        wx.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1000)
      } else {
        wx.showToast({ title: publishRes.data?.message || '发布失败', icon: 'none' })
      }
    } catch (err) {
      wx.showToast({ title: '系统错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
})
