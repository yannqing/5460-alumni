// pages/circle/detail/detail.js
Page({
  data: {
    circleId: '',
    circleInfo: {
      id: 1,
      name: '创业交流圈',
      cover: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Circle',
      memberCount: 3580,
      postCount: 1256,
      description: '校友创业经验分享与交流',
      isJoined: false
    },
    posts: [
      {
        id: 1,
        author: '张三',
        avatar: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=ZS',
        content: '分享一下最近的创业心得...',
        images: [],
        likeCount: 25,
        commentCount: 8,
        createTime: '2小时前'
      }
    ]
  },

  onLoad(options) {
    this.setData({ circleId: options.id })
  },

  toggleJoin() {
    const { circleInfo } = this.data
    circleInfo.isJoined = !circleInfo.isJoined
    this.setData({ circleInfo })
    wx.showToast({
      title: circleInfo.isJoined ? '加入成功' : '已退出',
      icon: 'success'
    })
  }
})
