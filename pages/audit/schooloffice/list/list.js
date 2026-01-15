// pages/audit/schooloffice/list/list.js
const app = getApp()

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
    }
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
      currentTab: index
    })
    this.loadApplicationList()
  },

  // 加载校处会列表（从缓存中获取校处会管理员的organizeId，然后调用接口）
  async loadPlatformList() {
    try {
      console.log('[Debug] 开始加载校处会列表')
      
      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)
      
      // 查找校处会管理员角色
      const schoolOfficeAdminRole = roles.find(role => role.remark === '校处会管理员')
      console.log('[Debug] 找到的校处会管理员角色:', schoolOfficeAdminRole)
      
      if (schoolOfficeAdminRole) {
        console.log('[Debug] 校处会管理员角色存在，检查organization:', schoolOfficeAdminRole.organization)
        
        if (schoolOfficeAdminRole.organization && schoolOfficeAdminRole.organization.organizeId) {
          const organizeId = schoolOfficeAdminRole.organization.organizeId
          console.log('[Debug] 获取到的organizeId:', organizeId)
          
          try {
            // 直接使用organizeId创建一个基本的校处会对象，避免接口调用失败
            const basicPlatformData = {
              id: organizeId,
              platformId: organizeId,
              platformName: `校处会 (ID: ${organizeId})`
            }
            
            this.setData({
              platformList: [basicPlatformData]
            })
            console.log('[Debug] 直接使用organizeId创建校处会列表:', [basicPlatformData])
            
            // 尝试调用接口获取更详细的信息（可选）
            try {
              // 使用正确的API方法
              const res = await app.api.localPlatformApi.getLocalPlatformDetail(organizeId)
              console.log('[Debug] 使用getLocalPlatformDetail方法调用成功')
              
              if (res.data && res.data.code === 200 && res.data.data) {
                console.log('[Debug] 接口调用成功，获取到的校处会信息:', res.data.data)
                
                const platformData = {
                  ...res.data.data,
                  id: res.data.data.platformId || res.data.data.id || organizeId,
                  platformName: res.data.data.platformName || res.data.data.name || `校处会 (ID: ${organizeId})`
                }
                
                this.setData({
                  platformList: [platformData]
                })
                console.log('[Debug] 已更新校处会列表:', [platformData])
              }
            } catch (apiError) {
              console.log('[Debug] 接口调用失败，继续使用基本数据:', apiError)
              // 继续使用之前创建的基本数据
            }
          } catch (error) {
            console.error('[Debug] 创建校处会数据失败:', error)
            this.setData({
              platformList: []
            })
          }
        } else {
          // 没有找到organizeId，设置为空数组
          console.warn('[Debug] 校处会管理员角色没有organization或organizeId')
          this.setData({
            platformList: []
          })
        }
      } else {
        // 没有找到校处会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校处会管理员角色')
        this.setData({
          platformList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校处会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        platformList: []
      })
    }
  },

  // 显示校处会选择器
  showPlatformSelector() {
    this.setData({ showPlatformPicker: true })
  },

  // 选择校处会
  async selectPlatform(e) {
    const { platformId, platformName } = e.currentTarget.dataset
    console.log('[Debug] 选择的校处会:', { platformId, platformName })
    
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
        console.log('[Debug] 接口调用成功，获取到的校处会信息:', res.data.data)
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

  // 取消选择校处会
  cancelPlatformSelect() {
    this.setData({ showPlatformPicker: false })
  },

  // 加载校处会审核列表
  async loadApplicationList() {
    this.setData({ 
      loading: true 
    })
    
    try {
      console.log('[Debug] 开始加载校处会审核列表，参数:', this.data.pageParams)
      
      // 调用后端API获取审核列表
      const res = await app.api.localPlatformApi.queryAssociationApplicationPage(this.data.pageParams)
      
      console.log('[Debug] 接口调用结果:', res)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的审核列表:', res.data.data)
        
        // 确保每条记录都有 applicationStatus 字段，并且是数字类型
        const records = res.data.data.records || []
        const processedRecords = records.map(record => {
          console.log('[Debug] 处理记录，applicationStatus:', record.applicationStatus)
          const status = parseInt(record.applicationStatus, 10)
          console.log('[Debug] 转换后的状态值:', status)
          return {
            ...record,
            applicationStatus: isNaN(status) ? 0 : status
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
    if (status === 0) return 'pending'
    if (status === 1) return 'approved'
    if (status === 2) return 'rejected'
    if (status === 3) return 'withdrawn'
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