// pages/merchant/activity/list/list.js
const { activityApi, merchantApi } = require('../../../../api/api.js')

Page({
  data: {
    merchantList: [],
    selectedMerchantId: '',
    selectedMerchantName: '',
    showMerchantSelector: false,
    activityList: [],
    current: 1,
    pageSize: 20,
    hasMore: true,
    loading: false,
    activityTypeFilter: 0,
    filterOptions: [
      { id: 0, name: '全部' },
      { id: 1, name: '优惠活动' },
      { id: 2, name: '话题活动' },
    ],
    statusMap: {
      0: '草稿',
      1: '报名中',
      2: '报名结束',
      3: '进行中',
      4: '已结束',
      5: '已取消',
    },
    statusClassMap: {
      0: 'status-draft',
      1: 'status-open',
      2: 'status-closed',
      3: 'status-ongoing',
      4: 'status-ended',
      5: 'status-cancelled',
    },
  },

  onLoad(options) {
    // 如果路由参数传了 merchantId，直接使用
    if (options.merchantId) {
      this.setData({
        selectedMerchantId: options.merchantId,
        selectedMerchantName: decodeURIComponent(options.merchantName || ''),
      })
      this.loadActivities(true)
    } else {
      // 否则加载用户的商户列表
      this.loadMerchantList()
    }
  },

  onShow() {
    // 从发布页返回时刷新列表
    if (this.data.selectedMerchantId) {
      this.loadActivities(true)
    }
  },

  onPullDownRefresh() {
    this.loadActivities(true).then(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadActivities(false)
    }
  },

  async loadMerchantList() {
    try {
      const res = await merchantApi.getMyMerchants()
      const pageData = res?.data?.data || {}
      const merchantList = Array.isArray(pageData.records) ? pageData.records : []
      const showMerchantSelector = merchantList.length > 1

      this.setData({ merchantList, showMerchantSelector })

      if (merchantList.length > 0) {
        const first = merchantList[0]
        const merchantIdStr = String(first.merchantId)
        this.setData({
          selectedMerchantId: merchantIdStr,
          selectedMerchantName: first.merchantName || '',
        })
        this.loadActivities(true)
      }
    } catch (e) {
      console.error('[MerchantActivityList] 加载商户列表失败:', e)
    }
  },

  onMerchantChange(e) {
    const idx = Number(e.detail.value)
    const merchant = this.data.merchantList[idx]
    if (!merchant) return
    this.setData({
      selectedMerchantId: String(merchant.merchantId),
      selectedMerchantName: merchant.merchantName || '',
      activityList: [],
      current: 1,
      hasMore: true,
    })
    this.loadActivities(true)
  },

  onFilterChange(e) {
    const activityType = Number(e.currentTarget.dataset.id)
    this.setData({ activityTypeFilter: activityType })
    this.loadActivities(true)
  },

  async loadActivities(reset) {
    if (this.data.loading) return
    const { selectedMerchantId, current, pageSize, activityTypeFilter } = this.data
    if (!selectedMerchantId) return

    const page = reset ? 1 : current
    this.setData({ loading: true })

    try {
      const params = {
        merchantId: Number(selectedMerchantId),
        current: page,
        pageSize,
      }
      if (activityTypeFilter > 0) {
        params.activityType = activityTypeFilter
      }

      const res = await activityApi.getMerchantActivityList(params)

      if (res.data && res.data.code === 200) {
        const pageData = res.data.data
        const records = (pageData.records || []).map(item => ({
          ...item,
          statusText: this.data.statusMap[item.status] || '未知',
          statusClass: this.data.statusClassMap[item.status] || '',
          typeLabel: item.activityType === 1 ? '优惠活动' : '话题活动',
        }))

        this.setData({
          activityList: reset ? records : [...this.data.activityList, ...records],
          current: page + 1,
          hasMore: records.length >= pageSize,
          loading: false,
        })
      } else {
        this.setData({ loading: false })
      }
    } catch (e) {
      console.error('[MerchantActivityList] 加载失败:', e)
      this.setData({ loading: false })
    }
  },

  goToPublish() {
    const { selectedMerchantId, selectedMerchantName } = this.data
    let url = '/pages/merchant/activity/publish/publish'
    if (selectedMerchantId) {
      url += `?merchantId=${selectedMerchantId}&merchantName=${encodeURIComponent(selectedMerchantName)}`
    }
    wx.navigateTo({ url })
  },

  viewDetail(e) {
    const activityId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${activityId}`,
    })
  },

  async deleteActivity(e) {
    const activityId = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复，确认删除该活动吗？',
      success: async res => {
        if (res.confirm) {
          try {
            const deleteRes = await activityApi.deleteActivity(activityId)
            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({ title: '删除成功', icon: 'success' })
              this.loadActivities(true)
            } else {
              wx.showToast({ title: deleteRes.data?.msg || '删除失败', icon: 'none' })
            }
          } catch (err) {
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      },
    })
  },
})
