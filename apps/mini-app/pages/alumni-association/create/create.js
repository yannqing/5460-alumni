const { schoolApi, userApi, associationApi, fileApi } = require('../../../api/api.js')
const app = getApp()
const config = require('../../../utils/config.js')

// 云托管环境下请求体大小限制较严，超过此阈值先压缩再上传（单位：字节）
const UPLOAD_IMAGE_COMPRESS_THRESHOLD = config.IS_CLOUD_HOST
  ? 512 * 1024
  : 1024 * 1024 // 云托管 512KB，非云托管 1MB

/**
 * 若图片超过阈值则压缩后返回路径，否则返回原路径（适配云托管）
 * @param {string} tempFilePath 本地临时路径
 * @param {number} fileSize 文件大小（字节）
 * @returns {Promise<string>} 用于上传的本地路径
 */
function getImagePathForUpload(tempFilePath, fileSize) {
  if (fileSize <= UPLOAD_IMAGE_COMPRESS_THRESHOLD) {
    return Promise.resolve(tempFilePath)
  }
  return new Promise((resolve) => {
    const quality = Math.max(20, Math.min(80, Math.floor((UPLOAD_IMAGE_COMPRESS_THRESHOLD / fileSize) * 80)))
    wx.compressImage({
      src: tempFilePath,
      quality,
      success: (res) => resolve(res.tempFilePath),
      fail: () => resolve(tempFilePath) // 压缩失败则用原图
    })
  })
}

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
    formData: {
      associationName: '',
      schoolId: '',
      schoolName: '',
      previousName: '',
      chargeName: '',
      contactInfo: '',
      msocialAffiliation: '',
      zhName: '',
      zhRole: '',
      zhPhone: '',
      zhSocialAffiliation: '',
      zhWxId: '',
      logo: '',

      applicationReason: '',
      associationProfile: '',
      presidentWxId: '',
      location: '',
      coverageArea: '', // 覆盖区域（选填）
      logoType: 'default', // logo来源类型: default, school, upload
    },
    schoolLogoUrl: '',
    defaultAlumniLogo: '',
    // 搜索结果列表
    schoolList: [],

    // 架构模板相关
    templateList: [],
    templateIndex: -1,
    selectedTemplate: null,
    templateTreeData: [], // 树形结构预览数据

    // 地区选择器
    regionValue: [],

    // 控制显示
    showSchoolResults: false,

    // 其他成员列表
    members: [],

    // 申请材料
    attachments: [],

    // 背景图
    bgImages: [],

    loading: false,
    submitting: false,
    defaultAvatar: config.defaultAvatar,
    headerImageUrl: `https://${config.DOMAIN}/upload/images/2026/02/09/9f328fe3-fcad-4019-a379-1a6db70f3a5d.png`,
  },

  onLoad() {
    // 创建学校搜索防抖函数（负责人姓名等由用户直接填写，不使用校友列表选择）
    this.searchSchoolDebounced = debounce(this.searchSchool, 500)

    this.loadInitialData()
    this.loadTemplateList()

    // 默认初始化logo为平台默认logo（本地静态资源）
    const defaultLogoUrl = '/assets/avatar/avatar.jpg'

    this.setData({
      defaultAlumniLogo: defaultLogoUrl,
      'formData.logo': defaultLogoUrl,
      // 初始化第一个成员为"主要负责人"，不能删除（负责人不传 wxid，姓名等由用户直接填写）
      members: [{ name: '', role: '会长', affiliation: '', phone: '' }],
    })
  },

  // 加载架构模板列表
  async loadTemplateList() {
    try {
      // organizeType: 0-校友会，1-校促会，2-商户
      const res = await associationApi.getOrganizeTemplateList({ organizeType: 0 })
      if (res.data && res.data.code === 200) {
        const templateList = res.data.data || []
        // 找到默认模板的索引
        let defaultIndex = templateList.findIndex(item => item.isDefault === 1)
        if (defaultIndex === -1 && templateList.length > 0) {
          defaultIndex = 0 // 如果没有默认模板，默认选中第一个
        }

        const defaultTemplate = defaultIndex >= 0 ? templateList[defaultIndex] : null
        // API 返回的 templateContent 已经是树形结构，直接使用
        const treeData =
          defaultTemplate && defaultTemplate.templateContent ? defaultTemplate.templateContent : []

        this.setData({
          templateList: templateList,
          templateIndex: defaultIndex,
          selectedTemplate: defaultTemplate,
          templateTreeData: treeData,
        })

        console.log('加载模板列表成功，树形数据:', treeData)
      }
    } catch (e) {
      console.error('加载架构模板列表失败', e)
    }
  },

  // 处理架构模板选择
  handleTemplateChange(e) {
    const index = e.detail.value
    const template = this.data.templateList[index]
    if (template) {
      // API 返回的 templateContent 已经是树形结构，直接使用
      const treeData = template.templateContent || []
      this.setData({
        templateIndex: index,
        selectedTemplate: template,
        templateTreeData: treeData,
      })
      console.log('选择模板，树形数据:', treeData)
    }
  },

  // 处理常驻地点选择（省/市/区）
  handleRegionChange(e) {
    const value = e.detail.value // [省, 市, 区]
    const province = value[0] || ''
    const city = value[1] || ''
    const district = value[2] || ''

    // 拼接为 "省 市 区" 格式
    let location = province
    if (city) location += ' ' + city
    if (district) location += ' ' + district

    this.setData({
      regionValue: value,
      'formData.location': location,
    })
  },

  async loadInitialData() {
    this.setData({ loading: true })
    try {
      await this.loadUserInfo()
    } catch (error) {
      console.error('加载初始化数据失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadUserInfo() {
    try {
      const res = await userApi.getUserInfo()
      if (res.data && res.data.code === 200) {
        const userInfo = res.data.data
        console.log('获取到的用户信息:', userInfo)

        // 尝试获取用户的 wx_id，检查所有可能的字段名
        const userId =
          userInfo.wxId || userInfo.wx_id || userInfo.userId || userInfo.user_id || userInfo.id
        console.log('获取到的用户ID:', userId)

        // 将当前登录用户信息填入驻会代表字段
        this.setData({
          'formData.zhName': userInfo.name || userInfo.realName || userInfo.nickname || '',
          'formData.zhPhone': userInfo.phone || userInfo.mobile || '',
          'formData.zhSocialAffiliation':
            userInfo.socialAffiliation || userInfo.social_affiliation || '',
          'formData.zhWxId': userId || '',
          'formData.presidentWxId': userId,
        })
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  },

  // --- 下拉框控制 ---

  closeAllDropdowns() {
    this.setData({
      showSchoolResults: false,
    })
  },

  preventBubble() {
    // 阻止冒泡
  },

  // 通用输入处理
  handleInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`formData.${field}`]: e.detail.value,
    })
  },

  // 处理校友会Logo类型切换
  handleLogoTypeChange(e) {
    const type = e.detail.value
    let logo = ''

    if (type === 'default') {
      logo = this.data.defaultAlumniLogo
    } else if (type === 'school') {
      // 只有当有 schoolId 时才展示相应学校的 logo，否则为空
      if (this.data.formData.schoolId) {
        logo = this.data.schoolLogoUrl || config.defaultSchoolAvatar
      } else {
        logo = '' // 未选学校时不展示预览
      }
    } else if (type === 'upload') {
      // 如果切到上传,保留原来的logo或者为空
      logo = this.data.formData.logoType === 'upload' ? this.data.formData.logo : ''
    }

    this.setData({
      'formData.logoType': type,
      'formData.logo': logo,
    })
  },

  // --- 学校搜索处理 ---

  handleSchoolInput(e) {
    const value = e.detail.value
    this.setData({
      'formData.schoolName': value,
      'formData.schoolId': '', // 清空ID，因为修改了名称
      showSchoolResults: true,
    })

    if (value.trim()) {
      this.searchSchoolDebounced(value)
    } else {
      this.setData({ schoolList: [] })
    }
  },

  handleSchoolFocus() {
    // 聚焦时如果已有内容，也展示结果
    if (this.data.formData.schoolName) {
      this.setData({ showSchoolResults: true })
      if (this.data.schoolList.length === 0) {
        this.searchSchool(this.data.formData.schoolName)
      }
    }
  },

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
          schoolList: res.data.data.records || [],
        })
      }
    } catch (e) {
      console.error('搜索学校失败', e)
    }
  },

  selectSchool(e) {
    const index = e.currentTarget.dataset.index
    const school = this.data.schoolList[index]

    // 提取学校Logo，如果学校没有Logo则使用系统默认母校图标
    const schoolLogo = school.logo ? config.getImageUrl(school.logo) : config.defaultSchoolAvatar

    const updateData = {
      'formData.schoolId': school.schoolId,
      'formData.schoolName': school.schoolName,
      schoolLogoUrl: schoolLogo,
      showSchoolResults: false,
    }

    // 如果当前选中的是"使用学校logo", 则实时更新预览图和提交用的logo地址
    if (this.data.formData.logoType === 'school') {
      updateData['formData.logo'] = schoolLogo
    }

    this.setData(updateData)
  },

  // --- 成员和其他逻辑 ---

  addMember() {
    const members = this.data.members
    members.push({ name: '', role: '', affiliation: '', phone: '' })
    this.setData({ members })
  },

  deleteMember(e) {
    const index = e.currentTarget.dataset.index
    // 第一个成员是主要负责人，不能删除
    if (index === 0) {
      wx.showToast({ title: '主要负责人不能删除', icon: 'none' })
      return
    }
    const members = this.data.members
    members.splice(index, 1)
    this.setData({ members })
  },

  handleMemberInput(e) {
    const { index, field } = e.currentTarget.dataset
    const value = e.detail.value
    const members = this.data.members
    members[index][field] = value
    this.setData({ members })
  },

  // --- 校友会logo上传处理 ---

  async chooseLogo() {
    // 如果当前不是上传模式,点击选择图片会自动切到上传模式
    if (this.data.formData.logoType !== 'upload') {
      this.setData({
        'formData.logoType': 'upload',
      })
    }
    try {
      // 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          sizeType: ['compressed'], // 优先选压缩图，便于云托管上传
          success: resolve,
          fail: reject,
        })
      })

      const tempFilePath = chooseRes.tempFiles?.[0]?.tempFilePath
      if (!tempFilePath) {
        return
      }

      // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
      const fileSize = chooseRes.tempFiles?.[0]?.size || 0
      const maxSize = 10 * 1024 * 1024 // 10MB
      if (fileSize > maxSize) {
        wx.showToast({
          title: '图片大小不能超过10MB',
          icon: 'none',
        })
        return
      }

      wx.showLoading({ title: '处理中...', mask: true })

      // 云托管等环境对请求体大小限制严，超过阈值先压缩再上传
      const pathToUpload = await getImagePathForUpload(tempFilePath, fileSize)
      wx.showLoading({ title: '上传中...', mask: true })

      const originalName = chooseRes.tempFiles?.[0]?.name || 'logo.jpg'
      const uploadRes = await fileApi.uploadImage(pathToUpload, originalName)

      if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
        // 获取返回的图片URL
        const rawImageUrl = uploadRes.data.fileUrl || ''
        if (rawImageUrl) {
          // 使用 config.getImageUrl 处理图片URL，确保是完整的URL
          const config = require('../../../utils/config.js')
          const imageUrl = config.getImageUrl(rawImageUrl)
          // 更新表单中的logo URL
          this.setData({ 'formData.logo': imageUrl })
          wx.showToast({
            title: '上传成功',
            icon: 'success',
          })
        } else {
          wx.showToast({
            title: '上传失败，未获取到图片地址',
            icon: 'none',
          })
        }
      } else {
        wx.showToast({
          title: uploadRes?.msg || '上传失败',
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

  // 删除已上传的logo
  deleteLogo() {
    this.setData({
      'formData.logo': '',
    })
  },

  // --- 申请材料上传处理 ---

  // 支持的文档格式
  ALLOWED_DOCUMENT_EXTENSIONS: [
    'pdf',
    'doc',
    'docx',
    'xls',
    'xlsx',
    'ppt',
    'pptx',
    'txt',
    'md',
    'csv',
    'rtf',
    'odt',
    'ods',
    'odp',
  ],

  async chooseAttachment() {
    try {
      // 使用 wx.chooseMessageFile 选择文档文件
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMessageFile({
          count: 5,
          type: 'file',
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
        const tempFilePath = file.path
        const originalName = file.name || 'document'

        // 获取文件扩展名
        const ext = originalName.split('.').pop().toLowerCase()

        // 检查文件格式是否支持
        if (!this.ALLOWED_DOCUMENT_EXTENSIONS.includes(ext)) {
          wx.showToast({
            title: `不支持的文件格式: ${ext}`,
            icon: 'none',
            duration: 2000,
          })
          continue
        }

        // 检查文件大小（5MB = 5 * 1024 * 1024 字节）
        const fileSize = file.size || 0
        const maxSize = 5 * 1024 * 1024 // 5MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: `${originalName} 大小不能超过5MB`,
            icon: 'none',
          })
          continue
        }

        // 上传文档文件
        const uploadRes = await fileApi.uploadDocument(tempFilePath, originalName)

        console.log('上传文档结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息
          const fileId = uploadRes.data.fileId || uploadRes.data.id || ''
          const fileName = uploadRes.data.fileName || originalName
          console.log('获取到的fileId:', fileId)

          if (fileId) {
            attachments.push({
              id: fileId,
              name: fileName,
            })
          } else {
            wx.showToast({
              title: `${originalName} 上传成功但未获取到文件ID`,
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

  deleteAttachment(e) {
    const index = e.currentTarget.dataset.index
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },

  // 选择图片作为附件
  async chooseAttachmentImage() {
    try {
      // 使用 wx.chooseMedia 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 9,
          mediaType: ['image'],
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

      // 逐个上传图片
      for (const file of tempFiles) {
        const tempFilePath = file.tempFilePath
        // 从路径中提取文件名
        const pathParts = tempFilePath.split('/')
        const originalName = pathParts[pathParts.length - 1] || 'image.jpg'

        // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
        const fileSize = file.size || 0
        const maxSize = 10 * 1024 * 1024 // 10MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: `图片大小不能超过10MB`,
            icon: 'none',
          })
          continue
        }

        // 上传图片文件
        const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

        console.log('上传图片结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息
          const fileId = uploadRes.data.fileId || uploadRes.data.id || ''
          const fileName = uploadRes.data.fileName || originalName
          console.log('获取到的fileId:', fileId)

          if (fileId) {
            attachments.push({
              id: fileId,
              name: fileName,
            })
          } else {
            wx.showToast({
              title: `图片上传成功但未获取到文件ID`,
              icon: 'none',
            })
          }
        } else {
          wx.showToast({
            title: `图片上传失败: ${uploadRes?.msg || '未知错误'}`,
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

  // --- 背景图上传处理 ---

  async chooseBgImage() {
    try {
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 5,
          mediaType: ['image'],
          sizeType: ['compressed'], // 优先选压缩图，便于云托管上传
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) {
        return
      }

      wx.showLoading({ title: '处理中...', mask: true })

      const bgImages = [...this.data.bgImages]

      for (const file of tempFiles) {
        const tempFilePath = file.tempFilePath
        const originalName = file.name || 'bg-image'
        const fileSize = file.size || 0
        const maxSize = 10 * 1024 * 1024 // 10MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: `${originalName} 大小不能超过10MB`,
            icon: 'none',
          })
          continue
        }

        // 云托管等环境对请求体大小限制严，超过阈值先压缩再上传
        const pathToUpload = await getImagePathForUpload(tempFilePath, fileSize)
        wx.showLoading({ title: '上传中...', mask: true })

        const uploadRes = await fileApi.uploadImage(pathToUpload, originalName)

        console.log('上传背景图结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息，确保获取到fileUrl
          const fileUrl = uploadRes.data.fileUrl || ''
          console.log('获取到的fileUrl:', fileUrl)

          if (fileUrl) {
            bgImages.push({
              url: fileUrl,
              name: originalName,
            })
          } else {
            wx.showToast({
              title: `${originalName} 上传成功但未获取到文件URL`,
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

      if (bgImages.length > this.data.bgImages.length) {
        this.setData({ bgImages })
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

  deleteBgImage(e) {
    const index = e.currentTarget.dataset.index
    const bgImages = [...this.data.bgImages]
    bgImages.splice(index, 1)
    this.setData({ bgImages })
  },

  async submitForm() {
    if (this.data.submitting) {
      return
    }

    const { formData, members } = this.data

    if (!formData.associationName) {
      wx.showToast({ title: '请输入校友会名称', icon: 'none' })
      return
    }
    if (!formData.schoolId) {
      wx.showToast({ title: '请选择并点击学校', icon: 'none' })
      return
    }
    if (!formData.location) {
      wx.showToast({ title: '请选择常驻地点', icon: 'none' })
      return
    }
    if (!formData.associationProfile) {
      wx.showToast({ title: '请输入校友会简介', icon: 'none' })
      return
    }
    // platformId is optional per API specs
    // 主要负责人信息现在从 members[0] 获取，在下面的成员验证中统一校验
    if (!formData.zhName) {
      wx.showToast({ title: '请输入联系人姓名', icon: 'none' })
      return
    }
    if (!formData.zhRole) {
      wx.showToast({ title: '请输入联系人职务', icon: 'none' })
      return
    }
    if (!formData.zhPhone) {
      wx.showToast({ title: '请输入联系人联系电话', icon: 'none' })
      return
    }
    if (!/^\d{11}$/.test(String(formData.zhPhone).trim())) {
      wx.showToast({ title: '联系人联系电话须为11位数字', icon: 'none' })
      return
    }
    if (!formData.zhSocialAffiliation) {
      wx.showToast({ title: '请输入联系人社会职务', icon: 'none' })
      return
    }

    // 确保presidentWxId不为空
    if (!formData.presidentWxId) {
      wx.showToast({ title: '提交者的wx_id不能为空', icon: 'none' })
      return
    }

    const chargeLeader = members[0]
    if (!chargeLeader) {
      wx.showToast({ title: '请完善主要负责人信息', icon: 'none' })
      return
    }

    // 确保所有成员都填写了完整的四项信息
    for (let i = 0; i < members.length; i++) {
      const member = members[i]
      const memberLabel = i === 0 ? '主要负责人' : `第 ${i} 位成员`
      if (!member.role) {
        wx.showToast({ title: `请输入${memberLabel}的职务`, icon: 'none' })
        return
      }
      if (!member.name) {
        wx.showToast({ title: `请输入${memberLabel}的姓名`, icon: 'none' })
        return
      }
      if (!member.affiliation) {
        wx.showToast({ title: `请输入${memberLabel}的社会职务`, icon: 'none' })
        return
      }
      if (!member.phone) {
        wx.showToast({ title: `请输入${memberLabel}的联系方式`, icon: 'none' })
        return
      }
      if (!/^\d{11}$/.test(String(member.phone).trim())) {
        const phoneLabel = i === 0 ? '主要负责人联系电话' : `第 ${i} 位负责人联系电话`
        wx.showToast({ title: `${phoneLabel}须为11位数字`, icon: 'none' })
        return
      }
    }

    console.log('准备提交的用户ID:', formData.presidentWxId)
    console.log('用户ID类型:', typeof formData.presidentWxId)

    // 提取申请材料的ID，使用字符串类型
    const attachmentIds = this.data.attachments.map(a => {
      const id = a.id || ''
      console.log('原始文件ID:', id, '类型:', typeof id)
      console.log('使用字符串类型文件ID:', id)
      return id
    })
    console.log('最终attachmentIds:', attachmentIds)

    // 提取背景图的URL，直接使用数组形式
    const bgImgArray = this.data.bgImages.map(a => {
      const url = a.url || ''
      return url
    })
    const bgImg = bgImgArray.length > 0 ? bgImgArray : undefined
    console.log('最终bgImg:', bgImg)

    // 第一个成员是主要负责人
    // 其余成员作为 initialMembers
    const otherMembers = members.slice(1)

    const submitData = {
      associationName: formData.associationName,
      schoolId: formData.schoolId,
      // 主要负责人和其他负责人均不传 wxid，由用户直接填写姓名等信息
      chargeName: chargeLeader.name,
      chargeRole: chargeLeader.role,
      contactInfo: chargeLeader.phone || undefined,
      msocialAffiliation: chargeLeader.affiliation || undefined,
      zhName: formData.zhName || undefined,
      zhRole: formData.zhRole || undefined,
      zhPhone: formData.zhPhone || undefined,
      zhSocialAffiliation: formData.zhSocialAffiliation || undefined,
      zhWxId: formData.zhWxId || undefined,
      logo: formData.logoType === 'default' ? undefined : formData.logo || undefined,
      applicationReason: formData.applicationReason,
      associationProfile: formData.associationProfile || undefined,
      attachmentIds: attachmentIds.length > 0 ? attachmentIds : undefined,
      initialMembers:
        otherMembers.length > 0
          ? otherMembers.map(m => ({
              name: m.name || undefined,
              role: m.role || undefined,
              phone: m.phone || undefined,
              affiliation: m.affiliation || undefined,
              wxId: null, // 负责人不从校友列表选择，传空（与之前格式一致）
            }))
          : undefined,
    }

    // 添加常驻地点（来自地区选择器）
    if (formData.location) {
      submitData.location = formData.location
    }

    // 覆盖区域（选填）
    if (formData.coverageArea) {
      submitData.coverageArea = formData.coverageArea
    }

    // 只有当bgImg存在时才添加到提交数据中
    if (bgImg !== undefined) {
      submitData.bgImg = bgImg
    }

    // 添加架构模板ID
    if (this.data.selectedTemplate && this.data.selectedTemplate.templateId) {
      submitData.templateId = this.data.selectedTemplate.templateId
    }

    console.log('最终提交数据:', submitData)

    this.setData({ submitting: true })

    try {
      const res = await associationApi.applyCreateAssociation(submitData)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '申请已提交',
          icon: 'success',
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else if (res.data && res.data.code === 50006) {
        // 处理重复提交的情况
        wx.showToast({
          title: res.data.msg || '该学校和地点已有待审核的校友会创建申请，请勿重复提交',
          icon: 'none',
        })
      } else {
        wx.showToast({
          title: res.data?.message || '提交失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none',
      })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 跳转到反馈页面
  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback?type=1&title=' + encodeURIComponent('创建校友会遇到问题'),
    })
  },
})
