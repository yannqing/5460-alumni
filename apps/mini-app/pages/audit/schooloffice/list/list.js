// pages/audit/schooloffice/list/list.js
const app = getApp()
const config = require('../../../../utils/config.js')

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

  // 加载校促会列表（从缓存中获取校促会管理员的organizeId，然后调用接口）
  async loadPlatformList() {
    try {
      console.log('[Debug] 开始加载校促会列表')
      
      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)
      
      // 查找所有校促会管理员角色
      const schoolOfficeAdminRoles = roles.filter(role => role.roleCode === 'ORGANIZE_LOCAL_ADMIN')
      console.log('[Debug] 找到的校促会管理员角色:', schoolOfficeAdminRoles)
      
      if (schoolOfficeAdminRoles.length > 0) {
        // 收集所有有效的organizeId
        const organizeIds = schoolOfficeAdminRoles
          .filter(role => role.organization && role.organization.organizeId)
          .map(role => role.organization.organizeId)
        
        console.log('[Debug] 获取到的organizeIds:', organizeIds)
        
        if (organizeIds.length > 0) {
          // 去重，确保每个organizeId只处理一次
          const uniqueOrganizeIds = [...new Set(organizeIds)]
          console.log('[Debug] 去重后的organizeIds:', uniqueOrganizeIds)
          
          // 先创建基本的校促会列表
          const basicPlatformList = uniqueOrganizeIds.map(organizeId => ({
            id: organizeId,
            platformId: organizeId,
            platformName: `校促会 (ID: ${organizeId})`
          }))
          
          this.setData({
            platformList: basicPlatformList
          })
          console.log('[Debug] 直接使用organizeIds创建校促会列表:', basicPlatformList)
          
          // 尝试调用接口获取更详细的信息（可选）
          try {
            // 并行调用所有校促会的详情接口
            const detailPromises = uniqueOrganizeIds.map(organizeId => 
              app.api.localPlatformApi.getLocalPlatformDetail(organizeId)
                .catch(error => {
                  console.log(`[Debug] 获取校促会 ${organizeId} 详情失败，使用基本数据:`, error)
                  return null // 接口失败时返回null，后续过滤
                })
            )
            
            const detailResults = await Promise.all(detailPromises)
            
            // 处理接口返回结果，更新校促会列表
            const updatedPlatformList = basicPlatformList.map((platform, index) => {
              const result = detailResults[index]
              if (result && result.data && result.data.code === 200 && result.data.data) {
                const detailData = result.data.data
                return {
                  ...detailData,
                  id: detailData.platformId || detailData.id || platform.platformId,
                  platformName: detailData.platformName || detailData.name || platform.platformName
                }
              }
              return platform // 接口失败时使用基本数据
            })
            
            this.setData({
              platformList: updatedPlatformList
            })
            console.log('[Debug] 已更新校促会列表:', updatedPlatformList)
            
            // 判断权限数量，处理自动选择逻辑
            this.handlePlatformSelection(updatedPlatformList)
          } catch (apiError) {
            console.log('[Debug] 批量获取校促会详情失败，继续使用基本数据:', apiError)
            // 继续使用之前创建的基本数据
            this.handlePlatformSelection(basicPlatformList)
          }
        } else {
          // 没有找到有效的organizeId，设置为空数组
          console.warn('[Debug] 校促会管理员角色没有有效的organization或organizeId')
          this.setData({
            platformList: []
          })
        }
      } else {
        // 没有找到校促会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校促会管理员角色')
        this.setData({
          platformList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校促会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        platformList: []
      })
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
    const { platformId, platformName } = e.currentTarget.dataset
    console.log('[Debug] 选择的校促会:', { platformId, platformName })
    
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
      
      // 调用后端API获取审核列表
      const res = await app.api.localPlatformApi.queryAssociationApplicationPage(this.data.pageParams)
      
      console.log('[Debug] 接口调用结果:', res)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的审核列表:', res.data.data)
        
        // 确保每条记录都有 applicationStatus 字段，并处理头像、申请人、提交时间
        const records = res.data.data.records || []
        const processedRecords = records.map(record => {
          const status = parseInt(record.applicationStatus, 10)
          const rawLogo = record.logo || record.associationLogo || ''
          const displayLogo = rawLogo ? config.getImageUrl(rawLogo) : config.defaultAlumniAvatar
          // 申请人：优先 chargeName，其次 applicantName、applicant
          const displayApplicant = record.chargeName || record.applicantName || record.applicant || '未知'
          // 提交时间：待审核时 reviewTime 为空，优先用 createTime/submitTime/applyTime；已通过/已拒绝可有 reviewTime
          let displaySubmitTime = record.applyTime || ''
          if (displaySubmitTime && typeof displaySubmitTime === 'string') {
            displaySubmitTime = displaySubmitTime.replace('T', ' ')
          }
          return {
            ...record,
            applicationStatus: isNaN(status) ? 0 : status,
            displayLogo,
            displayApplicant,
            displaySubmitTime
          }
        })
        
        // 根据当前标签过滤数据
        let filteredData = processedRecords
        if (this.data.currentTab === 1) {
          filteredData = processedRecords.filter(item => item.applicationStatus === 0)
        } else if (this.data.currentTab === 2) {
          filteredData = processedRecords.filter(item => item.applicationStatus === 1)
        } else if (this.data.currentTab === 3) {
          filteredData = processedRecords.filter(item => item.applicationStatus === 2)
        }
        
        this.setData({
          applicationList: filteredData,
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
    if (status === 0) {return 'pending'}
    if (status === 1) {return 'approved'}
    if (status === 2) {return 'rejected'}
    if (status === 3) {return 'withdrawn'}
    return ''
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/schooloffice/detail/detail?id=${id}`
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
        applicationId: id, // 直接使用字符串类型，避免数字精度丢失
        reviewResult: 1, // 1-通过
        reviewComment: '' // 通过时可以为空
      }
      
      console.log('[Debug] 审核参数:', reviewData)
      
      // 获取 token 并设置请求头（按照项目约定格式）
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }
      
      const headers = {
        'Content-Type': 'application/json'
      }
      
      if (token) {
        // 按照项目约定，token 直接放在 header 中，不使用 Bearer 前缀
        headers.token = token
        headers['x-token'] = token
      }
      
      // 直接使用 wx.request 调用审核接口
      const res = await new Promise((resolve, reject) => {
        wx.request({
          url: `${app.globalData.baseUrl}/localPlatformManagement/reviewAssociationApplication`,
          method: 'POST',
          data: reviewData,
          header: headers,
          success: resolve,
          fail: reject
        })
      })
      
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
            
            // 准备审核参数
            const reviewData = {
              applicationId: id, // 直接使用字符串类型，避免数字精度丢失
              reviewResult: 2, // 2-拒绝
              reviewComment: reviewComment
            }
            
            console.log('[Debug] 拒绝审核参数:', reviewData)
            
            // 获取 token 并设置请求头（按照项目约定格式）
            let token = wx.getStorageSync('token')
            if (!token) {
              const userInfo = wx.getStorageSync('userInfo') || {}
              token = userInfo.token || ''
            }
            
            const headers = {
              'Content-Type': 'application/json'
            }
            
            if (token) {
              // 按照项目约定，token 直接放在 header 中，不使用 Bearer 前缀
              headers.token = token
              headers['x-token'] = token
            }
            
            // 直接使用 wx.request 调用审核接口
            const apiRes = await new Promise((resolve, reject) => {
              wx.request({
                url: `${app.globalData.baseUrl}/localPlatformManagement/reviewAssociationApplication`,
                method: 'POST',
                data: reviewData,
                header: headers,
                success: resolve,
                fail: reject
              })
            })
            
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