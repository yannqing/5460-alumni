const { schoolApi, userApi, associationApi, fileApi } = require('../../../api/api.js')
const app = getApp()
const config = require('../../../utils/config.js')

// 云托管环境下请求体大小限制较严，超过此阈值先压缩再上传（单位：字节）
const UPLOAD_IMAGE_COMPRESS_THRESHOLD = config.IS_CLOUD_HOST ? 512 * 1024 : 1024 * 1024 // 云托管 512KB，非云托管 1MB

const ALLOWED_DOCUMENT_EXTENSIONS = [
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'txt',
  'md',
  'csv',
  'rtf',
  'odt',
  'ods',
  'odp',
  'zip',
  'rar',
  '7z',
  'tar',
  'gz',
  'jpg',
  'jpeg',
  'png',
  'gif',
  'bmp',
  'webp',
  'svg',
  'mp3',
  'wav',
  'flac',
  'aac',
  'ogg',
  'wma',
  'mp4',
  'avi',
  'mov',
  'wmv',
  'flv',
  'mkv',
  'json',
  'xml',
  'html',
  'htm',
]

/**
 * 若图片超过阈值则压缩后返回路径，否则返回原路径（适配云托管）
 * @param {string} tempFilePath 本地临时路径
 * @param {number} fileSize 文件大小（字节）
 * @returns {Promise<string>} 用于上传的本地路径
 */
function getImagePathForUpload(tempFilePath, fileSize) {
  if (fileSize <= UPLOAD_IMAGE_COMPRESS_THRESHOLD) {
    return Promise.resolve(tempFilePath)
  }
  return new Promise(resolve => {
    const quality = Math.max(
      20,
      Math.min(80, Math.floor((UPLOAD_IMAGE_COMPRESS_THRESHOLD / fileSize) * 80))
    )
    wx.compressImage({
      src: tempFilePath,
      quality,
      success: res => resolve(res.tempFilePath),
      fail: () => resolve(tempFilePath), // 压缩失败则用原图
    })
  })
}

// 防抖函数
function debounce(fn, delay) {
  let timer = null
  return function () {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(function () {
      fn.apply(context, args)
    }, delay)
  }
}

