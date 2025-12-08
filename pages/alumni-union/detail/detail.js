
// pages/alumni-union/detail/detail.js
const { unionApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    unionId: '',
    unionInfo: null,
    loading: true
  },

  async onLoad(options) {
    const { id } = options
    if (!id) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    this.setData({ unionId: id })
    await this.loadUnionDetail()
  },

  onShareAppMessage() {
    return {
      title: this.data.unionInfo?.headquartersName || '校友总会',
      path: `/pages/alumni-union/detail/detail?id=${this.data.unionId}`
    }
  },

  // 加载校友总会详情（/AlumniHeadquarters/{id}）
  async loadUnionDetail() {
    this.setData({ loading: true })
    try {
      const res = await unionApi.getUnionDetail(this.data.unionId)
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const schoolInfo = data.schoolInfo || {}

        // 将后端返回的数据映射到前端需要的格式，同时保留所有后端字段名
        const unionInfo = {
          // ===== 后端原始字段（名称与 AlumniHeadquartersDetailVo / SchoolListVo 保持一致）=====
          headquartersId: data.headquartersId,              // 校友总会ID
          headquartersName: data.headquartersName || '',    // 校友总会名称
          schoolInfo: schoolInfo,                           // 学校信息对象
          // schoolInfo 解构快捷访问
          schoolId: schoolInfo.schoolId,
          logo: schoolInfo.logo,
          schoolName: schoolInfo.schoolName,
          province: schoolInfo.province,
          city: schoolInfo.city,
          level: schoolInfo.level,
          foundingDate: schoolInfo.foundingDate,
          officialCertification: schoolInfo.officialCertification,

          description: data.description || '',
          contactInfo: data.contactInfo || '',
          address: data.address || '',
          website: data.website || '',
          wechatPublicAccount: data.wechatPublicAccount || '',
          email: data.email || '',
          phone: data.phone || '',
          establishedDate: data.establishedDate || '',
          memberCount: data.memberCount || 0,
          activeStatus: data.activeStatus,
          approvalStatus: data.approvalStatus,
          level: data.level,
          createdUser: data.createdUser || null,
          updatedUser: data.updatedUser || null,

          // ===== 前端展示补充字段 =====
          cover: schoolInfo.logo || config.defaultCover,
          icon: schoolInfo.logo || config.defaultAlumniAvatar,
          hasApp: !!data.wechatPublicAccount
        }

        this.setData({
          unionInfo,
          loading: false
        })
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载校友总会详情失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 复制官网链接
  copyWebsite() {
    const { unionInfo } = this.data
    if (unionInfo && unionInfo.website) {
      wx.setClipboardData({
        data: unionInfo.website,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  // 拨打电话
  makeCall(e) {
    const phone = e.currentTarget.dataset.phone
    if (!phone) return
    wx.makePhoneCall({
      phoneNumber: phone
    })
  },

  // 复制邮箱
  copyEmail(e) {
    const email = e.currentTarget.dataset.email
    if (!email) return
    wx.setClipboardData({
      data: email,
      success: () => {
        wx.showToast({
          title: '已复制',
          icon: 'success'
        })
      }
    })
  },

  // 跳转到对接的小程序（预留，暂时只提示）
  navigateToMiniProgram() {
    wx.showToast({
      title: '小程序跳转配置中',
      icon: 'none'
    })
  }
})
