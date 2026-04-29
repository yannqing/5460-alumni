// pages/merchant/activity/publish/publish.js
const app = getApp()
const fileUploadUtil = require('../../../../utils/fileUpload.js')
const { fileApi, activityApi, merchantApi } = require('../../../../api/api.js')

Page({
  data: {
    merchantId: '',
    merchantName: '',
    shopList: [],
    selectedShopIds: [],
    selectAllShops: true,
    formData: {
      activityType: 1,
      activityTitle: '',
      coverImage: '',
      activityImages: '',
      description: '',
      startTime: '',
      endTime: '',
      isSignup: 0,
      registrationStartTime: '',
      registrationEndTime: '',
      maxParticipants: null,
      province: '',
      city: '',
      district: '',
      address: '',
      latitude: 0,
      longitude: 0,
      isNeedReview: 0,
      contactPerson: '',
      contactPhone: '',
      contactEmail: '',
      remark: '',
    },
    activityImagesList: [],
    activityTypeOptions: [
      { id: 1, name: '优惠活动' },
      { id: 2, name: '话题活动' },
    ],
    isSignupOptions: [
      { id: 0, name: '否' },
      { id: 1, name: '是' },
    ],
    isNeedReviewOptions: [
      { id: 0, name: '无需审核' },
      { id: 1, name: '需要审核' },
    ],
    submitting: false,
    startTimeDisplay: '',
    endTimeDisplay: '',
    registrationStartTimeDisplay: '',
    registrationEndTimeDisplay: '',
  },

  onLoad(options) {
    const merchantId = options.merchantId || ''
    const merchantName = options.merchantName || ''
    this.setData({ merchantId, merchantName })
    this.loadShopList()
  },

  async loadShopList() {
    const { merchantId } = this.data
    if (!merchantId) return
    try {
      const res = await merchantApi.getMerchantInfo(merchantId)
      if (res.data && res.data.code === 200 && res.data.data) {
        const shops = (res.data.data.shops || []).map(s => ({
          shopId: s.shopId,
          shopName: s.shopName || s.address || '未知门店',
        }))
        this.setData({
          shopList: shops,
          selectedShopIds: shops.map(s => s.shopId),
        })
      }
    } catch (e) {
      console.error('[PublishActivity] 加载门店列表失败:', e)
    }
  },

  onActivityTypeChange(e) {
    const activityType = Number(e.detail.value)
    this.setData({
      'formData.activityType': activityType,
      'formData.isSignup': activityType === 1 ? 0 : this.data.formData.isSignup,
    })
  },

  onTitleInput(e) {
    this.setData({ 'formData.activityTitle': e.detail.value })
  },

  onDescriptionInput(e) {
    this.setData({ 'formData.description': e.detail.value })
  },

  onStartTimeChange(e) {
    this.setData({
      'formData.startTime': e.detail.value,
      startTimeDisplay: e.detail.value,
    })
  },

  onEndTimeChange(e) {
    this.setData({
      'formData.endTime': e.detail.value,
      endTimeDisplay: e.detail.value,
    })
  },

  onIsSignupChange(e) {
    this.setData({ 'formData.isSignup': Number(e.detail.value) })
  },

  onRegistrationStartTimeChange(e) {
    this.setData({
      'formData.registrationStartTime': e.detail.value,
      registrationStartTimeDisplay: e.detail.value,
    })
  },

  onRegistrationEndTimeChange(e) {
    this.setData({
      'formData.registrationEndTime': e.detail.value,
      registrationEndTimeDisplay: e.detail.value,
    })
  },

  onIsNeedReviewChange(e) {
    this.setData({ 'formData.isNeedReview': Number(e.detail.value) })
  },

  onMaxParticipantsInput(e) {
    this.setData({ 'formData.maxParticipants': e.detail.value ? Number(e.detail.value) : null })
  },

  onAddressInput(e) {
    this.setData({ 'formData.address': e.detail.value })
  },

  onContactPersonInput(e) {
    this.setData({ 'formData.contactPerson': e.detail.value })
  },

  onContactPhoneInput(e) {
    this.setData({ 'formData.contactPhone': e.detail.value })
  },

  onRemarkInput(e) {
    this.setData({ 'formData.remark': e.detail.value })
  },

  async chooseCoverImage() {
    try {
      const res = await wx.chooseMedia({ count: 1, mediaType: ['image'] })
      const tempFilePath = res.tempFiles[0].tempFilePath
      wx.showLoading({ title: '上传中...' })
      const uploadRes = await fileApi.uploadImage(tempFilePath)
      wx.hideLoading()
      if (uploadRes && uploadRes.code === 200) {
        const imageUrl =
          typeof uploadRes.data === 'string'
            ? uploadRes.data
            : uploadRes.data?.fileUrl || uploadRes.data?.fileId || ''
        this.setData({ 'formData.coverImage': imageUrl })
      } else {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    } catch (e) {
      wx.hideLoading()
      console.error('[PublishActivity] 上传封面图失败:', e)
    }
  },

  async chooseActivityImages() {
    const remaining = 9 - this.data.activityImagesList.length
    if (remaining <= 0) {
      wx.showToast({ title: '最多上传9张图片', icon: 'none' })
      return
    }
    try {
      const res = await wx.chooseMedia({ count: remaining, mediaType: ['image'] })
      wx.showLoading({ title: '上传中...' })
      const uploadedUrls = []
      for (const file of res.tempFiles) {
        const uploadRes = await fileApi.uploadImage(file.tempFilePath)
        if (uploadRes && uploadRes.code === 200) {
          const url =
            typeof uploadRes.data === 'string'
              ? uploadRes.data
              : uploadRes.data?.fileUrl || uploadRes.data?.fileId || ''
          uploadedUrls.push(url)
        }
      }
      wx.hideLoading()
      const newList = [...this.data.activityImagesList, ...uploadedUrls]
      this.setData({
        activityImagesList: newList,
        'formData.activityImages': JSON.stringify(newList),
      })
    } catch (e) {
      wx.hideLoading()
      console.error('[PublishActivity] 上传活动图片失败:', e)
    }
  },

  removeActivityImage(e) {
    const idx = e.currentTarget.dataset.index
    const newList = this.data.activityImagesList.filter((_, i) => i !== idx)
    this.setData({
      activityImagesList: newList,
      'formData.activityImages': JSON.stringify(newList),
    })
  },

  toggleSelectAllShops() {
    const selectAll = !this.data.selectAllShops
    this.setData({
      selectAllShops: selectAll,
      selectedShopIds: selectAll ? this.data.shopList.map(s => s.shopId) : [],
    })
  },

  toggleShopSelection(e) {
    const shopId = e.currentTarget.dataset.id
    let { selectedShopIds } = this.data
    const idx = selectedShopIds.indexOf(shopId)
    if (idx > -1) {
      selectedShopIds = selectedShopIds.filter(id => id !== shopId)
    } else {
      selectedShopIds = [...selectedShopIds, shopId]
    }
    this.setData({
      selectedShopIds,
      selectAllShops: selectedShopIds.length === this.data.shopList.length,
    })
  },

  async handleSubmit() {
    const { formData, merchantId, selectAllShops, selectedShopIds } = this.data

    // 校验
    if (!formData.activityTitle.trim()) {
      wx.showToast({ title: '请输入活动标题', icon: 'none' })
      return
    }
    if (!formData.coverImage) {
      wx.showToast({ title: '请上传封面图', icon: 'none' })
      return
    }
    if (!formData.description.trim()) {
      wx.showToast({ title: '请输入活动描述', icon: 'none' })
      return
    }
    if (!formData.startTime) {
      wx.showToast({ title: '请选择开始时间', icon: 'none' })
      return
    }
    if (!formData.endTime) {
      wx.showToast({ title: '请选择结束时间', icon: 'none' })
      return
    }
    if (formData.activityType === 2 && formData.isSignup === 1) {
      if (!formData.registrationStartTime) {
        wx.showToast({ title: '请选择报名开始时间', icon: 'none' })
        return
      }
      if (!formData.registrationEndTime) {
        wx.showToast({ title: '请选择报名截止时间', icon: 'none' })
        return
      }
    }

    this.setData({ submitting: true })

    try {
      const submitData = {
        ...(merchantId ? { merchantId: Number(merchantId) } : {}),
        activityType: formData.activityType,
        shopIds: selectAllShops ? [] : selectedShopIds,
        activityTitle: formData.activityTitle.trim(),
        coverImage: formData.coverImage,
        activityImages: formData.activityImages,
        description: formData.description.trim(),
        startTime: formData.startTime + 'T00:00:00',
        endTime: formData.endTime + 'T00:00:00',
        isSignup: formData.activityType === 1 ? 0 : formData.isSignup,
        registrationStartTime: formData.registrationStartTime
          ? formData.registrationStartTime + 'T00:00:00'
          : null,
        registrationEndTime: formData.registrationEndTime
          ? formData.registrationEndTime + 'T00:00:00'
          : null,
        maxParticipants: formData.maxParticipants,
        province: formData.province,
        city: formData.city,
        district: formData.district,
        address: formData.address,
        latitude: formData.latitude,
        longitude: formData.longitude,
        isNeedReview: formData.isNeedReview,
        contactPerson: formData.contactPerson,
        contactPhone: formData.contactPhone,
        contactEmail: formData.contactEmail,
        remark: formData.remark,
      }

      const res = await activityApi.publishMerchantActivity(submitData)

      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '发布失败', icon: 'none' })
      }
    } catch (e) {
      console.error('[PublishActivity] 发布失败:', e)
      wx.showToast({ title: '发布失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
})
