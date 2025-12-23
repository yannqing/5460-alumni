// pages/index/index.js
const { homeArticleApi } = require('../../api/api');
const config = require('../../utils/config.js');
const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 导航图标 - 请替换为实际的云存储图片地址或本地图片
    iconSchool: 'https://img.icons8.com/ios-filled/100/4a90e2/university.png', 
    iconAssociation: 'https://img.icons8.com/ios-filled/100/4a90e2/conference-call.png', 
    iconAlumni: 'https://img.icons8.com/ios-filled/100/4a90e2/student-male.png', 
    iconCircle: 'https://img.icons8.com/ios-filled/100/4a90e2/circled.png', 

    articleList: [],
    loading: false,
    page: 1,
    size: 10,
    hasMore: true,
    refreshing: false,
    
    // 预留字段，防止报错
    recommendedPeople: [], 
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.getArticleList(true);
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    this.setData({ refreshing: true });
    this.getArticleList(true);
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
          }
          
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
          
          return {
            ...item,
            id: id ? String(id) : '', // 确保ID为字符串，如果为空则保持空字符串
            title: title,
            description: description,
            username: username,
            avatar: avatar,
            cover: cover,
            time: time
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
    } finally {
      if (reset) {
        wx.stopPullDownRefresh();
      }
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
   * 跳转详情
   */
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    const index = e.currentTarget.dataset.index;
    const item = index !== undefined ? this.data.articleList[index] : null;
    
    console.log('[Index] 点击文章，ID:', id, '索引:', index, '完整数据:', item);
    
    if (!id || id === 'undefined' || id === 'null' || id === '') {
      console.warn('[Index] 文章ID无效，无法跳转，ID值:', id);
      wx.showToast({
        title: '文章ID错误，无法打开',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/article/detail/detail?id=${id}`,
      success: () => {
        console.log('[Index] 跳转成功，文章ID:', id);
      },
      fail: (err) => {
        console.error('[Index] 跳转失败:', err);
        wx.showToast({
          title: err.errMsg || '跳转失败',
          icon: 'none',
          duration: 2000
        });
      }
    });
  }
});
