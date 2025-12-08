// pages/my-follow/my-follow.js
const config = require('../../utils/config.js')

Page({
  data: {
    type: 'school', // school, alumni
    followList: []
  },

  onLoad(options) {
    this.setData({ type: options.type || 'school' })
    this.loadFollowList()
  },

  loadFollowList() {
    const mockSchools = [
      {
        id: 1,
        name: '南京大学',
        icon: config.defaultAvatar,
        location: '江苏省南京市',
        alumniCount: 12580,
        is985: true,
        is211: true
      },
      {
        id: 2,
        name: '浙江大学',
        icon: config.defaultAvatar,
        location: '浙江省杭州市',
        alumniCount: 15200,
        is985: true,
        is211: true
      },
      {
        id: 3,
        name: '清华大学',
        icon: config.defaultAvatar,
        location: '北京市',
        alumniCount: 28000,
        is985: true,
        is211: true
      },
      {
        id: 4,
        name: '北京大学',
        icon: config.defaultAvatar,
        location: '北京市',
        alumniCount: 26500,
        is985: true,
        is211: true
      },
      {
        id: 5,
        name: '复旦大学',
        icon: config.defaultAvatar,
        location: '上海市',
        alumniCount: 18900,
        is985: true,
        is211: true
      }
    ]

    const mockAlumni = [
      {
        id: 1,
        name: '张三',
        avatar: config.defaultAvatar,
        school: '南京大学',
        company: '腾讯科技',
        major: '计算机科学与技术',
        isVerified: true
      },
      {
        id: 2,
        name: '李四',
        avatar: config.defaultAvatar,
        school: '浙江大学',
        company: '阿里巴巴',
        major: '软件工程',
        isVerified: true
      },
      {
        id: 3,
        name: '王五',
        avatar: config.defaultAvatar,
        school: '清华大学',
        company: '字节跳动',
        major: '人工智能',
        isVerified: true
      },
      {
        id: 4,
        name: '赵六',
        avatar: config.defaultAvatar,
        school: '北京大学',
        company: '百度',
        major: '数据科学',
        isVerified: true
      },
      {
        id: 5,
        name: '孙七',
        avatar: config.defaultAvatar,
        school: '复旦大学',
        company: '美团',
        major: '信息管理',
        isVerified: true
      },
      {
        id: 6,
        name: '周八',
        avatar: config.defaultAvatar,
        school: '上海交通大学',
        company: '京东',
        major: '电子商务',
        isVerified: true
      }
    ]

    this.setData({
      followList: this.data.type === 'school' ? mockSchools : mockAlumni
    })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    const url = this.data.type === 'school'
      ? `/pages/school/detail/detail?id=${id}`
      : `/pages/alumni/detail/detail?id=${id}`
    wx.navigateTo({ url })
  }
})
