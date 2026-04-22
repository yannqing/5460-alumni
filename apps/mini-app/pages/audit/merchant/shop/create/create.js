const { uploadImage } = require('../../../../../utils/fileUpload.js')

Page({
  data: {
    loading: false,
    submitting: false,
    showMerchantPicker: false,
    merchantList: [],
    selectedMerchantId: '',
    selectedMerchantName: '',
    uploadedImages: [],
    formData: {
      merchantId: '',
      shopName: '',
      shopType: 1,
      province: '',
      city: '',
      district: '',
      address: '',
      locationName: '',
      latitude: '',
      longitude: '',
      phone: '',
      businessHours: '',
      shopImages: '',
      logo: '',
      description: '',
      status: 1,
    },
  },

  onLoad(options) {
    const merchantId = options.merchantId || ''
    const merchantName = decodeURIComponent(options.merchantName || '')
    if (merchantId) {
      this.setData({
        selectedMerchantId: merchantId,
        selectedMerchantName: merchantName,
        [`formData.merchantId`]: merchantId,
      })
    }
    this.loadMerchants()
  },

  async loadMerchants() {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../../utils/request.js')
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100,
      })
      if (res.data && res.data.code === 200) {
        const records = res.data.data.records || []
        // 双重兜底：前端仅展示审核通过且启用的商户
        const merchantList = records.filter(item => item && item.reviewStatus === 1 && item.status === 1)
        this.setData({ merchantList })
        if (!this.data.selectedMerchantId && merchantList.length > 0) {
          const firstMerchant = merchantList[0]
          const merchantIdStr = `${firstMerchant.merchantId}`
          this.setData({
            selectedMerchantId: merchantIdStr,
            selectedMerchantName: firstMerchant.merchantName || '',
            [`formData.merchantId`]: merchantIdStr,
          })
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none',
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName
    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false,
      [`formData.merchantId`]: merchantId,
    })
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`formData.${field}`]: value,
    })
  },

  onRadioChange(e) {
    const { field, value } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: parseInt(value, 10),
    })
  },

  /** 选择店铺地理位置 */
  selectLocation() {
    wx.chooseLocation({
      success: res => {
        console.log('选择位置成功:', res)
        this.setData({
          [`formData.latitude`]: res.latitude,
          [`formData.longitude`]: res.longitude,
          [`formData.locationName`]: res.name || res.address,
          // 如果用户还没填详细地址，自动填入地图选中的地址
          [`formData.address`]: this.data.formData.address || res.address,
        })
        wx.showToast({
          title: '位置已选择',
          icon: 'success',
        })
      },
      fail: err => {
        console.error('选择位置失败:', err)
        if (err.errMsg && err.errMsg.indexOf('auth deny') !== -1) {
          wx.showModal({
            title: '授权提示',
            content: '需要位置权限才能选择店铺位置',
            success: res => {
              if (res.confirm) {
                wx.openSetting()
              }
            },
          })
        }
      },
    })
  },

  /** 店铺 Logo：单张上传，写入 formData.logo（图片 URL） */
  chooseLogo() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async res => {
        const tempFilePaths = res.tempFilePaths || []
        if (!tempFilePaths.length) return
        try {
          wx.showLoading({ title: '上传中...', mask: true })
          const uploadRes = await uploadImage(tempFilePaths[0], '/file/upload/images', 'image')
          if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.fileUrl) {
            this.setData({
              [`formData.logo`]: uploadRes.data.fileUrl,
            })
            wx.showToast({ title: 'Logo 上传成功', icon: 'success' })
          } else {
            throw new Error(uploadRes.msg || '上传失败')
          }
        } catch (error) {
          console.error('上传 Logo 失败:', error)
          wx.showToast({
            title: (error && error.message) || '上传失败',
            icon: 'none',
          })
        } finally {
          wx.hideLoading()
        }
      },
      fail: err => {
        if (err.errMsg !== 'chooseImage:fail cancel') {
          wx.showToast({ title: '选择图片失败', icon: 'none' })
        }
      },
    })
  },

  clearLogo() {
    this.setData({ [`formData.logo`]: '' })
  },

  chooseImage() {
    wx.chooseImage({
      count: 9,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: res => {
        const tempFilePaths = res.tempFilePaths || []
        this.uploadSelectedImages(tempFilePaths)
      },
      fail: err => {
        console.error('选择图片失败:', err)
        if (err.errMsg !== 'chooseImage:fail cancel') {
          wx.showToast({
            title: '选择图片失败',
            icon: 'none',
          })
        }
      },
    })
  },

  async uploadSelectedImages(filePaths) {
    try {
      wx.showLoading({
        title: '上传中...',
        mask: true,
      })
      const uploadedImages = [...this.data.uploadedImages]
      for (const filePath of filePaths) {
        const uploadRes = await uploadImage(filePath, '/file/upload/images', 'image')
        if (uploadRes.code === 200 && uploadRes.data) {
          uploadedImages.push(uploadRes.data.fileUrl)
        } else {
          throw new Error(uploadRes.msg || '上传失败')
        }
      }
      this.setData({ uploadedImages })
      wx.showToast({
        title: '上传成功',
        icon: 'success',
      })
    } catch (error) {
      console.error('上传图片失败:', error)
      wx.showToast({
        title: error.msg || '上传失败',
        icon: 'none',
      })
    } finally {
      wx.hideLoading()
    }
  },

  deleteImage(e) {
    const { index } = e.currentTarget.dataset
    const uploadedImages = [...this.data.uploadedImages]
    uploadedImages.splice(index, 1)
    this.setData({ uploadedImages })
  },

  async submitShopForm() {
    try {
      const { formData, uploadedImages } = this.data
      if (!formData.merchantId) {
        wx.showToast({ title: '请选择所属商户', icon: 'none' })
        return
      }
      if (!formData.shopName) {
        wx.showToast({ title: '请输入店铺名称', icon: 'none' })
        return
      }
      if (!formData.province) {
        wx.showToast({ title: '请输入省份', icon: 'none' })
        return
      }
      if (!formData.city) {
        wx.showToast({ title: '请输入城市', icon: 'none' })
        return
      }
      if (!formData.district) {
        wx.showToast({ title: '请输入区县', icon: 'none' })
        return
      }
      if (!formData.address) {
        wx.showToast({ title: '请输入详细地址', icon: 'none' })
        return
      }
      if (!formData.latitude || !formData.longitude) {
        wx.showToast({ title: '请选择店铺位置', icon: 'none' })
        return
      }
      if (formData.phone && !/^\d{11}$/.test(formData.phone)) {
        wx.showToast({ title: '店铺电话须为11位数字', icon: 'none' })
        return
      }

      this.setData({ submitting: true })
      const shopImages = uploadedImages.length > 0 ? JSON.stringify(uploadedImages) : undefined
      const { post } = require('../../../../../utils/request.js')
      const res = await post('/shop/create', {
        merchantId: formData.merchantId,
        shopName: formData.shopName,
        shopType: formData.shopType,
        province: formData.province,
        city: formData.city,
        district: formData.district,
        address: formData.address,
        latitude: formData.latitude ? parseFloat(formData.latitude) : undefined,
        longitude: formData.longitude ? parseFloat(formData.longitude) : undefined,
        phone: formData.phone || undefined,
        businessHours: formData.businessHours || undefined,
        shopImages: shopImages,
        logo: formData.logo || undefined,
        description: formData.description || undefined,
      })

      if (res && res.data && res.data.code === 200) {
        wx.showToast({
          title: '创建成功',
          icon: 'success',
          duration: 1500,
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      }
    } catch (error) {
      console.error('创建店铺失败:', error)
    } finally {
      this.setData({ submitting: false })
    }
  },
})
