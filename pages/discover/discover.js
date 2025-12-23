// pages/discover/discover.js
const config = require('../../utils/config.js')

const MOCK_MERCHANTS = [
  {
    id: 1,
      name: '星巴克咖啡',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 520,
      rating: 4.8,
      location: '科技园店',
      avgPrice: 45,
      latitude: 31.2304,
      longitude: 121.4737,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 3
      },
    coupons: [
      {
        id: 1,
        title: '星巴克校友专属优惠',
        discount: '8折',
        expireDate: '2025-12-31',
        originalPrice: 100,
        discountPrice: 80
      },
      {
        id: 2,
        title: '买一送一',
        discount: '买一送一',
        expireDate: '2025-12-25',
        originalPrice: 50,
        discountPrice: 25
      },
      {
        id: 3,
        title: '满100减20',
        discount: '满100减20',
        expireDate: '2025-12-20',
        originalPrice: 100,
        discountPrice: 80
      }
    ]
  },
  {
    id: 2,
      name: '海底捞火锅',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 1280,
      rating: 4.9,
      location: '南山店',
      avgPrice: 120,
      latitude: 31.2314,
      longitude: 121.4747,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 5
      },
    coupons: [
      {
        id: 4,
        title: '海底捞校友专享',
        discount: '9折',
        expireDate: '2025-12-15',
        originalPrice: 200,
        discountPrice: 180
      },
      {
        id: 5,
        title: '满200减50',
        discount: '满200减50',
        expireDate: '2025-12-10',
        originalPrice: 200,
        discountPrice: 150
      }
    ]
  },
  {
    id: 3,
      name: '华影国际影城',
      avatar: config.defaultAvatar,
      category: 'entertainment',
      distance: 760,
      rating: 4.6,
      location: '购物中心店',
      avgPrice: 60,
      latitude: 31.2294,
      longitude: 121.4727,
      isCertified: false,
      socialProof: {
        recentAlumni: 2
      },
    coupons: [
      {
        id: 6,
        title: 'IMAX 影城观影券',
        discount: '7折',
        expireDate: '2025-11-01',
        originalPrice: 80,
        discountPrice: 56
      },
      {
        id: 7,
        title: '周末特惠',
        discount: '6折',
        expireDate: '2025-12-31',
        originalPrice: 80,
        discountPrice: 48
      }
    ]
  },
  {
    id: 4,
      name: '橙燃健身房',
      avatar: config.defaultAvatar,
      category: 'lifestyle',
      distance: 2100,
      rating: 4.7,
      location: '商业街店',
      avgPrice: 0,
      latitude: 31.2324,
      longitude: 121.4757,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 1
      },
    coupons: [
      {
        id: 8,
        title: '健身年卡伴侣价',
        discount: '立减¥800',
        expireDate: '2026-01-31',
        originalPrice: 3000,
        discountPrice: 2200
      }
    ]
  },
  {
    id: 5,
      name: '肯德基',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 890,
      rating: 4.5,
      location: '商业街店',
      avgPrice: 35,
      latitude: 31.2284,
      longitude: 121.4717,
      isCertified: false,
    coupons: [
      {
        id: 9,
        title: '肯德基套餐优惠',
        discount: '7折',
        expireDate: '2025-12-20',
        originalPrice: 50,
        discountPrice: 35
      },
      {
        id: 10,
        title: '早餐特惠',
        discount: '6折',
        expireDate: '2025-12-18',
        originalPrice: 30,
        discountPrice: 18
      }
    ]
  }
]

