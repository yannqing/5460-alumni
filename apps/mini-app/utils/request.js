// utils/request.js
// API 请求封装 - 集成签名验证功能
const SignatureUtil = require('./signature.js')

const getHeaders = (url) => {
  const headers = {
    'content-type': 'application/json',
  };

  // 登录接口不需要 Authorization 头
  if (url && url.includes('/auth/login')) {
    // 登录接口，不设置 Authorization 头
  } else {
    // 获取 token 并设置请求头
    let gtoken = wx.getStorageSync('token')
    if (gtoken && gtoken.trim() !== '') {
      // 根据文档，token 直接放在 header 中，不使用 Bearer 前缀
      headers.token = gtoken
      // 兼容部分接口使用 x-token
      headers['x-token'] = gtoken
    } else {
      let uinfo = wx.getStorageSync('userInfo') || {}
      if (uinfo.token && uinfo.token.trim() !== '') {
        headers.token = uinfo.token
        headers['x-token'] = uinfo.token
      }
    }
  }

  return headers
}

const request = (params) => {
  const app = getApp()

  let data = params.data || {}
  let baseUrl = app.globalData.baseUrl
  const fullUrl = data.isAllUrl ? params.url : baseUrl + params.url
  
  // 获取基础请求头（包含 token）
  const baseHeaders = getHeaders(params.url)

  return new Promise((resolve, reject) => {
    // 确保请求体数据正确
    let requestData = data
    if (data && data.isAllUrl) {
      requestData = undefined
    } else {
      // 过滤请求数据，移除 undefined、null 和空字符串
      requestData = filterParams(requestData)
    }

    // 添加签名（根据文档要求）
    const signedOptions = SignatureUtil.signRequest({
      url: params.url,  // 使用相对路径进行白名单判断
      fullUrl: fullUrl,  // 完整 URL，用于提取 URL 参数
      data: requestData,
      header: baseHeaders,
      method: params.method || 'post'
    })

    // 合并签名后的请求头
    const finalHeaders = {
      ...baseHeaders,
      ...signedOptions.header
    }
    
    wx.request({
      url: fullUrl,
      method: params.method || 'post',
      data: requestData,
      header: finalHeaders,
      success: async function (res) {
        // 无论状态码如何，都尝试解析响应数据
        let responseData = res.data || {}
        let { code, msg } = responseData

        // 后端约定的 token 相关错误码：
        // 401                 - 兼容 HTTP 风格
        // 10000 TOKEN_EXPIRE  - token 过期
        // 10001 TOKEN_AUTHENTICATE_FAILURE - token 认证失败（非法token）
        // 10002 TOKEN_ERROR   - token 错误（无效或为空）
        const TOKEN_INVALID_CODES = [401, 10000, 10001, 10002]

        if (code == 200) {
          // 直接使用返回数据（不再使用 base64 解密）
          resolve(res)
        } else if (TOKEN_INVALID_CODES.includes(code)) {
          // token 相关错误：过期 / 非法 / 认证失败 => 自动静默重新登录（仅重试一次）
          console.warn('请求返回 token 相关错误码(', code, '), 准备进行静默重新登录...')

          // 避免死循环：同一个请求只允许重试一次
          if (params._retry) {
            console.warn('静默重新登录后依然返回 token 错误，停止重试')
            wx.setStorageSync('token', '')
            wx.setStorageSync('expire_time', '')
            // 不再弹后端的“非法token”等提示，交给业务层自行处理
            resolve(res)
            return
          }

          // 清空本地 token
          wx.setStorageSync('token', '')
          wx.setStorageSync('expire_time', '')

          try {
            // 使用全局的 initApp 进行静默登录（auth.js 已混入到 App 实例）
            if (typeof app.initApp === 'function') {
              await app.initApp()
              console.log('静默重新登录成功，准备重试原始请求')

              // 重新发起原始请求，带上 _retry 标记，防止死循环
              request({
                ...params,
                _retry: true
              }).then(resolve).catch(reject)
              return
            } else {
              console.error('app.initApp 不存在，无法自动静默登录')
            }
          } catch (e) {
            console.error('静默重新登录失败：', e)
          }

          // 如果重新登录失败，则返回原始响应
          resolve(res)
        } else {
          // 其它业务错误，正常弹出后端提示
          msg && wx.showToast({
            icon: 'none',
            title: msg
          })
          resolve(res);
        }
      },
      fail: function (res) {
        console.error('请求失败:', res)
        // 显示错误信息
        wx.showToast({
          icon: 'none',
          title: '网络错误'
        })
        reject(res);
      }
    })
  })
}

// GET 请求
const get = (url, data = {}) => {
  // GET 请求将参数拼接到 URL 上
  // 但签名计算需要使用原始参数，所以将参数传递给 request
  let finalUrl = url
  if (data && Object.keys(data).length > 0) {
    const queryString = Object.keys(data)
      .filter(key => data[key] !== undefined && data[key] !== null && data[key] !== '')
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
      .join('&')
    if (queryString) {
      finalUrl += (finalUrl.includes('?') ? '&' : '?') + queryString
    }
  }
  return request({
    url: finalUrl,
    method: 'GET',
    data: data  // 传递原始参数用于签名计算
  })
}

// 参数过滤函数：移除 undefined、null 和空字符串
const filterParams = (data) => {
  const result = {}
  for (const key in data) {
    if (data.hasOwnProperty(key)) {
      const value = data[key]
      // 只保留非 undefined、非 null、非空字符串的值
      if (value !== undefined && value !== null && value !== '') {
        result[key] = value
      }
    }
  }
  return result
}

// POST 请求
const post = (url, data = {}) => {
  return request({
    url,
    method: 'POST',
    data: filterParams(data)
  })
}

// PUT 请求
const put = (url, data = {}) => {
  return request({
    url,
    method: 'PUT',
    data: filterParams(data)
  })
}

// DELETE 请求
const del = (url, data = {}) => {
  return request({
    url,
    method: 'DELETE',
    data: filterParams(data)
  })
}

module.exports = {
  get,
  post,
  put,
  del,
  request
}
