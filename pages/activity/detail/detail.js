// pages/activity/detail/detail.js
Page({
  data: {
    activityId: '',
    activityInfo: {
      id: 1,
      title: '2025年度校友联谊会',
      cover: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Activity',
      organizer: '南京大学校友总会',
      location: '南京国际会议中心',
      address: '南京市建邺区江东中路XXX号',
      startTime: '2025-12-15 14:00',
      endTime: '2025-12-15 18:00',
      participantCount: 156,
      maxParticipant: 200,
      description: '欢迎各地校友参加年度联谊会',
      isJoined: false
    }
  },

  onLoad(options) {
    this.setData({ activityId: options.id })
  },

  joinActivity() {
    const { activityInfo } = this.data
    activityInfo.isJoined = true
    activityInfo.participantCount++
    this.setData({ activityInfo })
    wx.showToast({
      title: '报名成功',
      icon: 'success'
    })
  }
})
