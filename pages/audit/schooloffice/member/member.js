// pages/audit/schooloffice/member/member.js
const app = getApp()

Page({
  data: {
    schoolOfficeList: [],
    loading: false,
    selectedSchoolOfficeId: 0,
    selectedSchoolOfficeName: '',
    showSchoolOfficePicker: false,
    selectedOrganizeId: 0 // 存储选中的organizeId
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  async initPage() {
    await this.loadSchoolOfficeList()
  },

  // 加载校处会列表（从缓存中获取校处会管理员的organizeId，然后调用接口）
  async loadSchoolOfficeList() {
    try {
      console.log('[Debug] 开始加载校处会列表')
      
      // 从 storage 中获取角色列表
      const roles = wx.getStorageSync('roles') || []
      console.log('[Debug] 从storage获取的角色列表:', roles)
      
      // 查找所有校处会管理员角色
      const schoolOfficeAdminRoles = roles.filter(role => role.remark === '校处会管理员')
      console.log('[Debug] 找到的校处会管理员角色:', schoolOfficeAdminRoles)
      
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
          
          // 先创建基本的校处会列表
          const basicSchoolOfficeList = uniqueOrganizeIds.map(organizeId => ({
            id: organizeId,
            schoolOfficeId: organizeId,
            schoolOfficeName: `校处会 (ID: ${organizeId})`
          }))
          
          this.setData({
            schoolOfficeList: basicSchoolOfficeList
          })
          console.log('[Debug] 直接使用organizeIds创建校处会列表:', basicSchoolOfficeList)
          
          // 尝试调用接口获取更详细的信息（可选）
          try {
            // 并行调用所有校处会的详情接口
            const detailPromises = uniqueOrganizeIds.map(organizeId => 
              app.api.localPlatformApi.getLocalPlatformDetail(organizeId)
                .catch(error => {
                  console.log(`[Debug] 获取校处会 ${organizeId} 详情失败，使用基本数据:`, error)
                  return null // 接口失败时返回null，后续过滤
                })
            )
            
            const detailResults = await Promise.all(detailPromises)
            
            // 处理接口返回结果，更新校处会列表
            const updatedSchoolOfficeList = basicSchoolOfficeList.map((platform, index) => {
              const result = detailResults[index]
              if (result && result.data && result.data.code === 200 && result.data.data) {
                const detailData = result.data.data
                return {
                  ...detailData,
                  id: detailData.platformId || detailData.id || platform.schoolOfficeId,
                  schoolOfficeId: detailData.platformId || platform.schoolOfficeId,
                  schoolOfficeName: detailData.platformName || detailData.name || platform.schoolOfficeName
                }
              }
              return platform // 接口失败时使用基本数据
            })
            
            this.setData({
              schoolOfficeList: updatedSchoolOfficeList
            })
            console.log('[Debug] 已更新校处会列表:', updatedSchoolOfficeList)
          } catch (apiError) {
            console.log('[Debug] 批量获取校处会详情失败，继续使用基本数据:', apiError)
            // 继续使用之前创建的基本数据
          }
        } else {
          // 没有找到有效的organizeId，设置为空数组
          console.warn('[Debug] 校处会管理员角色没有有效的organization或organizeId')
          this.setData({
            schoolOfficeList: []
          })
        }
      } else {
        // 没有找到校处会管理员角色，设置为空数组
        console.warn('[Debug] 没有找到校处会管理员角色')
        this.setData({
          schoolOfficeList: []
        })
      }
    } catch (error) {
      console.error('[Debug] 加载校处会列表失败:', error)
      // 发生错误时，设置为空数组
      this.setData({
        schoolOfficeList: []
      })
    }
  },

  // 显示校处会选择器
  showSchoolOfficeSelector() {
    this.setData({ showSchoolOfficePicker: false })
    this.setData({ showSchoolOfficePicker: true })
  },

  // 选择校处会
  async selectSchoolOffice(e) {
    // 正确获取数据集属性
    const schoolOfficeId = e.currentTarget.dataset.schoolOfficeId
    const schoolOfficeName = e.currentTarget.dataset.schoolOfficeName
    console.log('[Debug] 选择的校处会:', { schoolOfficeId, schoolOfficeName })

    // 获取对应的校处会对象
    const selectedSchoolOffice = this.data.schoolOfficeList.find(item => item.schoolOfficeId === schoolOfficeId)
    console.log('[Debug] 找到的校处会对象:', selectedSchoolOffice)

    this.setData({
      selectedSchoolOfficeId: schoolOfficeId,
      selectedSchoolOfficeName: schoolOfficeName,
      showSchoolOfficePicker: false,
      selectedOrganizeId: schoolOfficeId // 确保使用校处会ID
    })

    try {
      // 调用 /localPlatform/{id} 接口，入参为 schoolOfficeId
      console.log('[Debug] 准备调用 /localPlatform/{id} 接口，schoolOfficeId:', schoolOfficeId)

      const res = await this.getSchoolOfficeDetail(schoolOfficeId)

      console.log('[Debug] 接口调用结果:', res)

      if (res.data && res.data.code === 200 && res.data.data) {
        console.log('[Debug] 接口调用成功，获取到的校处会信息:', res.data.data)
        
        // 更新selectedSchoolOfficeId为正确的platformId
        const correctPlatformId = res.data.data.platformId || schoolOfficeId;
        this.setData({
          selectedSchoolOfficeId: correctPlatformId,
          selectedOrganizeId: correctPlatformId
        });
        console.log('[Debug] 更新selectedSchoolOfficeId为:', correctPlatformId);
      } else {
        console.error('[Debug] 接口调用失败，返回数据:', res)
      }
    } catch (apiError) {
      console.error('[Debug] 调用 /localPlatform/{id} 接口失败:', apiError)
    }
  },

  // 取消选择校处会
  cancelSchoolOfficeSelect() {
    this.setData({ showSchoolOfficePicker: false })
  },

  // 调用校处会详情接口
  getSchoolOfficeDetail(schoolOfficeId) {
    return new Promise((resolve, reject) => {
      // 获取 token
      let token = wx.getStorageSync('token')
      if (!token) {
        const userInfo = wx.getStorageSync('userInfo') || {}
        token = userInfo.token || ''
      }

      const headers = {
        'Content-Type': 'application/json'
      }

      if (token) {
        headers.token = token
        headers['x-token'] = token
      }

      wx.request({
        url: `${app.globalData.baseUrl}/localPlatform/${schoolOfficeId}`,
        method: 'GET',
        header: headers,
        success: resolve,
        fail: reject
      })
    })
  }
})
