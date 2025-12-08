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
    // 图标路径
    iconSearch: config.getIconUrl('search.png'),
    iconScan: config.getIconUrl('sys.png'),
    searchValue: '',
    selectedCategory: 'all',
    sortType: 'distance',
    loading: false,
    merchantList: [],
    displayList: [],
    viewMode: 'list', // list: 列表模式, map: 地图模式
    showAlumniOnly: false, // 是否只显示校友商铺
    mapCenter: {
      latitude: 31.2304, // 默认上海坐标
      longitude: 121.4737
    },
    mapScale: 15,
    mapMarkers: [],
    showDrawer: false,
    selectedMerchant: null,
    isAlumni: false, // 是否已认证校友
    categories: [
      { id: 'all', label: '全部分类' },
      { id: 'dining', label: '餐饮美食' },
      { id: 'entertainment', label: '娱乐休闲' },
      { id: 'lifestyle', label: '生活服务' }
    ],
    sortOptions: [
      { id: 'distance', label: '距离最近' },
      { id: 'popularity', label: '好评优先' },
      { id: 'discount', label: '优惠力度' }
    ],
    stats: {
      merchantCount: 0,
      couponCount: 0,
      activityCount: 0
    }
  },

  onLoad() {
    this.loadDiscoverData()
  },

  loadDiscoverData() {
    this.setData({ loading: true })
    
    // 模拟加载延迟
    setTimeout(() => {
      let totalCoupons = 0
      MOCK_MERCHANTS.forEach(merchant => {
        totalCoupons += merchant.coupons.length
      })
      
      this.setData({
        merchantList: MOCK_MERCHANTS,
        displayList: MOCK_MERCHANTS,
        loading: false,
        stats: {
          merchantCount: MOCK_MERCHANTS.length,
          couponCount: totalCoupons,
          activityCount: 5
        }
      })
      this.updateMapMarkers()
      this.applyFilterAndSort()
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
    } else {
      this.applyFilterAndSort()
    }
  },

  handleCategoryChange(e) {
    const categoryId = e.currentTarget.dataset.id
    this.setData({
      selectedCategory: categoryId
    })
    this.applyFilterAndSort()
  },

  handleSortChange(e) {
    const sortId = e.currentTarget.dataset.id
    this.setData({
      sortType: sortId
    })
    this.applyFilterAndSort()
  },

  applyFilterAndSort() {
    let list = [...this.data.merchantList]
    const { selectedCategory, sortType, searchValue, showAlumniOnly } = this.data

    // 校友商铺筛选
    if (showAlumniOnly) {
      list = list.filter(item => item.isCertified)
    }

    // 分类筛选
    if (selectedCategory !== 'all') {
      list = list.filter(item => item.category === selectedCategory)
    }

    // 搜索筛选
    if (searchValue.trim()) {
      const keyword = searchValue.toLowerCase()
      list = list.filter(item => 
        item.name.toLowerCase().includes(keyword) ||
        item.location.toLowerCase().includes(keyword) ||
        item.coupons.some(coupon => coupon.title.toLowerCase().includes(keyword))
      )
    }

    // 排序
    if (sortType === 'distance') {
      list.sort((a, b) => a.distance - b.distance)
    } else if (sortType === 'popularity') {
      list.sort((a, b) => b.rating - a.rating)
    } else if (sortType === 'discount') {
      // 优惠力度：按优惠券折扣力度排序
      list.sort((a, b) => {
        const aMaxDiscount = Math.max(...a.coupons.map(c => c.discountPrice || 0))
        const bMaxDiscount = Math.max(...b.coupons.map(c => c.discountPrice || 0))
        return bMaxDiscount - aMaxDiscount
      })
    }

    this.setData({
      displayList: list
    })
    
    // 更新地图标记
    if (this.data.viewMode === 'map') {
      this.updateMapMarkers()
    }
  },

  getLocation() {
    wx.showLoading({ title: '定位中...' })
    wx.getLocation({
      type: 'gcj02',
      success: () => {
        wx.hideLoading()
        wx.showToast({
          title: '定位成功',
          icon: 'success'
        })
        this.applyFilterAndSort()
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

  viewMerchantDetail(e) {
    const merchantId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${merchantId}`
    })
  },

  viewCouponDetail(e) {
    const couponId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?id=${couponId}`
    })
  },

  viewMerchantCoupons(e) {
    const merchantId = e.currentTarget.dataset.id
    // 跳转到商铺详情页，显示优惠券列表
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${merchantId}`
    })
  },

  refreshPage() {
    this.setData({
      selectedCategory: 'all',
      sortType: 'distance',
      searchValue: '',
      showAlumniOnly: false
    })
    this.applyFilterAndSort()
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

  // 切换校友商铺筛选
  toggleAlumniOnly(e) {
    const value = e.detail.value
    this.setData({
      showAlumniOnly: value
    })
    this.applyFilterAndSort()
  },

  // 更新地图标记
  updateMapMarkers() {
    const { displayList } = this.data
    const markers = displayList.map((item, index) => {
      // 校友商铺使用品牌色（深红），普通商铺使用灰色
      const markerColor = item.isCertified ? '#ff6b9d' : '#999'
      const markerBg = item.isCertified ? '#ff6b9d' : '#999'
      
      return {
        id: item.id,
        latitude: item.latitude || 31.2304,
        longitude: item.longitude || 121.4737,
        iconPath: config.defaultAvatar, // 可以使用自定义图标
        width: 50,
        height: 50,
        callout: {
          content: `${item.name}\n⭐ ${item.rating} | 距离${item.distance}m`,
          color: '#333',
          fontSize: 14,
          borderRadius: 8,
          bgColor: '#fff',
          padding: 12,
          display: 'BYCLICK',
          borderColor: markerColor,
          borderWidth: 2
        },
        label: {
          content: item.name,
          color: '#fff',
          fontSize: 12,
          bgColor: markerBg,
          borderRadius: 8,
          padding: 4,
          anchorX: 0,
          anchorY: 0
        }
      }
    })
    
    this.setData({
      mapMarkers: markers
    })
  },

  // 地图标记点击
  onMarkerTap(e) {
    const { markerId } = e.detail
    const merchant = this.data.displayList.find(item => item.id === markerId)
    if (merchant) {
      this.setData({
        selectedMerchant: merchant,
        showDrawer: true
      })
    }
  },

  // 地图点击
  onMapTap() {
    this.setData({
      showDrawer: false,
      selectedMerchant: null
    })
  },

  // 切换抽屉
  toggleDrawer() {
    this.setData({
      showDrawer: !this.data.showDrawer
    })
  },

  // 扫一扫
  scanCode() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码结果:', res)
        // TODO: 处理扫码结果，可能是优惠券、商家等
        wx.showToast({
          title: '扫码成功',
          icon: 'success'
        })
      },
      fail: (err) => {
        console.error('扫码失败:', err)
        wx.showToast({
          title: '扫码失败',
          icon: 'none'
        })
      }
    })
  },

  // 处理优惠券点击
  handleCouponClick(e) {
    const { coupon, merchant } = e.currentTarget.dataset
    if (!this.data.isAlumni) {
      wx.showModal({
        title: '提示',
        content: '认证后领取优惠券，享受更多权益',
        showCancel: true,
        cancelText: '取消',
        confirmText: '去认证',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/certification/certification'
            })
          }
        }
      })
      return
    }

    // 已认证校友，跳转到优惠券详情
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?id=${coupon.id}`
    })
  }
})
