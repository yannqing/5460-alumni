// pages/benefit/list/list.js
const MOCK_BENEFITS = [
  {
    id: 1,
    title: '星巴克校友专属优惠',
    merchant: '星巴克咖啡（科技园店）',
    category: 'dining',
    tags: ['8折', '全天通用', 'VIP 通道'],
    discount: '8折',
    distance: 520,
    popularity: 98,
    isNew: true,
    remindSet: false,
    favorite: true,
    expireDate: '2025-12-31',
    cover: 'https://cdn.example.com/benefits/starbucks.png',
    description: '凭校友身份可享全菜单 8 折，支持微信/小程序核销。'
  },
  {
    id: 2,
    title: '海底捞校友专享',
    merchant: '海底捞火锅（南山店）',
    category: 'dining',
    tags: ['9折', '节假日可用', '含饮料'],
    discount: '9折',
    distance: 1280,
    popularity: 86,
    isNew: false,
    remindSet: true,
    favorite: false,
    expireDate: '2025-12-15',
    cover: 'https://cdn.example.com/benefits/haidilao.png',
    description: '校友凭证享锅底 9 折，预约即送小吃大礼包。'
  },
  {
    id: 3,
    title: 'IMAX 影城观影券',
    merchant: '华影国际影城',
    category: 'entertainment',
    tags: ['7折', '周末可用'],
    discount: '7折',
    distance: 760,
    popularity: 91,
    isNew: true,
    remindSet: false,
    favorite: false,
    expireDate: '2025-11-01',
    cover: 'https://cdn.example.com/benefits/cinema.png',
    description: '凭校友卡全场 7 折，含 3D 眼镜租赁。'
  },
  {
    id: 4,
    title: '健身年卡伴侣价',
    merchant: '橙燃健身房',
    category: 'lifestyle',
    tags: ['限量', '私教体验'],
    discount: '立减¥800',
    distance: 2100,
    popularity: 75,
    isNew: false,
    remindSet: true,
    favorite: true,
    expireDate: '2026-01-31',
    cover: 'https://cdn.example.com/benefits/gym.png',
    description: '双人办理立减 800 元，赠 2 次私教课。'
  }
]

Page({
  data: {
    associationId: '',
    benefitList: [],
    displayList: [],
    loading: true,
    searchValue: '',
    selectedCategory: 'all',
    sortType: 'distance',
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
    location: {
      name: '定位中',
      distance: '--'
    },
    stats: {
      total: 0,
      newArrivals: 0,
      remindCount: 0
    }
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.initPage()
  },

  initPage() {
    this.getLocation()
    this.loadBenefitList()
  },

  getLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: () => {
        this.setData({
          location: {
            name: '深圳 · 后海',
            distance: '1.2km'
          }
        })
      },
      fail: () => {
        this.setData({
          location: {
            name: '定位失败，点击重试',
            distance: '--'
          }
        })
      }
    })
  },

  loadBenefitList() {
    this.setData({ loading: true })
    setTimeout(() => {
      const benefitList = MOCK_BENEFITS
      this.setData(
        {
          benefitList,
          stats: {
            total: benefitList.length,
            newArrivals: benefitList.filter(item => item.isNew).length,
            remindCount: benefitList.filter(item => item.remindSet).length
          },
          loading: false
        },
        () => {
          this.applyFilterAndSort()
        }
      )
    }, 400)
  },

  applyFilterAndSort() {
    const { benefitList, selectedCategory, searchValue, sortType } = this.data
    let list = [...benefitList]

    if (selectedCategory !== 'all') {
      list = list.filter(item => item.category === selectedCategory)
    }

    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.title.includes(keyword) ||
          item.merchant.includes(keyword) ||
          item.tags.some(tag => tag.includes(keyword))
      )
    }

    list.sort((a, b) => {
      if (sortType === 'distance') {
        return a.distance - b.distance
      }
      if (sortType === 'popularity') {
        return b.popularity - a.popularity
      }
      if (sortType === 'new') {
        return Number(b.isNew) - Number(a.isNew)
      }
      return 0
    })

    this.setData({ displayList: list })
  },

  handleSearchInput(e) {
    const value = e.detail.value
    this.setData({ searchValue: value }, () => {
      this.applyFilterAndSort()
    })
  },

  handleSearchConfirm() {
    this.applyFilterAndSort()
  },

  handleCategoryChange(e) {
    const { id } = e.currentTarget.dataset
    if (id === this.data.selectedCategory) return
    this.setData({ selectedCategory: id }, () => {
      this.applyFilterAndSort()
    })
  },

  handleSortChange(e) {
    const { id } = e.currentTarget.dataset
    if (id === this.data.sortType) return
    this.setData({ sortType: id }, () => {
      this.applyFilterAndSort()
    })
  },

  toggleRemind(e) {
    const { id } = e.currentTarget.dataset
    const benefitList = this.data.benefitList.map(item => {
      if (item.id === id) {
        return { ...item, remindSet: !item.remindSet }
      }
      return item
    })
    this.setData({ benefitList }, () => {
      this.applyFilterAndSort()
    })
  },

  toggleFavorite(e) {
    const { id } = e.currentTarget.dataset
    const benefitList = this.data.benefitList.map(item => {
      if (item.id === id) {
        return { ...item, favorite: !item.favorite }
      }
      return item
    })
    this.setData({ benefitList }, () => {
      this.applyFilterAndSort()
    })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/benefit/detail/detail?id=${id}`
    })
  },

  goToMerchant(e) {
    const { merchant } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/shop/detail/detail?name=${encodeURIComponent(merchant)}`
    })
  },

  refreshPage() {
    this.loadBenefitList()
  }
})

