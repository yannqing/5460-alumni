// utils/auth.js
const { authApi } = require('../api/api.js')

// ==================== 登录锁机制 ====================
// 防止多个请求并发触发登录，导致 "code been used" 错误
let isLoggingIn = false // 是否正在登录中
let loginPromise = null // 当前登录的 Promise（供其他请求等待）

/**
 * 获取登录锁状态
 * @returns {boolean} 是否正在登录中
 */
function isLoginInProgress() {
  return isLoggingIn
}

/**
 * 等待当前登录完成
 * @returns {Promise} 当前登录的 Promise
 */
function waitForLogin() {
  return loginPromise
}

// 判断时间是否过期
const judgeTime = time => {
  const strTime = time.replace('/-/g', '/')
  const date1 = new Date(strTime)
  const date2 = new Date()
  return date1 < date2
}

// 检测登录状态,返回 true / false
function checkHasLogined() {
  const token = wx.getStorageSync('token')
  const expire_time = wx.getStorageSync('expire_time')

  // 判断是否过期
  if (expire_time && judgeTime(expire_time)) {
    wx.setStorageSync('token', '')
    wx.setStorageSync('expire_time', '')
    return false
  }

  if (!token) {
    return false
  } else {
    return true
  }
}

// 静默获取微信登录 code
async function wxaCode() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        return resolve(res.code)
      },
      fail() {
        wx.showToast({
          title: '静默登录失败',
          icon: 'none',
        })
        return resolve('登录失败')
      },
    })
  })
}

// 微信 session 检查
async function checkSession() {
  return new Promise((resolve, reject) => {
    wx.checkSession({
      success() {
        return resolve(true)
      },
      fail() {
        return resolve(false)
      },
    })
  })
}

/**
 * 核心登录函数（带锁机制，防止并发登录）
 * @param {string} inviter_wx_uuid - 可选，邀请人的 UUID
 * @returns {object} 用户数据
 */
