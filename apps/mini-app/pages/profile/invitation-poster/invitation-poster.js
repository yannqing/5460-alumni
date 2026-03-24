// pages/profile/invitation-poster/invitation-poster.js
const { get } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    list: [],
    loading: false
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
  }
})
