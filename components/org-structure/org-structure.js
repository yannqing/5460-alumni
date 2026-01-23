Component({
  properties: {
    // 原始组织结构数据（带 children 和 members）
    list: {
      type: Array,
      value: []
    },
    // 加载状态，用于显示骨架屏
    loading: {
      type: Boolean,
      value: false
    },
    // 空状态文案
    emptyText: {
      type: String,
      value: '暂无角色数据'
    },
    // 是否是校处会（用于判断是否显示编辑功能）
    isLocalPlatform: {
      type: Boolean,
      value: false
    },
    // 校处会ID（用于调用接口）
    localPlatformId: {
      type: String,
      value: ''
    }
  },

  data: {
    displayList: [],
    isEditMode: false, // 编辑模式
    editingMemberId: null, // 正在编辑的成员ID（格式：roleId_memberId）
    newMemberRoleId: null, // 正在添加新成员的角色ID
    canEdit: false, // 是否有编辑权限
    inputWidths: {}, // 存储每个输入框的宽度
    inputValues: {}, // 存储每个输入框的值，确保输入正常
    roleNameValues: {}, // 存储每个成员职位的输入值
    roleNameWidths: {}, // 存储每个职位输入框的宽度
    iconIndent: '' // 缩进图标路径
  },

  attached() {
    // 检查编辑权限
    this.checkEditPermission()
    // 加载缩进图标
    const config = require('../../utils/config.js')
    this.setData({
      iconIndent: config.getIconUrl('sxsj@3x.png')
    })
  },

  observers: {
    list(newVal) {
      const list = Array.isArray(newVal) ? newVal : []
      const processed = this.addExpandedState(list, 'role')
      this.setData({
        displayList: processed
      })
    }
  },

  methods: {
    // 检查编辑权限
    checkEditPermission() {
      if (!this.data.isLocalPlatform) {
        this.setData({ canEdit: false })
        return
      }
      
      const { checkHasRoles } = require('../../utils/auth.js')
      const hasPermission = checkHasRoles(['SYSTEM_SUPER_ADMIN', 'ORGANIZE_LOCAL_ADMIN'])
      this.setData({ canEdit: hasPermission })
    },

    // 为每个节点添加唯一ID和展开状态
    addExpandedState(data, prefix = 'role') {
      return (data || []).map((item, index) => {
        const uId = `${prefix}_${index}`
        const roleOrId = item.roleOrId || item.roleId || item.id

        const members = (item.members || []).map(m => {
          const memberId = m.wxId || m.id || m.userId
          // 为每个成员添加编辑ID，用于匹配
          return {
            ...m,
            _editId: `${String(roleOrId)}_${String(memberId)}`
          }
        })

        return {
          ...item,
          uniqueId: uId,
          members,
          expanded: true,
          children: item.children ? this.addExpandedState(item.children, uId) : []
        }
      })
    },

    onToggleExpand(e) {
      const { id } = e.currentTarget.dataset
      const newList = this.toggleExpandRecursive([...this.data.displayList], id)
      this.setData({
        displayList: newList
      })
    },

    toggleExpandRecursive(list, id) {
      return list.map(item => {
        if (item.uniqueId === id) {
          return {
            ...item,
            expanded: !item.expanded
          }
        } else if (item.children && item.children.length > 0) {
          return {
            ...item,
            children: this.toggleExpandRecursive(item.children, id)
          }
        }
        return item
      })
    },

    // 进入编辑模式
    onEditClick() {
      console.log('点击编辑按钮，进入编辑模式')
      console.log('当前 canEdit:', this.data.canEdit, 'isLocalPlatform:', this.data.isLocalPlatform)
      this.setData({ isEditMode: true })
      console.log('设置后的 isEditMode:', this.data.isEditMode)
    },

    // 取消编辑
    onCancelEdit() {
      // 清除所有临时成员
      const newList = this.removeAllTempMembers([...this.data.displayList])
      this.setData({ 
        isEditMode: false,
        editingMemberId: null,
        newMemberRoleId: null,
        displayList: newList,
        inputValues: {},
        inputWidths: {},
        roleNameValues: {},
        roleNameWidths: {}
      })
    },

    // 移除所有临时成员
    removeAllTempMembers(list) {
      return list.map(item => {
        const members = (item.members || []).filter(m => !m.isTemp)
        const children = item.children && item.children.length > 0 
          ? this.removeAllTempMembers(item.children)
          : item.children
        return { ...item, members, children }
      })
    },

    // 开始编辑成员名称
    onStartEditMember(e) {
      const { roleid, memberid } = e.currentTarget.dataset
      // 统一转换为字符串，确保匹配（格式与 member._editId 一致）
      const editingId = `${String(roleid)}_${String(memberid)}`
      console.log('开始编辑，roleid:', roleid, 'memberid:', memberid, 'editingId:', editingId)
      
      // 找到对应的成员，计算初始宽度（最小宽度为五个中文字符）
      const member = this.findMemberInList(this.data.displayList, roleid, memberid)
      const minWidth = 28 * 5 + 24 // 五个中文字符宽度 + 内边距 = 164rpx
      let initialWidth = minWidth
      let initialValue = ''
      if (member && member.username) {
        initialValue = member.username
        let charWidth = 0
        for (let i = 0; i < member.username.length; i++) {
          const char = member.username.charAt(i)
          if (/[\u4e00-\u9fa5]/.test(char)) {
            charWidth += 28
          } else {
            charWidth += 10
          }
        }
        initialWidth = Math.max(minWidth, charWidth + 24) // 确保不小于最小宽度
      }
      
      // 计算职位初始宽度和值
      let roleNameInitialWidth = minWidth
      let roleNameInitialValue = ''
      if (member && member.roleName) {
        roleNameInitialValue = member.roleName
        let charWidth = 0
        for (let i = 0; i < member.roleName.length; i++) {
          const char = member.roleName.charAt(i)
          if (/[\u4e00-\u9fa5]/.test(char)) {
            charWidth += 28
          } else {
            charWidth += 10
          }
        }
        roleNameInitialWidth = Math.max(minWidth, charWidth + 24)
      }
      
      const inputWidths = { ...this.data.inputWidths, [editingId]: initialWidth }
      const inputValues = { ...this.data.inputValues, [editingId]: initialValue }
      const roleNameWidths = { ...this.data.roleNameWidths, [editingId]: roleNameInitialWidth }
      const roleNameValues = { ...this.data.roleNameValues, [editingId]: roleNameInitialValue }
      
      this.setData({ 
        editingMemberId: member ? member._editId : editingId,
        inputWidths: inputWidths,
        inputValues: inputValues,
        roleNameWidths: roleNameWidths,
        roleNameValues: roleNameValues
      })
    },

    // 计算文本宽度（rpx）
    calculateTextWidth(text) {
      if (!text) return 200 // 默认宽度
      // 中文字符约28rpx，英文字符约14rpx，取平均值约20rpx
      // 加上内边距24rpx
      return Math.max(200, text.length * 20 + 24)
    },

    // 输入框聚焦
    onMemberInputFocus(e) {
      console.log('输入框聚焦')
    },

    // 成员名称输入
    onMemberNameInput(e) {
      console.log('输入事件触发:', e.detail.value)
      const { roleid, memberid } = e.currentTarget.dataset
      const value = e.detail.value || ''
      const editId = `${String(roleid)}_${String(memberid)}`
      
      // 找到对应的成员，获取 member._editId（与输入框绑定的 key 一致）
      const member = this.findMemberInList(this.data.displayList, roleid, memberid)
      const keyToUse = member && member._editId ? member._editId : editId
      
      console.log('输入editId:', editId, 'member._editId:', member?._editId, '使用key:', keyToUse)
      
      // 计算输入框宽度（根据内容长度，中文字符按28rpx，英文字符按10rpx计算）
      const minWidth = 28 * 5 + 24 // 五个中文字符宽度 + 内边距 = 164rpx
      let charWidth = 0
      for (let i = 0; i < value.length; i++) {
        const char = value.charAt(i)
        // 判断是否为中文字符
        if (/[\u4e00-\u9fa5]/.test(char)) {
          charWidth += 28
        } else {
          charWidth += 10
        }
      }
      // 确保宽度不小于最小宽度（三个中文字符），然后根据内容自由增长
      const width = Math.max(minWidth, charWidth + 24)
      const inputWidths = { ...this.data.inputWidths, [keyToUse]: width }
      const inputValues = { ...this.data.inputValues, [keyToUse]: value }
      
      // 更新displayList中对应成员的用户名
      const newList = this.updateMemberNameRecursive([...this.data.displayList], roleid, memberid, value)
      this.setData({ 
        displayList: newList,
        inputWidths: inputWidths,
        inputValues: inputValues
      })
    },

    // 成员职位输入
    onMemberRoleNameInput(e) {
      const { roleid, memberid } = e.currentTarget.dataset
      const value = e.detail.value || ''
      const editId = `${String(roleid)}_${String(memberid)}`
      
      // 找到对应的成员，获取 member._editId（与输入框绑定的 key 一致）
      const member = this.findMemberInList(this.data.displayList, roleid, memberid)
      const keyToUse = member && member._editId ? member._editId : editId
      
      // 计算输入框宽度
      const minWidth = 28 * 5 + 24 // 五个中文字符宽度 + 内边距 = 164rpx
      let charWidth = 0
      for (let i = 0; i < value.length; i++) {
        const char = value.charAt(i)
        if (/[\u4e00-\u9fa5]/.test(char)) {
          charWidth += 28
        } else {
          charWidth += 10
        }
      }
      const width = Math.max(minWidth, charWidth + 24)
      const roleNameWidths = { ...this.data.roleNameWidths, [keyToUse]: width }
      const roleNameValues = { ...this.data.roleNameValues, [keyToUse]: value }
      
      this.setData({ 
        roleNameWidths: roleNameWidths,
        roleNameValues: roleNameValues
      })
    },

    // 递归更新成员名称
    updateMemberNameRecursive(list, roleOrId, memberId, username) {
      return list.map(item => {
        const itemRoleOrId = item.roleOrId || item.roleId || item.id
        if (itemRoleOrId === roleOrId) {
          const members = (item.members || []).map(m => {
            if (m.wxId === memberId || m.id === memberId || m.userId === memberId) {
              // 保持 _editId 字段
              return { ...m, username: username, _editId: m._editId }
            }
            return m
          })
          return { ...item, members }
        } else if (item.children && item.children.length > 0) {
          return {
            ...item,
            children: this.updateMemberNameRecursive(item.children, roleOrId, memberId, username)
          }
        }
        return item
      })
    },

    // 保存成员名称
    async onSaveMemberName(e) {
      const { roleid, memberid } = e.currentTarget.dataset
      const editId = `${String(roleid)}_${String(memberid)}`
      
      console.log('保存成员名称，editId:', editId, 'roleid:', roleid, 'memberid:', memberid)
      console.log('当前 inputValues:', this.data.inputValues)
      
      // 找到对应的成员
      const member = this.findMemberInList(this.data.displayList, roleid, memberid)
      if (!member) {
        wx.showToast({
          title: '未找到成员',
          icon: 'none'
        })
        return
      }
      
      console.log('找到成员，member._editId:', member._editId, 'member.username:', member.username)
      
      // 优先使用 member._editId 从 inputValues 中获取（与输入框绑定的 key 一致）
      let username = ''
      if (member._editId && this.data.inputValues[member._editId] !== undefined && this.data.inputValues[member._editId] !== null && this.data.inputValues[member._editId] !== '') {
        username = String(this.data.inputValues[member._editId])
      } else if (this.data.inputValues[editId] !== undefined && this.data.inputValues[editId] !== null && this.data.inputValues[editId] !== '') {
        // 备用：使用 editId 获取
        username = String(this.data.inputValues[editId])
      } else {
        // 如果 inputValues 中没有，使用 member.username（bindinput 已经更新了 displayList）
        username = member.username || ''
      }
      
      console.log('最终获取的username:', username, '使用key:', member._editId || editId)
      
      if (!username || !username.trim()) {
        wx.showToast({
          title: '名称不能为空',
          icon: 'none'
        })
        return
      }

      // 获取职位值
      let rolename = ''
      if (member._editId && this.data.roleNameValues[member._editId] !== undefined && this.data.roleNameValues[member._editId] !== null) {
        rolename = String(this.data.roleNameValues[member._editId] || '')
      } else if (this.data.roleNameValues[editId] !== undefined && this.data.roleNameValues[editId] !== null) {
        rolename = String(this.data.roleNameValues[editId] || '')
      } else {
        rolename = member.roleName || ''
      }

      try {
        // 获取成员的id字段（优先使用id，如果没有则使用wxId或userId）
        const memberId = member.id || member.wxId || member.userId || memberid
        
        const { localPlatformApi } = require('../../api/api.js')
        const res = await localPlatformApi.updateMemberRole({
          localPlatformId: this.data.localPlatformId,
          roleOrId: roleid,
          id: memberId,
          username: username.trim(),
          rolename: rolename.trim()
        })

        if (res.data && res.data.code === 200) {
          wx.showToast({
            title: '保存成功',
            icon: 'success'
          })
          // 清除编辑状态和输入值
          const inputValues = { ...this.data.inputValues }
          const roleNameValues = { ...this.data.roleNameValues }
          delete inputValues[member._editId || editId]
          delete roleNameValues[member._editId || editId]
          this.setData({ 
            editingMemberId: null,
            inputValues: inputValues,
            roleNameValues: roleNameValues
          })
          // 触发父组件刷新数据
          this.triggerEvent('refresh')
        } else {
          wx.showToast({
            title: res.data?.msg || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('保存成员名称失败:', error)
        wx.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        })
      }
    },

    // 在列表中查找成员
    findMemberInList(list, roleOrId, memberId) {
      for (const item of list) {
        const itemRoleOrId = item.roleOrId || item.roleId || item.id
        if (itemRoleOrId === roleOrId) {
          const member = (item.members || []).find(m => 
            m.wxId === memberId || m.id === memberId || m.userId === memberId
          )
          if (member) return member
        }
        if (item.children && item.children.length > 0) {
          const member = this.findMemberInList(item.children, roleOrId, memberId)
          if (member) return member
        }
      }
      return null
    },

    // 点击添加成员按钮
    onAddMemberClick(e) {
      const { roleid } = e.currentTarget.dataset
      console.log('点击添加成员，roleid:', roleid)
      // 只设置 newMemberRoleId，不添加临时成员到列表（由WXML条件渲染处理）
      const editId = `new_${String(roleid)}`
      this.setData({ 
        newMemberRoleId: roleid,
        inputValues: { ...this.data.inputValues, [editId]: '' },
        inputWidths: { ...this.data.inputWidths, [editId]: 164 },
        roleNameValues: { ...this.data.roleNameValues, [editId]: '' },
        roleNameWidths: { ...this.data.roleNameWidths, [editId]: 164 }
      })
      console.log('设置 newMemberRoleId:', roleid)
    },

    // 新成员名称输入
    onNewMemberNameInput(e) {
      const { roleid } = e.currentTarget.dataset
      const value = e.detail.value
      const editId = `new_${String(roleid)}`
      
      // 计算输入框宽度（根据内容长度，中文字符按28rpx，英文字符按10rpx计算）
      const minWidth = 28 * 5 + 24 // 五个中文字符宽度 + 内边距 = 164rpx
      let charWidth = 0
      for (let i = 0; i < value.length; i++) {
        const char = value.charAt(i)
        // 判断是否为中文字符
        if (/[\u4e00-\u9fa5]/.test(char)) {
          charWidth += 28
        } else {
          charWidth += 10
        }
      }
      // 确保宽度不小于最小宽度，然后根据内容自由增长
      const width = Math.max(minWidth, charWidth + 24)
      
      // 存储新成员的输入值和宽度
      const inputValues = { ...this.data.inputValues, [editId]: value }
      const inputWidths = { ...this.data.inputWidths, [editId]: width }
      this.setData({ 
        inputValues: inputValues,
        inputWidths: inputWidths
      })
    },

    // 新成员职位输入
    onNewMemberRoleNameInput(e) {
      const { roleid } = e.currentTarget.dataset
      const value = e.detail.value
      const editId = `new_${String(roleid)}`
      
      // 计算输入框宽度
      const minWidth = 28 * 5 + 24
      let charWidth = 0
      for (let i = 0; i < value.length; i++) {
        const char = value.charAt(i)
        if (/[\u4e00-\u9fa5]/.test(char)) {
          charWidth += 28
        } else {
          charWidth += 10
        }
      }
      const width = Math.max(minWidth, charWidth + 24)
      
      const roleNameValues = { ...this.data.roleNameValues, [editId]: value }
      const roleNameWidths = { ...this.data.roleNameWidths, [editId]: width }
      this.setData({ 
        roleNameValues: roleNameValues,
        roleNameWidths: roleNameWidths
      })
    },

    // 确保新成员占位存在
    ensureNewMemberPlaceholder(list, roleOrId, username) {
      return list.map(item => {
        const itemRoleOrId = item.roleOrId || item.roleId || item.id
        if (itemRoleOrId === roleOrId) {
          const members = [...(item.members || [])]
          // 检查是否已有临时成员
          const hasTempMember = members.some(m => m.isTemp)
          if (!hasTempMember) {
            // 添加临时成员
            members.push({
              wxId: 'temp_' + Date.now(),
              username: username || '',
              isTemp: true
            })
          } else {
            // 更新临时成员的名称
            const tempIndex = members.findIndex(m => m.isTemp)
            if (tempIndex >= 0) {
              members[tempIndex] = {
                ...members[tempIndex],
                username: username || ''
              }
            }
          }
          return { ...item, members }
        } else if (item.children && item.children.length > 0) {
          return {
            ...item,
            children: this.ensureNewMemberPlaceholder(item.children, roleOrId, username)
          }
        }
        return item
      })
    },

    // 保存新成员
    async onSaveNewMember(e) {
      const { roleid } = e.currentTarget.dataset
      const editId = `new_${String(roleid)}`
      
      console.log('保存新成员，roleid:', roleid, 'editId:', editId)
      console.log('当前 inputValues:', this.data.inputValues)
      
      // 从 inputValues 中获取输入的值（尝试多种可能的key格式）
      let username = ''
      if (this.data.inputValues[editId] !== undefined && this.data.inputValues[editId] !== null && this.data.inputValues[editId] !== '') {
        username = String(this.data.inputValues[editId])
      } else {
        // 尝试其他可能的key格式
        const roleOrId = roleid
        const altEditId = `new_${String(roleOrId)}`
        if (this.data.inputValues[altEditId] !== undefined && this.data.inputValues[altEditId] !== null && this.data.inputValues[altEditId] !== '') {
          username = String(this.data.inputValues[altEditId])
        }
      }
      
      console.log('最终获取的username:', username)
      
      if (!username || !username.trim()) {
        wx.showToast({
          title: '名称不能为空',
          icon: 'none'
        })
        return
      }

      // 获取职位值
      let rolename = ''
      if (this.data.roleNameValues[editId] !== undefined && this.data.roleNameValues[editId] !== null) {
        rolename = String(this.data.roleNameValues[editId] || '')
      }

      try {
        const { localPlatformApi } = require('../../api/api.js')
        // 添加新成员时不传id字段，只传roleOrId、username和rolename
        const res = await localPlatformApi.updateMemberRole({
          localPlatformId: this.data.localPlatformId,
          roleOrId: roleid,
          username: username.trim(),
          rolename: rolename.trim()
        })

        if (res.data && res.data.code === 200) {
          wx.showToast({
            title: '添加成功',
            icon: 'success'
          })
          // 清除编辑状态和输入值
          const inputValues = { ...this.data.inputValues }
          const inputWidths = { ...this.data.inputWidths }
          const roleNameValues = { ...this.data.roleNameValues }
          const roleNameWidths = { ...this.data.roleNameWidths }
          delete inputValues[editId]
          delete inputWidths[editId]
          delete roleNameValues[editId]
          delete roleNameWidths[editId]
          this.setData({ 
            newMemberRoleId: null,
            inputValues: inputValues,
            inputWidths: inputWidths,
            roleNameValues: roleNameValues,
            roleNameWidths: roleNameWidths
          })
          // 触发父组件刷新数据
          this.triggerEvent('refresh')
        } else {
          wx.showToast({
            title: res.data?.msg || '添加失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('添加成员失败:', error)
        wx.showToast({
          title: '添加失败，请重试',
          icon: 'none'
        })
      }
    },

    // 查找临时成员
    findTempMemberInList(list, roleOrId) {
      for (const item of list) {
        const itemRoleOrId = item.roleOrId || item.roleId || item.id
        if (itemRoleOrId === roleOrId) {
          const tempMember = (item.members || []).find(m => m.isTemp)
          if (tempMember) return tempMember
        }
        if (item.children && item.children.length > 0) {
          const tempMember = this.findTempMemberInList(item.children, roleOrId)
          if (tempMember) return tempMember
        }
      }
      return null
    },

    // 移除临时成员
    removeTempMember(list, roleOrId) {
      return list.map(item => {
        const itemRoleOrId = item.roleOrId || item.roleId || item.id
        if (itemRoleOrId === roleOrId) {
          const members = (item.members || []).filter(m => !m.isTemp)
          return { ...item, members }
        } else if (item.children && item.children.length > 0) {
          return {
            ...item,
            children: this.removeTempMember(item.children, roleOrId)
          }
        }
        return item
      })
    }
  }
})

