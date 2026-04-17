// pages/audit/merchant/coupon/verify/verify.js
const config = require('../../../../../utils/config.js')
const { couponApi, shopApi } = require('../../../../../api/api.js')

function extractVerificationCode(raw) {
  if (!raw) {
    return ''
  }

  const value = String(raw).trim()
  if (!value) {
    return ''
  }

  // 已经是纯核销码（数字）时直接返回
  if (/^\d+$/.test(value)) {
    return value
  }

  // 兼容扫码结果为页面路径，如：pages/xx/verify?scene=123456 或 ?code=123456
  const queryIndex = value.indexOf('?')
  if (queryIndex >= 0) {
    const query = value.slice(queryIndex + 1)
    const params = {}
    query.split('&').forEach((pair) => {
      const [k, v = ''] = pair.split('=')
      if (k) {
        params[decodeURIComponent(k)] = decodeURIComponent(v)
      }
    })
    return (params.scene || params.code || '').trim()
  }

  // 兼容完整 URL，例如 https://xxx/verify?scene=123456
  const sceneMatch = value.match(/[?&]scene=([^&]+)/)
  if (sceneMatch && sceneMatch[1]) {
    return decodeURIComponent(sceneMatch[1]).trim()
  }

  const codeMatch = value.match(/[?&]code=([^&]+)/)
  if (codeMatch && codeMatch[1]) {
    return decodeURIComponent(codeMatch[1]).trim()
  }

  return value
}