Page({
  data: {
    searchValue: '',
    selectedTab: 'all',
    sortType: 'distance',
    loading: false,
    viewMode: 'list', // list: 列表模式, map: 地图模式
    mapCenter: {
      latitude: 31.2304, // 默认上海坐标
      longitude: 121.4737
    },
    mapScale: 15,
    mapMarkers: [],
    navTabs: [
      { id: 'all', label: '全部分类', icon: '⊞' },
      { id: 'coupon', label: '附近优惠', icon: '🎟️' },
      { id: 'venue', label: '附近场所', icon: '🏌️' },
      { id: 'alumni', label: '附近校友', icon: '🎓' },
      { id: 'activity', label: '附近活动', icon: '🏃' }
    ],
    sortOptions: [
      { id: 'distance', label: '距离最近' },
      { id: 'popularity', label: '好评优先' },
      { id: 'discount', label: '优惠力度' }
    ],
    alumniList: [],
    activityList: []
  },

  onLoad() {
    this.loadDiscoverData()
  },

  loadDiscoverData() {
    this.setData({ loading: true })
    
    // 模拟加载延迟
    setTimeout(() => {
      // 模拟校友列表数据
      const mockAlumniList = [
        {
          id: 1,
          name: '刘汾阳',
          distance: 520,
          association: '江南大学无锡校友会',
          tag: '江南',
          avatar: config.defaultAvatar
        }
      ]
      
      // 模拟活动列表数据
      const mockActivityList = [
        {
          id: 1,
          title: '洛杉矶苏超观影会',
          dateRange: '2025.10.4 - 2026.5.3',
          association: '江南大学无锡校友会',
          participantCount: 24,
          participantAvatars: [
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar
          ],
          location: '北京市朝阳区',
          signedUp: true,
          signedCount: 22
        }
      ]
      
      this.setData({
        alumniList: mockAlumniList,
        activityList: mockActivityList,
        loading: false
      })
      this.updateMapMarkers()
    }, 500)
  },

  handleSearchInput(e) {
    this.setData({
      searchValue: e.detail.value
    })
  },

  handleSearchConfirm() {
    const { searchValue } = this.data
    if (searchValue.trim()) {
      wx.navigateTo({
        url: `/pages/search/search?keyword=${searchValue}`
      })
    }
  },

  handleTabChange(e) {
    const tabId = e.currentTarget.dataset.id
    this.setData({
      selectedTab: tabId
    })
    // TODO: 根据选中的标签加载对应数据
  },


  handleSortChange(e) {
    const sortId = e.currentTarget.dataset.id
    this.setData({
      sortType: sortId
    })
    // TODO: 根据排序类型重新排序列表
  },

  getLocation() {
    wx.showLoading({ title: '定位中...' })
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        wx.hideLoading()
        this.setData({
          mapCenter: {
            latitude: res.latitude,
            longitude: res.longitude
          }
        })
        this.updateMapMarkers()
        wx.showToast({
          title: '定位成功',
          icon: 'success'
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '定位失败，请重试',
          icon: 'none'
        })
      }
    })
  },


  handleFollow(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现关注功能
    wx.showToast({
      title: '关注成功',
      icon: 'success'
    })
  },

  handleSignup(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现报名功能
    wx.showToast({
      title: '报名成功',
      icon: 'success'
    })
  },

  handleLike(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现点赞功能
  },

  handleShare(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现分享功能
  },

  // 切换视图模式
  switchViewMode(e) {
    const { mode } = e.currentTarget.dataset
    this.setData({
      viewMode: mode
    })
    
    if (mode === 'map') {
      this.updateMapMarkers()
      // 获取当前位置
      this.getLocation()
    }
  },


  // 更新地图标记
  updateMapMarkers() {
    // 活动标记
    const activityMarkers = this.data.activityList.map((item, index) => ({
      id: `activity_${item.id}`,
      latitude: item.latitude || 31.2304,
      longitude: item.longitude || 121.4737,
      iconPath: '/assets/images/activity-marker.png', // 活动图标
      width: 50,
      height: 50,
      callout: {
        content: item.title,
        color: '#333',
        fontSize: 14,
        borderRadius: 8,
        bgColor: '#fff',
        padding: 12,
        display: 'BYCLICK'
      }
    }))
    
    // 场地标记
    const venueMarkers = [
      {
        id: 'venue_1',
        latitude: 31.2314,
        longitude: 121.4747,
        iconPath: '/assets/images/venue-marker.png', // 场地图标
        width: 50,
        height: 50,
        callout: {
          content: '场地',
          color: '#333',
          fontSize: 14,
          borderRadius: 8,
          bgColor: '#fff',
          padding: 12,
          display: 'BYCLICK'
        }
      }
    ]
    
    this.setData({
      mapMarkers: [...activityMarkers, ...venueMarkers]
    })
  },

  // 地图标记点击
  onMarkerTap(e) {
    const { markerId } = e.detail
    // TODO: 处理标记点击事件
  },

  // 地图点击
  onMapTap() {
    // TODO: 处理地图点击事件
  },


})
