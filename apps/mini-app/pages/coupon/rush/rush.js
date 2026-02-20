// pages/coupon/rush/rush.js
Page({
  data: {
    couponId: '',
    couponInfo: null,
    rushTime: null,
    countdown: {
      hours: 0,
      minutes: 0,
      seconds: 0
    },
    canRush: false,
    timer: null
  },

  onLoad(options) {
    this.setData({ couponId: options.id })
    this.loadCouponInfo()
  },

  onUnload() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
    }
  },

  loadCouponInfo() {
    const mockData = {
      id: this.data.couponId,
      title: '星巴克100元优惠券',
      merchant: '星巴克咖啡',
      originalPrice: 100,
      discountPrice: 59,
      image: 'https://via.placeholder.com/750x400/ff6b9d/ffffff?text=Starbucks',
      stock: 50,
      totalStock: 100,
      rushTime: '2025-11-13 14:00:00',
      endTime: '2025-11-20 23:59:59',
      description: '限量抢购，先到先得'
    }

    this.setData({ couponInfo: mockData })
    this.startCountdown(mockData.rushTime)
  },

  startCountdown(rushTime) {
    const targetTime = new Date(rushTime).getTime()

    const updateCountdown = () => {
      const now = Date.now()
      const diff = targetTime - now

      if (diff <= 0) {
        this.setData({ canRush: true })
        if (this.data.timer) {
          clearInterval(this.data.timer)
        }
        return
      }

      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)

      this.setData({
        countdown: {
          hours: hours < 10 ? '0' + hours : hours,
          minutes: minutes < 10 ? '0' + minutes : minutes,
          seconds: seconds < 10 ? '0' + seconds : seconds
        }
      })
    }

    updateCountdown()
    const timer = setInterval(updateCountdown, 1000)
    this.setData({ timer })
  },

  rushCoupon() {
    if (!this.data.canRush) {
      wx.showToast({
        title: '抢购未开始',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '抢购中...' })

    setTimeout(() => {
      wx.hideLoading()
      wx.showModal({
        title: '抢购成功',
        content: '优惠券已放入您的卡包',
        showCancel: false,
        success: () => {
          wx.navigateBack()
        }
      })
    }, 1000)
  }
})
