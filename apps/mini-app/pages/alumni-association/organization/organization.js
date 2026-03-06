// pages/alumni-association/organization/organization.js
const app = getApp()
const { associationApi, alumniAssociationManagementApi, userApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

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
    defaultUserAvatarUrl: config.defaultAvatar, // 默认头像
    // 编辑弹窗相关
    showEditModal: false,
    editingRole: null, // 当前正在编辑的角色
    editForm: {
      roleOrName: '',
      remark: '',
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
      remark: '',
      pid: '0' // 默认顶级
    },
    parentOptions: [], // 父级选项列表
    selectedParentName: '', // 选中的父级名称
    // 添加成员弹窗相关
    showAddMemberModal: false,
    memberSearchKeyword: '',
    memberSearchResults: [],
    memberSearchLoading: false,
    removedMembers: [], // 临时存储被移除的成员（用于提交时）
    addedMembers: [], // 临时存储新添加的成员（用于提交时）
    // 树形节点详情弹窗相关
    showNodeDetailModal: false,
    currentDetailRole: null, // 当前查看详情的节点
    addModalTitle: '', // 新增弹窗标题
    addParentRole: null // 新增子架构时的父节点
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadAlumniAssociationList()
  },

  // 加载校友会列表（调用接口获取用户管理的校友会组织）
  async loadAlumniAssociationList() {
    try {
      console.log('[Debug] 开始加载校友会列表')

      // 调用接口获取用户管理的组织列表，type=0 表示校友会
      const res = await userApi.getManagedOrganizations({ type: 0 })
      console.log('[Debug] 获取用户管理的校友会列表:', res)

      if (res.data && res.data.code === 200) {
        const organizationList = res.data.data || []
        console.log('[Debug] 接口返回的组织列表:', organizationList)

        // 设置是否有校友会管理员身份
        this.setData({
          hasAlumniAdminPermission: organizationList.length > 0
        })

        if (organizationList.length > 0) {
          // 将接口返回的数据映射为页面需要的格式
          const alumniAssociationList = organizationList.map(org => {
            // 处理logo头像
            let logo = org.logo || ''
            if (logo && !logo.startsWith('http://') && !logo.startsWith('https://')) {
              logo = config.getImageUrl(logo)
            }

            return {
              id: org.id,
              alumniAssociationId: org.id,
              alumniAssociationName: org.name || '校友会',
              organizeId: org.id,
              logo: logo,
              location: org.location || '',
              type: org.type
            }
          })

          // 设置校友会列表
          this.setData({
            alumniAssociationList: alumniAssociationList
          })
          console.log('[Debug] 最终校友会列表:', alumniAssociationList)

          // 判断权限数量，处理自动选择逻辑
          this.handleAlumniAssociationSelection(alumniAssociationList)
        } else {
          // 没有找到管理的校友会
          console.warn('[Debug] 用户没有管理的校友会')
          this.setData({
            alumniAssociationList: [],
            hasAlumniAdminPermission: false
          })
        }
      } else {
        console.error('[Debug] 获取校友会列表接口调用失败:', res)
        this.setData({
          alumniAssociationList: [],
          hasAlumniAdminPermission: false
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校友会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        alumniAssociationList: [],
        hasAlumniAdminPermission: false
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

  // 加载组织架构树（包含成员）
  async loadRoleList(alumniAssociationId) {
    try {
      console.log('[Debug] 开始加载组织架构树，alumniAssociationId:', alumniAssociationId, '类型:', typeof alumniAssociationId)

      this.setData({ roleLoading: true })

      // 调用 /AlumniAssociation/organizationTree/v2 接口 - 获取包含成员的组织架构树
      const { post } = require('../../../utils/request.js')
      const res = await post('/AlumniAssociation/organizationTree/v2', {
        alumniAssociationId: alumniAssociationId
      })

      console.log('[Debug] 组织架构树接口调用结果:', res)

      if (res.data && res.data.code === 200) {
        const treeData = res.data.data || []
        console.log('[Debug] 组织架构树接口调用成功，获取到的数据:', treeData)

        // 打印每个节点的成员数量，用于调试
        const logMembers = (list, level = 1) => {
          list.forEach(item => {
            console.log(`[Debug] 第${level}级角色 "${item.roleOrName}": 成员数量 = ${(item.members || []).length}`, item.members)
            if (item.children && item.children.length > 0) {
              logMembers(item.children, level + 1)
            }
          })
        }
        logMembers(treeData)

        // 初始化展开状态：默认全部展开
        const expandedRoles = this.initExpandedState(treeData)

        this.setData({
          roleList: treeData,
          roleLoading: false,
          expandedRoles: expandedRoles
        })
      } else {
        console.error('[Debug] 组织架构树接口调用失败，返回数据:', res)
        this.setData({
          roleList: [],
          roleLoading: false
        })
      }
    } catch (error) {
      console.error('[Debug] 加载组织架构树失败:', error)
      this.setData({
        roleList: [],
        roleLoading: false
      })
    }
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
        // 如果有子角色或有成员，则默认展开
        if ((item.children && item.children.length > 0) || (item.members && item.members.length > 0)) {
          expandedRoles[item.roleOrId] = true // 默认展开
        }
        if (item.children && item.children.length > 0) {
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
    const editSelectedParentName = editParentOptions[editSelectedParentIndex]?.roleOrName || '顶级架构（无父级）'

    // 深拷贝角色数据，包括成员列表，以便编辑时不影响原数据
    const editingRole = {
      ...role,
      members: role.members ? [...role.members] : []
    }

    this.setData({
      showEditModal: true,
      editingRole: editingRole,
      editForm: {
        roleOrName: role.roleOrName || '',
        remark: role.remark || '',
        status: role.status !== undefined ? role.status : 1,
        pid: currentPid
      },
      editParentOptions: editParentOptions,
      editSelectedParentIndex: editSelectedParentIndex >= 0 ? editSelectedParentIndex : 0,
      editSelectedParentName: editSelectedParentName,
      // 重置成员变更记录
      addedMembers: [],
      removedMembers: []
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
        status: 1,
        pid: '0'
      },
      editParentOptions: [],
      editSelectedParentIndex: 0,
      editSelectedParentName: '',
      // 重置成员变更记录
      addedMembers: [],
      removedMembers: []
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
        title: '请输入架构名称',
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
        status: editForm.status
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        })
        this.closeEditModal()
        // 如果详情弹窗也打开着，关闭它
        if (this.data.showNodeDetailModal) {
          this.closeNodeDetailModal()
        }
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
      roleOrName: '设为顶级架构',
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

    // 添加"顶级架构（无父级）"选项
    result.push({
      roleOrId: '0',
      roleOrName: '顶级架构（无父级）',
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
        remark: '',
        pid: '0'
      },
      selectedParentName: '顶级架构（无父级）',
      parentOptions: parentOptions
    })
  },

  // 构建父级选项列表
  buildParentOptions() {
    const { roleList } = this.data
    const result = []

    // 添加"顶级架构"选项
    result.push({
      roleOrId: '0',
      roleOrName: '顶级架构（无父级）',
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
        remark: '',
        pid: '0'
      },
      parentOptions: [],
      addModalTitle: '',
      addParentRole: null
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
        title: '请输入架构名称',
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
        remark: addForm.remark.trim()
      })

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        })
        this.closeAddModal()
        // 如果是从详情弹窗添加子架构，关闭详情弹窗
        if (this.data.showNodeDetailModal) {
          this.closeNodeDetailModal()
        }
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
  },

  // ========== 成员管理相关方法 ==========

  // 打开添加成员弹窗
  openAddMemberModal() {
    this.setData({
      showAddMemberModal: true,
      memberSearchKeyword: '',
      memberSearchResults: [],
      memberSearchLoading: false
    })
  },

  // 关闭添加成员弹窗
  closeAddMemberModal() {
    this.setData({
      showAddMemberModal: false,
      memberSearchKeyword: '',
      memberSearchResults: [],
      memberSearchLoading: false
    })
  },

  // 成员搜索输入
  onMemberSearchInput(e) {
    this.setData({
      memberSearchKeyword: e.detail.value
    })
  },

  // 搜索成员（从校友会成员列表中搜索）
  async searchMembers() {
    const keyword = this.data.memberSearchKeyword.trim()
    if (!keyword) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    const { selectedAlumniAssociationId } = this.data
    if (!selectedAlumniAssociationId) {
      wx.showToast({ title: '请先选择校友会', icon: 'none' })
      return
    }

    this.setData({ memberSearchLoading: true })

    try {
      // 调用获取校友会成员列表接口，直接传入 keyword 参数
      const res = await alumniAssociationManagementApi.getMemberList(selectedAlumniAssociationId, keyword)

      if (res.data && res.data.code === 200) {
        // API 返回分页格式：data.records 是成员数组
        const responseData = res.data.data || {}
        const searchResults = responseData.records || responseData || []

        // 支持从详情弹窗或编辑弹窗调用
        const targetRole = this.data.currentDetailRole || this.data.editingRole || {}
        // 过滤掉已经在当前分支中的成员
        const existingMemberIds = (targetRole.members || []).map(m => m.wxId)
        const filteredResults = (Array.isArray(searchResults) ? searchResults : [])
          .filter(item => !existingMemberIds.includes(item.wxId))
          .map(item => ({
            ...item,
            isSelected: false
          }))

        this.setData({
          memberSearchResults: filteredResults,
          memberSearchLoading: false
        })
      } else {
        wx.showToast({ title: res.data?.msg || '搜索失败', icon: 'none' })
        this.setData({ memberSearchLoading: false })
      }
    } catch (error) {
      console.error('[Debug] 搜索成员失败:', error)
      wx.showToast({ title: '搜索失败，请重试', icon: 'none' })
      this.setData({ memberSearchLoading: false })
    }
  },

  // 切换成员选中状态
  toggleMemberSelect(e) {
    const { index } = e.currentTarget.dataset
    const results = this.data.memberSearchResults
    results[index].isSelected = !results[index].isSelected
    this.setData({
      memberSearchResults: results
    })
  },

  // 确认添加成员
  async confirmAddMembers() {
    const selectedMembers = this.data.memberSearchResults.filter(m => m.isSelected)
    if (selectedMembers.length === 0) {
      wx.showToast({ title: '请选择要添加的成员', icon: 'none' })
      return
    }

    const { selectedAlumniAssociationId, editingRole, currentDetailRole } = this.data
    // 支持从详情弹窗或编辑弹窗调用
    const targetRole = currentDetailRole || editingRole
    const roleOrId = targetRole.roleOrId

    wx.showLoading({ title: '添加中...', mask: true })

    try {
      let successCount = 0
      let failCount = 0

      // 逐个添加成员到分支
      for (const member of selectedMembers) {
        try {
          const res = await alumniAssociationManagementApi.addMemberToBranch(
            selectedAlumniAssociationId,
            member.wxId,
            roleOrId
          )
          if (res.data && res.data.code === 200) {
            successCount++
          } else {
            failCount++
            console.warn('[Debug] 添加成员失败:', member.wxId, res.data?.msg)
          }
        } catch (err) {
          failCount++
          console.error('[Debug] 添加成员异常:', member.wxId, err)
        }
      }

      wx.hideLoading()

      if (successCount > 0) {
        // 更新本地显示
        const successfullyAdded = selectedMembers.slice(0, successCount)
        const updateData = {
          showAddMemberModal: false,
          memberSearchKeyword: '',
          memberSearchResults: []
        }

        // 根据来源更新不同的数据
        if (this.data.currentDetailRole) {
          const updatedDetailRole = { ...this.data.currentDetailRole }
          const currentMembers = updatedDetailRole.members || []
          updatedDetailRole.members = [...currentMembers, ...successfullyAdded]
          updateData.currentDetailRole = updatedDetailRole
        }
        if (this.data.editingRole) {
          const updatedEditingRole = { ...this.data.editingRole }
          const currentMembers = updatedEditingRole.members || []
          updatedEditingRole.members = [...currentMembers, ...successfullyAdded]
          updateData.editingRole = updatedEditingRole
        }

        this.setData(updateData)

        if (failCount > 0) {
          wx.showToast({ title: `成功添加 ${successCount} 人，${failCount} 人失败`, icon: 'none', duration: 2000 })
        } else {
          wx.showToast({ title: `已添加 ${successCount} 名成员`, icon: 'success' })
        }

        // 刷新组织架构树以获取最新数据
        await this.loadRoleList(selectedAlumniAssociationId)
      } else {
        wx.showToast({ title: '添加失败，请重试', icon: 'none' })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[Debug] 添加成员失败:', error)
      wx.showToast({ title: '添加失败，请重试', icon: 'none' })
    }
  },

  // 从分支移除成员
  removeMemberFromRole(e) {
    const { member } = e.currentTarget.dataset
    const { selectedAlumniAssociationId } = this.data

    wx.showModal({
      title: '确认移除',
      content: `确定要将「${member.username || member.name || '该成员'}」从此分支移除吗？\n（成员仍保留校友会成员身份）`,
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '移除中...', mask: true })

          try {
            const apiRes = await alumniAssociationManagementApi.removeMemberFromBranch(
              selectedAlumniAssociationId,
              member.wxId
            )

            wx.hideLoading()

            if (apiRes.data && apiRes.data.code === 200) {
              // 更新本地显示
              const editingRole = { ...this.data.editingRole }
              const currentMembers = editingRole.members || []
              editingRole.members = currentMembers.filter(m => m.wxId !== member.wxId)

              this.setData({
                editingRole: editingRole
              })

              wx.showToast({ title: '已移除', icon: 'success' })

              // 刷新组织架构树以获取最新数据
              await this.loadRoleList(selectedAlumniAssociationId)
            } else {
              wx.showToast({ title: apiRes.data?.msg || '移除失败', icon: 'none' })
            }
          } catch (error) {
            wx.hideLoading()
            console.error('[Debug] 移除成员失败:', error)
            wx.showToast({ title: '移除失败，请重试', icon: 'none' })
          }
        }
      }
    })
  },

  // ========== 树形节点详情弹窗相关方法 ==========

  // 打开节点详情弹窗
  openNodeDetailModal(e) {
    const { role } = e.currentTarget.dataset
    console.log('[Debug] 打开节点详情弹窗，角色数据:', role)

    this.setData({
      showNodeDetailModal: true,
      currentDetailRole: {
        ...role,
        members: role.members || []
      }
    })
  },

  // 关闭节点详情弹窗
  closeNodeDetailModal() {
    this.setData({
      showNodeDetailModal: false,
      currentDetailRole: null
    })
  },

  // 打开新增顶级架构弹窗
  openAddTopLevelModal() {
    if (!this.data.selectedAlumniAssociationId) {
      wx.showToast({
        title: '请先选择校友会',
        icon: 'none'
      })
      return
    }

    this.setData({
      showAddModal: true,
      addModalTitle: '新增顶级架构',
      addParentRole: null,
      addForm: {
        roleOrName: '',
        remark: '',
        pid: '0'
      }
    })
  },

  // 打开新增子架构弹窗（从详情弹窗）
  openAddChildModal() {
    const { currentDetailRole } = this.data

    this.setData({
      showAddModal: true,
      addModalTitle: '新增子架构',
      addParentRole: currentDetailRole,
      addForm: {
        roleOrName: '',
        remark: '',
        pid: String(currentDetailRole.roleOrId)
      }
    })
  },

  // 从详情弹窗打开编辑弹窗
  openEditModalFromDetail() {
    const { currentDetailRole } = this.data

    // 获取可选的父级列表（排除自己及其子节点）
    const editParentOptions = this.getAvailableParentsForEdit(currentDetailRole)

    // 设置当前父级ID（默认为0表示顶级）
    const currentPid = currentDetailRole.pid === null || currentDetailRole.pid === undefined ? '0' : String(currentDetailRole.pid)

    // 查找当前父级在选项列表中的索引
    const editSelectedParentIndex = editParentOptions.findIndex(item => item.roleOrId === currentPid)

    // 获取当前父级名称
    const editSelectedParentName = editParentOptions[editSelectedParentIndex]?.roleOrName || '顶级架构（无父级）'

    this.setData({
      showEditModal: true,
      editingRole: {
        ...currentDetailRole,
        members: currentDetailRole.members ? [...currentDetailRole.members] : []
      },
      editForm: {
        roleOrName: currentDetailRole.roleOrName || '',
        remark: currentDetailRole.remark || '',
        status: currentDetailRole.status !== undefined ? currentDetailRole.status : 1,
        pid: currentPid
      },
      editParentOptions: editParentOptions,
      editSelectedParentIndex: editSelectedParentIndex >= 0 ? editSelectedParentIndex : 0,
      editSelectedParentName: editSelectedParentName,
      addedMembers: [],
      removedMembers: []
    })
  },

  // 从详情弹窗删除角色
  deleteRoleFromDetail() {
    const { currentDetailRole, selectedAlumniAssociationId } = this.data
    const roleOrId = currentDetailRole.roleOrId

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个架构吗？删除后不可恢复。',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' })
            const deleteRes = await this.callDeleteRoleApi(roleOrId, selectedAlumniAssociationId)

            wx.hideLoading()

            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              this.closeNodeDetailModal()
              await this.loadRoleList(selectedAlumniAssociationId)
            } else {
              const errorMsg = (deleteRes.data && deleteRes.data.msg) || '删除失败'
              wx.showModal({
                title: '删除失败',
                content: errorMsg,
                showCancel: false,
                confirmText: '知道了'
              })
            }
          } catch (error) {
            wx.hideLoading()
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

  // 从详情弹窗打开添加成员弹窗
  openAddMemberModalFromDetail() {
    this.setData({
      showAddMemberModal: true,
      memberSearchKeyword: '',
      memberSearchResults: [],
      memberSearchLoading: false
    })
  },

  // 从详情弹窗移除成员
  removeMemberFromDetailModal(e) {
    const { member } = e.currentTarget.dataset
    const { selectedAlumniAssociationId, currentDetailRole } = this.data

    wx.showModal({
      title: '确认移除',
      content: `确定要将「${member.username || member.name || '该成员'}」从此架构移除吗？\n（成员仍保留校友会成员身份）`,
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '移除中...', mask: true })

          try {
            const apiRes = await alumniAssociationManagementApi.removeMemberFromBranch(
              selectedAlumniAssociationId,
              member.wxId
            )

            wx.hideLoading()

            if (apiRes.data && apiRes.data.code === 200) {
              // 更新当前详情弹窗中的成员列表
              const updatedRole = { ...currentDetailRole }
              updatedRole.members = (updatedRole.members || []).filter(m => m.wxId !== member.wxId)

              this.setData({
                currentDetailRole: updatedRole
              })

              wx.showToast({ title: '已移除', icon: 'success' })

              // 刷新组织架构树以获取最新数据
              await this.loadRoleList(selectedAlumniAssociationId)
            } else {
              wx.showToast({ title: apiRes.data?.msg || '移除失败', icon: 'none' })
            }
          } catch (error) {
            wx.hideLoading()
            console.error('[Debug] 移除成员失败:', error)
            wx.showToast({ title: '移除失败，请重试', icon: 'none' })
          }
        }
      }
    })
  }
})