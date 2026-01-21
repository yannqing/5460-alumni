// pages/coupon/list/list.js
const { couponApi } = require('../../../api/api.js')

Page({
  data: {
    activeTab: 0,
    tabs: ['全部', '抢购'],
    couponList: [],
    loading: false
  },

  onLoad(options) {
    this.loadCouponList()
  },

  async loadCouponList() {
    this.setData({ loading: true })

    try {
      // 分页参数：这里只拉取第一页，可根据需要扩展
      const params = {
        current: 1,
        size: 20
      }
      const res = await couponApi.getMyCoupons(params)

      if (res.data && res.data.code === 200) {
        const pageData = res.data.data || {}
        const records = pageData.records || []

        const list = records.map(item => {
          const statusTextMap = {
            1: '未使用',
            2: '已使用',
            3: '已过期',
            4: '已作废'
          }

          return {
            // 使用 couponId 作为详情/抢购用的主键
            id: item.couponId || item.userCouponId,
            title: item.couponName || '',
            merchant: item.merchantName || item.shopName || '',
            discountPrice: item.discountValue || 0,
            discount: statusTextMap[item.status] || '优惠券',
            endTime: item.validEndTime || '',
            // 个人券没有库存概念，这里固定为 1/1，进度条展示为满
            stock: 1,
            totalStock: 1,
            // 根据状态简单区分是否可用
            type: item.status === 1 ? 'normal' : 'disabled',
            rawStatus: item.status
          }
        })

        this.setData({ couponList: list })
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '加载失败',
          icon: 'none'
        })
        this.setData({ couponList: [] })
      }
    } catch (err) {
      console.error('[CouponList] 获取我的优惠券失败:', err)
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
      this.setData({ couponList: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: index })
    this.loadCouponList()
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?id=${id}`
    })
  },

  handleCoupon(e) {
    const { id, type } = e.currentTarget.dataset

    if (type === 'rush') {
      wx.navigateTo({
        url: `/pages/coupon/rush/rush?id=${id}`
      })
    } else {
      wx.showModal({
        title: '提示',
        content: '确认领取该优惠券吗？',
        success: (res) => {
          if (res.confirm) {
            wx.showToast({
              title: '领取成功',
              icon: 'success'
            })
          }
        }
      })
    }
  }
})
