// pages/audit/carousel/create/create.js
const app = getApp()
const { bannerApi } = require('../../../../api/api.js')
const fileUploadUtil = require('../../../../utils/fileUpload.js')

Page({
  data: {
    // 表单数据
    formData: {
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
    selectedTypeIndex: 0,
    selectedStatusIndex: 0,
    // 类型选项
    typeOptions: [
      { value: 1, label: '无跳转' },
      { value: 2, label: '内部路径' },
      { value: 3, label: '第三方链接' },
      { value: 4, label: '文章详情' }
    ],
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
    
    // 新增模式，初始化默认时间
    const now = new Date();
    const endDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    
    // 格式化时间（用于前端展示）
    const nowDisplay = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    const endDisplay = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')} ${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`;
    
    // 设置默认值
    this.setData({
      // 时间默认值
      'formData.startTime': this.formatDateToPicker(now),
      'formData.endTime': this.formatDateToPicker(endDate),
      startTimeDisplay: nowDisplay,
      endTimeDisplay: endDisplay,
      // 类型默认值 - 固定为无跳转
      'formData.bannerType': 1,
      // 状态默认值
      'formData.bannerStatus': 1,
      selectedStatusIndex: 0,
      // 初始化滚动值
      startTimePickerValue: this.getPickerValueFromDate(now),
      endTimePickerValue: this.getPickerValueFromDate(endDate)
    });
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
    
    // 打印日志，用于调试
    console.log('状态已更新:', selectedStatus, '索引:', selectedIndex)
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
              // 处理图片URL，确保是完整的URL
              const config = require('../../../../utils/config.js')
              const processedImageFile = {
                ...res.data,
                fileUrl: config.getImageUrl(res.data.fileUrl)
              }
              
              // 更新表单数据
              this.setData({
                'formData.bannerImage': res.data.fileId,
                imageFile: processedImageFile
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

  // 删除图片
  onDeleteImage() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这张图片吗？',
      success: (res) => {
        if (res.confirm) {
          // 清空图片数据
          this.setData({
            'formData.bannerImage': '',
            imageFile: null
          })
          
          wx.showToast({
            title: '图片已删除',
            icon: 'success'
          })
        }
      }
    })
  },

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
    
    // 新增模式，调用新增接口
    bannerApi.createBanner(formData).then(res => {
      this.setData({ loading: false })
      
      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: '新增成功',
          icon: 'success'
        })
        
        // 延时返回上一页
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: res.data.msg || '新增失败',
          icon: 'none'
        })
      }
    }).catch(err => {
      this.setData({ loading: false })
      console.error('新增轮播图失败:', err)
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
    })
  }
})