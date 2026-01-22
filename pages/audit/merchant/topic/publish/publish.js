// pages/audit/merchant/topic/publish/publish.js
const app = getApp()
const { get, post } = require('../../../../../utils/request.js')
const fileUploadUtil = require('../../../../../utils/fileUpload.js')

Page({
  data: {
    loading: false, // 加载状态
    uploadingOrganizerAvatar: false, // 主办方头像上传状态
    publishForm: {
      activityTitle: '', // 活动标题
      organizerId: '', // 主办方ID（店铺ID）
      organizerName: '', // 主办方名称
      organizerAvatar: '', // 主办方头像
      coverImage: '', // 活动封面图URL
      activityImages: '', // 活动图片URL数组（JSON格式）
      description: '', // 活动详情描述
      startTime: '', // 活动开始时间（字符串格式）
      endTime: '', // 活动结束时间（字符串格式）
      isSignup: 0, // 是否需要报名：0-否 1-是
      isNeedReview: 0, // 是否需要审核：0-无需审核 1-需要审核
      // 地理位置相关
      province: '', // 省份
      city: '', // 城市
      district: '', // 区县
      address: '', // 详细地址
      locationName: '', // 位置名称
      latitude: '', // 纬度
      longitude: '', // 经度
      // 联系人信息
      contactPerson: '', // 联系人
      contactPhone: '', // 联系电话
      contactEmail: '' // 联系邮箱
    },
    imageUrls: [], // 活动图片URL数组（临时存储，用于提交前转换为JSON）
    organizerAvatarUrl: '', // 主办方头像URL（临时存储）
    
    // 时间显示相关
    startTimeDisplay: '', // 活动开始时间（用于前端展示）
    endTimeDisplay: '', // 活动结束时间（用于前端展示）
    
    // 时间滑动选择器相关配置
    startTimePickerValue: [], // 开始时间选择器的滚动值
    endTimePickerValue: [], // 结束时间选择器的滚动值
    yearList: [], // 年列表
    monthList: [], // 月列表
    dayList: [], // 日列表
    hourList: [], // 时列表
    minuteList: [] // 分列表
  },

  onLoad(options) {
    // 接收从topic页面传递的shopId和shopName
    if (options.shopId && options.shopName) {
      this.setData({
        'publishForm.organizerId': options.shopId,
        'publishForm.organizerName': options.shopName
      })
    }

    // 初始化时间选择器的列数据
    this.initTimePickerData();
    // 初始化默认时间
    const now = new Date();
    const endDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    
    // 格式化时间（用于前端展示）
    const nowDisplay = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    const endDisplay = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')} ${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`;
    
    // 设置默认时间
    this.setData({
      'publishForm.startTime': this.formatDateToPicker(now),
      'publishForm.endTime': this.formatDateToPicker(endDate),
      startTimeDisplay: nowDisplay,
      endTimeDisplay: endDisplay,
      // 初始化滚动值
      startTimePickerValue: this.getPickerValueFromDate(now),
      endTimePickerValue: this.getPickerValueFromDate(endDate)
    });
  },

  // 返回上一页
  onBack() {
    wx.navigateBack()
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
    this.setData({ 'publishForm.startTime': isoTimeStr, startTimeDisplay: displayTimeStr });
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
    this.setData({ 'publishForm.endTime': isoTimeStr, endTimeDisplay: displayTimeStr });
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

  // 表单输入变化处理
  onFormInput(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`publishForm.${field}`]: value
    })
  },

  // 切换是否需要报名
  onSignupChange(e) {
    this.setData({
      [`publishForm.isSignup`]: parseInt(e.detail.value)
    })
  },

  // 切换是否需要审核
  onNeedReviewChange(e) {
    this.setData({
      [`publishForm.isNeedReview`]: parseInt(e.detail.value)
    })
  },

  // 上传封面图
  onUploadCover() {
    // 选择图片
    wx.chooseImage({
      count: 9, // 最多可选择9张图片
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 上传多张图片
        const uploadPromises = tempFilePaths.map(filePath => {
          return fileUploadUtil.uploadImage(filePath, '/file/upload/images')
        })
        
        Promise.all(uploadPromises)
          .then(results => {
            // 处理上传结果
            const uploadedUrls = results
              .filter(res => res.code === 200 && res.data && res.data.fileUrl)
              .map(res => res.data.fileUrl)
            
            if (uploadedUrls.length > 0) {
              // 更新图片URL数组
              this.setData({
                imageUrls: [...this.data.imageUrls, ...uploadedUrls],
                [`publishForm.coverImage`]: uploadedUrls[0] // 使用第一张作为封面图
              })
              
              wx.showToast({
                title: `上传成功 ${uploadedUrls.length} 张`,
                icon: 'success'
              })
            } else {
              wx.showToast({
                title: '上传失败',
                icon: 'none'
              })
            }
          })
          .catch(err => {
            wx.showToast({
              title: err.msg || '上传失败',
              icon: 'none'
            })
            console.error('上传图片失败:', err)
          })
          .finally(() => {
            wx.hideLoading()
          })
      },
      fail: (err) => {
        console.error('选择图片失败:', err)
      }
    })
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
                [`publishForm.organizerAvatar`]: res.data.fileUrl
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
            [`publishForm.organizerAvatar`]: ''
          })
        }
      }
    })
  },

  // 删除活动图片
  onDeleteImage(e) {
    const { index } = e.currentTarget.dataset
    const imageUrls = this.data.imageUrls
    imageUrls.splice(index, 1)
    this.setData({
      imageUrls: imageUrls,
      // 如果删除的是封面图，重新设置封面图
      [`publishForm.coverImage`]: imageUrls.length > 0 ? imageUrls[0] : ''
    })
  },



  // 选择位置
  onChooseLocation() {
    // 使用微信地图选择API获取位置信息
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          [`publishForm.locationName`]: res.name,
          [`publishForm.latitude`]: res.latitude.toString(),
          [`publishForm.longitude`]: res.longitude.toString()
        })
        
        wx.showToast({
          title: '位置选择成功',
          icon: 'success'
        })
      },
      fail: (err) => {
        console.error('位置选择失败:', err)
        // 如果用户取消选择，不显示错误提示
        if (err.errMsg !== 'chooseLocation:fail cancel') {
          wx.showToast({
            title: '位置选择失败，请重试',
            icon: 'none'
          })
        }
      }
    })
  },

  // 提交发布话题
  onSubmitPublish() {
    const { publishForm, imageUrls } = this.data
    
    // 表单验证
    if (!publishForm.activityTitle.trim()) {
      wx.showToast({
        title: '请输入活动标题',
        icon: 'none'
      })
      return
    }
    
    if (publishForm.organizerName.length === 0) {
      wx.showToast({
        title: '请输入主办方名称',
        icon: 'none'
      })
      return
    }
    
    if (publishForm.organizerName.length > 200) {
      wx.showToast({
        title: '主办方名称不能超过200个字符',
        icon: 'none'
      })
      return
    }
    
    if (imageUrls.length === 0) {
      wx.showToast({
        title: '请上传至少一张活动图片',
        icon: 'none'
      })
      return
    }
    
    if (!publishForm.description.trim()) {
      wx.showToast({
        title: '请输入活动详情描述',
        icon: 'none'
      })
      return
    }
    
    // 将图片URL数组转换为JSON字符串
    const finalForm = {
      ...publishForm,
      activityImages: JSON.stringify(imageUrls) // 活动图片URL数组（JSON格式）
    }
    
    this.setData({ loading: true })
    
    post('/activity/publishTopic', finalForm)
      .then(res => {
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
        console.error('发布话题失败:', err)
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})