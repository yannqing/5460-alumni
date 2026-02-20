// pages/audit/merchant/shop-audit/shop-audit.js
const app = getApp()

Page({
  data: {
    loading: false,
    shopLoading: false,
    // 审核列表相关
    approvalList: [],
    total: 0,
    current: 1,
    size: 10,
    hasNext: false,
    hasPrevious: false,
    pages: 0,
    // 筛选条件
    filters: {
      shopName: '',
      reviewStatus: '',
      merchantId: '',
      shopType: ''
    },
    // 商户选择相关
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    // 审核相关
    showAuditModal: false,
    currentAuditShop: null,
    auditStatus: 1,
    auditRemark: '',
    scrollListHeight: 400
  },

  onLoad(options) {
    this.setScrollListHeight()
    this.loadMerchants()
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

  // 加载商户列表
  async loadMerchants() {
    try {
      this.setData({ loading: true })
      const { get } = require('../../../../utils/request.js')
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100 // 加载足够多的商户数据
      })
      
      if (res.data && res.data.code === 200) {
        const merchantList = res.data.data.records || []
        this.setData({
          merchantList: merchantList
        })
        
        // 如果没有选中的商户且列表不为空，自动选择第一个商户
        if (!this.data.selectedMerchantId && merchantList.length > 0) {
          const firstMerchant = merchantList[0]
          // 将商户ID转换为字符串，避免大数字精度问题
          const merchantIdStr = firstMerchant.merchantId + ''
          this.setData({
            selectedMerchantId: merchantIdStr,
            selectedMerchantName: firstMerchant.merchantName,
            'filters.merchantId': merchantIdStr
          })
          // 加载第一个商户的审核列表
          this.loadShopApprovalRecords()
        }
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载店铺审核列表
  async loadShopApprovalRecords() {
    try {
      this.setData({ shopLoading: true })
      const { get } = require('../../../../utils/request.js')
      
      // 构建请求参数
      const params = {
        current: this.data.current,
        pageSize: this.data.size,
        sortField: 'createTime',
        sortOrder: 'descend',
        ...this.data.filters
      }
      
      // 移除空值参数
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key]
        }
      })
      
      const res = await get('/shop/approval/records', params)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data
        this.setData({
          approvalList: data.records || [],
          total: data.total || 0,
          current: data.current || 1,
          size: data.size || 10,
          pages: data.pages || 0,
          hasNext: data.hasNext || false,
          hasPrevious: data.hasPrevious || false
        })
      }
    } catch (error) {
      console.error('加载店铺审核列表失败:', error)
      wx.showToast({
        title: '加载审核列表失败',
        icon: 'none'
      })
    } finally {
      this.setData({ shopLoading: false })
    }
  },

  // 显示审核弹窗
  showAuditModal(e) {
    const shopId = e.currentTarget.dataset.shopId
    // 从审核列表中查找对应的店铺对象
    const shop = this.data.approvalList.find(item => item.shopId === shopId)
    
    if (!shop) {
      wx.showToast({
        title: '未找到店铺数据',
        icon: 'none'
      })
      return
    }
    
    this.setData({
      showAuditModal: true,
      currentAuditShop: shop,
      auditStatus: 1,
      auditRemark: ''
    })
  },

  // 隐藏审核弹窗
  hideAuditModal() {
    this.setData({
      showAuditModal: false,
      currentAuditShop: null,
      auditStatus: 1,
      auditRemark: ''
    })
  },

  // 审核状态选择
  onAuditStatusChange(e) {
    const status = e.currentTarget.dataset.status
    this.setData({
      auditStatus: status
    })
  },

  // 审核备注输入
  onRemarkInput(e) {
    const value = e.detail.value
    this.setData({
      auditRemark: value
    })
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
      'filters.merchantId': merchantId
    })
    
    // 根据选中的商户ID加载对应的审核列表
    this.loadShopApprovalRecords()
  },

  // 提交审核
  async submitAudit() {
    try {
      const { currentAuditShop, auditStatus, auditRemark } = this.data
      
      if (!currentAuditShop) {
        wx.showToast({
          title: '未找到店铺数据',
          icon: 'none'
        })
        return
      }
      
      // 审核失败时必须填写审核原因
      if (auditStatus === 2 && !auditRemark.trim()) {
        wx.showToast({
          title: '审核失败时必须填写原因',
          icon: 'none'
        })
        return
      }
      
      this.setData({ loading: true })
      
      // 调用实际审核接口
      const { post } = require('../../../../utils/request.js')
      const res = await post('/shop/approve', {
        shopId: currentAuditShop.shopId,
        reviewStatus: auditStatus,
        reviewReason: auditRemark
      })
      
      if (res.data && res.data.code === 200) {
        // 审核成功提示
        wx.showToast({
          title: auditStatus === 1 ? '审核通过' : '审核拒绝',
          icon: 'success'
        })
        
        // 关闭弹窗并清空数据
        this.setData({
          showAuditModal: false,
          currentAuditShop: null,
          auditStatus: 1,
          auditRemark: ''
        })
        
        // 重新加载审核列表
        this.loadShopApprovalRecords()
      } else {
        // 审核失败提示
        wx.showToast({
          title: res.data.msg || '审核失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('店铺审核失败:', error)
      wx.showToast({
        title: '审核失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 下一页
  nextPage() {
    if (this.data.hasNext) {
      this.setData({
        current: this.data.current + 1
      }, () => {
        this.loadShopApprovalRecords()
      })
    }
  },

  // 上一页
  previousPage() {
    if (this.data.hasPrevious) {
      this.setData({
        current: this.data.current - 1
      }, () => {
        this.loadShopApprovalRecords()
      })
    }
  },

  // 跳转到指定页码
  goToPage(e) {
    const page = parseInt(e.detail.value)
    if (page > 0 && page <= this.data.pages) {
      this.setData({
        current: page
      }, () => {
        this.loadShopApprovalRecords()
      })
    }
  },
})
