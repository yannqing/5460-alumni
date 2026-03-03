// pages/audit/info-maintenance/edit/edit.js
const app = getApp()

Page({
  data: {
    platformId: 0,
    platformDetail: null,
    formData: {},
    loading: false
  },

  onLoad(options) {
    if (options.platformId) {
      this.setData({
        platformId: options.platformId
      })
      this.loadPlatformDetail()
    } else {
      wx.showToast({
        title: '缺少校促会ID',
        icon: 'error'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1000)
    }
  },

  // 加载校促会详细信息
  async loadPlatformDetail() {
    try {
      this.setData({ loading: true })

      const res = await app.api.localPlatformApi.getLocalPlatformManagementDetail(this.data.platformId)

      if (res.data && res.data.code === 200 && res.data.data) {
        const detail = res.data.data
        this.setData({
          platformDetail: detail,
          formData: {
            platformId: detail.platformId,
            platformName: detail.platformName || '',
            avatar: detail.avatar || '',
            city: detail.city || '',
            scope: detail.scope || '',
            contactInfo: detail.contactInfo || '',
            description: detail.description || '',
            memberCount: detail.memberCount || 0,
            monthlyHomepageArticleQuota: detail.monthlyHomepageArticleQuota || 0,
            bgImg: detail.bgImg || '',
            status: detail.status || 1,
            principalName: detail.principalName || '',
            principalPosition: detail.principalPosition || '',
            phone: detail.phone || ''
          }
        })
      } else {
        wx.showToast({
          title: '获取校促会详情失败',
          icon: 'error'
        })
      }
    } catch (error) {
      console.error('获取校促会详情失败:', error)
      wx.showToast({
        title: '获取校促会详情失败',
        icon: 'error'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 处理输入
  handleInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    this.setData({
      [`formData.${field}`]: value
    })
  },

  // 设置状态
  setStatus(e) {
    const { status } = e.currentTarget.dataset
    this.setData({
      'formData.status': parseInt(status)
    })
  },

  // 上传图片
  uploadImage(e) {
    const { field } = e.currentTarget.dataset

    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths
        // 这里应该调用上传接口，暂时直接使用临时路径
        this.setData({
          [`formData.${field}`]: tempFilePaths[0]
        })
      }
    })
  },

  // 删除图片
  deleteImage(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: ''
    })
  },

  // 提交表单
  async submitForm() {
    try {
      this.setData({ loading: true })

      const res = await app.api.localPlatformApi.updateLocalPlatform(this.data.formData)

      if (res.data && res.data.code === 200 && res.data.data) {
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1000)
      } else {
        wx.showToast({
          title: res.data?.msg || '保存失败',
          icon: 'error'
        })
      }
    } catch (error) {
      console.error('保存失败:', error)
      wx.showToast({
        title: '保存失败',
        icon: 'error'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})