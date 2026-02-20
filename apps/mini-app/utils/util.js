// 工具函数集合

/**
 * 格式化时间
 */
const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('-')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

/**
 * 格式化日期
 */
const formatDate = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()

  return `${[year, month, day].map(formatNumber).join('-')}`
}

/**
 * 补零
 */
const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

/**
 * 节流函数
 */
const throttle = (fn, delay = 1000) => {
  let timer = null
  return function (...args) {
    if (timer) return
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 防抖函数
 */
const debounce = (fn, delay = 500) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 深拷贝
 */
const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj
  const clone = Array.isArray(obj) ? [] : {}
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      clone[key] = deepClone(obj[key])
    }
  }
  return clone
}

/**
 * 相对时间格式化
 */
const formatRelativeTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < month) {
    return `${Math.floor(diff / day)}天前`
  } else {
    const date = new Date(timestamp)
    return formatDate(date)
  }
}

/**
 * 手机号脱敏
 */
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 数字格式化（超过万显示w）
 */
const formatNumber2 = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num
}

/**
 * 验证手机号
 */
const validatePhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 */
const validateEmail = (email) => {
  return /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(email)
}

/**
 * 获取图片信息
 */
const getImageInfo = (url) => {
  return new Promise((resolve, reject) => {
    wx.getImageInfo({
      src: url,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 选择图片
 */
const chooseImage = (count = 1) => {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 预览图片
 */
const previewImage = (current, urls) => {
  wx.previewImage({
    current,
    urls
  })
}

/**
 * 复制到剪贴板
 */
const copyToClipboard = (text) => {
  return new Promise((resolve, reject) => {
    wx.setClipboardData({
      data: text,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 拨打电话
 */
const makePhoneCall = (phoneNumber) => {
  wx.makePhoneCall({
    phoneNumber
  })
}

/**
 * 获取位置信息
 */
const getLocation = () => {
  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: 'gcj02',
      success: resolve,
      fail: reject
    })
  })
}

module.exports = {
  formatTime,
  formatDate,
  formatNumber,
  throttle,
  debounce,
  deepClone,
  formatRelativeTime,
  maskPhone,
  formatNumber2,
  validatePhone,
  validateEmail,
  getImageInfo,
  chooseImage,
  previewImage,
  copyToClipboard,
  makePhoneCall,
  getLocation,
}
