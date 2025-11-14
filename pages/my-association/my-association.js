// pages/my-association/my-association.js
Page({
  data: {
    associationList: [
      {
        id: 1,
        name: '南京大学上海校友会',
        schoolName: '南京大学',
        icon: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=NJU-SH',
        location: '上海市',
        memberCount: 1580
      }
    ]
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
