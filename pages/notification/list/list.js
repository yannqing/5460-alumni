// pages/notification/list/list.js
Page({
  data: {
    associationId: '',
    notificationList: [],
    loading: false
  },

  onLoad(options) {
    const { associationId } = options
    this.setData({ associationId: associationId || '' })
    this.loadNotificationList()
  },

  loadNotificationList() {
    this.setData({ loading: true })

    const mockData = [
      {
        id: 1,
        title: '校友会年度大会通知',
        content: '将于2025年12月举办年度大会，请各位校友踊跃参加。会议将讨论校友会未来发展计划，并选举新一届理事会成员。',
        time: '2025-11-15 10:00',
        isRead: false
      },
      {
        id: 2,
        title: '校友会活动通知',
        content: '本周六将举办校友聚会活动，地点在XX酒店，欢迎各位校友参加。',
        time: '2025-11-14 15:00',
        isRead: false
      },
      {
        id: 3,
        title: '校友会重要通知',
        content: '关于校友会章程修订的通知，请各位校友查看并反馈意见。',
        time: '2025-11-13 09:00',
        isRead: true
      }
    ]

    setTimeout(() => {
      this.setData({
        notificationList: mockData,
        loading: false
      })
    }, 500)
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/notification/detail/detail?id=${id}`
    })
  }
})

