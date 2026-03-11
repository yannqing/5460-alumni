// pages/info-maintenance/index/index.js
const { alumniAssociationManagementApi, associationApi, userApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    // 校友会相关
    alumniAssociationList: [],
    loading: false,
    selectedAlumniAssociationId: 0,
    selectedAlumniAssociationName: '',
    showAlumniAssociationPicker: false,
    hasSingleAlumniAssociation: false, // 是否只有一个校友会权限,
    hasAlumniAdminPermission: false, // 是否有校友会管理员身份
    currentAlumniDetail: null, // 当前选中的校友会详情
  },

  onLoad(options) {
    this.initPage()
  },

  // 页面显示时（从编辑页返回时刷新数据）
  onShow() {
    // 如果已经选择了校友会，重新加载详情
    if (this.data.selectedAlumniAssociationId) {
      this.loadAlumniAssociationDetail(this.data.selectedAlumniAssociationId)
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
          hasAlumniAdminPermission: organizationList.length > 0
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
              type: org.type
            }
          })

          // 设置校友会列表
          this.setData({
            alumniAssociationList: alumniAssociationList
          })
          console.log('[Debug] 最终校友会列表:', alumniAssociationList)

          // 判断权限数量，处理自动选择逻辑
          this.handleAlumniAssociationSelection(alumniAssociationList)
        } else {
          // 没有找到管理的校友会
          console.warn('[Debug] 用户没有管理的校友会')
          this.setData({
            alumniAssociationList: [],
            hasAlumniAdminPermission: false
          })
        }
      } else {
        console.error('[Debug] 获取校友会列表接口调用失败:', res)
        this.setData({
          alumniAssociationList: [],
          hasAlumniAdminPermission: false
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校友会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        alumniAssociationList: [],
        hasAlumniAdminPermission: false
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
        selectedAlumniAssociationName: singleAlumni.associationName || singleAlumni.alumniAssociationName,
        hasSingleAlumniAssociation: true,
        currentAlumniDetail: null // 重置详情，准备加载新数据
      })
      console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
      
      // 自动选择时也加载校友会详情
      await this.loadAlumniAssociationDetail(singleAlumni.alumniAssociationId)
    } else if (alumniAssociationList.length > 1) {
      // 多个校友会权限，正常显示选择器
      this.setData({
        hasSingleAlumniAssociation: false
      })
      console.log('[Debug] 有多个校友会权限，正常显示选择器')
    } else {
      // 没有校友会权限
      this.setData({
        hasSingleAlumniAssociation: false
      })
      console.log('[Debug] 没有校友会权限')
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

    this.setData({
      selectedAlumniAssociationId: alumniAssociationId,
      selectedAlumniAssociationName: alumniAssociationName,
      showAlumniAssociationPicker: false,
      currentAlumniDetail: null // 重置详情，准备加载新数据
    })

    // 获取校友会详情
    await this.loadAlumniAssociationDetail(alumniAssociationId)
  },

  // 加载校友会详情
  async loadAlumniAssociationDetail(alumniAssociationId) {
    try {
      console.log('[Debug] 开始加载校友会详情:', alumniAssociationId)
      
      const res = await this.getAlumniAssociationDetail(alumniAssociationId)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 获取校友会详情成功:', res.data.data)
        
        // 处理logo字段，去除空格和反引号
        const processedData = res.data.data
        if (processedData.logo) {
          processedData.logo = processedData.logo.trim().replace(/[`\s]/g, '')
        }
        
        this.setData({
          currentAlumniDetail: processedData
        })
      } else {
        console.error('[Debug] 获取校友会详情失败:', res)
        this.setData({
          currentAlumniDetail: null
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校友会详情异常:', error)
      this.setData({
        currentAlumniDetail: null
      })
    }
  },

  // 取消选择校友会
  cancelAlumniAssociationSelect() {
    this.setData({ showAlumniAssociationPicker: false })
  },

  // 调用校友会详情接口
  getAlumniAssociationDetail(alumniAssociationId) {
    return associationApi.getAssociationDetail(alumniAssociationId)
  },

  // 跳转到编辑页面
  goToEditPage() {
    if (!this.data.selectedAlumniAssociationId) {
      wx.showToast({
        title: '请先选择校友会',
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: `/pages/info-maintenance/edit/edit?alumniAssociationId=${this.data.selectedAlumniAssociationId}`,
      success: (res) => {
        // 可以在这里传递额外的数据
        res.eventChannel.emit('acceptDataFromOpenerPage', {
          currentAlumniDetail: this.data.currentAlumniDetail
        })
      }
    })
  },


})
