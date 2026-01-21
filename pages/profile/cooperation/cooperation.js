const config = require('../../../utils/config.js')

Page({
    data: {
        iconWssj: config.getIconUrl('wssj@3x.png'),
        iconSqxyh: config.getIconUrl('sqxyh@3x.png')
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
