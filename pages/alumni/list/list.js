// pages/alumni/list/list.js
const { alumniApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

const BASE_ALUMNI = [
  {
    id: 1,
    name: '张三',
    avatar: config.defaultAvatar,
    school: '南京大学',
    city: '南京',
    location: '江苏省南京市',
    major: '计算机科学',
    graduateYear: 2015,
    company: '腾讯科技',
    position: '高级工程师',
    followerCount: 12580,
    followingCount: 156,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', '互联网'],
    identity: '企业高管'
  },
  {
    id: 2,
    name: '李四',
    avatar: config.defaultAvatar,
    school: '浙江大学',
    city: '杭州',
    location: '浙江省杭州市',
    major: '软件工程',
    graduateYear: 2016,
    company: '阿里巴巴',
    position: '技术专家',
    followerCount: 15620,
    followingCount: 189,
    isFollowed: true,
    isCertified: true,
    tags: ['985', '211', '电商'],
    identity: '创业校友'
  },
  {
    id: 3,
    name: '王五',
    avatar: config.defaultAvatar,
    school: '复旦大学',
    city: '上海',
    location: '上海市',
    major: '人工智能',
    graduateYear: 2017,
    company: '字节跳动',
    position: '算法工程师',
    followerCount: 14230,
    followingCount: 178,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', 'AI'],
    identity: '在读校友'
  },
  {
    id: 4,
    name: '赵六',
    avatar: config.defaultAvatar,
    school: '上海交通大学',
    city: '深圳',
    location: '广东省深圳市',
    major: '通信工程',
    graduateYear: 2014,
    company: '华为技术',
    position: '研发总监',
    followerCount: 16500,
    followingCount: 195,
    isFollowed: false,
    isCertified: true,
    tags: ['985', '211', '通信'],
    identity: '企业高管'
  }
]

Page({
  data: {
    // 图标路径
    iconSearch: config.getIconUrl('sslss.png'),
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    keyword: '',
    filters: [
      { label: '身份', options: ['全部校友', '企业高管', '创业校友', '在读校友'], selected: 0 },
      { label: '城市', options: ['全部城市', '南京', '上海', '杭州', '深圳'], selected: 0 },
      { label: '排序', options: ['默认排序', '最新加入', '人气最高'], selected: 0 },
      { label: '关注', options: ['全部', '我的关注'], selected: 0 }
    ],
    showFilterOptions: false,
    activeFilterIndex: -1,
    alumniList: [],
    page: 1,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadAlumniList(true)
  },

  onPullDownRefresh() {
    this.loadAlumniList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAlumniList(false)
    }
  },

  loadAlumniList(reset = false) {
    this.setData({ loading: true })

    const mockData = this.generateMockAlumni()

    setTimeout(() => {
      this.setData({
        alumniList: reset ? mockData : [...this.data.alumniList, ...mockData],
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
    this.loadAlumniList(true)
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
    this.loadAlumniList(true)
  },

  closeFilterOptions() {
    this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
  },

  generateMockAlumni() {
    const [identityFilter, cityFilter, sortFilter, followFilter] = this.data.filters
    let list = BASE_ALUMNI.slice()

    if (identityFilter.selected > 0) {
      list = list.filter(item => item.identity === identityFilter.options[identityFilter.selected])
    }

    if (cityFilter.selected > 0) {
      list = list.filter(item => item.city === cityFilter.options[cityFilter.selected])
    }

    if (followFilter.selected === 1) {
      list = list.filter(item => item.isFollowed)
    }

    if (sortFilter.selected === 1) {
      list = list.sort((a, b) => b.graduateYear - a.graduateYear)
    } else if (sortFilter.selected === 2) {
      list = list.sort((a, b) => b.followerCount - a.followerCount)
    }

    return list
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { alumniList } = this.data
    const index = alumniList.findIndex(item => item.id === id)
    if (index !== -1) {
      alumniList[index].isFollowed = !followed
      this.setData({ alumniList })
      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  }
})
