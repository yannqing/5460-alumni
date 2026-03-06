// pages/alumni-association/member/member.js
const { alumniApi, alumniAssociationManagementApi, associationApi, userApi } = require('../../../api/api.js')
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
    defaultUserAvatarUrl: config.defaultAvatar
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
    return alumniAssociationManagementApi.getMemberList(alumniAssociationId)
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 调用校友会详情接口
  getAlumniAssociationDetail(alumniAssociationId) {
    return associationApi.getAssociationDetail(alumniAssociationId)
  },

  // 显示邀请成员弹窗
  showInviteModal() {
    this.setData({
      showInviteModal: true,
      inviteForm: {
        name: '',
        wxId: ''
      },
      alumniSearchResults: [],
      showAlumniSearchResults: false
    })
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
    if (!keyword) { return }
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
  // 注意：此接口不再直接添加成员，而是发送邀请通知，用户需要同意后才能加入
  async submitInvite() {
    try {
      const { wxId } = this.data.inviteForm
      const alumniAssociationId = this.data.selectedAlumniAssociationId

      // 验证必填参数
      if (!wxId || wxId === 0 || !alumniAssociationId) {
        wx.showToast({
          title: '请通过搜索选择校友',
          icon: 'none'
        })
        return
      }

      // 调用邀请成员接口，直接传递字符串形式的wxId，避免大整数精度丢失
      // roleOrId 现在是可选参数，不再需要传递
      const res = await this.inviteMemberAPI(alumniAssociationId, wxId)

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '邀请通知已发送，等待用户确认',
          icon: 'success',
          duration: 2000
        })
        this.hideInviteModal()
        // 注意：不再刷新成员列表，因为用户还未确认邀请
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
    return alumniAssociationManagementApi.getRoleList(organizeId)
  },

  // 调用邀请成员接口（roleOrId 可选）
  inviteMemberAPI(alumniAssociationId, wxId, roleOrId) {
    return alumniAssociationManagementApi.inviteMember(alumniAssociationId, wxId, roleOrId)
  },

  // 打开编辑成员弹窗
  async openEditModal(e) {
    const member = e.currentTarget.dataset.member
    this.setData({
      editingMember: {
        ...member,
        // 编辑表单字段（用户名和手机号仅未注册用户可编辑）
        editUsername: member.name || member.nickname || '',
        editUserPhone: member.userPhone || member.phone || '',
        editUserAffiliation: member.userAffiliation || '',
        editIsShowOnHome: member.isShowOnHome || 0
      },
      showEditModal: true
    })
  },

  // 编辑表单输入处理 - 用户名
  onEditUsernameInput(e) {
    this.setData({
      'editingMember.editUsername': e.detail.value
    })
  },

  // 编辑表单输入处理 - 手机号
  onEditUserPhoneInput(e) {
    this.setData({
      'editingMember.editUserPhone': e.detail.value
    })
  },

  // 编辑表单输入处理 - 所属单位/职位
  onEditUserAffiliationInput(e) {
    this.setData({
      'editingMember.editUserAffiliation': e.detail.value
    })
  },

  // 编辑表单 - 主页展示开关
  onEditIsShowOnHomeChange(e) {
    this.setData({
      'editingMember.editIsShowOnHome': e.detail.value ? 1 : 0
    })
  },

  // 关闭编辑成员弹窗
  hideEditModal() {
    this.setData({
      showEditModal: false,
      editingMember: {}
    })
  },

  // 提交编辑成员信息
  async submitEdit() {
    try {
      const { editingMember } = this.data
      const alumniAssociationId = this.data.selectedAlumniAssociationId

      // 验证必填参数 - 需要成员id
      if (!editingMember.id) {
        wx.showToast({
          title: '成员信息异常',
          icon: 'none'
        })
        return
      }

      // 构建更新数据，只传入有变化的字段
      const updateData = {
        id: editingMember.id
      }

      // 用户名和手机号仅未注册用户可编辑
      if (!editingMember.wxId) {
        if (editingMember.editUsername && editingMember.editUsername.trim()) {
          updateData.username = editingMember.editUsername.trim()
        }
        if (editingMember.editUserPhone && editingMember.editUserPhone.trim()) {
          updateData.userPhone = editingMember.editUserPhone.trim()
        }
      }

      // 所属单位/职位
      if (editingMember.editUserAffiliation !== undefined) {
        updateData.userAffiliation = editingMember.editUserAffiliation.trim()
      }

      // 主页展示
      updateData.isShowOnHome = editingMember.editIsShowOnHome

      console.log('[Debug] 更新成员信息，请求数据:', updateData)

      // 调用更新成员信息接口
      const res = await alumniAssociationManagementApi.updateMemberInfo(updateData)

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
      console.error('[Debug] 修改成员信息失败:', error)
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
    return alumniAssociationManagementApi.updateMemberRole(alumniAssociationId, wxId, roleOrId)
  },

  // 调用删除成员接口
  deleteMemberAPI(alumniAssociationId, wxId) {
    return alumniAssociationManagementApi.deleteMember(alumniAssociationId, wxId)
  },

  // 点击成员跳转到用户详情页
  onMemberTap(e) {
    const member = e.currentTarget.dataset.member
    const wxId = member.wxId

    if (!wxId) {
      // 没有 wxId，提示未注册
      wx.showToast({
        title: '该成员尚未注册',
        icon: 'none'
      })
      return
    }

    // 有 wxId，跳转到用户详情页
    wx.navigateTo({
      url: `/pages/user/detail/detail?id=${wxId}`
    })
  }
})