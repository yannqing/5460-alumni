// pages/chat/detail/detail.js
Page({
  data: {
    chatId: null,
    chatType: 'chat',
    chatInfo: {
      name: '',
      avatar: '/assets/images/头像.png'
    },
    myAvatar: '/assets/images/头像.png',
    messageList: [],
    inputValue: '',
    hasInput: false,
    scrollIntoView: '',
    showEmoji: false,
    showMoreMenu: false,
    emojiList: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗', '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯', '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈', '👿', '👹', '👺', '🤡', '💩', '👻', '💀', '☠️', '👽', '👾', '🤖', '🎃']
  },

  onLoad(options) {
    const { id, type } = options
    if (id) {
      this.setData({ 
        chatId: id,
        chatType: type || 'chat'
      })
      this.loadChatInfo(id, type)
      this.loadMessages(id)
    }
  },

  loadChatInfo(id, type) {
    // 模拟加载聊天信息
    const chatInfoMap = {
      1: { name: '张三', avatar: '/assets/images/头像.png', userId: 1 },
      2: { name: '李四', avatar: '/assets/images/头像.png', userId: 2 },
      3: { name: '王五', avatar: '/assets/images/头像.png', userId: 3 },
      4: { name: '赵六', avatar: '/assets/images/头像.png', userId: 4 },
      5: { name: '南京大学上海校友会', avatar: '/assets/images/头像.png', associationId: 1 },
      6: { name: '孙七', avatar: '/assets/images/头像.png', userId: 6 },
      7: { name: '周八', avatar: '/assets/images/头像.png', userId: 7 },
      'oa_1': { name: '南京大学上海校友会', avatar: '/assets/images/头像.png', associationId: 1 },
      'oa_2': { name: '浙江大学杭州校友会', avatar: '/assets/images/头像.png', associationId: 2 },
      'oa_3': { name: '清华大学北京校友会', avatar: '/assets/images/头像.png', associationId: 3 },
      'oa_4': { name: '北京大学校友会', avatar: '/assets/images/头像.png', associationId: 4 }
    }
    
    this.setData({
      chatInfo: chatInfoMap[id] || { name: '未知用户', avatar: '/assets/images/头像.png' }
    })
    
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: this.data.chatInfo.name
    })
  },

  loadMessages(id) {
    // 模拟消息数据
    const mockMessages = [
      {
        id: 1,
        isMe: false,
        content: '你好，请问这个活动什么时候开始？',
        time: '10:25'
      },
      {
        id: 2,
        isMe: true,
        content: '活动是本周六下午2点开始',
        time: '10:26'
      },
      {
        id: 3,
        isMe: false,
        content: '好的，谢谢！',
        time: '10:27'
      },
      {
        id: 4,
        isMe: true,
        content: '不客气，到时候见！',
        time: '10:28'
      }
    ]
    
    this.setData({
      messageList: mockMessages
    })
    
    // 滚动到底部
    setTimeout(() => {
      this.scrollToBottom()
    }, 100)
  },

  onInput(e) {
    const value = e.detail.value
    this.setData({
      inputValue: value,
      hasInput: value.trim().length > 0
    })
  },

  sendMessage() {
    const { inputValue, messageList } = this.data
    if (!inputValue.trim()) return

    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    const newMessage = {
      id: messageList.length + 1,
      isMe: true,
      content: inputValue.trim(),
      time: timeStr
    }
    
    this.setData({
      messageList: [...messageList, newMessage],
      inputValue: '',
      hasInput: false,
      scrollIntoView: `msg-${newMessage.id}`
    })
    
    // 模拟对方回复
    setTimeout(() => {
      this.receiveMessage()
    }, 1000)
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

  sendImageMessage(imagePaths) {
    const { messageList } = this.data
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    // 为每张图片创建消息
    imagePaths.forEach((imagePath, index) => {
      const newMessage = {
        id: messageList.length + index + 1,
        isMe: true,
        content: '',
        image: imagePath,
        type: 'image',
        time: timeStr
      }
      
      messageList.push(newMessage)
    })
    
    this.setData({
      messageList: messageList,
      scrollIntoView: `msg-${messageList[messageList.length - 1].id}`
    })
    
    // 模拟对方回复
    setTimeout(() => {
      this.receiveMessage()
    }, 1000)
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

