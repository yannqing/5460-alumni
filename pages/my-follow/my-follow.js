// pages/my-follow/my-follow.js
Page({
  data: {
    type: 'school', // school, alumni
    followList: []
  },

  onLoad(options) {
    this.setData({ type: options.type || 'school' })
    this.loadFollowList()
  },

  loadFollowList() {
    const mockSchools = [
      {
        id: 1,
        name: '南京大学',
        icon: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=NJU',
        location: '江苏省南京市',
        alumniCount: 12580
      }
    ]

    const mockAlumni = [
      {
        id: 1,
        name: '张三',
        avatar: '/assets/images/头像.png',
        school: '南京大学',
        company: '腾讯科技'
      }
    ]

    this.setData({
      followList: this.data.type === 'school' ? mockSchools : mockAlumni
    })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    const url = this.data.type === 'school'
      ? `/pages/school/detail/detail?id=${id}`
      : `/pages/alumni/detail/detail?id=${id}`
    wx.navigateTo({ url })
  }
})
