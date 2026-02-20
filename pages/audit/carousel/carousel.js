// pages/audit/carousel/carousel.js
const app = getApp()
const { bannerApi } = require('../../../api/api.js')

Page({
  data: {
    // 轮播图列表
    bannerList: [],
    // 分页参数
    pageNum: 1,
    pageSize: 10,
    total: 0,
    hasNext: false,
    // 搜索条件
    searchParams: {
      bannerTitle: '',
      bannerType: ''
    },
    // 选中的索引值
    selectedTypeIndex: 0,
    // 加载状态
    loading: false,
    // 类型选项
    typeOptions: [
      { value: '', label: '全部类型' },
      { value: 1, label: '无跳转' },
      { value: 2, label: '内部路径' },
      { value: 3, label: '第三方链接' },
      { value: 4, label: '文章详情' }
    ]
  },

  onLoad(options) {
    // 页面加载时初始化数据
    this.initData()
  },

  onShow() {
    // 页面显示时重新加载数据
    this.initData()
  },

  onReady() {
    // 页面初次渲染完成
  },

  onHide() {
    // 页面隐藏
  },

  onUnload() {
    // 页面卸载
  },

  // 初始化数据
  initData() {
    // 重置分页参数和搜索条件
    this.setData({
      pageNum: 1,
      bannerList: [],
      hasNext: false,
      // 重置搜索条件
      searchParams: {
        bannerTitle: '',
        bannerType: ''
      },
      // 重置选择索引
      selectedTypeIndex: 0
    })
    // 加载数据
    this.loadBannerList()
  },

  // 加载轮播图列表
  loadBannerList() {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    
    const params = {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      ...this.data.searchParams
    }
    
    bannerApi.getBannerPage(params).then(res => {
      this.setData({ loading: false })
      
      if (res.data && res.data.code === 200) {
        const data = res.data.data
        let bannerList = this.data.bannerList
        
        if (this.data.pageNum === 1) {
          // 第一页，替换数据
          bannerList = data.records || []
        } else {
          // 非第一页，追加数据
          bannerList = bannerList.concat(data.records || [])
        }
        
        this.setData({
          bannerList: bannerList,
          total: parseInt(data.total) || 0,
          hasNext: data.hasNext === true
        })
      } else {
        wx.showToast({
          title: res.data.msg || '加载失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      this.setData({ loading: false })
      console.error('加载轮播图列表失败:', err)
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
    })
  },

  // 搜索
  onSearch() {
    this.initData()
  },

  // 输入搜索关键词
  onInput(e) {
    this.setData({
      'searchParams.bannerTitle': e.detail.value
    })
  },

  // 选择类型
  onTypeChange(e) {
    const index = e.detail.value
    const type = this.data.typeOptions[index].value
    this.setData({
      'searchParams.bannerType': type,
      selectedTypeIndex: index
    })
  },

  // 新增轮播图
  onAddBanner() {
    wx.navigateTo({
      url: '/pages/audit/carousel/create/create'
    })
  },

  // 加载更多
  onReachBottom() {
    if (this.data.hasNext && !this.data.loading) {
      this.setData({
        pageNum: this.data.pageNum + 1
      })
      this.loadBannerList()
    }
  },

  // 轮播图点击事件
  onBannerTap(e) {
    const banner = e.currentTarget.dataset.banner
    if (!banner) return

    switch (banner.bannerType) {
      case 1:
        // 无跳转
        break
      case 2:
        // 内部路径
        if (banner.linkUrl) {
          wx.navigateTo({
            url: banner.linkUrl
          })
        }
        break
      case 3:
        // 第三方链接
        if (banner.linkUrl) {
          wx.navigateTo({
            url: `/pages/article/web-view/web-view?url=${encodeURIComponent(banner.linkUrl)}`
          })
        }
        break
      case 4:
        // 文章详情
        if (banner.relatedId) {
          wx.navigateTo({
            url: `/pages/article/detail/detail?id=${banner.relatedId}`
          })
        }
        break
      default:
        break
    }
  },

  // 编辑轮播图
  onEditBanner(e) {
    const banner = e.currentTarget.dataset.banner
    if (!banner) return
    
    // 提取图片的fileId和fileUrl
    const bannerImage = banner.bannerImage || {}
    
    wx.navigateTo({
      url: `/pages/audit/carousel/edit/edit?bannerId=${banner.bannerId}&bannerTitle=${encodeURIComponent(banner.bannerTitle)}&bannerImageFileId=${bannerImage.fileId || ''}&bannerImageFileUrl=${encodeURIComponent(bannerImage.fileUrl || '')}&bannerType=${banner.bannerType}&linkUrl=${encodeURIComponent(banner.linkUrl || '')}&relatedId=${banner.relatedId || ''}&relatedType=${encodeURIComponent(banner.relatedType || '')}&sortOrder=${banner.sortOrder || 0}&bannerStatus=${banner.bannerStatus}&startTime=${encodeURIComponent(banner.startTime || '')}&endTime=${encodeURIComponent(banner.endTime || '')}&description=${encodeURIComponent(banner.description || '')}`
    })
  },

  // 删除轮播图
  onDeleteBanner(e) {
    const banner = e.currentTarget.dataset.banner
    if (!banner) return
    
    // 弹出确认对话框
    wx.showModal({
      title: '确认删除',
      content: `确定要删除轮播图"${banner.bannerTitle}"吗？`,
      success: (res) => {
        if (res.confirm) {
          // 用户确认删除，调用API
          wx.showLoading({
            title: '删除中...',
            mask: true
          })
          
          bannerApi.deleteBanner(banner.bannerId).then(res => {
            wx.hideLoading()
            
            if (res.data && res.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              
              // 重新加载列表数据
              this.initData()
            } else {
              wx.showToast({
                title: res.data.msg || '删除失败',
                icon: 'none'
              })
            }
          }).catch(err => {
            wx.hideLoading()
            console.error('删除轮播图失败:', err)
            wx.showToast({
              title: '网络错误',
              icon: 'none'
            })
          })
        }
      }
    })
  }
})