// pages/chat/detail/detail.js
const config = require('../../../utils/config.js')
const { chatApi, alumniApi, associationApi } = require('../../../api/api.js')

Page({
  data: {
    chatId: null,
    chatType: 'chat',
    chatInfo: {
      name: '',
      avatar: '',
      isOnline: false
    },
    myAvatar: '',
    myUserId: null,
    messageList: [],
    inputValue: '',
    hasInput: false,
    scrollIntoView: '',
    showEmoji: false,
    showMoreMenu: false,
    socketConnected: false,
    emojiList: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗', '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈', '👿', '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾', '🤖', '🎃']
  },

  // WebSocket 事件监听器引用
  messageListener: null,
  onlineStatusListener: null,
  connectListener: null,
  disconnectListener: null,

  onLoad(options) {
    const { id, type } = options
    const app = getApp()
    const myUserId = app.globalData.userData?.wxId || wx.getStorageSync('userId')
    let myAvatar = app.globalData.userData?.avatar || ''
    if (myAvatar) {
      myAvatar = config.getImageUrl(myAvatar)
    }
    
    if (id && id !== 'undefined' && id !== 'null') {
      this.setData({ 
        chatId: id,
        chatType: type || 'chat',
        myUserId: myUserId,
        myAvatar: myAvatar
      })
      this.loadChatInfo(id, type)
      this.loadMessages(id)
      this.initWebSocket()
    } else {
      console.error('[ChatDetail] 无效的聊天ID:', id)
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  onUnload() {
    // 页面卸载时移除 WebSocket 监听
    this.removeWebSocketListeners()
  },

  onShow() {
    // 页面显示时刷新在线状态
    this.refreshOnlineStatus()
  },

  /**
   * 初始化 WebSocket 监听
   */
  initWebSocket() {
    const app = getApp()
    const socketManager = app.globalData.socketManager

    if (!socketManager) {
      console.error('[ChatDetail] WebSocket 管理器未初始化')
      wx.showToast({
        title: '消息服务未连接',
        icon: 'none'
      })
      return
    }

    // 监听新消息
    this.messageListener = (data) => {
      if (data.type === 'message') {
        this.handleNewMessage(data)
      }
    }
    socketManager.on('onMessage', this.messageListener)

    // 监听在线状态变化
    this.onlineStatusListener = (data) => {
      this.handleOnlineStatusChange(data)
    }
    socketManager.on('onOnlineStatus', this.onlineStatusListener)

    // 监听连接状态
    this.connectListener = () => {
      this.setData({ socketConnected: true })
      this.refreshOnlineStatus()
    }
    socketManager.on('onConnect', this.connectListener)

    this.disconnectListener = () => {
      this.setData({ socketConnected: false })
      wx.showToast({
        title: '消息服务已断开',
        icon: 'none'
      })
    }
    socketManager.on('onDisconnect', this.disconnectListener)

    // 获取当前连接状态
    const status = socketManager.getStatus()
    this.setData({ socketConnected: status.isConnected })

    // 刷新对方在线状态
    this.refreshOnlineStatus()
  },

  /**
   * 移除 WebSocket 监听
   */
  removeWebSocketListeners() {
    const app = getApp()
    const socketManager = app.globalData.socketManager

    if (socketManager) {
      if (this.messageListener) {
        socketManager.off('onMessage', this.messageListener)
      }
      if (this.onlineStatusListener) {
        socketManager.off('onOnlineStatus', this.onlineStatusListener)
      }
      if (this.connectListener) {
        socketManager.off('onConnect', this.connectListener)
      }
      if (this.disconnectListener) {
        socketManager.off('onDisconnect', this.disconnectListener)
      }
    }
  },

  /**
   * 处理接收到的新消息
   */
  handleNewMessage(data) {
    console.log('[ChatDetail] 收到新消息:', data)
    
    const messageData = data.data || {}
    const { fromUserId, toUserId, content, messageType, timestamp } = messageData

    // 只处理当前聊天的消息
    if (fromUserId !== this.data.chatId && toUserId !== this.data.chatId) {
      return
    }

    // 判断是否是我发的消息
    const isMe = fromUserId === this.data.myUserId

    // 添加到消息列表
    const newMessage = {
      id: timestamp || Date.now(),
      isMe: isMe,
      content: content,
      type: messageType || 'text',
      time: this.formatTime(timestamp),
      avatar: isMe ? this.data.myAvatar : this.data.chatInfo.avatar,
      status: 'success'
    }

    // 如果是图片消息
    if (messageType === 'image') {
      newMessage.image = messageData.imageUrl || content
    }

    const messageList = [...this.data.messageList, newMessage]
    this.setData({
      messageList: messageList,
      scrollIntoView: `msg-${newMessage.id}`
    })
  },

  /**
   * 处理在线状态变化
   */
  handleOnlineStatusChange(data) {
    console.log('[ChatDetail] 在线状态变化:', data)
    
    const { userId, status, onlineUsers } = data
    
    // 检查对方是否在线
    if (userId === this.data.chatId || (onlineUsers && onlineUsers.includes(this.data.chatId))) {
      const isOnline = status === 'online' || (onlineUsers && onlineUsers.includes(String(this.data.chatId)))
      this.setData({
        'chatInfo.isOnline': isOnline
      })
    }
  },

  /**
   * 刷新在线状态
   */
  refreshOnlineStatus() {
    const app = getApp()
    const socketManager = app.globalData.socketManager
    
    if (socketManager && socketManager.isConnected) {
      const isOnline = socketManager.isUserOnline(this.data.chatId)
      this.setData({
        'chatInfo.isOnline': isOnline
      })
    }
  },

  /**
   * 格式化时间
   */
  formatTime(timestamp) {
    if (!timestamp) {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    }
    
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  },

  async loadChatInfo(id, type) {
    try {
      let name = '未知用户'
      let avatar = ''
      
      // 判断是否是校友会或官方账号
      if (type === 'association' || type === 'official') {
        const res = await associationApi.getAssociationDetail(id)
        if (res.data && res.data.code === 200) {
          const info = res.data.data
          name = info.name || '未知校友会'
          avatar = info.logo ? config.getImageUrl(info.logo) : ''
        }
      } else {
        // 默认为校友
      // const res = await alumniApi.getAlumniInfo(id)
      // if (res.data && res.data.code === 200) {
      //   const info = res.data.data
      //   name = info.name || info.nickname || '未知校友'
      //   avatar = info.avatarUrl ? config.getImageUrl(info.avatarUrl) : ''
      // }
      
      // 直接使用页面参数中的信息（如果有）
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage && prevPage.data.chatList) {
        const currentChat = prevPage.data.chatList.find(c => (c.userId || c.targetId) == id)
        if (currentChat) {
          name = currentChat.name || currentChat.peerNickname || '未知校友'
          avatar = currentChat.avatar || (currentChat.peerAvatar ? config.getImageUrl(currentChat.peerAvatar) : '')
        }
      }
      }
      
      this.setData({
        chatInfo: {
          name,
          avatar,
          userId: id,
          isOnline: this.data.chatInfo.isOnline // 保持在线状态不变
        }
      })
      
      // 设置导航栏标题
      wx.setNavigationBarTitle({
        title: name
      })
    } catch (error) {
      console.error('[ChatDetail] 加载聊天对象信息失败:', error)
    }
  },

  async loadMessages(id) {
    try {
      // 从后端获取聊天历史
      const params = {
        current: 1,
        size: 30,
        otherUserId: id,
      }
      const res = await chatApi.getChatHistory(params)
      
      console.log('[ChatDetail] 历史消息响应:', res)

      if (res.data && res.data.code === 200) {
        let messages = res.data.data?.records || []

        console.log('[ChatDetail] 历史消息列表:', messages)
        
        // 映射消息数据
        const mappedMessages = messages.map(msg => {
          // 处理消息内容：可能在 msgContent.content 中，也可能直接是 msgContent 字符串
          let content = ''
          let formUserPortrait = ''
          
          if (msg.msgContent) {
             if (typeof msg.msgContent === 'string') {
               try {
                 const parsed = JSON.parse(msg.msgContent)
                 content = parsed.content || msg.msgContent
                 formUserPortrait = parsed.formUserPortrait
               } catch (e) {
                 content = msg.msgContent
               }
             } else {
               content = msg.msgContent.content || ''
               formUserPortrait = msg.msgContent.formUserPortrait
             }
          }
          
          const msgType = (msg.messageFormat || 'TEXT').toLowerCase()
          
          return {
            id: msg.messageId,
            isMe: msg.isMine,
            content: content,
            type: msgType === 'image' ? 'image' : 'text', // 目前主要支持文本和图片
            time: this.formatTime(msg.createTime),
            // 如果是对方的消息，尝试从 msgContent 中获取头像，否则使用默认头像
            avatar: msg.isMine ? this.data.myAvatar : (formUserPortrait ? config.getImageUrl(formUserPortrait) : this.data.chatInfo.avatar),
            image: msgType === 'image' ? config.getImageUrl(content) : '',
            status: 'success'
          }
        })
        
        // 按时间正序排序（旧消息在前）
        mappedMessages.reverse()

        this.setData({
          messageList: mappedMessages
        })
        
        // 滚动到底部
        setTimeout(() => {
          this.scrollToBottom()
        }, 100)
        
        return
      }
    } catch (error) {
      console.error('[ChatDetail] 加载消息历史失败:', error)
    }
  },

  onInput(e) {
    const value = e.detail.value
    this.setData({
      inputValue: value,
      hasInput: value.trim().length > 0
    })
  },

  async sendMessage() {
    const { inputValue, messageList, chatId } = this.data
    if (!inputValue.trim()) {
      return
    }

    const content = inputValue.trim()
    const timestamp = Date.now()
    
    // 立即显示消息（发送中状态）
    const newMessage = {
      id: timestamp,
      isMe: true,
      content: content,
      type: 'text',
      time: this.formatTime(timestamp),
      avatar: this.data.myAvatar,
      status: 'sending'
    }
    
    this.setData({
      messageList: [...messageList, newMessage],
      inputValue: '',
      hasInput: false,
      scrollIntoView: `msg-${newMessage.id}`
    })

    try {
      // 构造发送参数
      const payload = {
        toUserId: chatId, // 使用 toUserId 
        toId: chatId,     // 保留 toId 以兼容
        otherUserId: chatId, // 保留 otherUserId 以兼容
        content: content,    // 直接在顶层添加 content 字段
        messageFormat: 'TEXT',
        messageType: 'MESSAGE',
        msgContent: JSON.stringify({ // 将 msgContent 转为字符串，以防后端需要
            content: content,
            type: 'text'
        })
      }

      const res = await chatApi.sendMessage(payload)
      
      if (res.data && res.data.code === 200) {
        // 发送成功
        const updatedList = this.data.messageList.map(msg => {
          if (msg.id === timestamp) {
            return { ...msg, status: 'success' }
          }
          return msg
        })
        this.setData({ messageList: updatedList })
      } else {
         throw new Error(res.data?.msg || '发送失败')
      }
    } catch (error) {
        console.error('发送消息失败:', error)
        // 发送失败
        const updatedList = this.data.messageList.map(msg => {
          if (msg.id === timestamp) {
            return { ...msg, status: 'failed' }
          }
          return msg
        })
        this.setData({ messageList: updatedList })
        
        wx.showToast({
          title: '发送失败',
          icon: 'none'
        })
    }
  },

  receiveMessage() {
    const { messageList } = this.data
    const replies = [
      '好的，我知道了',
      '谢谢你的回复',
      '没问题',
      '收到',
      '好的，到时候见'
    ]
    
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    const replyMessage = {
      id: messageList.length + 1,
      isMe: false,
      content: replies[Math.floor(Math.random() * replies.length)],
      time: timeStr
    }
    
    this.setData({
      messageList: [...messageList, replyMessage],
      scrollIntoView: `msg-${replyMessage.id}`
    })
  },

  scrollToBottom() {
    const { messageList } = this.data
    if (messageList.length > 0) {
      const lastId = messageList[messageList.length - 1].id
      this.setData({
        scrollIntoView: `msg-${lastId}`
      })
    }
  },

  showMoreActions() {
    this.setData({
      showMoreMenu: true,
      showEmoji: false
    })
  },

  hideMoreMenu() {
    this.setData({
      showMoreMenu: false
    })
  },

  selectImage() {
    this.hideMoreMenu()
    wx.chooseImage({
      count: 9,
      sizeType: ['original', 'compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths
        // 发送图片消息
        this.sendImageMessage(tempFilePaths)
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        })
      }
    })
  },

  async sendImageMessage(imagePaths) {
    const { messageList, chatId, socketConnected } = this.data

    if (!socketConnected) {
      wx.showToast({
        title: '消息服务未连接',
        icon: 'none'
      })
      return
    }
    
    wx.showLoading({ title: '发送中...' })
    
    try {
      // 为每张图片创建消息并上传
      for (let i = 0; i < imagePaths.length; i++) {
        const imagePath = imagePaths[i]
        const timestamp = Date.now() + i
        
        // 先显示本地图片（发送中状态）
        const newMessage = {
          id: timestamp,
          isMe: true,
          content: '',
          image: imagePath,
          type: 'image',
          time: this.formatTime(timestamp),
          avatar: this.data.myAvatar,
          status: 'sending'
        }
        
        messageList.push(newMessage)
        this.setData({
          messageList: messageList,
          scrollIntoView: `msg-${newMessage.id}`
        })
        
        // 上传图片
        const uploadRes = await chatApi.uploadChatImage(imagePath)
        
        if (uploadRes.data && uploadRes.data.code === 200) {
          const imageUrl = uploadRes.data.data.url
          
          // 更新消息中的图片URL
          const updatedList = messageList.map(msg => {
            if (msg.id === timestamp) {
              return { ...msg, image: imageUrl, status: 'success' }
            }
            return msg
          })
          this.setData({ messageList: updatedList })
          
          // 通过 WebSocket 发送图片消息
          const app = getApp()
          const socketManager = app.globalData.socketManager
          
          if (socketManager) {
            socketManager.sendChatMessage(chatId, imageUrl, 'image', {
              imageUrl: imageUrl
            })
          }
        } else {
          // 上传失败
          const updatedList = messageList.map(msg => {
            if (msg.id === timestamp) {
              return { ...msg, status: 'failed' }
            }
            return msg
          })
          this.setData({ messageList: updatedList })
        }
      }
      
      wx.hideLoading()
      
    } catch (error) {
      console.error('[ChatDetail] 发送图片失败:', error)
      wx.hideLoading()
      wx.showToast({
        title: '发送失败',
        icon: 'none'
      })
    }
  },

  selectLocation() {
    this.hideMoreMenu()
    wx.chooseLocation({
      success: (res) => {
        // 发送位置消息
        this.sendLocationMessage(res)
      },
      fail: (err) => {
        if (err.errMsg !== 'chooseLocation:fail cancel') {
          console.error('选择位置失败:', err)
          wx.showToast({
            title: '选择位置失败',
            icon: 'none'
          })
        }
      }
    })
  },

  sendLocationMessage(location) {
    const { messageList } = this.data
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    const newMessage = {
      id: messageList.length + 1,
      isMe: true,
      content: `位置：${location.name || location.address}`,
      location: {
        name: location.name,
        address: location.address,
        latitude: location.latitude,
        longitude: location.longitude
      },
      type: 'location',
      time: timeStr
    }
    
    this.setData({
      messageList: [...messageList, newMessage],
      scrollIntoView: `msg-${newMessage.id}`
    })
    
    // 模拟对方回复
    setTimeout(() => {
      this.receiveMessage()
    }, 1000)
  },

  selectContact() {
    this.hideMoreMenu()
    // 这里可以跳转到联系人选择页面，或者使用微信的通讯录选择
    wx.showActionSheet({
      itemList: ['从通讯录选择', '从校友列表选择'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 从通讯录选择（需要用户授权）
          this.selectFromContacts()
        } else {
          // 从校友列表选择
          this.selectFromAlumni()
        }
      }
    })
  },

  selectFromContacts() {
    // 这里可以调用微信的通讯录选择API（如果有）
    // 或者跳转到自定义的联系人选择页面
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  },

  selectFromAlumni() {
    // 跳转到校友列表页面选择
    wx.navigateTo({
      url: '/pages/alumni/list/list?mode=select',
      events: {
        selectAlumni: (alumni) => {
          this.sendContactMessage(alumni)
        }
      }
    })
  },

  sendContactMessage(contact) {
    const { messageList } = this.data
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    const newMessage = {
      id: messageList.length + 1,
      isMe: true,
      content: `推荐联系人：${contact.name}`,
      contact: {
        id: contact.id,
        name: contact.name,
        avatar: contact.avatar,
        school: contact.school
      },
      type: 'contact',
      time: timeStr
    }
    
    this.setData({
      messageList: [...messageList, newMessage],
      scrollIntoView: `msg-${newMessage.id}`
    })
    
    // 模拟对方回复
    setTimeout(() => {
      this.receiveMessage()
    }, 1000)
  },

  viewProfile(e) {
    const { type } = e.currentTarget.dataset
    const { chatInfo } = this.data
    
    if (type === 'official' || chatInfo.associationId) {
      // 跳转到校友会主页
      wx.navigateTo({
        url: `/pages/alumni-association/detail/detail?id=${chatInfo.associationId || 1}`
      })
    } else {
      // 跳转到个人主页
      wx.navigateTo({
        url: `/pages/alumni/detail/detail?id=${chatInfo.userId || this.data.chatId}`
      })
    }
  },

  toggleEmoji() {
    this.setData({
      showEmoji: !this.data.showEmoji
    })
  },

  insertEmoji(e) {
    const { emoji } = e.currentTarget.dataset
    const { inputValue } = this.data
    const newValue = inputValue + emoji
    this.setData({
      inputValue: newValue,
      hasInput: newValue.trim().length > 0,
      showEmoji: false
    })
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset
    const { messageList } = this.data
    const imageUrls = messageList
      .filter(msg => msg.type === 'image' && msg.image)
      .map(msg => msg.image)
    const currentIndex = imageUrls.indexOf(url)
    
    wx.previewImage({
      urls: imageUrls,
      current: url
    })
  }
})

