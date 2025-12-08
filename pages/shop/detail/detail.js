// pages/shop/detail/detail.js
const { shopApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

const DEFAULT_AVATAR = config.defaultAvatar

Page({
  data: {
    shopId: '',
    shopInfo: null,
    loading: true,
    defaultAvatar: DEFAULT_AVATAR,
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    iconPhone: config.getIconUrl('电话.png'),
    iconTime: config.getIconUrl('时间.png')
  },

  onLoad(options) {
    const { id } = options
    this.setData({ shopId: id })
    this.loadShopDetail()
  },

  loadShopDetail() {
    // TODO: 对接后端接口
    // shopApi.getShopDetail(this.data.shopId).then(res => {
    //   this.setData({
    //     shopInfo: res.data,
    //     loading: false
    //   })
    // }).catch(err => {
    //   console.error('获取商铺详情失败', err)
    //   this.setData({ loading: false })
    //   wx.showToast({
    //     title: '加载失败',
    //     icon: 'none'
    //   })
    // })

    // 模拟数据
    const mockData = {
      id: this.data.shopId || '1',
      name: '校友咖啡厅',
      avatar: DEFAULT_AVATAR,
      category: '餐饮',
      location: '上海市浦东新区世纪大道123号',
      distance: '800m',
      rating: 4.8,
      phone: '021-12345678',
      wechat: 'alumni_cafe',
      businessHours: '周一至周日 08:00-22:00',
      description: '温馨的校友聚会场所，提供优质咖啡和简餐。店内环境优雅，是校友们交流聚会的好去处。',
      isFavorited: false, // 是否已收藏
      certifiedAssociation: {
        id: 'association-001',
        name: '南京大学上海校友会' // 认证的校友会名称
      },
      gallery: [
        DEFAULT_AVATAR,
        DEFAULT_AVATAR,
        DEFAULT_AVATAR
      ],
      coupons: [
        {
          id: 1,
          type: 'discount',
          discount: '8折',
          title: '校友专属优惠',
          description: '全场商品8折优惠',
          expireDate: '2025-12-31',
          status: 'available' // available, claimed, alumni-only
        },
        {
          id: 2,
          type: 'full-reduction',
          discount: '满100减20',
          title: '满减优惠券',
          description: '满100元立减20元',
          expireDate: '2025-12-25',
          status: 'claimed'
        },
        {
          id: 3,
          type: 'gift',
          discount: '礼品券',
          title: '免费咖啡一杯',
          description: '消费满50元送咖啡一杯',
          expireDate: '2025-12-20',
          status: 'alumni-only'
        }
      ],
      recentAlumni: [
        {
          id: 1,
          name: '李四',
          avatar: DEFAULT_AVATAR,
          school: '南京大学',
          grade: '2015',
          privacy: 'public'
        },
        {
          id: 2,
          name: '王五',
          avatar: DEFAULT_AVATAR,
          school: '天津大学',
          grade: '2010',
          privacy: 'private'
        },
        {
          id: 3,
          name: '赵六',
          avatar: DEFAULT_AVATAR,
          school: '复旦大学',
          grade: '2018',
          privacy: 'public'
        },
        {
          id: 4,
          name: '孙七',
          avatar: DEFAULT_AVATAR,
          school: '上海交通大学',
          grade: '2012',
          privacy: 'private'
        },
        {
          id: 5,
          name: '周八',
          avatar: DEFAULT_AVATAR,
          school: '浙江大学',
          grade: '2016',
          privacy: 'public'
        }
      ],
      dynamics: [
        {
          id: 1,
          type: 'activity',
          title: '周末校友聚会活动',
          description: '本周末举办校友聚会活动，欢迎各位校友参加，现场有精美礼品赠送。',
          image: DEFAULT_AVATAR,
          time: '2天前'
        },
        {
          id: 2,
          type: 'product',
          title: '新品上市：手冲咖啡',
          description: '精选优质咖啡豆，手工现磨，口感醇厚，欢迎品尝。',
          image: DEFAULT_AVATAR,
          time: '5天前'
        },
        {
          id: 3,
          type: 'activity',
          title: '读书分享会',
          description: '每月一次的读书分享会，本期主题：科技与未来。',
          image: DEFAULT_AVATAR,
          time: '1周前'
        }
      ]
    }

    this.setData({
      shopInfo: mockData,
      loading: false
    })
  },

  // 查看校友会详情（认证标签点击）
  viewAssociationDetail(e) {
    console.log('点击认证标签', e)
    const { shopInfo } = this.data
    if (!shopInfo || !shopInfo.certifiedAssociation) {
      wx.showToast({
        title: '暂无校友会信息',
        icon: 'none'
      })
      return
    }

    const { certifiedAssociation } = shopInfo
    if (!certifiedAssociation.id) {
      wx.showToast({
        title: '校友会ID不存在',
        icon: 'none'
      })
      console.error('certifiedAssociation:', certifiedAssociation)
      return
    }

    // 直接跳转到校友会主页
    const url = `/pages/alumni-association/detail/detail?id=${certifiedAssociation.id}`
    console.log('跳转URL:', url)
    wx.navigateTo({
      url: url,
      fail: (err) => {
        console.error('跳转失败:', err)
        wx.showToast({
          title: '跳转失败',
          icon: 'none'
        })
      }
    })
  },

  // 一键导航
  openLocation() {
    const { shopInfo } = this.data
    if (!shopInfo || !shopInfo.location) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none'
      })
      return
    }

    wx.openLocation({
      latitude: shopInfo.latitude || 31.2304, // 默认上海坐标，实际应从后端获取
      longitude: shopInfo.longitude || 121.4737,
      name: shopInfo.name,
      address: shopInfo.location,
      success: () => {
        console.log('打开地图成功')
      },
      fail: () => {
        wx.showToast({
          title: '打开地图失败',
          icon: 'none'
        })
      }
    })
  },

  // 拨打电话
  makeCall(e) {
    const { phone } = e.currentTarget.dataset
    if (phone) {
      wx.makePhoneCall({
        phoneNumber: phone
      })
    }
  },

  // 处理优惠券
  handleCoupon(e) {
    const { id, status } = e.currentTarget.dataset
    
    if (status === 'alumni-only') {
      wx.showModal({
        title: '提示',
        content: '此优惠券仅限认证校友使用，请先完成校友认证。',
        showCancel: true,
        cancelText: '取消',
        confirmText: '去认证',
        success: (res) => {
          if (res.confirm) {
            // 跳转到认证页面
            wx.navigateTo({
              url: '/pages/profile/edit/edit?tab=certification'
            })
          }
        }
      })
      return
    }

    if (status === 'claimed') {
      // 跳转到优惠券详情或使用页面
      wx.navigateTo({
        url: `/pages/coupon/detail/detail?id=${id}`
      })
      return
    }

    // 领取优惠券
    wx.showModal({
      title: '提示',
      content: '确认领取该优惠券吗？',
      success: (res) => {
        if (res.confirm) {
          // TODO: 调用领取接口
          wx.showToast({
            title: '领取成功',
            icon: 'success'
          })
          // 更新状态
          const coupons = this.data.shopInfo.coupons.map(coupon => {
            if (coupon.id === id) {
              return { ...coupon, status: 'claimed' }
            }
            return coupon
          })
          this.setData({
            'shopInfo.coupons': coupons
          })
        }
      }
    })
  },

  // 查看校友详情
  viewAlumniDetail(e) {
    const { id, privacy } = e.currentTarget.dataset
    
    if (privacy === 'private') {
      wx.showToast({
        title: '该校友设置了隐私保护',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/alumni/detail/detail?id=${id}`
    })
  },

  // 查看优惠券详情
  viewCouponDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?id=${id}`
    })
  },

  // 查看动态详情
  viewDynamicDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    // 跳转到动态详情页（可以复用活动详情页或创建新的动态详情页）
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  },

  // 收藏/取消收藏
  toggleFavorite() {
    const { shopInfo } = this.data
    const newFavorited = !shopInfo.isFavorited

    // TODO: 对接后端接口
    // shopApi.followShop(this.data.shopId).then(() => {
    //   this.setData({
    //     'shopInfo.isFavorited': newFavorited
    //   })
    //   wx.showToast({
    //     title: newFavorited ? '收藏成功' : '取消收藏',
    //     icon: 'success'
    //   })
    // }).catch(err => {
    //   console.error('操作失败', err)
    //   wx.showToast({
    //     title: '操作失败',
    //     icon: 'none'
    //   })
    // })

    // 模拟操作
    this.setData({
      'shopInfo.isFavorited': newFavorited
    })
    wx.showToast({
      title: newFavorited ? '收藏成功' : '取消收藏',
      icon: 'success'
    })
  },

  // 联系店铺
  contactShop() {
    const { shopInfo } = this.data
    if (!shopInfo) return

    wx.showActionSheet({
      itemList: shopInfo.wechat ? ['拨打电话', '复制微信号', '复制地址'] : ['拨打电话', '复制地址'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 拨打电话
          if (shopInfo.phone) {
            wx.makePhoneCall({
              phoneNumber: shopInfo.phone
            })
          } else {
            wx.showToast({
              title: '暂无联系电话',
              icon: 'none'
            })
          }
        } else if (res.tapIndex === 1) {
          if (shopInfo.wechat) {
            // 复制微信号
            wx.setClipboardData({
              data: shopInfo.wechat,
              success: () => {
                wx.showToast({
                  title: '微信号已复制',
                  icon: 'success'
                })
              }
            })
          } else {
            // 复制地址
            wx.setClipboardData({
              data: shopInfo.location,
              success: () => {
                wx.showToast({
                  title: '地址已复制',
                  icon: 'success'
                })
              }
            })
          }
        } else if (res.tapIndex === 2) {
          // 复制地址
          wx.setClipboardData({
            data: shopInfo.location,
            success: () => {
              wx.showToast({
                title: '地址已复制',
                icon: 'success'
              })
            }
          })
        }
      }
    })
  }
})
