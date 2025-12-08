// pages/audit/index/index.js
const app = getApp()

Page({
  data: {
    // 管理功能列表
    auditFunctions: [
      {
        id: 1,
        name: '审核管理',
        icon: '🔍',
        url: '/pages/audit/list/list'
      },
      {
        id: 2,
        name: '用户审核',
        icon: '👤',
        url: '/pages/audit/user/list/list'
      },
      {
        id: 3,
        name: '内容审核',
        icon: '📝',
        url: '/pages/audit/content/list/list'
      },
      {
        id: 4,
        name: '商家审核',
        icon: '🏪',
        url: '/pages/audit/merchant/list/list'
      }
    ]
  },

  onLoad(options) {
    // 页面加载
  },

  // 点击功能按钮
  onFunctionTap(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    }
  }
})

