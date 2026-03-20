// pages/info-maintenance/edit/edit.js
const { get, put } = require('../../../utils/request.js')
const { fileApi, alumniApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    formData: {
      associationName: '',
      associationProfile: '',
      contactInfo: '',
      location: '',
      logo: '',
      bgImg: '',
      status: 1,
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
    submitting: false,
    uploadingLogo: false,
    uploadingBgImage: false,
    // 地区选择器
    regionValue: [],
    // 绑定用户相关
    showBindUserModal: false,
    bindUserType: '', // 'charge' 或 'zh'
    bindSearchKeyword: '',
    bindUserList: [],
    bindSearching: false,
    bindSearched: false,
    selectedBindUser: null,
  },
  onLoad(options) {
    this.alumniAssociationId = options.alumniAssociationId
    this.loadAlumniDetail()
  },

  // 返回上一页
  onBack() {
    wx.navigateBack()
  },

  async loadAlumniDetail() {
    try {
      const res = await get(`/alumniAssociationManagement/detail/${this.alumniAssociationId}`)

      if (res.data && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        // 处理背景图（兼容数组和单张，只取第一张）
        let bgImg = ''
        if (data.bgImg) {
          try {
            const cleanedStr = (data.bgImg || '').toString().trim().replace(/^["']|['"]$/g, '')
            const parsed = JSON.parse(cleanedStr)
            bgImg = Array.isArray(parsed) ? (parsed[0] || '') : (parsed || '')
          } catch {
            bgImg = typeof data.bgImg === 'string' ? data.bgImg : ''
          }
        }

        const locationStr = data.location || ''
        const regionValue = locationStr ? locationStr.split(' ') : []

        this.setData({
          regionValue,
          formData: {
            associationName: data.associationName || '',
            associationProfile: data.associationProfile || '',
            contactInfo: data.contactInfo || '',
            location: data.location || '',
            logo: data.logo || '',
            bgImg: bgImg,
            status: data.status || 1,
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
        })
      }
    } catch (error) {
      console.error('Failed to load alumni detail:', error)
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value

    this.setData({
      [`formData.${field}`]: value,
    })
  },

  handleStatusChange(e) {
    this.setData({
      'formData.status': parseInt(e.detail.value),
    })
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

        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true,
        })

        // 上传图片到服务器
        fileApi
          .uploadImage(tempFilePath)
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              // 上传成功，更新表单数据
              that.setData({
                [`formData.logo`]: res.data.fileUrl,
                uploadingLogo: false,
              })
              wx.showToast({
                title: '上传成功',
                icon: 'success',
              })
            } else {
              wx.showToast({
                title: res.msg || '上传失败',
                icon: 'none',
              })
              that.setData({ uploadingLogo: false })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none',
            })
            console.error('上传logo失败:', err)
            that.setData({ uploadingLogo: false })
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail(err) {
        console.error('选择图片失败:', err)
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
          this.setData({
            [`formData.logo`]: '',
          })
        }
      },
    })
  },

  // 上传背景图（与 logo 逻辑一致，仅支持单张）
  onUploadBgImage() {
    const that = this

    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFilePaths[0]
        that.setData({ uploadingBgImage: true })

        wx.showLoading({
          title: '上传中...',
          mask: true,
        })

        fileApi
          .uploadImage(tempFilePath)
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              that.setData({
                [`formData.bgImg`]: res.data.fileUrl,
                uploadingBgImage: false,
              })
              wx.showToast({
                title: '上传成功',
                icon: 'success',
              })
            } else {
              wx.showToast({
                title: res.msg || '上传失败',
                icon: 'none',
              })
              that.setData({ uploadingBgImage: false })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none',
            })
            console.error('上传背景图失败:', err)
            that.setData({ uploadingBgImage: false })
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail(err) {
        console.error('选择图片失败:', err)
        that.setData({ uploadingBgImage: false })
      },
    })
  },

  // 删除背景图
  onDeleteBgImage() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除背景图吗？',
      success: res => {
        if (res.confirm) {
          this.setData({
            [`formData.bgImg`]: '',
          })
          wx.showToast({
            title: '删除成功',
            icon: 'success',
          })
        }
      },
    })
  },

  async submitForm() {
    this.setData({ submitting: true })

    try {
      const formData = this.data.formData
      const payload = {
        alumniAssociationId: this.alumniAssociationId,
        associationProfile: formData.associationProfile,
        contactInfo: formData.contactInfo,
        location: formData.location,
        logo: formData.logo,
        bgImg: formData.bgImg ? JSON.stringify([formData.bgImg]) : '',
        status: formData.status,
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
        wx.showToast({ title: '更新成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '更新失败', icon: 'none' })
      }
    } catch (error) {
      console.error('Submit error:', error)
      wx.showToast({ title: '网络错误', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
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

      // 判断是手机号还是姓名
      if (/^\d{11}$/.test(keyword)) {
        params.phone = keyword
      } else {
        params.keyword = keyword
        params.name = keyword
      }

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
      console.error('搜索用户失败:', error)
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

  // 更换用户（清空原绑定信息）
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
            // 清空主要负责人信息
            this.setData({
              'formData.chargeWxId': '',
              'formData.chargeName': '',
              'formData.chargeRole': '',
              'formData.chargeSocialAffiliation': '',
              'formData.contactInfo': '',
            })
          } else if (type === 'zh') {
            // 清空联系人信息
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
})
