// pages/audit/merchant/shop-audit/shop-audit.js
const app = getApp()
const { userApi } = require('../../../../api/api.js')

/** ISO 时间串中的 T 不展示，改为空格分隔日期与时间 */
function formatReviewTimeDisplay(value) {
  if (value == null || value === '') return value
  return String(value).replace('T', ' ')
}

/**
 * 接口可能返回 number 或 string，统一为数字；无法解析时返回 null
 * @param {*} raw
 * @returns {0|1|2|null}
 */
function normalizeReviewStatus(raw) {
  if (raw === '' || raw === null || raw === undefined) return null
  const n = Number(raw)
  if (Number.isNaN(n)) return null
  if (n === 0 || n === 1 || n === 2) return n
  return null
}

function reviewStatusMeta(raw) {
  const n = normalizeReviewStatus(raw)
  if (n === 0) return { reviewStatus: 0, reviewStatusKind: 'pending', reviewStatusLabel: '待审核' }
  if (n === 1) return { reviewStatus: 1, reviewStatusKind: 'approved', reviewStatusLabel: '已通过' }
  if (n === 2) return { reviewStatus: 2, reviewStatusKind: 'rejected', reviewStatusLabel: '已拒绝' }
  return { reviewStatus: n, reviewStatusKind: 'unknown', reviewStatusLabel: '未知' }
}

