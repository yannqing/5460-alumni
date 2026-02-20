// pages/audit/carousel/edit/edit.js
const app = getApp()
const { bannerApi } = require('../../../../api/api.js')
const fileUploadUtil = require('../../../../utils/fileUpload.js')

Page({
  data: {
    // 页面标题
    pageTitle: '编辑轮播图',
    // 提交按钮文本
    submitText: '更新',
    // 表单数据
    formData: {
      bannerId: 0,
      bannerTitle: '',
      bannerImage: '',
      bannerType: 1,
      linkUrl: '',
      relatedId: '',
      relatedType: '',
      sortOrder: 0,
      bannerStatus: 1,
      startTime: '',
      endTime: '',
      description: ''
    },
    // 图片文件信息
    imageFile: null,
    // 加载状态
    loading: false,
    // 索引值
    selectedStatusIndex: 0,
    // 状态选项
    statusOptions: [
      { value: 1, label: '启用' },
      { value: 0, label: '禁用' }
    ],
    // 时间显示相关
    startTimeDisplay: '', // 生效开始时间（用于前端展示）
    endTimeDisplay: '', // 生效结束时间（用于前端展示）
    
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
    // 初始化时间选择器的列数据
    this.initTimePickerData();
    
    if (options.bannerId) {
      // 编辑模式，填充表单数据
      const formData = {
        bannerId: options.bannerId, // 保持字符串类型，避免大整数精度丢失
        bannerTitle: decodeURIComponent(options.bannerTitle || ''),
        bannerImage: options.bannerImageFileId || '', // 保持字符串类型，避免大整数精度丢失
        bannerType: parseInt(options.bannerType) || 1,
        linkUrl: decodeURIComponent(options.linkUrl || ''),
        relatedId: options.relatedId ? parseInt(options.relatedId) : '',
        relatedType: decodeURIComponent(options.relatedType || ''),
        sortOrder: parseInt(options.sortOrder || 0),
        bannerStatus: parseInt(options.bannerStatus || 1),
        startTime: decodeURIComponent(options.startTime || ''),
        endTime: decodeURIComponent(options.endTime || ''),
        description: decodeURIComponent(options.description || '')
      };
      
      // 解析时间字符串为Date对象
      const startTime = formData.startTime ? new Date(formData.startTime) : new Date();
      const endTime = formData.endTime ? new Date(formData.endTime) : new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
      
      // 格式化时间（用于前端展示）
      const startTimeDisplay = formData.startTime ? `${startTime.getFullYear()}-${String(startTime.getMonth() + 1).padStart(2, '0')}-${String(startTime.getDate()).padStart(2, '0')} ${String(startTime.getHours()).padStart(2, '0')}:${String(startTime.getMinutes()).padStart(2, '0')}` : '';
      const endTimeDisplay = formData.endTime ? `${endTime.getFullYear()}-${String(endTime.getMonth() + 1).padStart(2, '0')}-${String(endTime.getDate()).padStart(2, '0')} ${String(endTime.getHours()).padStart(2, '0')}:${String(endTime.getMinutes()).padStart(2, '0')}` : '';
      
      // 初始化图片文件信息
      const imageFile = options.bannerImageFileId && options.bannerImageFileUrl ? {
        fileId: options.bannerImageFileId,
        fileUrl: decodeURIComponent(options.bannerImageFileUrl)
      } : null;
      
      this.setData({
        formData: formData,
        imageFile: imageFile,
        startTimeDisplay: startTimeDisplay,
        endTimeDisplay: endTimeDisplay,
        selectedStatusIndex: formData.bannerStatus === 1 ? 0 : 1,
        startTimePickerValue: this.getPickerValueFromDate(startTime),
        endTimePickerValue: this.getPickerValueFromDate(endTime)
      });
    }
  },

  // 输入框输入事件
  onInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`formData.${field}`]: e.detail.value
    })
  },

  // 状态选择事件
  onStatusChange(e) {
    // 获取用户选择的索引
    const selectedIndex = e.detail.value
    // 根据索引获取对应的状态值
    const selectedStatus = this.data.statusOptions[selectedIndex].value
    
    // 更新数据
    this.setData({
      'formData.bannerStatus': selectedStatus,
      selectedStatusIndex: selectedIndex
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

  // 根据日期获取时间选择器的滚动值
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

  // 格式化日期为ISO格式，用于后端提交
  formatDateToPicker(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day}T${hours}:${minutes}:00`
  },

  // 生效开始时间滑动选择事件
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

  // 生效结束时间滑动选择事件
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

  // 上传图片
  uploadImage() {
    // 选择图片
    wx.chooseImage({
      count: 1, // 只允许选择1张图片
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        
        // 显示加载状态
        wx.showLoading({
          title: '上传中...',
          mask: true
        })
        
        // 直接调用文件上传工具类的uploadImage方法
        fileUploadUtil.uploadImage(tempFilePath, '/file/upload/images')
          .then(res => {
            if (res.code === 200 && res.data && res.data.fileId) {
              // 更新表单数据
              this.setData({
                'formData.bannerImage': res.data.fileId,
                imageFile: res.data
              })
              
              wx.showToast({
                title: '图片上传成功',
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

  // 提交表单
  submitForm() {
    const { formData } = this.data
    
    // 强制设置跳转类型为无跳转
    formData.bannerType = 1
    
    // 表单验证
    if (!formData.bannerTitle) {
      wx.showToast({
        title: '请输入轮播图标题',
        icon: 'none'
      })
      return
    }
    
    if (!formData.bannerImage) {
      wx.showToast({
        title: '请上传轮播图图片',
        icon: 'none'
      })
      return
    }
    
    // 处理关联业务ID和类型
    if (formData.relatedId) {
      formData.relatedId = parseInt(formData.relatedId)
    } else {
      delete formData.relatedId
    }
    
    // 处理排序
    if (formData.sortOrder) {
      formData.sortOrder = parseInt(formData.sortOrder)
    }
    
    // 处理图片ID - 直接使用字符串类型，避免大数值转换导致精度丢失
    this.setData({ loading: true })
    
    // 调用更新轮播图API
    bannerApi.updateBanner(formData).then(res => {
      this.setData({ loading: false })
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        })
        
        // 延时返回上一页
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: res.data.msg || '更新失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      this.setData({ loading: false })
      console.error('更新轮播图失败:', err)
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
    })
  }
})