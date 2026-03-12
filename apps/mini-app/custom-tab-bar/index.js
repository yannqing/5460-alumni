const auth = require('../utils/auth.js');
const config = require('../utils/config.js');

Component({
  data: {
    selected: 0,
    color: "#999999",
    selectedColor: "#40B2E6",
    unreadCount: 0,
    auditTodoCount: 0,
    list: [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        iconPath: "/assets/icons/home@2x.png",
        selectedIconPath: "/assets/icons/home_hover2@2x.png"
      },
      {
        pagePath: "/pages/discover/discover",
        text: "发现",
        iconPath: "/assets/icons/discover@2x.png",
        selectedIconPath: "/assets/icons/discover_hover@2x.png"
      },
      {
        pagePath: "/pages/chat/list/list",
        text: "5460",
        iconPath: `https://${config.DOMAIN}/upload/images/assets/icons/5460@2x.png`,
        selectedIconPath: `https://${config.DOMAIN}/upload/images/assets/icons/5460@2x.png`
      },
      {
        pagePath: "/pages/search/search",
        text: "搜索",
        iconPath: "/assets/icons/find_nor@2x.png",
        selectedIconPath: "/assets/icons/find_on@2x.png"
      },
      {
        pagePath: "/pages/profile/profile",
        text: "我的",
        iconPath: "/assets/icons/own@2x.png",
        selectedIconPath: "/assets/icons/own_hover@2x.png"
      }
    ]
  },

  attached() {
    // 初始化时获取未读消息数
    this.updateUnreadCount();
    // 初始化时获取审核待办数
    this.updateAuditTodoCount();
  },

  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset;
      const index = data.index;
      const url = data.path;

      // 禁用"发现"按钮（index === 1）的跳转
      if (index === 1) {
        return;
      }

      // 如果当前已经在首页（index === 0），不需要检查
      // 其他页面需要检查用户基本信息是否完善
      if (index !== 0) {
        // 检查用户基本信息是否完善，未完善则跳转注册页
        if (!auth.checkProfileAndRedirect(url)) {
          return;
        }
      }

      wx.switchTab({
        url,
        success: () => {
          // 切换页面后更新未读消息数
          this.updateUnreadCount();
          // 切换页面后更新审核待办数
          this.updateAuditTodoCount();
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

    // 更新审核待办数
    updateAuditTodoCount() {
      const app = getApp();
      if (app && app.updateAuditTodoCount) {
        app.updateAuditTodoCount().then(count => {
          this.setData({
            auditTodoCount: count || 0
          });
        });
      }
    },

    // 设置未读消息数（供外部调用）
    setUnreadCount(count) {
      this.setData({
        unreadCount: count || 0
      });
    },

    // 设置审核待办数（供外部调用）
    setAuditTodoCount(count) {
      this.setData({
        auditTodoCount: count || 0
      });
    }
  }
});
