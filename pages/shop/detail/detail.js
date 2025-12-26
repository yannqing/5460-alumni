// pages/shop/detail/detail.js
const { nearbyApi } = require('../../../api/api.js')
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

  async loadShopDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const res = await nearbyApi.getShopDetail({ shopId: this.data.shopId })
      wx.hideLoading()

      console.log('[ShopDetail] 商铺详情响应:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        const shopData = res.data.data
        
        // 处理图片
        let avatar = config.defaultAvatar
        let gallery = []
        if (shopData.shopImages) {
          if (Array.isArray(shopData.shopImages) && shopData.shopImages.length > 0) {
            avatar = config.getImageUrl(shopData.shopImages[0])
            gallery = shopData.shopImages.map(img => config.getImageUrl(img))
          } else if (typeof shopData.shopImages === 'string') {
            avatar = config.getImageUrl(shopData.shopImages)
            gallery = [avatar]
          }
        }
        if (gallery.length === 0) {
          gallery = [avatar]
        }

        // 处理距离
        let distanceText = '0m'
        if (shopData.distance !== undefined && shopData.distance !== null) {
          if (shopData.distance < 1) {
            distanceText = Math.round(shopData.distance * 1000) + 'm'
          } else {
            let kmValue = shopData.distance.toFixed(1)
            if (kmValue.endsWith('.0')) {
              distanceText = Math.round(shopData.distance) + 'km'
            } else {
              distanceText = kmValue + 'km'
            }
          }
        }

        // 处理优惠券列表
        let coupons = []
        if (shopData.coupons && Array.isArray(shopData.coupons) && shopData.coupons.length > 0) {
          coupons = shopData.coupons.map(coupon => {
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
              id: coupon.couponId || coupon.id,
              discount: discount,
              type: type,
              title: title,
              description: coupon.description || '',
              expireDate: expireDate,
              status: coupon.status || 'available' // available, claimed, alumni-only
            }
          })
        }

        // 处理地址信息
        let location = ''
        if (shopData.address) {
          location = shopData.address
        } else if (shopData.province || shopData.city || shopData.district) {
          location = [shopData.province, shopData.city, shopData.district].filter(Boolean).join('')
        }

        // 构建商铺信息对象
        const shopInfo = {
          id: shopData.shopId || shopData.id || this.data.shopId,
          name: shopData.shopName || shopData.name || '未知商铺',
          avatar: avatar,
          category: shopData.shopType || shopData.category || '其他',
          location: location,
          distance: distanceText,
          latitude: shopData.latitude,
          longitude: shopData.longitude,
          rating: shopData.ratingScore || shopData.rating || 0,
          phone: shopData.phone || '',
          wechat: shopData.wechat || '',
          businessHours: shopData.businessHours || '',
          description: shopData.description || '',
          isFavorited: shopData.isFavorited || false,
          certifiedAssociation: shopData.associations && shopData.associations.length > 0 ? {
            id: shopData.associations[0].id,
            name: shopData.associations[0].name || shopData.associations[0]
          } : null,
          ownerId: shopData.merchantId || shopData.ownerId,
          gallery: gallery,
          coupons: coupons,
          recentAlumni: shopData.recentAlumni || [],
          dynamics: shopData.dynamics || []
        }

        this.setData({
          shopInfo: shopInfo,
          loading: false
        })
      } else {
        console.error('[ShopDetail] 接口返回错误:', res.data?.code, res.data?.msg)
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ShopDetail] 获取商铺详情失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
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

  // 跳转到私信页面
  goToChat() {
    const { shopInfo } = this.data
    if (!shopInfo) return

    // 如果有ownerId，跳转到聊天页
    if (shopInfo.ownerId) {
      const name = encodeURIComponent(shopInfo.name) // 使用店铺名或店主名
      const avatar = encodeURIComponent(shopInfo.logo) // 使用店铺Logo
      
      wx.navigateTo({
        url: `/pages/chat/detail/detail?id=${shopInfo.ownerId}&name=${name}&avatar=${avatar}&type=chat`
      })
    } else {
      // 降级处理：如果没有ownerId，还是使用原来的联系方式弹窗
      this.contactShop()
    }
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
