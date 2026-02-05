// pages/local-platform/detail/detail.js
const { localPlatformApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconLocation: config.getIconUrl('position.png'),
    platformId: '',
    platformInfo: null,
    loading: true,
    // 校友会列表
    associations: [],
    // 顶部标签：基本信息 / 组织结构 / 会员列表
    activeTab: 0,
    tabs: ['基本信息', '组织结构', '会员列表'],
    // 组织结构数据
    roleList: [], // 存储角色列表
    organizationLoading: false, // 组织结构加载状态
    // 分页参数：设置为极大值，一次性加载所有数据
    current: 1,
    pageSize: 99999999,
    hasMore: false,
    associationLoading: false,
    // 组织结构编辑相关
    canEditOrg: false, // 是否有编辑权限
    isEditOrgMode: false // 是否处于编辑模式
  },

  onLoad(options) {
    const { id } = options
    if (!id) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    this.setData({ platformId: id })
    this.checkEditPermission()
    this.loadPlatformDetail()
  },

  // 检查编辑权限
  checkEditPermission() {
    const { checkHasRoles } = require('../../../utils/auth.js')
    const hasPermission = checkHasRoles(['SYSTEM_SUPER_ADMIN', 'ORGANIZE_LOCAL_ADMIN'])
    this.setData({ canEditOrg: hasPermission })
  },

  // 进入编辑模式
  onEditOrgClick() {
    this.setData({ isEditOrgMode: true })
    // 通过选择器获取组件实例并调用方法
    const orgStructure = this.selectComponent('#org-structure')
    if (orgStructure) {
      orgStructure.onEditClick()
    }
  },

  // 取消编辑
  onCancelOrgEdit() {
    this.setData({ isEditOrgMode: false })
    // 通过选择器获取组件实例并调用方法
    const orgStructure = this.selectComponent('#org-structure')
    if (orgStructure) {
      orgStructure.onCancelEdit()
    }
  },

  onShareAppMessage() {
    return {
      title: this.data.platformInfo?.platformName || '校处会',
      path: `/pages/local-platform/detail/detail?id=${this.data.platformId}`
    }
  },
  
  // 下拉刷新
  onPullDownRefresh() {
    // 如果当前是会员列表标签页，刷新校友会列表
    if (this.data.activeTab === 2) {
      this.loadAssociations(true).finally(() => {
        wx.stopPullDownRefresh()
      })
    } else {
      // 不是会员列表时，直接停止下拉刷新
      wx.stopPullDownRefresh()
    }
  },
  
  // 上拉加载更多
  onReachBottom() {
    // 如果当前是会员列表标签页且有更多数据，加载更多
    if (this.data.activeTab === 2 && this.data.hasMore) {
      this.loadAssociations()
    }
  },

  // 加载校处会详情
  async loadPlatformDetail() {
    this.setData({ loading: true })
    
    try {
      const res = await localPlatformApi.getLocalPlatformDetail(this.data.platformId)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}

        // 将后端返回的数据映射到前端需要的格式，补充前端展示所需字段
        
        // 处理头像
        let icon = data.avatar || ''
        if (icon) {
          if (icon.startsWith('http://') || icon.startsWith('https://')) {
            icon = data.avatar
          } else {
            icon = config.getImageUrl(icon)
          }
        } else {
          icon = config.defaultAlumniAvatar
        }
        
        // 处理背景图片
        let cover = data.bgImg || ''
        if (cover) {
          if (cover.startsWith('http://') || cover.startsWith('https://')) {
            cover = data.bgImg
          } else {
            cover = config.getImageUrl(cover)
          }
        } else {
          cover = config.defaultCover
        }
        
        const platformInfo = {
          platformId: this.data.platformId,
          platformName: data.platformName || '',
          city: data.city || '',
          scope: data.scope || '',
          adminUserId: data.adminUserId || null,
          contactInfo: data.contactInfo || '',
          // 以下为前端展示补充字段
          icon: icon,
          cover: cover,
          location: data.city || '',
          description: data.description || '',
          memberCount: data.memberCount || 0
        }

        this.setData({
          platformInfo,
          loading: false
        })
        
        // 设置导航栏标题为校处会的city字段
        wx.setNavigationBarTitle({
          title: platformInfo.city || '校处会'
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('加载校处会详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 顶部 Tab 切换
  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ activeTab: index })
    
    // 切换到组织结构标签时，加载组织结构数据
    if (index === 1) {
      // 如果还没加载过组织结构数据，则加载
      if (this.data.roleList.length === 0) {
        this.loadOrganizationTree()
      }
    }
    // 切换到会员列表时加载数据
    else if (index === 2) {
      // 如果是首次切换到会员列表，重新加载数据
      if (this.data.associations.length === 0) {
        this.loadAssociations(true)
      }
    }
  },
  
  // 加载组织架构树
  loadOrganizationTree() {
    this.setData({
      organizationLoading: true
    })
    
    // 调用API获取组织架构树（v2 接口）
    const { post } = require('../../../utils/request.js')
    post('/localPlatform/organizationTree/v2', {
      localPlatformId: this.data.platformId
    }).then(res => {
      if (res.data && res.data.code === 200) {
        this.setData({
          roleList: res.data.data || []
        })
      } else {
        wx.showToast({
          title: res.data.msg || '加载失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
      console.error('加载组织架构树失败:', err)
    }).finally(() => {
      this.setData({
        organizationLoading: false
      })
    })
  },
  
  // 加载校友会列表
  async loadAssociations(refresh = false) {
    // 如果正在加载，直接返回
    if (this.data.associationLoading) {
      return
    }
    
    try {
      this.setData({ associationLoading: true })
      
      // 准备请求参数：使用极大pageSize，一次性加载所有数据
      const params = {
        current: 1,
        pageSize: this.data.pageSize,
        platformId: this.data.platformId, // 直接使用字符串类型，避免数字精度丢失
        sortField: 'memberCount', // 默认按会员数量排序
        sortOrder: 'descend' // 默认降序
      }
      
      // 调用接口
      const res = await localPlatformApi.getPlatformAssociations(params)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        let associationList = data.records || []
        
        // 处理校友会列表数据，添加头像处理逻辑
        associationList = associationList.map(item => {
          // 优先使用logo作为头像，如果没有logo则使用默认头像
          let avatar = ''
          if (item.logo) {
            if (item.logo.startsWith('http://') || item.logo.startsWith('https://')) {
              avatar = item.logo
            } else {
              avatar = config.getImageUrl(item.logo)
            }
          } else {
            // 使用默认头像，与校友会列表页面保持一致
            avatar = '/assets/avatar/avatar-2.png'
          }
          
          return {
            ...item,
            avatar: avatar
          }
        })
        
        // 更新数据：直接替换原有数据，无需分页
        this.setData({
          associations: associationList,
          // 已经加载所有数据，无需分页
          hasMore: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载校友会列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ associationLoading: false })
    }
  },

  // 复制联系信息
  copyContactInfo() {
    const { platformInfo } = this.data
    if (platformInfo && platformInfo.contactInfo) {
      wx.setClipboardData({
        data: platformInfo.contactInfo,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  },
  
  // 查看校友会详情
  viewAssociationDetail(e) {
    const associationId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${associationId}`
    })
  },

  // 组织架构刷新事件
  onOrgStructureRefresh() {
    // 重新加载组织架构数据
    this.loadOrganizationTree()
  },

  // 创建校友会
  createAlumniAssociation() {
    wx.navigateTo({
      url: '/pages/alumni-association/create/create'
    })
  }
})


