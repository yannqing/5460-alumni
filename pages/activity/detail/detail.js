// pages/activity/detail/detail.js
const MOCK_DETAIL = {
  id: 1,
  title: '2025年度校友联谊会',
  cover: 'https://cdn.example.com/activity/detail-hero.png',
  organizer: '南京大学校友总会',
  location: '南京国际会议中心',
  address: '南京市建邺区江东中路 300 号',
  startTime: '2025-12-15 14:00',
  endTime: '2025-12-15 18:00',
  participantCount: 156,
  maxParticipant: 200,
  description: '欢迎各地校友参加年度联谊会，现场将发布校友年度成果、设立产业对接专区以及校友之夜。',
  isJoined: false,
  agenda: [
    { time: '13:30', title: '签到入场' },
    { time: '14:00', title: '开幕致辞 & 年度发布' },
    { time: '15:30', title: '圆桌论坛：共建校友产业生态' },
    { time: '17:00', title: '自由交流 & 校友之夜' }
  ],
  contact: {
    name: '李秘书',
    phone: '13800000000',
    wechat: 'alumni-secretary'
  },
  reminder: '活动支持电子签到，请提前准备校友码，正装出席'
}

Page({
  data: {
    activityId: '',
    activityInfo: {},
    loading: true,
    buttonLoading: false
  },

  onLoad(options) {
    this.setData({ activityId: options.id || '' }, () => {
      this.loadDetail()
    })
  },

  loadDetail() {
    this.setData({ loading: true })
    setTimeout(() => {
      this.setData({
        activityInfo: MOCK_DETAIL,
        loading: false
      })
    }, 300)
  },

  joinActivity() {
    const { activityInfo, buttonLoading } = this.data
    if (activityInfo.isJoined || buttonLoading) return
    this.setData({ buttonLoading: true })
    setTimeout(() => {
      this.setData({
        activityInfo: {
          ...activityInfo,
          isJoined: true,
          participantCount: activityInfo.participantCount + 1
        },
        buttonLoading: false
      })
      wx.showToast({
        title: '报名成功',
        icon: 'success'
      })
    }, 400)
  },

  handleCall() {
    const { phone } = this.data.activityInfo.contact || {}
    if (!phone) return
    wx.makePhoneCall({ phoneNumber: phone })
  },

  handleCopyWechat() {
    const { wechat } = this.data.activityInfo.contact || {}
    if (!wechat) return
    wx.setClipboardData({ data: wechat })
  },

  openMap() {
    const { location, address } = this.data.activityInfo
    wx.openLocation({
      latitude: 32.0104,
      longitude: 118.7353,
      name: location,
      address
    })
  },

  onShareAppMessage() {
    const { title, id } = this.data.activityInfo
    return {
      title: `我报名了：${title}`,
      path: `/pages/activity/detail/detail?id=${id}`
    }
  }
})
