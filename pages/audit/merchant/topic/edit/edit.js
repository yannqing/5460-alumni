const app = getApp()
const { get, post } = require('../../../../../utils/request.js')

Page({
  data: {
    activityId: '', // 活动ID
    activityInfo: {}, // 活动详情
    loading: false, // 加载状态
    submitting: false, // 提交状态
    
    // 表单数据
    formData: {
      activityTitle: '',
      organizerName: '',
      organizerAvatar: '',
      coverImage: '',
      activityImages: '',
      description: '',
      startTime: '',
      endTime: '',
      isSignup: 0,
      registrationStartTime: '',
      registrationEndTime: '',
      province: '',
      city: '',
      district: '',
      address: '',
      latitude: '',
      longitude: '',
      maxParticipants: '',
      isNeedReview: 0,
      contactPerson: '',
      contactPhone: '',
      contactEmail: ''
    },
    
    // 图片上传相关
    coverImageList: [], // 封面图列表
    activityImageList: [], // 活动图片列表
    uploading: false // 上传状态
  },

  onLoad(options) {
    if (options.activityId) {
      this.setData({
        activityId: options.activityId
      });
      this.getActivityDetail();
    }
  },

  // 返回上一页
  onBack() {
    wx.navigateBack();
  },

  // 获取活动详情
  getActivityDetail() {
    this.setData({ loading: true });
    get(`/activity/${this.data.activityId}`)
      .then(res => {
        if (res.data.code === 200) {
          const activityInfo = res.data.data || {};
          this.setData({
            activityInfo: activityInfo,
            formData: {
              activityTitle: activityInfo.activityTitle || '',
              organizerName: activityInfo.organizerName || '',
              organizerAvatar: activityInfo.organizerAvatar || '',
              coverImage: activityInfo.coverImage || '',
              activityImages: activityInfo.activityImages || '',
              description: activityInfo.description || '',
              startTime: activityInfo.startTime || '',
              endTime: activityInfo.endTime || '',
              isSignup: activityInfo.isSignup || 0,
              registrationStartTime: activityInfo.registrationStartTime || '',
              registrationEndTime: activityInfo.registrationEndTime || '',
              province: activityInfo.province || '',
              city: activityInfo.city || '',
              district: activityInfo.district || '',
              address: activityInfo.address || '',
              latitude: activityInfo.latitude || '',
              longitude: activityInfo.longitude || '',
              maxParticipants: activityInfo.maxParticipants || '',
              isNeedReview: activityInfo.isNeedReview || 0,
              contactPerson: activityInfo.contactPerson || '',
              contactPhone: activityInfo.contactPhone || '',
              contactEmail: activityInfo.contactEmail || ''
            },
            coverImageList: activityInfo.coverImage ? [{ url: activityInfo.coverImage }] : [],
            activityImageList: activityInfo.activityImages ? JSON.parse(activityInfo.activityImages).map(url => ({ url })) : []
          });
        }
      })
      .catch(err => {
        console.error('获取活动详情失败:', err);
        wx.showToast({
          title: '获取活动详情失败',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  // 表单输入处理
  onInput(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`formData.${field}`]: value
    });
  },

  // 选择是否需要报名
  onIsSignupChange(e) {
    this.setData({
      [`formData.isSignup`]: e.detail.value
    });
  },

  // 选择是否需要审核
  onIsNeedReviewChange(e) {
    this.setData({
      [`formData.isNeedReview`]: e.detail.value
    });
  },

  // 选择时间
  onTimeChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [`formData.${field}`]: e.detail.value
    });
  },

  // 上传封面图
  onCoverImageUpload() {
    this.uploadImage('cover');
  },

  // 上传活动图片
  onActivityImageUpload() {
    this.uploadImage('activity');
  },

  // 上传图片
  uploadImage(type) {
    const that = this;
    
    wx.chooseImage({
      count: type === 'cover' ? 1 : 9, // 封面图只能上传1张，活动图片最多9张
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePaths = res.tempFilePaths;
        that.setData({ uploading: true });
        
        // 这里需要根据实际项目的上传接口进行修改
        // 模拟上传成功
        setTimeout(() => {
          if (type === 'cover') {
            that.setData({
              coverImageList: tempFilePaths.map(path => ({ url: path })),
              [`formData.coverImage`]: tempFilePaths[0],
              uploading: false
            });
          } else {
            const activityImageList = [...that.data.activityImageList, ...tempFilePaths.map(path => ({ url: path }))];
            that.setData({
              activityImageList: activityImageList,
              [`formData.activityImages`]: JSON.stringify(activityImageList.map(item => item.url)),
              uploading: false
            });
          }
        }, 1000);
      },
      fail(err) {
        console.error('选择图片失败:', err);
        that.setData({ uploading: false });
      }
    });
  },

  // 删除图片
  onDeleteImage(e) {
    const { type, index } = e.currentTarget.dataset;
    
    if (type === 'cover') {
      this.setData({
        coverImageList: [],
        [`formData.coverImage`]: ''
      });
    } else {
      const activityImageList = [...this.data.activityImageList];
      activityImageList.splice(index, 1);
      this.setData({
        activityImageList: activityImageList,
        [`formData.activityImages`]: JSON.stringify(activityImageList.map(item => item.url))
      });
    }
  },

  // 表单验证
  validateForm() {
    const { formData } = this.data;
    
    if (!formData.activityTitle.trim()) {
      wx.showToast({
        title: '请输入活动标题',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.organizerName.trim()) {
      wx.showToast({
        title: '请输入主办方名称',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.coverImage) {
      wx.showToast({
        title: '请上传活动封面图',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.description.trim()) {
      wx.showToast({
        title: '请输入活动详情描述',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.startTime) {
      wx.showToast({
        title: '请选择活动开始时间',
        icon: 'none'
      });
      return false;
    }
    
    if (!formData.endTime) {
      wx.showToast({
        title: '请选择活动结束时间',
        icon: 'none'
      });
      return false;
    }
    
    if (new Date(formData.endTime) <= new Date(formData.startTime)) {
      wx.showToast({
        title: '活动结束时间不能早于开始时间',
        icon: 'none'
      });
      return false;
    }
    
    if (formData.isSignup === 1) {
      if (!formData.registrationStartTime) {
        wx.showToast({
          title: '请选择报名开始时间',
          icon: 'none'
        });
        return false;
      }
      
      if (!formData.registrationEndTime) {
        wx.showToast({
          title: '请选择报名截止时间',
          icon: 'none'
        });
        return false;
      }
      
      if (new Date(formData.registrationEndTime) <= new Date(formData.registrationStartTime)) {
        wx.showToast({
          title: '报名截止时间不能早于开始时间',
          icon: 'none'
        });
        return false;
      }
    }
    
    return true;
  },

  // 提交表单
  onSubmit() {
    if (this.data.submitting) {
      return;
    }
    
    if (!this.validateForm()) {
      return;
    }
    
    this.setData({ submitting: true });
    
    const submitData = {
      ...this.data.formData,
      activityId: this.data.activityId
    };
    
    // 处理空值和类型转换
    if (submitData.maxParticipants === '') {
      submitData.maxParticipants = null;
    } else {
      submitData.maxParticipants = parseInt(submitData.maxParticipants);
    }
    
    post('/activity/update', submitData)
      .then(res => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '编辑成功',
            icon: 'success'
          });
          
          // 返回上一页
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else {
          wx.showToast({
            title: res.data.msg || '编辑失败',
            icon: 'none'
          });
        }
      })
      .catch(err => {
        console.error('编辑活动失败:', err);
        wx.showToast({
          title: '编辑失败',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({ submitting: false });
      });
  }
});