async function login(inviter_wx_uuid) {
  // ========== 登录锁检查 ==========
  // 如果已有登录正在进行，等待它完成而不是发起新的登录
  if (isLoggingIn && loginPromise) {
    console.log('[Auth] 检测到登录正在进行中，等待当前登录完成...')
    try {
      const result = await loginPromise
      console.log('[Auth] 等待的登录已完成')
      return result
    } catch (e) {
      console.warn('[Auth] 等待的登录失败，将发起新的登录')
      // 继续执行新的登录
    }
  }

  // 设置登录锁
  isLoggingIn = true
  console.log('[Auth] 获取登录锁，开始登录流程')

  // 创建一个新的 Promise 供其他请求等待
  let resolveLogin, rejectLogin
  loginPromise = new Promise((resolve, reject) => {
    resolveLogin = resolve
    rejectLogin = reject
  })

  try {
    console.log('=== 开始登录流程 ===')
    console.log('邀请人的uuid', inviter_wx_uuid)

    // 引入配置，判断是否使用测试登录
    const config = require('./config.js')
    const useTestLogin = config.shouldUseTestLogin()

    let response

    if (useTestLogin) {
      // ========== 测试登录模式（仅开发版生效）==========
      console.log('[Auth] 使用测试登录模式')
      console.log('[Auth] 测试登录 wxId:', config.testLogin.wxId)
      console.log('[Auth] 测试登录接口:', config.testLogin.apiPath)

      const testParams = {
        wxId: config.testLogin.wxId,
      }

      // 测试模式也支持传入邀请人 ID
      inviter_wx_uuid && (testParams.inviterWxUuid = inviter_wx_uuid)

      // 直接使用 post 方法调用 config 中配置的接口路径
      const { post } = require('./request.js')
      response = await post(config.testLogin.apiPath, testParams)
    } else {
      // ========== 正式登录模式 ==========
      // 1. 调用 wx.login 获取 code
      const wxcode = await wxaCode()

      console.log('获取到的微信 code:', wxcode)
      console.log('code 类型:', typeof wxcode)
      console.log('code 长度:', wxcode ? wxcode.length : 0)

      // 验证 code 是否有效
      if (!wxcode || typeof wxcode !== 'string' || wxcode.trim() === '' || wxcode === '登录失败') {
        const errorMsg = '获取微信登录code失败，请重试'
        console.error('微信code无效:', wxcode)
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000,
        })
        throw new Error(errorMsg)
      }

      // 2. 构建请求参数
      // 确保 code 是字符串类型
      const params = {
        code: String(wxcode).trim(), // 确保是字符串且去除首尾空格
      }

      // 如果有邀请人，加入参数
      inviter_wx_uuid && (params.inviterWxUuid = inviter_wx_uuid)

      console.log('准备发送的请求参数:', params)

      // 3. 调用后端认证登录接口
      response = await authApi.auth(params)
    }
    console.log('登录接口完整响应:', response)

    const { data: { data, code, msg } = {} } = response || {}

    console.log('响应状态码:', code)
    console.log('响应消息:', msg)

    // 4. 处理返回结果
    if (code == 200) {
      // 存储 token 和过期时间
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('expire_time', data.expire_time)

      // 存储角色列表到缓存
      if (data.roles && Array.isArray(data.roles)) {
        wx.setStorageSync('roles', data.roles)
        console.log('[Auth] 已存储角色列表到缓存:', data.roles)
      } else {
        // 如果没有角色信息，存储空数组
        wx.setStorageSync('roles', [])
        console.warn('[Auth] 登录响应中没有角色信息')
      }

      // 存储用户基本信息完善状态
      const isProfileComplete = data.isProfileComplete === true
      wx.setStorageSync('isProfileComplete', isProfileComplete)
      console.log('[Auth] 用户基本信息完善状态:', isProfileComplete)

      // 取用户唯一ID（优先 wxId，补充常见字段）
      let userId =
        data.wxId || data.wx_id || data.wx_uuid || data.wxUid || data.userId || data.id || data.uuid

      // 如果登录响应没有 ID，再调用用户信息接口兜底
      if (!userId) {
        console.warn('[Auth] 登录响应缺少用户ID，尝试调用 /users/getInfo 兜底')
        try {
          const { userApi } = require('../api/api.js')
          const infoRes = await userApi.getUserInfo()
          if (infoRes?.data?.code === 200) {
            const info = infoRes.data.data || {}
            userId =
              info.wxId ||
              info.wx_id ||
              info.wx_uuid ||
              info.wxUid ||
              info.userId ||
              info.id ||
              info.uuid ||
              info.openId ||
              info.openid ||
              info.unionId ||
              info.unionid
            // 将获取到的用户信息与原始数据合并
            Object.assign(data, info)
          }
        } catch (e) {
          console.warn('[Auth] 兜底获取用户信息失败:', e)
        }
      }

      if (userId) {
        wx.setStorageSync('userId', userId)
      } else {
        console.warn('[Auth] 登录成功但未获取到用户ID字段，返回数据为:', data)
      }

      // 存储用户数据到全局，并补齐 wxId 字段
      const app = getApp()
      app.globalData.userData = {
        ...data,
        wxId:
          data.wxId ||
          data.wx_id ||
          data.wx_uuid ||
          data.wxUid ||
          data.userId ||
          data.id ||
          data.uuid ||
          '',
      }
      app.globalData.userDataLoaded = true
      app.globalData.token = data.token || ''
      app.globalData.isProfileComplete = data.isProfileComplete === true

      // 二次加工用户信息（包括角色处理）
      formatUserInfoData(data)

      // 释放登录锁，通知等待者成功
      isLoggingIn = false
      resolveLogin(data)
      console.log('[Auth] 登录成功，释放登录锁')

      return data
    } else {
      // 释放登录锁，通知等待者失败
      isLoggingIn = false
      rejectLogin(new Error(msg || '登录失败'))
      console.log('[Auth] 登录失败，释放登录锁')

      wx.showToast({
        title: '登陆失败',
      })
    }
  } catch (error) {
    // 异常时也要释放登录锁
    isLoggingIn = false
    rejectLogin(error)
    console.error('[Auth] 登录异常，释放登录锁:', error)
    throw error
  }
}

// 获取/刷新用户信息
async function getUserInfo() {
  // 如果 token 已过期，重新登录
  const isLogin = checkHasLogined()
  if (!isLogin) {
    return await login()
  }

  // 如果已登录，返回缓存的用户数据
  return getApp().globalData.userData
}

/**
 * 静默刷新用户角色（重新登录以获取最新 roles）
 * 当检测到用户已有管理权限但 roles 缓存未更新时调用，使后续页面展示与重新登录一致
 * @returns {Promise<object|null>} 登录成功返回用户数据，失败返回 null
 */
async function refreshUserRoles() {
  if (!checkHasLogined()) return null
  try {
    const data = await login()
    console.log('[Auth] 静默刷新角色成功')
    return data
  } catch (e) {
    console.warn('[Auth] 静默刷新角色失败:', e)
    return null
  }
}

