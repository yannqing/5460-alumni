// pages/alumni-association/detail/detail.js
Page({
  data: {
    associationId: '',
    associationInfo: null,
    activeTab: 0,
    tabs: ['基本信息', '成员列表', '校友总会'],
    members: [],
    activities: [],
    notifications: [],
    benefitActivities: [],
    nearbyBenefits: [],
    associationUnion: null
  },

  onLoad(options) {
    this.setData({ associationId: options.id })
    this.loadAssociationDetail()
  },

  loadAssociationDetail() {
    const mockData = {
      id: this.data.associationId,
      name: '南京大学上海校友会',
      schoolName: '南京大学',
      icon: 'https://via.placeholder.com/150/ff6b9d/ffffff?text=NJU-SH',
      cover: 'https://via.placeholder.com/750x400/ff8fb5/ffffff?text=Cover',
      location: '上海市',
      address: '上海市浦东新区世纪大道XXX号',
      memberCount: 1580,
      isJoined: false,
      isCertified: true,
      president: '张三',
      vicePresidents: ['李四', '王五'],
      establishedYear: 2010,
      description: '南京大学上海校友会成立于2010年，是南京大学在上海地区校友自愿组成的非营利性社会组织。'
    }

    const mockMembers = [
      {
        id: 1,
        name: '张三',
        avatar: '/assets/images/头像.png',
        role: '会长',
        company: '腾讯科技',
        showRealName: true
      },
      {
        id: 2,
        name: '李四',
        avatar: '/assets/images/头像.png',
        role: '副会长',
        company: '阿里巴巴',
        showRealName: true
      }
    ]

    const mockNotifications = [
      {
        id: 1,
        title: '校友会年度大会通知',
        content: '将于2025年12月举办年度大会，请各位校友踊跃参加',
        time: '2025-11-15 10:00',
        isRead: false
      },
      {
        id: 2,
        title: '校友会活动通知',
        content: '本周六将举办校友聚会活动',
        time: '2025-11-14 15:00',
        isRead: false
      }
    ]

    const mockBenefitActivities = [
      {
        id: 1,
        title: '校友企业招聘会',
        time: '2025-12-20 09:00',
        location: '上海国际会展中心',
        participantCount: 328
      },
      {
        id: 2,
        title: '校友联谊活动',
        time: '2025-12-15 14:00',
        location: '南京国际会议中心',
        participantCount: 156
      }
    ]

    const mockNearbyBenefits = [
      {
        id: 1,
        title: '星巴克校友专属优惠',
        merchant: '星巴克咖啡',
        discount: '8折',
        distance: '500m',
        isNew: true
      },
      {
        id: 2,
        title: '海底捞校友专享',
        merchant: '海底捞火锅',
        discount: '9折',
        distance: '1.2km',
        isNew: false
      }
    ]

    const mockUnion = {
      name: '南京大学全球校友总会',
      icon: '/assets/images/njdxxyh.jpg',
      description: '联合世界各地南京大学校友，共同推动校友事业发展。',
      website: 'https://union.nju.edu.cn',
      phone: '025-88888888',
      email: 'union@nju.edu.cn',
      address: '南京市鼓楼区广州路'
    }

    this.setData({
      associationInfo: mockData,
      members: mockMembers,
      notifications: mockNotifications,
      benefitActivities: mockBenefitActivities,
      nearbyBenefits: mockNearbyBenefits,
      associationUnion: mockUnion
    })
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
  },

  toggleJoin() {
    const { associationInfo } = this.data
    associationInfo.isJoined = !associationInfo.isJoined
    this.setData({ associationInfo })
    wx.showToast({
      title: associationInfo.isJoined ? '加入成功' : '已退出',
      icon: 'success'
    })
  },

  // 查看通知详情
  viewNotificationDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/notification/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/notification/detail/detail?id=${id}`
      })
    }
  },

  // 查看活动详情
  viewActivityDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  },

  // 查看权益详情
  viewBenefitDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/benefit/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/benefit/detail/detail?id=${id}`
      })
    }
  }
})
