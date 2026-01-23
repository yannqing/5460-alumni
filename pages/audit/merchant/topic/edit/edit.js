const app = getApp()
const { get, post } = require('../../../../../utils/request.js')
const fileUploadUtil = require('../../../../../utils/fileUpload.js')

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
    organizerAvatarUrl: '', // 主办方头像URL
    uploading: false, // 上传状态
    uploadingOrganizerAvatar: false, // 主办方头像上传状态
    
    // 时间显示相关
    startTimeDisplay: '', // 活动开始时间（用于前端展示）
    endTimeDisplay: '', // 活动结束时间（用于前端展示）
    registrationStartTimeDisplay: '', // 报名开始时间（用于前端展示）
    registrationEndTimeDisplay: '', // 报名截止时间（用于前端展示）
    
    // 时间滑动选择器相关配置
    startTimePickerValue: [], // 开始时间选择器的滚动值
    endTimePickerValue: [], // 结束时间选择器的滚动值
    registrationStartTimePickerValue: [], // 报名开始时间选择器的滚动值
    registrationEndTimePickerValue: [], // 报名截止时间选择器的滚动值
    yearList: [], // 年列表
    monthList: [], // 月列表
    dayList: [], // 日列表
    hourList: [], // 时列表
    minuteList: [] // 分列表
  },

  onLoad(options) {
    if (options.activityId) {
      this.setData({
        activityId: options.activityId
      });
      
      // 初始化时间选择器的列数据
      this.initTimePickerData();
      
      // 获取活动详情
      this.getActivityDetail();
    }
  },

  // 返回上一页
  onBack() {
    wx.navigateBack();
  },

  // 初始化时间选择器的列数据
  initTimePickerData() {
    // 生成年列表（2020-2030）
    const yearList = [];
    for (let i = 2020; i <= 2030; i++) {
      yearList.push(i + '年');
    }

    // 生成月列表（1-12）
    const monthList = [];
    for (let i = 1; i <= 12; i++) {
      monthList.push(i + '月');
    }

    // 生成日列表（1-31）
    const dayList = [];
    for (let i = 1; i <= 31; i++) {
      dayList.push(i + '日');
    }

    // 生成时列表（0-23）
    const hourList = [];
    for (let i = 0; i <= 23; i++) {
      hourList.push(i.toString().padStart(2, '0') + '时');
    }

    // 生成分列表（0-59）
    const minuteList = [];
    for (let i = 0; i <= 59; i++) {
      minuteList.push(i.toString().padStart(2, '0') + '分');
    }

    this.setData({
      yearList,
      monthList,
      dayList,
      hourList,
      minuteList
    });
  },

  // 将日期转换为picker-view的滚动值
  getPickerValueFromDate(date) {
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hour = date.getHours();
    const minute = date.getMinutes();

    // 计算各列的索引
    const yearIndex = year - 2020;
    const monthIndex = month - 1;
    const dayIndex = day - 1;
    const hourIndex = hour;
    const minuteIndex = minute;

    return [yearIndex, monthIndex, dayIndex, hourIndex, minuteIndex];
  },

  // 格式化日期为picker支持的格式（ISO格式，用于后端提交）
  formatDateToPicker(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day}T${hours}:${minutes}:00`
  },

  // 格式化日期为显示格式（用于前端展示）
  formatDateToDisplay(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },

  // 获取活动详情
  getActivityDetail() {
    this.setData({ loading: true });
    get(`/activity/${this.data.activityId}`)
      .then(res => {
        if (res.data.code === 200) {
          const activityInfo = res.data.data || {};
          
          // 处理时间数据
          const startTime = activityInfo.startTime ? new Date(activityInfo.startTime) : new Date();
          const endTime = activityInfo.endTime ? new Date(activityInfo.endTime) : new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
          const registrationStartTime = activityInfo.registrationStartTime ? new Date(activityInfo.registrationStartTime) : new Date();
          const registrationEndTime = activityInfo.registrationEndTime ? new Date(activityInfo.registrationEndTime) : new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
          
          this.setData({
            activityInfo: activityInfo,
            formData: {
              activityTitle: activityInfo.activityTitle || '',
              organizerName: activityInfo.organizerName || '',
              organizerAvatar: activityInfo.organizerAvatar || '',
              coverImage: activityInfo.coverImage || '',
              activityImages: activityInfo.activityImages || '',
              description: activityInfo.description || '',
              startTime: activityInfo.startTime || this.formatDateToPicker(startTime),
              endTime: activityInfo.endTime || this.formatDateToPicker(endTime),
              isSignup: activityInfo.isSignup || 0,
              registrationStartTime: activityInfo.registrationStartTime || this.formatDateToPicker(registrationStartTime),
              registrationEndTime: activityInfo.registrationEndTime || this.formatDateToPicker(registrationEndTime),
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
            organizerAvatarUrl: activityInfo.organizerAvatar || '',
            coverImageList: activityInfo.coverImage ? [{ url: activityInfo.coverImage }] : [],
            activityImageList: activityInfo.activityImages ? JSON.parse(activityInfo.activityImages).map(url => ({ url })) : [],
            
            // 设置时间显示
            startTimeDisplay: this.formatDateToDisplay(startTime),
            endTimeDisplay: this.formatDateToDisplay(endTime),
            registrationStartTimeDisplay: this.formatDateToDisplay(registrationStartTime),
            registrationEndTimeDisplay: this.formatDateToDisplay(registrationEndTime),
            
            // 设置时间选择器的滚动值
            startTimePickerValue: this.getPickerValueFromDate(startTime),
            endTimePickerValue: this.getPickerValueFromDate(endTime),
            registrationStartTimePickerValue: this.getPickerValueFromDate(registrationStartTime),
            registrationEndTimePickerValue: this.getPickerValueFromDate(registrationEndTime)
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
      [`formData.isSignup`]: parseInt(e.detail.value)
    });
  },

  // 选择是否需要审核
  onIsNeedReviewChange(e) {
    this.setData({
      [`formData.isNeedReview`]: parseInt(e.detail.value)
    });
  },

  // 开始时间滑动选择事件
  onStartTimePickerChange(e) {
    const val = e.detail.value;
    this.setData({ startTimePickerValue: val });

    // 解析选择的时间
    const year = 2020 + val[0];
    const month = val[1] + 1;
    const day = val[2] + 1;
    const hour = val[3];
    const minute = val[4];

    // 格式化时间字符串（用于前端展示）
    const displayTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    // 格式化时间字符串（ISO格式，用于后端提交）
    const isoTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`;
    this.setData({ 'formData.startTime': isoTimeStr, startTimeDisplay: displayTimeStr });
  },

  // 结束时间滑动选择事件
  onEndTimePickerChange(e) {
    const val = e.detail.value;
    this.setData({ endTimePickerValue: val });

    // 解析选择的时间
    const year = 2020 + val[0];
    const month = val[1] + 1;
    const day = val[2] + 1;
    const hour = val[3];
    const minute = val[4];

    // 格式化时间字符串（用于前端展示）
    const displayTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    // 格式化时间字符串（ISO格式，用于后端提交）
    const isoTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`;
    this.setData({ 'formData.endTime': isoTimeStr, endTimeDisplay: displayTimeStr });
  },

  // 报名开始时间滑动选择事件
  onRegistrationStartTimePickerChange(e) {
    const val = e.detail.value;
    this.setData({ registrationStartTimePickerValue: val });

    // 解析选择的时间
    const year = 2020 + val[0];
    const month = val[1] + 1;
    const day = val[2] + 1;
    const hour = val[3];
    const minute = val[4];

    // 格式化时间字符串（用于前端展示）
    const displayTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    // 格式化时间字符串（ISO格式，用于后端提交）
    const isoTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`;
    this.setData({ 'formData.registrationStartTime': isoTimeStr, registrationStartTimeDisplay: displayTimeStr });
  },

  // 报名截止时间滑动选择事件
  onRegistrationEndTimePickerChange(e) {
    const val = e.detail.value;
    this.setData({ registrationEndTimePickerValue: val });

    // 解析选择的时间
    const year = 2020 + val[0];
    const month = val[1] + 1;
    const day = val[2] + 1;
    const hour = val[3];
    const minute = val[4];

    // 格式化时间字符串（用于前端展示）
    const displayTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    // 格式化时间字符串（ISO格式，用于后端提交）
    const isoTimeStr = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`;
    this.setData({ 'formData.registrationEndTime': isoTimeStr, registrationEndTimeDisplay: displayTimeStr });
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

  // 上传主办方头像
  onUploadOrganizerAvatar() {
    // 选择图片
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 设置上传中状态
        this.setData({
          uploadingOrganizerAvatar: true
        })
        
        // 上传图片到服务器
        fileUploadUtil.uploadImage(tempFilePath, '/file/upload/images')
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              // 上传成功，更新表单数据
              this.setData({
                organizerAvatarUrl: res.data.fileUrl,
                [`formData.organizerAvatar`]: res.data.fileUrl
              })
              wx.showToast({
                title: '上传成功',
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: res.msg || '上传失败',
                icon: 'none'
              })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none'
            })
            console.error('上传主办方头像失败:', err)
          })
          .finally(() => {
            // 清除上传中状态
            this.setData({
              uploadingOrganizerAvatar: false
            })
            wx.hideLoading()
          })
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
      }
    })
  },

  // 删除主办方头像
  onDeleteOrganizerAvatar() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除主办方头像吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            organizerAvatarUrl: '',
            [`formData.organizerAvatar`]: ''
          })
        }
      }
    })
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