/**
 * 初始化小程序 - 页面调用入口
 * 检查是否已登录，未登录则自动登录
 * @param {string} inviter_wx_uuid - 可选，邀请人的 UUID
 */
async function initApp(inviter_wx_uuid) {
  let uinfoData = {}
  const isLogin = await checkHasLogined()

  if (isLogin) {
    uinfoData = getApp().globalData.userData
    // 如果已登录但数据为空，重新登录获取数据
    if (!uinfoData || Object.keys(uinfoData).length === 0) {
      console.log('已登录但数据为空，重新登录获取数据...')
      uinfoData = await login(inviter_wx_uuid)
    }
  } else {
    uinfoData = await login(inviter_wx_uuid)
  }

  return uinfoData
}

// 二次加工用户信息（处理角色等）
function formatUserInfoData(userData) {
  const config_roles = {}

  // 新格式：处理 roles 数组（优先使用）
  if (userData && userData.roles && Array.isArray(userData.roles)) {
    userData.roles.forEach(role => {
      // 使用 roleCode 作为标识（如 SYSTEM_USER, ASSOCIATION_PRESIDENT）
      if (role.roleCode) {
        config_roles[role.roleCode] = true
      }
      // 同时也存储 roleName 方便查询（如 "普通用户", "校友会会长"）
      if (role.roleName) {
        config_roles[role.roleName] = true
      }
    })
    console.log('[Auth] 处理新格式角色数据:', config_roles)
  }
  // 旧格式兼容：处理 wx_roles 数组
  else if (userData && userData.wx_roles && Array.isArray(userData.wx_roles)) {
    userData.wx_roles.forEach(element => {
      const roleName = element.name
      config_roles[roleName] = true
    })
    console.log('[Auth] 处理旧格式角色数据:', config_roles)
  }

  getApp().globalData.userConfig.roles = config_roles
  getApp().globalData.userConfig.is_apply_acard = userData.is_apply_acard
}

// 检测是否有某些角色
// 参数可以是角色代码（roleCode）或角色名称（roleName）
function checkHasRoles($arr) {
  // 优先从缓存读取角色列表（新格式）
  const roles = wx.getStorageSync('roles') || []

  // 新格式：检查 roles 数组
  if (roles.length > 0) {
    for (let i = 0; i < roles.length; i++) {
      const role = roles[i]
      // 检查 roleCode 或 roleName 是否在目标数组中
      if ($arr.includes(role.roleCode) || $arr.includes(role.roleName)) {
        return true
      }
    }
    return false
  }

  // 旧格式兼容：检查 wx_roles 数组
  const user_roles = getApp().globalData.userData.wx_roles || []
  for (let i = 0; i < user_roles.length; i++) {
    const name = user_roles[i].name
    if ($arr.includes(name)) {
      return true
    }
  }
  return false
}

/**
 * 获取当前用户的角色列表
 * @returns {Array} 角色列表数组
 */
function getUserRoles() {
  return wx.getStorageSync('roles') || []
}

/**
 * 检查用户是否有指定的角色代码
 * @param {string} roleCode - 角色代码，如 'SYSTEM_USER', 'ASSOCIATION_PRESIDENT'
 * @returns {boolean}
 */
function hasRoleCode(roleCode) {
  const roles = wx.getStorageSync('roles') || []
  return roles.some(role => role.roleCode === roleCode)
}

/**
 * 检查用户是否有指定的角色名称
 * @param {string} roleName - 角色名称，如 '普通用户', '校友会会长'
 * @returns {boolean}
 */
function hasRoleName(roleName) {
  const roles = wx.getStorageSync('roles') || []
  return roles.some(role => role.roleName === roleName)
}

/**
 * 获取用户的所有角色代码
 * @returns {Array<string>} 角色代码数组，如 ['SYSTEM_USER', 'ASSOCIATION_PRESIDENT']
 */
function getUserRoleCodes() {
  const roles = wx.getStorageSync('roles') || []
  return roles.map(role => role.roleCode).filter(code => code)
}

/**
 * 获取用户的所有角色名称
 * @returns {Array<string>} 角色名称数组，如 ['普通用户', '校友会会长']
 */
function getUserRoleNames() {
  const roles = wx.getStorageSync('roles') || []
  return roles.map(role => role.roleName).filter(name => name)
}

