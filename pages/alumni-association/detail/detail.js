// pages/alumni-association/detail/detail.js
Page({
  data: {
    associationId: '',
    associationInfo: null,
    activeTab: 0,
    tabs: ['基本信息', '成员列表'],
    members: [],
    activities: [],
    notifications: [],
    benefitActivities: [],
    nearbyBenefits: [],
    alumniEnterprises: [],
    alumniShops: []
  },

  onLoad(options) {
    this.setData({ associationId: options.id })
    this.loadAssociationDetail()
  },

  loadAssociationDetail() {
    const mockData = {
      id: this.data.associationId,
      name: '南京大学上海校友会',
      schoolId: 'school-001',
      schoolName: '南京大学',
      icon: '/assets/logo/njdxxyh.jpg',
      cover: '/assets/images/南京大学背景图.jpg',
      location: '上海市',
      address: '上海市浦东新区世纪大道XXX号',
      memberCount: 1580,
      isJoined: false,
      isCertified: true,
      president: '张三',
      vicePresidents: ['李四', '王五'],
      establishedYear: 2010,
      description: '南京大学上海校友会成立于2010年，是南京大学在上海地区校友自愿组成的非营利性组织。',
      certifications: [
        { id: 'union-001', type: 'union', label: '南京大学校友总会' },
        { id: 'promotion-001', type: 'promotion', label: '上海市校促会' }
      ]
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

    const mockAlumniEnterprises = [
      {
        id: 1,
        name: '腾讯科技',
        logo: '/assets/images/头像.png',
        industry: '互联网科技',
        founder: '张三',
        description: '腾讯科技是一家专注于互联网科技的公司',
        location: '深圳市',
        scale: '1000-5000人'
      },
      {
        id: 2,
        name: '阿里巴巴集团',
        logo: '/assets/images/头像.png',
        industry: '电子商务',
        founder: '李四',
        description: '阿里巴巴集团是全球领先的电子商务平台',
        location: '杭州市',
        scale: '5000人以上'
      }
    ]

    const mockAlumniShops = [
      {
        id: 1,
        name: '校友咖啡厅',
        cover: '/assets/images/头像.png',
        owner: '王五',
        category: '餐饮',
        location: '上海市浦东新区',
        distance: '800m',
        rating: 4.8,
        description: '温馨的校友聚会场所'
      },
      {
        id: 2,
        name: '校友书店',
        cover: '/assets/images/头像.png',
        owner: '赵六',
        category: '文化',
        location: '上海市黄浦区',
        distance: '1.5km',
        rating: 4.6,
        description: '提供优质图书和文化产品'
      }
    ]

    this.setData({
      associationInfo: mockData,
      members: mockMembers,
      notifications: mockNotifications,
      benefitActivities: mockBenefitActivities,
      nearbyBenefits: mockNearbyBenefits,
      alumniEnterprises: mockAlumniEnterprises,
      alumniShops: mockAlumniShops
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
  },

  // 认证标签点击
  handleCertificationTap(e) {
    const { type, id } = e.currentTarget.dataset
    if (!type || !id) return

    if (type === 'union') {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${id}`
      })
    } else if (type === 'promotion') {
      wx.navigateTo({
        url: `/pages/promotion/detail/detail?id=${id}`
      })
    }
  },

  // 查看母校详情
  viewSchoolDetail() {
    const { associationInfo } = this.data
    if (!associationInfo || !associationInfo.schoolId) {
      wx.showToast({
        title: '暂无母校信息',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${associationInfo.schoolId}`
    })
  },

  // 查看校友企业详情
  viewEnterpriseDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/enterprise/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/enterprise/detail/detail?id=${id}`
      })
    }
  },

  // 查看校友商铺详情
  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/shop/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/shop/detail/detail?id=${id}`
      })
    }
  }
})
