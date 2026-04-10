// pages/alumni-association/apply/apply.js
const { associationApi, fileApi, userApi, myApplicationRecordApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const auth = require('../../../utils/auth.js')

Page({
  data: {
    mode: 'edit', // 'edit' 或 'view'，默认为编辑模式
    isEditingApplication: false, // 是否正在编辑已有申请
    applicationId: '', // 申请ID（编辑模式下使用）
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
      educationLevel: '',
    },
    attachments: [], // 附件列表 {url: '', fileId: ''}
    educationLevels: ['专科', '本科', '硕士', '博士'],
    educationLevelIndex: -1,
    // 年份选择器相关
    yearRanges: [[], []], // [入学年份列表, 毕业年份列表]
    yearPickerIndex: [0, 0], // 当前选中的索引
    submitting: false,
    loadingUserInfo: false,
    applicationDetail: null, // 申请详情
    loadingDetail: false,
    fromMyRecord: false,
    myRecordType: 'ALUMNI_ASSOCIATION_JOIN',
    myRecordId: '',
    editableAssociationList: [],
    editableAssociationIndex: -1,
    headerImageUrl: `https://${config.DOMAIN}/upload/images/2026/02/09/9f328fe3-fcad-4019-a379-1a6db70f3a5d.png`,
  },

  async onLoad(options) {
    options = this.normalizeLoadOptions(options)

    // 保存页面参数，用于注册后返回
    this.pageOptions = options

    // 检查用户是否已完成注册，未注册则跳转到注册页
    const isProfileComplete = auth.checkProfileComplete()
    if (!isProfileComplete) {
      console.log('[Apply] 用户未完成注册，跳转到注册页')
      // 保存当前页面路径和参数到缓存，注册完成后可以跳转回来
      const currentPath = '/pages/alumni-association/apply/apply'
      const queryString = Object.keys(options)
        .map(key => `${key}=${encodeURIComponent(options[key])}`)
        .join('&')
      wx.setStorageSync('redirectAfterRegister', `${currentPath}?${queryString}`)

      wx.redirectTo({
        url: '/pages/register/register',
        fail: () => {
          console.log('[Apply] 跳转注册页失败')
        },
      })
      return
    }

    // 初始化年份选择器
    this.initYearRanges()

    // 获取校友会ID和学校信息
    if (options.id) {
      this.setData({
        alumniAssociationId: options.id,
      })
    }

    // 判断是编辑模式还是查看模式
    const mode = options.mode || 'edit'
    this.setData({
      mode,
      fromMyRecord: options.fromMyRecord === '1',
      myRecordType: decodeURIComponent(options.recordType || 'ALUMNI_ASSOCIATION_JOIN'),
      myRecordId: decodeURIComponent(options.recordId || ''),
    })

    if (this.data.fromMyRecord) {
      await this.loadApplicationDetail()
      if (mode === 'edit') {
        this.setData({ isEditingApplication: true })
        await this.loadEditableAssociationList()
        this.syncEditableAssociationSelection()
      }
      return
    }

    if (mode === 'view') {
      // 查看模式：加载申请详情
      await this.loadApplicationDetail()
    } else {
      // 编辑模式：加载学校信息和用户信息
      if (options.schoolId) {
        this.setData({
          schoolId: options.schoolId,
        })
      }
      if (options.schoolName) {
        this.setData({
          schoolName: decodeURIComponent(options.schoolName),
        })
      }

      // 如果 schoolId 缺失，从校友会详情接口获取
      if (!this.data.schoolId && this.data.alumniAssociationId) {
        await this.fetchSchoolIdFromAssociation()
      }

      // 加载用户信息并填充表单
      await this.loadUserInfo()
    }
  },

  normalizeLoadOptions(rawOptions) {
    const options = rawOptions || {}
    const decodeSafely = value => {
      if (!value) return ''
      let current = String(value)
      for (let i = 0; i < 2; i += 1) {
        try {
          const decoded = decodeURIComponent(current)
          if (decoded === current) break
          current = decoded
        } catch (error) {
          break
        }
      }
      return current
    }

    const parseKvString = value => {
      const result = {}
      if (!value) return result
      const text = decodeSafely(value).trim()
      if (!text) return result

      if (!text.includes('=') && /^\d+$/.test(text)) {
        result.id = text
        return result
      }

      text.split('&').forEach(pair => {
        const idx = pair.indexOf('=')
        if (idx <= 0) return
        const key = pair.slice(0, idx)
        const val = pair.slice(idx + 1)
        if (!key) return
        result[key] = decodeSafely(val)
      })
      return result
    }

    const sceneParams = parseKvString(options.scene)

    let qParams = {}
    if (options.q) {
      const decodedQ = decodeSafely(options.q)
      const queryIndex = decodedQ.indexOf('?')
      if (queryIndex >= 0) {
        qParams = parseKvString(decodedQ.slice(queryIndex + 1))
      }
    }

    const idFromParams =
      options.id ||
      options.alumniAssociationId ||
      options.associationId ||
      sceneParams.id ||
      sceneParams.alumniAssociationId ||
      sceneParams.associationId ||
      qParams.id ||
      qParams.alumniAssociationId ||
      qParams.associationId ||
      ''

    const schoolIdFromParams = options.schoolId || sceneParams.schoolId || qParams.schoolId || ''
    const schoolNameFromParams = options.schoolName || sceneParams.schoolName || qParams.schoolName || ''

    return {
      ...options,
      id: idFromParams ? String(idFromParams) : '',
      schoolId: schoolIdFromParams ? String(schoolIdFromParams) : '',
      schoolName: schoolNameFromParams || '',
    }
  },

  // 加载编辑模式可选的校友会列表（仅我的申请编辑使用）
  async loadEditableAssociationList() {
    try {
      const res = await associationApi.getAssociationList({
        current: 1,
        pageSize: 200,
        associationName: '',
      })
      if (!(res.data && res.data.code === 200 && res.data.data)) {
        this.setData({ editableAssociationList: [], editableAssociationIndex: -1 })
        return
      }
      const records = Array.isArray(res.data.data.records) ? res.data.data.records : []
      const editableAssociationList = records
        .map(item => {
          const associationId =
            item.alumniAssociationId || item.associationId || item.id || item.alumni_association_id
          const associationName = item.associationName || item.name || ''
          const schoolInfo = item.schoolInfo || {}
          const schoolId = schoolInfo.schoolId || item.schoolId || ''
          const schoolName = schoolInfo.schoolName || item.schoolName || ''
          if (!associationId || !associationName) return null
          return {
            associationId: String(associationId),
            associationName,
            schoolId: schoolId ? String(schoolId) : '',
            schoolName,
          }
        })
        .filter(Boolean)
      this.setData({ editableAssociationList })
    } catch (error) {
      console.error('[Apply] loadEditableAssociationList', error)
      this.setData({ editableAssociationList: [], editableAssociationIndex: -1 })
    }
  },

  syncEditableAssociationSelection() {
    const { editableAssociationList, alumniAssociationId } = this.data
    if (!editableAssociationList.length || !alumniAssociationId) {
      this.setData({ editableAssociationIndex: -1 })
      return
    }
    const idx = editableAssociationList.findIndex(
      item => String(item.associationId) === String(alumniAssociationId)
    )
    this.setData({ editableAssociationIndex: idx >= 0 ? idx : -1 })
  },

  async handleAssociationChange(e) {
    const index = Number(e.detail.value)
    const selected = this.data.editableAssociationList[index]
    if (!selected) return
    this.setData({
      editableAssociationIndex: index,
      alumniAssociationId: selected.associationId,
      schoolId: selected.schoolId || '',
      schoolName: selected.schoolName || '',
    })
    if (!selected.schoolId) {
      await this.fetchSchoolIdFromAssociation()
    }
  },

  // 从校友会详情接口获取 schoolId（当页面参数中未传入时调用）
  async fetchSchoolIdFromAssociation() {
    try {
      const res = await associationApi.getAssociationDetail(this.data.alumniAssociationId)
      if (res.data && res.data.code === 200 && res.data.data) {
        const detail = res.data.data
        const schoolInfo = detail.schoolInfo || {}
        const schoolId = schoolInfo.schoolId || detail.schoolId || ''
        const schoolName = schoolInfo.schoolName || detail.schoolName || ''
        if (schoolId) {
          this.setData({ schoolId: String(schoolId), schoolName })
          console.log('[Apply] 从校友会详情获取到 schoolId:', schoolId)
        } else {
          console.warn('[Apply] 校友会未关联学校，schoolId 为空')
        }
      }
    } catch (err) {
      console.error('[Apply] 获取校友会详情失败:', err)
    }
  },

  // 加载用户信息
  async loadUserInfo() {
    if (this.data.loadingUserInfo) {
      return
    }

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
          educationLevel: '',
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
              const educationLevelIndex = this.data.educationLevels.indexOf(
                matchedEducation.educationLevel
              )
              if (educationLevelIndex !== -1) {
                this.setData({ educationLevelIndex })
              }
            }
          }
        }

        this.setData({
          formData,
          loadingUserInfo: false,
        })

        // 更新年份选择器索引
        this.updateYearPickerIndex()
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
      [`formData.${field}`]: value,
    })
  },

  // 初始化年份选择器
  initYearRanges() {
    const currentYear = new Date().getFullYear()
    const startYear = 1950
    const endYear = currentYear + 4 // 允许选择未来4年（方便在读学生）

    // 生成年份列表（降序，从当前年份+4开始）
    const years = []
    for (let i = endYear; i >= startYear; i--) {
      years.push(i + '年')
    }

    // 默认选中：入学年份为当前年份-4，毕业年份为当前年份
    const defaultEnrollmentIndex = endYear - (currentYear - 4) // 当前年份-4在降序列表中的索引
    const defaultGraduationIndex = endYear - currentYear // 当前年份在降序列表中的索引

    this.setData({
      yearRanges: [years, years],
      yearPickerIndex: [defaultEnrollmentIndex, defaultGraduationIndex],
    })
  },

  // 更新年份选择器索引（根据已有数据）
  updateYearPickerIndex() {
    const { formData, yearRanges } = this.data
    if (!yearRanges[0].length) return

    const currentYear = new Date().getFullYear()
    const endYear = currentYear + 4
    let enrollmentIndex = 0
    let graduationIndex = 0

    if (formData.enrollmentYear) {
      // 降序列表：索引 = endYear - 年份
      enrollmentIndex = endYear - formData.enrollmentYear
      if (enrollmentIndex < 0) enrollmentIndex = 0
      if (enrollmentIndex >= yearRanges[0].length) enrollmentIndex = yearRanges[0].length - 1
    }

    if (formData.graduationYear) {
      graduationIndex = endYear - formData.graduationYear
      if (graduationIndex < 0) graduationIndex = 0
      if (graduationIndex >= yearRanges[1].length) graduationIndex = yearRanges[1].length - 1
    }

    this.setData({
      yearPickerIndex: [enrollmentIndex, graduationIndex],
    })
  },

  // 处理年份范围选择确认
  handleYearRangeChange(e) {
    const indexArr = e.detail.value
    const currentYear = new Date().getFullYear()
    const endYear = currentYear + 4

    // 降序列表：年份 = endYear - 索引
    const enrollmentYear = endYear - indexArr[0]
    const graduationYear = endYear - indexArr[1]

    this.setData({
      'formData.enrollmentYear': enrollmentYear,
      'formData.graduationYear': graduationYear,
      yearPickerIndex: indexArr,
    })
  },

  // 处理年份列变化（可选，用于动态联动）
  handleYearColumnChange() {
    // 暂不做联动处理，两列独立选择
  },

  // 处理学历层次选择
  handleEducationLevelChange(e) {
    const index = e.detail.value
    this.setData({
      educationLevelIndex: index,
      'formData.educationLevel': this.data.educationLevels[index],
    })
  },

  // 选择图片
  async chooseImage() {
    try {
      // 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 3 - this.data.attachments.length,
          mediaType: ['image'],
          sourceType: ['album', 'camera'],
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) {
        return
      }

      // 显示上传中提示
      wx.showLoading({
        title: '上传中...',
        mask: true,
      })

      const attachments = [...this.data.attachments]

      // 逐个上传文件
      for (const file of tempFiles) {
        const tempFilePath = file.tempFilePath
        const originalName = file.name || 'attachment.jpg'

        // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
        const fileSize = file.size || 0
        const maxSize = 10 * 1024 * 1024 // 10MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: `${originalName} 大小不能超过10MB`,
            icon: 'none',
          })
          continue
        }

        // 上传文件（与校友会logo上传使用相同的方法）
        const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

        console.log('上传文件结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息，确保获取到fileId
          const fileId =
            uploadRes.data.fileId ||
            uploadRes.data.id ||
            uploadRes.data.file_id ||
            uploadRes.data.id ||
            ''
          const fileUrl = uploadRes.data.fileUrl || ''
          console.log('获取到的fileId:', fileId)
          console.log('获取到的fileUrl:', fileUrl)

          if (fileId && fileUrl) {
            attachments.push({
              url: fileUrl,
              fileId: fileId,
            })
          } else {
            wx.showToast({
              title: `${originalName} 上传成功但未获取到文件信息`,
              icon: 'none',
            })
          }
        } else {
          wx.showToast({
            title: `${originalName} 上传失败: ${uploadRes?.msg || '未知错误'}`,
            icon: 'none',
          })
        }
      }

      if (attachments.length > this.data.attachments.length) {
        this.setData({ attachments })
        wx.showToast({
          title: '上传成功',
          icon: 'success',
        })
      } else {
        wx.showToast({
          title: '上传失败',
          icon: 'none',
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000,
      })
    } finally {
      wx.hideLoading()
    }
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
    const { formData, fromMyRecord, myRecordType, alumniAssociationId } = this.data

    if (fromMyRecord && myRecordType === 'ALUMNI_ASSOCIATION_JOIN' && !alumniAssociationId) {
      wx.showToast({
        title: '请选择校友会',
        icon: 'none',
      })
      return false
    }

    if (!formData.name || !formData.name.trim()) {
      wx.showToast({
        title: '请输入真实姓名',
        icon: 'none',
      })
      return false
    }

    if (!formData.identifyCode || !formData.identifyCode.trim()) {
      wx.showToast({
        title: '请输入身份证号',
        icon: 'none',
      })
      return false
    }

    // 验证身份证号格式（与 profile/edit 页面保持一致）
    const idCardReg = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
    if (!idCardReg.test(formData.identifyCode.trim())) {
      wx.showToast({
        title: '身份证号格式不正确',
        icon: 'none',
      })
      return false
    }

    // 验证手机号（必填）
    if (!formData.phone || !formData.phone.trim()) {
      wx.showToast({
        title: '未获取到手机号',
        icon: 'none',
      })
      return false
    }

    const phoneReg = /^1[3-9]\d{9}$/
    if (!phoneReg.test(formData.phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none',
      })
      return false
    }

    // 验证教育经历必填字段
    if (!formData.enrollmentYear) {
      wx.showToast({
        title: '请选择入学年份',
        icon: 'none',
      })
      return false
    }

    if (!formData.graduationYear) {
      wx.showToast({
        title: '请选择毕业年份',
        icon: 'none',
      })
      return false
    }

    if (!formData.department || !formData.department.trim()) {
      wx.showToast({
        title: '请输入院系',
        icon: 'none',
      })
      return false
    }

    if (!formData.educationLevel) {
      wx.showToast({
        title: '请选择学历层次',
        icon: 'none',
      })
      return false
    }

    // 验证入学和毕业年份的合理性
    if (formData.enrollmentYear >= formData.graduationYear) {
      wx.showToast({
        title: '毕业年份应晚于入学年份',
        icon: 'none',
      })
      return false
    }

    return true
  },

  // 提交申请
  async submitApplication() {
    if (this.data.submitting) {
      return
    }

    // 验证表单
    if (!this.validateForm()) {
      return
    }

    const { formData, attachments, alumniAssociationId, isEditingApplication, applicationId } = this.data
    let { schoolId } = this.data

    // 校验 schoolId 是否存在
    if (!schoolId) {
      // 兜底：提交前再次尝试从校友会详情拉取 schoolId（解决扫码场景偶发未初始化完整问题）
      await this.fetchSchoolIdFromAssociation()
      schoolId = this.data.schoolId
    }

    if (!schoolId) {
      wx.showToast({
        title: '您申请的校友会存在异常，无法成功申请',
        icon: 'none',
        duration: 2500,
      })
      return
    }

    // 构建请求数据
    const requestData = {
      name: formData.name.trim(),
      identifyCode: formData.identifyCode.trim(),
      phone: formData.phone ? formData.phone.trim() : undefined,
      applicationReason: formData.applicationReason ? formData.applicationReason.trim() : undefined,
      attachmentIds: attachments.length > 0 ? attachments.map(item => item.fileId) : undefined,
      schoolId: schoolId ? String(schoolId) : undefined, // 保持字符串类型，避免精度丢失
      enrollmentYear: formData.enrollmentYear || undefined,
      graduationYear: formData.graduationYear || undefined,
      department: formData.department ? formData.department.trim() : undefined,
      educationLevel: formData.educationLevel || undefined,
    }

    // 如果是编辑模式，添加 applicationId
    if (isEditingApplication && applicationId) {
      requestData.applicationId = String(applicationId) // 保持字符串类型，避免精度丢失
    } else {
      // 新申请模式，添加 alumniAssociationId
      requestData.alumniAssociationId = String(alumniAssociationId)
    }

    // 移除 undefined 值
    Object.keys(requestData).forEach(key => {
      if (requestData[key] === undefined) {
        delete requestData[key]
      }
    })

    this.setData({ submitting: true })

    try {
      // 根据是否为编辑模式调用不同的接口
      let res
      if (isEditingApplication && applicationId) {
        if (this.data.fromMyRecord && this.data.myRecordType === 'ALUMNI_ASSOCIATION_JOIN') {
          requestData.alumniAssociationId = String(alumniAssociationId)
        }
        const recordId = this.data.myRecordId || String(applicationId)
        res = await myApplicationRecordApi.update({
          recordType: this.data.myRecordType || 'ALUMNI_ASSOCIATION_JOIN',
          recordId,
          payload: requestData,
        })
      } else {
        res = await associationApi.applyToJoinAssociation(requestData)
      }

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: isEditingApplication ? '修改提交成功' : '申请提交成功',
          icon: 'success',
          duration: 2000,
        })

        // 延迟退出当前页面：有历史栈则返回，否则回到首页
        setTimeout(() => {
          this.navigateAfterSubmitSuccess()
        }, 2000)
      } else {
        // 检查是否是重复加入的错误
        const errorMsg = res.data?.msg || res.data?.message || '提交失败'
        wx.showToast({
          title: errorMsg,
          icon: 'none',
        })
        this.setData({ submitting: false })
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none',
      })
      this.setData({ submitting: false })
    }
  },

  navigateAfterSubmitSuccess() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      wx.navigateBack({
        fail: () => {
          this.redirectToHomeAfterSubmit()
        },
      })
      return
    }

    this.redirectToHomeAfterSubmit()
  },

  redirectToHomeAfterSubmit() {
    wx.switchTab({
      url: '/pages/index/index',
      fail: () => {
        // 极端情况下 switchTab 失败，兜底重启到首页
        wx.reLaunch({
          url: '/pages/index/index',
          complete: () => {
            this.setData({ submitting: false })
          },
        })
      },
      complete: () => {
        this.setData({ submitting: false })
      },
    })
  },

  // 加载申请详情（查看模式或编辑已有申请）
  async loadApplicationDetail() {
    if (this.data.loadingDetail) {
      return
    }

    this.setData({ loadingDetail: true })

    try {
      let res
      if (this.data.fromMyRecord && this.data.myRecordType === 'ALUMNI_ASSOCIATION_JOIN' && this.data.myRecordId) {
        res = await associationApi.getJoinApplicationDetailById(this.data.myRecordId)
      } else {
        res = await associationApi.getApplicationDetail(this.data.alumniAssociationId)
      }

      if (res.data && res.data.code === 200) {
        const detail = res.data.data || {}

        // 填充表单数据
        const formData = {
          name: detail.name || '',
          identifyCode: detail.identifyCode || '',
          phone: detail.phone || '',
          applicationReason: detail.applicationReason || '',
          enrollmentYear: detail.enrollmentYear || '',
          graduationYear: detail.graduationYear || '',
          department: detail.department || '',
          major: detail.major || '',
          className: detail.className || '',
          educationLevel: detail.educationLevel || '',
        }

        // 设置学历层次索引
        let educationLevelIndex = -1
        if (detail.educationLevel) {
          educationLevelIndex = this.data.educationLevels.indexOf(detail.educationLevel)
        }

        // 处理附件
        const attachments = (detail.attachmentFiles || []).map(file => ({
          url: file.url || '',
          fileId: file.id || '',
        }))

        this.setData({
          applicationDetail: detail,
          applicationId: detail.applicationId || '', // 保存申请ID
          alumniAssociationId: detail.alumniAssociationId || this.data.alumniAssociationId || '',
          formData,
          educationLevelIndex,
          attachments,
          schoolId: detail.schoolId || '',
          schoolName: detail.schoolName || '',
          loadingDetail: false,
        })

        await this.fillBasicFormByUserInfoIfMissing()

        // 更新年份选择器索引
        this.updateYearPickerIndex()
      } else {
        this.setData({ loadingDetail: false })
        wx.showToast({
          title: res.data?.message || '加载失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('加载申请详情失败:', error)
      this.setData({ loadingDetail: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      })
    }
  },

  // 详情字段缺失时，用当前用户信息兜底（仅补空值）
  async fillBasicFormByUserInfoIfMissing() {
    const current = this.data.formData || {}
    const needFallback = !current.name || !current.identifyCode || !current.phone
    if (!needFallback) return
    try {
      const res = await userApi.getUserInfo()
      if (!(res.data && res.data.code === 200 && res.data.data)) return
      const userInfo = res.data.data
      this.setData({
        'formData.name': current.name || userInfo.name || '',
        'formData.identifyCode': current.identifyCode || userInfo.identifyCode || '',
        'formData.phone': current.phone || userInfo.phone || '',
      })
    } catch (error) {
      console.warn('[Apply] fillBasicFormByUserInfoIfMissing', error)
    }
  },

  // 撤销申请
  async cancelApplication() {
    const { applicationId } = this.data

    if (!applicationId) {
      wx.showToast({
        title: '申请ID不存在',
        icon: 'none',
      })
      return
    }

    wx.showModal({
      title: '撤销申请',
      content: '确定要撤销申请吗？',
      confirmText: '确定撤销',
      confirmColor: '#40B2E6',
      success: async res => {
        if (res.confirm) {
          try {
            const result = await associationApi.cancelApplication(applicationId)

            if (result.data && result.data.code === 200) {
              wx.showToast({
                title: '已撤销申请',
                icon: 'success',
                duration: 2000,
              })

              // 延迟返回上一页
              setTimeout(() => {
                wx.navigateBack()
              }, 2000)
            } else {
              wx.showToast({
                title: result.data?.message || '撤销失败',
                icon: 'none',
              })
            }
          } catch (error) {
            console.error('撤销申请失败:', error)
            wx.showToast({
              title: '撤销失败，请重试',
              icon: 'none',
            })
          }
        }
      },
    })
  },

  // 进入编辑模式
  async enterEditMode() {
    this.setData({
      mode: 'edit',
      isEditingApplication: true, // 标记为编辑已有申请
    })
    if (this.data.fromMyRecord && this.data.myRecordType === 'ALUMNI_ASSOCIATION_JOIN') {
      await this.loadEditableAssociationList()
      this.syncEditableAssociationSelection()
    }
  },
})
