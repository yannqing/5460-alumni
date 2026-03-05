// pages/alumni-activity/list/list.js
const { activityApi } = require('../../../api/api');
const config = require('../../../utils/config.js');

Page({
  data: {
    activityList: [],
    loading: false,
    refreshing: false,
    hasMore: true,
    current: 1,
    pageSize: 10,
    total: 0,
    // 活动状态主题配置
    activityThemes: {
      1: { name: '报名中', theme: 'upcoming' },
      2: { name: '进行中', theme: 'ongoing' },
      3: { name: '已结束', theme: 'ended' },
      4: { name: '已取消', theme: 'ended' }
    }
  },

  onLoad(options) {
    this.getActivityList();
  },

  onShow() {
    // 页面显示时可刷新数据
  },

  /**
   * 获取活动列表
   */
  async getActivityList(isLoadMore = false) {
    if (this.data.loading) return;
    if (isLoadMore && !this.data.hasMore) return;

    this.setData({ loading: true });

    try {
      const res = await activityApi.getHomepageActivityList({
        current: isLoadMore ? this.data.current : 1,
        pageSize: this.data.pageSize
      });

      const result = res.data || res;

      if (result.code === 200) {
        const records = result.data?.records || result.data?.list || [];
        const total = result.data?.total || 0;

        // 映射接口数据到组件使用的格式
        const newList = records.map(item => this.formatActivityItem(item));

        const activityList = isLoadMore
          ? [...this.data.activityList, ...newList]
          : newList;

        const hasMore = activityList.length < total;

        this.setData({
          activityList,
          total,
          hasMore,
          current: isLoadMore ? this.data.current + 1 : 2,
          loading: false,
          refreshing: false
        });

        console.log('[AlumniActivity] 获取活动列表成功:', activityList.length, '/', total);
      } else {
        console.warn('[AlumniActivity] 获取活动列表非200:', result);
        this.setData({
          loading: false,
          refreshing: false
        });
      }
    } catch (err) {
      console.error('[AlumniActivity] 获取活动列表失败:', err);
      this.setData({
        loading: false,
        refreshing: false
      });
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  /**
   * 格式化活动数据
   */
  formatActivityItem(item) {
    // 处理封面图
    let posterUrl = '';
    if (item.coverImage) {
      if (typeof item.coverImage === 'object') {
        posterUrl = item.coverImage.fileUrl || item.coverImage.filePath || '';
      } else {
        posterUrl = item.coverImage;
      }
      if (posterUrl) {
        posterUrl = config.getImageUrl(posterUrl);
      }
    }

    // 处理活动时间格式
    let startTime = item.startTime || '';
    if (startTime && startTime.includes('T')) {
      startTime = startTime.replace('T', ' ').substring(0, 16);
    }

    // 处理主办方头像
    let organizerAvatar = '';
    if (item.organizerAvatar) {
      if (typeof item.organizerAvatar === 'object') {
        organizerAvatar = item.organizerAvatar.fileUrl || item.organizerAvatar.filePath || '';
      } else {
        organizerAvatar = item.organizerAvatar;
      }
      if (organizerAvatar) {
        organizerAvatar = config.getImageUrl(organizerAvatar);
      }
    }

    return {
      activity_uuid: item.activityId || item.id || '',
      activity_theme: item.activityTitle || item.title || '',
      activity_poster: {
        preview_url: posterUrl
      },
      activity_status: item.status || 1,
      activity_starttime: startTime,
      activity_address: item.address || item.activityAddress || '',
      activity_fees: item.fees || item.activityFees || '0.00',
      type: {
        activity_type_name: item.activityCategory || item.categoryName || ''
      },
      // 主办方信息
      organizerType: item.organizerType,
      organizerId: item.organizerId || '',
      organizerName: item.organizerName || '',
      organizerAvatar: organizerAvatar,
      // 新接口字段（兼容）
      activityTitle: item.activityTitle || item.title || '',
      startTime: startTime,
      province: item.province || '',
      city: item.city || '',
      district: item.district || '',
      address: item.address || ''
    };
  },

  /**
   * 下拉刷新
   */
  onRefresh() {
    this.setData({ refreshing: true });
    this.getActivityList(false);
  },

  /**
   * 加载更多
   */
  loadMore() {
    if (!this.data.loading && this.data.hasMore) {
      this.getActivityList(true);
    }
  },

  /**
   * 跳转到活动详情
   */
  gotoActivityDetail(e) {
    const item = e.currentTarget.dataset.item;
    if (item && item.activity_uuid) {
      wx.navigateTo({
        url: `/pages/activity/detail/detail?id=${item.activity_uuid}`
      });
    }
  }
});
