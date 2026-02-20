// pages/enterprise/edit/edit.js
const { placeApi } = require('../../../api/api.js')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    placeId: '',
    formData: {
      placeId: '',
      placeName: '',
      placeType: 1,
      alumniAssociationId: '',
      province: '',
      city: '',
      district: '',
      address: '',
      locationName: '',
      latitude: '',
      longitude: '',
      contactPhone: '',
      contactEmail: '',
      businessHours: '',
      images: '',
      logo: '',
      description: '',
      establishedTime: '',
      status: 1,
      isRecommended: 0
    },
    // picker 选项数组
    pickerOptions: {
      status: [
        {"id":0,"name":"停业"},
        {"id":1,"name":"正常运营"},
        {"id":2,"name":"装修中"}
      ]
    },
    // 校友会相关
    associations: [], // 校友会列表
    selectedAssociationIndex: 0, // 当前选中的校友会索引
    selectedAssociation: null, // 当前选中的校友会对象
    // 图片上传相关
    logoImageList: [], // Logo图片列表
    enterpriseImageList: [], // 企业图片列表
    uploading: false, // 上传状态
    loading: true,
    error: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.id) {
      this.setData({ placeId: options.id })
      this.loadAssociationList()
      this.loadPlaceDetail()
    } else {
      this.setData({ error: '缺少企业ID参数', loading: false })
    }
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
    this.loadPlaceDetail()
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
   * 加载企业详情
   */
  async loadPlaceDetail() {
    const { placeId } = this.data
    if (!placeId) {
      this.setData({ error: '缺少企业ID参数', loading: false })
      return
    }

    this.setData({ loading: true, error: '' })
    try {
      const res = await placeApi.getPlaceDetail(placeId)
      console.log('获取企业详情结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        let placeData = res.data && res.data.data ? res.data.data : res.data
        
        // 清理 logo 数据
        let cleanedLogo = placeData.logo || ''
        if (cleanedLogo) {
          cleanedLogo = cleanedLogo.trim().replace(/^["'`]+|["'`]+$/g, '')
        }
        
        // 清理 images 数据
        let cleanedImages = ''
        if (placeData.images) {
          try {
            // 如果是数组，直接使用
            if (Array.isArray(placeData.images)) {
              cleanedImages = JSON.stringify(placeData.images)
            } else if (typeof placeData.images === 'string') {
              // 如果是字符串，尝试解析
              let trimmedImages = placeData.images.trim()
              // 移除两端的引号、反引号等
              trimmedImages = trimmedImages.replace(/^["'`]+|["'`]+$/g, '')
              // 移除所有的反引号和多余的空格
              trimmedImages = trimmedImages.replace(/[`]+/g, '')
              // 移除所有的转义字符
              trimmedImages = trimmedImages.replace(/\\/g, '')
              
              try {
                const parsedImages = JSON.parse(trimmedImages)
                if (Array.isArray(parsedImages)) {
                  cleanedImages = JSON.stringify(parsedImages)
                } else {
                  // 如果不是数组，作为单个 URL 处理
                  cleanedImages = JSON.stringify([trimmedImages])
                }
              } catch (parseError) {
                // 解析失败，尝试更简单的处理方式
                // 直接按逗号分割字符串，处理多个 URL 的情况
                const imageUrls = trimmedImages.split(',').map(url => url.trim()).filter(url => url)
                cleanedImages = JSON.stringify(imageUrls)
              }
            }
          } catch (e) {
            console.error('处理 images 数据失败:', e)
            // 处理失败时使用空数组
            cleanedImages = '[]'
          }
        }
        
        // 构建表单数据
        const formData = {
          placeId: placeData.placeId,
          placeName: placeData.placeName || '',
          placeType: placeData.placeType || 1,
          alumniAssociationId: placeData.alumniAssociationId || '',
          province: placeData.province || '',
          city: placeData.city || '',
          district: placeData.district || '',
          address: placeData.address || '',
          locationName: placeData.locationName || '',
          latitude: placeData.latitude || '',
          longitude: placeData.longitude || '',
          contactPhone: placeData.contactPhone || '',
          contactEmail: placeData.contactEmail || '',
          businessHours: placeData.businessHours || '',
          images: cleanedImages,
          logo: cleanedLogo,
          description: placeData.description || '',
          establishedTime: placeData.establishedTime || '',
          status: placeData.status || 1,
          isRecommended: placeData.isRecommended || 0
        }
        
        // 初始化图片列表
        // 清理 logo 数据，移除多余的引号和反引号
        let displayLogo = formData.logo
        if (displayLogo) {
          displayLogo = displayLogo.trim().replace(/^["'`]+|["'`]+$/g, '')
        }
        const logoImageList = displayLogo ? [{ url: displayLogo }] : []
        
        // 清理和解析 images 数据
        let enterpriseImageList = []
        if (formData.images) {
          try {
            // 清理 images 数据，移除多余的引号和反引号
            let cleanedImages = formData.images.trim().replace(/^["'`]+|["'`]+$/g, '')
            const parsedImages = JSON.parse(cleanedImages)
            if (Array.isArray(parsedImages)) {
              // 清理数组中的每个 URL
              enterpriseImageList = parsedImages
                .map(url => {
                  if (typeof url === 'string') {
                    return { url: url.trim().replace(/^["'`]+|["'`]+$/g, '') }
                  }
                  return null
                })
                .filter(item => item && item.url)
            }
          } catch (e) {
            console.error('解析图片数据失败:', e)
            // 尝试作为单个 URL 处理
            try {
              let cleanedImages = formData.images.trim().replace(/^["'`]+|["'`]+$/g, '')
              enterpriseImageList = [{ url: cleanedImages }]
            } catch (e2) {
              console.error('处理单个图片 URL 失败:', e2)
            }
          }
        }
        
        // 获取校友会对象
        let selectedAssociation = null
        let selectedAssociationIndex = 0
        
        
        // 如果本地列表中没有找到，尝试通过ID获取校友会详情
        if (!selectedAssociation && formData.alumniAssociationId) {
          try {
            const { associationApi } = require('../../../api/api.js')
            const associationRes = await associationApi.getAssociationDetail(formData.alumniAssociationId)
            
            if (associationRes && (associationRes.data && associationRes.data.code === 200 || associationRes.code === 200)) {
              const associationData = associationRes.data && associationRes.data.data ? associationRes.data.data : associationRes.data
              // 确保返回的数据结构与本地列表一致
              if (associationData) {
                selectedAssociation = {
                  alumniAssociationId: associationData.alumniAssociationId || associationData.id,
                  associationName: associationData.associationName || associationData.alumniAssociationName,
                  schoolId: associationData.schoolId,
                  platformId: associationData.platformId,
                  contactInfo: associationData.contactInfo,
                  location: associationData.location,
                  memberCount: associationData.memberCount,
                  logo: associationData.logo
                }
              }
            }
          } catch (error) {
            console.error('获取校友会详情失败:', error)
          }
        }
        
        this.setData({ 
          formData: formData, 
          logoImageList: logoImageList,
          enterpriseImageList: enterpriseImageList,
          selectedAssociation: selectedAssociation,
          selectedAssociationIndex: selectedAssociationIndex,
          loading: false 
        })
        
        // 设置导航栏标题
        wx.setNavigationBarTitle({
          title: '编辑企业信息'
        })
      } else {
        this.setData({ error: '获取企业详情失败', loading: false })
      }
    } catch (error) {
      console.error('获取企业详情失败:', error)
      this.setData({ error: '获取企业详情失败', loading: false })
    }
  },

  /**
   * 表单输入事件
   */
  onInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    
    this.setData({
      [`formData.${field}`]: value
    })
  },

  /**
   * 选择器变化事件
   */
  onPickerChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    
    let selectedValue = value
    
    // 根据不同字段处理选择值
    if (field === 'status') {
      // 状态选择器：直接使用索引值
      selectedValue = value
    }
    
    this.setData({
      [`formData.${field}`]: selectedValue
    })
  },

  /**
   * 上传Logo图片
   */
  onLogoImageUpload() {
    this.uploadImage('logo')
  },

  /**
   * 上传企业图片
   */
  onEnterpriseImageUpload() {
    this.uploadImage('enterprise')
  },

  /**
   * 上传图片
   */
  uploadImage(type) {
    const that = this
    
    wx.chooseImage({
      count: type === 'logo' ? 1 : 9, // Logo只能上传1张，企业图片最多9张
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePaths = res.tempFilePaths
        that.setData({ uploading: true })
        
        // 模拟上传成功
        setTimeout(() => {
          if (type === 'logo') {
            that.setData({
              logoImageList: tempFilePaths.map(path => ({ url: path })),
              [`formData.logo`]: tempFilePaths[0],
              uploading: false
            })
          } else {
            const enterpriseImageList = [...that.data.enterpriseImageList, ...tempFilePaths.map(path => ({ url: path }))]
            that.setData({
              enterpriseImageList: enterpriseImageList,
              [`formData.images`]: JSON.stringify(enterpriseImageList.map(item => item.url)),
              uploading: false
            })
          }
        }, 1000)
      },
      fail(err) {
        console.error('选择图片失败:', err)
        that.setData({ uploading: false })
      }
    })
  },

  /**
   * 删除图片
   */
  onDeleteImage(e) {
    const { type, index } = e.currentTarget.dataset
    
    if (type === 'logo') {
      this.setData({
        logoImageList: [],
        [`formData.logo`]: ''
      })
    } else {
      const enterpriseImageList = [...this.data.enterpriseImageList]
      enterpriseImageList.splice(index, 1)
      this.setData({
        enterpriseImageList: enterpriseImageList,
        [`formData.images`]: JSON.stringify(enterpriseImageList.map(item => item.url))
      })
    }
  },

  /**
   * 选择位置
   */
  selectLocation() {
    // 使用微信地图选择API获取位置信息
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          [`formData.locationName`]: res.name,
          [`formData.latitude`]: res.latitude.toString(),
          [`formData.longitude`]: res.longitude.toString()
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
   * 加载校友会列表
   */
  async loadAssociationList() {
    try {
      wx.showLoading({ title: '加载校友会列表...' })
      const { associationApi } = require('../../../api/api.js')
      const res = await associationApi.getMyAssociations()
      console.log('获取校友会列表结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        const data = res.data && res.data.data ? res.data.data : res.data
        console.log('校友会数据:', data)
        
        this.setData({
          associations: data || [],
          selectedAssociation: data && data.length > 0 ? data[0] : null,
          selectedAssociationIndex: 0
        })
        
        console.log('设置后的 associations:', this.data.associations)
      } else {
        wx.showToast({ title: '获取校友会列表失败', icon: 'none' })
        this.setData({ associations: [] })
      }
    } catch (error) {
      console.error('获取校友会列表失败:', error)
      wx.showToast({ title: '获取校友会列表失败', icon: 'none' })
      this.setData({ associations: [] })
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
    if (selectedAssociation) {
      this.setData({
        selectedAssociationIndex: index,
        selectedAssociation: selectedAssociation,
        [`formData.alumniAssociationId`]: selectedAssociation.alumniAssociationId || selectedAssociation.id
      })
      console.log('选择的校友会:', selectedAssociation)
    }
  },


  /**
   * 提交表单
   */
  async submitForm() {
    const { formData } = this.data
    
    // 验证必填字段
    if (!formData.placeId) {
      wx.showToast({
        title: '缺少企业ID参数',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '提交中...' })
    try {
      // 准备提交数据
      const submitData = { ...formData }
      
      // 调用更新接口
      const res = await placeApi.updatePlace(submitData)
      console.log('更新企业信息结果:', res)
      
      if (res && (res.data && res.data.code === 200 || res.code === 200)) {
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        })
        
        // 返回上一页
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: '更新失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('更新企业信息失败:', error)
      wx.showToast({
        title: '更新失败',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  }
})
