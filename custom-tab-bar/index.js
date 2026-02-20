Component({
  data: {
    selected: 0,
    color: "#999999",
    selectedColor: "#40B2E6",
    unreadCount: 0,
    list: [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        iconPath: "/assets/icons/home@3x.png",
        selectedIconPath: "/assets/icons/home_hover2@3x.png"
      },
      {
        pagePath: "/pages/discover/discover",
        text: "发现",
        iconPath: "/assets/icons/discover@3x.png",
        selectedIconPath: "/assets/icons/discover_hover@3x.png"
      },
      {
        pagePath: "/pages/chat/list/list",
        text: "5460",
        iconPath: "/assets/icons/tab_5460@3x.png",
        selectedIconPath: "/assets/icons/tab_5460@3x.png"
      },
      {
        pagePath: "/pages/search/search",
        text: "搜索",
        iconPath: "/assets/icons/find_nor@3x.png",
        selectedIconPath: "/assets/icons/find_on@3x.png"
      },
      {
        pagePath: "/pages/profile/profile",
        text: "我的",
        iconPath: "/assets/icons/own@3x.png",
        selectedIconPath: "/assets/icons/own_hover@3x.png"
      }
    ]
  },

  attached() {
    // 初始化时获取未读消息数
    this.updateUnreadCount();
  },

  methods: {
    switchTab(e) {
    const data = e.currentTarget.dataset;
    const url = data.path;
    wx.switchTab({ 
      url,
      success: () => {
        // 切换页面后更新未读消息数
        this.updateUnreadCount();
      }
    });
  },

    // 更新未读消息数
    updateUnreadCount() {
      const app = getApp();
      if (app && app.updateUnreadCount) {
        app.updateUnreadCount().then(count => {
          this.setData({
            unreadCount: count || 0
          });
        });
      }
    },

    // 设置未读消息数（供外部调用）
    setUnreadCount(count) {
      this.setData({
        unreadCount: count || 0
      });
    }
  }
});
