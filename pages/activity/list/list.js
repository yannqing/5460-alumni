// pages/activity/list/list.js
Page({
  data: {
    activityList: [
      {
        id: 1,
        title: '2025年度校友联谊会',
        cover: 'https://via.placeholder.com/400x250/ff6b9d/ffffff?text=Activity1',
        organizer: '南京大学校友总会',
        location: '南京国际会议中心',
        startTime: '2025-12-15 14:00',
        participantCount: 156,
        status: 'upcoming'
      }
    ]
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
