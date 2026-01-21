// pages/audit/merchant/shop/shop.js
const app = getApp()

Page({
  data: {
    loading: false,
    showModal: false,
    submitting: false,
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
      description: ''
    }
  },

  onLoad(options) {
    // 页面加载
  },

  onShow() {
    // 页面显示
  },

  // 显示创建店铺弹窗
  showCreateShopModal() {
    this.setData({
      showModal: true
    })
  },

  // 隐藏创建店铺弹窗
  hideCreateShopModal() {
    this.setData({
      showModal: false
    })
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

  // 提交创建店铺表单
  async submitCreateShop() {
    try {
      const { formData } = this.data
      
      // 表单验证
      if (!formData.merchantId) {
        wx.showToast({
          title: '请输入所属商户ID',
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
      
      // 开始提交
      this.setData({
        submitting: true
      })
      
      // 调用创建店铺接口
      const { post } = require('../../../../utils/request.js')
      const res = await post('/shop/create', {
        merchantId: parseInt(formData.merchantId),
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
        shopImages: formData.shopImages || undefined,
        description: formData.description || undefined
      })
      
      // 提交成功
      wx.showToast({
        title: '创建成功',
        icon: 'success'
      })
      
      // 关闭弹窗
      this.hideCreateShopModal()
      
      // 重置表单
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
          description: ''
        }
      })
      
    } catch (error) {
      console.error('创建店铺失败:', error)
      wx.showToast({
        title: '创建失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({
        submitting: false
      })
    }
  }
})