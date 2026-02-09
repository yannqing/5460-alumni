// pages/alumni/detail/detail.js
const config = require('../../../utils/config.js')
const { alumniApi } = require('../../../api/api.js')
const { FollowTargetType, toggleFollow } = require('../../../utils/followHelper.js')

Page({
  data: {
    alumniId: '',
    alumniInfo: null,
    defaultAvatar: config.defaultAvatar
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
  /**
   * 映射校友数据
   * 兼容 profile/edit/edit 的字段返回，并处理空值
   */
  mapAlumniData(data) {
    const config = require('../../../utils/config.js')

    // 空值处理函数
    const formatValue = (val) => {
      if (val === null || val === undefined || val === '' || val === '未设置') {
        return {
          value: '不方便透露',
          isNull: true
        }
      }
      return {
        value: val,
        isNull: false
      }
    }

    // 处理头像
    let avatarUrl = data.avatarUrl || ''
    if (avatarUrl) {
      avatarUrl = config.getImageUrl(avatarUrl)
    } else {
<<<<<<< HEAD
      // 使用本地默认头像
      avatarUrl = config.defaultAvatar
=======
      avatarUrl = '/assets/avatar/default_avatar.jpeg'
>>>>>>> origin/dev
    }

    // 处理性别
    const genderMap = { 0: '未知', 1: '男', 2: '女' }
    const gender = genderMap[data.gender] || '未知'

    // 婚姻状态映射
    const maritalStatusOptions = ['未知', '未婚', '已婚', '离异', '丧偶']
    const maritalStatus = maritalStatusOptions[data.maritalStatus] || '未知'

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

    // 处理教育经历
    const educationList = (data.alumniEducationList || []).map(edu => {
      const schoolName = edu.schoolInfo?.schoolName || edu.schoolName || '未知学校'
      const rawLogo = edu.schoolInfo?.logo || edu.logo || ''

      return {
        schoolId: edu.schoolInfo?.schoolId || edu.schoolId,
        logo: rawLogo ? config.getImageUrl(rawLogo) : '',
        schoolName: schoolName,
        enrollmentYear: edu.enrollmentYear || '',
        graduationYear: edu.graduationYear || '',
        department: edu.department || '',
        major: edu.major || '',
        className: edu.className || '',
        educationLevel: edu.educationLevel || '',
        type: edu.type === 1 ? '主要经历' : '次要经历'
      }
    })

    // 处理工作经历
    const workExperienceList = (data.workExperienceList || []).map(work => ({
      companyName: work.companyName || '未知公司',
      position: work.position || '',
      industry: work.industry || '',
      startDate: work.startDate || '',
      endDate: work.endDate || (work.isCurrent === 1 ? '至今' : ''),
      isCurrent: work.isCurrent === 1,
      workDescription: work.workDescription || ''
    }))

    // 格式化展示字段
    return {
      id: data.id,
      nickname: formatValue(data.nickname || data.name),
      name: formatValue(data.name),
      avatarUrl: avatarUrl,
      background: data.background ? config.getImageUrl(data.background) : '',
      gender: formatValue(gender),
      age: formatValue(age ? `${age}岁` : ''),
      zodiac: formatValue(constellation),
      constellation: formatValue(constellation),
      birthDate: formatValue(data.birthDate),
      phone: formatValue(data.phone),
      signature: formatValue(data.signature),
      description: formatValue(data.description || data.bio),
      wxNum: formatValue(data.wxNum),
      qqNum: formatValue(data.qqNum),
      email: formatValue(data.email),
      maritalStatus: formatValue(maritalStatus),
      personalSpecialty: formatValue(data.personalSpecialty),

      // 位置信息
      originProvince: formatValue(data.originProvince),
      curContinent: formatValue(data.curContinent),
      curCountry: formatValue(data.curCountry),
      curProvince: formatValue(data.curProvince),
      curCity: formatValue(data.curCity),
      curCounty: formatValue(data.curCounty),
      address: formatValue(data.address),

      educationList: educationList.length > 0 ? educationList : null,
      workExperienceList: workExperienceList.length > 0 ? workExperienceList : null,
      isFollowed: data.isFollowed || false,
      identifyType: data.identifyType,
      schoolId: data.schoolId,
      school: data.school || '母校'
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
