const { schoolApi, localPlatformApi, userApi, associationApi, alumniApi, fileApi } = require('../../../api/api.js')
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
            associationName: '',
            schoolId: '',
            schoolName: '',
            platformId: '',
            platformName: '',
            chargeName: '',
            contactInfo: '',
            logo: '',

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
        
        // 成员搜索结果
        memberSearchResults: [],
        
        // 申请材料
        attachments: [],
        
        // 背景图
        bgImages: [],

        loading: false,
        submitting: false,
        defaultAvatar: config.defaultAvatar
    },

    onLoad(options) {
        // 创建搜索防抖函数
        this.searchSchoolDebounced = debounce(this.searchSchool, 500)
        this.searchPlatformDebounced = debounce(this.searchPlatform, 500)
        this.searchAlumniDebounced = debounce(this.searchAlumni, 500)

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
                console.log('获取到的用户信息:', userInfo)
                
                // 尝试获取用户的 wx_id，检查所有可能的字段名
                const userId = userInfo.wxId || userInfo.wx_id || userInfo.userId || userInfo.user_id || userInfo.id
                console.log('获取到的用户ID:', userId)
                
                this.setData({
                    'formData.chargeName': userInfo.name || userInfo.realName || userInfo.nickname || '',
                    'formData.contactInfo': userInfo.phone || userInfo.mobile || '',
                    'formData.presidentWxId': userId
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

    // --- 地区(校促会)搜索处理 ---

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
            console.error('搜索校促会失败', e)
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

    // --- 成员姓名搜索处理 ---

    handleMemberNameInput(e) {
        const { index } = e.currentTarget.dataset
        const value = e.detail.value
        const members = this.data.members
        members[index].name = value
        
        // 确保memberSearchResults数组长度足够
        const memberSearchResults = [...this.data.memberSearchResults]
        while (memberSearchResults.length <= index) {
            memberSearchResults.push([])
        }
        
        this.setData({ 
            members,
            memberSearchResults
        })

        if (value.trim()) {
            this.searchAlumniDebounced(value, index)
        } else {
            memberSearchResults[index] = []
            this.setData({ memberSearchResults })
        }
    },

    handleMemberNameFocus(e) {
        const { index } = e.currentTarget.dataset
        const members = this.data.members
        const memberName = members[index].name
        
        // 确保memberSearchResults数组长度足够
        const memberSearchResults = [...this.data.memberSearchResults]
        while (memberSearchResults.length <= index) {
            memberSearchResults.push([])
        }
        
        this.setData({ memberSearchResults })

        if (memberName) {
            this.setData({ memberSearchResults })
            if (memberSearchResults[index].length === 0) {
                this.searchAlumni(memberName, index)
            }
        }
    },

    async searchAlumni(keyword, index) {
        if (!keyword) return
        try {
            const res = await alumniApi.queryAlumniList({
                current: 1,
                pageSize: 10,
                name: keyword.trim()
            })
            if (res.data && res.data.code === 200) {
                const memberSearchResults = [...this.data.memberSearchResults]
                while (memberSearchResults.length <= index) {
                    memberSearchResults.push([])
                }
                memberSearchResults[index] = res.data.data.records || []
                this.setData({
                    memberSearchResults
                })
            }
        } catch (e) {
            console.error('搜索校友失败', e)
        }
    },

    selectMember(e) {
        const { index, userIndex } = e.currentTarget.dataset
        const memberSearchResults = this.data.memberSearchResults
        const selectedAlumni = memberSearchResults[index][userIndex]
        
        if (selectedAlumni) {
            const members = this.data.members
            members[index].name = selectedAlumni.name
            members[index].wxId = selectedAlumni.wxId || selectedAlumni.id || selectedAlumni.userId || selectedAlumni.user_id || selectedAlumni.wx_id || 0
            
            // 清空搜索结果，关闭下拉框
            memberSearchResults[index] = []
            
            this.setData({ 
                members,
                memberSearchResults
            })
        }
    },

    // --- 校友会logo上传处理 ---

    async chooseLogo() {
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

    // --- 申请材料上传处理 ---

    async chooseAttachment() {
        try {
            // 选择文件（与校友会logo上传保持一致，使用wx.chooseMedia）
            const chooseRes = await new Promise((resolve, reject) => {
                wx.chooseMedia({
                    count: 5,
                    mediaType: ['image', 'video', 'audio'],
                    success: resolve,
                    fail: reject
                })
            })

            const tempFiles = chooseRes.tempFiles
            if (!tempFiles || tempFiles.length === 0) {
                return
            }

            // 显示上传中提示
            wx.showLoading({
                title: '上传中...',
                mask: true
            })

            const attachments = [...this.data.attachments]

            // 逐个上传文件
            for (const file of tempFiles) {
                const tempFilePath = file.tempFilePath
                const originalName = file.name || 'attachment'

                // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
                const fileSize = file.size || 0
                const maxSize = 10 * 1024 * 1024 // 10MB
                if (fileSize > maxSize) {
                    wx.showToast({
                        title: `${originalName} 大小不能超过10MB`,
                        icon: 'none'
                    })
                    continue
                }

                // 上传文件（与校友会logo上传使用相同的方法）
                const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

                console.log('上传文件结果:', uploadRes)

                if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
                    // 获取返回的文件信息，确保获取到fileId
                    const fileId = uploadRes.data.fileId || uploadRes.data.id || uploadRes.data.file_id || uploadRes.data.id || ''
                    console.log('获取到的fileId:', fileId)
                    
                    if (fileId) {
                        attachments.push({
                            id: fileId,
                            name: originalName
                        })
                    } else {
                        wx.showToast({
                            title: `${originalName} 上传成功但未获取到文件ID`,
                            icon: 'none'
                        })
                    }
                } else {
                    wx.showToast({
                        title: `${originalName} 上传失败: ${uploadRes?.msg || '未知错误'}`,
                        icon: 'none'
                    })
                }
            }

            if (attachments.length > this.data.attachments.length) {
                this.setData({ attachments })
                wx.showToast({
                    title: '上传成功',
                    icon: 'success'
                })
            } else {
                wx.showToast({
                    title: '上传失败',
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

    deleteAttachment(e) {
        const index = e.currentTarget.dataset.index
        const attachments = [...this.data.attachments]
        attachments.splice(index, 1)
        this.setData({ attachments })
    },

    // --- 背景图上传处理 ---

    async chooseBgImage() {
        try {
            // 选择图片
            const chooseRes = await new Promise((resolve, reject) => {
                wx.chooseMedia({
                    count: 5,
                    mediaType: ['image'],
                    success: resolve,
                    fail: reject
                })
            })

            const tempFiles = chooseRes.tempFiles
            if (!tempFiles || tempFiles.length === 0) {
                return
            }

            // 显示上传中提示
            wx.showLoading({
                title: '上传中...',
                mask: true
            })

            const bgImages = [...this.data.bgImages]

            // 逐个上传文件
            for (const file of tempFiles) {
                const tempFilePath = file.tempFilePath
                const originalName = file.name || 'bg-image'

                // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
                const fileSize = file.size || 0
                const maxSize = 10 * 1024 * 1024 // 10MB
                if (fileSize > maxSize) {
                    wx.showToast({
                        title: `${originalName} 大小不能超过10MB`,
                        icon: 'none'
                    })
                    continue
                }

                // 上传文件（与校友会logo上传使用相同的方法）
                const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

                console.log('上传背景图结果:', uploadRes)

                if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
                    // 获取返回的文件信息，确保获取到fileUrl
                    const fileUrl = uploadRes.data.fileUrl || ''
                    console.log('获取到的fileUrl:', fileUrl)
                    
                    if (fileUrl) {
                        bgImages.push({
                            url: fileUrl,
                            name: originalName
                        })
                    } else {
                        wx.showToast({
                            title: `${originalName} 上传成功但未获取到文件URL`,
                            icon: 'none'
                        })
                    }
                } else {
                    wx.showToast({
                        title: `${originalName} 上传失败: ${uploadRes?.msg || '未知错误'}`,
                        icon: 'none'
                    })
                }
            }

            if (bgImages.length > this.data.bgImages.length) {
                this.setData({ bgImages })
                wx.showToast({
                    title: '上传成功',
                    icon: 'success'
                })
            } else {
                wx.showToast({
                    title: '上传失败',
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

    deleteBgImage(e) {
        const index = e.currentTarget.dataset.index
        const bgImages = [...this.data.bgImages]
        bgImages.splice(index, 1)
        this.setData({ bgImages })
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
        // platformId is optional per API specs
        if (!formData.chargeName) {
            wx.showToast({ title: '请输入负责人姓名', icon: 'none' })
            return
        }
        if (!formData.contactInfo) {
            wx.showToast({ title: '请输入负责人电话', icon: 'none' })
            return
        }
        if (!formData.applicationReason) {
            wx.showToast({ title: '请输入申请理由', icon: 'none' })
            return
        }

        // 确保presidentWxId不为空
        if (!formData.presidentWxId) {
            wx.showToast({ title: '提交者的wx_id不能为空', icon: 'none' })
            return
        }

        // 确保所有成员都有有效的 wxId
        for (let i = 0; i < members.length; i++) {
            const member = members[i];
            if (!member.wxId || member.wxId === 0 || member.wxId === '0') {
                wx.showToast({ title: `请通过搜索选择第 ${i + 1} 位成员，确保选择有效用户`, icon: 'none' });
                return;
            }
        }

        console.log('准备提交的用户ID:', formData.presidentWxId)
        console.log('用户ID类型:', typeof formData.presidentWxId)

        // 提取申请材料的ID，使用字符串类型
        const attachmentIds = this.data.attachments.map(a => {
            const id = a.id || ''
            console.log('原始文件ID:', id, '类型:', typeof id)
            console.log('使用字符串类型文件ID:', id)
            return id
        })
        console.log('最终attachmentIds:', attachmentIds)

        // 提取背景图的URL，直接使用数组形式
        const bgImgArray = this.data.bgImages.map(a => {
            const url = a.url || ''
            return url
        })
        const bgImg = bgImgArray.length > 0 ? bgImgArray : undefined
        console.log('最终bgImg:', bgImg)

        const submitData = {
            associationName: formData.associationName,
            schoolId: formData.schoolId,
            platformId: formData.platformId,
            chargeWxId: formData.presidentWxId,
            chargeName: formData.chargeName,
            chargeRole: '成员',
            contactInfo: formData.contactInfo || undefined,
            location: formData.location || formData.platformName || undefined,
            logo: formData.logo || undefined,
            applicationReason: formData.applicationReason,
            attachmentIds: attachmentIds.length > 0 ? attachmentIds : undefined,
            initialMembers: members.length > 0 ? members.map(m => ({
                wxId: m.wxId,
                name: m.name || undefined,
                role: m.role || undefined
            })) : undefined
        }

        // 只有当bgImg存在时才添加到提交数据中
        if (bgImg !== undefined) {
            submitData.bgImg = bgImg
        }
        
        console.log('最终提交数据:', submitData)

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
