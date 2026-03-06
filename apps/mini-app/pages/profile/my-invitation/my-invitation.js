// pages/profile/my-invitation/my-invitation.js
const { get } = require('../../../utils/request.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    inviteCount: 0,
    myRank: 0,
    list: [],
    loading: false
  },

  onLoad() {
    this.loadMyInvitationList()
  },

  onShow() {
    // 返回页面时刷新数据
    this.loadMyInvitationList()
  },

  loadMyInvitationList() {
    const userData = app.globalData.userData || {}
    const wxId =
      userData.wxId ||
      userData.userId ||
      userData.wx_id ||
      userData.id ||
      wx.getStorageSync('userId') ||
      ''

    if (!wxId) {
      this.setData({
        inviteCount: 0,
        myRank: 0,
        list: []
      })
      return
    }

    this.setData({ loading: true })

    get('/invitation/my-list', { wxId })
      .then((res) => {
        const resData = res && res.data ? res.data : {}
        if (resData.code === 200 && resData.data) {
          const { inviteCount = 0, myRank = 0, list = [] } = resData.data

          const formattedList = (Array.isArray(list) ? list : []).map((item) => {
            const avatarUrl = item.avatar
              ? (config.getImageUrl ? config.getImageUrl(item.avatar) : item.avatar)
              : config.defaultAvatar

            const displayName = item.inviteeName || item.inviteeNickname || '未填写姓名'

            return {
              ...item,
              avatarUrl,
              displayName
            }
          })

          this.setData({
            inviteCount,
            myRank,
            list: formattedList
          })
        } else {
          this.setData({
            inviteCount: 0,
            myRank: 0,
            list: []
          })
        }
      })
      .catch(() => {
        // 失败时保持当前数据或置空
        this.setData({
          inviteCount: 0,
          myRank: 0,
          list: []
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  goToRankPage() {
    wx.navigateTo({
      url: '/pages/profile/invitation-rank/invitation-rank'
    })
  },

  goToInvitePoster() {
    wx.navigateTo({
      url: '/pages/profile/invitation-poster/invitation-poster'
    })
  }
})
