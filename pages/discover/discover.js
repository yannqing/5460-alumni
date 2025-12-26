// pages/discover/discover.js
const config = require('../../utils/config.js')
const { shopApi, nearbyApi } = require('../../api/api.js')

const MOCK_MERCHANTS = [
  {
    id: 1,
      name: '星巴克咖啡',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 520,
      rating: 4.8,
      location: '科技园店',
      avgPrice: 45,
      latitude: 31.2304,
      longitude: 121.4737,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 3
      },
    coupons: [
      {
        id: 1,
        title: '星巴克校友专属优惠',
        discount: '8折',
        expireDate: '2025-12-31',
        originalPrice: 100,
        discountPrice: 80
      },
      {
        id: 2,
        title: '买一送一',
        discount: '买一送一',
        expireDate: '2025-12-25',
        originalPrice: 50,
        discountPrice: 25
      },
      {
        id: 3,
        title: '满100减20',
        discount: '满100减20',
        expireDate: '2025-12-20',
        originalPrice: 100,
        discountPrice: 80
      }
    ]
  },
  {
    id: 2,
      name: '海底捞火锅',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 1280,
      rating: 4.9,
      location: '南山店',
      avgPrice: 120,
      latitude: 31.2314,
      longitude: 121.4747,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 5
      },
    coupons: [
      {
        id: 4,
        title: '海底捞校友专享',
        discount: '9折',
        expireDate: '2025-12-15',
        originalPrice: 200,
        discountPrice: 180
      },
      {
        id: 5,
        title: '满200减50',
        discount: '满200减50',
        expireDate: '2025-12-10',
        originalPrice: 200,
        discountPrice: 150
      }
    ]
  },
  {
    id: 3,
      name: '华影国际影城',
      avatar: config.defaultAvatar,
      category: 'entertainment',
      distance: 760,
      rating: 4.6,
      location: '购物中心店',
      avgPrice: 60,
      latitude: 31.2294,
      longitude: 121.4727,
      isCertified: false,
      socialProof: {
        recentAlumni: 2
      },
    coupons: [
      {
        id: 6,
        title: 'IMAX 影城观影券',
        discount: '7折',
        expireDate: '2025-11-01',
        originalPrice: 80,
        discountPrice: 56
      },
      {
        id: 7,
        title: '周末特惠',
        discount: '6折',
        expireDate: '2025-12-31',
        originalPrice: 80,
        discountPrice: 48
      }
    ]
  },
  {
    id: 4,
      name: '橙燃健身房',
      avatar: config.defaultAvatar,
      category: 'lifestyle',
      distance: 2100,
      rating: 4.7,
      location: '商业街店',
      avgPrice: 0,
      latitude: 31.2324,
      longitude: 121.4757,
      isCertified: true,
      socialProof: {
        association: '南京大学上海校友会',
        recentAlumni: 1
      },
    coupons: [
      {
        id: 8,
        title: '健身年卡伴侣价',
        discount: '立减¥800',
        expireDate: '2026-01-31',
        originalPrice: 3000,
        discountPrice: 2200
      }
    ]
  },
  {
    id: 5,
      name: '肯德基',
      avatar: config.defaultAvatar,
      category: 'dining',
      distance: 890,
      rating: 4.5,
      location: '商业街店',
      avgPrice: 35,
      latitude: 31.2284,
      longitude: 121.4717,
      isCertified: false,
    coupons: [
      {
        id: 9,
        title: '肯德基套餐优惠',
        discount: '7折',
        expireDate: '2025-12-20',
        originalPrice: 50,
        discountPrice: 35
      },
      {
        id: 10,
        title: '早餐特惠',
        discount: '6折',
        expireDate: '2025-12-18',
        originalPrice: 30,
        discountPrice: 18
      }
    ]
  }
]

