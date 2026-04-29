const { post } = require('../../../utils/request.js')
const { myApplicationRecordApi, associationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const MY_APPLICATION_RECORD_LIST_NEED_REFRESH_KEY = 'MY_APPLICATION_RECORD_LIST_NEED_REFRESH'

const RECORD_TYPE_CREATE = 'ALUMNI_ASSOCIATION_CREATE'
const RECORD_TYPE_JOIN = 'ALUMNI_ASSOCIATION_JOIN'

const RECORD_TYPE_TEXT_MAP = {
  ALUMNI_ASSOCIATION_CREATE: '创建校友会',
  ALUMNI_ASSOCIATION_JOIN: '加入校友会',
  ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM: '校友会加入校促会',
  MERCHANT_APPLICATION: '商家创建申请',
  MERCHANT_ASSOCIATION_JOIN: '校友商户',
}

const EDITABLE_RECORD_TYPE = {
  ALUMNI_ASSOCIATION_JOIN: true,
  ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM: true,
  MERCHANT_APPLICATION: true,
}

function fmtTime(t) {
  if (!t) return ''
  if (typeof t === 'string') return t.replace('T', ' ').substring(0, 19)
  if (Array.isArray(t) && t.length >= 3) {
    const pad = n => String(n).padStart(2, '0')
    const y = t[0]
    const mo = pad(t[1])
    const d = pad(t[2])
    const h = t[3] != null ? pad(t[3]) : '00'
    const mi = t[4] != null ? pad(t[4]) : '00'
    const s = t[5] != null ? pad(t[5]) : '00'
    return `${y}-${mo}-${d} ${h}:${mi}:${s}`
  }
  return String(t)
}

function firstNonEmpty(...values) {
  for (let i = 0; i < values.length; i += 1) {
    const v = values[i]
    if (v === null || v === undefined) continue
    if (typeof v === 'string' && v.trim() === '') continue
    return v
  }
  return ''
}

function isPresent(v) {
  if (v === null || v === undefined) return false
  if (typeof v === 'string') return v.trim() !== ''
  return true
}

Page({
  data: {
    recordType: '',
    recordId: '',
    loading: false,
    detailWrapper: null,
    detail: null,
    recordTypeText: '',
    statusCodeText: '',
    basicRows: [],
    contentRows: [],
    auditRows: [],
    attachmentFiles: [],
    attachmentUrls: [],
    statusTextValue: '',
    statusClassName: 'unknown',
    canEditCurrent: false,
    canCancelCurrent: false,
    actionSubmitting: false,
    isCreateAssociation: false,
    createUi: null,
    isJoinAssociation: false,
    joinAssociationUi: null,
    merchantApplyUi: null,
    shopApplyUi: null,
    shopMerchantUi: null,
    merchantAssocUi: null,
    materialImageItems: [],
    materialImageUrls: [],
  },

  onLoad(options) {
    const recordType = decodeURIComponent(options?.recordType || '')
    const recordId = decodeURIComponent(options?.recordId || '')
    if (!recordType || !recordId) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1200)
      return
    }
    this._skipFirstOnShow = true
    this.setData({ recordType, recordId })
    this.loadDetail()
  },

  onShow() {
    if (this._skipFirstOnShow) {
      this._skipFirstOnShow = false
      return
    }
    if (this.data.recordType && this.data.recordId) {
      // 从编辑页返回时已有详情则静默刷新，避免整页变成「加载中」
      this.loadDetail({ silent: !!this.data.detailWrapper })
    }
  },

  async loadDetail(opts = {}) {
    const silent = !!opts.silent
    if (!silent) {
      if (this.data.loading) return
      this.setData({ loading: true })
    } else if (this._silentRefreshing) {
      return
    } else {
      this._silentRefreshing = true
    }
    try {
      const res = await post('/users/my-application-records/detail', {
        recordType: this.data.recordType,
        recordId: this.data.recordId,
      })
      if (!(res.data && res.data.code === 200 && res.data.data)) {
        wx.showToast({ title: res.data?.msg || res.data?.message || '加载失败', icon: 'none' })
        if (!silent) {
          this.setData({ loading: false })
        }
        return
      }

      const detailWrapper = res.data.data
      const detail = detailWrapper.detail || {}
      const attachmentFiles = this.normalizeAttachments(detail)
      const attachmentUrls = attachmentFiles.map(f => f._url)
      const materialImageItems = this.normalizeMaterialImages(detailWrapper, detail)
      const materialImageUrls = materialImageItems.map(it => it._url).filter(Boolean)
      const {
        basicRows,
        contentRows,
        auditRows,
        createUi,
        merchantApplyUi,
        shopApplyUi,
        shopMerchantUi,
        merchantAssocUi,
      } = this.buildDisplayRows(detailWrapper, detail)
      const joinAssociationUi = await this.buildJoinAssociationUi(detailWrapper, detail)
      this.setData({
        detailWrapper,
        detail,
        recordTypeText: this.getRecordTypeText(detailWrapper.recordType || this.data.recordType),
        statusCodeText: this.statusCodeText(detailWrapper.applicationStatus),
        statusTextValue: this.getStatusText(detailWrapper),
        statusClassName: this.getStatusClass(detailWrapper),
        basicRows,
        contentRows,
        auditRows,
        attachmentFiles,
        attachmentUrls,
        materialImageItems,
        materialImageUrls,
        canEditCurrent: this.isRecordEditable(detailWrapper),
        canCancelCurrent: this.isRecordCancelable(detailWrapper),
        isCreateAssociation: !!createUi && detailWrapper.recordType === RECORD_TYPE_CREATE,
        isMerchantApplication: detailWrapper.recordType === 'MERCHANT_APPLICATION',
        createUi: createUi || null,
        isJoinAssociation: !!joinAssociationUi,
        joinAssociationUi: joinAssociationUi || null,
        merchantApplyUi: merchantApplyUi || null,
        shopApplyUi: shopApplyUi || null,
        shopMerchantUi: shopMerchantUi || null,
        merchantAssocUi: merchantAssocUi || null,
        loading: false,
      })
    } catch (e) {
      console.error('[MyApplicationRecordDetail] loadDetail', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
      if (!silent) {
        this.setData({ loading: false })
      }
    } finally {
      if (silent) {
        this._silentRefreshing = false
      }
    }
  },

  isRecordEditable(detailWrapper) {
    if (!detailWrapper) {
      return false
    }
    const t = detailWrapper.recordType || this.data.recordType
    if (t === RECORD_TYPE_CREATE || t === 'MERCHANT_APPLICATION' || t === 'SHOP_APPLICATION') {
      return detailWrapper.canEdit === true
    }
    if (!detailWrapper.canEdit) {
      return false
    }
    return !!EDITABLE_RECORD_TYPE[t]
  },

  isRecordCancelable(detailWrapper) {
    if (!detailWrapper || detailWrapper.canCancel !== true) {
      return false
    }
    const t = detailWrapper.recordType || this.data.recordType
    return (
      t === RECORD_TYPE_CREATE ||
      t === 'ALUMNI_ASSOCIATION_JOIN' ||
      t === 'MERCHANT_APPLICATION' ||
      t === 'MERCHANT_ASSOCIATION_JOIN'
    )
  },

  onEditTap() {
    const { detailWrapper, detail, recordType, recordId } = this.data
    if (!this.isRecordEditable(detailWrapper)) {
      wx.showToast({ title: '当前申请暂不支持编辑', icon: 'none' })
      return
    }
    if (recordType === RECORD_TYPE_CREATE) {
      const applicationId = detail?.applicationId
      if (!applicationId) {
        wx.showToast({ title: '缺少申请信息', icon: 'none' })
        return
      }
      wx.navigateTo({
        url: `/pages/alumni-association/create/create?mode=edit&applicationId=${encodeURIComponent(String(applicationId))}&fromMyRecord=1&recordType=${encodeURIComponent(recordType)}&recordId=${encodeURIComponent(recordId)}`,
      })
      return
    }
    if (recordType === 'ALUMNI_ASSOCIATION_JOIN') {
      const associationId = detail?.alumniAssociationId || detail?.associationId
      if (!associationId) {
        wx.showToast({ title: '缺少校友会信息', icon: 'none' })
        return
      }
      wx.navigateTo({
        url: `/pages/alumni-association/apply/apply?id=${encodeURIComponent(String(associationId))}&mode=edit&fromMyRecord=1&recordType=${encodeURIComponent(recordType)}&recordId=${encodeURIComponent(recordId)}`,
      })
      return
    }
    if (recordType === 'ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM') {
      const associationId = detail?.alumniAssociationId
      if (!associationId) {
        wx.showToast({ title: '缺少校友会信息', icon: 'none' })
        return
      }
      wx.navigateTo({
        url: `/pages/local-platform/apply/apply?fromMyRecord=1&recordType=${encodeURIComponent(recordType)}&recordId=${encodeURIComponent(recordId)}&alumniAssociationId=${encodeURIComponent(String(associationId))}&platformId=${encodeURIComponent(String(detail?.platformId || ''))}`,
      })
      return
    }
    if (recordType === 'MERCHANT_APPLICATION') {
      const applicationId = firstNonEmpty(detail?.applicationId, recordId)
      if (!applicationId) {
        wx.showToast({ title: '缺少申请信息', icon: 'none' })
        return
      }
      wx.navigateTo({
        url: `/pages/merchant/application-edit/application-edit?recordId=${encodeURIComponent(String(applicationId))}`,
      })
      return
    }
    if (recordType === 'SHOP_APPLICATION') {
      const shopId = firstNonEmpty(detail?.shopId, recordId)
      if (!shopId) {
        wx.showToast({ title: '缺少门店信息', icon: 'none' })
        return
      }
      wx.navigateTo({
        url: `/pages/audit/merchant/shop/edit/edit?shopId=${encodeURIComponent(String(shopId))}&fromMyRecord=1&recordType=${encodeURIComponent(recordType)}&recordId=${encodeURIComponent(recordId)}`,
      })
      return
    }
    wx.showToast({ title: '当前申请暂不支持编辑', icon: 'none' })
  },

  onCancelTap() {
    if (this.data.actionSubmitting) {
      return
    }
    const { detailWrapper, recordType, recordId } = this.data
    if (!this.isRecordCancelable(detailWrapper)) {
      wx.showToast({ title: '当前申请暂不支持撤销', icon: 'none' })
      return
    }
    wx.showModal({
      title: '撤销申请',
      content: '确认撤销该申请吗？撤销后不可恢复。',
      confirmText: '确认撤销',
      confirmColor: '#ef4444',
      success: async res => {
        if (!res.confirm) {
          return
        }
        this.setData({ actionSubmitting: true })
        try {
          const result = await myApplicationRecordApi.cancel({
            recordType,
            recordId,
          })
          if (result?.data?.code === 200) {
            wx.setStorageSync(MY_APPLICATION_RECORD_LIST_NEED_REFRESH_KEY, Date.now())
            wx.showToast({ title: '撤销成功', icon: 'success' })
            this.loadDetail({ silent: true })
            return
          }
          wx.showToast({
            title: result?.data?.msg || result?.data?.message || '撤销失败',
            icon: 'none',
          })
        } catch (e) {
          console.error('[MyApplicationRecordDetail] cancel', e)
          wx.showToast({ title: '撤销失败', icon: 'none' })
        } finally {
          this.setData({ actionSubmitting: false })
        }
      },
    })
  },

  normalizeMaterialImages(detailWrapper, detail) {
    const raw = detailWrapper?.materialImages
    const recordType = detailWrapper?.recordType || ''
    const d = detail || {}
    if (!Array.isArray(raw) || raw.length === 0) {
      return []
    }
    const mapped = raw
      .map((item, index) => {
        const u = item?.url != null ? String(item.url).trim() : ''
        if (!u) {
          return null
        }
        const full = /^https?:\/\//i.test(u) ? u : config.getImageUrl(u)
        return {
          kind: item.kind || '',
          label: item.label || `图片${index + 1}`,
          url: u,
          _url: full,
        }
      })
      .filter(Boolean)
    // 商户详情顶部已展示 Logo + 背景图，下方资料区仅保留营业执照等
    if (recordType === 'MERCHANT_APPLICATION') {
      return mapped.filter(it => it.kind !== 'LOGO' && it.kind !== 'BACKGROUND')
    }
    // 门店：顶栏展示 Logo + 首张门店图作背景，下方不再重复这两项
    if (recordType === 'SHOP_APPLICATION') {
      const firstGallery = this.extractFirstShopGalleryPath(d.shopImages)
      let skippedFirstGallery = false
      return mapped.filter(it => {
        if (it.kind === 'SHOP_LOGO') {
          return false
        }
        if (firstGallery && it.kind === 'SHOP_IMAGE' && !skippedFirstGallery) {
          if (String(it.url).trim() === String(firstGallery).trim()) {
            skippedFirstGallery = true
            return false
          }
        }
        return true
      })
    }
    return mapped
  },

  /** 取门店图 JSON 首张路径（与 buildShopApplicationHeader 一致） */
  extractFirstShopGalleryPath(shopImagesJson) {
    if (shopImagesJson == null || !String(shopImagesJson).trim()) {
      return null
    }
    const t = String(shopImagesJson).trim()
    try {
      const parsed = JSON.parse(t)
      if (Array.isArray(parsed) && parsed.length > 0) {
        const first = parsed[0]
        if (typeof first === 'string') {
          return first.trim() || null
        }
        const p = first?.url || first?.fileUrl || ''
        return p ? String(p).trim() : null
      }
    } catch (e) {
      return t
    }
    return null
  },

  normalizeAttachments(detail) {
    const raw = Array.isArray(detail?.attachments)
      ? detail.attachments
      : Array.isArray(detail?.attachmentFiles)
        ? detail.attachmentFiles
        : []
    return raw
      .map(file => {
        const rawUrl = file?.url || file?.fileUrl || ''
        const url = rawUrl ? config.getImageUrl(rawUrl) : ''
        return {
          ...file,
          _url: url,
          _name: file?.fileName || file?.name || '附件',
        }
      })
      .filter(f => !!f._url)
  },

  getRecordTypeText(type) {
    if (!type) return '未知类型'
    return RECORD_TYPE_TEXT_MAP[type] || type
  },

  statusCodeText(code) {
    if (code === null || code === undefined || code === '') return ''
    return String(code)
  },

  addRow(target, label, value, multiline = false, noWrap = false) {
    if (!isPresent(value)) return
    target.push({ label, value: String(value), multiline, noWrap })
  },

  buildCreateAssociationHeader(detail) {
    const name = firstNonEmpty(detail.associationName, detail.alumniAssociationName) || '—'
    let bgUrl = config.defaultCover
    const rawBg = detail.bgImg
    if (rawBg != null && String(rawBg).trim()) {
      try {
        const parsed = JSON.parse(rawBg)
        if (Array.isArray(parsed) && parsed.length > 0) {
          bgUrl = config.getImageUrl(parsed[0])
        } else {
          bgUrl = config.getImageUrl(String(rawBg))
        }
      } catch (e) {
        bgUrl = config.getImageUrl(String(rawBg))
      }
    }
    let logoUrl = config.defaultAvatar
    if (detail.logo != null && String(detail.logo).trim()) {
      logoUrl = config.getImageUrl(detail.logo)
    }
    return { bgUrl, logoUrl, name }
  },

  buildJoinAssociationHeader(detail, associationDetail) {
    const source = associationDetail || detail || {}
    const name = firstNonEmpty(
      source.associationName,
      detail?.associationName,
      detail?.alumniAssociationName
    ) || '—'

    let bgUrl = config.defaultCover
    const rawBg = firstNonEmpty(source.bgImg, detail?.bgImg)
    if (rawBg != null && String(rawBg).trim()) {
      try {
        const parsed = typeof rawBg === 'string' ? JSON.parse(rawBg) : rawBg
        if (Array.isArray(parsed) && parsed.length > 0) {
          bgUrl = config.getImageUrl(parsed[0])
        } else if (typeof parsed === 'string') {
          bgUrl = config.getImageUrl(parsed)
        } else {
          bgUrl = config.getImageUrl(String(rawBg))
        }
      } catch (e) {
        bgUrl = config.getImageUrl(String(rawBg))
      }
    }

    let logoUrl = config.defaultAvatar
    const rawLogo = firstNonEmpty(source.logo, detail?.logo)
    if (rawLogo != null && String(rawLogo).trim()) {
      logoUrl = config.getImageUrl(String(rawLogo))
    }
    return { bgUrl, logoUrl, name }
  },

  async buildJoinAssociationUi(detailWrapper, detail) {
    const recordType = detailWrapper?.recordType || this.data.recordType
    if (recordType !== RECORD_TYPE_JOIN) {
      return null
    }
    const associationId = firstNonEmpty(detail?.alumniAssociationId, detail?.associationId)
    let associationDetail = null
    if (associationId) {
      try {
        const res = await associationApi.getAssociationDetail(associationId)
        if (res?.data?.code === 200 && res?.data?.data) {
          associationDetail = res.data.data
        }
      } catch (e) {
        console.warn('[MyApplicationRecordDetail] getAssociationDetail failed', e)
      }
    }
    return {
      header: this.buildJoinAssociationHeader(detail, associationDetail),
    }
  },

  parseInitialMembersList(detail) {
    const raw = detail.initialMembers
    if (!raw || typeof raw !== 'string') {
      return []
    }
    try {
      const list = JSON.parse(raw)
      return Array.isArray(list) ? list : []
    } catch (e) {
      return []
    }
  },

  buildCreateAssociationRows(detailWrapper, detail) {
    const basicRows = []
    const auditRows = []

    const applyTime = fmtTime(firstNonEmpty(detail.applyTime, detail.createTime))
    const reviewTime = fmtTime(firstNonEmpty(detail.reviewTime))

    this.addRow(basicRows, '申请类型', this.getRecordTypeText(detailWrapper.recordType || this.data.recordType))
    this.addRow(basicRows, '申请时间', applyTime)
    this.addRow(basicRows, '审核时间', reviewTime)

    this.addRow(auditRows, '审核意见', detail.reviewComment, true)
    this.addRow(auditRows, '审核时间', reviewTime)

    const header = this.buildCreateAssociationHeader(detail)

    let school = null
    const si = detail.schoolInfo
    if (si && (isPresent(si.schoolName) || isPresent(si.province))) {
      school = {
        schoolName: firstNonEmpty(si.schoolName) || '—',
        province: firstNonEmpty(si.province),
      }
    }

    const chargeRows = []
    this.addRow(chargeRows, '姓名', detail.chargeName)
    this.addRow(chargeRows, '联系方式', detail.contactInfo)
    this.addRow(chargeRows, '校友会职务', detail.chargeRole)
    this.addRow(chargeRows, '社会职务', detail.msocialAffiliation, false, true)

    const zhRows = []
    this.addRow(zhRows, '姓名', detail.zhName)
    this.addRow(zhRows, '联系电话', detail.zhPhone)
    this.addRow(zhRows, '校友会职务', detail.zhRole)
    this.addRow(zhRows, '社会职务', detail.zhSocialAffiliation, false, true)

    const initialMembersList = this.parseInitialMembersList(detail)

    const createUi = {
      header,
      school,
      chargeRows,
      zhRows,
      location: isPresent(detail.location) ? String(detail.location) : '',
      coverageArea: isPresent(detail.coverageArea) ? String(detail.coverageArea) : '',
      associationProfile: isPresent(detail.associationProfile) ? String(detail.associationProfile) : '',
      applicationReason: isPresent(detail.applicationReason) ? String(detail.applicationReason) : '',
      initialMembersList,
    }

    return {
      basicRows,
      contentRows: [],
      auditRows,
      createUi,
      merchantApplyUi: null,
      shopApplyUi: null,
      shopMerchantUi: null,
      merchantAssocUi: null,
    }
  },

  /**
   * 关联校友会（MerchantDetailVo.alumniAssociation）
   * 默认图与 pages/alumni-association/list/list 列表项 `icon` 一致：
   * `item.logo ? config.getImageUrl(item.logo) : config.defaultAvatar`
   */
  buildMerchantAssocUi(detail) {
    const a = detail?.alumniAssociation
    if (!a) {
      return null
    }
    const name = firstNonEmpty(a.associationName)
    if (!name) {
      return null
    }
    const raw = a.logo != null ? String(a.logo).trim() : ''
    const logoUrl = raw ? config.getImageUrl(raw) : config.defaultAvatar
    const schoolName = firstNonEmpty(a.school?.schoolName)
    return { associationName: name, logoUrl, schoolName }
  },

  /** 商户入驻：顶栏背景图 + Logo，与创建校友会详情同一套样式 */
  buildMerchantApplicationHeader(detail) {
    const name = firstNonEmpty(detail.merchantName) || '—'
    let bgUrl = config.defaultCover
    const rawBg = detail.backgroundImage
    if (rawBg != null && String(rawBg).trim()) {
      try {
        const parsed = JSON.parse(String(rawBg))
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first = parsed[0]
          const path =
            typeof first === 'string'
              ? first
              : first?.url || first?.fileUrl || ''
          if (path) {
            const p = String(path).trim()
            bgUrl = /^https?:\/\//i.test(p) ? p : config.getImageUrl(p)
          }
        } else {
          bgUrl = config.getImageUrl(String(rawBg).trim())
        }
      } catch (e) {
        bgUrl = config.getImageUrl(String(rawBg).trim())
      }
    }
    let logoUrl = config.defaultAvatar
    if (detail.logo != null && String(detail.logo).trim()) {
      logoUrl = config.getImageUrl(String(detail.logo).trim())
    }
    return { bgUrl, logoUrl, name }
  },

  /** 门店申请：首张门店图作顶栏背景 + 门店 Logo（样式与商户顶栏一致） */
  buildShopApplicationHeader(detail) {
    const name = firstNonEmpty(detail.shopName) || '—'
    let bgUrl = config.defaultCover
    const raw = detail.shopImages
    if (raw != null && String(raw).trim()) {
      try {
        const parsed = JSON.parse(String(raw))
        if (Array.isArray(parsed) && parsed.length > 0) {
          const first = parsed[0]
          const path =
            typeof first === 'string'
              ? first
              : first?.url || first?.fileUrl || ''
          if (path) {
            const p = String(path).trim()
            bgUrl = /^https?:\/\//i.test(p) ? p : config.getImageUrl(p)
          }
        } else {
          bgUrl = config.getImageUrl(String(raw).trim())
        }
      } catch (e) {
        bgUrl = config.getImageUrl(String(raw).trim())
      }
    }
    let logoUrl = config.defaultAvatar
    if (detail.logo != null && String(detail.logo).trim()) {
      logoUrl = config.getImageUrl(String(detail.logo).trim())
    }
    return { bgUrl, logoUrl, name }
  },

  /** 门店申请详情「所属商家」：与「关联校友会」同一套，仅 logo + 名称 */
  buildShopMerchantUi(detail) {
    const m = detail?.merchant
    if (!m || typeof m !== 'object') return null
    const name = firstNonEmpty(m.merchantName)
    if (!name) return null
    const raw = m.logo != null ? String(m.logo).trim() : ''
    const logoUrl = raw ? config.getImageUrl(raw) : config.defaultAvatar
    return { merchantName: name, logoUrl }
  },

  buildDisplayRows(detailWrapper, detail) {
    const recordType = detailWrapper.recordType || this.data.recordType
    if (recordType === RECORD_TYPE_CREATE) {
      return this.buildCreateAssociationRows(detailWrapper, detail)
    }
    if (recordType === 'MERCHANT_APPLICATION') {
      return this.buildMerchantApplicationRows(detailWrapper, detail)
    }
    if (recordType === 'SHOP_APPLICATION') {
      return this.buildShopApplicationRows(detailWrapper, detail)
    }
    if (recordType === 'MERCHANT_ASSOCIATION_JOIN') {
      return this.buildMerchantAssociationJoinRows(detailWrapper, detail)
    }

    const basicRows = []
    const contentRows = []
    const auditRows = []

    const applyTime = fmtTime(firstNonEmpty(detail.applyTime, detail.createTime))
    const reviewTime = fmtTime(firstNonEmpty(detail.reviewTime))
    const schoolName = firstNonEmpty(detail.schoolName, detail.schoolInfo?.schoolName)

    this.addRow(basicRows, '申请类型', this.getRecordTypeText(detailWrapper.recordType || this.data.recordType))
    this.addRow(basicRows, '申请时间', applyTime)
    this.addRow(basicRows, '审核时间', reviewTime)

    this.addRow(contentRows, '校友会', firstNonEmpty(detail.associationName, detail.alumniAssociationName))
    this.addRow(contentRows, '校促会', detail.platformName)
    this.addRow(contentRows, '母校', schoolName)
    this.addRow(contentRows, '姓名', firstNonEmpty(detail.name, detail.applicantName, detail.zhName, detail.applyZhName, detail.chargeName, detail.applyChargeName))
    this.addRow(contentRows, '手机号', firstNonEmpty(detail.phone, detail.applicantPhone, detail.zhPhone, detail.applyZhPhone))
    this.addRow(contentRows, '联系方式', firstNonEmpty(detail.contactInfo, detail.applyContactInfo))
    this.addRow(contentRows, '常驻地点', detail.location)
    this.addRow(contentRows, '覆盖区域', detail.coverageArea)
    this.addRow(contentRows, '院系', detail.department)
    this.addRow(contentRows, '专业', detail.major)
    this.addRow(contentRows, '班级', detail.className)
    this.addRow(contentRows, '入学年份', detail.enrollmentYear)
    this.addRow(contentRows, '毕业年份', detail.graduationYear)
    this.addRow(contentRows, '学历层次', detail.educationLevel)
    this.addRow(contentRows, '社会职务', firstNonEmpty(detail.zhSocialAffiliation, detail.chargeSocialAffiliation, detail.msocialAffiliation))
    this.addRow(contentRows, '申请理由', detail.applicationReason, true)
    this.addRow(contentRows, '校友会简介', detail.associationProfile, true)

    this.addRow(auditRows, '审核意见', detail.reviewComment, true)
    this.addRow(auditRows, '审核时间', reviewTime)

    return {
      basicRows,
      contentRows,
      auditRows,
      createUi: null,
      merchantApplyUi: null,
      shopApplyUi: null,
      shopMerchantUi: null,
      merchantAssocUi: null,
    }
  },

  buildMerchantApplicationRows(detailWrapper, detail) {
    const basicRows = []
    const contentRows = []
    const auditRows = []
    const reviewTime = fmtTime(firstNonEmpty(detail.reviewTime))
    this.addRow(basicRows, '申请类型', this.getRecordTypeText(detailWrapper.recordType || this.data.recordType))
    this.addRow(basicRows, '申请时间', fmtTime(firstNonEmpty(detail.createTime)))
    this.addRow(basicRows, '审核时间', reviewTime)
    this.addRow(contentRows, '商户名称', detail.merchantName)
    this.addRow(contentRows, '所在城市', detail.city)
    this.addRow(contentRows, '法人姓名', detail.legalPerson)
    this.addRow(contentRows, '联系电话', detail.phone)
    this.addRow(contentRows, '统一社会信用代码', detail.unifiedSocialCreditCode)
    this.addRow(auditRows, '审核意见', detail.reviewReason, true)
    this.addRow(auditRows, '审核时间', reviewTime)
    return {
      basicRows,
      contentRows,
      auditRows,
      createUi: {
        header: this.buildMerchantApplicationHeader(detail),
      },
      merchantApplyUi: null,
      shopApplyUi: null,
      shopMerchantUi: null,
      merchantAssocUi: null,
    }
  },

  buildShopApplicationRows(detailWrapper, detail) {
    const basicRows = []
    const contentRows = []
    const auditRows = []
    const reviewTime = fmtTime(firstNonEmpty(detail.reviewTime))
    const merchantName = firstNonEmpty(detail.merchant?.merchantName)
    const addr = [detail.province, detail.city, detail.district, detail.address].filter(x => isPresent(x)).join(' ')
    this.addRow(basicRows, '申请类型', this.getRecordTypeText(detailWrapper.recordType || this.data.recordType))
    this.addRow(basicRows, '申请时间', fmtTime(firstNonEmpty(detail.createTime)))
    this.addRow(basicRows, '审核时间', reviewTime)
    this.addRow(contentRows, '门店名称', detail.shopName)
    this.addRow(contentRows, '所属商户', merchantName)
    this.addRow(contentRows, '门店类型', detail.shopType === 1 ? '总店' : detail.shopType === 2 ? '分店' : detail.shopType)
    this.addRow(contentRows, '地址', addr, true)
    this.addRow(contentRows, '门店电话', detail.phone)
    this.addRow(contentRows, '营业时间', detail.businessHours)
    this.addRow(contentRows, '简介', detail.description, true)
    this.addRow(auditRows, '审核意见', detail.reviewReason, true)
    this.addRow(auditRows, '审核时间', reviewTime)
    return {
      basicRows,
      contentRows,
      auditRows,
      createUi: null,
      merchantApplyUi: null,
      shopApplyUi: {
        header: this.buildShopApplicationHeader(detail),
      },
      shopMerchantUi: this.buildShopMerchantUi(detail),
      merchantAssocUi: null,
    }
  },

  buildMerchantAssociationJoinRows(detailWrapper, detail) {
    const basicRows = []
    const contentRows = []
    const auditRows = []
    const reviewTime = fmtTime(firstNonEmpty(detail.reviewTime))
    this.addRow(basicRows, '申请类型', this.getRecordTypeText(detailWrapper.recordType || this.data.recordType))
    this.addRow(basicRows, '申请时间', fmtTime(firstNonEmpty(detail.createTime)))
    this.addRow(basicRows, '审核时间', reviewTime)
    
    this.addRow(contentRows, '商户名称', detail.merchantName)
    const assName = detail.alumniAssociation?.associationName
    this.addRow(contentRows, '申请加入校友会', assName)
    this.addRow(contentRows, '申请人', detail.applicantName)
    this.addRow(contentRows, '联系电话', detail.applicantPhone)
    
    this.addRow(auditRows, '审核意见', detail.reviewComment, true)
    this.addRow(auditRows, '审核时间', reviewTime)
    
    return {
      basicRows,
      contentRows,
      auditRows,
      createUi: null,
      merchantApplyUi: {
        header: this.buildMerchantApplicationHeader(detail),
      },
      shopApplyUi: null,
      shopMerchantUi: null,
      merchantAssocUi: this.buildMerchantAssocUi(detail),
    }
  },

  previewAttachment(e) {
    const current = e.currentTarget.dataset.url
    if (!current) return
    wx.previewImage({
      current,
      urls: this.data.attachmentUrls,
    })
  },

  previewMaterialImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    const urls = this.data.materialImageUrls || []
    if (!urls.length) return
    const current = urls[Number.isFinite(index) ? index : 0]
    if (!current) return
    wx.previewImage({ current, urls })
  },

  getStatusText(detailWrapper) {
    return detailWrapper?.applicationStatusText || '未知'
  },

  getStatusClass(detailWrapper) {
    const group = detailWrapper?.statusGroup || 'UNKNOWN'
    if (group === 'PENDING') return 'pending'
    if (group === 'APPROVED' || group === 'PENDING_PUBLISH') return 'approved'
    if (group === 'REJECTED') return 'rejected'
    if (group === 'CANCELLED') return 'cancelled'
    return 'unknown'
  },

  applyTimeText() {
    const d = this.data.detail || {}
    return fmtTime(d.applyTime || d.createTime)
  },
})

