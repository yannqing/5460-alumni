// pages/merchant/apply/apply.js
const app = getApp()
const { associationApi, fileApi, merchantApi } = require('../../../api/api.js')
const { post } = require('../../../utils/request.js')

Page({
  data: {
    form: {
      merchantName: '',
      city: '',
      unifiedSocialCreditCode: '',
      legalPerson: '',
      phone: ''
    },
    submitting: false,
    merchantStatus: 'none', // none, pending, approved, rejected
    /** 编辑待审核申请时存在（与 onLoad options.merchantId 一致） */
    merchantId: '',

    // 地区选择器
    regionData: [[], []], // 多列选择器的数据 [[省份列表], [城市列表]]
    regionIndex: [0, 0], // 当前选中的索引 [省索引, 市索引]
    provinceCityMap: {}, // 省份城市映射表
    provinceSuffixMap: {}, // 省份名称到带单位名称的映射
    provinceNameMap: {}, // 带单位名称到不带单位名称的反向映射
  },

  onLoad(options) {
    // 初始化省市级数据
    this.initRegionData()

    this.loadMerchantStatus()
    this.loadUserData()
    
    // 如果有merchantId参数，获取待审核商家详情
    if (options.merchantId) {
      this.setData({
        merchantId: options.merchantId
      })
      this.loadPendingMerchantDetail(options.merchantId)
    }
  },

  loadUserData() {
    const userData = app.globalData.userData || {}
    const userInfo = app.globalData.userInfo || {}
    
    const formData = {
      merchantName: '',
      city: '',
      unifiedSocialCreditCode: '',
      legalPerson: userInfo.nickName || userData.nickName || userData.name || '',
      phone: userInfo.mobile || userData.mobile || userData.phone || ''
    }
    
    this.setData({ form: formData })
  },

  loadMerchantStatus() {
    const userData = app.globalData.userData || {}
    // 假设后端返回 merchantStatus 字段
    const status = userData.merchantStatus || 'none'
    this.setData({ merchantStatus: status })
  },

  // 初始化省市级数据
  initRegionData() {
    // 省份名称到带单位名称的映射
    const provinceSuffixMap = {
      北京: '北京市',
      上海: '上海市',
      天津: '天津市',
      重庆: '重庆市',
      河北: '河北省',
      山西: '山西省',
      内蒙古: '内蒙古自治区',
      辽宁: '辽宁省',
      吉林: '吉林省',
      黑龙江: '黑龙江省',
      江苏: '江苏省',
      浙江: '浙江省',
      安徽: '安徽省',
      福建: '福建省',
      江西: '江西省',
      山东: '山东省',
      河南: '河南省',
      湖北: '湖北省',
      湖南: '湖南省',
      广东: '广东省',
      广西: '广西壮族自治区',
      海南: '海南省',
      四川: '四川省',
      贵州: '贵州省',
      云南: '云南省',
      西藏: '西藏自治区',
      陕西: '陕西省',
      甘肃: '甘肃省',
      青海: '青海省',
      宁夏: '宁夏回族自治区',
      新疆: '新疆维吾尔自治区',
      台湾: '台湾省',
      香港: '香港特别行政区',
      澳门: '澳门特别行政区',
    }

    // 带单位名称到不带单位名称的反向映射（用于传给后端）
    const provinceNameMap = {}
    Object.keys(provinceSuffixMap).forEach(key => {
      provinceNameMap[provinceSuffixMap[key]] = key
    })

    // 完整的省市级数据（所有34个省级行政区及其所有城市）
    // 直辖市只显示自己本身
    const provinceCityMap = {
      北京: ['北京市'],
      上海: ['上海市'],
      天津: ['天津市'],
      重庆: ['重庆市'],
      河北: [
        '石家庄市',
        '唐山市',
        '秦皇岛市',
        '邯郸市',
        '邢台市',
        '保定市',
        '张家口市',
        '承德市',
        '沧州市',
        '廊坊市',
        '衡水市',
        '密云市',
      ],
      山西: [
        '太原市',
        '大同市',
        '阳泉市',
        '长治市',
        '晋城市',
        '朔州市',
        '晋中市',
        '运城市',
        '忻州市',
        '临汾市',
        '吕梁市',
      ],
      内蒙古: [
        '呼和浩特市',
        '包头市',
        '乌海市',
        '赤峰市',
        '通辽市',
        '鄂尔多斯市',
        '呼伦贝尔市',
        '巴彦淖尔市',
        '乌兰察布市',
        '兴安盟',
        '锡林郭勒盟',
        '阿拉善盟',
      ],
      辽宁: [
        '沈阳市',
        '大连市',
        '鞍山市',
        '抚顺市',
        '本溪市',
        '丹东市',
        '锦州市',
        '营口市',
        '阜新市',
        '辽阳市',
        '盘锦市',
        '铁岭市',
        '朝阳市',
        '葫芦岛市',
      ],
      吉林: [
        '长春市',
        '吉林市',
        '四平市',
        '辽源市',
        '通化市',
        '白山市',
        '松原市',
        '白城市',
        '延边朝鲜族自治州',
      ],
      黑龙江: [
        '哈尔滨市',
        '齐齐哈尔市',
        '鸡西市',
        '鹤岗市',
        '双鸭山市',
        '大庆市',
        '伊春市',
        '佳木斯市',
        '七台河市',
        '牡丹江市',
        '黑河市',
        '绥化市',
        '大兴安岭地区',
      ],
      江苏: [
        '南京市',
        '无锡市',
        '徐州市',
        '常州市',
        '苏州市',
        '南通市',
        '连云港市',
        '淮安市',
        '盐城市',
        '扬州市',
        '镇江市',
        '泰州市',
        '宿迁市',
      ],
      浙江: [
        '杭州市',
        '宁波市',
        '温州市',
        '嘉兴市',
        '湖州市',
        '绍兴市',
        '金华市',
        '衢州市',
        '舟山市',
        '台州市',
        '丽水市',
      ],
      安徽: [
        '合肥市',
        '芜湖市',
        '蚌埠市',
        '淮南市',
        '马鞍山市',
        '淮北市',
        '铜陵市',
        '安庆市',
        '黄山市',
        '滁州市',
        '阜阳市',
        '宿州市',
        '六安市',
        '亳州市',
        '池州市',
        '宣城市',
      ],
      福建: [
        '福州市',
        '厦门市',
        '莆田市',
        '三明市',
        '泉州市',
        '漳州市',
        '南平市',
        '龙岩市',
        '宁德市',
      ],
      江西: [
        '南昌市',
        '景德镇市',
        '萍乡市',
        '九江市',
        '新余市',
        '鹰潭市',
        '赣州市',
        '吉安市',
        '宜春市',
        '抚州市',
        '上饶市',
      ],
      山东: [
        '济南市',
        '青岛市',
        '淄博市',
        '枣庄市',
        '东营市',
        '烟台市',
        '潍坊市',
        '济宁市',
        '泰安市',
        '威海市',
        '日照市',
        '临沂市',
        '德州市',
        '聊城市',
        '滨州市',
        '菏泽市',
      ],
      河南: [
        '郑州市',
        '开封市',
        '洛阳市',
        '平顶山市',
        '安阳市',
        '鹤壁市',
        '新乡市',
        '焦作市',
        '濮阳市',
        '许昌市',
        '漯河市',
        '三门峡市',
        '南阳市',
        '商丘市',
        '信阳市',
        '周口市',
        '驻马店市',
        '济源市',
      ],
      湖北: [
        '武汉市',
        '黄石市',
        '十堰市',
        '宜昌市',
        '襄阳市',
        '鄂州市',
        '荆门市',
        '孝感市',
        '荆州市',
        '黄冈市',
        '咸宁市',
        '随州市',
        '恩施土家族苗族自治州',
        '仙桃市',
        '潜江市',
        '天门市',
        '神农架林区',
      ],
      湖南: [
        '长沙市',
        '株洲市',
        '湘潭市',
        '衡阳市',
        '邵阳市',
        '岳阳市',
        '常德市',
        '张家界市',
        '益阳市',
        '郴州市',
        '永州市',
        '怀化市',
        '娄底市',
        '湘西土家族苗族自治州',
      ],
      广东: [
        '广州市',
        '韶关市',
        '深圳市',
        '珠海市',
        '汕头市',
        '佛山市',
        '江门市',
        '湛江市',
        '茂名市',
        '肇庆市',
        '惠州市',
        '梅州市',
        '汕尾市',
        '河源市',
        '阳江市',
        '清远市',
        '东莞市',
        '中山市',
        '潮州市',
        '揭阳市',
        '云浮市',
      ],
      广西: [
        '南宁市',
        '柳州市',
        '桂林市',
        '梧州市',
        '北海市',
        '防城港市',
        '钦州市',
        '贵港市',
        '玉林市',
        '百色市',
        '贺州市',
        '河池市',
        '来宾市',
        '崇左市',
      ],
      海南: [
        '海口市',
        '三亚市',
        '三沙市',
        '儋州市',
        '五指山市',
        '琼海市',
        '文昌市',
        '万宁市',
        '东方市',
        '定安县',
        '屯昌县',
        '澄迈县',
        '临高县',
        '白沙黎族自治县',
        '昌江黎族自治县',
        '乐东黎族自治县',
        '陵水黎族自治县',
        '保亭黎族苗族自治县',
        '琼中黎族苗族自治县',
      ],
      四川: [
        '成都市',
        '自贡市',
        '攀枝花市',
        '泸州市',
        '德阳市',
        '绵阳市',
        '广元市',
        '遂宁市',
        '内江市',
        '乐山市',
        '南充市',
        '眉山市',
        '宜宾市',
        '广安市',
        '达州市',
        '雅安市',
        '巴中市',
        '资阳市',
        '阿坝藏族羌族自治州',
        '甘孜藏族自治州',
        '凉山彝族自治州',
      ],
      贵州: [
        '贵阳市',
        '六盘水市',
        '遵义市',
        '安顺市',
        '毕节市',
        '铜仁市',
        '黔西南布依族苗族自治州',
        '黔东南苗族侗族自治州',
        '黔南布依族苗族自治州',
      ],
      云南: [
        '昆明市',
        '曲靖市',
        '玉溪市',
        '保山市',
        '昭通市',
        '丽江市',
        '普洱市',
        '临沧市',
        '楚雄彝族自治州',
        '红河哈尼族彝族自治州',
        '文山壮族苗族自治州',
        '西双版纳傣族自治州',
        '大理白族自治州',
        '德宏傣族景颇族自治州',
        '怒江傈僳族自治州',
        '迪庆藏族自治州',
      ],
      西藏: ['拉萨市', '日喀则市', '昌都市', '林芝市', '山南市', '那曲市', '阿里地区'],
      陕西: [
        '西安市',
        '铜川市',
        '宝鸡市',
        '咸阳市',
        '渭南市',
        '延安市',
        '汉中市',
        '榆林市',
        '安康市',
        '商洛市',
      ],
      甘肃: [
        '兰州市',
        '嘉峪关市',
        '金昌市',
        '白银市',
        '天水市',
        '武威市',
        '张掖市',
        '平凉市',
        '酒泉市',
        '庆阳市',
        '定西市',
        '陇南市',
        '临夏回族自治州',
        '甘南藏族自治州',
      ],
      青海: [
        '西宁市',
        '海东市',
        '海北藏族自治州',
        '黄南藏族自治州',
        '海南藏族自治州',
        '果洛藏族自治州',
        '玉树藏族自治州',
        '海西蒙古族藏族自治州',
      ],
      宁夏: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'],
      新疆: [
        '乌鲁木齐市',
        '克拉玛依市',
        '吐鲁番市',
        '哈密市',
        '昌吉回族自治州',
        '博尔塔拉蒙古自治州',
        '巴音郭楞蒙古自治州',
        '阿克苏地区',
        '克孜勒苏柯尔克孜自治州',
        '喀什地区',
        '和田地区',
        '伊犁哈萨克自治州',
        '塔城地区',
        '阿勒泰地区',
        '石河子市',
        '阿拉尔市',
        '图木舒克市',
        '五家渠市',
        '北屯市',
        '铁门关市',
        '双河市',
        '可克达拉市',
        '昆玉市',
        '胡杨河市',
      ],
      台湾: ['台北市', '新北市', '桃园市', '台中市', '台南市', '高雄市'],
      香港: ['香港特别行政区'],
      澳门: ['澳门特别行政区'],
    }

    // 提取省份列表
    const provinceKeys = Object.keys(provinceCityMap)
    const provinceList = provinceKeys.map(key => provinceSuffixMap[key] || key)

    // 初始化：第一列是第一个省份，第二列是该省的所有城市
    const firstProvinceKey = provinceKeys[0]
    const firstProvinceCities = provinceCityMap[firstProvinceKey] || []

    this.setData({
      provinceCityMap: provinceCityMap,
      provinceSuffixMap: provinceSuffixMap, // 省份名称到带单位名称的映射
      provinceNameMap: provinceNameMap, // 带单位名称到不带单位名称的反向映射
      regionData: [provinceList, firstProvinceCities],
      regionIndex: [0, 0],
    })
  },

  // 地区选择器列改变（联动选择）
  handleRegionColumnChange(e) {
    const column = e.detail.column // 改变的列索引（0: 省, 1: 市）
    const value = e.detail.value // 新选中的索引

    const { regionData, provinceCityMap, provinceNameMap, regionIndex } = this.data
    const provinceList = regionData[0]

    // 如果改变的是省份列（第一列）
    if (column === 0) {
      // 选择具体省份，城市列表显示该省的所有城市
      // 直辖市只显示自己本身
      const selectedProvinceWithSuffix = provinceList[value]
      const selectedProvinceKey =
        provinceNameMap[selectedProvinceWithSuffix] || selectedProvinceWithSuffix
      const provinceCities = provinceCityMap[selectedProvinceKey] || []

      // 判断是否为直辖市
      const isMunicipality = ['北京', '上海', '天津', '重庆'].includes(selectedProvinceKey)
      const cityList = provinceCities

      const newRegionData = [provinceList, cityList]
      const newRegionIndex = [value, 0] // 重置城市索引为0

      this.setData({
        regionData: newRegionData,
        regionIndex: newRegionIndex,
      })
    } else if (column === 1) {
      // 如果改变的是城市列（第二列），只更新城市索引
      const newRegionIndex = [regionIndex[0], value]
      this.setData({
        regionIndex: newRegionIndex,
      })
    }
  },

  // 处理城市选择
  handleRegionChange(e) {
    const index = e.detail.value // [省索引, 市索引]
    const { regionData } = this.data

    const provinceList = regionData[0]
    const cityList = regionData[1]

    // 根据索引获取选中的省和市
    const selectedProvinceWithSuffix = provinceList[index[0]] || ''
    const selectedCity = cityList[index[1]] || ''

    // 拼接为 "省 市" 格式
    let location = selectedProvinceWithSuffix
    if (selectedCity && selectedCity !== selectedProvinceWithSuffix) {
      location += ' ' + selectedCity
    }

    this.setData({
      regionIndex: index,
      'form.city': location,
    })
  },

  // 加载待审核商家详情
  loadPendingMerchantDetail(merchantId) {
    wx.showLoading({ title: '加载中...' })
    
    merchantApi.getPendingMerchantDetail(merchantId).then((res) => {
      wx.hideLoading()
      
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        // 初始化地区选择器的索引
        let regionIndex = [0, 0]
        let regionData = this.data.regionData
        if (data.city) {
          const locParts = String(data.city).split(' ')
          const prov = locParts[0]
          const city = locParts[1]

          const { provinceCityMap, provinceNameMap } = this.data
          const provinceList = regionData[0]

          // 查找省份索引
          const provIdx = provinceList.findIndex(p => p === prov || provinceNameMap[p] === prov)
          if (provIdx !== -1) {
            const selectedProvWithSuffix = provinceList[provIdx]
            const selectedProvKey = provinceNameMap[selectedProvWithSuffix] || selectedProvWithSuffix
            const cityList = provinceCityMap[selectedProvKey] || []

            // 查找城市索引
            let cityIdx = 0
            if (city) {
              cityIdx = cityList.indexOf(city)
              if (cityIdx === -1) cityIdx = 0
            }

            regionIndex = [provIdx, cityIdx]
            regionData = [provinceList, cityList]
          }
        }

        // 预填充表单数据
        this.setData({
          form: {
            merchantName: data.merchantName || '',
            city: data.city || '',
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            phone: data.phone || ''
          },
          regionIndex,
          regionData,
          merchantStatus: this.getMerchantStatusText(data.reviewStatus) // 设置审核状态
        })
      } else {
        wx.showToast({
          title: msg || '加载失败，请稍后重试',
          icon: 'none'
        })
      }
    }).catch((err) => {
      wx.hideLoading()
      console.error('加载待审核商家详情失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
    })
  },

  // 获取商家状态文本
  getMerchantStatusText(reviewStatus) {
    switch (reviewStatus) {
      case 0: // 待审核
        return 'pending'
      case 1: // 已通过
        return 'approved'
      case 2: // 已拒绝
        return 'rejected'
      default:
        return 'none'
    }
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  validateForm() {
    const { merchantName, city, unifiedSocialCreditCode, legalPerson, phone } = this.data.form
    
    if (!merchantName || !merchantName.trim()) {
      wx.showToast({ title: '请输入商户名称', icon: 'none' })
      return false
    }
    
    if (merchantName.length > 100) {
      wx.showToast({ title: '商户名称不能超过100个字符', icon: 'none' })
      return false
    }

    if (!city || !city.trim()) {
      wx.showToast({ title: '请输入所在城市', icon: 'none' })
      return false
    }
    
    if (!unifiedSocialCreditCode || !unifiedSocialCreditCode.trim()) {
      wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
      return false
    }
    
    const normalizedCreditCode = String(unifiedSocialCreditCode).trim().toUpperCase()
    if (normalizedCreditCode.length !== 18) {
      wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' })
      return false
    }
    if (!/^[0-9A-Z]{18}$/.test(normalizedCreditCode)) {
      wx.showToast({ title: '统一社会信用代码格式不正确', icon: 'none' })
      return false
    }
    
    if (!legalPerson || !legalPerson.trim()) {
      wx.showToast({ title: '请输入法人姓名', icon: 'none' })
      return false
    }
    
    if (legalPerson.length > 50) {
      wx.showToast({ title: '法人姓名不能超过50个字符', icon: 'none' })
      return false
    }
    
    if (!phone || !phone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return false
    }
    
    if (!/^\d{11}$/.test(String(phone).trim())) {
      wx.showToast({ title: '联系电话须为11位数字', icon: 'none' })
      return false
    }
    
    return true
  },

  submitApply() {
    if (this.data.merchantStatus === 'approved') {
      wx.showToast({
        title: '您已是认证商家',
        icon: 'none'
      })
      return
    }
    
    if (!this.validateForm()) {return}
    
    this.setData({ submitting: true })
    
    const { form } = this.data
    const normalizedCreditCode = String(form.unifiedSocialCreditCode || '').trim().toUpperCase()

    const applyPayload = {
      merchantName: form.merchantName,
      city: form.city,
      unifiedSocialCreditCode: normalizedCreditCode,
      legalPerson: form.legalPerson,
      phone: form.phone
    }

    const mid = this.data.merchantId
    const useUpdatePending = mid && this.data.merchantStatus === 'pending'
    const req = useUpdatePending
      ? merchantApi.updatePendingMerchantApplication(mid, applyPayload)
      : post('/merchant/apply', applyPayload)

    req.then((res) => {
      const { code, data, msg } = res.data || {}
      
      if (code === 200 && data) {
        wx.showToast({
          title: msg || '申请提交成功，请等待审核',
          icon: 'success',
          duration: 2000
        })
        this.setData({
          submitting: false,
          merchantStatus: 'pending'
        })
        
        // 更新全局数据
        if (app.globalData.userData) {
          app.globalData.userData.merchantStatus = 'pending'
        }
        
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({
          title: msg || '提交失败，请稍后重试',
          icon: 'none'
        })
        this.setData({ submitting: false })
      }
    }).catch((err) => {
      console.error('提交申请失败:', err)
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      })
      this.setData({ submitting: false })
    })
  }
})



