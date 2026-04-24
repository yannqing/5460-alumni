// pages/shop/detail/detail.js
const { nearbyApi, merchantApi, favoriteApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

// 无商户 logo 时与 pages/merchant/list 一致
const DEFAULT_MERCHANT_LOGO = config.defaultAvatar
// 无背景轮播图时与 pages/alumni-association/detail 一致
const DEFAULT_COVER = config.defaultCover

/** 与 merchant/list 的 resolveLogoUrl 一致 */
function resolveLogoUrl(logo) {
  if (!logo) return ''
  const s = String(logo).trim().replace(/[`\s]/g, '')
  return s ? config.getImageUrl(s) : ''
}

/** 解析商户 background_image（JSON 数组或已解析数组） */
function parseBackgroundImageList(backgroundImage) {
  if (!backgroundImage) return []
  if (Array.isArray(backgroundImage)) {
    return backgroundImage.map((x) => String(x).trim()).filter(Boolean)
  }
  if (typeof backgroundImage === 'string') {
    const s = backgroundImage.trim()
    if (!s) return []
    try {
      const parsed = JSON.parse(s)
      return Array.isArray(parsed) ? parsed.map((x) => String(x).trim()).filter(Boolean) : []
    } catch (e) {
      return []
    }
  }
  return []
}

function dedupeImageUrls(urls) {
  const seen = new Set()
  const out = []
  for (const u of urls) {
    if (u && !seen.has(u)) {
      seen.add(u)
      out.push(u)
    }
  }
  return out
}

function normalizeDisplayText(value) {
  if (value == null) return ''
  const text = String(value).trim()
  if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') {
    return ''
  }
  return text
}

/** 从第一家门店取门店图（完整 URL） */
function collectShopGalleryImages(merchantData) {
  const gallery = []
  if (!merchantData.shops || merchantData.shops.length === 0) return gallery
  const firstShop = merchantData.shops[0]
  if (!firstShop.shopImages) return gallery
  if (Array.isArray(firstShop.shopImages) && firstShop.shopImages.length > 0) {
    firstShop.shopImages.forEach((img) => {
      const u = config.getImageUrl(String(img).trim())
      if (u) gallery.push(u)
    })
  } else if (typeof firstShop.shopImages === 'string') {
    const u = config.getImageUrl(firstShop.shopImages.trim())
    if (u) gallery.push(u)
  }
  return gallery
}

Page({
  data: {
    shopId: '',
    shopInfo: null,
    loading: true,
    favoriteLoading: false,
    defaultAvatar: DEFAULT_MERCHANT_LOGO,
    iconLocation: config.getIconUrl('position.png')
  },

  onLoad(options) {
    const { id } = options
    this.setData({ shopId: id })
    this.loadShopDetail()
  },

  async loadShopDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      // 使用原来的商家详情接口获取商家信息，包含门店列表
      const res = await merchantApi.getMerchantInfo(this.data.shopId)
      wx.hideLoading()

      console.log('[ShopDetail] 商家详情响应:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        const merchantData = res.data.data

        const shopGallery = collectShopGalleryImages(merchantData)
        const bgList = parseBackgroundImageList(merchantData.backgroundImage)
        let gallery = dedupeImageUrls(
          bgList.map((p) => config.getImageUrl(String(p).trim())).filter(Boolean)
        )
        if (gallery.length === 0) {
          gallery = dedupeImageUrls([...shopGallery])
        }
        if (gallery.length === 0) {
          gallery = [DEFAULT_COVER]
        }

        // 无 logo 时仅用默认头像，与 merchant/list 一致（不用门店图顶替）
        const avatar = resolveLogoUrl(merchantData.logo) || DEFAULT_MERCHANT_LOGO

        // 处理地址信息 - 从第一个门店获取地址
        let location = ''
        let latitude = null
        let longitude = null
        let phone = ''
        let businessHours = ''
        if (merchantData.shops && merchantData.shops.length > 0) {
          const firstShop = merchantData.shops[0]
          const address = normalizeDisplayText(firstShop.address)
          if (address) {
            location = address
          } else if (firstShop.province || firstShop.city || firstShop.district) {
            location = [firstShop.province, firstShop.city, firstShop.district]
              .map((item) => normalizeDisplayText(item))
              .filter(Boolean)
              .join('')
          }
          latitude = firstShop.latitude
          longitude = firstShop.longitude
          phone = normalizeDisplayText(firstShop.phone) || normalizeDisplayText(merchantData.contactPhone)
          businessHours = firstShop.businessHours || ''
        }

        // 构建商铺信息对象
        const shopInfo = {
          id: this.data.shopId,
          name: merchantData.merchantName || '未知商户',
          avatar: avatar,
          logo: avatar,
          category: merchantData.businessCategory || '其他',
          location: location,
          distance: '0m', // 新接口未提供距离信息
          latitude: latitude,
          longitude: longitude,
          rating: merchantData.ratingScore || 0,
          phone: phone,
          wechat: '', // 新接口未提供微信信息
          businessHours: businessHours,
          description: merchantData.businessScope || '',
          isFavorited: false, // 新接口未提供关注状态
          certifiedAssociation: merchantData.alumniAssociation ? {
            id: merchantData.alumniAssociation.alumniAssociationId,
            name: merchantData.alumniAssociation.associationName
          } : null,
          ownerId: merchantData.merchantId,
          gallery: gallery,
          coupons: [], // 新接口未提供优惠券信息
          recentAlumni: [], // 新接口未提供校友足迹
          dynamics: [], // 新接口未提供店铺动态
          // 新增商家信息
          merchantInfo: {
            contactPhone: merchantData.contactPhone,
            businessScope: merchantData.businessScope,
            businessCategory: merchantData.businessCategory,
            reviewStatus: merchantData.reviewStatus,
            reviewReason: merchantData.reviewReason,
            reviewTime: merchantData.reviewTime,
            memberTier: merchantData.memberTier,
            tierExpireTime: merchantData.tierExpireTime,
            status: merchantData.status,
            shopCount: merchantData.shopCount,
            shops: merchantData.shops || [],
            ratingCount: merchantData.ratingCount,
            isAlumniCertified: merchantData.isAlumniCertified,
            alumniAssociation: merchantData.alumniAssociation
          }
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
      console.error('[ShopDetail] 获取商家详情失败:', error)
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
      wx.navigateTo({
        url: `/pages/coupon/detail/detail?userCouponId=${encodeURIComponent(
          String(id)
        )}`,
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
    if (!id) {
      return
    }
    wx.navigateTo({
      url: `/pages/coupon/detail/detail?userCouponId=${encodeURIComponent(
        String(id)
      )}`,
    })
  },

  // 查看动态详情
  viewDynamicDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {return}
    // 跳转到动态详情页（可以复用活动详情页或创建新的动态详情页）
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  },

  // 收藏/取消收藏（商户维度）
  async toggleFavorite() {
    const { shopInfo, favoriteLoading } = this.data
    if (!shopInfo || favoriteLoading) {
      return
    }

    const app = getApp()
    const wxId =
      app?.globalData?.userData?.wxId ||
      app?.globalData?.userInfo?.wxId ||
      wx.getStorageSync('userId') ||
      ''
    const merchantId = this.data.shopId || shopInfo.id

    if (!wxId || !merchantId) {
      wx.showToast({
        title: '用户或商户信息缺失',
        icon: 'none',
      })
      return
    }

    this.setData({ favoriteLoading: true })
    try {
      const res = await favoriteApi.toggleMerchantFavorite({
        wxId: String(wxId),
        merchantId: String(merchantId),
      })

      if (res?.data?.code === 200 && res?.data?.data) {
        const favorited = !!res.data.data.favorited
        this.setData({
          'shopInfo.isFavorited': favorited,
        })
        wx.showToast({
          title: favorited ? '收藏成功' : '取消收藏成功',
          icon: 'success',
        })
      } else {
        wx.showToast({
          title: res?.data?.msg || '操作失败',
          icon: 'none',
        })
      }
    } catch (err) {
      console.error('[ShopDetail] 切换收藏失败:', err)
      wx.showToast({
        title: '操作失败',
        icon: 'none',
      })
    } finally {
      this.setData({ favoriteLoading: false })
    }
  },

  // 跳转到私信页面
  goToChat() {
    const { shopInfo } = this.data
    if (!shopInfo) {return}

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
    if (!shopInfo) {return}

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
  },

  // 查看门店详情
  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {return}
    wx.navigateTo({
      url: `/pages/shop/shop-detail/shop-detail?id=${id}`
    })
  }
})
