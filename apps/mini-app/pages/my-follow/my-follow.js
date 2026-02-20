// pages/my-follow/my-follow.js
const config = require('../../utils/config.js')
const { followApi } = require('../../api/api.js')

// 类型映射
const TYPE_MAP = {
  user: 1,        // 用户
  association: 2, // 校友会
  school: 3,      // 母校
  merchant: 4     // 商铺
}

Page({
  data: {
    currentTab: 'follow', // follow: 我的关注, fans: 我的粉丝, friend: 好友
    activeType: '', // '', 'user', 'association', 'school', 'merchant'
    list: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    // 设置当前tab：follow(关注)、fans(粉丝) 或 friend(好友)
    const tab = options.tab || 'follow'
    this.setData({ currentTab: tab })

    // 加载数据
    this.loadList(true)
  },

  onShow() {
    // 页面显示时，如果列表为空则加载数据
    if (this.data.list.length === 0) {
      this.loadList(true)
    }
  },

  onPullDownRefresh() {
    this.loadList(true)
    wx.stopPullDownRefresh()
  },

  // 切换Tab（关注/粉丝/好友）
  switchTab(e) {
    const { tab } = e.currentTarget.dataset
    if (tab === this.data.currentTab) return

    this.setData({
      currentTab: tab,
      activeType: '',
      list: [],
      page: 1,
      hasMore: true
    })

    this.loadList(true)
  },

  // 选择类型筛选
  selectType(e) {
    const { type } = e.currentTarget.dataset
    if (type === this.data.activeType) return

    this.setData({
      activeType: type,
      list: [],
      page: 1,
      hasMore: true
    })

    this.loadList(true)
  },

  // 加载列表
  async loadList(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    this.setData({ loading: true })

    const { currentTab, activeType, page, pageSize } = this.data
    const targetType = activeType ? TYPE_MAP[activeType] : undefined

    try {
      // 好友列表：通过关注状态筛选互相关注的用户
      if (currentTab === 'friend') {
        await this.loadFriendListByMutualFollow(reset)
        return
      }

      // 根据currentTab选择API
      let api
      if (currentTab === 'follow') {
        api = followApi.getMyFollowingList
      } else if (currentTab === 'fans') {
        // 粉丝列表需要根据类型筛选，需要特殊处理
        await this.loadFansListWithTypeFilter(reset)
        return
      }

      const params = {
        page: reset ? 1 : page,
        size: pageSize
      }

      // 如果有类型筛选，添加targetType参数
      if (targetType) {
        params.targetType = targetType
      }

      const res = await api(params)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []

        // 数据映射
        const mappedList = records
          .filter(item => item.followStatus !== 4)  // 过滤掉状态为4（取消关注）的数据
          .map(item => this.mapItem(item))

        this.setData({
          list: reset ? mappedList : [...this.data.list, ...mappedList],
          page: reset ? 2 : page + 1,
          hasMore: mappedList.length >= pageSize,
          loading: false
        })
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 根据类型筛选加载粉丝列表
  async loadFansListWithTypeFilter(reset = false) {
    try {
      const { activeType, page, pageSize } = this.data
      const targetType = activeType ? TYPE_MAP[activeType] : undefined

      // 1. 获取粉丝列表（后端只返回用户类型）
      const fansRes = await followApi.getMyFollowerList({
        page: reset ? 1 : page,
        size: pageSize * 2 // 获取更多数据，因为需要筛选
      })

      if (!fansRes.data || fansRes.data.code !== 200) {
        this.setData({ loading: false })
        wx.showToast({
          title: fansRes.data?.msg || '加载失败',
          icon: 'none'
        })
        return
      }

      const fansData = fansRes.data.data || {}
      const fansRecords = fansData.records || []

      // 2. 如果没有类型筛选（全部），直接返回所有粉丝
      if (!targetType) {
        const mappedList = fansRecords
          .filter(item => item.followStatus !== 4)
          .map(item => this.mapItem(item))
          .slice(0, pageSize)

        this.setData({
          list: reset ? mappedList : [...this.data.list, ...mappedList],
          page: reset ? 2 : page + 1,
          hasMore: mappedList.length >= pageSize && fansRecords.length >= pageSize * 2,
          loading: false
        })
        return
      }

      // 3. 如果有类型筛选，需要检查我是否也关注了对方，以及我关注对方的类型
      // 获取我关注的列表（根据筛选类型）
      const followingRes = await followApi.getMyFollowingList({
        page: 1,
        size: 999, // 获取所有关注，用于匹配
        targetType: targetType
      })

      const followingData = followingRes.data?.data || {}
      const followingRecords = followingData.records || []

      // 构建我关注的用户ID集合（根据类型）
      const followingIdSet = new Set()
      followingRecords.forEach(item => {
        if (item.targetId && item.followStatus !== 4) {
          followingIdSet.add(String(item.targetId))
        }
      })

      // 4. 筛选粉丝：只显示我也关注了对方，且我关注对方的类型匹配筛选条件的
      const filteredFans = fansRecords
        .filter(item => {
          // 过滤掉取消关注的
          if (item.followStatus === 4) return false
          // 检查我是否也关注了对方
          const wxId = String(item.wxId || '')
          return followingIdSet.has(wxId)
        })
        .slice(0, pageSize)

      // 5. 数据映射
      const mappedList = filteredFans.map(item => this.mapItem(item))

      this.setData({
        list: reset ? mappedList : [...this.data.list, ...mappedList],
        page: reset ? 2 : page + 1,
        hasMore: filteredFans.length >= pageSize && fansRecords.length >= pageSize * 2,
        loading: false
      })
    } catch (error) {
      console.error('加载粉丝列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 通过互相关注状态加载好友列表
  async loadFriendListByMutualFollow(reset = false) {
    try {
      const { page, pageSize } = this.data
      
      // 1. 获取"我的关注"列表（只获取用户类型）
      const followingRes = await followApi.getMyFollowingList({
        page: reset ? 1 : page,
        size: pageSize * 2, // 获取更多数据，因为需要筛选
        targetType: 1 // 只获取用户类型
      })

      if (!followingRes.data || followingRes.data.code !== 200) {
        this.setData({ loading: false })
        wx.showToast({
          title: followingRes.data?.msg || '加载失败',
          icon: 'none'
        })
        return
      }

      const followingData = followingRes.data.data || {}
      const followingRecords = followingData.records || []

      // 2. 获取"我的粉丝"列表（用于检查互相关注）
      const followerRes = await followApi.getMyFollowerList({
        page: 1,
        size: 999 // 获取所有粉丝，用于检查互相关注
      })

      const followerData = followerRes.data?.data || {}
      const followerRecords = followerData.records || []
      
      // 构建粉丝ID集合，用于快速查找
      const followerWxIdSet = new Set()
      followerRecords.forEach(item => {
        if (item.wxId && item.followStatus !== 4) {
          followerWxIdSet.add(String(item.wxId))
        }
      })

      // 3. 筛选出互相关注的用户（我关注了对方，且对方也关注了我）
      const mutualFollowList = followingRecords
        .filter(item => {
          // 只处理用户类型
          if (item.targetType !== 1) return false
          // 过滤掉取消关注的
          if (item.followStatus === 4) return false
          // 检查对方是否也关注了我
          const targetId = String(item.targetId || '')
          return followerWxIdSet.has(targetId)
        })
        .slice(0, pageSize) // 限制返回数量

      // 4. 数据映射
      const mappedList = mutualFollowList.map(item => this.mapItem(item))

      this.setData({
        list: reset ? mappedList : [...this.data.list, ...mappedList],
        page: reset ? 2 : page + 1,
        hasMore: mutualFollowList.length >= pageSize && followingRecords.length >= pageSize * 2,
        loading: false
      })
    } catch (error) {
      console.error('加载好友列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 数据映射
  mapItem(item) {
    const currentTab = this.data.currentTab
    
    // 我的粉丝列表：数据直接在 item 上，不在 targetInfo 中
    if (currentTab === 'fans') {
      const followId = item.followId
      const wxId = item.wxId || ''
      const userName = item.userName || '未知用户'
      const avatar = item.avatar || ''
      const description = item.description || ''
      
      // 处理头像URL
      let avatarUrl = ''
      if (avatar) {
        avatarUrl = config.getImageUrl(avatar)
      } else {
        avatarUrl = config.defaultAvatar || ''
      }
      
      // 处理关注时间
      const followTime = this.formatTime(item.createdTime)
      
      // 关注状态文本映射
      const followStatusText = {
        1: '已关注',
        2: '特别关注',
        3: '免打扰',
        4: '取消关注'
      }
      
      // 返回统一格式的数据
      return {
        id: followId,
        targetId: wxId, // 粉丝列表使用 wxId 作为 targetId
        targetType: 1,  // 粉丝列表固定为用户类型
        targetName: userName,
        targetAvatar: avatarUrl,
        targetDescription: description,
        followStatus: item.followStatus || 1,
        followStatusText: followStatusText[item.followStatus] || '已关注',
        remark: item.remark || '',
        followTime: followTime
      }
    }
    
    // 好友列表：通过关注列表筛选出的互相关注用户，数据结构与关注列表相同
    // 注意：现在好友列表的数据来自"我的关注"列表，所以使用关注列表的映射逻辑
    // 如果以后改为使用 getMyFriendList 接口，则需要使用下面的映射逻辑
    if (currentTab === 'friend') {
      // 好友列表现在从关注列表中筛选，所以数据结构与关注列表相同
      // 使用关注列表的映射逻辑（在下面的代码中处理）
      // 如果后端返回的是 FriendItemVo 结构，则使用以下逻辑：
      if (item.friendName) {
        // 这是 FriendItemVo 结构（从 getMyFriendList 接口返回）
        const friendshipId = item.friendshipId || ''
        const friendWxId = item.friendWxId || ''
        const friendName = item.friendName || '未知用户'
        const friendAvatar = item.friendAvatar || ''
        const friendDescription = item.friendDescription || ''
        
        // 处理头像URL
        let avatarUrl = ''
        if (friendAvatar) {
          avatarUrl = config.getImageUrl(friendAvatar)
        } else {
          avatarUrl = config.defaultAvatar || ''
        }
        
        // 处理添加时间（好友列表使用 addTime，不是 createdTime）
        const followTime = this.formatTime(item.addTime)
        
        // 好友列表的状态文本映射（使用 status 字段）
        const statusTextMap = {
          1: '已关注',
          2: '仅聊天',
          3: '免打扰',
          4: '已隐藏',
          5: '已拉黑'
        }
        
        // 返回统一格式的数据
        return {
          id: friendshipId,
          targetId: friendWxId, // 好友列表使用 friendWxId 作为 targetId
          targetType: 1,  // 好友列表固定为用户类型
          targetName: friendName,
          targetAvatar: avatarUrl,
          targetDescription: friendDescription,
          followStatus: item.status || 1, // 好友列表使用 status 字段
          followStatusText: statusTextMap[item.status] || '已关注',
          remark: item.myRemark || '', // 好友列表使用 myRemark
          followTime: followTime
        }
      }
      // 否则，继续使用关注列表的映射逻辑（从 targetInfo 中提取）
    }
    
    // 我的关注列表：数据在 targetInfo 中
    const followId = item.followId
    const targetType = item.targetType
    const targetId = item.targetId
    const targetInfo = item.targetInfo || {}

    // 从 targetInfo 中提取名称（根据不同类型）
    let targetName = '未知'
    let avatarUrl = ''
    let targetDescription = ''

    switch(targetType) {
      case 1: // 用户
        targetName = targetInfo.nickname || targetInfo.username || targetInfo.name || '未知用户'
        avatarUrl = targetInfo.avatar || targetInfo.avatarUrl || ''
        targetDescription = targetInfo.signature || targetInfo.description || ''
        break
      case 2: // 校友会
        targetName = targetInfo.associationName || targetInfo.name || targetInfo.alumniName || '未知校友会'
        avatarUrl = targetInfo.logo || targetInfo.avatar || ''
        // 优先显示地址，如果没有地址再显示描述
        targetDescription = targetInfo.location ||
                           (targetInfo.province && targetInfo.city ? `${targetInfo.province} ${targetInfo.city}` : '') ||
                           targetInfo.description || targetInfo.intro || ''
        break
      case 3: // 母校
        targetName = targetInfo.schoolName || targetInfo.name || '未知母校'
        avatarUrl = targetInfo.logo || ''
        targetDescription = targetInfo.province && targetInfo.city 
          ? `${targetInfo.province} ${targetInfo.city}` 
          : (targetInfo.description || '')
        break
      case 4: // 商铺
        targetName = targetInfo.shopName || targetInfo.merchantName || targetInfo.name || '未知商铺'
        avatarUrl = targetInfo.logo || targetInfo.avatar || ''
        targetDescription = targetInfo.address || targetInfo.description || ''
        break
      default:
        targetName = targetInfo.name || item.targetName || '未知'
        avatarUrl = targetInfo.avatar || targetInfo.logo || item.targetAvatar || ''
        targetDescription = targetInfo.description || item.targetDescription || ''
    }

    // 处理头像URL
    if (avatarUrl) {
      avatarUrl = config.getImageUrl(avatarUrl)
    } else {
      // 使用默认头像
      avatarUrl = targetType === 1 ? config.defaultAvatar :
                  targetType === 2 ? config.defaultAlumniAvatar :
                  targetType === 3 ? config.defaultSchoolAvatar :
                  config.defaultAvatar
    }

    // 处理关注时间
    const followTime = this.formatTime(item.createdTime)

    // 关注状态文本映射
    const followStatusText = {
      1: '已关注',
      2: '特别关注',
      3: '免打扰',
      4: '取消关注'
    }

    // 返回统一格式的数据
    return {
      id: followId,
      targetId: targetId,
      targetType: targetType,
      targetName: targetName,
      targetAvatar: avatarUrl,
      targetDescription: targetDescription,
      followStatus: item.followStatus || 1,
      followStatusText: followStatusText[item.followStatus] || '已关注',
      remark: item.remark || '',
      followTime: followTime
    }
  },

  // 格式化时间
  formatTime(timestamp) {
    if (!timestamp) return ''

    const date = new Date(timestamp)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}-${month}-${day}`
  },

  // 加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadList(false)
  },

  // 更新关注状态
  updateFollowStatus(e) {
    const { item } = e.currentTarget.dataset
    const { id, targetType, targetId, followStatus } = item

    // 状态选项（移除取消关注选项）
    const statusOptions = [
      { value: 1, text: '正常关注' },
      { value: 2, text: '特别关注' },
      { value: 3, text: '免打扰' }
    ]

    // 过滤掉当前状态
    const filteredOptions = statusOptions.filter(opt => opt.value !== followStatus)

    // 添加取消关注选项
    const allOptions = [...filteredOptions, { value: 'remove', text: '取消关注' }]

    wx.showActionSheet({
      itemList: allOptions.map(opt => opt.text),
      success: async (res) => {
        if (res.cancel) return

        const selectedOption = allOptions[res.tapIndex]

        try {
          // 如果选择的是取消关注，调用 removeFollow 接口
          if (selectedOption.value === 'remove') {
            const result = await followApi.removeFollow({
              id: id,
              targetType: targetType,
              targetId: targetId
            })

            if (result.data && result.data.code === 200) {
              // 从列表中移除该项
              const { list } = this.data
              const filteredList = list.filter(i => i.id !== id)

              this.setData({ list: filteredList })

              wx.showToast({
                title: '已取消关注',
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: result.data?.msg || '取消关注失败',
                icon: 'none'
              })
            }
          } else {
            // 更新关注状态
            const selectedStatus = selectedOption.value
            const result = await followApi.updateFollowStatus({
              id: id,
              targetType: targetType,
              targetId: targetId,
              followStatus: selectedStatus
            })

            if (result.data && result.data.code === 200) {
              // 更新本地列表数据
              const { list } = this.data
              const index = list.findIndex(i => i.targetId === targetId && i.targetType === targetType)

              if (index !== -1) {
                const statusTextMap = {
                  1: '正常关注',
                  2: '特别关注',
                  3: '免打扰'
                }

                list[index].followStatus = selectedStatus
                list[index].followStatusText = statusTextMap[selectedStatus]

                this.setData({ list })

                wx.showToast({
                  title: '状态更新成功',
                  icon: 'success'
                })
              }
            } else {
              wx.showToast({
                title: result.data?.msg || '更新失败',
                icon: 'none'
              })
            }
          }
        } catch (error) {
          console.error('操作失败:', error)
          wx.showToast({
            title: '操作失败，请重试',
            icon: 'none'
          })
        }
      }
    })
  },

  // 查看详情
  viewDetail(e) {
    const { item } = e.currentTarget.dataset
    const { targetType, targetId } = item

    let url = ''
    switch (targetType) {
      case 1: // 用户
        url = `/pages/alumni/detail/detail?id=${targetId}`
        break
      case 2: // 校友会
        url = `/pages/alumni-association/detail/detail?id=${targetId}`
        break
      case 3: // 母校
        url = `/pages/school/detail/detail?id=${targetId}`
        break
      case 4: // 商铺
        url = `/pages/shop/detail/detail?id=${targetId}`
        break
      default:
        wx.showToast({
          title: '未知类型',
          icon: 'none'
        })
        return
    }

    wx.navigateTo({ url })
  }
})
