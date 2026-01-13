const { schoolApi, localPlatformApi, userApi, associationApi } = require('../../../api/api.js')
const app = getApp()

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
            associationName: '',
            schoolId: '',
            schoolName: '',
            platformId: '',
            platformName: '',
            contactName: '',
            contactPhone: '',

            applicationReason: '',
            presidentWxId: '',
            location: ''
        },

        // 搜索结果列表
        schoolList: [],
        platformList: [],

        // 控制显示
        showSchoolResults: false,
        showPlatformResults: false,

        // 其他成员列表
        members: [],

        loading: false,
        submitting: false
    },

    onLoad(options) {
        // 创建搜索防抖函数
        this.searchSchoolDebounced = debounce(this.searchSchool, 500)
        this.searchPlatformDebounced = debounce(this.searchPlatform, 500)

        this.loadInitialData()
    },

    async loadInitialData() {
        this.setData({ loading: true })
        try {
            await this.loadUserInfo()
        } catch (error) {
            console.error('加载初始化数据失败:', error)
        } finally {
            this.setData({ loading: false })
        }
    },

    async loadUserInfo() {
        try {
            const res = await userApi.getUserInfo()
            if (res.data && res.data.code === 200) {
                const userInfo = res.data.data
                this.setData({
                    'formData.contactName': userInfo.name || userInfo.realName || userInfo.nickname || '',
                    'formData.contactPhone': userInfo.phone || userInfo.mobile || '',
                    'formData.presidentWxId': userInfo.userId || userInfo.id
                })
            }
        } catch (e) {
            console.error('获取用户信息失败', e)
        }
    },

    // --- 下拉框控制 ---

    closeAllDropdowns() {
        this.setData({
            showSchoolResults: false,
            showPlatformResults: false
        })
    },

    preventBubble() {
        // 阻止冒泡
    },

    // 通用输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field
        this.setData({
            [`formData.${field}`]: e.detail.value
        })
    },

    // --- 学校搜索处理 ---

    handleSchoolInput(e) {
        const value = e.detail.value
        this.setData({
            'formData.schoolName': value,
            'formData.schoolId': '', // 清空ID，因为修改了名称
            showSchoolResults: true,
            showPlatformResults: false // 确保另一个关闭
        })

        if (value.trim()) {
            this.searchSchoolDebounced(value)
        } else {
            this.setData({ schoolList: [] })
        }
    },

    handleSchoolFocus() {
        // 聚焦时如果已有内容，也展示结果
        this.setData({
            showPlatformResults: false // 关闭另一个
        })

        if (this.data.formData.schoolName) {
            this.setData({ showSchoolResults: true })
            if (this.data.schoolList.length === 0) {
                this.searchSchool(this.data.formData.schoolName)
            }
        }
    },

    async searchSchool(keyword) {
        if (!keyword) return
        try {
            const res = await schoolApi.getSchoolPage({
                current: 1,
                pageSize: 20,
                schoolName: keyword.trim()
            })
            if (res.data && res.data.code === 200) {
                this.setData({
                    schoolList: res.data.data.records || []
                })
            }
        } catch (e) {
            console.error('搜索学校失败', e)
        }
    },

    selectSchool(e) {
        const index = e.currentTarget.dataset.index
        const school = this.data.schoolList[index]
        this.setData({
            'formData.schoolId': school.schoolId,
            'formData.schoolName': school.schoolName,
            showSchoolResults: false
        })
    },

    // --- 地区(校处会)搜索处理 ---

    handlePlatformInput(e) {
        const value = e.detail.value
        this.setData({
            'formData.platformName': value,
            'formData.platformId': '', // 清空ID
            showPlatformResults: true,
            showSchoolResults: false // 确保另一个关闭
        })

        if (value.trim()) {
            this.searchPlatformDebounced(value)
        } else {
            this.setData({ platformList: [] })
        }
    },

    handlePlatformFocus() {
        this.setData({
            showSchoolResults: false // 关闭另一个
        })

        if (this.data.formData.platformName) {
            this.setData({ showPlatformResults: true })
            if (this.data.platformList.length === 0) {
                this.searchPlatform(this.data.formData.platformName)
            }
        }
    },

    async searchPlatform(keyword) {
        if (!keyword) return
        try {
            const res = await localPlatformApi.getLocalPlatformPage({
                current: 1,
                pageSize: 20,
                platformName: keyword.trim()
            })
            if (res.data && res.data.code === 200) {
                this.setData({
                    platformList: res.data.data.records || []
                })
            }
        } catch (e) {
            console.error('搜索校处会失败', e)
        }
    },

    selectPlatform(e) {
        const index = e.currentTarget.dataset.index
        const platform = this.data.platformList[index]
        const location = platform.city || platform.location || platform.platformName

        this.setData({
            'formData.platformId': platform.platformId,
            'formData.platformName': platform.platformName,
            'formData.location': location,
            showPlatformResults: false
        })
    },

    // --- 成员和其他逻辑 ---

    addMember() {
        const members = this.data.members
        members.push({ name: '', role: '' })
        this.setData({ members })
    },

    deleteMember(e) {
        const index = e.currentTarget.dataset.index
        const members = this.data.members
        members.splice(index, 1)
        this.setData({ members })
    },

    handleMemberInput(e) {
        const { index, field } = e.currentTarget.dataset
        const value = e.detail.value
        const members = this.data.members
        members[index][field] = value
        this.setData({ members })
    },

    async submitForm() {
        if (this.data.submitting) return

        const { formData, members } = this.data

        if (!formData.associationName) {
            wx.showToast({ title: '请输入校友会名称', icon: 'none' })
            return
        }
        if (!formData.schoolId) {
            wx.showToast({ title: '请选择并点击学校', icon: 'none' })
            return
        }
        if (!formData.platformId) {
            wx.showToast({ title: '请选择并点击相关地区', icon: 'none' })
            return
        }

        const submitData = {
            associationName: formData.associationName,
            schoolId: formData.schoolId,
            platformId: formData.platformId,
            presidentWxId: formData.presidentWxId,
            presidentName: formData.contactName,
            contactInfo: formData.contactPhone,
            location: formData.location || formData.platformName,
            applicationReason: formData.applicationReason,
            attachmentIds: [],
            initialMembers: members.map(m => ({
                name: m.name,
                roleId: 0,
                roleName: m.role
            }))
        }

        console.log('提交申请数据:', submitData)

        this.setData({ submitting: true })

        try {
            const res = await associationApi.applyCreateAssociation(submitData)

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
