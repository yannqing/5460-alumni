// pages/audit/enterprise/detail/detail.js
const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    enterpriseDetail: null,
    loading: true,
    error: '',
    imageList: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const id = options.id;
    if (id) {
      this.loadEnterpriseDetail(id);
    } else {
      this.setData({ error: '缺少企业ID参数', loading: false });
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    const id = this.options.id;
    if (id) {
      this.loadEnterpriseDetail(id);
    }
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 加载企业详情
   */
  loadEnterpriseDetail(id) {
    this.setData({ loading: true, error: '' });
    
    // 获取 token
    const token = wx.getStorageSync('token') || (wx.getStorageSync('userInfo') || {}).token || '';
    
    // 发送 GET 请求
    wx.request({
      url: `${app.globalData.baseUrl}/alumni-place/management/${id}`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        ...(token ? { token, 'x-token': token } : {})
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const detail = res.data.data;
          // 处理图片列表
          let imageList = [];
          if (detail.images) {
            try {
              imageList = JSON.parse(detail.images);
              if (!Array.isArray(imageList)) {
                imageList = [];
              }
            } catch (e) {
              console.error('解析图片列表失败:', e);
              imageList = [];
            }
          }
          
          this.setData({
            enterpriseDetail: detail,
            imageList: imageList,
            loading: false
          });
        } else {
          console.error('获取企业详情失败:', res.data && res.data.msg || '接口调用失败');
          this.setData({
            error: res.data && res.data.msg || '获取企业详情失败',
            loading: false
          });
        }
      },
      fail: (error) => {
        console.error('获取企业详情异常:', error);
        this.setData({
          error: '网络请求失败，请检查网络连接',
          loading: false
        });
      },
      complete: () => {
        wx.stopPullDownRefresh();
      }
    });
  },

  /**
   * 预览图片
   */
  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    const imageList = this.data.imageList;
    if (imageList.length > 0) {
      wx.previewImage({
        current: imageList[index],
        urls: imageList
      });
    }
  }
})