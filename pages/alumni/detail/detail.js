// pages/alumni/detail/detail.js
Page({
  data: {
    alumniId: '',
    alumniInfo: null
  },

  onLoad(options) {
    this.setData({ alumniId: options.id })
    this.loadAlumniDetail()
  },

  loadAlumniDetail() {
    const mockData = {
      id: this.data.alumniId,
      name: '张三',
      avatar: 'https://via.placeholder.com/150/ff6b9d/ffffff?text=ZS',
      school: '南京大学',
      major: '计算机科学与技术',
      graduateYear: 2015,
      company: '腾讯科技',
      position: '高级工程师',
      location: '上海市',
      isFollowed: false,
      isCertified: true,
      bio: '热爱技术，专注于前端开发领域',
      associations: ['南京大学上海校友会']
    }
    this.setData({ alumniInfo: mockData })
  },

  toggleFollow() {
    const { alumniInfo } = this.data
    alumniInfo.isFollowed = !alumniInfo.isFollowed
    this.setData({ alumniInfo })
    wx.showToast({
      title: alumniInfo.isFollowed ? '关注成功' : '已取消关注',
      icon: 'success'
    })
  }
})
