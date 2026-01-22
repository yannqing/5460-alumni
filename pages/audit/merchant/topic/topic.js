// pages/audit/merchant/topic/topic.js
const app = getApp()

Page({
  data: {
    loading: false,
    topicList: [],
    currentTab: 0,
    tabs: [
      { id: 0, name: '全部话题', count: 0 },
      { id: 1, name: '已发布', count: 0 },
      { id: 2, name: '待审核', count: 0 },
      { id: 3, name: '已下架', count: 0 }
    ]
  },

  onLoad(options) {
    this.loadTopicData()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      })
    }
  },

  onPullDownRefresh() {
    this.loadTopicData()
    wx.stopPullDownRefresh()
  },

  loadTopicData() {
    this.setData({ loading: true })
    
    setTimeout(() => {
      const mockTopics = [
        {
          id: 1,
          title: '毕业季特别活动',
          description: '邀请各位校友分享毕业时的难忘回忆，一起重温青春岁月',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '张校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '计算机学院校友会'
          },
          status: 'published',
          viewCount: 1256,
          commentCount: 89,
          likeCount: 234,
          createTime: '2024-01-15 10:30'
        },
        {
          id: 2,
          title: '创业经验分享会',
          description: '成功创业校友分享创业历程和经验，为有创业想法的学弟学妹提供指导',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '李校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '商学院校友会'
          },
          status: 'pending',
          viewCount: 0,
          commentCount: 0,
          likeCount: 0,
          createTime: '2024-01-18 14:20'
        },
        {
          id: 3,
          title: '校友返校日活动',
          description: '组织校友返校参观，重温校园美好时光，参观校史馆和实验室',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '王校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '机械学院校友会'
          },
          status: 'published',
          viewCount: 892,
          commentCount: 56,
          likeCount: 178,
          createTime: '2024-01-10 09:15'
        },
        {
          id: 4,
          title: '职场交流沙龙',
          description: '不同行业校友交流职场心得，分享行业动态和求职技巧',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '赵校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '电子学院校友会'
          },
          status: 'draft',
          viewCount: 0,
          commentCount: 0,
          likeCount: 0,
          createTime: '2024-01-20 16:45'
        },
        {
          id: 5,
          title: '年度校友聚会',
          description: '2024年度校友聚会盛况回顾，各地校友齐聚一堂共叙情谊',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '陈校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '文学院校友会'
          },
          status: 'published',
          viewCount: 2103,
          commentCount: 145,
          likeCount: 398,
          createTime: '2024-01-05 11:00'
        },
        {
          id: 6,
          title: '学术交流论坛',
          description: '各领域专家校友分享最新研究成果，促进学术交流与合作',
          coverImage: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
          author: {
            name: '刘校友',
            avatar: 'https://cni-alumni.yannqing.com/upload/images/assets/images/avatar.png',
            association: '研究生院校友会'
          },
          status: 'offline',
          viewCount: 567,
          commentCount: 34,
          likeCount: 89,
          createTime: '2024-01-08 15:30'
        }
      ]

      const publishedCount = mockTopics.filter(t => t.status === 'published').length
      const pendingCount = mockTopics.filter(t => t.status === 'pending').length
      const offlineCount = mockTopics.filter(t => t.status === 'offline').length

      const updatedTabs = [
        { id: 0, name: '全部话题', count: mockTopics.length },
        { id: 1, name: '已发布', count: publishedCount },
        { id: 2, name: '待审核', count: pendingCount },
        { id: 3, name: '已下架', count: offlineCount }
      ]

      this.setData({
        topicList: mockTopics,
        tabs: updatedTabs,
        loading: false
      })
    }, 500)
  },

  onTabChange(e) {
    const { id } = e.currentTarget.dataset
    this.setData({ currentTab: id })
    this.filterTopics(id)
  },

  filterTopics(tabId) {
    let filteredList = []
    
    switch (tabId) {
      case 0:
        filteredList = this.data.topicList
        break
      case 1:
        filteredList = this.data.topicList.filter(t => t.status === 'published')
        break
      case 2:
        filteredList = this.data.topicList.filter(t => t.status === 'pending')
        break
      case 3:
        filteredList = this.data.topicList.filter(t => t.status === 'offline' || t.status === 'draft')
        break
    }
    
    this.setData({ topicList: filteredList })
  },

  onTopicTap(e) {
    const { id } = e.currentTarget.dataset
    wx.showToast({
      title: `查看话题 ${id}`,
      icon: 'none'
    })
  },

  onAddTopic() {
    wx.showToast({
      title: '新增话题功能',
      icon: 'none'
    })
  }
})