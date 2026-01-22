// pages/audit/merchant/coupon/verify/verify.js
const config = require('../../../../../utils/config.js')
const { couponApi } = require('../../../../../api/api.js')

Page({
  data: {
    searchValue: '',
    iconScan: config.getIconUrl('sys.png'),
    couponInfo: null, // 优惠券信息
    loading: false,
    orderAmount: '', // 订单金额
    shopId: null, // 店铺ID
    verificationMethod: 2, // 核销方式：1-扫码 2-输入卡号，默认为输入
    isFromScan: false // 是否来自扫码
  },

  onLoad(options) {
    // 如果URL中有code参数，自动填入并搜索
    if (options.code) {
      const code = decodeURIComponent(options.code)
      this.setData({ 
        searchValue: code,
        verificationMethod: 1, // 来自扫码
        isFromScan: true
      })
      this.searchCoupon(code)
    }
    // 加载商户店铺信息
    this.loadShopInfo()
  },

  // 加载店铺信息
  async loadShopInfo() {
    try {
      const { get } = require('../../../../../utils/request.js')
      // 获取当前用户的商户列表
      const merchantRes = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 1
      })
      
      if (merchantRes.data && merchantRes.data.code === 200) {
        const merchantList = merchantRes.data.data?.records || []
        if (merchantList.length > 0) {
          const merchantId = merchantList[0].merchantId
          // 获取该商户的店铺列表
          const shopRes = await get(`/shop/list/${merchantId}`)
          
          if (shopRes.data && shopRes.data.code === 200) {
            const shopList = shopRes.data.data?.records || shopRes.data.data || []
            if (shopList.length > 0) {
              this.setData({ shopId: shopList[0].shopId })
            }
          }
        }
      }
    } catch (error) {
      console.error('加载店铺信息失败:', error)
      // 如果加载失败，不影响核销功能，shopId可以为空，由后端处理
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
        const result = res.result || ''
        if (!result) {
          wx.showToast({
            title: '扫描失败，请重试',
            icon: 'none'
          })
          return
        }
        // 将扫描结果填入搜索框并搜索
        this.setData({
          searchValue: result,
          verificationMethod: 1, // 扫码方式
          isFromScan: true
        })
        // 搜索优惠券
        this.searchCoupon(result)
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
