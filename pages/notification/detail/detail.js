// pages/notification/detail/detail.js
const MOCK_DETAIL = {
  title: '校友会年度大会通知',
  content:
    '将于2025年12月举办年度大会，请各位校友踊跃参加。会议将讨论校友会未来发展计划，并选举新一届理事会成员。会议时间：2025年12月15日 14:00-18:00，会议地点：XX国际会议中心。',
  time: '2025-11-15 10:00',
  publisher: '南京大学上海校友会',
  attachments: [
    { name: '大会日程.pdf', url: 'https://cdn.example.com/agenda.pdf', size: '1.2MB' },
    { name: '会场交通指引.jpg', url: 'https://cdn.example.com/route.jpg', size: '320KB' }
  ],
  actions: [
    { label: '添加日历', type: 'calendar' },
    { label: '分享给同学', type: 'share' }
  ]
}

Page({
  data: {
    notificationId: '',
    notificationInfo: null,
    loading: false
  },

  onLoad(options) {
    const { id } = options
    this.setData({ notificationId: id || '' })
    this.loadNotificationDetail()
  },

  loadNotificationDetail() {
    this.setData({ loading: true })
    setTimeout(() => {
      this.setData({
        notificationInfo: { id: this.data.notificationId, ...MOCK_DETAIL },
        loading: false
      })
    }, 300)
  },

  handleAttachment(e) {
    const { url } = e.currentTarget.dataset
    wx.showToast({ title: '开始预览', icon: 'none' })
    wx.downloadFile({
      url,
      success(res) {
        wx.openDocument({ filePath: res.tempFilePath })
      }
    })
  },

  handleAction(e) {
    const { type } = e.currentTarget.dataset
    if (type === 'calendar') {
      wx.showToast({ title: '已加入日历', icon: 'success' })
      return
    }
    if (type === 'share') {
      wx.showShareMenu()
    }
  },

  onShareAppMessage() {
    const { notificationInfo } = this.data
    return {
      title: notificationInfo?.title || '校友通知',
      path: `/pages/notification/detail/detail?id=${notificationInfo?.id || ''}`
    }
  }
})
