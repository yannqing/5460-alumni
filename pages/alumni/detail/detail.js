// pages/alumni/detail/detail.js
Page({
  data: {
    alumniId: '',
    alumniInfo: null
  },

  onLoad(options) {
    this.setData({ alumniId: options.id })
    this.loadAlumniDetail()
  },

  loadAlumniDetail() {
    // 计算年龄和星座
    const birthDate = new Date('1990-05-15')
    const today = new Date()
    const age = today.getFullYear() - birthDate.getFullYear()
    const month = birthDate.getMonth() + 1
    const day = birthDate.getDate()
    const zodiac = this.getZodiac(month, day)
    
    const mockData = {
      id: this.data.alumniId,
      name: '张三',
      realName: '张三',
      nickname: '三三',
      account: 'zhangsan2024',
      avatar: '/assets/images/头像.png',
      background: 'https://cdn.example.com/backgrounds/alumni-bg.jpg',
      school: '南京大学',
      major: '计算机科学与技术',
      graduateYear: 2015,
      company: '腾讯科技',
      position: '高级工程师',
      location: '上海市',
      email: 'zhangsan@example.com',
      phone: '138****8888',
      wechat: 'zhangsan_wx',
      qq: '123456789',
      gender: 'male', // 'male' or 'female'
      birthDate: '1990-05-15',
      age: age,
      zodiac: zodiac,
      bio: '热爱技术，专注于前端开发领域，喜欢分享技术心得，致力于推动前端技术发展。',
      signature: '代码改变世界，技术成就未来',
      hobbies: ['编程', '阅读', '旅行', '摄影'],
      isFollowed: false,
      isCertified: true,
      associations: [
        { id: 1, name: '南京大学上海校友会', role: '会员' },
        { id: 2, name: '南京大学计算机系校友会', role: '理事' }
      ]
    }
    this.setData({ alumniInfo: mockData })
  },

  getZodiac(month, day) {
    const zodiacDates = [
      { name: '水瓶座', start: [1, 20], end: [2, 18] },
      { name: '双鱼座', start: [2, 19], end: [3, 20] },
      { name: '白羊座', start: [3, 21], end: [4, 19] },
      { name: '金牛座', start: [4, 20], end: [5, 20] },
      { name: '双子座', start: [5, 21], end: [6, 21] },
      { name: '巨蟹座', start: [6, 22], end: [7, 22] },
      { name: '狮子座', start: [7, 23], end: [8, 22] },
      { name: '处女座', start: [8, 23], end: [9, 22] },
      { name: '天秤座', start: [9, 23], end: [10, 23] },
      { name: '天蝎座', start: [10, 24], end: [11, 22] },
      { name: '射手座', start: [11, 23], end: [12, 21] },
      { name: '摩羯座', start: [12, 22], end: [1, 19] }
    ]
    
    for (let zodiac of zodiacDates) {
      const [startMonth, startDay] = zodiac.start
      const [endMonth, endDay] = zodiac.end
      
      if (startMonth === endMonth) {
        if (month === startMonth && day >= startDay && day <= endDay) {
          return zodiac.name
        }
      } else if (startMonth < endMonth) {
        if ((month === startMonth && day >= startDay) || (month === endMonth && day <= endDay)) {
          return zodiac.name
        }
      } else {
        if (month === startMonth && day >= startDay || month === endMonth && day <= endDay) {
          return zodiac.name
        }
      }
    }
    return '未知'
  },

  toggleFollow() {
    const { alumniInfo } = this.data
    alumniInfo.isFollowed = !alumniInfo.isFollowed
    this.setData({ alumniInfo })
    wx.showToast({
      title: alumniInfo.isFollowed ? '关注成功' : '已取消关注',
      icon: 'success'
    })
  },

  viewSchoolDetail(e) {
    const schoolId = e.currentTarget.dataset.id
    if (schoolId) {
      wx.navigateTo({
        url: `/pages/school/detail/detail?id=${schoolId}`
      })
    } else {
      // 如果没有ID，可以根据学校名称搜索或跳转到列表页
      wx.showToast({
        title: '母校信息加载中',
        icon: 'loading'
      })
    }
  },

  viewAssociationDetail(e) {
    const associationId = e.currentTarget.dataset.id
    if (associationId) {
      wx.navigateTo({
        url: `/pages/alumni-association/detail/detail?id=${associationId}`
      })
    }
  }
})
