// pages/audit/merchant/info-maintenance/info-maintenance.js
const { merchantApi } = require('../../../../api/api.js')
const config = require('../../../../utils/config.js')

Page({
  data: {
    loading: false,
    detailLoading: false,
    merchantList: [],
    showMerchantPicker: false,
    selectedMerchantId: '',
    selectedMerchantName: '',
    showMerchantSelector: false,
    currentMerchantDetail: null,
  },

  onShow() {
    this.loadMerchants()
  },

  normalizeMerchantDetail(raw) {
    if (!raw || typeof raw !== 'object') {
      return null
    }
    const d = { ...raw }

    if (d.logo) {
      d.logo = String(d.logo).trim().replace(/[`\s]/g, '')
    }
    d.logoUrl = d.logo ? config.getImageUrl(d.logo) : ''

    if (d.businessLicense) {
      const lic = String(d.businessLicense).trim()
      d.businessLicenseUrl = /^https?:\/\//i.test(lic) ? lic : config.getImageUrl(lic)
    } else {
      d.businessLicenseUrl = ''
    }

    let bgPreview = ''
    if (d.backgroundImage) {
      try {
        const parsed = JSON.parse(d.backgroundImage)
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first =
            typeof parsed[0] === 'string' ? parsed[0] : parsed[0].url || parsed[0].fileUrl || ''
          bgPreview = first ? config.getImageUrl(String(first).trim()) : ''
        }
      } catch (e) {
        bgPreview = config.getImageUrl(String(d.backgroundImage).trim())
      }
    }
    d.backgroundPreviewUrl = bgPreview

    if (d.detailImages) {
      try {
        let parsed = d.detailImages
        if (typeof d.detailImages === 'string') {
          parsed = JSON.parse(d.detailImages)
        }
        d.detailImageUrls = Array.isArray(parsed)
          ? parsed
              .map(img => {
                const s = typeof img === 'string' ? img : img.url || img.fileUrl || ''
                return s ? config.getImageUrl(String(s).trim()) : ''
              })
              .filter(Boolean)
          : []
      } catch (e) {
        d.detailImageUrls = []
      }
    } else {
      d.detailImageUrls = []
    }

    const mt = d.merchantType
    d.merchantTypeText =
      mt === 1 ? '校友商铺' : mt === 2 ? '普通商铺' : mt != null ? String(mt) : '—'

    const rs = d.reviewStatus
    d.reviewStatusText =
      rs === 0
        ? '待审核'
        : rs === 1
          ? '审核通过'
          : rs === 2
            ? '审核未通过'
            : rs != null
              ? String(rs)
              : '—'

    const st = d.status
    d.statusText =
      st === 0 ? '禁用' : st === 1 ? '启用' : st === 2 ? '已注销' : st != null ? String(st) : '—'

    const tier = d.memberTier
    const tierMap = { 1: '基础版', 2: '标准版', 3: '专业版', 4: '旗舰版' }
    d.memberTierText = tierMap[tier] || (tier != null ? String(tier) : '—')

    d.isAlumniCertifiedText =
      d.isAlumniCertified === 1 ? '是' : d.isAlumniCertified === 0 ? '否' : '—'

    if (d.shops && Array.isArray(d.shops)) {
      d.shops = d.shops.map(s => ({
        ...s,
        shopTypeText: s.shopType === 1 ? '总店' : s.shopType === 2 ? '分店' : '',
        addressLine: [s.province, s.city, s.district, s.address].filter(Boolean).join(''),
      }))
    }

    if (d.tierExpireTime) {
      d.tierExpireTimeText = String(d.tierExpireTime).replace('T', ' ').slice(0, 19)
    } else {
      d.tierExpireTimeText = ''
    }

    return d
  },

  async loadMerchantDetail(merchantId) {
    if (!merchantId) {
      this.setData({ currentMerchantDetail: null, detailLoading: false })
      return
    }
    this.setData({ detailLoading: true })
    try {
      const res = await merchantApi.getMerchantDetailById(merchantId)
      if (res.data && res.data.code === 200 && res.data.data) {
        const processed = this.normalizeMerchantDetail(res.data.data)
        this.setData({ currentMerchantDetail: processed })
      } else {
        this.setData({ currentMerchantDetail: null })
        wx.showToast({
          title: (res.data && res.data.message) || '加载商家详情失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('加载商家详情失败:', error)
      this.setData({ currentMerchantDetail: null })
      wx.showToast({ title: '加载商家详情失败', icon: 'none' })
    } finally {
      this.setData({ detailLoading: false })
    }
  },

  async loadMerchants() {
    const { get } = require('../../../../utils/request.js')
    let nextId = ''
    let nextName = ''
    let list = []
    let showSelector = false

    try {
      this.setData({ loading: true })
      const res = await get('/merchant-management/my-merchants', {
        current: 1,
        size: 100,
      })

      if (res.data && res.data.code === 200) {
        const records = (res.data.data && res.data.data.records) || []
        list = Array.isArray(records) ? records : []
        showSelector = list.length > 1

        const prevId = this.data.selectedMerchantId
        if (!prevId && list.length > 0) {
          const first = list[0]
          nextId = first.merchantId != null ? String(first.merchantId) : ''
          nextName = first.merchantName || ''
        } else if (prevId) {
          const exists = list.some(item => item && String(item.merchantId) === String(prevId))
          if (exists) {
            const m = list.find(item => String(item.merchantId) === String(prevId))
            nextId = String(prevId)
            nextName = (m && m.merchantName) || ''
          } else if (list.length > 0) {
            const first = list[0]
            nextId = first.merchantId != null ? String(first.merchantId) : ''
            nextName = first.merchantName || ''
          } else {
            nextId = ''
            nextName = ''
          }
        }
      } else {
        list = []
        showSelector = false
        nextId = ''
        nextName = ''
      }
    } catch (error) {
      console.error('加载商户列表失败:', error)
      wx.showToast({
        title: '加载商户列表失败',
        icon: 'none',
      })
    } finally {
      this.setData({
        loading: false,
        merchantList: list,
        showMerchantSelector: showSelector,
        selectedMerchantId: nextId,
        selectedMerchantName: nextName,
      })
      if (nextId) {
        await this.loadMerchantDetail(nextId)
      } else {
        this.setData({ currentMerchantDetail: null, detailLoading: false })
      }
    }
  },

  showMerchantSelector() {
    this.setData({ showMerchantPicker: true })
  },

  cancelMerchantSelect() {
    this.setData({ showMerchantPicker: false })
  },

  async selectMerchant(e) {
    const merchantId = e.currentTarget.dataset.merchantId
    const merchantName = e.currentTarget.dataset.merchantName

    this.setData({
      selectedMerchantId: merchantId,
      selectedMerchantName: merchantName,
      showMerchantPicker: false,
    })
    await this.loadMerchantDetail(merchantId)
  },

  goToEditPage() {
    const merchantId = this.data.selectedMerchantId
    if (!merchantId) {
      wx.showToast({
        title: '请先选择商家',
        icon: 'none',
      })
      return
    }
    wx.navigateTo({
      url: `/pages/audit/merchant/info-maintenance/edit/edit?merchantId=${merchantId}`,
    })
  },

  goToShopDetail() {
    const merchantId = this.data.selectedMerchantId
    if (!merchantId) {
      wx.showToast({
        title: '请先选择商家',
        icon: 'none',
      })
      return
    }
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${merchantId}`,
    })
  },

  previewDetailImage(e) {
    const { index } = e.currentTarget.dataset
    const urls = this.data.currentMerchantDetail?.detailImageUrls || []
    if (urls.length > 0) {
      wx.previewImage({
        current: urls[index],
        urls: urls,
      })
    }
  },
})
