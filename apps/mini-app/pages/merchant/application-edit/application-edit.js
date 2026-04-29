const app = getApp()
const { merchantApi } = require('../../../api/api.js')

Page({
  data: {
    recordId: '',
    form: {
      merchantName: '',
      city: '',
      unifiedSocialCreditCode: '',
      legalPerson: '',
      phone: ''
    },
    header: {
      name: '商户申请修改',
      logoUrl: 'https://cdn.cmswe.com/alumni/default_merchant_logo.png', // 默认Logo
      bgUrl: 'https://cdn.cmswe.com/alumni/default_merchant_bg.png' // 默认背景
    },
    submitting: false,

    // 地区选择器
    regionData: [[], []],
    regionIndex: [0, 0],
    provinceCityMap: {},
    provinceSuffixMap: {},
    provinceNameMap: {},
  },

  onLoad(options) {
    if (options.recordId) {
      const recordIdStr = decodeURIComponent(String(options.recordId))
      this.setData({ recordId: recordIdStr })
      this.initRegionData()
      this.loadDetail(recordIdStr)
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => wx.navigateBack(), 1500)
    }
  },

  loadDetail(recordId) {
    const recordIdStr = recordId != null ? String(recordId) : ''
    if (!recordIdStr) {
      wx.showToast({ title: '缺少申请ID', icon: 'none' })
      return
    }
    wx.showLoading({ title: '加载中...' })
    // 使用已有的获取详情接口
    merchantApi.getPendingMerchantDetail(recordIdStr).then(res => {
      wx.hideLoading()
      const { code, data, msg } = res.data || {}
      if (code === 200 && data) {
        // 预填充表单
        this.setData({
          form: {
            merchantName: data.merchantName || '',
            city: data.city || '',
            unifiedSocialCreditCode: data.unifiedSocialCreditCode || '',
            legalPerson: data.legalPerson || '',
            phone: data.phone || ''
          },
          'header.name': data.merchantName || '商户申请修改'
        })
        
        // 如果有Logo或背景，可以在这里更新
        // if (data.logoUrl) this.setData({ 'header.logoUrl': data.logoUrl })

        // 初始化地区选择器的索引
        this.syncRegionIndex(data.city)
      } else {
        wx.showToast({ title: msg || '加载失败', icon: 'none' })
      }
    }).catch(err => {
      wx.hideLoading()
      console.error('加载详情失败:', err)
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  },

  // 同步城市选择器索引
  syncRegionIndex(cityStr) {
    if (!cityStr) return
    const locParts = String(cityStr).split(' ')
    const prov = locParts[0]
    const city = locParts[1]

    const { provinceCityMap, provinceNameMap, regionData } = this.data
    const provinceList = regionData[0]

    const provIdx = provinceList.findIndex(p => p === prov || provinceNameMap[p] === prov)
    if (provIdx !== -1) {
      const selectedProvWithSuffix = provinceList[provIdx]
      const selectedProvKey = provinceNameMap[selectedProvWithSuffix] || selectedProvWithSuffix
      const cityList = provinceCityMap[selectedProvKey] || []

      let cityIdx = 0
      if (city) {
        cityIdx = cityList.indexOf(city)
        if (cityIdx === -1) cityIdx = 0
      }

      this.setData({
        regionIndex: [provIdx, cityIdx],
        regionData: [provinceList, cityList]
      })
    }
  },

  initRegionData() {
    // 这里的逻辑直接复制自 pages/merchant/apply/apply.js
    const provinceSuffixMap = {
      北京: '北京市', 上海: '上海市', 天津: '天津市', 重庆: '重庆市', 河北: '河北省',
      山西: '山西省', 内蒙古: '内蒙古自治区', 辽宁: '辽宁省', 吉林: '吉林省', 黑龙江: '黑龙江省',
      江苏: '江苏省', 浙江: '浙江省', 安徽: '安徽省', 福建: '福建省', 江西: '江西省',
      山东: '山东省', 河南: '河南省', 湖北: '湖北省', 湖南: '湖南省', 广东: '广东省',
      广西: '广西壮族自治区', 海南: '海南省', 四川: '四川省', 贵州: '贵州省', 云南: '云南省',
      西藏: '西藏自治区', 陕西: '陕西省', 甘肃: '甘肃省', 青海: '青海省', 宁夏: '宁夏回族自治区',
      新疆: '新疆维吾尔自治区', 台湾: '台湾省', 香港: '香港特别行政区', 澳门: '澳门特别行政区'
    }

    const provinceNameMap = {}
    Object.keys(provinceSuffixMap).forEach(key => {
      provinceNameMap[provinceSuffixMap[key]] = key
    })

    const provinceCityMap = {
      北京: ['北京市'], 上海: ['上海市'], 天津: ['天津市'], 重庆: ['重庆市'],
      河北: ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市', '保定市', '张家口市', '承德市', '沧州市', '廊坊市', '衡水市'],
      山西: ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市', '忻州市', '临汾市', '吕梁市'],
      内蒙古: ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市', '呼伦贝尔市', '巴彦淖尔市', '乌兰察布市', '兴安盟', '锡林郭勒盟', '阿拉善盟'],
      辽宁: ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市', '锦州市', '营口市', '阜新市', '辽阳市', '盘锦市', '铁岭市', '朝阳市', '葫芦岛市'],
      吉林: ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市', '松原市', '白城市', '延边朝鲜族自治州'],
      黑龙江: ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市', '伊春市', '佳木斯市', '七台河市', '牡丹江市', '黑河市', '绥化市', '大兴安岭地区'],
      江苏: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市', '盐城市', '扬州市', '镇江市', '泰州市', '宿迁市'],
      浙江: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市', '舟山市', '台州市', '丽水市'],
      安徽: ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市', '铜陵市', '安庆市', '黄山市', '滁州市', '阜阳市', '宿州市', '六安市', '亳州市', '池州市', '宣城市'],
      福建: ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市', '宁德市'],
      江西: ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市', '赣州市', '吉安市', '宜春市', '抚州市', '上饶市'],
      山东: ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市', '泰安市', '威海市', '日照市', '临沂市', '德州市', '聊城市', '滨州市', '菏泽市'],
      河南: ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市', '濮阳市', '许昌市', '漯河市', '三门峡市', '南阳市', '商丘市', '信阳市', '周口市', '驻马店市', '济源市'],
      湖北: ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市', '荆州市', '黄冈市', '咸宁市', '随州市', '恩施土家族苗族自治州', '仙桃市', '潜江市', '天门市', '神农架林区'],
      湖南: ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市', '益阳市', '郴州市', '永州市', '怀化市', '娄底市', '湘西土家族苗族自治州'],
      广东: ['广州市', '韶关市', '深圳市', '珠海市', '汕头市', '佛山市', '江门市', '湛江市', '茂名市', '肇庆市', '惠州市', '梅州市', '汕尾市', '河源市', '阳江市', '清远市', '东莞市', '中山市', '潮州市', '揭阳市', '云浮市'],
      广西: ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市', '钦州市', '贵港市', '玉林市', '百色市', '贺州市', '河池市', '来宾市', '崇左市'],
      海南: ['海口市', '三亚市', '三沙市', '儋州市', '五指山市', '琼海市', '文昌市', '万宁市', '东方市', '定安县', '屯昌县', '澄迈县', '临高县', '白沙黎族自治县', '昌江黎族自治县', '乐东黎族自治县', '陵水黎族自治县', '保亭黎族苗族自治县', '琼中黎族苗族自治县'],
      四川: ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市', '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市', '阿坝藏族羌族自治州', '甘孜藏族自治州', '凉山彝族自治州'],
      贵州: ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市', '黔西南布依族苗族自治州', '黔东南苗族侗族自治州', '黔南布依族苗族自治州'],
      云南: ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市', '普洱市', '临沧市', '楚雄彝族自治州', '红河哈尼族彝族自治州', '文山壮族苗族自治州', '西双版纳傣族自治州', '大理白族自治州', '德宏傣族景颇族自治州', '怒江傈僳族自治州', '迪庆藏族自治州'],
      西藏: ['拉萨市', '日喀则市', '昌都市', '林芝市', '山南市', '那曲市', '阿里地区'],
      陕西: ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市', '汉中市', '榆林市', '安康市', '商洛市'],
      甘肃: ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '武威市', '张掖市', '平凉市', '酒泉市', '庆阳市', '定西市', '陇南市', '临夏回族自治州', '甘南藏族自治州'],
      青海: ['西宁市', '海东市', '海北藏族自治州', '黄南藏族自治州', '海南藏族自治州', '果洛藏族自治州', '玉树藏族自治州', '海西蒙古族藏族自治州'],
      宁夏: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'],
      新疆: ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市', '昌吉回族自治州', '博尔塔拉蒙古自治州', '巴音郭楞蒙古自治州', '阿克苏地区', '克孜勒苏柯尔克孜自治州', '喀什地区', '和田地区', '伊犁哈萨克自治州', '塔城地区', '阿勒泰地区', '石河子市', '阿拉尔市', '图木舒克市', '五家渠市', '北屯市', '铁门关市', '双河市', '可克达拉市', '昆玉市', '胡杨河市'],
      台湾: ['台北市', '新北市', '桃园市', '台中市', '台南市', '高雄市'],
      香港: ['香港特别行政区'],
      澳门: ['澳门特别行政区']
    }

    const provinceKeys = Object.keys(provinceCityMap)
    const provinceList = provinceKeys.map(key => provinceSuffixMap[key] || key)
    const firstProvinceKey = provinceKeys[0]
    const firstProvinceCities = provinceCityMap[firstProvinceKey] || []

    this.setData({
      provinceCityMap,
      provinceSuffixMap,
      provinceNameMap,
      regionData: [provinceList, firstProvinceCities],
      regionIndex: [0, 0]
    })
  },

  handleRegionColumnChange(e) {
    const column = e.detail.column
    const value = e.detail.value
    const { regionData, provinceCityMap, provinceNameMap, regionIndex } = this.data
    const provinceList = regionData[0]

    if (column === 0) {
      const selectedProvinceWithSuffix = provinceList[value]
      const selectedProvinceKey = provinceNameMap[selectedProvinceWithSuffix] || selectedProvinceWithSuffix
      const cityList = provinceCityMap[selectedProvinceKey] || []
      this.setData({
        regionData: [provinceList, cityList],
        regionIndex: [value, 0]
      })
    } else {
      this.setData({
        regionIndex: [regionIndex[0], value]
      })
    }
  },

  handleRegionChange(e) {
    const index = e.detail.value
    const { regionData } = this.data
    const provinceList = regionData[0]
    const cityList = regionData[1]
    const selectedProvinceWithSuffix = provinceList[index[0]] || ''
    const selectedCity = cityList[index[1]] || ''

    let location = selectedProvinceWithSuffix
    if (selectedCity && selectedCity !== selectedProvinceWithSuffix) {
      location += ' ' + selectedCity
    }

    this.setData({
      regionIndex: index,
      'form.city': location
    })
  },

  handleInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
    if (field === 'merchantName') {
      this.setData({ 'header.name': e.detail.value || '商户申请修改' })
    }
  },

  validateForm() {
    const { merchantName, city, unifiedSocialCreditCode, legalPerson, phone } = this.data.form
    if (!merchantName?.trim()) return wx.showToast({ title: '请输入商户名称', icon: 'none' }), false
    if (!city?.trim()) return wx.showToast({ title: '请选择所在城市', icon: 'none' }), false
    if (!unifiedSocialCreditCode?.trim()) return wx.showToast({ title: '请输入统一社会信用代码', icon: 'none' }), false
    if (unifiedSocialCreditCode.trim().length !== 18) return wx.showToast({ title: '统一社会信用代码须为18位', icon: 'none' }), false
    if (!legalPerson?.trim()) return wx.showToast({ title: '请输入法人姓名', icon: 'none' }), false
    if (!phone?.trim()) return wx.showToast({ title: '请输入联系电话', icon: 'none' }), false
    if (!/^\d{11}$/.test(phone.trim())) return wx.showToast({ title: '联系电话须为11位数字', icon: 'none' }), false
    return true
  },

  submitEdit() {
    if (!this.validateForm()) return
    this.setData({ submitting: true })

    const payload = {
      applicationId: this.data.recordId,
      ...this.data.form
    }

    // 调用新生成的修改接口
    merchantApi.updateMerchantApplication(payload).then(res => {
      this.setData({ submitting: false })
      const { code, msg } = res.data || {}
      if (code === 200) {
        wx.showToast({ title: '修改成功', icon: 'success' })
        setTimeout(() => {
          // 返回上一页并触发刷新
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage && prevPage.refreshData) {
            prevPage.refreshData()
          }
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: msg || '修改失败', icon: 'none' })
      }
    }).catch(err => {
      this.setData({ submitting: false })
      console.error('修改失败:', err)
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  }
})
