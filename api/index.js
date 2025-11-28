// API接口统一管理
const { get, post, put, del } = require('../utils/request.js')

// ==================== 母校相关接口 ====================
const schoolApi = {
  // 获取母校列表
  getSchoolList: (params) => get('/schools', params),

  // 获取母校详情
  getSchoolDetail: (id) => get(`/schools/${id}`),

  // 关注母校
  followSchool: (id) => post(`/schools/${id}/follow`),

  // 取消关注母校
  unfollowSchool: (id) => del(`/schools/${id}/follow`),

  // 获取我的关注列表
  getMyFollowSchools: (params) => get('/schools/my-follow', params),

  // 获取母校的校友会列表
  getSchoolAssociations: (id, params) => get(`/schools/${id}/associations`, params),
}

// ==================== 校友会相关接口 ====================
const associationApi = {
  // 获取校友会列表
  getAssociationList: (params) => get('/associations', params),

  // 获取校友会详情
  getAssociationDetail: (id) => get(`/associations/${id}`),

  // 加入校友会
  joinAssociation: (id) => post(`/associations/${id}/join`),

  // 退出校友会
  leaveAssociation: (id) => post(`/associations/${id}/leave`),

  // 获取我加入的校友会列表
  getMyAssociations: (params) => get('/associations/my-joined', params),

  // 获取校友会成员列表
  getAssociationMembers: (id, params) => get(`/associations/${id}/members`, params),

  // 获取校友会活动列表
  getAssociationActivities: (id, params) => get(`/associations/${id}/activities`, params),

  // 关注校友会
  followAssociation: (id) => post(`/associations/${id}/follow`),

  // 取消关注校友会
  unfollowAssociation: (id) => del(`/associations/${id}/follow`),
}

// ==================== 校友相关接口 ====================
const alumniApi = {
  // 获取校友列表
  getAlumniList: (params) => get('/alumni', params),

  // 获取校友详情
  getAlumniDetail: (id) => get(`/alumni/${id}`),

  // 关注校友
  followAlumni: (id) => post(`/alumni/${id}/follow`),

  // 取消关注校友
  unfollowAlumni: (id) => del(`/alumni/${id}/follow`),

  // 获取推荐校友
  getRecommendAlumni: (params) => get('/alumni/recommend', params),
}

// ==================== 优惠券相关接口 ====================
const couponApi = {
  // 获取优惠券列表
  getCouponList: (params) => get('/coupons', params),

  // 获取优惠券详情
  getCouponDetail: (id) => get(`/coupons/${id}`),

  // 领取优惠券
  receiveCoupon: (id) => post(`/coupons/${id}/receive`),

  // 抢购优惠券
  rushCoupon: (id) => post(`/coupons/${id}/rush`),

  // 获取我的优惠券
  getMyCoupons: (params) => get('/coupons/my-coupons', params),

  // 获取抢购列表
  getRushList: (params) => get('/coupons/rush-list', params),
}

// ==================== 圈子相关接口 ====================
const circleApi = {
  // 获取圈子列表
  getCircleList: (params) => get('/circles', params),

  // 获取圈子详情
  getCircleDetail: (id) => get(`/circles/${id}`),

  // 加入圈子
  joinCircle: (id) => post(`/circles/${id}/join`),

  // 退出圈子
  leaveCircle: (id) => post(`/circles/${id}/leave`),

  // 获取圈子动态
  getCirclePosts: (id, params) => get(`/circles/${id}/posts`, params),

  // 发布动态
  publishPost: (data) => post('/circles/posts', data),
}

// ==================== 商家相关接口 ====================
const merchantApi = {
  // 获取商家列表
  getMerchantList: (params) => get('/merchants', params),

  // 获取商家详情
  getMerchantDetail: (id) => get(`/merchants/${id}`),

  // 关注商家
  followMerchant: (id) => post(`/merchants/${id}/follow`),

  // 取消关注商家
  unfollowMerchant: (id) => del(`/merchants/${id}/follow`),
}

// ==================== 商铺相关接口 ====================
const shopApi = {
  // 获取商铺列表
  getShopList: (params) => get('/shops', params),

  // 获取商铺详情
  getShopDetail: (id) => get(`/shops/${id}`),

  // 关注商铺
  followShop: (id) => post(`/shops/${id}/follow`),

  // 取消关注商铺
  unfollowShop: (id) => del(`/shops/${id}/follow`),
}

// ==================== 活动相关接口 ====================
const activityApi = {
  // 获取活动列表
  getActivityList: (params) => get('/activities', params),

  // 获取活动详情
  getActivityDetail: (id) => get(`/activities/${id}`),

  // 报名活动
  joinActivity: (id, data) => post(`/activities/${id}/join`, data),

  // 取消报名
  cancelActivity: (id) => post(`/activities/${id}/cancel`),
}

// ==================== 用户相关接口 ====================
const userApi = {
  // 登录
  login: (data) => post('/user/login', data),

  // 获取用户信息
  getUserInfo: () => get('/user/info'),

  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data),

  // 获取我的关注
  getMyFollows: (params) => get('/user/follows', params),

  // 获取我的粉丝
  getMyFans: (params) => get('/user/fans', params),
}

// ==================== 搜索相关接口 ====================
const searchApi = {
  // 综合搜索
  search: (params) => get('/search', params),

  // 搜索母校
  searchSchools: (params) => get('/search/schools', params),

  // 搜索校友会
  searchAssociations: (params) => get('/search/associations', params),

  // 搜索校友
  searchAlumni: (params) => get('/search/alumni', params),

  // 获取热门搜索
  getHotSearch: () => get('/search/hot'),

  // 获取搜索历史
  getSearchHistory: () => get('/search/history'),

  // 清空搜索历史
  clearSearchHistory: () => del('/search/history'),
}

module.exports = {
  schoolApi,
  associationApi,
  alumniApi,
  couponApi,
  circleApi,
  merchantApi,
  shopApi,
  activityApi,
  userApi,
  searchApi,
}
