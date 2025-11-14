// pages/alumni-association/list/list.js
const { associationApi } = require('../../../api/index.js')

Page({
  data: {
    keyword: '',
    activeTab: 0,
    tabs: ['全部', '全部', '默认排序', '我的关注'],
    associationList: [],
    page: 1,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    this.loadAssociationList(true)
  },

  onPullDownRefresh() {
    this.loadAssociationList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAssociationList(false)
    }
  },

  loadAssociationList(reset = false) {
    this.setData({ loading: true })

    const mockData = [
      {
        id: 1,
        name: '南京大学上海校友会',
        schoolName: '南京大学',
        icon: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=NJU',
        location: '江苏省南京市',
        memberCount: 12580,
        associationCount: 156,
        followCount: 156,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流']
      },
      {
        id: 2,
        name: '浙江大学杭州校友会',
        schoolName: '浙江大学',
        icon: 'https://via.placeholder.com/100/ff8fb5/ffffff?text=ZJU',
        location: '浙江省杭州市',
        memberCount: 15620,
        associationCount: 189,
        followCount: 189,
        isFollowed: true,
        isCertified: true,
        tags: ['985', '211', '双一流']
      },
      {
        id: 3,
        name: '复旦大学上海校友会',
        schoolName: '复旦大学',
        icon: 'https://via.placeholder.com/100/ffb6d4/ffffff?text=FDU',
        location: '上海市',
        memberCount: 14230,
        associationCount: 178,
        followCount: 178,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流']
      },
      {
        id: 4,
        name: '上海交通大学校友会',
        schoolName: '上海交通大学',
        icon: 'https://via.placeholder.com/100/ffc9e0/ffffff?text=SJTU',
        location: '上海市',
        memberCount: 16500,
        associationCount: 195,
        followCount: 195,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流']
      }
    ]

    setTimeout(() => {
      this.setData({
        associationList: reset ? mockData : [...this.data.associationList, ...mockData],
        loading: false,
        page: reset ? 1 : this.data.page + 1
      })
      if (reset) {
        wx.stopPullDownRefresh()
      }
    }, 500)
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.loadAssociationList(true)
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: index })
    this.loadAssociationList(true)
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { associationList } = this.data
    const index = associationList.findIndex(item => item.id === id)
    if (index !== -1) {
      associationList[index].isFollowed = !followed
      this.setData({ associationList })
      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  }
})
