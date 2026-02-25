// pages/article-publish/index/index.js
const app = getApp()
const { homeArticleApi, alumniApi, associationApi, localPlatformApi } = require('../../../api/api')
const config = require('../../../utils/config.js')

// 防抖函数
function debounce(fn, delay) {
  let timer = null
  return function () {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(function () {
      fn.apply(context, args)
    }, delay)
  }
}

Page({
  data: {
    title: '',
    content: '',
    articleLink: '', // 文章链接
    coverImage: '',
    coverImgId: '', // 封面图文件ID，用于发布文章时传给后端
    articleType: 3, // 默认第三方链接类型，固定值：3-第三方链接
    publishType: 'ALUMNI', // 默认校友发布类型，可选：ALUMNI-校友，ASSOCIATION-校友会，LOCAL_PLATFORM-校促会
    articleTypes: [
      { value: 1, label: '公众号' },
      { value: 2, label: '内部路径' },
      { value: 3, label: '第三方链接' }
    ],
    publishTypes: [
      { value: 'ALUMNI', label: '校友' },
      { value: 'ASSOCIATION', label: '校友会' },
      { value: 'LOCAL_PLATFORM', label: '校促会' }
    ],
    // 发布者选择相关数据
    selectedPublisherId: '',
    selectedPublisherName: '',
    publisherList: [],
    showPublisherPicker: false,
    publisherSearchKeyword: '',
    showPublisherSearchResults: false,
    defaultAvatar: config.defaultAvatar,
    defaultUserAvatarUrl: `https://${config.DOMAIN}/upload/images/assets/images/avatar.png`
  },

  onLoad(options) {
    // 创建搜索防抖函数
    this.searchPublisherDebounced = debounce(this.searchPublisher, 500)
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

  // 文章描述输入
  onContentInput(e) {
    this.setData({
      content: e.detail.value
    })
  },

  // 文章链接输入
  onArticleLinkInput(e) {
    this.setData({
      articleLink: e.detail.value
    })
  },

  // 选择封面图
  chooseCoverImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        this.setData({
          coverImage: tempFilePath
        })

        // 上传图片到服务器
        this.uploadCoverImage(tempFilePath)
      }
    })
  },

  // 上传封面图到服务器
  uploadCoverImage(filePath) {
    wx.showLoading({
      title: '上传中...'
    })

    // 使用文件上传工具上传图片
    const { fileApi } = require('../../../api/api')
    fileApi.uploadImage(filePath)
      .then(res => {
        wx.hideLoading()

        // 详细日志，用于调试
        console.log('上传图片返回完整数据:', JSON.stringify(res))
        console.log('res.code:', res.code)
        console.log('res.data:', JSON.stringify(res.data))

        if (res.code === 200) {
          // 确保res.data是对象
          const fileData = res.data;
          if (typeof fileData !== 'object' || fileData === null) {
            console.error('上传成功，但res.data不是对象:', fileData);
            wx.showToast({
              title: '数据格式错误',
              icon: 'none'
            });
            return;
          }

          // 直接从res.data获取fileId，根据接口文档这是正确的格式
          const fileId = fileData.fileId !== undefined && fileData.fileId !== null ? fileData.fileId : 0;
          console.log('直接获取fileId:', fileId, '类型:', typeof fileId);

          // 保存为字符串类型，避免超大整数精度丢失
          // JavaScript的number类型精度有限，超大整数会丢失精度
          const stringFileId = String(fileId);
          console.log('转换为string后:', stringFileId, '类型:', typeof stringFileId);

          if (stringFileId && stringFileId !== '0') {
            // 保存封面图文件ID，使用字符串类型避免精度丢失
            this.setData({
              coverImgId: stringFileId
            })

            // 立即打印保存后的值，用于调试
            console.log('保存后的coverImgId:', this.data.coverImgId)
            console.log('coverImgId类型:', typeof this.data.coverImgId)

            wx.showToast({
              title: '上传成功',
              icon: 'success'
            })
          } else {
            console.error('上传成功，但fileId无效:', fileId);
            console.error('转换后的numericFileId:', numericFileId);
            wx.showToast({
              title: '未获取到有效的文件ID',
              icon: 'none'
            })
          }
        } else {
          console.error('上传失败:', res.msg || '未知错误');
          wx.showToast({
            title: res.msg || '上传失败',
            icon: 'none'
          })
        }
      })
      .catch(err => {
        wx.hideLoading()
        console.error('上传封面图失败:', err)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      })
  },



  // 发布者类型选择变化
  onPublishTypeChange(e) {
    this.setData({
      publishType: e.detail.value
    })
  },

  // 显示发布者选择器
  showPublisherSelector() {
    this.setData({
      showPublisherPicker: true,
      publisherSearchKeyword: '',
      publisherList: [],
      showPublisherSearchResults: false
    })
  },

  // 取消发布者选择
  cancelPublisherSelect() {
    this.setData({
      showPublisherPicker: false,
      publisherSearchKeyword: '',
      publisherList: [],
      showPublisherSearchResults: false
    })
  },

  // 处理发布者搜索输入
  onPublisherSearchInput(e) {
    const value = e.detail.value
    this.setData({
      publisherSearchKeyword: value
    })

    if (value.trim()) {
      this.searchPublisherDebounced(value)
    } else {
      this.setData({ publisherList: [] })
    }
  },

  // 处理发布者搜索输入框聚焦
  onPublisherSearchFocus() {
    if (this.data.publisherSearchKeyword) {
      this.searchPublisher(this.data.publisherSearchKeyword)
    }
  },

  // 搜索发布者
  async searchPublisher(keyword) {
    if (!keyword) { return }

    const { publishType } = this.data
    try {
      let res

      // 根据发布者类型调用不同的API
      if (publishType === 'ALUMNI') {
        // 搜索校友
        res = await alumniApi.queryAlumniList({
          current: 1,
          pageSize: 10,
          name: keyword.trim()
        })

        if (res.data && res.data.code === 200) {
          // 处理校友搜索结果，保留完整信息
          const alumniList = res.data.data.records || []
          const publisherList = alumniList.map(alumni => ({
            id: alumni.wxId || alumni.id || alumni.userId || '',
            name: alumni.name || alumni.nickname || alumni.realName || '未命名',
            avatarUrl: alumni.avatarUrl,
            school: alumni.school
          }))

          this.setData({ publisherList })
        }
      } else if (publishType === 'ASSOCIATION') {
        // 搜索校友会
        res = await associationApi.getAssociationList({
          current: 1,
          pageSize: 10,
          associationName: keyword.trim()
        })

        if (res.data && res.data.code === 200) {
          // 处理校友会搜索结果
          const associationList = res.data.data.records || []
          const publisherList = associationList.map(association => ({
            id: association.id || association.associationId || '',
            name: association.associationName || '未命名'
          }))

          this.setData({ publisherList })
        }
      } else if (publishType === 'LOCAL_PLATFORM') {
        // 搜索校促会
        res = await localPlatformApi.getLocalPlatformPage({
          current: 1,
          pageSize: 10,
          platformName: keyword.trim()
        })

        if (res.data && res.data.code === 200) {
          // 处理校促会搜索结果
          const platformList = res.data.data.records || []
          const publisherList = platformList.map(platform => ({
            id: platform.id || platform.platformId || '',
            name: platform.platformName || '未命名'
          }))

          this.setData({ publisherList })
        }
      }
    } catch (e) {
      console.error('搜索发布者失败:', e)
      this.setData({ publisherList: [] })
    }
  },

  // 选择发布者
  selectPublisher(e) {
    const { publisherId, publisherName } = e.currentTarget.dataset
    this.setData({
      selectedPublisherId: publisherId,
      selectedPublisherName: publisherName,
      showPublisherPicker: false,
      publisherSearchKeyword: '',
      publisherList: []
    })
  },

  // 发布文章
  publishArticle() {
    const { title, content, articleLink, coverImgId, articleType, publishType, selectedPublisherId, selectedPublisherName } = this.data

    if (!title.trim()) {
      wx.showToast({
        title: '请输入文章标题',
        icon: 'none'
      })
      return
    }

    if (!articleLink.trim()) {
      wx.showToast({
        title: '请输入文章链接',
        icon: 'none'
      })
      return
    }

    if (!coverImgId || coverImgId === '0') {
      wx.showToast({
        title: '请上传封面图',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '发布中...'
    })

    // 详细日志，用于调试
    console.log('发布文章前的数据:', {
      title: title.trim(),
      content: content.trim(),
      articleLink: articleLink.trim(),
      coverImgId: coverImgId,
      coverImgIdType: typeof coverImgId,
      articleType: articleType,
      publishType: publishType,
      selectedPublisherId: selectedPublisherId,
      selectedPublisherName: selectedPublisherName
    })

    // 获取用户信息
    const app = getApp()
    const userData = app.globalData.userData || {}
    const userInfo = wx.getStorageSync('userInfo') || {}

    // 构建请求参数
    const requestData = {
      articleTitle: title.trim(),
      description: content.trim(),
      articleStatus: 1, // 默认启用状态
    }

    // 固定文章类型为第三方链接
    requestData.articleType = 3
    requestData.publishType = publishType

    // 添加文章链接
    if (articleLink.trim()) {
      requestData.articleLink = articleLink.trim()
    }

    // 添加封面图文件ID：直接使用字符串类型，避免超大整数精度丢失
    if (coverImgId !== undefined && coverImgId !== null && coverImgId !== '' && coverImgId !== '0') {
      // 直接使用字符串类型，不要转换为number
      // 超大整数转换为number会丢失精度
      console.log('添加封面图ID:', coverImgId, '类型:', typeof coverImgId)

      // 直接将字符串作为值传入，后端会自动处理
      requestData.coverImg = coverImgId
    } else {
      console.log('未添加封面图ID，coverImgId:', coverImgId, '类型:', typeof coverImgId)
    }

    // 打印最终请求数据，便于调试
    console.log('最终发布请求数据:', JSON.stringify(requestData))

    // 添加发布者信息
    if (selectedPublisherId) {
      // 使用选择的发布者信息，直接使用字符串类型，避免超大整数精度丢失
      requestData.publishWxId = selectedPublisherId
      requestData.publishUsername = selectedPublisherName
    } else {
      // 使用当前用户信息作为发布者，直接使用字符串类型，避免超大整数精度丢失
      const userId = userData.wxId || userData.id || userInfo.wxId || userInfo.id || '0'
      requestData.publishWxId = userId
      requestData.publishUsername = selectedPublisherName || userData.nickname || userData.nickName || userInfo.nickname || userInfo.nickName || ''
    }

    // 调用API发布文章
    homeArticleApi.createArticle(requestData)
      .then(res => {
        wx.hideLoading()

        if (res.data.code === 200) {
          wx.showToast({
            title: '发布成功',
            icon: 'success'
          })
          // 发布成功后返回上一页
          setTimeout(() => {
            wx.navigateBack()
          }, 1500)
        } else {
          wx.showToast({
            title: res.data.msg || '发布失败',
            icon: 'none'
          })
        }
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
        console.error('发布文章失败:', err)
      })
  }
})
