// pages/info-maintenance/edit/edit.js
const { get, put } = require('../../../utils/request.js')
const { fileApi } = require('../../../api/api.js')

Page({
  data: {
    formData: {
      associationName: '',
      contactInfo: '',
      location: '',
      logo: '',
      bgImg: '',
      status: 1
    },
    submitting: false,
    uploadingLogo: false,
    bgImageList: [],
    uploadingBgImage: false
  },
  onLoad(options) {
    this.alumniAssociationId = options.alumniAssociationId
    this.loadAlumniDetail()
  },
  
  // 返回上一页
  onBack() {
    wx.navigateBack()
  },
  
  async loadAlumniDetail() {
    try {
      const res = await get(`/alumniAssociationManagement/detail/${this.alumniAssociationId}`)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        // 处理背景图数组
        let bgImageList = []
        if (data.bgImg) {
          try {
            // 移除可能的首尾空格和引号
            const cleanedStr = data.bgImg.trim().replace(/^["']|['"]$/g, '')
            // 解析JSON数组
            const bgImgArray = JSON.parse(cleanedStr)
            // 确保返回的是数组
            if (Array.isArray(bgImgArray)) {
              bgImageList = bgImgArray.map(url => ({ url }))
            }
          } catch (error) {
            console.error('Failed to parse bgImg:', error)
          }
        }
        
        this.setData({
          formData: {
            associationName: data.associationName || '',
            contactInfo: data.contactInfo || '',
            location: data.location || '',
            logo: data.logo || '',
            bgImg: data.bgImg || '',
            status: data.status || 1
          },
          bgImageList: bgImageList
        })
      }
    } catch (error) {
      console.error('Failed to load alumni detail:', error)
    }
  },
  
  handleInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    
    this.setData({
      [`formData.${field}`]: value
    })
  },
  
  handleStatusChange(e) {
    this.setData({
      'formData.status': parseInt(e.detail.value)
    })
  },
  
  // 上传logo
  onUploadLogo() {
    const that = this
    
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFilePaths[0]
        that.setData({ uploadingLogo: true })
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 上传图片到服务器
        fileApi.uploadImage(tempFilePath)
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              // 上传成功，更新表单数据
              that.setData({
                [`formData.logo`]: res.data.fileUrl,
                uploadingLogo: false
              })
              wx.showToast({
                title: '上传成功',
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: res.msg || '上传失败',
                icon: 'none'
              })
              that.setData({ uploadingLogo: false })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none'
            })
            console.error('上传logo失败:', err)
            that.setData({ uploadingLogo: false })
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail(err) {
        console.error('选择图片失败:', err)
        that.setData({ uploadingLogo: false })
      }
    })
  },
  
  // 删除logo
  onDeleteLogo() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除logo吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            [`formData.logo`]: ''
          })
        }
      }
    })
  },
  
  // 上传背景图
  onUploadBgImage() {
    const that = this
    
    wx.chooseImage({
      count: 9 - that.data.bgImageList.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePaths = res.tempFilePaths
        that.setData({ uploadingBgImage: true })
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 上传多张图片
        const uploadPromises = tempFilePaths.map(filePath => {
          return fileApi.uploadImage(filePath)
        })
        
        Promise.all(uploadPromises)
          .then(results => {
            // 处理上传结果
            const uploadedUrls = results
              .filter(res => res.code === 200 && res.data && res.data.fileUrl)
              .map(res => res.data.fileUrl)
            
            if (uploadedUrls.length > 0) {
              // 更新图片URL数组
              const bgImageList = [...that.data.bgImageList, ...uploadedUrls.map(url => ({ url }))]
              that.setData({
                bgImageList: bgImageList,
                [`formData.bgImg`]: JSON.stringify(bgImageList.map(item => item.url)),
                uploadingBgImage: false
              })
              wx.showToast({
                title: `上传成功 ${uploadedUrls.length} 张`,
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: '上传失败',
                icon: 'none'
              })
              that.setData({ uploadingBgImage: false })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none'
            })
            console.error('上传背景图失败:', err)
            that.setData({ uploadingBgImage: false })
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail(err) {
        console.error('选择图片失败:', err)
        that.setData({ uploadingBgImage: false })
        wx.hideLoading()
      }
    })
  },
  
  // 删除背景图
  onDeleteBgImage(e) {
    const index = e.currentTarget.dataset.index
    const bgImageList = [...this.data.bgImageList]
    bgImageList.splice(index, 1)
    this.setData({
      bgImageList: bgImageList,
      [`formData.bgImg`]: JSON.stringify(bgImageList.map(item => item.url))
    })
    wx.showToast({
      title: '删除成功',
      icon: 'success'
    })
  },
  
  async submitForm() {
    this.setData({ submitting: true })
    
    try {
      const formData = this.data.formData
      const payload = {
        alumniAssociationId: this.alumniAssociationId,
        associationName: formData.associationName,
        contactInfo: formData.contactInfo,
        location: formData.location,
        logo: formData.logo,
        bgImg: formData.bgImg,
        status: formData.status
      }
      
      const res = await put('/alumniAssociationManagement/update', payload)
      
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '更新成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '更新失败', icon: 'none' })
      }
    } catch (error) {
      console.error('Submit error:', error)
      wx.showToast({ title: '网络错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
