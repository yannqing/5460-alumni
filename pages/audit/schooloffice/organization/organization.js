// pages/audit/schooloffice/organization/organization.js
const app = getApp()

Page({
  data: {
    schoolOfficeList: [],
    loading: false,
    selectedSchoolOfficeId: 0,
    selectedSchoolOfficeName: '',
    showSchoolOfficePicker: false,
    roleList: [], // 存储角色列表
    roleLoading: false, // 角色加载状态
    selectedOrganizeId: 0, // 存储选中的organizeId
    expandedRoles: {}, // 存储展开状态的角色ID
    // 编辑弹窗相关
    showEditModal: false,
    editingRole: null, // 当前正在编辑的角色
    editForm: {
      roleOrName: '',
      remark: '',
      roleOrCode: '',
      status: 1
    },
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
    await this.loadSchoolOfficeList()
  },

  // 加载校处会列表（从缓存中获取校处会管理员的organizeId，然后调用接口）
  async loadSchoolOfficeList() {
    try {
      console.log('[Debug] 开始加载校处会列表')
      
      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)
      
      // 查找所有校处会管理员角色
      const schoolOfficeAdminRoles = roles.filter(role => role.remark === '校处会管理员')
      console.log('[Debug] 找到的校处会管理员角色:', schoolOfficeAdminRoles)
      
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
          
          // 先创建基本的校处会列表
          const basicSchoolOfficeList = uniqueOrganizeIds.map(organizeId => ({
            id: organizeId,
            schoolOfficeId: organizeId,
            schoolOfficeName: `校处会 (ID: ${organizeId})`
          }))
          
          this.setData({
            schoolOfficeList: basicSchoolOfficeList
          })
          console.log('[Debug] 直接使用organizeIds创建校处会列表:', basicSchoolOfficeList)
          
          // 尝试调用接口获取更详细的信息（可选）
          try {
            // 并行调用所有校处会的详情接口
            const detailPromises = uniqueOrganizeIds.map(organizeId => 
              app.api.localPlatformApi.getLocalPlatformDetail(organizeId)
                .catch(error => {
                  console.log(`[Debug] 获取校处会 ${organizeId} 详情失败，使用基本数据:`, error)
                  return null // 接口失败时返回null，后续过滤
                })
            )
            
            const detailResults = await Promise.all(detailPromises)
            
            // 处理接口返回结果，更新校处会列表
            const updatedSchoolOfficeList = basicSchoolOfficeList.map((platform, index) => {
              const result = detailResults[index]
              if (result && result.data && result.data.code === 200 && result.data.data) {
                const detailData = result.data.data
                return {
                  ...detailData,
                  id: detailData.platformId || detailData.id || platform.schoolOfficeId,
                  schoolOfficeId: detailData.platformId || platform.schoolOfficeId,
                  schoolOfficeName: detailData.platformName || detailData.name || platform.schoolOfficeName
                }
              }
              return platform // 接口失败时使用基本数据
            })
            
            this.setData({
              schoolOfficeList: updatedSchoolOfficeList
            })
            console.log('[Debug] 已更新校处会列表:', updatedSchoolOfficeList)
          } catch (apiError) {
            console.log('[Debug] 批量获取校处会详情失败，继续使用基本数据:', apiError)
            // 继续使用之前创建的基本数据
          }
        } else {
          // 没有找到有效的organizeId，设置为空数组
          console.warn('[Debug] 校处会管理员角色没有有效的organization或organizeId')
          this.setData({
            schoolOfficeList: []
          })
        }
      } else {
        // 没有找到校处会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校处会管理员角色')
        this.setData({
          schoolOfficeList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校处会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        schoolOfficeList: []
      })
    }
  },

  // 显示校处会选择器
  showSchoolOfficeSelector() {
    this.setData({ showSchoolOfficePicker: false })
    this.setData({ showSchoolOfficePicker: true })
  },

  // 选择校处会
  async selectSchoolOffice(e) {
    // 正确获取数据集属性
    const schoolOfficeId = e.currentTarget.dataset.schoolOfficeId
    const schoolOfficeName = e.currentTarget.dataset.schoolOfficeName
    console.log('[Debug] 选择的校处会:', { schoolOfficeId, schoolOfficeName })

    // 获取对应的校处会对象
    const selectedSchoolOffice = this.data.schoolOfficeList.find(item => item.schoolOfficeId === schoolOfficeId)
    console.log('[Debug] 找到的校处会对象:', selectedSchoolOffice)

    this.setData({
      selectedSchoolOfficeId: schoolOfficeId,
      selectedSchoolOfficeName: schoolOfficeName,
      showSchoolOfficePicker: false,
      selectedOrganizeId: schoolOfficeId // 确保使用校处会ID
    })

    try {
      // 调用 /localPlatform/{id} 接口，入参为 schoolOfficeId
      console.log('[Debug] 准备调用 /localPlatform/{id} 接口，schoolOfficeId:', schoolOfficeId)

      const res = await this.getSchoolOfficeDetail(schoolOfficeId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校处会信息:', res.data.data)
        
        // 更新selectedSchoolOfficeId为正确的platformId
        const correctPlatformId = res.data.data.platformId || schoolOfficeId;
        this.setData({
          selectedSchoolOfficeId: correctPlatformId,
          selectedOrganizeId: correctPlatformId
        });
        console.log('[Debug] 更新selectedSchoolOfficeId为:', correctPlatformId);

        // 调用角色列表接口 - 使用正确的platformId
        await this.loadRoleList(correctPlatformId)
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)

        // 即使校处会详情接口失败，也尝试调用角色列表接口 - 使用校处会ID
        await this.loadRoleList(schoolOfficeId)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatform/{id} 接口失败:', apiError)

      // 即使出错，也尝试调用角色列表接口 - 使用校处会ID
      await this.loadRoleList(schoolOfficeId)
    }
  },

  // 加载角色列表
  async loadRoleList(schoolOfficeId) {
    try {
      console.log('[Debug] 开始加载角色列表，schoolOfficeId:', schoolOfficeId, '类型:', typeof schoolOfficeId)

      // 保持schoolOfficeId为字符串形式，避免数字精度丢失
      const organizeId = schoolOfficeId
      console.log('[Debug] 保持字符串形式的organizeId:', organizeId, '类型:', typeof organizeId)

      this.setData({ roleLoading: true })

      // 调用 /localPlatformManagement/role/list 接口 - 使用校处会ID
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/role/list`,
        method: 'POST',
        data: { 
          organizeId: organizeId, // 保持字符串形式，避免数字精度丢失
          organizeType: 1 // 校处会类型为1
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 取消选择校处会
  cancelSchoolOfficeSelect() {
    this.setData({ showSchoolOfficePicker: false })
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

    this.setData({
      showEditModal: true,
      editingRole: role,
      editForm: {
        roleOrName: role.roleOrName || '',
        remark: role.remark || '',
        roleOrCode: role.roleOrCode || '',
        status: role.status !== undefined ? role.status : 1
      }
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
        status: 1
      }
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

  // 提交编辑
  async submitEdit() {
    const { editingRole, editForm, selectedOrganizeId } = this.data

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
        organizeId: selectedOrganizeId,
        roleOrId: editingRole.roleOrId,
        pid: editingRole.pid,
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
        await this.loadRoleList(selectedOrganizeId)
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

  // 获取某个角色的所有子孙节点ID
  getDescendantIds(movingRole) {
    const ids = []
    const traverse = (children) => {
      if (!children) return
      children.forEach(child => {
        ids.push(String(child.roleOrId))
        if (child.children && child.children.length > 0) {
          traverse(child.children)
        }
      })
    }
    traverse(movingRole.children)
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
    const { movingRole, selectedSchoolOfficeId } = this.data

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
        organizeId: this.data.selectedOrganizeId,
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
        await this.loadRoleList(this.data.selectedOrganizeId)
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
    if (!this.data.selectedSchoolOfficeId) {
      wx.showToast({
        title: '请先选择校处会',
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
    const { addForm, selectedOrganizeId } = this.data

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

      // 调用新增角色接口，使用和list接口一样的selectedOrganizeId
      const res = await this.callAddRoleApi({
        organizeId: selectedOrganizeId, // 使用和list接口一样的selectedOrganizeId
        organizeType: 1, // 校处会类型为1
        pid: addForm.pid, // 保持字符串形式，与校友会页面保持一致
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
        // 重新加载角色列表，传递原始字符串形式的organizeId
        await this.loadRoleList(selectedOrganizeId)
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
    // 按接口规范转换参数类型，organizeId保持字符串格式，避免数字精度丢失
    const requestData = {
      organizeId: String(data.organizeId), // 转换为字符串形式，与校友会页面保持一致
      organizeType: data.organizeType, // 已经是数字类型
      pid: String(data.pid), // 转换为字符串形式，与校友会页面保持一致
      roleOrName: data.roleOrName, // 角色名，字符串类型
      roleOrCode: data.roleOrCode, // 角色唯一代码，字符串类型
      remark: data.remark // 角色含义，字符串类型
    }

    console.log('[Debug] 调用新增角色接口，请求数据:', requestData)

    return new Promise((resolve, reject) => {
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/role/add`,
        method: 'POST',
        data: requestData,
        header: headers,
        success: resolve,
        fail: reject
      })
    })
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

    return new Promise((resolve, reject) => {
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/role/update`,
        method: 'PUT',
        data: requestData,
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 调用校处会详情接口
  getSchoolOfficeDetail(schoolOfficeId) {
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
        url: `${app.globalData.baseUrl}/localPlatform/${schoolOfficeId}`,
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

    // 获取当前选中的校处会ID
    const organizeId = this.data.selectedOrganizeId
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
    
    const requestData = {
      roleOrId: strRoleOrId, // 保持字符串格式，避免数字精度丢失
      organizeId: strOrganizeId // 保持字符串格式，避免数字精度丢失
    }
    console.log('[Debug] 调用删除角色接口，请求数据:', requestData)

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
        url: `${app.globalData.baseUrl}/localPlatformManagement/role/delete`,
        method: 'DELETE',
        data: requestData,
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})
