// pages/activity/detail/detail.js
const { activityApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    activityId: '',
    activityInfo: null,
    loading: true,
    buttonLoading: false,
    activityImages: [],
    iconLocation: config.getIconUrl('position.png'),
    iconPhone: config.getIconUrl('电话.png')
  },

  onLoad(options) {
    const { id } = options
    this.setData({ activityId: id })
    this.loadActivityDetail()
  },

  async loadActivityDetail() {
    try {
      const { activityId } = this.data
      
      // 检查activityId是否存在
      if (!activityId || activityId === 'undefined') {
        this.setData({ loading: false })
        wx.showToast({
          title: '活动ID无效',
          icon: 'none'
        })
        return
      }
      
      this.setData({ loading: true })
      wx.showLoading({ title: '加载中...' })
      
      // 调用活动详情接口 /activity/{activityId}
      const res = await activityApi.getActivityDetail(activityId)
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
            // 去除反引号和空格
            const cleanStr = activityData.activityImages.replace(/[`\s]/g, '')
            
            // 检查是否是JSON数组格式
            if (cleanStr.startsWith('[') && cleanStr.endsWith(']')) {
              try {
                // 尝试解析为JSON数组
                imagesArray = JSON.parse(cleanStr)
              } catch (e) {
                // 解析失败，作为单个URL处理
                imagesArray = [cleanStr]
              }
            } else {
              // 不是数组格式，作为单个URL处理
              imagesArray = [cleanStr]
            }
          } else if (Array.isArray(activityData.activityImages)) {
            // 已经是数组，直接使用
            imagesArray = activityData.activityImages
          }
          
          // 处理图片URL数组
          if (imagesArray.length > 0) {
            // 去除图片URL中的反引号和空格
            const cleanedImages = imagesArray.map(img => {
              return typeof img === 'string' ? img.replace(/[`\s]/g, '') : img
            })
            
            // 过滤掉与封面图重复的图片
            const coverImage = activityData.coverImage ? activityData.coverImage.replace(/[`\s]/g, '') : ''
            activityImages = cleanedImages.filter(img => img !== coverImage)
          }
        }

        this.setData({
          activityInfo: activityData,
          activityImages: activityImages,
          loading: false
        })
      } else {
        console.error('[ActivityDetail] 接口返回错误:', res.data?.code, res.data?.msg)
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ActivityDetail] 获取活动详情失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  joinActivity() {
    // 这里可以实现报名功能
    wx.showToast({
      title: '报名功能开发中',
      icon: 'none'
    })
  },

  // 一键导航
  openLocation() {
    const { activityInfo } = this.data
    if (!activityInfo) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none'
      })
      return
    }

    wx.openLocation({
      latitude: activityInfo.latitude || 0,
      longitude: activityInfo.longitude || 0,
      name: activityInfo.activityTitle,
      address: activityInfo.address || '',
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

  onShareAppMessage() {
    const { activityInfo } = this.data
    return {
      title: activityInfo?.activityTitle || '活动详情',
      path: `/pages/activity/detail/detail?id=${this.data.activityId}`
    }
  }
})
