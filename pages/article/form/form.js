const { homeArticleApi, fileApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    formData: {
      id: '',
      articleTitle: '', // 对应 articleTitle
      cover: '', // 显示用
      coverImg: '', // 对应 coverImg (fileId)
      content: '', // 对应 content (虽然接口文档未明确展示，但保留以防万一，或映射到 description)
      description: '', // 对应 description
      articleType: 1, // 对应 articleType (1-公众号, 2-内部路径, 3-第三方链接)
      articleLink: '', // 对应 articleLink
      articleStatus: 1, // 对应 articleStatus (0-禁用, 1-启用)
      isTop: false
    },
    articleTypes: ['公众号', '内部路径', '第三方链接'], // 对应值 1, 2, 3
    isEdit: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true })
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    homeArticleApi.getHomeArticleDetail(id).then(res => {
      if (res.data && res.data.code === 200) {
        const data = res.data.data
        
        // 处理封面图回显
        let cover = data.cover || data.image || ''
        if (data.coverImg && !cover) {
          cover = config.getImageUrl(`/file/download/${data.coverImg}`)
        }

        this.setData({
          formData: {
            id: data.id || data.homeArticleId, // 兼容 id 和 homeArticleId
            articleTitle: data.articleTitle || data.title || '',
            cover: cover,
            coverImg: data.coverImg || '', 
            content: data.content || '',
            description: data.description || '',
            articleType: data.articleType || 1,
            articleLink: data.articleLink || '',
            articleStatus: data.articleStatus !== undefined ? data.articleStatus : 1,
            isTop: data.isTop === true || data.isTop === 1 || data.top === true
          }
        })
      }
    })
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: e.detail.value
    })
  },

  onTypeChange(e) {
    this.setData({
      'formData.articleType': parseInt(e.detail.value) + 1
    })
  },

  onSwitchChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    // 如果是 articleStatus，true -> 1, false -> 0
    // 如果是 isTop，保持 boolean
    this.setData({
      [`formData.${field}`]: field === 'articleStatus' ? (value ? 1 : 0) : value
    })
  },

  async chooseImage() {
    try {
      const res = await wx.chooseMedia({ count: 1, mediaType: ['image'] })
      const tempFilePath = res.tempFiles[0].tempFilePath
      
      wx.showLoading({ title: '上传中...' })
      const uploadRes = await fileApi.uploadImage(tempFilePath, 'cover.jpg')
      wx.hideLoading()

      if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
        const rawUrl = uploadRes.data.fileUrl
        const fullUrl = config.getImageUrl(rawUrl)
        const fileId = uploadRes.data.fileId || uploadRes.data.id // 假设返回了 id
        
        this.setData({ 
          'formData.cover': fullUrl,
          'formData.coverImg': fileId
        })
      } else {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    } catch (err) {
      wx.hideLoading()
      console.error(err)
    }
  },

  async submit() {
    const { formData, isEdit } = this.data
    if (!formData.articleTitle) return wx.showToast({ title: '请输入标题', icon: 'none' })
    // if (!formData.content) return wx.showToast({ title: '请输入内容', icon: 'none' })

    const userInfo = wx.getStorageSync('userInfo') || {}

    const payload = {
      id: formData.id,
      articleTitle: formData.articleTitle,
      coverImg: formData.coverImg, // int64
      description: formData.description,
      articleType: formData.articleType,
      articleLink: formData.articleLink,
      articleStatus: formData.articleStatus,
      // 补充字段
      articleFile: 0, // 暂时给0
      metaData: '{}',
      publishWxId: userInfo.id || 0, // 假设 userInfo 有 id
      publishUsername: userInfo.nickName || userInfo.username || '',
      // 保留原有字段以防万一
      content: formData.content,
      isTop: formData.isTop
    }

    try {
      wx.showLoading({ title: '保存中...' })
      let res
      if (isEdit) {
        res = await homeArticleApi.updateArticle(payload)
      } else {
        res = await homeArticleApi.createArticle(payload)
      }
      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.data.msg || '保存失败', icon: 'none' })
      }
    } catch (err) {
      wx.hideLoading()
      wx.showToast({ title: '保存失败', icon: 'none' })
    }
  }
})