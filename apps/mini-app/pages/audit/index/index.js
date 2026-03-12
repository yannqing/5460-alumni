// pages/audit/index/index.js
const app = getApp()
const config = require('../../../utils/config.js')
const { userApi } = require('../../../api/api.js')
const { refreshUserRoles } = require('../../../utils/auth.js')

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
        id: 3,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage'
      },
      {
        id: 2,
        name: '文章审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/user/list/list'
      },
      {
        id: 4,
        name: '轮播图管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/carousel/carousel'
      },
      {
        id: 5,
        name: '审核校友总会',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/headquarters/list/list'
      },
      {
        id: 6,
        name: '校友会审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association/list/list'
      }
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
    // 校促会功能列表
    schoolOfficeFunctions: [
      {
        id: 1,
        name: '校友会审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/schooloffice/list/list'
      },
      {
        id: 3,
        name: '成员管理',
        icon: config.getIconUrl('xchcygl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/schooloffice/member/member'
      },
      {
        id: 2,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/schooloffice/organization/organization'
      },
      {
        id: 4,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage'
      },
      {
        id: 5,
        name: '信息维护',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/info-maintenance/index/index'
      },
      {
        id: 6,
        name: '校友会认证',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association-certification/list/list'
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
        id: 5,
        name: '加入审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/join-audit/index'
      },
      {
        id: 2,
        name: '成员管理',
        icon: config.getIconUrl('xyhcygl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/member/member'
      },
      {
        id: 1,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/organization/organization'
      },
      {
        id: 3,
        name: '商户审核',
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
      {
        id: 6,
        name: '活动管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/activity/list/list'
      },
      {
        id: 7,
        name: '企业管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/enterprise/index'
      },
      {
        id: 8,
        name: '信息维护',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/info-maintenance/index/index'
      },
      {
        id: 9,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage'
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
      {
        id: 3,
        name: '成员管理',
        icon: config.getIconUrl('shcygl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/member/member'
      },
      {
        id: 2,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/architecture/architecture'
      },
      {
        id: 4,
        name: '优惠券管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/coupon/coupon'
      },
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
    ],
    // 中间 Logo 图片（images 文件夹下）
    imageCenterLogo: config.getAssetImageUrl('dbdhl@2x.png'),
    statusBarHeight: 20
  },

  onLoad(options) {
    // 获取状态栏高度
    const systemInfo = wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: systemInfo.statusBarHeight || 20
    })

    // 页面加载
    this.checkPermissions()
  },

  onShow() {
    // 每次显示时重新检查权限，兼容审核通过后 roles 缓存未更新的情况
    this.checkPermissions()
  },

  // 检查用户是否有特定权限
  hasPermission(permissionCode) {
    // 获取用户的原始角色列表（从缓存中读取）
    const originalRoles = wx.getStorageSync('roles') || []

    // 遍历所有角色
    for (const role of originalRoles) {
      // 检查角色是否有permissions数组
      if (role.permissions && Array.isArray(role.permissions)) {
        // 遍历角色的所有权限
        for (const permission of role.permissions) {
          // 检查当前权限是否有code属性且匹配
          if (permission.code === permissionCode) {
            return true
          }
          // 检查子权限
          if (permission.children && Array.isArray(permission.children)) {
            for (const childPermission of permission.children) {
              if (childPermission.code === permissionCode) {
                return true
              }
            }
          }
        }
      }
    }
    return false
  },

  // 检查用户权限并控制功能模块显示
  // 当 roles 缓存未更新（如校友会审核通过后刚成为管理员），调用 getManagedOrganizations 接口兜底
  async checkPermissions() {
    const app = getApp()
    const userConfig = app.globalData.userConfig || {}
    const roles = userConfig.roles || {}

    // 获取用户的原始角色列表（从缓存中读取）
    const originalRoles = wx.getStorageSync('roles') || []

    // 默认不显示任何功能模块
    let showSchoolOfficeFunctions = false
    let showAlumniFunctions = false
    let showMerchantFunctions = false

    // 检查用户角色（同时支持对象格式和数组格式）
    let hasSuperAdmin = false
    let hasLocalAdmin = false
    let hasAlumniAdmin = false
    let hasMerchantAdmin = false
    let hasShopAdmin = false

    // 方法1：检查对象格式的角色（userConfig.roles）
    if (typeof roles === 'object' && roles !== null) {
      hasSuperAdmin = roles['SYSTEM_SUPER_ADMIN']
      hasLocalAdmin = roles['ORGANIZE_LOCAL_ADMIN']
      hasAlumniAdmin = roles['ORGANIZE_ALUMNI_ADMIN']
      hasMerchantAdmin = roles['ORGANIZE_MERCHANT_ADMIN']
      hasShopAdmin = roles['ORGANIZE_SHOP_ADMIN']
    }

    // 方法2：如果对象格式检查失败，使用数组格式检查（originalRoles）
    if (!hasSuperAdmin && !hasLocalAdmin && !hasAlumniAdmin && !hasMerchantAdmin && !hasShopAdmin) {
      hasSuperAdmin = originalRoles.some(role => role.roleCode === 'SYSTEM_SUPER_ADMIN')
      hasLocalAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_LOCAL_ADMIN')
      hasAlumniAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_ALUMNI_ADMIN')
      hasMerchantAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_MERCHANT_ADMIN')
      hasShopAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_SHOP_ADMIN')
    }

    // 方法3：roles 缓存无对应管理权限时，调用接口兜底（审核通过后刚成为管理员，缓存未更新）
    let alumniFromApi = false
    let localFromApi = false
    let merchantFromApi = false
    const needApiFallback = !hasAlumniAdmin || !hasLocalAdmin || (!hasMerchantAdmin && !hasShopAdmin)
    if (needApiFallback) {
      try {
        const res = await userApi.getManagedOrganizations({}) // 不传 type 返回所有类型
        const list = (res?.data?.data ?? res?.data ?? []) || []
        if (Array.isArray(list) && list.length > 0) {
          for (const item of list) {
            const t = item.type
            if (t === 0 && !hasAlumniAdmin) {
              hasAlumniAdmin = true
              alumniFromApi = true
            } else if (t === 1 && !hasLocalAdmin) {
              hasLocalAdmin = true
              localFromApi = true
            } else if (t === 2 && !hasMerchantAdmin) {
              hasMerchantAdmin = true
              hasShopAdmin = true
              merchantFromApi = true
            }
          }
        }
        if (alumniFromApi || localFromApi || merchantFromApi) {
          refreshUserRoles() // 静默刷新 roles，使后续页面展示与重新登录一致
        }
      } catch (err) {
        console.warn('[AuditIndex] 管理权限接口兜底检查失败:', err)
      }
    }

    // 过滤系统管理功能（auditFunctions）
    const filteredAuditFunctions = this.data.auditFunctions.filter(item => {
      // 根据功能名称检查对应权限
      if (item.name === '文章审核') {
        return this.hasPermission('HOME_PAGE_ARTICLE_REVIEW')
      } else if (item.name === '资讯管理') {
        return this.hasPermission('HOME_PAGE_ARTICLE_MANAGEMENT')
      } else if (item.name === '轮播图管理') {
        return this.hasPermission('HOME_PAGE_BANNER_MANAGEMENT')
      } else if (item.name === '审核校友总会') {
        return this.hasPermission('SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT')
      } else if (item.name === '校友会审核') {
        return this.hasPermission('SYSTEM_ALUMNI_ASSOCIATION_APPLICATION')
      }
      return false
    })

    // 过滤校促会管理功能（schoolOfficeFunctions）
    // localFromApi 时权限来自接口兜底，直接显示全部（子页面会用 getManagedOrganizations 校验）
    const filteredSchoolOfficeFunctions = localFromApi
      ? this.data.schoolOfficeFunctions
      : this.data.schoolOfficeFunctions.filter(item => {
          if (item.name === '校友会审核') return this.hasPermission('LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION')
          if (item.name === '成员管理') return this.hasPermission('LOCAL_PLATFORM_MEMBER_MANAGEMENT')
          if (item.name === '架构管理') return this.hasPermission('LOCAL_PLATFORM_ARCHIVE_MANAGEMENT')
          if (item.name === '资讯管理') return this.hasPermission('LOCAL_PLATFORM_ARTICLE_MANAGEMENT')
          if (item.name === '信息维护' || item.name === '校友会认证') return true
          return false
        })

    // 过滤校友会管理功能（alumniFunctions）
    // alumniFromApi 时权限来自接口兜底，直接显示全部（子页面会用 getManagedOrganizations 校验）
    const filteredAlumniFunctions = alumniFromApi
      ? this.data.alumniFunctions
      : this.data.alumniFunctions.filter(item => {
          if (item.name === '架构管理') return this.hasPermission('ALUMNI_ASSOCIATION_ARCHIVE_MANAGEMENT')
          if (item.name === '成员管理') return this.hasPermission('ALUMNI_ASSOCIATION_MEMBER_MANAGEMENT')
          if (item.name === '商户审核') return this.hasPermission('ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT')
          if (item.name === '店铺审核') return this.hasPermission('ALUMNI_ASSOCIATION_SHOP_REVIEW')
          if (item.name === '加入审核') return this.hasPermission('ALUMNI_ASSOCIATION_JOIN_REVIEW')
          if (item.name === '活动管理') return this.hasPermission('ALUMNI_ASSOCIATION_ACTIVITY_MANAGEMENT')
          if (item.name === '企业管理') return this.hasPermission('ALUMNI_ASSOCIATION_ENTERPRISE_MANAGEMENT')
          if (item.name === '信息维护') return this.hasPermission('ALUMNI_ASSOCIATION_INFORMATION')
          if (item.name === '资讯管理') return this.hasPermission('ALUMNI_ASSOCIATION_ARTICLE_MANAGEMENT')
          return false
        })

    // 过滤商家管理功能（merchantFunctions）
    // merchantFromApi 时权限来自接口兜底，直接显示全部
    const filteredMerchantFunctions = merchantFromApi
      ? this.data.merchantFunctions
      : this.data.merchantFunctions.filter(item => {
          if (item.name === '店铺管理') return this.hasPermission('MERCHANT_SHOP_MANAGEMENT')
          if (item.name === '成员管理') return this.hasPermission('MERCHANT_MEMBER_MANAGEMENT')
          if (item.name === '架构管理') return this.hasPermission('MERCHANT_ARCHIVE_MANAGEMENT')
          if (item.name === '优惠券管理') return this.hasPermission('MERCHANT_COUPON_MANAGEMENT')
          if (item.name === '核销优惠券') return this.hasPermission('MERCHANT_DEAL_COUPON')
          if (item.name === '话题管理') return this.hasPermission('MERCHANT_TOPIC_MANAGEMENT')
          return false
        })

    // 根据角色设置其他功能模块显示权限
    if (hasLocalAdmin || this.hasPermission('LOCAL_PLATFORM_CONFIG')) {
      showSchoolOfficeFunctions = true
    }
    if (hasLocalAdmin || hasAlumniAdmin || this.hasPermission('ALUMNI_ASSOCIATION_CONFIG')) {
      showAlumniFunctions = true
    }
    if (hasLocalAdmin || hasAlumniAdmin || hasMerchantAdmin || hasShopAdmin || this.hasPermission('MERCHANT_CONFIG')) {
      showMerchantFunctions = true
    }

    // 更新数据，根据权限过滤功能列表
    this.setData({
      auditFunctions: filteredAuditFunctions,
      schoolOfficeFunctions: showSchoolOfficeFunctions ? filteredSchoolOfficeFunctions : [],
      alumniFunctions: showAlumniFunctions ? filteredAlumniFunctions : [],
      merchantFunctions: showMerchantFunctions ? filteredMerchantFunctions : []
    })
  },

  // 点击功能按钮
  onFunctionTap(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      wx.navigateTo({ url })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})


