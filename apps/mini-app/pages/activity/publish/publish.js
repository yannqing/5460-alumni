// pages/activity/publish/publish.js
const app = getApp()
const fileUploadUtil = require('../../../utils/fileUpload.js')
const { fileApi, alumniAssociationManagementApi } = require('../../../api/api.js')

Page({
  data: {
    alumniAssociationId: 0,
    alumniAssociationName: '',
    formData: {
      activityTitle: '',
      activityCategory: '',
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
      locationName: '',
      latitude: 0,
      longitude: 0,
      maxParticipants: null,
      isNeedReview: 0,
      contactPerson: '',
      contactPhone: '',
      contactEmail: '',
      isPublic: 1,
      tagsId: '',
      remark: ''
    },
    activityImagesList: [],
    uploadingImages: false,
    isNeedReviewOptions: [
      { id: 0, name: '无需审核' },
      { id: 1, name: '需要审核' }
    ],
    isPublicOptions: [
      { id: 0, name: '不公开' },
      { id: 1, name: '公开' }
    ],
    submitting: false,
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
    // 优先从路由参数获取 associationId
    if (options.associationId) {
      this.setData({
        alumniAssociationId: options.associationId
      })
    }

    const pages = getCurrentPages()
    const prevPage = pages[pages.length - 2]
    if (prevPage) {
      this.setData({
        alumniAssociationId: this.data.alumniAssociationId || prevPage.data.selectedAlumniAssociationId || prevPage.data.associationId,
        alumniAssociationName: prevPage.data.selectedAlumniAssociationName || (prevPage.data.associationInfo ? prevPage.data.associationInfo.name : '')
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
      'formData.startTime': this.formatDateToPicker(now),
      'formData.endTime': this.formatDateToPicker(endDate),
      'formData.registrationStartTime': this.formatDateToPicker(now),
      'formData.registrationEndTime': this.formatDateToPicker(endDate),
      startTimeDisplay: nowDisplay,
      endTimeDisplay: endDisplay,
      registrationStartTimeDisplay: nowDisplay,
      registrationEndTimeDisplay: endDisplay,
      // 初始化滚动值
      startTimePickerValue: this.getPickerValueFromDate(now),
      endTimePickerValue: this.getPickerValueFromDate(endDate),
      registrationStartTimePickerValue: this.getPickerValueFromDate(now),
      registrationEndTimePickerValue: this.getPickerValueFromDate(endDate)
    });
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: e.detail.value
    })
  },

  onPickerChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail

    let selectedValue = value

    // 根据不同字段获取对应的id值
    if (field === 'isNeedReview') {
      const option = this.data.isNeedReviewOptions[value]
      selectedValue = option ? option.id : value
    } else if (field === 'isPublic') {
      const option = this.data.isPublicOptions[value]
      selectedValue = option ? option.id : value
    }

    this.setData({
      [`formData.${field}`]: selectedValue
    })
  },



  onNumberChange(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: e.detail.value ? parseInt(e.detail.value) : null
    })
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

  async submitForm() {
    const { formData, alumniAssociationId } = this.data

    if (!formData.activityTitle || formData.activityTitle.trim() === '') {
      wx.showToast({ title: '请输入活动标题', icon: 'none' })
      return
    }

    if (!formData.coverImage || formData.coverImage.trim() === '') {
      wx.showToast({ title: '请上传活动封面图', icon: 'none' })
      return
    }

    if (!formData.description || formData.description.trim() === '') {
      wx.showToast({ title: '请输入活动详情描述', icon: 'none' })
      return
    }

    if (!formData.startTime || formData.startTime.trim() === '') {
      wx.showToast({ title: '请选择活动开始时间', icon: 'none' })
      return
    }

    if (!formData.endTime || formData.endTime.trim() === '') {
      wx.showToast({ title: '请选择活动结束时间', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    try {
      const activityImages = formData.activityImages.trim() || '[]'

      // 构建提交数据，不需要报名时移除报名时间字段
      const submitData = {
        ...formData,
        activityImages,
        alumniAssociationId
      }

      // 如果不需要报名，删除报名时间相关字段
      if (submitData.isSignup === 0) {
        delete submitData.registrationStartTime
        delete submitData.registrationEndTime
      }

      const res = await this.publishActivity(submitData)

      if (res.data && res.data.code === 200) {
        wx.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: res.data && res.data.msg ? res.data.msg : '发布失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('发布活动失败:', error)
      wx.showToast({ title: '发布失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  publishActivity(data) {
    return alumniAssociationManagementApi.publishActivity(data)
  },

  // 上传封面图
  onUploadCover() {
    // 选择图片
    wx.chooseImage({
      count: 1, // 只允许选择1张图片作为封面
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]

        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })

        // 上传图片
        fileUploadUtil.uploadImage(tempFilePath, '/file/upload/images')
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileUrl) {
              // 更新封面图
              this.setData({
                [`formData.coverImage`]: res.data.fileUrl
              })

              wx.showToast({
                title: '封面图上传成功',
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
            console.error('上传封面图失败:', err)
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

  // 更换封面图
  onChangeCover() {
    this.onUploadCover()
  },

  // 上传活动图片
  onUploadActivityImage() {
    // 选择图片
    wx.chooseImage({
      count: 9 - this.data.activityImagesList.length, // 最多可选择的图片数量，不超过9张
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
          return fileApi.uploadImage(filePath)
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
                activityImagesList: [...this.data.activityImagesList, ...uploadedUrls],
                'formData.activityImages': JSON.stringify([...this.data.activityImagesList, ...uploadedUrls])
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
            console.error('上传活动图片失败:', err)
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

  chooseActivityImages() {
    this.onUploadActivityImage()
  },


  deleteImage(e) {
    const { index } = e.currentTarget.dataset
    const updatedList = this.data.activityImagesList.filter((_, i) => i !== index)
    this.setData({
      activityImagesList: updatedList,
      'formData.activityImages': JSON.stringify(updatedList)
    })
  },

  // 删除封面图
  onDeleteCover() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除活动封面图吗？',
      success: (res) => {
        if (res.confirm) {
          this.setData({
            'formData.coverImage': ''
          })
          wx.showToast({
            title: '封面图已删除',
            icon: 'success'
          })
        }
      }
    })
  },

  // 选择位置
  onChooseLocation() {
    // 使用微信地图选择API获取位置信息
    wx.chooseLocation({
      success: (res) => {
        this.setData({
          [`formData.locationName`]: res.name,
          [`formData.latitude`]: res.latitude,
          [`formData.longitude`]: res.longitude
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
  }
})
