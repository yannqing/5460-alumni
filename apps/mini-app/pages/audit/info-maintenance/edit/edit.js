// pages/audit/info-maintenance/edit/edit.js
const app = getApp()
const { fileApi, localPlatformManagementApi } = require('../../../../api/api.js')
const config = require('../../../../utils/config.js')

Page({
  data: {
    platformId: 0,
    platformDetail: null,
    formData: {},
    loading: false,
    uploadingAvatar: false,
    uploadingBgImage: false,
    // 联系人是否入住平台：true=是，false=否
    contactOnPlatform: false,
    // 成员搜索相关
    memberList: [],
    memberSearchKeyword: '',
    memberSearchResults: [],
    memberSearchLoading: false,
    showContactMemberPicker: false,
    defaultUserAvatarUrl: config.defaultAvatar
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
        const hasWxId = detail.wxId != null && detail.wxId !== ''
        this.setData({
          platformDetail: detail,
          contactOnPlatform: !!hasWxId,
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
            contactName: detail.contactName || '',
            contactPosition: detail.contactPosition || '',
            contactPhone: detail.contactPhone || '',
            wxId: hasWxId ? detail.wxId : null,
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

  // 设置联系人是否入住平台
  setContactOnPlatform(e) {
    const onPlatform = e.currentTarget.dataset.onPlatform === 'true'
    this.setData({
      contactOnPlatform: onPlatform,
      'formData.wxId': null
    })
    if (onPlatform) {
      this.loadMemberList()
      // 不再清空 contactName，保留原有姓名
    } else {
      this.setData({
        showContactMemberPicker: false,
        memberSearchKeyword: '',
        memberSearchResults: []
      })
    }
  },

  // 打开联系人选择弹窗
  openContactMemberPicker() {
    this.loadMemberList()
    this.setData({
      showContactMemberPicker: true,
      memberSearchKeyword: '',
      memberSearchResults: []
    })
  },

  // 关闭联系人选择弹窗
  closeContactMemberPicker() {
    this.setData({
      showContactMemberPicker: false,
      memberSearchKeyword: '',
      memberSearchResults: []
    })
  },

  // 加载校促会成员列表
  async loadMemberList() {
    try {
      this.setData({ memberSearchLoading: true })
      const res = await localPlatformManagementApi.getMemberList(this.data.platformId)
      if (res.data && res.data.code === 200) {
        const list = res.data.data || []
        this.setData({
          memberList: list,
          memberSearchLoading: false
        })
        return list
      }
    } catch (error) {
      console.error('加载成员列表失败:', error)
      this.setData({ memberSearchLoading: false })
      wx.showToast({ title: '加载成员列表失败', icon: 'none' })
    }
    return []
  },

  // 搜索联系人（前端过滤）
  onContactSearchInput(e) {
    const keyword = (e.detail.value || '').trim()
    this.setData({ memberSearchKeyword: keyword })
    if (!keyword) {
      this.setData({ memberSearchResults: [] })
      return
    }
    const { memberList } = this.data
    const kw = keyword.toLowerCase()
    const results = memberList.filter(m => {
      const name = (m.username || m.nickname || '').toLowerCase()
      return name.includes(kw)
    })
    this.setData({ memberSearchResults: results })
  },

  // 搜索成员（点击搜索按钮）
  searchContactMembers() {
    const keyword = this.data.memberSearchKeyword.trim()
    if (!keyword) {
      this.setData({ memberSearchResults: [] })
      return
    }
    const { memberList } = this.data
    const kw = keyword.toLowerCase()
    const results = memberList.filter(m => {
      const name = (m.username || m.nickname || '').toLowerCase()
      return name.includes(kw)
    })
    this.setData({ memberSearchResults: results })
  },

  // 选择联系人（入住平台的成员）
  selectContactMember(e) {
    const member = e.currentTarget.dataset.member
    // 保持 wxId 为字符串，避免 JS 大数精度丢失
    const wxId = member.wxId != null && member.wxId !== '' ? String(member.wxId) : null
    this.setData({
      'formData.contactName': member.username || member.nickname || '未命名',
      'formData.wxId': wxId,
      showContactMemberPicker: false,
      memberSearchKeyword: '',
      memberSearchResults: []
    })
  },

  // 清空已选联系人（仅当入住平台时）
  clearContactMember() {
    this.setData({
      'formData.contactName': '',
      'formData.wxId': null
    })
  },

  // 设置状态
  setStatus(e) {
    const { status } = e.currentTarget.dataset
    this.setData({
      'formData.status': parseInt(status)
    })
  },

  // 上传图片（选图 -> 检测大小 -> 必要时压缩 -> 上传服务器）
  // 云托管网关对请求体有大小限制，大图需先压缩
  uploadImage(e) {
    const { field } = e.currentTarget.dataset
    const that = this
    const COMPRESS_THRESHOLD = 1024 * 1024 // 超过 1MB 触发压缩

    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        const uploadKey = field === 'avatar' ? 'uploadingAvatar' : 'uploadingBgImage'
        that.setData({ [uploadKey]: true })
        wx.showLoading({ title: '处理中...', mask: true })

        wx.getFileInfo({
          filePath: tempFilePath,
          success: (fileInfo) => {
            console.log(`[Upload] 原始文件大小: ${(fileInfo.size / 1024).toFixed(1)}KB`)
            if (fileInfo.size > COMPRESS_THRESHOLD) {
              const quality = Math.max(20, Math.floor(COMPRESS_THRESHOLD / fileInfo.size * 80))
              console.log(`[Upload] 文件超过阈值，压缩 quality=${quality}`)
              wx.compressImage({
                src: tempFilePath,
                quality,
                success: (compressRes) => {
                  console.log('[Upload] 压缩完成，使用压缩图上传')
                  that._doUploadImage(compressRes.tempFilePath, field, uploadKey)
                },
                fail: () => {
                  console.warn('[Upload] 压缩失败，尝试上传原图')
                  that._doUploadImage(tempFilePath, field, uploadKey)
                }
              })
            } else {
              that._doUploadImage(tempFilePath, field, uploadKey)
            }
          },
          fail: () => {
            that._doUploadImage(tempFilePath, field, uploadKey)
          }
        })
      }
    })
  },

  _doUploadImage(filePath, field, uploadKey) {
    const that = this
    wx.showLoading({ title: '上传中...', mask: true })

    fileApi
      .uploadImage(filePath)
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
        console.error('[Upload] 上传图片失败:', err)
        that.setData({ [uploadKey]: false })
      })
      .finally(() => wx.hideLoading())
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
      // 联系人是否入住平台：否则传 wxId 为 null
      submitData.wxId = this.data.contactOnPlatform ? (submitData.wxId || null) : null
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