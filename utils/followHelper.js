// 关注功能通用工具类
const { followApi } = require('../api/api.js')

/**
 * 关注目标类型枚举
 */
const FollowTargetType = {
  USER: 1,        // 用户
  ASSOCIATION: 2, // 校友会
  SCHOOL: 3,      // 母校
  MERCHANT: 4     // 商户
}

/**
 * 关注状态枚举
 */
const FollowStatus = {
  NORMAL: 1,      // 正常关注
  SPECIAL: 2,     // 特别关注
  NO_DISTURB: 3,  // 免打扰
  CANCELED: 4     // 已取消
}

/**
 * 添加关注
 * @param {number} targetType - 目标类型：1-用户, 2-校友会, 3-母校, 4-商户
 * @param {number} targetId - 目标ID
 * @param {number} followStatus - 关注状态，默认为1(正常关注)
 * @param {string} remark - 备注，可选
 * @returns {Promise}
 */
async function addFollow(targetType, targetId, followStatus = FollowStatus.NORMAL, remark = '') {
  try {
    const params = {
      targetType,
      targetId,
      followStatus,
    }

    if (remark) {
      params.remark = remark
    }

    const res = await followApi.addFollow(params)

    if (res.data && res.data.code === 200) {
      return {
        success: true,
        message: '关注成功',
        data: res.data.data
      }
    } else {
      return {
        success: false,
        message: res.data?.msg || '关注失败'
      }
    }
  } catch (error) {
    console.error('关注失败:', error)
    return {
      success: false,
      message: '关注失败，请重试'
    }
  }
}

/**
 * 取消关注
 * @param {number} targetType - 目标类型：1-用户, 2-校友会, 3-母校, 4-商户
 * @param {number} targetId - 目标ID
 * @returns {Promise}
 */
async function removeFollow(targetType, targetId) {
  try {
    const params = {
      targetType,
      targetId
    }

    const res = await followApi.removeFollow(params)

    if (res.data && res.data.code === 200) {
      return {
        success: true,
        message: '已取消关注',
        data: res.data.data
      }
    } else {
      return {
        success: false,
        message: res.data?.msg || '取消关注失败'
      }
    }
  } catch (error) {
    console.error('取消关注失败:', error)
    return {
      success: false,
      message: '取消关注失败，请重试'
    }
  }
}

/**
 * 切换关注状态（关注/取消关注）
 * @param {boolean} isFollowed - 当前是否已关注
 * @param {number} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @param {number} followStatus - 关注状态，默认为1(正常关注)
 * @returns {Promise}
 */
async function toggleFollow(isFollowed, targetType, targetId, followStatus = FollowStatus.NORMAL) {
  if (isFollowed) {
    // 已关注，执行取消关注
    return await removeFollow(targetType, targetId)
  } else {
    // 未关注，执行添加关注
    return await addFollow(targetType, targetId, followStatus)
  }
}

/**
 * 在页面中处理关注/取消关注的通用方法
 * @param {Object} page - 页面实例(this)
 * @param {string} dataKey - 数据对象在 data 中的键名，如 'userInfo', 'schoolInfo'
 * @param {string} followedKey - 是否已关注的字段名，默认为 'isFollowed'
 * @param {number} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @param {Function} onSuccess - 成功回调（可选）
 * @param {Function} onError - 失败回调（可选）
 */
async function handleFollowInPage(page, dataKey, followedKey, targetType, targetId, onSuccess, onError) {
  const dataObj = page.data[dataKey]
  if (!dataObj) {
    wx.showToast({
      title: '数据加载中，请稍后',
      icon: 'none'
    })
    return
  }

  const isFollowed = dataObj[followedKey] || false

  // 调用切换关注状态
  const result = await toggleFollow(isFollowed, targetType, targetId)

  if (result.success) {
    // 更新页面数据
    const updateData = {}
    updateData[`${dataKey}.${followedKey}`] = !isFollowed
    page.setData(updateData)

    // 显示提示
    wx.showToast({
      title: result.message,
      icon: 'success'
    })

    // 执行成功回调
    if (onSuccess && typeof onSuccess === 'function') {
      onSuccess(result)
    }
  } else {
    // 显示错误提示
    wx.showToast({
      title: result.message,
      icon: 'none'
    })

    // 执行失败回调
    if (onError && typeof onError === 'function') {
      onError(result)
    }
  }
}

module.exports = {
  FollowTargetType,
  FollowStatus,
  addFollow,
  removeFollow,
  toggleFollow,
  handleFollowInPage
}
