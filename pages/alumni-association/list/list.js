// pages/alumni-association/list/list.js
const { associationApi } = require('../../../api/index.js')

const BASE_ASSOCIATIONS = [
  {
    id: 1,
    name: '南京大学上海校友会',
    schoolName: '南京大学',
    icon: '/assets/logo/njdxxyh.jpg',
    location: '上海市',
    city: '上海',
    memberCount: 12580,
    associationCount: 156,
    followCount: 156,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', '双一流'],
    type: '地方校友会',
    establishedYear: 2010
  },
  {
    id: 2,
    name: '浙江大学杭州校友会',
    schoolName: '浙江大学',
    icon: '/assets/logo/njdxxyh.jpg',
    location: '杭州市',
    city: '杭州',
    memberCount: 15620,
    associationCount: 189,
    followCount: 189,
    isFollowed: true,
    isCertified: true,
    tags: ['985', '211', '双一流'],
    type: '地方校友会',
    establishedYear: 2012
  },
  {
    id: 3,
    name: '复旦大学行业校友会',
    schoolName: '复旦大学',
    icon: '/assets/logo/njdxxyh.jpg',
    location: '上海市',
    city: '上海',
    memberCount: 14230,
    associationCount: 178,
    followCount: 178,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', '金融'],
    type: '行业校友会',
    establishedYear: 2015
  },
  {
    id: 4,
    name: '上海交通大学深圳校友会',
    schoolName: '上海交通大学',
    icon: '/assets/logo/njdxxyh.jpg',
    location: '深圳市',
    city: '深圳',
    memberCount: 16500,
    associationCount: 195,
    followCount: 195,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', '科技'],
    type: '海外校友会',
    establishedYear: 2018
  }
]

Page({
  data: {
    keyword: '',
    filters: [
      { label: '类型', options: ['全部校友会', '地方校友会', '行业校友会', '海外校友会'], selected: 0 },
      { label: '城市', options: ['全部城市', '南京', '上海', '杭州', '深圳'], selected: 0 },
      { label: '排序', options: ['默认排序', '最新成立', '成员最多'], selected: 0 },
      { label: '关注', options: ['全部', '我的关注'], selected: 0 }
    ],
    showFilterOptions: false,
    activeFilterIndex: -1,
    associationList: [],
    page: 1,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    this.loadAssociationList(true)
  },

  onPullDownRefresh() {
    this.loadAssociationList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAssociationList(false)
    }
  },

  loadAssociationList(reset = false) {
    this.setData({ loading: true })

    const mockData = this.generateMockAssociations()

    setTimeout(() => {
      this.setData({
        associationList: reset ? mockData : [...this.data.associationList, ...mockData],
        loading: false,
        page: reset ? 1 : this.data.page + 1
      })
      if (reset) {
        wx.stopPullDownRefresh()
      }
    }, 500)
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.loadAssociationList(true)
  },

  openFilterOptions(e) {
    const { index } = e.currentTarget.dataset
    if (this.data.activeFilterIndex === index && this.data.showFilterOptions) {
      this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
      return
    }
    this.setData({ activeFilterIndex: index, showFilterOptions: true })
  },

  selectFilterOption(e) {
    const { optionIndex } = e.currentTarget.dataset
    const { activeFilterIndex, filters } = this.data
    if (activeFilterIndex === -1) return
    filters[activeFilterIndex].selected = optionIndex
    this.setData({
      filters,
      showFilterOptions: false,
      activeFilterIndex: -1
    })
    this.loadAssociationList(true)
  },

  closeFilterOptions() {
    this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
  },

  generateMockAssociations() {
    const [typeFilter, cityFilter, sortFilter, followFilter] = this.data.filters
    let list = BASE_ASSOCIATIONS.slice()

    if (typeFilter.selected > 0) {
      list = list.filter(item => item.type === typeFilter.options[typeFilter.selected])
    }

    if (cityFilter.selected > 0) {
      list = list.filter(item => item.city === cityFilter.options[cityFilter.selected])
    }

    if (followFilter.selected === 1) {
      list = list.filter(item => item.isFollowed)
    }

    if (sortFilter.selected === 1) {
      list = list.sort((a, b) => b.establishedYear - a.establishedYear)
    } else if (sortFilter.selected === 2) {
      list = list.sort((a, b) => b.memberCount - a.memberCount)
    }

    return list
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { associationList } = this.data
    const index = associationList.findIndex(item => item.id === id)
    if (index !== -1) {
      associationList[index].isFollowed = !followed
      this.setData({ associationList })
      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  }
})
