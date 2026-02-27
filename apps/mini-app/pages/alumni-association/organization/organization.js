// pages/alumni-association/organization/organization.js
const app = getApp()
const { associationApi, alumniAssociationManagementApi } = require('../../../api/api.js')

Page({
  data: {
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    roleList: [], // 存储角色列表
    roleLoading: false, // 角色加载状态
    selectedOrganizeId: 0, // 存储选中的organizeId
    expandedRoles: {}, // 存储展开状态的角色ID
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份
    // 编辑弹窗相关
    showEditModal: false,
    editingRole: null, // 当前正在编辑的角色
    editForm: {
      roleOrName: '',
      remark: '',
      roleOrCode: '',
      status: 1,
      pid: '0' // 父角色ID，0表示顶级
    },
    editParentOptions: [], // 编辑时的父级选项列表
    editSelectedParentIndex: 0, // 编辑时选中的父级索引
    editSelectedParentName: '', // 编辑时选中的父级名称
    // 移动层级相关
    showMoveModal: false,
    movingRole: null, // 当前正在移动的角色
    availableParents: [], // 可选的父级列表
    // 新增角色相关
    showAddModal: false,
    addForm: {
      roleOrName: '',
      roleOrCode: '',
      remark: '',
      pid: '0' // 默认顶级
    },
    parentOptions: [], // 父级选项列表
    selectedParentName: '' // 选中的父级名称
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
        selectedOrganizeId: singleAlumni.alumniAssociationId,
        hasSingleAlumniAssociation: true
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      // 加载角色列表
      await this.loadRoleList(singleAlumni.alumniAssociationId)
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

        // 初始化展开状态：默认全部展开
        const expandedRoles = this.initExpandedState(res.data.data || [])

        this.setData({
          roleList: res.data.data || [],
          roleLoading: false,
          expandedRoles: expandedRoles
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
    return alumniAssociationManagementApi.getRoleList(organizeId)
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 初始化展开状态（默认全部展开）
  initExpandedState(roleList) {
    const expandedRoles = {}
    const traverse = (list) => {
      list.forEach(item => {
        if (item.children && item.children.length > 0) {
          expandedRoles[item.roleOrId] = true // 默认展开
          traverse(item.children)
        }
      })
    }
    traverse(roleList)
    return expandedRoles
  },

  // 切换展开/收缩状态
  toggleExpand(e) {
    const roleOrId = e.currentTarget.dataset.roleOrId
    const expandedRoles = { ...this.data.expandedRoles }
    expandedRoles[roleOrId] = !expandedRoles[roleOrId]
    this.setData({ expandedRoles })
  },

  // 打开编辑弹窗
  openEditModal(e) {
    const { role } = e.currentTarget.dataset
    console.log('[Debug] 打开编辑弹窗，角色数据:', role)

    // 获取可选的父级列表（排除自己及其子节点）
    const editParentOptions = this.getAvailableParentsForEdit(role)
    
    // 设置当前父级ID（默认为0表示顶级）
    const currentPid = role.pid === null || role.pid === undefined ? '0' : String(role.pid)
    
    // 查找当前父级在选项列表中的索引
    const editSelectedParentIndex = editParentOptions.findIndex(item => item.roleOrId === currentPid)
    
    // 获取当前父级名称
    const editSelectedParentName = editParentOptions[editSelectedParentIndex]?.roleOrName || '顶级角色（无父级）'

    this.setData({
      showEditModal: true,
      editingRole: role,
      editForm: {
        roleOrName: role.roleOrName || '',
        remark: role.remark || '',
        roleOrCode: role.roleOrCode || '',
        status: role.status !== undefined ? role.status : 1,
        pid: currentPid
      },
      editParentOptions: editParentOptions,
      editSelectedParentIndex: editSelectedParentIndex >= 0 ? editSelectedParentIndex : 0,
      editSelectedParentName: editSelectedParentName
    })
  },

  // 关闭编辑弹窗
  closeEditModal() {
    this.setData({
      showEditModal: false,
      editingRole: null,
      editForm: {
        roleOrName: '',
        remark: '',
        roleOrCode: '',
        status: 1,
        pid: '0'
      },
      editParentOptions: [],
      editSelectedParentIndex: 0,
      editSelectedParentName: ''
    })
  },

  // 编辑表单输入处理
  onEditInputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`editForm.${field}`]: value
    })
  },

  // 状态切换
  onStatusChange(e) {
    const status = e.detail.value ? 1 : 0
    this.setData({
      'editForm.status': status
    })
  },

  // 编辑时选择父级
  onEditParentChange(e) {
    const index = e.detail.value
    const editParentOptions = this.data.editParentOptions
    const selectedParent = editParentOptions[index]
    this.setData({
      'editForm.pid': selectedParent.roleOrId,
      editSelectedParentIndex: index,
      editSelectedParentName: selectedParent.roleOrName
    })
  },

  // 提交编辑
  async submitEdit() {
    const { editingRole, editForm, selectedAlumniAssociationId } = this.data

    // 校验必填项
    if (!editForm.roleOrName.trim()) {
      wx.showToast({
        title: '请输入角色名',
        icon: 'none'
      })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })

      const res = await this.callUpdateRoleApi({
        organizeId: selectedAlumniAssociationId,
        roleOrId: editingRole.roleOrId,
        pid: editForm.pid === '0' ? null : editForm.pid, // 0表示顶级，传null
        roleOrName: editForm.roleOrName.trim(),
        remark: editForm.remark.trim(),
        roleOrCode: editForm.roleOrCode.trim(),
        status: editForm.status
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        })
        this.closeEditModal()
        // 重新加载角色列表
        await this.loadRoleList(selectedAlumniAssociationId)
      } else {
        const errorMsg = (res.data && res.data.msg) || '保存失败'
        wx.showModal({
          title: '保存失败',
          content: errorMsg,
          showCancel: false,
          confirmText: '知道了'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[Debug] 更新角色失败:', error)
      wx.showModal({
        title: '保存失败',
        content: '网络请求失败，请稍后重试',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  },

  // 长按角色 - 打开移动层级弹窗
  onRoleLongPress(e) {
    const { role } = e.currentTarget.dataset
    console.log('[Debug] 长按角色，准备移动层级:', role)

    // 获取可选的父级列表（排除自己及其子节点）
    const availableParents = this.getAvailableParents(role)

    this.setData({
      showMoveModal: true,
      movingRole: role,
      availableParents: availableParents
    })

    // 震动反馈
    wx.vibrateShort({ type: 'medium' })
  },

  // 获取可选的父级列表（排除自己及其所有子孙节点）
  getAvailableParents(movingRole) {
    const { roleList } = this.data
    const excludeIds = this.getDescendantIds(movingRole)
    excludeIds.push(String(movingRole.roleOrId))

    const result = []

    // 添加"设为顶级"选项
    result.push({
      roleOrId: '0',
      roleOrName: '设为顶级角色',
      level: 0,
      isRoot: true
    })

    // 遍历树形结构，收集可选的父级
    const traverse = (list, level) => {
      list.forEach(item => {
        if (!excludeIds.includes(String(item.roleOrId))) {
          result.push({
            roleOrId: item.roleOrId,
            roleOrName: item.roleOrName,
            level: level
          })
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children, level + 1)
        }
      })
    }

    traverse(roleList, 1)
    return result
  },

  // 获取编辑时可选的父级角色列表
  getAvailableParentsForEdit(editingRole) {
    const { roleList } = this.data
    const excludeIds = this.getDescendantIds(editingRole)
    excludeIds.push(String(editingRole.roleOrId))

    const result = []

    // 添加"顶级角色（无父级）"选项
    result.push({
      roleOrId: '0',
      roleOrName: '顶级角色（无父级）',
      level: 0,
      isRoot: true
    })

    // 遍历树形结构，收集可选的父级
    const traverse = (list, level) => {
      list.forEach(item => {
        if (!excludeIds.includes(String(item.roleOrId))) {
          result.push({
            roleOrId: item.roleOrId,
            roleOrName: item.roleOrName,
            level: level
          })
        }
        if (item.children && item.children.length > 0) {
          traverse(item.children, level + 1)
        }
      })
    }

    traverse(roleList, 1)
    return result
  },

  // 获取某个角色的所有子孙节点ID
  getDescendantIds(role) {
    const ids = []
    const traverse = (children) => {
      if (!children) {return}
      children.forEach(child => {
        ids.push(String(child.roleOrId))
        if (child.children && child.children.length > 0) {
          traverse(child.children)
        }
      })
    }
    traverse(role.children)
    return ids
  },

  // 关闭移动层级弹窗
  closeMoveModal() {
    this.setData({
      showMoveModal: false,
      movingRole: null,
      availableParents: []
    })
  },

  // 选择新的父级
  async selectNewParent(e) {
    const { parentId } = e.currentTarget.dataset
    const { movingRole, selectedAlumniAssociationId } = this.data

    console.log('[Debug] 选择新父级:', parentId, '移动角色:', movingRole)

    // 如果选择的是当前父级，不做处理
    const currentPid = movingRole.pid === null ? '0' : String(movingRole.pid)
    if (String(parentId) === currentPid) {
      wx.showToast({
        title: '已是当前层级',
        icon: 'none'
      })
      return
    }

    try {
      wx.showLoading({ title: '移动中...' })

      // 调用更新接口，只更新 pid
      const res = await this.callUpdateRoleApi({
        organizeId: selectedAlumniAssociationId,
        roleOrId: movingRole.roleOrId,
        pid: parentId === '0' ? null : parentId, // 0 表示顶级，传 null
        roleOrName: movingRole.roleOrName,
        remark: movingRole.remark || '',
        roleOrCode: movingRole.roleOrCode || '',
        status: movingRole.status !== undefined ? movingRole.status : 1
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '移动成功',
          icon: 'success'
        })
        this.closeMoveModal()
        // 重新加载角色列表
        await this.loadRoleList(selectedAlumniAssociationId)
      } else {
        const errorMsg = (res.data && res.data.msg) || '移动失败'
        wx.showModal({
          title: '移动失败',
          content: errorMsg,
          showCancel: false,
          confirmText: '知道了'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[Debug] 移动角色失败:', error)
      wx.showModal({
        title: '移动失败',
        content: '网络请求失败，请稍后重试',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  },

  // 打开新增角色弹窗
  openAddModal() {
    if (!this.data.selectedAlumniAssociationId) {
      wx.showToast({
        title: '请先选择校友会',
        icon: 'none'
      })
      return
    }

    // 构建父级选项列表
    const parentOptions = this.buildParentOptions()

    this.setData({
      showAddModal: true,
      addForm: {
        roleOrName: '',
        roleOrCode: '',
        remark: '',
        pid: '0'
      },
      selectedParentName: '顶级角色（无父级）',
      parentOptions: parentOptions
    })
  },

  // 构建父级选项列表
  buildParentOptions() {
    const { roleList } = this.data
    const result = []

    // 添加"顶级角色"选项
    result.push({
      roleOrId: '0',
      roleOrName: '顶级角色（无父级）',
      level: 0
    })

    // 遍历树形结构
    const traverse = (list, level) => {
      list.forEach(item => {
        result.push({
          roleOrId: String(item.roleOrId),
          roleOrName: item.roleOrName,
          level: level
        })
        if (item.children && item.children.length > 0) {
          traverse(item.children, level + 1)
        }
      })
    }

    traverse(roleList, 1)
    return result
  },

  // 关闭新增角色弹窗
  closeAddModal() {
    this.setData({
      showAddModal: false,
      addForm: {
        roleOrName: '',
        roleOrCode: '',
        remark: '',
        pid: '0'
      },
      parentOptions: []
    })
  },

  // 新增表单输入处理
  onAddInputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`addForm.${field}`]: value
    })
  },

  // 选择父级
  onParentChange(e) {
    const index = e.detail.value
    const parentOptions = this.data.parentOptions
    const selectedParent = parentOptions[index]
    this.setData({
      'addForm.pid': selectedParent.roleOrId,
      selectedParentName: selectedParent.roleOrName
    })
  },

  // 获取当前选中的父级名称
  getSelectedParentName() {
    const { addForm, parentOptions } = this.data
    const selected = parentOptions.find(item => item.roleOrId === addForm.pid)
    return selected ? selected.roleOrName : '请选择父级'
  },

  // 提交新增角色
  async submitAdd() {
    const { addForm, selectedAlumniAssociationId } = this.data

    // 校验必填项
    if (!addForm.roleOrName.trim()) {
      wx.showToast({
        title: '请输入角色名',
        icon: 'none'
      })
      return
    }
    
    if (!addForm.roleOrCode.trim()) {
      wx.showToast({
        title: '请输入角色唯一代码',
        icon: 'none'
      })
      return
    }

    try {
      wx.showLoading({ title: '添加中...' })

      const res = await this.callAddRoleApi({
        organizeId: selectedAlumniAssociationId,
        organizeType: 0, // 默认类型
        pid: addForm.pid === '0' ? '0' : addForm.pid,
        roleOrName: addForm.roleOrName.trim(),
        roleOrCode: addForm.roleOrCode.trim(),
        remark: addForm.remark.trim()
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        })
        this.closeAddModal()
        // 重新加载角色列表
        await this.loadRoleList(selectedAlumniAssociationId)
      } else {
        const errorMsg = (res.data && res.data.msg) || '添加失败'
        wx.showModal({
          title: '添加失败',
          content: errorMsg,
          showCancel: false,
          confirmText: '知道了'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[Debug] 添加角色失败:', error)
      wx.showModal({
        title: '添加失败',
        content: '网络请求失败，请稍后重试',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  },

  // 调用新增角色接口
  callAddRoleApi(data) {
    // 保持字符串格式，避免数字精度丢失
    const requestData = {
      organizeId: String(data.organizeId),
      organizeType: data.organizeType,
      pid: String(data.pid),
      roleOrName: data.roleOrName,
      roleOrCode: data.roleOrCode,
      remark: data.remark
    }

    console.log('[Debug] 调用新增角色接口，请求数据:', requestData)
    return alumniAssociationManagementApi.addRole(requestData)
  },

  // 调用更新角色接口
  callUpdateRoleApi(data) {
    // 保持字符串格式，避免数字精度丢失
    const requestData = {
      organizeId: String(data.organizeId),
      roleOrId: String(data.roleOrId),
      roleOrName: data.roleOrName,
      remark: data.remark,
      roleOrCode: data.roleOrCode,
      status: data.status
    }
    // pid 可能为 null 或 0，需要特殊处理
    if (data.pid !== null && data.pid !== undefined) {
      requestData.pid = String(data.pid)
    }

    console.log('[Debug] 调用更新角色接口，请求数据:', requestData)
    return alumniAssociationManagementApi.updateRole(requestData)
  },

  // 调用校友会详情接口
  getAlumniAssociationDetail(alumniAssociationId) {
    return associationApi.getAssociationDetail(alumniAssociationId)
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
              // 显示后端返回的错误信息
              const errorMsg = (deleteRes.data && deleteRes.data.msg) || '删除失败'
              wx.showModal({
                title: '删除失败',
                content: errorMsg,
                showCancel: false,
                confirmText: '知道了'
              })
            }
          } catch (error) {
            console.error('[Debug] 删除角色失败:', error)
            wx.showModal({
              title: '删除失败',
              content: '网络请求失败，请稍后重试',
              showCancel: false,
              confirmText: '知道了'
            })
          }
        }
      }
    })
  },

  // 调用删除角色接口
  callDeleteRoleApi(roleOrId, organizeId) {
    // 保持字符串格式，避免数字精度丢失
    const strRoleOrId = String(roleOrId)
    const strOrganizeId = String(organizeId)
    console.log('[Debug] 调用删除角色接口，roleOrId:', strRoleOrId, '类型:', typeof strRoleOrId)
    console.log('[Debug] organizeId:', strOrganizeId, '类型:', typeof strOrganizeId)
    return alumniAssociationManagementApi.deleteRole(strRoleOrId, strOrganizeId)
  }
})