// pages/profile/privacy/privacy.js
const { userApi } = require('../../../api/api.js')

// 字段分组配置
const FIELD_GROUPS = {
  basicInfo: {
    title: '基本信息',
    fields: ['avatar', 'nickname', 'username', 'gender', 'birthDate', 'phone', 'constellation', 'signature', 'wxNum', 'qqNum', 'email']
  },
  location: {
    title: '位置信息',
    fields: ['originProvince', 'curContinent', 'curCountry', 'curProvince', 'curCity', 'curCounty', 'address']
  },
  identity: {
    title: '证件信息',
    fields: ['identifyType', 'identifyCode']
  },
  description: {
    title: '个人简介',
    fields: ['description']
  }
}

Page({
  data: {
    // 分组后的隐私设置列表（用于渲染）
    groupedPrivacyList: [],
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
          'avatarUrl': 'showAvatar',
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
        // 为每个字段添加开关状态
        // visibility: 1表示可见（开关关闭=false），0表示不可见（开关打开=true）
        const processedList = dataList.map(item => ({
          ...item,
          checked: item.visibility === 0  // true=隐藏，false=显示
        }))

        // 按分组整理数据
        const groupedPrivacyList = this.groupPrivacyItems(processedList)

        this.setData({ groupedPrivacyList })
      } else {
        // 接口失败
        console.warn('获取隐私设置失败')
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('加载隐私设置失败:', error)
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 将隐私项按分组整理
  groupPrivacyItems(items) {
    const groups = []
    const usedFieldCodes = new Set()

    // 按预定义的分组顺序处理
    Object.keys(FIELD_GROUPS).forEach(groupKey => {
      const groupConfig = FIELD_GROUPS[groupKey]
      const groupItems = []

      // 找出属于这个分组的字段
      groupConfig.fields.forEach(fieldCode => {
        const item = items.find(i => i.fieldCode === fieldCode)
        if (item) {
          groupItems.push(item)
          usedFieldCodes.add(fieldCode)
        }
      })

      // 只有当分组有内容时才添加
      if (groupItems.length > 0) {
        groups.push({
          title: groupConfig.title,
          items: groupItems
        })
      }
    })

    // 处理未分组的字段（后端新增的字段），放到"其他"分组
    const otherItems = items.filter(item => !usedFieldCodes.has(item.fieldCode))
    if (otherItems.length > 0) {
      groups.push({
        title: '其他',
        items: otherItems
      })
    }

    return groups
  },

  // 切换隐私设置
  async togglePrivacy(e) {
    const { groupIndex, itemIndex, fieldCode } = e.currentTarget.dataset
    const value = e.detail.value

    // 先更新UI
    this.setData({
      [`groupedPrivacyList[${groupIndex}].items[${itemIndex}].checked`]: value
    })

    try {
      // 前端字段到后端 fieldCode 的映射关系（按编辑资料页面字段顺序）
      const keyToFieldCodeMap = {
        // 基本信息
        'showAvatar': 'avatarUrl',
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

      if (!fieldCode) {
        // 如果找不到对应的 fieldCode 映射，恢复原值并提示错误
        console.error('[Privacy] 找不到字段映射，key:', key)
        this.setData({
          [`privacySettings.${key}`]: !value
        })
        wx.showToast({
          title: '字段映射错误，请刷新后重试',
          icon: 'none'
        })
        return
      }

      // 从 privacyList 中找到当前要更新的字段
      const currentItem = this.data.privacyList.find(item => item.fieldCode === fieldCode)

      // 构造请求数据
      // 注意：value=true表示开关打开（隐藏信息，visibility=0）
      //       value=false表示开关关闭（显示信息，visibility=1）
      const requestData = {
        fieldCode: fieldCode,
        visibility: value ? 0 : 1,  // 反转逻辑：true=0（隐藏），false=1（显示）
        searchable: 1  // 固定为1，表示可被搜索
      }

      // 如果找到了已有的记录，添加 userPrivacySettingId
      if (currentItem && currentItem.userPrivacySettingId) {
        requestData.userPrivacySettingId = String(currentItem.userPrivacySettingId)
      }

      const res = await userApi.updatePrivacy(requestData)

      if (res.data && res.data.code === 200) {
        // 更新本地存储的 privacyList 中对应字段的值
        const updatedPrivacyList = this.data.privacyList.map(item => {
          if (item.fieldCode === fieldCode) {
            return {
              ...item,
              visibility: requestData.visibility,
              searchable: requestData.searchable
            }
          }
          return item
        })

        this.setData({ privacyList: updatedPrivacyList })

        wx.showToast({
          title: value ? '已隐藏' : '已显示',
          icon: 'success',
          duration: 1500
        })
      } else {
        // 保存失败，恢复原值
        this.setData({
          [`groupedPrivacyList[${groupIndex}].items[${itemIndex}].checked`]: !value
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
        [`groupedPrivacyList[${groupIndex}].items[${itemIndex}].checked`]: !value
      })
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      })
    }
  }
})







