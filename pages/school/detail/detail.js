// pages/school/detail/detail.js
Page({
  data: {
    schoolId: '',
    schoolInfo: null,
    loading: true,
    
    // 校友会列表
    associationList: []
  },

  onLoad(options) {
    const { id } = options
    this.setData({ schoolId: id })
    this.loadSchoolDetail()
  },

  onShareAppMessage() {
    return {
      title: this.data.schoolInfo?.name || '母校详情',
      path: `/pages/school/detail/detail?id=${this.data.schoolId}`
    }
  },

  // 加载学校详情
  loadSchoolDetail() {
    // TODO: 接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/school/${this.data.schoolId}`,
    //   method: 'GET',
    //   success: (res) => {
    //     if (res.data.code === 200) {
    //       const data = res.data.data
    //       // 后端返回的数据中，如果学校已通过校友总会认证，会包含 certifiedUnion 字段
    //       // certifiedUnion 格式: { id: 1, name: 'XX校友总会', ... }
    //       this.setData({
    //         schoolInfo: data,
    //         associationList: data.associationList || [],
    //         loading: false
    //       })
    //     }
    //   },
    //   fail: () => {
    //     this.setData({ loading: false })
    //   }
    // })

    // 模拟数据 - 只保留基本信息字段
    const mockData = {
      id: this.data.schoolId,
      name: '南京大学',
      icon: '/assets/logo/njdx.jpg',
      cover: '/assets/images/南京大学背景图.jpg',
      location: '江苏省南京市',
      oldNames: ['金陵大学', '国立中央大学'],
      // 认证的校友总会信息（通过认证功能认证成功后自动添加，不是前端写死的）
      // 如果学校未认证，此字段为 null 或 undefined
      certifiedUnion: {
        id: 1,
        name: '南京大学校友总会'
      }
    }

    // 校友会列表假数据
    const associationListData = [
      {
        id: 101,
        name: '南京大学上海校友会',
        icon: 'https://via.placeholder.com/120/ff6b9d/ffffff?text=SH',
        location: '上海市',
        memberCount: 1250,
        isCertified: true,
        isJoined: false
      },
      {
        id: 102,
        name: '南京大学北京校友会',
        icon: 'https://via.placeholder.com/120/4a90e2/ffffff?text=BJ',
        location: '北京市',
        memberCount: 980,
        isCertified: true,
        isJoined: true
      },
      {
        id: 103,
        name: '南京大学深圳校友会',
        icon: 'https://via.placeholder.com/120/50c878/ffffff?text=SZ',
        location: '深圳市',
        memberCount: 856,
        isCertified: false,
        isJoined: false
      },
      {
        id: 104,
        name: '南京大学广州校友会',
        icon: 'https://via.placeholder.com/120/ffa500/ffffff?text=GZ',
        location: '广州市',
        memberCount: 642,
        isCertified: true,
        isJoined: false
      },
      {
        id: 105,
        name: '南京大学杭州校友会',
        icon: 'https://via.placeholder.com/120/9b59b6/ffffff?text=HZ',
        location: '杭州市',
        memberCount: 523,
        isCertified: false,
        isJoined: false
      }
    ]

    this.setData({
      schoolInfo: mockData,
      associationList: associationListData,
      loading: false
    })
  },

  // 查看认证的校友总会详情
  viewCertifiedUnion() {
    const { schoolInfo } = this.data
    if (schoolInfo && schoolInfo.certifiedUnion && schoolInfo.certifiedUnion.id) {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${schoolInfo.certifiedUnion.id}`
      })
    }
  },

  // 预览图片
  previewImage(e) {
    const { url } = e.currentTarget.dataset
    wx.previewImage({
      urls: [url],
      current: url
    })
  },

  // 查看校友会详情
  viewAssociationDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 加入/退出校友会
  toggleJoinAssociation(e) {
    const { id } = e.currentTarget.dataset
    const associationList = this.data.associationList.map(item => {
      if (item.id === id) {
        return { ...item, isJoined: !item.isJoined }
      }
      return item
    })
    this.setData({ associationList })
    
    const association = associationList.find(item => item.id === id)
    wx.showToast({
      title: association.isJoined ? '加入成功' : '已退出',
      icon: 'success',
      duration: 1500
    })
  }
})
