// pages/alumni-association/list/list.js
const { associationApi, schoolApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')
const { FollowTargetType, toggleFollow } = require('../../../utils/followHelper.js')

const DEFAULT_ALUMNI_AVATAR = config.defaultAlumniAvatar

Page({
  data: {
    // 图标路径
    iconSearch: config.getIconUrl('sslss.png'),
    iconSchool: config.getIconUrl('xx.png'),
    iconLocation: config.getIconUrl('position.png'),
    keyword: '',
    filters: [
      { label: '类型', options: ['全部校友会', '地方校友会', '行业校友会', '海外校友会'], selected: 0 },
      { label: '城市', options: ['全部城市'], selected: 0 },
      { label: '排序', options: ['默认排序', '最新成立', '成员最多'], selected: 0 },
      { label: '关注', options: ['全部', '我的关注'], selected: 0 }
    ],
    showFilterOptions: false,
    activeFilterIndex: -1,
    associationList: [],
    current: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,

    // 地区筛选（与母校列表页一致）- 自定义省市级选择器（只到市）
    region: [], // 地区选择器的值 [省, 市]
    regionDisplayText: '全部城市', // 地区显示文本
    location: undefined, // 用于传给后端的合并地区字段
    selectedProvince: undefined, // 选中的省份（不带单位，用于前端过滤）
    selectedProvinceWithSuffix: undefined, // 选中的省份（带单位，用于显示）
    selectedCity: undefined, // 选中的城市
    regionData: [[], []], // 多列选择器的数据 [[省份列表], [城市列表]]
    regionIndex: [0, 0], // 当前选中的索引 [省索引, 市索引]
    provinceCityMap: {}, // 省份城市映射表
    provinceSuffixMap: {}, // 省份名称到带单位名称的映射
    provinceNameMap: {} // 带单位名称到不带单位名称的反向映射
  },

  async onLoad(options) {
    // 初始化省市级数据
    this.initRegionData()
    
    // 确保已登录后再加载数据
    await this.ensureLogin()
    this.loadAssociationList(true)
  },

  // 初始化省市级数据（与母校列表页完全一致）
  initRegionData() {
    // 省份名称到带单位名称的映射
    const provinceSuffixMap = {
      '北京': '北京市',
      '上海': '上海市',
      '天津': '天津市',
      '重庆': '重庆市',
      '河北': '河北省',
      '山西': '山西省',
      '内蒙古': '内蒙古自治区',
      '辽宁': '辽宁省',
      '吉林': '吉林省',
      '黑龙江': '黑龙江省',
      '江苏': '江苏省',
      '浙江': '浙江省',
      '安徽': '安徽省',
      '福建': '福建省',
      '江西': '江西省',
      '山东': '山东省',
      '河南': '河南省',
      '湖北': '湖北省',
      '湖南': '湖南省',
      '广东': '广东省',
      '广西': '广西壮族自治区',
      '海南': '海南省',
      '四川': '四川省',
      '贵州': '贵州省',
      '云南': '云南省',
      '西藏': '西藏自治区',
      '陕西': '陕西省',
      '甘肃': '甘肃省',
      '青海': '青海省',
      '宁夏': '宁夏回族自治区',
      '新疆': '新疆维吾尔自治区',
      '台湾': '台湾省',
      '香港': '香港特别行政区',
      '澳门': '澳门特别行政区'
    }

    // 带单位名称到不带单位名称的反向映射（用于传给后端）
    const provinceNameMap = {}
    Object.keys(provinceSuffixMap).forEach(key => {
      provinceNameMap[provinceSuffixMap[key]] = key
    })

    // 完整的省市级数据（所有34个省级行政区及其所有城市）
    // 直辖市包含所有区级市
    const provinceCityMap = {
      '北京': ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云区', '延庆区'],
      '上海': ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '闵行区', '宝山区', '嘉定区', '浦东新区', '金山区', '松江区', '青浦区', '奉贤区', '崇明区'],
      '天津': ['和平区', '河东区', '河西区', '南开区', '河北区', '红桥区', '东丽区', '西青区', '津南区', '北辰区', '武清区', '宝坻区', '滨海新区', '宁河区', '静海区', '蓟州区'],
      '重庆': ['万州区', '涪陵区', '渝中区', '大渡口区', '江北区', '沙坪坝区', '九龙坡区', '南岸区', '北碚区', '綦江区', '大足区', '渝北区', '巴南区', '黔江区', '长寿区', '江津区', '合川区', '永川区', '南川区', '璧山区', '铜梁区', '潼南区', '荣昌区', '开州区', '梁平区', '武隆区'],
      '河北': ['石家庄市', '唐山市', '秦皇岛市', '邯郸市', '邢台市', '保定市', '张家口市', '承德市', '沧州市', '廊坊市', '衡水市'],
      '山西': ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市', '晋中市', '运城市', '忻州市', '临汾市', '吕梁市'],
      '内蒙古': ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市', '呼伦贝尔市', '巴彦淖尔市', '乌兰察布市', '兴安盟', '锡林郭勒盟', '阿拉善盟'],
      '辽宁': ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市', '锦州市', '营口市', '阜新市', '辽阳市', '盘锦市', '铁岭市', '朝阳市', '葫芦岛市'],
      '吉林': ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市', '松原市', '白城市', '延边朝鲜族自治州'],
      '黑龙江': ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市', '伊春市', '佳木斯市', '七台河市', '牡丹江市', '黑河市', '绥化市', '大兴安岭地区'],
      '江苏': ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市', '连云港市', '淮安市', '盐城市', '扬州市', '镇江市', '泰州市', '宿迁市'],
      '浙江': ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市', '金华市', '衢州市', '舟山市', '台州市', '丽水市'],
      '安徽': ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市', '铜陵市', '安庆市', '黄山市', '滁州市', '阜阳市', '宿州市', '六安市', '亳州市', '池州市', '宣城市'],
      '福建': ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市', '宁德市'],
      '江西': ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市', '赣州市', '吉安市', '宜春市', '抚州市', '上饶市'],
      '山东': ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市', '潍坊市', '济宁市', '泰安市', '威海市', '日照市', '临沂市', '德州市', '聊城市', '滨州市', '菏泽市'],
      '河南': ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市', '新乡市', '焦作市', '濮阳市', '许昌市', '漯河市', '三门峡市', '南阳市', '商丘市', '信阳市', '周口市', '驻马店市', '济源市'],
      '湖北': ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市', '荆门市', '孝感市', '荆州市', '黄冈市', '咸宁市', '随州市', '恩施土家族苗族自治州', '仙桃市', '潜江市', '天门市', '神农架林区'],
      '湖南': ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市', '常德市', '张家界市', '益阳市', '郴州市', '永州市', '怀化市', '娄底市', '湘西土家族苗族自治州'],
      '广东': ['广州市', '韶关市', '深圳市', '珠海市', '汕头市', '佛山市', '江门市', '湛江市', '茂名市', '肇庆市', '惠州市', '梅州市', '汕尾市', '河源市', '阳江市', '清远市', '东莞市', '中山市', '潮州市', '揭阳市', '云浮市'],
      '广西': ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市', '钦州市', '贵港市', '玉林市', '百色市', '贺州市', '河池市', '来宾市', '崇左市'],
      '海南': ['海口市', '三亚市', '三沙市', '儋州市', '五指山市', '琼海市', '文昌市', '万宁市', '东方市', '定安县', '屯昌县', '澄迈县', '临高县', '白沙黎族自治县', '昌江黎族自治县', '乐东黎族自治县', '陵水黎族自治县', '保亭黎族苗族自治县', '琼中黎族苗族自治县'],
      '四川': ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市', '广元市', '遂宁市', '内江市', '乐山市', '南充市', '眉山市', '宜宾市', '广安市', '达州市', '雅安市', '巴中市', '资阳市', '阿坝藏族羌族自治州', '甘孜藏族自治州', '凉山彝族自治州'],
      '贵州': ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市', '黔西南布依族苗族自治州', '黔东南苗族侗族自治州', '黔南布依族苗族自治州'],
      '云南': ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市', '普洱市', '临沧市', '楚雄彝族自治州', '红河哈尼族彝族自治州', '文山壮族苗族自治州', '西双版纳傣族自治州', '大理白族自治州', '德宏傣族景颇族自治州', '怒江傈僳族自治州', '迪庆藏族自治州'],
      '西藏': ['拉萨市', '日喀则市', '昌都市', '林芝市', '山南市', '那曲市', '阿里地区'],
      '陕西': ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市', '汉中市', '榆林市', '安康市', '商洛市'],
      '甘肃': ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '武威市', '张掖市', '平凉市', '酒泉市', '庆阳市', '定西市', '陇南市', '临夏回族自治州', '甘南藏族自治州'],
      '青海': ['西宁市', '海东市', '海北藏族自治州', '黄南藏族自治州', '海南藏族自治州', '果洛藏族自治州', '玉树藏族自治州', '海西蒙古族藏族自治州'],
      '宁夏': ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'],
      '新疆': ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市', '昌吉回族自治州', '博尔塔拉蒙古自治州', '巴音郭楞蒙古自治州', '阿克苏地区', '克孜勒苏柯尔克孜自治州', '喀什地区', '和田地区', '伊犁哈萨克自治州', '塔城地区', '阿勒泰地区', '石河子市', '阿拉尔市', '图木舒克市', '五家渠市', '北屯市', '铁门关市', '双河市', '可克达拉市', '昆玉市', '胡杨河市'],
      '台湾': ['台北市', '新北市', '桃园市', '台中市', '台南市', '高雄市'],
      '香港': ['香港特别行政区'],
      '澳门': ['澳门特别行政区']
    }

    // 提取省份列表，并在开头添加"全部"
    // 显示时使用带单位的名称，但内部仍使用不带单位的名称作为key
    const provinceKeys = Object.keys(provinceCityMap)
    const provinceList = ['全部', ...provinceKeys.map(key => provinceSuffixMap[key] || key)]
    
    // 初始化：第一列是省份（包含"全部"），第二列是"全部"或第一个省份的城市
    // 当选择"全部"省份时，城市列表也显示"全部"
    const firstProvinceCities = ['全部', ...(provinceCityMap[provinceKeys[0]] || [])]
    
    this.setData({
      provinceCityMap: provinceCityMap,
      provinceSuffixMap: provinceSuffixMap, // 省份名称到带单位名称的映射
      provinceNameMap: provinceNameMap, // 带单位名称到不带单位名称的反向映射
      regionData: [provinceList, firstProvinceCities],
      regionIndex: [0, 0] // 默认选中"全部"
    })
  },

  onShow() {
    // 页面显示时重新检查登录状态
    this.ensureLogin().then(() => {
      // 如果列表为空，重新加载
      if (this.data.associationList.length === 0) {
        this.loadAssociationList(true)
      }
    })
  },

  // 确保已登录
  async ensureLogin() {
    const app = getApp()
    const isLogin = app.checkHasLogined()
    
    if (!isLogin) {
      try {
        await app.initApp()
      } catch (error) {
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
        throw error
      }
    }
  },

  // 登录成功回调（由 auth.js 调用）
  onLoginSuccess(userData) {
    // 重新加载数据
    this.loadAssociationList(true)
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true, current: 1 })
    this.loadAssociationList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore()
    }
  },

  // 加载校友会列表
  async loadAssociationList(reset = false) {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const { keyword, filters, current, pageSize } = this.data
      const [typeFilter, cityFilter, sortFilter, followFilter] = filters
      
      // 构建请求参数（与后端接口定义一致）
      // 如果只选择了省份（城市是"全部"），不传 location 参数，在前端过滤
      // 如果选择了具体城市，传 location 参数
      const params = {
        current: reset ? 1 : current,
        pageSize: pageSize,
        // 搜索关键词：校友会名称
        associationName: keyword || undefined,
        // 地区筛选：如果选择了具体城市，传 location；如果只选了省份，不传（在前端过滤）
        location: (this.data.selectedCity && this.data.selectedCity !== '全部') ? this.data.location : undefined,
        // 会长名称（暂不使用）
        presidentUsername: undefined,
        // 排序字段：根据筛选选择
        sortField: sortFilter.selected === 1 ? 'createTime' : (sortFilter.selected === 2 ? 'memberCount' : undefined),
        // 排序顺序
        sortOrder: sortFilter.selected > 0 ? 'descend' : undefined
      }

      // 移除 undefined 参数
      Object.keys(params).forEach(key => {
        if (params[key] === undefined) {
          delete params[key]
        }
      })

      const res = await associationApi.getAssociationList(params)
      
      if (res.data && res.data.code === 200) {
        const responseData = res.data.data || {}
        const list = responseData.records || responseData.list || []
        const total = responseData.total || 0
        const currentPage = responseData.current || 1
        const totalPages = responseData.pages || Math.ceil(total / pageSize)

        // 数据映射（与后端字段保持同步）
        // 后端字段示例（AlumniAssociationListVo）：
        // {
        //   "alumniAssociationId": 123,          // 当前接口返回的校友会ID字段
        //   "associationName": "清华大学北京校友会",
        //   "schoolId": 456,
        //   "platformId": 789,
        //   "presidentUserId": 101,
        //   "contactInfo": "JSON格式",
        //   "location": "北京市朝阳区xxx",
        //   "memberCount": 1000
        // }
        const mappedList = list.map(item => ({
          // ===== 与后端 VO 完全一致的字段（AlumniAssociationListVo）=====
          alumniAssociationId: item.alumniAssociationId,   // 校友会ID
          associationName: item.associationName,           // 校友会名称
          schoolId: item.schoolId,                         // 所属母校ID
          platformId: item.platformId,                     // 所属校处会ID
          presidentUserId: item.presidentUserId,           // 会长用户ID
          contactInfo: item.contactInfo,                   // 联系信息
          location: item.location,                         // 常驻地点
          memberCount: item.memberCount,                   // 会员数量

          // ===== 前端内部使用的通用字段（保持原有逻辑不变）=====
          // 注意：前端统一用字符串形式的 id，避免大整数精度丢失
          id: item.alumniAssociationId != null ? String(item.alumniAssociationId) : '',
          name: item.associationName,
          icon: DEFAULT_ALUMNI_AVATAR, // 后端暂无图标字段，使用默认
          // 下面这些是前端扩展字段，后端暂时没有
          associationCount: 0,
          followCount: 0,
          isFollowed: false,
          isCertified: false,
          schoolName: '' // 需要根据 schoolId 查询
        }))

        // 根据 schoolId 批量补齐 schoolName（并行请求优化）
        const schoolIdSet = new Set(mappedList.map(i => i.schoolId).filter(Boolean))
        const schoolNameMap = {}
        
        // 将所有请求改为并行执行，显著提升加载速度
        const schoolPromises = Array.from(schoolIdSet).map(sid => 
          schoolApi.getSchoolDetail(sid)
            .then(schoolRes => {
              if (schoolRes.data && schoolRes.data.code === 200 && schoolRes.data.data) {
                schoolNameMap[sid] = schoolRes.data.data.schoolName || schoolRes.data.data.name || ''
              }
            })
            .catch(e => {
              // 请求失败时忽略，保持空
            })
        )
        
        // 等待所有学校信息请求完成
        await Promise.all(schoolPromises)
        
        // 填充学校名称
        mappedList.forEach(item => {
          if (item.schoolId && schoolNameMap[item.schoolId]) {
            item.schoolName = schoolNameMap[item.schoolId]
          }
        })

        // 如果只选择了省份（城市是"全部"），在前端根据省份过滤数据
        if (this.data.selectedProvince && (!this.data.selectedCity || this.data.selectedCity === '全部')) {
          const { provinceCityMap } = this.data
          const provinceCities = provinceCityMap[this.data.selectedProvince] || []
          
          // 过滤：location 字段包含该省份下的任何城市名称，或包含省份名称
          const filteredList = mappedList.filter(item => {
            if (!item.location) return false
            
            const location = item.location
            
            // 检查是否包含省份名称（带单位或不带单位）
            if (location.includes(this.data.selectedProvinceWithSuffix) || 
                location.includes(this.data.selectedProvince)) {
              return true
            }
            
            // 检查是否包含该省份下的任何城市名称
            return provinceCities.some(city => location.includes(city))
          })
          
          // 更新分页信息（前端过滤后，总数需要重新计算）
          const filteredTotal = filteredList.length
          const filteredPages = Math.ceil(filteredTotal / pageSize)
          
          if (reset) {
            this.setData({
              associationList: filteredList,
              current: 1,
              hasMore: false, // 前端过滤后，不再支持分页加载更多
              loading: false,
              refreshing: false
            })
          } else {
            this.setData({
              associationList: [...this.data.associationList, ...filteredList],
              current: currentPage,
              hasMore: false, // 前端过滤后，不再支持分页加载更多
              loading: false
            })
          }
          return
        }

        if (reset) {
          this.setData({
            associationList: mappedList,
            current: currentPage,
            hasMore: currentPage < totalPages,
            loading: false,
            refreshing: false
          })
        } else {
          this.setData({
            associationList: [...this.data.associationList, ...mappedList],
            current: currentPage,
            hasMore: currentPage < totalPages,
            loading: false
          })
        }
      } else {
        // API 返回错误
        this.setData({
          loading: false,
          refreshing: false,
          hasMore: false
        })
        wx.showToast({
          title: res.data?.msg || '加载失败',
          icon: 'none'
        })
      }

      wx.stopPullDownRefresh()
    } catch (error) {
      this.setData({
        loading: false,
        refreshing: false,
        hasMore: false
      })
      wx.stopPullDownRefresh()
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }
  },

  // 加载更多
  loadMore() {
    this.loadAssociationList(false)
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    this.setData({ current: 1 })
    this.loadAssociationList(true)
  },

  openFilterOptions(e) {
    const { index } = e.currentTarget.dataset
    // 城市筛选（索引为1）使用多列选择器，不打开下拉面板
    if (index === 1) {
      return
    }
    if (this.data.activeFilterIndex === index && this.data.showFilterOptions) {
      this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
      return
    }
    this.setData({ activeFilterIndex: index, showFilterOptions: true })
  },

  selectFilterOption(e) {
    const { optionIndex } = e.currentTarget.dataset
    const { activeFilterIndex, filters } = this.data
    if (activeFilterIndex === -1) return
    
    filters[activeFilterIndex].selected = optionIndex
    this.setData({
      filters,
      showFilterOptions: false,
      activeFilterIndex: -1,
      current: 1
    })
    this.loadAssociationList(true)
  },

  // 地区选择器列改变（联动选择）- 与母校列表页完全一致
  onRegionColumnChange(e) {
    const column = e.detail.column // 改变的列索引（0: 省, 1: 市）
    const value = e.detail.value // 新选中的索引
    
    const { regionData, provinceCityMap, provinceNameMap, regionIndex } = this.data
    const provinceList = regionData[0]
    
    // 如果改变的是省份列（第一列）
    if (column === 0) {
      // 如果选择的是"全部"（索引0），城市列表也显示"全部"
      if (value === 0) {
        const cityList = ['全部']
        const newRegionData = [provinceList, cityList]
        const newRegionIndex = [0, 0]
        
        this.setData({
          regionData: newRegionData,
          regionIndex: newRegionIndex
        })
      } else {
        // 选择具体省份，城市列表显示"全部" + 该省的所有城市
        // 需要将带单位的省份名称转换为不带单位的key
        const selectedProvinceWithSuffix = provinceList[value]
        const selectedProvinceKey = provinceNameMap[selectedProvinceWithSuffix] || selectedProvinceWithSuffix
        const provinceCities = provinceCityMap[selectedProvinceKey] || []
        const cityList = ['全部', ...provinceCities]
        
        const newRegionData = [provinceList, cityList]
        const newRegionIndex = [value, 0] // 重置城市索引为0（选中"全部"）
        
        this.setData({
          regionData: newRegionData,
          regionIndex: newRegionIndex
        })
      }
    } else if (column === 1) {
      // 如果改变的是城市列（第二列），只更新城市索引
      const newRegionIndex = [regionIndex[0], value]
      this.setData({
        regionIndex: newRegionIndex
      })
    }
  },

  // 选择地区（自定义省市级选择器）- 与母校列表页完全一致
  onRegionChange(e) {
    const index = e.detail.value // [省索引, 市索引]
    const { regionData, provinceNameMap } = this.data
    
    const provinceList = regionData[0]
    const cityList = regionData[1]
    
    // 根据索引获取选中的省和市（带单位的显示名称）
    const selectedProvinceWithSuffix = provinceList[index[0]] || ''
    const selectedCity = cityList[index[1]] || ''
    
    // 将带单位的省份名称转换为不带单位的名称（用于传给后端）
    const selectedProvince = provinceNameMap[selectedProvinceWithSuffix] || selectedProvinceWithSuffix
    
    // 构建地区数组、显示文本和 location 字段
    const region = []
    let regionDisplayText = '全部城市'
    let location = undefined // 用于传给后端的合并字段
    
    // 如果选择的是"全部"省份，不传任何地区参数
    if (selectedProvinceWithSuffix === '全部') {
      regionDisplayText = '全部城市'
      this.setData({
        region: [],
        regionDisplayText: '全部城市',
        regionIndex: index,
        location: undefined,
        selectedProvince: undefined,
        selectedProvinceWithSuffix: undefined,
        selectedCity: undefined,
        current: 1
      })
      this.loadAssociationList(true)
      return
    } else if (selectedProvinceWithSuffix) {
      // 选择了具体省份（传给后端时使用不带单位的名称）
      region.push(selectedProvince)
      
      // 如果选择的是"全部"城市，只传省份（单一选择）
      // 注意：如果只选择省份，不传 location 参数，在前端过滤
      if (selectedCity === '全部' || !selectedCity) {
        regionDisplayText = selectedProvinceWithSuffix // 显示时使用带单位的名称
        location = undefined // 不传 location，在前端过滤
      } else {
        // 选择了具体城市
        region.push(selectedCity)
        regionDisplayText = `${selectedProvinceWithSuffix}${selectedCity}` // 显示时使用带单位的省份名称，不添加空格避免换行
        // location 只传城市名称（数据库中存储的是城市名称，如 "无锡市"）
        // 不传省份名称，因为数据库中可能只存储城市名称
        location = selectedCity
      }
    }
    
    this.setData({
      region: region,
      regionDisplayText: regionDisplayText,
      regionIndex: index,
      location: location, // 存储 location 值用于传给后端
      selectedProvince: selectedProvince, // 存储选中的省份（不带单位，用于前端过滤）
      selectedProvinceWithSuffix: selectedProvinceWithSuffix, // 存储选中的省份（带单位，用于显示和过滤）
      selectedCity: selectedCity || undefined, // 存储选中的城市
      current: 1
    })
    
    // 地区改变后重新加载数据
    this.loadAssociationList(true)
  },

  closeFilterOptions() {
    this.setData({ showFilterOptions: false, activeFilterIndex: -1 })
  },

  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/alumni-association/detail/detail?id=${id}`
    })
  },

  async toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { associationList } = this.data
    const index = associationList.findIndex(item => item.id === id)

    if (index === -1) return

    // 调用通用关注接口
    const result = await toggleFollow(
      followed,
      FollowTargetType.ASSOCIATION, // 2-校友会
      id
    )

    if (result.success) {
      // 更新列表中的关注状态
      associationList[index].isFollowed = !followed
      this.setData({ associationList })

      wx.showToast({
        title: result.message,
        icon: 'success'
      })
    } else {
      wx.showToast({
        title: result.message,
        icon: 'none'
      })
    }
  }
})
