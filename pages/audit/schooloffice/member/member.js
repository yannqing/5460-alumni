// pages/audit/schooloffice/member/member.js
const { alumniApi } = require('../../../../api/api.js')
const app = getApp()

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
    schoolOfficeList: [],
    loading: false,
    selectedSchoolOfficeId: 0,
    selectedSchoolOfficeName: '',
    showSchoolOfficePicker: false,
    selectedOrganizeId: 0, // 存储选中的organizeId
    hasSingleSchoolOffice: false, // 是否只有一个校处会权限
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
    roleList: []
  },

  onLoad(options) {
    // 创建搜索防抖函数
    this.searchAlumniDebounced = debounce(this.searchAlumni, 500)
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
            
            // 判断权限数量，处理自动选择逻辑
            this.handleSchoolOfficeSelection(updatedSchoolOfficeList)
          } catch (apiError) {
            console.log('[Debug] 批量获取校处会详情失败，继续使用基本数据:', apiError)
            // 继续使用之前创建的基本数据
            this.handleSchoolOfficeSelection(basicSchoolOfficeList)
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

  // 处理校处会选择逻辑
  async handleSchoolOfficeSelection(schoolOfficeList) {
    if (schoolOfficeList.length === 1) {
      // 只有一个校处会权限，自动选择并禁用选择器
      const singleSchoolOffice = schoolOfficeList[0]
      this.setData({
        selectedSchoolOfficeId: singleSchoolOffice.schoolOfficeId,
        selectedSchoolOfficeName: singleSchoolOffice.schoolOfficeName,
        selectedOrganizeId: singleSchoolOffice.schoolOfficeId,
        hasSingleSchoolOffice: true
      })
      console.log('[Debug] 只有一个校处会权限，自动选择:', singleSchoolOffice)
      // 加载成员列表
      await this.loadMemberList(singleSchoolOffice.schoolOfficeId)
    } else if (schoolOfficeList.length > 1) {
      // 多个校处会权限，正常显示选择器
      this.setData({
        hasSingleSchoolOffice: false
      })
      console.log('[Debug] 有多个校处会权限，正常显示选择器')
    } else {
      // 没有校处会权限
      this.setData({
        hasSingleSchoolOffice: false
      })
      console.log('[Debug] 没有校处会权限')
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
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
      }
      
      // 加载该校处会的成员列表
      await this.loadMemberList(this.data.selectedSchoolOfficeId)
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatform/{id} 接口失败:', apiError)
    }
  },
  
  // 加载成员列表
  async loadMemberList(localPlatformId) {
    try {
      this.setData({ memberLoading: true })
      console.log('[Debug] 开始加载成员列表，localPlatformId:', localPlatformId)
      
      // 调用成员列表接口
      const res = await this.queryMemberList(localPlatformId)
      
      if (res.data && res.data.code === 200) {
        // 获取原始成员列表
        let memberList = (res.data.data && res.data.data.records) || []
        
        // 排序成员列表：pid=0的排在前面，然后按角色名称排序
        memberList.sort((a, b) => {
          // 首先比较pid，pid=0的排在前面
          const pidA = a.organizeArchiRole ? a.organizeArchiRole.pid : null
          const pidB = b.organizeArchiRole ? b.organizeArchiRole.pid : null
          
          if (pidA === '0' && pidB !== '0') return -1
          if (pidA !== '0' && pidB === '0') return 1
          
          // 如果pid相同，按角色名称排序
          const roleNameA = a.organizeArchiRole ? a.organizeArchiRole.roleOrName : ''
          const roleNameB = b.organizeArchiRole ? b.organizeArchiRole.roleOrName : ''
          
          return roleNameA.localeCompare(roleNameB)
        })
        
        this.setData({
          memberList: memberList,
          memberLoading: false
        })
        console.log('[Debug] 成员列表加载完成:', memberList)
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
  queryMemberList(localPlatformId) {
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
        url: `${app.globalData.baseUrl}/localPlatform/members/page`,
        method: 'POST',
        data: { localPlatformId: localPlatformId },
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
  
  // 取消选择校处会
  cancelSchoolOfficeSelect() {
    this.setData({ showSchoolOfficePicker: false })
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
    if (!keyword) return
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
      const localPlatformId = this.data.selectedSchoolOfficeId

      // 验证必填参数
      if (!wxId || wxId === 0 || !roleOrId || !localPlatformId) {
        wx.showToast({
          title: '请通过搜索选择校友并选择身份',
          icon: 'none'
        })
        return
      }

      // 调用邀请成员接口，直接传递字符串形式的wxId和roleOrId，避免大整数精度丢失
      const res = await this.inviteMemberAPI(localPlatformId, wxId, roleOrId)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '邀请成功',
          icon: 'success'
        })
        this.hideInviteModal()
        // 刷新成员列表
        await this.loadMemberList(localPlatformId)
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
      const localPlatformId = this.data.selectedSchoolOfficeId
      if (!localPlatformId) {
        console.error('[Debug] 加载角色列表失败：缺少localPlatformId')
        return
      }
      
      const res = await this.getRoleList(localPlatformId)
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/role/list`,
        method: 'POST',
        data: {
          organizeId: organizeId,
          organizeType: 1 // 1-校处会
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },
  
  // 调用邀请成员接口
  inviteMemberAPI(localPlatformId, wxId, roleOrId) {
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/inviteMember`,
        method: 'POST',
        data: {
          localPlatformId,
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
      const localPlatformId = this.data.selectedSchoolOfficeId

      // 验证必填参数
      if (!newRoleId || !wxId || !localPlatformId) {
        wx.showToast({
          title: '请选择新角色',
          icon: 'none'
        })
        return
      }

      // 调用更新成员角色接口
      const res = await this.updateMemberRoleAPI(localPlatformId, wxId, newRoleId)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '修改成功',
          icon: 'success'
        })
        this.hideEditModal()
        // 刷新成员列表
        await this.loadMemberList(localPlatformId)
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
    const localPlatformId = this.data.selectedSchoolOfficeId

    // 确认删除
    wx.showModal({
      title: '确认删除',
      content: `确定要删除成员 "${member.name || member.nickname || '未命名'}" 吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用删除成员接口
            const result = await this.deleteMemberAPI(localPlatformId, wxId)
            
            if (result.data && result.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              // 刷新成员列表
              await this.loadMemberList(localPlatformId)
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

  // 调用删除成员接口
  deleteMemberAPI(localPlatformId, wxId) {
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/deleteMember`,
        method: 'DELETE',
        data: {
          localPlatformId,
          wxId
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 调用更新成员角色接口
  updateMemberRoleAPI(localPlatformId, wxId, roleOrId) {
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
        url: `${app.globalData.baseUrl}/localPlatformManagement/updateMemberRole`,
        method: 'PUT',
        data: {
          localPlatformId,
          wxId,
          roleOrId
        },
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})
