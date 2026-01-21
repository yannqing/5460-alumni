// pages/article-publish/index/index.js
const app = getApp()

Page({
  data: {
    title: '',
    content: '',
    coverImage: ''
  },

  onLoad(options) {
    // 页面加载
  },

  onShow() {
    // 页面显示
  },

  // 标题输入
  onTitleInput(e) {
    this.setData({
      title: e.detail.value
    })
  },

  // 内容输入
  onContentInput(e) {
    this.setData({
      content: e.detail.value
    })
  },

  // 选择封面图
  chooseCoverImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          coverImage: res.tempFiles[0].tempFilePath
        })
      }
    })
  },

  // 发布文章
  publishArticle() {
    const { title, content } = this.data
    
    if (!title.trim()) {
      wx.showToast({
        title: '请输入文章标题',
        icon: 'none'
      })
      return
    }
    
    if (!content.trim()) {
      wx.showToast({
        title: '请输入文章内容',
        icon: 'none'
      })
      return
    }
    
    // 这里可以添加文章发布的API调用逻辑
    wx.showLoading({
      title: '发布中...'
    })
    
    // 模拟发布成功
    setTimeout(() => {
      wx.hideLoading()
      wx.showToast({
        title: '发布成功',
        icon: 'success'
      })
      // 发布成功后返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }, 1500)
  }
})
