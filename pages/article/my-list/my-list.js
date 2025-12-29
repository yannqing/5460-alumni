const { homeArticleApi, associationApi } = require('../../../api/api.js')
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
    loading: false,
    activeStatus: -1, // 当前选中的状态：-1-待审核, 1-已通过, 0-已拒绝
    statusTabs: [
      { value: -1, label: '待审核' },
      { value: 1, label: '已通过' },
      { value: 0, label: '已拒绝' }
    ]
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
      const { current, pageSize, activeStatus } = this.data
      const page = reset ? 1 : current + 1
      const params = { current: page, size: pageSize }
      
      const res = await homeArticleApi.getMyArticlePage(params)
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data || {};
        const records = data.records || []
        const total = data.total || 0;
        
        // 根据当前选中的状态筛选文章
        const filteredRecords = records.filter(item => {
          const articleStatus = item.articleStatus !== undefined && item.articleStatus !== null 
            ? item.articleStatus 
            : (item.article_status !== undefined && item.article_status !== null ? item.article_status : -1)
          return articleStatus === activeStatus
        })
        
        const mappedRecords = filteredRecords.map((item, idx) => {
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
          // 如果都没有且是校友会类型，使用 publishWxId 获取校友会头像
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
          } else {
            // 如果头像为空，且发布类型是校友会，且 publishWxId 存在，标记需要异步获取
            // 这里先保存 publishWxId 和 publishType，后续异步获取
            avatar = null; // 保持为 null，后续异步获取
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
            publishWxId: item.publishWxId || item.publish_wx_id || null, // 保存 publishWxId，用于获取校友会头像
            articleType: item.articleType || item.article_type || 1, // 保存文章类型：1-公众号，2-内部路径，3-第三方链接
            articleLink: item.articleLink || item.article_link || '', // 保存文章链接
            time: displayTime,
            isTop: item.isTop === true || item.isTop === 1 || item.top === true,
            needFetchAvatar: !avatar && (publishType === 'association' || publishType === 1) && (item.publishWxId || item.publish_wx_id), // 标记需要获取头像
            articleStatus: item.articleStatus !== undefined && item.articleStatus !== null 
              ? item.articleStatus 
              : (item.article_status !== undefined && item.article_status !== null ? item.article_status : -1) // 保存文章状态
          }
        })

        // 注意：由于前端筛选，需要根据实际筛选后的数据判断是否还有更多
        // 如果后端返回的数据为空，说明没有更多了
        // 如果筛选后的数据少于请求的数据（records.length），说明当前状态的数据可能已经加载完了
        // 但如果筛选后的数据等于 pageSize，可能还有更多数据，需要继续加载
        const hasMoreData = records.length > 0 && (filteredRecords.length === pageSize || records.length === pageSize);
        
        this.setData({
          articles: reset ? mappedRecords : this.data.articles.concat(mappedRecords),
          current: page,
          hasMore: hasMoreData,
          loading: false
        });
        
        // 异步获取缺失的头像（校友会类型）
        this.fetchMissingAvatars(mappedRecords);
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
    let item = null;
    if (index !== undefined && this.data.articles[index]) {
      item = this.data.articles[index];
    if (!articleId || articleId === 'undefined' || articleId === 'null' || articleId === '') {
        articleId = item.id || item.homeArticleId;
      }
    }
    
    if (!item) {
      wx.showToast({ title: '文章数据错误', icon: 'none' });
      return;
    }
    
    const articleType = item.articleType || item.article_type || 1;
    const articleLink = item.articleLink || item.article_link || '';
    
    // 根据文章类型跳转
    if (articleType === 1) {
      // 公众号：直接跳转到公众号文章（使用 web-view）
      if (articleLink) {
        // 检查是否是公众号文章链接
        if (articleLink.includes('mp.weixin.qq.com') || articleLink.startsWith('http')) {
          // 使用 web-view 打开公众号文章
          wx.navigateTo({
            url: `/pages/article/web-view/web-view?url=${encodeURIComponent(articleLink)}`,
            fail: (err) => {
              // 如果跳转失败，尝试使用 openUrl（需要基础库 2.20.2+）
              if (wx.openUrl) {
                wx.openUrl({
                  url: articleLink,
                  fail: () => {
                    // 如果都失败，复制链接
                    wx.setClipboardData({
                      data: articleLink,
                      success: () => {
                        wx.showToast({
                          title: '链接已复制',
                          icon: 'success'
                        });
                      }
                    });
                  }
                });
              } else {
                // 不支持 openUrl，复制链接
                wx.setClipboardData({
                  data: articleLink,
                  success: () => {
                    wx.showToast({
                      title: '链接已复制',
                      icon: 'success'
                    });
                  }
                });
              }
            }
          });
        } else {
          wx.showToast({
            title: '链接格式错误',
            icon: 'none'
          });
        }
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        });
      }
    } else if (articleType === 2) {
      // 内部路径：跳转到小程序内部页面
      if (articleLink) {
        let path = articleLink.startsWith('/') ? articleLink : `/${articleLink}`;
        wx.navigateTo({
          url: path,
          fail: () => {
            wx.switchTab({
              url: path,
              fail: () => {
                wx.showToast({
                  title: '页面不存在',
                  icon: 'none'
                });
              }
            });
          }
        });
      } else {
        wx.showToast({
          title: '路径不存在',
          icon: 'none'
        });
      }
    } else if (articleType === 3) {
      // 第三方链接：复制链接到剪贴板
      if (articleLink) {
        wx.setClipboardData({
          data: articleLink,
          success: () => {
            wx.showToast({
              title: '链接已复制',
              icon: 'success'
            });
          }
        });
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        });
      }
    } else {
      // 未知类型，默认跳转到详情页
      if (articleId && articleId !== 'undefined' && articleId !== 'null' && articleId !== '') {
        this.setData({ isBack: true });
    wx.navigateTo({ 
      url: `/pages/article/detail/detail?id=${articleId}&from=manage`,
      fail: (err) => {
        wx.showToast({ title: '跳转失败', icon: 'none' });
          }
        });
      } else {
        wx.showToast({ title: '文章ID错误', icon: 'none' });
      }
    }
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

  // 异步获取缺失的头像（校友会类型）
  async fetchMissingAvatars(records) {
    if (!records || records.length === 0) return;
    
    // 找出需要获取头像的记录
    const needFetchList = records.filter(item => 
      item.needFetchAvatar && 
      item.publishWxId && 
      (item.publishType === 'association' || item.publishType === 1)
    );
    
    if (needFetchList.length === 0) return;
    
    // 批量获取校友会信息
    const fetchPromises = needFetchList.map(async (item) => {
      try {
        const res = await associationApi.getAssociationDetail(item.publishWxId);
        if (res.data && res.data.code === 200) {
          const association = res.data.data || {};
          let logoUrl = association.logo || association.icon || association.avatar || '';
          if (logoUrl) {
            logoUrl = config.getImageUrl(logoUrl);
            // 更新对应文章的头像
            const articles = this.data.articles.map(article => {
              if (article.id === item.id && article.publishWxId === item.publishWxId) {
                return {
                  ...article,
                  avatar: logoUrl,
                  publisherAvatar: logoUrl,
                  needFetchAvatar: false
                };
              }
              return article;
            });
            this.setData({ articles });
          }
        }
      } catch (err) {
        // 获取失败，静默处理
      }
    });
    
    // 并行获取，不等待所有完成
    Promise.all(fetchPromises).catch(() => {
      // 静默处理错误
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
  },

  // 切换状态标签
  switchStatus(e) {
    const { status } = e.currentTarget.dataset;
    if (status === this.data.activeStatus) return;
    
    this.setData({
      activeStatus: status,
      articles: [],
      current: 1,
      hasMore: true
    });
    
    // 重新加载数据
    this.loadData(true);
  },

  // 获取状态文本
  getStatusText(status) {
    const statusMap = {
      '-1': '待审核',
      '1': '已通过',
      '0': '已拒绝'
    };
    return statusMap[String(status)] || '未知';
  }
})