Page({
  data: {
    searchValue: '',
    selectedTab: 'coupon',
    sortType: 'distance',
    loading: false,
    viewMode: 'list', // list: 列表模式, map: 地图模式
    defaultAvatar: config.defaultAvatar,
    mapCenter: {
      latitude: 31.2304, // 默认上海坐标
      longitude: 121.4737
    },
    mapScale: 15,
    mapMarkers: [],
    navTabs: [
      { id: 'coupon', label: '附近优惠', icon: '🎟️' },
      { id: 'venue', label: '附近场所', icon: '🏌️' },
      { id: 'alumni', label: '附近校友', icon: '🎓' },
      { id: 'activity', label: '附近活动', icon: '🏃' }
    ],
    sortOptions: [
      { id: 'distance', label: '距离最近' },
      { id: 'popularity', label: '好评优先' },
      { id: 'discount', label: '优惠力度' }
    ],
    alumniList: [],
    activityList: [],
    couponList: [],
    venueList: [],
    refreshing: false,
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    myLocation: null, // 自己的位置信息
    searchKeyword: '' // 搜索关键词
  },

  onLoad() {
    this.loadDiscoverData()
    // 获取当前位置信息
    this.initMyLocation()
  },

  // 初始化自己的位置信息
  initMyLocation() {
    const app = getApp()
    const location = app.globalData.location
    
    if (location && location.latitude && location.longitude) {
      this.setData({
        myLocation: {
          latitude: location.latitude,
          longitude: location.longitude
        }
      })
      // 如果当前是地图模式，更新标记
      if (this.data.viewMode === 'map') {
        this.updateMapMarkers()
      }
    } else {
      // 如果没有全局位置信息，尝试获取
      this.getLocation()
    }
  },

  // 下拉刷新
  async onPullDownRefresh() {
    console.log('[Discover] 下拉刷新触发')
    this.setData({ refreshing: true })
    
    try {
      await this.loadDiscoverData()
    } catch (error) {
      console.error('[Discover] 刷新失败:', error)
      wx.showToast({
        title: '刷新失败',
        icon: 'none'
      })
    } finally {
      this.setData({ refreshing: false })
      // 停止下拉刷新动画
      wx.stopPullDownRefresh()
    }
  },

  // 上拉加载更多
  onReachBottom() {
    console.log('[Discover] 上拉加载更多')
    // 如果正在加载或没有更多数据，则不执行
    if (this.data.loading || !this.data.hasMore) {
      return
    }
    // 根据选中的tab加载更多数据
    const tabToQueryType = {
      'coupon': 1,  // 商铺
      'venue': 2,   // 企业/场所
      'alumni': 3   // 校友
    }
    const queryType = tabToQueryType[this.data.selectedTab]
    if (queryType) {
      this.loadNearbyData(queryType, false, this.data.searchKeyword)
    }
  },

  async loadDiscoverData() {
    this.setData({ loading: true, currentPage: 1, hasMore: true })
    
    // 根据选中的tab调用统一接口
    const tabToQueryType = {
      'coupon': 1,  // 商铺
      'venue': 2,   // 企业/场所
      'alumni': 3   // 校友
    }
    
    const queryType = tabToQueryType[this.data.selectedTab]
    if (queryType) {
      await this.loadNearbyData(queryType, true)
    } else {
      // 活动tab使用模拟数据
      this.loadMockData()
    }
  },

  // 统一的附近数据加载方法
  // queryType: 1-商铺, 2-企业/场所, 3-校友
  async loadNearbyData(queryType, reset = true, keyword = '') {
    try {
      // 如果正在加载且不是重置，则不执行
      if (this.data.loading && !reset) {
        return
      }

      // 从全局数据获取位置信息
      const app = getApp()
      const location = app.globalData.location
      
      // 如果全局数据中没有位置信息，显示失败
      if (!location) {
        const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
        this.setData({
          [emptyList]: [],
          loading: false
        })
        wx.showToast({
          title: '获取位置失败，请重试',
          icon: 'none',
          duration: 2000
        })
        return
      }

      // 计算当前页码
      const currentPage = reset ? 1 : this.data.currentPage + 1
      
      const requestData = {
        queryType: queryType,
        latitude: location.latitude,
        longitude: location.longitude,
        radius: 30, // 默认30公里
        current: currentPage,
        pageSize: this.data.pageSize
      }

      // 如果有搜索关键词，添加到请求参数
      if (keyword && keyword.trim()) {
        requestData.keyword = keyword.trim()
      }

      // 调试日志：输出请求参数
      console.log('[Discover] 请求附近数据参数:', requestData)

      const res = await nearbyApi.getNearby(requestData)
      
      // 调试日志：输出响应数据
      console.log('[Discover] 附近数据响应:', res)
      console.log('[Discover] 响应code:', res.data?.code)
      console.log('[Discover] 响应data:', res.data?.data)
      console.log('[Discover] 响应msg:', res.data?.msg)
      
      // 检查响应是否成功
      if (!res || !res.data) {
        console.error('[Discover] 响应数据格式错误:', res)
        const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false
        })
        wx.showToast({
          title: '请求失败，请重试',
          icon: 'none'
        })
        return
      }
      
      // 检查业务错误码
      if (res.data.code !== 200) {
        console.error('[Discover] 接口返回错误:', res.data.code, res.data.msg)
        const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false
        })
        wx.showToast({
          title: res.data.msg || '请求失败，请重试',
          icon: 'none',
          duration: 2000
        })
        return
      }
      
      if (res.data.data) {
        const data = res.data.data
        const records = data.records || data.items || data.list || []
        const total = data.total || 0
        
        // 调试日志：输出解析后的列表
        console.log('[Discover] 解析后的数据列表:', records)
        console.log('[Discover] 数据数量:', records.length)
        console.log('[Discover] 总数量:', total)
        
        // 如果没有数据
        if (records.length === 0) {
          const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
          this.setData({
            [emptyList]: reset ? [] : this.data[emptyList],
            loading: false,
            hasMore: false
          })
          return
        }

        // 根据 queryType 处理不同类型的数据
        if (queryType === 1) {
          // 商铺类型
          const couponList = records.map(shop => {
            // 处理图片
            let image = config.defaultAvatar
            if (shop.shopImages) {
              if (Array.isArray(shop.shopImages) && shop.shopImages.length > 0) {
                image = config.getImageUrl(shop.shopImages[0])
              } else if (typeof shop.shopImages === 'string') {
                image = config.getImageUrl(shop.shopImages)
              }
            }

            // 处理距离
            let distanceText = '0m'
            if (shop.distance !== undefined && shop.distance !== null) {
              if (shop.distance < 1) {
                distanceText = Math.round(shop.distance * 1000) + 'm'
              } else {
                let kmValue = shop.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(shop.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            // 处理优惠券列表
            let coupons = []
            if (shop.coupons && Array.isArray(shop.coupons) && shop.coupons.length > 0) {
              coupons = shop.coupons.map(coupon => {
                let discount = '优惠'
                if (coupon.discountValue !== undefined && coupon.discountValue !== null) {
                  if (coupon.couponType === 1) {
                    discount = Math.round(coupon.discountValue * 10) + '折'
                  } else if (coupon.couponType === 2) {
                    discount = '满' + (coupon.minSpend || 0) + '减' + coupon.discountValue
                  } else if (coupon.couponType === 3) {
                    discount = '礼品券'
                  }
                }
                
                let type = '优惠券'
                if (coupon.couponType === 1) {
                  type = '折扣券'
                } else if (coupon.couponType === 2) {
                  type = '满减券'
                } else if (coupon.couponType === 3) {
                  type = '礼品券'
                }
                
                const title = coupon.couponName || discount || ''
                let expireDate = '有效期至长期有效'
                if (coupon.validEndTime) {
                  const dateStr = coupon.validEndTime.split('T')[0] || coupon.validEndTime.split(' ')[0]
                  expireDate = '有效期至' + dateStr
                }
                
                return {
                  discount: discount,
                  type: type,
                  title: title,
                  expireDate: expireDate
                }
              })
            }

            const associations = shop.associations || []

            return {
              id: shop.shopId || shop.id,
              name: shop.shopName || shop.name || '',
              distance: distanceText,
              image: image,
              associations: associations,
              coupons: coupons,
              latitude: shop.latitude,
              longitude: shop.longitude
            }
          })

          const currentList = reset ? couponList : this.data.couponList.concat(couponList)
          const hasMore = currentList.length < total && records.length > 0
          
          this.setData({
            couponList: currentList,
            currentPage: currentPage,
            hasMore: hasMore,
            loading: false
          }, () => {
            if (this.data.viewMode === 'map') {
              this.updateMapMarkers()
            }
          })
        } else if (queryType === 2) {
          // 企业/场所类型
          const venueList = records.map(venue => {
            let image = config.defaultAvatar
            if (venue.image || venue.venueImage) {
              const img = venue.image || venue.venueImage
              if (Array.isArray(img) && img.length > 0) {
                image = config.getImageUrl(img[0])
              } else if (typeof img === 'string') {
                image = config.getImageUrl(img)
              }
            }

            let distanceText = '0m'
            if (venue.distance !== undefined && venue.distance !== null) {
              if (venue.distance < 1) {
                distanceText = Math.round(venue.distance * 1000) + 'm'
              } else {
                let kmValue = venue.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(venue.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            return {
              id: venue.venueId || venue.id,
              name: venue.venueName || venue.name || '',
              distance: distanceText,
              image: image,
              associations: venue.associations || [],
              rating: venue.rating || venue.ratingScore || 0,
              latitude: venue.latitude,
              longitude: venue.longitude
            }
          })

          const currentList = reset ? venueList : this.data.venueList.concat(venueList)
          const hasMore = currentList.length < total && records.length > 0
          
          this.setData({
            venueList: currentList,
            currentPage: currentPage,
            hasMore: hasMore,
            loading: false
          }, () => {
            if (this.data.viewMode === 'map') {
              this.updateMapMarkers()
            }
          })
        } else if (queryType === 3) {
          // 校友类型
          const alumniList = records.map(alumni => {
            let avatar = config.defaultAvatar
            if (alumni.avatar) {
              avatar = config.getImageUrl(alumni.avatar)
            }

            let distanceText = '0m'
            if (alumni.distance !== undefined && alumni.distance !== null) {
              if (alumni.distance < 1) {
                distanceText = Math.round(alumni.distance * 1000) + 'm'
              } else {
                let kmValue = alumni.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(alumni.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            return {
              id: alumni.userId || alumni.id,
              name: alumni.name || alumni.realName || '',
              distance: distanceText,
              avatar: avatar,
              association: alumni.association || alumni.associationName || '',
              tag: alumni.tag || '',
              latitude: alumni.latitude,
              longitude: alumni.longitude
            }
          })

          const currentList = reset ? alumniList : this.data.alumniList.concat(alumniList)
          const hasMore = currentList.length < total && records.length > 0
          
          this.setData({
            alumniList: currentList,
            currentPage: currentPage,
            hasMore: hasMore,
            loading: false
          }, () => {
            if (this.data.viewMode === 'map') {
              this.updateMapMarkers()
            }
          })
        }
      } else {
        const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false
        })
      }
    } catch (error) {
      console.error('[Discover] 加载附近数据失败:', error)
      const emptyList = queryType === 1 ? 'couponList' : (queryType === 2 ? 'venueList' : 'alumniList')
      this.setData({
        [emptyList]: reset ? [] : this.data[emptyList],
        loading: false,
        hasMore: false
      })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  getCurrentLocation() {
    return new Promise((resolve, reject) => {
      // 使用微信官方接口获取位置信息
      wx.getLocation({
        type: 'gcj02', // 返回可以用于wx.openLocation的经纬度
        altitude: false, // 传入 true 会返回高度信息，由于获取高度需要较高精度，会减慢接口返回速度
        success: (res) => {
          console.log('[Discover] 获取到当前位置:', res.latitude, res.longitude)
          resolve({
            latitude: res.latitude,
            longitude: res.longitude
          })
        },
        fail: (err) => {
          console.error('[Discover] 获取位置失败:', err)
          // 如果获取位置失败，提示用户并尝试使用默认位置
          if (err.errMsg && err.errMsg.includes('auth deny')) {
            wx.showToast({
              title: '需要位置权限',
              icon: 'none',
              duration: 2000
            })
          }
          // 使用默认位置（无锡，靠近店铺位置）
          resolve({
            latitude: 31.5907370,
            longitude: 120.3597840
          })
        }
      })
    })
  },

  loadMockData() {
    // 模拟加载延迟
    setTimeout(() => {
      // 模拟优惠列表数据
      const mockCouponList = [
        {
          id: 1,
          name: '星巴克咖啡·江南大悦城店',
          distance: 520,
          image: config.defaultAvatar,
          associations: ['江南大学无锡校友会', '南京大学无锡校友会'],
          coupons: [
            {
              discount: '8折',
              type: '优惠券',
              title: '星巴克校友专属优惠',
              expireDate: '有效期至2025-12-31'
            },
            {
              discount: '买一送一',
              type: '优惠券',
              title: '买一送一',
              expireDate: '有效期至2025-12-25'
            },
            {
              discount: '买一送一',
              type: '优惠券',
              title: '买一送一',
              expireDate: '有效期至2025-12-20'
            }
          ]
        },
        {
          id: 2,
          name: '无锡市新区体育馆',
          distance: 520,
          image: config.defaultAvatar,
          associations: ['江南大学无锡校友会', '南京大学无锡校友会'],
          coupons: [
            {
              discount: '8折',
              type: '优惠券',
              title: '星巴克校友专属优惠',
              expireDate: '有效期至2025-12-31'
            },
            {
              discount: '买一送一',
              type: '优惠券',
              title: '买一送一',
              expireDate: '有效期至2025-12-20'
            }
          ]
        }
      ]
      
      // 模拟场所列表数据
      const mockVenueList = [
        {
          id: 1,
          name: '星巴克咖啡·江南大悦城店',
          distance: 520,
          image: config.defaultAvatar,
          associations: ['江南大学无锡校友会'],
          rating: 4.8
        },
        {
          id: 2,
          name: '无锡市新区体育馆',
          distance: 520,
          image: config.defaultAvatar,
          associations: ['江南大学无锡校友会'],
          rating: 4.9
        }
      ]
      
      // 模拟校友列表数据
      const mockAlumniList = [
        {
          id: 1,
          name: '刘汾阳',
          distance: 520,
          association: '江南大学无锡校友会',
          tag: '江南',
          avatar: config.defaultAvatar
        }
      ]
      
      // 模拟活动列表数据
      const mockActivityList = [
        {
          id: 1,
          title: '洛杉矶苏超观影会',
          dateRange: '2025.10.4 - 2026.5.3',
          association: '江南大学无锡校友会',
          participantCount: 24,
          participantAvatars: [
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar,
            config.defaultAvatar
          ],
          location: '北京市朝阳区',
          signedUp: true,
          signedCount: 22
        }
      ]
      
      this.setData({
        couponList: mockCouponList,
        venueList: mockVenueList,
        alumniList: mockAlumniList,
        activityList: mockActivityList,
        loading: false
      })
      this.updateMapMarkers()
    }, 500)
  },

  handleSearchInput(e) {
    this.setData({
      searchValue: e.detail.value,
      searchKeyword: e.detail.value
    })
  },

  handleSearchConfirm() {
    const { searchValue, selectedTab } = this.data
    if (searchValue.trim()) {
      // 使用当前选中的tab进行搜索
      const tabToQueryType = {
        'coupon': 1,  // 商铺
        'venue': 2,   // 企业/场所
        'alumni': 3   // 校友
      }
      const queryType = tabToQueryType[selectedTab]
      if (queryType) {
        // 重置页码并加载搜索结果
        this.setData({
          searchKeyword: searchValue.trim(),
          currentPage: 1,
          hasMore: true
        })
        this.loadNearbyData(queryType, true, searchValue.trim())
      } else {
        // 活动tab跳转到搜索页面
        wx.navigateTo({
          url: `/pages/search/search?keyword=${searchValue}`
        })
      }
    }
  },

  handleTabChange(e) {
    const tabId = e.currentTarget.dataset.id
    this.setData({
      selectedTab: tabId
    })
    // 根据选中的标签加载对应数据
    this.loadDiscoverData()
    // 如果当前是地图模式，更新地图标记
    if (this.data.viewMode === 'map') {
      setTimeout(() => {
        this.updateMapMarkers()
      }, 500)
    }
  },


  handleSortChange(e) {
    const sortId = e.currentTarget.dataset.id
    this.setData({
      sortType: sortId
    })
    // TODO: 根据排序类型重新排序列表
  },

  getLocation() {
    wx.showLoading({ title: '定位中...' })
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        wx.hideLoading()
        const myLocation = {
          latitude: res.latitude,
          longitude: res.longitude
        }
        this.setData({
          mapCenter: myLocation,
          myLocation: myLocation
        })
        this.updateMapMarkers()
        wx.showToast({
          title: '定位成功',
          icon: 'success'
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '定位失败，请重试',
          icon: 'none'
        })
      }
    })
  },

  // 定位到自己位置
  locateToMyPosition() {
    if (!this.data.myLocation) {
      // 如果没有位置信息，先获取位置
      this.getLocation()
      return
    }
    
    // 更新地图中心到自己的位置
    this.setData({
      mapCenter: {
        latitude: this.data.myLocation.latitude,
        longitude: this.data.myLocation.longitude
      },
      mapScale: 15 // 重置缩放级别
    })
    
    wx.showToast({
      title: '已定位到当前位置',
      icon: 'success',
      duration: 1500
    })
  },


  handleFollow(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现关注功能
    wx.showToast({
      title: '关注成功',
      icon: 'success'
    })
  },

  handleSignup(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现报名功能
    wx.showToast({
      title: '报名成功',
      icon: 'success'
    })
  },

  handleLike(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现点赞功能
  },

  handleShare(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现分享功能
  },

  // 切换视图模式
  switchViewMode(e) {
    const { mode } = e.currentTarget.dataset
    this.setData({
      viewMode: mode
    })
    
    if (mode === 'map') {
      // 延迟更新标记，确保数据已加载
      setTimeout(() => {
        this.updateMapMarkers()
      }, 300)
      // 获取当前位置
      this.getLocation()
    }
  },


  // 创建圆形头像（使用 Canvas）
  createRoundAvatar(imageUrl, size = 50) {
    return new Promise((resolve, reject) => {
      const canvasId = 'roundAvatarCanvas'
      const ctx = wx.createCanvasContext(canvasId, this)
      const radius = size / 2
      
      // 先绘制白色圆形背景（作为边框）
      ctx.beginPath()
      ctx.arc(radius, radius, radius, 0, 2 * Math.PI)
      ctx.setFillStyle('#fff')
      ctx.fill()
      
      // 绘制圆形头像
      ctx.save()
      ctx.beginPath()
      ctx.arc(radius, radius, radius - 2, 0, 2 * Math.PI)
      ctx.clip()
      
      // 加载并绘制图片
      wx.getImageInfo({
        src: imageUrl,
        success: (res) => {
          ctx.drawImage(res.path, 0, 0, size, size)
          ctx.restore()
          
          ctx.draw(false, () => {
            // 导出为临时文件
            wx.canvasToTempFilePath({
              canvasId: canvasId,
              width: size,
              height: size,
              destWidth: size,
              destHeight: size,
              success: (result) => {
                resolve(result.tempFilePath)
              },
              fail: (err) => {
                console.warn('[Discover] 创建圆形头像失败，使用原图:', err)
                resolve(imageUrl) // 失败时返回原图
              }
            }, this)
          })
        },
        fail: (err) => {
          console.warn('[Discover] 加载图片失败，使用原图:', err)
          resolve(imageUrl) // 失败时返回原图
        }
      })
    })
  },

  // 更新地图标记
  async updateMapMarkers() {
    const markers = []
    let markerId = 1 // 从1开始，确保id是数字
    
    console.log('[Discover] 更新地图标记，当前标签:', this.data.selectedTab)
    console.log('[Discover] 优惠列表数量:', this.data.couponList.length)
    
    // 先添加自己的位置标记（如果有位置信息）
    if (this.data.myLocation && this.data.myLocation.latitude && this.data.myLocation.longitude) {
      const myLat = Number(this.data.myLocation.latitude)
      const myLng = Number(this.data.myLocation.longitude)
      
      if (!isNaN(myLat) && !isNaN(myLng) && myLat !== 0 && myLng !== 0) {
        markers.push({
          id: 0, // 自己的位置使用固定ID 0
          latitude: myLat,
          longitude: myLng,
          // 不设置 iconPath，使用默认的红色标记点
          width: 10,
          height: 10,
          anchor: { x: 0.5, y: 1 }, // 锚点设置在底部中心
          callout: {
            content: '我的位置',
            color: '#333',
            fontSize: 12,
            borderRadius: 6,
            bgColor: '#fff',
            padding: 6,
            display: 'BYCLICK'
          }
        })
        console.log('[Discover] 添加自己的位置标记:', myLat, myLng)
      }
    }
    
    // 附近优惠标记（只标记带有优惠券的店铺）
    if (this.data.selectedTab === 'coupon' && this.data.couponList.length > 0) {
      for (const item of this.data.couponList) {
        // 只标记有优惠券且有经纬度的店铺
        if (item.latitude && item.longitude && item.coupons && Array.isArray(item.coupons) && item.coupons.length > 0) {
          // 使用店铺头像作为标记图标，如果没有则使用默认图标
          const originalIconPath = item.image || config.defaultAvatar
          
          // 确保经纬度是数字类型
          const latitude = Number(item.latitude)
          const longitude = Number(item.longitude)
          
          // 验证经纬度是否有效
          if (isNaN(latitude) || isNaN(longitude) || latitude === 0 || longitude === 0) {
            console.warn('[Discover] 无效的经纬度:', item.name, latitude, longitude)
            continue
          }
          
          // 创建圆形头像
          let iconPath = originalIconPath
          try {
            iconPath = await this.createRoundAvatar(originalIconPath, 50)
          } catch (error) {
            console.warn('[Discover] 创建圆形头像失败，使用原图:', error)
            iconPath = originalIconPath
          }
          
          markers.push({
            id: markerId++,
            latitude: latitude,
            longitude: longitude,
            iconPath: iconPath,
            width: 50,
            height: 50,
            anchor: { x: 0.5, y: 0.5 },
            callout: {
              content: item.name || '商铺',
              color: '#333',
              fontSize: 14,
              borderRadius: 8,
              bgColor: '#fff',
              padding: 8,
              display: 'BYCLICK'
            }
          })
          
          console.log('[Discover] 添加标记:', item.name, latitude, longitude, iconPath)
        }
      }
    }
    
    // 附近场所标记
    if (this.data.selectedTab === 'venue' && this.data.venueList.length > 0) {
      for (const item of this.data.venueList) {
        if (item.latitude && item.longitude) {
          const originalIconPath = item.image || config.defaultAvatar
          const latitude = Number(item.latitude)
          const longitude = Number(item.longitude)
          
          if (isNaN(latitude) || isNaN(longitude) || latitude === 0 || longitude === 0) {
            continue
          }
          
          // 创建圆形头像
          let iconPath = originalIconPath
          try {
            iconPath = await this.createRoundAvatar(originalIconPath, 50)
          } catch (error) {
            console.warn('[Discover] 创建圆形头像失败，使用原图:', error)
            iconPath = originalIconPath
          }
          
          markers.push({
            id: markerId++,
            latitude: latitude,
            longitude: longitude,
            iconPath: iconPath,
            width: 50,
            height: 50,
            anchor: { x: 0.5, y: 0.5 },
            callout: {
              content: item.name || '场所',
              color: '#333',
              fontSize: 14,
              borderRadius: 8,
              bgColor: '#fff',
              padding: 8,
              display: 'BYCLICK'
            }
          })
        }
      }
    }
    
    // 附近校友标记
    if (this.data.selectedTab === 'alumni' && this.data.alumniList.length > 0) {
      for (const item of this.data.alumniList) {
        if (item.latitude && item.longitude) {
          const originalIconPath = item.avatar || config.defaultAvatar
          const latitude = Number(item.latitude)
          const longitude = Number(item.longitude)
          
          if (isNaN(latitude) || isNaN(longitude) || latitude === 0 || longitude === 0) {
            continue
          }
          
          // 创建圆形头像
          let iconPath = originalIconPath
          try {
            iconPath = await this.createRoundAvatar(originalIconPath, 50)
          } catch (error) {
            console.warn('[Discover] 创建圆形头像失败，使用原图:', error)
            iconPath = originalIconPath
          }
          
          markers.push({
            id: markerId++,
            latitude: latitude,
            longitude: longitude,
            iconPath: iconPath,
            width: 50,
            height: 50,
            anchor: { x: 0.5, y: 0.5 },
            callout: {
              content: item.name || '校友',
              color: '#333',
              fontSize: 14,
              borderRadius: 8,
              bgColor: '#fff',
              padding: 8,
              display: 'BYCLICK'
            }
          })
        }
      }
    }
    
    // 附近活动标记
    if (this.data.selectedTab === 'activity' && this.data.activityList.length > 0) {
      this.data.activityList.forEach((item, index) => {
        if (item.latitude && item.longitude) {
          markers.push({
            id: markerId++,
            latitude: item.latitude,
            longitude: item.longitude,
            width: 30,
            height: 30,
            callout: {
              content: item.title || '活动',
              color: '#333',
              fontSize: 14,
              borderRadius: 8,
              bgColor: '#fff',
              padding: 8,
              display: 'BYCLICK'
            }
          })
        }
      })
    }
    
    console.log('[Discover] 最终标记数量:', markers.length)
    console.log('[Discover] 标记数据:', markers)
    
    this.setData({
      mapMarkers: markers
    }, () => {
      console.log('[Discover] 地图标记更新完成，当前mapMarkers:', this.data.mapMarkers)
    })
  },

  // 地图标记点击
  onMarkerTap(e) {
    const { markerId } = e.detail
    // TODO: 处理标记点击事件
  },

  // 地图点击
  onMapTap() {
    // TODO: 处理地图点击事件
  },

  // 展开更多
  handleExpand(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 处理展开更多事件
    wx.showToast({
      title: '展开更多',
      icon: 'none'
    })
  }

})
