// pages/alumni/list/list.js
const { alumniApi } = require('../../../api/index.js')

Page({
  data: {
    keyword: '',
    activeTab: 0,
    tabs: ['全部', '全部', '默认排序', '我的关注'],
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

    const mockData = [
      {
        id: 1,
        name: '张三',
        avatar: '/assets/images/头像.png',
        school: '南京大学',
        location: '江苏省南京市',
        major: '计算机科学',
        graduateYear: 2015,
        company: '腾讯科技',
        position: '高级工程师',
        followerCount: 12580,
        followingCount: 156,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '互联网']
      },
      {
        id: 2,
        name: '李四',
        avatar: '/assets/images/头像.png',
        school: '浙江大学',
        location: '浙江省杭州市',
        major: '软件工程',
        graduateYear: 2016,
        company: '阿里巴巴',
        position: '技术专家',
        followerCount: 15620,
        followingCount: 189,
        isFollowed: true,
        isCertified: true,
        tags: ['985', '211', '电商']
      },
      {
        id: 3,
        name: '王五',
        avatar: '/assets/images/头像.png',
        school: '复旦大学',
        location: '上海市',
        major: '人工智能',
        graduateYear: 2017,
        company: '字节跳动',
        position: '算法工程师',
        followerCount: 14230,
        followingCount: 178,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', 'AI']
      },
      {
        id: 4,
        name: '赵六',
        avatar: '/assets/images/头像.png',
        school: '上海交通大学',
        location: '上海市',
        major: '通信工程',
        graduateYear: 2014,
        company: '华为技术',
        position: '研发总监',
        followerCount: 16500,
        followingCount: 195,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '通信']
      }
    ]

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

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: index })
    this.loadAlumniList(true)
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
