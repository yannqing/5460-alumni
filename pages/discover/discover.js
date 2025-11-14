// pages/discover/discover.js
Page({
  data: {
    categories: [
      { id: 1, name: '商家', icon: '🏪', url: '/pages/merchant/list/list' },
      { id: 2, name: '活动', icon: '🎉', url: '/pages/activity/list/list' },
      { id: 3, name: '圈子', icon: '💬', url: '/pages/circle/list/list' },
      { id: 4, name: '优惠券', icon: '🎫', url: '/pages/coupon/list/list' }
    ],

    merchants: [],
    activities: [],
    circles: []
  },

  onLoad() {
    this.loadDiscoverData()
  },

  loadDiscoverData() {
    const mockMerchants = [
      {
        id: 1,
        name: '星巴克咖啡',
        cover: 'https://via.placeholder.com/300x200/ff6b9d/ffffff?text=Starbucks',
        category: '餐饮美食',
        discount: '全场8折',
        tags: ['校友优惠', '连锁品牌']
      },
      {
        id: 2,
        name: '海底捞火锅',
        cover: 'https://via.placeholder.com/300x200/ff8fb5/ffffff?text=Haidilao',
        category: '餐饮美食',
        discount: '满200减50',
        tags: ['校友优惠', '热门推荐']
      }
    ]

    const mockActivities = [
      {
        id: 1,
        title: '2025年度校友联谊会',
        cover: 'https://via.placeholder.com/400x250/ff6b9d/ffffff?text=Activity1',
        organizer: '南京大学校友总会',
        startTime: '2025-12-15',
        participantCount: 156
      }
    ]

    const mockCircles = [
      {
        id: 1,
        name: '创业交流圈',
        cover: 'https://via.placeholder.com/200/ff6b9d/ffffff?text=Circle1',
        memberCount: 3580,
        postCount: 1256
      },
      {
        id: 2,
        name: '技术分享圈',
        cover: 'https://via.placeholder.com/200/ff8fb5/ffffff?text=Circle2',
        memberCount: 5620,
        postCount: 2890
      }
    ]

    this.setData({
      merchants: mockMerchants,
      activities: mockActivities,
      circles: mockCircles
    })
  },

  navigateTo(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    }
  },

  viewMerchantDetail(e) {
    wx.navigateTo({
      url: `/pages/merchant/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewActivityDetail(e) {
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewCircleDetail(e) {
    wx.navigateTo({
      url: `/pages/circle/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
