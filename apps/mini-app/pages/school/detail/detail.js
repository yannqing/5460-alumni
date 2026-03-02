// pages/school/detail/detail.js
const { unionApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

const DEFAULT_AVATAR = config.defaultAvatar
const DEFAULT_COVER = config.defaultCover

Page({
  data: {
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    iconPeople: '/assets/icons/people.png',
    headquartersId: '',
    headquartersInfo: null,
    loading: true,
    activeTab: 0,
    tabs: ['基本信息', '组织架构', '成员列表']
  },

  onLoad(options) {
    const { id } = options
    if (id) {
      this.setData({ headquartersId: id })
      this.loadHeadquartersDetail()
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
      title: this.data.headquartersInfo?.headquartersName || '校友总会详情',
      path: `/pages/school/detail/detail?id=${this.data.headquartersId}`
    }
  },

  // 加载校友总会详情
  async loadHeadquartersDetail() {
    try {
      const res = await unionApi.getUnionDetail(this.data.headquartersId)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        
        // 数据映射（与后端 AlumniHeadquartersDetailVo 字段完全一致）
        const headquartersInfo = {
          // 后端原始字段
          headquartersId: data.headquartersId || this.data.headquartersId,
          headquartersName: data.headquartersName || '',
          schoolInfo: data.schoolInfo || null,
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
          createCode: data.createCode,
          createdUser: data.createdUser || null,
          updatedUser: data.updatedUser || null,

          // 前端内部使用的通用字段
          id: data.headquartersId || this.data.headquartersId,
          name: data.headquartersName || '',
          icon: data.schoolInfo?.logo || DEFAULT_AVATAR,
          cover: DEFAULT_COVER
        }

        this.setData({
          headquartersInfo: headquartersInfo,
          loading: false
        })

        // 设置导航栏标题
        wx.setNavigationBarTitle({
          title: headquartersInfo.headquartersName || '校友总会详情'
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

  // 预览图片
  previewImage(e) {
    const { url } = e.currentTarget.dataset
    wx.previewImage({
      urls: [url],
      current: url
    })
  },

  // 切换标签
  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeTab: index })
  }
})
