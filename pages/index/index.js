// pages/index/index.js
const { homeArticleApi } = require('../../api/api');
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
      // 调用 API
      const res = await homeArticleApi.getPage({
        page: reset ? 1 : page,
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
        
        // 简单处理数据（如时间格式化、默认头像等）
        const newList = records.map(item => ({
          ...item,
          // 如果没有头像，使用默认头像
          avatar: item.avatar || '/assets/icons/own.png', 
          // 格式化时间，取日期部分
          time: item.createTime ? item.createTime.split(' ')[0] : '',
        }));

        this.setData({
          articleList: reset ? newList : this.data.articleList.concat(newList),
          page: (reset ? 1 : page) + 1,
          hasMore: (reset ? newList.length : this.data.articleList.length + newList.length) < total,
          loading: false,
          refreshing: false
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
    if (id) {
      wx.navigateTo({
        url: `/pages/article/detail/detail?id=${id}`,
      });
    }
  }
});
