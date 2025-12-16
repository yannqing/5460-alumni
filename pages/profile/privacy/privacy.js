// pages/profile/privacy/privacy.js
const { userApi } = require('../../../api/api.js')

Page({
  data: {
    privacySettings: {
      // 基本信息（按编辑资料页面顺序）
      // 注意：false=显示信息（visibility=1），true=隐藏信息（visibility=0）
      showAvatar: false,
      showNickname: false,
      showName: false,
      showGender: false,
      showBirthDate: false,
      showPhone: false,
      showConstellation: false,
      showSignature: false,
      showWxNum: false,
      showQqNum: false,
      showEmail: false,
      // 位置信息（按编辑资料页面顺序）
      showOriginProvince: false,
      showCurContinent: false,
      showCurCountry: false,
      showCurProvince: false,
      showCurCity: false,
      showCurCounty: false,
      showAddress: false,
      // 证件信息（按编辑资料页面顺序）
      showIdentifyType: false,
      showIdentifyCode: false,
      // 个人简介（按编辑资料页面顺序）
      showDescription: false
    },
    // 存储后端返回的原始数据（用于更新时回传）
    privacyList: []
  },

  onLoad() {
    this.loadPrivacySettings()
  },

  // 加载隐私设置
  async loadPrivacySettings() {
    try {
      // 调用后端接口获取隐私设置
      const res = await userApi.getPrivacy()

      if (res.data && res.data.code === 200) {
        const dataList = res.data.data || []

        // 保存原始数据
        this.setData({ privacyList: dataList })

        // 将后端数据数组映射到前端字段
        // fieldCode 字段代码映射关系（按编辑资料页面字段顺序）
        const fieldCodeMap = {
          // 基本信息
          'avatar': 'showAvatar',
          'nickname': 'showNickname',
          'username': 'showName',  // 真实姓名对应 username
          'gender': 'showGender',
          'birthDate': 'showBirthDate',
          'phone': 'showPhone',
          'constellation': 'showConstellation',
          'signature': 'showSignature',
          'wxNum': 'showWxNum',
          'qqNum': 'showQqNum',
          'email': 'showEmail',
          // 位置信息
          'originProvince': 'showOriginProvince',
          'curContinent': 'showCurContinent',
          'curCountry': 'showCurCountry',
          'curProvince': 'showCurProvince',
          'curCity': 'showCurCity',
          'curCounty': 'showCurCounty',
          'address': 'showAddress',
          // 证件信息
          'identifyType': 'showIdentifyType',
          'identifyCode': 'showIdentifyCode',
          // 个人简介
          'description': 'showDescription'
        }

        const privacySettings = {
          // 基本信息（默认都是 false，表示显示信息）
          showAvatar: false,
          showNickname: false,
          showName: false,
          showGender: false,
          showBirthDate: false,
          showPhone: false,
          showConstellation: false,
          showSignature: false,
          showWxNum: false,
          showQqNum: false,
          showEmail: false,
          // 位置信息
          showOriginProvince: false,
          showCurContinent: false,
          showCurCountry: false,
          showCurProvince: false,
          showCurCity: false,
          showCurCounty: false,
          showAddress: false,
          // 证件信息
          showIdentifyType: false,
          showIdentifyCode: false,
          // 个人简介
          showDescription: false
        }

        // 遍历后端返回的数组，更新对应的设置
        // 注意：visibility=1表示可见（开关关闭false），visibility=0表示不可见（开关打开true）
        dataList.forEach(item => {
          const fieldCode = item.fieldCode
          const frontendKey = fieldCodeMap[fieldCode]

          if (frontendKey) {
            // visibility: 1表示可见（开关关闭=false），0表示不可见（开关打开=true）
            privacySettings[frontendKey] = item.visibility === 0
          }
        })

        this.setData({ privacySettings })
      } else {
        // 接口失败，使用默认值
        console.warn('获取隐私设置失败，使用默认值')
      }
    } catch (error) {
      console.error('加载隐私设置失败:', error)
      // 出错时使用默认值
    }
  },

  // 切换隐私设置
  async togglePrivacy(e) {
    const { key } = e.currentTarget.dataset
    const value = e.detail.value

    // 先更新UI
    this.setData({
      [`privacySettings.${key}`]: value
    })

    try {
      // 前端字段到后端 fieldCode 的映射关系（按编辑资料页面字段顺序）
      const keyToFieldCodeMap = {
        // 基本信息
        'showAvatar': 'avatar',
        'showNickname': 'nickname',
        'showName': 'username',  // 真实姓名对应 username
        'showGender': 'gender',
        'showBirthDate': 'birthDate',
        'showPhone': 'phone',
        'showConstellation': 'constellation',
        'showSignature': 'signature',
        'showWxNum': 'wxNum',
        'showQqNum': 'qqNum',
        'showEmail': 'email',
        // 位置信息
        'showOriginProvince': 'originProvince',
        'showCurContinent': 'curContinent',
        'showCurCountry': 'curCountry',
        'showCurProvince': 'curProvince',
        'showCurCity': 'curCity',
        'showCurCounty': 'curCounty',
        'showAddress': 'address',
        // 证件信息
        'showIdentifyType': 'identifyType',
        'showIdentifyCode': 'identifyCode',
        // 个人简介
        'showDescription': 'description'
      }

      const fieldCode = keyToFieldCodeMap[key]

      // 更新 privacyList 中对应字段的值
      // 注意：value=true表示开关打开（隐藏信息，visibility=0）
      //       value=false表示开关关闭（显示信息，visibility=1）
      const privacyList = this.data.privacyList.map(item => {
        if (item.fieldCode === fieldCode) {
          return {
            ...item,
            visibility: value ? 0 : 1,  // 反转逻辑：true=0（隐藏），false=1（显示）
            searchable: 1  // 固定为1，表示可被搜索
          }
        }
        // 对于其他字段，也需要确保 searchable 为 1
        return {
          ...item,
          searchable: 1
        }
      })

      // 调用后端接口保存隐私设置
      // 注意：userPrivacySettingId 保持字符串格式（避免 JavaScript 精度丢失）
      // 后端的 Jackson 或 FastJson 会自动将字符串解析为 Long 类型
      const cleanedList = privacyList.map(item => ({
        userPrivacySettingId: String(item.userPrivacySettingId),  // 确保是字符串
        fieldCode: item.fieldCode,
        visibility: item.visibility,
        searchable: item.searchable
      }))
      
      const requestData = {
        PrivacySettingsRequestList: cleanedList
      }
      const res = await userApi.updatePrivacy(requestData)

      if (res.data && res.data.code === 200) {
        // 更新本地存储的 privacyList
        this.setData({ privacyList })

        wx.showToast({
          title: value ? '已隐藏' : '已显示',  // 更新提示文案
          icon: 'success',
          duration: 1500
        })
      } else {
        // 保存失败，恢复原值
        this.setData({
          [`privacySettings.${key}`]: !value
        })
        wx.showToast({
          title: res.data?.msg || '保存失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('保存隐私设置失败:', error)
      // 保存失败，恢复原值
      this.setData({
        [`privacySettings.${key}`]: !value
      })
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
    }
  }
})


