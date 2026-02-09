const app = getApp()
const { get, post, del } = require('../../../../utils/request.js')

Page({
  data: {
    shops: [],
    selectedShopIndex: -1,
    loading: false,
    activityList: [],
    current: 1,
    pageSize: 10,
    total: 0,
    sortField: 'createTime',
    sortOrder: 'ascend',
    activityTitle: '',
    activityLoading: false,
    scrollListHeight: 400
  },

  onLoad(options) {
    this.setScrollListHeight()
    this.getShops()
  },

  setScrollListHeight() {
    try {
      const res = wx.getSystemInfoSync()
      const navRpx = 190.22
      const navPx = (res.windowWidth * navRpx) / 750
      const contentH = res.windowHeight - navPx
      const scrollH = Math.floor(contentH * 0.5)
      this.setData({ scrollListHeight: scrollH > 200 ? scrollH : 400 })
    } catch (e) {
      this.setData({ scrollListHeight: 400 })
    }
  },

  // 获取店铺列表
  getShops() {
    this.setData({ loading: true });
    get('/shop/my/available')
      .then(res => {
        if (res.data.code === 200) {
          const shops = res.data.data || [];
          let selectedShopIndex = -1;
          
          // 如果只有一个店铺，自动选择
          if (shops.length === 1) {
            selectedShopIndex = 0;
          }
          
          this.setData({
            shops: shops,
            selectedShopIndex: selectedShopIndex,
            activityList: [], // 重置活动列表
            total: 0, // 重置总数
            current: 1 // 重置页码
          });
          
          // 如果自动选择了店铺，获取活动列表
          if (selectedShopIndex >= 0) {
            this.getActivityList();
          }
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
  },

  // 删除活动
  onDeleteActivity(e) {
    const activityId = e.currentTarget.dataset.activityId;
    if (!activityId) {
      return;
    }

    // 弹出确认对话框
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个活动吗？',
      success: (res) => {
        if (res.confirm) {
          // 用户确认删除，调用API
          this.setData({ activityLoading: true });
          
          del(`/activity/${activityId}`)
            .then(res => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '删除成功',
                  icon: 'success'
                });
                // 重新获取活动列表
                this.getActivityList();
              } else {
                wx.showToast({
                  title: res.data.msg || '删除失败',
                  icon: 'none'
                });
              }
            })
            .catch(err => {
              console.error('删除活动失败:', err);
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              });
            })
            .finally(() => {
              this.setData({ activityLoading: false });
            });
        }
      }
    });
  }
});