Component({
  properties: {
    shop: {
      type: Object,
      value: null,
      observer: 'normalizeShop'
    },
    iconLocation: {
      type: String,
      value: ''
    }
  },

  data: {
    displayName: '',
    displayAddress: ''
  },

  methods: {
    normalizeText(value) {
      if (value === null || value === undefined) return ''
      const text = String(value).trim()
      if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') {
        return ''
      }
      return text
    },

    normalizeShop(shop) {
      const data = shop || {}
      const name = this.normalizeText(data.shopName) || this.normalizeText(data.name) || '未命名门店'
      const address =
        this.normalizeText(data.address) ||
        [data.province, data.city, data.district]
          .map((item) => this.normalizeText(item))
          .filter(Boolean)
          .join('') ||
        '暂无地址'

      this.setData({
        displayName: name,
        displayAddress: address
      })
    },

    handleTap() {
      const shop = this.data.shop || {}
      const id = shop.shopId || shop.id
      this.triggerEvent('cardtap', { id, shop })
    }
  }
})
