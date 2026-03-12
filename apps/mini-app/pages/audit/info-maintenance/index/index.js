// pages/audit/info-maintenance/index/index.js
const app = getApp()
const config = require('../../../../utils/config.js')

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

  // 加载校促会列表（从缓存中获取校促会管理员的organizeId，然后调用接口）
  async loadPlatformList() {
    try {
      console.log('[Debug] 开始加载校促会列表')

      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)

      // 查找所有校促会管理员角色
      const schoolOfficeAdminRoles = roles.filter(role => role.roleCode === 'ORGANIZE_LOCAL_ADMIN')
      console.log('[Debug] 找到的校促会管理员角色:', schoolOfficeAdminRoles)

      if (schoolOfficeAdminRoles.length > 0) {
        // 收集所有有效的organizeId
        const organizeIds = schoolOfficeAdminRoles
          .filter(role => role.organization && role.organization.organizeId)
          .map(role => role.organization.organizeId)

        console.log('[Debug] 获取到的organizeIds:', organizeIds)

        if (organizeIds.length > 0) {
          // 去重，确保每个organizeId只处理一次
          const uniqueOrganizeIds = [...new Set(organizeIds)]
          console.log('[Debug] 去重后的organizeIds:', uniqueOrganizeIds)

          // 先创建基本的校促会列表
          const basicPlatformList = uniqueOrganizeIds.map(organizeId => ({
            id: organizeId,
            platformId: organizeId,
            platformName: `校促会 (ID: ${organizeId})`
          }))

          this.setData({
            platformList: basicPlatformList
          })
          console.log('[Debug] 直接使用organizeIds创建校促会列表:', basicPlatformList)

          // 尝试调用接口获取更详细的信息（可选）
          try {
            // 并行调用所有校促会的详情接口
            const detailPromises = uniqueOrganizeIds.map(organizeId =>
              app.api.localPlatformApi.getLocalPlatformDetail(organizeId)
                .catch(error => {
                  console.log(`[Debug] 获取校促会 ${organizeId} 详情失败，使用基本数据:`, error)
                  return null // 接口失败时返回null，后续过滤
                })
            )

            const detailResults = await Promise.all(detailPromises)

            // 处理接口返回结果，更新校促会列表
            const updatedPlatformList = basicPlatformList.map((platform, index) => {
              const result = detailResults[index]
              if (result && result.data && result.data.code === 200 && result.data.data) {
                const detailData = result.data.data
                
                // 处理背景图
                if (detailData.bgImg && detailData.bgImg.trim()) {
                  detailData.bgImg = config.getImageUrl(detailData.bgImg)
                }
                
                // 处理 logo
                if (detailData.avatar && detailData.avatar.trim()) {
                  detailData.avatar = config.getImageUrl(detailData.avatar)
                }
                
                return {
                  ...detailData,
                  id: detailData.platformId || detailData.id || platform.platformId,
                  platformName: detailData.platformName || detailData.name || platform.platformName
                }
              }
              return platform // 接口失败时使用基本数据
            })

            this.setData({
              platformList: updatedPlatformList
            })
            console.log('[Debug] 已更新校促会列表:', updatedPlatformList)

            // 判断权限数量，处理自动选择逻辑
            this.handlePlatformSelection(updatedPlatformList)
          } catch (apiError) {
            console.log('[Debug] 批量获取校促会详情失败，继续使用基本数据:', apiError)
            // 继续使用之前创建的基本数据
            this.handlePlatformSelection(basicPlatformList)
          }
        } else {
          // 没有找到有效的organizeId，设置为空数组
          console.warn('[Debug] 校促会管理员角色没有有效的organization或organizeId')
          this.setData({
            platformList: []
          })
        }
      } else {
        // 没有找到校促会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校促会管理员角色')
        this.setData({
          platformList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校促会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        platformList: []
      })
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