// pages/discover/search-list/search-list.js
const config = require('../../../utils/config.js')
const { nearbyApi } = require('../../../api/api.js')

Page({
  data: {
    keyword: '', // 搜索关键词
    queryType: 1, // 查询类型：1-商铺, 2-企业/场所, 3-校友
    queryTypeName: '附近优惠', // 查询类型名称
    loading: false,
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    refreshing: false,
    list: [], // 搜索结果列表
    defaultAvatar: config.defaultAvatar
  },

  onLoad(options) {
    const { keyword, queryType } = options
    const queryTypeNum = parseInt(queryType) || 1
    
    // 查询类型名称映射
    const typeNameMap = {
      1: '附近优惠',
      2: '附近场所',
      3: '附近校友'
    }
    
    this.setData({
      keyword: keyword || '',
      queryType: queryTypeNum,
      queryTypeName: typeNameMap[queryTypeNum] || '附近优惠'
    })
    
    // 设置页面标题
    wx.setNavigationBarTitle({
      title: `搜索${this.data.queryTypeName}`
    })
    
    // 加载搜索结果
    this.loadSearchResults(true)
  },

  // 下拉刷新
  async onPullDownRefresh() {
    this.setData({ refreshing: true })
    try {
      await this.loadSearchResults(true)
    } catch (error) {
      console.error('[SearchList] 刷新失败:', error)
    } finally {
      this.setData({ refreshing: false })
      wx.stopPullDownRefresh()
    }
  },

  // 上拉加载更多
  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) {
      return
    }
    this.loadSearchResults(false)
  },

  // 加载搜索结果
  async loadSearchResults(reset = true) {
    try {
      if (this.data.loading && !reset) {
        return
      }

      // 从全局数据获取位置信息
      const app = getApp()
      const location = app.globalData.location
      
      if (!location) {
        this.setData({
          list: [],
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
        queryType: this.data.queryType,
        latitude: location.latitude,
        longitude: location.longitude,
        radius: 30,
        current: currentPage,
        pageSize: this.data.pageSize,
        keyword: this.data.keyword
      }

      console.log('[SearchList] 请求搜索参数:', requestData)

      this.setData({ loading: true })

      const res = await nearbyApi.getNearby(requestData)
      
      console.log('[SearchList] 搜索响应:', res)
      
      if (!res || !res.data) {
        console.error('[SearchList] 响应数据格式错误:', res)
        this.setData({
          list: reset ? [] : this.data.list,
          loading: false,
          hasMore: false
        })
        wx.showToast({
          title: '请求失败，请重试',
          icon: 'none'
        })
        return
      }
      
      if (res.data.code !== 200) {
        console.error('[SearchList] 接口返回错误:', res.data.code, res.data.msg)
        this.setData({
          list: reset ? [] : this.data.list,
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
        
        console.log('[SearchList] 解析后的数据列表:', records)
        console.log('[SearchList] 数据数量:', records.length)
        console.log('[SearchList] 总数量:', total)
        
        if (records.length === 0) {
          this.setData({
            list: reset ? [] : this.data.list,
            loading: false,
            hasMore: false
          })
          return
        }

        // 根据 queryType 处理不同类型的数据
        let processedList = []
        if (this.data.queryType === 1) {
          // 商铺类型
          processedList = records.map(shop => {
            let image = config.defaultAvatar
            if (shop.shopImages) {
              if (Array.isArray(shop.shopImages) && shop.shopImages.length > 0) {
                image = config.getImageUrl(shop.shopImages[0])
              } else if (typeof shop.shopImages === 'string') {
                image = config.getImageUrl(shop.shopImages)
              }
            }

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

            return {
              id: shop.shopId || shop.id,
              name: shop.shopName || shop.name || '',
              distance: distanceText,
              image: image,
              associations: shop.associations || [],
              coupons: coupons,
              latitude: shop.latitude,
              longitude: shop.longitude
            }
          })
        } else if (this.data.queryType === 2) {
          // 企业/场所类型
          processedList = records.map(venue => {
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
        } else if (this.data.queryType === 3) {
          // 校友类型
          processedList = records.map(alumni => {
            // 处理头像：使用 avatarUrl 字段（与后端一致）
            let avatar = config.defaultAvatar
            if (alumni.avatarUrl) {
              avatar = config.getImageUrl(alumni.avatarUrl)
            }
            // 处理距离
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
              longitude: alumni.longitude
            }
          })
        }

        const currentList = reset ? processedList : this.data.list.concat(processedList)
        const hasMore = currentList.length < total && records.length > 0
        
        this.setData({
          list: currentList,
          currentPage: currentPage,
          hasMore: hasMore,
          loading: false
        })
      } else {
        this.setData({
          list: reset ? [] : this.data.list,
          loading: false,
          hasMore: false
        })
      }
    } catch (error) {
      console.error('[SearchList] 加载搜索结果失败:', error)
      this.setData({
        list: reset ? [] : this.data.list,
        loading: false,
        hasMore: false
      })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 展开更多
  handleExpand(e) {
    const id = e.currentTarget.dataset.id
    // TODO: 处理展开更多事件
  },

  // 点击商铺卡片，跳转到商铺详情页
  handleShopTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      console.error('[SearchList] 商铺ID不存在')
      return
    }
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${id}`
    })
  }
})

