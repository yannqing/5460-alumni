// pages/alumni-association/alumni-info-edit/alumni-info-edit.js
const { get, put } = require('../../../utils/request.js')
const { fileApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    detailLoading: true,
    submitting: false,
    associationIdStr: '',
    formData: {
      associationName: '',
      associationProfile: '',
      contactInfo: '',
      location: '',
      logo: '',
      bgImg: '',
      // 主要负责人信息
      chargeWxId: '',
      chargeName: '',
      chargeRole: '',
      chargeSocialAffiliation: '',
      // 联系人信息
      zhWxId: '',
      zhName: '',
      zhRole: '',
      zhPhone: '',
      zhSocialAffiliation: '',
    },
    regionValue: [],
    uploadingLogo: false,
    uploadingBgImage: false,
    bgImageList: [],
    bgImagePreviewUrls: [],
    // 绑定用户相关
    showBindUserModal: false,
    bindUserType: '',
    bindSearchKeyword: '',
    bindUserList: [],
    bindSearching: false,
    bindSearched: false,
    selectedBindUser: null,
  },

  onLoad(options) {
    const associationIdStr = options.associationId ? String(options.associationId) : ''
    this.setData({ associationIdStr })
    this.loadAssociationDetail(associationIdStr)
  },

  parseBgImages(raw) {
    if (!raw) return []
    if (Array.isArray(raw)) return raw.filter(Boolean).map(String)
    if (typeof raw === 'string') {
      try {
        const parsed = JSON.parse(raw)
        return Array.isArray(parsed) ? parsed.filter(Boolean).map(String) : []
      } catch (e) {
        return []
      }
    }
    return []
  },

  syncBgImagePreviews() {
    const list = this.data.bgImageList || []
    const urls = list.map(u => {
      const s = String(u).trim()
      if (!s) return ''
      return /^https?:\/\//i.test(s) ? s : config.getImageUrl(s)
    })
    this.setData({ bgImagePreviewUrls: urls })
  },

  async loadAssociationDetail(associationIdStr) {
    this.setData({ detailLoading: true })
    try {
      const res = await get(`/alumniAssociationManagement/detail/${associationIdStr}`)
      const body = res.data || {}
      if (body.code !== 200 || !body.data) {
        wx.showToast({ title: body.message || '加载失败', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
      const data = body.data

      // 处理背景图
      let bgImg = ''
      if (data.bgImg) {
        try {
          const cleanedStr = (data.bgImg || '')
            .toString()
            .trim()
            .replace(/^["']|['"]$/g, '')
          const parsed = JSON.parse(cleanedStr)
          bgImg = Array.isArray(parsed) ? parsed[0] || '' : parsed || ''
        } catch {
          bgImg = typeof data.bgImg === 'string' ? data.bgImg : ''
        }
      }

      const locationStr = data.location || ''
      const regionValue = locationStr ? locationStr.split(' ').filter(Boolean) : []

      // 处理 bgImageList（多张背景图）
      const bgImageList = this.parseBgImages(data.bgImg)

      this.setData({
        formData: {
          associationName: data.associationName || '',
          associationProfile: data.associationProfile || '',
          contactInfo: data.contactInfo || '',
          location: data.location || '',
          logo: data.logo || '',
          bgImg: bgImg,
          // 主要负责人信息
          chargeWxId: data.chargeWxId || '',
          chargeName: data.chargeName || '',
          chargeRole: data.chargeRole || '',
          chargeSocialAffiliation: data.chargeSocialAffiliation || '',
          // 联系人信息
          zhWxId: data.zhWxId || '',
          zhName: data.zhName || '',
          zhRole: data.zhRole || '',
          zhPhone: data.zhPhone || '',
          zhSocialAffiliation: data.zhSocialAffiliation || '',
        },
        regionValue,
        bgImageList,
        detailLoading: false,
      })
      this.syncBgImagePreviews()
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ detailLoading: false })
      setTimeout(() => wx.navigateBack(), 1500)
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({ [`formData.${field}`]: value })
  },

  // 处理常驻地点选择（省/市/区）
  handleRegionChange(e) {
    const value = e.detail.value
    const province = value[0] || ''
    const city = value[1] || ''
    const district = value[2] || ''

    let location = province
    if (city) location += ' ' + city
    if (district) location += ' ' + district

    this.setData({
      regionValue: value,
      'formData.location': location,
    })
  },

  // 上传logo
  onUploadLogo() {
    const that = this
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFilePaths[0]
        that.setData({ uploadingLogo: true })
        wx.showLoading({ title: '上传中...', mask: true })

        fileApi
          .uploadImage(tempFilePath)
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              that.setData({ [`formData.logo`]: res.data.fileUrl, uploadingLogo: false })
              wx.showToast({ title: '上传成功', icon: 'success' })
            } else {
              wx.showToast({ title: res.msg || '上传失败', icon: 'none' })
              that.setData({ uploadingLogo: false })
            }
          })
          .catch(err => {
            wx.showToast({ title: err.msg || '上传失败', icon: 'none' })
            that.setData({ uploadingLogo: false })
          })
          .finally(() => wx.hideLoading())
      },
      fail(err) {
        that.setData({ uploadingLogo: false })
      },
    })
  },

  // 删除logo
  onDeleteLogo() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除logo吗？',
      success: res => {
        if (res.confirm) {
          this.setData({ [`formData.logo`]: '' })
        }
      },
    })
  },

  // 上传背景图
  async chooseBgImage() {
    const remain = 9 - this.data.bgImageList.length
    if (remain <= 0) return

    try {
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: remain,
          mediaType: ['image'],
          sizeType: ['compressed'],
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) return

      wx.showLoading({ title: '上传中...', mask: true })
      const next = [...this.data.bgImageList]

      for (const file of tempFiles) {
        const uploadRes = await fileApi.uploadImage(
          file.tempFilePath,
          file.name || 'bg.jpg',
          file.size
        )
        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          const rawUrl = uploadRes.data.fileUrl || ''
          if (rawUrl) {
            next.push(rawUrl)
          }
        }
      }

      this.setData({ bgImageList: next }, () => this.syncBgImagePreviews())
      wx.showToast({ title: '上传完成', icon: 'success' })
    } catch (error) {
      if (error?.errMsg !== 'chooseMedia:fail cancel') {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    } finally {
      wx.hideLoading()
    }
  },

  // 删除背景图
  removeBgImage(e) {
    const { index } = e.currentTarget.dataset
    const list = [...this.data.bgImageList]
    list.splice(index, 1)
    this.setData({ bgImageList: list }, () => this.syncBgImagePreviews())
  },

  // 显示绑定用户弹窗
  showBindUserModal(e) {
    const type = e.currentTarget.dataset.type
    this.setData({
      showBindUserModal: true,
      bindUserType: type,
      bindSearchKeyword: '',
      bindUserList: [],
      bindSearching: false,
      bindSearched: false,
      selectedBindUser: null,
    })
  },

  // 隐藏绑定用户弹窗
  hideBindUserModal() {
    this.setData({
      showBindUserModal: false,
      bindUserType: '',
      bindSearchKeyword: '',
      bindUserList: [],
      bindSearching: false,
      bindSearched: false,
      selectedBindUser: null,
    })
  },

  // 搜索输入
  onBindSearchInput(e) {
    this.setData({ bindSearchKeyword: e.detail.value })
  },

  // 执行搜索
  async onBindSearch() {
    const keyword = this.data.bindSearchKeyword.trim()
    if (!keyword) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    this.setData({ bindSearching: true, bindSearched: false, bindUserList: [] })

    try {
      const params = { current: 1, size: 20 }
      if (/^\d{11}$/.test(keyword)) {
        params.phone = keyword
      } else {
        params.keyword = keyword
        params.name = keyword
      }

      const { alumniApi } = require('../../../api/api.js')
      const res = await alumniApi.queryAlumniList(params)

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || []
        const mappedList = records.map(item => ({
          wxId: item.wxId,
          name: item.name || item.realName || '',
          nickname: item.nickname || '',
          phone: item.phone || '',
          avatarUrl: item.avatarUrl ? config.getImageUrl(item.avatarUrl) : config.defaultAvatar,
        }))

        this.setData({
          bindUserList: mappedList,
          bindSearching: false,
          bindSearched: true,
        })
      } else {
        this.setData({ bindSearching: false, bindSearched: true })
        wx.showToast({ title: res.data?.msg || '搜索失败', icon: 'none' })
      }
    } catch (error) {
      this.setData({ bindSearching: false, bindSearched: true })
      wx.showToast({ title: '搜索失败', icon: 'none' })
    }
  },

  // 选择用户
  selectBindUser(e) {
    const user = e.currentTarget.dataset.user
    this.setData({ selectedBindUser: user })
  },

  // 确认绑定
  confirmBindUser() {
    const { selectedBindUser, bindUserType } = this.data
    if (!selectedBindUser) {
      wx.showToast({ title: '请选择用户', icon: 'none' })
      return
    }

    if (bindUserType === 'charge') {
      this.setData({
        'formData.chargeWxId': selectedBindUser.wxId,
        'formData.chargeName': selectedBindUser.name || selectedBindUser.nickname || '',
      })
    } else if (bindUserType === 'zh') {
      this.setData({
        'formData.zhWxId': selectedBindUser.wxId,
        'formData.zhName': selectedBindUser.name || selectedBindUser.nickname || '',
      })
    }

    this.hideBindUserModal()
    wx.showToast({ title: '绑定成功', icon: 'success' })
  },

  // 更换用户
  replaceUser(e) {
    const type = e.currentTarget.dataset.type
    const title = type === 'charge' ? '更换主要负责人' : '更换联系人'
    const content =
      type === 'charge'
        ? '确定要更换主要负责人吗？原负责人的信息将被清空。'
        : '确定要更换联系人吗？原代表的信息将被清空。'

    wx.showModal({
      title: title,
      content: content,
      success: res => {
        if (res.confirm) {
          if (type === 'charge') {
            this.setData({
              'formData.chargeWxId': '',
              'formData.chargeName': '',
              'formData.chargeRole': '',
              'formData.chargeSocialAffiliation': '',
              'formData.contactInfo': '',
            })
          } else if (type === 'zh') {
            this.setData({
              'formData.zhWxId': '',
              'formData.zhName': '',
              'formData.zhRole': '',
              'formData.zhPhone': '',
              'formData.zhSocialAffiliation': '',
            })
          }
          wx.showToast({ title: '已清空，请重新绑定', icon: 'none' })
        }
      },
    })
  },

  async submitForm() {
    const { associationIdStr, formData, bgImageList } = this.data

    this.setData({ submitting: true })
    try {
      const payload = {
        alumniAssociationId: associationIdStr,
        associationProfile: formData.associationProfile,
        contactInfo: formData.contactInfo,
        location: formData.location,
        logo: formData.logo,
        bgImg: bgImageList.length > 0 ? JSON.stringify(bgImageList) : '',
        // 主要负责人信息
        chargeWxId: formData.chargeWxId,
        chargeName: formData.chargeName,
        chargeRole: formData.chargeRole,
        chargeSocialAffiliation: formData.chargeSocialAffiliation,
        // 联系人信息
        zhWxId: formData.zhWxId,
        zhName: formData.zhName,
        zhRole: formData.zhRole,
        zhPhone: formData.zhPhone,
        zhSocialAffiliation: formData.zhSocialAffiliation,
      }

      const res = await put('/alumniAssociationManagement/update', payload)

      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '保存失败', icon: 'none' })
      }
    } catch (err) {
      wx.showToast({ title: '系统错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 跳转到反馈页面
  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback?type=1&title=' + encodeURIComponent('完善校友会信息遇到问题'),
    })
  },
})
