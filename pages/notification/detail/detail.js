// pages/notification/detail/detail.js
Page({
  data: {
    notificationId: '',
    notificationInfo: null,
    loading: false
  },

  onLoad(options) {
    const { id } = options
    this.setData({ notificationId: id })
    this.loadNotificationDetail()
  },

  loadNotificationDetail() {
    this.setData({ loading: true })

    const mockData = {
      id: this.data.notificationId,
      title: '校友会年度大会通知',
      content: '将于2025年12月举办年度大会，请各位校友踊跃参加。会议将讨论校友会未来发展计划，并选举新一届理事会成员。会议时间：2025年12月15日 14:00-18:00，会议地点：XX国际会议中心。',
      time: '2025-11-15 10:00',
      publisher: '南京大学上海校友会',
      isRead: false
    }

    setTimeout(() => {
      this.setData({
        notificationInfo: mockData,
        loading: false
      })
    }, 500)
  }
})

