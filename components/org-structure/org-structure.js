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
    }
  },

  data: {
    displayList: []
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
    // 为每个节点添加唯一ID和展开状态
    addExpandedState(data, prefix = 'role') {
      return (data || []).map((item, index) => {
        const uId = `${prefix}_${index}`

        const members = (item.members || []).map(m => ({
          ...m
          // 这里不再处理头像，组件中只展示姓名和职位
        }))

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
    }
  }
})

