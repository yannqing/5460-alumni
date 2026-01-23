const app = getApp()
const { get, post } = require('../../../../utils/request.js')

Page({
  data: {
    shops: [], // 店铺列表
    selectedShopIndex: -1, // 选中的店铺索引
    loading: false, // 加载状态
    
    // 活动列表相关数据
    activityList: [], // 活动列表
    current: 1, // 当前页码
    pageSize: 10, // 每页数量
    total: 0, // 总记录数
    sortField: 'createTime', // 排序字段
    sortOrder: 'ascend', // 排序顺序
    activityTitle: '', // 活动标题筛选
    activityLoading: false, // 活动列表加载状态
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
            selectedShopIndex: -1, // 重置选择
            activityList: [], // 重置活动列表
            total: 0, // 重置总数
            current: 1 // 重置页码
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
      selectedShopIndex: e.detail.value,
      activityList: [], // 重置活动列表
      total: 0, // 重置总数
      current: 1 // 重置页码
    });
    this.getActivityList();
  },

  // 获取活动列表
  getActivityList() {
    if (this.data.selectedShopIndex < 0) {
      return;
    }

    const selectedShop = this.data.shops[this.data.selectedShopIndex];
    if (!selectedShop || !selectedShop.shopId) {
      return;
    }

    this.setData({ activityLoading: true });
    
    const params = {
      current: this.data.current,
      pageSize: this.data.pageSize,
      sortField: this.data.sortField,
      sortOrder: this.data.sortOrder,
      shopId: selectedShop.shopId,
      activityTitle: this.data.activityTitle
    };

    // 移除undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });

    post('/activity/shop/list', params)
      .then(res => {
        if (res.data.code === 200) {
          const data = res.data.data || {};
          this.setData({
            activityList: data.records || [],
            total: data.total || 0,
            current: data.current || 1,
            pageSize: data.size || 10
          });
        }
      })
      .catch(err => {
        console.error('获取活动列表失败:', err);
      })
      .finally(() => {
        this.setData({ activityLoading: false });
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
  },

  // 活动标题输入
  onActivityTitleInput(e) {
    this.setData({
      activityTitle: e.detail.value
    });
  },

  // 搜索活动
  onSearchActivity() {
    this.setData({ current: 1 });
    this.getActivityList();
  },

  // 重置搜索条件
  onResetSearch() {
    this.setData({
      activityTitle: '',
      current: 1
    });
    this.getActivityList();
  },

  // 分页事件
  onPageChange(e) {
    this.setData({
      current: e.detail.current
    });
    this.getActivityList();
  },

  // 编辑活动
  onEditActivity(e) {
    const activityId = e.currentTarget.dataset.activityId;
    if (activityId) {
      wx.navigateTo({
        url: `/pages/audit/merchant/topic/edit/edit?activityId=${activityId}`
      });
    }
  }
});