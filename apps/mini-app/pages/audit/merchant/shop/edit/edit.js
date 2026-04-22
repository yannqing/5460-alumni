const { uploadImage } = require('../../../../../utils/fileUpload.js')

Page({
  data: {
    detailLoading: true,
    submitting: false,
    merchantDisplayName: '',
    currentShopId: '',
    uploadedImages: [],
    formData: {
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

  async onLoad(options) {
    const shopId = options.shopId
    if (!shopId) {
      wx.showToast({ title: '缺少店铺信息', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ currentShopId: shopId })
    try {
      const ok = await this.loadShopDetail(shopId)
      if (ok) {
        this.setData({ detailLoading: false })
      }
    } catch (e) {
      console.error(e)
      this.setData({ detailLoading: false })
    }
  },

  async loadShopDetail(shopId) {
    const { merchantApi } = require('../../../../../api/api.js')
    const res = await merchantApi.getShopDetailForEdit(shopId)
    if (!res.data || res.data.code !== 200 || !res.data.data) {
      wx.showToast({
        title: res.data?.msg || '加载店铺失败',
        icon: 'none',
      })
      setTimeout(() => wx.navigateBack(), 1500)
      return false
    }

    const shop = res.data.data
    let uploadedImages = []
    if (shop.shopImages) {
      try {
        uploadedImages = JSON.parse(shop.shopImages)
      } catch (error) {
        console.error('解析店铺图片失败:', error)
        uploadedImages = []
      }
    }

    const m = shop.merchant || {}
    const rawMid = m.merchantId != null && m.merchantId !== '' ? m.merchantId : shop.merchantId
    const merchantIdStr = rawMid != null && rawMid !== '' ? String(rawMid) : ''
    const merchantDisplayName =
      m.merchantName || shop.merchantName || (merchantIdStr ? `商户ID ${merchantIdStr}` : '—')

    this.setData({
      merchantDisplayName,
      uploadedImages,
      formData: {
        shopName: shop.shopName || '',
        shopType: shop.shopType || 1,
        province: shop.province || '',
        city: shop.city || '',
        district: shop.district || '',
        address: shop.address || '',
        locationName: shop.locationName || shop.address || '',
        latitude: shop.latitude != null ? String(shop.latitude) : '',
        longitude: shop.longitude != null ? String(shop.longitude) : '',
        phone: shop.phone || '',
        businessHours: shop.businessHours || '',
        shopImages: shop.shopImages || '',
        logo: shop.logo || '',
        description: shop.description || '',
        status: shop.status != null ? shop.status : 1,
      },
    })
    return true
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
      latitude: this.data.formData.latitude ? parseFloat(this.data.formData.latitude) : undefined,
      longitude: this.data.formData.longitude ? parseFloat(this.data.formData.longitude) : undefined,
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
      const { formData, uploadedImages, currentShopId } = this.data

      if (!formData.shopName) {
        wx.showToast({
          title: '请输入店铺名称',
          icon: 'none',
        })
        return
      }

      if (!formData.province) {
        wx.showToast({
          title: '请输入省份',
          icon: 'none',
        })
        return
      }

      if (!formData.city) {
        wx.showToast({
          title: '请输入城市',
          icon: 'none',
        })
        return
      }

      if (!formData.district) {
        wx.showToast({
          title: '请输入区县',
          icon: 'none',
        })
        return
      }

      if (!formData.address) {
        wx.showToast({
          title: '请输入详细地址',
          icon: 'none',
        })
        return
      }

      if (formData.phone && !/^\d{11}$/.test(formData.phone)) {
        wx.showToast({
          title: '店铺电话须为11位数字',
          icon: 'none',
        })
        return
      }

      this.setData({ submitting: true })

      const shopImages = uploadedImages.length > 0 ? JSON.stringify(uploadedImages) : undefined

      const { post } = require('../../../../../utils/request.js')
      const res = await post('/shop/update', {
        shopId: currentShopId,
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
        logo: formData.logo,
        description: formData.description || undefined,
        status: formData.status,
      })

      if (res && res.data && res.data.code === 200) {
        wx.showToast({
          title: '更新成功',
          icon: 'success',
          duration: 1500,
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      }
    } catch (error) {
      console.error('更新店铺失败:', error)
    } finally {
      this.setData({ submitting: false })
    }
  },
})
