// utils/auth.js
const { authApi } = require('../api/api.js')

// 判断时间是否过期
const judgeTime = (time) => {
  let strTime = time.replace("/-/g", "/");
  let date1 = new Date(strTime);
  let date2 = new Date();
  return date1 < date2;
}

// 检测登录状态,返回 true / false
function checkHasLogined() {
  const token = wx.getStorageSync('token')
  const expire_time = wx.getStorageSync('expire_time')

  // 判断是否过期
  if (expire_time && judgeTime(expire_time)) {
    wx.setStorageSync('token', '');
    wx.setStorageSync('expire_time', '');
    return false;
  }

  if (!token) {
    return false
  } else {
    return true;
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
          icon: 'none'
        })
        return resolve('登录失败')
      }
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
      }
    })
  })
}

/**
 * 核心登录函数
 * @param {string} inviter_wx_uuid - 可选，邀请人的 UUID
 * @returns {object} 用户数据
 */
async function login(inviter_wx_uuid) {
  console.log('=== 开始登录流程 ===')
  console.log('邀请人的uuid', inviter_wx_uuid)

  // 1. 调用 wx.login 获取 code
  const wxcode = await wxaCode();

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
      duration: 2000
    })
    throw new Error(errorMsg)
  }
  
    // 2. 构建请求参数
    // 确保 code 是字符串类型
    let params = {
      code: String(wxcode).trim()  // 确保是字符串且去除首尾空格
    }

  // 如果有邀请人，加入参数
  inviter_wx_uuid && (params.inviter_wx_uuid = inviter_wx_uuid)
  
  console.log('准备发送的请求参数:', params)

  // 3. 调用后端认证登录接口
  const response = await authApi.auth(params)
  console.log('登录接口完整响应:', response)
  
  const {
    data: {
      data,
      code,
      msg
    } = {}
  } = response || {}
  
  console.log('响应状态码:', code)
  console.log('响应消息:', msg)

  // 4. 处理返回结果
  if (code == 200) {
    // 存储 token 和过期时间
    wx.setStorageSync('token', data.token)
    wx.setStorageSync('expire_time', data.expire_time)

    // 取用户唯一ID（优先 wxId，补充常见字段）
    let userId =
      data.wxId ||
      data.wx_id ||
      data.wx_uuid ||
      data.wxUid ||
      data.userId ||
      data.id ||
      data.uuid

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
      wxId: data.wxId || data.wx_id || data.wx_uuid || data.wxUid || data.userId || data.id || data.uuid || ''
    }
    app.globalData.token = data.token || ''

    // 二次加工用户信息
    formatUserInfoData(data)

    return data
  } else {
    wx.showToast({
      title: '登陆失败',
    })
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
 * 初始化小程序 - 页面调用入口
 * 检查是否已登录，未登录则自动登录
 */
async function initApp() {
  let uinfoData = {}
  let isLogin = await checkHasLogined()

  if (isLogin) {
    uinfoData = getApp().globalData.userData
    // 如果已登录但数据为空，重新登录获取数据
    if (!uinfoData || Object.keys(uinfoData).length === 0) {
      console.log('已登录但数据为空，重新登录获取数据...')
      uinfoData = await login()
    }
  } else {
    uinfoData = await login()
  }

  return uinfoData
}

// 二次加工用户信息（处理角色等）
function formatUserInfoData(userData) {
  let config_roles = {};

  userData && userData.wx_roles && userData.wx_roles.forEach(element => {
    let roleName = element.name
    config_roles[roleName] = true;
  });

  getApp().globalData.userConfig.roles = config_roles;
  getApp().globalData.userConfig.is_apply_acard = userData.is_apply_acard;
}

// 检测是否有某些角色
function checkHasRoles($arr) {
  const user_roles = getApp().globalData.userData.wx_roles || []
  for (let i = 0; i < user_roles.length; i++) {
    let name = user_roles[i].name;
    if ($arr.includes(name)) {
      return true;
    }
  }
  return false;
}

module.exports = {
  login,
  initApp,
  getUserInfo,
  checkHasLogined,
  checkHasRoles
};
