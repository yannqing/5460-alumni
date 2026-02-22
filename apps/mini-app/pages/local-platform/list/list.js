// pages/local-platform/list/list.js
const { localPlatformApi } = require('../../../api/api.js')
const config = require('../../../utils/config.js')

Page({
    data: {
        // 图标路径
        iconSearch: '../../../assets/icons/magnifying glass.png',
        iconLocation: config.getIconUrl('position.png'),
        iconScope: config.getIconUrl('xx.png'),
        iconContact: config.getIconUrl('phone.png'),
        keyword: '',
        filters: [
            { label: '城市', options: ['全部城市', '北京', '上海', '广州', '深圳', '杭州', '南京'], selected: 0 },
            { label: '状态', options: ['全部状态', '活跃', '未激活'], selected: 0 },
            { label: '排序', options: ['默认排序', '最新创建'], selected: 0 }
        ],
        platformList: [],
        current: 1,
        pageSize: 10,
        hasMore: true,
        loading: false
    },

    onLoad() {
        this.loadPlatformList(true)
    },

    

    async loadPlatformList(reset = false) {
        if (this.data.loading) {return}
        if (!reset && !this.data.hasMore) {return}

        this.setData({ loading: true })

        const { keyword, filters, current, pageSize } = this.data
        const [cityFilter, statusFilter, sortFilter] = filters

        // 构建请求参数
        const params = {
            current: reset ? 1 : current,
            pageSize: pageSize,
            sortField: 'createTime',
            sortOrder: 'descend'
        }

        // 平台名称搜索
        if (keyword && keyword.trim()) {
            params.platformName = keyword.trim()
        }

        // 城市筛选
        if (cityFilter.selected > 0) {
            params.city = cityFilter.options[cityFilter.selected]
        }

        // 状态筛选
        if (statusFilter.selected === 1) {
            params.status = 1 // 活跃
        } else if (statusFilter.selected === 2) {
            params.status = 0 // 未激活
        }

        // 排序方式
        if (sortFilter.selected === 1) {
            params.sortField = 'createTime'
            params.sortOrder = 'descend'
        }

        try {
            const res = await localPlatformApi.getLocalPlatformPage(params)
            console.log('校促会列表接口返回:', res)

            if (res.data && res.data.code === 200) {
                const data = res.data.data || {}
                const records = data.records || []

                // 数据映射
                const mappedList = records.map(item => this.mapPlatformItem(item))

                // 更新列表数据
                const finalList = reset ? mappedList : [...this.data.platformList, ...mappedList]

                this.setData({
                    platformList: finalList,
                    current: reset ? 2 : current + 1,
                    hasMore: false,
                    loading: false
                })
            } else {
                this.setData({ loading: false })
                wx.showToast({
                    title: res.data?.msg || '加载失败',
                    icon: 'none'
                })
            }
        } catch (error) {
            console.error('加载校促会列表失败:', error)
            this.setData({ loading: false })
            wx.showToast({
                title: '加载失败，请重试',
                icon: 'none'
            })
        }
    },

    // 数据映射：将后端数据映射为前端所需格式
    mapPlatformItem(item) {
        // 处理头像
        let avatar = item.avatar || ''
        if (avatar) {
            // 如果头像已经是完整的 URL（以 http 或 https 开头），直接使用
            if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
                avatar = item.avatar
            } else {
                avatar = config.getImageUrl(avatar)
            }
        } else {
            // 使用默认头像
            avatar = config.defaultAvatar
        }
        
        // 处理背景图片
        let bgImg = item.bgImg || ''
        if (bgImg) {
            // 如果背景图片已经是完整的 URL（以 http 或 https 开头），直接使用
            if (bgImg.startsWith('http://') || bgImg.startsWith('https://')) {
                bgImg = item.bgImg
            } else {
                bgImg = config.getImageUrl(bgImg)
            }
        }

        return {
            platformId: item.platformId,
            platformName: item.platformName || '未命名校促会',
            avatar: avatar,
            bgImg: bgImg,
            city: item.city || '未知城市',
            scope: item.scope || '',
            contactInfo: item.contactInfo || '',
            status: item.status || 0,
            createTime: item.createTime || '',
            updateTime: item.updateTime || ''
        }
    },

    // 搜索
    onSearchInput(e) {
        this.setData({ keyword: e.detail.value })
    },

    onSearch() {
        console.log('搜索:', this.data.keyword)
        this.loadPlatformList(true)
    },

    // 城市筛选
    onCityChange(e) {
        const filters = this.data.filters
        filters[0].selected = e.detail.value
        this.setData({ filters })
        this.loadPlatformList(true)
    },

    // 状态筛选
    onStatusChange(e) {
        const filters = this.data.filters
        filters[1].selected = e.detail.value
        this.setData({ filters })
        this.loadPlatformList(true)
    },

    // 排序筛选
    onSortChange(e) {
        const filters = this.data.filters
        filters[2].selected = e.detail.value
        this.setData({ filters })
        this.loadPlatformList(true)
    },

    viewDetail(e) {
        wx.navigateTo({
            url: `/pages/local-platform/detail/detail?id=${e.currentTarget.dataset.id}`
        })
    }
})
