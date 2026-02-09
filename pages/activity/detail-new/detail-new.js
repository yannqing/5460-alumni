// pages/activity/detail-new/detail-new.js
const app = getApp()

Page({
  data: {
    activityId: '',
    activityInfo: {},
    activityImages: [],
    loading: true,
    error: '',
    canJoinActivity: true
  },

  onLoad(options) {
    const { id } = options
    this.setData({ activityId: id })
    this.loadActivityDetail()
  },

  async loadActivityDetail() {
    const { activityId } = this.data
    
    if (!activityId) {
      this.setData({
        loading: false,
        error: '活动ID无效'
      })
      return
    }

    try {
      this.setData({ loading: true, error: '' })
      wx.showLoading({ title: '加载中...' })
      
      // 调用 /alumniAssociationManagement/activity/detail/{activityId} 接口
      const res = await this.getActivityDetail(activityId)
      wx.hideLoading()

      console.log('[ActivityDetail] 活动详情响应:', res)

      if (res.data && (res.data.code === 0 || res.data.code === 200) && res.data.data) {
        const activityData = res.data.data
        
        // 处理活动图片
        let activityImages = []
        if (activityData.activityImages) {
          let imagesArray = []
          
          // 处理字符串类型的activityImages
          if (typeof activityData.activityImages === 'string') {
            // 去除反引号
            let cleanStr = activityData.activityImages.replace(/[`]/g, '')
            // 去除开头和结尾的引号（如果存在）
            if (cleanStr.startsWith('"') && cleanStr.endsWith('"')) {
              cleanStr = cleanStr.substring(1, cleanStr.length - 1)
            }
            // 去除多余的空格，但保留URL内部的空格
            cleanStr = cleanStr.replace(/\s*([\[\]\",])\s*/g, '$1')
            
            console.log('[ActivityDetail] 清理后的activityImages:', cleanStr)
            
            // 检查是否是JSON数组格式
            if (cleanStr.startsWith('[') && cleanStr.endsWith(']')) {
              try {
                // 尝试解析为JSON数组
                imagesArray = JSON.parse(cleanStr)
                console.log('[ActivityDetail] 解析成功的图片数组:', imagesArray)
              } catch (e) {
                console.error('[ActivityDetail] JSON解析失败:', e)
                // 解析失败，尝试手动提取URL
                try {
                  // 提取所有URL
                  const urlRegex = /https?:\/\/[^"'\s,]+/g
                  const urls = cleanStr.match(urlRegex) || []
                  imagesArray = urls
                  console.log('[ActivityDetail] 手动提取的URL数组:', imagesArray)
                } catch (e2) {
                  console.error('[ActivityDetail] 手动提取失败:', e2)
                  // 作为单个URL处理
                  imagesArray = [cleanStr]
                }
              }
            } else {
              // 不是数组格式，检查是否包含多个URL
              try {
                const urlRegex = /https?:\/\/[^"'\s,]+/g
                const urls = cleanStr.match(urlRegex) || []
                if (urls.length > 0) {
                  imagesArray = urls
                  console.log('[ActivityDetail] 从非数组字符串中提取的URL数组:', imagesArray)
                } else {
                  // 作为单个URL处理
                  imagesArray = [cleanStr]
                }
              } catch (e) {
                // 作为单个URL处理
                imagesArray = [cleanStr]
              }
            }
          } else if (Array.isArray(activityData.activityImages)) {
            // 已经是数组，直接使用
            imagesArray = activityData.activityImages
          }
          
          // 处理图片URL数组
          if (imagesArray.length > 0) {
            // 去除图片URL中的反引号和空格
            activityImages = imagesArray.map(img => {
              const cleaned = typeof img === 'string' ? img.replace(/[`\s]/g, '') : img
              console.log('[ActivityDetail] 清理后的单个URL:', cleaned)
              return cleaned
            }).filter(url => url && url.startsWith('http')) // 过滤掉无效的URL
            
            console.log('[ActivityDetail] 最终的图片数组:', activityImages)
          }
        }
        
        console.log('[ActivityDetail] 最终要显示的图片数组:', activityImages)

        this.setData({
          activityInfo: activityData,
          activityImages: activityImages,
          loading: false,
          // 判断是否可以报名：只有当活动状态为1（报名中）时才允许报名
          // 注意：目前"已报名"状态是基于活动状态判断的临时方案
          // 理想情况下，应该根据用户是否实际报名来判断，需要额外接口获取用户报名状态
          canJoinActivity: activityData.status === 1
        })
      } else {
        console.error('[ActivityDetail] 接口返回错误:', res.data?.code, res.data?.msg)
        this.setData({
          loading: false,
          error: res.data?.msg || '加载失败'
        })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ActivityDetail] 获取活动详情失败:', error)
      this.setData({
        loading: false,
        error: '加载失败，请稍后重试'
      })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 调用活动详情接口
  getActivityDetail(activityId) {
    return new Promise((resolve, reject) => {
      // 获取 token
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }

      const headers = {
        'Content-Type': 'application/json'
      }

      if (token) {
        headers.token = token
        headers['x-token'] = token
      }

      wx.request({
        url: `${app.globalData.baseUrl}/alumniAssociationManagement/activity/detail/${activityId}`,
        method: 'GET',
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  },

  // 格式化日期时间
  formatDateTime(dateTimeString) {
    if (!dateTimeString) return ''
    try {
      const date = new Date(dateTimeString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    } catch (error) {
      console.error('[formatDateTime] 时间格式错误:', error)
      return dateTimeString
    }
  },

  // 获取活动状态文本
  getStatusText(status) {
    const statusMap = {
      0: '草稿',
      1: '报名中',
      2: '报名结束',
      3: '进行中',
      4: '已结束',
      5: '已取消'
    }
    return statusMap[status] || ''
  },

  // 获取主办方类型文本
  getOrganizerTypeText(organizerType) {
    const typeMap = {
      1: '校友会',
      2: '校促会',
      3: '商铺',
      4: '母校',
      5: '门店'
    }
    return typeMap[organizerType] || ''
  },

  // 一键导航
  openLocation() {
    const { activityInfo } = this.data
    if (!activityInfo || !activityInfo.latitude || !activityInfo.longitude) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none'
      })
      return
    }

    wx.openLocation({
      latitude: activityInfo.latitude,
      longitude: activityInfo.longitude,
      name: activityInfo.activityTitle,
      address: activityInfo.province + activityInfo.city + activityInfo.district + activityInfo.address,
      success: () => {
        console.log('打开地图成功')
      },
      fail: () => {
        wx.showToast({
          title: '打开地图失败',
          icon: 'none'
        })
      }
    })
  },

  // 拨打电话
  makeCall() {
    const { activityInfo } = this.data
    if (activityInfo.contactPhone) {
      wx.makePhoneCall({
        phoneNumber: activityInfo.contactPhone
      })
    } else {
      wx.showToast({
        title: '暂无联系电话',
        icon: 'none'
      })
    }
  },

  // 报名活动
  joinActivity() {
    const { activityInfo } = this.data
    
    if (!activityInfo.isSignup || activityInfo.isSignup === 0) {
      wx.showToast({
        title: '该活动无需报名',
        icon: 'none'
      })
      return
    }

    // 这里可以实现报名功能
    wx.showToast({
      title: '报名功能开发中',
      icon: 'none'
    })
  },

  onShareAppMessage() {
    const { activityInfo, activityId } = this.data
    return {
      title: activityInfo.activityTitle || '活动详情',
      path: `/pages/activity/detail-new/detail-new?id=${activityId}`
    }
  }
})