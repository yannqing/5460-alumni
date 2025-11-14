// pages/index/index.js
const { schoolApi, alumniApi, couponApi, activityApi } = require('../../api/index.js')

Page({
  data: {
    // 轮播图数据
    banners: [
      {
        id: 1,
        image: 'https://via.placeholder.com/750x300/ff6b9d/ffffff?text=Banner1',
        title: '校友总会年度大会',
        url: ''
      },
      {
        id: 2,
        image: 'https://via.placeholder.com/750x300/ff8fb5/ffffff?text=Banner2',
        title: '优惠券限时抢购',
        url: ''
      },
      {
        id: 3,
        image: 'https://via.placeholder.com/750x300/ffb6d4/ffffff?text=Banner3',
        title: '校友企业展示',
        url: ''
      }
    ],

    // 快速入口
    quickEntries: [
      { id: 1, name: '母校', icon: '/assets/icons/母校.png', url: '/pages/school/list/list' },
      { id: 2, name: '校友会', icon: '/assets/icons/校友会.png', url: '/pages/alumni-association/list/list' },
      { id: 3, name: '校友', icon: '/assets/icons/校友.png', url: '/pages/alumni/list/list' },
      { id: 4, name: '优惠券', icon: '/assets/icons/优惠券.png', url: '/pages/coupon/list/list' },
      { id: 5, name: '圈子', icon: '/assets/icons/圈子.png', url: '/pages/circle/list/list' },
      { id: 6, name: '活动', icon: '/assets/icons/活动.png', url: '/pages/activity/list/list' },
      { id: 7, name: '商家', icon: '/assets/icons/商家.png', url: '/pages/merchant/list/list' },
      { id: 8, name: '更多', icon: '/assets/icons/更多.png', url: '' }
    ],

    // 推荐母校
    recommendSchools: [],

    // 热门校友会
    hotAssociations: [],

    // 推荐校友
    recommendAlumni: [],

    // 限时优惠券
    limitedCoupons: [],

    // 最新活动
    recentActivities: [],

    loading: false,
    refreshing: false
  },

  onLoad() {
    this.loadPageData()
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadPageData()
  },

  // 加载页面数据
  loadPageData() {
    this.setData({ loading: true })

    // 模拟数据
    this.setData({
      recommendSchools: [
        {
          id: 1,
          name: '南京大学',
          icon: '/assets/logo/njdx.jpg',
          location: '江苏省南京市',
          alumniCount: 12580,
          associationCount: 156,
          isFollowed: false,
          isCertified: true,
          tags: ['985', '211', '双一流']
        },
        {
          id: 2,
          name: '浙江大学',
          icon: '/assets/logo/njdx.jpg',
          location: '浙江省杭州市',
          alumniCount: 15620,
          associationCount: 189,
          isFollowed: true,
          isCertified: true,
          tags: ['985', '211', '双一流']
        },
        {
          id: 3,
          name: '复旦大学',
          icon: '/assets/logo/njdx.jpg',
          location: '上海市',
          alumniCount: 14230,
          associationCount: 178,
          isFollowed: false,
          isCertified: true,
          tags: ['985', '211', '双一流']
        }
      ],

      hotAssociations: [
        {
          id: 1,
          name: '南京大学上海校友会',
          schoolName: '南京大学',
          icon: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=NJU-SH',
          location: '上海市',
          memberCount: 1580,
          isJoined: false
        },
        {
          id: 2,
          name: '浙江大学北京校友会',
          schoolName: '浙江大学',
          icon: 'https://via.placeholder.com/100/ff8fb5/ffffff?text=ZJU-BJ',
          location: '北京市',
          memberCount: 2350,
          isJoined: true
        }
      ],

      recommendAlumni: [
        {
          id: 1,
          name: '张三',
          avatar: 'https://via.placeholder.com/150/ff6b9d/ffffff?text=ZS',
          school: '南京大学',
          major: '计算机科学与技术',
          graduateYear: 2015,
          company: '腾讯科技',
          position: '高级工程师',
          isFollowed: false
        },
        {
          id: 2,
          name: '李四',
          avatar: 'https://via.placeholder.com/150/ff8fb5/ffffff?text=LS',
          school: '浙江大学',
          major: '软件工程',
          graduateYear: 2016,
          company: '阿里巴巴',
          position: '技术专家',
          isFollowed: true
        },
        {
          id: 3,
          name: '王五',
          avatar: 'https://via.placeholder.com/150/ffb6d4/ffffff?text=WW',
          school: '复旦大学',
          major: '人工智能',
          graduateYear: 2017,
          company: '字节跳动',
          position: '算法工程师',
          isFollowed: false
        }
      ],

      limitedCoupons: [
        {
          id: 1,
          title: '星巴克100元优惠券',
          merchant: '星巴克咖啡',
          originalPrice: 100,
          discountPrice: 59,
          image: 'https://via.placeholder.com/300x200/ff6b9d/ffffff?text=Starbucks',
          stock: 50,
          totalStock: 100,
          startTime: '2025-11-13 10:00:00',
          endTime: '2025-11-20 23:59:59',
          type: 'rush' // rush: 抢购, normal: 正常领取
        },
        {
          id: 2,
          title: '海底捞200元代金券',
          merchant: '海底捞火锅',
          originalPrice: 200,
          discountPrice: 0,
          image: 'https://via.placeholder.com/300x200/ff8fb5/ffffff?text=Haidilao',
          stock: 200,
          totalStock: 200,
          endTime: '2025-11-30 23:59:59',
          type: 'normal'
        }
      ],

      recentActivities: [
        {
          id: 1,
          title: '2025年度校友联谊会',
          cover: 'https://via.placeholder.com/400x250/ff6b9d/ffffff?text=Activity1',
          organizer: '南京大学校友总会',
          location: '南京国际会议中心',
          startTime: '2025-12-15 14:00',
          participantCount: 156,
          status: 'upcoming' // upcoming: 即将开始, ongoing: 进行中, ended: 已结束
        },
        {
          id: 2,
          title: '校友企业招聘会',
          cover: 'https://via.placeholder.com/400x250/ff8fb5/ffffff?text=Activity2',
          organizer: '浙江大学上海校友会',
          location: '上海国际会展中心',
          startTime: '2025-12-20 09:00',
          participantCount: 328,
          status: 'upcoming'
        }
      ],

      loading: false,
      refreshing: false
    })

    wx.stopPullDownRefresh()
  },

  // 轮播图点击
  onBannerTap(e) {
    const { index } = e.currentTarget.dataset
    console.log('点击轮播图', index)
  },

  // 快速入口点击
  onQuickEntryTap(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    } else {
      wx.showToast({
        title: '敬请期待',
        icon: 'none'
      })
    }
  },

  // 查看更多母校
  viewMoreSchools() {
    wx.navigateTo({
      url: '/pages/school/list/list'
    })
  },

  // 查看母校详情
  viewSchoolDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${id}`
    })
  },

  // 关注/取消关注母校
  toggleFollowSchool(e) {
    const { id, followed } = e.currentTarget.dataset
    const { recommendSchools } = this.data

    const index = recommendSchools.findIndex(item => item.id === id)
    if (index !== -1) {
      recommendSchools[index].isFollowed = !followed
      this.setData({ recommendSchools })

      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  },

  // 查看更多校友会
  viewMoreAssociations() {
    wx.navigateTo({
      url: '/pages/alumni-association/list/list'
    })
  },

  // 查看校友会详情
  viewAssociationDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 查看更多校友
  viewMoreAlumni() {
    wx.navigateTo({
      url: '/pages/alumni/list/list'
    })
  },

  // 查看校友详情
  viewAlumniDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${id}`
    })
  },

  // 关注/取消关注校友
  toggleFollowAlumni(e) {
    const { id, followed } = e.currentTarget.dataset
    const { recommendAlumni } = this.data

    const index = recommendAlumni.findIndex(item => item.id === id)
    if (index !== -1) {
      recommendAlumni[index].isFollowed = !followed
      this.setData({ recommendAlumni })

      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  },

  // 查看更多优惠券
  viewMoreCoupons() {
    wx.navigateTo({
      url: '/pages/coupon/list/list'
    })
  },

  // 查看优惠券详情
  viewCouponDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?id=${id}`
    })
  },

  // 抢购/领取优惠券
  handleCoupon(e) {
    const { id, type } = e.currentTarget.dataset

    if (type === 'rush') {
      wx.navigateTo({
        url: `/pages/coupon/rush/rush?id=${id}`
      })
    } else {
      wx.showModal({
        title: '提示',
        content: '确认领取该优惠券吗？',
        success: (res) => {
          if (res.confirm) {
            wx.showToast({
              title: '领取成功',
              icon: 'success'
            })
          }
        }
      })
    }
  },

  // 查看更多活动
  viewMoreActivities() {
    wx.navigateTo({
      url: '/pages/activity/list/list'
    })
  },

  // 查看活动详情
  viewActivityDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  }
})
