// pages/coupon/list/list.js
const { couponApi } = require('../../../api/api.js')

Page({
  data: {
    activeTab: 0,
    // 0: 全部  1: 折扣券  2: 满减券  3: 礼品券
    tabs: ['全部', '折扣券', '满减券', '礼品券'],
    allCoupons: [],
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

        const statusTextMap = {
          1: '未使用',
          2: '已使用',
          3: '已过期',
          4: '已作废'
        }

        const couponTypeLabelMap = {
          1: '折扣券',
          2: '满减券',
          3: '礼品券'
        }

        const list = records.map(item => {

          return {
            // 使用 couponId 作为详情/抢购用的主键
            id: item.couponId || item.userCouponId,
            // 保存 userCouponId 用于详情页查询，确保有值
            userCouponId: item.userCouponId || '',
            title: item.couponName || '',
            merchant: item.merchantName || item.shopName || '',
            discountPrice: item.discountValue || 0,
            // 左侧金额区备用文案（当没有金额时显示券类型）
            discount: couponTypeLabelMap[item.couponType] || '优惠券',
            endTime: item.validEndTime || '',
            // 个人券没有库存概念，这里固定为 1/1，进度条展示为满
            stock: 1,
            totalStock: 1,
            // 根据状态简单区分是否可用
            type: item.status === 1 ? 'normal' : 'disabled',
            rawStatus: item.status,
            statusText: statusTextMap[item.status] || '',
            couponType: item.couponType || 0
          }
        })

        this.setData({ allCoupons: list }, () => {
          this.applyFilter()
        })
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '加载失败',
          icon: 'none'
        })
        this.setData({ allCoupons: [], couponList: [] })
      }
    } catch (err) {
      console.error('[CouponList] 获取我的优惠券失败:', err)
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
      this.setData({ allCoupons: [], couponList: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: index })
    this.applyFilter()
  },

  // 根据当前 tab 过滤优惠券
  applyFilter() {
    const { activeTab, allCoupons } = this.data

    if (!allCoupons || allCoupons.length === 0) {
      this.setData({ couponList: [] })
      return
    }

    let filtered = allCoupons
    if (activeTab === 1) {
      filtered = allCoupons.filter(item => item.couponType === 1)
    } else if (activeTab === 2) {
      filtered = allCoupons.filter(item => item.couponType === 2)
    } else if (activeTab === 3) {
      filtered = allCoupons.filter(item => item.couponType === 3)
    }

    this.setData({ couponList: filtered })
  },

  viewDetail(e) {
    const { userCouponId, index } = e.currentTarget.dataset
    // 如果从 dataset 获取不到，尝试从 couponList 中获取
    let finalUserCouponId = userCouponId
    if (!finalUserCouponId && (index !== undefined && index !== null)) {
      const item = this.data.couponList[index]
      if (item && item.userCouponId) {
        finalUserCouponId = item.userCouponId
      }
    }
    // 检查 userCouponId 是否有效（不能为空字符串、null、undefined）
    if (!finalUserCouponId || finalUserCouponId === 'null' || finalUserCouponId === 'undefined') {
      console.error('[CouponList] userCouponId 无效:', { userCouponId, finalUserCouponId, index, item: index !== undefined ? this.data.couponList[index] : null })
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      return
    }
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?userCouponId=${finalUserCouponId}`
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
