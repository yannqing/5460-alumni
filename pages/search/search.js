// pages/search/search.js
const { searchApi } = require('../../api/index.js')

Page({
  data: {
    keyword: '',
    activeTab: 0,
    tabs: ['全部', '母校', '校友会', '校友'],
    hotSearchList: ['南京大学', '上海校友会', '计算机', '优惠券'],
    searchHistory: [],
    searchResult: {
      schools: [],
      associations: [],
      alumni: []
    },
    showResult: false
  },

  onLoad() {
    const history = wx.getStorageSync('searchHistory') || []
    this.setData({ searchHistory: history })
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    const { keyword } = this.data
    if (!keyword.trim()) {
      wx.showToast({ title: '请输入搜索内容', icon: 'none' })
      return
    }

    this.saveSearchHistory(keyword)
    this.performSearch(keyword)
  },

  saveSearchHistory(keyword) {
    let history = this.data.searchHistory
    history = history.filter(item => item !== keyword)
    history.unshift(keyword)
    history = history.slice(0, 10)
    this.setData({ searchHistory: history })
    wx.setStorageSync('searchHistory', history)
  },

  performSearch(keyword) {
    // 模拟搜索结果
    const mockResult = {
      schools: [
        {
          id: 1,
          name: '南京大学',
          icon: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=NJU',
          location: '江苏省南京市',
          alumniCount: 12580
        }
      ],
      associations: [
        {
          id: 1,
          name: '南京大学上海校友会',
          location: '上海市',
          memberCount: 1580
        }
      ],
      alumni: [
        {
          id: 1,
          name: '张三',
          avatar: '/assets/images/头像.png',
          school: '南京大学',
          company: '腾讯科技'
        }
      ]
    }

    this.setData({
      searchResult: mockResult,
      showResult: true
    })
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
  },

  onHotSearchTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.onSearch()
  },

  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.onSearch()
  },

  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({ searchHistory: [] })
          wx.removeStorageSync('searchHistory')
        }
      }
    })
  },

  viewSchoolDetail(e) {
    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewAssociationDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewAlumniDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
