// pages/alumni-association/member/member.js
const { alumniApi } = require('../../../api/api.js')
const app = getApp()
const config = require('../../../utils/config.js')

// 防抖函数
function debounce(fn, delay) {
    let timer = null
    return function () {
        const context = this
        const args = arguments
        clearTimeout(timer)
        timer = setTimeout(function () {
            fn.apply(context, args)
        }, delay)
    }
}

Page({
  data: {
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    selectedOrganizeId: 0, // 存储选中的organizeId
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份
    // 成员列表相关
    memberList: [],
    memberLoading: false, // 成员加载状态
    // 邀请成员相关
    showInviteModal: false,
    inviteForm: {
      name: '',
      wxId: '',
      roleOrId: ''
    },
    // 校友搜索结果
    alumniSearchResults: [],
    showAlumniSearchResults: false,
    // 编辑成员相关
    showEditModal: false,
    editingMember: {},
    roleList: [],
    defaultAvatar: config.defaultAvatar
  },

  onLoad(options) {
    // 创建搜索防抖函数
    this.searchAlumniDebounced = debounce(this.searchAlumni, 500)
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
            const associationName = alumniAdminRole.associationName || (alumniAdminRole.organization && alumniAdminRole.organization.associationName) || '校友会'
            
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
      // 加载成员列表
      await this.loadMemberList(singleAlumni.alumniAssociationId)
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
      
      // 加载该校友会的成员列表
      await this.loadMemberList(alumniAssociationId)
    } catch (apiError) {
      console.error('[Debug] 调用 /AlumniAssociation/{id} 接口失败:', apiError)
    }
  },
  
  // 加载成员列表
  async loadMemberList(alumniAssociationId) {
    try {
      this.setData({ memberLoading: true })
      console.log('[Debug] 开始加载成员列表，alumniAssociationId:', alumniAssociationId)
      
      // 调用成员列表接口
      const res = await this.queryMemberList(alumniAssociationId)
      
      if (res.data && res.data.code === 200) {
        this.setData({
          memberList: (res.data.data && res.data.data.records) || [],
          memberLoading: false
        })
        console.log('[Debug] 成员列表加载完成:', (res.data.data && res.data.data.records) || [])
      } else {
        this.setData({
          memberList: [],
          memberLoading: false
        })
        console.error('[Debug] 成员列表接口调用失败，返回数据:', res)
      }
    } catch (error) {
      console.error('[Debug] 加载成员列表失败:', error)
      this.setData({
        memberList: [],
        memberLoading: false
      })
    }
  },
  
  // 调用查询成员列表接口
  queryMemberList(alumniAssociationId) {
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/queryMemberList`,
        method: 'POST',
        data: { alumniAssociationId: alumniAssociationId },
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

  // 显示邀请成员弹窗
  async showInviteModal() {
    this.setData({
      showInviteModal: true,
      inviteForm: {
        name: '',
        wxId: '',
        roleOrId: '',
        roleOrName: '',
        roleIndex: 0
      },
      alumniSearchResults: [],
      showAlumniSearchResults: false,
      roleList: []
    })
    
    // 获取角色列表
    await this.loadRoleList()
  },
  
  // 邀请成员时角色选择变化
  onRoleChange(e) {
    const index = e.detail.value
    const selectedRole = this.data.roleList[index]
    if (selectedRole) {
      this.setData({
        'inviteForm.roleOrId': selectedRole.roleOrId,
        'inviteForm.roleOrName': selectedRole.roleOrName,
        'inviteForm.roleIndex': index
      })
    }
  },

  // 隐藏邀请成员弹窗
  hideInviteModal() {
    this.setData({
      showInviteModal: false,
      alumniSearchResults: [],
      showAlumniSearchResults: false
    })
  },

  // 阻止冒泡
  preventBubble() {
    // 阻止冒泡，防止点击搜索结果时关闭弹窗
  },

  // 处理校友姓名输入
  onMemberNameInput(e) {
    const value = e.detail.value
    this.setData({
      'inviteForm.name': value,
      showAlumniSearchResults: true
    })

    if (value.trim()) {
      this.searchAlumniDebounced(value)
    } else {
      this.setData({ alumniSearchResults: [] })
    }
  },

  // 处理校友姓名输入框聚焦
  onMemberNameFocus() {
    if (this.data.inviteForm.name) {
      this.setData({ showAlumniSearchResults: true })
      if (this.data.alumniSearchResults.length === 0) {
        this.searchAlumni(this.data.inviteForm.name)
      }
    }
  },

  // 搜索校友
  async searchAlumni(keyword) {
    if (!keyword) {return}
    try {
      const res = await alumniApi.queryAlumniList({
        current: 1,
        pageSize: 10,
        name: keyword.trim()
      })
      if (res.data && res.data.code === 200) {
        this.setData({
          alumniSearchResults: res.data.data.records || []
        })
      }
    } catch (e) {
      console.error('搜索校友失败', e)
    }
  },

  // 选择校友
  selectAlumni(e) {
    const { index } = e.currentTarget.dataset
    const selectedAlumni = this.data.alumniSearchResults[index]
    
    if (selectedAlumni) {
      // 从selectedAlumni中获取wxId，尝试所有可能的字段名，保留字符串形式
      const wxId = selectedAlumni.wxId || selectedAlumni.id || selectedAlumni.userId || selectedAlumni.user_id || selectedAlumni.wx_id || '0'
      
      this.setData({
        'inviteForm.name': selectedAlumni.name || selectedAlumni.nickname || selectedAlumni.realName,
        'inviteForm.wxId': wxId,
        alumniSearchResults: [],
        showAlumniSearchResults: false
      })
    }
  },



  // 提交邀请
  async submitInvite() {
    try {
      const { wxId, roleOrId } = this.data.inviteForm
      const alumniAssociationId = this.data.selectedAlumniAssociationId

      // 验证必填参数
      if (!wxId || wxId === 0 || !roleOrId || !alumniAssociationId) {
        wx.showToast({
          title: '请通过搜索选择校友并选择身份',
          icon: 'none'
        })
        return
      }

      // 调用邀请成员接口，直接传递字符串形式的wxId和roleOrId，避免大整数精度丢失
      const res = await this.inviteMemberAPI(alumniAssociationId, wxId, roleOrId)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '邀请成功',
          icon: 'success'
        })
        this.hideInviteModal()
        // 刷新成员列表
        await this.loadMemberList(alumniAssociationId)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '邀请失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('[Debug] 邀请成员失败:', error)
      wx.showToast({
        title: '邀请失败',
        icon: 'none'
      })
    }
  },

  // 加载角色列表
  async loadRoleList() {
    try {
      const alumniAssociationId = this.data.selectedAlumniAssociationId
      if (!alumniAssociationId) {
        console.error('[Debug] 加载角色列表失败：缺少alumniAssociationId')
        return
      }
      
      const res = await this.getRoleList(alumniAssociationId)
      if (res.data && res.data.code === 200) {
        // 将树形结构的角色数据扁平化为一维数组
        const flattenedRoles = this.flattenRoleTree(res.data.data || [])
        this.setData({
          roleList: flattenedRoles
        })
      }
    } catch (error) {
      console.error('[Debug] 加载角色列表失败:', error)
    }
  },

  // 将树形结构的角色数据扁平化为一维数组
  flattenRoleTree(roleTree) {
    const result = []
    
    function traverse(node) {
      // 添加当前节点，不保留层级信息
      result.push(node)
      
      // 递归处理子节点
      if (node.children && node.children.length > 0) {
        node.children.forEach(child => traverse(child))
      }
    }
    
    // 遍历所有根节点
    roleTree.forEach(root => traverse(root))
    
    return result
  },

  // 调用获取角色列表接口
  getRoleList(organizeId) {
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
        data: { organizeId: organizeId },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 调用邀请成员接口
  inviteMemberAPI(alumniAssociationId, wxId, roleOrId) {
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/inviteMember`,
        method: 'POST',
        data: {
          alumniAssociationId,
          wxId,
          roleOrId
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 打开编辑成员弹窗
  async openEditModal(e) {
    const member = e.currentTarget.dataset.member
    this.setData({
      editingMember: {
        ...member,
        newRoleId: (member.organizeArchiRole && member.organizeArchiRole.roleOrId) || '',
        newRoleName: (member.organizeArchiRole && member.organizeArchiRole.roleOrName) || '',
        roleIndex: 0
      },
      showEditModal: true
    })
    
    // 加载角色列表
    await this.loadRoleList()
    
    // 设置默认角色索引
    const roleId = (member.organizeArchiRole && member.organizeArchiRole.roleOrId) || ''
    const roleIndex = this.data.roleList.findIndex(role => role.roleOrId === roleId)
    if (roleIndex !== -1) {
      this.setData({
        'editingMember.roleIndex': roleIndex
      })
    }
  },
  
  // 编辑成员时角色选择变化
  onEditRoleChange(e) {
    const index = e.detail.value
    const selectedRole = this.data.roleList[index]
    if (selectedRole) {
      this.setData({
        'editingMember.newRoleId': selectedRole.roleOrId,
        'editingMember.newRoleName': selectedRole.roleOrName,
        'editingMember.roleIndex': index
      })
    }
  },

  // 关闭编辑成员弹窗
  hideEditModal() {
    this.setData({
      showEditModal: false
    })
  },

  // 提交编辑成员角色
  async submitEdit() {
    try {
      const { editingMember } = this.data
      const { newRoleId, wxId } = editingMember
      const alumniAssociationId = this.data.selectedAlumniAssociationId

      // 验证必填参数
      if (!newRoleId || !wxId || !alumniAssociationId) {
        wx.showToast({
          title: '请选择新角色',
          icon: 'none'
        })
        return
      }

      // 调用更新成员角色接口
      const res = await this.updateMemberRoleAPI(alumniAssociationId, wxId, newRoleId)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '修改成功',
          icon: 'success'
        })
        this.hideEditModal()
        // 刷新成员列表
        await this.loadMemberList(alumniAssociationId)
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '修改失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('[Debug] 修改成员角色失败:', error)
      wx.showToast({
        title: '修改失败',
        icon: 'none'
      })
    }
  },

  // 删除成员
  async deleteMember(e) {
    const member = e.currentTarget.dataset.member
    const { wxId } = member
    const alumniAssociationId = this.data.selectedAlumniAssociationId

    // 确认删除
    wx.showModal({
      title: '确认删除',
      content: `确定要删除成员 "${member.name || member.nickname || '未命名'}" 吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用删除成员接口
            const result = await this.deleteMemberAPI(alumniAssociationId, wxId)
            
            if (result.data && result.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              // 刷新成员列表
              await this.loadMemberList(alumniAssociationId)
            } else {
              wx.showToast({
                title: (result.data && result.data.msg) || '删除失败',
                icon: 'none'
              })
            }
          } catch (error) {
            console.error('[Debug] 删除成员失败:', error)
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 调用更新成员角色接口
  updateMemberRoleAPI(alumniAssociationId, wxId, roleOrId) {
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/updateMemberRole`,
        method: 'PUT',
        data: {
          alumniAssociationId,
          wxId,
          roleOrId
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 调用删除成员接口
  deleteMemberAPI(alumniAssociationId, wxId) {
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
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/deleteMember`,
        method: 'DELETE',
        data: {
          alumniAssociationId,
          wxId
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})