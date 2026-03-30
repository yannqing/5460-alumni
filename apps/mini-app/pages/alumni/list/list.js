// pages/alumni/list/list.js
const { alumniApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const {
  FollowTargetType,
  loadAndUpdateFollowStatus,
  handleListItemFollow,
} = require('../../../utils/followHelper.js')

Page({
  data: {
    // 图标路径
    iconSearch: '../../../assets/icons/magnifying glass.png',
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    // 校友认证等级图片
    alumniCertFirstImg:
      'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/alumni_first_certification.png',
    alumniCertSecondImg:
      'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/alumni_second_certification.png',
    alumniCertThirdImg:
      'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/alumni_third_certification.png',
    keyword: '',
    filters: [
      { label: '注册时间', options: ['升序', '降序'], selected: 0 },
      { label: '性别', options: ['全部', '男', '女'], selected: 0 },
      { label: '关注', options: ['我的关注'], selected: 0 },
    ],
    // 关注筛选下拉选择器
    followFilterOptions: ['全部', '我的关注'],
    followFilterIndex: 0,
    myFollow: 0, // 0-全部，1-仅我关注的
    topImageUrl: `https://${config.DOMAIN}/upload/images/2026/02/26/1fbd821e-3a41-41eb-b284-d11d0296a2dc.png`,
    alumniList: [],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    refreshing: false,
    scrollTop: 0,
    refresherHeight: 0,
    scrollThreshold: 100,
  },

  onLoad() {
    // 计算图片区域高度用于吸顶阈值
    this.initMeasurements()

    this.loadAlumniList(true)
  },

  // 初始化测量数据
  initMeasurements() {
    setTimeout(() => {
      const query = wx.createSelectorQuery()
      query.select('.banner-area').boundingClientRect()
      query.exec(res => {
        if (res && res[0]) {
          this.setData({
            scrollThreshold: res[0].height,
          })
        }
      })
    }, 300)
  },

  onPullDownRefresh() {
    // 页面级下拉刷新已禁用，直接停止
    wx.stopPullDownRefresh()
  },

  /**
   * 滚动事件（节流：200ms 内只处理一次底部检测）
   */
  onScroll: function (e) {
    const { scrollTop, scrollHeight } = e.detail
    this.setData({ scrollTop })

    // 节流：避免高频触发加载
    const now = Date.now()
    if (this._lastScrollCheck && now - this._lastScrollCheck < 200) return
    this._lastScrollCheck = now

    // 手动检测是否接近底部（兼容 bindscrolltolower 不触发的情况）
    if (scrollHeight && scrollHeight > 0) {
      // 获取 scroll-view 的可视高度（首次时查询并缓存）
      if (!this._scrollViewHeight) {
        const query = wx.createSelectorQuery()
        query.select('.main-scroller').boundingClientRect()
        query.exec(res => {
          if (res && res[0]) {
            this._scrollViewHeight = res[0].height
          }
        })
        return
      }
      const distanceToBottom = scrollHeight - scrollTop - this._scrollViewHeight
      if (distanceToBottom < 150 && this.data.hasMore && !this._isLoading) {
        this.loadAlumniList(false)
      }
    }
  },

  /**
   * 触摸开始事件
   */
  onTouchStart: function (e) {
    if (this.data.scrollTop <= 5) {
      this.startY = e.touches[0].pageY
      this.canPull = true
    } else {
      this.canPull = false
    }
  },

  /**
   * 触摸移动事件
   */
  onTouchMove: function (e) {
    if (!this.canPull || this.data.refreshing) return

    const moveY = e.touches[0].pageY
    const diff = (moveY - this.startY) * 0.5 // 阻尼效果

    if (diff > 0) {
      this.setData({
        refresherHeight: Math.min(diff, 80),
      })
    }
  },

  /**
   * 触摸结束事件
   */
  onTouchEnd: function () {
    if (!this.canPull || this.data.refreshing) return

    if (this.data.refresherHeight >= 40) {
      this.setData({
        refreshing: true,
        refresherHeight: 60,
      })
      this.loadAlumniList(true)
    } else {
      this.setData({
        refresherHeight: 0,
      })
    }
  },

  // scroll-view 下拉刷新（保留兼容接口，主要走 onTouchEnd）
  onScrollViewRefresh() {
    this.setData({ refreshing: true })
    this.loadAlumniList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this._isLoading) {
      this.loadAlumniList(false)
    }
  },

  async loadAlumniList(reset = false) {
    // 使用同步实例变量防重入（setData 是异步的，this.data.loading 无法阻止高频并发调用）
    if (this._isLoading) {
      return
    }
    if (!reset && !this.data.hasMore) {
      return
    }

    this._isLoading = true
    this.setData({ loading: true })

    const { keyword, filters, current, pageSize, myFollow } = this.data
    const [sortFilter, genderFilter] = filters

    // 构建请求参数
    const params = {
      current: reset ? 1 : current,
      pageSize: pageSize,
      // 关注筛选：0-全部，1-仅我关注的
      myFollow: myFollow,
    }

    // 注册时间排序
    if (sortFilter.selected >= 0) {
      // params.sortField = 'createTime'
      params.sortOrder = sortFilter.selected == 1 ? 'descend' : 'ascend'
    }

    // 性别筛选
    if (genderFilter.selected > 0) {
      params.gender = genderFilter.selected // 1-男，2-女
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
          alumniApi.queryAlumniList(params2),
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
                current: params.current,
                size: pageSize,
              },
              msg: 'success',
            },
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
        const total = data.total || 0

        // 数据映射
        const mappedList = records.map(item => this.mapAlumniItem(item))

        // 更新列表数据（去重，防止分页重叠导致 wx:key 重复）
        let finalList
        if (reset) {
          finalList = mappedList
        } else {
          const existingIds = new Set(this.data.alumniList.map(item => item.id))
          const newItems = mappedList.filter(item => !existingIds.has(item.id))
          finalList = [...this.data.alumniList, ...newItems]
        }

        // 判断是否还有更多数据：优先用 total 判断，兜底用返回记录数判断
        const hasMore = total > 0 ? finalList.length < total : records.length >= pageSize

        this._isLoading = false
        this.setData({
          alumniList: finalList,
          current: reset ? 2 : current + 1,
          hasMore: hasMore,
          loading: false,
          refreshing: false,
          refresherHeight: 0,
        })

        wx.stopPullDownRefresh()

        // 加载完列表后，获取关注状态（使用工具类方法）
        loadAndUpdateFollowStatus(this, 'alumniList', FollowTargetType.USER)
      } else {
        this._isLoading = false
        this.setData({ loading: false, refreshing: false, refresherHeight: 0 })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none',
        })
        wx.stopPullDownRefresh()
      }
    } catch (error) {
      console.error('加载校友列表失败:', error)
      this._isLoading = false
      this.setData({ loading: false, refreshing: false, refresherHeight: 0 })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      })
      wx.stopPullDownRefresh()
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
      id: item.wxId, // 使用后端返回的 wxId 字段作为用户ID
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
      certificationFlag: item.certificationFlag || 0, // 认证等级：0-未认证，1-一级认证，2-二级认证，3-三级认证
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
      birthDate: item.birthDate || '',
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

  // 注册时间筛选
  onSortChange(e) {
    const filters = this.data.filters
    filters[0].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  // 性别筛选
  onGenderChange(e) {
    const filters = this.data.filters
    filters[1].selected = e.detail.value
    this.setData({ filters })
    this.loadAlumniList(true)
  },

  // 关注筛选变更（下拉选择器）
  onFollowFilterChange(e) {
    const index = parseInt(e.detail.value)
    this.setData({
      followFilterIndex: index,
      myFollow: index, // 0-全部，1-仅我关注的
      current: 1,
    })
    this.loadAlumniList(true)
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${id}`,
      events: {
        // 监听来自详情页的状态更新
        updateFollowStatus: data => {
          console.log('接收到详情页关注状态同步请求:', data)
          const { id, isFollowed, followStatus, isFriend } = data

          const alumniList = this.data.alumniList
          const index = alumniList.findIndex(item => item.id === id)

          if (index !== -1) {
            const key = `alumniList[${index}]`
            this.setData({
              [`${key}.isFollowed`]: isFollowed,
              [`${key}.followStatus`]: followStatus,
              [`${key}.isFriend`]: isFriend,
            })
          }
        },
      },
    })
  },

  // 跳转到认证说明页面
  goToCertificationInfo() {
    wx.navigateTo({
      url: '/pages/certification-info/certification-info',
    })
  },

  // 关注/取消关注（使用工具类方法）
  async toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    await handleListItemFollow(this, 'alumniList', id, followed, FollowTargetType.USER)
  },
})
