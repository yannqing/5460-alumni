// pages/merchant/list/list.js
Page({
  data: {
    merchantList: [
      {
        id: 1,
        name: '星巴克咖啡',
        cover: 'https://via.placeholder.com/300x200/ff6b9d/ffffff?text=Starbucks',
        category: '餐饮美食',
        discount: '全场8折',
        tags: ['校友优惠', '连锁品牌']
      }
    ]
  },

  viewDetail(e) {
    wx.navigateTo({
      url: `/pages/merchant/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
