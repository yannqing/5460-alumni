// pages/audit/merchant/coupon/coupon.js
const { merchantApi, couponApi } = require('../../../../api/api.js')

const COUPON_TYPE_LABEL = { 1: '折扣券', 2: '满减券', 3: '礼品券' }
const STATUS_LABEL = { 0: '未发布', 1: '已发布', 2: '已结束', 3: '已下架' }

function formatCouponRow(item) {
  const validEnd = item.validEndTime
    ? String(item.validEndTime).replace('T', ' ').slice(0, 16)
    : ''
  let discountText = ''
  if (item.couponType === 1) {
    discountText =
      item.discountValue != null
        ? `${item.discountValue}折`
        : '折扣'
  } else if (item.couponType === 2) {
    const min = item.minSpend != null ? item.minSpend : 0
    const val = item.discountValue != null ? item.discountValue : ''
    discountText = val !== '' ? `满${min}减${val}` : '满减'
  } else {
    discountText = item.couponName || '礼品券'
  }
  return {
    ...item,
    _couponTypeLabel: COUPON_TYPE_LABEL[item.couponType] || '优惠券',
    _statusLabel: STATUS_LABEL[item.status] ?? '',
    _validEndShort: validEnd,
    _discountText: discountText,
  }
}

Page({
  data: {
    merchantLoading: false,
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',

    couponList: [],
    couponLoading: false,
    couponCurrentPage: 1,
    couponPageSize: 10,
    couponTotal: 0,
    couponPages: 0,
    couponHasNext: false,
    couponHasPrevious: false,
    couponDeleting: false,

    scrollListHeight: 400,
  },

  onLoad() {
    this.setScrollListHeight()
  },

  onShow() {
    this.loadMerchants()
  },

  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      const scrollH = Math.floor(contentH * 0.5)
      this.setData({ scrollListHeight: scrollH > 200 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  async loadMerchants() {
    let merchantIdForList = ''
    try {
      this.setData({ merchantLoading: true })
      const res = await merchantApi.getMyMerchants({
        current: 1,
        size: 100,
      })

      if (res.data && res.data.code === 200) {
        const merchantList = res.data.data.records || []
        this.setData({ merchantList })

        const { selectedMerchantId } = this.data
        if (!selectedMerchantId && merchantList.length > 0) {
          const first = merchantList[0]
          merchantIdForList = first.merchantId + ''
          this.setData({
            selectedMerchantId: merchantIdForList,
            selectedMerchantName: first.merchantName,
          })
        } else if (selectedMerchantId) {
          merchantIdForList = selectedMerchantId
          const match = merchantList.find(
            m => String(m.merchantId) === String(selectedMerchantId)
          )
          if (match) {
            this.setData({ selectedMerchantName: match.merchantName })
          }
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none',
      })
    } finally {
      this.setData({ merchantLoading: false })
    }

    this.setScrollListHeight()
    if (merchantIdForList) {
      await this.loadCouponList(1)
    } else {
      this.setData({
        couponList: [],
        couponTotal: 0,
        couponPages: 0,
        couponCurrentPage: 1,
        couponHasNext: false,
        couponHasPrevious: false,
      })
    }
  },

  async loadCouponList(page) {
    const merchantId = this.data.selectedMerchantId
    if (!merchantId) {
      return
    }

    this.setData({ couponLoading: true })

    try {
      const res = await couponApi.getManagementCouponList({
        current: page,
        pageSize: this.data.couponPageSize,
        merchantId: String(merchantId),
      })

      if (res.data && res.data.code === 200) {
        const d = res.data.data || {}
        const records = (d.records || []).map(formatCouponRow)
        const cur = d.current != null ? Number(d.current) : page
        const pages = d.pages != null ? Number(d.pages) : 1
        const hasNext =
          d.hasNext !== undefined && d.hasNext !== null
            ? d.hasNext
            : cur < pages
        const hasPrev =
          d.hasPrevious !== undefined && d.hasPrevious !== null
            ? d.hasPrevious
            : cur > 1

        this.setData({
          couponList: records,
          couponCurrentPage: cur,
          couponTotal: d.total != null ? d.total : records.length,
          couponPages: pages,
          couponHasNext: hasNext,
          couponHasPrevious: hasPrev,
        })
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '加载失败',
          icon: 'none',
        })
        this.setData({ couponList: [] })
      }
    } catch (error) {
      console.error('加载优惠券列表失败:', error)
      wx.showToast({ title: '加载优惠券列表失败', icon: 'none' })
      this.setData({ couponList: [] })
    } finally {
      this.setData({ couponLoading: false })
    }
  },

  goCouponPrevPage() {
    const { couponCurrentPage, couponLoading, couponHasPrevious } = this.data
    if (couponLoading || !couponHasPrevious || couponCurrentPage <= 1) {
      return
    }
    this.loadCouponList(couponCurrentPage - 1)
  },

  goCouponNextPage() {
    const { couponCurrentPage, couponLoading, couponHasNext } = this.data
    if (couponLoading || !couponHasNext) {
      return
    }
    this.loadCouponList(couponCurrentPage + 1)
  },

  onCouponScrollToLower() {
    this.goCouponNextPage()
  },

  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  async selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName

    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false,
    })

    await this.loadCouponList(1)
  },

  goCreateCoupon() {
    const { selectedMerchantId, selectedMerchantName } = this.data
    if (!selectedMerchantId) {
      wx.showToast({ title: '请先选择商户', icon: 'none' })
      return
    }
    const q = [
      `merchantId=${encodeURIComponent(selectedMerchantId)}`,
      `merchantName=${encodeURIComponent(selectedMerchantName || '')}`,
    ]
    wx.navigateTo({
      url: `/pages/audit/merchant/coupon/create/create?${q.join('&')}`,
    })
  },

  goCouponDetail(e) {
    const couponId = e.currentTarget.dataset.couponId
    if (!couponId) {
      wx.showToast({ title: '优惠券ID缺失', icon: 'none' })
      return
    }
    // 雪花 ID 超出 JS 安全整数范围，保持字符串传递
    wx.navigateTo({
      url: `/pages/audit/merchant/coupon/detail/detail?couponId=${encodeURIComponent(String(couponId))}`,
    })
  },

  goEditCoupon(e) {
    const couponId = e.currentTarget.dataset.couponId
    const { selectedMerchantId, selectedMerchantName } = this.data
    if (!couponId) {
      wx.showToast({ title: '优惠券ID缺失', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/audit/merchant/coupon/edit/edit?couponId=${encodeURIComponent(String(couponId))}&merchantId=${encodeURIComponent(String(selectedMerchantId || ''))}&merchantName=${encodeURIComponent(selectedMerchantName || '')}`,
    })
  },

  confirmDeleteCoupon(e) {
    const couponId = e.currentTarget.dataset.couponId
    if (!couponId) {
      wx.showToast({ title: '优惠券ID缺失', icon: 'none' })
      return
    }
    wx.showModal({
      title: '删除确认',
      content: '删除后不可恢复，确定删除该优惠券吗？',
      confirmColor: '#ff4d4f',
      success: ({ confirm }) => {
        if (confirm) {
          this.deleteCouponById(String(couponId))
        }
      },
    })
  },

  async deleteCouponById(couponId) {
    if (!couponId || this.data.couponDeleting) {
      return
    }

    this.setData({ couponDeleting: true })
    wx.showLoading({ title: '删除中...', mask: true })

    try {
      const res = await couponApi.deleteManagementCoupon(String(couponId))
      const ok = res && res.data && res.data.code === 200 && res.data.data === true
      if (!ok) {
        wx.showToast({
          title: (res && res.data && res.data.msg) || '删除失败',
          icon: 'none',
        })
        return
      }

      wx.showToast({ title: '删除成功', icon: 'success' })

      const { couponCurrentPage, couponList } = this.data
      const isLastItemOnPage = Array.isArray(couponList) && couponList.length <= 1
      const targetPage = isLastItemOnPage && couponCurrentPage > 1
        ? couponCurrentPage - 1
        : couponCurrentPage
      await this.loadCouponList(targetPage)
    } catch (error) {
      console.error('删除优惠券失败:', error)
      wx.showToast({ title: '删除失败', icon: 'none' })
    } finally {
      wx.hideLoading()
      this.setData({ couponDeleting: false })
    }
  },
})