// pages/discover/discover.js
const config = require('../../utils/config.js')
const { shopApi, nearbyApi, couponApi } = require('../../api/api.js')
const { FollowTargetType, handleListItemFollow } = require('../../utils/followHelper.js')

Page({
  data: {
    searchValue: '',
    selectedTab: 'coupon',
    sortType: 'distance',
    loading: false,
    viewMode: 'list', // list: 列表模式, map: 地图模式
    defaultAvatar: config.defaultAvatar,
    icon5460: config.getIconUrl('5460@3x.png'),
    mapCenter: {
      latitude: 31.2304, // 默认上海坐标
      longitude: 121.4737,
    },
    mapScale: 15,
    mapMarkers: [],
    navTabs: [
      { id: 'coupon', label: '附近优惠', icon: config.getIconUrl('fjyh@3x.png') },
      { id: 'venue', label: '附近场所', icon: config.getIconUrl('fjcs@3x.png') },
      { id: 'activity', label: '附近活动', icon: config.getIconUrl('fjhd@3x.png') },
    ],
    sortOptions: [
      { id: 'distance', label: '距离最近' },
      { id: 'popularity', label: '好评优先' },
      { id: 'discount', label: '优惠力度' },
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
    searchKeyword: '', // 搜索关键词
    imageCache: {}, // 图片URL缓存，避免重复处理
  },

  // 地图 marker 与业务数据的映射（仅内存使用）
  markerDataMap: {},

  onLoad() {
    // 获取当前位置信息
    this.initMyLocation()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 1,
      })
      // 更新未读消息数
      this.getTabBar().updateUnreadCount()
    }
    // 如果当前是地图模式，确保地图定位到用户位置
    if (this.data.viewMode === 'map') {
      this.getLocation()
    }
  },

  // 初始化自己的位置信息
  initMyLocation() {
    const app = getApp()
    const location = app.globalData.location

    if (location && location.latitude && location.longitude) {
      const currentLocation = {
        latitude: location.latitude,
        longitude: location.longitude,
      }
      this.setData({
        myLocation: currentLocation,
        mapCenter: currentLocation,
      })

      // 已有位置信息，直接加载数据
      this.loadDiscoverData()

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
        icon: 'none',
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
      coupon: 1, // 商铺
      venue: 2, // 企业/场所
      alumni: 3, // 校友
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
      coupon: 1, // 商铺
      venue: 2, // 企业/场所
      alumni: 3, // 校友
    }

    const queryType = tabToQueryType[this.data.selectedTab]
    if (queryType) {
      // 如果有搜索关键词，传递关键词；否则传递空字符串
      const keyword = this.data.searchKeyword || ''
      await this.loadNearbyData(queryType, true, keyword)
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
      const location = app.globalData.location || this.data.myLocation

      // 如果全局数据中没有位置信息，显示失败
      if (!location) {
        const emptyList =
          queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
        this.setData({
          [emptyList]: [],
          loading: false,
        })
        wx.showToast({
          title: '获取位置失败，请重试',
          icon: 'none',
          duration: 2000,
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
        pageSize: this.data.pageSize,
      }

      // 如果有搜索关键词，添加到请求参数
      if (keyword && keyword.trim()) {
        requestData.keyword = keyword.trim()
      }

      // 移除调试日志，提升性能
      const res = await nearbyApi.getNearby(requestData)

      // 检查响应是否成功
      if (!res || !res.data) {
        console.error('[Discover] 响应数据格式错误:', res)
        const emptyList =
          queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false,
        })
        wx.showToast({
          title: '请求失败，请重试',
          icon: 'none',
        })
        return
      }

      // 检查业务错误码
      if (res.data.code !== 200) {
        console.error('[Discover] 接口返回错误:', res.data.code, res.data.msg)
        const emptyList =
          queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false,
        })
        wx.showToast({
          title: res.data.msg || '请求失败，请重试',
          icon: 'none',
          duration: 2000,
        })
        return
      }

      if (res.data.data) {
        const data = res.data.data
        const records = data.records || data.items || data.list || []
        const total = data.total || 0

        // 移除调试日志，提升性能

        // 如果没有数据
        if (records.length === 0) {
          const emptyList =
            queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
          this.setData({
            [emptyList]: reset ? [] : this.data[emptyList],
            loading: false,
            hasMore: false,
          })
          return
        }

        // 根据 queryType 处理不同类型的数据
        if (queryType === 1) {
          // 商铺类型
          const couponList = records.map(shop => {
            // 优化图片处理逻辑，使用缓存避免重复处理
            let image = ''
            const shopLogo = (shop.logo && String(shop.logo) !== 'null' && String(shop.logo) !== 'undefined') ? shop.logo : ''

            // 优先使用门店 logo
            if (shopLogo) {
              image = config.getImageUrl(shopLogo)
            } else if (shop.shopImages) {
              // 生成缓存键
              const cacheKey = `shop_${shop.shopId || shop.id}_image`

              // 检查缓存
              if (this.data.imageCache[cacheKey]) {
                image = this.data.imageCache[cacheKey]
              } else {
                let imageUrl = ''
                if (Array.isArray(shop.shopImages) && shop.shopImages.length > 0) {
                  imageUrl = shop.shopImages[0]
                } else if (typeof shop.shopImages === 'string') {
                  // 简化字符串处理，直接使用第一个有效图片
                  const imageStr = shop.shopImages.trim()
                  if (imageStr) {
                    // 快速检查是否为 JSON 数组，避免 try-catch 开销
                    if (imageStr.startsWith('[') && imageStr.endsWith(']')) {
                      // 直接提取第一个图片 URL，不使用 JSON.parse
                      const firstImage = imageStr.slice(1, -1).split(',')[0].trim()
                      if (
                        firstImage &&
                        (firstImage.startsWith('"') || firstImage.startsWith('`'))
                      ) {
                        // 移除引号或反引号
                        const cleanUrl = firstImage.replace(/^["`]|["`]$/g, '')
                        if (cleanUrl) {
                          imageUrl = cleanUrl
                        }
                      } else if (firstImage && firstImage.includes('http')) {
                        imageUrl = firstImage
                      }
                    } else {
                      // 不是数组，直接作为单个图片 URL
                      imageUrl = imageStr
                    }
                  }
                }

                if (imageUrl) {
                  image = config.getImageUrl(imageUrl)
                  // 缓存结果
                  this.data.imageCache[cacheKey] = image
                }
              }
            }

            // // 如果最终没有图片，则设为默认头像
            // if (!image) {
            //   image = config.defaultAvatar
            // }

            // 处理距离
            let distanceText = '0m'
            if (shop.distance !== undefined && shop.distance !== null) {
              if (shop.distance < 1) {
                distanceText = Math.round(shop.distance * 1000) + 'm'
              } else {
                const kmValue = shop.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(shop.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            // 简化优惠券处理逻辑，减少重复计算
            let coupons = []
            if (shop.coupons && Array.isArray(shop.coupons) && shop.coupons.length > 0) {
              coupons = shop.coupons.map(coupon => {
                // 预计算优惠券折扣和类型
                const discountValue = coupon.discountValue || 0
                const couponType = coupon.couponType || 0

                // 简化折扣计算
                let discount = '优惠'
                if (discountValue) {
                  if (couponType === 1) {
                    discount = Math.round(discountValue * 10) + '折'
                  } else if (couponType === 2) {
                    discount = '满' + (coupon.minSpend || 0) + '减' + discountValue
                  } else {
                    discount = '礼品券'
                  }
                }

                // 简化类型判断
                let type = '优惠券'
                if (couponType === 1) {
                  type = '折扣券'
                } else if (couponType === 2) {
                  type = '满减券'
                } else if (couponType === 3) {
                  type = '礼品券'
                }

                // 简化标题和有效期处理
                const title = coupon.couponName || discount || ''
                let expireDate = '有效期至长期有效'
                if (coupon.validEndTime) {
                  // 简化日期处理
                  expireDate =
                    '有效期至' +
                    (coupon.validEndTime.split('T')[0] || coupon.validEndTime.split(' ')[0])
                }

                return {
                  discount: discount,
                  type: type,
                  title: title,
                  expireDate: expireDate,
                  couponId: coupon.couponId,
                  remainQuantity: coupon.remainQuantity || 0,
                  minSpend: coupon.minSpend || 0,
                  isAlumniOnly: coupon.isAlumniOnly || 0,
                }
              })
            }

            const associations = shop.associations || []

            return {
              id: shop.shopId || shop.id,
              name: shop.shopName || shop.name || '',
              distance: distanceText,
              image: image,
              logo: shopLogo, // 保存经过验证的 logo 路径供地图使用
              associations: associations,
              coupons: coupons,
              latitude: shop.latitude,
              longitude: shop.longitude,
              ratingScore: parseFloat(shop.ratingScore) || 0,
              ratingCount: parseInt(shop.ratingCount) || 0,
              viewCount: parseInt(shop.viewCount) || 0,
              clickCount: parseInt(shop.clickCount) || 0,
              couponReceivedCount: parseInt(shop.couponReceivedCount) || 0,
              couponVerifiedCount: parseInt(shop.couponVerifiedCount) || 0,
              isRecommended: parseInt(shop.isRecommended) || 0,
              status: shop.status || 1,
              phone: shop.phone || '',
              businessHours: shop.businessHours || '',
              description: shop.description || '',
            }
          })

          const currentList = reset ? couponList : this.data.couponList.concat(couponList)
          const hasMore = currentList.length < total && records.length > 0

          // 优化 setData 操作，减少不必要的回调
          this.setData({
            couponList: currentList,
            currentPage: currentPage,
            hasMore: hasMore,
            loading: false,
          })

          // 只在地图模式且数据有变化时更新标记
          if (this.data.viewMode === 'map' && records.length > 0) {
            // 延迟更新标记，避免频繁调用
            this.updateMapMarkers()
          }
        } else if (queryType === 2) {
          // 企业/场所类型
          const venueList = records.map(venue => {
            // 处理图片
            let image = ''
            const venueLogo = (venue.logo && String(venue.logo) !== 'null' && String(venue.logo) !== 'undefined') ? venue.logo : ''

            // 优先使用场所 logo
            if (venueLogo) {
              image = config.getImageUrl(venueLogo)
            } else if (venue.images) {
              if (Array.isArray(venue.images) && venue.images.length > 0) {
                image = config.getImageUrl(venue.images[0])
              } else if (typeof venue.images === 'string') {
                // 如果是字符串，可能是逗号分隔的图片列表
                const imageList = venue.images.split(',')
                if (imageList.length > 0 && imageList[0]) {
                  image = config.getImageUrl(imageList[0].trim())
                }
              }
            }

            // 如果最终没有图片，则设为默认头像
            if (!image) {
              image = config.defaultAvatar
            }

            // 处理距离
            let distanceText = '0m'
            if (venue.distance !== undefined && venue.distance !== null) {
              if (venue.distance < 1) {
                distanceText = Math.round(venue.distance * 1000) + 'm'
              } else {
                const kmValue = venue.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(venue.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            // 处理场所类型标签
            let typeLabel = ''
            if (venue.placeType === 1) {
              typeLabel = '商铺'
            } else if (venue.placeType === 2) {
              typeLabel = '企业'
            } else if (venue.placeType === 3) {
              typeLabel = '场馆'
            }

            // 处理地址
            let address = venue.address || ''
            if (!address && (venue.province || venue.city || venue.district)) {
              const addressParts = []
              if (venue.province) {
                addressParts.push(venue.province)
              }
              if (venue.city && venue.city !== venue.province) {
                addressParts.push(venue.city)
              }
              if (venue.district) {
                addressParts.push(venue.district)
              }
              if (venue.address) {
                addressParts.push(venue.address)
              }
              address = addressParts.join('')
            }

            // 处理评分
            let ratingScore = 0
            if (venue.ratingScore !== undefined && venue.ratingScore !== null) {
              ratingScore = parseFloat(venue.ratingScore) || 0
            }

            return {
              id: venue.placeId || venue.venueId || venue.id,
              name: venue.placeName || venue.venueName || venue.name || '未知场所',
              distance: distanceText,
              image: image,
              logo: venueLogo, // 保存经过验证的 logo 路径供地图使用
              typeLabel: typeLabel,
              address: address,
              contactPhone: venue.contactPhone || '',
              contactEmail: venue.contactEmail || '',
              description: venue.description || '',
              ratingScore: ratingScore,
              ratingCount: venue.ratingCount || 0,
              viewCount: venue.viewCount || 0,
              clickCount: venue.clickCount || 0,
              businessHours: venue.businessHours || '',
              latitude: venue.latitude,
              longitude: venue.longitude,
            }
          })

          const currentList = reset ? venueList : this.data.venueList.concat(venueList)
          const hasMore = currentList.length < total && records.length > 0

          this.setData(
            {
              venueList: currentList,
              currentPage: currentPage,
              hasMore: hasMore,
              loading: false,
            },
            () => {
              if (this.data.viewMode === 'map') {
                this.updateMapMarkers()
              }
            }
          )
        } else if (queryType === 3) {
          // 校友类型
          const alumniList = records.map(alumni => {
            // 处理头像：使用 avatarUrl 字段（与后端一致）
            let avatar = config.defaultAvatar
            if (alumni.avatarUrl) {
              avatar = config.getImageUrl(alumni.avatarUrl)
            } else if (alumni.avatar) {
              // 兼容旧字段
              avatar = config.getImageUrl(alumni.avatar)
            }

            // 处理距离
            let distanceText = '0m'
            if (alumni.distance !== undefined && alumni.distance !== null) {
              if (alumni.distance < 1) {
                distanceText = Math.round(alumni.distance * 1000) + 'm'
              } else {
                const kmValue = alumni.distance.toFixed(1)
                if (kmValue.endsWith('.0')) {
                  distanceText = Math.round(alumni.distance) + 'km'
                } else {
                  distanceText = kmValue + 'km'
                }
              }
            }

            // 处理姓名：优先使用 name，如果没有则使用 nickname
            const displayName = alumni.name || alumni.realName || alumni.nickname || ''

            return {
              id: alumni.wxId || alumni.userId || alumni.id,
              name: displayName,
              distance: distanceText,
              avatar: avatar,
              signature: alumni.signature || '', // 个性签名
              association: alumni.association || alumni.associationName || '',
              tag: alumni.tag || '',
              latitude: alumni.latitude,
              longitude: alumni.longitude,
              followStatus: alumni.followStatus || null, // 关注状态
              isFollowed: alumni.isFollowed || false, // 是否已关注
            }
          })

          const currentList = reset ? alumniList : this.data.alumniList.concat(alumniList)
          const hasMore = currentList.length < total && records.length > 0

          this.setData(
            {
              alumniList: currentList,
              currentPage: currentPage,
              hasMore: hasMore,
              loading: false,
            },
            () => {
              if (this.data.viewMode === 'map') {
                this.updateMapMarkers()
              }
            }
          )
        }
      } else {
        const emptyList =
          queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
        this.setData({
          [emptyList]: reset ? [] : this.data[emptyList],
          loading: false,
          hasMore: false,
        })
      }
    } catch (error) {
      console.error('[Discover] 加载附近数据失败:', error)
      const emptyList =
        queryType === 1 ? 'couponList' : queryType === 2 ? 'venueList' : 'alumniList'
      this.setData({
        [emptyList]: reset ? [] : this.data[emptyList],
        loading: false,
        hasMore: false,
      })
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      })
    }
  },

  getCurrentLocation() {
    return new Promise((resolve, reject) => {
      // 暂时注释：等待微信公众平台权限申请通过后恢复
      // wx.getLocation({
      //   type: 'gcj02', // 返回可以用于wx.openLocation的经纬度
      //   altitude: false, // 传入 true 会返回高度信息，由于获取高度需要较高精度，会减慢接口返回速度
      //   success: (res) => {
      //     console.log('[Discover] 获取到当前位置:', res.latitude, res.longitude)
      //     resolve({
      //       latitude: res.latitude,
      //       longitude: res.longitude
      //     })
      //   },
      //   fail: (err) => {
      //     console.error('[Discover] 获取位置失败:', err)
      //     // 如果获取位置失败，提示用户并尝试使用默认位置
      //     if (err.errMsg && err.errMsg.includes('auth deny')) {
      //       wx.showToast({
      //         title: '需要位置权限',
      //         icon: 'none',
      //         duration: 2000
      //       })
      //     }
      //     // 使用默认位置（无锡，靠近店铺位置）
      //     resolve({
      //       latitude: 31.5907370,
      //       longitude: 120.3597840
      //     })
      //   }
      // })
      console.log('[Discover] wx.getLocation 已暂时禁用，使用默认位置')
      // 使用默认位置（无锡，靠近店铺位置）
      resolve({
        latitude: 31.590737,
        longitude: 120.359784,
      })
    })
  },

  loadMockData() {
    // 只保留活动列表的模拟数据（附近优惠、附近场所、附近校友已使用真实数据）
    setTimeout(() => {
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
            config.defaultAvatar,
          ],
          location: '北京市朝阳区',
          signedUp: true,
          signedCount: 22,
        },
      ]

      this.setData({
        couponList: [],
        venueList: [],
        alumniList: [],
        activityList: mockActivityList,
        loading: false,
      })
      this.updateMapMarkers()
    }, 500)
  },

  handleSearchInput(e) {
    this.setData({
      searchValue: e.detail.value,
      searchKeyword: e.detail.value,
    })
  },

  handleSearchConfirm() {
    const { searchValue, selectedTab } = this.data
    // 保存搜索关键词，直接在当前页面加载搜索结果
    this.setData({
      searchKeyword: searchValue.trim(),
    })

    // 使用当前选中的tab进行搜索，直接加载数据
    const tabToQueryType = {
      coupon: 1, // 商铺
      venue: 2, // 企业/场所
      alumni: 3, // 校友
    }
    const queryType = tabToQueryType[selectedTab]

    if (queryType) {
      // 直接在当前页面加载搜索结果，传递 queryType 和 keyword
      this.loadNearbyData(queryType, true, searchValue.trim())
    } else {
      // 活动tab跳转到搜索页面
      if (searchValue.trim()) {
        wx.navigateTo({
          url: `/pages/search/search?keyword=${searchValue}`,
        })
      }
    }
  },

  handleTabChange(e) {
    const tabId = e.currentTarget.dataset.id
    // 切换tab时清除搜索关键词，重新加载数据
    this.setData({
      selectedTab: tabId,
      searchKeyword: '',
      searchValue: '',
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
      sortType: sortId,
    })
    // TODO: 根据排序类型重新排序列表
  },

  getLocation() {
    wx.showLoading({ title: '定位中...' })
    wx.getLocation({
      type: 'gcj02',
      success: res => {
        wx.hideLoading()
        const myLocation = {
          latitude: res.latitude,
          longitude: res.longitude,
        }

        // 同步到全局数据
        const app = getApp()
        app.globalData.location = myLocation

        this.setData({
          mapCenter: myLocation,
          myLocation: myLocation,
        })

        // 定位成功后，重新加载数据（地图标记在数据加载后统一刷新）
        this.loadDiscoverData()

        wx.showToast({
          title: '定位成功',
          icon: 'success',
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.getSetting({
          success: settingRes => {
            const hasLocationAuth = settingRes.authSetting && settingRes.authSetting['scope.userLocation']
            if (hasLocationAuth === false) {
              wx.showModal({
                title: '需要位置权限',
                content: '开启位置权限后，才能按你的位置搜索附近优惠店铺',
                confirmText: '去开启',
                success: modalRes => {
                  if (modalRes.confirm) {
                    wx.openSetting({})
                  }
                },
              })
              return
            }

            wx.showToast({
              title: '定位失败，请重试',
              icon: 'none',
            })
          },
          fail: () => {
            wx.showToast({
              title: '定位失败，请重试',
              icon: 'none',
            })
          },
        })
      },
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
        longitude: this.data.myLocation.longitude,
      },
      mapScale: 15, // 重置缩放级别
    })

    wx.showToast({
      title: '已定位到当前位置',
      icon: 'success',
      duration: 1500,
    })
  },

  // 关注/取消关注校友
  async handleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    await handleListItemFollow(this, 'alumniList', id, followed, FollowTargetType.USER)
  },

  handleSignup(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 实现报名功能
    wx.showToast({
      title: '报名成功',
      icon: 'success',
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
      viewMode: mode,
    })

    if (mode === 'map') {
      // 获取当前位置后重新拉取“附近”数据，标记将在拉取完成后刷新
      this.getLocation()
    }
  },

  // 头像缓存对象，避免重复创建
  avatarCache: {},

  // 创建圆形头像（使用 Canvas）
  createRoundAvatar(imageUrl, size = 50) {
    return new Promise((resolve) => {
      // 与 pages/alumni-association/list 列表默认头一致使用 config.defaultAvatar（本地 /assets/...）
      const fallbackAvatar = config.defaultAvatar || '/assets/avatar/avatar.jpg'
      // 勿对 /assets/ 下包内资源调用 getImageUrl，否则会拼成 baseUrl+/assets/... 导致 404
      const fallbackForRemote =
        !fallbackAvatar.startsWith('/assets/')
          ? config.getImageUrl(fallbackAvatar)
          : ''
      const fallbackImages = [fallbackAvatar, fallbackForRemote, config.defaultAlumniAvatar, '/assets/home/home_page_shop.png']
        .filter(Boolean)
      
      // 处理图片路径，确保本地路径正确
      const normalizePath = (path) => {
        if (!path) return ''
        let p = String(path).trim()
        if (p === 'null' || p === 'undefined') return ''
        return p
      }

      let targetUrl = normalizePath(imageUrl) || normalizePath(fallbackAvatar)

      // 检查缓存
      const cacheKey = `${targetUrl}_${size}`
      if (this.avatarCache[cacheKey]) {
        resolve(this.avatarCache[cacheKey])
        return
      }

      const canvasId = 'roundAvatarCanvas'
      const ctx = wx.createCanvasContext(canvasId, this)
      const radius = size / 2

      // 内部绘制函数（不画白色底圆；有 logo 时仍用圆形容器裁切）
      const drawCanvas = (imagePath) => {
        ctx.clearRect(0, 0, size, size)

        ctx.save()
        ctx.beginPath()
        ctx.arc(radius, radius, radius, 0, 2 * Math.PI)
        ctx.clip()

        if (imagePath) {
          ctx.drawImage(imagePath, 0, 0, size, size)
        }
        ctx.restore()

        // draw 的第二个参数回调在真机上可能过快触发，增加 setTimeout 确保渲染完成
        ctx.draw(false, () => {
          setTimeout(() => {
            wx.canvasToTempFilePath({
              canvasId: canvasId,
              width: size,
              height: size,
              destWidth: size * 2, // 提升清晰度
              destHeight: size * 2,
              success: result => {
                this.avatarCache[cacheKey] = result.tempFilePath
                resolve(result.tempFilePath)
              },
              fail: err => {
                console.warn('[Discover] 导出Canvas失败:', err)
                resolve(imagePath)
              }
            }, this)
          }, 50) // 延迟 50ms 确保 GPU 渲染完成
        })
      }

    // 无论是本地还是远程资源，getImageInfo 都能返回设备最兼容的路径
    const getImage = (src) => {
      return new Promise((resolve) => {
        // 处理协议自适应路径
        let processedSrc = src
        if (typeof src === 'string' && src.startsWith('//')) {
          processedSrc = 'https:' + src
        }

        wx.getImageInfo({
          src: processedSrc,
          success: (res) => resolve(res.path),
          fail: (err) => {
            console.warn(`[Discover] getImageInfo 失败: ${processedSrc}`, err)
            resolve('')
          }
        })
      })
    }

    // 获取图片信息并开始绘制；所有候选图都失败时，直接使用最后一个可用默认图，避免导出白圆。
    const loadImageAndDraw = async (sources, index = 0) => {
      const src = sources[index]
      if (!src) {
        resolve('/assets/home/home_page_shop.png')
        return
      }

      const path = await getImage(src)
      if (!path) {
        loadImageAndDraw(sources, index + 1)
        return
      }

      drawCanvas(path)
    }

      loadImageAndDraw([targetUrl, ...fallbackImages])
    })
  },

  // 更新地图标记
  async updateMapMarkers() {
    const markers = []
    let markerId = 1 // 从1开始，确保id是数字
    this.markerDataMap = {}
    const markerFallbackIcon = '/assets/home/home_page_shop.png'
    const defaultAvatarRoundIcon = '/assets/avatar/avatar_round.png'

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
          // 使用正常的坐标图标尺寸
          width: 20,
          height: 30,
          // 不使用自定义label，使用微信地图默认的标记样式
          callout: {
            content: '我的位置',
            color: '#fff',
            fontSize: 14,
            borderRadius: 8,
            bgColor: '#FF3B30', // 使用醒目的红色背景
            padding: 8,
            display: 'ALWAYS', // 始终显示，更加醒目
            textAlign: 'center',
            borderWidth: 2,
            borderColor: '#fff',
          },
        })
        console.log('[Discover] 添加自己的位置标记:', myLat, myLng)
      }
    }

    const normalizeImagePath = value => {
      const text = value == null ? '' : String(value).trim()
      if (!text || text === 'null' || text === 'undefined') {
        return ''
      }
      return text
    }

    // 通用的标记创建函数
    const createMarker = async (item, listType) => {
      const imagePath = normalizeImagePath(item.image)
      const avatarPath = normalizeImagePath(item.avatar)
      const defaultAvatarPath = normalizeImagePath(config.defaultAvatar)
      let targetIconPath = ''
      
      // 1. 门店原始 Logo
      const normalizedLogo = normalizeImagePath(item.logo)
      
      if (listType === 'coupon') {
        targetIconPath = normalizedLogo ? config.getImageUrl(normalizedLogo) : (config.defaultAvatar || markerFallbackIcon)
      } else if (normalizedLogo) {
        targetIconPath = config.getImageUrl(normalizedLogo)
      } else if (imagePath && imagePath !== defaultAvatarPath) {
        targetIconPath = imagePath
      } else if (avatarPath && avatarPath !== defaultAvatarPath) {
        targetIconPath = avatarPath
      } else {
        // 彻底没有自定义图片时的默认图标
        if (listType === 'coupon' || listType === 'venue') {
          targetIconPath = '/assets/home/home_page_shop.png'
        } else {
          targetIconPath = defaultAvatarPath || markerFallbackIcon
        }
      }

      const latitude = Number(item.latitude)
      const longitude = Number(item.longitude)

      // 验证经纬度是否有效
      if (isNaN(latitude) || isNaN(longitude) || latitude === 0 || longitude === 0) {
        return null
      }

      let iconPath = targetIconPath
      const isCouponDefaultIcon = listType === 'coupon' && !normalizedLogo

      if (isCouponDefaultIcon) {
        iconPath = defaultAvatarRoundIcon
      } else {
        try {
          iconPath = await this.createRoundAvatar(targetIconPath, 50)
        } catch (error) {
          console.warn(`[Discover] 为 ${item.name || '标记'} 创建圆形标记失败:`, error)
          iconPath = targetIconPath
        }
      }

      // 确定标记内容
      let content = '未知'
      if (listType === 'coupon') {
        content = item.name || '商铺'
      } else if (listType === 'venue') {
        content = item.name || '场所'
      } else if (listType === 'alumni') {
        content = item.name || '校友'
      }

      return {
        id: markerId++,
        sourceId: item.id,
        sourceType: listType,
        latitude: latitude,
        longitude: longitude,
        iconPath: iconPath,
        width: 50,
        height: 50,
        anchor: { x: 0.5, y: 0.5 },
        callout: {
          content: content,
          color: '#333',
          fontSize: 14,
          borderRadius: 8,
          bgColor: '#fff',
          padding: 8,
          display: 'BYCLICK',
        },
      }
    }

    // 附近优惠标记（只标记带有优惠券的店铺）
    if (this.data.selectedTab === 'coupon' && this.data.couponList.length > 0) {
      const validShops = this.data.couponList.filter(
        item =>
          item.latitude &&
          item.longitude &&
          item.coupons &&
          Array.isArray(item.coupons) &&
          item.coupons.length > 0
      )

      // 使用 Promise.all 并行处理所有标记，显著提升速度
      const markerPromises = validShops.map(item => createMarker(item, 'coupon'))
      const results = await Promise.all(markerPromises)
      results.forEach(marker => {
        if (marker) {
          markers.push(marker)
          this.markerDataMap[marker.id] = marker
        }
      })
    }

    // 附近场所标记
    if (this.data.selectedTab === 'venue' && this.data.venueList.length > 0) {
      const validVenues = this.data.venueList.filter(item => item.latitude && item.longitude)
      const markerPromises = validVenues.map(item => createMarker(item, 'venue'))
      const results = await Promise.all(markerPromises)
      results.forEach(marker => {
        if (marker) {
          markers.push(marker)
          this.markerDataMap[marker.id] = marker
        }
      })
    }

    // 附近校友标记
    if (this.data.selectedTab === 'alumni' && this.data.alumniList.length > 0) {
      const validAlumni = this.data.alumniList.filter(item => item.latitude && item.longitude)
      const markerPromises = validAlumni.map(item => createMarker(item, 'alumni'))
      const results = await Promise.all(markerPromises)
      results.forEach(marker => {
        if (marker) {
          markers.push(marker)
          this.markerDataMap[marker.id] = marker
        }
      })
    }

    // 附近活动标记（无需头像处理，直接添加）
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
              display: 'BYCLICK',
            },
          })
        }
      })
    }

    console.log('[Discover] 最终标记数量:', markers.length)
    console.log('[Discover] 标记数据:', markers)

    this.setData(
      {
        mapMarkers: markers,
      },
      () => {
        console.log('[Discover] 地图标记更新完成，当前mapMarkers:', this.data.mapMarkers)
      }
    )
  },

  // 地图标记点击
  onMarkerTap(e) {
    const { markerId } = e.detail
    // markerId 为 0 代表“我的位置”
    if (markerId === 0) {
      this.locateToMyPosition()
      return
    }

    const marker = this.markerDataMap[markerId]
    if (!marker) {
      return
    }

    // 附近优惠：点击 marker 直接进入店铺详情
    if (this.data.selectedTab === 'coupon') {
      const matchedShop = this.data.couponList.find(item => String(item.id) === String(marker.sourceId))

      if (matchedShop && matchedShop.id) {
        wx.navigateTo({
          url: `/pages/shop/shop-detail/shop-detail?id=${matchedShop.id}`,
        })
      }
    }
  },

  // 地图点击
  onMapTap() {
    // TODO: 处理地图点击事件
  },

  // 展开更多
  // 点击商铺卡片，跳转到商铺详情页
  handleShopTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      console.error('[Discover] 商铺ID不存在')
      return
    }
    wx.navigateTo({
      url: `/pages/shop/shop-detail/shop-detail?id=${id}`,
    })
  },

  // 跳转到优惠券详情页
  handleCouponTap(e) {
    const { couponId, shopId } = e.currentTarget.dataset
    if (!couponId) return

    wx.navigateTo({
      url: `/pages/coupon/public-detail/detail?id=${couponId}&shopId=${shopId}`,
    })
  },

  // 领取优惠券
  async handleClaimCoupon(e) {
    const { couponId, shopId } = e.currentTarget.dataset

    if (!couponId || !shopId) {
      console.error('[Discover] 优惠券ID或商铺ID不存在')
      return
    }

    wx.showLoading({ title: '领取中...' })

    try {
      // 调用领取优惠券接口
      const res = await couponApi.claimCoupon({
        couponId: String(couponId),
        receiveChannel: 'discover_page',
        receiveSource: 'shop_id_' + String(shopId),
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200 && res.data.data) {
        wx.showToast({
          title: '领取成功',
          icon: 'success',
        })

        // 更新优惠券剩余数量
        const updatedCouponList = [...this.data.couponList]
        for (let i = 0; i < updatedCouponList.length; i++) {
          if (String(updatedCouponList[i].id) === String(shopId) && updatedCouponList[i].coupons) {
            updatedCouponList[i].coupons = updatedCouponList[i].coupons.map(coupon => {
              if (String(coupon.couponId) === String(couponId) && coupon.remainQuantity > 0) {
                return { ...coupon, remainQuantity: coupon.remainQuantity - 1 }
              }
              return coupon
            })
            break
          }
        }

        this.setData({ couponList: updatedCouponList })
      } else {
        const rawMsg = res.data?.msg || '领取失败'
        const displayMsg = rawMsg.includes('校友专享') && rawMsg.includes('商户所属校友会成员可领')
          ? '该优惠为校友专享,商户所属校友会成员可领'
          : rawMsg
        wx.showToast({
          title: displayMsg,
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[Discover] 领取优惠券失败:', error)
      wx.showToast({
        title: '领取失败，请稍后重试',
        icon: 'none',
      })
    }
  },

  handleExpand(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 处理展开更多事件
    wx.showToast({
      title: '展开更多',
      icon: 'none',
    })
  },
})
