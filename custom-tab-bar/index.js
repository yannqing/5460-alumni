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
        iconPath: "/assets/icons/home.png",
        selectedIconPath: "/assets/icons/home.png"
      },
      {
        pagePath: "/pages/discover/discover",
        text: "发现",
        iconPath: "/assets/icons/find.png",
        selectedIconPath: "/assets/icons/find.png"
      },
      {
        pagePath: "/pages/chat/list/list",
        text: "5460",
        iconPath: "/assets/icons/chat.png",
        selectedIconPath: "/assets/icons/chat.png"
      },
      {
        pagePath: "/pages/search/search",
        text: "搜索",
        iconPath: "/assets/icons/search.png",
        selectedIconPath: "/assets/icons/search.png"
      },
      {
        pagePath: "/pages/profile/profile",
        text: "我的",
        iconPath: "/assets/icons/own.png",
        selectedIconPath: "/assets/icons/own.png"
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
      wx.switchTab({ url });
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
