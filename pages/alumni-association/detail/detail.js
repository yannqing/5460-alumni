// pages/alumni-association/detail/detail.js
const { associationApi } = require('../../../api/api.js')

Page({
  data: {
    associationId: '',
    associationInfo: null,
    activeTab: 0,
    tabs: ['基本信息', '成员列表'],
    members: [],
    activities: [],
    notifications: [],
    benefitActivities: [],
    nearbyBenefits: [],
    alumniEnterprises: [],
    alumniShops: [],
    loading: false,
    // 测试用：校友总会 ID（后续可由接口返回 unionId 替换）
    testUnionId: 1,
    // 加入校友会申请表单
    showJoinModal: false,
    joinForm: {
      realName: '',
      graduationYear: '',
      major: '',
      remark: ''
    },
    joinSubmitting: false
  },

  async onLoad(options) {
    this.setData({ associationId: options.id })
    // 确保已登录后再加载数据
    await this.ensureLogin()
    this.loadAssociationDetail()
  },

  onShow() {
    // 页面显示时重新检查登录状态
    this.ensureLogin().then(() => {
      // 如果详情为空，重新加载
      if (!this.data.associationInfo && this.data.associationId) {
        this.loadAssociationDetail()
      }
    })
  },

  // 确保已登录
  async ensureLogin() {
    const app = getApp()
    const isLogin = app.checkHasLogined()
    
    if (!isLogin) {
      try {
        await app.initApp()
      } catch (error) {
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
        throw error
      }
    }
  },

  // 加载校友会详情
  async loadAssociationDetail() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await associationApi.getAssociationDetail(this.data.associationId)
      
      if (res.data && res.data.code === 200) {
        const item = res.data.data || {}

        // 数据映射（与后端字段保持同步）
        // 后端字段示例（AlumniAssociationListVo）：
        // 实际返回结构中，ID 信息分散在不同对象中：
        // - 校友会主键：alumni_association_id（后端未在 VO 中显式暴露，前端使用请求时的 id）
        // - 母校 ID：data.schoolInfo.schoolId
        // - 校处会 ID：目前 VO 中未暴露，前端暂不直接使用
        const schoolInfo = item.schoolInfo || {}
        const platformInfo = item.platform || {}

        const mappedInfo = {
          // 使用前端当前请求的 id 作为校友会 ID，避免依赖后端未暴露字段
          id: this.data.associationId,
          associationId: this.data.associationId,
          name: item.associationName,
          associationName: item.associationName,
          // 从 schoolInfo 中读取真正的母校 ID，避免为 null/undefined
          schoolId: schoolInfo.schoolId || null,
          // 平台 ID 后端当前未直接返回，这里预留字段，兼容后续扩展
          platformId: platformInfo.platformId || platformInfo.id || null,
          presidentUserId: item.presidentUserId,
          icon: '/assets/logo/njdxxyh.jpg', // 后端暂无图标字段，使用默认
          cover: '/assets/images/南京大学背景图.jpg', // 后端暂无封面字段，使用默认
          location: item.location || '',
          memberCount: item.memberCount || 0,
          contactInfo: item.contactInfo || '',
          // 预留字段（后端暂无，使用默认值）
          schoolName: schoolInfo.schoolName || '', // 优先使用返回的学校名称
          address: item.location || '', // 使用 location 作为地址
          isJoined: false, // 后端暂无此字段
          isCertified: false, // 后端暂无此字段
          president: '', // 需要根据 presidentUserId 查询，或后端返回
          vicePresidents: [], // 后端暂无此字段
          establishedYear: null, // 后端暂无此字段
          description: '', // 后端暂无此字段
          certifications: [] // 后端暂无此字段
        }

        this.setData({
          associationInfo: mappedInfo,
          loading: false
        })

        // 加载其他相关数据（成员、活动等，这些接口可能还未实现）
        // this.loadMembers()
        // this.loadActivities()
        // this.loadNotifications()
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
  },

  // 点击加入/退出按钮
  toggleJoin() {
    const { associationInfo } = this.data
    // 已加入：保持原来的退出逻辑
    if (associationInfo.isJoined) {
      associationInfo.isJoined = false
      this.setData({ associationInfo })
      wx.showToast({
        title: '已退出',
        icon: 'success'
      })
      return
    }
    // 未加入：打开申请表单
    this.setData({
      showJoinModal: true,
      joinForm: {
        realName: '',
        graduationYear: '',
        major: '',
        remark: ''
      }
    })
  },

  // 关闭申请弹窗
  closeJoinModal() {
    if (this.data.joinSubmitting) return
    this.setData({ showJoinModal: false })
  },

  // 表单输入绑定
  handleJoinInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      joinForm: {
        ...this.data.joinForm,
        [field]: value
      }
    })
  },

  // 提交加入申请
  async submitJoinApplication() {
    const { associationId, joinForm, joinSubmitting, associationInfo } = this.data
    if (joinSubmitting) return

    if (!joinForm.realName) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' })
      return
    }

    this.setData({ joinSubmitting: true })
    try {
      // 当前后端 join 接口未要求表单字段，这里先直接提交申请
      const res = await associationApi.joinAssociation(associationId)
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '申请已提交，待审核',
          icon: 'success'
        })
        this.setData({
          showJoinModal: false,
          associationInfo: {
            ...associationInfo,
            isJoined: true
          }
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '提交失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ joinSubmitting: false })
    }
  },

  // 查看通知详情
  viewNotificationDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/notification/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/notification/detail/detail?id=${id}`
      })
    }
  },

  // 查看活动详情
  viewActivityDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail/detail?id=${id}`
    })
  },

  // 查看权益详情
  viewBenefitDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/benefit/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/benefit/detail/detail?id=${id}`
      })
    }
  },

  // 认证标签点击
  handleCertificationTap(e) {
    const { type, id } = e.currentTarget.dataset
    if (!type || !id) return

    if (type === 'union') {
      wx.navigateTo({
        url: `/pages/alumni-union/detail/detail?id=${id}`
      })
    } else if (type === 'platform') {
      wx.navigateTo({
        url: `/pages/local-platform/detail/detail?id=${id}`
      })
    } else if (type === 'promotion') {
      wx.navigateTo({
        url: `/pages/promotion/detail/detail?id=${id}`
      })
    }
  },

  // 查看母校详情
  viewSchoolDetail() {
    const { associationInfo } = this.data
    if (!associationInfo || !associationInfo.schoolId) {
      wx.showToast({
        title: '暂无母校信息',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${associationInfo.schoolId}`
    })
  },

  // 查看校友企业详情
  viewEnterpriseDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/enterprise/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/enterprise/detail/detail?id=${id}`
      })
    }
  },

  // 查看校友商铺详情
  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id === 'all') {
      wx.navigateTo({
        url: `/pages/shop/list/list?associationId=${this.data.associationId}`
      })
    } else {
      wx.navigateTo({
        url: `/pages/shop/detail/detail?id=${id}`
      })
    }
  }
})
