// 递归树节点组件：微信小程序不支持 template 递归，需使用自调用组件
Component({
  properties: {
    node: { type: Object, value: null },
    depth: { type: Number, value: 0 },
    iconIndent: { type: String, value: '' },
    iconArrowDown: { type: String, value: '' },
    iconArrowRight: { type: String, value: '' },
    isEditMode: { type: Boolean, value: false },
    isLocalPlatform: { type: Boolean, value: false },
    canEdit: { type: Boolean, value: false },
    editingMemberId: { type: null, value: null },
    newMemberRoleId: { type: null, value: null },
    inputValues: { type: Object, value: {} },
    inputWidths: { type: Object, value: {} },
    roleNameValues: { type: Object, value: {} },
    roleNameWidths: { type: Object, value: {} }
  },

  methods: {
    onToggleExpand(e) {
      const id = (e && e.detail && e.detail.id) || (this.data.node && this.data.node.uniqueId)
      if (id) this.triggerEvent('toggleexpand', { id })
    },
    onMemberClick(e) {
      const detail = (e && e.detail) || (e && e.currentTarget && e.currentTarget.dataset)
      if (detail) this.triggerEvent('memberclick', detail)
    },
    onStartEditMember(e) {
      const d = e.detail || (e.currentTarget && e.currentTarget.dataset) || {}
      this.triggerEvent('starteditmember', d)
    },
    onMemberNameInput(e) {
      const ds = e.currentTarget && e.currentTarget.dataset
      this.triggerEvent('membernameinput', { value: e.detail.value, roleid: ds && ds.roleid, memberid: ds && ds.memberid })
    },
    onMemberRoleNameInput(e) {
      const ds = e.currentTarget && e.currentTarget.dataset
      this.triggerEvent('memberrolenameinput', { value: e.detail.value, roleid: ds && ds.roleid, memberid: ds && ds.memberid })
    },
    onMemberInputFocus(e) {
      this.triggerEvent('memberinputfocus', e.detail || {})
    },
    onSaveMemberName(e) {
      const d = e.detail || (e.currentTarget && e.currentTarget.dataset) || {}
      this.triggerEvent('savemembername', d)
    },
    onAddMemberClick(e) {
      const d = e.detail || (e.currentTarget && e.currentTarget.dataset) || {}
      this.triggerEvent('addmemberclick', d)
    },
    onNewMemberNameInput(e) {
      const ds = e.currentTarget && e.currentTarget.dataset
      this.triggerEvent('newmembernameinput', { value: e.detail.value, roleid: ds && ds.roleid })
    },
    onNewMemberRoleNameInput(e) {
      const ds = e.currentTarget && e.currentTarget.dataset
      this.triggerEvent('newmemberrolenameinput', { value: e.detail.value, roleid: ds && ds.roleid })
    },
    onSaveNewMember(e) {
      const d = e.detail || (e.currentTarget && e.currentTarget.dataset) || {}
      this.triggerEvent('savenewmember', d)
    }
  }
})
