const { homeArticleApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    articles: [],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadData(true)
  },

  onShow() {
    // 只有当不是首次加载时才刷新数据，避免 onLoad 和 onShow 重复调用
    // 或者可以通过页面栈判断是否是从详情页/表单页返回
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      // 如果自定义属性 isBack 为 true，则刷新
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

  async loadData(reset = false) {
    if (this.data.loading && !reset) return
    this.setData({ loading: true })

    try {
      const { current, pageSize } = this.data
      const page = reset ? 1 : current + 1
      const params = { current: page, pageSize }
      
      const res = await homeArticleApi.getMyArticlePage(params)
      
      if (res.data && res.data.code === 200) {
        const records = res.data.data.records || []
        const mappedRecords = records.map(item => {
          // 处理封面图逻辑：优先使用 coverImg 对象中的 fileUrl 或 thumbnailUrl
          let coverUrl = ''
          if (item.coverImg && typeof item.coverImg === 'object') {
            coverUrl = item.coverImg.thumbnailUrl || item.coverImg.fileUrl || ''
          } else {
            // 兼容旧字段或 coverImg 为 ID 的情况
            coverUrl = item.coverImg || item.cover || item.image || (item.images && item.images[0]) || ''
          }

          // 处理发布时间显示
          let displayTime = item.createTime || item.publishTime || ''
          if (displayTime) {
             // 简单处理时间格式，去掉秒
             displayTime = displayTime.split(' ')[0]
          }

          return {
            ...item,
            id: String(item.id || item.homeArticleId), // 确保 ID 为字符串
            title: item.articleTitle || item.title || '无标题',
            cover: config.getImageUrl(coverUrl),
            publisher: item.publishUsername || item.author || '未知用户',
            avatar: item.publishAvatar, // 使用默认头像或接口返回的头像
            time: displayTime,
            isTop: item.isTop === true || item.isTop === 1 || item.top === true
          }
        })

        this.setData({
          articles: reset ? mappedRecords : this.data.articles.concat(mappedRecords),
          current: page,
          hasMore: mappedRecords.length >= pageSize,
          loading: false
        })
      } else {
        this.setData({ loading: false })
      }
    } catch (err) {
      console.error(err)
      this.setData({ loading: false })
    }
    wx.stopPullDownRefresh()
  },

  goToAdd() {
    this.setData({ isBack: true })
    wx.navigateTo({ url: '/pages/article/form/form' })
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    this.setData({ isBack: true })
    wx.navigateTo({ url: `/pages/article/detail/detail?id=${id}&from=manage` })
  }
})