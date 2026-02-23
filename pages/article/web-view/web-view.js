// pages/article/web-view/web-view.js
Page({
  data: {
    url: '',
    title: '',
    loading: true
  },

  onLoad(options) {
    if (options.url) {
      const url = decodeURIComponent(options.url);
      const title = options.title ? decodeURIComponent(options.title) : '';
      
      // 处理URL，添加来源标识
      const processedUrl = this.processUrl(url);
      
      this.setData({
        url: processedUrl,
        title: title || '文章详情'
      });
      
      // 设置导航栏标题
      if (title) {
        wx.setNavigationBarTitle({
          title: title
        });
      }
    } else {
      wx.showToast({
        title: '链接不存在',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  /**
   * 处理URL，添加来源标识
   */
  processUrl(url) {
    if (!url) {return url;}
    
    // 如果是公众号文章链接，确保参数正确
    if (url.includes('mp.weixin.qq.com')) {
      // 添加来源标识
      const separator = url.includes('?') ? '&' : '?';
      return `${url}${separator}from=miniprogram`;
    }
    return url;
  },

  /**
   * web-view加载成功
   */
  onWebViewLoad() {
    this.setData({ loading: false });
  },

  /**
   * web-view加载错误
   */
  onWebViewError(e) {
    console.error('[WebView] 加载失败', e);
    this.setData({ loading: false });
    wx.showToast({
      title: '加载失败，请稍后重试',
      icon: 'none',
      duration: 2000
    });
  }
})


