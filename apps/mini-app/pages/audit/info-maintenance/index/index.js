// pages/audit/info-maintenance/index/index.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { userApi } = require('../../../../api/api.js')

Page({
  data: {
    selectedPlatformId: 0,
    selectedPlatformName: '',
    platformList: [],
    showPlatformPicker: false,
    hasSinglePlatform: false, // 是否只有一个校促会权限
    platformDetail: null, // 校促会详细信息
    loading: false, // 加载状态
    defaultAlumniAvatar: config.defaultAlumniAvatar || config.defaultAvatar,
    defaultBackground: config.defaultCover,
    // 隐私设置相关
    showPrivacyModal: false,
    privacyLoading: false,
    privacySettings: []
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadPlatformList()
  },

  // 加载校促会列表（调用 users/managed-organizations?type=1 接口，type=1 表示校促会）
  async loadPlatformList() {
    try {
      console.log('[Debug] 开始加载校促会列表')
      const res = await userApi.getManagedOrganizations({ type: 1 })
      if (!res.data || res.data.code !== 200) {
        this.setData({ platformList: [] })
        return
      }
      const organizationList = res.data.data || []
      if (!Array.isArray(organizationList) || organizationList.length === 0) {
        this.setData({ platformList: [] })
        return
      }
      const platformList = organizationList.map(org => {
        let logo = org.logo || ''
        if (logo && !logo.startsWith('http://') && !logo.startsWith('https://')) {
          logo = config.getImageUrl(logo)
        }
        return {
          id: org.id,
          platformId: org.id,
          platformName: org.name || '校促会',
          logo: logo,
          location: org.location || '',
          type: org.type
        }
      })
      this.setData({ platformList })
      console.log('[Debug] 校促会列表:', platformList)
      this.handlePlatformSelection(platformList)
    } catch (error) {
      console.error('[Debug] 加载校促会列表失败:', error)
      this.setData({ platformList: [] })
    }
  },

  // 处理校促会选择逻辑
  async handlePlatformSelection(platformList) {
    if (platformList.length === 1) {
      // 只有一个校促会权限，自动选择并禁用选择器
      const singlePlatform = platformList[0]
      this.setData({
        selectedPlatformId: singlePlatform.platformId,
        selectedPlatformName: singlePlatform.platformName,
        hasSinglePlatform: true
      })
      console.log('[Debug] 只有一个校促会权限，自动选择:', singlePlatform)
      // 调用加载校促会详细信息的方法
      await this.loadPlatformDetail(singlePlatform.platformId)
    } else {
      // 多个校促会权限，正常显示选择器
      this.setData({
        hasSinglePlatform: false
      })
      console.log('[Debug] 有多个校促会权限，正常显示选择器')
    }
  },

  // 显示校促会选择器
  showPlatformSelector() {
    this.setData({ showPlatformPicker: true })
  },

  // 选择校促会
  async selectPlatform(e) {
    const { platformId, platformName } = e.currentTarget.dataset
    console.log('[Debug] 选择的校促会:', { platformId, platformName })

    this.setData({
      selectedPlatformId: platformId,
      selectedPlatformName: platformName,
      showPlatformPicker: false
    })

    // 调用加载校促会详细信息的方法
    await this.loadPlatformDetail(platformId)
  },

  // 取消选择校促会
  cancelPlatformSelect() {
    this.setData({ showPlatformPicker: false })
  },

  // 加载校促会详细信息
  async loadPlatformDetail(platformId) {
    if (!platformId) return

    try {
      this.setData({ loading: true })
      console.log('[Debug] 开始加载校促会详细信息，platformId:', platformId)

      // 调用 /localPlatformManagement/detail/{platformId} 接口
      const res = await app.api.localPlatformApi.getLocalPlatformManagementDetail(platformId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校促会详细信息:', res.data.data)
        let platformDetail = res.data.data

        // 处理背景图
        if (platformDetail.bgImg && platformDetail.bgImg.trim()) {
          platformDetail.bgImg = config.getImageUrl(platformDetail.bgImg)
        } else {
          platformDetail.bgImg = null
        }

        // 处理 logo
        if (platformDetail.avatar && platformDetail.avatar.trim()) {
          platformDetail.avatar = config.getImageUrl(platformDetail.avatar)
        }

        this.setData({
          platformDetail
        })
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
        wx.showToast({
          title: '获取校促会详情失败',
          icon: 'error'
        })
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatformManagement/detail/{platformId} 接口失败:', apiError)
      wx.showToast({
        title: '获取校促会详情失败',
        icon: 'error'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 跳转到编辑页面
  navigateToEdit() {
    if (this.data.selectedPlatformId) {
      wx.navigateTo({
        url: `/pages/audit/info-maintenance/edit/edit?platformId=${this.data.selectedPlatformId}`
      })
    }
  },

  // 显示隐私设置弹窗
  showPrivacySettings() {
    if (!this.data.selectedPlatformId) {
      wx.showToast({
        title: '请先选择校促会',
        icon: 'error'
      })
      return
    }
    
    this.setData({ showPrivacyModal: true })
    this.loadPrivacySettings(this.data.selectedPlatformId)
  },

  // 关闭隐私设置弹窗
  closePrivacyModal() {
    this.setData({ showPrivacyModal: false })
  },

  // 加载隐私设置数据
  async loadPrivacySettings(platformId) {
    if (!platformId) return

    try {
      this.setData({ privacyLoading: true })
      console.log('[Debug] 开始加载隐私设置，platformId:', platformId)

      // 调用 /localPlatformManagement/privacy/{platformId} 接口
      const res = await app.api.localPlatformApi.getLocalPlatformPrivacySetting(platformId)

      console.log('[Debug] 隐私设置接口调用结果:', res)

      if (res.data && res.data.code === 200) {
        console.log('[Debug] 隐私设置接口调用成功，获取到的数据:', res.data.data)
        this.setData({
          privacySettings: res.data.data || []
        })
      } else {
        console.error('[Debug] 隐私设置接口调用失败，返回数据:', res)
        wx.showToast({
          title: '获取隐私设置失败',
          icon: 'error'
        })
        this.setData({
          privacySettings: []
        })
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatformManagement/privacy/{platformId} 接口失败:', apiError)
      wx.showToast({
        title: '获取隐私设置失败',
        icon: 'error'
      })
      this.setData({
        privacySettings: []
      })
    } finally {
      this.setData({ privacyLoading: false })
    }
  },

  // 切换隐私设置
  async togglePrivacySetting(e) {
    const { platformId, fieldCode, visibility } = e.currentTarget.dataset
    const newVisibility = e.detail.value ? 1 : 0

    try {
      // 构造请求数据
      const requestData = {
        platformId: platformId,
        fieldCode: fieldCode,
        visibility: newVisibility
      }

      console.log('[Debug] 切换隐私设置，请求数据:', requestData)

      // 调用修改隐私设置接口
      const res = await app.api.localPlatformApi.updateLocalPlatformPrivacySetting(requestData)

      console.log('[Debug] 修改隐私设置接口调用结果:', res)

      if (res.data && res.data.code === 200) {
        console.log('[Debug] 修改隐私设置成功')
        
        // 更新本地数据
        const updatedPrivacySettings = this.data.privacySettings.map(item => {
          if (item.fieldCode === fieldCode) {
            return {
              ...item,
              visibility: newVisibility
            }
          }
          return item
        })

        this.setData({
          privacySettings: updatedPrivacySettings
        })

        wx.showToast({
          title: '修改成功',
          icon: 'success'
        })
      } else {
        console.error('[Debug] 修改隐私设置失败，返回数据:', res)
        wx.showToast({
          title: '修改失败',
          icon: 'error'
        })
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatformManagement/privacy/update 接口失败:', apiError)
      wx.showToast({
        title: '修改失败',
        icon: 'error'
      })
    }
  }
})