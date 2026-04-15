const { couponApi } = require('../../../../../api/api.js')

const COUPON_TYPE_LABEL = { 1: '折扣券', 2: '满减券', 3: '礼品券' }
const DISCOUNT_TYPE_LABEL = { 1: '固定金额', 2: '折扣比例' }
const STATUS_LABEL = { 0: '未发布', 1: '已发布', 2: '已结束', 3: '已下架' }
const PUBLISH_TYPE_LABEL = { 1: '立即发布', 2: '定时发布' }

function formatDateTime(value) {
  if (!value) {
    return ''
  }
  const s = String(value).replace('T', ' ')
  return s.length > 19 ? s.slice(0, 19) : s
}

function mapCouponDetail(data) {
  const couponType = data.couponType
  let discountDisplay = '—'
  if (couponType === 1) {
    if (data.discountType === 2 && data.discountValue != null) {
      discountDisplay = `${data.discountValue}折`
    } else if (data.discountValue != null) {
      discountDisplay = `${data.discountValue}元`
    }
  } else if (couponType === 2) {
    const minSpend = data.minSpend != null ? data.minSpend : '—'
    const discountValue = data.discountValue != null ? data.discountValue : '—'
    discountDisplay = `满${minSpend}减${discountValue}`
  } else if (couponType === 3) {
    discountDisplay = data.couponName || '礼品券'
  }

  return {
    ...data,
    couponTypeLabel: COUPON_TYPE_LABEL[couponType] || '优惠券',
    discountTypeLabel: DISCOUNT_TYPE_LABEL[data.discountType] || '',
    statusLabel: STATUS_LABEL[data.status] || '',
    publishTypeLabel: PUBLISH_TYPE_LABEL[data.publishType] || '',
    alumniOnlyLabel: Number(data.isAlumniOnly) === 1 ? '是' : '否',
    validStartText: formatDateTime(data.validStartTime) || '—',
    validEndText: formatDateTime(data.validEndTime) || '—',
    publishTimeText: formatDateTime(data.publishTime) || '—',
    createTimeText: formatDateTime(data.createTime) || '—',
    updateTimeText: formatDateTime(data.updateTime) || '—',
    discountDisplay,
  }
}

Page({
  data: {
    couponId: '',
    loading: true,
    couponDetail: null,
    availableShops: [],
  },

  onLoad(options) {
    const couponId = options.couponId ? decodeURIComponent(options.couponId) : ''
    if (!couponId) {
      wx.showToast({ title: '优惠券ID不能为空', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1200)
      return
    }

    // 雪花 ID 可能超出 Number 安全范围，始终按字符串使用
    this.setData({ couponId: String(couponId) })
    this.loadCouponDetail()
  },

  async loadCouponDetail() {
    this.setData({ loading: true })
    try {
      const res = await couponApi.getManagementCouponDetail(this.data.couponId)
      if (res.data && res.data.code === 200 && res.data.data) {
        const detailData = res.data.data || {}
        const couponData = detailData.coupon || null
        this.setData({
          couponDetail: couponData ? mapCouponDetail(couponData) : null,
          availableShops: Array.isArray(detailData.availableShops) ? detailData.availableShops : [],
          loading: false,
        })
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '加载失败',
          icon: 'none',
        })
        this.setData({ loading: false, couponDetail: null, availableShops: [] })
      }
    } catch (error) {
      console.error('加载优惠券详情失败:', error)
      wx.showToast({ title: '加载优惠券详情失败', icon: 'none' })
      this.setData({ loading: false, couponDetail: null, availableShops: [] })
    }
  },
})
