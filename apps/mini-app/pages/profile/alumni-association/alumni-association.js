// pages/profile/alumni-association/alumni-association.js
const { associationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const { hasManagementPermission } = require('../../../utils/auth.js')

// 状态映射
// status: 0=已禁用/待审核, 1=已发布, 2=待发布
const STATUS_MAP = {
  0: { text: '待审核', class: 'status-0' },
  1: { text: '已发布', class: 'status-1' },
  2: { text: '待发布', class: 'status-2' },
}

Page({
  data: {
    associations: [],
    loading: true,
    error: false,
    errorMsg: '',
    defaultAvatar: config.defaultAvatar,
    hasAdminPermission: false,
  },

  onLoad() {
    this.loadMyAssociations()
  },

  onShow() {
    this.setData({ hasAdminPermission: hasManagementPermission() })
    this.loadMyAssociations()
  },

  // 加载我的校友会列表（包括所有状态）
  async loadMyAssociations() {
    this.setData({ loading: true, error: false })

    try {
      // 调用获取我创建的校友会列表（包含所有状态）
      const res = await associationApi.getMyPresidentAssociations({ current: 1, pageSize: 100 })
      console.log('获取我的校友会列表:', res)

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || []
        // 处理状态显示
        const associations = records.map(item => ({
          ...item,
          statusText: STATUS_MAP[item.status]?.text || '未知',
        }))
        this.setData({
          associations: associations,
          loading: false,
        })
      } else {
        // 降级方案
        const fallbackRes = await associationApi.getMyAssociations()
        if (fallbackRes.data && fallbackRes.data.code === 200) {
          this.setData({
            associations: fallbackRes.data.data || [],
            loading: false,
          })
        } else {
          this.setData({
            error: true,
            errorMsg: res.data?.message || res.data?.msg || '获取校友会列表失败',
            loading: false,
          })
        }
      }
    } catch (error) {
      console.error('获取我的校友会列表失败:', error)
      this.setData({
        error: true,
        errorMsg: '网络错误，请稍后重试',
        loading: false,
      })
    }
  },

  // 跳转到校友会详情页
  goToAssociationDetail(e) {
    const { id, status } = e.currentTarget.dataset
    // status=0 表示待审核，不允许进入详情页
    if (status === 0) {
      wx.showToast({ title: '正在审核中，请稍后', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`,
    })
  },

  // 跳转到完善信息并发布页面
  goToPerfectInfo(e) {
    const { id } = e.currentTarget.dataset
    console.log('goToPerfectInfo called, id:', id)
    if (!id) return
    const url = `/pages/alumni-association/alumni-info-edit/alumni-info-edit?associationId=${id}`
    console.log('Navigating to:', url)

    try {
      const pages = getCurrentPages()
      console.log(
        'Current pages:',
        pages.map(p => p.route)
      )
    } catch (e) {
      console.error('getCurrentPages failed:', e)
    }

    wx.navigateTo({
      url,
      fail: err => {
        console.error('navigateTo fail:', err)
        wx.redirectTo({ url })
      },
    })
  },

  // 跳转到校友会列表页
  goToAssociationList() {
    wx.navigateTo({
      url: '/pages/alumni-association/list/list',
    })
  },

  // 跳转到校友会管理页
  goToAdmin(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({
      url: `/pages/audit/index/index?associationId=${id}`,
    })
  },
})
