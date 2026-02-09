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
    currentBannerIndex: 0,
    // 轮播图 translateY 值
    bannerTranslateY: 0,
    // 文章列表 scroll-view 高度
    articleScrollHeight: 0,
    // 导航菜单是否固定
    navFixed: false,
    // 当前页面滚动位置
    _scrollTop: 0,
    // 触摸事件相关
    _touchStartY: 0,
    _touchCurrentY: 0
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.getBannerList();
    this.getArticleList(true);
    // 添加滚动事件监听
    wx.pageScrollTo({ scrollTop: 0, duration: 0 });
    // 计算 scroll-view 高度
    this.calculateScrollViewHeight();
  },

  /**
   * 计算 scroll-view 高度
   */
  calculateScrollViewHeight: function () {
    try {
      const systemInfo = wx.getSystemInfoSync();
      const screenHeight = systemInfo.windowHeight;
      // 计算其他元素的高度（轮播图 + 导航菜单）
      // 轮播图高度：450rpx 转换为 px
      const bannerHeight = 450 / 2;
      // 导航菜单高度：考虑负边距和内边距
      const navHeight = 200;
      // 计算 scroll-view 可用高度
      const scrollViewHeight = screenHeight - (bannerHeight + navHeight);
      this.setData({
        articleScrollHeight: Math.max(scrollViewHeight, 300) // 确保最小高度为 300px
      });
    } catch (error) {
      console.error('计算 scroll-view 高度失败:', error);
      this.setData({
        articleScrollHeight: 500 // 默认高度
      });
    }
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
  },

  /**
   * 页面滚动事件处理函数
   */
  onPageScroll: function (e) {
    const scrollTop = e.scrollTop;
    // 保存当前滚动位置
    this.setData({
      _scrollTop: scrollTop
    });
    
    // 实现导航菜单的固定效果
    const navFixed = scrollTop > 150;
    
    // 计算轮播图的 translateY 值
    // 核心思路：轮播图和导航菜单应该保持相对静止
    // 当导航菜单固定时，轮播图的位置需要相应调整
    
    // 导航菜单原始 margin-top 是 -120rpx（约 -60px）
    const navMarginTop = -60; // 导航菜单原始 margin-top（-120rpx 转换为 px）
    
    // 计算轮播图位置
    // 无论导航菜单是否固定，轮播图都应该与导航菜单保持相对静止
    // 轮播图的位置 = -scrollTop + (导航菜单固定时的位置补偿)
    let bannerTranslateY;
    
    if (navFixed) {
      // 导航菜单固定时
      // 导航菜单固定后，它的顶部位置变为 0
      // 为了保持轮播图和导航菜单的相对位置不变
      // 轮播图需要向上移动 navMarginTop 的距离
      bannerTranslateY = Math.max(scrollTop * -1 + navMarginTop, -240); // 最大移动距离调整为 -240px
    } else {
      // 导航菜单未固定时
      // 轮播图正常跟随页面滚动
      bannerTranslateY = Math.max(scrollTop * -1, -180); // 最大移动距离保持 -180px
    }
    
    // 更新轮播图位置
    this.setData({
      bannerTranslateY: bannerTranslateY
    });
    
    if (navFixed !== this.data.navFixed) {
      this.setData({
        navFixed: navFixed
      });
    }
  },

  /**
   * 页面下拉刷新事件处理函数
   */
  onPullDownRefresh: function () {
    console.log('[Index] 下拉刷新触发');
    this.setData({ refreshing: true });
    this.getArticleList(true).finally(() => {
      wx.stopPullDownRefresh();
      this.setData({ refreshing: false });
    });
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
    // 重新计算 scroll-view 高度，确保在不同设备上都能正确显示
    this.calculateScrollViewHeight();
  },

  /**
   * 列表下拉刷新处理函数
   */
  onListRefresh: function () {
    console.log('[Index] 列表下拉刷新触发')
    this.setData({ refreshing: true });
    this.getArticleList(true);
  },

  /**
   * scroll-view 触摸开始事件处理函数
   */
  onScrollViewTouchStart: function (e) {
    this.setData({
      _touchStartY: e.touches[0].pageY
    });
  },

  /**
   * scroll-view 触摸移动事件处理函数
   * 确保先实现校友功能卡片的滑动极限状态，再进行列表的局部滑动
   */
  onScrollViewTouchMove: function (e) {
    const currentY = e.touches[0].pageY;
    const deltaY = currentY - this.data._touchStartY;
    
    // 无论什么位置滑动，都先检查校友功能卡片的状态
    // 1. 向上滑动（手指向下移动，deltaY > 0）：
    //    - 首先让校友功能卡片达到固定状态（极限状态）
    //    - 只有当校友功能卡片完全固定后，才允许列表向上滚动
    if (deltaY > 0) {
      // 如果导航区域还没有固定，说明校友功能卡片还未达到极限状态
      // 阻止 scroll-view 的滚动，让页面级滚动先处理校友功能卡片的固定
      if (!this.data.navFixed) {
        return false;
      }
    }
    
    // 2. 向下滑动（手指向上移动，deltaY < 0）：
    //    - 首先让校友功能卡片回到初始状态（解除固定）
    //    - 只有当校友功能卡片完全回到初始状态后，才允许列表向下滚动
    if (deltaY < 0) {
      // 如果导航区域已经固定，说明校友功能卡片还未回到初始状态
      // 阻止 scroll-view 的滚动，让页面级滚动先处理校友功能卡片的解除固定
      if (this.data.navFixed) {
        return false;
      }
    }
  },

  /**
   * 触摸开始事件处理函数
   */
  onTouchStart: function (e) {
    this.setData({
      _touchStartY: e.touches[0].pageY
    });
  },

  /**
   * 触摸结束事件处理函数
   */
  onTouchEnd: function () {
    this.setData({
      _touchStartY: 0,
      _touchCurrentY: 0
    });
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
    if (this.data.loading && !reset) return;

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

          // 处理头像：使用 publisherAvatar 字段
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
            // 处理头像 URL
            if (avatar) {
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
              // 如果是对象，使用 thumbnailUrl 或 fileUrl
              cover = item.coverImg.thumbnailUrl || item.coverImg.fileUrl || '';
              if (cover) {
                cover = config.getImageUrl(cover);
              }
            } else {
              // 如果是ID（数字或字符串），构造下载URL
              cover = config.getImageUrl(`/file/download/${item.coverImg}`);
            }
          }

          // 处理时间：格式化时间，去掉T
          let time = '';
          if (item.createTime) {
            time = item.createTime.replace('T', ' ');
          } else if (item.publishTime) {
            time = item.publishTime.replace('T', ' ');
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
                  childCover = child.coverImg.thumbnailUrl || child.coverImg.fileUrl || '';
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
            publishWxId: item.publishWxId || item.publish_wx_id || null, // 保存 publishWxId，用于获取校友会头像
            articleType: item.articleType || item.article_type || 1, // 保存文章类型：1-公众号，2-内部路径，3-第三方链接
            articleLink: item.articleLink || item.article_link || '', // 保存文章链接
            needFetchAvatar: !avatar && (publishType === 'association' || publishType === 1) && (item.publishWxId || item.publish_wx_id), // 标记需要获取头像
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
          hasMore: currentTotal < total && newList.length > 0, // 如果返回的数据为空，说明没有更多了
          loading: false,
          refreshing: false
        });

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
   * 异步获取缺失的头像（校友会类型）
   */
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
        let path = articleLink.startsWith('/') ? articleLink : `/${articleLink}`
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
          wx.showToast({
            title: '链接格式错误',
            icon: 'none'
          })
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
              // 如果是对象，提取 fileId 或 fileUrl
              const fileId = item.bannerImage.fileId;
              if (fileId) {
                imageUrl = config.getImageUrl(`/file/download/${fileId}`);
              } else {
                imageUrl = item.bannerImage.fileUrl || item.bannerImage.thumbnailUrl || item.bannerImage.url || '';
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
