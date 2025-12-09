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
      // 根据currentTab选择API
      let api
      if (currentTab === 'follow') {
        api = followApi.getMyFollowingList
      } else if (currentTab === 'fans') {
        api = followApi.getMyFollowerList
      } else if (currentTab === 'friend') {
        api = followApi.getMyFriendList
      }

      const params = {
        page: reset ? 1 : page,
        size: pageSize
      }

      // 如果有类型筛选，添加targetType参数（好友列表不支持类型筛选）
      if (targetType && currentTab !== 'friend') {
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

  // 数据映射
  mapItem(item) {
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
        targetDescription = targetInfo.description || targetInfo.intro || ''
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
