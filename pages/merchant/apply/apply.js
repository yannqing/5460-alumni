// pages/merchant/apply/apply.js
const app = getApp()

Page({
  data: {
    form: {
      shopName: '',
      shopType: '',
      contactName: '',
      contactPhone: '',
      contactEmail: '',
      businessLicense: '',
      shopAddress: '',
      shopDescription: '',
      shopLogo: ''
    },
    submitting: false,
    shopTypeOptions: ['餐饮', '零售', '服务', '教育', '娱乐', '其他'],
    shopTypeIndex: 0,
    uploadType: '', // 'license' or 'logo'
    merchantStatus: 'none' // none, pending, approved, rejected
  },

  onLoad() {
    this.loadMerchantStatus()
    this.loadUserData()
  },

  loadUserData() {
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || {}
    
    const formData = {
      shopName: '',
      shopType: '',
      contactName: userInfo.nickName || userData.nickName || userData.name || '',
      contactPhone: userInfo.mobile || userData.mobile || userData.phone || '',
      contactEmail: userInfo.email || userData.email || '',
      businessLicense: '',
      shopAddress: '',
      shopDescription: '',
      shopLogo: ''
    }
    
    this.setData({ form: formData })
  },

  loadMerchantStatus() {
    const userData = app.globalData.userData || {}
    // 假设后端返回 merchantStatus 字段
    const status = userData.merchantStatus || 'none'
    this.setData({ merchantStatus: status })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleShopTypeChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      shopTypeIndex: index,
      'form.shopType': this.data.shopTypeOptions[index]
    })
  },

  chooseLocation() {
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          'form.shopAddress': res.address || res.name
        })
      },
      fail: (err) => {
        console.error('选择位置失败:', err)
      }
    })
  },

  chooseLogo() {
    this.setData({ uploadType: 'logo' })
    this.chooseImage()
  },

  chooseLicense() {
    this.setData({ uploadType: 'license' })
    this.chooseImage()
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles?.[0]?.tempFilePath
        if (tempFilePath) {
          const field = this.data.uploadType === 'logo' ? 'shopLogo' : 'businessLicense'
          this.setData({
            [`form.${field}`]: tempFilePath
          })
          // TODO: 上传图片到服务器
          this.uploadImage(tempFilePath, field)
        }
      }
    })
  },

  uploadImage(filePath, field) {
    // TODO: 实现图片上传到服务器
    wx.showLoading({ title: '上传中...' })
    setTimeout(() => {
      wx.hideLoading()
      wx.showToast({
        title: '上传成功',
        icon: 'success'
      })
    }, 1000)
  },

  removeImage(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: ''
    })
  },

  validateForm() {
    const { shopName, shopType, contactName, contactPhone, contactEmail, businessLicense, shopAddress, shopDescription } = this.data.form
    
    if (!shopName || !shopName.trim()) {
      wx.showToast({ title: '请输入店铺名称', icon: 'none' })
      return false
    }
    
    if (!shopType) {
      wx.showToast({ title: '请选择店铺类型', icon: 'none' })
      return false
    }
    
    if (!contactName || !contactName.trim()) {
      wx.showToast({ title: '请输入联系人姓名', icon: 'none' })
      return false
    }
    
    if (!contactPhone || !contactPhone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return false
    }
    
    if (!/^1[3-9]\d{9}$/.test(contactPhone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return false
    }
    
    if (contactEmail && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(contactEmail)) {
      wx.showToast({ title: '邮箱格式不正确', icon: 'none' })
      return false
    }
    
    if (!businessLicense) {
      wx.showToast({ title: '请上传营业执照', icon: 'none' })
      return false
    }
    
    if (!shopAddress || !shopAddress.trim()) {
      wx.showToast({ title: '请选择店铺地址', icon: 'none' })
      return false
    }
    
    if (!shopDescription || !shopDescription.trim()) {
      wx.showToast({ title: '请输入店铺简介', icon: 'none' })
      return false
    }
    
    if (shopDescription.length < 10) {
      wx.showToast({ title: '店铺简介至少10个字符', icon: 'none' })
      return false
    }
    
    return true
  },

  submitApply() {
    if (this.data.merchantStatus === 'approved') {
      wx.showToast({
        title: '您已是认证商家',
        icon: 'none'
      })
      return
    }
    
    if (this.data.merchantStatus === 'pending') {
      wx.showToast({
        title: '申请审核中，请耐心等待',
        icon: 'none'
      })
      return
    }
    
    if (!this.validateForm()) return
    
    this.setData({ submitting: true })
    
    // TODO: 调用后端接口提交商家申请
    setTimeout(() => {
      wx.showToast({
        title: '提交成功，等待审核',
        icon: 'success'
      })
      this.setData({
        submitting: false,
        merchantStatus: 'pending'
      })
      
      // 更新全局数据
      if (app.globalData.userData) {
        app.globalData.userData.merchantStatus = 'pending'
      }
      
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }, 1000)
  }
})

