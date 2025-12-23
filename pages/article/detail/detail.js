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
    } else {
      wx.showToast({
        title: '缺少文章ID',
        icon: 'none'
      });
      this.setData({ loading: false });
    }
  },

  goToEdit() {
    wx.navigateTo({
      url: `/pages/article/form/form?id=${this.data.id}`
    })
  },

  loadArticleDetail(id) {
    // 确保ID有效
    if (!id || id === 'undefined' || id === 'null' || id === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' });
      this.setData({ loading: false });
      return;
    }
    
    this.setData({ loading: true })
    homeArticleApi.getHomeArticleDetail(id).then(res => {
      if (res.data && res.data.code === 200) {
        const article = res.data.data
        if (!article) {
          wx.showToast({
            title: '文章不存在',
            icon: 'none'
          });
          this.setData({ loading: false });
          return;
        }
        
        // 字段映射
        article.title = article.articleTitle || article.title || '无标题'
        article.author = article.publishUsername || article.author || article.groupName || article.source || '官方发布'
        
        // 处理时间格式，去掉T
        let displayTime = article.createTime || article.time || ''
        if (displayTime) {
          displayTime = displayTime.replace('T', ' ')
        }
        article.time = displayTime
        article.createTime = displayTime
        
        // 处理更新时间格式，去掉T
        if (article.updateTime) {
          article.updateTime = article.updateTime.replace('T', ' ')
        }
        
        article.content = article.content || article.description || ''

        // 处理封面图
        if (article.coverImg && !article.cover) {
          if (typeof article.coverImg === 'object') {
            article.cover = config.getImageUrl(article.coverImg.fileUrl || article.coverImg.thumbnailUrl || '');
          } else {
            article.cover = config.getImageUrl(`/file/download/${article.coverImg}`);
          }
        }
        
        // 处理发布者头像：优先使用 publisherAvatar，其次 publishAvatar
        let publisherAvatar = '';
        if (article.publisherAvatar) {
          // 如果 publisherAvatar 是对象，提取 URL
          if (typeof article.publisherAvatar === 'object') {
            publisherAvatar = article.publisherAvatar.fileUrl || article.publisherAvatar.thumbnailUrl || article.publisherAvatar.url || '';
          } else {
            // 如果是字符串，直接使用
            publisherAvatar = article.publisherAvatar;
          }
        } else if (article.publishAvatar) {
          if (typeof article.publishAvatar === 'object') {
            publisherAvatar = article.publishAvatar.fileUrl || article.publishAvatar.thumbnailUrl || article.publishAvatar.url || '';
          } else {
            publisherAvatar = article.publishAvatar;
          }
        }
        
        // 处理头像 URL
        if (publisherAvatar) {
          article.publisherAvatar = config.getImageUrl(publisherAvatar);
        }
        
        // 处理富文本中的图片宽度
        if (article.content && typeof article.content === 'string') {
          article.content = article.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;"')
        }
        
        this.setData({
          article: article,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '获取详情失败',
          icon: 'none',
          duration: 2000
        })
        this.setData({ loading: false })
      }
    }).catch(err => {
      wx.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none',
        duration: 2000
      })
      this.setData({ loading: false })
    })
  },

  // 预览图片
  previewImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      current: url,
      urls: [url]
    })
  },

  // 打开链接
  openLink(e) {
    const link = e.currentTarget.dataset.link
    if (link) {
      // 如果是外部链接，需要复制到剪贴板或使用web-view
      wx.setClipboardData({
        data: link,
        success: () => {
          wx.showToast({
            title: '链接已复制',
            icon: 'success'
          })
        }
      })
    }
  }
})