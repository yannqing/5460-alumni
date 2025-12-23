const { homeArticleApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const app = getApp()

// 引入删除API
const { deleteArticle } = homeArticleApi

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
      const params = { current: page, size: pageSize }
      
      const res = await homeArticleApi.getMyArticlePage(params)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {};
        const records = data.records || []
        const total = data.total || 0;
        
        const mappedRecords = records.map((item, idx) => {
          // 处理封面图逻辑：优先使用 coverImg 字段
          let coverUrl = ''
          if (item.coverImg) {
            if (typeof item.coverImg === 'object') {
              // 如果是对象，使用 thumbnailUrl 或 fileUrl
              coverUrl = item.coverImg.thumbnailUrl || item.coverImg.fileUrl || ''
            } else {
              // 如果是ID（数字或字符串），构造下载URL
              coverUrl = `/file/download/${item.coverImg}`;
            }
          }
          
          // 如果 coverImg 处理后仍为空，尝试兼容旧字段
          if (!coverUrl) {
            coverUrl = item.cover || item.image || (item.images && item.images[0]) || ''
          }

          // 处理发布时间显示
          let displayTime = item.createTime || item.publishTime || ''
          if (displayTime) {
             // 将 ISO 格式中的 T 替换为空格，如 "2025-12-22T06:05:13" -> "2025-12-22 06:05:13"
             displayTime = displayTime.replace('T', ' ')
          }

          // 处理头像：优先使用 publisherAvatar，其次 publishAvatar，最后 avatar
          let avatar = '';
          if (item.publisherAvatar) {
            // 如果 publisherAvatar 是对象，提取 URL
            if (typeof item.publisherAvatar === 'object') {
              avatar = item.publisherAvatar.fileUrl || item.publisherAvatar.thumbnailUrl || item.publisherAvatar.url || '';
            } else {
              // 如果是字符串，直接使用
              avatar = item.publisherAvatar;
            }
          } else if (item.publishAvatar) {
            if (typeof item.publishAvatar === 'object') {
              avatar = item.publishAvatar.fileUrl || item.publishAvatar.thumbnailUrl || item.publishAvatar.url || '';
            } else {
              avatar = item.publishAvatar;
            }
          } else if (item.avatar) {
            if (typeof item.avatar === 'object') {
              avatar = item.avatar.fileUrl || item.avatar.thumbnailUrl || item.avatar.url || '';
            } else {
              avatar = item.avatar;
            }
          }
          
          // 处理头像 URL
          if (avatar) {
            avatar = config.getImageUrl(avatar);
          }

          // 处理发布类型
          const publishType = item.publishType || item.publish_type || null;

          // 处理ID：严格使用 homeArticleId（数据库字段 home_article_id）
          // 绝对不使用 coverImg（这是图片ID，不是文章ID）
          let articleId = null;
          
          // 数据库字段是 home_article_id，后端可能返回 homeArticleId 或 home_article_id
          if (item.homeArticleId !== undefined && item.homeArticleId !== null && item.homeArticleId !== '') {
            articleId = item.homeArticleId;
          } else if (item.home_article_id !== undefined && item.home_article_id !== null && item.home_article_id !== '') {
            articleId = item.home_article_id;
          }
          
          // 如果使用 id 字段，必须严格验证不是 coverImg
          if (!articleId && item.id !== undefined && item.id !== null && item.id !== '') {
            // 确保 id 不是 coverImg
            if (String(item.id) !== String(item.coverImg)) {
              articleId = item.id;
            }
          }
          
          // 最后尝试其他可能的ID字段，但必须排除 coverImg
          if (!articleId) {
            const otherIdFields = ['articleId', 'homePageArticleId'];
            for (const field of otherIdFields) {
              if (item[field] !== undefined && item[field] !== null && item[field] !== '' && 
                  String(item[field]) !== String(item.coverImg)) {
                articleId = item[field];
                break;
              }
            }
          }
          
          // 最终验证：确保不是 coverImg
          if (articleId && String(articleId) === String(item.coverImg)) {
            articleId = null;
          }
          
          // 如果还是没有找到
          if (!articleId && articleId !== 0) {
            articleId = '';
          } else {
            articleId = String(articleId);
          }

          return {
            ...item,
            id: articleId, // 使用处理后的ID（确保不是coverImg）
            homeArticleId: articleId, // 同时保存 homeArticleId，确保一致性
            title: item.articleTitle || item.title || '无标题',
            cover: coverUrl ? config.getImageUrl(coverUrl) : '',
            publisher: item.publishUsername || item.author || '未知用户',
            avatar: avatar,
            publisherAvatar: avatar, // 保存 publisherAvatar 字段
            publishType: publishType, // 保存 publishType 字段
            time: displayTime,
            isTop: item.isTop === true || item.isTop === 1 || item.top === true
          }
        })

        const currentTotal = reset ? mappedRecords.length : this.data.articles.length + mappedRecords.length;
        this.setData({
          articles: reset ? mappedRecords : this.data.articles.concat(mappedRecords),
          current: page,
          hasMore: currentTotal < total && mappedRecords.length > 0,
          loading: false
        });
      } else {
        wx.showToast({ title: res.data?.msg || '加载失败', icon: 'none' });
        this.setData({ loading: false });
      }
    } catch (err) {
      wx.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
      this.setData({ loading: false });
    }
    wx.stopPullDownRefresh()
  },

  goToAdd() {
    this.setData({ isBack: true })
    wx.navigateTo({ url: '/pages/article/form/form' })
  },

  goToDetail(e) {
    const { id, index } = e.currentTarget.dataset
    // 如果ID无效，尝试从列表数据中获取
    let articleId = id;
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      if (index !== undefined && this.data.articles[index]) {
        articleId = this.data.articles[index].id || this.data.articles[index].homeArticleId;
      }
    }
    
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' });
      return;
    }
    
    this.setData({ isBack: true })
    wx.navigateTo({ 
      url: `/pages/article/detail/detail?id=${articleId}&from=manage`,
      fail: (err) => {
        wx.showToast({ title: '跳转失败', icon: 'none' });
      }
    })
  },

  // 删除文章
  async deleteArticle(e) {
    const { id, index } = e.currentTarget.dataset;
    // 如果ID无效，尝试从列表数据中获取
    let articleId = id;
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      if (index !== undefined && this.data.articles[index]) {
        articleId = this.data.articles[index].id || this.data.articles[index].homeArticleId;
      }
    }
    
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这篇文章吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' });
            const deleteRes = await homeArticleApi.deleteArticle(articleId);
            wx.hideLoading();

            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({ title: '删除成功', icon: 'success' });
              // 从列表中移除
              const articles = this.data.articles.filter((item, idx) => idx !== index);
              this.setData({ articles });
            } else {
              wx.showToast({ title: deleteRes.data?.msg || '删除失败', icon: 'none' });
            }
          } catch (err) {
            wx.hideLoading();
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 编辑文章
  goToEdit(e) {
    const { id, index } = e.currentTarget.dataset;
    // 如果ID无效，尝试从列表数据中获取
    let articleId = id;
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      if (index !== undefined && this.data.articles[index]) {
        articleId = this.data.articles[index].id || this.data.articles[index].homeArticleId;
      }
    }
    
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' });
      return;
    }
    
    this.setData({ isBack: true });
    wx.navigateTo({ 
      url: `/pages/article/form/form?id=${articleId}`,
      fail: (err) => {
        wx.showToast({ title: '跳转失败', icon: 'none' });
      }
    });
  }
})