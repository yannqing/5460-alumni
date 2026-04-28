const { couponApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

function formatCouponDate(time) {
  if (!time) return ''
  return String(time).replace('T', ' ').slice(0, 10)
}

function normalizeCoupon(coupon) {
  const typeLabelMap = {
    1: '折扣券',
    2: '满减券',
    3: '礼品券',
  }
  const couponType = Number(coupon.couponType || 0)

  return {
    ...coupon,
    couponId: String(coupon.couponId || ''),
    title: coupon.couponName || '',
    endTime: formatCouponDate(coupon.validEndTime),
    stock:
      coupon.remainQuantity !== undefined && coupon.remainQuantity !== null
        ? coupon.remainQuantity
        : 0,
    totalStock:
      coupon.totalQuantity !== undefined && coupon.totalQuantity !== null
        ? coupon.totalQuantity
        : 1,
    couponType: couponType,
  }
}

Page({
  data: {
    merchantId: '',
    merchantName: '',
    coupons: [],
    current: 1,
    pageSize: 10,
    hasNext: true,
    loading: false,
    refreshing: false,
    couponCardBgUrl: config.cloud.cosBaseUrl + '/cni-alumni/images/assets/coupon/coupon_1.png',
  },

  onLoad(options) {
    const merchantId = options.merchantId || ''
    const merchantName = options.merchantName ? decodeURIComponent(options.merchantName) : ''
    this.setData({ merchantId, merchantName })
    this.loadCoupons(true)
  },

  async loadCoupons(reset = false) {
    if (this.data.loading) return
    if (!this.data.merchantId) {
      wx.showToast({ title: '商户信息缺失', icon: 'none' })
      return
    }
    if (!reset && !this.data.hasNext) return

    const nextCurrent = reset ? 1 : this.data.current
    this.setData({ loading: true })
    try {
      const res = await couponApi.getMerchantCouponPage(this.data.merchantId, {
        current: nextCurrent,
        pageSize: this.data.pageSize,
      })
      if (res.data && res.data.code === 200) {
        const pageData = res.data.data || {}
        const records = (pageData.records || []).map(normalizeCoupon)
        this.setData({
          coupons: reset ? records : this.data.coupons.concat(records),
          current: nextCurrent + 1,
          hasNext: !!pageData.hasNext,
        })
      } else {
        wx.showToast({ title: res.data?.msg || '加载失败', icon: 'none' })
      }
    } catch (error) {
      console.error('[MerchantCouponList] 加载优惠券失败:', error)
      wx.showToast({ title: '网络错误', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  loadMore() {
    this.loadCoupons(false)
  },

  refreshList() {
    this.setData({ refreshing: true, hasNext: true })
    this.loadCoupons(true)
  },

  viewCouponDetail(e) {
    const id = e.detail?.id || e.currentTarget?.dataset?.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/coupon/public-detail/detail?id=${encodeURIComponent(String(id))}`,
    })
  },
})
