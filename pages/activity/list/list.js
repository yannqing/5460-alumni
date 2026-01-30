// pages/activity/list/list.js
const app = getApp()

Page({
  data: {
    searchValue: '',
    filterStatus: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'upcoming', label: '即将开始' },
      { id: 'ongoing', label: '进行中' },
      { id: 'finished', label: '已结束' }
    ],
    activityList: [],
    displayList: [],
    stats: {
      total: 0,
      upcoming: 0
    },
    // 校友会选择相关
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    selectedOrganizeId: 0, // 存储选中的organizeId
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份
  },

  onLoad() {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadAlumniAssociationList()
  },

  // 加载校友会列表（从缓存中获取校友会管理员的alumniAssociationId，然后调用接口）
  async loadAlumniAssociationList() {
    try {
      console.log('[Debug] 开始加载校友会列表')

      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)

      // 查找所有校友会管理员角色（根据roleName或remark）
      const alumniAdminRoles = roles.filter(role => 
        role.roleName === '校友会管理员' || role.remark === '校友会管理员'
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
            let associationName = alumniAdminRole.associationName || (alumniAdminRole.organization && alumniAdminRole.organization.associationName) || '校友会'
            
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
        selectedOrganizeId: singleAlumni.alumniAssociationId,
        hasSingleAlumniAssociation: true
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      // 加载该校友会的活动列表
      await this.loadActivityList()
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
      // 清空活动列表
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0
        }
      })
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

    // 获取对应的校友会对象
    const selectedAlumni = this.data.alumniAssociationList.find(item => item.alumniAssociationId === alumniAssociationId)
    console.log('[Debug] 找到的校友会对象:', selectedAlumni)

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false,
      selectedOrganizeId: alumniAssociationId // 确保使用校友会ID
    })

    try {
      // 调用 /AlumniAssociation/{id} 接口，入参为 alumniAssociationId
      console.log('[Debug] 准备调用 /AlumniAssociation/{id} 接口，alumniAssociationId:', alumniAssociationId)

      const res = await this.getAlumniAssociationDetail(alumniAssociationId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校友会信息:', res.data.data)
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /AlumniAssociation/{id} 接口失败:', apiError)
    }
    
    // 加载该校友会的活动列表
    await this.loadActivityList()
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
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 加载活动列表
  async loadActivityList() {
    const { selectedAlumniAssociationId } = this.data
    
    // 如果没有选择校友会，清空数据
    if (!selectedAlumniAssociationId) {
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0
        }
      })
      return
    }
    
    try {
      const res = await this.getActivityList(selectedAlumniAssociationId)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const activityList = res.data.data.map(item => ({
          id: item.activityId,
          title: item.activityTitle,
          organizer: item.organizerName,
          cover: item.organizerAvatar,
          participantCount: item.currentParticipants,
          location: `${item.province}${item.city}${item.district}${item.address}`,
          startTime: this.formatDateTime(item.startTime),
          endTime: this.formatDateTime(item.endTime),
          status: this.getActivityStatus(item.status),
          tags: [item.activityCategory],
          distance: 0 // 暂时设置为0，后续可以根据实际位置计算
        }))
        
        this.setData({
          activityList,
          stats: {
            total: activityList.length,
            upcoming: activityList.filter(item => item.status === 'upcoming').length
          }
        }, () => {
          this.applyFilter()
        })
      } else {
        console.error('获取活动列表失败:', res.data && res.data.msg || '接口调用失败')
        this.setData({
          activityList: [],
          displayList: [],
          stats: {
            total: 0,
            upcoming: 0
          }
        })
      }
    } catch (error) {
      console.error('获取活动列表异常:', error)
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0
        }
      })
    }
  },
  
  // 调用活动列表接口
  getActivityList(alumniAssociationId) {
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/activities/${alumniAssociationId}`,
        method: 'GET',
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },
  
  // 格式化日期时间
  formatDateTime(dateTimeString) {
    if (!dateTimeString) return ''
    const date = new Date(dateTimeString)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },
  
  // 获取活动状态
  getActivityStatus(status) {
    switch (status) {
      case 1:
      case 2:
        return 'upcoming'
      case 3:
        return 'ongoing'
      case 4:
      case 5:
        return 'finished'
      default:
        return 'upcoming'
    }
  },

  applyFilter() {
    const { activityList, filterStatus, searchValue } = this.data
    let list = [...activityList]

    if (filterStatus !== 'all') {
      list = list.filter(item => item.status === filterStatus)
    }

    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.title.includes(keyword) ||
          item.organizer.includes(keyword) ||
          item.location.includes(keyword)
      )
    }

    this.setData({ displayList: list })
  },

  handleSearchInput(e) {
    this.setData({ searchValue: e.detail.value }, () => {
      this.applyFilter()
    })
  },

  handleFilterChange(e) {
    const { id } = e.currentTarget.dataset
    if (id === this.data.filterStatus) return
    this.setData({ filterStatus: id }, () => {
      this.applyFilter()
    })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  },

  goToPublish() {
    if (!this.data.selectedAlumniAssociationId) {
      wx.showToast({ title: '请先选择校友会', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: '/pages/activity/publish/publish'
    })
  }
})
