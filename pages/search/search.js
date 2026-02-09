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
    tabs: ['全部', '母校', '校友会', '校友', '商铺'],
    hotSearchList: ['南京大学', '上海校友会', '计算机', '优惠券'],
    searchHistory: [],
    searchHistory: [],
    // 搜索榜单数据 (Mock)
    searchRankingList: [
      { rank: 1, keyword: '南京大学', hot: true },
      { rank: 2, keyword: '校友企业', hot: true },
      { rank: 3, keyword: '互联网协会', hot: true },
      { rank: 4, keyword: '足球俱乐部', hot: false },
      { rank: 5, keyword: '张三', hot: false },
      { rank: 6, keyword: '创业路演', hot: false },
      { rank: 7, keyword: '周杰伦', hot: false },
      { rank: 8, keyword: '人工智能', hot: false },
      { rank: 9, keyword: '区块链', hot: false },
      { rank: 10, keyword: '元宇宙', hot: false }
    ],
    searchResult: {
      schools: [],
      associations: [],
      alumni: [],
      merchants: []
    },
    showResult: false,
    // 分页相关
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    loading: false
  },

  onLoad() {
    const history = wx.getStorageSync('searchHistory') || []
    this.setData({ searchHistory: history })
  },
  
  onReady() {
    // 在页面渲染完成后，重写 custom-nav-bar 的返回方法
    const navBar = this.selectComponent('#custom-nav-bar')
    if (navBar) {
      const originalGoBack = navBar.goBack.bind(navBar)
      navBar.goBack = () => {
        this.handleNavBarBack()
      }
    }
  },
  
  // 处理导航栏返回
  handleNavBarBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      // 如果有上一页，则返回
      wx.navigateBack({
        fail: () => {
          // 如果返回失败，则清空搜索结果
          this.setData({
            showResult: false,
            keyword: '',
            searchResult: {
              schools: [],
              associations: [],
              alumni: [],
              merchants: []
            },
            pageNum: 1,
            hasMore: true
          })
        }
      })
    } else {
      // 如果没有上一页（从 tabBar 进入），则清空搜索结果
      this.setData({
        showResult: false,
        keyword: '',
        searchResult: {
          schools: [],
          associations: [],
          alumni: [],
          merchants: []
        },
        pageNum: 1,
        hasMore: true
      })
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 3
      });
      // 更新未读消息数
      this.getTabBar().updateUnreadCount();
    }
  },

  // 点击榜单项
  onRankingTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.onSearch()
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

  async performSearch(keyword, isLoadMore = false) {
    try {
      if (!isLoadMore) {
        wx.showLoading({ title: '搜索中...' })
        // 重置分页
        this.setData({
          pageNum: 1,
          hasMore: true,
          loading: false
        })
      } else {
        if (this.data.loading || !this.data.hasMore) {
          return
        }
        this.setData({ loading: true })
      }

      const { activeTab, pageNum, pageSize } = this.data

      // 根据当前tab确定搜索类型
      // 后端SearchType枚举：ALUMNI(校友), ASSOCIATION(校友会), MERCHANT(商户), SCHOOL(母校), ALL(全部)
      let types = []
      if (activeTab === 0) {
        // 全部：包含所有类型
        types = ['ALL']
      } else if (activeTab === 1) {
        // 母校
        types = ['SCHOOL']
      } else if (activeTab === 2) {
        // 校友会
        types = ['ASSOCIATION']
      } else if (activeTab === 3) {
        // 校友
        types = ['ALUMNI']
      } else if (activeTab === 4) {
        // 商铺
        types = ['MERCHANT']
      }

      // 如果没有可搜索的类型，显示空结果
      if (types.length === 0) {
        if (!isLoadMore) {
          wx.hideLoading()
        } else {
          this.setData({ loading: false })
        }
        this.setData({
          searchResult: {
            schools: [],
            associations: [],
            alumni: [],
            merchants: []
          },
          showResult: true,
          hasMore: false
        })
        return
      }

      // 构建请求参数
      const requestParams = {
        keyword: keyword.trim(),
        types: types,
        pageNum: isLoadMore ? pageNum + 1 : 1,
        pageSize: pageSize,
        highlight: true
      }

      // 调用统一搜索接口
      const res = await searchApi.unifiedSearch(requestParams)

      if (!isLoadMore) {
        wx.hideLoading()
      }

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const items = data.items || []
        const total = data.total || 0

        // 将搜索结果按类型分类
        const schools = isLoadMore ? [...this.data.searchResult.schools] : []
        const associations = isLoadMore ? [...this.data.searchResult.associations] : []
        const alumni = isLoadMore ? [...this.data.searchResult.alumni] : []
        const merchants = isLoadMore ? [...this.data.searchResult.merchants] : []

        items.forEach((item) => {
          // 处理type字段：可能是字符串、枚举对象或枚举code
          let type = item.type
          if (typeof type === 'object' && type !== null) {
            type = type.code || type.name || type
          }
          type = String(type).toUpperCase()

          const extra = item.extra || {}

          if (type === 'ALUMNI') {
            // 处理位置信息：只使用 curProvince 和 curCity，合成省和市
            let location = '';
            if (extra.curProvince || extra.curCity) {
              const parts = [extra.curProvince, extra.curCity].filter(Boolean);
              location = parts.join('');
            }

            alumni.push({
              id: item.id,
              name: item.title || '',
              avatar: item.avatar ? config.getImageUrl(item.avatar) : config.defaultAvatar,
              school: extra.schoolName || item.subtitle || '',
              company: extra.company || '',
              location: location || '',
              signature: extra.signature || ''
            })
          } else if (type === 'ASSOCIATION') {
            associations.push({
              id: item.id,
              name: item.title || '',
              location: extra.location || extra.city || extra.province || item.subtitle || '',
              memberCount: extra.memberCount || extra.memberNum || 0
            })
          } else if (type === 'MERCHANT') {
            merchants.push({
              id: item.id,
              name: item.title || '',
              avatar: item.avatar ? config.getImageUrl(item.avatar) : config.defaultAvatar,
              location: extra.location || extra.address || item.subtitle || '',
              rating: extra.rating || 0
            })
          } else if (type === 'SCHOOL') {
            schools.push({
              id: item.id,
              name: item.title || '',
              icon: item.avatar ? config.getImageUrl(item.avatar) : (item.icon ? config.getImageUrl(item.icon) : config.defaultAvatar),
              location: extra.location || extra.city || extra.province || item.subtitle || '',
              alumniCount: extra.alumniCount || extra.alumniNum || 0
            })
          } else if (type === 'ALL') {
            // ALL类型需要根据extra中的实际类型来判断
            const actualType = extra.type || extra.searchType
            if (actualType === 'ALUMNI' || actualType === '校友') {
              // 处理位置信息：只使用 curProvince 和 curCity，合成省和市
              let location = '';
              if (extra.curProvince || extra.curCity) {
                const parts = [extra.curProvince, extra.curCity].filter(Boolean);
                location = parts.join('');
              }

              alumni.push({
                id: item.id,
                name: item.title || '',
                avatar: item.avatar ? config.getImageUrl(item.avatar) : config.defaultAvatar,
                school: extra.schoolName || item.subtitle || '',
                company: extra.company || '',
                location: location || '',
                signature: extra.signature || ''
              })
            } else if (actualType === 'ASSOCIATION' || actualType === '校友会') {
              associations.push({
                id: item.id,
                name: item.title || '',
                location: extra.location || extra.city || extra.province || item.subtitle || '',
                memberCount: extra.memberCount || extra.memberNum || 0
              })
            } else if (actualType === 'MERCHANT' || actualType === '商户' || actualType === '商铺') {
              merchants.push({
                id: item.id,
                name: item.title || '',
                avatar: item.avatar ? config.getImageUrl(item.avatar) : config.defaultAvatar,
                location: extra.location || extra.address || item.subtitle || '',
                rating: extra.rating || 0
              })
            } else if (actualType === 'SCHOOL' || actualType === '母校' || actualType === '学校') {
              schools.push({
                id: item.id,
                name: item.title || '',
                icon: item.avatar ? config.getImageUrl(item.avatar) : (item.icon ? config.getImageUrl(item.icon) : config.defaultAvatar),
                location: extra.location || extra.city || extra.province || item.subtitle || '',
                alumniCount: extra.alumniCount || extra.alumniNum || 0
              })
            }
          }
        })

        // 计算是否还有更多数据
        const currentTotal = schools.length + associations.length + alumni.length + merchants.length
        const hasMore = currentTotal < total && items.length === pageSize

        this.setData({
          searchResult: {
            schools: schools,
            associations: associations,
            alumni: alumni,
            merchants: merchants
          },
          showResult: true,
          pageNum: isLoadMore ? pageNum + 1 : 1,
          hasMore: hasMore,
          loading: false
        })
      } else {
        if (!isLoadMore) {
          wx.showToast({
            title: res.data?.msg || '搜索失败',
            icon: 'none'
          })
        }
        this.setData({
          searchResult: {
            schools: [],
            associations: [],
            alumni: [],
            merchants: []
          },
          showResult: true,
          hasMore: false,
          loading: false
        })
      }
    } catch (err) {
      if (!isLoadMore) {
        wx.hideLoading()
      }
      this.setData({ loading: false })
      if (!isLoadMore) {
        wx.showToast({
          title: '搜索失败，请稍后重试',
          icon: 'none'
        })
      }
      this.setData({
        searchResult: {
          schools: [],
          associations: [],
          alumni: [],
          merchants: []
        },
        showResult: true,
        hasMore: false
      })
    }
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeTab: index })

    // 如果已经搜索过，切换tab后重新搜索
    if (this.data.showResult && this.data.keyword.trim()) {
      this.performSearch(this.data.keyword, false)
    }
  },

  // 滚动加载更多
  onReachBottom() {
    if (this.data.showResult && this.data.keyword.trim() && this.data.hasMore && !this.data.loading) {
      this.performSearch(this.data.keyword, true)
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

  viewMerchantDetail(e) {
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${e.currentTarget.dataset.id}`
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
  },

  // 返回按钮点击事件
  goBack(e) {
    // 阻止事件冒泡
    if (e && e.stopPropagation) {
      e.stopPropagation()
    }

    const pages = getCurrentPages()
    if (pages.length > 1) {
      // 如果有上一页，则返回
      wx.navigateBack({
        fail: () => {
          // 如果返回失败，则清空搜索结果
          this.setData({
            showResult: false,
            keyword: '',
            searchResult: {
              schools: [],
              associations: [],
              alumni: [],
              merchants: []
            },
            pageNum: 1,
            hasMore: true
          })
        }
      })
    } else {
      // 如果没有上一页（从 tabBar 进入），则清空搜索结果
      this.setData({
        showResult: false,
        keyword: '',
        searchResult: {
          schools: [],
          associations: [],
          alumni: [],
          merchants: []
        },
        pageNum: 1,
        hasMore: true
      })
    }
  }
})
