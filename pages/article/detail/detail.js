const { homeArticleApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    id: '',
    article: null,
    loading: true,
    canEdit: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ 
        id: options.id,
        canEdit: options.from === 'manage'
      })
      this.loadArticleDetail(options.id)
    }
  },

  goToEdit() {
    wx.navigateTo({
      url: `/pages/article/form/form?id=${this.data.id}`
    })
  },

  loadArticleDetail(id) {
    this.setData({ loading: true })
    homeArticleApi.getHomeArticleDetail(id).then(res => {
      if (res.data && res.data.code === 200) {
        const article = res.data.data
        // 字段映射
        article.title = article.articleTitle || article.title
        article.author = article.publishUsername || article.author || article.groupName || article.source || '官方发布'
        article.time = article.createTime || article.time
        article.content = article.content || article.description

        // 处理封面图
        if (article.coverImg && !article.cover) {
          article.cover = config.getImageUrl(`/file/download/${article.coverImg}`)
        }
        
        // 处理富文本中的图片宽度
        if (article.content) {
          article.content = article.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;"')
        }
        this.setData({
          article: article,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data.msg || '获取详情失败',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    }).catch(err => {
      console.error(err)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
      this.setData({ loading: false })
    })
  }
})