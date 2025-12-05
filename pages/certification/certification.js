// pages/certification/certification.js
const app = getApp()

Page({
  data: {
    form: {
      name: '',
      school: '',
      major: '',
      enrollYear: '',
      graduateYear: '',
      idCard: '',
      diploma: '',
      certificate: ''
    },
    submitting: false,
    yearOptions: [],
    enrollYearIndex: 0,
    graduateYearIndex: 0,
    uploadType: '', // 'diploma' or 'certificate'
    certificationStatus: 'none' // none, pending, verified
  },

  onLoad() {
    this.initYearOptions()
    this.loadCertificationStatus()
    this.loadUserData()
  },

  initYearOptions() {
    const current = new Date().getFullYear()
    const years = []
    for (let i = current; i >= current - 50; i -= 1) {
      years.push(`${i}年`)
    }
    this.setData({ yearOptions: years })
  },

  loadUserData() {
    // 从全局数据获取用户信息
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || {}
    
    const formData = {
      name: userInfo.nickName || userData.nickName || userData.name || '',
      school: userInfo.school || userData.school || userData.schoolName || '',
      major: userInfo.major || userData.major || '',
      enrollYear: userInfo.enrollYear || userData.enrollYear || '',
      graduateYear: userData.graduateYear || '',
      idCard: '',
      diploma: userData.diplomaImage || '',
      certificate: userData.certificateImage || ''
    }
    
    const enrollYearIndex = Math.max(this.data.yearOptions.indexOf(formData.enrollYear ? `${formData.enrollYear}年` : ''), 0)
    const graduateYearIndex = Math.max(this.data.yearOptions.indexOf(formData.graduateYear ? `${formData.graduateYear}年` : ''), 0)
    
    this.setData({
      form: formData,
      enrollYearIndex,
      graduateYearIndex
    })
  },

  loadCertificationStatus() {
    const userData = app.globalData.userData || {}
    const status = userData.certificationStatus || userData.is_apply_acard === 1 ? 'verified' : 'none'
    this.setData({ certificationStatus: status })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleEnrollYearChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      enrollYearIndex: index,
      'form.enrollYear': this.data.yearOptions[index].replace('年', '')
    })
  },

  handleGraduateYearChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      graduateYearIndex: index,
      'form.graduateYear': this.data.yearOptions[index].replace('年', '')
    })
  },

  chooseDiploma() {
    this.setData({ uploadType: 'diploma' })
    this.chooseImage()
  },

  chooseCertificate() {
    this.setData({ uploadType: 'certificate' })
    this.chooseImage()
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles?.[0]?.tempFilePath
        if (tempFilePath) {
          const field = this.data.uploadType === 'diploma' ? 'diploma' : 'certificate'
          this.setData({
            [`form.${field}`]: tempFilePath
          })
          // TODO: 上传图片到服务器
          this.uploadImage(tempFilePath, field)
        }
      }
    })
  },

  uploadImage(filePath, field) {
    // TODO: 实现图片上传到服务器
    wx.showLoading({ title: '上传中...' })
    setTimeout(() => {
      wx.hideLoading()
      wx.showToast({
        title: '上传成功',
        icon: 'success'
      })
    }, 1000)
  },

  removeImage(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: ''
    })
  },

  validateForm() {
    const { name, school, major, enrollYear, graduateYear, idCard, diploma } = this.data.form
    
    if (!name || !name.trim()) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' })
      return false
    }
    
    if (!school || !school.trim()) {
      wx.showToast({ title: '请输入学校名称', icon: 'none' })
      return false
    }
    
    if (!major || !major.trim()) {
      wx.showToast({ title: '请输入专业', icon: 'none' })
      return false
    }
    
    if (!enrollYear) {
      wx.showToast({ title: '请选择入学年份', icon: 'none' })
      return false
    }
    
    if (!graduateYear) {
      wx.showToast({ title: '请选择毕业年份', icon: 'none' })
      return false
    }
    
    if (!idCard || !idCard.trim()) {
      wx.showToast({ title: '请输入身份证号', icon: 'none' })
      return false
    }
    
    if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(idCard)) {
      wx.showToast({ title: '身份证号格式不正确', icon: 'none' })
      return false
    }
    
    if (!diploma) {
      wx.showToast({ title: '请上传毕业证照片', icon: 'none' })
      return false
    }
    
    return true
  },

  submitCertification() {
    if (this.data.certificationStatus === 'verified') {
      wx.showToast({
        title: '您已通过认证',
        icon: 'none'
      })
      return
    }
    
    if (this.data.certificationStatus === 'pending') {
      wx.showToast({
        title: '认证审核中，请耐心等待',
        icon: 'none'
      })
      return
    }
    
    if (!this.validateForm()) return
    
    this.setData({ submitting: true })
    
    // TODO: 调用后端接口提交认证申请
    setTimeout(() => {
      wx.showToast({
        title: '提交成功，等待审核',
        icon: 'success'
      })
      this.setData({
        submitting: false,
        certificationStatus: 'pending'
      })
      
      // 更新全局数据
      if (app.globalData.userData) {
        app.globalData.userData.certificationStatus = 'pending'
      }
      
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }, 1000)
  }
})

