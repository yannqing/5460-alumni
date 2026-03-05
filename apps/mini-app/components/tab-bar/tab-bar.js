// components/tab-bar/tab-bar.js
Component({
  properties: {
    // tab 列表
    tabs: {
      type: Array,
      value: []
    },
    // 当前选中的 tab 索引
    activeTab: {
      type: Number,
      value: 0
    }
  },

  methods: {
    onTabClick(e) {
      const index = e.currentTarget.dataset.index;
      if (index !== this.data.activeTab) {
        this.triggerEvent('tabchange', { index });
      }
    }
  }
});
