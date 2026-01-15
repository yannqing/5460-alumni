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
      },
      {
        id: 5,
        name: '文章审核',
        icon: '📄',
        url: '/pages/article/audit-list/audit-list'
      }
    ],
    // 校处会功能列表
    schoolOfficeFunctions: [
      {
        id: 1,
        name: '校友会审核',
        icon: 'https://cni-alumni.yannqing.com/upload/images/2026/01/14/review-1.png',
        iconType: 'image',
        url: '/pages/audit/schooloffice/list/list'
      },
      {
        id: 2,
        name: '会员管理',
        icon: '👥',
        url: ''
      },
      {
        id: 3,
        name: '通知公告',
        icon: '📢',
        url: ''
      },
      {
        id: 4,
        name: '资料库',
        icon: '📚',
        url: ''
      },
      {
        id: 5,
        name: '校处风采',
        icon: '🌟',
        url: ''
      },
      {
        id: 6,
        name: '捐赠记录',
        icon: '💝',
        url: ''
      },
      {
        id: 7,
        name: '联系我们',
        icon: '📞',
        url: ''
      },
      {
        id: 8,
        name: '数据统计',
        icon: '📊',
        url: ''
      }
    ],
    // 校友会功能列表
    alumniFunctions: [
      {
        id: 1,
        name: '组织架构管理',
        icon: '🎉',
        url: '/pages/alumni-association/organization/organization'
      },
      {
        id: 2,
        name: '会员管理',
        icon: '👥',
        url: ''
      },
      {
        id: 3,
        name: '通知公告',
        icon: '📢',
        url: ''
      },
      {
        id: 4,
        name: '资料库',
        icon: '📚',
        url: ''
      },
      {
        id: 5,
        name: '校友风采',
        icon: '🌟',
        url: ''
      },
      {
        id: 6,
        name: '捐赠记录',
        icon: '💝',
        url: ''
      },
      {
        id: 7,
        name: '联系我们',
        icon: '📞',
        url: ''
      },
      {
        id: 8,
        name: '数据统计',
        icon: '📊',
        url: ''
      }
    ],
    // 商户功能列表
    merchantFunctions: [
      {
        id: 1,
        name: '店铺管理',
        icon: '🏬',
        url: ''
      },
      {
        id: 2,
        name: '商品管理',
        icon: '📦',
        url: ''
      },
      {
        id: 3,
        name: '订单管理',
        icon: '📋',
        url: ''
      },
      {
        id: 4,
        name: '优惠券',
        icon: '🎫',
        url: ''
      },
      {
        id: 5,
        name: '营销推广',
        icon: '📱',
        url: ''
      },
      {
        id: 6,
        name: '财务管理',
        icon: '💰',
        url: ''
      },
      {
        id: 7,
        name: '客服中心',
        icon: '💬',
        url: ''
      },
      {
        id: 8,
        name: '数据分析',
        icon: '📈',
        url: ''
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


