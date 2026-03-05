// pages/register/register.js
const app = getApp()
const config = require('../../utils/config.js')
const { userApi } = require('../../api/api.js')
const auth = require('../../utils/auth.js')

Page({
  data: {
    statusBarHeight: 20,
    // 表单数据
    formData: {
      realName: '',
      gender: '', // male 或 female
      school: '',
      phone: ''
    },
    // 学校选择器
    schoolList: ['清华大学', '北京大学', '复旦大学', '上海交通大学', '浙江大学', '中国人民大学', '南京大学', '武汉大学', '中山大学', '华中科技大学'],
    schoolIndex: -1,
    // 用户协议
    isAgreed: false,
    // 表单验证状态
    isFormValid: false,
    // 3D 插图 (暂时使用占位)
    illustration: ''
  },

  onLoad(options) {
    const systemInfo = wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: systemInfo.statusBarHeight || 20
    })
  },

  // 返回上一页
  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }
    })
  },

  // 输入真实姓名
  onNameInput(e) {
    this.setData({
      'formData.realName': e.detail.value
    })
    this.validateForm()
  },

  // 选择性别
  selectGender(e) {
    const gender = e.currentTarget.dataset.gender
    this.setData({
      'formData.gender': gender
    })
    this.validateForm()
  },

  // 选择学校
  onSchoolChange(e) {
    const index = e.detail.value
    this.setData({
      schoolIndex: index,
      'formData.school': this.data.schoolList[index]
    })
    this.validateForm()
  },

  // 获取微信手机号（新方式：基础库 2.21.2+）
  async onGetPhoneNumber(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      // 用户拒绝授权
      wx.showToast({
        title: '需要授权手机号才能注册',
        icon: 'none'
      })
      return
    }

    try {
      wx.showLoading({ title: '获取中...' })

      // 新方式：直接从回调中获取 code（不需要调用 wx.login）
      // 将 code 发送到后端，后端调用微信 phonenumber.getPhoneNumber 接口获取手机号
      const code = e.detail.code

      // 调用后端接口获取手机号
      const res = await userApi.getPhoneNumber({ code })

      wx.hideLoading()

      if (res.data && res.data.code === 200 && res.data.data) {
        const phoneNumber = res.data.data.phoneNumber || res.data.data.purePhoneNumber || res.data.data
        this.setData({
          'formData.phone': phoneNumber
        })
        this.validateForm()
      } else {
        wx.showToast({
          title: res.data?.msg || '获取手机号失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('获取手机号失败:', error)
      wx.showToast({
        title: '获取手机号失败，请重试',
        icon: 'none'
      })
    }
  },

  // 清除手机号（更换）
  clearPhone() {
    this.setData({
      'formData.phone': ''
    })
    this.validateForm()
  },

  // 切换用户协议勾选状态
  toggleAgreement() {
    this.setData({
      isAgreed: !this.data.isAgreed
    })
    this.validateForm()
  },

  // 查看用户协议
  viewUserAgreement() {
    wx.showToast({
      title: '用户协议',
      icon: 'none'
    })
  },

  // 查看隐私政策
  viewPrivacyPolicy() {
    wx.showToast({
      title: '隐私政策',
      icon: 'none'
    })
  },

  // 验证表单
  validateForm() {
    const { formData, isAgreed } = this.data
    const isValid =
      formData.realName.trim() !== '' &&
      formData.gender !== '' &&
      formData.school !== '' &&
      formData.phone.trim() !== '' &&
      /^1[3-9]\d{9}$/.test(formData.phone) &&
      isAgreed

    this.setData({
      isFormValid: isValid
    })
  },

  // 提交注册
  async submitRegister() {
    if (!this.data.isFormValid) {
      // 显示具体的错误提示
      const { formData, isAgreed } = this.data
      if (!formData.realName.trim()) {
        wx.showToast({ title: '请输入真实姓名', icon: 'none' })
        return
      }
      if (!formData.gender) {
        wx.showToast({ title: '请选择性别', icon: 'none' })
        return
      }
      if (!formData.school) {
        wx.showToast({ title: '请选择学校', icon: 'none' })
        return
      }
      if (!formData.phone.trim()) {
        wx.showToast({ title: '请获取手机号', icon: 'none' })
        return
      }
      if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
        wx.showToast({ title: '手机号格式不正确', icon: 'none' })
        return
      }
      if (!isAgreed) {
        wx.showToast({ title: '请阅读并同意用户协议', icon: 'none' })
        return
      }
      return
    }

    try {
      wx.showLoading({ title: '注册中...' })

      const { formData } = this.data

      // 构建用户信息更新数据
      // 性别映射：male -> 1, female -> 2
      const genderMap = {
        'male': 1,
        'female': 2
      }

      const updateData = {
        name: formData.realName.trim(),
        gender: genderMap[formData.gender] || 0,
        phone: formData.phone.trim()
        // 学校信息暂时不保存，后续可以扩展教育经历
      }

      // 调用用户信息更新接口
      const res = await userApi.updateUserInfo(updateData)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        // 更新全局用户数据
        app.globalData.userData = {
          ...(app.globalData.userData || {}),
          ...updateData
        }
        app.globalData.userInfo = {
          ...(app.globalData.userInfo || {}),
          ...updateData
        }

        // 更新用户基本信息完善状态
        auth.updateProfileComplete(true)

        wx.showToast({
          title: '注册成功',
          icon: 'success'
        })

        // 跳转到首页
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/index/index'
          })
        }, 1500)
      } else {
        wx.showToast({
          title: res.data?.msg || '注册失败，请重试',
          icon: 'none'
        })
      }

    } catch (error) {
      wx.hideLoading()
      console.error('注册失败:', error)
      wx.showToast({
        title: '注册失败，请重试',
        icon: 'none'
      })
    }
  }
})
