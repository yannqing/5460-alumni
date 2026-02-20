// pages/circle/list/list.js
Page({
  data: {
    circleList: [
      {
        id: 1,
        name: '创业交流圈',
        cover: 'https://via.placeholder.com/200/ff6b9d/ffffff?text=Circle1',
        memberCount: 3580,
        postCount: 1256,
        isJoined: false
      },
      {
        id: 2,
        name: '技术分享圈',
        cover: 'https://via.placeholder.com/200/ff8fb5/ffffff?text=Circle2',
        memberCount: 5620,
        postCount: 2890,
        isJoined: true
      }
    ]
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/circle/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
