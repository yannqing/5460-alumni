// pages/audit/merchant/shop/shop.js
const app = getApp()
const { uploadImage } = require('../../../../utils/fileUpload.js')

Page({
  data: {
    loading: false,
    showModal: false,
    submitting: false,
    // 操作类型：create - 创建，edit - 编辑
    operationType: 'create',
    // 当前编辑的店铺ID
    currentShopId: '',
    // 商户选择相关
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    // 店铺列表相关
    shopList: [],
    shopLoading: false,
    // 图片上传相关
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
      description: '',
      status: 1
    },
    scrollListHeight: 400
  },

  onLoad(options) {
    this.setScrollListHeight()
  },

  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      const scrollH = Math.floor(contentH * 0.5)
      this.setData({ scrollListHeight: scrollH > 200 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  onShow() {
    // 页面显示时加载商户列表
    this.loadMerchants()
    // 注意：loadShopList会在商户选择时调用，这里不需要重复调用
    // 避免在页面显示时立即调用shop/page接口，减少不必要的请求
  },

  // 加载商户列表
  async loadMerchants() {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../utils/request.js')
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100 // 加载足够多的商户数据
      })
      
      if (res.data && res.data.code === 200) {
        const merchantList = res.data.data.records || []
        this.setData({
          merchantList: merchantList
        })
        
        // 如果没有选中的商户且列表不为空，自动选择第一个商户
        const { selectedMerchantId } = this.data
        if (!selectedMerchantId && merchantList.length > 0) {
          const firstMerchant = merchantList[0]
          // 将商户ID转换为字符串，避免大数字精度问题
          const merchantIdStr = firstMerchant.merchantId + ''
          this.setData({
            selectedMerchantId: merchantIdStr,
            selectedMerchantName: firstMerchant.merchantName,
            [`formData.merchantId`]: merchantIdStr
          })
          // 加载第一个商户的店铺列表
          this.loadShopList(merchantIdStr)
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载店铺列表
  async loadShopList(merchantId = '') {
    try {
      this.setData({ shopLoading: true })
      const { get, post } = require('../../../../utils/request.js')
      
      let res
      if (merchantId) {
        // 根据商户ID获取店铺列表
        res = await get(`/shop/list/${merchantId}`)
      } else {
        // 获取所有店铺列表
        res = await post('/shop/page', {
          current: 1,
          pageSize: 10
        })
      }
      
      if (res.data && res.data.code === 200) {
        // 处理返回数据，确保records字段存在
        const records = res.data.data?.records || res.data.data || []
        // 调试：查看第一个店铺的完整数据
        if (records.length > 0) {
          console.log('店铺数据调试:', records[0])
        }
        this.setData({
          shopList: records
        })
      }
    } catch (error) {
      console.error('加载店铺列表失败:', error)
    } finally {
      this.setData({ shopLoading: false })
    }
  },

  // 显示商户选择器
  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  // 取消商户选择
  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  // 选择商户
  selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName
    
    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false,
      [`formData.merchantId`]: merchantId
    })
    
    // 根据选中的商户ID加载对应的店铺列表
    this.loadShopList(merchantId)
  },

  // 显示创建店铺弹窗
  showCreateShopModal() {
    // 重置表单数据
    this.setData({
      operationType: 'create',
      currentShopId: '',
      uploadedImages: [],
      formData: {
        merchantId: this.data.selectedMerchantId || '',
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
        description: '',
        status: 1
      },
      showModal: true
    })
  },

  // 显示编辑店铺弹窗
  async showEditShopModal(e) {
    const shopId = e.currentTarget.dataset.shopId
    // 从店铺列表中查找对应的店铺对象
    const shop = this.data.shopList.find(item => item.shopId === shopId)
    
    if (!shop) {
      wx.showToast({
        title: '未找到店铺数据',
        icon: 'none'
      })
      return
    }
    
    // 调用店铺详情接口获取包含description的完整数据
    const { get } = require('../../../../utils/request.js')
    const res = await get(`/merchant/shop/${shopId}`)
    if (res.data && res.data.code === 200) {
      // 合并详情数据到店铺对象
      Object.assign(shop, res.data.data)
    }
    
    // 处理店铺图片，转换为数组格式
    let uploadedImages = []
    if (shop.shopImages) {
      try {
        uploadedImages = JSON.parse(shop.shopImages)
      } catch (error) {
        console.error('解析店铺图片失败:', error)
        uploadedImages = []
      }
    }
    
    // 设置表单数据
    this.setData({
      operationType: 'edit',
      currentShopId: shop.shopId,
      uploadedImages: uploadedImages,
      formData: {
        merchantId: shop.merchantId + '',
        shopName: shop.shopName || '',
        shopType: shop.shopType || 1,
        province: shop.province || '',
        city: shop.city || '',
        district: shop.district || '',
        address: shop.address || '',
        locationName: shop.locationName || '',
        latitude: shop.latitude ? shop.latitude.toString() : '',
        longitude: shop.longitude ? shop.longitude.toString() : '',
        phone: shop.phone || '',
        businessHours: shop.businessHours || '',
        shopImages: shop.shopImages || '',
        description: shop.description || '',
        status: shop.status || 1
      },
      // 保持商户选择与当前店铺一致
      selectedMerchantId: shop.merchantId + '',
      selectedMerchantName: this.data.merchantList.find(m => m.merchantId + '' === shop.merchantId + '')?.merchantName || '',
      showModal: true
    })
  },

  // 隐藏创建店铺弹窗
  hideCreateShopModal() {
    this.setData({
      showModal: false
    })
  },

  // 显示删除确认提示
  showDeleteConfirm(e) {
    const shopId = e.currentTarget.dataset.shopId
    wx.showModal({
      title: '删除确认',
      content: '确定要删除该店铺吗？删除后不可恢复',
      success: (res) => {
        if (res.confirm) {
          // 用户确认删除，执行删除操作
          this.deleteShop(shopId)
        }
      }
    })
  },

  // 删除店铺
  async deleteShop(shopId) {
    try {
      wx.showLoading({
        title: '删除中...',
        mask: true
      })
      
      const { del } = require('../../../../utils/request.js')
      const res = await del(`/shop/delete/${shopId}`)
      
      if (res.data && res.data.code === 200) {
        // 删除成功
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        })
        
        // 重新加载店铺列表
        this.loadShopList(this.data.selectedMerchantId)
      } else {
        // 删除失败
        wx.showToast({
          title: res.data.msg || '删除失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('删除店铺失败:', error)
      wx.showToast({
        title: '删除失败，请重试',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 输入框变化处理
  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`formData.${field}`]: value
    })
  },

  // 单选框变化处理
  onRadioChange(e) {
    const { field, value } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: parseInt(value)
    })
  },

  // 选择位置
  selectLocation() {
    // 使用微信地图选择API获取位置信息
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          [`formData.locationName`]: res.name,
          [`formData.latitude`]: res.latitude.toString(),
          [`formData.longitude`]: res.longitude.toString()
          // 仅获取经纬度，不自动填充地址信息，保持原有地址填写功能
        })
        
        wx.showToast({
          title: '位置选择成功',
          icon: 'success'
        })
      },
      fail: (err) => {
        console.error('位置选择失败:', err)
        // 如果用户取消选择，不显示错误提示
        if (err.errMsg !== 'chooseLocation:fail cancel') {
          wx.showToast({
            title: '位置选择失败，请重试',
            icon: 'none'
          })
        }
      }
    })
  },

  // 选择图片
  chooseImage() {
    wx.chooseImage({
      count: 9, // 最多可以选择9张图片
      sizeType: ['compressed'], // 压缩图
      sourceType: ['album', 'camera'], // 可以从相册或相机选择
      success: (res) => {
        const tempFilePaths = res.tempFilePaths
        // 上传选择的图片
        this.uploadSelectedImages(tempFilePaths)
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
        if (err.errMsg !== 'chooseImage:fail cancel') {
          wx.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      }
    })
  },

  // 上传选择的图片
  async uploadSelectedImages(filePaths) {
    try {
      wx.showLoading({
        title: '上传中...',
        mask: true
      })

      const uploadedImages = this.data.uploadedImages
      for (const filePath of filePaths) {
        // 调用上传图片方法
        const uploadRes = await uploadImage(filePath, '/file/upload/images', 'image')
        if (uploadRes.code === 200 && uploadRes.data) {
          // 将上传成功的图片URL添加到数组中
          uploadedImages.push(uploadRes.data.fileUrl)
        } else {
          throw new Error(uploadRes.msg || '上传失败')
        }
      }

      // 更新已上传图片列表
      this.setData({
        uploadedImages: uploadedImages
      })

      wx.showToast({
        title: '上传成功',
        icon: 'success'
      })
    } catch (error) {
      console.error('上传图片失败:', error)
      wx.showToast({
        title: error.msg || '上传失败',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 删除图片
  deleteImage(e) {
    const { index } = e.currentTarget.dataset
    const uploadedImages = this.data.uploadedImages
    uploadedImages.splice(index, 1)
    this.setData({
      uploadedImages: uploadedImages
    })
  },

  // 提交店铺表单（创建或更新）
  async submitShopForm() {
    try {
      let { formData, uploadedImages, operationType, currentShopId } = this.data
      
      // 表单验证
      if (!formData.merchantId) {
        wx.showToast({
          title: '请选择所属商户',
          icon: 'none'
        })
        return
      }
      
      if (!formData.shopName) {
        wx.showToast({
          title: '请输入店铺名称',
          icon: 'none'
        })
        return
      }
      
      if (!formData.province) {
        wx.showToast({
          title: '请输入省份',
          icon: 'none'
        })
        return
      }
      
      if (!formData.city) {
        wx.showToast({
          title: '请输入城市',
          icon: 'none'
        })
        return
      }
      
      if (!formData.district) {
        wx.showToast({
          title: '请输入区县',
          icon: 'none'
        })
        return
      }
      
      if (!formData.address) {
        wx.showToast({
          title: '请输入详细地址',
          icon: 'none'
        })
        return
      }
      
      if (!formData.latitude || !formData.longitude) {
        wx.showToast({
          title: '请选择店铺位置',
          icon: 'none'
        })
        return
      }
      
      // 处理店铺图片，将上传的图片数组转换为JSON格式字符串
      const shopImages = uploadedImages.length > 0 ? JSON.stringify(uploadedImages) : undefined
      
      // 开始提交
      this.setData({
        submitting: true
      })
      
      // 调用API
      const { post } = require('../../../../utils/request.js')
      let res
      let apiUrl
      let apiData
      
      if (operationType === 'create') {
        // 创建店铺
        apiUrl = '/shop/create'
        apiData = {
          merchantId: formData.merchantId, // 直接使用字符串形式的商户ID，避免大数字精度丢失
          shopName: formData.shopName,
          shopType: formData.shopType,
          province: formData.province,
          city: formData.city,
          district: formData.district,
          address: formData.address,
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          phone: formData.phone || undefined,
          businessHours: formData.businessHours || undefined,
          shopImages: shopImages,
          description: formData.description || undefined
        }
      } else {
        // 更新店铺
        apiUrl = '/shop/update'
        apiData = {
          shopId: currentShopId,
          shopName: formData.shopName,
          shopType: formData.shopType,
          province: formData.province,
          city: formData.city,
          district: formData.district,
          address: formData.address,
          latitude: parseFloat(formData.latitude),
          longitude: parseFloat(formData.longitude),
          phone: formData.phone || undefined,
          businessHours: formData.businessHours || undefined,
          shopImages: shopImages,
          description: formData.description || undefined,
          status: formData.status
        }
      }
      
      res = await post(apiUrl, apiData)
      
      // 提交成功
      wx.showToast({
        title: operationType === 'create' ? '创建成功' : '更新成功',
        icon: 'success'
      })
      
      // 关闭弹窗
      this.hideCreateShopModal()
      
      // 重新加载店铺列表
      this.loadShopList(this.data.selectedMerchantId)
      
      // 如果是创建操作，重置表单
      if (operationType === 'create') {
        this.setData({
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
            description: '',
            status: 1
          },
          selectedMerchantId: '',
          selectedMerchantName: '',
          uploadedImages: []
        })
      }
      
    } catch (error) {
      console.error(operationType === 'create' ? '创建店铺失败:' : '更新店铺失败:', error)
      wx.showToast({
        title: operationType === 'create' ? '创建失败，请重试' : '更新失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({
        submitting: false
      })
    }
  }
})