const app = getApp()
const { get } = require('../../../../utils/request.js')

Page({
  data: {
    shops: [], // 店铺列表
    selectedShopIndex: -1, // 选中的店铺索引
    loading: false, // 加载状态
  },

  onLoad(options) {
    this.getShops();
  },

  // 获取店铺列表
  getShops() {
    this.setData({ loading: true });
    get('/shop/my/available')
      .then(res => {
        if (res.data.code === 200) {
          this.setData({
            shops: res.data.data || [],
            selectedShopIndex: -1 // 重置选择
          });
        }
      })
      .catch(err => {
        console.error('获取店铺列表失败:', err);
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  // 选择店铺
  onShopChange(e) {
    this.setData({
      selectedShopIndex: e.detail.value
    });
  },

  // 跳转到发布话题页面
  onNavigateToPublish() {
    if (this.data.selectedShopIndex < 0) {
      wx.showToast({
        title: '请先选择店铺',
        icon: 'none'
      });
      return;
    }

    const selectedShop = this.data.shops[this.data.selectedShopIndex];
    
    // 跳转到新的发布话题页面，并传递shopId和shopName
    wx.navigateTo({
      url: `/pages/audit/merchant/topic/publish/publish?shopId=${selectedShop.shopId}&shopName=${selectedShop.shopName}`
    });
  }
});