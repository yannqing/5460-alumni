// pages/local-platform/detail/detail.js
const { localPlatformApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    platformId: '',
    platformInfo: null,
    loading: true,
    // 成员列表（后端暂未提供，预留字段）
    members: [],
    // 顶部标签：与校友会主页保持一致
    activeTab: 0,
    tabs: ['基本信息', '成员列表']
  },

  onLoad(options) {
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
    this.setData({ platformId: id })
    this.loadPlatformDetail()
  },

  onShareAppMessage() {
    return {
      title: this.data.platformInfo?.platformName || '校处会',
      path: `/pages/local-platform/detail/detail?id=${this.data.platformId}`
    }
  },

  // 加载校处会详情
  async loadPlatformDetail() {
    this.setData({ loading: true })
    
    try {
      const res = await localPlatformApi.getLocalPlatformDetail(this.data.platformId)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}

        // 将后端返回的数据映射到前端需要的格式，补充前端展示所需字段
        const platformInfo = {
          platformId: this.data.platformId,
          platformName: data.platformName || '',
          city: data.city || '',
          scope: data.scope || '',
          adminUserId: data.adminUserId || null,
          contactInfo: data.contactInfo || '',
          // 以下为前端展示补充字段（后端暂无则给默认值）
          icon: config.defaultAlumniAvatar,
          cover: config.defaultCover,
          location: data.city || '',
          description: data.scope || '',
          memberCount: data.memberCount || 0
        }

        this.setData({
          platformInfo,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('加载校处会详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 顶部 Tab 切换
  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
  },

  // 复制联系信息
  copyContactInfo() {
    const { platformInfo } = this.data
    if (platformInfo && platformInfo.contactInfo) {
      wx.setClipboardData({
        data: platformInfo.contactInfo,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  }
})