/**
 * 检查用户是否有管理权限（文章管理和审核管理）
 * 允许的角色：SYSTEM_SUPER_ADMIN, ORGANIZE_LOCAL_ADMIN, ORGANIZE_ALUMNI_ADMIN, ORGANIZE_MERCHANT_ADMIN, DEVELOPMENT_MANAGER
 * @returns {boolean} 是否有管理权限
 */
function hasManagementPermission() {
  // 允许的角色代码列表
  const allowedRoleCodes = [
    'SYSTEM_SUPER_ADMIN', // 系统管理员
    'ORGANIZE_LOCAL_ADMIN', // 校促会管理员
    'ORGANIZE_ALUMNI_ADMIN', // 校友会管理员
    'ORGANIZE_MERCHANT_ADMIN', // 商户管理员
    'ORGANIZE_SHOP_ADMIN', // 门店管理员
    'DEVELOPMENT_MANAGER', // 开发者管理员
  ]

  // 优先从缓存读取角色列表
  const roles = wx.getStorageSync('roles') || []

  // 检查是否有允许的角色
  if (roles.length > 0) {
    for (let i = 0; i < roles.length; i++) {
      const role = roles[i]
      // 检查 roleCode 是否在允许列表中
      if (role.roleCode && allowedRoleCodes.includes(role.roleCode)) {
        return true
      }
    }
  }

  return false
}

/**
 * 检查用户基本信息是否完善
 * @returns {boolean} 是否完善
 */
function checkProfileComplete() {
  // 优先从 globalData 读取，其次从缓存读取
  const app = getApp()
  let isComplete = false
  if (app && app.globalData && app.globalData.isProfileComplete !== undefined) {
    isComplete = app.globalData.isProfileComplete === true
  } else {
    isComplete = wx.getStorageSync('isProfileComplete') === true
  }

  // 额外检查：仅在用户数据已完成加载后，才用昵称兜底校验
  // 避免冷启动异步登录尚未回填 userData 时，误判为未完善并弹注册页
  if (
    isComplete &&
    app &&
    app.globalData &&
    app.globalData.userDataLoaded === true &&
    app.globalData.userData
  ) {
    const nickname = app.globalData.userData.nickname
    if (!nickname || (typeof nickname === 'string' && !nickname.trim())) {
      console.log('[Auth] 用户昵称为空，视为基本信息未完善')
      return false
    }
  }

  return isComplete
}

/**
 * 检查用户基本信息是否完善，如果未完善则跳转到注册页
 * @param {string} targetUrl - 目标页面URL（可选，用于日志记录）
 * @returns {boolean} 是否可以继续导航（true-可以，false-已拦截并跳转到注册页）
 */
function checkProfileAndRedirect(targetUrl = '') {
  const app = getApp()
  // 启动登录初始化尚未完成时，不直接判定为未完善并跳注册页
  // 先短暂拦截当前点击，待初始化完成后再按真实状态判断
  if (app && app.globalData && app.globalData.authInitializing === true) {
    console.log('[Auth] 登录初始化中，暂不触发注册页跳转:', targetUrl)
    return false
  }

  const isComplete = checkProfileComplete()

  if (!isComplete) {
    console.log('[Auth] 用户基本信息未完善，拦截导航:', targetUrl)
    wx.navigateTo({
      url: '/pages/register/register',
      fail: () => {
        // 如果跳转失败（可能在注册页），静默处理
        console.log('[Auth] 跳转注册页失败，可能已在注册页')
      },
    })
    return false
  }

  return true
}

/**
 * 更新用户基本信息完善状态（注册成功后调用）
 * @param {boolean} isComplete - 是否完善
 */
function updateProfileComplete(isComplete) {
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.isProfileComplete = isComplete
    if (isComplete) {
      // 注册完成后，认为用户基本数据已具备可校验条件
      app.globalData.userDataLoaded = true
    }
  }
  wx.setStorageSync('isProfileComplete', isComplete)
  console.log('[Auth] 更新用户基本信息完善状态:', isComplete)
}

module.exports = {
  login,
  initApp,
  refreshUserRoles,
  getUserInfo,
  checkHasLogined,
  checkHasRoles,
  getUserRoles,
  hasRoleCode,
  hasRoleName,
  getUserRoleCodes,
  getUserRoleNames,
  hasManagementPermission,
  // 登录锁相关
  isLoginInProgress,
  waitForLogin,
  // 用户信息完善检查
  checkProfileComplete,
  checkProfileAndRedirect,
  updateProfileComplete,
}
