const { couponApi, merchantApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    couponId: '',
    shopId: '',
    loading: true,
    couponDetail: null,
    availableShops: [],
    merchantInfo: null,
    defaultLogo: config.defaultAvatar,
    defaultCover: config.defaultCover
  },

  onLoad(options) {
    console.log('[PublicDetail] onLoad options:', options)
    const id = options.id || options.couponId
    const shopId = options.shopId
    
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ 
      couponId: String(id),
      shopId: shopId || ''
    })
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const res = await couponApi.getManagementCouponDetail(this.data.couponId)
      console.log('[PublicDetail] API Response:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        const detailData = res.data.data
        const coupon = detailData.coupon || null
        let shops = detailData.availableShops || []
        
        if (coupon) {
          // 处理优惠券图片
          if (coupon.couponImage) {
            coupon.couponImage = config.getImageUrl(coupon.couponImage)
          } else if (coupon.coupon_image) {
            coupon.couponImage = config.getImageUrl(coupon.coupon_image)
          }

          // 格式化时间
          if (coupon.validStartTime) coupon.validStartTime = coupon.validStartTime.replace('T', ' ')
          if (coupon.validEndTime) coupon.validEndTime = coupon.validEndTime.replace('T', ' ')
          
          // 确保折扣值显示正确
          if (coupon.couponType === 1) {
            const val = parseFloat(coupon.discountValue)
            if (val < 1 && val > 0) {
              coupon.discountDisplay = (val * 10).toFixed(1)
            } else {
              coupon.discountDisplay = val
            }
          } else {
            coupon.discountDisplay = coupon.discountValue
          }

          // 如果是校友专属券，获取商家关联的校友会信息
          if (coupon.isAlumniOnly === 1 && coupon.merchantId) {
            try {
              const merchantRes = await merchantApi.getMerchantInfo(coupon.merchantId)
              if (merchantRes.data && merchantRes.data.code === 200 && merchantRes.data.data) {
                const mInfo = merchantRes.data.data
                // 处理校友会 logo
                if (mInfo.joinedAssociations) {
                  mInfo.joinedAssociations = mInfo.joinedAssociations.map(assoc => ({
                    ...assoc,
                    logoUrl: assoc.logo ? config.getImageUrl(assoc.logo) : config.defaultAvatar
                  }))
                }
                if (mInfo.alumniAssociation) {
                  mInfo.alumniAssociation.logoUrl = mInfo.alumniAssociation.logo ? config.getImageUrl(mInfo.alumniAssociation.logo) : config.defaultAvatar
                }
                this.setData({
                  merchantInfo: mInfo
                })
              }
            } catch (e) {
              console.error('[PublicDetail] 获取商家信息失败:', e)
            }
          }
        }

        // 处理门店 logo
        shops = shops.map(shop => ({
          ...shop,
          logoUrl: shop.logo ? config.getImageUrl(shop.logo) : config.defaultAvatar
        }))

        this.setData({
          couponDetail: coupon,
          availableShops: shops,
          loading: false
        })
      } else {
        throw new Error(res.data?.msg || '接口返回错误')
      }
    } catch (error) {
      console.error('[PublicDetail] loadData Error:', error)
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async handleClaim() {
    if (!this.data.couponDetail) return
    wx.showLoading({ title: '领取中...' })
    try {
      const res = await couponApi.claimCoupon({
        couponId: String(this.data.couponId),
        receiveChannel: 'coupon_detail',
        receiveSource: this.data.shopId ? `shop_id_${this.data.shopId}` : 'public_detail'
      })
      wx.hideLoading()
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '领取成功', icon: 'success' })
        setTimeout(() => wx.navigateTo({ url: '/pages/coupon/list/list' }), 1500)
      } else {
        wx.showToast({ title: res.data?.msg || '领取失败', icon: 'none' })
      }
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '系统繁忙，请重试', icon: 'none' })
    }
  },

  goShopDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id) wx.navigateTo({ url: `/pages/shop/shop-detail/shop-detail?id=${id}` })
  },

  goBack() {
    wx.navigateBack()
  }
})