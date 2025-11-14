// pages/school/detail/detail.js
const { schoolApi, associationApi } = require('../../../api/index.js')

Page({
  data: {
    schoolId: '',
    schoolInfo: null,
    associations: [],
    loading: true,

    // 选项卡
    activeTab: 0,
    tabs: ['基本信息', '校友会', '校友总会'],

    // 校友总会信息
    alumniUnion: null
  },

  onLoad(options) {
    const { id } = options
    this.setData({ schoolId: id })
    this.loadSchoolDetail()
    this.loadAssociations()
  },

  onShareAppMessage() {
    return {
      title: this.data.schoolInfo?.name || '母校详情',
      path: `/pages/school/detail/detail?id=${this.data.schoolId}`
    }
  },

  // 加载学校详情
  loadSchoolDetail() {
    // 模拟数据
    const mockData = {
      id: this.data.schoolId,
      name: '南京大学',
      icon: '/assets/logo/njdx.jpg',
      cover: 'https://via.placeholder.com/750x400/ff8fb5/ffffff?text=NJU+Cover',
      location: '江苏省南京市',
      address: '江苏省南京市栖霞区仙林大道163号',
      alumniCount: 12580,
      associationCount: 156,
      isFollowed: false,
      isCertified: true,
      tags: ['985', '211', '双一流'],
      oldNames: ['金陵大学', '国立中央大学'],
      foundedYear: 1902,
      description: '南京大学坐落于钟灵毓秀、虎踞龙蟠的金陵古都，是一所历史悠久、声誉卓著的百年名校。其前身是创建于1902年的三江师范学堂，此后历经两江师范学堂、南京高等师范学校、国立东南大学、国立第四中山大学、国立中央大学、国立南京大学等历史时期，于1950年更名为南京大学。',
      website: 'https://www.nju.edu.cn',
      phone: '025-83593186'
    }

    // 校友总会信息
    const alumniUnionData = {
      id: 1,
      name: '南京大学校友总会',
      icon: 'https://via.placeholder.com/150/ff6b9d/ffffff?text=NJU+Union',
      isCertified: true,
      description: '南京大学校友总会是南京大学校友的全球性组织',
      hasApp: true,
      appPath: '',
      website: 'https://alumni.nju.edu.cn',
      phone: '025-83593186',
      email: 'alumni@nju.edu.cn',
      wechat: 'NJU_Alumni'
    }

    this.setData({
      schoolInfo: mockData,
      alumniUnion: alumniUnionData,
      loading: false
    })
  },

  // 加载校友会列表
  loadAssociations() {
    const mockData = [
      {
        id: 1,
        name: '南京大学上海校友会',
        location: '上海市',
        memberCount: 1580,
        isJoined: false,
        president: '张三',
        establishedYear: 2010
      },
      {
        id: 2,
        name: '南京大学北京校友会',
        location: '北京市',
        memberCount: 2350,
        isJoined: true,
        president: '李四',
        establishedYear: 2008
      },
      {
        id: 3,
        name: '南京大学深圳校友会',
        location: '广东省深圳市',
        memberCount: 1820,
        isJoined: false,
        president: '王五',
        establishedYear: 2012
      }
    ]

    this.setData({ associations: mockData })
  },

  // 切换选项卡
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({ activeTab: index })
  },

  // 关注/取消关注
  toggleFollow() {
    const { schoolInfo } = this.data
    schoolInfo.isFollowed = !schoolInfo.isFollowed
    this.setData({ schoolInfo })

    wx.showToast({
      title: schoolInfo.isFollowed ? '关注成功' : '已取消关注',
      icon: 'success'
    })
  },

  // 查看校友会详情
  viewAssociationDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 查看所有校友会
  viewAllAssociations() {
    wx.navigateTo({
      url: `/pages/alumni-association/list/list?schoolId=${this.data.schoolId}`
    })
  },

  // 查看校友总会详情
  viewAlumniUnion() {
    const { alumniUnion } = this.data
    if (alumniUnion.hasApp && alumniUnion.appPath) {
      // 跳转到小程序
      wx.navigateToMiniProgram({
        appId: alumniUnion.appPath,
        success: () => {
          console.log('跳转成功')
        },
        fail: () => {
          wx.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    } else {
      // 显示校友总会信息
      wx.showModal({
        title: alumniUnion.name,
        content: `${alumniUnion.description}\n\n联系方式：\n电话：${alumniUnion.phone}\n邮箱：${alumniUnion.email}\n微信：${alumniUnion.wechat}`,
        showCancel: false
      })
    }
  },

  // 拨打电话
  makeCall() {
    wx.makePhoneCall({
      phoneNumber: this.data.schoolInfo.phone
    })
  },

  // 复制网址
  copyWebsite() {
    wx.setClipboardData({
      data: this.data.schoolInfo.website,
      success: () => {
        wx.showToast({
          title: '已复制',
          icon: 'success'
        })
      }
    })
  },

  // 预览图片
  previewImage(e) {
    const { url } = e.currentTarget.dataset
    wx.previewImage({
      urls: [url],
      current: url
    })
  }
})
