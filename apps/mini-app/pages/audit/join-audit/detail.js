const { joinApplicationApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const app = getApp()

Page({
  data: {
    applicationId: '',
    detail: null,
    loading: false,
    reviewing: false
  },

  onLoad(options) {
    if (options.applicationId) {
      this.setData({
        applicationId: options.applicationId
      })
      this.loadDetail()
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  // 加载申请详情
  async loadDetail() {
    try {
      this.setData({ loading: true })
      const res = await joinApplicationApi.getApplicationDetail(this.data.applicationId)
      
      if (res.data && res.data.code === 200 && res.data.data) {
        const detail = res.data.data
        
        // 格式化时间
        if (detail.applyTime) {
          detail.applyTime = detail.applyTime.replace('T', ' ')
        }
        if (detail.reviewTime) {
          detail.reviewTime = detail.reviewTime.replace('T', ' ')
        }
        
        // 处理附件 URL
        if (detail.attachmentFiles && detail.attachmentFiles.length > 0) {
          detail.attachmentFiles = detail.attachmentFiles.map(file => {
            file.fileUrl = config.getImageUrl(file.fileUrl)
            return file
          })
        }
        
        this.setData({
          detail: detail
        })
      } else {
        wx.showToast({
          title: res.data?.msg || '获取详情失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('[Debug] 获取详情异常:', error)
      wx.showToast({
        title: '网络异常，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 预览图片
  previewImage(e) {
    const url = e.currentTarget.dataset.url
    const urls = this.data.detail.attachmentFiles.map(f => f.fileUrl)
    wx.previewImage({
      current: url,
      urls: urls
    })
  },

  // 通过申请
  async passApplication() {
    await this.submitReview(1)
  },

  // 拒绝申请
  async rejectApplication() {
    // 弹出输入框输入审核意见
    wx.showModal({
      title: '拒绝申请',
      placeholderText: '请输入审核意见',
      editable: true,
      success: async (res) => {
        if (res.confirm) {
          await this.submitReview(2, res.content)
        }
      }
    })
  },

  // 提交审核
  async submitReview(reviewResult, reviewComment = '') {
    if (this.data.reviewing) return
    
    try {
      this.setData({ reviewing: true })
      
      const params = {
        applicationId: this.data.applicationId,
        reviewResult: reviewResult,
        reviewComment: reviewComment
      }
      
      const res = await joinApplicationApi.reviewApplication(params)
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: reviewResult === 1 ? '通过成功' : '拒绝成功',
          icon: 'success',
          duration: 2000
        })
        // 延时刷新或返回
        setTimeout(() => {
          this.loadDetail()
          // 通知列表页刷新
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage && prevPage.loadApplicationList) {
            prevPage.loadApplicationList(true)
          }
        }, 1500)
      } else {
        wx.showToast({
          title: res.data?.msg || '审核失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('[Debug] 提交审核异常:', error)
      wx.showToast({
        title: '网络异常，请重试',
        icon: 'none'
      })
    } finally {
      this.setData({ reviewing: false })
    }
  }
})