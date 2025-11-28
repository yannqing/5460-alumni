// pages/activity/list/list.js
const MOCK_ACTIVITIES = [
      {
        id: 1,
        title: '2025年度校友联谊会',
    cover: 'https://cdn.example.com/activity/cover-1.png',
        organizer: '南京大学校友总会',
        location: '南京国际会议中心',
        startTime: '2025-12-15 14:00',
    endTime: '2025-12-15 18:00',
        participantCount: 156,
    tags: ['大型峰会', '年度重磅'],
    status: 'upcoming',
    distance: 2.6
  },
  {
    id: 2,
    title: '校友创业故事会',
    cover: 'https://cdn.example.com/activity/cover-2.png',
    organizer: '南京大学创新创业中心',
    location: '深圳前海创新馆',
    startTime: '2025-11-20 19:00',
    endTime: '2025-11-20 21:00',
    participantCount: 68,
    tags: ['创业', '线下分享'],
    status: 'ongoing',
    distance: 0.8
  },
  {
    id: 3,
    title: '校友家庭日 · 海边露营',
    cover: 'https://cdn.example.com/activity/cover-3.png',
    organizer: '深圳校友分会',
    location: '大梅沙露营公园',
    startTime: '2025-10-05 09:00',
    endTime: '2025-10-05 19:00',
    participantCount: 112,
    tags: ['亲子', '户外'],
    status: 'finished',
    distance: 18.4
  }
]

Page({
  data: {
    searchValue: '',
    filterStatus: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'upcoming', label: '即将开始' },
      { id: 'ongoing', label: '进行中' },
      { id: 'finished', label: '已结束' }
    ],
    activityList: [],
    displayList: [],
    stats: {
      total: 0,
      upcoming: 0,
      nearby: 0
    }
  },

  onLoad() {
    this.loadActivityList()
  },

  loadActivityList() {
    const activityList = MOCK_ACTIVITIES
    this.setData(
      {
        activityList,
        stats: {
          total: activityList.length,
          upcoming: activityList.filter(item => item.status === 'upcoming').length,
          nearby: activityList.filter(item => item.distance <= 5).length
        }
      },
      () => {
        this.applyFilter()
      }
    )
  },

  applyFilter() {
    const { activityList, filterStatus, searchValue } = this.data
    let list = [...activityList]

    if (filterStatus !== 'all') {
      list = list.filter(item => item.status === filterStatus)
    }

    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.title.includes(keyword) ||
          item.organizer.includes(keyword) ||
          item.location.includes(keyword)
      )
    }

    this.setData({ displayList: list })
  },

  handleSearchInput(e) {
    this.setData({ searchValue: e.detail.value }, () => {
      this.applyFilter()
    })
  },

  handleFilterChange(e) {
    const { id } = e.currentTarget.dataset
    if (id === this.data.filterStatus) return
    this.setData({ filterStatus: id }, () => {
      this.applyFilter()
    })
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
