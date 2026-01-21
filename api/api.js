// API接口统一管理
const { get, post, put, del, request } = require('../utils/request.js')



// ==================== 母校相关接口 ====================
const schoolApi = {
  // 分页查询母校列表（使用 POST 请求，参数在 body 中）列表和搜索都是用这个接口，注意分页参数
  getSchoolPage: (params) => post('/school/page', params),
  // 根据id查询母校信息
  getSchoolDetail: (id) => get(`/school/${id}`),
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
  // 分页查询校友会列表（使用 POST 请求，参数在 body 中）列表和搜索都是用这个接口，注意分页参数
  getAssociationList: (params) => post('/AlumniAssociation/page', params),
  // 根据id查询校友会详情
  getAssociationDetail: (id) => get(`/AlumniAssociation/${id}`),
  // 加入校友会
  joinAssociation: (id) => post(`/associations/${id}/join`),
  // 退出校友会
  leaveAssociation: (id) => post(`/associations/${id}/leave`),
  // 获取我加入的校友会列表
  getMyAssociations: (params) => get('/associations/my-joined', params),
  // 获取校友会成员列表
  getAssociationMembers: (id, params) => get(`/associations/${id}/members`, params),
  // 分页查询校友会成员列表
  getMemberPage: (params) => post('/AlumniAssociation/member/page', params),
  // 获取校友会活动列表
  getAssociationActivities: (id, params) => get(`/associations/${id}/activities`, params),
  // 关注校友会
  followAssociation: (id) => post(`/associations/${id}/follow`),
  // 取消关注校友会
  unfollowAssociation: (id) => del(`/associations/${id}/follow`),
  // 分页查询本人是会长的校友会列表（超级管理员可查看所有）
  getMyPresidentAssociations: (params) => post('/AlumniAssociation/my-president/page', params),
  // 申请加入校友会（普通用户）
  applyToJoinAssociation: (data) => post('/AlumniAssociationApplication/apply', data),
  // 申请创建校友会
  applyCreateAssociation: (data) => post('/AlumniAssociationApplication/apply', data),
  // 查看用户自己的校友会申请详情
  getApplicationDetail: (alumniAssociationId) => get(`/AlumniAssociationApplication/detail/${alumniAssociationId}`),
  // 撤销校友会申请
  cancelApplication: (applicationId) => put(`/AlumniAssociationApplication/cancel/${applicationId}`),
  // 编辑并重新提交待审核的校友会申请（普通用户）
  updateApplication: (data) => put('/AlumniAssociationApplication/update', data),
  // 获取组织结构列表
  getOrganizationRoles: (params) => post('/alumniAssociationManagement/role/list', params),
}

// ==================== 校友总会相关接口 ====================
const unionApi = {
  // 根据 id 查询校友总会详情
  getUnionDetail: (id) => get(`/AlumniHeadquarters/${id}`),
}

// ==================== 校处会相关接口 ====================
const localPlatformApi = {
  // 分页查询校处会列表（使用 POST 请求，参数在 body 中）
  getLocalPlatformPage: (params) => post('/localPlatform/page', params),
  // 根据id查询校处会详情
  getLocalPlatformDetail: (id) => get(`/localPlatform/${id}`),
  // 分页查询校处会审核列表
  queryAssociationApplicationPage: (params) => post('/localPlatformManagement/queryAssociationApplicationPage', params),
  // 获取校处会审核详情
  getAssociationApplicationDetail: (id) => get(`/localPlatformManagement/getAssociationApplication/${id}`),
  // 批准校处会审核
  approveAssociationApplication: (id) => post(`/localPlatformManagement/approveAssociationApplication/${id}`),
  // 拒绝校处会审核
  rejectAssociationApplication: (id) => post(`/localPlatformManagement/rejectAssociationApplication/${id}`),
  // 审核校处会申请（新接口）
  reviewAssociationApplication: (data) => post('/localPlatformManagement/reviewAssociationApplication', data),
  // 获取校处会下的校友会列表
  getPlatformAssociations: (params) => post('/localPlatform/alumniAssociations/page', params),
}



// ==================== 校友相关接口 ====================
const alumniApi = {
  // 查询校友列表（新接口）
  queryAlumniList: (params) => post('/users/query/alumni', params),
  // 获取校友信息（根据隐私设置）
  getAlumniInfo: (id) => get(`/users/getAlumniInfo/${id}`),
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
  // 分页查询商铺列表（使用 POST 请求，参数在 body 中）
  getMerchantPage: (params) => post('/merchant/page', params),
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
  // 关注商铺
  followShop: (id) => post(`/shops/${id}/follow`),
  // 取消关注商铺
  unfollowShop: (id) => del(`/shops/${id}/follow`),
}

