// pages/my-association/my-association.js
Page({
  data: {
    associationList: [
      {
        id: 1,
        name: '南京大学上海校友会',
        schoolName: '南京大学',
        icon: '/assets/images/头像.png',
        location: '上海市',
        memberCount: 1580,
        isVerified: true,
        tags: ['官方认证', '活跃']
      },
      {
        id: 2,
        name: '浙江大学杭州校友会',
        schoolName: '浙江大学',
        icon: '/assets/images/头像.png',
        location: '杭州市',
        memberCount: 2350,
        isVerified: true,
        tags: ['官方认证']
      },
      {
        id: 3,
        name: '清华大学北京校友会',
        schoolName: '清华大学',
        icon: '/assets/images/头像.png',
        location: '北京市',
        memberCount: 5200,
        isVerified: true,
        tags: ['官方认证', '活跃', '大型']
      },
      {
        id: 4,
        name: '北京大学校友会',
        schoolName: '北京大学',
        icon: '/assets/images/头像.png',
        location: '北京市',
        memberCount: 4800,
        isVerified: true,
        tags: ['官方认证', '活跃']
      },
      {
        id: 5,
        name: '复旦大学上海校友会',
        schoolName: '复旦大学',
        icon: '/assets/images/头像.png',
        location: '上海市',
        memberCount: 3200,
        isVerified: true,
        tags: ['官方认证']
      },
      {
        id: 6,
        name: '上海交通大学校友会',
        schoolName: '上海交通大学',
        icon: '/assets/images/头像.png',
        location: '上海市',
        memberCount: 2800,
        isVerified: true,
        tags: ['官方认证', '活跃']
      }
    ]
  },

  onLoad() {
    // 可以在这里加载真实数据
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
