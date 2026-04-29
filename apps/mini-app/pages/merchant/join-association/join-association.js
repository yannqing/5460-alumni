const { merchantApi, associationApi } = require('../../../api/api.js')

Page({
  data: {
    loading: false,
    submitting: false,
    myMerchants: [],
    merchantIndex: -1,
    selectedMerchantId: '',
    applyInfo: {},
    associations: [],
    associationIndex: -1,
    selectedAssociationId: '',
    showMerchantPicker: false,
    showAssociationPicker: false
  },

  onLoad() {
    this.loadMyMerchants()
    this.loadAssociations()
  },

  // 加载我的商户列表
  loadMyMerchants() {
    this.setData({ loading: true })
    merchantApi.getMyMerchants({
      current: 1,
      size: 100
    }).then(res => {
      if (res.data && res.data.code === 200) {
        const records = res.data.data || []
        // 取消强制 reviewStatus === 1 的筛选，直接展示所有商户
        this.setData({
          myMerchants: records
        })
        
        // 如果只有一个商户，自动选中
        if (records.length === 1) {
          this.selectMerchant({ currentTarget: { dataset: { index: 0 } } })
        }
      }
    }).catch(err => {
      console.error('获取商户列表失败:', err)
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  // 加载校友会列表
  loadAssociations() {
    associationApi.getAssociationList({
      current: 1,
      pageSize: 1000
    }).then(res => {
      if (res.data && res.data.code === 200) {
        this.setData({
          associations: res.data.data?.records || []
        })
      }
    }).catch(err => {
      console.error('获取校友会列表失败:', err)
    })
  },

  // 显示/隐藏选择器
  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },
  hideMerchantSelector() {
    this.setData({ showMerchantPicker: false })
  },
  showAssociationSelector() {
    this.setData({ showAssociationPicker: true })
  },
  hideAssociationSelector() {
    this.setData({ showAssociationPicker: false })
  },

  // 商户选择
  selectMerchant(e) {
    const index = e.currentTarget.dataset.index
    const merchant = this.data.myMerchants[index]
    if (!merchant) return
    const selectedMerchantId = merchant.merchantId || merchant.applicationId || ''

    this.setData({
      merchantIndex: index,
      selectedMerchantId,
      associationIndex: -1, // 重置校友会选择
      selectedAssociationId: '',
      showMerchantPicker: false,
      applyInfo: {}
    })

    if (selectedMerchantId) {
      this.loadMerchantDetail(selectedMerchantId)
    }
  },

  // 校友会选择
  selectAssociation(e) {
    const index = e.currentTarget.dataset.index
    const association = this.data.associations[index]
    if (!association) return

    this.setData({
      associationIndex: index,
      selectedAssociationId: association.alumniAssociationId,
      showAssociationPicker: false
    })
  },

  // 加载商户详情 (复用申请详情逻辑)
  async loadMerchantDetail(merchantId) {
    wx.showLoading({ title: '加载商户信息...' })
    try {
      const res = await merchantApi.getMerchantInfo(merchantId)
      if (res.data && res.data.code === 200) {
        this.setData({ applyInfo: res.data.data || {} })
      }
    } catch (err) {
      console.error('获取详情失败:', err)
      wx.showToast({ title: '获取详情失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // 提交申请
  submitApply() {
    const { selectedMerchantId, selectedAssociationId } = this.data
    
    if (!selectedMerchantId) {
      wx.showToast({ title: '请选择商户', icon: 'none' })
      return
    }
    if (!selectedAssociationId) {
      wx.showToast({ title: '请选择目标校友会', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    wx.showLoading({ title: '提交中...' })

    merchantApi.applyJoinAssociation({
      merchantId: selectedMerchantId,
      alumniAssociationId: selectedAssociationId
    }).then(res => {
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '提交成功',
          icon: 'success',
          duration: 2000
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 2000)
      } else {
        wx.showToast({
          title: res.data.msg || '提交失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      console.error('提交失败:', err)
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
    }).finally(() => {
      this.setData({ submitting: false })
      wx.hideLoading()
    })
  }
})
