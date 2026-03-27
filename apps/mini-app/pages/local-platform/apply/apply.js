// pages/local-platform/apply/apply.js
const { associationApi, localPlatformApi, userApi, myApplicationRecordApi } = require('../../../api/api.js')
const { refreshUserRoles } = require('../../../utils/auth.js')
const config = require('../../../utils/config.js')
const MY_APPLICATION_RECORD_LIST_NEED_REFRESH_KEY = 'MY_APPLICATION_RECORD_LIST_NEED_REFRESH'

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
        platformIndex: -1,
        fromMyRecord: false,
        myRecordType: '',
        myRecordId: '',
        fixedAlumniAssociationId: '',
        fixedPlatformId: ''
    },

    async onLoad(options) {
        // 如果从列表页传递了平台名称，则自动填充
        this.platformNameFromList = options.platformName ? decodeURIComponent(options.platformName) : null
        const fromMyRecord = options.fromMyRecord === '1'
        const fixedAlumniAssociationId = options.alumniAssociationId
          ? decodeURIComponent(options.alumniAssociationId)
          : ''
        const fixedPlatformId = options.platformId ? decodeURIComponent(options.platformId) : ''
        this.setData({
            fromMyRecord,
            myRecordType: decodeURIComponent(options.recordType || ''),
            myRecordId: decodeURIComponent(options.recordId || ''),
            fixedAlumniAssociationId,
            fixedPlatformId,
        })
        if (this.platformNameFromList) {
            this.setData({
                'formData.platformName': this.platformNameFromList
            })
        }
        // 初始化页面数据
        await this.initPage()
        if (fromMyRecord && fixedAlumniAssociationId) {
            const fixed = this.data.alumniAssociationList.find(
                item => String(item.alumniAssociationId) === String(fixedAlumniAssociationId)
            )
            this.setData({
                selectedAlumniAssociationId: fixedAlumniAssociationId,
                selectedAlumniAssociationName: fixed?.alumniAssociationName || '校友会',
                hasSingleAlumniAssociation: true,
            })
            await this.getAlumniAssociationDetail(fixedAlumniAssociationId)
        }
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
                if (this.data.fixedPlatformId) {
                    const fixedIndex = platformList.findIndex(
                        item => String(item.platformId) === String(this.data.fixedPlatformId)
                    )
                    if (fixedIndex !== -1) {
                        const p = platformList[fixedIndex]
                        this.setData({
                            platformIndex: fixedIndex,
                            'formData.platformId': p.platformId,
                            'formData.platformName': p.platformName,
                        })
                    }
                }
            }
        } catch (e) {
            console.error('加载平台列表失败', e)
        }
    },

    // 加载校友会列表
    // 优先使用 getManagedOrganizations 接口（服务端为准），避免 roles 缓存未更新或缺少 organizeId 导致列表为空
    async loadAlumniAssociationList() {
        try {
            console.log('[Debug] 开始加载校友会列表（优先从接口获取）')
            await this.loadAlumniAssociationListFromApi()
        } catch (error) {
            console.error('[Debug] 加载校友会列表失败:', error)
            this.setData({
                alumniAssociationList: [],
                hasAlumniAdminPermission: false
            })
        }
    },

    // 从 users/managed-organizations?type=0 接口加载校友会列表（与 member 页一致）
    async loadAlumniAssociationListFromApi() {
        try {
            const res = await userApi.getManagedOrganizations({ type: 0 })
            if (!res.data || res.data.code !== 200) {
                this.setData({ alumniAssociationList: [], hasAlumniAdminPermission: false })
                return
            }
            const organizationList = res.data.data || []
            if (!Array.isArray(organizationList) || organizationList.length === 0) {
                this.setData({ alumniAssociationList: [], hasAlumniAdminPermission: false })
                return
            }
            const alumniAssociationList = organizationList.map(org => {
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
            this.setData({
                alumniAssociationList,
                hasAlumniAdminPermission: true
            })
            refreshUserRoles() // 静默刷新 roles，使后续访问与重新登录一致
            this.handleAlumniAssociationSelection(alumniAssociationList)
        } catch (err) {
            console.warn('[Debug] 加载校友会列表失败:', err)
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

        const app = getApp()
        const userData = app.globalData?.userData || {}
        const applicantWxId = userData.wxId || userData.wx_id || userData.userId || wx.getStorageSync('userId')
        if (!applicantWxId) {
            wx.showToast({ title: '请先登录', icon: 'none' })
            return
        }

        try {
            let res
            if (this.data.fromMyRecord && this.data.myRecordId) {
                // 编辑场景仅允许修改 platformId
                res = await myApplicationRecordApi.update({
                    recordType: this.data.myRecordType || 'ALUMNI_ASSOCIATION_JOIN_LOCAL_PLATFORM',
                    recordId: this.data.myRecordId,
                    payload: {
                        platformId: formData.platformId,
                    },
                })
            } else {
                // 调用申请加入校促会接口
                res = await associationApi.applyJoinPlatform({
                    alumniAssociationId: selectedAlumniAssociationId,
                    platformId: formData.platformId,
                    applicantWxId: applicantWxId
                })
            }

            if (res.data && res.data.code === 200) {
                if (this.data.fromMyRecord && this.data.myRecordId) {
                    wx.setStorageSync(MY_APPLICATION_RECORD_LIST_NEED_REFRESH_KEY, Date.now())
                }
                wx.showToast({ title: this.data.fromMyRecord ? '修改成功' : '提交成功', icon: 'success' })
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