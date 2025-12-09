// pages/alumni/list/list.js
const { alumniApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const { FollowTargetType, toggleFollow } = require('../../../utils/followHelper.js')

Page({
  data: {
    // 图标路径
    iconSearch: config.getIconUrl('sslss.png'),
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    keyword: '',
    filters: [
      { label: '身份', options: ['全部校友', '企业高管', '创业校友', '在读校友'], selected: 0 },
      { label: '城市', options: ['全部城市', '南京', '上海', '杭州', '深圳'], selected: 0 },
      { label: '排序', options: ['默认排序', '最新加入', '人气最高'], selected: 0 },
      { label: '关注', options: ['全部', '我的关注'], selected: 0 }
    ],
    showFilterOptions: false,
    activeFilterIndex: -1,
    alumniList: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadAlumniList(true)
  },

  onPullDownRefresh() {
    this.loadAlumniList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadAlumniList(false)
    }
  },

  async loadAlumniList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    this.setData({ loading: true })

    const { keyword, filters, page, pageSize } = this.data
    const [identityFilter, cityFilter, sortFilter, followFilter] = filters

    // 构建请求参数
    const params = {
      page: reset ? 1 : page,
      size: pageSize
    }

    // 搜索关键词
    if (keyword && keyword.trim()) {
      params.keyword = keyword.trim()
    }

    // 身份筛选
    if (identityFilter.selected > 0) {
      params.identity = identityFilter.options[identityFilter.selected]
    }

    // 城市筛选
    if (cityFilter.selected > 0) {
      params.city = cityFilter.options[cityFilter.selected]
    }

    // 排序方式
    if (sortFilter.selected === 1) {
      params.sortBy = 'createTime' // 最新加入
      params.sortOrder = 'desc'
    } else if (sortFilter.selected === 2) {
      params.sortBy = 'followerCount' // 人气最高
      params.sortOrder = 'desc'
    }

    // 关注筛选
    if (followFilter.selected === 1) {
      params.onlyFollowed = true
    }

    try {
      const res = await alumniApi.queryAlumniList(params)
      console.log('校友列表接口返回:', res)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []

        // 数据映射
        const mappedList = records.map(item => this.mapAlumniItem(item))

        this.setData({
          alumniList: reset ? mappedList : [...this.data.alumniList, ...mappedList],
          page: reset ? 2 : page + 1,
          hasMore: mappedList.length >= pageSize,
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
      console.error('加载校友列表失败:', error)
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
  mapAlumniItem(item) {
    // 处理头像 - 使用后端返回的 avatarUrl 字段
    let avatarUrl = item.avatarUrl || ''
    if (avatarUrl) {
      avatarUrl = config.getImageUrl(avatarUrl)
    }

    // 构建位置信息 - 优先使用完整的位置组合
    let location = ''
    const locationParts = []

    if (item.curProvince) {
      locationParts.push(item.curProvince)
    }
    if (item.curCity) {
      locationParts.push(item.curCity)
    }

    if (locationParts.length > 0) {
      location = locationParts.join('')
    } else if (item.curCountry) {
      location = item.curCountry
    } else if (item.curContinent) {
      location = item.curContinent
    }

    // 如果没有位置信息，显示默认文本
    if (!location) {
      location = '未设置位置'
    }

    // 显示昵称，如果没有则显示真实姓名
    const displayName = item.nickname || item.name || '未知用户'

    // 返回统一格式
    return {
      id: item.wxId,  // 使用后端返回的 wxId 字段作为用户ID
      name: displayName,
      avatarUrl: avatarUrl,
      school: '暂无学校信息', // 后端接口未返回学校信息，显示占位文本
      city: item.curCity || '',
      location: location,
      major: '', // 后端接口未返回专业信息
      graduateYear: '', // 后端接口未返回毕业年份
      company: '', // 后端接口未返回公司信息
      position: '', // 后端接口未返回职位信息
      followerCount: 0, // 后端接口未返回粉丝数
      followingCount: 0, // 后端接口未返回关注数
      isFollowed: false, // 后端接口未返回关注状态
      isCertified: false, // 后端接口未返回认证状态
      tags: [], // 后端接口未返回标签
      identity: '', // 后端接口未返回身份
      // 保留后端原始字段
      wxId: item.wxId,
      phone: item.phone || '',
      wxNum: item.wxNum || '',
      qqNum: item.qqNum || '',
      email: item.email || '',
      gender: item.gender || 0,
      signature: item.signature || '',
      constellation: item.constellation || 0,
      identifyCode: item.identifyCode || '',
      birthDate: item.birthDate || ''
    }
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.loadAlumniList(true)
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
    this.loadAlumniList(true)
  },

  closeFilterOptions() {
    this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  async toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { alumniList } = this.data
    const index = alumniList.findIndex(item => item.id === id)

    if (index === -1) return

    // 调用通用关注接口
    const result = await toggleFollow(
      followed,
      FollowTargetType.USER, // 1-用户
      id
    )

    if (result.success) {
      // 更新列表中的关注状态
      alumniList[index].isFollowed = !followed
      this.setData({ alumniList })

      wx.showToast({
        title: result.message,
        icon: 'success'
      })
    } else {
      wx.showToast({
        title: result.message,
        icon: 'none'
      })
    }
  }
})
