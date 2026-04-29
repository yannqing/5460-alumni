// components/merchant-card/merchant-card.js
const config = require('../../utils/config.js')

Component({
  properties: {
    item: {
      type: Object,
      value: {},
    },
    iconCategory: {
      type: String,
      value: config.getIconUrl('xx.png'),
    },
  },

  data: {
    logoUrl: '',
    merchantName: '',
    businessCategory: '',
    shopCount: 0,
    favoriteCount: 0,
    isAlumniCertified: 0,
    coupons: [],
    activities: [],
    distance: '',
  },

  lifetimes: {
    attached() {
      this.updateData()
    },
  },

  observers: {
    'item.**': function () {
      this.updateData()
    },
  },

  methods: {
    updateData() {
      const { item } = this.data
      if (!item) return

      const coupons = (item.coupons || []).map(coupon => ({
        ...coupon,
        discountText: this.formatDiscount(coupon),
      }))

      this.setData({
        logoUrl: item.logoUrl || config.defaultAvatar,
        merchantName: item.merchantName || '未命名商户',
        businessCategory: item.businessCategory || '',
        shopCount: item.shopCount || 0,
        favoriteCount: item.favoriteCount || 0,
        isAlumniCertified: item.isAlumniCertified || 0,
        coupons: coupons,
        activities: item.activities || [],
        distance: item.distance || '',
      })
    },

    formatDiscount(coupon) {
      if (!coupon) return ''
      const { discountType, discountValue } = coupon
      if (discountType === 1) {
        // 固定金额
        return `¥${discountValue}`
      } else if (discountType === 2) {
        // 折扣比例
        return `${discountValue}折`
      }
      return `¥${discountValue}`
    },

    handleTap() {
      const { item } = this.data
      if (item && item.merchantId) {
        this.triggerEvent('tap', { merchantId: item.merchantId })
      }
    },

    handleCouponTap(e) {
      const couponId = e.currentTarget.dataset.couponId
      if (couponId) {
        this.triggerEvent('coupontap', { couponId })
      }
    },

    handleActivityTap(e) {
      const activityId = e.currentTarget.dataset.activityId
      if (activityId) {
        this.triggerEvent('activitytap', { activityId })
      }
    },
  },
})
