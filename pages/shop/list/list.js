// pages/shop/list/list.js
const config = require('../../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    associationId: '',
    searchValue: '',
    filterCategory: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'dining', label: '餐饮' },
      { id: 'retail', label: '零售' },
      { id: 'service', label: '服务' },
      { id: 'culture', label: '文化' },
      { id: 'other', label: '其他' }
    ],
    shopList: [],
    displayList: []
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.loadShopList()
  },

  onPullDownRefresh() {
    this.loadShopList(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadShopList(callback) {
    // TODO: 对接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/shop/list`,
    //   data: { associationId: this.data.associationId },
    //   success: (res) => {
    //     this.setData({ shopList: res.data.list })
    //     this.applyFilter()
    //   }
    // })

    // 模拟数据
    const mockData = [
      {
        id: 1,
        name: '校友咖啡厅',
        cover: config.defaultAvatar,
        owner: '王五',
        category: '餐饮',
        location: '上海市浦东新区世纪大道',
        distance: '800m',
        rating: 4.8,
        description: '温馨的校友聚会场所，提供优质咖啡和简餐',
        categoryId: 'dining'
      },
      {
        id: 2,
        name: '校友书店',
        cover: config.defaultAvatar,
        owner: '赵六',
        category: '文化',
        location: '上海市黄浦区南京路',
        distance: '1.5km',
        rating: 4.6,
        description: '提供优质图书和文化产品，定期举办读书会',
        categoryId: 'culture'
      },
      {
        id: 3,
        name: '校友餐厅',
        cover: config.defaultAvatar,
        owner: '孙七',
        category: '餐饮',
        location: '上海市徐汇区淮海路',
        distance: '2.0km',
        rating: 4.9,
        description: '正宗本帮菜，校友专享优惠',
        categoryId: 'dining'
      }
    ]

    this.setData({ shopList: mockData }, () => {
      this.applyFilter()
      callback && callback()
    })
  },

  handleSearchInput(e) {
    this.setData({ searchValue: e.detail.value }, () => {
      this.applyFilter()
    })
  },

  handleFilterChange(e) {
    const { id } = e.currentTarget.dataset
    this.setData({ filterCategory: id }, () => {
      this.applyFilter()
    })
  },

  applyFilter() {
    const { shopList, searchValue, filterCategory } = this.data
    let list = [...shopList]

    // 分类筛选
    if (filterCategory !== 'all') {
      list = list.filter(item => item.categoryId === filterCategory)
    }

    // 搜索筛选
    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.name.includes(keyword) ||
          item.category.includes(keyword) ||
          item.owner.includes(keyword) ||
          item.location.includes(keyword) ||
          item.description.includes(keyword)
      )
    }

    this.setData({ displayList: list })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${id}`
    })
  }
})

