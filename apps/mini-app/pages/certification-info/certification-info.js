// pages/certification-info/certification-info.js
Page({
  data: {
    // 校友会认证等级图片
    certFirstImg: 'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/association_first_certification.png',
    certSecondImg: 'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/association_second_certification.png',
    certThirdImg: 'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/certification/association_third_certification.png'
  },

  onLoad(options) {
    // 页面加载
  },

  onShareAppMessage() {
    return {
      title: '认证等级说明',
      path: '/pages/certification-info/certification-info'
    }
  }
})
