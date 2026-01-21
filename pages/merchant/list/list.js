// pages/merchant/list/list.js
const { merchantApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconSearch: config.getIconUrl('sslss.png'),
    iconCategory: config.getIconUrl('xx.png'),
    iconStar: config.getIconUrl('star.png'),
    keyword: '',
    filters: [
      { label: '商铺类型', options: ['全部类型', '个体商户', '企业'], selected: 0 },
      { label: '会员等级', options: ['全部等级', '普通会员', '银卡会员', '金卡会员', '钻石会员'], selected: 0 },
      { label: '业务类别', options: ['全部类别', '餐饮', '零售', '服务', '娱乐'], selected: 0 },
      { label: '校友认证', options: ['全部', '已认证'], selected: 0 }
    ],
    showFilterOptions: false,
    activeFilterIndex: -1,
    merchantList: [],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadMerchantList(true)
  },

  onPullDownRefresh() {
    this.loadMerchantList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMerchantList(false)
    }
  },

  async loadMerchantList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    this.setData({ loading: true })

    const { keyword, filters, current, pageSize } = this.data
    const [typeFilter, tierFilter, categoryFilter, certFilter] = filters

    // 构建请求参数
    const params = {
      current: reset ? 1 : current,
      pageSize: pageSize,
      sortField: 'createTime',
      sortOrder: 'descend'
    }

    // 商铺名称搜索
    if (keyword && keyword.trim()) {
      params.merchantName = keyword.trim()
    }

    // 商铺类型筛选
    if (typeFilter.selected === 1) {
      params.merchantType = 0 // 个体商户
    } else if (typeFilter.selected === 2) {
      params.merchantType = 1 // 企业
    }

    // 会员等级筛选
    if (tierFilter.selected > 0) {
      params.memberTier = tierFilter.selected // 1-普通, 2-银卡, 3-金卡, 4-钻石
    }

    // 业务类别筛选
    if (categoryFilter.selected > 0) {
      params.businessCategory = categoryFilter.options[categoryFilter.selected]
    }

    // 校友认证筛选
    if (certFilter.selected === 1) {
      params.isAlumniCertified = 1
    }

    try {
      const res = await merchantApi.getMerchantPage(params)
      console.log('商铺列表接口返回:', res)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []

        // 数据映射
        const mappedList = records.map(item => this.mapMerchantItem(item))

        // 更新列表数据
        const finalList = reset ? mappedList : [...this.data.merchantList, ...mappedList]

        this.setData({
          merchantList: finalList,
          current: reset ? 2 : current + 1,
          hasMore: data.hasNext || false,
          loading: false
        })

        if (reset) {
          wx.stopPullDownRefresh()
        }
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
        if (reset) {
          wx.stopPullDownRefresh()
        }
      }
    } catch (error) {
      console.error('加载商铺列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      if (reset) {
        wx.stopPullDownRefresh()
      }
    }
  },

  // 数据映射：将后端数据映射为前端所需格式
  mapMerchantItem(item) {
    // 会员等级名称映射
    const tierNames = ['', '普通会员', '银卡会员', '金卡会员', '钻石会员']
    const tierName = tierNames[item.memberTier] || ''

    return {
      merchantId: item.merchantId,
      merchantName: item.merchantName || '未命名商户',
      merchantType: item.merchantType || 0,
      businessCategory: item.businessCategory || '',
      memberTier: item.memberTier || 0,
      tierName: tierName,
      ratingScore: item.ratingScore ? item.ratingScore.toFixed(1) : '0.0',
      ratingCount: item.ratingCount || 0,
      shopCount: item.shopCount || 0,
      totalCouponIssued: item.totalCouponIssued || 0,
      isAlumniCertified: item.isAlumniCertified || 0,
      alumniAssociationId: item.alumniAssociationId || '',
      legalPerson: item.legalPerson || '',
      contactPhone: item.contactPhone || '',
      createTime: item.createTime || '',
      updateTime: item.updateTime || ''
    }
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.loadMerchantList(true)
  },

  openFilterOptions(e) {
    const { index } = e.currentTarget.dataset
    if (this.data.activeFilterIndex === index && this.data.showFilterOptions) {
      this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
      return
    }
    this.setData({ activeFilterIndex: index, showFilterOptions: true })
  },

  selectFilterOption(e) {
    const { optionIndex } = e.currentTarget.dataset
    const { activeFilterIndex, filters } = this.data
    if (activeFilterIndex === -1) return
    filters[activeFilterIndex].selected = optionIndex
    this.setData({
      filters,
      showFilterOptions: false,
      activeFilterIndex: -1
    })
    this.loadMerchantList(true)
  },

  closeFilterOptions() {
    this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})