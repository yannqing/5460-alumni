// pages/profile/invitation-poster/invitation-poster.js
const { get } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    list: [],
    loading: true,
    canvasWidth: 750,
    canvasHeight: 1200
  },

  onLoad() {
    this.loadPosterTemplates()
  },

  loadPosterTemplates() {
    this.setData({ loading: true })

    get('/invitation/poster-templates/rendered')
      .then((res) => {
        const resData = res && res.data ? res.data : {}
        if (resData.code === 200 && Array.isArray(resData.data)) {
          const formattedList = (resData.data || []).map((item) => {
            const rawUrl = item.url || ''
            const url = rawUrl
              ? (rawUrl.startsWith('data:')
                ? rawUrl
                : (config.getImageUrl ? config.getImageUrl(rawUrl) : rawUrl))
              : ''
            return {
              ...item,
              imageUrl: url
            }
          })
          this.setData({ list: formattedList })
        } else {
          this.setData({ list: [] })
        }
      })
      .catch(() => {
        this.setData({ list: [] })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  /**
   * 长按保存海报到手机相册
   * 长按后先弹出确认，再执行保存
   */
  onPosterLongPress(e) {
    const dataset = e && e.currentTarget ? e.currentTarget.dataset : {}
    const url = dataset ? dataset.url : ''
    if (!url) return

    wx.showActionSheet({
      itemList: ['保存到手机'],
      success: (res) => {
        if (res && res.tapIndex === 0) {
          this.savePosterWithText(url)
        }
      },
      fail: () => {}
    })
  },

  savePosterWithText(url) {
    wx.showLoading({ title: '正在保存...' })

    this.ensureAlbumPermission()
      .then(() => this.downloadImageAsTempFile(url))
      .then((localImagePath) => this.composePosterWithCopy(localImagePath))
      .then((tempFilePath) => this.saveImageToAlbum(tempFilePath))
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '已保存到相册', icon: 'success' })
      })
      .catch((err) => {
        wx.hideLoading()
        wx.showToast({
          title: (err && (err.message || err.errMsg)) || '保存失败',
          icon: 'none'
        })
      })
  },

  composePosterWithCopy(imagePath) {
    return this.getImageInfo(imagePath).then((imageInfo) => {
      const originalWidth = imageInfo.width || 750
      const originalHeight = imageInfo.height || 1200
      const maxWidth = 1080
      const scale = originalWidth > maxWidth ? (maxWidth / originalWidth) : 1
      const canvasWidth = Math.round(originalWidth * scale)
      const canvasHeight = Math.round(originalHeight * scale)

      this.setData({ canvasWidth, canvasHeight })

      return this.waitNextTick().then(() => {
        const ctx = wx.createCanvasContext('posterSaveCanvas', this)
        ctx.drawImage(imagePath, 0, 0, canvasWidth, canvasHeight)
        this.drawPosterCopyText(ctx, canvasWidth, canvasHeight, imageInfo)
        return this.canvasDrawAndExport(ctx, canvasWidth, canvasHeight)
      })
    })
  },

  /**
   * 与 WXML/WXSS 对齐：海报在页面上的宽度为 group_7 内容区 714-50=664rpx；
   * rpx 转 px 用 windowWidth/750；文案位置按「页面显示区域」比例映射到画布（底图像素尺寸）。
   */
  drawPosterCopyText(ctx, canvasWidth, canvasHeight, imageInfo) {
    const textLine1 = 'HI 同学：'
    const textLine2 = '思念母校，发现校友；搭建校友平台，助力校友经济，邀请您加入中国校友录5460，享5460专属权益。'

    const sys = wx.getSystemInfoSync ? wx.getSystemInfoSync() : {}
    const windowWidth = sys.windowWidth || 375
    const rpxPx = windowWidth / 750
    // 与 invitation-poster.wxss 中 .group_7 宽度 714rpx、左右 padding 25rpx 一致
    const POSTER_WIDTH_RPX = 714 - 25 * 2

    const naturalW = (imageInfo && imageInfo.width) || canvasWidth
    const naturalH = (imageInfo && imageInfo.height) || canvasHeight
    const imageDisplayWidthPx = POSTER_WIDTH_RPX * rpxPx
    const imageDisplayHeightPx = imageDisplayWidthPx * (naturalH / naturalW)

    const scaleX = canvasWidth / imageDisplayWidthPx
    const scaleY = canvasHeight / imageDisplayHeightPx

    // WXSS: left/right 72rpx, top 680rpx; font 30rpx / 26rpx; line2 margin-top 8rpx
    const leftPx = 72 * rpxPx
    const topPx = 680 * rpxPx
    const left = leftPx * scaleX
    const top = topPx * scaleY
    const maxWidth = canvasWidth - leftPx * scaleX * 2

    const line1Font = Math.max(12, Math.round(30 * rpxPx * scaleX))
    const line2Font = Math.max(12, Math.round(26 * rpxPx * scaleX))
    // .poster-copy-line2 { margin-top: 8rpx; }
    const lineGap = Math.max(4, Math.round(8 * rpxPx * scaleY))
    const paragraphGap = Math.max(4, Math.round(8 * rpxPx * scaleY))

    ctx.setFillStyle('#5f6b7a')
    ctx.setTextBaseline('top')

    ctx.setFontSize(line1Font)
    ctx.fillText(textLine1, left, top)

    ctx.setFontSize(line2Font)
    const text2Y = top + line1Font + lineGap
    const indent = line2Font * 2
    const wrappedLines = this.wrapTextByWidth(ctx, textLine2, maxWidth - indent)

    for (let i = 0; i < wrappedLines.length; i++) {
      const line = wrappedLines[i]
      const x = i === 0 ? (left + indent) : left
      const y = text2Y + i * (line2Font + paragraphGap)
      ctx.fillText(line, x, y)
    }
  },

  wrapTextByWidth(ctx, text, maxWidth) {
    const lines = []
    let current = ''
    for (let i = 0; i < text.length; i++) {
      const ch = text[i]
      const testLine = current + ch
      if (ctx.measureText(testLine).width > maxWidth && current) {
        lines.push(current)
        current = ch
      } else {
        current = testLine
      }
    }
    if (current) lines.push(current)
    return lines
  },

  waitNextTick() {
    return new Promise((resolve) => {
      if (typeof wx.nextTick === 'function') {
        wx.nextTick(() => resolve())
      } else {
        setTimeout(() => resolve(), 50)
      }
    })
  },

  canvasDrawAndExport(ctx, width, height) {
    return new Promise((resolve, reject) => {
      ctx.draw(false, () => {
        wx.canvasToTempFilePath({
          canvasId: 'posterSaveCanvas',
          width,
          height,
          destWidth: width,
          destHeight: height,
          fileType: 'png',
          quality: 1,
          success: (res) => resolve(res.tempFilePath),
          fail: (err) => reject(err)
        }, this)
      })
    })
  },

  saveImageToAlbum(filePath) {
    return new Promise((resolve, reject) => {
      wx.saveImageToPhotosAlbum({
        filePath,
        success: () => resolve(),
        fail: (err) => reject(err)
      })
    })
  },

  getImageInfo(src) {
    return new Promise((resolve, reject) => {
      wx.getImageInfo({
        src,
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })
  },

  ensureAlbumPermission() {
    return new Promise((resolve, reject) => {
      wx.getSetting({
        success: (res) => {
          const authSetting = res && res.authSetting ? res.authSetting : {}
          const isAuthorized = !!authSetting['scope.writePhotosAlbum']
          if (isAuthorized) return resolve()

          wx.authorize({
            scope: 'scope.writePhotosAlbum',
            success: () => resolve(),
            fail: (err) => reject(err)
          })
        },
        fail: (err) => reject(err)
      })
    })
  },

  downloadImageAsTempFile(url) {
    if (typeof url !== 'string' || !url.trim()) {
      return Promise.reject(new Error('图片地址为空'))
    }

    if (url.startsWith('data:image/')) {
      return this.writeBase64ImageToFile(url)
    }

    const normalizedUrl = this.normalizeNetworkImageUrl(url)
    if (!normalizedUrl) {
      return Promise.reject(new Error('图片地址格式不支持'))
    }

    return new Promise((resolve, reject) => {
      wx.downloadFile({
        url: normalizedUrl,
        success: (downloadRes) => {
          const statusCode = downloadRes && downloadRes.statusCode
          if (statusCode && statusCode !== 200) {
            return reject(new Error(`download failed, statusCode: ${statusCode}`))
          }
          const tempFilePath = downloadRes && downloadRes.tempFilePath
          if (!tempFilePath) {
            return reject(new Error('download failed: empty tempFilePath'))
          }
          resolve(tempFilePath)
        },
        fail: (err) => reject(err)
      })
    })
  },

  normalizeNetworkImageUrl(url) {
    if (!url || typeof url !== 'string') return ''
    const trimmed = url.trim()
    if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
      return trimmed
    }
    if (trimmed.startsWith('//')) {
      return `https:${trimmed}`
    }
    return ''
  },

  writeBase64ImageToFile(dataUrl) {
    return new Promise((resolve, reject) => {
      const match = /^data:image\/([a-zA-Z0-9+]+);base64,(.+)$/.exec(dataUrl)
      if (!match) {
        return reject(new Error('base64 图片格式不正确'))
      }

      const ext = (match[1] || 'png').replace('jpeg', 'jpg')
      const base64Data = match[2]
      const fs = wx.getFileSystemManager()
      const filePath = `${wx.env.USER_DATA_PATH}/invitation_poster_${Date.now()}.${ext}`

      fs.writeFile({
        filePath,
        data: base64Data,
        encoding: 'base64',
        success: () => resolve(filePath),
        fail: (err) => reject(err)
      })
    })
  }
})
