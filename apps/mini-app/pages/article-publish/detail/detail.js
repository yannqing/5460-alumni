const { homeArticleApi, articleApplyApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    id: '',
    article: null,
    loading: true,
    canEdit: false,
    canReview: false, // 是否有审核权限（只有系统管理员才能审核）
    // 审核弹框相关
    isApprovalModalVisible: false,
    approvalAction: 1, // 1-通过，2-拒绝
    approvalForm: {
      homeArticleApplyId: '',
      applyStatus: 1,
      appliedDescription: ''
    }
  },

  onLoad(options) {
    // 检查审核权限
    this.checkReviewPermission()

    if (options.id) {
      this.setData({
        id: options.id,
        canEdit: options.from === 'manage'
      })
      this.loadArticleDetail(options.id)
    } else {
      wx.showToast({
        title: '缺少文章ID',
        icon: 'none'
      })
      this.setData({ loading: false })
    }
  },

  // 检查用户是否有特定权限
  hasPermission(permissionCode) {
    const originalRoles = wx.getStorageSync('roles') || []

    for (const role of originalRoles) {
      if (role.permissions && Array.isArray(role.permissions)) {
        for (const permission of role.permissions) {
          if (permission.code === permissionCode) {
            return true
          }
          if (permission.children && Array.isArray(permission.children)) {
            for (const childPermission of permission.children) {
              if (childPermission.code === permissionCode) {
                return true
              }
            }
          }
        }
      }
    }
    return false
  },

  // 检查审核权限
  checkReviewPermission() {
    // 只有拥有文章审核权限的用户才能看到通过/拒绝按钮
    const canReview = this.hasPermission('HOME_PAGE_ARTICLE_REVIEW')
    this.setData({ canReview })
  },

  goToEdit() {
    wx.navigateTo({
      url: `/pages/article/form/form?id=${this.data.id}`
    })
  },

  goBack() {
    wx.navigateBack()
  },

  // 获取审核状态信息
  getApplyStatusInfo(applyStatus) {
    let applyStatusText = ''
    let applyStatusClass = ''

    if (applyStatus === 0) {
      applyStatusText = '待审核'
      applyStatusClass = 'status-pending'
    } else if (applyStatus === 1) {
      applyStatusText = '已通过'
      applyStatusClass = 'status-approved'
    } else if (applyStatus === 2) {
      applyStatusText = '已拒绝'
      applyStatusClass = 'status-rejected'
    }

    return { applyStatusText, applyStatusClass }
  },

  // 格式化时间
  formatTime(timeStr) {
    if (!timeStr) return ''
    return timeStr.replace('T', ' ')
  },

  loadArticleDetail(id) {
    // 确保ID有效
    if (!id || id === 'undefined' || id === 'null' || id === '') {
      wx.showToast({ title: '文章ID错误', icon: 'none' })
      this.setData({ loading: false })
      return
    }

    this.setData({ loading: true })
    homeArticleApi.getHomeArticleDetail(id).then(res => {
      if (res.data && (res.data.code === 200 || res.data.code === 0)) {
        const article = res.data.data
        if (!article) {
          wx.showToast({
            title: '文章不存在',
            icon: 'none'
          })
          this.setData({ loading: false })
          return
        }

        // 字段映射
        article.title = article.articleTitle || article.title || '无标题'
        article.author = article.publishUsername || article.author || article.groupName || article.source || '官方发布'

        // 处理时间格式
        article.time = this.formatTime(article.createTime || article.time)
        article.createTime = article.time

        // 处理更新时间格式
        if (article.updateTime) {
          article.updateTime = this.formatTime(article.updateTime)
        }

        article.content = article.content || article.description || ''

        // 处理封面图
        if (article.coverImg && !article.cover) {
          if (typeof article.coverImg === 'object') {
            article.cover = config.getImageUrl(article.coverImg.fileUrl || article.coverImg.thumbnailUrl || '')
          } else {
            article.cover = config.getImageUrl(`/file/download/${article.coverImg}`)
          }
        }

        // 处理发布者头像
        let publisherAvatar = ''
        if (article.publisherAvatar) {
          if (typeof article.publisherAvatar === 'object') {
            publisherAvatar = article.publisherAvatar.fileUrl || article.publisherAvatar.thumbnailUrl || article.publisherAvatar.url || ''
          } else {
            publisherAvatar = article.publisherAvatar
          }
        } else if (article.publishAvatar) {
          if (typeof article.publishAvatar === 'object') {
            publisherAvatar = article.publishAvatar.fileUrl || article.publishAvatar.thumbnailUrl || article.publishAvatar.url || ''
          } else {
            publisherAvatar = article.publishAvatar
          }
        }

        if (publisherAvatar) {
          article.publisherAvatar = config.getImageUrl(publisherAvatar)
        }

        // 处理审核状态
        const statusInfo = this.getApplyStatusInfo(article.applyStatus)
        article.applyStatusText = statusInfo.applyStatusText
        article.applyStatusClass = statusInfo.applyStatusClass

        // 处理富文本中的图片宽度
        if (article.content && typeof article.content === 'string') {
          article.content = article.content.replace(/<img/g, '<img style="max-width:100%;height:auto;display:block;border-radius:8rpx;margin:16rpx 0;"')
        }

        // 处理子文章列表
        if (article.children && article.children.length > 0) {
          article.children = article.children.map(child => {
            // 格式化子文章时间
            child.createTimeFormatted = this.formatTime(child.createTime)

            // 处理子文章封面图
            if (child.coverImg) {
              if (typeof child.coverImg === 'object') {
                child.coverUrl = config.getImageUrl(child.coverImg.fileUrl || child.coverImg.thumbnailUrl || '')
              } else {
                child.coverUrl = config.getImageUrl(`/file/download/${child.coverImg}`)
              }
            }

            // 处理子文章审核状态
            const childStatusInfo = this.getApplyStatusInfo(child.applyStatus)
            child.applyStatusText = childStatusInfo.applyStatusText
            child.applyStatusClass = childStatusInfo.applyStatusClass

            return child
          })
        }

        this.setData({
          article: article,
          loading: false
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '获取详情失败',
          icon: 'none',
          duration: 2000
        })
        this.setData({ loading: false })
      }
    }).catch(err => {
      console.error('加载文章详情失败:', err)
      wx.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none',
        duration: 2000
      })
      this.setData({ loading: false })
    })
  },

  // 查看子文章详情
  viewChildDetail(e) {
    const { id } = e.currentTarget.dataset
    if (id) {
      wx.navigateTo({
        url: `/pages/article-publish/detail/detail?id=${id}&from=manage`
      })
    }
  },

  // 预览图片
  previewImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      current: url,
      urls: [url]
    })
  },

  // 打开链接
  openLink(e) {
    const link = e.currentTarget.dataset.link
    if (link) {
      wx.setClipboardData({
        data: link,
        success: () => {
          wx.showToast({
            title: '链接已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  // 点击编辑按钮
  onEditTap() {
    wx.navigateTo({
      url: `/pages/article/form/form?id=${this.data.id}`
    })
  },

  // 点击删除按钮
  onDeleteTap() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这篇文章吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' })

            const deleteRes = await homeArticleApi.deleteArticle(this.data.id)

            wx.hideLoading()

            if (deleteRes.data && deleteRes.data.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              // 返回上一页
              setTimeout(() => {
                wx.navigateBack()
              }, 1500)
            } else {
              wx.showToast({
                title: deleteRes.data?.msg || '删除失败',
                icon: 'none'
              })
            }
          } catch (error) {
            wx.hideLoading()
            console.error('删除文章失败:', error)
            wx.showToast({
              title: '删除失败，请重试',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 点击通过按钮
  onApproveTap() {
    // 需要获取审核记录ID
    this.getApplyIdAndShowModal(1)
  },

  // 点击拒绝按钮
  onRejectTap() {
    this.getApplyIdAndShowModal(2)
  },

  // 获取审核记录ID并显示弹框
  async getApplyIdAndShowModal(action) {
    try {
      wx.showLoading({ title: '加载中...' })

      // 获取该文章的审核记录
      const applyRes = await articleApplyApi.getApplyPage({
        current: 1,
        size: 100
      })

      wx.hideLoading()

      if (applyRes.data && applyRes.data.code === 200) {
        const applyRecords = applyRes.data.data?.records || []
        // 找到当前文章对应的审核记录
        const applyRecord = applyRecords.find(item =>
          item.articleInfo?.homeArticleId === this.data.id ||
          item.homeArticleId === this.data.id
        )

        if (applyRecord) {
          this.showApprovalModal(applyRecord.homeArticleApplyId, action)
        } else {
          wx.showToast({
            title: '未找到审核记录',
            icon: 'none'
          })
        }
      } else {
        wx.showToast({
          title: '获取审核记录失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('获取审核记录失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
  },

  // 显示审核弹框
  showApprovalModal(applyId, action) {
    this.setData({
      isApprovalModalVisible: true,
      approvalAction: action,
      approvalForm: {
        homeArticleApplyId: applyId,
        applyStatus: action,
        appliedDescription: ''
      }
    })
  },

  // 隐藏审核弹框
  hideApprovalModal() {
    this.setData({
      isApprovalModalVisible: false,
      approvalForm: {
        homeArticleApplyId: '',
        applyStatus: 1,
        appliedDescription: ''
      }
    })
  },

  // 处理审核表单输入
  onApprovalInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    this.setData({
      [`approvalForm.${field}`]: value
    })
  },

  // 提交审核
  async submitApproval() {
    try {
      wx.showLoading({ title: '提交中...' })

      const { approvalForm } = this.data

      // 调用审核API
      const res = await articleApplyApi.approveArticle(approvalForm)

      wx.hideLoading()

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: approvalForm.applyStatus === 1 ? '审核通过成功' : '审核拒绝成功',
          icon: 'success'
        })

        // 隐藏弹框
        this.hideApprovalModal()

        // 重新加载详情
        this.loadArticleDetail(this.data.id)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('提交审核失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    }
  }
})
