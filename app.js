// app.js
App({
  globalData: {
    userInfo: null,
    token: '',
    baseUrl: 'https://api.example.com/api/v1'
  },

  onLaunch() {
    // 获取系统信息
    this.getSystemInfo();

    // 检查登录状态
    this.checkLogin();
  },

  getSystemInfo() {
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res;
        this.globalData.statusBarHeight = res.statusBarHeight;
      }
    });
  },

  checkLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      // 这里可以调用接口验证token有效性
    }
  },

  // 设置用户信息
  setUserInfo(userInfo) {
    this.globalData.userInfo = userInfo;
    wx.setStorageSync('userInfo', userInfo);
  },

  // 设置token
  setToken(token) {
    this.globalData.token = token;
    wx.setStorageSync('token', token);
  },

  // 退出登录
  logout() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  }
})
