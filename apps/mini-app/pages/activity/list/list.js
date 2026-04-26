// pages/activity/list/list.js
const app = getApp()
const { associationApi, alumniAssociationManagementApi, userApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    searchValue: '',
    filterStatus: 'all',
    filterOptions: [
      { id: 'all', label: '全部' },
      { id: 'upcoming', label: '即将开始' },
      { id: 'ongoing', label: '进行中' },
      { id: 'finished', label: '已结束' },
    ],
    activityList: [],
    displayList: [],
    stats: {
      total: 0,
      upcoming: 0,
    },
    // 校友会选择相关
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    selectedOrganizeId: 0, // 存储选中的organizeId
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份
    scrollListHeight: 400, // 下方卡片内 scroll-view 高度(px)，onLoad 中按屏幕计算
    defaultUserAvatarUrl: config.defaultAvatar, // 选择校友会时的默认 logo
  },

  onLoad() {
    this.setScrollListHeight()
    this.initPage()
  },

  onShow() {
    // 每次进入页面时，如果有选中的校友会，则刷新活动列表
    if (this.data.selectedAlumniAssociationId) {
      console.log('[Debug] onShow: 刷新活动列表')
      this.loadActivityList()
    }
  },

  // 计算下方卡片内列表可滚动区域高度（scroll-view 必须明确高度才能滚动）
  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      // 预留顶部校友会选择卡片（约200rpx）、活动标题区域（约120rpx）、以及内边距
      // 使用更大的比例（0.7）让列表占据更多空间
      const scrollH = Math.floor(contentH * 0.7)
      this.setData({ scrollListHeight: scrollH > 300 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  // 初始化页面数据
  async initPage() {
    await this.loadAlumniAssociationList()
  },

  // 加载校友会列表（调用接口获取用户管理的校友会组织）
  async loadAlumniAssociationList() {
    try {
      console.log('[Debug] 开始加载校友会列表')

      // 调用接口获取用户管理的组织列表，type=0 表示校友会
      const res = await userApi.getManagedOrganizations({ type: 0 })
      console.log('[Debug] 获取用户管理的校友会列表:', res)

      if (res.data && res.data.code === 200) {
        const organizationList = res.data.data || []
        console.log('[Debug] 接口返回的组织列表:', organizationList)

        // 设置是否有校友会管理员身份
        this.setData({
          hasAlumniAdminPermission: organizationList.length > 0,
        })

        if (organizationList.length > 0) {
          // 将接口返回的数据映射为页面需要的格式
          const alumniAssociationList = organizationList.map(org => {
            // 处理logo头像
            let logo = org.logo || ''
            if (logo && !logo.startsWith('http://') && !logo.startsWith('https://')) {
              logo = config.getImageUrl(logo)
            }

            return {
              id: org.id,
              alumniAssociationId: org.id,
              alumniAssociationName: org.name || '校友会',
              organizeId: org.id,
              logo: logo,
              location: org.location || '',
              type: org.type,
            }
          })

          // 设置校友会列表
          this.setData({
            alumniAssociationList: alumniAssociationList,
          })
          console.log('[Debug] 最终校友会列表:', alumniAssociationList)

          // 判断权限数量，处理自动选择逻辑
          this.handleAlumniAssociationSelection(alumniAssociationList)
        } else {
          // 没有找到管理的校友会
          console.warn('[Debug] 用户没有管理的校友会')
          this.setData({
            alumniAssociationList: [],
            hasAlumniAdminPermission: false,
          })
        }
      } else {
        console.error('[Debug] 获取校友会列表接口调用失败:', res)
        this.setData({
          alumniAssociationList: [],
          hasAlumniAdminPermission: false,
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校友会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        alumniAssociationList: [],
        hasAlumniAdminPermission: false,
      })
    }
  },

  // 处理校友会选择逻辑
  async handleAlumniAssociationSelection(alumniAssociationList) {
    if (alumniAssociationList.length === 1) {
      // 只有一个校友会权限，自动选择并禁用选择器
      const singleAlumni = alumniAssociationList[0]
      this.setData({
        selectedAlumniAssociationId: singleAlumni.alumniAssociationId,
        selectedAlumniAssociationName: singleAlumni.alumniAssociationName,
        selectedOrganizeId: singleAlumni.alumniAssociationId,
        hasSingleAlumniAssociation: true,
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      // 加载该校友会的活动列表
      await this.loadActivityList()
    } else if (alumniAssociationList.length > 1) {
      // 多个校友会权限，正常显示选择器
      this.setData({
        hasSingleAlumniAssociation: false,
      })
      console.log('[Debug] 有多个校友会权限，正常显示选择器')
    } else {
      // 没有校友会权限
      this.setData({
        hasSingleAlumniAssociation: false,
      })
      console.log('[Debug] 没有校友会权限')
      // 清空活动列表
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0,
        },
      })
    }
  },

  // 显示校友会选择器
  showAlumniAssociationSelector() {
    this.setData({ showAlumniAssociationPicker: false })
    this.setData({ showAlumniAssociationPicker: true })
  },

  // 选择校友会
  async selectAlumniAssociation(e) {
    // 正确获取数据集属性
    const alumniAssociationId = e.currentTarget.dataset.alumniAssociationId
    const alumniAssociationName = e.currentTarget.dataset.alumniAssociationName
    console.log('[Debug] 选择的校友会:', { alumniAssociationId, alumniAssociationName })

    // 获取对应的校友会对象
    const selectedAlumni = this.data.alumniAssociationList.find(
      item => item.alumniAssociationId === alumniAssociationId
    )
    console.log('[Debug] 找到的校友会对象:', selectedAlumni)

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false,
      selectedOrganizeId: alumniAssociationId, // 确保使用校友会ID
    })

    try {
      // 调用 /AlumniAssociation/{id} 接口，入参为 alumniAssociationId
      console.log(
        '[Debug] 准备调用 /AlumniAssociation/{id} 接口，alumniAssociationId:',
        alumniAssociationId
      )

      const res = await this.getAlumniAssociationDetail(alumniAssociationId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校友会信息:', res.data.data)
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /AlumniAssociation/{id} 接口失败:', apiError)
    }

    // 加载该校友会的活动列表
    await this.loadActivityList()
  },

  // 调用校友会详情接口
  getAlumniAssociationDetail(alumniAssociationId) {
    return associationApi.getAssociationDetail(alumniAssociationId)
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 加载活动列表
  async loadActivityList() {
    const { selectedAlumniAssociationId } = this.data

    // 如果没有选择校友会，清空数据
    if (!selectedAlumniAssociationId) {
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0,
        },
      })
      return
    }

    try {
      const res = await this.getActivityList(selectedAlumniAssociationId)

      if (res.data && res.data.code === 200 && res.data.data) {
        // 过滤掉 null / undefined / 字面量字符串 "null"
        const cleanText = v => (v == null || v === 'null' ? '' : v)
        const activityList = res.data.data.map(item => {
          const location = [item.province, item.city, item.district, item.address]
            .map(cleanText)
            .join('')
          const category = cleanText(item.activityCategory)
          return {
            id: item.activityId,
            title: item.activityTitle,
            organizer: item.organizerName,
            cover: item.organizerAvatar,
            participantCount: item.currentParticipants,
            location,
            startTime: this.formatDateTime(item.startTime),
            endTime: this.formatDateTime(item.endTime),
            status: this.getActivityStatus(item.status),
            originalStatus: item.status,
            isSignup: item.isSignup,
            tags: category ? [category] : [],
            distance: 0, // 暂时设置为0，后续可以根据实际位置计算
          }
        })

        this.setData(
          {
            activityList,
            stats: {
              total: activityList.length,
              upcoming: activityList.filter(item => item.status === 'upcoming').length,
            },
          },
          () => {
            this.applyFilter()
          }
        )
      } else {
        console.error('获取活动列表失败:', (res.data && res.data.msg) || '接口调用失败')
        this.setData({
          activityList: [],
          displayList: [],
          stats: {
            total: 0,
            upcoming: 0,
          },
        })
      }
    } catch (error) {
      console.error('获取活动列表异常:', error)
      this.setData({
        activityList: [],
        displayList: [],
        stats: {
          total: 0,
          upcoming: 0,
        },
      })
    }
  },

  // 调用活动列表接口
  getActivityList(alumniAssociationId) {
    return alumniAssociationManagementApi.getActivities(alumniAssociationId)
  },

  // 格式化日期时间
  formatDateTime(dateTimeString) {
    if (!dateTimeString) {
      return ''
    }
    const date = new Date(dateTimeString)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },

  // 获取活动状态
  getActivityStatus(status) {
    switch (status) {
      case 0:
        return 'draft'
      case 1:
      case 2:
        return 'upcoming'
      case 3:
        return 'ongoing'
      case 4:
      case 5:
        return 'finished'
      default:
        return 'upcoming'
    }
  },

  applyFilter() {
    const { activityList, filterStatus, searchValue } = this.data
    let list = [...activityList]

    if (filterStatus !== 'all') {
      list = list.filter(item => item.status === filterStatus)
    }

    if (searchValue) {
      const keyword = searchValue.trim()
      list = list.filter(
        item =>
          item.title.includes(keyword) ||
          item.organizer.includes(keyword) ||
          item.location.includes(keyword)
      )
    }

    this.setData({ displayList: list })
  },

  handleSearchInput(e) {
    this.setData({ searchValue: e.detail.value }, () => {
      this.applyFilter()
    })
  },

  handleFilterChange(e) {
    const { id } = e.currentTarget.dataset
    if (id === this.data.filterStatus) {
      return
    }
    this.setData({ filterStatus: id }, () => {
      this.applyFilter()
    })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/activity/detail-new/detail-new?id=${id}`,
    })
  },

  goToPublish() {
    if (!this.data.selectedAlumniAssociationId) {
      wx.showToast({ title: '请先选择校友会', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: '/pages/activity/publish/publish',
    })
  },

  editActivity(e) {
    console.log('编辑按钮被点击:', e)
    const { id } = e.currentTarget.dataset
    console.log('活动ID:', id)
    try {
      wx.navigateTo({
        url: `/pages/activity/edit/edit?id=${id}`,
        success: function (res) {
          console.log('跳转成功:', res)
        },
        fail: function (err) {
          console.log('跳转失败:', err)
        },
      })
    } catch (error) {
      console.log('跳转异常:', error)
    }
  },

  // 跳转到该活动的报名审核页
  goToRegistrationAudit(e) {
    const { id, title } = e.currentTarget.dataset
    if (!id) return
    const titleParam = title ? `&activityTitle=${encodeURIComponent(title)}` : ''
    wx.navigateTo({
      url: `/pages/activity/registration-audit/index?activityId=${id}${titleParam}`,
    })
  },

  // 删除活动
  deleteActivity(e) {
    const { id } = e.currentTarget.dataset
    console.log('删除按钮被点击，活动ID:', id)

    // 显示确认对话框
    wx.showModal({
      title: '确认删除',
      content: '确定要删除此活动吗？删除后不可恢复。',
      success: async res => {
        if (res.confirm) {
          console.log('用户确认删除')
          try {
            const deleteResult = await this.deleteActivityById(id)
            console.log('删除操作结果:', deleteResult)

            if (deleteResult.success) {
              wx.showToast({ title: '删除成功', icon: 'success' })
              // 重新加载活动列表
              await this.loadActivityList()
            } else {
              wx.showToast({ title: deleteResult.message || '删除失败', icon: 'none' })
            }
          } catch (error) {
            console.error('删除活动异常:', error)
            wx.showToast({ title: '删除失败，请稍后重试', icon: 'none' })
          }
        } else if (res.cancel) {
          console.log('用户取消删除')
        }
      },
    })
  },

  // 调用删除活动接口
  deleteActivityById(activityId) {
    return new Promise((resolve, reject) => {
      alumniAssociationManagementApi
        .deleteActivity(activityId)
        .then(res => {
          console.log('删除活动接口返回:', res)
          if (res.data && res.data.code === 200 && res.data.data === true) {
            resolve({ success: true, message: '删除成功' })
          } else {
            resolve({ success: false, message: (res.data && res.data.msg) || '删除失败' })
          }
        })
        .catch(err => {
          console.error('删除活动接口调用失败:', err)
          reject(err)
        })
    })
  },
})
