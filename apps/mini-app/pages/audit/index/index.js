// pages/audit/index/index.js
const app = getApp()
const config = require('../../../utils/config.js')
const { userApi, auditApi } = require('../../../api/api.js')
const { refreshUserRoles } = require('../../../utils/auth.js')

Page({
  data: {
    // 管理功能列表 (源数据，不直接用于渲染)
    _allAuditFunctions: [
      {
        id: 3,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage',
      },
      {
        id: 2,
        name: '文章审核',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/user/list/list',
      },
      {
        id: 4,
        name: '轮播图管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/carousel/carousel',
      },
      {
        id: 5,
        name: '审核校友总会',
        code: 'SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/headquarters/list/list',
      },
      {
        id: 6,
        name: '校友会审核',
        code: 'SYSTEM_ALUMNI_ASSOCIATION_APPLICATION',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association/list/list',
      },
      {
        id: 7,
        name: '商户审核',
        code: 'HOME_MERCHANT_REVIEW',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant-super/list/list',
      },
    ],
    // 校促会功能列表 (源数据)
    // 已下线：城市下的「校友会审核」（LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION），不再展示入口
    _allSchoolOfficeFunctions: [
      {
        id: 3,
        name: '成员管理',
        icon: config.getIconUrl('xchcygl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/schooloffice/member/member',
      },
      {
        id: 2,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/schooloffice/organization/organization',
      },
      {
        id: 4,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage',
      },
      {
        id: 5,
        name: '信息维护',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/info-maintenance/index/index',
      },
      {
        id: 6,
        name: '校友会认证',
        code: 'SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association-certification/list/list',
      },
    ],
    // 校友会功能列表 (源数据)
    _allAlumniFunctions: [
      {
        id: 5,
        name: '加入审核',
        code: 'ALUMNI_ASSOCIATION_JOIN_REVIEW',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/join-audit/index',
      },
      {
        id: 2,
        name: '成员管理',
        icon: config.getIconUrl('xyhcygl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/member/member',
      },
      {
        id: 1,
        name: '架构管理',
        icon: config.getIconUrl('jggl@3x.png'),
        iconType: 'image',
        url: '/pages/alumni-association/organization/organization',
      },
      {
        id: 3,
        name: '商户审核',
        code: 'ALUMNI_ASSOCIATION_MERCHANT_AUDIT',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/apply/apply',
      },
      /*
      {
        id: 4,
        name: '店铺审核',
        code: 'ALUMNI_ASSOCIATION_SHOP_AUDIT',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/shop-audit/shop-audit'
      },
      */
      {
        id: 6,
        name: '活动管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/activity/list/list',
      },
      {
        id: 7,
        name: '企业管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/enterprise/index',
      },
      {
        id: 8,
        name: '信息维护',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/info-maintenance/index/index',
      },
      {
        id: 9,
        name: '资讯管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/article/manage/manage',
      },
    ],
    // 商户功能列表 (源数据)
    // 暂时下线：商户下「架构管理」，先隐藏页面入口
    _allMerchantFunctions: [
      {
        id: 1,
        name: '店铺管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/shop/shop',
      },
      {
        id: 3,
        name: '成员管理',
        icon: config.getIconUrl('shcygl@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/member/member',
      },
      {
        id: 4,
        name: '优惠券管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/coupon/coupon',
      },
      {
        id: 5,
        name: '核销优惠券',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/coupon/verify/verify',
      },
      {
        id: 6,
        name: '话题管理',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/topic/topic',
      },
      {
        id: 7,
        name: '信息维护',
        code: 'MERCHANT_ASSOCIATION_INFORMATION',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/merchant/info-maintenance/info-maintenance',
      },
    ],
    // 实际渲染用的数据
    auditFunctions: [],
    schoolOfficeFunctions: [],
    alumniFunctions: [],
    merchantFunctions: [],
    // 中间 Logo 图片（images 文件夹下）
    imageCenterLogo: config.getAssetImageUrl('dbdhl@2x.png'),
    statusBarHeight: 20,
    todoCounts: {},
  },

  onLoad(options) {
    // 获取状态栏高度
    const systemInfo = wx.getSystemInfoSync()
    this.setData({
      statusBarHeight: systemInfo.statusBarHeight || 20,
    })
  },

  onShow() {
    // 先算权限与列表，再拉待办角标，避免与 getTodoCounts 竞态；WXML 角标用 WXS 按 code 读 todoCounts
    void this.refreshAuditPage()
  },

  async refreshAuditPage() {
    await this.checkPermissions()
    await this.getTodoCounts()
  },

  // 获取待办数量统计
  async getTodoCounts() {
    try {
      const res = await auditApi.getTodoCount()
      console.log('[AuditIndex] 待办统计返回:', res)
      if (res.data && res.data.code === 200 && res.data.data) {
        const counts = { ...(res.data.data.todoCounts || {}) }
        // 兼容后端新旧 key：避免商户/店铺审核角标因字段名差异不显示
        if (
          counts.ALUMNI_ASSOCIATION_MERCHANT_AUDIT == null &&
          counts.ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT != null
        ) {
          counts.ALUMNI_ASSOCIATION_MERCHANT_AUDIT = counts.ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT
        }
        if (
          counts.ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT == null &&
          counts.ALUMNI_ASSOCIATION_MERCHANT_AUDIT != null
        ) {
          counts.ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT = counts.ALUMNI_ASSOCIATION_MERCHANT_AUDIT
        }
        if (
          counts.ALUMNI_ASSOCIATION_SHOP_AUDIT == null &&
          counts.ALUMNI_ASSOCIATION_SHOP_REVIEW != null
        ) {
          counts.ALUMNI_ASSOCIATION_SHOP_AUDIT = counts.ALUMNI_ASSOCIATION_SHOP_REVIEW
        }
        if (
          counts.ALUMNI_ASSOCIATION_SHOP_REVIEW == null &&
          counts.ALUMNI_ASSOCIATION_SHOP_AUDIT != null
        ) {
          counts.ALUMNI_ASSOCIATION_SHOP_REVIEW = counts.ALUMNI_ASSOCIATION_SHOP_AUDIT
        }
        this.setData({
          todoCounts: counts,
        })
        console.log('[AuditIndex] 设置 todoCounts:', this.data.todoCounts)

        // 同时更新全局和 TabBar 计数
        let total = 0
        for (const key in counts) {
          total += counts[key]
        }
        if (typeof this.getTabBar === 'function' && this.getTabBar()) {
          this.getTabBar().setAuditTodoCount(total)
        }
      }
    } catch (err) {
      console.warn('[AuditIndex] 获取待办统计失败:', err)
    }
  },

  // 检查用户是否有特定权限（递归遍历 roles 缓存中的权限树，与后端分配的 SYSTEM_SUPER_ADMIN 等角色一致）
  hasPermission(permissionCode) {
    const originalRoles = wx.getStorageSync('roles') || []

    const matchInTree = nodes => {
      if (!nodes || !Array.isArray(nodes)) return false
      for (const node of nodes) {
        if (node.code === permissionCode) return true
        if (node.children && matchInTree(node.children)) return true
      }
      return false
    }

    for (const role of originalRoles) {
      if (role.permissions && matchInTree(role.permissions)) {
        return true
      }
    }
    return false
  },

  // 检查用户权限并控制功能模块显示
  // 当 roles 缓存未更新（如校友会审核通过后刚成为管理员），调用 getManagedOrganizations({ roleScopedOnly: true }) 兜底
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
    let hasDevelopmentManager = false

    // 方法1：检查对象格式的角色（userConfig.roles）
    console.log('[AuditIndex] userConfig.roles:', roles)
    console.log('[AuditIndex] originalRoles:', originalRoles)
    if (typeof roles === 'object' && roles !== null) {
      hasSuperAdmin = roles['SYSTEM_SUPER_ADMIN']
      hasLocalAdmin = roles['ORGANIZE_LOCAL_ADMIN']
      hasAlumniAdmin = roles['ORGANIZE_ALUMNI_ADMIN']
      hasMerchantAdmin = roles['ORGANIZE_MERCHANT_ADMIN']
      hasShopAdmin = roles['ORGANIZE_SHOP_ADMIN']
      hasDevelopmentManager = roles['DEVELOPMENT_MANAGER']
      console.log('[AuditIndex] 方法1结果:', {
        hasSuperAdmin,
        hasLocalAdmin,
        hasAlumniAdmin,
        hasMerchantAdmin,
        hasShopAdmin,
        hasDevelopmentManager,
      })
    }

    // 方法2：如果对象格式检查失败，使用数组格式检查（originalRoles）
    if (!hasSuperAdmin && !hasLocalAdmin && !hasAlumniAdmin && !hasMerchantAdmin && !hasShopAdmin) {
      hasSuperAdmin = originalRoles.some(role => role.roleCode === 'SYSTEM_SUPER_ADMIN')
      hasLocalAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_LOCAL_ADMIN')
      hasAlumniAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_ALUMNI_ADMIN')
      hasMerchantAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_MERCHANT_ADMIN')
      hasShopAdmin = originalRoles.some(role => role.roleCode === 'ORGANIZE_SHOP_ADMIN')
      hasDevelopmentManager = originalRoles.some(role => role.roleCode === 'DEVELOPMENT_MANAGER')
      console.log('[AuditIndex] 方法2结果:', {
        hasSuperAdmin,
        hasLocalAdmin,
        hasAlumniAdmin,
        hasMerchantAdmin,
        hasShopAdmin,
        hasDevelopmentManager,
      })
    } else if (!hasDevelopmentManager) {
      // 方法1 检查通过但可能漏掉 DEVELOPMENT_MANAGER，复核数组格式
      hasDevelopmentManager = originalRoles.some(role => role.roleCode === 'DEVELOPMENT_MANAGER')
      console.log('[AuditIndex] 方法2复核DEVELOPMENT_MANAGER:', hasDevelopmentManager)
    }

    // 方法3：roles 缓存无对应管理权限时，调用接口兜底（审核通过后刚成为管理员，缓存未更新）
    // 仅 role_user：避免系统管理员被误判为校友会/校促会管理员（与后端待办统计一致）
    let alumniFromApi = false
    let localFromApi = false
    let merchantFromApi = false
    const needApiFallback =
      !hasAlumniAdmin || !hasLocalAdmin || (!hasMerchantAdmin && !hasShopAdmin)
    if (needApiFallback) {
      try {
        const res = await userApi.getManagedOrganizations({ roleScopedOnly: true })
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

    // 1. 系统管理功能 (auditFunctions)：仅超级管理员可见
    const filteredAuditFunctions =
      hasSuperAdmin || hasDevelopmentManager
        ? this.data._allAuditFunctions.filter(item => true)
        : []

    // 2. 城市功能 (schoolOfficeFunctions)：仅城市管理员可见
    let filteredSchoolOfficeFunctions = []
    if (hasLocalAdmin || localFromApi || hasDevelopmentManager) {
      filteredSchoolOfficeFunctions = this.data._allSchoolOfficeFunctions.filter(item => {
        const cityModule = this.hasPermission('LOCAL_PLATFORM_CONFIG')
        if (item.name === '成员管理')
          return this.hasPermission('LOCAL_PLATFORM_MEMBER_MANAGEMENT') || hasLocalAdmin
        if (item.name === '架构管理')
          return this.hasPermission('LOCAL_PLATFORM_ARCHIVE_MANAGEMENT') || hasLocalAdmin
        if (item.name === '资讯管理')
          return this.hasPermission('LOCAL_PLATFORM_ARTICLE_MANAGEMENT') || hasLocalAdmin
        if (item.name === '校友会认证')
          return (
            this.hasPermission('SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION') ||
            cityModule ||
            hasLocalAdmin
          )
        if (item.name === '信息维护')
          return (
            this.hasPermission('SYSTEM_ALUMNI_ASSOCIATION_MAINTENANCE') ||
            cityModule ||
            hasLocalAdmin
          )
        return false
      })
    }

    // 3. 校友会功能 (alumniFunctions)：仅校友会管理员可见
    let filteredAlumniFunctions = []
    if (hasAlumniAdmin || alumniFromApi || hasDevelopmentManager) {
      filteredAlumniFunctions = this.data._allAlumniFunctions.filter(item => {
        const alumniModule = this.hasPermission('ALUMNI_ASSOCIATION_CONFIG')
        if (item.name === '架构管理')
          return this.hasPermission('ALUMNI_ASSOCIATION_ARCHIVE_MANAGEMENT') || hasAlumniAdmin
        if (item.name === '成员管理')
          return this.hasPermission('ALUMNI_ASSOCIATION_MEMBER_MANAGEMENT') || hasAlumniAdmin
        if (item.name === '商户审核')
          return (
            this.hasPermission('ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT') ||
            this.hasPermission('ALUMNI_ASSOCIATION_MERCHANT_AUDIT') ||
            hasAlumniAdmin
          )
        if (item.name === '店铺审核')
          return this.hasPermission('ALUMNI_ASSOCIATION_SHOP_REVIEW') || hasAlumniAdmin
        if (item.name === '加入审核')
          return this.hasPermission('ALUMNI_ASSOCIATION_JOIN_REVIEW') || hasAlumniAdmin
        if (item.name === '活动管理')
          return this.hasPermission('ALUMNI_ASSOCIATION_ACTIVITY_MANAGEMENT') || hasAlumniAdmin
        if (item.name === '企业管理')
          return this.hasPermission('ALUMNI_ASSOCIATION_ENTERPRISE_MANAGEMENT') || hasAlumniAdmin
        if (item.name === '信息维护')
          return (
            this.hasPermission('ALUMNI_ASSOCIATION_INFORMATION') || alumniModule || hasAlumniAdmin
          )
        if (item.name === '资讯管理')
          return this.hasPermission('ALUMNI_ASSOCIATION_ARTICLE_MANAGEMENT') || hasAlumniAdmin
        return false
      })
    }

    // 4. 商家功能 (merchantFunctions)：商户管理员可见全部，门店管理员仅见核销
    let filteredMerchantFunctions = []
    if (hasMerchantAdmin || hasShopAdmin || merchantFromApi || hasDevelopmentManager) {
      filteredMerchantFunctions = this.data._allMerchantFunctions.filter(item => {
        // 如果是商户管理员，按具体权限码过滤
        if (hasMerchantAdmin || merchantFromApi || hasDevelopmentManager) {
          if (item.name === '店铺管理')
            return this.hasPermission('MERCHANT_SHOP_MANAGEMENT') || hasMerchantAdmin
          if (item.name === '成员管理')
            return this.hasPermission('MERCHANT_MEMBER_MANAGEMENT') || hasMerchantAdmin
          if (item.name === '优惠券管理')
            return this.hasPermission('MERCHANT_COUPON_MANAGEMENT') || hasMerchantAdmin
          if (item.name === '核销优惠券')
            return this.hasPermission('MERCHANT_DEAL_COUPON') || hasMerchantAdmin || hasShopAdmin
          if (item.name === '话题管理')
            return this.hasPermission('MERCHANT_TOPIC_MANAGEMENT') || hasMerchantAdmin
          if (item.name === '信息维护')
            return this.hasPermission('MERCHANT_ASSOCIATION_INFORMATION') || hasMerchantAdmin
        }
        // 如果只是门店管理员，只能看到“核销优惠券”
        else if (hasShopAdmin) {
          return item.name === '核销优惠券'
        }
        return false
      })
    }

    // 更新显示标志
    showSchoolOfficeFunctions = filteredSchoolOfficeFunctions.length > 0
    showAlumniFunctions = filteredAlumniFunctions.length > 0
    showMerchantFunctions = filteredMerchantFunctions.length > 0

    // 更新数据
    console.log('[AuditIndex] 最终结果:', {
      filteredAuditFunctions: filteredAuditFunctions.length,
      filteredSchoolOfficeFunctions: filteredSchoolOfficeFunctions.length,
      filteredAlumniFunctions: filteredAlumniFunctions.length,
      filteredMerchantFunctions: filteredMerchantFunctions.length,
      hasDevelopmentManager,
      hasMerchantAdmin,
      hasShopAdmin,
    })
    this.setData({
      auditFunctions: filteredAuditFunctions,
      schoolOfficeFunctions: filteredSchoolOfficeFunctions,
      alumniFunctions: filteredAlumniFunctions,
      merchantFunctions: filteredMerchantFunctions,
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
  },
})
