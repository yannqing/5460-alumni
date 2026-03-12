// pages/local-platform/apply/apply.js
const { associationApi, localPlatformApi, userApi } = require('../../../api/api.js')
const { refreshUserRoles } = require('../../../utils/auth.js')
const config = require('../../../utils/config.js')

Page({
    data: {
        config: config,
        formData: {
            platformName: '',
            platformId: ''
        },
        alumniAssociationList: [],
        selectedAlumniAssociationId: 0,
        selectedAlumniAssociationName: '',
        showAlumniAssociationPicker: false,
        hasSingleAlumniAssociation: false, // 是否只有一个校友会权限,
        hasAlumniAdminPermission: false, // 是否有校友会管理员身份
        alumniAssociationDetail: null, // 校友会详情
        loading: false, // 加载状态
        defaultAlumniAvatar: config.defaultAvatar,
        defaultBackground: config.defaultCover,
        platformList: [],
        platformIndex: -1
    },

    onLoad(options) {
        // 如果从列表页传递了平台名称，则自动填充
        this.platformNameFromList = options.platformName ? decodeURIComponent(options.platformName) : null
        if (this.platformNameFromList) {
            this.setData({
                'formData.platformName': this.platformNameFromList
            })
        }
        // 初始化页面数据
        this.initPage()
    },

    // 初始化页面数据
    async initPage() {
        await Promise.all([
            this.loadAlumniAssociationList(),
            this.loadPlatformList()
        ])
    },

    // 加载平台列表
    async loadPlatformList() {
        try {
            const res = await localPlatformApi.getLocalPlatformPage({
                current: 1,
                pageSize: 100,
                platformName: ''
            })
            if (res.data && res.data.code === 200) {
                const platformList = res.data.data.records || []
                this.setData({
                    platformList: platformList
                })

                // 如果有从列表页面传递过来的platformName，查找并设置对应的平台信息
                if (this.platformNameFromList) {
                    const platformIndex = platformList.findIndex(item => item.platformName === this.platformNameFromList)
                    if (platformIndex !== -1) {
                        const platform = platformList[platformIndex]
                        this.setData({
                            platformIndex: platformIndex,
                            'formData.platformId': platform.platformId,
                            'formData.platformName': platform.platformName
                        })
                    }
                }
            }
        } catch (e) {
            console.error('加载平台列表失败', e)
        }
    },

    // 加载校友会列表（从缓存中获取校友会管理员的alumniAssociationId，然后调用接口）
    async loadAlumniAssociationList() {
        try {
            console.log('[Debug] 开始加载校友会列表')

            // 从 storage 中获取角色列表
            const roles = wx.getStorageSync('roles') || []
            console.log('[Debug] 从storage获取的角色列表:', roles)

            // 查找所有校友会管理员角色（根据roleCode）
            const alumniAdminRoles = roles.filter(role =>
                role.roleCode === 'ORGANIZE_ALUMNI_ADMIN'
            )
            console.log('[Debug] 找到的所有校友会管理员角色:', alumniAdminRoles)

            // 设置是否有校友会管理员身份
            this.setData({
                hasAlumniAdminPermission: alumniAdminRoles.length > 0
            })

            if (alumniAdminRoles.length > 0) {
                console.log('[Debug] 存在校友会管理员角色，开始处理每个角色')

                // 存储所有校友会数据
                const alumniAssociationList = []

                // 遍历所有校友会管理员角色，创建校友会数据
                for (const alumniAdminRole of alumniAdminRoles) {
                    console.log('[Debug] 处理校友会管理员角色:', alumniAdminRole)

                    // 尝试从不同可能的位置获取ID
                    let alumniAssociationId = null

                    // 检查直接字段
                    if (alumniAdminRole.alumniAssociationId) {
                        alumniAssociationId = alumniAdminRole.alumniAssociationId
                        console.log('[Debug] 从直接字段获取到alumniAssociationId:', alumniAssociationId)
                    }
                    // 检查嵌套的organization字段
                    else if (alumniAdminRole.organization && alumniAdminRole.organization.alumniAssociationId) {
                        alumniAssociationId = alumniAdminRole.organization.alumniAssociationId
                        console.log('[Debug] 从organization字段获取到alumniAssociationId:', alumniAssociationId)
                    }
                    // 检查organizeId字段（作为备用）
                    else if (alumniAdminRole.organizeId) {
                        alumniAssociationId = alumniAdminRole.organizeId
                        console.log('[Debug] 从organizeId字段获取到ID:', alumniAssociationId)
                    }
                    // 检查嵌套的organization.organizeId字段
                    else if (alumniAdminRole.organization && alumniAdminRole.organization.organizeId) {
                        alumniAssociationId = alumniAdminRole.organization.organizeId
                        console.log('[Debug] 从organization.organizeId字段获取到ID:', alumniAssociationId)
                    }

                    console.log('[Debug] 最终获取到的alumniAssociationId:', alumniAssociationId)

                    if (alumniAssociationId) {
                        // 获取协会名称（如果直接提供）
                        const associationName = alumniAdminRole.associationName || alumniAdminRole.organization?.associationName || '校友会'

                        // 创建基本的校友会对象
                        const basicAlumniData = {
                            id: alumniAssociationId,
                            alumniAssociationId: alumniAssociationId,
                            alumniAssociationName: associationName,
                            organizeId: alumniAdminRole.organizeId || alumniAssociationId // 存储organizeId
                        }

                        // 检查是否已经存在相同ID的校友会
                        const existingIndex = alumniAssociationList.findIndex(item =>
                            item.alumniAssociationId === alumniAssociationId
                        )

                        // 如果不存在，则添加到列表
                        if (existingIndex === -1) {
                            alumniAssociationList.push(basicAlumniData)
                            console.log('[Debug] 添加校友会到列表:', basicAlumniData)
                        } else {
                            console.log('[Debug] 校友会已存在，跳过:', alumniAssociationId)
                        }
                    }
                }

                // 设置校友会列表
                this.setData({
                    alumniAssociationList: alumniAssociationList
                })
                console.log('[Debug] 最终校友会列表:', alumniAssociationList)

                // 尝试为所有校友会调用接口获取更详细的信息
                try {
                    // 创建一个新的列表来存储更新后的校友会数据
                    const updatedList = [...alumniAssociationList]

                    // 使用Promise.all并行获取所有校友会的详细信息
                    const detailPromises = updatedList.map(async (alumni, index) => {
                        try {
                            const res = await this.getAlumniAssociationDetail(alumni.alumniAssociationId)
                            if (res.data && res.data.code === 200 && res.data.data) {
                                console.log(`[Debug] 获取校友会 ${index + 1} 详细信息成功:`, res.data.data)

                                // 更新校友会的详细信息
                                return {
                                    ...res.data.data,
                                    id: res.data.data.alumniAssociationId || res.data.data.id || alumni.alumniAssociationId,
                                    alumniAssociationId: res.data.data.alumniAssociationId || alumni.alumniAssociationId,
                                    alumniAssociationName: res.data.data.associationName || res.data.data.name || alumni.alumniAssociationName,
                                    organizeId: res.data.data.organizeId || alumni.alumniAssociationId // 确保有organizeId
                                }
                            }
                            return alumni // 如果接口调用失败，返回原始数据
                        } catch (error) {
                            console.log(`[Debug] 获取校友会 ${index + 1} 详细信息失败:`, error)
                            return alumni // 如果发生错误，返回原始数据
                        }
                    })

                    // 等待所有请求完成
                    const detailedAlumniList = await Promise.all(detailPromises)

                    // 更新校友会列表
                    this.setData({
                        alumniAssociationList: detailedAlumniList
                    })
                    console.log('[Debug] 已更新所有校友会详细信息:', detailedAlumniList)

                    // 判断权限数量，处理自动选择逻辑
                    this.handleAlumniAssociationSelection(detailedAlumniList)
                } catch (apiError) {
                    console.log('[Debug] 获取校友会详细信息失败:', apiError)
                    // 继续使用之前创建的基本数据
                    this.handleAlumniAssociationSelection(alumniAssociationList)
                }
            } else {
                // 没有找到校友会管理员角色，尝试接口兜底（审核通过后 roles 缓存未更新）
                await this.loadAlumniAssociationListFromApi()
            }
        } catch (error) {
            console.error('[Debug] 加载校友会列表失败:', error)
            // 发生错误时，设置为空数组
            this.setData({
                alumniAssociationList: []
            })
        }
    },

    // 从 getManagedOrganizations 接口加载校友会列表（roles 缓存无权限时兜底）
    async loadAlumniAssociationListFromApi() {
        try {
            const res = await userApi.getManagedOrganizations({ type: 0 })
            const list = (res?.data?.data ?? res?.data ?? []) || []
            if (!Array.isArray(list) || list.length === 0) {
                this.setData({ alumniAssociationList: [], hasAlumniAdminPermission: false })
                return
            }
            const alumniAssociationList = list.map(org => ({
                id: org.id,
                alumniAssociationId: org.id,
                alumniAssociationName: org.name || '校友会',
                organizeId: org.id
            }))
            this.setData({
                alumniAssociationList,
                hasAlumniAdminPermission: true
            })
            refreshUserRoles() // 静默刷新 roles，使后续访问与重新登录一致
            try {
                const detailPromises = alumniAssociationList.map(async alumni => {
                    try {
                        const res = await this.getAlumniAssociationDetail(alumni.alumniAssociationId)
                        if (res?.data?.code === 200 && res?.data?.data) {
                            const d = res.data.data
                            return {
                                ...d,
                                id: d.alumniAssociationId || d.id || alumni.alumniAssociationId,
                                alumniAssociationId: d.alumniAssociationId || alumni.alumniAssociationId,
                                alumniAssociationName: d.associationName || d.name || alumni.alumniAssociationName,
                                organizeId: d.organizeId || alumni.alumniAssociationId
                            }
                        }
                        return alumni
                    } catch {
                        return alumni
                    }
                })
                const detailedList = await Promise.all(detailPromises)
                this.setData({ alumniAssociationList: detailedList })
                this.handleAlumniAssociationSelection(detailedList)
            } catch (e) {
                this.handleAlumniAssociationSelection(alumniAssociationList)
            }
        } catch (err) {
            console.warn('[Debug] 接口兜底加载校友会列表失败:', err)
            this.setData({ alumniAssociationList: [], hasAlumniAdminPermission: false })
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
                hasSingleAlumniAssociation: true,
                loading: true
            })
            console.log('[Debug] 只有一个校友会权限，自动选择:', singleAlumni)
            // 获取校友会详情
            await this.getAlumniAssociationDetail(singleAlumni.alumniAssociationId)
            this.setData({ loading: false })
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
            loading: true
        })

        // 获取校友会详情
        await this.getAlumniAssociationDetail(alumniAssociationId)

        this.setData({ loading: false })
    },

    // 取消选择校友会
    cancelAlumniAssociationSelect() {
        this.setData({ showAlumniAssociationPicker: false })
    },

    // 处理平台选择
    handlePlatformChange(e) {
        const index = e.detail.value
        const platform = this.data.platformList[index]
        if (platform) {
            this.setData({
                platformIndex: index,
                'formData.platformId': platform.platformId,
                'formData.platformName': platform.platformName
            })
        }
    },

    // 调用校友会详情接口
    async getAlumniAssociationDetail(alumniAssociationId) {
        try {
            const res = await associationApi.getAssociationDetail(alumniAssociationId)
            if (res.data && res.data.code === 200 && res.data.data) {
                console.log('[Debug] 获取校友会详情成功:', res.data.data)
                let alumniAssociationDetail = res.data.data

                // 处理背景图（支持字符串和数组形式）
                if (alumniAssociationDetail.bgImg && alumniAssociationDetail.bgImg.trim()) {
                    try {
                        // 尝试解析为数组
                        const bgImgArray = JSON.parse(alumniAssociationDetail.bgImg)
                        if (Array.isArray(bgImgArray) && bgImgArray.length > 0) {
                            // 取数组第一个元素作为背景图
                            alumniAssociationDetail.bgImg = config.getImageUrl(bgImgArray[0])
                        } else {
                            // 如果不是有效数组，直接使用
                            alumniAssociationDetail.bgImg = config.getImageUrl(alumniAssociationDetail.bgImg)
                        }
                    } catch (e) {
                        // 解析失败，直接使用
                        alumniAssociationDetail.bgImg = config.getImageUrl(alumniAssociationDetail.bgImg)
                    }
                } else {
                    // 使用默认背景图
                    alumniAssociationDetail.bgImg = this.data.defaultBackground
                }

                // 处理 Logo
                if (alumniAssociationDetail.logo && alumniAssociationDetail.logo.trim()) {
                    alumniAssociationDetail.logo = config.getImageUrl(alumniAssociationDetail.logo)
                } else {
                    // 使用默认头像
                    alumniAssociationDetail.logo = this.data.defaultAlumniAvatar
                }

                this.setData({
                    alumniAssociationDetail: alumniAssociationDetail
                })
            } else {
                console.error('[Debug] 获取校友会详情失败:', res)
                wx.showToast({
                    title: res.data?.msg || '获取校友会详情失败',
                    icon: 'none'
                })
            }
            return res
        } catch (error) {
            console.error('[Debug] 获取校友会详情异常:', error)
            wx.showToast({
                title: '网络异常，请重试',
                icon: 'none'
            })
            throw error
        }
    },

    // 处理表单输入
    handleInput(e) {
        const field = e.currentTarget.dataset.field
        const value = e.detail.value
        this.setData({
            [`formData.${field}`]: value
        })
    },



    // 关闭所有下拉列表
    closeAllDropdowns() {
        // 这里可以添加关闭下拉列表的逻辑
    },

    // 提交表单
    async submitForm() {
        const { formData, selectedAlumniAssociationId } = this.data

        // 表单验证
        if (!selectedAlumniAssociationId) {
            wx.showToast({ title: '请选择校友会', icon: 'none' })
            return
        }
        if (!formData.platformId) {
            wx.showToast({ title: '请选择校促会', icon: 'none' })
            return
        }

        try {
            // 调用申请加入校促会接口
            const res = await associationApi.applyJoinPlatform({
                alumniAssociationId: selectedAlumniAssociationId,
                platformId: formData.platformId
            })

            if (res.data && res.data.code === 200) {
                wx.showToast({ title: '提交成功', icon: 'success' })
                // 提交成功后返回上一页
                setTimeout(() => {
                    wx.navigateBack()
                }, 1500)
            } else {
                wx.showToast({ title: res.data?.msg || '提交失败', icon: 'none' })
            }
        } catch (error) {
            console.error('提交申请失败:', error)
            wx.showToast({ title: '网络异常，请重试', icon: 'none' })
        }
    },

    // 跳转到反馈页面
    goToFeedback() {
        wx.navigateTo({
            url: '/pages/feedback/feedback?type=1&title=' + encodeURIComponent('申请加入校促会遇到问题')
        })
    }
})