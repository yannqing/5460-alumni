// pages/feedback/feedback.js
const { feedbackApi } = require('../../api/api.js')

Page({
  data: {
    // 反馈类型选项
    feedbackTypes: [
      { value: 1, label: '数据问题' },
      { value: 2, label: '功能建议' },
      { value: 3, label: 'Bug反馈' },
      { value: 4, label: '使用问题' },
      { value: 5, label: '其他' }
    ],
    // 表单数据
    formData: {
      feedbackType: 1,
      feedbackTitle: '',
      feedbackContent: '',
      contactInfo: ''
    },
    submitting: false,
    isFormValid: false
  },

  onLoad(options) {
    // 如果有传入默认类型，则设置
    if (options.type) {
      const type = parseInt(options.type)
      if (type >= 1 && type <= 5) {
        this.setData({
          'formData.feedbackType': type
        })
      }
    }
    // 如果有传入默认标题，则设置
    if (options.title) {
      this.setData({
        'formData.feedbackTitle': decodeURIComponent(options.title)
      })
    }
    // 初始验证表单
    this.validateForm()
  },

  // 选择反馈类型
  selectType(e) {
    const { value } = e.currentTarget.dataset
    this.setData({
      'formData.feedbackType': value
    })
    this.validateForm()
  },

  // 标题输入
  onTitleInput(e) {
    this.setData({
      'formData.feedbackTitle': e.detail.value
    })
    this.validateForm()
  },

  // 内容输入
  onContentInput(e) {
    this.setData({
      'formData.feedbackContent': e.detail.value
    })
    this.validateForm()
  },

  // 联系方式输入
  onContactInput(e) {
    this.setData({
      'formData.contactInfo': e.detail.value
    })
  },

  // 验证表单
  validateForm() {
    const { feedbackType, feedbackTitle, feedbackContent } = this.data.formData
    const isValid = feedbackType && feedbackTitle.trim() && feedbackContent.trim()
    this.setData({ isFormValid: isValid })
  },

  // 提交反馈
  async submitFeedback() {
    const { formData, submitting } = this.data

    // 防止重复提交
    if (submitting) {
      return
    }

    // 表单验证
    if (!formData.feedbackType) {
      wx.showToast({
        title: '请选择反馈类型',
        icon: 'none'
      })
      return
    }

    if (!formData.feedbackTitle.trim()) {
      wx.showToast({
        title: '请输入反馈标题',
        icon: 'none'
      })
      return
    }

    if (!formData.feedbackContent.trim()) {
      wx.showToast({
        title: '请输入详细描述',
        icon: 'none'
      })
      return
    }

    this.setData({ submitting: true })

    try {
      const res = await feedbackApi.submit({
        feedbackType: formData.feedbackType,
        feedbackTitle: formData.feedbackTitle.trim(),
        feedbackContent: formData.feedbackContent.trim(),
        contactInfo: formData.contactInfo.trim() || null
      })

      if (res.data && res.data.code === 200) {
        wx.showModal({
          title: '提交成功',
          content: '感谢您的反馈！我们会尽快处理。',
          showCancel: false,
          confirmText: '我知道了',
          success: () => {
            wx.navigateBack()
          }
        })
      } else {
        const errorMsg = (res.data && res.data.message) || '提交失败，请稍后重试'
        wx.showToast({
          title: errorMsg,
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('[Feedback] 提交反馈失败:', error)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
