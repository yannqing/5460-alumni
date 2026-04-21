// pages/shop/shop-detail/shop-detail.js
const { merchantApi, couponApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

/** ISO 时间展示：去掉日期与时间之间的 T，如 2026-04-13T10:42:22 → 2026-04-13 10:42:22 */
function formatCouponValidTime(value) {
  if (value == null || value === '') {
    return value
  }
  return String(value).replace('T', ' ')
}

function normalizeDisplayText(value) {
  if (value == null) {
    return ''
  }
  const text = String(value).trim()
  if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') {
    return ''
  }
  return text
}

Page({
  data: {
    shopId: '',
    shopInfo: null,
    loading: true,
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    iconPhone: config.getIconUrl('电话.png'),
    iconTime: config.getIconUrl('时间.png'),
    defaultLogo: config.defaultAvatar,
    defaultCover: config.defaultCover
  },

  onLoad(options) {
    const { id } = options
    this.setData({ shopId: id })
    this.loadShopDetail()
  },

  // 跳转到优惠券详情页
  goCouponDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({
      url: `/pages/coupon/public-detail/detail?id=${id}&shopId=${this.data.shopId}`
    })
  },

  async loadShopDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      // 使用店铺详情接口
      const res = await merchantApi.getShopDetail(this.data.shopId)
      wx.hideLoading()

      console.log('[ShopDetail] 门店详情响应:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        const shopData = res.data.data
        shopData.logoUrl = shopData.logo ? config.getImageUrl(shopData.logo) : config.defaultAvatar
        
        // 处理店铺图片
        let gallery = []
        if (shopData.shopImages) {
          let imagesArray = []
          
          // 处理字符串类型的shopImages
          if (typeof shopData.shopImages === 'string') {
            // 去除反引号和空格
            const cleanStr = shopData.shopImages.replace(/[`\s]/g, '')
            
            // 检查是否是JSON数组格式
            if (cleanStr.startsWith('[') && cleanStr.endsWith(']')) {
              try {
                // 尝试解析为JSON数组
                imagesArray = JSON.parse(cleanStr)
              } catch (e) {
                // 解析失败，作为单个URL处理
                imagesArray = [cleanStr]
              }
            } else {
              // 不是数组格式，作为单个URL处理
              imagesArray = [cleanStr]
            }
          } else if (Array.isArray(shopData.shopImages)) {
            // 已经是数组，直接使用
            imagesArray = shopData.shopImages
          }
          
          // 处理图片URL数组
          if (imagesArray.length > 0) {
            gallery = imagesArray.map(img => {
              const cleanUrl = typeof img === 'string' ? img.replace(/[`\s]/g, '') : img
              return config.getImageUrl(cleanUrl)
            })
          }
        }

        // 如果没有图片，则使用默认背景图
        if (gallery.length === 0) {
          gallery = [config.defaultCover]
        }

        // 处理地址信息
        let location = ''
        const address = normalizeDisplayText(shopData.address)
        if (address) {
          location = address
        } else if (shopData.province || shopData.city || shopData.district) {
          location = [shopData.province, shopData.city, shopData.district]
            .map((item) => normalizeDisplayText(item))
            .filter(Boolean)
            .join('')
        }
        shopData.phone = normalizeDisplayText(shopData.phone)

        if (shopData.coupons && Array.isArray(shopData.coupons)) {
          shopData.coupons = shopData.coupons.map(c => ({
            ...c,
            validEndTime: formatCouponValidTime(c.validEndTime),
          }))
        }

        this.setData({
          shopInfo: shopData,
          gallery: gallery,
          location: location,
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
      console.error('[ShopDetail] 获取门店详情失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 一键导航
  openLocation() {
    const { shopInfo, location } = this.data
    if (!shopInfo || !location) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none'
      })
      return
    }

    wx.openLocation({
      latitude: shopInfo.latitude || 31.2304, // 默认上海坐标，实际应从后端获取
      longitude: shopInfo.longitude || 121.4737,
      name: shopInfo.shopName,
      address: location,
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
  makeCall() {
    const { shopInfo } = this.data
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
  },

  // 领取优惠券
  async handleCoupon(e) {
    try {
      const { id } = e.currentTarget.dataset
      
      wx.showLoading({ title: '领取中...' })
      
      // 调用新的领取优惠券接口
      const res = await couponApi.claimCoupon({
        couponId: String(id),
        receiveChannel: 'shop_detail',
        receiveSource: 'shop_id_' + String(this.data.shopId),
      })
      
      wx.hideLoading()
      
      if (res.data && res.data.code === 200 && res.data.data) {
        wx.showToast({
          title: '领取成功',
          icon: 'success'
        })
        
        // 可以在这里更新优惠券状态，例如减少剩余数量
        const updatedShopInfo = { ...this.data.shopInfo }
        if (updatedShopInfo.coupons) {
          updatedShopInfo.coupons = updatedShopInfo.coupons.map(coupon => {
            if (String(coupon.couponId) === String(id) && coupon.remainQuantity > 0) {
              return { ...coupon, remainQuantity: coupon.remainQuantity - 1 }
            }
            return coupon
          })
        }
        this.setData({ shopInfo: updatedShopInfo })
      } else {
        wx.showToast({
          title: res.data?.msg || '领取失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ShopDetail] 领取优惠券失败:', error)
      wx.showToast({
        title: '领取失败，请稍后重试',
        icon: 'none'
      })
    }
  },

  // 活动点击事件
  handleActivityTap(e) {
    try {
      const { id } = e.currentTarget.dataset
      console.log('[ShopDetail] 点击活动:', id)
      
      // 跳转到活动详情页
      wx.navigateTo({
        url: `/pages/activity/detail/detail?id=${id}`
      })
    } catch (error) {
      console.error('[ShopDetail] 处理活动点击失败:', error)
      wx.showToast({
        title: '处理失败',
        icon: 'none'
      })
    }
  }
})
