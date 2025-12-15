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
 * @param {boolean} showConfirm - 取消关注时是否显示确认框，默认为true
 * @returns {Promise}
 */
async function toggleFollow(isFollowed, targetType, targetId, followStatus = FollowStatus.NORMAL, showConfirm = true) {
  if (isFollowed) {
    // 已关注，显示确认框
    if (showConfirm) {
      return new Promise((resolve) => {
        wx.showModal({
          title: '取消关注',
          content: '确定要取消关注吗？',
          confirmText: '确定',
          cancelText: '取消',
          success: async (res) => {
            if (res.confirm) {
              // 用户确认取消关注
              const result = await removeFollow(targetType, targetId)
              resolve(result)
            } else {
              // 用户取消操作
              resolve({
                success: false,
                canceled: true,
                message: '已取消'
              })
            }
          },
          fail: () => {
            resolve({
              success: false,
              message: '操作失败'
            })
          }
        })
      })
    } else {
      // 不显示确认框，直接取消关注
    return await removeFollow(targetType, targetId)
    }
  } else {
    // 未关注，执行添加关注
    return await addFollow(targetType, targetId, followStatus)
  }
}

/**
 * 更新关注状态
 * @param {number} id - 关注记录ID
 * @param {number} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @param {number} followStatus - 新的关注状态
 * @returns {Promise}
 */
async function updateFollowStatus(id, targetType, targetId, followStatus) {
  try {
    const params = {
      id,
      targetType,
      targetId,
      followStatus
    }

    const res = await followApi.updateFollowStatus(params)

    if (res.data && res.data.code === 200) {
      return {
        success: true,
        message: '状态更新成功',
        data: res.data.data
      }
    } else {
      return {
        success: false,
        message: res.data?.msg || '更新失败'
      }
    }
  } catch (error) {
    console.error('更新关注状态失败:', error)
    return {
      success: false,
      message: '更新失败，请重试'
    }
  }
}

/**
 * 获取指定类型的关注状态映射（只取用于显示的必要字段）
 * @param {number} targetType - 目标类型（1-用户, 2-校友会, 3-母校, 4-商户）
 * @returns {Promise<Map>} 返回 Map<targetId, {isFollowed, followStatus}>
 */
async function getFollowStatusMap(targetType) {
  try {
    const res = await followApi.getMyFollowingList({
      page: 1,
      size: 999, // 获取所有关注
      targetType: targetType
    })

    if (res.data && res.data.code === 200) {
      const records = res.data.data?.records || []
      const followMap = new Map()

      // 只取用于显示关注状态的字段：targetId, followStatus
      records.forEach(item => {
        if (item.targetId && item.followStatus !== 4) { // 排除已取消关注的
          followMap.set(String(item.targetId), {
            isFollowed: true,
            followStatus: item.followStatus || 1
          })
        }
      })

      return followMap
    }

    return new Map()
  } catch (error) {
    console.error('获取关注状态失败:', error)
    return new Map()
  }
}

/**
 * 更新列表中的关注状态（纯函数）
 * @param {Array} list - 列表数据
 * @param {Map} followMap - 关注状态映射
 * @param {string} idKey - ID字段名，默认'id'
 * @returns {Array} 更新后的列表
 */
function updateListFollowStatus(list, followMap, idKey = 'id') {
  return list.map(item => {
    const followInfo = followMap.get(String(item[idKey]))
    if (followInfo) {
      return {
        ...item,
        isFollowed: followInfo.isFollowed,
        followStatus: followInfo.followStatus
      }
    }
    return item
  })
}

/**
 * 加载并更新页面列表的关注状态（一站式方法）
 * @param {Object} page - 页面实例(this)
 * @param {string} listKey - 列表数据键名，如'schoolList'
 * @param {number} targetType - 目标类型
 * @param {string} idKey - ID字段名，默认'id'
 */
