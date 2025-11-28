// pages/coupon/list/list.js
const { couponApi } = require('../../../api/index.js')

Page({
  data: {
    activeTab: 0,
    tabs: ['全部', '抢购', '我的'],
    couponList: [],
    loading: false
  },

  onLoad(options) {
    if (options.type === 'my') {
      this.setData({ activeTab: 2 })
    }
    this.loadCouponList()
  },

  loadCouponList() {
    const mockData = [
      {
        id: 1,
        title: '星巴克校友专属优惠券',
        merchant: '星巴克咖啡',
        originalPrice: 100,
        discountPrice: 59,
        discount: '8折',
        image: 'https://via.placeholder.com/300x200/ff6b9d/ffffff?text=Starbucks',
        stock: 50,
        totalStock: 100,
        startTime: '2025-11-13 10:00:00',
        endTime: '2025-12-31 23:59:59',
        type: 'rush',
        status: 'available'
      },
      {
        id: 2,
        title: '海底捞200元代金券',
        merchant: '海底捞火锅',
        originalPrice: 200,
        discountPrice: 200,
        discount: '满200减50',
        image: 'https://via.placeholder.com/300x200/ff8fb5/ffffff?text=Haidilao',
        stock: 200,
        totalStock: 200,
        endTime: '2025-12-25 23:59:59',
        type: 'normal',
        status: 'available'
      },
      {
        id: 3,
        title: '肯德基50元优惠券',
        merchant: '肯德基',
        originalPrice: 50,
        discountPrice: 29,
        discount: '7折',
        image: 'https://via.placeholder.com/300x200/ffb6d4/ffffff?text=KFC',
        stock: 80,
        totalStock: 100,
        endTime: '2025-12-20 23:59:59',
        type: 'rush',
        status: 'available'
      },
      {
        id: 4,
        title: 'IMAX 影城观影券',
        merchant: '华影国际影城',
        originalPrice: 80,
        discountPrice: 56,
        discount: '7折',
        image: 'https://via.placeholder.com/300x200/ffcce0/ffffff?text=CIN',
        stock: 120,
        totalStock: 200,
        endTime: '2025-12-31 23:59:59',
        type: 'normal',
        status: 'available'
      }
    ]

    this.setData({ couponList: mockData })
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