Page({
  data: {
    searchValue: '',
    iconScan: config.getIconUrl('sys.png'),
    couponInfo: null, // 优惠券信息
    loading: false,
    orderAmount: '', // 订单金额
    shopId: null, // 店铺ID
    shopList: [], // 门店列表
    selectedShop: null, // 选中的门店
    showShopSelector: false, // 是否显示门店选择器
    loadingShops: false, // 加载门店列表状态
    verificationMethod: 2, // 核销方式：1-扫码 2-输入卡号，默认为输入
    isFromScan: false // 是否来自扫码
  },

  onLoad(options) {
    // 兼容后端通过 scene 传参的小程序码，以及 code 直传
    const codeFromOptions = extractVerificationCode(options.scene || options.code)
    if (codeFromOptions) {
      this.setData({ 
        searchValue: codeFromOptions,
        verificationMethod: 1, // 来自扫码
        isFromScan: true
      })
      this.searchCoupon(codeFromOptions)
    }
    
    // 加载门店列表
    this.loadShopList()
  },

  // 加载门店列表
  async loadShopList() {
    try {
      this.setData({ loadingShops: true })
      const res = await shopApi.getAvailableShops()
      
      if (res.data && res.data.code === 200) {
        const shopList = res.data.data || []
        this.setData({
          shopList: shopList,
          loadingShops: false
        })
        
        // 如果有门店列表，默认选择第一个
        if (shopList.length > 0) {
          this.setData({
            selectedShop: shopList[0],
            shopId: shopList[0].shopId
          })
        }
      } else {
        wx.showToast({
          title: res.data?.msg || '加载门店列表失败',
          icon: 'none'
        })
        this.setData({ loadingShops: false })
      }
    } catch (error) {
      console.error('加载门店列表失败:', error)
      wx.showToast({
        title: '加载门店列表失败，请重试',
        icon: 'none'
      })
      this.setData({ loadingShops: false })
    }
  },

  // 显示/隐藏门店选择器
  toggleShopSelector() {
    this.setData({
      showShopSelector: !this.data.showShopSelector
    })
  },

  // 取消门店选择
  cancelShopSelector() {
    this.setData({
      showShopSelector: false
    })
  },

  // 选择门店
  onSelectShop(e) {
    const { shopid } = e.currentTarget.dataset
    if (!shopid) {
      return
    }
    
    const shop = this.data.shopList.find(item => item.shopId === shopid)
    if (shop) {
      this.setData({
        selectedShop: shop,
        shopId: shop.shopId,
        showShopSelector: false
      })
    }
  },


  // 订单金额输入
  onAmountInput(e) {
    this.setData({
      orderAmount: e.detail.value
    })
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({
      searchValue: e.detail.value
    })
  },

  // 搜索确认
  onSearchConfirm(e) {
    const value = e.detail.value || this.data.searchValue
    if (!value.trim()) {
      wx.showToast({
        title: '请输入优惠券码',
        icon: 'none'
      })
      return
    }
    // 输入方式，设置为输入卡号
    this.setData({
      verificationMethod: 2,
      isFromScan: false
    })
    this.searchCoupon(value.trim())
  },

  // 搜索优惠券（可选，如果后端支持查询优惠券信息）
  async searchCoupon(code) {
    if (!code || !code.trim()) {
      return
    }

    // 如果后端有查询接口，可以调用；如果没有，直接显示核销按钮
    // 这里先注释掉查询逻辑，直接显示核销按钮
    this.setData({ 
      loading: false,
      couponInfo: null // 不查询优惠券信息，直接使用核销码
    })
    
    // 如果需要查询优惠券信息，可以取消下面的注释
    /*
    this.setData({ loading: true, couponInfo: null })

    try {
      const res = await couponApi.getCouponByCode(code.trim())
      
      if (res.data && res.data.code === 200 && res.data.data) {
        this.setData({ 
          couponInfo: res.data.data,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '未找到该优惠券',
          icon: 'none'
        })
        this.setData({ loading: false, couponInfo: null })
      }
    } catch (error) {
      console.error('查询优惠券失败:', error)
      // 查询失败不影响核销，直接显示核销按钮
      this.setData({ loading: false, couponInfo: null })
    }
    */
  },

  // 扫一扫
  onScanClick() {
    wx.scanCode({
      success: (res) => {
        // 扫小程序码时，优先使用 path（真实页面参数），result 可能是加密短链
        const source = res.path || res.result || ''
        const verificationCode = extractVerificationCode(source)
        if (!verificationCode) {
          wx.showToast({
            title: '扫描失败，请重试',
            icon: 'none'
          })
          return
        }
        // 将扫描结果填入搜索框并搜索
        this.setData({
          searchValue: verificationCode,
          verificationMethod: 1, // 扫码方式
          isFromScan: true
        })
        // 搜索优惠券
        this.searchCoupon(verificationCode)
      },
      fail: (err) => {
        console.error('扫描失败:', err)
        wx.showToast({
          title: '扫描失败，请重试',
          icon: 'none'
        })
      }
    })
  },

  // 核销优惠券
  async verifyCoupon() {
    const { couponInfo, searchValue, orderAmount, shopId, verificationMethod } = this.data
    const verificationCode = couponInfo?.code || searchValue

    if (!verificationCode || !verificationCode.trim()) {
      wx.showToast({
        title: '请输入优惠券码',
        icon: 'none'
      })
      return
    }

    if (!orderAmount || !orderAmount.trim() || parseFloat(orderAmount) <= 0) {
      wx.showToast({
        title: '请输入订单金额',
        icon: 'none'
      })
      return
    }

    if (!shopId) {
      wx.showToast({
        title: '未找到店铺信息，请先配置店铺',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '确认核销',
      content: `确认核销优惠券码"${verificationCode}"吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '核销中...' })
            
            // 获取设备信息
            const systemInfo = wx.getSystemInfoSync()
            const deviceInfo = `${systemInfo.platform} ${systemInfo.system} ${systemInfo.version}`
            
            const verifyRes = await couponApi.verifyCoupon({
              verificationCode: verificationCode.trim(),
              orderAmount: parseFloat(orderAmount),
              shopId: shopId,
              verificationMethod: verificationMethod, // 1-扫码 2-输入卡号
              deviceInfo: deviceInfo
            })

            wx.hideLoading()

            if (verifyRes.data && verifyRes.data.code === 200) {
              wx.showToast({
                title: '核销成功',
                icon: 'success',
                duration: 2000
              })
              // 清空搜索框和优惠券信息
              setTimeout(() => {
                this.setData({
                  searchValue: '',
                  couponInfo: null,
                  orderAmount: '',
                  verificationMethod: 2,
                  isFromScan: false
                })
              }, 2000)
            } else {
              wx.showToast({
                title: verifyRes.data?.msg || '核销失败',
                icon: 'none'
              })
            }
          } catch (error) {
            wx.hideLoading()
            console.error('核销失败:', error)
            wx.showToast({
              title: error?.response?.data?.msg || '核销失败，请重试',
              icon: 'none'
            })
          }
        }
      }
    })
  }
})
