// pages/discover/discover.js
const MOCK_MERCHANTS = [
  {
    id: 1,
    name: '星巴克咖啡',
    avatar: '/assets/images/头像.png',
    category: 'dining',
    distance: 520,
    rating: 4.8,
    location: '科技园店',
    isCertified: true,
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
    avatar: '/assets/images/头像.png',
    category: 'dining',
    distance: 1280,
    rating: 4.9,
    location: '南山店',
    isCertified: true,
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
    avatar: '/assets/images/头像.png',
    category: 'entertainment',
    distance: 760,
    rating: 4.6,
    location: '购物中心店',
    isCertified: false,
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
    avatar: '/assets/images/头像.png',
    category: 'lifestyle',
    distance: 2100,
    rating: 4.7,
    location: '商业街店',
    isCertified: true,
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
    avatar: '/assets/images/头像.png',
    category: 'dining',
    distance: 890,
    rating: 4.5,
    location: '商业街店',
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
    selectedCategory: 'all',
    sortType: 'distance',
    loading: false,
    merchantList: [],
    displayList: [],
    categories: [
      { id: 'all', label: '全部权益' },
      { id: 'dining', label: '餐饮美食' },
      { id: 'entertainment', label: '娱乐休闲' },
      { id: 'lifestyle', label: '生活服务' }
    ],
    sortOptions: [
      { id: 'distance', label: '距离优先' },
      { id: 'popularity', label: '热度优先' },
      { id: 'new', label: '最新上架' }
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
    const { selectedCategory, sortType, searchValue } = this.data

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
    } else if (sortType === 'new') {
      list.sort((a, b) => b.id - a.id)
    }

    this.setData({
      displayList: list
    })
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
      searchValue: ''
    })
    this.applyFilterAndSort()
  }
})
