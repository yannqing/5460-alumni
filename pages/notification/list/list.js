// pages/notification/list/list.js
const MOCK_NOTIFICATIONS = [
  {
    id: 1,
    title: '校友会年度大会通知',
    content: '将于12月举办年度大会，请各位校友踊跃参加，现场将发布校友计划。',
    time: '2025-11-15 10:00',
    type: 'system',
    isRead: false
  },
  {
    id: 2,
    title: '校友会活动通知',
    content: '本周六校友聚会活动开放报名，地点在滨海酒店，请尽快确认。',
    time: '2025-11-14 15:00',
    type: 'event',
    isRead: false
  },
  {
    id: 3,
    title: '章程修订投票',
    content: '关于校友会章程修订的投票通道已开启，欢迎大家反馈意见。',
    time: '2025-11-13 09:00',
    type: 'important',
    isRead: true
  }
]

Page({
  data: {
    associationId: '',
    notificationList: [],
    displayList: [],
    loading: false,
    searchValue: '',
    filterType: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'unread', label: '未读' },
      { id: 'system', label: '系统' },
      { id: 'event', label: '活动' },
      { id: 'important', label: '重要' }
    ]
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.loadNotificationList()
  },

  loadNotificationList() {
    this.setData({ loading: true })
    setTimeout(() => {
      this.setData(
        {
          notificationList: MOCK_NOTIFICATIONS,
          loading: false
        },
        () => {
          this.applyFilter()
        }
      )
    }, 300)
  },

  applyFilter() {
    const { notificationList, filterType, searchValue } = this.data
    let list = [...notificationList]

    if (filterType === 'unread') {
      list = list.filter(item => !item.isRead)
    } else if (filterType !== 'all') {
      list = list.filter(item => item.type === filterType)
    }

    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item => item.title.includes(keyword) || item.content.includes(keyword)
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
    if (id === this.data.filterType) {return}
    this.setData({ filterType: id }, () => {
      this.applyFilter()
    })
  },

  markAllRead() {
    const notificationList = this.data.notificationList.map(item => ({
      ...item,
      isRead: true
    }))
    this.setData({ notificationList }, () => {
      this.applyFilter()
    })
    wx.showToast({ title: '全部已读', icon: 'success', duration: 800 })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    this.updateReadStatus(id)
    wx.navigateTo({
      url: `/pages/notification/detail/detail?id=${id}`
    })
  },

  updateReadStatus(id) {
    const notificationList = this.data.notificationList.map(item => {
      if (item.id === id) {
        return { ...item, isRead: true }
      }
      return item
    })
    this.setData({ notificationList }, () => {
      this.applyFilter()
    })
  }
})

