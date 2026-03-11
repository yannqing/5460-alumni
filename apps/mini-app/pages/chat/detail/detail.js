// pages/chat/detail/detail.js
const config = require('../../../utils/config.js')
const { chatApi, alumniApi, associationApi, userApi } = require('../../../api/api.js')

Page({
  data: {
    chatId: null,
    conversationId: null, // 会话ID
    chatType: 'chat',
    chatInfo: {
      name: '',
      avatar: '',
      isOnline: false,
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
    emojiList: [
      '😀',
      '😃',
      '😄',
      '😁',
      '😆',
      '😅',
      '😂',
      '🤣',
      '😊',
      '😇',
      '🙂',
      '🙃',
      '😉',
      '😌',
      '😍',
      '🥰',
      '😘',
      '😗',
      '😙',
      '😚',
      '😋',
      '😛',
      '😝',
      '😜',
      '🤪',
      '🤨',
      '🧐',
      '🤓',
      '😎',
      '🤩',
      '🥳',
      '😏',
      '😒',
      '😞',
      '😔',
      '😟',
      '😕',
      '🙁',
      '☹️',
      '😣',
      '😖',
      '😫',
      '😩',
      '🥺',
      '😢',
      '😭',
      '😤',
      '😠',
      '😡',
      '🤬',
      '🤯',
      '😳',
      '🥵',
      '🥶',
      '😱',
      '😨',
      '😰',
      '😥',
      '😓',
      '🤗',
      '🤔',
      '🤭',
      '🤫',
      '🤥',
      '😶',
      '😐',
      '😑',
      '😬',
      '🙄',
      '😯',
      '😦',
      '😧',
      '😮',
      '😲',
      '🥱',
      '😴',
      '🤤',
      '😪',
      '😵',
      '🤐',
      '🥴',
      '🤢',
      '🤮',
      '🤧',
      '😷',
      '🤒',
      '🤕',
      '🤑',
      '🤠',
      '😈',
      '👿',
      '👹',
      '👺',
      '🤡',
      '💩',
      '👻',
      '💀',
      '☠️',
      '👽',
      '👾',
      '🤖',
      '🎃',
    ],
  },

  // WebSocket 事件监听器引用
  messageListener: null,
  onlineStatusListener: null,
  connectListener: null,
  disconnectListener: null,

  async onLoad(options) {
    const { id, type, name, avatar, conversationId, draftContent } = options
    const app = getApp()
    const myUserId = app.globalData.userData?.wxId || wx.getStorageSync('userId')

    // 如果有草稿，恢复显示
    if (draftContent && draftContent !== 'undefined' && draftContent !== 'null') {
      const decodedDraft = decodeURIComponent(draftContent)
      if (decodedDraft) {
        this.setData({
          inputValue: decodedDraft,
          hasInput: true,
        })
      }
    }

    // 获取我的头像：优先从全局数据获取，如果没有则尝试从缓存获取
    let myAvatar = app.globalData.userData?.avatar
    if (!myAvatar) {
      const userInfo = wx.getStorageSync('userInfo')
      // 尝试多种可能的字段名
      if (userInfo) {
        myAvatar = userInfo.avatar || userInfo.avatarUrl || userInfo.portrait || userInfo.headImgUrl
      }
    }

    // 如果还是没有，尝试从 app.globalData.userInfo 获取（有些小程序存储在这里）
    if (!myAvatar && app.globalData.userInfo) {
      const gUserInfo = app.globalData.userInfo
      myAvatar =
        gUserInfo.avatar || gUserInfo.avatarUrl || gUserInfo.portrait || gUserInfo.headImgUrl
    }

    // 如果所有缓存都失效，尝试从接口获取最新用户信息
    if (!myAvatar) {
      console.log('[ChatDetail] 缓存中未找到头像，尝试从接口获取...')
      try {
        const res = await userApi.getUserInfo()
        if (res.data && res.data.code === 200) {
          const info = res.data.data || {}
          // 更新全局数据
          app.globalData.userData = {
            ...(app.globalData.userData || {}),
            ...info,
          }
          // 再次尝试获取头像
          myAvatar = info.avatar || info.avatarUrl || info.portrait || info.headImgUrl
        }
      } catch (e) {
        console.error('[ChatDetail] 获取用户信息失败:', e)
      }
    }

    if (id && id !== 'undefined' && id !== 'null') {
      this.setData({
        chatId: id,
        conversationId: conversationId || null,
        chatType: type || 'chat',
        myUserId: myUserId,
        myAvatar: config.getImageUrl(myAvatar || config.defaultAvatar),
      })

      // 进入聊天详情页时，自动标记该会话为已读
      this.markConversationAsRead(id)

      this.loadChatInfo(id, type, name, avatar) // 传递 URL 参数中的 name 和 avatar
      this.loadMessages(id)
      this.initWebSocket()
    } else {
      console.error('[ChatDetail] 无效的聊天ID:', id)
      wx.showToast({
        title: '参数错误',
        icon: 'none',
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  onUnload() {
    // 保存草稿
    if (this.data.conversationId) {
      // 即使内容为空也调用，以便清空草稿
      chatApi.saveDraft(this.data.conversationId, this.data.inputValue)
    }

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
        icon: 'none',
      })
      return
    }

    // 监听新消息
    this.messageListener = data => {
      if (data.type === 'msg') {
        this.handleNewMessage(data)
      }
    }
    socketManager.on('onMessage', this.messageListener)

    // 监听在线状态变化
    this.onlineStatusListener = data => {
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
        icon: 'none',
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
    const { fromUserId, toUserId, content, messageType, timestamp, messageId } = messageData

    // 只处理当前聊天的消息
    if (fromUserId !== this.data.chatId && toUserId !== this.data.chatId) {
      return
    }

    // 判断是否是我发的消息
    const isMe = fromUserId === this.data.myUserId

    // 使用 messageId 或 timestamp 作为消息ID
    const msgId = messageId || timestamp || Date.now()
    const msgTimestamp = timestamp || Date.now()

    // 检查消息是否已存在（避免重复添加）
    const existingMessage = this.data.messageList.find(msg => {
      // 如果消息ID相同
      if (msg.id === msgId) {
        return true
      }
      // 如果是我发送的消息，通过内容和时间戳匹配（允许一定的时间误差）
      if (isMe && msg.isMe && msg.content === content) {
        // 如果消息的 timestamp 与接收到的 timestamp 匹配，或者时间差在10秒内
        const msgTime =
          msg.timestamp || (typeof msg.id === 'number' && msg.id > 1577836800000 ? msg.id : null)
        if (msgTime) {
          const timeDiff = Math.abs(msgTime - msgTimestamp)
          if (timeDiff < 10000) {
            // 10秒内的消息认为是同一条
            return true
          }
        }
      }
      return false
    })

    if (existingMessage) {
      // 消息已存在，更新它而不是添加新消息
      const updatedList = this.data.messageList.map(msg => {
        // 匹配临时消息：通过ID或内容和时间匹配
        if (
          msg.id === existingMessage.id ||
          (isMe &&
            msg.isMe &&
            msg.content === content &&
            (msg.id === existingMessage.id ||
              (msg.timestamp && Math.abs(msg.timestamp - msgTimestamp) < 10000)))
        ) {
          return {
            ...msg,
            id: msgId, // 使用后端返回的真实ID
            timestamp: msgTimestamp, // 确保 timestamp 字段存在
            status: 'success',
            // 确保所有字段都正确
            isMe: isMe,
            content: content,
            type: messageType || 'text',
            time: this.formatTime(msgTimestamp),
            avatar: isMe ? this.data.myAvatar : this.data.chatInfo.avatar,
          }
        }
        return msg
      })
      this.setData({ messageList: updatedList })
      console.log('[ChatDetail] WebSocket消息更新了临时消息的ID:', msgId)
      return
    }

    // 添加到消息列表
    const newMessage = {
      id: msgId,
      isMe: isMe,
      content: content,
      type: messageType || 'text',
      time: this.formatTime(msgTimestamp),
      timestamp: msgTimestamp, // 确保 timestamp 字段存在，用于撤回判断
      avatar: isMe ? this.data.myAvatar : this.data.chatInfo.avatar,
      status: 'success',
    }

    // 如果是图片消息
    if (messageType === 'image') {
      newMessage.image = messageData.imageUrl || content
    }

    const messageList = [...this.data.messageList, newMessage]
    this.setData({
      messageList: messageList,
      scrollIntoView: `msg-${newMessage.id}`,
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
      const isOnline =
        status === 'online' || (onlineUsers && onlineUsers.includes(String(this.data.chatId)))
      this.setData({
        'chatInfo.isOnline': isOnline,
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
        'chatInfo.isOnline': isOnline,
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

  async loadChatInfo(id, type, urlName, urlAvatar) {
    try {
      let name = '未知用户'
      let avatar = ''

      // 优先使用 URL 参数传递过来的信息（这是最可靠的，因为来自列表页）
      if (urlName && urlName !== 'undefined') {
        name = decodeURIComponent(urlName)
      }

      if (urlAvatar && urlAvatar !== 'undefined') {
        avatar = decodeURIComponent(urlAvatar)
        // 这里的 urlAvatar 已经是处理过的完整 URL，不需要再次调用 config.getImageUrl
      }

      // 如果 URL 参数没有提供足够信息，则尝试其他方式
      if (name === '未知用户' || !avatar) {
        // 判断是否是校友会或官方账号
        if (type === 'association' || type === 'official') {
          const res = await associationApi.getAssociationDetail(id)
          if (res.data && res.data.code === 200) {
            const info = res.data.data
            name = info.name || '未知校友会'
            avatar = info.logo ? config.getImageUrl(info.logo) : ''
          }
        } else {
          // 尝试从上一页获取（作为 URL 参数的备选）
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage && prevPage.data.chatList) {
            // 使用 id (可能是 userId, peerId, targetId 等) 进行模糊匹配
            const currentChat = prevPage.data.chatList.find(
              c => c.peerId == id || c.userId == id || c.id == id
            )
            if (currentChat) {
              if (name === '未知用户') {
                name = currentChat.name || currentChat.peerNickname || '未知校友'
              }
              if (!avatar) {
                avatar =
                  currentChat.avatar ||
                  (currentChat.peerAvatar ? config.getImageUrl(currentChat.peerAvatar) : '')
              }
            }
          }
        }
      }

      this.setData({
        chatInfo: {
          name,
          avatar,
          userId: id,
          isOnline: this.data.chatInfo.isOnline, // 保持在线状态不变
        },
      })

      // 设置导航栏标题
      wx.setNavigationBarTitle({
        title: name,
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
        const messages = res.data.data?.records || []

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

          // 检查是否为撤回消息：status === 4 表示已撤回，或者内容包含"撤回"
          const isRecalled =
            msg.status === 4 ||
            (content && (content.includes('撤回了一条消息') || content.includes('撤回')))

          // 如果是撤回消息，统一显示为"你撤回了一条消息"或"对方撤回了一条消息"
          if (isRecalled) {
            if (msg.isMine) {
              content = '你撤回了一条消息'
            } else {
              content = '对方撤回了一条消息'
            }
          }

          const msgType = (msg.messageFormat || 'TEXT').toLowerCase()

          return {
            id: msg.messageId,
            isMe: msg.isMine,
            content: content,
            type: isRecalled ? 'system' : msgType === 'image' ? 'image' : 'text', // 撤回消息设置为 system 类型
            time: this.formatTime(msg.createTime),
            timestamp: msg.createTime, // 保存原始时间戳用于撤回判断
            // 如果是对方的消息，尝试从 msgContent 中获取头像，否则使用默认头像
            avatar: msg.isMine
              ? this.data.myAvatar
              : formUserPortrait
                ? config.getImageUrl(formUserPortrait)
                : this.data.chatInfo.avatar,
            image: msgType === 'image' ? config.getImageUrl(content) : '',
            status: 'success',
            isRecall: isRecalled, // 标记为撤回消息
          }
        })

        // 按时间正序排序（旧消息在前）
        mappedMessages.reverse()

        this.setData({
          messageList: mappedMessages,
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

  /**
   * 重新加载最新消息（用于发送消息后获取真实的消息ID）
   */
  async reloadLatestMessages(sentContent, sentTimestamp) {
    try {
      const { chatId, messageList } = this.data

      // 只加载最后几条消息
      const params = {
        current: 1,
        size: 5, // 只加载最后5条，减少请求量
        otherUserId: chatId,
      }
      const res = await chatApi.getChatHistory(params)

      if (res.data && res.data.code === 200) {
        const messages = res.data.data?.records || []

        if (messages.length > 0) {
          // 映射消息数据
          const mappedMessages = messages.map(msg => {
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

            // 检查是否为撤回消息：status === 4 表示已撤回，或者内容包含"撤回"
            const isRecalled =
              msg.status === 4 ||
              (content && (content.includes('撤回了一条消息') || content.includes('撤回')))

            // 如果是撤回消息，统一显示为"你撤回了一条消息"或"对方撤回了一条消息"
            if (isRecalled) {
              if (msg.isMine) {
                content = '你撤回了一条消息'
              } else {
                content = '对方撤回了一条消息'
              }
            }

            const msgType = (msg.messageFormat || 'TEXT').toLowerCase()

            return {
              id: msg.messageId,
              isMe: msg.isMine,
              content: content,
              type: isRecalled ? 'system' : msgType === 'image' ? 'image' : 'text', // 撤回消息设置为 system 类型
              time: this.formatTime(msg.createTime),
              timestamp: msg.createTime,
              avatar: msg.isMine
                ? this.data.myAvatar
                : formUserPortrait
                  ? config.getImageUrl(formUserPortrait)
                  : this.data.chatInfo.avatar,
              image: msgType === 'image' ? config.getImageUrl(content) : '',
              status: 'success',
              isRecall: isRecalled, // 标记为撤回消息
            }
          })

          // 按时间正序排序
          mappedMessages.reverse()

          // 查找匹配的临时消息并更新
          // 优先匹配：通过内容和时间戳匹配最新消息中的最后一条我发送的消息
          const myLatestMessage = mappedMessages.filter(m => m.isMe).pop() // 获取最新的一条我发送的消息

          const updatedList = messageList.map(msg => {
            // 如果是临时消息（使用timestamp作为ID），且内容和时间匹配
            if (msg.id === sentTimestamp && msg.content === sentContent && msg.isMe) {
              // 优先使用最新消息中匹配的消息
              const matchedMsg = mappedMessages.find(
                m =>
                  m.isMe &&
                  m.content === sentContent &&
                  Math.abs(m.timestamp - sentTimestamp) < 10000 // 10秒内的消息认为是同一条
              )

              // 如果找到了精确匹配的消息
              if (matchedMsg) {
                console.log('[ChatDetail] 找到匹配的消息，更新ID:', matchedMsg.id)
                return {
                  ...msg,
                  id: matchedMsg.id,
                  timestamp: matchedMsg.timestamp,
                  status: 'success',
                  time: matchedMsg.time,
                }
              }

              // 如果没有找到精确匹配，但最新消息中有我发送的消息，且内容相同，也更新
              if (myLatestMessage && myLatestMessage.content === sentContent) {
                console.log('[ChatDetail] 使用最新消息更新ID:', myLatestMessage.id)
                return {
                  ...msg,
                  id: myLatestMessage.id,
                  timestamp: myLatestMessage.timestamp,
                  status: 'success',
                  time: myLatestMessage.time,
                }
              }
            }
            return msg
          })

          // 如果找到了匹配的消息并更新了，更新列表
          const hasUpdate = updatedList.some((msg, index) => msg.id !== messageList[index]?.id)
          if (hasUpdate) {
            this.setData({ messageList: updatedList })
            console.log('[ChatDetail] 消息ID已更新，现在可以正常撤回')

            // 滚动到底部，确保新消息可见
            setTimeout(() => {
              const lastMsg = updatedList[updatedList.length - 1]
              if (lastMsg) {
                this.setData({ scrollIntoView: `msg-${lastMsg.id}` })
              }
            }, 100)
          } else {
            console.warn('[ChatDetail] 未找到匹配的消息进行更新，可能消息还未保存到后端')
          }
        }
      }
    } catch (error) {
      console.error('[ChatDetail] 重新加载最新消息失败:', error)
    }
  },

  onInput(e) {
    const value = e.detail.value
    this.setData({
      inputValue: value,
      hasInput: value.trim().length > 0,
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
      timestamp: timestamp,
      avatar: this.data.myAvatar,
      status: 'sending',
    }

    this.setData({
      messageList: [...messageList, newMessage],
      inputValue: '',
      hasInput: false,
      scrollIntoView: `msg-${newMessage.id}`,
    })

    try {
      // 构造发送参数
      const payload = {
        toUserId: chatId, // 使用 toUserId
        toId: chatId, // 保留 toId 以兼容
        otherUserId: chatId, // 保留 otherUserId 以兼容
        content: content, // 直接在顶层添加 content 字段
        messageFormat: 'TEXT',
        messageType: 'MESSAGE',
        msgContent: JSON.stringify({
          // 将 msgContent 转为字符串，以防后端需要
          content: content,
          type: 'text',
        }),
      }

      const res = await chatApi.sendMessage(payload)

      console.log('[ChatDetail] 发送消息响应:', res.data)

      if (res.data && res.data.code === 200) {
        // 无论后端是否返回消息ID，都立即重新加载最新消息，确保消息已保存到历史记录
        // 这样可以保证撤回时能找到消息
        console.log('[ChatDetail] 消息发送成功，立即重新加载最新消息以更新消息ID...')
        // 立即调用，不延迟，确保消息实时可撤回
        await this.reloadLatestMessages(content, timestamp)
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
        icon: 'none',
      })
    }
  },

  receiveMessage() {
    const { messageList } = this.data
    const replies = ['好的，我知道了', '谢谢你的回复', '没问题', '收到', '好的，到时候见']

    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`

    const replyMessage = {
      id: messageList.length + 1,
      isMe: false,
      content: replies[Math.floor(Math.random() * replies.length)],
      time: timeStr,
    }

    this.setData({
      messageList: [...messageList, replyMessage],
      scrollIntoView: `msg-${replyMessage.id}`,
    })
  },

  scrollToBottom() {
    const { messageList } = this.data
    if (messageList.length > 0) {
      const lastId = messageList[messageList.length - 1].id
      this.setData({
        scrollIntoView: `msg-${lastId}`,
      })
    }
  },

  showMoreActions() {
    this.setData({
      showMoreMenu: true,
      showEmoji: false,
    })
  },

  hideMoreMenu() {
    this.setData({
      showMoreMenu: false,
    })
  },

  selectImage() {
    this.hideMoreMenu()
    wx.chooseImage({
      count: 9,
      sizeType: ['original', 'compressed'],
      sourceType: ['album', 'camera'],
      success: res => {
        const tempFilePaths = res.tempFilePaths
        // 发送图片消息
        this.sendImageMessage(tempFilePaths)
      },
      fail: err => {
        console.error('选择图片失败:', err)
        wx.showToast({
          title: '选择图片失败',
          icon: 'none',
        })
      },
    })
  },

  async sendImageMessage(imagePaths) {
    const { messageList, chatId, socketConnected } = this.data

    if (!socketConnected) {
      wx.showToast({
        title: '消息服务未连接',
        icon: 'none',
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
          status: 'sending',
        }

        messageList.push(newMessage)
        this.setData({
          messageList: messageList,
          scrollIntoView: `msg-${newMessage.id}`,
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
              imageUrl: imageUrl,
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
        icon: 'none',
      })
    }
  },

  selectLocation() {
    this.hideMoreMenu()
    // 暂时注释：等待微信公众平台权限申请通过后恢复
    // wx.chooseLocation({
    //   success: (res) => {
    //     // 发送位置消息
    //     this.sendLocationMessage(res)
    //   },
    //   fail: (err) => {
    //     if (err.errMsg !== 'chooseLocation:fail cancel') {
    //       console.error('选择位置失败:', err)
    //       wx.showToast({
    //         title: '选择位置失败',
    //         icon: 'none'
    //       })
    //     }
    //   }
    // })
    wx.showToast({
      title: '位置选择功能暂时不可用',
      icon: 'none',
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
        longitude: location.longitude,
      },
      type: 'location',
      time: timeStr,
    }

    this.setData({
      messageList: [...messageList, newMessage],
      scrollIntoView: `msg-${newMessage.id}`,
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
      success: res => {
        if (res.tapIndex === 0) {
          // 从通讯录选择（需要用户授权）
          this.selectFromContacts()
        } else {
          // 从校友列表选择
          this.selectFromAlumni()
        }
      },
    })
  },

  selectFromContacts() {
    // 这里可以调用微信的通讯录选择API（如果有）
    // 或者跳转到自定义的联系人选择页面
    wx.showToast({
      title: '功能开发中',
      icon: 'none',
    })
  },

  selectFromAlumni() {
    // 跳转到校友列表页面选择
    wx.navigateTo({
      url: '/pages/alumni/list/list?mode=select',
      events: {
        selectAlumni: alumni => {
          this.sendContactMessage(alumni)
        },
      },
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
        school: contact.school,
      },
      type: 'contact',
      time: timeStr,
    }

    this.setData({
      messageList: [...messageList, newMessage],
      scrollIntoView: `msg-${newMessage.id}`,
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
        url: `/pages/alumni-association/detail/detail?id=${chatInfo.associationId || 1}`,
      })
    } else {
      // 跳转到个人主页
      wx.navigateTo({
        url: `/pages/alumni/detail/detail?id=${chatInfo.userId || this.data.chatId}`,
      })
    }
  },

  toggleEmoji() {
    this.setData({
      showEmoji: !this.data.showEmoji,
    })
  },

  insertEmoji(e) {
    const { emoji } = e.currentTarget.dataset
    const { inputValue } = this.data
    const newValue = inputValue + emoji
    this.setData({
      inputValue: newValue,
      hasInput: newValue.trim().length > 0,
      showEmoji: false,
    })
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset
    const { messageList } = this.data
    const imageUrls = messageList
      .filter(msg => msg.type === 'image' && msg.image)
      .map(msg => msg.image)

    wx.previewImage({
      current: url,
      urls: imageUrls,
    })
  },

  onLongPressMessage(e) {
    const { msg } = e.currentTarget.dataset
    // 只能撤回自己的消息
    if (!msg.isMe) {
      return
    }

    // 检查时间限制（2分钟内可撤回）
    const now = Date.now()
    // 优先使用 timestamp，如果不存在则尝试使用 id（如果是本地发送的 timestamp）
    // 如果 id 是数字且看起来是时间戳（大于某个合理值），则使用它
    let msgTime = msg.timestamp
    if (!msgTime) {
      const msgId = msg.id
      // 如果 id 是数字且大于 2020-01-01 的时间戳（1577836800000），则可能是时间戳
      if (typeof msgId === 'number' && msgId > 1577836800000) {
        msgTime = msgId
      } else if (typeof msgId === 'string' && !isNaN(msgId) && parseInt(msgId) > 1577836800000) {
        msgTime = parseInt(msgId)
      }
    }

    // 如果仍然没有时间戳，说明可能是从历史记录加载的消息，默认允许撤回（由后端判断）
    if (!msgTime) {
      // 没有时间戳，仍然显示撤回选项，让后端判断是否可以撤回
      wx.showActionSheet({
        itemList: ['撤回'],
        success: res => {
          if (res.tapIndex === 0) {
            this.recallMessage(msg)
          }
        },
      })
      return
    }

    // 检查是否在2分钟内
    if (now - msgTime > 2 * 60 * 1000) {
      return // 超过2分钟不可撤回，不显示菜单
    }

    wx.showActionSheet({
      itemList: ['撤回'],
      success: res => {
        if (res.tapIndex === 0) {
          this.recallMessage(msg)
        }
      },
    })
  },

  async recallMessage(msg) {
    try {
      wx.showLoading({ title: '撤回中' })

      // 检查消息ID是否是临时的（看起来像时间戳）
      let messageId = msg.id
      const isTemporaryId =
        typeof messageId === 'number' &&
        messageId > 1577836800000 &&
        Math.abs(Date.now() - messageId) < 60000 // 1分钟内的消息ID可能是临时的

      // 如果是临时ID，尝试重新加载消息来获取真实ID
      if (isTemporaryId && msg.content) {
        console.log('[ChatDetail] 检测到临时消息ID，尝试获取真实ID...')
        try {
          await this.reloadLatestMessages(msg.content, messageId)
          // 重新获取消息列表，查找更新后的消息
          const updatedMsg = this.data.messageList.find(
            m =>
              m.content === msg.content &&
              m.isMe &&
              m.id !== messageId &&
              Math.abs((m.timestamp || m.id) - messageId) < 10000
          )
          if (updatedMsg && updatedMsg.id !== messageId) {
            messageId = updatedMsg.id
            console.log('[ChatDetail] 获取到真实消息ID:', messageId)
          }
        } catch (e) {
          console.warn('[ChatDetail] 获取真实消息ID失败，使用临时ID:', e)
        }
      }

      const res = await chatApi.recallMessage(messageId)
      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '已撤回', icon: 'none' })

        // 更新本地消息列表（通过原始消息ID或内容匹配）
        const updatedList = this.data.messageList.map(item => {
          // 匹配消息：通过ID或内容和时间戳匹配
          if (
            item.id === msg.id ||
            item.id === messageId ||
            (item.content === msg.content &&
              item.isMe &&
              Math.abs((item.timestamp || item.id) - (msg.timestamp || msg.id)) < 10000)
          ) {
            // 替换为系统消息提示
            return {
              ...item,
              type: 'system',
              content: '你撤回了一条消息',
              isRecall: true,
            }
          }
          return item
        })

        this.setData({ messageList: updatedList })
      } else {
        wx.showToast({ title: res.data?.msg || '撤回失败', icon: 'none' })
        // 如果是"消息不存在"的错误，可能是消息ID还没更新，尝试重新加载
        if (res.data?.msg && res.data.msg.includes('不存在')) {
          console.log('[ChatDetail] 撤回失败，消息可能还未同步，尝试重新加载...')
          if (msg.content) {
            setTimeout(async () => {
              await this.reloadLatestMessages(msg.content, msg.id)
            }, 1000)
          }
        }
      }
    } catch (e) {
      wx.hideLoading()
      console.error('撤回消息失败:', e)
      wx.showToast({ title: '撤回失败', icon: 'none' })
    }
  },

  /**
   * 标记会话为已读
   * 进入聊天详情页时自动调用
   */
  async markConversationAsRead(chatId) {
    try {
      if (!chatId) {
        console.warn('[ChatDetail] 聊天ID为空，无法标记已读')
        return
      }

      console.log('[ChatDetail] 标记会话为已读，chatId:', chatId)
      const res = await chatApi.markConversationRead(chatId)

      if (res.data && res.data.code === 200) {
        console.log('[ChatDetail] 会话已标记为已读')
      } else {
        console.warn('[ChatDetail] 标记已读失败:', res.data?.msg)
      }
    } catch (error) {
      console.error('[ChatDetail] 标记已读异常:', error)
      // 静默失败，不影响页面正常使用
    }
  },
})
