Component({
  properties: {
    coupon: {
      type: Object,
      value: null
    },
    showAction: {
      type: Boolean,
      value: true
    },
    couponBgUrl: {
      type: String,
      value: ''
    }
  },

  data: {
    typeLabelMap: {
      1: '折扣券',
      2: '满减券',
      3: '礼品券'
    }
  },

  methods: {
    formatCouponDate(time) {
      if (!time) return ''
      return String(time).replace('T', ' ').slice(0, 10)
    },

    getStockPercent() {
      const coupon = this.data.coupon || {}
      const stock = coupon.stock || coupon.remainQuantity || 0
      const totalStock = coupon.totalStock || coupon.totalQuantity || 1
      if (totalStock <= 0) return 0
      return Math.max(0, Math.min(100, (stock / totalStock) * 100))
    },

    handleTap() {
      const coupon = this.data.coupon || {}
      const id = coupon.couponId || coupon.userCouponId || coupon.id
      this.triggerEvent('cardtap', { id, coupon })
    },

    handleAction() {
      const coupon = this.data.coupon || {}
      const id = coupon.couponId || coupon.userCouponId || coupon.id
      this.triggerEvent('action', { id, coupon })
    }
  }
})