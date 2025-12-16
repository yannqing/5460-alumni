// pages/alumni/detail/detail.js
const config = require('../../../utils/config.js')
const { alumniApi } = require('../../../api/api.js')
const { FollowTargetType, toggleFollow } = require('../../../utils/followHelper.js')

Page({
  data: {
    alumniId: '',
    alumniInfo: null
  },

  onLoad(options) {
    this.setData({ alumniId: options.id })
    this.loadAlumniDetail()
  },

  async loadAlumniDetail() {
    const { alumniId } = this.data

    try {
      const res = await alumniApi.getAlumniInfo(alumniId)
      console.log('校友详情接口返回:', res)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}

        // 数据映射：将后端字段映射为前端所需格式
        const alumniInfo = this.mapAlumniData(data)

        this.setData({ alumniInfo })
      } else {
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载校友详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 数据映射：将后端数据映射为前端格式
  mapAlumniData(data) {
    // 处理头像
    let avatarUrl = data.avatarUrl || ''
    if (avatarUrl) {
      avatarUrl = config.getImageUrl(avatarUrl)
    } else {
      avatarUrl = config.defaultAvatar
    }

    // 处理性别
    const genderMap = {
      0: 'unknown',
      1: 'male',
      2: 'female'
    }
    const gender = genderMap[data.gender] || 'unknown'

    // 计算年龄和星座
    let age = ''
    let constellation = ''
    if (data.birthDate) {
      const birthDate = new Date(data.birthDate)
      const today = new Date()
      age = today.getFullYear() - birthDate.getFullYear()

      const month = birthDate.getMonth() + 1
      const day = birthDate.getDate()
      constellation = this.getZodiacName(data.constellation, month, day)
    }

    // 构建位置信息
    let location = data.address || ''
    if (!location) {
      const locationParts = []
      if (data.curProvince) locationParts.push(data.curProvince)
      if (data.curCity) locationParts.push(data.curCity)
      if (data.curCounty) locationParts.push(data.curCounty)
      location = locationParts.join('') || '未设置'
    }

    // 处理教育经历
    const educationList = (data.alumniEducationList || []).map(edu => ({
      schoolId: edu.schoolId,
      logo: edu.logo ? config.getImageUrl(edu.logo) : '',
      schoolName: edu.schoolName || '',
      province: edu.province || '',
      city: edu.city || '',
      level: edu.level || '',
      enrollmentYear: edu.enrollmentYear || '',
      graduationYear: edu.graduationYear || '',
      department: edu.department || '',
      major: edu.major || '',
      className: edu.className || '',
      educationLevel: edu.educationLevel || '',
    }))

    return {
      id: data.id,
      nickname: data.nickname || data.name || '未知校友',
      name: data.name || '',
      avatarUrl: avatarUrl,
      background: data.background ? config.getImageUrl(data.background) : '',
      gender: gender,
      age: age || '?',
      zodiac: constellation || '未知',
      constellation: constellation || '未知',
      birthDate: data.birthDate || '未设置',
      phone: data.phone || '未设置',
      signature: data.signature || '',
      wxNum: data.wxNum || '未设置',
      qqNum: data.qqNum || '未设置',
      email: data.email || '未设置',
      originProvince: data.originProvince || '未设置',
      curContinent: data.curContinent || '未设置',
      curCountry: data.curCountry || '未设置',
      curProvince: data.curProvince || '未设置',
      curCity: data.curCity || '未设置',
      curCounty: data.curCounty || '未设置',
      address: data.address || '未设置',
      educationList: educationList,
      isFollowed: data.isFollowed || false,
      identifyType: data.identifyType
    }
  },

  // 跳转到私信页面
  goToChat() {
    const { alumniInfo, alumniId } = this.data
    if (!alumniInfo) return

    const name = encodeURIComponent(alumniInfo.nickname)
    const avatar = encodeURIComponent(alumniInfo.avatarUrl)
    
    wx.navigateTo({
      url: `/pages/chat/detail/detail?id=${alumniId}&name=${name}&avatar=${avatar}&type=chat`
    })
  },

  // 获取星座名称
  getZodiacName(constellationCode, month, day) {
    // 如果后端返回了星座代码，可以根据代码映射
    // 否则根据生日计算
    if (constellationCode) {
      const zodiacMap = {
        1: '白羊座', 2: '金牛座', 3: '双子座', 4: '巨蟹座',
        5: '狮子座', 6: '处女座', 7: '天秤座', 8: '天蝎座',
        9: '射手座', 10: '摩羯座', 11: '水瓶座', 12: '双鱼座'
      }
      return zodiacMap[constellationCode] || ''
    }

    if (month && day) {
      return this.getZodiac(month, day)
    }

    return ''
  },

  getZodiac(month, day) {
    const zodiacDates = [
      { name: '水瓶座', start: [1, 20], end: [2, 18] },
      { name: '双鱼座', start: [2, 19], end: [3, 20] },
      { name: '白羊座', start: [3, 21], end: [4, 19] },
      { name: '金牛座', start: [4, 20], end: [5, 20] },
      { name: '双子座', start: [5, 21], end: [6, 21] },
      { name: '巨蟹座', start: [6, 22], end: [7, 22] },
      { name: '狮子座', start: [7, 23], end: [8, 22] },
      { name: '处女座', start: [8, 23], end: [9, 22] },
      { name: '天秤座', start: [9, 23], end: [10, 23] },
      { name: '天蝎座', start: [10, 24], end: [11, 22] },
      { name: '射手座', start: [11, 23], end: [12, 21] },
      { name: '摩羯座', start: [12, 22], end: [1, 19] }
    ]
    
    for (let zodiac of zodiacDates) {
      const [startMonth, startDay] = zodiac.start
      const [endMonth, endDay] = zodiac.end
      
      if (startMonth === endMonth) {
        if (month === startMonth && day >= startDay && day <= endDay) {
          return zodiac.name
        }
      } else if (startMonth < endMonth) {
        if ((month === startMonth && day >= startDay) || (month === endMonth && day <= endDay)) {
          return zodiac.name
        }
      } else {
        if (month === startMonth && day >= startDay || month === endMonth && day <= endDay) {
          return zodiac.name
        }
      }
    }
    return '未知'
  },

  async toggleFollow() {
    const { alumniInfo } = this.data
    if (!alumniInfo || !alumniInfo.wxId) {
      wx.showToast({
        title: '数据加载中，请稍后',
        icon: 'none'
      })
      return
    }

    const isFollowed = alumniInfo.isFollowed || false
    const targetId = alumniInfo.wxId

    // 调用通用关注函数
    const result = await toggleFollow(isFollowed, FollowTargetType.USER, targetId, 1, true)

    // 如果用户取消了操作，不做处理
    if (result.canceled) {
      return
    }

    if (result.success) {
      // 更新关注状态
      this.setData({
        'alumniInfo.isFollowed': !isFollowed,
        'alumniInfo.followStatus': !isFollowed ? 1 : 4
      })

    wx.showToast({
        title: result.message,
      icon: 'success'
    })
    } else {
      wx.showToast({
        title: result.message,
        icon: 'none'
      })
    }
  },

  viewSchoolDetail(e) {
    const schoolId = e.currentTarget.dataset.id
    if (schoolId) {
      wx.navigateTo({
        url: `/pages/school/detail/detail?id=${schoolId}`
      })
    } else {
      // 如果没有ID，可以根据学校名称搜索或跳转到列表页
      wx.showToast({
        title: '母校信息加载中',
        icon: 'loading'
      })
    }
  },

  viewAssociationDetail(e) {
    const associationId = e.currentTarget.dataset.id
    if (associationId) {
      wx.navigateTo({
        url: `/pages/alumni-association/detail/detail?id=${associationId}`
      })
    }
  }
})
