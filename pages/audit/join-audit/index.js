// pages/audit/join-audit/index.js
const app = getApp()

Page({
  data: {
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限,
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份,
    // 审核列表相关
    applicationList: [],
    applicationLoading: false,
    currentPage: 1,
    pageSize: 10,
    total: 0,
    applicationStatus: 0, // 0-待审核 1-已通过 2-已拒绝 3-已撤销
    hasMore: true,
    // 状态列表
    statusList: [
      { value: 0, label: '待审核' },
      { value: 1, label: '已通过' },
      { value: 2, label: '已拒绝' },
      { value: 3, label: '已撤销' }
    ],
    // 审核相关
    reviewing: false,
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadAlumniAssociationList()
  },

  // 加载审核列表
  async loadApplicationList(refresh = false) {
    if (this.data.applicationLoading) {return}
    
    if (refresh) {
      this.setData({
        currentPage: 1,
        applicationList: [],
        hasMore: true
      })
    }
    
    if (!this.data.hasMore) {return}
    
    try {
      this.setData({ applicationLoading: true })
      
      const params = {
        current: this.data.currentPage,
        pageSize: this.data.pageSize,
        alumniAssociationId: this.data.selectedAlumniAssociationId || 0,
        applicationStatus: this.data.applicationStatus
      }
      
      console.log('[Debug] 加载审核列表参数:', params)
      
      const res = await this.getApplicationList(params)
      
      console.log('[Debug] 加载审核列表结果:', res)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        // 处理数据：格式化时间（去掉T）
        const processedRecords = (res.data.data.records || []).map(item => {
          if (item.applyTime) {
            item.applyTime = item.applyTime.replace('T', ' ')
          }
          return item
        })
        
        const newList = refresh ? processedRecords : [...this.data.applicationList, ...processedRecords]
        
        this.setData({
          applicationList: newList,
          total: res.data.data.total || 0,
          hasMore: newList.length < (res.data.data.total || 0),
          currentPage: refresh ? 2 : this.data.currentPage + 1
        })
      } else {
        console.error('[Debug] 加载审核列表失败:', res)
        this.setData({ hasMore: false })
      }
    } catch (error) {
      console.error('[Debug] 加载审核列表异常:', error)
      this.setData({ hasMore: false })
    } finally {
      this.setData({ applicationLoading: false })
    }
  },

  // 调用审核列表接口
  getApplicationList(params) {
    return new Promise((resolve, reject) => {
      // 获取 token
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }

      const headers = {
        'Content-Type': 'application/json'
      }

      if (token) {
        headers.token = token
        headers['x-token'] = token
      }

      wx.request({
        url: `${app.globalData.baseUrl}/AlumniAssociationJoinApplication/page`,
        method: 'POST',
        data: params,
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 加载校友会列表（从缓存中获取校友会管理员的alumniAssociationId，然后调用接口）
  async loadAlumniAssociationList() {
    try {
      console.log('[Debug] 开始加载校友会列表')

      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)

      // 查找所有校友会管理员角色（根据roleCode）
      const alumniAdminRoles = roles.filter(role => 
        role.roleCode === 'ORGANIZE_ALUMNI_ADMIN'
      )
      console.log('[Debug] 找到的所有校友会管理员角色:', alumniAdminRoles)

      // 设置是否有校友会管理员身份
      this.setData({
        hasAlumniAdminPermission: alumniAdminRoles.length > 0
      })

      if (alumniAdminRoles.length > 0) {
        console.log('[Debug] 存在校友会管理员角色，开始处理每个角色')
        
        // 存储所有校友会数据
        const alumniAssociationList = []
        
        // 遍历所有校友会管理员角色，创建校友会数据
        for (const alumniAdminRole of alumniAdminRoles) {
          console.log('[Debug] 处理校友会管理员角色:', alumniAdminRole)
          
          // 尝试从不同可能的位置获取ID
          let alumniAssociationId = null

          // 检查直接字段
          if (alumniAdminRole.alumniAssociationId) {
            alumniAssociationId = alumniAdminRole.alumniAssociationId
            console.log('[Debug] 从直接字段获取到alumniAssociationId:', alumniAssociationId)
          }
          // 检查嵌套的organization字段
          else if (alumniAdminRole.organization && alumniAdminRole.organization.alumniAssociationId) {
            alumniAssociationId = alumniAdminRole.organization.alumniAssociationId
            console.log('[Debug] 从organization字段获取到alumniAssociationId:', alumniAssociationId)
          }
          // 检查organizeId字段（作为备用）
          else if (alumniAdminRole.organizeId) {
            alumniAssociationId = alumniAdminRole.organizeId
            console.log('[Debug] 从organizeId字段获取到ID:', alumniAssociationId)
          }
          // 检查嵌套的organization.organizeId字段
          else if (alumniAdminRole.organization && alumniAdminRole.organization.organizeId) {
            alumniAssociationId = alumniAdminRole.organization.organizeId
            console.log('[Debug] 从organization.organizeId字段获取到ID:', alumniAssociationId)
          }

          console.log('[Debug] 最终获取到的alumniAssociationId:', alumniAssociationId)

          if (alumniAssociationId) {
            // 获取协会名称（如果直接提供）
            const associationName = alumniAdminRole.associationName || alumniAdminRole.organization?.associationName || '校友会'
            
            // 创建基本的校友会对象
            const basicAlumniData = {
              id: alumniAssociationId,
              alumniAssociationId: alumniAssociationId,
              alumniAssociationName: `${associationName} (ID: ${alumniAssociationId})`,
              organizeId: alumniAdminRole.organizeId || alumniAssociationId // 存储organizeId
            }
            
            // 检查是否已经存在相同ID的校友会
            const existingIndex = alumniAssociationList.findIndex(item => 
              item.alumniAssociationId === alumniAssociationId
            )
            
            // 如果不存在，则添加到列表
            if (existingIndex === -1) {
              alumniAssociationList.push(basicAlumniData)
              console.log('[Debug] 添加校友会到列表:', basicAlumniData)
            } else {
              console.log('[Debug] 校友会已存在，跳过:', alumniAssociationId)
            }
          }
        }
        
        // 设置校友会列表
        this.setData({
          alumniAssociationList: alumniAssociationList
        })
        console.log('[Debug] 最终校友会列表:', alumniAssociationList)
        
        // 尝试为所有校友会调用接口获取更详细的信息
        try {
          // 创建一个新的列表来存储更新后的校友会数据
          const updatedList = [...alumniAssociationList]
          
          // 使用Promise.all并行获取所有校友会的详细信息
          const detailPromises = updatedList.map(async (alumni, index) => {
            try {
              const res = await this.getAlumniAssociationDetail(alumni.alumniAssociationId)
              if (res.data && res.data.code === 200 && res.data.data) {
                console.log(`[Debug] 获取校友会 ${index + 1} 详细信息成功:`, res.data.data)
                
                // 更新校友会的详细信息
                return {
                  ...res.data.data,
                  id: res.data.data.alumniAssociationId || res.data.data.id || alumni.alumniAssociationId,
                  alumniAssociationId: res.data.data.alumniAssociationId || alumni.alumniAssociationId,
                  alumniAssociationName: res.data.data.associationName || res.data.data.name || alumni.alumniAssociationName,
                  organizeId: res.data.data.organizeId || alumni.alumniAssociationId // 确保有organizeId
                }
              }
              return alumni // 如果接口调用失败，返回原始数据
            } catch (error) {
              console.log(`[Debug] 获取校友会 ${index + 1} 详细信息失败:`, error)
              return alumni // 如果发生错误，返回原始数据
            }
          })
          
          // 等待所有请求完成
          const detailedAlumniList = await Promise.all(detailPromises)
          
          // 更新校友会列表
          this.setData({
            alumniAssociationList: detailedAlumniList
          })
          console.log('[Debug] 已更新所有校友会详细信息:', detailedAlumniList)
          
          // 判断权限数量，处理自动选择逻辑
          this.handleAlumniAssociationSelection(detailedAlumniList)
        } catch (apiError) {
          console.log('[Debug] 获取校友会详细信息失败:', apiError)
          // 继续使用之前创建的基本数据
          this.handleAlumniAssociationSelection(alumniAssociationList)
        }
      } else {
        // 没有找到校友会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校友会管理员角色')
        this.setData({
          alumniAssociationList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校友会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        alumniAssociationList: []
      })
    }
  },

  // 处理校友会选择逻辑
  async handleAlumniAssociationSelection(alumniAssociationList) {
    if (alumniAssociationList.length === 1) {
      // 只有一个校友会权限，自动选择并禁用选择器
      const singleAlumni = alumniAssociationList[0]
      this.setData({
        selectedAlumniAssociationId: singleAlumni.alumniAssociationId,
        selectedAlumniAssociationName: singleAlumni.alumniAssociationName,
        hasSingleAlumniAssociation: true
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      // 加载审核列表
      await this.loadApplicationList(true)
    } else if (alumniAssociationList.length > 1) {
      // 多个校友会权限，正常显示选择器
      this.setData({
        hasSingleAlumniAssociation: false
      })
      console.log('[Debug] 有多个校友会权限，正常显示选择器')
    } else {
      // 没有校友会权限
      this.setData({
        hasSingleAlumniAssociation: false
      })
      console.log('[Debug] 没有校友会权限')
    }
  },

  // 显示校友会选择器
  showAlumniAssociationSelector() {
    this.setData({ showAlumniAssociationPicker: false })
    this.setData({ showAlumniAssociationPicker: true })
  },

  // 选择校友会
  async selectAlumniAssociation(e) {
    // 正确获取数据集属性
    const alumniAssociationId = e.currentTarget.dataset.alumniAssociationId
    const alumniAssociationName = e.currentTarget.dataset.alumniAssociationName
    console.log('[Debug] 选择的校友会:', { alumniAssociationId, alumniAssociationName })

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false
    })
    
    // 重新加载审核列表
    await this.loadApplicationList(true)
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 选择状态
  async selectStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status)
    if (status === this.data.applicationStatus) {return}
    
    this.setData({ applicationStatus: status })
    await this.loadApplicationList(true)
  },

  // 滚动触底加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.applicationLoading) {return}
    this.loadApplicationList()
  },

  // 通过申请
  async passApplication(e) {
    const applicationId = e.currentTarget.dataset.applicationId
    await this.submitReview(applicationId, 1)
  },

  // 拒绝申请
  async rejectApplication(e) {
    const applicationId = e.currentTarget.dataset.applicationId
    await this.submitReview(applicationId, 2)
  },

  // 提交审核
  async submitReview(applicationId, reviewResult) {
    if (this.data.reviewing) {return}
    
    try {
      this.setData({ reviewing: true })
      
      const params = {
        applicationId: applicationId,
        reviewResult: reviewResult,
        reviewComment: ''
      }
      
      console.log('[Debug] 提交审核参数:', params)
      
      const res = await this.reviewApplication(params)
      
      console.log('[Debug] 提交审核结果:', res)
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewResult === 1 ? '通过成功' : '拒绝成功',
          icon: 'success',
          duration: 2000
        })
        await this.loadApplicationList(true)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败',
          icon: 'none',
          duration: 2000
        })
      }
    } catch (error) {
      console.error('[Debug] 提交审核异常:', error)
      wx.showToast({
        title: '网络异常，请重试',
        icon: 'none',
        duration: 2000
      })
    } finally {
      this.setData({ reviewing: false })
    }
  },

  // 调用审核接口
  reviewApplication(params) {
    return new Promise((resolve, reject) => {
      // 获取 token
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }

      const headers = {
        'Content-Type': 'application/json'
      }

      if (token) {
        headers.token = token
        headers['x-token'] = token
      }

      wx.request({
        url: `${app.globalData.baseUrl}/AlumniAssociationJoinApplication/review`,
        method: 'POST',
        data: params,
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 调用校友会详情接口
  getAlumniAssociationDetail(alumniAssociationId) {
    return new Promise((resolve, reject) => {
      // 获取 token
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }

      const headers = {
        'Content-Type': 'application/json'
      }

      if (token) {
        headers.token = token
        headers['x-token'] = token
      }

      wx.request({
        url: `${app.globalData.baseUrl}/AlumniAssociation/${alumniAssociationId}`,
        method: 'GET',
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})
