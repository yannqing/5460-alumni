// pages/register/register.js
const app = getApp()
const config = require('../../utils/config.js')
const { userApi, schoolApi, authApi } = require('../../api/api.js')
const auth = require('../../utils/auth.js')

// 防抖函数
function debounce(fn, delay) {
  let timer = null
  return function () {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(function () {
      fn.apply(context, args)
    }, delay)
  }
}

Page({
  data: {
    statusBarHeight: 20,
    // 表单数据
    formData: {
      nickname: '',
      realName: '',
      gender: '', // male 或 female
      school: '',
      schoolId: '',
      previousName: '', // 学校曾用名
      phone: '',
    },
    // 学校搜索
    schoolSearchList: [],
    showSchoolResults: false,
    // 用户协议
    isAgreed: false,
    // 表单验证状态
    isFormValid: false,
    // 是否为开发模式
    isDevelopMode: false,
    // 3D 插图 (暂时使用占位)
    illustration: config.cloud.cosBaseUrl + '/cni-alumni/images/assets/home/background_11.png',
  },

  onLoad(options) {
    // 创建搜索防抖函数
    this.searchSchoolDebounced = debounce(this.searchSchool, 500)

    const systemInfo = wx.getSystemInfoSync()

    // 判断是否为开发模式
    const accountInfo = wx.getAccountInfoSync()
    const isDevelopMode = accountInfo.miniProgram.envVersion === 'develop'

    this.setData({
      statusBarHeight: systemInfo.statusBarHeight || 20,
      isDevelopMode,
    })

    // 获取用户信息并回填表单
    this.loadUserInfo()
  },

  // 获取用户信息回填表单
  async loadUserInfo() {
    try {
      const res = await userApi.getUserInfo()
      if (res && res.data && res.data.code === 200 && res.data.data) {
        const user = res.data.data
        const genderMap = { 1: 'male', 2: 'female' }
        const updates = {}

        if (user.nickname) {
          updates['formData.nickname'] = user.nickname
        }
        if (user.name) {
          updates['formData.realName'] = user.name
        }
        if (user.gender && genderMap[user.gender]) {
          updates['formData.gender'] = genderMap[user.gender]
        }
        // 学校信息在 alumniEducationList 中
        const edu = (user.alumniEducationList || [])[0]
        if (edu) {
          const schoolId = edu.schoolInfo?.schoolId || edu.schoolId
          const schoolName = edu.schoolInfo?.schoolName || edu.schoolName
          if (schoolId) {
            updates['formData.schoolId'] = schoolId
          }
          if (schoolName) {
            updates['formData.school'] = schoolName
          }
        }
        if (user.phone) {
          updates['formData.phone'] = user.phone
        }

        if (Object.keys(updates).length > 0) {
          this.setData(updates)
          this.validateForm()
        }
      }
    } catch (e) {
      console.error('获取用户信息失败:', e)
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index',
        })
      },
    })
  },

  // 获取微信昵称
  onNicknameChange(e) {
    const nickname = e.detail.value
    if (nickname) {
      this.setData({ 'formData.nickname': nickname })
      this.validateForm()
    }
  },

  // 清除昵称
  clearNickname() {
    this.setData({ 'formData.nickname': '' })
    this.validateForm()
  },

  // 输入真实姓名
  onNameInput(e) {
    this.setData({
      'formData.realName': e.detail.value,
    })
    this.validateForm()
  },

  // 选择性别
  selectGender(e) {
    const gender = e.currentTarget.dataset.gender
    this.setData({
      'formData.gender': gender,
    })
    this.validateForm()
  },

  // --- 学校搜索处理 ---

  // 学校输入处理
  handleSchoolInput(e) {
    const value = e.detail.value
    this.setData({
      'formData.school': value,
      'formData.schoolId': '', // 清空ID，因为修改了名称
      'formData.previousName': '', // 清空曾用名
      showSchoolResults: true,
    })

    if (value.trim()) {
      this.searchSchoolDebounced(value)
    } else {
      this.setData({ schoolSearchList: [] })
    }
    this.validateForm()
  },

  // 学校输入聚焦
  handleSchoolFocus() {
    if (this.data.formData.school) {
      this.setData({ showSchoolResults: true })
      if (this.data.schoolSearchList.length === 0) {
        this.searchSchool(this.data.formData.school)
      }
    }
  },

  // 搜索学校
  async searchSchool(keyword) {
    if (!keyword) {
      return
    }
    try {
      const res = await schoolApi.getSchoolPage({
        current: 1,
        pageSize: 20,
        schoolName: keyword.trim(),
        previousName: keyword.trim(),
      })
      if (res.data && res.data.code === 200) {
        this.setData({
          schoolSearchList: res.data.data.records || [],
        })
      }
    } catch (e) {
      console.error('搜索学校失败', e)
    }
  },

  // 选择学校
  selectSchool(e) {
    const index = e.currentTarget.dataset.index
    const school = this.data.schoolSearchList[index]

    this.setData({
      'formData.schoolId': school.schoolId,
      'formData.school': school.schoolName,
      'formData.previousName': school.previousName || '', // 保存曾用名
      showSchoolResults: false,
    })
    this.validateForm()
  },

  // 关闭学校搜索结果
  closeSchoolResults() {
    this.setData({
      showSchoolResults: false,
    })
  },

  // 阻止冒泡
  preventBubble() {
    // 阻止冒泡
  },

  // 开发模式下直接填充测试手机号
  handleTestPhone() {
    this.setData({
      'formData.phone': '18823234345',
    })
    this.validateForm()
    wx.showToast({
      title: '已填充测试手机号',
      icon: 'none',
    })
  },

  // 获取微信手机号（新方式：基础库 2.21.2+）
  async onGetPhoneNumber(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      // 用户拒绝授权
      wx.showToast({
        title: '需要授权手机号才能注册',
        icon: 'none',
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
        const phoneNumber =
          res.data.data.phoneNumber || res.data.data.purePhoneNumber || res.data.data
        this.setData({
          'formData.phone': phoneNumber,
        })
        this.validateForm()
      } else {
        wx.showToast({
          title: res.data?.msg || '获取手机号失败',
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('获取手机号失败:', error)
      wx.showToast({
        title: '获取手机号失败，请重试',
        icon: 'none',
      })
    }
  },

  // 清除手机号（更换）
  clearPhone() {
    this.setData({
      'formData.phone': '',
    })
    this.validateForm()
  },

  // 切换用户协议勾选状态
  toggleAgreement() {
    this.setData({
      isAgreed: !this.data.isAgreed,
    })
    this.validateForm()
  },

  // 查看用户协议
  viewUserAgreement() {
    wx.showLoading({
      title: '加载中...',
    })
    wx.downloadFile({
      url: 'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/documents/privacy/user.pdf',
      success: function (res) {
        wx.hideLoading()
        const filePath = res.tempFilePath
        wx.openDocument({
          filePath: filePath,
          fileType: 'pdf',
          success: function (res) {
            console.log('打开用户协议成功')
          },
          fail: function (err) {
            console.error('打开用户协议失败', err)
            wx.showToast({
              title: '打开文档失败',
              icon: 'none',
            })
          },
        })
      },
      fail: function (err) {
        wx.hideLoading()
        console.error('下载用户协议失败', err)
        wx.showToast({
          title: '下载文档失败',
          icon: 'none',
        })
      },
    })
  },

  // 查看隐私政策
  viewPrivacyPolicy() {
    wx.showLoading({
      title: '加载中...',
    })
    wx.downloadFile({
      url: 'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/documents/privacy/privacy.pdf',
      success: function (res) {
        wx.hideLoading()
        const filePath = res.tempFilePath
        wx.openDocument({
          filePath: filePath,
          fileType: 'pdf',
          success: function (res) {
            console.log('打开隐私政策成功')
          },
          fail: function (err) {
            console.error('打开隐私政策失败', err)
            wx.showToast({
              title: '打开文档失败',
              icon: 'none',
            })
          },
        })
      },
      fail: function (err) {
        wx.hideLoading()
        console.error('下载隐私政策失败', err)
        wx.showToast({
          title: '下载文档失败',
          icon: 'none',
        })
      },
    })
  },

  // 跳转到反馈页面
  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback?type=1&title=' + encodeURIComponent('找不到学校'),
    })
  },

  // 验证表单
  validateForm() {
    const { formData, isAgreed } = this.data
    const isValid =
      formData.nickname.trim() !== '' &&
      formData.realName.trim() !== '' &&
      formData.gender !== '' &&
      formData.school !== '' &&
      formData.schoolId !== '' && // 确保从搜索结果中选择了学校
      formData.phone.trim() !== '' &&
      /^1[3-9]\d{9}$/.test(formData.phone) &&
      isAgreed

    this.setData({
      isFormValid: isValid,
    })
  },

  // 提交注册
  async submitRegister() {
    if (!this.data.isFormValid) {
      // 显示具体的错误提示
      const { formData, isAgreed } = this.data
      if (!formData.nickname.trim()) {
        wx.showToast({ title: '请输入昵称', icon: 'none' })
        return
      }
      if (!formData.realName.trim()) {
        wx.showToast({ title: '请输入真实姓名', icon: 'none' })
        return
      }
      if (!formData.gender) {
        wx.showToast({ title: '请选择性别', icon: 'none' })
        return
      }
      if (!formData.school || !formData.schoolId) {
        wx.showToast({ title: '请从搜索结果中选择学校', icon: 'none' })
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

      // 构建注册数据
      // 性别映射：male -> 1, female -> 2, 0-未知
      const genderMap = {
        male: 1,
        female: 2,
      }

      const registerData = {
        nickname: formData.nickname.trim(),
        name: formData.realName.trim(),
        schoolId: formData.schoolId,
        gender: genderMap[formData.gender] || 0,
        phone: formData.phone.trim(),
      }

      // 调用注册接口（会同时更新用户信息和添加教育经历）
      const res = await authApi.register(registerData)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        // 更新全局用户数据
        app.globalData.userData = {
          ...(app.globalData.userData || {}),
          ...registerData,
        }
        app.globalData.userInfo = {
          ...(app.globalData.userInfo || {}),
          ...registerData,
        }

        // 更新用户基本信息完善状态
        auth.updateProfileComplete(true)

        wx.showToast({
          title: '注册成功',
          icon: 'success',
        })

        // 检查是否有待跳转的页面（如从邀请链接/二维码进入）
        const redirectUrl = wx.getStorageSync('redirectAfterRegister')
        setTimeout(() => {
          if (redirectUrl) {
            // 清除缓存
            wx.removeStorageSync('redirectAfterRegister')
            // 跳转到目标页面
            wx.redirectTo({
              url: redirectUrl,
              fail: () => {
                // 如果跳转失败，回到首页
                wx.switchTab({
                  url: '/pages/index/index',
                })
              },
            })
          } else {
            // 没有待跳转页面，跳转到首页
            wx.switchTab({
              url: '/pages/index/index',
            })
          }
        }, 1500)
      } else {
        wx.showToast({
          title: res.data?.msg || '注册失败，请重试',
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('注册失败:', error)
      wx.showToast({
        title: '注册失败，请重试',
        icon: 'none',
      })
    }
  },
})
