Page({
    data: {
    },

    onLoad(options) {

    },

    // 申请商家
    applyMerchant() {
        wx.navigateTo({
            url: '/pages/merchant/apply/apply'
        })
    },

    navigateTo(e) {
        const { url } = e.currentTarget.dataset
        if (url) {
            wx.navigateTo({ url })
        }
    }
})
