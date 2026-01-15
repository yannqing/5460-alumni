// pages/alumni-association/organization-detail/organization-detail.js
Page({
  data: {
    roleList: [] // 存储角色列表
  },

  onLoad(options) {
    this.initPage()
  },

  // 初始化页面数据
  initPage() {
    // 暂时不需要调用接口，直接设置为空数组
    this.setData({
      roleList: []
    })
  }
})