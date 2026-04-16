// pages/shop-audit-detail/shop-audit-detail.js
const { get } = require('../../utils/request.js')
const config = require('../../utils/config.js')

/** 解析 shopImages：支持已解析数组、JSON 字符串（含换行）、单 URL */
function normalizeShopImages(si) {
  if (si == null || si === '') {
    return []
  }
  if (Array.isArray(si)) {
    return si
      .map(item => (typeof item === 'string' ? item.trim() : item))
      .filter(item => item != null && item !== '')
  }
  if (typeof si === 'string') {
    const t = si.trim().replace(/`/g, '').replace(/\u00a0/g, ' ')
    if (t.startsWith('[')) {
      try {
        const parsed = JSON.parse(t)
        return Array.isArray(parsed) ? parsed : [String(parsed)]
      } catch (e1) {
        const compact = t.replace(/\s+/g, ' ')
        try {
          const parsed = JSON.parse(compact)
          return Array.isArray(parsed) ? parsed : []
        } catch (e2) {
          return [t]
        }
      }
    }
    return [t]
  }
  return []
}

/** 解析店铺图：相对路径用 getImageUrl；已是 http(s) 的完整地址原样保留 */
function buildGalleryAndLogo(raw) {
  let gallery = []
  if (raw && raw.shopImages) {
    const imagesArray = normalizeShopImages(raw.shopImages)
    if (imagesArray.length > 0) {
      gallery = imagesArray.map(img => {
        const cleanUrl = typeof img === 'string' ? img.trim() : String(img)
        if (!cleanUrl) {
          return ''
        }
        if (cleanUrl.startsWith('http://') || cleanUrl.startsWith('https://') || cleanUrl.startsWith('data:')) {
          return cleanUrl
        }
        return config.getImageUrl(cleanUrl.replace(/[`\s]/g, ''))
      }).filter(Boolean)
    }
  }
  const logoRaw = raw && raw.logo ? String(raw.logo).trim() : ''
  let logoUrl = ''
  if (logoRaw) {
    if (logoRaw.startsWith('http://') || logoRaw.startsWith('https://') || logoRaw.startsWith('data:')) {
      logoUrl = logoRaw
    } else {
      logoUrl = config.getImageUrl(logoRaw)
    }
  }
  return { gallery, logoUrl }
}

function formatTime(t) {
  if (t == null || t === '') {
    return ''
  }
  const s = typeof t === 'string' ? t.replace('T', ' ') : String(t)
  return s.length > 19 ? s.slice(0, 19) : s
}

function reviewStatusText(rs) {
  const n = Number(rs)
  if (n === 0) {
    return '待审核'
  }
  if (n === 1) {
    return '已通过'
  }
  if (n === 2) {
    return '已拒绝'
  }
  return '未知'
}

Page({
  data: {
    loading: true,
    detail: null,
    shopId: '',
    reviewStatusText: '',
    formatCreateTime: '',
    formatReviewTime: '',
    /** 与 alumni-association/detail 的 status-tag 一致：status-pending / status-approved / status-rejected */
    auditTagClass: '',
    /** 门店环境图/背景（多图轮播） */
    gallery: [],
    /** 门店 logo（补全域名后） */
    logoUrl: '',
    /** 仅有 logo、无店铺图时顶部用大图展示 */
    showLogoHero: false,
  },

  onLoad(options) {
    const shopId = options.shop_id != null && options.shop_id !== ''
      ? String(options.shop_id)
      : (options.id != null && options.id !== '' ? String(options.id) : '')
    if (!shopId) {
      wx.showToast({ title: '缺少店铺参数', icon: 'none' })
      return
    }
    this.setData({ shopId })
    this.loadDetail()
  },

  /** 点击某张图：微信预览，可左右滑动看多张 */
  previewGalleryImage(e) {
    const idx = Number(e.currentTarget.dataset.index)
    const { gallery } = this.data
    if (!gallery || !gallery.length || Number.isNaN(idx)) {
      return
    }
    wx.previewImage({
      current: gallery[idx],
      urls: gallery,
    })
  },

  async loadDetail() {
    const { shopId } = this.data
    if (!shopId) {
      return
    }
    this.setData({ loading: true })
    try {
      const res = await get('/shop/approval/detail', { shop_id: shopId })
      if (res.data && res.data.code === 200 && res.data.data) {
        const d = res.data.data
        const { gallery, logoUrl } = buildGalleryAndLogo(d)
        const showLogoHero = gallery.length === 0 && !!logoUrl
        const rs = Number(d.reviewStatus)
        let auditTagClass = ''
        if (rs === 0) {
          auditTagClass = 'status-pending'
        } else if (rs === 1) {
          auditTagClass = 'status-approved'
        } else if (rs === 2) {
          auditTagClass = 'status-rejected'
        }
        this.setData({
          detail: d,
          gallery,
          logoUrl,
          showLogoHero,
          reviewStatusText: reviewStatusText(d.reviewStatus),
          formatCreateTime: formatTime(d.createTime),
          formatReviewTime: formatTime(d.reviewTime),
          auditTagClass,
          loading: false,
        })
      } else {
        this.setData({
          loading: false,
          detail: null,
          gallery: [],
          logoUrl: '',
          showLogoHero: false,
          auditTagClass: '',
        })
      }
    } catch (e) {
      console.error('加载店铺申请详情失败:', e)
      this.setData({
        loading: false,
        detail: null,
        gallery: [],
        logoUrl: '',
        showLogoHero: false,
        auditTagClass: '',
      })
    }
  },
})
