// pages/alumni-association/detail/detail.js
Page({
  data: {
    associationId: '',
    associationInfo: null,
    activeTab: 0,
    tabs: ['基本信息', '成员列表', '活动公告'],
    members: [],
    activities: []
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
        avatar: 'https://via.placeholder.com/100/ff6b9d/ffffff?text=ZS',
        role: '会长',
        company: '腾讯科技',
        showRealName: true
      },
      {
        id: 2,
        name: '李四',
        avatar: 'https://via.placeholder.com/100/ff8fb5/ffffff?text=LS',
        role: '副会长',
        company: '阿里巴巴',
        showRealName: true
      }
    ]

    this.setData({
      associationInfo: mockData,
      members: mockMembers
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
  }
})
