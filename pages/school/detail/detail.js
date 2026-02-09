// pages/school/detail/detail.js
const { schoolApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

const DEFAULT_SCHOOL_AVATAR = config.defaultSchoolAvatar
const DEFAULT_COVER = config.defaultCover
const DEFAULT_ALUMNI_AVATAR = config.defaultAlumniAvatar

Page({
  data: {
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    iconPeople: '/assets/icons/people.png',
    schoolId: '',
    schoolInfo: null,
    loading: true,
    
    // 校友会列表
    associationList: []
  },

  onLoad(options) {
    const { id } = options
    if (id) {
      this.setData({ schoolId: id })
      this.loadSchoolDetail()
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  onShareAppMessage() {
    return {
      title: this.data.schoolInfo?.name || '母校详情',
      path: `/pages/school/detail/detail?id=${this.data.schoolId}`
    }
  },

  // 加载学校详情
  async loadSchoolDetail() {
    try {
      const res = await schoolApi.getSchoolDetail(this.data.schoolId)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        
        // 数据映射（与后端 SchoolDetailVo 字段完全一致）
        const schoolInfo = {
          // ===== 后端原始字段 =====
          schoolId: data.schoolId || this.data.schoolId,
          logo: data.logo || '',
          schoolName: data.schoolName || '',
          schoolCode: data.schoolCode || '',
          province: data.province || '',
          city: data.city || '',
          level: data.level || '',
          mergedInstitutions: data.mergedInstitutions || '',
          previousName: data.previousName || '',
          otherInfo: data.otherInfo || '',
          description: data.description || '',
          foundingDate: data.foundingDate || '',
          location: data.location || '',
          officialCertification: data.officialCertification,
          // 校友总会信息（从 alumniHeadquarters 字段获取）
          alumniHeadquarters: data.alumniHeadquarters || null,
          // 校友会列表（从 alumniAssociationListVos 字段获取）
          alumniAssociationListVos: data.alumniAssociationListVos || [],

          // ===== 前端内部使用的通用字段 =====
          id: data.schoolId || this.data.schoolId,
          name: data.schoolName || '',
          icon: data.logo || DEFAULT_SCHOOL_AVATAR,
          cover: data.cover || data.coverImage || data.background || DEFAULT_COVER,
          // 兼容旧字段
          certifiedUnion: data.certifiedUnion || null,
          alumniCount: data.alumniCount || data.alumniNum || 0,
          associationCount: data.associationCount || data.associationNum || 0
        }

        // 校友会列表（从 alumniAssociationListVos 映射）
        const associationList = (schoolInfo.alumniAssociationListVos || []).map(item => ({
          // 后端原始字段
          alumniAssociationId: item.alumniAssociationId,
          associationName: item.associationName,
          schoolId: item.schoolId,
          platformId: item.platformId,
          presidentUserId: item.presidentUserId,
          contactInfo: item.contactInfo,
          location: item.location,
          memberCount: item.memberCount,
          logo: item.logo,
          // 前端通用字段
          id: item.alumniAssociationId != null ? String(item.alumniAssociationId) : '',
          name: item.associationName || '',
          icon: item.logo || DEFAULT_ALUMNI_AVATAR,
          isCertified: false,
          isJoined: false
        }))

        // 计算省市合并显示（用于位置信息）
        const provinceCityParts = []
        if (schoolInfo.province) {
          provinceCityParts.push(schoolInfo.province)
        }
        if (schoolInfo.city) {
          provinceCityParts.push(schoolInfo.city)
        }
        const provinceCity = provinceCityParts.join('')

        this.setData({
          schoolInfo: {
            ...schoolInfo,
            provinceCity: provinceCity // 省市合并字段
          },
          associationList: associationList,
          loading: false
        })

        // 设置导航栏标题
        wx.setNavigationBarTitle({
          title: schoolInfo.name || '母校详情'
        })
      } else {
        // API 返回错误
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
        setTimeout(() => {
          wx.navigateBack()
      }, 1500)
    }
    } catch (error) {
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  // 查看认证的校友总会详情
  viewCertifiedUnion() {
    const { schoolInfo } = this.data
    if (schoolInfo && schoolInfo.certifiedUnion && schoolInfo.certifiedUnion.id) {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${schoolInfo.certifiedUnion.id}`
      })
    }
  },

  // 查看校友总会详情（从 alumniHeadquarters 字段）
  viewAlumniHeadquarters() {
    const { schoolInfo } = this.data
    if (schoolInfo && schoolInfo.alumniHeadquarters && schoolInfo.alumniHeadquarters.headquartersId) {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${schoolInfo.alumniHeadquarters.headquartersId}`
      })
    } else {
      wx.showToast({
        title: '暂无校友总会信息',
        icon: 'none'
      })
    }
  },

  // 预览图片
  previewImage(e) {
    const { url } = e.currentTarget.dataset
    wx.previewImage({
      urls: [url],
      current: url
    })
  },

  // 查看校友会详情
  viewAssociationDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  // 加入/退出校友会
  toggleJoinAssociation(e) {
    const { id } = e.currentTarget.dataset
    const associationList = this.data.associationList.map(item => {
      if (item.id === id) {
        return { ...item, isJoined: !item.isJoined }
      }
      return item
    })
    this.setData({ associationList })
    
    const association = associationList.find(item => item.id === id)
    wx.showToast({
      title: association.isJoined ? '加入成功' : '已退出',
      icon: 'success',
      duration: 1500
    })
  }
})
