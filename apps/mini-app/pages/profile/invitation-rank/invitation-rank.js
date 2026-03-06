// pages/profile/invitation-rank/invitation-rank.js
const { get } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    myInviteCount: 0,
    myRank: 0,
    myAvatarUrl: '',
    myName: '',
    mySchool: '',
    rankList: [],
    loading: false
  },

  onLoad() {
    this.loadInvitationRank()
  },

  onShow() {
    this.loadInvitationRank()
  },

  loadInvitationRank() {
    const userData = app.globalData.userData || {}
    const wxId =
      userData.wxId ||
      userData.userId ||
      userData.wx_id ||
      userData.id ||
      wx.getStorageSync('userId') ||
      ''

    this.setData({ loading: true })

    get('/invitation/rank', { wxId })
      .then((res) => {
        const resData = res && res.data ? res.data : {}
        if (resData.code === 200 && resData.data) {
          let {
            myInviteCount = 0,
            myRank = 0,
            myAvatar = '',
            myName = '',
            mySchool = '',
            rankList = []
          } = resData.data

          if (!Array.isArray(rankList)) {
            rankList = []
          }

          const myAvatarUrl = myAvatar
            ? (config.getImageUrl ? config.getImageUrl(myAvatar) : myAvatar)
            : config.defaultAvatar

          const formattedRankList = rankList.map((item) => {
            const avatarUrl = item.avatar
              ? (config.getImageUrl ? config.getImageUrl(item.avatar) : item.avatar)
              : config.defaultAvatar

            return {
              ...item,
              avatarUrl
            }
          })

          this.setData({
            myInviteCount,
            myRank,
            myAvatarUrl,
            myName,
            mySchool,
            rankList: formattedRankList
          })
        } else {
          this.setData({
            myInviteCount: 0,
            myRank: 0,
            myAvatarUrl: '',
            myName: '',
            mySchool: '',
            rankList: []
          })
        }
      })
      .catch(() => {
        this.setData({
          myInviteCount: 0,
          myRank: 0,
          myAvatarUrl: '',
          myName: '',
          mySchool: '',
          rankList: []
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})

