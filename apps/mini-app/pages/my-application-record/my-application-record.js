// pages/my-application-record/my-application-record.js
// 与后端 MyApplicationRecordController#queryMyApplicationRecordPage 一致：POST /users/my-application-records/page
// 请求体：QueryMyApplicationRecordListDto — current, pageSize, recordTypes(可选), statusGroup(可选)
const { post } = require('../../utils/request.js')
const config = require('../../utils/config.js')

const TYPE_TABS = [
  { value: '', label: '全部' },
  { value: 'ALUMNI_ASSOCIATION_CREATE', label: '创建校友会' },
  { value: 'ALUMNI_ASSOCIATION_JOIN', label: '加入校友会' },
  { value: 'ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM', label: '加入校促会' },
]

const STATUS_TABS = [
  { value: '', label: '全部' },
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' },
  { value: 'CANCELLED', label: '已撤销' },
]

Page({
  data: {
    typeTabs: TYPE_TABS,
    statusTabs: STATUS_TABS,
    selectedRecordType: '',
    selectedStatusGroup: '',
    list: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    loadingMore: false,
  },

  onLoad() {
    this.loadList(true)
  },

  onShow() {
    if (this.data.list.length === 0 && !this.data.loading) {
      this.loadList(true)
    }
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  formatApplyTime(t) {
    if (t == null || t === '') {
      return ''
    }
    if (typeof t === 'string') {
      return t.replace('T', ' ').substring(0, 19)
    }
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
    return ''
  },

  mapRecord(item) {
    const isJoinPlatform = item.recordType === 'ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM'
    const associationLogoUrl = item.associationLogo
      ? config.getImageUrl(item.associationLogo)
      : config.defaultAvatar
    const platformLogoUrl = item.platformLogo
      ? config.getImageUrl(item.platformLogo)
      : config.defaultAvatar
    let platformNameOnly = ''
    if (isJoinPlatform && item.subtitle) {
      const s = String(item.subtitle).trim()
      platformNameOnly = s.replace(/^校促会[：:]\s*/i, '').trim() || s
    }
    return {
      ...item,
      _key: `${item.recordType || ''}-${item.recordId || ''}`,
      applyTimeText: this.formatApplyTime(item.applyTime),
      showDualLogos: isJoinPlatform,
      associationLogoUrl,
      platformLogoUrl,
      platformNameOnly,
    }
  },

  buildQueryBody(currentPage) {
    const body = {
      current: currentPage,
      pageSize: this.data.pageSize,
    }
    if (this.data.selectedRecordType) {
      body.recordTypes = [this.data.selectedRecordType]
    }
    if (this.data.selectedStatusGroup) {
      body.statusGroup = this.data.selectedStatusGroup
    }
    return body
  },

  selectRecordType(e) {
    const { value: raw } = e.currentTarget.dataset
    const value = raw === undefined || raw === null ? '' : raw
    if (value === this.data.selectedRecordType) {
      return
    }
    this.setData(
      {
        selectedRecordType: value,
        list: [],
        page: 1,
        hasMore: true,
        loading: false,
      },
      () => {
        this.loadList(true)
      }
    )
  },

  selectStatus(e) {
    const { value: raw } = e.currentTarget.dataset
    const v = raw === undefined || raw === null ? '' : raw
    if (v === this.data.selectedStatusGroup) {
      return
    }
    this.setData(
      {
        selectedStatusGroup: v,
        list: [],
        page: 1,
        hasMore: true,
        loading: false,
      },
      () => {
        this.loadList(true)
      }
    )
  },

  async loadList(reset = false) {
    if (this.data.loading) {
      return
    }
    if (!reset && !this.data.hasMore) {
      return
    }

    const nextPage = reset ? 1 : this.data.page

    this.setData({ loading: true })

    try {
      const res = await post('/users/my-application-records/page', this.buildQueryBody(nextPage))

      if (res.data && res.data.code === 200 && res.data.data) {
        const pageVo = res.data.data
        const raw = pageVo.records || []
        const mapped = raw.map(r => this.mapRecord(r))
        const total = Number(pageVo.total) || 0
        const merged = reset ? mapped : [...this.data.list, ...mapped]
        const hasMore = merged.length < total

        this.setData({
          list: merged,
          page: nextPage + 1,
          hasMore,
          loading: false,
        })
        return
      }

      wx.showToast({
        title: res.data?.msg || res.data?.message || '加载失败',
        icon: 'none',
      })
    } catch (e) {
      console.error('[MyApplicationRecord] loadList', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    }

    this.setData({ loading: false })
  },

  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) {
      return
    }
    this.setData({ loadingMore: true })
    this.loadList(false).finally(() => {
      this.setData({ loadingMore: false })
    })
  },

  onItemTap(e) {
    const { index } = e.currentTarget.dataset
    const item = this.data.list[index]
    if (!item) {
      return
    }
    wx.navigateTo({
      url: `/pages/my-application-record/detail/detail?recordType=${encodeURIComponent(item.recordType || '')}&recordId=${encodeURIComponent(item.recordId || '')}`,
    })
  },
})
