// pages/audit/article/manage/manage.js
const app = getApp()
const config = require('../../../../utils/config.js')
const { homeArticleApi, articleApplyApi, fileApi } = require('../../../../api/api.js')

Page({
  data: {
    articleList: [],
    loading: false,
    // 编辑弹框相关
    isEditModalVisible: false,
    currentArticle: null,
    editForm: {
      homeArticleId: '',
      articleTitle: '',
      coverImg: '',
      coverImgUrl: '', // 封面图URL，用于预览
      description: '',
      articleType: '',
      articleLink: '',
      articleFile: '',
      metaData: '',
      articleStatus: '',
      publishType: ''
    }
  },

  onLoad(options) {
    this.loadMyArticles()
  },

  onShow() {
    this.loadMyArticles()
  },

  onPullDownRefresh() {
    this.loadMyArticles()
    wx.stopPullDownRefresh()
  },

  // 加载我的上传文章列表
  async loadMyArticles() {
    this.setData({ loading: true })
    
    try {
      // 获取当前用户ID
      const currentUserId = app.globalData.userData?.userId || app.globalData.userData?.wxId || 'user123' // 默认用户ID用于测试
      
      let articleList = []
      
      // 获取本人创建的所有文章
      const articlePageParams = {
        current: 1, // 当前页码，后续可以添加分页功能
        size: 10, // 每页大小
        publishWxId: currentUserId // 发布者ID，使用当前用户ID
      }
      
      // 调用API获取本人首页文章列表
      const articleRes = await homeArticleApi.getMyArticlePage(articlePageParams)
      
      if (articleRes.data && articleRes.data.code === 200) {
        const articleRecords = articleRes.data.data?.records || []
        
        // 获取所有文章的审核记录
        const applyPageParams = {
          current: 1,
          size: 100, // 获取足够多的审核记录，确保能匹配上所有文章
          // 不传递applyStatus，获取所有状态的审核记录
        }
        
        const applyRes = await articleApplyApi.getApplyPage(applyPageParams)
        const applyRecords = applyRes.data && applyRes.data.code === 200 ? applyRes.data.data?.records || [] : []
        
        // 创建审核记录映射表，key为homeArticleId
        const applyMap = new Map()
        applyRecords.forEach(apply => {
          applyMap.set(apply.articleInfo?.homeArticleId, apply)
        })
        
        // 将API返回的数据转换为页面需要的格式
        articleList = articleRecords.map(item => {
          // 获取对应的审核记录
          const applyRecord = applyMap.get(item.homeArticleId)
          
          // 根据审核记录确定状态
          let status = 'pending'
          let statusText = '待审核'
          
          if (applyRecord) {
            // 有审核记录，使用审核记录的状态
            if (applyRecord.applyStatus === 1) {
              status = 'approved'
              statusText = '已通过'
            } else if (applyRecord.applyStatus === 2) {
              status = 'rejected'
              statusText = '已拒绝'
            } else {
              status = 'pending'
              statusText = '待审核'
            }
          } else {
            // 没有审核记录，默认显示待审核
            status = 'pending'
            statusText = '待审核'
          }
          
          // 格式化时间，移除T字符
          let formattedTime = item.createTime || ''
          if (formattedTime) {
            formattedTime = formattedTime.replace('T', ' ')
          }
          
          return {
            id: item.homeArticleId,
            articleId: item.homeArticleId,
            nickname: item.articleTitle,
            avatar: item.publisherAvatar,
            school: '', // API返回中没有school字段，暂时为空
            submitTime: formattedTime,
            status: status,
            statusText: statusText,
            userId: currentUserId,
            coverImg: item.coverImg?.fileUrl?.replace(/`/g, '') || '' // 使用coverImg对象中的fileUrl字段，并移除可能存在的反引号
          }
        })
      }
      
      this.setData({
        articleList: articleList,
        loading: false
      })
    } catch (error) {
      console.error('加载我的上传文章列表失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/audit/user/detail/detail?id=${id}`
    })
  },

  // 发布文章
  onPublishTap() {
    wx.navigateTo({
      url: '/pages/article-publish/index/index'
    })
  },

  // 点击编辑按钮
  async onEditTap(e) {
    const { id } = e.currentTarget.dataset
    await this.loadArticleDetail(id)
  },

  // 加载文章详情用于编辑
  async loadArticleDetail(id) {
    try {
      this.setData({ loading: true })
      const res = await homeArticleApi.getHomeArticleDetail(id)
      
      if (res.data && res.data.code === 200) {
        const article = res.data.data
        // 填充编辑表单
        this.setData({
          currentArticle: article,
          editForm: {
            homeArticleId: article.homeArticleId,
            articleTitle: article.articleTitle || '',
            coverImg: article.coverImg || '',
            coverImgUrl: article.coverImg?.fileUrl || '', // 设置封面图预览URL
            description: article.description || '',
            articleType: article.articleType || '',
            articleLink: article.articleLink || '',
            articleFile: article.articleFile || '',
            metaData: article.metaData || '{}',
            articleStatus: article.articleStatus || '',
            publishType: article.publishType || ''
          }
        })
        this.showEditModal()
      }
    } catch (error) {
      console.error('加载文章详情失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 显示编辑弹框
  showEditModal() {
    this.setData({
      isEditModalVisible: true
    })
  },

  // 隐藏编辑弹框
  hideEditModal() {
    this.setData({
      isEditModalVisible: false
    })
  },

  // 处理编辑表单输入
  onEditInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    
    this.setData({
      [`editForm.${field}`]: value
    })
  },

  // 点击上传封面图
  onCoverUploadTap() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      camera: 'back',
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        this.handleImageUpload(tempFilePath)
      },
      fail: (error) => {
        console.error('选择图片失败:', error)
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        })
      }
    })
  },

  // 上传图片到服务器
  async handleImageUpload(filePath) {
    wx.showLoading({
      title: '上传中...'
    })
    
    try {
      // 使用fileApi上传图片
      const res = await fileApi.uploadImage(filePath)
      
      // 详细日志，用于调试
      console.log('上传图片返回完整数据:', JSON.stringify(res))
      console.log('res.code:', res.code)
      console.log('res.data:', JSON.stringify(res.data))
      
      if (res.code === 200) {
        // 确保res.data是对象
        let fileData = res.data;
        if (typeof fileData !== 'object' || fileData === null) {
          console.error('上传成功，但res.data不是对象:', fileData);
          wx.showToast({
            title: '数据格式错误',
            icon: 'none'
          });
          return;
        }
        
        // 直接从res.data获取fileId和fileUrl
        const fileId = fileData.fileId !== undefined && fileData.fileId !== null ? fileData.fileId : 0;
        const fileUrl = fileData.fileUrl || '';
        console.log('直接获取fileId:', fileId, '类型:', typeof fileId);
        
        // 保存为字符串类型，避免超大整数精度丢失
        // JavaScript的number类型精度有限，超大整数会丢失精度
        const stringFileId = String(fileId);
        console.log('转换为string后:', stringFileId, '类型:', typeof stringFileId);
        
        if (stringFileId && stringFileId !== '0') {
          this.setData({
            'editForm.coverImg': stringFileId, // 设置封面图文件ID，使用字符串类型
            'editForm.coverImgUrl': fileUrl // 设置封面图预览URL
          })
          wx.showToast({
            title: '封面图上传成功',
            icon: 'success'
          })
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
    } catch (error) {
      console.error('上传图片失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 提交编辑
  async onSubmitEdit() {
    try {
      this.setData({ loading: true })
      
      // 构建请求数据，确保coverImg使用字符串类型，避免超大整数精度丢失
      const requestData = { ...this.data.editForm }
      
      // 详细日志，用于调试
      console.log('提交编辑前的数据:', {
        coverImg: requestData.coverImg,
        coverImgType: typeof requestData.coverImg
      })
      
      const res = await homeArticleApi.updateArticle(requestData)
      
      // 详细日志，用于调试
      console.log('更新文章返回完整数据:', JSON.stringify(res))
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        })
        this.hideEditModal()
        this.loadMyArticles() // 重新加载列表
      } else {
        console.error('更新失败:', res.data?.msg || '未知错误');
        wx.showToast({
          title: res.data?.msg || '更新失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('更新文章失败:', error)
      wx.showToast({
        title: '更新失败，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
