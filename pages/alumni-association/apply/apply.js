// pages/alumni-association/apply/apply.js
const { associationApi, fileApi, userApi } = require('../../../api/api.js')

Page({
  data: {
    alumniAssociationId: '', // 校友会ID
    schoolId: '', // 学校ID（从详情页获取，不展示）
    schoolName: '', // 学校名称（从详情页获取，仅展示）
    formData: {
      name: '',
      identifyCode: '',
      phone: '',
      applicationReason: '',
      enrollmentYear: '',
      graduationYear: '',
      department: '',
      major: '',
      className: '',
      educationLevel: ''
    },
    attachments: [], // 附件列表 {url: '', fileId: ''}
    educationLevels: ['专科', '本科', '硕士', '博士'],
    educationLevelIndex: -1,
    submitting: false,
    loadingUserInfo: false
  },

  async onLoad(options) {
    // 获取校友会ID和学校信息
    if (options.id) {
      this.setData({
        alumniAssociationId: options.id
      })
    }
    if (options.schoolId) {
      this.setData({
        schoolId: options.schoolId
      })
    }
    if (options.schoolName) {
      this.setData({
        schoolName: decodeURIComponent(options.schoolName)
      })
    }

    // 加载用户信息并填充表单
    await this.loadUserInfo()
  },

  // 加载用户信息
  async loadUserInfo() {
    if (this.data.loadingUserInfo) return

    this.setData({ loadingUserInfo: true })

    try {
      const res = await userApi.getUserInfo()

      if (res.data && res.data.code === 200) {
        const userInfo = res.data.data || {}

        // 填充基本信息
        const formData = {
          name: userInfo.name || '',
          identifyCode: userInfo.identifyCode || '',
          phone: userInfo.phone || '',
          applicationReason: '',
          enrollmentYear: '',
          graduationYear: '',
          department: '',
          major: '',
          className: '',
          educationLevel: ''
        }

        // 只有当详情页传入的 schoolId 与用户教育经历中的 schoolId 匹配时，才填充教育经历信息
        const { schoolId } = this.data
        if (schoolId && userInfo.alumniEducationList && userInfo.alumniEducationList.length > 0) {
          // 查找匹配的教育经历
          const matchedEducation = userInfo.alumniEducationList.find(edu => {
            const eduSchoolId = edu.schoolInfo?.schoolId || edu.schoolId
            return eduSchoolId && String(eduSchoolId) === String(schoolId)
          })

          // 如果找到匹配的教育经历，则填充
          if (matchedEducation) {
            formData.enrollmentYear = matchedEducation.enrollmentYear || ''
            formData.graduationYear = matchedEducation.graduationYear || ''
            formData.department = matchedEducation.department || ''
            formData.major = matchedEducation.major || ''
            formData.className = matchedEducation.className || ''
            formData.educationLevel = matchedEducation.educationLevel || ''

            // 设置学历层次的选择器索引
            if (matchedEducation.educationLevel) {
              const educationLevelIndex = this.data.educationLevels.indexOf(matchedEducation.educationLevel)
              if (educationLevelIndex !== -1) {
                this.setData({ educationLevelIndex })
              }
            }
          }
        }

        this.setData({
          formData,
          loadingUserInfo: false
        })
      } else {
        this.setData({ loadingUserInfo: false })
        console.warn('获取用户信息失败:', res.data?.message)
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
      this.setData({ loadingUserInfo: false })
    }
  },

  // 处理输入
  handleInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`formData.${field}`]: value
    })
  },

  // 处理年份选择
  handleYearChange(e) {
    const { field } = e.currentTarget.dataset
    const year = e.detail.value.split('-')[0] // 获取年份部分
    this.setData({
      [`formData.${field}`]: parseInt(year)
    })
  },

  // 处理学历层次选择
  handleEducationLevelChange(e) {
    const index = e.detail.value
    this.setData({
      educationLevelIndex: index,
      'formData.educationLevel': this.data.educationLevels[index]
    })
  },

  // 选择图片
  chooseImage() {
    const that = this
    wx.chooseMedia({
      count: 3 - this.data.attachments.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFiles = res.tempFiles
        wx.showLoading({ title: '上传中...', mask: true })

        // 逐个上传图片
        const uploadPromises = tempFiles.map(file => {
          return that.uploadImage(file.tempFilePath)
        })

        Promise.all(uploadPromises)
          .then(results => {
            const newAttachments = results.map(result => ({
              url: result.url,
              fileId: result.fileId
            }))
            that.setData({
              attachments: [...that.data.attachments, ...newAttachments]
            })
            wx.hideLoading()
            wx.showToast({
              title: '上传成功',
              icon: 'success'
            })
          })
          .catch(error => {
            console.error('上传失败:', error)
            wx.hideLoading()
            wx.showToast({
              title: '上传失败，请重试',
              icon: 'none'
            })
          })
      }
    })
  },

  // 上传图片到服务器
  uploadImage(filePath) {
    return new Promise((resolve, reject) => {
      fileApi.uploadImage(filePath, 'attachment.jpg')
        .then(res => {
          if (res.data && res.data.code === 200) {
            resolve({
              url: res.data.data.url || filePath,
              fileId: res.data.data.id
            })
          } else {
            reject(new Error('上传失败'))
          }
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 删除附件
  deleteAttachment(e) {
    const { index } = e.currentTarget.dataset
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },

  // 表单验证
  validateForm() {
    const { formData } = this.data

    if (!formData.name || !formData.name.trim()) {
      wx.showToast({
        title: '请输入真实姓名',
        icon: 'none'
      })
      return false
    }

    if (!formData.identifyCode || !formData.identifyCode.trim()) {
      wx.showToast({
        title: '请输入身份证号',
        icon: 'none'
      })
      return false
    }

    // 验证身份证号格式
    const idCardReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    if (!idCardReg.test(formData.identifyCode)) {
      wx.showToast({
        title: '身份证号格式不正确',
        icon: 'none'
      })
      return false
    }

    // 如果填写了手机号，验证手机号格式
    if (formData.phone && formData.phone.trim()) {
      const phoneReg = /^1[3-9]\d{9}$/
      if (!phoneReg.test(formData.phone)) {
        wx.showToast({
          title: '手机号格式不正确',
          icon: 'none'
        })
        return false
      }
    }

    // 验证入学和毕业年份的合理性
    if (formData.enrollmentYear && formData.graduationYear) {
      if (formData.enrollmentYear >= formData.graduationYear) {
        wx.showToast({
          title: '毕业年份应晚于入学年份',
          icon: 'none'
        })
        return false
      }
    }

    return true
  },

  // 提交申请
  async submitApplication() {
    if (this.data.submitting) return

    // 验证表单
    if (!this.validateForm()) {
      return
    }

    const { formData, attachments, alumniAssociationId, schoolId } = this.data

    // 构建请求数据
    const requestData = {
      alumniAssociationId: parseInt(alumniAssociationId),
      name: formData.name.trim(),
      identifyCode: formData.identifyCode.trim(),
      phone: formData.phone ? formData.phone.trim() : undefined,
      applicationReason: formData.applicationReason ? formData.applicationReason.trim() : undefined,
      attachmentIds: attachments.length > 0 ? attachments.map(item => item.fileId) : undefined,
      schoolId: schoolId ? parseInt(schoolId) : undefined,
      enrollmentYear: formData.enrollmentYear || undefined,
      graduationYear: formData.graduationYear || undefined,
      department: formData.department ? formData.department.trim() : undefined,
      major: formData.major ? formData.major.trim() : undefined,
      className: formData.className ? formData.className.trim() : undefined,
      educationLevel: formData.educationLevel || undefined
    }

    // 移除 undefined 值
    Object.keys(requestData).forEach(key => {
      if (requestData[key] === undefined) {
        delete requestData[key]
      }
    })

    this.setData({ submitting: true })

    try {
      const res = await associationApi.applyToJoinAssociation(requestData)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '申请提交成功，请等待审核',
          icon: 'success',
          duration: 2000
        })

        // 延迟返回上一页
        setTimeout(() => {
          wx.navigateBack()
        }, 2000)
      } else {
        wx.showToast({
          title: res.data?.message || '申请提交失败',
          icon: 'none'
        })
        this.setData({ submitting: false })
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      wx.showToast({
        title: '申请提交失败，请重试',
        icon: 'none'
      })
      this.setData({ submitting: false })
    }
  }
})