Page({
  data: {
    formData: {
      associationName: '',
      schoolId: '',
      schoolName: '',
      previousName: '',
      chargeName: '',
      contactInfo: '',
      msocialAffiliation: '',
      zhName: '',
      zhRole: '',
      zhPhone: '',
      zhSocialAffiliation: '',
      zhWxId: '',
      logo: '',

      applicationReason: '',
      associationProfile: '',
      presidentWxId: '',
      location: '',
      coverageArea: '', // 覆盖区域（选填）
      logoType: 'default', // logo来源类型: default, school, upload
      platformId: '', // 所属校促会（选填，编辑时回填）
    },
    schoolLogoUrl: '',
    defaultAlumniLogo: '',
    // 搜索结果列表
    schoolList: [],

    // 架构模板相关
    templateList: [],
    templateIndex: -1,
    selectedTemplate: null,
    templateTreeData: [], // 树形结构预览数据

    // 地区选择器
    regionData: [[], []], // 多列选择器的数据 [[省份列表], [城市列表]]
    regionIndex: [0, 0], // 当前选中的索引 [省索引, 市索引]
    provinceCityMap: {}, // 省份城市映射表
    provinceSuffixMap: {}, // 省份名称到带单位名称的映射
    provinceNameMap: {}, // 带单位名称到不带单位名称的反向映射

    // 控制显示
    showSchoolResults: false,

    // 其他成员列表
    members: [],

    // 申请材料
    attachments: [],

    // 背景图
    bgImages: [],

    loading: false,
    submitting: false,
    isEditMode: false,
    editApplicationId: '',
    pageTitle: '创建校友会',
    fromMyRecord: false,
    defaultAvatar: config.defaultAvatar,
    headerImageUrl: `https://${config.DOMAIN}/upload/images/2026/02/09/9f328fe3-fcad-4019-a379-1a6db70f3a5d.png`,
  },

  async onLoad(options) {
    // 创建学校搜索防抖函数（负责人姓名等由用户直接填写，不使用校友列表选择）
    this.searchSchoolDebounced = debounce(this.searchSchool, 500)

    const defaultLogoUrl = '/assets/avatar/avatar.jpg'
    const mode = options?.mode || ''
    const applicationId = options?.applicationId || ''
    const isEdit = mode === 'edit' && applicationId

    this.setData({
      defaultAlumniLogo: defaultLogoUrl,
      isEditMode: !!isEdit,
      editApplicationId: isEdit ? applicationId : '',
      pageTitle: isEdit ? '编辑创建申请' : '创建校友会',
      fromMyRecord: options?.fromMyRecord === '1',
    })

    // 初始化省市级数据
    this.initRegionData()

    if (isEdit) {
      // 编辑模式也先加载当前登录用户，兜底拿到 zhWxId/presidentWxId
      await this.loadInitialData()
      await this.loadTemplateList()
      await this.loadApplicationForEdit(applicationId)
    } else {
      await this.loadInitialData()
      await this.loadTemplateList()
      this.setData({
        'formData.logo': defaultLogoUrl,
        members: [{ name: '', role: '会长', affiliation: '', phone: '' }],
      })
    }
  },

  // 加载架构模板列表
  async loadTemplateList() {
    try {
      // organizeType: 0-校友会，1-校促会，2-商户
      const res = await associationApi.getOrganizeTemplateList({ organizeType: 0 })
      if (res.data && res.data.code === 200) {
        const templateList = res.data.data || []
        // 找到默认模板的索引
        let defaultIndex = templateList.findIndex(item => item.isDefault === 1)
        if (defaultIndex === -1 && templateList.length > 0) {
          defaultIndex = 0 // 如果没有默认模板，默认选中第一个
        }

        const defaultTemplate = defaultIndex >= 0 ? templateList[defaultIndex] : null
        // API 返回的 templateContent 已经是树形结构，直接使用
        const treeData =
          defaultTemplate && defaultTemplate.templateContent ? defaultTemplate.templateContent : []

        this.setData({
          templateList: templateList,
          templateIndex: defaultIndex,
          selectedTemplate: defaultTemplate,
          templateTreeData: treeData,
        })

        console.log('加载模板列表成功，树形数据:', treeData)
      }
    } catch (e) {
      console.error('加载架构模板列表失败', e)
    }
  },

  // 处理架构模板选择
  handleTemplateChange(e) {
    const index = e.detail.value
    const template = this.data.templateList[index]
    if (template) {
      // API 返回的 templateContent 已经是树形结构，直接使用
      const treeData = template.templateContent || []
      this.setData({
        templateIndex: index,
        selectedTemplate: template,
        templateTreeData: treeData,
      })
      console.log('选择模板，树形数据:', treeData)
    }
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

  // 处理常驻地点选择
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
      'formData.location': location,
    })
  },

  async loadInitialData() {
    this.setData({ loading: true })
    try {
      await this.loadUserInfo()
    } catch (error) {
      console.error('加载初始化数据失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 从「我的申请」进入编辑：拉取待审核申请并回填表单 */
  async loadApplicationForEdit(applicationId) {
    this.setData({ loading: true })
    wx.showLoading({ title: '加载中...', mask: true })
    try {
      const res = await associationApi.getApplicationDetail(applicationId)
      if (!res.data || res.data.code !== 200 || !res.data.data) {
        wx.showToast({ title: res.data?.msg || res.data?.message || '加载失败', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }
      const d = res.data.data
      const st = d.applicationStatus
      if (st !== 0 && st !== '0') {
        wx.showToast({ title: '仅待审核的申请可编辑', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
        return
      }

      const schoolId = d.schoolId != null ? String(d.schoolId) : ''
      const schoolName = d.schoolInfo?.schoolName || ''
      const schoolLogoUrl = d.schoolInfo?.logo
        ? config.getImageUrl(d.schoolInfo.logo)
        : config.defaultSchoolAvatar

      const leader = {
        name: d.chargeName || '',
        role: d.chargeRole || '会长',
        phone: d.contactInfo || '',
        affiliation: d.msocialAffiliation || '',
      }
      let extra = []
      if (d.initialMembers) {
        try {
          const parsed =
            typeof d.initialMembers === 'string' ? JSON.parse(d.initialMembers) : d.initialMembers
          if (Array.isArray(parsed)) {
            extra = parsed.map(m => ({
              name: m.name || '',
              role: m.role || '',
              phone: m.phone || '',
              affiliation: m.affiliation || '',
            }))
          }
        } catch (e) {
          console.error('parse initialMembers', e)
        }
      }
      const members = [leader, ...extra]

      let bgImages = []
      if (d.bgImg && String(d.bgImg).trim()) {
        try {
          const raw = String(d.bgImg).trim()
          const arr = raw.startsWith('[') ? JSON.parse(raw) : [raw]
          const list = Array.isArray(arr) ? arr : [raw]
          bgImages = list.filter(Boolean).map(u => ({ url: config.getImageUrl(u) }))
        } catch (e) {
          bgImages = [{ url: config.getImageUrl(d.bgImg) }]
        }
      }

      let logoType = 'default'
      let logo = this.data.defaultAlumniLogo
      if (d.logo && String(d.logo).trim()) {
        logoType = 'upload'
        logo = config.getImageUrl(d.logo)
      }

      const attachments = (d.attachments || []).map(f => {
        const id = f.fileId != null ? String(f.fileId) : ''
        const rawUrl = f.fileUrl || ''
        const url = rawUrl ? config.getImageUrl(rawUrl) : ''
        const ft = (f.fileType || '').toLowerCase()
        const ext = (f.fileExtension || '').toLowerCase()
        const isImage =
          ft.includes('image') || ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
        return {
          id,
          name: f.displayName || '附件',
          url: isImage ? url : '',
          type: isImage ? 'image' : undefined,
        }
      })

      const tid = d.templateId
      let templateIndex = this.data.templateIndex
      let selectedTemplate = this.data.selectedTemplate
      let templateTreeData = this.data.templateTreeData
      if (tid != null && this.data.templateList && this.data.templateList.length) {
        const idx = this.data.templateList.findIndex(t => String(t.templateId) === String(tid))
        if (idx >= 0) {
          templateIndex = idx
          selectedTemplate = this.data.templateList[idx]
          templateTreeData = selectedTemplate.templateContent || []
        }
      }

      const currentWxId = this.data.formData.presidentWxId || this.data.formData.zhWxId || ''
      const zhWxStr = d.zhWxId != null ? String(d.zhWxId) : currentWxId

      // 初始化地区选择器的索引
      let regionIndex = [0, 0]
      let regionData = this.data.regionData
      if (d.location) {
        const locParts = String(d.location).split(' ')
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

      this.setData({
        schoolLogoUrl,
        bgImages,
        attachments,
        members,
        templateIndex,
        selectedTemplate,
        templateTreeData,
        regionIndex,
        regionData,
        'formData.associationName': d.associationName || '',
        'formData.schoolId': schoolId,
        'formData.schoolName': schoolName,
        'formData.platformId': d.platformId != null ? String(d.platformId) : '',
        'formData.location': d.location || '',
        'formData.coverageArea': d.coverageArea || '',
        'formData.applicationReason': d.applicationReason || '',
        'formData.associationProfile': d.associationProfile || '',
        'formData.zhName': d.zhName || '',
        'formData.zhRole': d.zhRole || '',
        'formData.zhPhone': d.zhPhone || '',
        'formData.zhSocialAffiliation': d.zhSocialAffiliation || '',
        'formData.zhWxId': zhWxStr,
        'formData.presidentWxId': zhWxStr,
        'formData.logoType': logoType,
        'formData.logo': logo,
      })
    } catch (e) {
      console.error('[create] loadApplicationForEdit', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
    } finally {
      wx.hideLoading()
      this.setData({ loading: false })
    }
  },

  async loadUserInfo() {
    try {
      const res = await userApi.getUserInfo()
      if (res.data && res.data.code === 200) {
        const userInfo = res.data.data
        console.log('获取到的用户信息:', userInfo)

        // 尝试获取用户的 wx_id，检查所有可能的字段名
        const userId =
          userInfo.wxId || userInfo.wx_id || userInfo.userId || userInfo.user_id || userInfo.id
        console.log('获取到的用户ID:', userId)

        // 将当前登录用户信息填入驻会代表字段
        this.setData({
          'formData.zhName': userInfo.name || userInfo.realName || userInfo.nickname || '',
          'formData.zhPhone': userInfo.phone || userInfo.mobile || '',
          'formData.zhSocialAffiliation':
            userInfo.socialAffiliation || userInfo.social_affiliation || '',
          'formData.zhWxId': userId || '',
          'formData.presidentWxId': userId,
        })
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  },

  // --- 下拉框控制 ---

  closeAllDropdowns() {
    this.setData({
      showSchoolResults: false,
    })
  },

  preventBubble() {
    // 阻止冒泡
  },

  // 通用输入处理
  handleInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`formData.${field}`]: e.detail.value,
    })
  },

  // 处理校友会Logo类型切换
  handleLogoTypeChange(e) {
    const type = e.detail.value
    let logo = ''

    if (type === 'default') {
      logo = this.data.defaultAlumniLogo
    } else if (type === 'school') {
      // 只有当有 schoolId 时才展示相应学校的 logo，否则为空
      if (this.data.formData.schoolId) {
        logo = this.data.schoolLogoUrl || config.defaultSchoolAvatar
      } else {
        logo = '' // 未选学校时不展示预览
      }
    } else if (type === 'upload') {
      // 如果切到上传,保留原来的logo或者为空
      logo = this.data.formData.logoType === 'upload' ? this.data.formData.logo : ''
    }

    this.setData({
      'formData.logoType': type,
      'formData.logo': logo,
    })
  },

  // --- 学校搜索处理 ---

  handleSchoolInput(e) {
    const value = e.detail.value
    this.setData({
      'formData.schoolName': value,
      'formData.schoolId': '', // 清空ID，因为修改了名称
      showSchoolResults: true,
    })

    if (value.trim()) {
      this.searchSchoolDebounced(value)
    } else {
      this.setData({ schoolList: [] })
    }
  },

  handleSchoolFocus() {
    // 聚焦时如果已有内容，也展示结果
    if (this.data.formData.schoolName) {
      this.setData({ showSchoolResults: true })
      if (this.data.schoolList.length === 0) {
        this.searchSchool(this.data.formData.schoolName)
      }
    }
  },

  async searchSchool(keyword) {
    if (!keyword) {
      return
    }
    try {
      const res = await schoolApi.getSchoolPage({
        current: 1,
        pageSize: 20,
        schoolName: keyword.trim(),
        previousName: keyword.trim(),
      })
      if (res.data && res.data.code === 200) {
        this.setData({
          schoolList: res.data.data.records || [],
        })
      }
    } catch (e) {
      console.error('搜索学校失败', e)
    }
  },

  selectSchool(e) {
    const index = e.currentTarget.dataset.index
    const school = this.data.schoolList[index]

    // 提取学校Logo，如果学校没有Logo则使用系统默认母校图标
    const schoolLogo = school.logo ? config.getImageUrl(school.logo) : config.defaultSchoolAvatar

    const updateData = {
      'formData.schoolId': school.schoolId,
      'formData.schoolName': school.schoolName,
      schoolLogoUrl: schoolLogo,
      showSchoolResults: false,
    }

    // 如果当前选中的是"使用学校logo", 则实时更新预览图和提交用的logo地址
    if (this.data.formData.logoType === 'school') {
      updateData['formData.logo'] = schoolLogo
    }

    this.setData(updateData)
  },

  // --- 成员和其他逻辑 ---

  addMember() {
    const members = this.data.members
    members.push({ name: '', role: '', affiliation: '', phone: '' })
    this.setData({ members })
  },

  deleteMember(e) {
    const index = e.currentTarget.dataset.index
    // 第一个成员是主要负责人，不能删除
    if (index === 0) {
      wx.showToast({ title: '主要负责人不能删除', icon: 'none' })
      return
    }
    const members = this.data.members
    members.splice(index, 1)
    this.setData({ members })
  },

  handleMemberInput(e) {
    const { index, field } = e.currentTarget.dataset
    const value = e.detail.value
    const members = this.data.members
    members[index][field] = value
    this.setData({ members })
  },

  // --- 校友会logo上传处理 ---

  async chooseLogo() {
    // 如果当前不是上传模式,点击选择图片会自动切到上传模式
    if (this.data.formData.logoType !== 'upload') {
      this.setData({
        'formData.logoType': 'upload',
      })
    }
    try {
      // 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 1,
          mediaType: ['image'],
          sizeType: ['compressed'], // 优先选压缩图，便于云托管上传
          success: resolve,
          fail: reject,
        })
      })

      const tempFilePath = chooseRes.tempFiles?.[0]?.tempFilePath
      if (!tempFilePath) {
        return
      }

      // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
      const fileSize = chooseRes.tempFiles?.[0]?.size || 0
      const maxSize = 10 * 1024 * 1024 // 10MB
      if (fileSize > maxSize) {
        wx.showToast({
          title: '图片大小不能超过10MB',
          icon: 'none',
        })
        return
      }

      wx.showLoading({ title: '上传中...', mask: true })

      const originalName = chooseRes.tempFiles?.[0]?.name || 'logo.jpg'
      const uploadRes = await fileApi.uploadImage(tempFilePath, originalName, fileSize)

      if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
        // 获取返回的图片URL
        const rawImageUrl = uploadRes.data.fileUrl || ''
        if (rawImageUrl) {
          // 使用 config.getImageUrl 处理图片URL，确保是完整的URL
          const config = require('../../../utils/config.js')
          const imageUrl = config.getImageUrl(rawImageUrl)
          // 更新表单中的logo URL
          this.setData({ 'formData.logo': imageUrl })
          wx.showToast({
            title: '上传成功',
            icon: 'success',
          })
        } else {
          wx.showToast({
            title: '上传失败，未获取到图片地址',
            icon: 'none',
          })
        }
      } else {
        wx.showToast({
          title: uploadRes?.msg || '上传失败',
          icon: 'none',
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000,
      })
    } finally {
      wx.hideLoading()
    }
  },

  // 删除已上传的logo
  deleteLogo() {
    this.setData({
      'formData.logo': '',
    })
  },

  // --- 申请材料上传处理 ---

  async chooseAttachment() {
    try {
      // 使用 wx.chooseMessageFile 选择文档文件
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMessageFile({
          count: 5,
          type: 'file',
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) {
        return
      }

      // 显示上传中提示
      wx.showLoading({
        title: '上传中...',
        mask: true,
      })

      const attachments = [...this.data.attachments]

      // 逐个上传文件
      for (const file of tempFiles) {
        console.log(
          '[上传调试] file:',
          JSON.stringify({ name: file.name, size: file.size, path: file.path, type: file.type })
        )
        const tempFilePath = file.path
        const originalName = file.name || 'document'

        // 获取文件扩展名
        const ext = originalName.split('.').pop().toLowerCase()
        console.log('[上传调试] ext:', ext, '是否支持:', ALLOWED_DOCUMENT_EXTENSIONS.includes(ext))

        // 检查文件格式是否支持
        if (!ALLOWED_DOCUMENT_EXTENSIONS.includes(ext)) {
          this._uploadFormatError = true
          break
        }

        // 检查文件大小（5MB = 5 * 1024 * 1024 字节）
        const fileSize = file.size || 0
        const maxSize = 5 * 1024 * 1024 // 5MB
        if (fileSize > maxSize) {
          this._uploadSizeError = true
          break
        }

        // 上传文档文件
        const uploadRes = await fileApi.uploadDocument(tempFilePath, originalName)

        console.log('上传文档结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息
          const fileId = uploadRes.data.fileId || uploadRes.data.id || ''
          const fileName = uploadRes.data.fileName || originalName
          console.log('获取到的fileId:', fileId)

          if (fileId) {
            attachments.push({
              id: fileId,
              name: fileName,
            })
          } else {
            wx.showToast({
              title: `${originalName} 上传成功但未获取到文件ID`,
              icon: 'none',
            })
          }
        } else {
          wx.showToast({
            title: `${originalName} 上传失败: ${uploadRes?.msg || '未知错误'}`,
            icon: 'none',
          })
        }
      }

      wx.hideLoading()
      if (this._uploadFormatError) {
        this._uploadFormatError = false
        wx.showToast({
          title: '您上传的文件格式不支持',
          icon: 'none',
          duration: 2000,
        })
      } else if (this._uploadSizeError) {
        this._uploadSizeError = false
        wx.showToast({
          title: '请上传小于5MB的文件',
          icon: 'none',
          duration: 2000,
        })
      } else if (attachments.length > this.data.attachments.length) {
        this.setData({ attachments })
        wx.showToast({
          title: '上传成功',
          icon: 'success',
        })
      } else {
        wx.showToast({
          title: '上传失败',
          icon: 'none',
        })
      }
    } catch (error) {
      wx.hideLoading()
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000,
      })
    }
  },

  deleteAttachment(e) {
    const index = e.currentTarget.dataset.index
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },

  previewAttachmentImage(e) {
    const index = e.currentTarget.dataset.index
    const imageAttachments = this.data.attachments.filter(a => a.type === 'image')
    const urls = imageAttachments.map(a => a.url)
    // 找到当前图片在图片附件列表中的位置
    const currentAttachment = this.data.attachments[index]
    const imageIndex = imageAttachments.indexOf(currentAttachment)
    wx.previewImage({
      current: urls[imageIndex >= 0 ? imageIndex : 0],
      urls: urls,
    })
  },

  // 选择图片作为附件
  async chooseAttachmentImage() {
    try {
      // 使用 wx.chooseMedia 选择图片
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 9,
          mediaType: ['image'],
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) {
        return
      }

      // 显示上传中提示
      wx.showLoading({
        title: '上传中...',
        mask: true,
      })

      const attachments = [...this.data.attachments]

      // 逐个上传图片
      for (const file of tempFiles) {
        const tempFilePath = file.tempFilePath
        // 从路径中提取文件名
        const pathParts = tempFilePath.split('/')
        const originalName = pathParts[pathParts.length - 1] || 'image.jpg'

        // 检查文件大小（10MB = 10 * 1024 * 1024 字节）
        const fileSize = file.size || 0
        const maxSize = 10 * 1024 * 1024 // 10MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: `图片大小不能超过10MB`,
            icon: 'none',
          })
          continue
        }

        // 上传图片文件
        const uploadRes = await fileApi.uploadImage(tempFilePath, originalName)

        console.log('上传图片结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息
          const fileId = uploadRes.data.fileId || uploadRes.data.id || ''
          const fileName = uploadRes.data.fileName || originalName
          console.log('获取到的fileId:', fileId)

          if (fileId) {
            attachments.push({
              id: fileId,
              name: fileName,
              url: uploadRes.data.fileUrl || '',
              type: 'image',
            })
          } else {
            wx.showToast({
              title: `图片上传成功但未获取到文件ID`,
              icon: 'none',
            })
          }
        } else {
          wx.showToast({
            title: `图片上传失败: ${uploadRes?.msg || '未知错误'}`,
            icon: 'none',
          })
        }
      }

      if (attachments.length > this.data.attachments.length) {
        this.setData({ attachments })
        wx.showToast({
          title: '上传成功',
          icon: 'success',
        })
      } else {
        wx.showToast({
          title: '上传失败',
          icon: 'none',
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000,
      })
    } finally {
      wx.hideLoading()
    }
  },

  // --- 背景图上传处理 ---

  async chooseBgImage() {
    try {
      const chooseRes = await new Promise((resolve, reject) => {
        wx.chooseMedia({
          count: 5,
          mediaType: ['image'],
          sizeType: ['compressed'], // 优先选压缩图，便于云托管上传
          success: resolve,
          fail: reject,
        })
      })

      const tempFiles = chooseRes.tempFiles
      if (!tempFiles || tempFiles.length === 0) {
        return
      }

      wx.showLoading({ title: '处理中...', mask: true })

      const bgImages = [...this.data.bgImages]

      for (const file of tempFiles) {
        const tempFilePath = file.tempFilePath
        const fileSize = file.size || 0
        const maxSize = 10 * 1024 * 1024 // 10MB
        if (fileSize > maxSize) {
          wx.showToast({
            title: '图片大小不能超过10MB',
            icon: 'none',
          })
          continue
        }

        wx.showLoading({ title: '上传中...', mask: true })

        const uploadRes = await fileApi.uploadImage(tempFilePath, null, fileSize)

        console.log('上传背景图结果:', uploadRes)

        if (uploadRes && uploadRes.code === 200 && uploadRes.data) {
          // 获取返回的文件信息，确保获取到fileUrl
          const fileUrl = uploadRes.data.fileUrl || ''
          console.log('获取到的fileUrl:', fileUrl)

          if (fileUrl) {
            bgImages.push({
              url: fileUrl,
              name: uploadRes.data.fileName || 'bg-image.jpg',
            })
          } else {
            wx.showToast({
              title: '上传成功但未获取到文件URL',
              icon: 'none',
            })
          }
        } else {
          wx.showToast({
            title: `上传失败: ${uploadRes?.msg || '未知错误'}`,
            icon: 'none',
          })
        }
      }

      if (bgImages.length > this.data.bgImages.length) {
        this.setData({ bgImages })
        wx.showToast({
          title: '上传成功',
          icon: 'success',
        })
      } else {
        wx.showToast({
          title: '上传失败',
          icon: 'none',
        })
      }
    } catch (error) {
      // 显示具体的错误信息
      const errorMsg = error?.msg || error?.message || '上传失败，请重试'
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2000,
      })
    } finally {
      wx.hideLoading()
    }
  },

  deleteBgImage(e) {
    const index = e.currentTarget.dataset.index
    const bgImages = [...this.data.bgImages]
    bgImages.splice(index, 1)
    this.setData({ bgImages })
  },

  previewBgImage(e) {
    const index = e.currentTarget.dataset.index
    const urls = this.data.bgImages.map(item => item.url)
    wx.previewImage({
      current: urls[index],
      urls: urls,
    })
  },

  async submitForm() {
    if (this.data.submitting) {
      return
    }

    const { formData, members } = this.data

    if (!formData.associationName) {
      wx.showToast({ title: '请输入校友会名称', icon: 'none' })
      return
    }
    if (!formData.schoolId) {
      wx.showToast({ title: '请选择并点击学校', icon: 'none' })
      return
    }
    if (!formData.location) {
      wx.showToast({ title: '请输入校友会办公地点', icon: 'none' })
      return
    }
    if (!formData.zhName) {
      wx.showToast({ title: '请输入联系人姓名', icon: 'none' })
      return
    }

    // 提取申请材料的ID，使用字符串类型
    const attachmentIds = this.data.attachments.map(a => {
      const id = a.id || ''
      console.log('原始文件ID:', id, '类型:', typeof id)
      console.log('使用字符串类型文件ID:', id)
      return id
    })
    console.log('最终attachmentIds:', attachmentIds)

    // 提取背景图的URL，直接使用数组形式
    const bgImgArray = this.data.bgImages.map(a => {
      const url = a.url || ''
      return url
    })
    const bgImg = bgImgArray.length > 0 ? bgImgArray : undefined
    console.log('最终bgImg:', bgImg)

    const submitData = {
      associationName: formData.associationName,
      schoolId: formData.schoolId,
      attachmentIds: attachmentIds.length > 0 ? attachmentIds : undefined,
      // 联系人信息（用于后续发布时的角色分配）
      zhName: formData.zhName,
      zhRole: formData.zhRole,
      zhPhone: formData.zhPhone,
      zhSocialAffiliation: formData.zhSocialAffiliation,
    }

    // 补充联系人微信ID（从登录用户信息获取）
    if (formData.zhWxId) {
      submitData.zhWxId = formData.zhWxId
    }

    if (formData.location) {
      submitData.location = formData.location
    }

    if (bgImg !== undefined) {
      submitData.bgImg = bgImg
    }

    // 添加架构模板ID
    if (this.data.selectedTemplate && this.data.selectedTemplate.templateId) {
      submitData.templateId = this.data.selectedTemplate.templateId
    }

    console.log('最终提交数据:', submitData)

    this.setData({ submitting: true })

    try {
      const isEdit = this.data.isEditMode && this.data.editApplicationId
      let res
      if (isEdit) {
        const updateBody = {
          applicationId: this.data.editApplicationId,
          ...submitData,
        }
        res = await associationApi.updateApplication(updateBody)
      } else {
        res = await associationApi.applyCreateAssociation(submitData)
      }

      if (res.data && res.data.code === 200) {
        wx.showToast({
          title: isEdit ? '已保存' : '申请提交成功，请等待审核',
          icon: 'success',
          duration: 2000,
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else if (res.data && res.data.code === 50006) {
        // 处理重复提交的情况
        wx.showToast({
          title: res.data.msg || '该学校已存在校友会或待审核申请，请勿重复提交',
          icon: 'none',
        })
      } else {
        wx.showToast({
          title: res.data?.message || '提交失败',
          icon: 'none',
        })
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none',
      })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 跳转到反馈页面
  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback?type=1&title=' + encodeURIComponent('创建校友会遇到问题'),
    })
  },
})
