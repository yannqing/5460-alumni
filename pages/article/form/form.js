const { homeArticleApi, fileApi, associationApi } = require('../../../api/api.js')
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
      isTop: false,
      publishUsername: '', // 发布者名称
      publishType: 0, // 发布类型 (0-母校, 1-校友会, 2-商铺, 3-校友)
      publishAssociationId: null // 选中的校友会ID
    },
    articleTypes: ['公众号', '内部路径', '第三方链接'], // 对应值 1, 2, 3
    publishTypes: ['母校', '校友会', '商铺', '校友'], // 发布类型选项
    publisherList: [], // 发布者列表（校友会列表）
    selectedPublisherIndex: -1, // 选中的发布者索引
    isEdit: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true })
      this.loadDetail(options.id)
    }
  },

  async loadDetail(id) {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await homeArticleApi.getHomeArticleDetail(id);
      wx.hideLoading();
      if (res.data && res.data.code === 200) {
        const data = res.data.data
        if (!data) {
          wx.showToast({ title: '文章不存在', icon: 'none' });
          return;
        }
        
        // 处理封面图回显
        let cover = data.cover || data.image || ''
        if (data.coverImg) {
          if (typeof data.coverImg === 'object') {
            cover = config.getImageUrl(data.coverImg.fileUrl || data.coverImg.thumbnailUrl || '');
          } else {
            cover = config.getImageUrl(`/file/download/${data.coverImg}`);
          }
        }

        // 处理ID：严格使用 homeArticleId（数据库字段 home_article_id）
        // 绝对不使用 coverImg（这是图片ID，不是文章ID）
        let articleId = null;
        if (data.homeArticleId !== undefined && data.homeArticleId !== null && data.homeArticleId !== '') {
          articleId = data.homeArticleId;
        } else if (data.home_article_id !== undefined && data.home_article_id !== null && data.home_article_id !== '') {
          articleId = data.home_article_id;
        } else if (data.id !== undefined && data.id !== null && data.id !== '' && String(data.id) !== String(data.coverImg)) {
          // 确保 id 不是 coverImg
          articleId = data.id;
        }
        
        // 最终验证：确保不是 coverImg
        if (articleId && String(articleId) === String(data.coverImg)) {
          articleId = null;
        }
        
        if (!articleId && articleId !== 0) {
          wx.showToast({ title: '文章ID错误', icon: 'none' });
          return;
        }

        const publishType = data.publishType !== undefined && data.publishType !== null ? data.publishType : 0;
        
        // 如果发布类型是校友会，先加载校友会列表
        if (publishType === 1) {
          await this.loadPublisherList();
        }
        
        // 等待列表加载完成后再设置数据
        const publisherList = this.data.publisherList || [];
        let selectedIndex = -1;
        
        // 如果发布类型是校友会，尝试匹配选中的项
        if (publishType === 1 && data.publishUsername && publisherList.length > 0) {
          // 尝试通过名称匹配
          selectedIndex = publisherList.findIndex(item => item.name === data.publishUsername);
          // 如果名称匹配失败，尝试通过ID匹配
          if (selectedIndex < 0 && data.publishAssociationId) {
            selectedIndex = publisherList.findIndex(item => {
              const itemId = String(item.id);
              const targetId = String(data.publishAssociationId);
              return itemId === targetId;
            });
          }
        }
        
        this.setData({
          formData: {
            id: String(articleId), // 确保ID为字符串
            articleTitle: data.articleTitle || data.title || '',
            cover: cover,
            coverImg: typeof data.coverImg === 'object' ? (data.coverImg.fileId || data.coverImg.id) : (data.coverImg || ''), 
            content: data.content || '',
            description: data.description || '',
            articleType: data.articleType || 1,
            articleLink: data.articleLink || '',
            articleStatus: data.articleStatus !== undefined ? data.articleStatus : 1,
            isTop: data.isTop === true || data.isTop === 1 || data.top === true,
            publishUsername: data.publishUsername || '',
            publishType: publishType,
            publishAssociationId: data.publishAssociationId || null
          },
          selectedPublisherIndex: selectedIndex >= 0 ? selectedIndex : -1
        });
      } else {
        wx.showToast({ title: res.data?.msg || '加载失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
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

  async onPublishTypeChange(e) {
    const publishType = parseInt(e.detail.value);
    this.setData({
      'formData.publishType': publishType,
      'formData.publishUsername': '',
      'formData.publishAssociationId': null,
      'selectedPublisherIndex': -1,
      'publisherList': []
    });
    
    // 如果选择的是校友会（publishType === 1），加载校友会列表
    if (publishType === 1) {
      await this.loadPublisherList();
    }
  },

  // 加载发布者列表（校友会列表）
  async loadPublisherList() {
    try {
      wx.showLoading({ title: '加载中...' });
      const res = await associationApi.getMyPresidentAssociations({
        current: 1,
        size: 100 // 加载足够多的数据
      });
      wx.hideLoading();

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {};
        const records = data.records || [];
        
        // 映射数据，提取名称和头像
        const publisherList = records.map(item => {
          let avatar = '';
          if (item.logo || item.icon || item.avatar) {
            const avatarUrl = item.logo || item.icon || item.avatar;
            avatar = config.getImageUrl(avatarUrl);
          }
          
          return {
            id: item.alumniAssociationId || item.associationId || item.id,
            name: item.associationName || item.name || '未知校友会',
            avatar: avatar
          };
        });

        this.setData({
          publisherList: publisherList
        });
      } else {
        wx.showToast({ title: res.data?.msg || '加载失败', icon: 'none' });
        this.setData({ publisherList: [] });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
      this.setData({ publisherList: [] });
    }
  },

  // 选择发布者
  onPublisherChange(e) {
    const index = parseInt(e.detail.value);
    const publisher = this.data.publisherList[index];
    
    if (publisher) {
      this.setData({
        'selectedPublisherIndex': index,
        'formData.publishUsername': publisher.name,
        'formData.publishAssociationId': publisher.id
      });
    }
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
      if (!res || !res.tempFiles || res.tempFiles.length === 0) {
        return;
      }
      
      const tempFilePath = res.tempFiles[0].tempFilePath
      const fileName = res.tempFiles[0].name || 'cover.jpg'
      
      wx.showLoading({ title: '上传中...' })
      
      const uploadRes = await fileApi.uploadImage(tempFilePath, fileName)
      wx.hideLoading()

      // 处理不同的响应格式
      let fileData = null;
      if (uploadRes && uploadRes.data) {
        if (uploadRes.data.code === 200 && uploadRes.data.data) {
          fileData = uploadRes.data.data;
        } else if (uploadRes.data.fileUrl || uploadRes.data.fileId) {
          fileData = uploadRes.data;
        }
      } else if (uploadRes && (uploadRes.fileUrl || uploadRes.fileId)) {
        fileData = uploadRes;
      }

      if (fileData) {
        const rawUrl = fileData.fileUrl || fileData.url || '';
        const fullUrl = rawUrl ? config.getImageUrl(rawUrl) : '';
        const fileId = fileData.fileId || fileData.id || fileData.fileId || 0;
        
        this.setData({ 
          'formData.cover': fullUrl,
          'formData.coverImg': fileId
        });
        
        wx.showToast({ title: '上传成功', icon: 'success' });
      } else {
        wx.showToast({ title: '上传失败，请重试', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '上传失败', icon: 'none' });
    }
  },

  async submit() {
    const { formData, isEdit } = this.data
    if (!formData.articleTitle || !formData.articleTitle.trim()) {
      return wx.showToast({ title: '请输入标题', icon: 'none' });
    }

    // 如果是链接类型，需要填写链接
    if (formData.articleType === 3 && !formData.articleLink) {
      return wx.showToast({ title: '链接类型需要填写文章链接', icon: 'none' });
    }

    // 如果发布类型是校友会，必须选择发布者
    if (formData.publishType === 1) {
      if (!formData.publishAssociationId || !formData.publishUsername) {
        return wx.showToast({ title: '请选择发布者', icon: 'none' });
      }
    }

    const app = getApp();
    const userData = app.globalData.userData || {};
    const userInfo = wx.getStorageSync('userInfo') || {};

    // 构建请求参数
    const payload = {
      articleTitle: formData.articleTitle.trim(),
      description: formData.description || '',
      articleType: formData.articleType,
      articleLink: formData.articleLink || '',
      articleStatus: formData.articleStatus,
      content: formData.content || '',
      isTop: formData.isTop || false
    };

    // 如果有封面图，添加封面图ID
    if (formData.coverImg) {
      // 如果是对象，提取ID；如果是字符串/数字，直接使用
      if (typeof formData.coverImg === 'object') {
        payload.coverImg = formData.coverImg.fileId || formData.coverImg.id || 0;
      } else {
        payload.coverImg = formData.coverImg;
      }
    } else {
      payload.coverImg = 0;
    }

    // 如果是编辑模式，添加ID（数据库字段是 home_article_id，后端期望 homeArticleId）
    if (isEdit) {
      if (!formData.id) {
        wx.showToast({ title: '文章ID错误，无法更新', icon: 'none' });
        return;
      }
      
      // 处理ID：确保使用正确的 homeArticleId
      // 由于可能存在 JavaScript Number 精度问题，对于大整数保持字符串形式
      let finalId = formData.id;
      
      // 如果 ID 是字符串，检查是否在安全整数范围内
      if (typeof finalId === 'string') {
        // 尝试转换为数字进行范围检查
        const idNum = Number(finalId);
        // 检查是否在安全整数范围内（Number.MAX_SAFE_INTEGER = 9007199254740991）
        if (!isNaN(idNum) && idNum <= Number.MAX_SAFE_INTEGER && idNum >= Number.MIN_SAFE_INTEGER) {
          // 在安全范围内，可以转换为数字
          finalId = idNum;
        }
      } else if (typeof finalId === 'number') {
        // 如果已经是数字，检查是否在安全范围内
        if (finalId > Number.MAX_SAFE_INTEGER || finalId < Number.MIN_SAFE_INTEGER) {
          // 超出范围，转换为字符串
          finalId = String(finalId);
        }
      }
      
      // 后端更新接口必须使用 homeArticleId 字段（UpdateHomePageArticleDto 要求）
      // 后端Jackson会自动将字符串数字转换为Long类型
      payload.homeArticleId = finalId;
    }

    // 添加发布者信息
    // 如果发布类型是校友会且选择了发布者，使用选中的发布者信息
    if (formData.publishType === 1 && formData.publishAssociationId && formData.publishUsername) {
      // 使用选中的校友会作为发布者
      payload.publishWxId = formData.publishAssociationId; // 使用校友会ID作为发布者ID
      payload.publishUsername = formData.publishUsername.trim();
    } else {
      // 其他情况使用个人账号信息
      payload.publishWxId = userData.wxId || userData.id || userInfo.wxId || userInfo.id || 0;
      // 发布者名称：优先使用表单中填入的值，如果没有则使用用户信息
      payload.publishUsername = formData.publishUsername && formData.publishUsername.trim() 
        ? formData.publishUsername.trim() 
        : (userData.nickname || userData.nickName || userInfo.nickname || userInfo.nickName || '');
    }
    
    // 添加发布类型
    if (formData.publishType !== undefined && formData.publishType !== null) {
      payload.publishType = formData.publishType;
    }

    try {
      wx.showLoading({ title: '保存中...' });
      let res;
      if (isEdit) {
        res = await homeArticleApi.updateArticle(payload);
      } else {
        res = await homeArticleApi.createArticle(payload);
      }
      wx.hideLoading();

      if (res.data && res.data.code === 200) {
        // 如果是新建，确保返回的数据中包含 publishWxId 字段
        if (!isEdit) {
          // 如果data直接是ID（数字或字符串），转换为对象并添加 publishWxId
          if (res.data.data && (typeof res.data.data === 'number' || typeof res.data.data === 'string')) {
            // 将返回的ID转换为对象，并添加 publishWxId 字段
            res.data.data = {
              homeArticleId: res.data.data,
              id: res.data.data,
              publishWxId: payload.publishWxId,
              publishUsername: payload.publishUsername
            };
          } 
          // 如果data是对象，确保包含 publishWxId 字段
          else if (res.data.data && typeof res.data.data === 'object') {
            // 如果返回对象中没有 publishWxId，则添加
            if (!res.data.data.publishWxId) {
              res.data.data.publishWxId = payload.publishWxId;
            }
            // 如果返回对象中没有 publishUsername，则添加
            if (!res.data.data.publishUsername) {
              res.data.data.publishUsername = payload.publishUsername;
            }
          }
        }
        wx.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1500);
      } else {
        wx.showToast({ title: res.data?.msg || '保存失败', icon: 'none', duration: 2000 });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '保存失败，请稍后重试', icon: 'none', duration: 2000 });
    }
  }
})