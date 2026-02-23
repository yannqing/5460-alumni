const { articleApplyApi, homeArticleApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    applyList: [],
    currentTab: 0, // 0-待审核，1-已审核，2-已拒绝
    tabs: ['待审核', '已审核', '已拒绝'],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadData(true)
  },

  onShow() {
    // 如果从详情页返回，刷新数据
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      if (currentPage.data.isBack) {
        this.loadData(true)
        currentPage.setData({ isBack: false })
      }
    }
  },

  onPullDownRefresh() {
    this.loadData(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadData(false)
    }
  },

  // 切换标签
  switchTab(e) {
    const { index } = e.currentTarget.dataset
    this.setData({
      currentTab: index,
      applyList: [],
      current: 1,
      hasMore: true
    })
    this.loadData(true)
  },

  // 加载审核列表数据
  async loadData(reset = false) {
    if (this.data.loading && !reset) {return}
    this.setData({ loading: true })

    try {
      const { current, pageSize, currentTab } = this.data
      const page = reset ? 1 : current + 1
      const params = { current: page, size: pageSize }

      // 根据不同标签设置审核状态
      if (currentTab === 0) {
        // 待审核列表
        params.applyStatus = 0
      } else if (currentTab === 1) {
        // 已通过列表
        params.applyStatus = 1
      } else if (currentTab === 2) {
        // 已拒绝列表
        params.applyStatus = 2
      }

      // 使用统一的审核列表API
      const res = await articleApplyApi.getApplyPage(params)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []
        const total = data.total || 0

        // 处理数据，获取文章详情
        const mappedRecords = await Promise.all(
          records.map(async (item) => {
            // 获取文章详情
            let articleInfo = null
            if (item.homeArticleId) {
              try {
                const articleRes = await homeArticleApi.getHomeArticleDetail(item.homeArticleId)
                if (articleRes.data && articleRes.data.code === 200) {
                  articleInfo = articleRes.data.data
                }
              } catch (err) {
                // 获取文章详情失败，继续处理
              }
            }

            // 处理审核状态
            let statusText = '未知'
            let statusClass = ''
            if (item.applyStatus === 0) {
              statusText = '待审核'
              statusClass = 'pending'
            } else if (item.applyStatus === 1) {
              statusText = '已通过'
              statusClass = 'approved'
            } else if (item.applyStatus === 2) {
              statusText = '已拒绝'
              statusClass = 'rejected'
            }

            // 处理时间：去掉T
            let createTime = item.createTime || ''
            if (createTime) {
              createTime = createTime.replace('T', ' ')
            }

            let completedTime = item.completedTime || ''
            if (completedTime) {
              completedTime = completedTime.replace('T', ' ')
            }

            // 处理文章信息
            let articleTitle = '加载中...'
            let articleDescription = ''
            let publishUsername = ''
            let publisherAvatar = ''
            let coverImg = ''

            if (articleInfo) {
              // 处理标题
              articleTitle = articleInfo.articleTitle || articleInfo.title || '无标题'
              
              // 处理描述
              articleDescription = articleInfo.description || articleInfo.content || ''
              
              // 处理发布者名称
              publishUsername = articleInfo.publishUsername || '未知用户'
              
              // 处理发布者头像
              if (articleInfo.publisherAvatar) {
                if (typeof articleInfo.publisherAvatar === 'object') {
                  publisherAvatar = articleInfo.publisherAvatar.fileUrl || articleInfo.publisherAvatar.thumbnailUrl || articleInfo.publisherAvatar.url || ''
                } else {
                  publisherAvatar = articleInfo.publisherAvatar
                }
                if (publisherAvatar) {
                  publisherAvatar = config.getImageUrl(publisherAvatar)
                }
              }
              
              // 处理封面图
              if (articleInfo.coverImg) {
                if (typeof articleInfo.coverImg === 'object') {
                  coverImg = articleInfo.coverImg.thumbnailUrl || articleInfo.coverImg.fileUrl || ''
                  if (coverImg) {
                    coverImg = config.getImageUrl(coverImg)
                  }
                } else {
                  coverImg = config.getImageUrl(`/file/download/${articleInfo.coverImg}`)
                }
              }
            }

            return {
              ...item,
              id: item.homeArticleApplyId ? String(item.homeArticleApplyId) : '',
              homeArticleId: item.homeArticleId ? String(item.homeArticleId) : '',
              articleTitle: articleTitle,
              articleDescription: articleDescription,
              publishUsername: publishUsername,
              publisherAvatar: publisherAvatar,
              coverImg: coverImg,
              statusText: statusText,
              statusClass: statusClass,
              createTime: createTime,
              completedTime: completedTime,
              appliedName: item.appliedName || '',
              appliedDescription: item.appliedDescription || ''
            }
          })
        )

        const currentTotal = reset ? mappedRecords.length : this.data.applyList.length + mappedRecords.length
        this.setData({
          applyList: reset ? mappedRecords : this.data.applyList.concat(mappedRecords),
          current: page,
          hasMore: currentTotal < total && mappedRecords.length > 0,
          loading: false
        })
      } else {
        wx.showToast({ title: res.data?.msg || '加载失败', icon: 'none' })
        this.setData({ loading: false })
      }
    } catch (err) {
      wx.showToast({ title: '加载失败，请稍后重试', icon: 'none' })
      this.setData({ loading: false })
    }
    wx.stopPullDownRefresh()
  },

  // 查看文章详情
  viewArticleDetail(e) {
    const { id, index } = e.currentTarget.dataset
    let articleId = id
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      if (index !== undefined && this.data.applyList[index]) {
        articleId = this.data.applyList[index].homeArticleId
      }
    }

    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' })
      return
    }

    this.setData({ isBack: true })
    wx.navigateTo({
      url: `/pages/article/detail/detail?id=${articleId}&from=audit`,
      fail: (err) => {
        wx.showToast({ title: '跳转失败', icon: 'none' })
      }
    })
  },

  // 审核通过
  async approveArticle(e) {
    await this.handleAudit(e, 1, '确定要通过这篇文章的审核吗？', '审核通过')
  },

  // 审核拒绝
  async rejectArticle(e) {
    await this.handleAudit(e, 2, '确定要拒绝这篇文章的审核吗？', '审核拒绝')
  },

  // 统一的审核处理方法
  async handleAudit(e, applyStatus, confirmText, successText) {
    const { id, index } = e.currentTarget.dataset
    let applyId = id
    if (!applyId || applyId === 'undefined' || applyId === 'null' || applyId === '') {
      if (index !== undefined && this.data.applyList[index]) {
        applyId = this.data.applyList[index].id
      }
    }

    if (!applyId || applyId === 'undefined' || applyId === 'null' || applyId === '') {
      wx.showToast({ title: '审核记录ID错误', icon: 'none' })
      return
    }

    // 确保ID是字符串形式，避免大整数精度丢失
    // 后端Jackson会自动将字符串数字转换为Long类型
    const applyIdStr = String(applyId).trim()
    
    // 验证ID是否为有效数字字符串
    if (!/^\d+$/.test(applyIdStr)) {
      wx.showToast({ title: '审核记录ID格式错误', icon: 'none' })
      return
    }

    wx.showModal({
      title: '确认审核',
      content: confirmText,
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '审核中...' })

            const approveData = {
              homeArticleApplyId: applyIdStr, // 直接使用字符串，后端会自动转换为Long
              applyStatus: applyStatus, // 1-通过，2-拒绝
              appliedDescription: ''
            }

            const approveRes = await articleApplyApi.approveArticle(approveData)
            wx.hideLoading()

            if (approveRes.data && approveRes.data.code === 200) {
              wx.showToast({ title: successText, icon: 'success' })
              // 刷新列表
              this.loadData(true)
            } else {
              wx.showToast({ title: approveRes.data?.msg || '审核失败', icon: 'none' })
            }
          } catch (err) {
            wx.hideLoading()
            wx.showToast({ title: '审核失败', icon: 'none' })
          }
        }
      }
    })
  }
})

