// pages/index/index.js
const { homeArticleApi, associationApi, bannerApi } = require('../../api/api');
const config = require('../../utils/config.js');
const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 顶部 5460 标志图
    icon5460: config.getIconUrl('5460@3x.png'),
    // 导航图标（使用 config.getIconUrl 拼接服务器图标路径）
    iconSchool: config.getIconUrl('school@3x.png'),
    iconAssociation: config.getIconUrl('alumni_association@3x.png'),
    iconAlumni: config.getIconUrl('alumni@3x.png'),
    iconCircle: config.getIconUrl('circle@3x.png'),
    iconActivity: config.getIconUrl('city@3x.png'),

    articleList: [],
    loading: false,
    page: 1,
    size: 10,
    hasMore: true,
    refreshing: false,

    // 预留字段，防止报错
    recommendedPeople: [],

    // 消息通知相关
    showMessageNotification: false,
    messageNotificationData: {
      senderName: '',
      senderAvatar: '',
      messageContent: ''
    },
    // 轮播图列表
    bannerList: [],
    // 轮播图 translateY 值 (已由 sticky 替代)
    bannerTranslateY: 0,
    // 导航菜单是否固定
    navFixed: false,
    // 状态栏高度
    statusBarHeight: 20,
    // 下拉刷新是否启用
    refresherEnabled: false,
    // 手动刷新高度
    refresherHeight: 0,
    // 当前滚动位置
    scrollTop: 0
  },



  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const systemInfo = wx.getSystemInfoSync();
    const rpxRatio = systemInfo.windowWidth / 750;

    // 获取胶囊按钮位置，用于精准对齐
    const menuButtonInfo = wx.getMenuButtonBoundingClientRect();
    const navBarHeight = (menuButtonInfo.top - systemInfo.statusBarHeight) * 2 + menuButtonInfo.height;

    const statusBarHeight = systemInfo.statusBarHeight || 20;
    const navStickyTop = statusBarHeight + (navBarHeight || 44);

    // 关键计算：
    // 轮播图高度 450rpx，导航栏向上偏移 120rpx
    // 导航栏在 Header 组合内的相对起始位置 = 450rpx - 120rpx = 330rpx
    const bannerHeightPx = 450 * rpxRatio;
    const navOverlapPx = 120 * rpxRatio;
    const navTopInGroupPx = bannerHeightPx - navOverlapPx;

    // 当导航栏到达 navStickyTop 时，Header 组合的 top 值应该是：
    // stickyGroupTop = navStickyTop - navTopInGroupPx
    const stickyGroupTop = navStickyTop - navTopInGroupPx;

    this.setData({
      statusBarHeight: statusBarHeight,
      navBarHeight: navBarHeight || 44,
      navStickyTop: navStickyTop,
      stickyGroupTop: stickyGroupTop,
      // 触发状态切换的滚动距离：就是 Header 组合从初始位置（0）滚动到 stickyGroupTop 位置的距离
      // 初始时 HeaderTop=0，我们要让它停在 stickyGroupTop。
      // 因为 sticky 是相对于视口的，当容器 top < stickyGroupTop 时，它会停在 stickyGroupTop。
      // 所以滚动距离阈值 = -stickyGroupTop
      scrollThreshold: -stickyGroupTop,
      rpxRatio: rpxRatio
    });

    this.getBannerList();
    this.getArticleList(true);
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
    if (this._observer) {
      this._observer.disconnect();
    }
  },

  /**
   * 滚动事件处理函数 (由 scroll-view 触发)
   */
  onScroll: function (e) {
    const scrollTop = e.detail.scrollTop;
    this.setData({ scrollTop: scrollTop }); // 关键：更新当前滚动高度
    const threshold = this.data.scrollThreshold || 77;

    // 增加逻辑判断
    if (this.data.navFixed) {
      if (scrollTop < threshold) {
        this.setData({
          navFixed: false
        });
      }
    } else {
      if (scrollTop >= threshold) {
        this.setData({
          navFixed: true
        });
      }
    }
  },

  /**
   * 触摸开始事件
   */
  onTouchStart: function (e) {
    this.touchStartX = e.touches[0].clientX;
    this.touchStartY = e.touches[0].clientY;
    this.isPullDown = false;
    this.canPullDown = false; // 重置标记

    // 获取文章列表的起始位置，判断触摸起点是否在文章列表区域
    const query = wx.createSelectorQuery();
    query.select('#article-list-start').boundingClientRect((rect) => {
      if (rect) {
        // 只有触摸点在列表开始位置下方，且页面处于顶部（scrollTop很小）时，才允许下拉
        // 这里用 10 作为容错
        this.canPullDown = this.touchStartY > rect.top && this.data.scrollTop <= 10;
        console.log('[Index] TouchStart rect.top:', rect.top, 'touchStartY:', this.touchStartY, 'scrollTop:', this.data.scrollTop, 'canPullDown:', this.canPullDown);
      }
    }).exec();
  },

  /**
   * 触摸移动事件
   */
  onTouchMove: function (e) {
    if (!this.canPullDown || this.data.refreshing) return;

    const touchY = e.touches[0].clientY;
    const moveY = touchY - this.touchStartY;

    if (moveY > 0) {
      // 下拉阻尼感
      const height = Math.min(80, moveY * 0.4);
      this.setData({
        refresherHeight: height
      });
      this.isPullDown = true;
    }
  },

  /**
   * 触摸结束事件
   */
  onTouchEnd: function (e) {
    if (!this.isPullDown) {
      this.canPullDown = false;
      return;
    }

    if (this.data.refresherHeight >= 45) {
      // 达到触发阈值，执行刷新
      this.setData({
        refresherHeight: 50,
        refreshing: true
      });
      this.onPullDownRefreshInternal();
    } else {
      // 未达到阈值，回弹
      this.setData({
        refresherHeight: 0
      });
    }
    this.isPullDown = false;
    this.canPullDown = false;
  },

  /**
   * 内部触发下拉刷新
   */
  async onPullDownRefreshInternal() {
    console.log('[Index] 手动下拉刷新开始');
    try {
      await Promise.all([
        this.getBannerList(),
        this.getArticleList(true)
      ]);
    } catch (err) {
      console.error('[Index] 刷新异常:', err);
    } finally {
      // 延迟关闭，让动画显示完整
      setTimeout(() => {
        this.setData({
          refreshing: false,
          refresherHeight: 0
        });
      }, 500);
    }
  },

  /**
   * 页面下拉刷新重定向
   */
  onPullDownRefresh: function () {
    this.onPullDownRefreshInternal();
  },


  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      });
      // 更新未读消息数
      this.getTabBar().updateUnreadCount();
    }
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    if (this.data.hasMore && !this.data.loading) {
      this.getArticleList(false);
    }
  },

  /**
   * 获取首页文章列表
   */
  async getArticleList(reset = false) {
    if (this.data.loading && !reset) { return; }

    this.setData({ loading: true });

    if (reset) {
      this.setData({ page: 1, hasMore: true });
    }

    try {
      const { page, size } = this.data;
      // 调用 API - 使用 current 作为分页参数（与后端保持一致）
      const currentPage = reset ? 1 : page;
      const res = await homeArticleApi.getPage({
        current: currentPage,
        size: size
      });

      // 根据实际接口返回结构处理
      // 假设 res.data 是后端返回的 Result 对象
      const result = res.data || res;

      if (result.code === 200) {
        // 假设分页数据在 result.data.records 或 result.data.list 中
        // 根据后端习惯调整
        const data = result.data || {};
        const records = data.records || data.list || [];
        const total = data.total || 0;

        // 处理数据，映射字段名
        const newList = records.map(item => {
          // 处理标题：优先使用 articleTitle，否则使用 title
          const title = item.articleTitle || item.title || '无标题';

          // 处理描述/内容：优先使用 description，否则使用 content
          const description = item.description || item.content || '';

          // 处理作者名：使用 publishUsername 字段
          const username = item.publishUsername || '官方发布';

          // 处理头像：直接使用 publisherAvatar 字段
          // 如果都没有且是校友会类型，使用 publishWxId 获取校友会头像
          let avatar = '';
          if (item.publisherAvatar) {
            // 直接使用 publisherAvatar 字段
            avatar = item.publisherAvatar;
            // 处理头像 URL
            if (avatar) {
              // 清理字符串中的额外空格和反引号
              if (typeof avatar === 'string') {
                avatar = avatar.trim().replace(/`/g, '');
              }
              avatar = config.getImageUrl(avatar);
            }
          } else {
            // 如果头像为空，且发布类型是校友会，且 publishWxId 存在，标记需要异步获取
            avatar = null; // 保持为 null，后续异步获取
          }

          // 处理发布类型
          const publishType = item.publishType || item.publish_type || null;

          // 处理封面图：使用 coverImg 字段
          let cover = '';
          if (item.coverImg) {
            if (typeof item.coverImg === 'object') {
              // 优先使用 fileUrl 作为图片路径直接访问
              if (item.coverImg.fileUrl) {
                cover = item.coverImg.fileUrl;
              }
              // 如果 fileUrl 不存在，使用 filePath
              else if (item.coverImg.filePath) {
                cover = item.coverImg.filePath;
              }
              // 兼容其他情况
              else {
                cover = item.coverImg.thumbnailUrl || '';
              }
              if (cover) {
                cover = config.getImageUrl(cover);
              }
            } else {
              // 如果是ID（数字或字符串），构造下载URL
              cover = config.getImageUrl(`/file/download/${item.coverImg}`);
            }
          }

          // 处理时间：格式化时间为 MM-DD HH:MM
          let time = '';
          if (item.createTime) {
            time = item.createTime.replace('T', ' ').substring(5, 16);
          } else if (item.publishTime) {
            time = item.publishTime.replace('T', ' ').substring(5, 16);
          }

          // 处理ID：确保ID存在且有效
          const id = item.id || item.homeArticleId || item.homeArticleId || '';

          // 如果ID为空，记录警告但继续处理（可能后端数据有问题）
          if (!id) {
            console.warn('[Index] 文章ID为空，数据:', item);
          }

          // 处理子文章
          let children = [];
          let hasChildren = false;
          if (item.children && Array.isArray(item.children) && item.children.length > 0) {
            hasChildren = true;
            children = item.children.map(child => {
              // 处理子文章的封面图
              let childCover = '';
              if (child.coverImg) {
                if (typeof child.coverImg === 'object') {
                  // 优先使用 fileUrl 作为图片路径直接访问
                  if (child.coverImg.fileUrl) {
                    childCover = child.coverImg.fileUrl;
                  }
                  // 如果 fileUrl 不存在，使用 filePath
                  else if (child.coverImg.filePath) {
                    childCover = child.coverImg.filePath;
                  }
                  // 兼容其他情况
                  else {
                    childCover = child.coverImg.thumbnailUrl || '';
                  }
                  if (childCover) {
                    childCover = config.getImageUrl(childCover);
                  }
                } else {
                  childCover = config.getImageUrl(`/file/download/${child.coverImg}`);
                }
              }

              return {
                id: child.id || child.homeArticleId || '',
                title: child.articleTitle || child.title || '无标题',
                cover: childCover,
                articleType: child.articleType || child.article_type || 1,
                articleLink: child.articleLink || child.article_link || ''
              };
            });
          }

          return {
            ...item,
            id: id ? String(id) : '', // 确保ID为字符串，如果为空则保持空字符串
            title: title,
            description: description,
            username: username,
            avatar: avatar,
            cover: cover,
            time: time,
            publishType: publishType, // 保存 publishType 字段
            publishWxId: item.publishWxId || item.publish_wx_id || null, // 优先使用后端返回的组织 ID
            articleType: item.articleType || item.article_type || 1, // 保存文章类型：1-公众号，2-内部路径，3-第三方链接
            articleLink: item.articleLink || item.article_link || '', // 保存文章链接
            needFetchAvatar: !avatar && (publishType === 'association' || publishType === 'ASSOCIATION' || publishType === 1) && (item.publishWxId || item.publish_wx_id), // 标记需要获取头像
            hasChildren: hasChildren, // 是否有子文章
            children: children // 子文章列表
          };
        });

        // 更新分页状态
        const nextPage = currentPage + 1;
        const currentTotal = reset ? newList.length : this.data.articleList.length + newList.length;

        this.setData({
          articleList: reset ? newList : this.data.articleList.concat(newList),
          page: nextPage,
          hasMore: newList.length >= size, // 只要返回的数据达到一页数量，就认为可能有下一页
          loading: false,
          refreshing: false
        });

        // 如果不是重置加载，且返回结果少于一页大小，说明已经到末尾了
        if (!reset && newList.length < size) {
          wx.showToast({
            title: '没有数据啦～',
            icon: 'none'
          });
        }

        // 异步获取缺失的头像（校友会类型）
        this.fetchMissingAvatars(newList);

        console.log('[Index] 加载文章列表成功:', {
          currentPage,
          total,
          loaded: currentTotal,
          hasMore: currentTotal < total && newList.length > 0
        });
      } else {
        console.warn('获取文章列表非200:', result);
        this.setData({ loading: false, refreshing: false });
      }
    } catch (error) {
      console.error('获取首页文章失败', error);
      this.setData({ loading: false, refreshing: false });
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  /**
   * 页面导航
   */
  navTo(e) {
    const disabled = e.currentTarget.dataset.disabled;
    if (disabled) {
      return;
    }
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({
        url: url,
        fail: () => {
          // 如果是 tabBar 页面，尝试 switchTab
          wx.switchTab({
            url: url,
            fail: () => {
              wx.showToast({
                title: '功能开发中',
                icon: 'none'
              });
            }
          });
        }
      });
    }
  },

  /**
   * 点击发布者（校友会/校促会）跳转详情
   */
  onPublisherTap(e) {
    const { index } = e.currentTarget.dataset;
    const item = this.data.articleList[index];

    if (!item || !item.publishWxId) {
      console.warn('[Index] 发布者 ID 为空，无法跳转', { item });
      return;
    }

    const { publishType, publishWxId } = item;
    let url = '';

    // publishType 处理逻辑（支持大写、小写及数字）
    const typeStr = String(publishType || '').toUpperCase();

    if (typeStr === 'ASSOCIATION' || publishType === 1) {
      url = `/pages/alumni-association/detail/detail?id=${publishWxId}`;
    } else if (typeStr === 'LOCAL_PLATFORM' || publishType === 2) {
      url = `/pages/local-platform/detail/detail?id=${publishWxId}`;
    } else if (typeStr === 'ALUMNI' || typeStr === 'USER') {
      url = `/pages/alumni/detail/detail?id=${publishWxId}`;
    }

    if (url) {
      wx.navigateTo({ url });
    } else {
      console.log('[Index] 未知的发布者类型或 ID 为空:', { publishType, publishWxId });
    }
  },

  /**
   * 异步获取缺失的头像（校友会类型）
   */
  async fetchMissingAvatars(records) {
    if (!records || records.length === 0) { return; }

    // 找出需要获取头像的记录
    const needFetchList = records.filter(item =>
      item.needFetchAvatar &&
      item.publishWxId &&
      (item.publishType === 'association' || item.publishType === 'ASSOCIATION' || item.publishType === 1)
    );

    if (needFetchList.length === 0) { return; }

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
            const articleList = this.data.articleList.map(article => {
              if (article.id === item.id && article.publishWxId === item.publishWxId) {
                return {
                  ...article,
                  avatar: logoUrl,
                  needFetchAvatar: false
                };
              }
              return article;
            });
            this.setData({ articleList });
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

  /**
   * 跳转详情或链接
   * 按照方案A：使用web-view作为中间页（推荐方案）
   */
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    const index = e.currentTarget.dataset.index;
    const item = index !== undefined ? this.data.articleList[index] : null;

    if (!item) {
      wx.showToast({
        title: '文章数据错误',
        icon: 'none'
      });
      return;
    }

    const articleType = item.articleType || item.article_type || 1;
    const articleLink = item.articleLink || item.article_link || '';
    const articleTitle = item.title || item.articleTitle || '文章详情';

    // 根据文章类型跳转
    if (articleType === 1) {
      // 公众号：使用微信官方API打开公众号文章
      if (articleLink) {
        wx.openOfficialAccountArticle({
          url: articleLink,
          success(res) {
            console.log('[Index] 打开公众号文章成功');
          },
          fail: (err) => {
            console.error('[Index] 打开公众号文章失败:', err);
            wx.showToast({
              title: '打开文章失败',
              icon: 'none'
            });
          }
        });
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        });
      }
    } else if (articleType === 2) {
      // 内部路径：跳转到小程序内部页面
      if (articleLink) {
        const path = articleLink.startsWith('/') ? articleLink : `/${articleLink}`;
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
      // 第三方链接：使用 web-view 打开
      if (articleLink) {
        if (articleLink.startsWith('http://') || articleLink.startsWith('https://')) {
          wx.navigateTo({
            url: `/pages/article/web-view/web-view?url=${encodeURIComponent(articleLink)}&title=${encodeURIComponent(articleTitle)}`,
            fail: (err) => {
              console.error('[Index] 跳转web-view失败:', err);
              wx.showToast({
                title: '跳转失败，请稍后重试',
                icon: 'none'
              });
            }
          });
        } else {
          // 链接格式错误，不提示，直接无反应
        }
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        });
      }
    } else {
      // 未知类型，默认跳转到详情页
      if (id && id !== 'undefined' && id !== 'null' && id !== '') {
        wx.navigateTo({
          url: `/pages/article/detail/detail?id=${id}`,
          fail: (err) => {
            wx.showToast({
              title: '跳转失败',
              icon: 'none'
            });
          }
        });
      } else {
        wx.showToast({
          title: '文章ID错误',
          icon: 'none'
        });
      }
    }
  },

  /**
   * 点击子文章跳转
   */
  goToChildDetail(e) {
    const { item } = e.currentTarget.dataset

    if (!item) {
      wx.showToast({
        title: '文章数据错误',
        icon: 'none'
      })
      return
    }

    const articleType = item.articleType || 1
    const articleLink = item.articleLink || ''
    const articleTitle = item.title || '文章详情'

    // 根据文章类型跳转
    if (articleType === 1) {
      // 公众号：使用微信官方API打开公众号文章
      if (articleLink) {
        wx.openOfficialAccountArticle({
          url: articleLink,
          success(res) {
            console.log('[Index] 打开公众号文章成功')
          },
          fail: (err) => {
            console.error('[Index] 打开公众号文章失败:', err)
            wx.showToast({
              title: '打开文章失败',
              icon: 'none'
            })
          }
        })
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        })
      }
    } else if (articleType === 2) {
      // 内部路径：跳转到小程序内部页面
      if (articleLink) {
        const path = articleLink.startsWith('/') ? articleLink : `/${articleLink}`
        wx.navigateTo({
          url: path,
          fail: () => {
            wx.switchTab({
              url: path,
              fail: () => {
                wx.showToast({
                  title: '页面不存在',
                  icon: 'none'
                })
              }
            })
          }
        })
      } else {
        wx.showToast({
          title: '路径不存在',
          icon: 'none'
        })
      }
    } else if (articleType === 3) {
      // 第三方链接：使用 web-view 打开
      if (articleLink) {
        if (articleLink.startsWith('http://') || articleLink.startsWith('https://')) {
          wx.navigateTo({
            url: `/pages/article/web-view/web-view?url=${encodeURIComponent(articleLink)}&title=${encodeURIComponent(articleTitle)}`,
            fail: (err) => {
              console.error('[Index] 跳转web-view失败:', err)
              wx.showToast({
                title: '跳转失败，请稍后重试',
                icon: 'none'
              })
            }
          })
        } else {
          // 链接格式错误，不提示，直接无反应
        }
      } else {
        wx.showToast({
          title: '链接不存在',
          icon: 'none'
        })
      }
    } else {
      // 未知类型，默认跳转到详情页
      const id = item.id
      if (id && id !== 'undefined' && id !== 'null' && id !== '') {
        wx.navigateTo({
          url: `/pages/article/detail/detail?id=${id}`,
          fail: (err) => {
            wx.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
      } else {
        wx.showToast({
          title: '文章ID错误',
          icon: 'none'
        })
      }
    }
  },

  /**
   * 获取轮播图列表
   */
  async getBannerList() {
    try {
      const res = await bannerApi.getBannerList();

      const result = res.data || res;

      if (result.code === 200) {
        // 新接口可能直接返回数组，或者包装在 data 中
        const records = Array.isArray(result.data) ? result.data : (result.data?.records || result.data?.list || []);

        // 处理轮播图数据，获取图片URL
        const bannerList = records.map(item => {
          let imageUrl = '';

          // 优先处理 bannerImage 字段（后端返回的字段名）
          if (item.bannerImage) {
            if (typeof item.bannerImage === 'object') {
              // 优先使用 fileUrl 作为图片路径直接访问
              if (item.bannerImage.fileUrl) {
                imageUrl = config.getImageUrl(item.bannerImage.fileUrl);
              }
              // 如果 fileUrl 不存在，使用 baseUrl + filePath
              else if (item.bannerImage.filePath) {
                imageUrl = config.getImageUrl(item.bannerImage.filePath);
              }
              // 兼容其他情况
              else if (item.bannerImage.fileId) {
                imageUrl = config.getImageUrl(`/file/download/${item.bannerImage.fileId}`);
              } else {
                imageUrl = item.bannerImage.thumbnailUrl || item.bannerImage.url || '';
                if (imageUrl) {
                  imageUrl = config.getImageUrl(imageUrl);
                }
              }
            } else {
              // 如果是字符串或数字，当作 fileId 处理
              imageUrl = config.getImageUrl(`/file/download/${item.bannerImage}`);
            }
          } else if (item.imageUrl) {
            // 兼容 imageUrl 字段
            if (typeof item.imageUrl === 'object') {
              imageUrl = item.imageUrl.fileUrl || item.imageUrl.thumbnailUrl || item.imageUrl.url || '';
            } else {
              imageUrl = item.imageUrl;
            }
            if (imageUrl) {
              imageUrl = config.getImageUrl(imageUrl);
            }
          } else if (item.imageId) {
            // 兼容 imageId 字段
            imageUrl = config.getImageUrl(`/file/download/${item.imageId}`);
          }

          return {
            ...item,
            imageUrl: imageUrl
          };
        });

        this.setData({
          bannerList: bannerList
        });
      }
    } catch (error) {
      console.error('获取轮播图列表失败', error);
    }
  },

  /**
   * 轮播图切换事件
   */
  onBannerChange(e) {
    this.setData({
      currentBannerIndex: e.detail.current
    });
  }
});
