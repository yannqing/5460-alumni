// pages/activity/detail/detail.js
const { activityApi, activityRegistrationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    activityId: '',
    activityInfo: null,
    loading: true,
    buttonLoading: false,
    activityImages: [],
    iconLocation: config.getIconUrl('position.png'),
    iconPhone: config.getIconUrl('电话.png'),
    // 报名相关
    myRegistration: {
      hasRegistered: false,
      registrationStatus: null,
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
    if (this.data.activityId && !this.data.loading) {
      this.refreshRegistrationState()
    }
  },

  async loadActivityDetail() {
    try {
      const { activityId } = this.data

      // 检查activityId是否存在
      if (!activityId || activityId === 'undefined') {
        this.setData({ loading: false })
        wx.showToast({
          title: '活动ID无效',
          icon: 'none',
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
            const coverImage = activityData.coverImage
              ? activityData.coverImage.replace(/[`\s]/g, '')
              : ''
            activityImages = cleanedImages.filter(img => img !== coverImage)
          }
        }

        // 处理时间显示，移除 'T'
        if (activityData.startTime) {
          activityData.startTime = activityData.startTime.replace('T', ' ')
        }
        if (activityData.endTime) {
          activityData.endTime = activityData.endTime.replace('T', ' ')
        }
        if (activityData.registrationStartTime) {
          activityData.registrationStartTime = activityData.registrationStartTime.replace('T', ' ')
        }
        if (activityData.registrationEndTime) {
          activityData.registrationEndTime = activityData.registrationEndTime.replace('T', ' ')
        }

        this.setData({
          activityInfo: activityData,
          activityImages: activityImages,
          loading: false,
        })

        // 详情加载完后并行拉取报名状态与参与者
        if (activityData.isSignup === 1) {
          this.refreshRegistrationState()
          this.loadParticipants()
        }
      } else {
        console.error('[ActivityDetail] 接口返回错误:', res.data?.code, res.data?.msg)
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[ActivityDetail] 获取活动详情失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'none',
      })
    }
  },

  // 一键导航
  openLocation() {
    const { activityInfo } = this.data
    if (!activityInfo) {
      wx.showToast({
        title: '地址信息不完整',
        icon: 'none',
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

  // 拉取当前用户在该活动中的报名状态
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
          participantsTotal:
            (this.data.activityInfo && this.data.activityInfo.currentParticipants) ||
            participants.length,
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
    if (myRegistration.hasRegistered) {
      const status = myRegistration.registrationStatus
      if (status === 0) {
        this.setData({ signupBtn: { visible: true, disabled: true, text: '审核中' } })
        return
      }
      if (status === 1) {
        this.setData({
          signupBtn: { visible: true, disabled: false, text: '已报名（点击取消）' },
        })
        return
      }
      if (status === 2) {
        this.setData({
          signupBtn: { visible: true, disabled: false, text: '申请被拒，重新报名' },
        })
        return
      }
    }
    const max = activityInfo.maxParticipants
    const current = activityInfo.currentParticipants || 0
    if (max && current >= max) {
      this.setData({ signupBtn: { visible: true, disabled: true, text: '名额已满' } })
      return
    }
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

  // 报名按钮点击：根据当前状态分发
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
    const { activityInfo } = this.data
    return {
      title: activityInfo?.activityTitle || '活动详情',
      path: `/pages/activity/detail/detail?id=${this.data.activityId}`,
    }
  },
})
