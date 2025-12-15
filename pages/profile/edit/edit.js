// pages/profile/edit/edit.js
const app = getApp()
const { userApi, fileApi, schoolApi } = require('../../../api/api.js')

// ==================== 配置区域 ====================
// 联调时：将 USE_MOCK_DATA 改为 false，删除下面的假数据，补充缺失字段
const USE_MOCK_DATA = false // true: 使用假数据, false: 使用真实接口

// ==================== 数据映射函数 ====================
/**
 * 将后端用户数据映射为前端表单格式
 * 字段名称与后端 UserDetailVo 完全一致
 */
function mapUserInfoToForm(userInfo) {
  // 性别映射：0-未知，1-男，2-女 -> 前端索引
  const genderMap = { 0: 0, 1: 1, 2: 2 }
  const genderIndex = userInfo.gender !== null && userInfo.gender !== undefined 
    ? (genderMap[userInfo.gender] !== undefined ? genderMap[userInfo.gender] : 0)
    : 0

  // 证件类型映射：0-身份证，1-护照 -> 前端索引
  const identifyTypeMap = { 0: 0, 1: 1 }
  const identifyTypeIndex = userInfo.identifyType !== null && userInfo.identifyType !== undefined
    ? (identifyTypeMap[userInfo.identifyType] !== undefined ? identifyTypeMap[userInfo.identifyType] : 0)
    : 0

  // 星座映射：后端值1-12 -> 前端索引0-11
  // 后端定义：1-摩羯座 2-水瓶座 3-双鱼座 4-白羊座 5-金牛座 6-双子座 7-巨蟹座 8-狮子座 9-处女座 10-天秤座 11-天蝎座 12-射手座
  // 前端数组索引对应：0-摩羯座 1-水瓶座 2-双鱼座 3-白羊座 4-金牛座 5-双子座 6-巨蟹座 7-狮子座 8-处女座 9-天秤座 10-天蝎座 11-射手座
  const constellationIndex = userInfo.constellation !== null && userInfo.constellation !== undefined
    ? (userInfo.constellation >= 1 && userInfo.constellation <= 12 ? userInfo.constellation - 1 : 0)
    : 0

  // 出生日期格式化：LocalDate -> YYYY-MM-DD
  let birthDateStr = ''
  if (userInfo.birthDate) {
    if (typeof userInfo.birthDate === 'string') {
      birthDateStr = userInfo.birthDate
    } else if (userInfo.birthDate.year && userInfo.birthDate.month && userInfo.birthDate.day) {
      const year = userInfo.birthDate.year
      const month = String(userInfo.birthDate.month).padStart(2, '0')
      const day = String(userInfo.birthDate.day).padStart(2, '0')
      birthDateStr = `${year}-${month}-${day}`
    }
  }

  // 教育经历映射
  const educationList = (userInfo.alumniEducationList || []).map(edu => {
    const rawSchoolName = edu.schoolInfo?.schoolName || edu.schoolName
    // 确保 schoolName 是有效字符串
    const schoolName = rawSchoolName ? String(rawSchoolName) : ''

    return {
      schoolInfo: edu.schoolInfo || null,
      schoolId: edu.schoolInfo?.schoolId || '',
      schoolName: schoolName,
      enrollmentYear: edu.enrollmentYear || null,
      graduationYear: edu.graduationYear || null,
      department: edu.department || '',
      major: edu.major || '',
      className: edu.className || '',
      educationLevel: edu.educationLevel || '',
      certificationStatus: edu.certificationStatus !== null && edu.certificationStatus !== undefined ? edu.certificationStatus : null
    }
  })

  // 处理头像URL，确保使用正确的 baseUrl
  const config = require('../../../utils/config.js')
  const rawAvatarUrl = userInfo.avatarUrl || ''
  const avatarUrl = rawAvatarUrl ? config.getImageUrl(rawAvatarUrl) : ''

  return {
    // 基础信息
    nickname: userInfo.nickname || '',
    name: userInfo.name || '',
    avatarUrl: avatarUrl,
    phone: userInfo.phone || '',
    wxNum: userInfo.wxNum || '',
    qqNum: userInfo.qqNum || '',
    email: userInfo.email || '',
    // 性别：0-未知，1-男，2-女
    gender: userInfo.gender !== null && userInfo.gender !== undefined ? userInfo.gender : 0,
    genderIndex: genderIndex,
    // 位置信息
    originProvince: userInfo.originProvince || '',
    curContinent: userInfo.curContinent || '',
    curCountry: userInfo.curCountry || '',
    curProvince: userInfo.curProvince || '',
    curCity: userInfo.curCity || '',
    curCounty: userInfo.curCounty || '',
    address: userInfo.address || '',
    latitude: userInfo.latitude ? String(userInfo.latitude) : '',
    longitude: userInfo.longitude ? String(userInfo.longitude) : '',
    // 其他信息
    constellation: userInfo.constellation !== null && userInfo.constellation !== undefined ? userInfo.constellation : null,
    constellationIndex: constellationIndex,
    signature: userInfo.signature || '',
    description: userInfo.description || '',
    // 证件信息
    identifyType: userInfo.identifyType !== null && userInfo.identifyType !== undefined ? userInfo.identifyType : 0,
    identifyTypeIndex: identifyTypeIndex,
    identifyCode: userInfo.identifyCode || '',
    birthDate: birthDateStr,
    // 教育经历
    educationList: educationList
  }
}

