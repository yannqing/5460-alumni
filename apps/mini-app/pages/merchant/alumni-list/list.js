const { merchantApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    merchantList: [],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    locationIconUrl: '',
  },

  onLoad() {
    this.setData({
      locationIconUrl: `${config.cloud.cosBaseUrl}/cni-alumni/images/assets/icon/location.png`,
    })
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true)
  },

  onReachBottom() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadList(false)
    }
  },

  loadMore() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadList(false)
    }
  },

  async loadList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    this.setData({ loading: true })

    const params = {
      current: reset ? 1 : this.data.current,
      pageSize: this.data.pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
    }

    try {
      const res = await merchantApi.getAlumniMerchantPage(params)
      const body = res.data || {}
      if (body.code !== 200) {
        throw new Error(body.msg || '加载失败')
      }

      const pageData = body.data || {}
      const records = Array.isArray(pageData.records) ? pageData.records : []
      const mapped = records.map(item => this.mapMerchantItem(item))
      const finalList = reset ? mapped : [...this.data.merchantList, ...mapped]

      this.setData({
        merchantList: finalList,
        current: (reset ? 1 : this.data.current) + 1,
        hasMore: !!pageData.hasNext,
      })
    } catch (e) {
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      })
    } finally {
      this.setData({ loading: false })
      if (reset) wx.stopPullDownRefresh()
    }
  },

  resolveLogoUrl(logo) {
    if (!logo) return config.defaultAvatar
    const normalized = String(logo).trim().replace(/[`\s]/g, '')
    return normalized ? config.getImageUrl(normalized) : config.defaultAvatar
  },

  resolveAssociationLogo(alumniAssociation) {
    if (!alumniAssociation || typeof alumniAssociation !== 'object') {
      return config.defaultAvatar
    }
    const rawLogo = alumniAssociation.logo || alumniAssociation.icon || alumniAssociation.avatar || ''
    if (!rawLogo) return config.defaultAvatar
    const normalized = String(rawLogo).trim().replace(/[`\s]/g, '')
    return normalized ? config.getImageUrl(normalized) : config.defaultAvatar
  },

  mapMerchantItem(item) {
    const latestCoupon = item.latestCoupon || null
    const couponName = latestCoupon && latestCoupon.couponName ? String(latestCoupon.couponName) : ''
    return {
      merchantId: item.merchantId ? String(item.merchantId) : '',
      merchantName: item.merchantName || '未命名商户',
      logoUrl: this.resolveLogoUrl(item.logo),
      address: item.address || '',
      associationName:
        item.alumniAssociation && item.alumniAssociation.associationName
          ? item.alumniAssociation.associationName
          : '',
      associationLogoUrl: this.resolveAssociationLogo(item.alumniAssociation),
      latestCoupon,
      couponTypeText: this.getCouponTypeText(latestCoupon && latestCoupon.couponType),
      couponNameShort: couponName.slice(0, 2),
    }
  },

  getCouponTypeText(couponType) {
    const typeMap = {
      1: '折扣券',
      2: '满减券',
      3: '礼品券',
    }
    return typeMap[couponType] || '优惠券'
  },

  onMerchantTap(e) {
    const merchantId = e.currentTarget.dataset && e.currentTarget.dataset.id
    if (!merchantId) return
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${encodeURIComponent(String(merchantId))}`,
    })
  },

})
