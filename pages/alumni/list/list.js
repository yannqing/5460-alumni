// pages/alumni/list/list.js
const { alumniApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const { FollowTargetType, loadAndUpdateFollowStatus, handleListItemFollow } = require('../../../utils/followHelper.js')

Page({
  data: {
    // 图标路径
    iconSearch: '../../../assets/icons/magnifying glass.png',
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    keyword: '',
    filters: [
      { label: '身份', options: ['全部校友', '企业高管', '创业校友', '在读校友'], selected: 0 },
      { label: '城市', options: ['全部城市', '南京', '上海', '杭州', '深圳'], selected: 0 },
      { label: '排序', options: ['默认排序', '最新加入', '人气最高'], selected: 0 },
      { label: '关注', options: ['全部', '我的关注'], selected: 0 }
    ],
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

    // 搜索逻辑
    let requestPromise
    if (keyword && keyword.trim()) {
      const searchKey = keyword.trim()
      params.keyword = searchKey
      
      // 手机号搜索
      if (/^\d{11}$/.test(searchKey)) {
        params.phone = searchKey
        requestPromise = alumniApi.queryAlumniList(params)
      } else {
        // 混合搜索：分别请求 name 和 nickname，然后合并结果
        // 请求1：按名称搜索
        const params1 = { ...params, name: searchKey }
        // 请求2：按昵称搜索
        const params2 = { ...params, nickname: searchKey }

        requestPromise = Promise.all([
          alumniApi.queryAlumniList(params1),
          alumniApi.queryAlumniList(params2)
        ]).then(([res1, res2]) => {
          // 构造合并后的结果
          const records1 = (res1.data && res1.data.data && res1.data.data.records) || []
          const records2 = (res2.data && res2.data.data && res2.data.data.records) || []
          
          // 合并并去重（根据 wxId）
          const combined = [...records1, ...records2]
          const map = new Map()
          combined.forEach(item => {
            if (item.wxId && !map.has(item.wxId)) {
              map.set(item.wxId, item)
            }
          })
          const uniqueRecords = Array.from(map.values())
          
          // 返回伪造的响应结构，保持与单个请求一致
          return {
            data: {
              code: 200,
              data: {
                records: uniqueRecords,
                total: uniqueRecords.length,
                current: page,
                size: pageSize
              },
              msg: 'success'
            }
          }
        })
      }
    } else {
      // 无搜索关键词，正常请求
      requestPromise = alumniApi.queryAlumniList(params)
    }

    try {
      const res = await requestPromise
      console.log('校友列表接口返回:', res)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []

        // 数据映射
        const mappedList = records.map(item => this.mapAlumniItem(item))

        // 更新列表数据
        const finalList = reset ? mappedList : [...this.data.alumniList, ...mappedList]

        this.setData({
          alumniList: finalList,
          page: reset ? 2 : page + 1,
          hasMore: mappedList.length >= pageSize,
          loading: false
        })

        if (reset) {
          wx.stopPullDownRefresh()
        }

        // 加载完列表后，获取关注状态（使用工具类方法）
        loadAndUpdateFollowStatus(this, 'alumniList', FollowTargetType.USER)
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
    // 处理头像 - 使用后端返回的 avatarUrl 字段，如果为空则使用 config 中的默认头像
    let avatarUrl = item.avatarUrl || ''
    if (avatarUrl) {
      avatarUrl = config.getImageUrl(avatarUrl)
    } else {
      // 使用 config.js 中配置的默认个人头像
      avatarUrl = config.defaultAvatar
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
    const displayName = item.nickname || item.name || item.realName || '未知用户'

    // 返回统一格式（isDefaultAvatar 用于列表页仅对默认头像增大展示尺寸）
    const isDefaultAvatar = !item.avatarUrl
    
    // 从新的后端结构中获取学校信息
    const schoolName = item.primaryEducation?.schoolInfo?.schoolName || '暂无学校信息'
    
    return {
      id: item.wxId,  // 使用后端返回的 wxId 字段作为用户ID
      name: displayName,
      avatarUrl: avatarUrl,
      isDefaultAvatar: isDefaultAvatar,
      school: schoolName, // 从 primaryEducation.schoolInfo.schoolName 获取学校信息
      city: item.curCity || '',
      location: location,
      major: item.major || '', // 尝试从后端获取专业信息
      graduateYear: item.graduateYear || '', // 尝试从后端获取毕业年份
      company: item.company || '', // 尝试从后端获取公司信息
      position: item.position || '', // 尝试从后端获取职位信息
      followerCount: item.followerCount || 0, // 尝试从后端获取粉丝数
      followingCount: item.followingCount || 0, // 尝试从后端获取关注数
      isFollowed: item.isFollowed || false, // 关注状态
      followStatus: item.followStatus || 4, // 关注状态
      isCertified: item.certificationStatus === 1, // 根据 certificationStatus 判断认证状态
      tags: item.tags || [], // 尝试从后端获取标签
      identity: item.identity || '', // 尝试从后端获取身份
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

  // 身份筛选
  onIdentityChange(e) {
    const filters = this.data.filters
    filters[0].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  // 城市筛选
  onCityChange(e) {
    const filters = this.data.filters
    filters[1].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  // 排序筛选
  onSortChange(e) {
    const filters = this.data.filters
    filters[2].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  // 关注筛选
  onFollowChange(e) {
    const filters = this.data.filters
    filters[3].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },

  // 关注/取消关注（使用工具类方法）
  async toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    await handleListItemFollow(this, 'alumniList', id, followed, FollowTargetType.USER)
  }
})
