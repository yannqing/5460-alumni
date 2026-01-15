// pages/alumni-association/organization/organization.js
const app = getApp()

Page({
  data: {
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    roleList: [], // 存储角色列表
    roleLoading: false, // 角色加载状态
    selectedOrganizeId: 0 // 存储选中的organizeId
  },

  onLoad(options) {
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
      
      // 查找校友会管理员角色
      const alumniAdminRole = roles.find(role => role.remark === '校友会管理员')
      console.log('[Debug] 找到的校友会管理员角色:', alumniAdminRole)
      
      if (alumniAdminRole) {
        console.log('[Debug] 校友会管理员角色存在，检查数据结构:', alumniAdminRole)
        
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
          let associationName = alumniAdminRole.associationName || '校友会'
          if (alumniAdminRole.organization && alumniAdminRole.organization.associationName) {
            associationName = alumniAdminRole.organization.associationName
          }
          
          try {
            // 直接使用alumniAssociationId创建一个基本的校友会对象，避免接口调用失败
            const basicAlumniData = {
              id: alumniAssociationId,
              alumniAssociationId: alumniAssociationId,
              alumniAssociationName: `${associationName} (ID: ${alumniAssociationId})`,
              organizeId: alumniAdminRole.organizeId || alumniAssociationId // 存储organizeId
            }
            
            this.setData({
              alumniAssociationList: [basicAlumniData]
            })
            console.log('[Debug] 直接使用alumniAssociationId创建校友会列表:', [basicAlumniData])
            
            // 自动选择第一个校友会并加载角色列表
            this.setData({
              selectedAlumniAssociationId: alumniAssociationId,
              selectedAlumniAssociationName: basicAlumniData.alumniAssociationName,
              selectedOrganizeId: alumniAssociationId
            })
            
            // 立即加载角色列表
            await this.loadRoleList(alumniAssociationId)
            
            // 尝试调用接口获取更详细的信息（可选）
            try {
              // 使用正确的API方法
              const res = await this.getAlumniAssociationDetail(alumniAssociationId)
              console.log('[Debug] 使用getAlumniAssociationDetail方法调用成功')
              
              if (res.data && res.data.code === 200 && res.data.data) {
                console.log('[Debug] 接口调用成功，获取到的校友会信息:', res.data.data)
                
                const alumniData = {
                  ...res.data.data,
                  id: res.data.data.alumniAssociationId || res.data.data.id || alumniAssociationId,
                  alumniAssociationId: res.data.data.alumniAssociationId || alumniAssociationId,
                  alumniAssociationName: res.data.data.associationName || res.data.data.name || `${associationName} (ID: ${alumniAssociationId})`,
                  organizeId: res.data.data.organizeId || alumniAssociationId // 确保有organizeId
                }
                
                this.setData({
                  alumniAssociationList: [alumniData],
                  selectedAlumniAssociationName: alumniData.alumniAssociationName
                })
                console.log('[Debug] 已更新校友会列表:', [alumniData])
              }
            } catch (apiError) {
              console.log('[Debug] 接口调用失败，继续使用基本数据:', apiError)
              // 继续使用之前创建的基本数据
            }
          } catch (error) {
            console.error('[Debug] 创建校友会数据失败:', error)
            this.setData({
              alumniAssociationList: []
            })
          }
        } else {
          // 没有找到alumniAssociationId，设置为空数组
          console.warn('[Debug] 校友会管理员角色没有alumniAssociationId或organizeId')
          this.setData({
            alumniAssociationList: []
          })
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
        
        // 调用角色列表接口 - 使用校友会ID作为organizeId
        await this.loadRoleList(alumniAssociationId)
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
        
        // 即使校友会详情接口失败，也尝试调用角色列表接口 - 使用校友会ID作为organizeId
        await this.loadRoleList(alumniAssociationId)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /AlumniAssociation/{id} 接口失败:', apiError)
      
      // 即使出错，也尝试调用角色列表接口 - 使用校友会ID作为organizeId
      await this.loadRoleList(alumniAssociationId)
    }
  },

  // 加载角色列表
  async loadRoleList(alumniAssociationId) {
    try {
      console.log('[Debug] 开始加载角色列表，alumniAssociationId:', alumniAssociationId, '类型:', typeof alumniAssociationId)
      
      // 保持alumniAssociationId为字符串形式，避免数字精度丢失
      const organizeId = alumniAssociationId
      console.log('[Debug] 保持字符串形式的organizeId:', organizeId, '类型:', typeof organizeId)
      
      this.setData({ roleLoading: true })
      
      // 调用 /alumniAssociationManagement/role/list 接口 - 使用校友会ID
      const res = await this.getRoleList(organizeId)
      
      console.log('[Debug] 角色列表接口调用结果:', res)
      
      if (res.data && res.data.code === 200) {
        console.log('[Debug] 角色列表接口调用成功，获取到的角色列表:', res.data.data)
        
        this.setData({
          roleList: res.data.data || [],
          roleLoading: false
        })
      } else {
        console.error('[Debug] 角色列表接口调用失败，返回数据:', res)
        this.setData({
          roleList: [],
          roleLoading: false
        })
      }
    } catch (error) {
      console.error('[Debug] 加载角色列表失败:', error)
      this.setData({
        roleList: [],
        roleLoading: false
      })
    }
  },

  // 调用角色列表接口
  getRoleList(organizeId) {
    // 保持organizeId为字符串形式，避免数字精度丢失
    console.log('[Debug] 调用角色列表接口，organizeId:', organizeId, '类型:', typeof organizeId)
    
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/role/list`,
        method: 'POST',
        data: { organizeId: organizeId }, // 保持字符串形式，避免数字精度丢失
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

  // 删除角色
  deleteRole(e) {
    const roleOrId = e.currentTarget.dataset.roleOrId
    console.log('[Debug] 准备删除角色，roleOrId:', roleOrId, '类型:', typeof roleOrId)
    
    // 获取当前选中的校友会ID
    const organizeId = this.data.selectedAlumniAssociationId
    console.log('[Debug] 删除角色时的organizeId:', organizeId, '类型:', typeof organizeId)
    
    // 显示确认对话框
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个角色吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用删除接口
            const deleteRes = await this.callDeleteRoleApi(roleOrId, organizeId)
            
            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              
              // 重新加载角色列表
              await this.loadRoleList(organizeId)
            } else {
              wx.showToast({
                title: '删除失败',
                icon: 'error'
              })
            }
          } catch (error) {
            console.error('[Debug] 删除角色失败:', error)
            wx.showToast({
              title: '删除失败',
              icon: 'error'
            })
          }
        }
      }
    })
  },

  // 调用删除角色接口
  callDeleteRoleApi(roleOrId, organizeId) {
    // 确保 roleOrId 是数字类型（int64）
    const numericRoleOrId = typeof roleOrId === 'string' ? parseInt(roleOrId, 10) : roleOrId
    // 确保 organizeId 是数字类型（int64）
    const numericOrganizeId = typeof organizeId === 'string' ? parseInt(organizeId, 10) : organizeId
    console.log('[Debug] 调用删除角色接口，roleOrId:', numericRoleOrId, '类型:', typeof numericRoleOrId)
    console.log('[Debug] organizeId:', numericOrganizeId, '类型:', typeof numericOrganizeId)
    
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/role/delete`,
        method: 'DELETE',
        data: {
          roleOrId: numericRoleOrId, // 确保是数字类型
          organizeId: numericOrganizeId     // 确保是数字类型（int64）
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})