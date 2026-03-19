// pages/audit/alumni-association-certification/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { userApi } = require('../../../../api/api.js')

Page({
  data: {
    applicationList: [],
    loading: false,
    selectedPlatformId: 0,
    selectedPlatformName: '',
    platformList: [],
    showPlatformPicker: false,
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已拒绝'],
    pageParams: {
      current: 1,
      pageSize: 10,
      platformId: 0
    },
    hasSinglePlatform: false // 是否只有一个校促会权限
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadPlatformList()
    this.loadApplicationList()
  },

  onShow() {
    this.loadApplicationList()
  },

  onPullDownRefresh() {
    this.data.pageParams.current = 1
    this.loadApplicationList()
    wx.stopPullDownRefresh()
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: parseInt(index)
    })
    this.loadApplicationList()
  },

  // 加载校促会列表（调用 users/managed-organizations?type=1 接口，type=1 表示校促会）
  async loadPlatformList() {
    try {
      console.log('[Debug] 开始加载校促会列表')
      const res = await userApi.getManagedOrganizations({ type: 1 })
      if (!res.data || res.data.code !== 200) {
        this.setData({ platformList: [] })
        return
      }
      const organizationList = res.data.data || []
      if (!Array.isArray(organizationList) || organizationList.length === 0) {
        this.setData({ platformList: [] })
        return
      }
      const platformList = organizationList.map(org => {
        let logo = org.logo || ''
        if (logo && !logo.startsWith('http://') && !logo.startsWith('https://')) {
          logo = config.getImageUrl(logo)
        }
        return {
          id: org.id,
          platformId: org.id,
          platformName: org.name || '校促会',
          logo: logo,
          location: org.location || '',
          type: org.type
        }
      })
      this.setData({ platformList })
      console.log('[Debug] 校促会列表:', platformList)
      this.handlePlatformSelection(platformList)
    } catch (error) {
      console.error('[Debug] 加载校促会列表失败:', error)
      this.setData({ platformList: [] })
    }
  },

  // 处理校促会选择逻辑
  handlePlatformSelection(platformList) {
    if (platformList.length === 1) {
      // 只有一个校促会权限，自动选择并禁用选择器
      const singlePlatform = platformList[0]
      this.setData({
        selectedPlatformId: singlePlatform.platformId,
        selectedPlatformName: singlePlatform.platformName,
        hasSinglePlatform: true,
        'pageParams.platformId': singlePlatform.platformId
      })
      console.log('[Debug] 只有一个校促会权限，自动选择:', singlePlatform)
      // 加载数据
      this.loadApplicationList()
    } else {
      // 多个校促会权限，正常显示选择器
      this.setData({
        hasSinglePlatform: false
      })
      console.log('[Debug] 有多个校促会权限，正常显示选择器')
    }
  },

  // 显示校促会选择器
  showPlatformSelector() {
    this.setData({ showPlatformPicker: true })
  },

  // 选择校促会
  async selectPlatform(e) {
    let { platformId, platformName } = e.currentTarget.dataset
    // 确保platformId是数字类型
    platformId = typeof platformId === 'string' ? parseInt(platformId, 10) : platformId
    console.log('[Debug] 选择的校促会:', { platformId, platformName, type: typeof platformId })

    this.setData({
      selectedPlatformId: platformId,
      selectedPlatformName: platformName,
      showPlatformPicker: false
    })

    // 更新查询参数
    this.setData({
      'pageParams.platformId': platformId,
      'pageParams.current': 1
    })

    try {
      // 调用 /localPlatform/{id} 接口，入参为 platformId
      console.log('[Debug] 准备调用 /localPlatform/{id} 接口，platformId:', platformId)

      // 使用正确的API方法名和参数格式
      const res = await app.api.localPlatformApi.getLocalPlatformDetail(platformId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校促会信息:', res.data.data)
        // 接口调用成功，可以在这里处理返回的数据
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatform/{id} 接口失败:', apiError)
    }

    // 重新加载数据
    this.loadApplicationList()
  },

  // 取消选择校促会
  cancelPlatformSelect() {
    this.setData({ showPlatformPicker: false })
  },

  // 加载校促会审核列表
  async loadApplicationList() {
    this.setData({
      loading: true
    })

    try {
      console.log('[Debug] 开始加载校促会审核列表，参数:', this.data.pageParams)

      // 构建API请求参数（platformId 用字符串传递，避免 JS 大数精度丢失）
      const apiParams = {
        current: this.data.pageParams.current,
        size: this.data.pageParams.pageSize,
        platformId: this.data.pageParams.platformId != null ? String(this.data.pageParams.platformId) : undefined
      }
      console.log('[Debug] 处理后的API请求参数:', apiParams)

      // 根据当前标签设置状态参数
      if (this.data.currentTab > 0) {
        apiParams.status = this.data.currentTab - 1 // 0: 待审核, 1: 已通过, 2: 已拒绝
      }

      console.log('[Debug] API请求参数:', apiParams)

      // 调用后端API获取审核列表
      const res = await app.api.associationApi.queryJoinApplyPage(apiParams)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的审核列表:', res.data.data)

        // 处理返回数据
        const records = res.data.data.records || []
        const processedRecords = records.map(record => {
          const status = parseInt(record.applyStatus, 10)
          const rawLogo = record.logo || ''
          // 清理logo URL中的多余空格和反引号
          const cleanedLogo = rawLogo.replace(/[`\s]/g, '')
          const displayLogo = cleanedLogo ? config.getImageUrl(cleanedLogo) : config.defaultAvatar
          // 申请人：使用 applicantName
          const displayApplicant = record.applicantName || ''
          // 提交时间：使用 createTime
          let displaySubmitTime = record.createTime || ''
          if (displaySubmitTime && typeof displaySubmitTime === 'string') {
            displaySubmitTime = displaySubmitTime.replace('T', ' ')
          }
          return {
            ...record,
            applicationId: typeof record.id === 'number' ? record.id.toString() : record.id, // 确保转换为字符串
            id: typeof record.id === 'number' ? record.id.toString() : record.id, // 确保转换为字符串
            alumniAssociationId: typeof record.alumniAssociationId === 'number' ? record.alumniAssociationId.toString() : record.alumniAssociationId || '', // 确保转换为字符串
            platformId: typeof record.platformId === 'number' ? record.platformId.toString() : record.platformId || '', // 确保转换为字符串
            applicationStatus: isNaN(status) ? 0 : status,
            displayLogo,
            displayApplicant,
            displaySubmitTime
          }
        })

        this.setData({
          applicationList: processedRecords,
          loading: false
        })
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
        this.setData({
          applicationList: [],
          loading: false
        })
      }
    } catch (error) {
      console.error('[Debug] 加载审核列表失败:', error)
      this.setData({
        applicationList: [],
        loading: false
      })
    }
  },

  // 获取状态文本
  getStatusText(status) {
    console.log('[Debug] getStatusText called with status:', status)
    // 确保 status 是数字类型，处理 null/undefined 情况
    const statusNum = status === null || status === undefined ? 0 : parseInt(status, 10)
    console.log('[Debug] statusNum:', statusNum)

    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      3: '已撤销'
    }
    return statusMap[statusNum] || '未知'
  },

  // 获取状态样式类
  getStatusClass(status) {
    console.log('[Debug] getStatusClass called with status:', status)

    // 根据状态值返回对应的样式类
    if (status === 0) { return 'pending' }
    if (status === 1) { return 'approved' }
    if (status === 2) { return 'rejected' }
    if (status === 3) { return 'withdrawn' }
    return ''
  },

  // 查看详情
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      wx.showToast({
        title: '申请ID不存在',
        icon: 'none'
      })
      return
    }
    wx.navigateTo({
      url: `/pages/audit/alumni-association-certification/detail/detail?id=${id}`
    })
  },

  // 批准申请
  async approveApplication(e) {
    const { id } = e.currentTarget.dataset
    console.log('[Debug] 批准申请，applicationId:', id, '类型:', typeof id)

    try {
      wx.showLoading({
        title: '处理中...'
      })

      // 准备审核参数
      const reviewData = {
        id: id, // 直接使用字符串类型，避免数字精度丢失
        status: 1 // 1-通过
      }

      console.log('[Debug] 审核参数:', reviewData)

      // 使用封装的 API 方法，确保云托管环境下的正确处理
      const res = await app.api.associationApi.reviewJoinPlatform(reviewData)

      console.log('[Debug] 批准申请结果:', res)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '批准成功',
          icon: 'success'
        })
        // 重新加载列表
        this.loadApplicationList()
      } else {
        console.error('[Debug] 批准失败，返回数据:', res)
        wx.showToast({
          title: '批准失败',
          icon: 'error'
        })
      }
    } catch (error) {
      console.error('[Debug] 批准申请失败:', error)
      wx.showToast({
        title: '批准失败',
        icon: 'error'
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 拒绝申请
  rejectApplication(e) {
    const { id } = e.currentTarget.dataset
    console.log('[Debug] 拒绝申请，applicationId:', id, '类型:', typeof id)

    // 弹出输入框，让用户输入审核意见
    wx.showModal({
      title: '拒绝申请',
      content: '请输入拒绝原因',
      editable: true,
      placeholderText: '请输入拒绝原因',
      success: async (res) => {
        if (res.confirm) {
          const reviewComment = res.content.trim()
          if (!reviewComment) {
            wx.showToast({
              title: '请输入拒绝原因',
              icon: 'error'
            })
            return
          }

          try {
            wx.showLoading({
              title: '处理中...'
            })

            // 准备审核参数（reviewComment 会随系统消息一并发送给申请人）
            const reviewData = {
              id: id, // 直接使用字符串类型，避免数字精度丢失
              status: 2, // 2-拒绝
              reviewComment: reviewComment // 审核意见，会显示在拒绝通知中
            }

            console.log('[Debug] 拒绝审核参数:', reviewData)

            // 使用封装的 API 方法，确保云托管环境下的正确处理
            const apiRes = await app.api.associationApi.reviewJoinPlatform(reviewData)

            console.log('[Debug] 拒绝申请结果:', apiRes)

            if (apiRes.data && apiRes.data.code === 200) {
              wx.showToast({
                title: '拒绝成功',
                icon: 'success'
              })
              // 重新加载列表
              this.loadApplicationList()
            } else {
              console.error('[Debug] 拒绝失败，返回数据:', apiRes)
              wx.showToast({
                title: '拒绝失败',
                icon: 'error'
              })
            }
          } catch (error) {
            console.error('[Debug] 拒绝申请失败:', error)
            wx.showToast({
              title: '拒绝失败',
              icon: 'error'
            })
          } finally {
            wx.hideLoading()
          }
        }
      }
    })
  }
})