/**
 * 将前端表单数据映射为后端更新格式
 * 字段名称与后端 UpdateUserInfoDto 完全一致
 */
function mapFormToUpdateData(form) {
  const data = {
    // 基础信息
    nickname: form.nickname || null,
    name: form.name || null,
    avatarUrl: form.avatarUrl || null, // 确保 avatarUrl 被包含，即使可能为空
    phone: form.phone || null,
    wxNum: form.wxNum || null,
    qqNum: form.qqNum || null,
    email: form.email || null,
    // 性别：0-未知，1-男，2-女
    gender: form.gender !== null && form.gender !== undefined ? form.gender : null,
    // 位置信息
    originProvince: form.originProvince || null,
    curContinent: form.curContinent || null,
    curCountry: form.curCountry || null,
    curProvince: form.curProvince || null,
    curCity: form.curCity || null,
    curCounty: form.curCounty || null,
    address: form.address || null,
    latitude: form.latitude && form.latitude.trim() ? (isNaN(parseFloat(form.latitude)) ? null : parseFloat(form.latitude)) : null,
    longitude: form.longitude && form.longitude.trim() ? (isNaN(parseFloat(form.longitude)) ? null : parseFloat(form.longitude)) : null,
    // 其他信息
    constellation: form.constellation !== null && form.constellation !== undefined ? form.constellation : null,
    signature: form.signature || null,
    description: form.description || null,
    // 证件信息
    identifyType: form.identifyType !== null && form.identifyType !== undefined ? form.identifyType : null,
    identifyCode: form.identifyCode || null,
    birthDate: form.birthDate || null,
    // 教育经历
    // 过滤掉没有有效 schoolId 的教育经历（后端要求 schoolId 不能为 null）
    alumniEducationList: (form.educationList || [])
      .filter(edu => {
        // 必须有有效的 schoolId 才提交
        const schoolId = edu.schoolInfo?.schoolId || edu.schoolId
        return schoolId && schoolId !== '' && schoolId !== null && schoolId !== undefined
      })
      .map(edu => {
        const schoolId = edu.schoolInfo?.schoolId || edu.schoolId
        return {
          schoolId: schoolId, // 确保 schoolId 不为 null
          enrollmentYear: edu.enrollmentYear || null,
          graduationYear: edu.graduationYear || null,
          department: edu.department || null,
          major: edu.major || null,
          className: edu.className || null,
          educationLevel: edu.educationLevel || null,
          certificationStatus: edu.certificationStatus !== null && edu.certificationStatus !== undefined ? edu.certificationStatus : null
        }
      })
  }

  // 移除空字符串和 null 值，但保留 avatarUrl（即使为空字符串，也允许更新为空）
  // 注意：如果 avatarUrl 是空字符串，表示要清空头像，应该保留
  Object.keys(data).forEach(key => {
    // avatarUrl 特殊处理：空字符串也保留（表示清空头像）
    if (key === 'avatarUrl') {
      // avatarUrl 为 null 或 undefined 时才删除，空字符串保留
      if (data[key] === null || data[key] === undefined) {
        delete data[key]
      }
    } else {
      // 其他字段：空字符串和 null 都删除
      if (data[key] === '' || data[key] === null) {
        delete data[key]
      }
    }
  })

  return data
}

