const { couponApi } = require('../../../../../api/api.js')
const { uploadImage } = require('../../../../../utils/fileUpload.js')
const { get } = require('../../../../../utils/request.js')

function pad2(n) {
  const s = String(n)
  return s.length < 2 ? '0' + s : s
}

function todayDateStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

function addDaysDateStr(days) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

function toDateTimeLocal(dateStr, timeStr) {
  if (!dateStr || !timeStr) {
    return ''
  }
  const d = new Date(`${dateStr.replace(/-/g, '/')} ${timeStr}`)
  if (isNaN(d.getTime())) {
    return ''
  }
  return `${dateStr}T${timeStr}:00`
}

function splitDateTime(value, defaultDate, defaultTime) {
  if (!value) {
    return { date: defaultDate, time: defaultTime }
  }
  const d = new Date(value)
  if (!isNaN(d.getTime())) {
    return {
      date: `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`,
      time: `${pad2(d.getHours())}:${pad2(d.getMinutes())}`,
    }
  }
  const s = String(value).replace('T', ' ').trim()
  if (s.length >= 16) {
    return {
      date: s.slice(0, 10).replace(/\//g, '-'),
      time: s.slice(11, 16),
    }
  }
  return { date: defaultDate, time: defaultTime }
}

Page({
  data: {
    couponId: '',
    loading: false,
    submitting: false,
    selectedMerchantId: '',
    selectedMerchantName: '',

    shopList: [],
    shopPickerRange: ['全部门店（不限制）'],
    shopIndex: 0,

    couponTypeLabels: ['折扣券', '满减券', '礼品券'],
    couponTypeValues: [1, 2, 3],
    couponTypeIndex: 0,

    discountTypeLabels: ['固定金额', '折扣比例'],
    discountTypeValues: [1, 2],
    discountTypeIndex: 1,

    publishLabels: ['立即发布', '定时发布'],
    publishValues: [1, 2],
    publishIndex: 0,

    formData: {
      couponName: '',
      couponDesc: '',
      couponImage: '',
      couponType: 1,
      discountType: 2,
      discountValue: '',
      minSpend: '',
      maxDiscount: '',
      totalQuantity: '100',
      perUserLimit: '1',
      isAlumniOnly: 0,
      useTimeLimit: '',
      publishType: 1,
      validStartDate: todayDateStr(),
      validStartTime: '00:00',
      validEndDate: addDaysDateStr(30),
      validEndTime: '23:59',
      publishDate: todayDateStr(),
      publishTime: '09:00',
    },
  },

  onLoad(options) {
    const couponId = options.couponId ? decodeURIComponent(options.couponId) : ''
    if (!couponId) {
      wx.showToast({ title: '优惠券ID缺失', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1200)
      return
    }
    this.setData({
      couponId: String(couponId),
      selectedMerchantId: options.merchantId ? decodeURIComponent(options.merchantId) : '',
      selectedMerchantName: decodeURIComponent(options.merchantName || ''),
    })
    this.loadCouponDetail()
  },

  async loadCouponDetail() {
    this.setData({ loading: true })
    try {
      const res = await couponApi.getManagementCouponDetail(this.data.couponId)
      if (!(res.data && res.data.code === 200 && res.data.data && res.data.data.coupon)) {
        wx.showToast({ title: (res.data && res.data.msg) || '加载失败', icon: 'none' })
        this.setData({ loading: false })
        return
      }

      const coupon = res.data.data.coupon || {}
      const merchantId = coupon.merchantId != null ? String(coupon.merchantId) : this.data.selectedMerchantId
      const couponType = Number(coupon.couponType || 1)
      const publishType = Number(coupon.publishType || 1)
      let discountType = Number(coupon.discountType || 2)
      if (couponType === 2 || couponType === 3) {
        discountType = 1
      }

      const couponTypeIndex = Math.max(0, this.data.couponTypeValues.indexOf(couponType))
      const discountTypeIndex = Math.max(0, this.data.discountTypeValues.indexOf(discountType))
      const publishIndex = Math.max(0, this.data.publishValues.indexOf(publishType))

      const start = splitDateTime(coupon.validStartTime, todayDateStr(), '00:00')
      const end = splitDateTime(coupon.validEndTime, addDaysDateStr(30), '23:59')
      const publish = splitDateTime(coupon.publishTime, todayDateStr(), '09:00')

      this.setData({
        selectedMerchantId: merchantId,
        selectedMerchantName: this.data.selectedMerchantName || coupon.merchantName || '',
        couponTypeIndex,
        discountTypeIndex,
        publishIndex,
        formData: {
          couponName: coupon.couponName || '',
          couponDesc: coupon.couponDesc || '',
          couponImage: coupon.couponImage || '',
          couponType,
          discountType,
          discountValue:
            coupon.discountValue != null && coupon.discountValue !== ''
              ? String(coupon.discountValue)
              : '',
          minSpend:
            coupon.minSpend != null && coupon.minSpend !== '' ? String(coupon.minSpend) : '',
          maxDiscount:
            coupon.maxDiscount != null && coupon.maxDiscount !== ''
              ? String(coupon.maxDiscount)
              : '',
          totalQuantity:
            coupon.totalQuantity != null && coupon.totalQuantity !== ''
              ? String(coupon.totalQuantity)
              : '',
          perUserLimit:
            coupon.perUserLimit != null && coupon.perUserLimit !== ''
              ? String(coupon.perUserLimit)
              : '1',
          isAlumniOnly: Number(coupon.isAlumniOnly || 0),
          useTimeLimit: coupon.useTimeLimit || '',
          publishType,
          validStartDate: start.date,
          validStartTime: start.time,
          validEndDate: end.date,
          validEndTime: end.time,
          publishDate: publish.date,
          publishTime: publish.time,
        },
      })

      await this.loadShops(merchantId, coupon.shopId)
    } catch (e) {
      console.error(e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadShops(merchantId, selectedShopId) {
    if (!merchantId) {
      this.setData({
        shopList: [],
        shopPickerRange: ['全部门店（不限制）'],
        shopIndex: 0,
      })
      return
    }
    try {
      const res = await get(`/shop/list/${merchantId}`)
      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || res.data.data || []
        const list = Array.isArray(records) ? records : []
        const shopPickerRange = ['全部门店（不限制）'].concat(
          list.map(s => s.shopName || `店铺${s.shopId}`)
        )
        let shopIndex = 0
        if (selectedShopId != null && selectedShopId !== '') {
          const target = String(selectedShopId)
          const idx = list.findIndex(s => String(s.shopId) === target)
          shopIndex = idx >= 0 ? idx + 1 : 0
        }
        this.setData({ shopList: list, shopPickerRange, shopIndex })
      }
    } catch (e) {
      console.error(e)
      this.setData({
        shopList: [],
        shopPickerRange: ['全部门店（不限制）'],
        shopIndex: 0,
      })
    }
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [`formData.${field}`]: e.detail.value })
  },

  onNumberInput(e) {
    const { field } = e.currentTarget.dataset
    let v = e.detail.value
    if (field === 'totalQuantity' && v === '-') {
      v = '-'
    }
    this.setData({ [`formData.${field}`]: v })
  },

  onCouponTypeChange(e) {
    const idx = Number(e.detail.value)
    const couponType = this.data.couponTypeValues[idx]
    let discountType = 2
    let discountTypeIndex = 1
    if (couponType === 2 || couponType === 3) {
      discountType = 1
      discountTypeIndex = 0
    }
    this.setData({
      couponTypeIndex: idx,
      discountTypeIndex,
      'formData.couponType': couponType,
      'formData.discountType': discountType,
    })
  },

  onDiscountTypeRadio(e) {
    const v = Number(e.currentTarget.dataset.value)
    if (v !== 1 && v !== 2) {
      return
    }
    const dtIdx = this.data.discountTypeValues.indexOf(v)
    this.setData({
      'formData.discountType': v,
      discountTypeIndex: dtIdx >= 0 ? dtIdx : 0,
    })
  },

  onShopChange(e) {
    this.setData({ shopIndex: Number(e.detail.value) })
  },

  onPublishTypeChange(e) {
    const idx = Number(e.detail.value)
    const publishType = this.data.publishValues[idx]
    this.setData({
      publishIndex: idx,
      'formData.publishType': publishType,
    })
  },

  onAlumniSwitch(e) {
    this.setData({ 'formData.isAlumniOnly': e.detail.value ? 1 : 0 })
  },

  onValidStartDateChange(e) {
    this.setData({ 'formData.validStartDate': e.detail.value })
  },
  onValidStartTimeChange(e) {
    this.setData({ 'formData.validStartTime': e.detail.value })
  },
  onValidEndDateChange(e) {
    this.setData({ 'formData.validEndDate': e.detail.value })
  },
  onValidEndTimeChange(e) {
    this.setData({ 'formData.validEndTime': e.detail.value })
  },
  onPublishDateChange(e) {
    this.setData({ 'formData.publishDate': e.detail.value })
  },
  onPublishTimeChange(e) {
    this.setData({ 'formData.publishTime': e.detail.value })
  },

  clearCouponImage() {
    this.setData({ 'formData.couponImage': '' })
  },

  chooseCouponImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async res => {
        const paths = res.tempFilePaths || []
        if (!paths.length) {
          return
        }
        try {
          wx.showLoading({ title: '上传中...', mask: true })
          const uploadRes = await uploadImage(paths[0], '/file/upload/images', 'image')
          if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.fileUrl) {
            this.setData({ 'formData.couponImage': uploadRes.data.fileUrl })
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

  validate() {
    const { formData, selectedMerchantId, shopIndex, shopList, couponId } = this.data
    if (!couponId) {
      wx.showToast({ title: '优惠券ID缺失', icon: 'none' })
      return null
    }
    if (!selectedMerchantId) {
      wx.showToast({ title: '缺少商户信息', icon: 'none' })
      return null
    }
    const name = (formData.couponName || '').trim()
    if (!name) {
      wx.showToast({ title: '请填写优惠券名称', icon: 'none' })
      return null
    }

    const parseOptNumber = (raw, label) => {
      const t = raw != null && raw !== '' ? String(raw).trim() : ''
      if (t === '') {
        return null
      }
      const n = Number(t)
      if (Number.isNaN(n)) {
        wx.showToast({ title: `${label}格式不正确`, icon: 'none' })
        return undefined
      }
      return n
    }

    const parseRequiredNumber = (raw, errMsg) => {
      const t = raw != null && raw !== '' ? String(raw).trim() : ''
      if (t === '') {
        wx.showToast({ title: errMsg, icon: 'none' })
        return undefined
      }
      const n = Number(t)
      if (Number.isNaN(n)) {
        wx.showToast({ title: errMsg, icon: 'none' })
        return undefined
      }
      return n
    }

    const couponType = Number(formData.couponType)
    let discountValueParsed = null
    let minSpendParsed = null
    let maxDiscountParsed = null

    if (couponType === 1) {
      const md = parseRequiredNumber(formData.maxDiscount, '请填写最高优惠金额')
      if (md === undefined) {
        return null
      }
      maxDiscountParsed = md
      const dv = parseRequiredNumber(formData.discountValue, '请填写优惠值')
      if (dv === undefined) {
        return null
      }
      discountValueParsed = dv
      const ms = parseOptNumber(formData.minSpend, '门槛')
      if (ms === undefined) {
        return null
      }
      minSpendParsed = ms
    } else if (couponType === 2) {
      const ms = parseRequiredNumber(formData.minSpend, '请填写使用门槛')
      if (ms === undefined) {
        return null
      }
      minSpendParsed = ms
      const dv = parseRequiredNumber(formData.discountValue, '请填写减免金额')
      if (dv === undefined) {
        return null
      }
      discountValueParsed = dv
    } else {
      const dv = parseRequiredNumber(formData.discountValue, '请填写礼品价值')
      if (dv === undefined) {
        return null
      }
      discountValueParsed = dv
    }

    let totalQuantity = parseInt(formData.totalQuantity, 10)
    if (formData.totalQuantity === '-1') {
      totalQuantity = -1
    } else if (Number.isNaN(totalQuantity)) {
      wx.showToast({ title: '请填写发行总量', icon: 'none' })
      return null
    }

    const validStartTime = toDateTimeLocal(formData.validStartDate, formData.validStartTime)
    const validEndTime = toDateTimeLocal(formData.validEndDate, formData.validEndTime)
    if (!validStartTime || !validEndTime) {
      wx.showToast({ title: '请完善有效期', icon: 'none' })
      return null
    }

    const payload = {
      couponId: String(couponId),
      merchantId: String(selectedMerchantId),
      couponName: name,
      couponType,
      discountValue: discountValueParsed,
      totalQuantity,
      validStartTime,
      validEndTime,
      publishType: Number(formData.publishType),
    }

    if (couponType === 2) {
      payload.discountType = 1
      payload.minSpend = minSpendParsed
    } else if (couponType === 1) {
      payload.discountType = Number(formData.discountType)
      payload.maxDiscount = maxDiscountParsed
      if (minSpendParsed !== null) {
        payload.minSpend = minSpendParsed
      }
    } else {
      payload.discountType = 1
    }

    if (shopIndex > 0 && shopList[shopIndex - 1]) {
      const sid = shopList[shopIndex - 1].shopId
      if (sid != null && sid !== '') {
        payload.shopId = String(sid)
      }
    }

    const desc = (formData.couponDesc || '').trim()
    if (desc) {
      payload.couponDesc = desc
    }
    const img = (formData.couponImage || '').trim()
    if (img) {
      payload.couponImage = img
    }

    const perUserLimitRaw = (formData.perUserLimit || '').trim()
    if (perUserLimitRaw !== '') {
      const perUserLimit = parseInt(perUserLimitRaw, 10)
      if (Number.isNaN(perUserLimit)) {
        wx.showToast({ title: '每人限领格式错误', icon: 'none' })
        return null
      }
      payload.perUserLimit = perUserLimit
    }
    payload.isAlumniOnly = Number(formData.isAlumniOnly)

    const utl = (formData.useTimeLimit || '').trim()
    if (utl) {
      payload.useTimeLimit = utl
    }

    if (payload.publishType === 2) {
      const publishTime = toDateTimeLocal(formData.publishDate, formData.publishTime)
      if (!publishTime) {
        wx.showToast({ title: '请设置定时发布时间', icon: 'none' })
        return null
      }
      payload.publishTime = publishTime
    }

    return payload
  },

  async submitForm() {
    if (this.data.submitting) {
      return
    }
    const payload = this.validate()
    if (!payload) {
      return
    }
    this.setData({ submitting: true })
    wx.showLoading({ title: '提交中...', mask: true })
    try {
      const res = await couponApi.updateManagementCoupon(payload)
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '更新成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 800)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '更新失败',
          icon: 'none',
        })
      }
    } catch (e) {
      console.error(e)
      wx.showToast({ title: '更新失败', icon: 'none' })
    } finally {
      wx.hideLoading()
      this.setData({ submitting: false })
    }
  },
})
