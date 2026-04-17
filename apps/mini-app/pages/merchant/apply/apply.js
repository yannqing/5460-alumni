// pages/merchant/apply/apply.js
const app = getApp()
const { associationApi, fileApi, merchantApi } = require('../../../api/api.js')
const { post } = require('../../../utils/request.js')

Page({
  data: {
    form: {
      merchantName: '',
      merchantType: 1,
      businessLicense: '',
      logo: '',
      backgroundImageList: [],
      unifiedSocialCreditCode: '',
      legalPerson: '',
      legalPersonId: '',
      contactPhone: '',
      contactEmail: '',
      businessScope: '',
      businessCategory: '',
      alumniAssociationId: '',
      selectedAlumniAssociation: null
    },
    submitting: false,
    merchantTypeOptions: [
      { label: '校友商铺', value: 1 }
    ],
    merchantTypeIndex: 0,
    uploadType: 'license',
    merchantStatus: 'none', // none, pending, approved, rejected
    // 校友会搜索相关
    showAssociationSearch: false,
    associationSearchText: '',
    associationList: [],
    associationPage: 1,
    associationTotal: 0,
    associationLoading: false,
    /** 编辑待审核申请时存在（与 onLoad options.merchantId 一致） */
    merchantId: '',
  },

  onLoad(options) {
    this.loadMerchantStatus()
    this.loadUserData()
    
    // 如果有merchantId参数，获取待审核商家详情
    if (options.merchantId) {
      this.setData({
        merchantId: options.merchantId
      })
      this.loadPendingMerchantDetail(options.merchantId)
    }
  },

  loadUserData() {
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || {}
    
    const formData = {
      merchantName: '',
      merchantType: 1,
      businessLicense: '',
      logo: '',
      backgroundImageList: [],
      unifiedSocialCreditCode: '',
      legalPerson: userInfo.nickName || userData.nickName || userData.name || '',
      legalPersonId: '',
      contactPhone: userInfo.mobile || userData.mobile || userData.phone || '',
      contactEmail: userInfo.email || userData.email || '',
      businessScope: '',
      businessCategory: '',
      alumniAssociationId: '',
      selectedAlumniAssociation: null
    }
    
    this.setData({ form: formData })
  },

  loadMerchantStatus() {
    const userData = app.globalData.userData || {}
    // 假设后端返回 merchantStatus 字段
    const status = userData.merchantStatus || 'none'
    this.setData({ merchantStatus: status })
  },

  // 加载待审核商家详情
  loadPendingMerchantDetail(merchantId) {
    wx.showLoading({ title: '加载中...' })
    
    merchantApi.getPendingMerchantDetail(merchantId).then((res) => {
      wx.hideLoading()
      
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        // 预填充表单数据
        this.setData({
          form: {
            merchantName: data.merchantName || '',
            merchantType: data.merchantType || 1,
            businessLicense: data.businessLicense || '',
            logo: data.logo || '',
            backgroundImageList: this.parseBackgroundImage(data.backgroundImage),
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            legalPersonId: '', // 敏感信息不返回
            contactPhone: data.contactPhone || '',
            contactEmail: data.contactEmail || '',
            businessScope: data.businessScope || '',
            businessCategory: data.businessCategory || '',
            alumniAssociationId: data.alumniAssociationId || '',
            selectedAlumniAssociation: null // 需要重新关联校友会
          },
          merchantTypeIndex: data.merchantType === 1 ? 0 : 0,
          merchantStatus: this.getMerchantStatusText(data.reviewStatus) // 设置审核状态
        })
        
        // 如果有关联校友会，加载校友会信息
        if (data.alumniAssociationId) {
          this.loadAssociationDetail(data.alumniAssociationId)
        }
      } else {
        wx.showToast({
          title: msg || '加载失败，请稍后重试',
          icon: 'none'
        })
      }
    }).catch((err) => {
      wx.hideLoading()
      console.error('加载待审核商家详情失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    })
  },

  // 获取商家状态文本
  getMerchantStatusText(reviewStatus) {
    switch (reviewStatus) {
      case 0: // 待审核
        return 'pending'
      case 1: // 已通过
        return 'approved'
      case 2: // 已拒绝
        return 'rejected'
      default:
        return 'none'
    }
  },

  // 加载校友会详情
  loadAssociationDetail(alumniAssociationId) {
    associationApi.getAssociationDetail(alumniAssociationId).then((res) => {
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        // 设置选中的校友会
        this.setData({
          'form.selectedAlumniAssociation': {
            alumniAssociationId: data.alumniAssociationId,
            associationName: data.associationName,
            schoolId: data.schoolId,
            platformId: data.platformId,
            contactInfo: data.contactInfo,
            location: data.location,
            memberCount: data.memberCount,
            logo: data.logo
          },
          'form.alumniAssociationId': data.alumniAssociationId
        })
      }
    }).catch((err) => {
      console.error('加载校友会详情失败:', err)
    })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  handleMerchantTypeChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      merchantTypeIndex: index,
      'form.merchantType': this.data.merchantTypeOptions[index].value,
      'form.alumniAssociationId': '',
      'form.selectedAlumniAssociation': null
    })
  },
  
  // 校友会搜索相关方法
  showAssociationSearch() {
    this.setData({
      showAssociationSearch: true,
      associationSearchText: '',
      associationList: [],
      associationPage: 1,
      associationTotal: 0
    })
  },
  
  hideAssociationSearch() {
    this.setData({
      showAssociationSearch: false
    })
  },
  
  onAssociationSearchInput(e) {
    this.setData({
      associationSearchText: e.detail.value
    })
  },
  
  searchAssociations() {
    const { associationSearchText } = this.data
    
    this.setData({
      associationLoading: true,
      associationPage: 1
    })
    
    associationApi.getAssociationList({
      current: 1,
      pageSize: 10,
      associationName: associationSearchText
    }).then((res) => {
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        const list = data.records || data.list || []
        
        // 数据映射，与list.js保持一致
        const mappedList = list.map(item => ({
          // 与后端VO完全一致的字段
          alumniAssociationId: item.alumniAssociationId,   // 校友会ID
          associationName: item.associationName,           // 校友会名称
          schoolId: item.schoolId,                         // 所属母校ID
          platformId: item.platformId,                     // 所属校促会ID
          contactInfo: item.contactInfo,                   // 联系信息
          location: item.location,                         // 常驻地点
          memberCount: item.memberCount,                   // 会员数量
          logo: item.logo                                  // 校友会logo
        }))
        
        this.setData({
          associationList: mappedList,
          associationTotal: data.total || 0,
          associationPage: 1
        })
      } else {
        wx.showToast({
          title: msg || '搜索失败，请稍后重试',
          icon: 'none'
        })
      }
    }).catch((err) => {
      console.error('搜索校友会失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }).finally(() => {
      this.setData({
        associationLoading: false
      })
    })
  },
  
  selectAssociation(e) {
    const selectedItem = e.currentTarget.dataset.item
    
    this.setData({
      'form.selectedAlumniAssociation': selectedItem,
      'form.alumniAssociationId': selectedItem.alumniAssociationId,
      showAssociationSearch: false
    })
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles?.[0]?.tempFilePath
        if (tempFilePath) {
          const field = this.data.uploadType === 'logo' ? 'logo' : 'businessLicense'
          this.setData({
            [`form.${field}`]: tempFilePath
          })
          // TODO: 上传图片到服务器
          this.uploadImage(tempFilePath, field)
        }
      }
    })
  },

  chooseBusinessLicense() {
    this.setData({ uploadType: 'license' })
    this.chooseImage()
  },

  chooseLogo() {
    this.setData({ uploadType: 'logo' })
    this.chooseImage()
  },

  chooseBackgroundImage() {
    wx.chooseMedia({
      count: 9,
      mediaType: ['image'],
      success: (res) => {
        const files = res.tempFiles || []
        if (!files.length) {return}
        const uploadTasks = files.map(file => this.uploadImage(file.tempFilePath, 'backgroundImageList', false))
        Promise.all(uploadTasks).then(urls => {
          const validUrls = urls.filter(Boolean)
          if (!validUrls.length) {return}
          this.setData({
            'form.backgroundImageList': [...(this.data.form.backgroundImageList || []), ...validUrls]
          })
          wx.showToast({
            title: '上传成功',
            icon: 'success'
          })
        })
      }
    })
  },

  uploadImage(filePath, field, showLoading = true) {
    if (showLoading) {
      wx.showLoading({ title: '上传中...' })
    }
    return fileApi.uploadImage(filePath).then((res) => {
      if (showLoading) {
        wx.hideLoading()
      }
      
      if (res.code === 200 && res.data && res.data.fileUrl) {
        if (field !== 'backgroundImageList') {
          this.setData({
            [`form.${field}`]: res.data.fileUrl
          })
          wx.showToast({
            title: '上传成功',
            icon: 'success'
          })
        }
        return res.data.fileUrl
      } else {
        wx.showToast({
          title: res.msg || '上传失败',
          icon: 'none'
        })
        if (field !== 'backgroundImageList') {
          this.setData({
            [`form.${field}`]: ''
          })
        }
        return ''
      }
    }).catch((err) => {
      if (showLoading) {
        wx.hideLoading()
      }
      wx.showToast({
        title: err.msg || '上传失败，请稍后重试',
        icon: 'none'
      })
      if (field !== 'backgroundImageList') {
        this.setData({
          [`form.${field}`]: ''
        })
      }
      return ''
    })
  },

  removeImage(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: ''
    })
  },

  removeBackgroundImage(e) {
    const { index } = e.currentTarget.dataset
    const list = [...(this.data.form.backgroundImageList || [])]
    list.splice(index, 1)
    this.setData({
      'form.backgroundImageList': list
    })
  },

  parseBackgroundImage(backgroundImage) {
    if (!backgroundImage) {return []}
    if (Array.isArray(backgroundImage)) {return backgroundImage}
    if (typeof backgroundImage === 'string') {
      try {
        const parsed = JSON.parse(backgroundImage)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return []
      }
    }
    return []
  },

  validateForm() {
    const { merchantName, merchantType, businessLicense, unifiedSocialCreditCode, legalPerson, legalPersonId, contactPhone, contactEmail, alumniAssociationId, selectedAlumniAssociation } = this.data.form
    
    if (!merchantName || !merchantName.trim()) {
      wx.showToast({ title: '请输入商户名称', icon: 'none' })
      return false
    }
    
    if (merchantName.length > 100) {
      wx.showToast({ title: '商户名称不能超过100个字符', icon: 'none' })
      return false
    }
    
    if (!merchantType || ![1, 2].includes(merchantType)) {
      wx.showToast({ title: '请选择正确的商户类型', icon: 'none' })
      return false
    }
    
    if (!businessLicense) {
      wx.showToast({ title: '请上传营业执照', icon: 'none' })
      return false
    }
    
    if (!unifiedSocialCreditCode || !unifiedSocialCreditCode.trim()) {
      wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
      return false
    }
    
    const normalizedCreditCode = String(unifiedSocialCreditCode).trim().toUpperCase()
    if (normalizedCreditCode.length !== 18) {
      wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' })
      return false
    }
    if (!/^[0-9A-Z]{18}$/.test(normalizedCreditCode)) {
      wx.showToast({ title: '统一社会信用代码格式不正确', icon: 'none' })
      return false
    }
    
    if (!legalPerson || !legalPerson.trim()) {
      wx.showToast({ title: '请输入法人姓名', icon: 'none' })
      return false
    }
    
    if (legalPerson.length > 50) {
      wx.showToast({ title: '法人姓名不能超过50个字符', icon: 'none' })
      return false
    }
    
    if (!legalPersonId || !legalPersonId.trim()) {
      wx.showToast({ title: '请输入法人身份证号', icon: 'none' })
      return false
    }
    
    const normalizedLegalPersonId = String(legalPersonId).trim()
    if (normalizedLegalPersonId.length !== 18) {
      wx.showToast({ title: '法人身份证号须为18位', icon: 'none' })
      return false
    }
    if (!/^\d{17}[\dXx]$/.test(normalizedLegalPersonId)) {
      wx.showToast({ title: '法人身份证号格式不正确', icon: 'none' })
      return false
    }
    
    if (!contactPhone || !contactPhone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return false
    }
    
    if (!/^\d{11}$/.test(String(contactPhone).trim())) {
      wx.showToast({ title: '联系电话须为11位数字', icon: 'none' })
      return false
    }
    
    if (contactEmail && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(contactEmail)) {
      wx.showToast({ title: '邮箱格式不正确', icon: 'none' })
      return false
    }
    
    if (merchantType === 1 && !selectedAlumniAssociation) {
      wx.showToast({ title: '校友商铺请搜索并选择关联校友会', icon: 'none' })
      return false
    }
    
    return true
  },

  submitApply() {
    if (this.data.merchantStatus === 'approved') {
      wx.showToast({
        title: '您已是认证商家',
        icon: 'none'
      })
      return
    }
    
    if (!this.validateForm()) {return}
    
    this.setData({ submitting: true })
    
    const { form } = this.data
    const normalizedCreditCode = String(form.unifiedSocialCreditCode || '').trim().toUpperCase()
    const normalizedLegalPersonId = String(form.legalPersonId || '').trim().toUpperCase()

    const applyPayload = {
      merchantName: form.merchantName,
      merchantType: form.merchantType,
      businessLicense: form.businessLicense,
      logo: form.logo || undefined,
      backgroundImage: (form.backgroundImageList && form.backgroundImageList.length)
        ? JSON.stringify(form.backgroundImageList)
        : undefined,
      unifiedSocialCreditCode: normalizedCreditCode,
      legalPerson: form.legalPerson,
      legalPersonId: normalizedLegalPersonId,
      contactPhone: form.contactPhone,
      contactEmail: form.contactEmail,
      businessScope: form.businessScope,
      businessCategory: form.businessCategory,
      alumniAssociationId: form.selectedAlumniAssociation ? form.selectedAlumniAssociation.alumniAssociationId : 0
    }

    const mid = this.data.merchantId
    const useUpdatePending = mid && this.data.merchantStatus === 'pending'
    const req = useUpdatePending
      ? merchantApi.updatePendingMerchantApplication(mid, applyPayload)
      : post('/merchant/apply', applyPayload)

    req.then((res) => {
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        wx.showToast({
          title: msg || '提交成功，等待审核',
          icon: 'success'
        })
        this.setData({
          submitting: false,
          merchantStatus: 'pending'
        })
        
        // 更新全局数据
        if (app.globalData.userData) {
          app.globalData.userData.merchantStatus = 'pending'
        }
        
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: msg || '提交失败，请稍后重试',
          icon: 'none'
        })
        this.setData({ submitting: false })
      }
    }).catch((err) => {
      console.error('提交申请失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
      this.setData({ submitting: false })
    })
  }
})



