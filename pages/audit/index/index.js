// pages/audit/index/index.js
const app = getApp()
const config = require('../../../utils/config.js')

Page({
  data: {
    // 管理功能列表
    auditFunctions: [
      // {
      //   id: 1,
      //   name: '文章发布',
      //   icon: '🔍',
      //   url: '/pages/article-publish/index/index'
      // },
      {
        id: 2,
        name: '文章管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/user/list/list'
      },
      // {
      //   id: 3,
      //   name: '轮播图管理',
      //   icon: config.getIconUrl('xyhsh@3x.png'),
      //   iconType: 'image',
      //   url: ''
      // }
      // {
      //   id: 3,
      //   name: '内容审核',
      //   icon: '📝',
      //   url: '/pages/audit/content/list/list'
      // },
      // {
      //   id: 4,
      //   name: '商家审核',
      //   icon: '🏪',
      //   url: '/pages/audit/merchant/list/list'
      // },
      // {
      //   id: 5,
      //   name: '文章审核',
      //   icon: '📄',
      //   url: '/pages/article/audit-list/audit-list'
      // }
    ],
    // 校处会功能列表
  schoolOfficeFunctions: [
    {
      id: 1,
      name: '校友会审核',
      icon: config.getIconUrl('xyhsh@3x.png'),
      iconType: 'image',
      url: '/pages/audit/schooloffice/list/list'
    },
    {
      id: 2,
      name: '架构管理',
      icon: config.getIconUrl('jggl@3x.png'),
      iconType: 'image',
      url: '/pages/audit/schooloffice/organization/organization'
    },
    {
      id: 3,
      name: '成员管理',
      icon: config.getIconUrl('xchcygl@3x.png'),
      iconType: 'image',
      url: '/pages/audit/schooloffice/member/member'
    },
      // {
      //   id: 4,
      //   name: '资料库',
      //   icon: '📚',
      //   url: ''
      // },
      // {
      //   id: 5,
      //   name: '校处风采',
      //   icon: '🌟',
      //   url: ''
      // },
      // {
      //   id: 6,
      //   name: '捐赠记录',
      //   icon: '💝',
      //   url: ''
      // },
      // {
      //   id: 7,
      //   name: '联系我们',
      //   icon: '📞',
      //   url: ''
      // },
      // {
      //   id: 8,
      //   name: '数据统计',
      //   icon: '📊',
      //   url: ''
      // }
    ],
    // 校友会功能列表
    alumniFunctions: [
      {
        id: 1,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/organization/organization'
      },
      {
        id: 2,
        name: '成员管理',
        icon: config.getIconUrl('xyhcygl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/member/member'
      },
      {
        id: 3,
        name: '商户管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/apply/apply'
      },
      {
        id: 4,
        name: '店铺审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/shop-audit/shop-audit'
      },
      // {
      //   id: 4,
      //   name: '资料库',
      //   icon: '📚',
      //   url: ''
      // },
      // {
      //   id: 5,
      //   name: '校友风采',
      //   icon: '🌟',
      //   url: ''
      // },
      // {
      //   id: 6,
      //   name: '捐赠记录',
      //   icon: '💝',
      //   url: ''
      // },
      // {
      //   id: 7,
      //   name: '联系我们',
      //   icon: '📞',
      //   url: ''
      // },
      // {
      //   id: 8,
      //   name: '数据统计',
      //   icon: '📊',
      //   url: ''
      // }
    ],
    // 商户功能列表
    merchantFunctions: [
      {
        id: 1,
        name: '店铺管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/shop/shop'
      },
      // {
      //   id: 2,
      //   name: '架构管理',
      //   icon: config.getIconUrl('jggl@3x.png'),
      //   iconType: 'image',
      //   url: '/pages/audit/merchant/architecture/architecture'
      // },
      // {
      //   id: 3,
      //   name: '成员管理',
      //   icon: config.getIconUrl('shcygl@3x.png'),
      //   iconType: 'image',
      //   url: '/pages/audit/merchant/member/member'
      // },
      // {
      //   id: 4,
      //   name: '优惠券',
      //   icon: config.getIconUrl('xyhsh@3x.png'),
      //   iconType: 'image',
      //   url: '/pages/audit/merchant/coupon/coupon'
      // },
      {
        id: 5,
        name: '核销优惠券',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/coupon/verify/verify'
      },
      {
        id: 6,
        name: '话题管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/topic/topic'
      }
      // 以下功能暂时注释
      // {
      //   id: 5,
      //   name: '营销推广',
      //   icon: '📱',
      //   url: ''
      // },
      // {
      //   id: 6,
      //   name: '财务管理',
      //   icon: '💰',
      //   url: ''
      // },
      // {
      //   id: 7,
      //   name: '客服中心',
      //   icon: '💬',
      //   url: ''
      // },
      // {
      //   id: 8,
      //   name: '数据分析',
      //   icon: '📈',
      //   url: ''
      // }
    ]
  },

  onLoad(options) {
    // 页面加载
    this.checkPermissions()
  },

  // 检查用户权限并控制功能模块显示
  checkPermissions() {
    const app = getApp()
    const userConfig = app.globalData.userConfig || {}
    const roles = userConfig.roles || {}
    
    // 获取用户的原始角色列表（从缓存中读取）
    const originalRoles = wx.getStorageSync('roles') || []
    
    // 默认不显示任何功能模块
    let showAuditFunctions = false
    let showSchoolOfficeFunctions = false
    let showAlumniFunctions = false
    let showMerchantFunctions = false
    
    // 检查用户角色（同时支持对象格式和数组格式）
    let hasSuperAdmin = false
    let hasLocalAdmin = false
    let hasAlumniAdmin = false
    let hasMerchantAdmin = false
    
    // 方法1：检查对象格式的角色（userConfig.roles）
    if (typeof roles === 'object' && roles !== null) {
      hasSuperAdmin = roles['SYSTEM_SUPER_ADMIN']
      hasLocalAdmin = roles['ORGANIZE_LOCAL_ADMIN']
      hasAlumniAdmin = roles['ORGANIZE_ALUMNI_ADMIN']
      hasMerchantAdmin = roles['ORGANIZE_MERCHANT_ADMIN']
    }
    
    // 方法2：如果对象格式检查失败，使用数组格式检查（originalRoles）
    if (!hasSuperAdmin && !hasLocalAdmin && !hasAlumniAdmin && !hasMerchantAdmin) {
      hasSuperAdmin = originalRoles.some(role => role.roleCode === 'SYSTEM_SUPER_ADMIN')
      hasLocalAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_LOCAL_ADMIN')
      hasAlumniAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_ALUMNI_ADMIN')
      hasMerchantAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_MERCHANT_ADMIN')
    }
    
    // 根据角色设置显示权限
    if (hasSuperAdmin) {
      // 超级管理员：显示所有功能
      showAuditFunctions = true
      showSchoolOfficeFunctions = true
      showAlumniFunctions = true
      showMerchantFunctions = true
    } else if (hasLocalAdmin) {
      // 校处会管理员：显示校处会、校友会和商户功能
      showSchoolOfficeFunctions = true
      showAlumniFunctions = true
      showMerchantFunctions = true
    } else if (hasAlumniAdmin) {
      // 校友会管理员：显示校友会和商户功能
      showAlumniFunctions = true
      showMerchantFunctions = true
    } else if (hasMerchantAdmin) {
      // 商户管理员：只显示商户功能
      showMerchantFunctions = true
    }
    
    // 更新数据，根据权限过滤功能列表
    this.setData({
      auditFunctions: showAuditFunctions ? this.data.auditFunctions : [],
      schoolOfficeFunctions: showSchoolOfficeFunctions ? this.data.schoolOfficeFunctions : [],
      alumniFunctions: showAlumniFunctions ? this.data.alumniFunctions : [],
      merchantFunctions: showMerchantFunctions ? this.data.merchantFunctions : []
    })
  },

  // 点击功能按钮
  onFunctionTap(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    }
  }
})