// ==================== 附近权益相关接口 ====================
const nearbyApi = {
  // 统一附近查询（附近优惠，附近场所，附近校友）
  // queryType: 1-商铺, 2-企业/场所, 3-校友
  getNearby: (data) => post('/NearbyBenefits/nearby', data),
  // 获取商铺详情（POST请求，使用request body）
  getShopDetail: (data) => post('/NearbyBenefits/shops/detail', data),
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
  // 获取用户信息
  getUserInfo: () => get('/users/getInfo'),
  // 更新用户信息
  updateUserInfo: (data) => put('/users/update', data),
  // 获取我的关注
  getMyFollows: (params) => get('/user/follows', params),
  // 获取我的粉丝
  getMyFans: (params) => get('/user/fans', params),
  // 获取个人隐私设置
  getPrivacy: () => get('/users/getPrivacy'),
  // 更新个人隐私设置
  updatePrivacy: (data) => put('/users/update/privacy', data),
}



// ==================== 搜索相关接口 ====================
const searchApi = {
  // 统一搜索接口
  unifiedSearch: (params) => post('/search/unified', params),
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



// ==================== 文件上传相关接口 ====================
// 使用独立的文件上传工具
const fileUploadUtil = require('../utils/fileUpload.js')

// 文件上传/下载接口路径配置
const FILE_API_PATHS = {
  // 上传接口
  UPLOAD_IMAGE: '/file/upload/images',      // 上传图片
  UPLOAD_AUDIO: '/file/upload/audio',      // 上传音频
  UPLOAD_VIDEO: '/file/upload/video',      // 上传视频（待后端提供接口）
  UPLOAD_OTHER: '/file/upload/other',      // 上传其他格式文件（待后端提供接口）

  // 下载接口
  DOWNLOAD_FILE: '/file/download/{fileId}' // 下载文件（{fileId} 会被替换为实际文件ID）
}

const fileApi = {
  // 上传图片
  uploadImage: (filePath, originalName) => {
    return fileUploadUtil.uploadImage(filePath, FILE_API_PATHS.UPLOAD_IMAGE, originalName)
  },

  // 上传音频
  uploadAudio: (filePath, originalName) => {
    return fileUploadUtil.uploadAudio(filePath, FILE_API_PATHS.UPLOAD_AUDIO, originalName)
  },

  // 上传视频（接口路径在 FILE_API_PATHS.UPLOAD_VIDEO 中配置）
  uploadVideo: (filePath, originalName) => {
    return fileUploadUtil.uploadVideo(filePath, FILE_API_PATHS.UPLOAD_VIDEO, originalName)
  },

  // 上传其他格式文件（接口路径在 FILE_API_PATHS.UPLOAD_OTHER 中配置）
  uploadOtherFile: (filePath, originalName) => {
    return fileUploadUtil.uploadOtherFile(filePath, FILE_API_PATHS.UPLOAD_OTHER, originalName)
  },

  // 下载文件
  downloadFile: (fileId, savePath) => {
    return fileUploadUtil.downloadFile(fileId, FILE_API_PATHS.DOWNLOAD_FILE, savePath)
  },

  // 保存文件到本地
  saveFileToLocal: (tempFilePath) => {
    return fileUploadUtil.saveFileToLocal(tempFilePath)
  }
}




// ==================== 关注相关接口 ====================
const followApi = {
  // 添加关注
  // targetType: 1-用户, 2-校友会, 3-母校, 4-商户
  // followStatus: 1-正常关注, 2-特别关注, 3-免打扰, 4-已取消
  addFollow: (params) => post('/follow/add', params),
  // 取消关注
  removeFollow: (params) => del('/follow/remove', params),
  // 更新关注状态
  updateFollowStatus: (params) => put('/follow/updateStatus', params),
  // 分页查询我关注的列表
  getMyFollowingList: (params) => post('/follow/following/page', params),
  // 分页查询我的粉丝列表
  getMyFollowerList: (params) => post('/follow/follower/page', params),
  // 分页查询好友列表（互相关注）
  getMyFriendList: (params) => post('/follow/friend/page', params),
  // 获取关注和粉丝统计（旧接口，保留兼容）
  getFollowStats: () => get('/follow/stats'),
  // 获取当前用户的关注统计
  getCurrentUserStats: () => get('/follow/statistics/current'),
}

// ==================== 认证相关接口 ====================
const authApi = {
  // 认证登录（静默登录）
  // 用 wx.login 的 code 换取 token 和用户信息

  auth: (data) => post('/auth/login', data),
  // auth: (data) => {
  //   // 将参数拼接到 URL 上（查询参数）
  //   let url = '/auth/login'
  //   console.log('=== auth 接口参数处理 ===')
  //   console.log('接收到的参数:', data)

  //   if (data && Object.keys(data).length > 0) {
  //     const queryString = Object.keys(data)
  //       .filter(key => data[key] !== undefined && data[key] !== null && data[key] !== '')
  //       .map(key => {
  //         const value = data[key]
  //         console.log(`参数 ${key}:`, value, '类型:', typeof value)
  //         return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
  //       })
  //       .join('&')
  //     console.log('拼接的查询字符串:', queryString)
  //     if (queryString) {
  //       url += (url.includes('?') ? '&' : '?') + queryString
  //     }
  //   }

  //   console.log('最终请求 URL:', url)

  //   // POST 请求，但参数在 URL 上，body 为空
  //   return request({
  //     url,
  //     method: 'POST',
  //     data: {}
  //   })
  // },
}

// ==================== 聊天相关接口 ====================
const chatApi = {
  // 获取会话列表
  getConversations: (params) => get('/chat/conversations', params),

  // 获取聊天历史记录
  getChatHistory: (params) => post('/chat/history', params),

  // 发送消息
  sendMessage: (data) => post('/chat/send', data),

  // 获取与某人的聊天记录
  getChatMessages: (userId, params) => get(`/chat/messages/${userId}`, params),


  // 标记会话已读（传对方的 wxId/userId）
  markConversationRead: (otherWxId) => put(`/chat/read/${otherWxId}`),

  // 删除会话（传会话ID）
  deleteConversation: (conversationId) => del(`/chat/conversation/${conversationId}`),

  // 删除聊天记录（旧接口，保留兼容）
  deleteChat: (userId) => del(`/chat/${userId}`),

  // 清空聊天记录
  clearChatHistory: (userId) => post(`/chat/${userId}/clear`),

  // 获取未读消息数量
  getUnreadCount: () => get('/chat/unread/count'),

  // 获取所有未读消息总数（包括聊天消息和通知消息）
  getUnreadTotal: () => get('/chat/unread/total'),

  // 获取在线用户列表
  getOnlineUsers: (params) => get('/chat/online/users', params),

  // 检查用户是否在线
  checkUserOnline: (userId) => get(`/chat/user/${userId}/online`),

  // 保存草稿
  saveDraft: (conversationId, draftContent) => put(`/chat/conversation/${conversationId}/draft?draftContent=${encodeURIComponent(draftContent)}`, { conversationId }),

  // 撤回消息
  recallMessage: (messageId) => del(`/chat/recall/${messageId}`),

  // 置顶/取消置顶会话（conversationId 作为路径参数，isPinned 作为查询参数）
  pinConversation: (conversationId, isPinned) => put(`/chat/conversation/${conversationId}/pin?isPinned=${isPinned}`, {}),

  // 上传聊天图片
  uploadChatImage: (filePath) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const app = getApp()
      const baseUrl = app.globalData.baseUrl

      wx.uploadFile({
        url: `${baseUrl}/chat/upload/image`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': `Bearer ${token}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            resolve({ data })
          } catch (error) {
            reject(error)
          }
        },
        fail: reject
      })
    })
  },

  // 上传聊天语音
  uploadChatVoice: (filePath) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const app = getApp()
      const baseUrl = app.globalData.baseUrl

      wx.uploadFile({
        url: `${baseUrl}/chat/upload/voice`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': `Bearer ${token}`
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            resolve({ data })
          } catch (error) {
            reject(error)
          }
        },
        fail: reject
      })
    })
  },

  // 获取用户通知列表
  getNotificationList: (params) => post('/chat/notifications', params),

  // 标记通知为已读（单条或全部）
  // notificationId 为空时标记全部已读，有值时标记单条已读
  markNotificationRead: (notificationId) => {
    const url = notificationId
      ? `/chat/notifications/read?notificationId=${notificationId}`
      : '/chat/notifications/read'
    return put(url)
  }
}

