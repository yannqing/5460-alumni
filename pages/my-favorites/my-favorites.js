// pages/my-favorites/my-favorites.js
Page({
  data: {
    favoriteShops: []
  },

  onLoad() {
    this.loadFavoriteShops()
  },

  onShow() {
    // 每次显示页面时刷新列表
    this.loadFavoriteShops()
  },

  loadFavoriteShops() {
    // 模拟收藏的店铺数据
    const mockShops = [
      {
        id: 1,
        name: '星巴克咖啡',
        cover: '/assets/images/头像.png',
        description: '高品质咖啡，舒适环境，适合商务洽谈',
        rating: 4.8,
        distance: 520,
        avgPrice: 45,
        isCertified: true,
        tags: ['咖啡', '休闲', '商务']
      },
      {
        id: 2,
        name: '海底捞火锅',
        cover: '/assets/images/头像.png',
        description: '正宗川味火锅，服务周到，校友专享优惠',
        rating: 4.9,
        distance: 1280,
        avgPrice: 120,
        isCertified: true,
        tags: ['火锅', '聚餐', '校友优惠']
      },
      {
        id: 4,
        name: '华影国际影城',
        cover: '/assets/images/头像.png',
        description: 'IMAX影厅，舒适座椅，校友观影优惠',
        rating: 4.6,
        distance: 760,
        avgPrice: 60,
        isCertified: false,
        tags: ['电影', '娱乐']
      },
      {
        id: 5,
        name: '肯德基',
        cover: '/assets/images/头像.png',
        description: '快餐美食，方便快捷，适合工作餐',
        rating: 4.5,
        distance: 890,
        avgPrice: 35,
        isCertified: false,
        tags: ['快餐', '便捷']
      },
      {
        id: 6,
        name: '必胜客',
        cover: '/assets/images/头像.png',
        description: '意式披萨，家庭聚餐好选择',
        rating: 4.6,
        distance: 1500,
        avgPrice: 80,
        isCertified: false,
        tags: ['披萨', '西餐']
      }
    ]

    this.setData({
      favoriteShops: mockShops
    })
  },

  viewShopDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${id}`
    })
  },

  unfavoriteShop(e) {
    const { id } = e.currentTarget.dataset
    wx.showModal({
      title: '提示',
      content: '确定取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          // 从列表中移除
          const { favoriteShops } = this.data
          const newList = favoriteShops.filter(shop => shop.id !== id)
          this.setData({
            favoriteShops: newList
          })
          wx.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
        }
      }
    })
  },

  goToDiscover() {
    wx.switchTab({
      url: '/pages/discover/discover'
    })
  }
})


