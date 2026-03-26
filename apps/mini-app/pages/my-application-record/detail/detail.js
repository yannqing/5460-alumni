const { post } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')

const RECORD_TYPE_TEXT_MAP = {
  ALUMNI_ASSOCIATION_CREATE: '创建校友会',
  ALUMNI_ASSOCIATION_JOIN: '加入校友会',
  ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM: '校友会加入校促会',
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
  },

  onLoad(options) {
    const recordType = decodeURIComponent(options?.recordType || '')
    const recordId = decodeURIComponent(options?.recordId || '')
    if (!recordType || !recordId) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1200)
      return
    }
    this.setData({ recordType, recordId })
    this.loadDetail()
  },

  async loadDetail() {
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const res = await post('/users/my-application-records/detail', {
        recordType: this.data.recordType,
        recordId: this.data.recordId,
      })
      if (!(res.data && res.data.code === 200 && res.data.data)) {
        wx.showToast({ title: res.data?.msg || res.data?.message || '加载失败', icon: 'none' })
        this.setData({ loading: false })
        return
      }

      const detailWrapper = res.data.data
      const detail = detailWrapper.detail || {}
      const attachmentFiles = this.normalizeAttachments(detail)
      const attachmentUrls = attachmentFiles.map(f => f._url)
      const { basicRows, contentRows, auditRows } = this.buildDisplayRows(detailWrapper, detail)
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
        loading: false,
      })
    } catch (e) {
      console.error('[MyApplicationRecordDetail] loadDetail', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ loading: false })
    }
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

  addRow(target, label, value, multiline = false) {
    if (!isPresent(value)) return
    target.push({ label, value: String(value), multiline })
  },

  buildDisplayRows(detailWrapper, detail) {
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

    return { basicRows, contentRows, auditRows }
  },

  previewAttachment(e) {
    const current = e.currentTarget.dataset.url
    if (!current) return
    wx.previewImage({
      current,
      urls: this.data.attachmentUrls,
    })
  },

  getStatusText(detailWrapper) {
    return detailWrapper?.applicationStatusText || '未知'
  },

  getStatusClass(detailWrapper) {
    const group = detailWrapper?.statusGroup || 'UNKNOWN'
    if (group === 'PENDING') return 'pending'
    if (group === 'APPROVED') return 'approved'
    if (group === 'REJECTED') return 'rejected'
    if (group === 'CANCELLED') return 'cancelled'
    return 'unknown'
  },

  applyTimeText() {
    const d = this.data.detail || {}
    return fmtTime(d.applyTime || d.createTime)
  },
})

