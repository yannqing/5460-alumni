// pages/alumni-union/detail/detail.js
Page({
  data: {
    unionId: '',
    unionInfo: null,
    loading: true
  },

  onLoad(options) {
    const { id } = options
    this.setData({ unionId: id || '' })
    this.loadUnionDetail()
  },

  onShareAppMessage() {
    return {
      title: this.data.unionInfo?.name || '校友总会',
      path: `/pages/alumni-union/detail/detail?id=${this.data.unionId}`
    }
  },

  // 加载校友总会详情
  loadUnionDetail() {
    this.setData({ loading: true })
    
    // TODO: 接后端接口
    // wx.request({
    //   url: `${app.globalData.apiBase}/alumni-union/${this.data.unionId}`,
    //   method: 'GET',
    //   success: (res) => {
    //     if (res.data.code === 200) {
    //       this.setData({
    //         unionInfo: res.data.data,
    //         loading: false
    //       })
    //     }
    //   },
    //   fail: () => {
    //     this.setData({ loading: false })
    //   }
    // })

    // 模拟数据
    setTimeout(() => {
      const mockData = {
        id: this.data.unionId || '1',
        name: '南京大学校友总会',
        schoolName: '南京大学',
        icon: 'https://via.placeholder.com/150/ff6b9d/ffffff?text=NJU+Union',
        cover: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Alumni+Union',
        isCertified: true,
        description: '南京大学校友总会是南京大学校友的全球性组织，致力于加强校友之间的联系，促进校友与母校的交流合作，为校友提供全方位的服务和支持。',
        website: 'https://alumni.nju.edu.cn',
        phone: '025-83593186',
        email: 'alumni@nju.edu.cn',
        wechat: 'NJU_Alumni',
        address: '江苏省南京市栖霞区仙林大道163号',
        hasApp: true,
        appId: '',
        appPath: ''
      }

      this.setData({
        unionInfo: mockData,
        loading: false
      })
    }, 300)
  },

  // 预览图片
  previewImage(e) {
    const { url } = e.currentTarget.dataset
    wx.previewImage({
      urls: [url],
      current: url
    })
  },

  // 复制网址
  copyWebsite() {
    const { unionInfo } = this.data
    if (unionInfo.website) {
      wx.setClipboardData({
        data: unionInfo.website,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  // 拨打电话
  makeCall(e) {
    const { phone } = e.currentTarget.dataset
    if (phone) {
      wx.makePhoneCall({
        phoneNumber: phone
      })
    }
  },

  // 复制邮箱
  copyEmail(e) {
    const { email } = e.currentTarget.dataset
    if (email) {
      wx.setClipboardData({
        data: email,
        success: () => {
          wx.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  // 跳转到小程序
  navigateToMiniProgram() {
    const { unionInfo } = this.data
    if (unionInfo.hasApp && unionInfo.appId) {
      wx.navigateToMiniProgram({
        appId: unionInfo.appId,
        path: unionInfo.appPath || '',
        success: () => {
          console.log('跳转成功')
        },
        fail: () => {
          wx.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    } else {
      wx.showToast({
        title: '暂未开通小程序',
        icon: 'none'
      })
    }
  }
})

