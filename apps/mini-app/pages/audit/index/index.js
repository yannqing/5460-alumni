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
        code: 'SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/headquarters/list/list'
      },
      {
        id: 6,
        name: '校友会审核',
        code: 'SYSTEM_ALUMNI_ASSOCIATION_APPLICATION',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association/list/list'
      }
    ],
    // 校促会功能列表 (源数据)
    _allSchoolOfficeFunctions: [
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
        code: 'SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION',
        icon: config.getIconUrl('xyhsh@3x.png'),
        iconType: 'image',
        url: '/pages/audit/alumni-association-certification/list/list'
      }
    ],
    // 校友会功能列表 (源数据)
    _allAlumniFunctions: [
      {
        id: 5,
        name: '加入审核',
        code: 'ALUMNI_ASSOCIATION_JOIN_REVIEW',
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
      }
    ],
    // 商户功能列表 (源数据)
    _allMerchantFunctions: [
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
    ],
    // 实际渲染用的数据
    auditFunctions: [],
    schoolOfficeFunctions: [],
    alumniFunctions: [],
    merchantFunctions: [],
    // 中间 Logo 图片（images 文件夹下）
    imageCenterLogo: config.getAssetImageUrl('dbdhl@2x.png'),
    statusBarHeight: 20,
    todoCounts: {}
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
    // 获取待办数量统计
    this.getTodoCounts()
  },

  // 获取待办数量统计
  async getTodoCounts() {
    try {
      const res = await auditApi.getTodoCount()
      console.log('[AuditIndex] 待办统计返回:', res)
      if (res.data && res.data.code === 200 && res.data.data) {
        const counts = res.data.data.todoCounts || {}
        this.setData({
          todoCounts: counts
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
  // 当 roles 缓存未更新（如校友会审核通过后刚成为管理员），调用 getManagedOrganizations({ roleScopedOnly: true }) 兜底
  async checkPermissions() {
    const app = getApp()
    const userConfig = app.globalData.userConfig || {}
    const roles = userConfig.roles || {}

    // 获取用户的原始角色列表（从缓存中读取）
    const originalRoles = wx.getStorageSync('roles') || []

    // 默认不显示任何功能模块（商家模块全局不展示）
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
    // 仅 role_user：避免系统管理员被误判为校友会/校促会管理员（与后端待办统计一致）
    let alumniFromApi = false
    let localFromApi = false
    let merchantFromApi = false
    const needApiFallback = !hasAlumniAdmin || !hasLocalAdmin || (!hasMerchantAdmin && !hasShopAdmin)
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

    // 过滤系统管理功能（auditFunctions）
    const filteredAuditFunctions = this.data._allAuditFunctions.filter(item => {
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

    // 组织侧入口：纯系统管理员仅展示系统级菜单；若 role_user 有校促会/校友会绑定则再展示对应块
    const canUseLocalOrgFeatures = !hasSuperAdmin || hasLocalAdmin || localFromApi
    const canUseAlumniOrgFeatures = !hasSuperAdmin || hasAlumniAdmin || alumniFromApi

    // 过滤校促会管理功能（schoolOfficeFunctions）
    // localFromApi 时权限来自接口兜底，直接显示全部（子页面用 roleScopedOnly 或业务接口校验）
    const filteredSchoolOfficeFunctions = localFromApi
      ? this.data._allSchoolOfficeFunctions
      : this.data._allSchoolOfficeFunctions.filter(item => {
          if (item.name === '校友会审核') {
            return canUseLocalOrgFeatures && this.hasPermission('LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION')
          }
          if (item.name === '成员管理') {
            return canUseLocalOrgFeatures && this.hasPermission('LOCAL_PLATFORM_MEMBER_MANAGEMENT')
          }
          if (item.name === '架构管理') {
            return canUseLocalOrgFeatures && this.hasPermission('LOCAL_PLATFORM_ARCHIVE_MANAGEMENT')
          }
          if (item.name === '资讯管理') {
            return canUseLocalOrgFeatures && this.hasPermission('LOCAL_PLATFORM_ARTICLE_MANAGEMENT')
          }
          if (item.name === '校友会认证') {
            return canUseLocalOrgFeatures && this.hasPermission('SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION')
          }
          if (item.name === '信息维护') {
            return canUseLocalOrgFeatures && this.hasPermission('SYSTEM_ALUMNI_ASSOCIATION_MAINTENANCE')
          }
          return false
        })

    // 过滤校友会管理功能（alumniFunctions）
    // alumniFromApi 时权限来自接口兜底，直接显示全部（子页面用 roleScopedOnly 或业务接口校验）
    const filteredAlumniFunctions = alumniFromApi
      ? this.data._allAlumniFunctions
      : this.data._allAlumniFunctions.filter(item => {
          if (item.name === '架构管理') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_ARCHIVE_MANAGEMENT')
          }
          if (item.name === '成员管理') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_MEMBER_MANAGEMENT')
          }
          if (item.name === '商户审核') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_MERCHANT_MANAGEMENT')
          }
          if (item.name === '店铺审核') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_SHOP_REVIEW')
          }
          if (item.name === '加入审核') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_JOIN_REVIEW')
          }
          if (item.name === '活动管理') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_ACTIVITY_MANAGEMENT')
          }
          if (item.name === '企业管理') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_ENTERPRISE_MANAGEMENT')
          }
          if (item.name === '信息维护') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_INFORMATION')
          }
          if (item.name === '资讯管理') {
            return canUseAlumniOrgFeatures && this.hasPermission('ALUMNI_ASSOCIATION_ARTICLE_MANAGEMENT')
          }
          return false
        })

    // 过滤商家管理功能（merchantFunctions）
    // 需求：商家及其下面的功能暂不展示（包括超级管理员）
    const filteredMerchantFunctions = []

    // 根据角色设置其他功能模块显示权限
    if (hasLocalAdmin || this.hasPermission('LOCAL_PLATFORM_CONFIG')) {
      showSchoolOfficeFunctions = true
    }
    if (hasLocalAdmin || hasAlumniAdmin || this.hasPermission('ALUMNI_ASSOCIATION_CONFIG')) {
      showAlumniFunctions = true
    }
    // 商家模块全局隐藏，不再根据任何角色或权限展示
    showMerchantFunctions = false

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


