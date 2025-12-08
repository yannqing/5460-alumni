// pages/chat/list/list.js
const config = require('../../../utils/config.js')

Page({
  data: {
    chatList: [],
    allChatList: [],
    refreshing: false,
    loading: false,
    showSidebar: false
  },

  onLoad() {
    this.loadChatList()
  },

  onShow() {
    // 每次显示页面时刷新列表
    this.loadChatList()
  },

  loadChatList() {
    this.setData({ loading: true })
    
    // 模拟数据
    setTimeout(() => {
      // 校友会通知（作为普通聊天显示）
      const mockOfficialAccounts = [
        {
          id: 'oa_1',
          name: '南京大学上海校友会',
          avatar: config.defaultAvatar,
          isVerified: true,
          lastTime: '2小时前',
          unreadCount: 3,
          lastMessage: '【活动通知】本周六校友聚会活动',
          type: 'official'
        },
        {
          id: 'oa_2',
          name: '浙江大学杭州校友会',
          avatar: config.defaultAvatar,
          isVerified: true,
          lastTime: '5小时前',
          unreadCount: 1,
          lastMessage: '【优惠活动】校友专属优惠券上线',
          type: 'official'
        },
        {
          id: 'oa_3',
          name: '清华大学北京校友会',
          avatar: config.defaultAvatar,
          isVerified: true,
          lastTime: '昨天',
          unreadCount: 0,
          lastMessage: '欢迎新加入的校友，请完善个人资料',
          type: 'official'
        },
        {
          id: 'oa_4',
          name: '北京大学校友会',
          avatar: config.defaultAvatar,
          isVerified: true,
          lastTime: '昨天',
          unreadCount: 2,
          lastMessage: '【重要通知】校友会章程更新',
          type: 'official'
        }
      ]

      const mockChatList = [
        {
          id: 1,
          name: '张三',
          avatar: config.defaultAvatar,
          lastMessage: '你好，请问这个活动什么时候开始？',
          lastTime: '10:30',
          unreadCount: 2,
          isMuted: false
        },
        {
          id: 2,
          name: '李四',
          avatar: config.defaultAvatar,
          lastMessage: '好的，我会准时参加的',
          lastTime: '昨天',
          unreadCount: 0,
          isMuted: false
        },
        {
          id: 3,
          name: '王五',
          avatar: config.defaultAvatar,
          lastMessage: '校友会聚餐，一起来吗？',
          lastTime: '昨天',
          unreadCount: 5,
          isMuted: true
        },
        {
          id: 4,
          name: '赵六',
          avatar: config.defaultAvatar,
          lastMessage: '谢谢你的帮助！',
          lastTime: '周一',
          unreadCount: 0,
          isMuted: false
        },
        {
          id: 5,
          name: '南京大学上海校友会',
          avatar: config.defaultAvatar,
          lastMessage: '本周六下午2点有校友活动，欢迎大家参加',
          lastTime: '周二',
          unreadCount: 1,
          isMuted: false
        },
        {
          id: 6,
          name: '孙七',
          avatar: config.defaultAvatar,
          lastMessage: '好的，没问题',
          lastTime: '周三',
          unreadCount: 0,
          isMuted: false
        },
        {
          id: 7,
          name: '周八',
          avatar: config.defaultAvatar,
          lastMessage: '这个优惠券怎么使用？',
          lastTime: '周四',
          unreadCount: 3,
          isMuted: false
        }
      ]
      
      // 合并所有聊天列表，校友会通知放在前面
      const allChatList = [...mockOfficialAccounts, ...mockChatList]
      
      this.setData({
        chatList: mockChatList,
        allChatList: allChatList,
        loading: false
      })
    }, 500)
  },

  onRefresh() {
    this.setData({ refreshing: true })
    this.loadChatList()
    setTimeout(() => {
      this.setData({ refreshing: false })
    }, 1000)
  },

  openChat(e) {
    const { id, type } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/chat/detail/detail?id=${id}&type=${type || 'chat'}`
    })
  },

  toggleSidebar() {
    this.setData({
      showSidebar: !this.data.showSidebar
    })
  },

  navigateToMyAssociations() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-association/my-association'
    })
  },

  navigateToMyFollows() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-follow/my-follow'
    })
  },

  navigateToMyFavorites() {
    this.toggleSidebar()
    wx.navigateTo({
      url: '/pages/my-favorites/my-favorites'
    })
  }
})

