// pages/profile/invitation-poster/invitation-poster.js
const { get } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    list: [],
    loading: false,
    qrCodeSrc: ''
  },

  onLoad() {
    this.loadPosterTemplates()
    this.loadQrcode()
  },

  loadPosterTemplates() {
    this.setData({ loading: true })

    get('/invitation/poster-templates')
      .then((res) => {
        const resData = res && res.data ? res.data : {}
        if (resData.code === 200 && Array.isArray(resData.data)) {
          const formattedList = (resData.data || []).map((item) => {
            const url = item.url
              ? (config.getImageUrl ? config.getImageUrl(item.url) : item.url)
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

  loadQrcode() {
    get('/invitation/qrcode')
      .then((res) => {
        const resData = res && res.data ? res.data : {}
        if (resData.code === 200 && resData.data && resData.data.qrCodeBase64) {
          const base64 = resData.data.qrCodeBase64
          const qrCodeSrc = base64.startsWith('data:')
            ? base64
            : 'data:image/png;base64,' + base64
          this.setData({ qrCodeSrc })
        }
      })
      .catch(() => {})
  }
})
