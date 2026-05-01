// components/organization-picker/organization-picker.js
const config = require('../../utils/config.js')

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false,
    },
    title: {
      type: String,
      value: '选择组织',
    },
    list: {
      type: Array,
      value: [],
    },
    selectedId: {
      type: String,
      value: '',
    },
    showSearch: {
      type: Boolean,
      value: true,
    },
    searchPlaceholder: {
      type: String,
      value: '搜索名称',
    },
    emptyText: {
      type: String,
      value: '暂无数据',
    },
    itemKey: {
      type: String,
      value: 'id',
    },
    nameKey: {
      type: String,
      value: 'name',
    },
    loadingMore: {
      type: Boolean,
      value: false,
    },
    showAlphabet: {
      type: Boolean,
      value: false,
    },
  },

  data: {
    searchKeyword: '',
    displayList: [],
    alphabetList: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''),
    activeAlphabet: '',
    currentAlphabet: '',
    availableAlphabets: [],
  },

  observers: {
    list: function (list) {
      this.updateDisplayList(list, this.data.searchKeyword)
      this.updateAvailableAlphabets(list)
    },
    searchKeyword: function (keyword) {
      this.updateDisplayList(this.data.list, keyword)
    },
  },

  methods: {
    updateDisplayList(list, keyword) {
      if (!keyword || !keyword.trim()) {
        this.setData({ displayList: list })
        return
      }
      const lowerKeyword = keyword.trim().toLowerCase()
      const filtered = list.filter(item => {
        const name = item.name || item.alumniAssociationName || ''
        return name.toLowerCase().includes(lowerKeyword)
      })
      this.setData({ displayList: filtered })
    },

    updateAvailableAlphabets(list) {
      const alphabets = new Set()
      list.forEach(item => {
        const initial = item.pinyinInitial
        if (initial) {
          alphabets.add(initial.toUpperCase())
        }
      })
      this.setData({ availableAlphabets: Array.from(alphabets).sort() })
    },

    onSearchInput(e) {
      this.setData({ searchKeyword: e.detail.value })
    },

    onSelect(e) {
      const item = e.currentTarget.dataset.item
      this.triggerEvent('select', { item })
    },

    onCancel() {
      this.triggerEvent('cancel')
      this.setData({ searchKeyword: '', activeAlphabet: '' })
    },

    onAlphabetTap(e) {
      const letter = e.currentTarget.dataset.letter
      const { availableAlphabets } = this.data
      if (!availableAlphabets.includes(letter)) {
        return
      }
      this.setData({
        activeAlphabet: letter,
        currentAlphabet: letter,
      })
      // 触发滚动到对应字母位置
      this.triggerEvent('alphabetchange', { letter })
    },

    onListScroll(e) {
      // 滚动时更新当前显示的字母索引（简化处理）
    },

    onScrollLower() {
      if (!this.data.loadingMore) {
        this.triggerEvent('loadmore')
      }
    },

    onLogoError(e) {
      const { index } = e.currentTarget.dataset
      const list = this.data.list
      if (list[index]) {
        list[index].logo = config.defaultAvatar
        this.setData({ list })
        this.updateDisplayList(list, this.data.searchKeyword)
      }
    },
  },
})
