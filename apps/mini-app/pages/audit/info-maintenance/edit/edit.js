// pages/audit/info-maintenance/edit/edit.js
const app = getApp()
const { fileApi } = require('../../../../api/api.js')

Page({
  data: {
    platformId: 0,
    platformDetail: null,
    formData: {},
    loading: false,
    uploadingAvatar: false,
    uploadingBgImage: false
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
            phone: detail.phone || '',
            importantEvents: typeof detail.importantEvents === 'string' ? JSON.parse(detail.importantEvents || '[]') : (detail.importantEvents || [])
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

  // 上传图片（调用 fileApi 上传到服务器，避免 tmp 临时路径）
  uploadImage(e) {
    const { field } = e.currentTarget.dataset
    const that = this

    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        const uploadKey = field === 'avatar' ? 'uploadingAvatar' : 'uploadingBgImage'
        that.setData({ [uploadKey]: true })
        wx.showLoading({ title: '上传中...', mask: true })

        fileApi
          .uploadImage(tempFilePath)
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              that.setData({
                [`formData.${field}`]: res.data.fileUrl,
                [uploadKey]: false
              })
              wx.showToast({ title: '上传成功', icon: 'success' })
            } else {
              wx.showToast({ title: res.msg || '上传失败', icon: 'none' })
              that.setData({ [uploadKey]: false })
            }
          })
          .catch(err => {
            wx.showToast({ title: err.msg || '上传失败', icon: 'none' })
            console.error('上传图片失败:', err)
            that.setData({ [uploadKey]: false })
          })
          .finally(() => wx.hideLoading())
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

  // 添加年份
  addYear() {
    const { importantEvents } = this.data.formData
    importantEvents.push({
      year: '',
      events: ['']
    })
    this.setData({
      'formData.importantEvents': importantEvents
    })
  },

  // 删除年份
  deleteYear(e) {
    const { index } = e.currentTarget.dataset
    const { importantEvents } = this.data.formData
    importantEvents.splice(index, 1)
    this.setData({
      'formData.importantEvents': importantEvents
    })
  },

  // 更新年份
  handleYearChange(e) {
    const { index } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`formData.importantEvents[${index}].year`]: value
    })
  },

  // 添加事件
  addEvent(e) {
    const { yearIndex } = e.currentTarget.dataset
    const { importantEvents } = this.data.formData
    importantEvents[yearIndex].events.push('')
    this.setData({
      'formData.importantEvents': importantEvents
    })
  },

  // 删除事件
  deleteEvent(e) {
    const { yearIndex, eventIndex } = e.currentTarget.dataset
    const { importantEvents } = this.data.formData
    importantEvents[yearIndex].events.splice(eventIndex, 1)
    this.setData({
      'formData.importantEvents': importantEvents
    })
  },

  // 更新事件内容
  handleEventChange(e) {
    const { yearIndex, eventIndex } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`formData.importantEvents[${yearIndex}].events[${eventIndex}]`]: value
    })
  },

  // 提交表单
  async submitForm() {
    try {
      this.setData({ loading: true })

      // 深拷贝并转换 JSON 字段
      const submitData = { ...this.data.formData }
      if (submitData.importantEvents) {
        // 过滤掉空的年份和事件
        const filteredEvents = submitData.importantEvents
          .filter(item => item.year || item.events.some(e => e))
          .map(item => ({
            year: item.year,
            events: item.events.filter(e => e)
          }))
          .filter(item => item.events.length > 0)
        
        submitData.importantEvents = JSON.stringify(filteredEvents)
      }

      const res = await app.api.localPlatformApi.updateLocalPlatform(submitData)

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