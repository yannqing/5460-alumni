// pages/index/index.js
const { schoolApi, alumniApi, couponApi, activityApi } = require('../../api/index.js')

const DEFAULT_AVATAR = '/assets/images/头像.png'

Page({
  data: {
    scrollIntoView: '', // 滑动到指定卡片
    currentCardIndex: 0, // 当前卡片索引
    activeTab: 0, // 当前标签页索引
    // 校友会卡片数据
    associationCards: [
      {
        id: 1,
        name: '南京大学上海校友会',
        schoolName: '南京大学',
        icon: '/assets/logo/njdx.jpg',
        location: '上海市',
        hasNotification: true,
        hasNewActivity: false,
        hasNewBenefit: true
      },
      {
        id: 2,
        name: '浙江大学杭州校友会',
        schoolName: '浙江大学',
        icon: '/assets/logo/njdx.jpg',
        location: '浙江省杭州市',
        hasNotification: false,
        hasNewActivity: true,
        hasNewBenefit: false
      },
      {
        id: 3,
        name: '复旦大学上海校友会',
        schoolName: '复旦大学',
        icon: '/assets/logo/njdx.jpg',
        location: '上海市',
        hasNotification: true,
        hasNewActivity: true,
        hasNewBenefit: true
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

    // 校友圈帖子
    circlePosts: [],

    // 附近权益
    nearbyBenefits: [],

    // 可能认识的人
    recommendedPeople: [],

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
      circlePosts: [
        {
          id: 1,
          type: 'news',
          username: '王小刚',
          avatar: DEFAULT_AVATAR,
          time: '3小时前',
          groupIcon: '/assets/logo/njdx.jpg',
          groupName: '江南大学无锡校友会',
          cover: '/assets/images/南京大学背景图.jpg',
          title: '江南大学洛杉矶校友会成立。',
          description: '欢迎各位新老校友加入，欢迎各位新老校友加入，欢迎各位新老校友加入……',
          location: 'Casliser Roll.RD',
          likes: 47,
          shares: 47
        },
        {
          id: 2,
          type: 'event',
          username: '王小刚',
          avatar: DEFAULT_AVATAR,
          time: '3小时前',
          groupIcon: '/assets/logo/njdx.jpg',
          groupName: '江南大学无锡校友会',
          eventTitle: '洛杉矶苏超观影会',
          timeRange: '2024.11.04 17:50 — 2024.11.04 21:50',
          participants: [
            { id: 'p1', avatar: DEFAULT_AVATAR },
            { id: 'p2', avatar: DEFAULT_AVATAR },
            { id: 'p3', avatar: DEFAULT_AVATAR },
            { id: 'p4', avatar: DEFAULT_AVATAR },
            { id: 'p5', avatar: DEFAULT_AVATAR },
            { id: 'p6', avatar: DEFAULT_AVATAR }
          ],
          participantText: '24位校友已经报名参与',
          ctaText: '我要报名',
          locationTag: '定位\n符',
          location: 'Casliser Roll.RD',
          likes: 47,
          shares: 47
        },
        {
          id: 3,
          type: 'event',
          username: '王小刚',
          avatar: DEFAULT_AVATAR,
          time: '2小时前',
          groupIcon: '/assets/logo/njdx.jpg',
          groupName: '江南大学无锡校友会',
          eventTitle: '校友徒步嘉年华',
          timeRange: '2024.12.01 08:00 — 2024.12.01 12:00',
          participants: [
            { id: 'p1', avatar: DEFAULT_AVATAR },
            { id: 'p2', avatar: DEFAULT_AVATAR },
            { id: 'p3', avatar: DEFAULT_AVATAR },
            { id: 'p4', avatar: DEFAULT_AVATAR },
            { id: 'p5', avatar: DEFAULT_AVATAR },
            { id: 'p6', avatar: DEFAULT_AVATAR }
          ],
          participantText: '56位校友已经报名参与',
          ctaText: '我要报名',
          locationTag: '定位\n符',
          location: '玄武湖公园',
          likes: 102,
          shares: 35
        }
      ],
      nearbyBenefits: [
        {
          id: 1,
          storeAvatar: DEFAULT_AVATAR,
          storeName: '店铺名',
          distance: '3.13km',
          associations: '江南大学无锡校友会 等13个校友会',
          products: [
            { id: 1, image: '/assets/images/商品图.jpg', name: '商品名称', price: '满200减100' },
            { id: 2, image: '/assets/images/商品图.jpg', name: '商品名称', price: '159-367-' },
            { id: 3, image: '/assets/images/商品图.jpg', name: '商品名称', price: '满200减100' }
          ]
        },
        {
          id: 2,
          storeAvatar: DEFAULT_AVATAR,
          storeName: '店铺名',
          distance: '3.13km',
          associations: '江南大学无锡校友会 等13个校友会',
          products: [
            { id: 1, image: '/assets/images/商品图.jpg', name: '商品名称', price: '满200减100' },
            { id: 2, image: '/assets/images/商品图.jpg', name: '商品名称', price: '159-367-' },
            { id: 3, image: '/assets/images/商品图.jpg', name: '商品名称', price: '满200减100' }
          ]
        }
      ],
      recommendedPeople: [
        {
          id: 1,
          name: '刘奋洋',
          avatar: DEFAULT_AVATAR,
          tags: [
            { name: '江南大学无锡校友会' },
            { icon: '/assets/logo/njdx.jpg', name: '江南大学无锡校友会' }
          ]
        },
        {
          id: 2,
          name: '刘奋洋',
          avatar: DEFAULT_AVATAR,
          tags: [
            { name: '江南大学无锡校友会' },
            { icon: '/assets/logo/njdx.jpg', name: '江南大学无锡校友会' }
          ]
        },
        {
          id: 3,
          name: '刘奋洋',
          avatar: DEFAULT_AVATAR,
          tags: [
            { name: '江南大学无锡校友会' },
            { icon: '/assets/logo/njdx.jpg', name: '江南大学无锡校友会' }
          ]
        },
        {
          id: 4,
          name: '刘奋洋',
          avatar: DEFAULT_AVATAR,
          tags: [
            { name: '江南大学无锡校友会' },
            { icon: '/assets/logo/njdx.jpg', name: '江南大学无锡校友会' }
          ]
        },
        {
          id: 5,
          name: '刘奋洋',
          avatar: DEFAULT_AVATAR,
          tags: [
            { name: '江南大学无锡校友会' },
            { icon: '/assets/logo/njdx.jpg', name: '江南大学无锡校友会' }
          ]
        }
      ],
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
          avatar: DEFAULT_AVATAR,
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
          avatar: DEFAULT_AVATAR,
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
          avatar: DEFAULT_AVATAR,
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

  // 校友会卡片点击
  onAssociationCardTap(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 通知点击
  onNotificationTap(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/notification/list/list?associationId=${id}`
    })
  },

  // 活动点击
  onActivityTap(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/list/list?associationId=${id}`
    })
  },

  // 附近权益点击
  onBenefitTap(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/benefit/list/list?associationId=${id}`
    })
  },

  viewCircleDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({
      url: `/pages/circle/detail/detail?id=${id}`
    })
  },

  onLikeTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    this.updateCirclePost(id, post => ({
      likes: (post.likes || 0) + 1
    }))
    wx.showToast({ title: '点赞 +1', icon: 'none' })
  },

  onShareTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    this.updateCirclePost(id, post => ({
      shares: (post.shares || 0) + 1
    }))
    wx.showToast({ title: '转发次数 +1', icon: 'none' })
  },

  onEventCtaTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.showToast({ title: '报名成功', icon: 'success' })
  },

  updateCirclePost(id, updater) {
    const updated = this.data.circlePosts.map(post => {
      if (post.id === id) {
        const changes = typeof updater === 'function' ? updater(post) : {}
        return { ...post, ...changes }
      }
      return post
    })
    this.setData({ circlePosts: updated })
  },

  // 滑动事件处理
  onScroll(e) {
    const scrollLeft = e.detail.scrollLeft
    const cardWidth = 345 // 卡片宽度
    const cardGap = 20 // 卡片间距
    const cardTotalWidth = cardWidth + cardGap // 每个卡片占用的总宽度
    
    // 计算当前应该显示哪个卡片（四舍五入到最近的卡片）
    const currentIndex = Math.round(scrollLeft / cardTotalWidth)
    
    // 更新当前卡片索引
    if (currentIndex !== this.data.currentCardIndex) {
      this.setData({
        currentCardIndex: currentIndex
      })
    }
  },

  // 滑动结束事件处理 - 确保对齐到完整卡片
  onScrollEnd(e) {
    const scrollLeft = e.detail.scrollLeft
    const cardWidth = 345 // 卡片宽度
    const cardGap = 20 // 卡片间距
    const cardTotalWidth = cardWidth + cardGap // 每个卡片占用的总宽度
    
    // 计算当前应该显示哪个卡片（四舍五入到最近的卡片）
    const currentIndex = Math.round(scrollLeft / cardTotalWidth)
    
    // 确保索引在有效范围内
    const maxIndex = this.data.associationCards.length - 1
    const safeIndex = Math.max(0, Math.min(currentIndex, maxIndex))
    
    // 计算目标滚动位置
    const targetScrollLeft = safeIndex * cardTotalWidth
    
    // 如果当前位置与目标位置差距较大（超过5px），则滚动到目标卡片
    if (Math.abs(scrollLeft - targetScrollLeft) > 5) {
      const targetCard = this.data.associationCards[safeIndex]
      if (targetCard) {
        // 先清空 scrollIntoView，然后再设置，确保每次都能触发滚动
        this.setData({
          scrollIntoView: '',
          currentCardIndex: safeIndex
        }, () => {
          // 使用 setTimeout 确保清空后再设置
          setTimeout(() => {
            this.setData({
              scrollIntoView: `card-${targetCard.id}`
            })
          }, 50)
        })
      }
    } else {
      // 即使位置正确，也更新索引
      this.setData({
        currentCardIndex: safeIndex
      })
    }
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

  // 切换标签页
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      activeTab: parseInt(index)
    })
  },

  // 关注用户
  followPerson(e) {
    const { id } = e.currentTarget.dataset
    wx.showToast({
      title: '关注成功',
      icon: 'success'
    })
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
