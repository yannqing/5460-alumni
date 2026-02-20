// pages/enterprise/apply-enterprise/apply-enterprise.js
const { placeApi, associationApi } = require('../../../api/api.js')
const fileUploadUtil = require('../../../utils/fileUpload.js')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    images: [], // 存储上传的图片URL数组
    logo: '', // 存储上传的Logo URL
    associations: [], // 存储校友会列表
    selectedAssociationIndex: 0, // 当前选中的校友会索引
    selectedAssociation: null, // 当前选中的校友会对象
    locationName: '', // 企业地点名称
    latitude: 0, // 纬度
    longitude: 0, // 经度
    establishedTime: '', // 成立时间
    currentDate: '' // 当前日期
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 初始化当前日期
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const currentDate = `${year}-${month}-${day}`
    
    this.setData({
      currentDate
    })
    
    this.loadAssociations()
  },

  /**
   * 成立时间选择变更
   */
  onEstablishedTimeChange(e) {
    this.setData({
      establishedTime: e.detail.value
    })
  },

  /**
   * 加载用户的校友会列表
   */
  async loadAssociations() {
    try {
      wx.showLoading({ title: '加载校友会列表...' })
      const res = await associationApi.getMyAssociations()
      console.log('获取校友会列表结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        const data = res.data && res.data.data ? res.data.data : res.data
        this.setData({
          associations: data || [],
          selectedAssociation: data && data.length > 0 ? data[0] : null
        })
      } else {
        wx.showToast({ title: '获取校友会列表失败', icon: 'none' })
      }
    } catch (error) {
      console.error('获取校友会列表失败:', error)
      wx.showToast({ title: '获取校友会列表失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  /**
   * 校友会选择变更
   */
  bindAssociationChange(e) {
    const index = e.detail.value
    const selectedAssociation = this.data.associations[index]
    this.setData({
      selectedAssociationIndex: index,
      selectedAssociation: selectedAssociation
    })
    console.log('选择的校友会:', selectedAssociation)
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    wx.stopPullDownRefresh()
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 选择并上传企业宣传图片
   */
  chooseImages() {
    const maxCount = 9 - this.data.images.length
    if (maxCount <= 0) {
      wx.showToast({ title: '最多只能上传9张图片', icon: 'none' })
      return
    }
    
    // 选择图片
    wx.chooseImage({
      count: maxCount, // 最多可选择的图片数量，不超过9张
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths
        
        // 立即显示本地预览
        const newImages = [...this.data.images, ...tempFilePaths]
        this.setData({
          images: newImages
        })
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 上传多张图片
        const uploadPromises = tempFilePaths.map(filePath => {
          return fileUploadUtil.uploadImage(filePath, '/file/upload/images')
        })
        
        Promise.all(uploadPromises)
          .then(results => {
            // 处理上传结果
            const uploadedImages = results
              .filter(res => res.code === 200 && res.data && res.data.fileUrl)
              .map(res => res.data.fileUrl)
            
            if (uploadedImages.length > 0) {
              // 上传成功后更新为服务器URL
              const updatedImages = [...this.data.images.slice(0, this.data.images.length - tempFilePaths.length), ...uploadedImages]
              this.setData({
                images: updatedImages
              })
              
              wx.showToast({
                title: `上传成功 ${uploadedImages.length} 张`,
                icon: 'success'
              })
            } else {
              // 上传失败，处理错误
              this.handleUploadError('images', '上传失败', tempFilePaths.length)
            }
          })
          .catch(err => {
            // 上传失败，处理错误
            this.handleUploadError('images', err.msg || '上传失败', tempFilePaths.length)
            console.error('上传企业宣传图片失败:', err)
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
      }
    })
  },

  /**
   * 删除企业宣传图片
   */
  deleteImage(e) {
    const index = e.currentTarget.dataset.index
    const newImages = [...this.data.images]
    newImages.splice(index, 1)
    this.setData({
      images: newImages
    })
  },

  /**
   * 选择并上传企业Logo
   */
  chooseLogo() {
    // 选择图片
    wx.chooseImage({
      count: 1, // 只允许选择1张图片作为Logo
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        
        // 立即显示本地预览
        this.setData({
          logo: tempFilePath
        })
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 上传图片
        fileUploadUtil.uploadImage(tempFilePath, '/file/upload/images')
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              // 上传成功后更新为服务器URL
              this.setData({
                logo: res.data.fileUrl
              })
              
              wx.showToast({
                title: 'Logo上传成功',
                icon: 'success'
              })
            } else {
              // 上传失败，处理错误
              this.handleUploadError('logo', res.msg || '上传失败')
            }
          })
          .catch(err => {
            // 上传失败，处理错误
            this.handleUploadError('logo', err.msg || '上传失败')
            console.error('上传Logo失败:', err)
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
      }
    })
  },

  /**
   * 删除企业Logo
   */
  deleteLogo() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除企业Logo吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            logo: ''
          })
          wx.showToast({
            title: 'Logo已删除',
            icon: 'success'
          })
        }
      }
    })
  },

  /**
   * 处理上传失败
   * @param {string} type 上传类型 (logo, images)
   * @param {string} errMsg 错误信息
   * @param {number} count 上传数量 (仅用于images)
   */
  handleUploadError(type, errMsg, count = 1) {
    // 清空对应类型的预览
    if (type === 'logo') {
      this.setData({ logo: '' })
    } else if (type === 'images') {
      // 移除预览的图片
      const originalImages = this.data.images.slice(0, this.data.images.length - count)
      this.setData({ images: originalImages })
    }
    
    // 显示错误提示
    wx.showToast({
      title: errMsg || '上传失败，请稍后重试',
      icon: 'none',
      duration: 2000
    })
  },



  /**
   * 预览图片
   */
  previewImage(e) {
    const index = e.currentTarget.dataset.index
    wx.previewImage({
      current: this.data.images[index],
      urls: this.data.images
    })
  },

  /**
   * 选择企业地点
   */
  chooseLocation() {
    // 使用微信地图选择API获取位置信息
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          locationName: res.name,
          latitude: res.latitude,
          longitude: res.longitude
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

  /**
   * 提交表单
   */
  async submitForm(e) {
    const formData = e.detail.value
    console.log('表单数据:', formData)
    
    // 验证必填字段
    if (!formData.placeName) {
      wx.showToast({ title: '请输入企业名称', icon: 'none' })
      return
    }
    
    if (!this.data.selectedAssociation) {
      wx.showToast({ title: '请选择所属校友会', icon: 'none' })
      return
    }
    
    // 构建请求参数
    const params = {
      placeName: formData.placeName,
      placeType: 1, // 硬编码为企业类型
      alumniAssociationId: this.data.selectedAssociation.alumniAssociationId,
      province: formData.province || '',
      city: formData.city || '',
      district: formData.district || '',
      address: formData.address || '',
      latitude: this.data.latitude || undefined,
      longitude: this.data.longitude || undefined,
      contactPhone: formData.contactPhone || '',
      contactEmail: formData.contactEmail || '',
      businessHours: formData.businessHours || '',
      images: this.data.images.length > 0 ? JSON.stringify(this.data.images) : '',
      logo: this.data.logo || '',
      description: formData.description || '',
      establishedTime: this.data.establishedTime || ''
    }
    
    console.log('请求参数:', params)
    
    try {
      wx.showLoading({ title: '提交中...' })
      const res = await placeApi.applyForPlace(params)
      console.log('申请结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        wx.showToast({ title: '申请成功', icon: 'success' })
        // 跳转回我的企业页面
        setTimeout(() => {
          wx.navigateBack({
            delta: 1
          })
        }, 1500)
      } else {
        wx.showToast({ 
          title: res && (res.data && res.data.msg || res.msg) || '申请失败', 
          icon: 'none' 
        })
      }
    } catch (error) {
      console.error('申请企业失败:', error)
      wx.showToast({ title: '申请失败，请稍后重试', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  }
})
