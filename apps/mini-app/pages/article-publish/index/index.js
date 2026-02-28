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
    publishType: 0, // 默认校友会发布类型，可选：0-校友会，1-校促会
    showOnHomepage: 0, // 是否在首页展示：0-不展示，1-展示
    childArticles: [], // 子文章列表
    articleTypes: [
      { value: 1, label: '公众号' },
      { value: 2, label: '内部路径' },
      { value: 3, label: '第三方链接' }
    ],
    publishTypes: [
      { value: 0, label: '校友会' },
      { value: 1, label: '校促会' }
    ],
    showOnHomepageOptions: [
      { value: 0, label: '不展示' },
      { value: 1, label: '展示' }
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
    const publishType = parseInt(e.detail.value)
    this.setData({
      publishType: publishType
    })
    
    // 选择发布者类型后，自动获取组织列表
    this.getManagedOrganizations()
  },

  // 是否在首页展示选择变化
  onShowOnHomepageChange(e) {
    this.setData({
      showOnHomepage: parseInt(e.detail.value)
    })
  },

  // 添加子文章
  addChildArticle() {
    const { childArticles, articleType } = this.data
    const newChildArticle = {
      articleTitle: '',
      coverImage: '',
      coverImgId: '',
      coverImg: null,
      description: '',
      articleType: articleType, // 与主文章类型一致
      articleLink: '',
      articleFile: null,
      metaData: '{}'
    }
    childArticles.push(newChildArticle)
    this.setData({
      childArticles: childArticles
    })
  },

  // 删除子文章
  deleteChildArticle(e) {
    const { index } = e.currentTarget.dataset
    const childArticles = this.data.childArticles || []
    if (index >= 0 && index < childArticles.length) {
      childArticles.splice(index, 1)
      this.setData({
        childArticles: childArticles
      })
    }
  },

  // 子文章输入
  onChildArticleInput(e) {
    const { index, field } = e.currentTarget.dataset
    const { value } = e.detail
    const childArticles = this.data.childArticles || []
    if (index >= 0 && index < childArticles.length) {
      childArticles[index] = {
        ...childArticles[index],
        [field]: value
      }
      this.setData({
        childArticles: childArticles
      })
    }
  },

  // 选择子文章封面图
  chooseChildArticleCoverImage(e) {
    const { index } = e.currentTarget.dataset
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        const childArticles = this.data.childArticles || []
        if (index >= 0 && index < childArticles.length) {
          childArticles[index] = {
            ...childArticles[index],
            coverImage: tempFilePath
          }
          this.setData({
            childArticles: childArticles
          })

          // 上传图片到服务器
          this.uploadChildArticleCoverImage(tempFilePath, index)
        }
      }
    })
  },

  // 上传子文章封面图到服务器
  uploadChildArticleCoverImage(filePath, index) {
    wx.showLoading({
      title: '上传中...'
    })

    // 使用文件上传工具上传图片
    const { fileApi } = require('../../../api/api')
    fileApi.uploadImage(filePath)
      .then(res => {
        wx.hideLoading()

        // 详细日志，用于调试
        console.log('上传子文章图片返回完整数据:', JSON.stringify(res))
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
            const childArticles = this.data.childArticles || []
            if (index >= 0 && index < childArticles.length) {
              childArticles[index] = {
                ...childArticles[index],
                coverImgId: stringFileId,
                coverImg: stringFileId
              }
              this.setData({
                childArticles: childArticles
              })

              // 立即打印保存后的值，用于调试
              console.log('保存后的子文章coverImgId:', childArticles[index].coverImgId)
              console.log('coverImgId类型:', typeof childArticles[index].coverImgId)

              wx.showToast({
                title: '上传成功',
                icon: 'success'
              })
            }
          } else {
            console.error('上传成功，但fileId无效:', fileId);
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
        console.error('上传子文章封面图失败:', err)
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      })
  },



  // 显示发布者选择器
  showPublisherSelector() {
    // 先获取组织列表，然后再显示选择器
    this.getManagedOrganizations().then(() => {
      this.setData({
        showPublisherPicker: true
      })
    })
  },

  // 取消发布者选择
  cancelPublisherSelect() {
    this.setData({
      showPublisherPicker: false,
      publisherList: []
    })
  },



  // 获取组织列表
  async getManagedOrganizations() {
    const { publishType } = this.data
    try {
      // 使用新的接口获取管理的组织列表
      const { homeArticleApi } = require('../../../api/api')
      const res = await homeArticleApi.getManagedOrganizations({
        organizationType: publishType
      })

      console.log('获取组织列表接口返回:', JSON.stringify(res))

      // 检查接口返回格式（与 publishArticle 方法保持一致）
      if (res.data && res.data.code === 200) {
        // 处理结果
        const organizationList = res.data.data || []
        console.log('组织列表:', JSON.stringify(organizationList))
        // 转换为发布者列表格式
        const publisherList = organizationList.map(org => ({
          id: org.organizationId || '',
          name: org.organizationName || '未命名',
          avatar: org.avatar || '',
          memberCount: org.memberCount || 0,
          monthlyHomepageArticleQuota: org.monthlyHomepageArticleQuota || 0
        }))

        console.log('发布者列表:', JSON.stringify(publisherList))
        this.setData({ publisherList })
        return publisherList
      } else {
        console.error('获取组织列表失败:', res.data?.msg || res.msg || '未知错误')
        this.setData({ publisherList: [] })
        return []
      }
    } catch (e) {
      console.error('获取组织列表失败:', e)
      this.setData({ publisherList: [] })
      return []
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
    const { title, content, articleLink, coverImgId, articleType, publishType, selectedPublisherId, selectedPublisherName, showOnHomepage, childArticles } = this.data

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

    // 验证子文章
    for (let i = 0; i < childArticles.length; i++) {
      const childArticle = childArticles[i]
      if (!childArticle.articleTitle || !childArticle.articleTitle.trim()) {
        wx.showToast({
          title: `子文章 ${i + 1}：请输入文章标题`,
          icon: 'none'
        })
        return
      }
      if (!childArticle.articleType) {
        wx.showToast({
          title: `子文章 ${i + 1}：请选择文章类型`,
          icon: 'none'
        })
        return
      }
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
      showOnHomepage: showOnHomepage, // 是否在首页展示：0-不展示，1-展示
      childArticles: childArticles // 子文章列表
    }

    // 固定文章类型为第三方链接
    requestData.articleType = 3
    // 将 publishType 转换为对应的字符串类型
    requestData.publishType = publishType === 0 ? 'ASSOCIATION' : 'LOCAL_PLATFORM'

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
