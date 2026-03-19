const config = require('../../../../utils/config.js')
const { associationApi } = require('../../../../api/api.js')

Page({
  data: {
    id: '',
    loading: false,
    detail: null,
    statusText: '',
    statusClass: '',
    imageUrls: [],
    defaultAlumniAvatar: config.defaultAvatar,
    defaultBackground: config.defaultCover,
  },

  onLoad(options) {
    const id = options && options.id ? options.id : ''
    if (!id) {
      wx.showToast({
        title: '参数错误',
        icon: 'none',
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1200)
      return
    }
    this.setData({ id })
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const res = await associationApi.getJoinApplyDetailWithAttachment(this.data.id)
      if (!res.data || res.data.code !== 200 || !res.data.data) {
        wx.showToast({
          title: (res.data && res.data.msg) || '获取详情失败',
          icon: 'none',
        })
        this.setData({ detail: null })
        return
      }

      const detail = this.normalizeDetail(res.data.data)
      this.setData({
        detail,
        statusText: this.getStatusText(detail.status),
        statusClass: this.getStatusClass(detail.status),
        imageUrls: detail.attachmentFiles
          .filter(file => file.isImage && !!file.fileUrl)
          .map(file => file.fileUrl),
      })
    } catch (error) {
      console.error('[Debug] 获取校友会认证详情失败:', error)
      wx.showToast({
        title: '网络异常，请稍后重试',
        icon: 'none',
      })
      this.setData({ detail: null })
    } finally {
      this.setData({ loading: false })
    }
  },

  normalizeDetail(raw) {
    const normalized = { ...raw }
    normalized.createTime = this.formatDateTime(raw.createTime)
    normalized.logo = this.normalizeUrl(raw.logo)
    normalized.bgImg = this.parseBgImg(raw.bgImg)
    normalized.applyLogo = this.normalizeUrl(raw.applyLogo)
    normalized.applyBgImg = this.parseBgImg(raw.applyBgImg)
    normalized.applicantAvatarUrl = this.normalizeUrl(raw.applicantAvatarUrl)
    normalized.attachmentFiles = Array.isArray(raw.attachmentFiles)
      ? raw.attachmentFiles.map(file => {
          const fileType = (file.fileType || '').toLowerCase()
          const fileExtension = (file.fileExtension || (file.filePath ? file.filePath.split('.').pop() : '') || '').toLowerCase()
          const rawUrl = file.fileUrl || file.url || file.filePath || ''
          const fileUrl = this.normalizeUrl(rawUrl)
          const isImage =
            fileType.includes('image') ||
            ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(fileExtension)
          
          const fileSize = file.fileSize || 0
          let fileSizeText = '-'
          if (fileSize > 0) {
            if (fileSize < 1024) fileSizeText = fileSize + ' B'
            else if (fileSize < 1024 * 1024) fileSizeText = (fileSize / 1024).toFixed(1) + ' KB'
            else fileSizeText = (fileSize / (1024 * 1024)).toFixed(1) + ' MB'
          }

          return {
            ...file,
            fileUrl,
            rawUrl, // 保存原始地址用于展示
            displayName: file.displayName || (file.filePath ? file.filePath.split('/').pop() : '未命名文件'),
            isImage,
            fileSizeText,
          }
        })
      : []
    return normalized
  },

  normalizeUrl(url) {
    if (!url || typeof url !== 'string') return ''
    if (url.startsWith('http://') || url.startsWith('https://')) return url
    return config.getImageUrl(url)
  },

  parseBgImg(bgImg) {
    if (!bgImg || typeof bgImg !== 'string' || !bgImg.trim()) return config.defaultCover
    try {
      const parsed = JSON.parse(bgImg)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return this.normalizeUrl(parsed[0])
      }
      return this.normalizeUrl(bgImg)
    } catch (e) {
      return this.normalizeUrl(bgImg)
    }
  },

  formatDateTime(dateTime) {
    if (!dateTime) return ''
    return String(dateTime).replace('T', ' ')
  },

  getStatusText(status) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
    }
    return statusMap[Number(status)] || '未知'
  },

  getStatusClass(status) {
    const num = Number(status)
    if (num === 0) return 'status-pending'
    if (num === 1) return 'status-approved'
    if (num === 2) return 'status-rejected'
    return 'status-unknown'
  },

  handleAttachmentTap(e) {
    const { isImage } = e.currentTarget.dataset
    if (isImage) {
      this.previewImage(e)
    } else {
      this.openAttachment(e)
    }
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset
    if (!url) return
    wx.previewImage({
      current: url,
      urls: this.data.imageUrls.length ? this.data.imageUrls : [url],
    })
  },

  openAttachment(e) {
    const { url } = e.currentTarget.dataset
    if (!url) {
      wx.showToast({
        title: '文件地址无效',
        icon: 'none',
      })
      return
    }
    wx.showLoading({ title: '打开中...' })
    wx.downloadFile({
      url,
      success: res => {
        if (res.statusCode !== 200 || !res.tempFilePath) {
          wx.showToast({ title: '文件下载失败', icon: 'none' })
          return
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          showMenu: true,
          fail: () => {
            wx.showToast({ title: '当前文件暂不支持预览', icon: 'none' })
          },
        })
      },
      fail: () => {
        wx.showToast({ title: '文件下载失败', icon: 'none' })
      },
      complete: () => {
        wx.hideLoading()
      },
    })
  },
})
