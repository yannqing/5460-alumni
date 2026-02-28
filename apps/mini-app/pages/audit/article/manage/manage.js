// pages/audit/article/manage/manage.js
const app = getApp()
const { homeArticleApi, articleApplyApi } = require('../../../../api/api.js')

Page({
  data: {
    currentTab: 'all', // 当前选中的tab: 'all'、'pending' 或 'reviewed'
    articleList: [],
    filteredArticleList: [], // 根据tab过滤后的列表
    loading: false,
    loadingMore: false, // 是否正在加载更多
    // 分页相关
    currentPage: 1,
    pageSize: 10,
    hasMore: true,
    total: 0,
    // 审核弹框相关
    isApprovalModalVisible: false,
    approvalAction: 1, // 1-通过，2-拒绝
    currentApprovalId: null,
    approvalForm: {
      homeArticleApplyId: '',
      applyStatus: 1,
      appliedDescription: ''
    },
    // 权限控制
    canReview: false // 是否有审核权限（只有系统管理员才能审核）
  },

  onLoad(options) {
    // 检查审核权限
    this.checkReviewPermission()
    this.loadMyArticles(false)
  },

  // 检查用户是否有特定权限
  hasPermission(permissionCode) {
    const originalRoles = wx.getStorageSync('roles') || []

    for (const role of originalRoles) {
      if (role.permissions && Array.isArray(role.permissions)) {
        for (const permission of role.permissions) {
          if (permission.code === permissionCode) {
            return true
          }
          if (permission.children && Array.isArray(permission.children)) {
            for (const childPermission of permission.children) {
              if (childPermission.code === permissionCode) {
                return true
              }
            }
          }
        }
      }
    }
    return false
  },

  // 检查审核权限
  checkReviewPermission() {
    // 只有拥有文章审核权限的用户才能看到通过/拒绝按钮
    const canReview = this.hasPermission('HOME_PAGE_ARTICLE_REVIEW')
    this.setData({ canReview })
  },

  onShow() {
    this.loadMyArticles(false)
  },

  onPullDownRefresh() {
    this.loadMyArticles(false) // 下拉刷新，重新加载第一页
    wx.stopPullDownRefresh()
  },

  // 滚动到底部加载更多
  onReachBottom() {
    if (!this.data.loadingMore && this.data.hasMore) {
      this.loadMyArticles(true) // 加载更多
    }
  },

  // 加载我的上传文章列表
  // isLoadMore: true 表示加载更多，false 表示刷新/重新加载
  async loadMyArticles(isLoadMore = false) {
    const { loading, loadingMore, hasMore, currentPage, pageSize } = this.data

    // 如果正在加载，直接返回
    if (loading || loadingMore) return

    // 如果是加载更多但没有更多数据，直接返回
    if (isLoadMore && !hasMore) return

    // 设置加载状态
    if (isLoadMore) {
      this.setData({ loadingMore: true })
    } else {
      this.setData({
        loading: true,
        currentPage: 1,
        hasMore: true
      })
    }

    const page = isLoadMore ? currentPage + 1 : 1

    try {
      // 获取当前用户ID
      const currentUserId = app.globalData.userData?.userId || app.globalData.userData?.wxId || 'user123' // 默认用户ID用于测试

      let newArticleList = []

      // 获取本人创建的所有文章
      const articlePageParams = {
        current: page,
        size: pageSize
      }

      // 根据当前 tab 添加筛选参数
      const { currentTab } = this.data
      if (currentTab === 'pending') {
        articlePageParams.applyStatusList = [0] // 0-待审核
      } else if (currentTab === 'reviewed') {
        articlePageParams.applyStatusList = [1, 2] // 1-审核通过, 2-审核拒绝
      }
      // 'all' 不传 applyStatusList，查询所有状态

      // 调用API获取本人首页文章列表
      const articleRes = await homeArticleApi.getMyArticlePage(articlePageParams)

      if (articleRes.data && articleRes.data.code === 200) {
        const articleRecords = articleRes.data.data?.records || []
        const total = articleRes.data.data?.total || 0

        // 获取所有文章的审核记录
        const applyPageParams = {
          current: 1,
          size: 100, // 获取足够多的审核记录，确保能匹配上所有文章
          // 不传递applyStatus，获取所有状态的审核记录
        }

        const applyRes = await articleApplyApi.getApplyPage(applyPageParams)
        const applyRecords = applyRes.data && applyRes.data.code === 200 ? applyRes.data.data?.records || [] : []

        // 创建审核记录映射表，key为homeArticleId
        const applyMap = new Map()
        applyRecords.forEach(apply => {
          applyMap.set(apply.articleInfo?.homeArticleId, apply)
        })

        // 将API返回的数据转换为页面需要的格式
        newArticleList = articleRecords.map(item => {
          // 获取对应的审核记录
          const applyRecord = applyMap.get(item.homeArticleId)

          // 根据审核记录确定状态
          let status = 'pending'
          let statusText = '待审核'

          if (applyRecord) {
            // 有审核记录，使用审核记录的状态
            if (applyRecord.applyStatus === 1) {
              status = 'approved'
              statusText = '已通过'
            } else if (applyRecord.applyStatus === 2) {
              status = 'rejected'
              statusText = '已拒绝'
            } else {
              status = 'pending'
              statusText = '待审核'
            }
          } else {
            // 没有审核记录，默认显示待审核
            status = 'pending'
            statusText = '待审核'
          }

          // 格式化时间，移除T字符
          let formattedTime = item.createTime || ''
          if (formattedTime) {
            formattedTime = formattedTime.replace('T', ' ')
          }

          // 处理发布者头像
          let publisherAvatarUrl = ''
          if (item.publisherAvatar) {
            if (typeof item.publisherAvatar === 'object') {
              publisherAvatarUrl = item.publisherAvatar.fileUrl || item.publisherAvatar.thumbnailUrl || ''
            } else {
              publisherAvatarUrl = item.publisherAvatar
            }
            // 使用 config.getImageUrl 处理相对路径
            if (publisherAvatarUrl && !publisherAvatarUrl.startsWith('http')) {
              publisherAvatarUrl = config.getImageUrl(publisherAvatarUrl)
            }
          }

          return {
            id: item.homeArticleId,
            articleId: item.homeArticleId,
            applyId: applyRecord?.homeArticleApplyId || '', // 审核记录ID，用于审核操作
            articleTitle: item.articleTitle, // 文章标题
            publishUsername: item.publishUsername || '未知发布者', // 发布者昵称
            publisherAvatar: publisherAvatarUrl, // 发布者头像
            publishType: item.publishType || '', // 发布者类型
            submitTime: formattedTime,
            status: status,
            statusText: statusText,
            userId: currentUserId,
            coverImg: item.coverImg?.fileUrl?.replace(/`/g, '') || '' // 使用coverImg对象中的fileUrl字段，并移除可能存在的反引号
          }
        })

        // 计算是否还有更多数据
        const currentTotal = isLoadMore ? this.data.articleList.length + newArticleList.length : newArticleList.length
        const newHasMore = currentTotal < total

        // 合并或替换列表
        const finalList = isLoadMore ? [...this.data.articleList, ...newArticleList] : newArticleList

        this.setData({
          articleList: finalList,
          filteredArticleList: finalList,
          currentPage: page,
          total: total,
          hasMore: newHasMore,
          loading: false,
          loadingMore: false
        })
      } else {
        this.setData({
          loading: false,
          loadingMore: false
        })
      }
    } catch (error) {
      console.error('加载我的上传文章列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({
        loading: false,
        loadingMore: false
      })
    }
  },

  // Tab 切换
  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab !== this.data.currentTab) {
      this.setData({
        currentTab: tab,
        currentPage: 1,
        hasMore: true,
        articleList: [],
        filteredArticleList: []
      })
      this.loadMyArticles(false) // 重新加载第一页
    }
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article-publish/detail/detail?id=${id}&from=manage`
    })
  },

  // 发布文章
  onPublishTap() {
    wx.navigateTo({
      url: '/pages/article-publish/index/index'
    })
  },

  // 点击编辑按钮 - 跳转到编辑页面
  onEditTap(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/article/form/form?id=${id}`
    })
  },

  // 点击删除按钮
  onDeleteTap(e) {
    const { id } = e.currentTarget.dataset
    
    // 显示确认删除对话框
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这篇文章吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            this.setData({ loading: true })
            
            // 调用删除文章接口
            const deleteRes = await homeArticleApi.deleteArticle(id)
            
            // 详细日志，用于调试
            console.log('删除文章返回完整数据:', JSON.stringify(deleteRes))
            
            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              this.loadMyArticles() // 重新加载列表
            } else {
              console.error('删除失败:', deleteRes.data?.msg || '未知错误');
              wx.showToast({
                title: deleteRes.data?.msg || '删除失败',
                icon: 'none'
              })
            }
          } catch (error) {
            console.error('删除文章失败:', error)
            wx.showToast({
              title: '删除失败，请重试',
              icon: 'none'
            })
          } finally {
            this.setData({ loading: false })
          }
        }
      }
    })
  },

  // 点击通过按钮
  onApproveTap(e) {
    const { id } = e.currentTarget.dataset
    this.showApprovalModal(id, 1) // 1表示通过
  },

  // 点击拒绝按钮
  onRejectTap(e) {
    const { id } = e.currentTarget.dataset
    this.showApprovalModal(id, 2) // 2表示拒绝
  },

  // 显示审核弹框
  showApprovalModal(id, action) {
    this.setData({
      isApprovalModalVisible: true,
      approvalAction: action,
      currentApprovalId: id,
      approvalForm: {
        homeArticleApplyId: id,
        applyStatus: action,
        appliedDescription: ''
      }
    })
  },

  // 隐藏审核弹框
  hideApprovalModal() {
    this.setData({
      isApprovalModalVisible: false,
      currentApprovalId: null,
      approvalForm: {
        homeArticleApplyId: '',
        applyStatus: 1,
        appliedDescription: ''
      }
    })
  },

  // 处理审核表单输入
  onApprovalInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    this.setData({
      [`approvalForm.${field}`]: value
    })
  },

  // 提交审核
  async submitApproval() {
    try {
      wx.showLoading({ title: '提交中...' })

      const { approvalForm } = this.data

      // 调用审核API
      const res = await articleApplyApi.approveArticle(approvalForm)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: approvalForm.applyStatus === 1 ? '审核通过成功' : '审核拒绝成功',
          icon: 'success'
        })

        // 隐藏弹框
        this.hideApprovalModal()

        // 重新加载列表（需要先确保 loading 为 false）
        this.loadMyArticles(false)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('提交审核失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
  }
})
