// pages/school/list/list.js
const { schoolApi } = require('../../../api/index.js')

Page({
  data: {
    // 搜索关键词
    keyword: '',

    // 筛选条件
    filterType: 'all', // all: 全部, followed: 已关注
    sortType: 'default', // default: 默认, alumni: 校友数, association: 校友会数, name: 名称
    cityFilter: '', // 城市筛选

    // 列表数据
    schoolList: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    refreshing: false,

    // 显示筛选面板
    showFilter: false,
    showSort: false,

    // 城市列表
    cityList: ['全部', '北京', '上海', '江苏', '浙江', '广东', '四川', '湖北', '陕西'],
    selectedCity: '全部',

    // 排序选项
    sortOptions: [
      { value: 'default', label: '默认排序' },
      { value: 'alumni', label: '校友数量' },
      { value: 'association', label: '校友会数量' },
      { value: 'name', label: '名称排序' }
    ],
    selectedSort: '默认排序'
  },

  onLoad(options) {
    this.loadSchoolList(true)
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true, page: 1 })
    this.loadSchoolList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore()
    }
  },

  // 加载学校列表
  loadSchoolList(reset = false) {
    if (this.data.loading) return

    this.setData({ loading: true })

    // 模拟数据
    const mockData = [
      {
        id: 1,
        name: '南京大学',
        icon: '/assets/logo/njdx.jpg',
        location: '江苏省南京市',
        alumniCount: 12580,
        associationCount: 156,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: ['金陵大学'],
        description: '南京大学是一所历史悠久、声誉卓著的百年名校'
      },
      {
        id: 2,
        name: '浙江大学',
        icon: '/assets/logo/njdx.jpg',
        location: '浙江省杭州市',
        alumniCount: 15620,
        associationCount: 189,
        isFollowed: true,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: [],
        description: '浙江大学是一所历史悠久、声誉卓著的高等学府'
      },
      {
        id: 3,
        name: '复旦大学',
        icon: '/assets/logo/njdx.jpg',
        location: '上海市',
        alumniCount: 14230,
        associationCount: 178,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: [],
        description: '复旦大学是中国人自主创办的第一所高等院校'
      },
      {
        id: 4,
        name: '上海交通大学',
        icon: '/assets/logo/njdx.jpg',
        location: '上海市',
        alumniCount: 16500,
        associationCount: 195,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: ['南洋公学'],
        description: '上海交通大学是我国历史最悠久、享誉海内外的著名高等学府之一'
      },
      {
        id: 5,
        name: '同济大学',
        icon: '/assets/logo/njdx.jpg',
        location: '上海市',
        alumniCount: 11200,
        associationCount: 142,
        isFollowed: false,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: [],
        description: '同济大学历史悠久、声誉卓著，是中国最早的国立大学之一'
      },
      {
        id: 6,
        name: '中国科学技术大学',
        icon: '/assets/logo/njdx.jpg',
        location: '安徽省合肥市',
        alumniCount: 9800,
        associationCount: 128,
        isFollowed: true,
        isCertified: true,
        tags: ['985', '211', '双一流'],
        oldNames: [],
        description: '中国科学技术大学是中国科学院所属的一所以前沿科学和高新技术为主的大学'
      }
    ]

    setTimeout(() => {
      if (reset) {
        this.setData({
          schoolList: mockData,
          page: 1,
          hasMore: true,
          loading: false,
          refreshing: false
        })
      } else {
        this.setData({
          schoolList: [...this.data.schoolList, ...mockData],
          page: this.data.page + 1,
          loading: false
        })
      }

      wx.stopPullDownRefresh()
    }, 500)
  },

  // 加载更多
  loadMore() {
    this.loadSchoolList(false)
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    console.log('搜索:', this.data.keyword)
    this.setData({ page: 1 })
    this.loadSchoolList(true)
  },

  // 显示筛选
  showFilterPanel() {
    this.setData({ showFilter: true })
  },

  // 隐藏筛选
  hideFilterPanel() {
    this.setData({ showFilter: false })
  },

  // 选择城市
  onCityChange(e) {
    const city = e.currentTarget.dataset.city
    this.setData({
      selectedCity: city,
      cityFilter: city === '全部' ? '' : city
    })
  },

  // 确认筛选
  confirmFilter() {
    this.setData({
      showFilter: false,
      page: 1
    })
    this.loadSchoolList(true)
  },

  // 显示排序
  showSortPanel() {
    this.setData({ showSort: true })
  },

  // 隐藏排序
  hideSortPanel() {
    this.setData({ showSort: false })
  },

  // 选择排序
  onSortChange(e) {
    const { value, label } = e.currentTarget.dataset
    this.setData({
      sortType: value,
      selectedSort: label,
      showSort: false,
      page: 1
    })
    this.loadSchoolList(true)
  },

  // 切换关注筛选
  toggleFollowFilter() {
    const filterType = this.data.filterType === 'all' ? 'followed' : 'all'
    this.setData({
      filterType,
      page: 1
    })
    this.loadSchoolList(true)
  },

  // 查看详情
  viewDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/school/detail/detail?id=${id}`
    })
  },

  // 关注/取消关注
  toggleFollow(e) {
    const { id, followed } = e.currentTarget.dataset
    const { schoolList } = this.data

    const index = schoolList.findIndex(item => item.id === id)
    if (index !== -1) {
      schoolList[index].isFollowed = !followed
      this.setData({ schoolList })

      wx.showToast({
        title: followed ? '已取消关注' : '关注成功',
        icon: 'success'
      })
    }
  },

  // 跳转到我的关注
  goToMyFollow() {
    wx.navigateTo({
      url: '/pages/my-follow/my-follow?type=school'
    })
  }
})
