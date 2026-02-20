// pages/enterprise/list/list.js
const config = require('../../../utils/config.js')

Page({
  data: {
    associationId: '',
    searchValue: '',
    filterCategory: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'tech', label: '互联网科技' },
      { id: 'finance', label: '金融' },
      { id: 'education', label: '教育' },
      { id: 'retail', label: '零售' },
      { id: 'other', label: '其他' }
    ],
    enterpriseList: [],
    displayList: []
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.loadEnterpriseList()
  },

  onPullDownRefresh() {
    this.loadEnterpriseList(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadEnterpriseList(callback) {
    // TODO: 对接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/enterprise/list`,
    //   data: { associationId: this.data.associationId },
    //   success: (res) => {
    //     this.setData({ enterpriseList: res.data.list })
    //     this.applyFilter()
    //   }
    // })

    // 模拟数据
    const mockData = [
      {
        id: 1,
        name: '腾讯科技',
        logo: config.defaultAvatar,
        industry: '互联网科技',
        founder: '张三',
        description: '腾讯科技是一家专注于互联网科技的公司，致力于为用户提供优质的产品和服务。',
        location: '深圳市南山区',
        scale: '1000-5000人',
        category: 'tech'
      },
      {
        id: 2,
        name: '阿里巴巴集团',
        logo: config.defaultAvatar,
        industry: '电子商务',
        founder: '李四',
        description: '阿里巴巴集团是全球领先的电子商务平台，为全球商家和消费者提供一站式服务。',
        location: '杭州市余杭区',
        scale: '5000人以上',
        category: 'retail'
      },
      {
        id: 3,
        name: '字节跳动',
        logo: config.defaultAvatar,
        industry: '互联网科技',
        founder: '王五',
        description: '字节跳动是一家全球化的移动互联网平台公司，致力于用技术丰富人们的生活。',
        location: '北京市海淀区',
        scale: '5000人以上',
        category: 'tech'
      }
    ]

    this.setData({ enterpriseList: mockData }, () => {
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
    const { enterpriseList, searchValue, filterCategory } = this.data
    let list = [...enterpriseList]

    // 分类筛选
    if (filterCategory !== 'all') {
      list = list.filter(item => item.category === filterCategory)
    }

    // 搜索筛选
    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.name.includes(keyword) ||
          item.industry.includes(keyword) ||
          item.founder.includes(keyword) ||
          item.description.includes(keyword)
      )
    }

    this.setData({ displayList: list })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/enterprise/detail/detail?id=${id}`
    })
  }
})

