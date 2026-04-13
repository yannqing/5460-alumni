const auth = require('../utils/auth.js');
const config = require('../utils/config.js');
const featureFlags = require('../utils/feature-flags.js');

Component({
  data: {
    constructionNavBlocked: featureFlags.constructionNavBlocked,
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
      const tabIndex = Number(data.index);
      const idx = Number.isNaN(tabIndex) ? 0 : tabIndex;
      const rawPath = data.path || '';
      const url = rawPath.startsWith('/') ? rawPath : `/${rawPath}`;
      const blocked = featureFlags.constructionNavBlocked;

      if (idx === 1 && blocked) {
        return;
      }

      const app = getApp();
      const authInitializing = !!(app && app.globalData && app.globalData.authInitializing);

      // 首页不校验；其余 tab 需资料完善。登录初始化中若仍校验会静默 return，导致「点了没反应」
      if (idx !== 0 && !authInitializing) {
        if (!auth.checkProfileAndRedirect(url)) {
          return;
        }
      }

      wx.switchTab({
        url,
        success: () => {
          this.updateUnreadCount();
          this.updateAuditTodoCount();
        },
        fail: (err) => {
          console.error('[tabBar] switchTab fail', url, err);
        },
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