Page({
  data: {
    form: {
      // 基础信息
      nickname: '',
      name: '',
      avatarUrl: '',
      phone: '',
      wxNum: '',
      qqNum: '',
      email: '',
      // 性别：0-未知，1-男，2-女
      gender: 0,
      genderIndex: 0,
      // 位置信息
      originProvince: '',
      curContinent: '',
      curCountry: '',
      curProvince: '',
      curCity: '',
      curCounty: '',
      address: '',
      latitude: '',
      longitude: '',
      // 其他信息
      constellation: null,
      constellationIndex: 0,
      signature: '',
      description: '',
      // 证件信息
      identifyType: 0,
      identifyTypeIndex: 0,
      identifyCode: '',
      birthDate: '',
      // 教育经历列表
      educationList: []
    },
    saving: false,
    // 学历层次选项
    educationLevelOptions: ['小学', '初中', '高中', '中专', '大专', '本科', '硕士', '博士', '博士后'],
    // 学校搜索相关
    schoolSearchResults: {}, // { index: [schoolList] } 每个教育经历索引对应的搜索结果
    showSchoolDropdown: {}, // { index: true/false } 控制每个教育经历的下拉列表显示
    schoolSearchLoading: {}, // { index: true/false } 控制每个教育经历的搜索加载状态
    hasSchoolDropdownVisible: false, // 是否有任何下拉列表显示
    searchSchoolTimer: {}, // 搜索防抖定时器 { index: timerId }
    genderOptions: ['未知', '男', '女'],
    identifyTypeOptions: ['身份证', '护照'],
    // 星座选项：与后端数据库定义一致（1-摩羯座 2-水瓶座 3-双鱼座 4-白羊座 5-金牛座 6-双子座 7-巨蟹座 8-狮子座 9-处女座 10-天秤座 11-天蝎座 12-射手座）
    constellationOptions: ['摩羯座', '水瓶座', '双鱼座', '白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座', '天秤座', '天蝎座', '射手座']
  },

  async onLoad() {
    // 确保已登录后再加载数据
    await this.ensureLogin()
    this.loadProfile()
  },

  // 确保已登录
  async ensureLogin() {
    const isLogin = app.checkHasLogined()
    const token = wx.getStorageSync('token')
    
    if (!isLogin) {
      console.log('未登录，开始登录...')
      try {
        await app.initApp()
        console.log('登录成功，用户数据:', app.globalData.userData)
      } catch (error) {
        console.error('登录失败:', error)
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
        throw error
      }
    } else {
      // 检查本地存储中的 token，而不是 globalData.token
      console.log('已登录，token:', token ? '存在' : '不存在')
      if (!token) {
        console.warn('警告：checkHasLogined 返回 true，但本地存储中没有 token，可能需要重新登录')
      }
    }
  },


  // ==================== 数据加载逻辑 ====================
  
  /**
   * 加载用户资料
   * 联调时：只需要修改 loadFromApi 函数中的接口调用
   */
  async loadProfile() {
    try {
      let userInfo
      if (USE_MOCK_DATA) {
        // 使用假数据（从全局数据获取）
        userInfo = this.loadFromMock()
      } else {
        // 使用真实接口
        userInfo = await this.loadFromApi()
      }

      if (userInfo) {
        // 使用统一的数据映射函数
        const formData = mapUserInfoToForm(userInfo)
        this.setData({
          form: formData
        })
      }
    } catch (error) {
      console.error('加载用户资料失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  /**
   * 从假数据加载（联调时可删除此函数）
   */
  loadFromMock() {
    // 从全局数据获取用户信息
    const userData = app.globalData.userData || {}
    const globalInfo = app.globalData.userInfo || {}
    
    // 合并用户数据
    return {
      ...userData,
      ...globalInfo
    }
  },

  /**
   * 从真实接口加载
   * 联调时：只需要修改这里的接口调用
   */
  async loadFromApi() {
    const res = await userApi.getUserInfo()
    
    if (res.data && res.data.code === 200) {
      const userInfo = res.data.data || {}
      
      // 同步更新全局数据
      if (userInfo) {
        app.globalData.userData = {
          ...(app.globalData.userData || {}),
          ...userInfo
        }
        app.globalData.userInfo = {
          ...(app.globalData.userInfo || {}),
          ...userInfo
        }
      }
      
      return userInfo
    } else {
      wx.showToast({
        title: res.data?.msg || '加载失败',
        icon: 'none'
      })
      return null
    }
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleTextarea(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleGenderChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      'form.genderIndex': index,
      'form.gender': index // 0-未知，1-男，2-女
    })
  },

  handleIdentifyTypeChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      'form.identifyTypeIndex': index,
      'form.identifyType': index // 0-身份证，1-护照
    })
  },

  handleConstellationChange(e) {
    const index = Number(e.detail.value)
    // 前端索引0-11 -> 后端值1-12（与后端数据库定义一致）
    this.setData({
      'form.constellationIndex': index,
      'form.constellation': index + 1
    })
  },

  handleBirthDateChange(e) {
    this.setData({
      'form.birthDate': e.detail.value
    })
  },

  /**
   * 选择并上传头像
   * 使用统一的文件上传工具 fileApi.uploadImage
   */
  async chooseAvatar() {
    try {
      // 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          success: resolve,
          fail: reject
        })
      })

      const tempFilePath = chooseRes.tempFiles?.[0]?.tempFilePath
      if (!tempFilePath) {
        return
      }

      // 检查文件大小（10MB = 2 * 1024 * 1024 字节）
      const fileSize = chooseRes.tempFiles?.[0]?.size || 0
      const maxSize = 10 * 1024 * 1024 // 10MB
      if (fileSize > maxSize) {
        wx.showToast({
          title: '图片大小不能超过10MB',
          icon: 'none'
        })
        return
      }

      // 显示上传中提示
      wx.showLoading({
        title: '上传中...',
        mask: true
      })

      // 获取原始文件名（如果有）
      const originalName = chooseRes.tempFiles?.[0]?.name || 'avatar.jpg'

      // 直接调用公共的文件上传方法
      const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

      if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
        // 获取返回的图片URL
        const rawImageUrl = uploadRes.data.fileUrl || ''
        if (rawImageUrl) {
          // 使用 config.getImageUrl 处理图片URL，确保是完整的URL
          const config = require('../../../utils/config.js')
          const imageUrl = config.getImageUrl(rawImageUrl)
          // 更新表单中的头像URL
          this.setData({ 'form.avatarUrl': imageUrl })
          wx.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          wx.showToast({
            title: '上传失败，未获取到图片地址',
            icon: 'none'
          })
        }
      } else {
        wx.showToast({
          title: uploadRes?.msg || '上传失败',
          icon: 'none'
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000
      })
    } finally {
      wx.hideLoading()
    }
  },

  validateForm() {
    const { phone, email, identifyCode, educationList } = this.data.form
    
    // 验证手机号
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return false
    }
    
    // 验证邮箱
    if (email && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
      wx.showToast({ title: '邮箱格式不正确', icon: 'none' })
      return false
    }
    
    // 验证证件号
    if (identifyCode) {
      const { identifyType } = this.data.form
      if (identifyType === 0) {
        // 身份证验证
        if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(identifyCode)) {
          wx.showToast({ title: '身份证号格式不正确', icon: 'none' })
          return false
        }
      } else if (identifyType === 1) {
        // 护照验证（简单验证，可根据实际需求调整）
        if (identifyCode.length < 6 || identifyCode.length > 20) {
          wx.showToast({ title: '护照号格式不正确', icon: 'none' })
          return false
        }
      }
    }
    
    // 验证教育经历中的学校
    if (educationList && educationList.length > 0) {
      for (let i = 0; i < educationList.length; i++) {
        const edu = educationList[i]
        const schoolName = (edu.schoolName || '').trim()
        const schoolId = edu.schoolInfo?.schoolId || edu.schoolId
        
        // 如果填写了学校名称，必须有有效的 schoolId（说明是从数据库中选择的）
        if (schoolName && (!schoolId || schoolId === '' || schoolId === null || schoolId === undefined)) {
          wx.showToast({ 
            title: '没有该学校，请从下拉列表中选择', 
            icon: 'none',
            duration: 3000
          })
          return false
        }
      }
    }
    
    return true
  },

  // ==================== 数据保存逻辑 ====================
  
  /**
   * 保存用户资料
   * 联调时：只需要修改 saveToApi 函数中的接口调用
   */
  async saveProfile() {
    if (!this.validateForm()) return
    
    this.setData({ saving: true })

    try {
      let success = false
      if (USE_MOCK_DATA) {
        // 使用假数据（保存到全局数据）
        success = this.saveToMock()
      } else {
        // 使用真实接口
        success = await this.saveToApi()
      }

      if (success) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        this.setData({ saving: false })
        setTimeout(() => {
          wx.navigateBack()
        }, 600)
      } else {
        this.setData({ saving: false })
      }
    } catch (error) {
      console.error('保存用户资料失败:', error)
      this.setData({ saving: false })
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
    }
  },

  /**
   * 保存到假数据（联调时可删除此函数）
   */
  saveToMock() {
    app.globalData.userInfo = {
      ...(app.globalData.userInfo || {}),
      ...this.data.form
    }
    return true
  },

  /**
   * 保存到真实接口
   * 联调时：只需要修改这里的接口调用和参数构建
   */
  async saveToApi() {
    // 使用统一的数据映射函数
    const updateData = mapFormToUpdateData(this.data.form)

    const res = await userApi.updateUserInfo(updateData)
    
    if (res.data && res.data.code === 200) {
      // 保存成功后，重新从后端加载最新数据，确保获取到完整的用户信息（包括头像）
      // 因为后端更新接口返回的 data 是 null，所以需要重新加载
      try {
        const latestUserInfo = await this.loadFromApi()
        if (latestUserInfo) {
          // 使用最新数据更新全局数据
          app.globalData.userData = {
            ...(app.globalData.userData || {}),
            ...latestUserInfo
          }
          app.globalData.userInfo = {
            ...(app.globalData.userInfo || {}),
            ...latestUserInfo
          }
          
          // 同时更新当前页面的表单数据，确保头像等字段显示正确
          const formData = mapUserInfoToForm(latestUserInfo)
          // genderIndex 已经在 mapUserInfoToForm 中计算好了
          this.setData({
            form: formData
          })
        } else {
          // 如果重新加载失败，至少使用当前表单数据更新全局数据
          app.globalData.userData = {
            ...(app.globalData.userData || {}),
            ...this.data.form
          }
          app.globalData.userInfo = {
            ...(app.globalData.userInfo || {}),
            ...this.data.form
          }
        }
      } catch (error) {
        // 重新加载失败时，使用当前表单数据更新全局数据
        app.globalData.userData = {
          ...(app.globalData.userData || {}),
          ...this.data.form
        }
        app.globalData.userInfo = {
          ...(app.globalData.userInfo || {}),
          ...this.data.form
        }
      }
      
      return true
    } else {
      wx.showToast({
        title: res.data?.msg || '保存失败',
        icon: 'none'
      })
      return false
    }
  },

  // ==================== 教育经历相关方法 ====================

  // 添加教育经历
  addEducation() {
    const educationList = this.data.form.educationList || []
    const newItem = {
      schoolInfo: null,
      schoolId: '',
      schoolName: '',
      enrollmentYear: null,
      graduationYear: null,
      department: '',
      major: '',
      className: '',
      educationLevel: '',
      certificationStatus: null
    }
    educationList.push(newItem)
    this.setData({
      'form.educationList': educationList
    })
  },

  // 删除教育经历
  removeEducation(e) {
    const { index } = e.currentTarget.dataset
    const indexNum = parseInt(index)
    if (isNaN(indexNum) || indexNum < 0) {
      console.error('删除教育经历失败：索引无效', index)
      return
    }
    const educationList = this.data.form.educationList || []
    if (indexNum >= 0 && indexNum < educationList.length) {
      educationList.splice(indexNum, 1)
      // 同时清理相关的搜索数据
      const showSchoolDropdown = { ...this.data.showSchoolDropdown }
      const schoolSearchResults = { ...this.data.schoolSearchResults }
      const schoolSearchLoading = { ...this.data.schoolSearchLoading }
      delete showSchoolDropdown[indexNum]
      delete schoolSearchResults[indexNum]
      delete schoolSearchLoading[indexNum]
      
      // 重新索引（因为删除了一个元素，后面的索引需要减1）
      const newShowSchoolDropdown = {}
      const newSchoolSearchResults = {}
      const newSchoolSearchLoading = {}
      Object.keys(showSchoolDropdown).forEach(key => {
        const keyNum = parseInt(key)
        if (keyNum > indexNum) {
          newShowSchoolDropdown[keyNum - 1] = showSchoolDropdown[key]
        } else if (keyNum < indexNum) {
          newShowSchoolDropdown[keyNum] = showSchoolDropdown[key]
        }
      })
      Object.keys(schoolSearchResults).forEach(key => {
        const keyNum = parseInt(key)
        if (keyNum > indexNum) {
          newSchoolSearchResults[keyNum - 1] = schoolSearchResults[key]
        } else if (keyNum < indexNum) {
          newSchoolSearchResults[keyNum] = schoolSearchResults[key]
        }
      })
      Object.keys(schoolSearchLoading).forEach(key => {
        const keyNum = parseInt(key)
        if (keyNum > indexNum) {
          newSchoolSearchLoading[keyNum - 1] = schoolSearchLoading[key]
        } else if (keyNum < indexNum) {
          newSchoolSearchLoading[keyNum] = schoolSearchLoading[key]
        }
      })
      
      const hasVisible = Object.values(newShowSchoolDropdown).some(v => v === true)
      this.setData({
        'form.educationList': educationList,
        showSchoolDropdown: newShowSchoolDropdown,
        schoolSearchResults: newSchoolSearchResults,
        schoolSearchLoading: newSchoolSearchLoading,
        hasSchoolDropdownVisible: hasVisible
      })
    }
  },

  // 搜索学校（输入框输入时触发）- 完全重构，使用微信小程序官方推荐方式
  searchSchool(e) {
    const { index } = e.currentTarget.dataset
    const indexNum = parseInt(index)

    if (isNaN(indexNum)) {
      console.error('搜索学校失败：索引无效', index)
      return
    }

    // 获取输入值，直接使用 e.detail.value（与 handleInput 方法保持一致）
    const inputValue = e.detail.value || ''
    const safeInputValue = String(inputValue)

    // 使用与 handleEducationInput 完全相同的方式更新数据
    // 这是微信小程序官方推荐的方式，兼容性最好
    const educationList = this.data.form.educationList || []
    
    // 确保数组项存在
    if (!educationList[indexNum]) {
      educationList[indexNum] = {
        schoolInfo: null,
        schoolId: '',
        schoolName: '',
        enrollmentYear: null,
        graduationYear: null,
        department: '',
        major: '',
        className: '',
        educationLevel: '',
        certificationStatus: null
      }
    }
    
    // 获取当前已有的学校信息
    const currentItem = educationList[indexNum]
    const originalSchoolName = currentItem.schoolInfo?.schoolName || currentItem.schoolName || ''
    
    // 检查新输入的值是否与原来的学校名称匹配
    // 如果不匹配，说明用户手动修改了学校名称，需要清除 schoolInfo 和 schoolId
    const isSchoolNameChanged = safeInputValue.trim() !== originalSchoolName.trim()
    
    // 更新 schoolName，如果学校名称被修改，清除 schoolInfo 和 schoolId
    educationList[indexNum] = {
      ...educationList[indexNum],
      schoolName: safeInputValue,
      // 如果学校名称被修改，清除关联的学校信息
      ...(isSchoolNameChanged ? {
        schoolInfo: null,
        schoolId: ''
      } : {})
    }
    
    // 整体更新数组，确保数据正确响应
    this.setData({
      'form.educationList': educationList
    })

    // 处理搜索逻辑（使用防抖优化）
    const keyword = safeInputValue.trim()
    
    // 清除之前的搜索定时器
    if (this.searchSchoolTimer && this.searchSchoolTimer[indexNum]) {
      clearTimeout(this.searchSchoolTimer[indexNum])
    }
    
    // 如果关键词为空，隐藏下拉列表
    if (!keyword) {
      const showSchoolDropdown = { ...this.data.showSchoolDropdown }
      showSchoolDropdown[indexNum] = false
      const hasVisible = Object.values(showSchoolDropdown).some(v => v === true)
      this.setData({
        [`showSchoolDropdown.${indexNum}`]: false,
        [`schoolSearchResults.${indexNum}`]: [],
        hasSchoolDropdownVisible: hasVisible
      })
      return
    }

    // 显示加载状态
    this.setData({
      [`schoolSearchLoading.${indexNum}`]: true,
      [`showSchoolDropdown.${indexNum}`]: true,
      hasSchoolDropdownVisible: true
    })

    // 防抖搜索：300ms 后执行搜索
    if (!this.searchSchoolTimer) {
      this.searchSchoolTimer = {}
    }
    this.searchSchoolTimer[indexNum] = setTimeout(() => {
      this.performSchoolSearch(indexNum, keyword)
    }, 300)
  },

  // 执行学校搜索（通用方法）- 完全重构
  async performSchoolSearch(indexNum, keyword) {
    if (!keyword || keyword.trim() === '') {
      this.setData({
        [`schoolSearchResults.${indexNum}`]: [],
        [`schoolSearchLoading.${indexNum}`]: false
      })
      return
    }

    try {
      // 调用 API：使用 schoolApi.getSchoolPage
      const params = {
        current: 1,
        pageSize: 10,
        schoolName: keyword.trim()
      }
      const res = await schoolApi.getSchoolPage(params)
      
      if (res.data && res.data.code === 200) {
        const responseData = res.data.data || {}
        const list = responseData.records || responseData.list || []
        
        // 映射学校数据
        const schoolList = list.map(item => ({
          schoolId: item.schoolId || '',
          schoolName: item.schoolName || '',
          logo: item.logo || null,
          province: item.province || null,
          city: item.city || null,
          level: item.level || null,
          foundingDate: item.foundingDate || null,
          officialCertification: item.officialCertification !== null && item.officialCertification !== undefined ? item.officialCertification : null
        }))

        this.setData({
          [`schoolSearchResults.${indexNum}`]: schoolList,
          [`schoolSearchLoading.${indexNum}`]: false
        })
      } else {
        this.setData({
          [`schoolSearchResults.${indexNum}`]: [],
          [`schoolSearchLoading.${indexNum}`]: false
        })
      }
    } catch (error) {
      console.error('搜索学校失败:', error)
      this.setData({
        [`schoolSearchResults.${indexNum}`]: [],
        [`schoolSearchLoading.${indexNum}`]: false
      })
    }
  },

  // 选择学校（点击下拉列表项）- 完全重构
  selectSchool(e) {
    const { index, schoolIndex } = e.currentTarget.dataset
    const indexNum = parseInt(index)
    const schoolIndexNum = parseInt(schoolIndex)
    
    if (isNaN(indexNum) || isNaN(schoolIndexNum)) {
      console.error('选择学校失败：索引无效', index, schoolIndex)
      return
    }
    
    const schoolSearchResults = this.data.schoolSearchResults || {}
    const schoolList = schoolSearchResults[indexNum] || []
    const school = schoolList[schoolIndexNum]

    if (!school) {
      return
    }

    // 确保 schoolName 是有效字符串
    const schoolName = school.schoolName ? String(school.schoolName) : ''

    // 使用与 handleEducationInput 完全相同的方式更新数据
    const educationList = this.data.form.educationList || []
    
    if (!educationList[indexNum]) {
      educationList[indexNum] = {
        schoolInfo: null,
        schoolId: '',
        schoolName: '',
        enrollmentYear: null,
        graduationYear: null,
        department: '',
        major: '',
        className: '',
        educationLevel: '',
        certificationStatus: null
      }
    }
    
    educationList[indexNum] = {
      ...educationList[indexNum],
      schoolInfo: {
        schoolId: school.schoolId || '',
        logo: school.logo || null,
        schoolName: schoolName,
        province: school.province || null,
        city: school.city || null,
        level: school.level || null,
        foundingDate: school.foundingDate || null,
        officialCertification: school.officialCertification !== null && school.officialCertification !== undefined ? school.officialCertification : null
      },
      schoolId: school.schoolId || '',
      schoolName: schoolName
    }
    
    // 更新数据并隐藏下拉列表
    const showSchoolDropdown = { ...this.data.showSchoolDropdown }
    showSchoolDropdown[indexNum] = false
    const hasVisible = Object.values(showSchoolDropdown).some(v => v === true)
    
    this.setData({
      'form.educationList': educationList,
      [`showSchoolDropdown.${indexNum}`]: false,
      [`schoolSearchResults.${indexNum}`]: [],
      hasSchoolDropdownVisible: hasVisible
    })
  },

  // 学校输入框聚焦时触发（显示下拉列表，如果有输入内容）- 完全重构
  onSchoolFocus(e) {
    const { index } = e.currentTarget.dataset
    const indexNum = parseInt(index)
    
    if (isNaN(indexNum)) {
      return
    }
    
    // 获取当前输入的值
    const educationList = this.data.form.educationList || []
    const currentItem = educationList[indexNum]
    
    if (!currentItem) {
      return
    }
    
    const currentValue = currentItem.schoolName || ''
    const keyword = String(currentValue).trim()
    
    // 如果有输入内容，显示搜索下拉框并触发搜索
    if (keyword && keyword.length > 0) {
      this.setData({
        [`schoolSearchLoading.${indexNum}`]: true,
        [`showSchoolDropdown.${indexNum}`]: true,
        hasSchoolDropdownVisible: true
      })
      
      // 执行搜索
      this.performSchoolSearch(indexNum, keyword)
    }
  },

  // 隐藏学校下拉列表
  hideSchoolDropdown() {
    const showSchoolDropdown = this.data.showSchoolDropdown || {}
    const newShowSchoolDropdown = {}
    Object.keys(showSchoolDropdown).forEach(key => {
      newShowSchoolDropdown[key] = false
    })
    this.setData({
      showSchoolDropdown: newShowSchoolDropdown,
      hasSchoolDropdownVisible: false
    })
  },

  // 教育经历输入
  handleEducationInput(e) {
    const { index, field } = e.currentTarget.dataset
    const { value } = e.detail
    const educationList = this.data.form.educationList || []
    educationList[index] = {
      ...educationList[index],
      [field]: value
    }
    this.setData({
      'form.educationList': educationList
    })
  },

  // 教育经历年份输入
  handleEducationYearInput(e) {
    const { index, field } = e.currentTarget.dataset
    const value = e.detail.value ? parseInt(e.detail.value) : null
    const educationList = this.data.form.educationList || []
    educationList[index] = {
      ...educationList[index],
      [field]: value
    }
    this.setData({
      'form.educationList': educationList
    })
  },

  // 学历层次选择
  handleEducationLevelChange(e) {
    const { index } = e.currentTarget.dataset
    const levelIndex = Number(e.detail.value)
    const educationList = this.data.form.educationList || []
    educationList[index] = {
      ...educationList[index],
      educationLevel: this.data.educationLevelOptions[levelIndex] || ''
    }
    this.setData({
      'form.educationList': educationList
    })
  },

  // 认证状态选择
  handleEducationCertificationChange(e) {
    const { index } = e.currentTarget.dataset
    const statusIndex = Number(e.detail.value)
    const educationList = this.data.form.educationList || []
    educationList[index] = {
      ...educationList[index],
      certificationStatus: statusIndex === 1 ? 1 : 0
    }
    this.setData({
      'form.educationList': educationList
    })
  },

})



