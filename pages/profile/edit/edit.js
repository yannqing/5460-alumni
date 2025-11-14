// pages/profile/edit/edit.js
const app = getApp()

Page({
  data: {
    form: {
      avatarUrl: '/assets/images/头像.png',
      nickName: '',
      gender: '',
      mobile: '',
      email: '',
      school: '',
      major: '',
      enrollYear: '',
      city: '',
      tags: [],
      bio: ''
    },
    saving: false,
    tagInput: '',
    genderOptions: ['保密', '男', '女'],
    genderIndex: 0,
    yearOptions: [],
    yearIndex: 0
  },

  onLoad() {
    this.initYearOptions()
    this.loadProfile()
  },

  initYearOptions() {
    const current = new Date().getFullYear()
    const years = []
    for (let i = current; i >= current - 50; i -= 1) {
      years.push(`${i}年`)
    }
    this.setData({ yearOptions: years })
  },

  loadProfile() {
    const globalInfo = app.globalData.userInfo || {}
    const mock = {
      avatarUrl: globalInfo.avatarUrl || '/assets/images/头像.png',
      nickName: globalInfo.nickName || '张三',
      gender: globalInfo.gender || '保密',
      mobile: globalInfo.mobile || '',
      email: globalInfo.email || '',
      school: globalInfo.school || '南京大学',
      major: globalInfo.major || '计算机科学',
      enrollYear: globalInfo.enrollYear || '2015年',
      city: globalInfo.city || '深圳',
      tags: globalInfo.tags || ['产品经理', '篮球迷'],
      bio: globalInfo.bio || '热爱校友活动，期待与更多伙伴交流。'
    }
    const genderIndex = this.data.genderOptions.indexOf(mock.gender)
    const yearIndex = Math.max(this.data.yearOptions.indexOf(mock.enrollYear), 0)
    this.setData({
      form: mock,
      genderIndex: genderIndex === -1 ? 0 : genderIndex,
      yearIndex
    })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleTextarea(e) {
    this.setData({
      'form.bio': e.detail.value
    })
  },

  handleGenderChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      genderIndex: index,
      'form.gender': this.data.genderOptions[index]
    })
  },

  handleYearChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      yearIndex: index,
      'form.enrollYear': this.data.yearOptions[index]
    })
  },

  handleTagInput(e) {
    this.setData({ tagInput: e.detail.value })
  },

  addTag(e) {
    const value = e.detail.value.trim()
    if (!value) return
    const { tags } = this.data.form
    if (tags.length >= 3) {
      wx.showToast({ title: '最多添加3个标签', icon: 'none' })
      this.setData({ tagInput: '' })
      return
    }
    if (tags.includes(value)) {
      wx.showToast({ title: '标签已存在', icon: 'none' })
      this.setData({ tagInput: '' })
      return
    }
    this.setData({
      'form.tags': [...tags, value],
      tagInput: ''
    })
  },

  removeTag(e) {
    const { index } = e.currentTarget.dataset
    const tags = [...this.data.form.tags]
    tags.splice(index, 1)
    this.setData({ 'form.tags': tags })
  },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles?.[0]?.tempFilePath
        if (tempFilePath) {
          this.setData({ 'form.avatarUrl': tempFilePath })
        }
      }
    })
  },

  validateForm() {
    const { nickName, mobile, email } = this.data.form
    if (!nickName) {
      wx.showToast({ title: '请填写姓名', icon: 'none' })
      return false
    }
    if (mobile && !/^1[3-9]\d{9}$/.test(mobile)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return false
    }
    if (email && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
      wx.showToast({ title: '邮箱格式不正确', icon: 'none' })
      return false
    }
    return true
  },

  saveProfile() {
    if (!this.validateForm()) return
    this.setData({ saving: true })
    setTimeout(() => {
      app.globalData.userInfo = {
        ...(app.globalData.userInfo || {}),
        ...this.data.form
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.setData({ saving: false })
      setTimeout(() => {
        wx.navigateBack()
      }, 600)
    }, 500)
  }
})

