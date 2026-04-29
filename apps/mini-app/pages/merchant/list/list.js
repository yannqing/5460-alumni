// pages/merchant/list/list.js
const { merchantApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
  data: {
    // 图标路径
    iconSearch: config.getIconUrl('sslss.png'),
    iconCategory: config.getIconUrl('xx.png'),
    topImageUrl:
      'https://7072-prod-2gtjr12j6ab77902-1373505745.tcb.qcloud.la/cni-alumni/images/assets/list/merchant.png',
    keyword: '',
    conditionDisplayText: '推荐',
    conditionData: [
      ['推荐', '我的收藏', '经营类目', '全部'],
      [''],
    ],
    conditionIndex: [0, 0],
    categoryOptions: [],
    regionDisplayText: '全部城市',
    regionData: [[], []],
    regionIndex: [0, 0],
    provinceCityMap: {},
    provinceSuffixMap: {},
    provinceNameMap: {},
    selectedProvinceWithSuffix: '全部',
    selectedCity: '全部',
    sortDisplayText: '排序方式',
    sortFilterOptions: [
      ['名称排序'],
      ['升序', '降序'],
    ],
    sortFilterIndex: [0, 0],
    filters: [
      { label: '筛选条件', options: ['推荐', '我的收藏', '经营类目', '全部'], selected: 0 },
      { label: '城市', options: ['全部城市'], selected: 0 },
      { label: '排序', options: ['排序方式'], selected: 0 },
    ],
    merchantList: [],
    current: 1,
    pageSize: 9999,
    hasMore: true,
    loading: false,
  },

  onLoad() {
    this.initRegionData()
    this.initConditionData()
    this.loadMerchantList(true)
  },

  initRegionData() {
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
    Object.keys(provinceSuffixMap).forEach(key => { provinceNameMap[provinceSuffixMap[key]] = key })
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
    const provinceList = ['全部', ...provinceKeys.map(key => provinceSuffixMap[key] || key)]
    this.setData({
      provinceCityMap,
      provinceSuffixMap,
      provinceNameMap,
      regionData: [provinceList, ['全部']],
      regionIndex: [0, 0],
      regionDisplayText: '全部城市',
      selectedProvinceWithSuffix: '全部',
      selectedCity: '全部'
    })
  },

  async initConditionData() {
    const categories = await this.loadCategoryOptions()
    this.setData({
      categoryOptions: categories,
      conditionData: [
        ['推荐', '我的收藏', '经营类目', '全部'],
        [''],
      ],
      conditionIndex: [0, 0],
      conditionDisplayText: '推荐',
    })
  },

  async loadCategoryOptions() {
    try {
      const res = await merchantApi.getCategoryTree()
      const body = res.data || {}
      if (body.code !== 200 || !Array.isArray(body.data)) {
        return []
      }
      return body.data
        .map(item => (item && item.name ? String(item.name).trim() : ''))
        .filter(Boolean)
    } catch (e) {
      console.warn('加载经营类目失败', e)
      return []
    }
  },

  onPullDownRefresh() {
    this.loadMerchantList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMerchantList(false)
    }
  },

  async loadMerchantList(reset = false) {
    if (this.data.loading) {
      return
    }
    if (!reset && !this.data.hasMore) {
      return
    }

    this.setData({ loading: true })

    const { keyword, current, pageSize, selectedProvinceWithSuffix, selectedCity } = this.data

    // 构建请求参数
    const params = {
      current: reset ? 1 : current,
      pageSize: pageSize,
      sortField: 'createTime',
      sortOrder: 'descend',
    }

    // 商铺名称搜索
    if (keyword && keyword.trim()) {
      params.merchantName = keyword.trim()
    }

    const { recommend, favoriteOnly, businessCategoryFromCondition } = this.getConditionFilterParams()
    if (recommend === 1) {
      params.recommend = 1
    }
    if (favoriteOnly === 1) {
      params.favoriteOnly = 1
    }
    if (businessCategoryFromCondition) {
      params.businessCategory = businessCategoryFromCondition
    }

    // 城市筛选：全部不传；仅省份传省；省+市则传完整字符串
    if (selectedProvinceWithSuffix && selectedProvinceWithSuffix !== '全部') {
      if (selectedCity && selectedCity !== '全部') {
        params.city = `${selectedProvinceWithSuffix} ${selectedCity}`
      } else {
        params.city = selectedProvinceWithSuffix
      }
    }

    // 关注筛选：点击"我的关注"时会跳转到关注页面，这里不需要设置参数

    try {
      const res = await merchantApi.getMerchantPage(params)
      console.log('商铺列表接口返回:', res)

      if (res.data && res.data.code === 200) {
        const data = res.data.data || {}
        const records = data.records || []

        // 数据映射
        const mappedList = records.map(item => this.mapMerchantItem(item))
        const sortedMappedList = this.sortMerchantListByName(mappedList)

        // 更新列表数据
        const finalList = reset
          ? sortedMappedList
          : this.sortMerchantListByName([...this.data.merchantList, ...sortedMappedList])

        this.setData({
          merchantList: finalList,
          current: reset ? 2 : current + 1,
          hasMore: data.hasNext || false,
          loading: false,
        })

        if (reset) {
          wx.stopPullDownRefresh()
        }
      } else {
        this.setData({ loading: false })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none',
        })
        if (reset) {
          wx.stopPullDownRefresh()
        }
      }
    } catch (error) {
      console.error('加载商铺列表失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
      })
      if (reset) {
        wx.stopPullDownRefresh()
      }
    }
  },

  resolveLogoUrl(logo) {
    if (!logo) {
      return ''
    }
    const s = String(logo).trim().replace(/[`\s]/g, '')
    return s ? config.getImageUrl(s) : ''
  },

  // 优惠券数据映射
  mapCoupons(coupons) {
    if (!coupons || !Array.isArray(coupons)) return []
    return coupons.map(coupon => ({
      couponId: coupon.couponId,
      couponName: coupon.couponName || '',
      couponType: coupon.couponType || 1,
      discountType: coupon.discountType || 1,
      discountValue: coupon.discountValue || 0,
      minSpend: coupon.minSpend || 0,
    }))
  },

  // 数据映射：将后端数据映射为前端所需格式
  mapMerchantItem(item) {
    return {
      merchantId: item.merchantId,
      merchantName: item.merchantName || '未命名商户',
      merchantType: item.merchantType || 0,
      businessCategory: item.businessCategory || '',
      // 与 pages/alumni-association/list/list 一致：无 logo 时用 config.defaultAvatar
      logoUrl: this.resolveLogoUrl(item.logo) || config.defaultAvatar,
      shopCount: item.shopCount || 0,
      totalCouponIssued: item.totalCouponIssued || 0,
      isAlumniCertified: item.isAlumniCertified || 0,
      favoriteCount: item.favoriteCount || 0,
      coupons: this.mapCoupons(item.coupons),
      alumniAssociationId: item.alumniAssociationId || '',
      legalPerson: item.legalPerson || '',
      contactPhone: item.contactPhone || '',
      createTime: item.createTime || '',
      updateTime: item.updateTime || '',
    }
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.loadMerchantList(true)
  },

  onConditionColumnChange(e) {
    const column = e.detail.column
    const value = e.detail.value
    const { conditionData, conditionIndex, categoryOptions } = this.data
    const newConditionIndex = [...conditionIndex]
    newConditionIndex[column] = value

    if (column === 0) {
      const selectedMainOption = conditionData[0][value]
      const secondColumn =
        selectedMainOption === '经营类目'
          ? ['全部', ...categoryOptions]
          : ['']
      newConditionIndex[1] = 0
      this.setData({
        conditionData: [conditionData[0], secondColumn],
        conditionIndex: newConditionIndex,
      })
      return
    }

    this.setData({ conditionIndex: newConditionIndex })
  },

  onConditionChange(e) {
    const index = e.detail.value
    const { conditionData } = this.data
    const mainOption = conditionData[0][index[0]] || '全部'
    const subOption = conditionData[1][index[1]] || ''
    const conditionDisplayText =
      mainOption === '经营类目' && subOption ? `经营类目-${subOption}` : mainOption

    this.setData({
      conditionIndex: index,
      conditionDisplayText,
      current: 1,
    })
    this.loadMerchantList(true)
  },

  getConditionFilterParams() {
    const { conditionData, conditionIndex } = this.data
    const mainOption = conditionData[0][conditionIndex[0]] || '全部'
    const subOption = conditionData[1][conditionIndex[1]] || ''

    const params = {
      recommend: 0,
      favoriteOnly: 0,
      businessCategoryFromCondition: undefined,
    }

    if (mainOption === '推荐') {
      params.recommend = 1
    } else if (mainOption === '我的收藏') {
      params.favoriteOnly = 1
    } else if (mainOption === '经营类目' && subOption && subOption !== '全部') {
      params.businessCategoryFromCondition = subOption
    }

    return params
  },

  sortMerchantListByName(list) {
    if (!Array.isArray(list) || list.length === 0) {
      return []
    }
    const { sortDisplayText, sortFilterIndex } = this.data
    if (sortDisplayText === '排序方式') {
      return list
    }
    const isAsc = sortFilterIndex[1] === 0
    return [...list].sort((a, b) => {
      const nameA = (a.merchantName || '').trim()
      const nameB = (b.merchantName || '').trim()
      return isAsc
        ? nameA.localeCompare(nameB, 'zh')
        : nameB.localeCompare(nameA, 'zh')
    })
  },

  onSortFilterChange(e) {
    const index = e.detail.value // [fieldIdx, orderIdx]
    const { sortFilterOptions } = this.data
    const fieldText = sortFilterOptions[0][index[0]] || '名称排序'
    const orderText = sortFilterOptions[1][index[1]] || '升序'
    this.setData({
      sortFilterIndex: index,
      sortDisplayText: `${fieldText}-${orderText}`,
    })
    if (this.data.merchantList && this.data.merchantList.length > 0) {
      this.setData({
        merchantList: this.sortMerchantListByName(this.data.merchantList),
      })
    }
  },

  onRegionColumnChange(e) {
    const column = e.detail.column
    const value = e.detail.value
    const { regionData, provinceCityMap, provinceNameMap, regionIndex } = this.data
    const newIndex = [...regionIndex]
    newIndex[column] = value

    if (column === 0) {
      const provinceList = regionData[0]
      const selectedProvinceWithSuffix = provinceList[value] || '全部'
      if (selectedProvinceWithSuffix === '全部') {
        this.setData({
          regionData: [provinceList, ['全部']],
          regionIndex: [value, 0],
        })
      } else {
        const selectedProvinceKey = provinceNameMap[selectedProvinceWithSuffix] || selectedProvinceWithSuffix
        const provinceCities = provinceCityMap[selectedProvinceKey] || []
        const isMunicipality = ['北京市', '上海市', '天津市', '重庆市'].includes(selectedProvinceWithSuffix)
        // multiSelector 需要两列；直辖市第二列使用空占位，避免出现“北京市 北京市”
        const cityList = isMunicipality ? [''] : ['全部', ...provinceCities]
        this.setData({
          regionData: [provinceList, cityList],
          regionIndex: [value, 0],
        })
      }
      return
    }

    this.setData({ regionIndex: newIndex })
  },

  onRegionChange(e) {
    const index = e.detail.value
    const { regionData } = this.data
    const provinceList = regionData[0]
    const cityList = regionData[1]
    const selectedProvinceWithSuffix = provinceList[index[0]] || '全部'
    const rawSelectedCity = cityList[index[1]] || '全部'
    const isMunicipality = ['北京市', '上海市', '天津市', '重庆市'].includes(selectedProvinceWithSuffix)
    const selectedCity = isMunicipality ? '全部' : rawSelectedCity

    let regionDisplayText = '全部城市'
    if (selectedProvinceWithSuffix !== '全部') {
      regionDisplayText = selectedCity && selectedCity !== '全部'
        ? `${selectedProvinceWithSuffix} ${selectedCity}`
        : selectedProvinceWithSuffix
    }

    this.setData({
      regionIndex: index,
      selectedProvinceWithSuffix,
      selectedCity,
      regionDisplayText,
      current: 1,
    })
    this.loadMerchantList(true)
  },

  // 商户卡片点击事件
  onMerchantTap(e) {
    const { merchantId } = e.detail
    if (!merchantId) return
    wx.navigateTo({
      url: `/pages/shop/detail/detail?id=${merchantId}`,
    })
  },

  onCouponTap(e) {
    const { couponId } = e.detail
    if (!couponId) return
    wx.navigateTo({
      url: `/pages/coupon/public-detail/detail?id=${encodeURIComponent(String(couponId))}`,
    })
  },
})
