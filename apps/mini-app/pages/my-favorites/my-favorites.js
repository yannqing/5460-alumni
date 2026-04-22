// pages/my-favorites/my-favorites.js
const config = require('../../utils/config.js')
const { favoriteApi } = require('../../api/api.js')

Page({
  data: {
    favoriteShops: [],
    loading: false,
    loadingMore: false,
    current: 1,
    pageSize: 10,
    hasMore: true,
  },

  onLoad() {
    this.resetAndLoad()
  },

  onShow() {
    // 每次显示页面时刷新列表
    this.resetAndLoad()
  },

  resetAndLoad() {
    this.setData({
      favoriteShops: [],
      current: 1,
      hasMore: true,
    })
    this.loadFavoriteShops(false)
  },

  async loadFavoriteShops(loadMore = false) {
    const { loading, loadingMore, hasMore, current, pageSize, favoriteShops } = this.data
    if (loadMore && (!hasMore || loadingMore || loading)) {
      return
    }
    if (!loadMore && loading) {
      return
    }

    const app = getApp()
    const wxId =
      app?.globalData?.userData?.wxId ||
      app?.globalData?.userInfo?.wxId ||
      wx.getStorageSync('userId') ||
      ''

    if (!wxId) {
      this.setData({ favoriteShops: [] })
      return
    }

    this.setData(loadMore ? { loadingMore: true } : { loading: true })
    try {
      const requestCurrent = loadMore ? current + 1 : 1
      const res = await favoriteApi.getMerchantFavoriteList({
        wxId: String(wxId),
        current: requestCurrent,
        pageSize,
      })
      if (res?.data?.code === 200) {
        const pageData = res.data.data || {}
        const list = Array.isArray(pageData.records) ? pageData.records : []
        const mapped = list.map(item => ({
          id: item.merchantId || '',
          favoriteId: item.favoriteId || '',
          name: item.merchantName || '未命名商户',
          cover: item.logo ? config.getImageUrl(item.logo) : config.defaultAvatar,
          isCertified: Number(item.merchantType) === 1,
          businessCategoryText: `经营类目:${item.businessCategory || '--'}`,
          contactPhoneText: `联系电话:${item.contactPhone || '--'}`,
        }))
        const mergedList = loadMore ? [...favoriteShops, ...mapped] : mapped
        const hasNext =
          typeof pageData.hasNext === 'boolean'
            ? pageData.hasNext
            : (pageData.pages || 0) > (pageData.current || requestCurrent)
        this.setData({
          favoriteShops: mergedList,
          current: requestCurrent,
          hasMore: !!hasNext,
        })
      } else {
        wx.showToast({
          title: res?.data?.msg || '加载失败',
          icon: 'none',
        })
      }
    } catch (err) {
      console.error('[MyFavorites] 加载收藏列表失败:', err)
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      })
    } finally {
      this.setData(loadMore ? { loadingMore: false } : { loading: false })
    }
  },

  onScrollToLower() {
    this.loadFavoriteShops(true)
  },

  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${id}`
    })
  },

  unfavoriteShop(e) {
    const { id } = e.currentTarget.dataset
    const app = getApp()
    const wxId =
      app?.globalData?.userData?.wxId ||
      app?.globalData?.userInfo?.wxId ||
      wx.getStorageSync('userId') ||
      ''

    wx.showModal({
      title: '提示',
      content: '确定取消收藏吗？',
      success: async (res) => {
        if (res.confirm) {
          if (!wxId || !id) {
            wx.showToast({ title: '参数缺失', icon: 'none' })
            return
          }
          try {
            const ret = await favoriteApi.toggleMerchantFavorite({
              wxId: String(wxId),
              merchantId: String(id),
            })
            if (ret?.data?.code === 200) {
              const { favoriteShops } = this.data
              const newList = favoriteShops.filter(shop => String(shop.id) !== String(id))
              this.setData({
                favoriteShops: newList
              })
              wx.showToast({
                title: '已取消收藏',
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: ret?.data?.msg || '操作失败',
                icon: 'none'
              })
            }
          } catch (err) {
            console.error('[MyFavorites] 取消收藏失败:', err)
            wx.showToast({
              title: '操作失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  goToDiscover() {
    wx.switchTab({
      url: '/pages/discover/discover'
    })
  }
})


