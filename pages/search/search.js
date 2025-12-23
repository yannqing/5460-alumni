// pages/search/search.js
const { searchApi } = require('../../api/api.js')
const config = require('../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconScan: config.getIconUrl('sys.png'),
    iconSearch: config.getIconUrl('sslss.png'),
    keyword: '',
    activeTab: 0,
    tabs: ['全部', '母校', '校友会', '校友'],
    hotSearchList: ['南京大学', '上海校友会', '计算机', '优惠券'],
    searchHistory: [],
    searchResult: {
      schools: [],
      associations: [],
      alumni: []
    },
    showResult: false
  },

  onLoad() {
    const history = wx.getStorageSync('searchHistory') || []
    this.setData({ searchHistory: history })
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    const { keyword } = this.data
    if (!keyword.trim()) {
      wx.showToast({ title: '请输入搜索内容', icon: 'none' })
      return
    }

    this.saveSearchHistory(keyword)
    this.performSearch(keyword)
  },

  saveSearchHistory(keyword) {
    let history = this.data.searchHistory
    history = history.filter(item => item !== keyword)
    history.unshift(keyword)
    history = history.slice(0, 10)
    this.setData({ searchHistory: history })
    wx.setStorageSync('searchHistory', history)
  },

  async performSearch(keyword) {
    try {
      wx.showLoading({ title: '搜索中...' })

      const { activeTab } = this.data
      
      // 根据当前tab确定搜索类型
      // 后端SearchType枚举：ALUMNI(校友), ASSOCIATION(校友会), MERCHANT(商户), ALL(全部)
      let types = []
      if (activeTab === 0) {
        // 全部：搜索所有类型
        types = ['ALUMNI', 'ASSOCIATION']
      } else if (activeTab === 1) {
        // 母校：统一搜索接口不支持学校搜索
        types = []
      } else if (activeTab === 2) {
        // 校友会
        types = ['ASSOCIATION']
      } else if (activeTab === 3) {
        // 校友
        types = ['ALUMNI']
      }

      // 如果没有可搜索的类型，显示空结果
      if (types.length === 0) {
        wx.hideLoading()
        this.setData({
          searchResult: {
            schools: [],
            associations: [],
            alumni: []
          },
          showResult: true
        })
        return
      }

      // 构建请求参数
      const requestParams = {
        keyword: keyword.trim(),
        types: types,
        pageNum: 1,
        pageSize: 20,
        highlight: true
      }

      console.log('[Search] 统一搜索请求参数:', requestParams)

      // 调用统一搜索接口
      const res = await searchApi.unifiedSearch(requestParams)
      wx.hideLoading()

      console.log('[Search] 统一搜索响应:', res.data)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const items = data.items || []
        const typeCounts = data.typeCounts || {}

        console.log('[Search] 原始响应数据:', JSON.stringify(data, null, 2))
        console.log('[Search] items数量:', items.length)
        console.log('[Search] items内容:', JSON.stringify(items, null, 2))

        // 将搜索结果按类型分类
        const schools = []
        const associations = []
        const alumni = []

        items.forEach((item, index) => {
          // 处理type字段：可能是字符串、枚举对象或枚举code
          let type = item.type
          if (typeof type === 'object' && type !== null) {
            // 如果是对象，可能是枚举对象，尝试获取code属性
            type = type.code || type.name || type
          }
          // 确保type是字符串
          type = String(type).toUpperCase()
          
          const extra = item.extra || {}

          console.log(`[Search] 处理第${index + 1}项:`, {
            type: type,
            id: item.id,
            title: item.title,
            extra: extra
          })

          if (type === 'ALUMNI') {
            // 校友结果
            alumni.push({
              id: item.id,
              name: item.title || '',
              avatar: item.avatar ? config.getImageUrl(item.avatar) : config.defaultAvatar,
              school: extra.schoolName || item.subtitle || '',
              company: extra.company || ''
            })
          } else if (type === 'ASSOCIATION') {
            // 校友会结果
            associations.push({
              id: item.id,
              name: item.title || '',
              location: extra.location || extra.city || extra.province || item.subtitle || '',
              memberCount: extra.memberCount || extra.memberNum || 0
            })
          } else if (type === 'SCHOOL' || type === 'MERCHANT') {
            // 母校或商户结果（暂时不支持，但保留处理逻辑）
            // 可以根据需要扩展
          } else {
            console.warn(`[Search] 未知的搜索类型: ${type}`, item)
          }
        })

        this.setData({
          searchResult: {
            schools: schools,
            associations: associations,
            alumni: alumni
          },
          showResult: true
        })

        console.log('[Search] 搜索结果处理完成:', {
          schools: schools.length,
          associations: associations.length,
          alumni: alumni.length
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '搜索失败',
          icon: 'none'
        })
        this.setData({
          searchResult: {
            schools: [],
            associations: [],
            alumni: []
          },
          showResult: true
        })
      }
    } catch (err) {
      wx.hideLoading()
      console.error('[Search] 搜索异常:', err)
      wx.showToast({
        title: '搜索失败，请稍后重试',
        icon: 'none'
      })
      this.setData({
        searchResult: {
          schools: [],
          associations: [],
          alumni: []
        },
        showResult: true
      })
    }
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeTab: index })
    
    // 如果已经搜索过，切换tab后重新搜索
    if (this.data.showResult && this.data.keyword.trim()) {
      this.performSearch(this.data.keyword)
    }
  },

  onHotSearchTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.onSearch()
  },

  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.onSearch()
  },

  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({ searchHistory: [] })
          wx.removeStorageSync('searchHistory')
        }
      }
    })
  },

  viewSchoolDetail(e) {
    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewAssociationDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  viewAlumniDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  scanCode() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码结果:', res)
        // 处理扫码结果，可能是优惠券、商家等
        const result = res.result
        if (result) {
          // 如果是URL，可以跳转
          if (result.startsWith('http')) {
            wx.showModal({
              title: '扫码结果',
              content: `检测到链接：${result}`,
              showCancel: true,
              confirmText: '打开',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  // 可以在这里处理链接跳转
                  wx.showToast({
                    title: '功能开发中',
                    icon: 'none'
                  })
                }
              }
            })
          } else {
            // 其他类型的扫码结果，可以用于搜索
            this.setData({ keyword: result })
            this.onSearch()
          }
        }
      },
      fail: (err) => {
        console.error('扫码失败:', err)
        if (err.errMsg !== 'scanCode:fail cancel') {
          wx.showToast({
            title: '扫码失败',
            icon: 'none'
          })
        }
      }
    })
  }
})
