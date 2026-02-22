// pages/profile/my-card/my-card.js
const app = getApp()
const request = require('../../../utils/request');
const { userApi, fileApi } = require('../../../api/api.js')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: null,
    loading: true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.getUserInfo();
  },

  /**
   * 获取用户信息
   */
  getUserInfo() {
    this.setData({ loading: true });
    // 从全局数据获取用户信息，与profile页面保持一致
    const app = getApp();
    let userData = app.globalData.userData || {};
    const userInfo = app.globalData.userInfo || userData;
    
    // 如果全局数据为空，从后端获取
    if (!userData.nickname) {
      request.get('/users/getInfo').then(res => {
        if (res.data && res.data.code === 200) {
          userData = res.data.data;
          console.log('获取到的用户信息:', userData);
          // 更新全局数据
          app.globalData.userData = userData;
          app.globalData.userInfo = userData;
        }
      }).catch(err => {
        console.error('获取用户信息失败:', err);
      }).finally(() => {
        this.processUserData(userData);
      });
    } else {
      this.processUserData(userData);
    }
  },
  
  /**
   * 处理用户数据
   */
  processUserData(userData) {
    // 确保 isAlumni 字段存在，与profile页面保持一致
    const formattedUserInfo = {
      ...userData,
      isAlumni: userData.isAlumni || 0
    };
    console.log('处理后的用户信息:', formattedUserInfo);
    console.log('isAlumni值:', formattedUserInfo.isAlumni);
    this.setData({ 
      userInfo: formattedUserInfo,
      loading: false 
    });
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.getUserInfo();
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.getUserInfo().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },
  
  /**
   * 编辑资料
   */
  editProfile() {
    wx.navigateTo({
      url: '/pages/profile/edit/edit'
    })
  },
  
  /**
   * 跳转到我的关注页面
   */
  goToMyFollow() {
    wx.navigateTo({
      url: '/pages/my-follow/my-follow'
    })
  },
  
  /**
   * 选择并上传头像
   */
  async chooseAvatar() {
    try {
      // 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          success: resolve,
          fail: reject
        })
      })

      const tempFilePath = chooseRes.tempFiles?.[0]?.tempFilePath
      if (!tempFilePath) {
        return
      }

      // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
      const fileSize = chooseRes.tempFiles?.[0]?.size || 0
      const maxSize = 10 * 1024 * 1024 // 10MB
      if (fileSize > maxSize) {
        wx.showToast({
          title: '图片大小不能超过10MB',
          icon: 'none'
        })
        return
      }

      // 显示上传中提示
      wx.showLoading({
        title: '上传中...',
        mask: true
      })

      // 获取原始文件名（如果有）
      const originalName = chooseRes.tempFiles?.[0]?.name || 'avatar.jpg'

      // 直接调用公共的文件上传方法
      const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

      if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
        // 获取返回的图片URL
        const rawImageUrl = uploadRes.data.fileUrl || ''
        if (rawImageUrl) {
          // 使用 config.getImageUrl 处理图片URL，确保是完整的URL
          const config = require('../../../utils/config.js')
          const imageUrl = config.getImageUrl(rawImageUrl)
          
          // 更新本地用户信息
          const updatedUserInfo = {
            ...this.data.userInfo,
            avatarUrl: imageUrl
          }
          this.setData({ userInfo: updatedUserInfo })
          
          // 上传成功后自动保存
          const updateData = { avatarUrl: imageUrl }
          await this.saveSingleField(updateData, true)
        } else {
          wx.showToast({
            title: '上传失败，未获取到图片地址',
            icon: 'none'
          })
        }
      } else {
        wx.showToast({
          title: uploadRes?.msg || '上传失败',
          icon: 'none'
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000
      })
    } finally {
      wx.hideLoading()
    }
  },
  
  /**
   * 保存单个字段（通用方法）
   * @param {Object} updateData - 要更新的字段数据
   * @param {Boolean} showSuccessToast - 是否显示成功提示，默认 true
   */
  async saveSingleField(updateData, showSuccessToast = true) {
    try {
      const success = await this.saveToApi(updateData)

      if (success) {
        if (showSuccessToast) {
          wx.showToast({ title: '保存成功', icon: 'success', duration: 1500 })
        }
        return true
      } else {
        return false
      }
    } catch (error) {
      console.error('保存字段失败:', error)
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
      return false
    }
  },
  
  /**
   * 保存到真实接口
   * @param {Object} updateData - 要更新的数据
   */
  async saveToApi(updateData) {
    const res = await userApi.updateUserInfo(updateData)
    
    if (res.data && res.data.code === 200) {
      // 保存成功后，更新全局数据
      app.globalData.userData = {
        ...(app.globalData.userData || {}),
        ...updateData
      }
      app.globalData.userInfo = {
        ...(app.globalData.userInfo || {}),
        ...updateData
      }
      
      return true
    } else {
      wx.showToast({
        title: res.data?.msg || '保存失败',
        icon: 'none'
      })
      return false
    }
  }
})