// ==================== 首页文章相关接口 ====================
const homeArticleApi = {
  // 分页查询首页文章列表
  getPage: (params) => post('/home/articles', params),
  // 分页查询本人创建的文章列表
  getMyArticlePage: (params) => post('/home-page-article/my-page', params),
  // 新增首页文章
  createArticle: (data) => post('/home-page-article/create', data),
  // 根据ID查询文章详情
  getHomeArticleDetail: (id) => get(`/home-page-article/${id}`),
  // 更新首页文章
  updateArticle: (data) => put('/home-page-article/update', data),
  // 删除文章
  deleteArticle: (id) => del(`/home-page-article/${id}`),
}

// ==================== 文章审核相关接口 ====================
const articleApplyApi = {
  // 获取待审核记录列表（分页）
  getPendingList: (params) => post('/home-page-article-apply/pending/page', params),
  // 获取已审核记录列表（分页）
  getApprovedList: (params) => post('/home-page-article-apply/approved/page', params),
  // 审核文章
  approveArticle: (data) => post('/home-page-article-apply/approve', data),
}


module.exports = {
  schoolApi,
  associationApi,
  unionApi,
  localPlatformApi,
  alumniApi,
  couponApi,
  circleApi,
  merchantApi,
  shopApi,
  nearbyApi,
  activityApi,
  userApi,
  searchApi,
  followApi,
  authApi,
  fileApi,
  chatApi,
  homeArticleApi,
  articleApplyApi,
}