async function loadAndUpdateFollowStatus(page, listKey, targetType, idKey = 'id') {
  try {
    // 获取关注状态映射
    const followMap = await getFollowStatusMap(targetType)

    // 更新列表数据
    const list = page.data[listKey] || []
    const updatedList = updateListFollowStatus(list, followMap, idKey)

    // 更新页面数据
    const updateData = {}
    updateData[listKey] = updatedList
    page.setData(updateData)
  } catch (error) {
    console.error('更新列表关注状态失败:', error)
    // 静默失败，不影响列表正常显示
  }
}

/**
 * 处理列表项的关注操作（一站式方法）
 * @param {Object} page - 页面实例(this)
 * @param {string} listKey - 列表数据键名，如'schoolList'
 * @param {string} itemId - 项目ID
 * @param {boolean} isFollowed - 当前是否已关注
 * @param {number} targetType - 目标类型
 * @param {string} idKey - ID字段名，默认'id'
 * @param {Function} onSuccess - 成功回调（可选）
 */
async function handleListItemFollow(page, listKey, itemId, isFollowed, targetType, idKey = 'id', onSuccess) {
  const list = page.data[listKey] || []
  const index = list.findIndex(item => String(item[idKey]) === String(itemId))

  if (index === -1) {
    console.error('未找到对应的列表项')
    return
  }

  // 调用切换关注状态（带确认框）
  const result = await toggleFollow(isFollowed, targetType, itemId, FollowStatus.NORMAL, true)

  // 如果用户取消了操作，不做处理
  if (result.canceled) {
    return
  }

  if (result.success) {
    // 更新列表中的关注状态
    list[index].isFollowed = !isFollowed
    list[index].followStatus = !isFollowed ? FollowStatus.NORMAL : FollowStatus.CANCELED

    const updateData = {}
    updateData[listKey] = list
    page.setData(updateData)

    wx.showToast({
      title: result.message,
      icon: 'success'
    })

    // 执行成功回调
    if (onSuccess && typeof onSuccess === 'function') {
      onSuccess(result)
    }
  } else {
    wx.showToast({
      title: result.message,
      icon: 'none'
    })
  }
}

/**
 * 在页面中处理关注/取消关注的通用方法（详情页使用）
 * @param {Object} page - 页面实例(this)
 * @param {string} dataKey - 数据对象在 data 中的键名，如 'userInfo', 'schoolInfo'
 * @param {string} followedKey - 是否已关注的字段名，默认为 'isFollowed'
 * @param {string} followStatusKey - 关注状态的字段名，默认为 'followStatus'
 * @param {number} targetType - 目标类型
 * @param {number} targetId - 目标ID
 * @param {boolean} showConfirm - 取消关注时是否显示确认框，默认为true
 * @param {Function} onSuccess - 成功回调（可选）
 * @param {Function} onError - 失败回调（可选）
 */
async function handleFollowInPage(page, dataKey, followedKey = 'isFollowed', followStatusKey = 'followStatus', targetType, targetId, showConfirm = true, onSuccess, onError) {
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
  const result = await toggleFollow(isFollowed, targetType, targetId, FollowStatus.NORMAL, showConfirm)

  // 如果用户取消了操作，不做任何处理
  if (result.canceled) {
    return
  }

  if (result.success) {
    // 更新页面数据
    const updateData = {}
    updateData[`${dataKey}.${followedKey}`] = !isFollowed
    // 更新关注状态：关注时设为1（正常关注），取消时设为4（已取消）
    updateData[`${dataKey}.${followStatusKey}`] = !isFollowed ? FollowStatus.NORMAL : FollowStatus.CANCELED
    page.setData(updateData)

    // 显示提示
    if (!result.canceled) {
    wx.showToast({
      title: result.message,
      icon: 'success'
    })
    }

    // 执行成功回调
    if (onSuccess && typeof onSuccess === 'function') {
      onSuccess(result)
    }
  } else {
    // 显示错误提示（排除用户主动取消的情况）
    if (!result.canceled) {
    wx.showToast({
      title: result.message,
      icon: 'none'
    })
    }

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
  updateFollowStatus,
  toggleFollow,
  getFollowStatusMap,
  updateListFollowStatus,
  loadAndUpdateFollowStatus,
  handleListItemFollow,
  handleFollowInPage
}
