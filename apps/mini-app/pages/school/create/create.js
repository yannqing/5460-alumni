const { userApi, fileApi, unionApi } = require('../../../api/api.js')
const app = getApp()
const config = require('../../../utils/config.js')

// 防抖函数
function debounce(fn, delay) {
    let timer = null
    return function () {
        const context = this
        const args = arguments
        clearTimeout(timer)
        timer = setTimeout(function () {
            fn.apply(context, args)
        }, delay)
    }
}

Page({
    data: {
        formData: {
            headquartersId: '',
            headquartersName: '',
            createCode: '',
            logo: '',
            description: '',
            contactInfo: '',
            address: '',
            website: '',
            wechatPublicAccount: '',
            email: '',
            phone: '',
            establishedDate: '',
            createdUserId: 0,
            updatedUserId: 0,
            logoType: 'default' // logo来源类型: default, school, upload
        },
        defaultLogo: config.defaultAvatar,
        // 未激活的校友总会列表
        inactiveUnionList: [],
        // 过滤后的校友总会列表
        filteredUnionList: [],
        // 控制显示
        showUnionResults: false,

        loading: false,
        submitting: false,
        defaultAvatar: config.defaultAvatar,
        headerImageUrl: `https://${config.DOMAIN}/upload/images/2026/02/09/9f328fe3-fcad-4019-a379-1a6db70f3a5d.png`
    },

    onLoad(options) {
        // 创建搜索防抖函数
        this.searchUnionDebounced = debounce(this.filterUnions, 500)

        this.loadInitialData()

        // 默认初始化logo为平台默认logo
        this.setData({
            'formData.logo': this.data.defaultLogo
        })
    },

    async loadInitialData() {
        this.setData({ loading: true })
        try {
            await this.loadUserInfo()
            await this.loadInactiveUnions()
        } catch (error) {
            console.error('加载初始化数据失败:', error)
        } finally {
            this.setData({ loading: false })
        }
    },

    // 加载未激活的校友总会列表
    async loadInactiveUnions() {
        try {
            const res = await unionApi.getInactiveUnionPage({
                current: 1,
                pageSize: 10
            })
            if (res.data && res.data.code === 200) {
                const unions = res.data.data.records || []
                this.setData({
                    inactiveUnionList: unions,
                    filteredUnionList: unions
                })
            }
        } catch (e) {
            console.error('加载未激活的校友总会列表失败', e)
        }
    },

    async loadUserInfo() {
        try {
            const res = await userApi.getUserInfo()
            if (res.data && res.data.code === 200) {
                const userInfo = res.data.data
                console.log('获取到的用户信息:', userInfo)

                // 尝试获取用户的 ID
                const userId = userInfo.userId || userInfo.user_id || userInfo.id
                console.log('获取到的用户ID:', userId)

                this.setData({
                    'formData.createdUserId': userId,
                    'formData.updatedUserId': userId,
                    'formData.phone': userInfo.phone || userInfo.mobile || '',
                    'formData.email': userInfo.email || ''
                })
            }
        } catch (e) {
            console.error('获取用户信息失败', e)
        }
    },

    // 通用输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field
        this.setData({
            [`formData.${field}`]: e.detail.value
        })
    },

    // --- 下拉框控制 ---

    closeAllDropdowns() {
        this.setData({
            showUnionResults: false
        })
    },

    preventBubble() {
        // 阻止冒泡
    },

    // --- 校友总会搜索处理 ---

    handleUnionInput(e) {
        const value = e.detail.value
        this.setData({
            'formData.headquartersName': value,
            'formData.headquartersId': '', // 清空ID，因为修改了名称
            showUnionResults: true
        })

        if (value.trim()) {
            this.searchUnionDebounced(value)
        } else {
            // 当输入为空时，显示所有校友总会
            this.setData({ filteredUnionList: this.data.inactiveUnionList })
        }
    },

    handleUnionFocus() {
        // 聚焦时展示结果
        this.setData({ showUnionResults: true })
    },

    filterUnions(keyword) {
        if (!keyword) {
            this.setData({ filteredUnionList: this.data.inactiveUnionList })
            return
        }
<<<<<<< HEAD

        const filtered = this.data.inactiveUnionList.filter(union =>
            union.headquartersName.toLowerCase().includes(keyword.toLowerCase())
        )

=======
        
        const filtered = this.data.inactiveUnionList.filter(union => 
            union.headquartersName.toLowerCase().includes(keyword.toLowerCase())
        )
        
>>>>>>> origin/dev
        this.setData({ filteredUnionList: filtered })
    },

    selectUnion(e) {
        const index = e.currentTarget.dataset.index
        const union = this.data.filteredUnionList[index]

        const updateData = {
            'formData.headquartersId': union.headquartersId,
            'formData.headquartersName': union.headquartersName,
            showUnionResults: false
<<<<<<< HEAD
        }

        // 保存校友总会logo
        if (union.logo) {
            updateData.unionLogoUrl = union.logo
=======
>>>>>>> origin/dev
        }
        
        // 保存校友总会logo
        if (union.logo) {
            updateData.unionLogoUrl = union.logo
        }
        
        // 如果当前选中的是"使用学校logo", 则实时更新预览图和提交用的logo地址
        if (this.data.formData.logoType === 'school' && union.logo) {
            updateData['formData.logo'] = union.logo
        }
        
        this.setData(updateData)
    },



    // 处理logo类型切换
    handleLogoTypeChange(e) {
        const type = e.detail.value
        let logo = ''

        if (type === 'default') {
            logo = this.data.defaultLogo
        } else if (type === 'school') {
            // 使用校友总会logo
            if (this.data.unionLogoUrl) {
                logo = this.data.unionLogoUrl
            } else {
                logo = '' // 未选校友总会时不展示预览
            }
        } else if (type === 'upload') {
            // 如果切到上传,保留原来的logo或者为空
            logo = this.data.formData.logoType === 'upload' ? this.data.formData.logo : ''
        }

        this.setData({
            'formData.logoType': type,
            'formData.logo': logo
        })
    },

    // --- logo上传处理 ---

    async chooseLogo() {
        // 如果当前不是上传模式,点击选择图片会自动切到上传模式
        if (this.data.formData.logoType !== 'upload') {
            this.setData({
                'formData.logoType': 'upload'
            })
        }
        try {
            // 选择图片
            const chooseRes = await new Promise((resolve, reject) => {
                wx.chooseMedia({
                    count: 1,
                    mediaType: ['image'],
                    success: resolve,
                    fail: reject
                })
            })

            const tempFilePath = chooseRes.tempFiles?.[0]?.tempFilePath
            if (!tempFilePath) {
                return
            }

            // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
            const fileSize = chooseRes.tempFiles?.[0]?.size || 0
            const maxSize = 10 * 1024 * 1024 // 10MB
            if (fileSize > maxSize) {
                wx.showToast({
                    title: '图片大小不能超过10MB',
                    icon: 'none'
                })
                return
            }

            // 显示上传中提示
            wx.showLoading({
                title: '上传中...',
                mask: true
            })

            // 获取原始文件名（如果有）
            const originalName = chooseRes.tempFiles?.[0]?.name || 'logo.jpg'

            // 直接调用公共的文件上传方法
            const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

            if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
                // 获取返回的图片URL
                const rawImageUrl = uploadRes.data.fileUrl || ''
                if (rawImageUrl) {
                    // 使用 config.getImageUrl 处理图片URL，确保是完整的URL
                    const config = require('../../../utils/config.js')
                    const imageUrl = config.getImageUrl(rawImageUrl)
                    // 更新表单中的logo URL
                    this.setData({ 'formData.logo': imageUrl })
                    wx.showToast({
                        title: '上传成功',
                        icon: 'success'
                    })
                } else {
                    wx.showToast({
                        title: '上传失败，未获取到图片地址',
                        icon: 'none'
                    })
                }
            } else {
                wx.showToast({
                    title: uploadRes?.msg || '上传失败',
                    icon: 'none'
                })
            }
        } catch (error) {
            // 显示具体的错误信息
            const errorMsg = error?.msg || error?.message || '上传失败，请重试'
            wx.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 2000
            })
        } finally {
            wx.hideLoading()
        }
    },

    // 删除已上传的logo
    deleteLogo() {
        this.setData({
            'formData.logo': ''
        })
    },

    async submitForm() {
        if (this.data.submitting) { return }

        const { formData } = this.data

        if (!formData.headquartersId) {
            wx.showToast({ title: '请选择校友总会', icon: 'none' })
            return
        }
        if (!formData.createCode) {
            wx.showToast({ title: '请输入创建码/邀请码', icon: 'none' })
            return
        }

        // 验证日期格式
        if (formData.establishedDate) {
            const dateRegex = /^\d{4}-\d{2}-\d{2}$/
            if (!dateRegex.test(formData.establishedDate)) {
                wx.showToast({ title: '请输入正确的日期格式 (YYYY-MM-DD)', icon: 'none' })
                return
            }

            // 验证日期是否有效
            const date = new Date(formData.establishedDate)
            if (isNaN(date.getTime())) {
                wx.showToast({ title: '请输入有效的日期', icon: 'none' })
                return
            }
        }

        const submitData = {
            headquartersId: formData.headquartersId,
            createCode: formData.createCode,
            description: formData.description || undefined,
            contactInfo: formData.contactInfo ? JSON.stringify({content: formData.contactInfo}) : undefined,
            address: formData.address || undefined,
            website: formData.website || undefined,
            wechatPublicAccount: formData.wechatPublicAccount || undefined,
            email: formData.email || undefined,
            phone: formData.phone || undefined,
            establishedDate: formData.establishedDate || undefined,
            createdUserId: formData.createdUserId || undefined,
            updatedUserId: formData.updatedUserId || undefined
        }

        // 确保logo字段在非默认情况下总是传递
        if (formData.logoType !== 'default') {
            submitData.logo = formData.logo || ''
        }

        console.log('最终提交数据:', submitData)

        this.setData({ submitting: true })

        try {
            const res = await unionApi.applyActivate(submitData)

            if (res.data && res.data.code === 200) {
                wx.showToast({
                    title: '申请已提交',
                    icon: 'success'
                })
                setTimeout(() => {
                    wx.navigateBack()
                }, 1500)
            } else {
                wx.showToast({
                    title: res.data?.message || '提交失败',
                    icon: 'none'
                })
            }
        } catch (error) {
            console.error('提交申请失败:', error)
            wx.showToast({
                title: '提交失败，请重试',
                icon: 'none'
            })
        } finally {
            this.setData({ submitting: false })
        }
    }
})