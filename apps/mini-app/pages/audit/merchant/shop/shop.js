// pages/audit/merchant/shop/shop.js
Page({
  data: {
    loading: false,
    // 商户选择相关
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    showMerchantSelector: false,
    // 店铺列表相关
    shopList: [],
    shopLoading: false,
    scrollListHeight: 400,
  },

  onLoad(options) {
    this.setScrollListHeight()
  },

  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      const scrollH = Math.floor(contentH * 0.5)
      this.setData({ scrollListHeight: scrollH > 200 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  onShow() {
    this.loadMerchants()
  },

  // 加载商户列表
  async loadMerchants() {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../utils/request.js')
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100, // 加载足够多的商户数据
      })

      if (res.data && res.data.code === 200) {
        const records = res.data.data.records || []
        const merchantList = Array.isArray(records) ? records : []
        const showMerchantSelector = merchantList.length > 1
        this.setData({
          merchantList: merchantList,
          showMerchantSelector: showMerchantSelector,
        })

        const { selectedMerchantId } = this.data
        if (!selectedMerchantId && merchantList.length > 0) {
          const firstMerchant = merchantList[0]
          const merchantIdStr = firstMerchant.merchantId + ''
          this.setData({
            selectedMerchantId: merchantIdStr,
            selectedMerchantName: firstMerchant.merchantName,
          })
          this.loadShopList(merchantIdStr)
        } else if (selectedMerchantId) {
          const selectedExist = merchantList.some(
            item => item && String(item.merchantId) === String(selectedMerchantId)
          )
          if (selectedExist) {
            this.loadShopList(selectedMerchantId)
          } else if (merchantList.length > 0) {
            const firstMerchant = merchantList[0]
            const merchantIdStr = firstMerchant.merchantId + ''
            this.setData({
              selectedMerchantId: merchantIdStr,
              selectedMerchantName: firstMerchant.merchantName,
            })
            this.loadShopList(merchantIdStr)
          } else {
            this.setData({
              selectedMerchantId: '',
              selectedMerchantName: '',
              shopList: [],
            })
          }
        } else {
          this.setData({ shopList: [] })
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none',
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载店铺列表
  async loadShopList(merchantId = '') {
    try {
      this.setData({ shopLoading: true })
      const { get, post } = require('../../../../utils/request.js')

      let res
      if (merchantId) {
        res = await get(`/shop/list/${merchantId}`)
      } else {
        res = await post('/shop/page', {
          current: 1,
          pageSize: 10,
        })
      }

      if (res.data && res.data.code === 200) {
        const records = res.data.data?.records || res.data.data || []
        this.setData({
          shopList: records,
        })
      }
    } catch (error) {
      console.error('加载店铺列表失败:', error)
    } finally {
      this.setData({ shopLoading: false })
    }
  },

  // 显示商户选择器
  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  // 取消商户选择
  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  // 选择商户
  selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName

    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false,
    })

    this.loadShopList(merchantId)
  },

  // 跳转到创建店铺页面
  goToCreateShopPage() {
    const { selectedMerchantId, selectedMerchantName } = this.data
    const query = []
    if (selectedMerchantId) {
      query.push(`merchantId=${encodeURIComponent(selectedMerchantId)}`)
    }
    if (selectedMerchantName) {
      query.push(`merchantName=${encodeURIComponent(selectedMerchantName)}`)
    }
    const suffix = query.length > 0 ? `?${query.join('&')}` : ''
    wx.navigateTo({
      url: `/pages/audit/merchant/shop/create/create${suffix}`,
    })
  },

  /** 进入 C 端店铺详情页（与发现页一致：/pages/shop/shop-detail/shop-detail） */
  goToShopDetailPage(e) {
    const shopId = e.currentTarget.dataset.shopId
    if (!shopId) {
      wx.showToast({ title: '缺少店铺信息', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/shop/shop-detail/shop-detail?id=${encodeURIComponent(shopId)}`,
    })
  },

  // 跳转到编辑店铺页面（与原先弹窗相同：详情 GET /merchant/shop/:id，提交 POST /shop/update）
  goToEditShopPage(e) {
    const shopId = e.currentTarget.dataset.shopId
    if (!shopId) {
      wx.showToast({
        title: '缺少店铺信息',
        icon: 'none',
      })
      return
    }
    wx.navigateTo({
      url: `/pages/audit/merchant/shop/edit/edit?shopId=${encodeURIComponent(shopId)}`,
    })
  },

  // 显示删除确认提示
  showDeleteConfirm(e) {
    const shopId = e.currentTarget.dataset.shopId
    wx.showModal({
      title: '删除确认',
      content: '确定要删除该店铺吗？删除后不可恢复',
      success: res => {
        if (res.confirm) {
          this.deleteShop(shopId)
        }
      },
    })
  },

  // 删除店铺
  async deleteShop(shopId) {
    try {
      wx.showLoading({
        title: '删除中...',
        mask: true,
      })

      const { del } = require('../../../../utils/request.js')
      const res = await del(`/shop/delete/${shopId}`)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '删除成功',
          icon: 'success',
        })

        this.loadShopList(this.data.selectedMerchantId)
      } else {
        wx.showToast({
          title: res.data.msg || '删除失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('删除店铺失败:', error)
      wx.showToast({
        title: '删除失败，请重试',
        icon: 'none',
      })
    } finally {
      wx.hideLoading()
    }
  },
})
