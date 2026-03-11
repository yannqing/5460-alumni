// pages/audit/alumni-association/detail/detail.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { associationApi } = require('../../../../api/api.js')

Page({
  data: {
    application: null,
    loading: true,
    statusClass: '',
    applicationStatusText: '',
    initialMembersList: [], // 解析后的初始成员列表
    defaultAlumniAvatar: config.defaultAvatar,
    defaultBackground: '/assets/icons/background.png'
  },

  onLoad(options) {
    const { id } = options
    if (id) {
      this.loadApplicationDetail(id)
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  async loadApplicationDetail(applicationId) {
    try {
      this.setData({ loading: true })

      // 调用获取校友会申请详情的接口
      const res = await associationApi.getApplicationDetail(applicationId)

      if (res.data && res.data.code === 200) {
        let application = res.data.data

        // 处理状态文本和样式
        const statusClass = this.getStatusClass(application.applicationStatus)
        const applicationStatusText = this.getApplicationStatusText(application.applicationStatus)

        // 处理背景图（可能是JSON数组格式）
        if (application.bgImg && application.bgImg.trim()) {
          try {
            const bgImgArr = JSON.parse(application.bgImg)
            if (Array.isArray(bgImgArr) && bgImgArr.length > 0) {
              application.bgImg = config.getImageUrl(bgImgArr[0])
            } else {
              application.bgImg = config.getImageUrl(application.bgImg)
            }
          } catch (e) {
            application.bgImg = config.getImageUrl(application.bgImg)
          }
        } else {
          application.bgImg = null
        }

        // 处理 Logo
        if (application.logo && application.logo.trim()) {
          application.logo = config.getImageUrl(application.logo)
        }

        // 解析初始成员列表（JSON格式字符串）
        let initialMembersList = []
        if (application.initialMembers) {
          try {
            initialMembersList = JSON.parse(application.initialMembers)
            if (!Array.isArray(initialMembersList)) {
              initialMembersList = []
            }
          } catch (e) {
            console.error('解析初始成员列表失败:', e)
            initialMembersList = []
          }
        }

        // 格式化时间
        if (application.applyTime) {
          application.applyTimeFormatted = this.formatDateTime(application.applyTime)
        }
        if (application.reviewTime) {
          application.reviewTimeFormatted = this.formatDateTime(application.reviewTime)
        }
        if (application.createTime) {
          application.createTimeFormatted = this.formatDateTime(application.createTime)
        }
        if (application.updateTime) {
          application.updateTimeFormatted = this.formatDateTime(application.updateTime)
        }

        this.setData({
          application,
          statusClass,
          applicationStatusText,
          initialMembersList,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '获取详情失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('获取校友会申请详情失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 格式化时间
  formatDateTime(dateTime) {
    if (!dateTime) return ''
    // 处理 ISO 格式或其他格式
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) {
      // 如果解析失败，尝试直接返回字符串格式
      return dateTime.replace('T', ' ').substring(0, 19)
    }
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },

  // 获取申请状态样式类
  getStatusClass(status) {
    switch (status) {
      case 0:
        return 'status-pending'
      case 1:
        return 'status-approved'
      case 2:
        return 'status-rejected'
      case 3:
        return 'status-cancelled'
      default:
        return ''
    }
  },

  // 获取申请状态文本
  getApplicationStatusText(status) {
    switch (status) {
      case 0:
        return '待审核'
      case 1:
        return '已通过'
      case 2:
        return '已拒绝'
      case 3:
        return '已撤销'
      default:
        return '未知'
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack({ delta: 1 })
  }
})