Page({
  data: {
    loading: false,
    shopLoading: false,
    hasAlumniAdminPermission: false,
    hasSingleAlumniAssociation: false,
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
      alumniAssociationId: '',
      merchantId: '',
      shopType: ''
    },
    // 校友会选择相关
    alumniAssociationList: [],
    showAlumniAssociationPicker: false,
    selectedAlumniAssociationId: '',
    selectedAlumniAssociationName: '',
    // 拒绝审核弹窗（需填备注）
    showRejectModal: false,
    currentAuditShop: null,
    auditRemark: '',
    scrollListHeight: 400
  },

  onLoad(options) {
    this.loadAlumniAssociationList()
  },

  onReady() {
    this.setScrollListHeight()
  },

  onShow() {
    // 从详情返回或窗口变化后重算，避免列表区域高度与留白不一致
    wx.nextTick(() => this.setScrollListHeight())
  },

  /** 跳转店铺申请详情（/shop/approval/detail） */
  goApprovalDetail(e) {
    const shopId = e.currentTarget.dataset.shopId
    if (!shopId) {
      return
    }
    wx.navigateTo({
      url: `/pages/shop-audit-detail/shop-audit-detail?shop_id=${encodeURIComponent(shopId)}`,
    })
  },

  /**
   * 列表卡片占满「选择商家」以下的剩余高度；scroll-view 高度按 group_7 实际布局测量，
   * 不再使用 50% 屏高 + 55vh 上限，避免列表底部与屏幕底部之间大块空白。
   */
  setScrollListHeight() {
    const applyFallback = () => {
      try {
        const res = wx.getSystemInfoSync()
        const navRpx = 190.22
        const navPx = (res.windowWidth * navRpx) / 750
        const contentH = res.windowHeight - navPx
        const rpx = res.windowWidth / 750
        const topRough =
          (47 + 38 + 40 + 16 + 16 + 80 + 27 + 34 + 38 + 40 + 16 + 12) * rpx
        const scrollH = Math.floor(contentH - topRough)
        this.setData({
          scrollListHeight: scrollH > 200 ? scrollH : Math.min(400, Math.floor(contentH * 0.72))
        })
      } catch (e) {
        this.setData({ scrollListHeight: 400 })
      }
    }

    wx.nextTick(() => {
      const query = wx.createSelectorQuery().in(this)
      query.select('.group_7').boundingClientRect()
      query.select('.list-header').boundingClientRect()
      query.exec((rects) => {
        const g7 = rects && rects[0]
        const lh = rects && rects[1]
        if (!g7 || !lh || g7.height <= 0) {
          applyFallback()
          return
        }
        const sys = wx.getSystemInfoSync()
        const rpx = sys.windowWidth / 750
        const padB = 20 * rpx
        const headerMb = 16 * rpx
        const bottomInner = g7.bottom - padB
        const scrollH = Math.floor(bottomInner - lh.bottom - headerMb)
        if (scrollH >= 120) {
          this.setData({ scrollListHeight: scrollH })
        } else {
          applyFallback()
        }
      })
    })
  },

  // 加载校友会列表
  async loadAlumniAssociationList() {
    try {
      this.setData({ loading: true })
      const res = await userApi.getManagedOrganizations({ type: 0 })
      if (res.data && res.data.code === 200) {
        const organizationList = res.data.data || []
        const alumniAssociationList = organizationList.map((org) => ({
          alumniAssociationId: String(org.id),
          alumniAssociationName: org.name || '校友会'
        }))
        this.setData({
          alumniAssociationList,
          hasAlumniAdminPermission: alumniAssociationList.length > 0
        })

        if (alumniAssociationList.length === 1) {
          const firstAssociation = alumniAssociationList[0]
          this.setData({
            hasSingleAlumniAssociation: true,
            selectedAlumniAssociationId: firstAssociation.alumniAssociationId,
            selectedAlumniAssociationName: firstAssociation.alumniAssociationName,
            'filters.alumniAssociationId': firstAssociation.alumniAssociationId
          })
          this.loadShopApprovalRecords()
          return
        }

        this.setData({
          hasSingleAlumniAssociation: false,
          selectedAlumniAssociationId: '',
          selectedAlumniAssociationName: '',
          'filters.alumniAssociationId': ''
        })
      }
    } catch (error) {
      console.error('加载校友会列表失败:', error)
      this.setData({
        alumniAssociationList: [],
        hasAlumniAdminPermission: false,
        hasSingleAlumniAssociation: false
      })
      wx.showToast({
        title: '加载校友会失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 加载店铺审核列表
  async loadShopApprovalRecords() {
    try {
      if (
        this.data.hasAlumniAdminPermission &&
        !this.data.hasSingleAlumniAssociation &&
        !this.data.selectedAlumniAssociationId
      ) {
        this.setData({
          approvalList: [],
          total: 0,
          current: 1,
          hasNext: false,
          hasPrevious: false,
          pages: 0
        })
        return
      }
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
        const records = (data.records || []).map((row) => {
          const meta = reviewStatusMeta(row.reviewStatus)
          return {
            ...row,
            ...meta,
            reviewTime:
              row.reviewTime != null
                ? formatReviewTimeDisplay(row.reviewTime)
                : row.reviewTime
          }
        })
        this.setData({
          approvalList: records,
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

  /** 从列表解析店铺（兼容 data-shop-id 字符串与数字 id） */
  _findShopFromEvent(e) {
    const raw = e.currentTarget.dataset.shopId
    return this.data.approvalList.find(
      item => item.shopId === raw || String(item.shopId) === String(raw)
    )
  },

  /** 通过：二次确认，无需备注 */
  confirmApprove(e) {
    const shop = this._findShopFromEvent(e)
    if (!shop) {
      wx.showToast({ title: '未找到店铺数据', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认通过',
      content: `确定通过「${shop.shopName || '该店铺'}」的审核吗？`,
      confirmText: '通过',
      confirmColor: '#4caf50',
      success: (res) => {
        if (res.confirm) {
          this.doSubmitAudit(shop, 1, '')
        }
      }
    })
  },

  /** 拒绝：打开填写备注弹窗 */
  openRejectModal(e) {
    const shop = this._findShopFromEvent(e)
    if (!shop) {
      wx.showToast({ title: '未找到店铺数据', icon: 'none' })
      return
    }
    this.setData({
      showRejectModal: true,
      currentAuditShop: shop,
      auditRemark: ''
    })
  },

  hideRejectModal() {
    this.setData({
      showRejectModal: false,
      currentAuditShop: null,
      auditRemark: ''
    })
  },

  // 审核备注输入
  onRemarkInput(e) {
    const value = e.detail.value
    this.setData({
      auditRemark: value
    })
  },

  // 显示校友会选择器
  showAlumniAssociationSelector() {
    if (this.data.hasSingleAlumniAssociation || !this.data.hasAlumniAdminPermission) {
      return
    }
    this.setData({ showAlumniAssociationPicker: true })
  },

  // 取消校友会选择
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 选择校友会
  selectAlumniAssociation(e) {
    const alumniAssociationId = String(e.currentTarget.dataset.alumniAssociationId || '')
    const alumniAssociationName = e.currentTarget.dataset.alumniAssociationName || ''

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false,
      current: 1,
      'filters.alumniAssociationId': alumniAssociationId,
      'filters.merchantId': ''
    })

    this.loadShopApprovalRecords()
  },

  /** 拒绝弹窗内二次确认后再提交（需备注） */
  confirmRejectFromModal() {
    const { currentAuditShop, auditRemark } = this.data
    const reason = (auditRemark || '').trim()
    if (!currentAuditShop) {
      wx.showToast({ title: '未找到店铺数据', icon: 'none' })
      return
    }
    if (!reason) {
      wx.showToast({ title: '请填写拒绝原因', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认拒绝',
      content: '确定拒绝该店铺审核吗？提交后将以填写的原因为准。',
      confirmText: '确定拒绝',
      confirmColor: '#f44336',
      success: (res) => {
        if (res.confirm) {
          this.doSubmitAudit(currentAuditShop, 2, reason)
        }
      }
    })
  },

  /**
   * 调用审核接口 reviewStatus: 1 通过 2 拒绝
   */
  async doSubmitAudit(shop, reviewStatus, reviewReason) {
    if (!shop) {
      wx.showToast({ title: '未找到店铺数据', icon: 'none' })
      return
    }
    try {
      this.setData({ loading: true })
      const { post } = require('../../../../utils/request.js')
      const res = await post('/shop/approve', {
        shopId: shop.shopId,
        reviewStatus,
        reviewReason: reviewReason || ''
      })

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewStatus === 1 ? '审核通过' : '已拒绝审核',
          icon: 'success'
        })
        this.setData({
          showRejectModal: false,
          currentAuditShop: null,
          auditRemark: ''
        })
        this.loadShopApprovalRecords()
      } else {
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
