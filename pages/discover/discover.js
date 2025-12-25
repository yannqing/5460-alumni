// pages/discover/discover.js
const config = require('../../utils/config.js')
const { shopApi } = require('../../api/api.js')

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
    hasMore: true
  },

  onLoad() {
    this.loadDiscoverData()
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
    // 如果是附近优惠tab，加载更多数据
    if (this.data.selectedTab === 'coupon') {
      this.loadNearbyShops(false)
    }
  },

  async loadDiscoverData() {
    this.setData({ loading: true, currentPage: 1, hasMore: true })
    
    // 如果是附近优惠tab，调用后端接口
    if (this.data.selectedTab === 'coupon') {
      await this.loadNearbyShops(true)
    } else {
      // 其他tab使用模拟数据
      this.loadMockData()
    }
  },

  async loadNearbyShops(reset = true) {
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
        this.setData({
          couponList: [],
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
        latitude: location.latitude,
        longitude: location.longitude,
        radius: 30, // 默认30公里
        current: currentPage,
        pageSize: this.data.pageSize
      }

      // 调试日志：输出请求参数
      console.log('[Discover] 请求附近商铺参数:', requestData)

      const res = await shopApi.getNearbyShops(requestData)
      
      // 调试日志：输出响应数据
      console.log('[Discover] 附近商铺响应:', res)
      console.log('[Discover] 响应code:', res.code)
      console.log('[Discover] 响应data:', res.data)
      
      if (res && res.data.code === 200 && res.data.data) {
        const data = res.data.data
        const shops = data.records || data.items || data.list || []
        const total = data.total || 0
        
        // 调试日志：输出解析后的店铺列表
        console.log('[Discover] 解析后的店铺列表:', shops)
        console.log('[Discover] 店铺数量:', shops.length)
        console.log('[Discover] 总数量:', total)
        
        // 如果没有数据
        if (shops.length === 0) {
          this.setData({
            couponList: reset ? [] : this.data.couponList,
            loading: false,
            hasMore: false
          })
          return
        }
        const couponList = shops.map(shop => {
          // 处理图片：shopImages可能是字符串数组或单个字符串
          let image = config.defaultAvatar
          if (shop.shopImages) {
            if (Array.isArray(shop.shopImages) && shop.shopImages.length > 0) {
              image = config.getImageUrl(shop.shopImages[0])
            } else if (typeof shop.shopImages === 'string') {
              image = config.getImageUrl(shop.shopImages)
            }
          }

          // 处理距离：小于1km用m显示，大于等于1km用km显示，保留一位小数
          let distanceText = '0m'
          if (shop.distance !== undefined && shop.distance !== null) {
            if (shop.distance < 1) {
              // 小于1km，显示为米
              distanceText = Math.round(shop.distance * 1000) + 'm'
            } else {
              // 大于等于1km，显示为公里，保留一位小数
              let kmValue = shop.distance.toFixed(1)
              // 如果小数部分是0，则只显示整数
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
              // 处理优惠券字段映射
              // 折扣信息：根据 discountValue 和 couponType 计算
              let discount = '优惠'
              if (coupon.discountValue !== undefined && coupon.discountValue !== null) {
                if (coupon.couponType === 1) {
                  // 折扣券：discountValue 如 0.8 表示 8折
                  discount = Math.round(coupon.discountValue * 10) + '折'
                } else if (coupon.couponType === 2) {
                  // 满减券：discountValue 表示减免金额
                  discount = '满' + (coupon.minSpend || 0) + '减' + coupon.discountValue
                } else if (coupon.couponType === 3) {
                  // 礼品券
                  discount = '礼品券'
                }
              }
              
              // 优惠券类型
              let type = '优惠券'
              if (coupon.couponType === 1) {
                type = '折扣券'
              } else if (coupon.couponType === 2) {
                type = '满减券'
              } else if (coupon.couponType === 3) {
                type = '礼品券'
              }
              
              // 标题：使用 couponName（后端字段名）
              const title = coupon.couponName || discount || ''
              
              // 有效期：使用 validEndTime 格式化（后端字段名）
              let expireDate = '有效期至长期有效'
              if (coupon.validEndTime) {
                // 格式化日期：从 "2025-12-31T23:59:59" 格式转换为 "有效期至2025-12-31"
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

          // 处理校友会标签（如果后端没有返回，暂时为空数组）
          const associations = shop.associations || []

          return {
            id: shop.shopId || shop.id,
            name: shop.shopName || shop.name || '',
            distance: distanceText,
            image: image,
            associations: associations,
            coupons: coupons
          }
        })

        // 调试日志：输出最终处理后的列表
        console.log('[Discover] 最终处理后的优惠列表:', couponList)
        console.log('[Discover] 最终列表数量:', couponList.length)
        
        // 计算当前总数据量和是否还有更多
        const currentList = reset ? couponList : this.data.couponList.concat(couponList)
        const hasMore = currentList.length < total && shops.length > 0
        
        this.setData({
          couponList: currentList,
          currentPage: currentPage,
          hasMore: hasMore,
          loading: false
        }, () => {
          console.log('[Discover] setData完成，当前couponList:', this.data.couponList)
          console.log('[Discover] 当前页码:', currentPage, '是否还有更多:', hasMore)
        })
      } else {
        this.setData({
          couponList: reset ? [] : this.data.couponList,
          loading: false,
          hasMore: false
        })
      }
    } catch (error) {
      console.error('[Discover] 加载附近商铺失败:', error)
      this.setData({
        couponList: reset ? [] : this.data.couponList,
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
      searchValue: e.detail.value
    })
  },

  handleSearchConfirm() {
    const { searchValue } = this.data
    if (searchValue.trim()) {
      wx.navigateTo({
        url: `/pages/search/search?keyword=${searchValue}`
      })
    }
  },

  handleTabChange(e) {
    const tabId = e.currentTarget.dataset.id
    this.setData({
      selectedTab: tabId
    })
    // 根据选中的标签加载对应数据
    this.loadDiscoverData()
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
        this.setData({
          mapCenter: {
            latitude: res.latitude,
            longitude: res.longitude
          }
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
      this.updateMapMarkers()
      // 获取当前位置
      this.getLocation()
    }
  },


  // 更新地图标记
  updateMapMarkers() {
    // 活动标记
    const activityMarkers = this.data.activityList.map((item, index) => ({
      id: `activity_${item.id}`,
      latitude: item.latitude || 31.2304,
      longitude: item.longitude || 121.4737,
      iconPath: '/assets/images/activity-marker.png', // 活动图标
      width: 50,
      height: 50,
      callout: {
        content: item.title,
        color: '#333',
        fontSize: 14,
        borderRadius: 8,
        bgColor: '#fff',
        padding: 12,
        display: 'BYCLICK'
      }
    }))
    
    // 场地标记
    const venueMarkers = [
      {
        id: 'venue_1',
        latitude: 31.2314,
        longitude: 121.4747,
        iconPath: '/assets/images/venue-marker.png', // 场地图标
        width: 50,
        height: 50,
        callout: {
          content: '场地',
          color: '#333',
          fontSize: 14,
          borderRadius: 8,
          bgColor: '#fff',
          padding: 12,
          display: 'BYCLICK'
        }
      }
    ]
    
    this.setData({
      mapMarkers: [...activityMarkers, ...venueMarkers]
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
