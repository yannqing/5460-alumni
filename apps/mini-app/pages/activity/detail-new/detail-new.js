// pages/activity/detail-new/detail-new.js
const app = getApp()
const config = require('../../../utils/config.js')
const { alumniAssociationManagementApi, activityRegistrationApi } = require('../../../api/api.js')

Page({
  data: {
    activityId: '',
    activityInfo: {},
    activityImages: [],
    loading: true,
    error: '',
    canJoinActivity: true,
    // 报名相关
    myRegistration: {
      hasRegistered: false,
      registrationStatus: null, // 0-待审 1-通过 2-拒绝 3-已取消
      registrationId: '',
      auditReason: '',
    },
    signupBtn: {
      visible: false,
      disabled: false,
      text: '立即报名',
    },
    submittingSignup: false,
    // 已报名校友（隐私过滤后）
    participants: [],
    participantsTotal: 0,
    defaultUserAvatarUrl: config.defaultAvatar,
  },

  onLoad(options) {
    const { id } = options
    this.setData({ activityId: id })
    this.loadActivityDetail()
  },

  onShow() {
    // 从子页面返回时刷新报名状态
    if (this.data.activityId && !this.data.loading) {
      this.refreshRegistrationState()
    }
  },

  async loadActivityDetail() {
    const { activityId } = this.data

    if (!activityId) {
      this.setData({
        loading: false,
        error: '活动ID无效',
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
            activityImages = imagesArray
              .map(img => {
                const cleaned = typeof img === 'string' ? img.replace(/[`\s]/g, '') : img
                console.log('[ActivityDetail] 清理后的单个URL:', cleaned)
                return cleaned
              })
              .filter(url => url && url.startsWith('http')) // 过滤掉无效的URL

            console.log('[ActivityDetail] 最终的图片数组:', activityImages)
          }
        }

        console.log('[ActivityDetail] 最终要显示的图片数组:', activityImages)

        // 格式化时间字段
        if (activityData.startTime) {
          activityData.startTime = this.formatDateTime(activityData.startTime)
        }
        if (activityData.endTime) {
          activityData.endTime = this.formatDateTime(activityData.endTime)
        }
        if (activityData.registrationStartTime) {
          activityData.registrationStartTime = this.formatDateTime(
            activityData.registrationStartTime
          )
        }
        if (activityData.registrationEndTime) {
          activityData.registrationEndTime = this.formatDateTime(activityData.registrationEndTime)
        }

        this.setData({
          activityInfo: activityData,
          activityImages: activityImages,
          loading: false,
          canJoinActivity: activityData.status === 1,
        })

        // 详情加载完后并行拉取报名状态与参与者
        if (activityData.isSignup === 1) {
          this.refreshRegistrationState()
          this.loadParticipants()
        }
      } else {
        console.error('[ActivityDetail] 接口返回错误:', res.data?.code, res.data?.msg)
        this.setData({
          loading: false,
          error: res.data?.msg || '加载失败',
        })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ActivityDetail] 获取活动详情失败:', error)
      this.setData({
        loading: false,
        error: '加载失败，请稍后重试',
      })
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      })
    }
  },

  // 调用活动详情接口
  getActivityDetail(activityId) {
    return alumniAssociationManagementApi.getActivityDetail(activityId)
  },

  // 格式化日期时间
  formatDateTime(dateTimeString) {
    if (!dateTimeString) {
      return ''
    }
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
      5: '已取消',
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
      5: '门店',
    }
    return typeMap[organizerType] || ''
  },

  // 一键导航
  openLocation() {
    const { activityInfo } = this.data
    if (!activityInfo || !activityInfo.latitude || !activityInfo.longitude) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none',
      })
      return
    }

    wx.openLocation({
      latitude: activityInfo.latitude,
      longitude: activityInfo.longitude,
      name: activityInfo.activityTitle,
      address:
        activityInfo.province + activityInfo.city + activityInfo.district + activityInfo.address,
      success: () => {
        console.log('打开地图成功')
      },
      fail: () => {
        wx.showToast({
          title: '打开地图失败',
          icon: 'none',
        })
      },
    })
  },

  // 拨打电话
  makeCall() {
    const { activityInfo } = this.data
    if (activityInfo.contactPhone) {
      wx.makePhoneCall({
        phoneNumber: activityInfo.contactPhone,
      })
    } else {
      wx.showToast({
        title: '暂无联系电话',
        icon: 'none',
      })
    }
  },

  // 拉取当前用户在该活动中的报名状态，并刷新按钮文案
  async refreshRegistrationState() {
    const { activityId } = this.data
    if (!activityId) return
    try {
      const res = await activityRegistrationApi.getMyStatus(activityId)
      if (res.data && res.data.code === 200 && res.data.data) {
        const status = res.data.data
        this.setData({
          myRegistration: {
            hasRegistered: !!status.hasRegistered,
            registrationStatus:
              status.registrationStatus !== undefined ? status.registrationStatus : null,
            registrationId: status.registrationId || '',
            auditReason: status.auditReason || '',
          },
        })
        this.computeSignupBtn()
      }
    } catch (error) {
      console.error('[ActivityDetail] 拉取报名状态失败:', error)
    }
  },

  // 点击参与者头像，跳转到用户详情页
  goToParticipantDetail(e) {
    const { userId } = e.currentTarget.dataset
    if (!userId) return
    wx.navigateTo({ url: `/pages/alumni/detail/detail?id=${userId}` })
  },

  // 拉取已通过审核的参与者
  async loadParticipants() {
    const { activityId } = this.data
    if (!activityId) return
    try {
      const res = await activityRegistrationApi.getParticipants(activityId, 6)
      if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
        const participants = res.data.data.map(item => ({
          ...item,
          avatarUrl: item.avatarUrl ? config.getImageUrl(item.avatarUrl) : config.defaultAvatar,
        }))
        this.setData({
          participants,
          // 总数取已加载数与活动当前参与人数的较大值，已审核计数以 currentParticipants 为准
          participantsTotal: this.data.activityInfo.currentParticipants || participants.length,
        })
      }
    } catch (error) {
      console.error('[ActivityDetail] 拉取参与者失败:', error)
    }
  },

  // 根据活动信息与当前用户报名状态计算按钮文案
  computeSignupBtn() {
    const { activityInfo, myRegistration } = this.data
    if (!activityInfo || activityInfo.isSignup !== 1) {
      this.setData({ signupBtn: { visible: false, disabled: true, text: '' } })
      return
    }
    // 已有有效报名
    if (myRegistration.hasRegistered) {
      const status = myRegistration.registrationStatus
      if (status === 0) {
        this.setData({ signupBtn: { visible: true, disabled: true, text: '审核中' } })
        return
      }
      if (status === 1) {
        this.setData({ signupBtn: { visible: true, disabled: false, text: '已报名（点击取消）' } })
        return
      }
      if (status === 2) {
        // 被拒，可以重新报名
        this.setData({
          signupBtn: { visible: true, disabled: false, text: '申请被拒，重新报名' },
        })
        return
      }
      // 已取消（3）或其他，回到默认可报名状态
    }
    // 名额校验
    const max = activityInfo.maxParticipants
    const current = activityInfo.currentParticipants || 0
    if (max && current >= max) {
      this.setData({ signupBtn: { visible: true, disabled: true, text: '名额已满' } })
      return
    }
    // 报名时间窗校验
    const now = Date.now()
    const regStart = activityInfo.registrationStartTime
      ? new Date(activityInfo.registrationStartTime.replace(' ', 'T')).getTime()
      : null
    const regEnd = activityInfo.registrationEndTime
      ? new Date(activityInfo.registrationEndTime.replace(' ', 'T')).getTime()
      : null
    if (regStart && now < regStart) {
      this.setData({ signupBtn: { visible: true, disabled: true, text: '报名未开始' } })
      return
    }
    if (regEnd && now > regEnd) {
      this.setData({ signupBtn: { visible: true, disabled: true, text: '报名已截止' } })
      return
    }
    this.setData({ signupBtn: { visible: true, disabled: false, text: '立即报名' } })
  },

  // 按钮点击：根据当前状态分发
  onSignupBtnTap() {
    const { myRegistration, signupBtn } = this.data
    if (signupBtn.disabled) return
    if (myRegistration.hasRegistered && myRegistration.registrationStatus === 1) {
      this.confirmCancelRegistration()
      return
    }
    this.confirmSignup()
  },

  // 报名前二次确认（以登录用户的真实姓名报名）
  confirmSignup() {
    const { activityInfo } = this.data
    const needReview =
      activityInfo && activityInfo.isNeedReview === 1 ? '提交后需主办方审核通过才算报名成功。' : ''
    wx.showModal({
      title: '确认报名',
      content: `报名将以您的真实姓名进行，请确认是否继续？${needReview}`,
      confirmText: '确认报名',
      success: async res => {
        if (res.confirm) {
          await this.submitSignup()
        }
      },
    })
  },

  // 提交报名（姓名/手机号由服务端从登录用户资料中取）
  async submitSignup() {
    if (this.data.submittingSignup) return
    const { activityId } = this.data

    this.setData({ submittingSignup: true })
    try {
      const res = await activityRegistrationApi.apply({ activityId })
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '报名提交成功', icon: 'success' })
        await this.refreshRegistrationState()
        await this.loadActivityDetail()
        await this.loadParticipants()
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '报名失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('[ActivityDetail] 报名失败:', error)
      wx.showToast({ title: '报名失败，请稍后重试', icon: 'none' })
    } finally {
      this.setData({ submittingSignup: false })
    }
  },

  // 取消报名前二次确认
  confirmCancelRegistration() {
    wx.showModal({
      title: '取消报名',
      content: '确定要取消本次报名吗？',
      success: async res => {
        if (res.confirm) {
          await this.doCancelRegistration()
        }
      },
    })
  },

  async doCancelRegistration() {
    const { myRegistration } = this.data
    if (!myRegistration.registrationId) return
    try {
      const res = await activityRegistrationApi.cancel(myRegistration.registrationId)
      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '已取消报名', icon: 'success' })
        await this.refreshRegistrationState()
        await this.loadActivityDetail()
        await this.loadParticipants()
      } else {
        wx.showToast({
          title: (res.data && res.data.msg) || '取消失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('[ActivityDetail] 取消报名失败:', error)
      wx.showToast({ title: '取消失败，请稍后重试', icon: 'none' })
    }
  },

  onShareAppMessage() {
    const { activityInfo, activityId } = this.data
    return {
      title: activityInfo.activityTitle || '活动详情',
      path: `/pages/activity/detail-new/detail-new?id=${activityId}`,
    }
  },
})
