// pages/article/web-view/web-view.js
Page({
  data: {
    url: ''
  },

  onLoad(options) {
    if (options.url) {
      const url = decodeURIComponent(options.url);
      this.setData({
        url: url
      });
    } else {
      wx.showToast({
        title: '链接不存在',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  }
})

