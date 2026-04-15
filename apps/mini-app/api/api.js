// API接口统一管理
const { get, post, put, del, request } = require('../utils/request.js')

// ==================== 母校相关接口 ====================
const schoolApi = {
  // 分页查询母校列表（使用 POST 请求，参数在 body 中）列表和搜索都是用这个接口，注意分页参数
  getSchoolPage: params => post('/school/page', params),
  // 根据id查询母校信息
  getSchoolDetail: id => get(`/school/${id}`),
  // 关注母校
  followSchool: id => post(`/schools/${id}/follow`),
  // 取消关注母校
  unfollowSchool: id => del(`/schools/${id}/follow`),
  // 获取我的关注列表
  getMyFollowSchools: params => get('/schools/my-follow', params),
  // 获取母校的校友会列表
  getSchoolAssociations: (id, params) => get(`/schools/${id}/associations`, params),
}

// ==================== 校友会相关接口 ====================
const associationApi = {
  // 分页查询校友会列表（使用 POST 请求，参数在 body 中）列表和搜索都是用这个接口，注意分页参数
  getAssociationList: params => post('/AlumniAssociation/page', params),
  // 根据id查询校友会详情
  getAssociationDetail: id => get(`/AlumniAssociation/${id}`),
  // 加入校友会
  joinAssociation: id => post(`/associations/${id}/join`),
  // 退出校友会
  leaveAssociation: id => post(`/associations/${id}/leave`),
  // 获取我加入的校友会列表
  getMyAssociations: params => get('/users/my-associations', params),
  // 获取校友会成员列表
  getAssociationMembers: (id, params) => get(`/associations/${id}/members`, params),
  // 分页查询校友会成员列表
  getMemberPage: params => post('/AlumniAssociation/member/page', params),
  // 获取校友会活动列表
  getAssociationActivities: (id, params) => get(`/associations/${id}/activities`, params),
  // 关注校友会
  followAssociation: id => post(`/associations/${id}/follow`),
  // 取消关注校友会
  unfollowAssociation: id => del(`/associations/${id}/follow`),
  // 分页查询本人是会长的校友会列表（超级管理员可查看所有）
  getMyPresidentAssociations: params => post('/AlumniAssociation/my-president/page', params),
  // 申请加入校友会（普通用户）
  applyToJoinAssociation: data => post('/AlumniAssociationJoinApplication/apply', data),
  // 申请创建校友会
  applyCreateAssociation: data => post('/AlumniAssociationApplication/apply', data),
  // 查看用户自己的校友会申请详情
  getApplicationDetail: alumniAssociationId =>
    get(`/AlumniAssociationApplication/detail/${alumniAssociationId}`),
  // 根据申请ID查看加入校友会申请详情（用于我的申请编辑）
  getJoinApplicationDetailById: applicationId =>
    get(`/AlumniAssociationJoinApplication/detailById/${applicationId}`),
  // 撤销校友会申请
  cancelApplication: applicationId => put(`/AlumniAssociationApplication/cancel/${applicationId}`),
  // 编辑并重新提交待审核的校友会申请（普通用户）
  updateApplication: data => put('/AlumniAssociationApplication/update', data),
  // 退出校友会
  quitAssociation: data => post('/AlumniAssociationJoinApplication/quit', data),
  // 获取组织结构列表
  getOrganizationRoles: params => post('/alumniAssociationManagement/role/list', params),
  // 申请加入校促会
  applyJoinPlatform: data => post('/AlumniAssociation/applyJoinPlatform', data),
  // 查询加入申请列表
  queryJoinApplyPage: params => post('/AlumniAssociation/queryJoinApplyPage', params),
  // 根据ID查询加入校促会申请详情（含创建校友会申请附件）
  getJoinApplyDetailWithAttachment: id =>
    get(`/AlumniAssociation/joinApplyDetailWithAttachment/${id}`),
  // 审核加入校促会申请
  reviewJoinPlatform: data => post('/AlumniAssociation/reviewJoinPlatform', data),
  // 系统管理员分页查询所有校友会创建申请列表
  querySystemAdminApplicationPage: params =>
    post('/AlumniAssociationApplication/querySystemAdminApplicationPage', params),
  // 系统管理员审核校友会创建申请
  reviewApplication: data => post('/AlumniAssociationApplication/reviewApplication', data),
  // 获取组织架构模板列表
  getOrganizeTemplateList: params => get('/organizeArchiTemplate/list', params),
  // 生成小程序码
  generateMiniProgramQrcode: data => post('/AlumniAssociation/qrcode/generate', data),
}

// ==================== 校友总会相关接口 ====================
const unionApi = {
  // 根据 id 查询校友总会详情
  getUnionDetail: id => get(`/AlumniHeadquarters/${id}`),
  // 分页查询校友总会列表
  getUnionPage: params => post('/AlumniHeadquarters/page', params),
  // 申请激活校友总会
  applyActivate: data => post('/AlumniHeadquarters/applyActivate', data),
  // 获取未激活的校友总会列表
  getInactiveUnionPage: params => post('/AlumniHeadquarters/inactive/page', params),
  // 获取待审核的校友总会列表
  getPendingUnionPage: params => post('/AlumniHeadquarters/pending/page', params),
  // 审核校友总会
  auditUnion: data => post('/AlumniHeadquarters/audit', data),
  // 验证创建码/邀请码
  verifyCreateCode: data =>
    request({ url: '/AlumniHeadquarters/verifyCode', method: 'POST', data: data, silent: true }),
  // 查看校友总会申请详情
  getApplyDetail: headquartersId =>
    get(`/AlumniHeadquarters/admin/applyDetail`, { headquartersId }),
}

// ==================== 校促会相关接口 ====================
const localPlatformApi = {
  // 分页查询校促会列表（使用 POST 请求，参数在 body 中）
  getLocalPlatformPage: params => post('/localPlatform/page', params),
  // 根据id查询校促会详情
  getLocalPlatformDetail: id => get(`/localPlatform/${id}`),
  // 获取校促会管理端详情
  getLocalPlatformManagementDetail: platformId =>
    get(`/localPlatformManagement/detail/${platformId}`),
  // 分页查询校促会审核列表
  queryAssociationApplicationPage: params =>
    post('/localPlatformManagement/queryAssociationApplicationPage', params),
  // 获取校促会审核详情
  getAssociationApplicationDetail: id =>
    get(`/localPlatformManagement/getAssociationApplication/${id}`),
  // 批准校促会审核
  approveAssociationApplication: id =>
    post(`/localPlatformManagement/approveAssociationApplication/${id}`),
  // 拒绝校促会审核
  rejectAssociationApplication: id =>
    post(`/localPlatformManagement/rejectAssociationApplication/${id}`),
  // 审核校促会申请（新接口）
  reviewAssociationApplication: data =>
    post('/localPlatformManagement/reviewAssociationApplication', data),
  // 获取校促会下的校友会列表
  getPlatformAssociations: params => post('/localPlatform/alumniAssociations/page', params),
  // 更新校促会成员角色
  updateMemberRole: data => put('/localPlatformManagement/updateMemberRole/v2', data),
  // 更新校促会信息
  updateLocalPlatform: data => put('/localPlatformManagement/update', data),
  // 获取校促会隐私设置
  getLocalPlatformPrivacySetting: platformId =>
    get(`/localPlatformManagement/privacy/${platformId}`),
  // 修改校促会隐私设置
  updateLocalPlatformPrivacySetting: data => post('/localPlatformManagement/privacy/update', data),
}

// ==================== 校友相关接口 ====================
const alumniApi = {
  // 查询校友列表（ES版本，支持降级到MySQL）
  // params: { nickname, name, phone, gender, myFollow, pageNum, pageSize, ... }
  queryAlumniList: params => post('/users/query/alumni/es', params),
  // 查询校友列表（MySQL 版本）— 管理端选人等场景 wxId 常以字符串返回，避免大整数精度问题
  // params: { current, pageSize, name, nickname, phone, ... } 见 QueryAlumniListDto
  queryAlumniListMysql: params => post('/users/query/alumni', params),
  // 获取校友信息（根据隐私设置）
  getAlumniInfo: id => get(`/users/getAlumniInfo/${id}`),
  // 关注校友
  followAlumni: id => post(`/alumni/${id}/follow`),
  // 取消关注校友
  unfollowAlumni: id => del(`/alumni/${id}/follow`),
  // 获取推荐校友
  getRecommendAlumni: params => get('/alumni/recommend', params),
}

// ==================== 优惠券相关接口 ====================
const couponApi = {
  // 获取优惠券列表
  getCouponList: params => get('/coupons', params),
  // 获取优惠券详情
  getCouponDetail: id => get(`/coupons/${id}`),
  // 领取优惠券
  receiveCoupon: id => post(`/coupons/${id}/receive`),
  // 抢购优惠券
  rushCoupon: id => post(`/coupons/${id}/rush`),
  // 获取我的优惠券
  getMyCoupons: params => get('/coupon/my-coupons', params),
  // 获取抢购列表
  getRushList: params => get('/coupons/rush-list', params),
  // 新的领取优惠券接口
  claimCoupon: data => post('/coupon/claim', data),
  // 根据优惠券码查询优惠券信息（用于核销）
  getCouponByCode: code => get(`/coupon/verify/${code}`),
  // 核销优惠券
  verifyCoupon: data => post('/coupon/verify', data),
  // 获取用户券详情（含核销码生成）
  // 路径中的 userCouponId 必须用字符串拼接，避免大整数经 Number 后精度丢失
  getUserCouponDetail: userCouponId =>
    get(`/coupon/user-coupon/${encodeURIComponent(String(userCouponId))}`),
  refreshCouponCode: userCouponId =>
    post(`/coupon/user-coupon/refresh-code/${encodeURIComponent(String(userCouponId))}`),
  // 商户端：分页查询优惠券列表（管理）
  getManagementCouponList: data => post('/coupon/management/list', data),
  // 商户端：查询优惠券详情（管理）
  // couponId 为雪花 ID，必须按字符串透传，避免 Number 精度丢失
  getManagementCouponDetail: couponId =>
    get(`/coupon/management/${encodeURIComponent(String(couponId))}`),
  // 商户端：创建优惠券
  createCoupon: data => post('/coupon/create', data),
  // 商户端：更新优惠券
  updateManagementCoupon: data => post('/coupon/management/update', data),
  // 商户端：删除优惠券（管理）
  // couponId 为雪花 ID，必须按字符串透传，避免 Number 精度丢失
  deleteManagementCoupon: couponId =>
    del(`/coupon/management/${encodeURIComponent(String(couponId))}`),
}

// ==================== 圈子相关接口 ====================
const circleApi = {
  // 获取圈子列表
  getCircleList: params => get('/circles', params),
  // 获取圈子详情
  getCircleDetail: id => get(`/circles/${id}`),
  // 加入圈子
  joinCircle: id => post(`/circles/${id}/join`),
  // 退出圈子
  leaveCircle: id => post(`/circles/${id}/leave`),
  // 获取圈子动态
  getCirclePosts: (id, params) => get(`/circles/${id}/posts`, params),
  // 发布动态
  publishPost: data => post('/circles/posts', data),
}

// ==================== 商家相关接口 ====================
const merchantApi = {
  // 分页查询商铺列表（使用 POST 请求，参数在 body 中）
  getMerchantPage: params => post('/merchant/page', params),
  // 获取商家列表
  getMerchantList: params => get('/merchants', params),
  // 获取商家详情
  getMerchantDetail: id => get(`/merchants/${id}`),
  // 新商家详情接口
  getMerchantInfo: id => get(`/merchant/${id}`),
  // 获取待审核商家详情
  getPendingMerchantDetail: merchantId => get(`/merchant/pending/${merchantId}`),
  // 关注商家
  followMerchant: id => post(`/merchants/${id}/follow`),
  // 取消关注商家
  unfollowMerchant: id => del(`/merchants/${id}/follow`),
  // 获取店铺详情
  getShopDetail: shopId => get(`/merchant/shop/${shopId}`),
  // 获取我的商户列表
  getMyMerchants: params => get('/merchant-management/my-merchants', params),
}

// ==================== 商铺相关接口 ====================
const shopApi = {
  // 关注商铺
  followShop: id => post(`/shops/${id}/follow`),
  // 取消关注商铺
  unfollowShop: id => del(`/shops/${id}/follow`),
  // 获取本人可用的门店列表
  getAvailableShops: () => get('/shop/my/available'),
}

// ==================== 附近权益相关接口 ====================
const nearbyApi = {
  // 统一附近查询（附近优惠，附近场所，附近校友）
  // queryType: 1-商铺, 2-企业/场所, 3-校友
  getNearby: data => post('/NearbyBenefits/nearby', data),
  // 获取商铺详情（POST请求，使用request body）
  getShopDetail: data => post('/NearbyBenefits/shops/detail', data),
}

// ==================== 活动相关接口 ====================
const activityApi = {
  // 获取活动列表
  getActivityList: params => get('/activities', params),
  // 查询所有公开活动列表（分页）
  getPublicActivityList: params => post('/activity/public/list', params),
  // 查询首页展示的活动列表
  getHomepageActivityList: params => post('/activity/homepage/list', params),
  // 获取活动详情
  getActivityDetail: id => get(`/activity/${id}`),
  // 报名活动
  joinActivity: (id, data) => post(`/activities/${id}/join`, data),
  // 取消报名
  cancelActivity: id => post(`/activities/${id}/cancel`),
}

// ==================== 用户相关接口 ====================
const userApi = {
  // 获取用户信息
  getUserInfo: () => get('/users/getInfo'),
  // 更新用户信息
  updateUserInfo: data => put('/users/update', data),
  // 获取我的关注
  getMyFollows: params => get('/user/follows', params),
  // 获取我的粉丝
  getMyFans: params => get('/user/fans', params),
  // 获取个人隐私设置
  getPrivacy: () => get('/users/getPrivacy'),
  // 更新个人隐私设置
  updatePrivacy: data => put('/users/update/privacy', data),
  // 获取微信手机号（通过 button 组件回调中的 code）
  getPhoneNumber: data => post('/auth/getPhoneNumber', data),
  // 获取用户管理的组织列表
  // type: 0-校友会 1-校促会 2-商户 3-校友总会
  // roleScopedOnly: true 时仅返回 role_user 绑定的组织，系统超级管理员不展开全站（管理入口与待办范围一致）
  getManagedOrganizations: params => get('/users/managed-organizations', params),
}

// ==================== 搜索相关接口 ====================
const searchApi = {
  // 统一搜索接口
  unifiedSearch: params => post('/search/unified', params),
  // 综合搜索
  search: params => get('/search', params),
  // 搜索母校
  searchSchools: params => get('/search/schools', params),
  // 搜索校友会
  searchAssociations: params => get('/search/associations', params),
  // 搜索校友
  searchAlumni: params => get('/search/alumni', params),
  // 获取热门搜索
  getHotSearch: () => get('/search/hot'),
  // 获取搜索历史
  getSearchHistory: () => get('/search/history'),
  // 清空搜索历史
  clearSearchHistory: () => del('/search/history'),
}

// ==================== 文件上传相关接口 ====================
// 说明：图片/音频/文档在「云托管」与「传统 API（如连 localhost）」下走不同链路。
// - IS_CLOUD_HOST === true：走 cosUpload（先调后端 /file/cos/credential 拿临时密钥，再直传 COS），
//   用于绕开云托管网关对包体大小的限制；要求后端 file.storage-type=cos 且能签发凭证。
// - IS_CLOUD_HOST === false：走 fileUploadUtil（wx.uploadFile  multipart 到后端），
//   与本地开发 file.storage-type=local 一致，不会出现「未启用 COS 无法获取临时凭证」。
const fileUploadUtil = require('../utils/fileUpload.js')
const config = require('../utils/config.js')
const cosUploadUtil = require('../utils/cosUpload.js')

// 文件上传/下载接口路径配置
const FILE_API_PATHS = {
  // 上传接口
  UPLOAD_IMAGE: '/file/upload/images', // 上传图片
  UPLOAD_AUDIO: '/file/upload/audio', // 上传音频
  UPLOAD_VIDEO: '/file/upload/video', // 上传视频（待后端提供接口）
  UPLOAD_OTHER: '/file/upload/other', // 上传其他格式文件（待后端提供接口）
  UPLOAD_DOCUMENT: '/file/upload/document', // 上传文档（pdf, doc, docx, xls, xlsx, ppt, pptx, txt, md, csv, rtf, odt, ods, odp）

  // 下载接口
  DOWNLOAD_FILE: '/file/download/{fileId}', // 下载文件（{fileId} 会被替换为实际文件ID）
}

const fileApi = {
  /**
   * 上传图片（统一入口：各业务页如商户申请营业执照、资料编辑等调用此方法即可）
   * @param {string} filePath 本地临时文件路径
   * @param {string} [originalName] 原始文件名（可选，传统上传会带给后端）
   * @param {number} [fileSize] 字节大小（可选；仅 COS 直传链路会用到）
   * @returns {Promise<{code:number,data:FilesVo}>} 与历史约定一致：成功时 code=200，data 含 fileUrl 等
   */
  uploadImage: async (filePath, originalName = '', fileSize) => {
    if (config.IS_CLOUD_HOST) {
      // 云托管：浏览器/小程序不经过网关直传 COS，返回的 FilesVo 与 saveFileRecord 等后端逻辑对齐
      const filesVo = await cosUploadUtil.uploadImageToCos(filePath, originalName, fileSize)
      return { code: 200, data: filesVo }
    }
    // 传统模式：POST multipart 到 Java，后端 FileController /file/upload/images，支持 local 磁盘存储
    return fileUploadUtil.uploadImage(filePath, FILE_API_PATHS.UPLOAD_IMAGE, originalName || '')
  },

  /**
   * 上传音频（分支规则同 uploadImage）
   */
  uploadAudio: async (filePath, originalName = '', fileSize) => {
    if (config.IS_CLOUD_HOST) {
      const filesVo = await cosUploadUtil.uploadAudioToCos(filePath, originalName, fileSize)
      return { code: 200, data: filesVo }
    }
    return fileUploadUtil.uploadAudio(filePath, FILE_API_PATHS.UPLOAD_AUDIO, originalName || '')
  },

  /**
   * 上传文档（分支规则同 uploadImage）
   */
  uploadDocument: async (filePath, originalName = '', fileSize) => {
    if (config.IS_CLOUD_HOST) {
      const filesVo = await cosUploadUtil.uploadDocumentToCos(filePath, originalName, fileSize)
      return { code: 200, data: filesVo }
    }
    return fileUploadUtil.uploadDocument(filePath, FILE_API_PATHS.UPLOAD_DOCUMENT, originalName || '')
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
  saveFileToLocal: tempFilePath => {
    return fileUploadUtil.saveFileToLocal(tempFilePath)
  },

  // ===== 显式 COS 直传（始终走凭证接口；仅当确认后端已启用 COS 时使用，一般业务请用 uploadImage）=====
  cosUploadImage: (tempFilePath, originalName, fileSize) => {
    return cosUploadUtil.uploadImageToCos(tempFilePath, originalName, fileSize)
  },

  cosUploadAudio: (tempFilePath, originalName, fileSize) => {
    return cosUploadUtil.uploadAudioToCos(tempFilePath, originalName, fileSize)
  },

  cosUploadDocument: (tempFilePath, originalName, fileSize) => {
    return cosUploadUtil.uploadDocumentToCos(tempFilePath, originalName, fileSize)
  },
}

// ==================== 关注相关接口 ====================
const followApi = {
  // 添加关注
  // targetType: 1-用户, 2-校友会, 3-母校, 4-商户
  // followStatus: 1-正常关注, 2-特别关注, 3-免打扰, 4-已取消
  addFollow: params => post('/follow/add', params),
  // 取消关注
  removeFollow: params => del('/follow/remove', params),
  // 更新关注状态
  updateFollowStatus: params => put('/follow/updateStatus', params),
  // 分页查询我关注的列表
  getMyFollowingList: params => post('/follow/following/page', params),
  // 分页查询我的粉丝列表
  getMyFollowerList: params => post('/follow/follower/page', params),
  // 分页查询好友列表（互相关注）
  getMyFriendList: params => post('/follow/friend/page', params),
  // 获取关注和粉丝统计（旧接口，保留兼容）
  getFollowStats: () => get('/follow/stats'),
  // 获取当前用户的关注统计
  getCurrentUserStats: () => get('/follow/statistics/current'),
}

// ==================== 认证相关接口 ====================
const authApi = {
  // 认证登录（静默登录）
  // 用 wx.login 的 code 换取 token 和用户信息
  auth: data => post('/auth/login', data),

  // 用户注册（更新用户信息并添加教育经历）
  // data: { nickname, name, schoolId, gender, phone }
  register: data => post('/auth/register', data),
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
  getConversations: params => get('/chat/conversations', params),

  // 获取聊天历史记录
  getChatHistory: params => post('/chat/history', params),

  // 发送消息
  sendMessage: data => post('/chat/send', data),

  // 获取与某人的聊天记录
  getChatMessages: (userId, params) => get(`/chat/messages/${userId}`, params),

  // 标记会话已读（传对方的 wxId/userId）
  markConversationRead: otherWxId => put(`/chat/read/${otherWxId}`),

  // 删除会话（传会话ID）
  deleteConversation: conversationId => del(`/chat/conversation/${conversationId}`),

  // 删除聊天记录（旧接口，保留兼容）
  deleteChat: userId => del(`/chat/${userId}`),

  // 清空聊天记录
  clearChatHistory: userId => post(`/chat/${userId}/clear`),

  // 获取未读消息数量
  getUnreadCount: () => get('/chat/unread/count'),

  // 获取所有未读消息总数（包括聊天消息和通知消息）
  getUnreadTotal: () => get('/chat/unread/total'),

  // 获取在线用户列表
  getOnlineUsers: params => get('/chat/online/users', params),

  // 检查用户是否在线
  checkUserOnline: userId => get(`/chat/user/${userId}/online`),

  // 保存草稿
  saveDraft: (conversationId, draftContent) =>
    put(
      `/chat/conversation/${conversationId}/draft?draftContent=${encodeURIComponent(draftContent)}`,
      { conversationId }
    ),

  // 撤回消息
  recallMessage: messageId => del(`/chat/recall/${messageId}`),

  // 置顶/取消置顶会话（conversationId 作为路径参数，isPinned 作为查询参数）
  pinConversation: (conversationId, isPinned) =>
    put(`/chat/conversation/${conversationId}/pin?isPinned=${isPinned}`, {}),

  // 上传聊天图片
  uploadChatImage: filePath => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const app = getApp()
      const baseUrl = app.globalData.baseUrl

      wx.uploadFile({
        url: `${baseUrl}/chat/upload/image`,
        filePath: filePath,
        name: 'file',
        header: {
          Authorization: `Bearer ${token}`,
        },
        success: res => {
          try {
            const data = JSON.parse(res.data)
            resolve({ data })
          } catch (error) {
            reject(error)
          }
        },
        fail: reject,
      })
    })
  },

  // 上传聊天语音
  uploadChatVoice: filePath => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const app = getApp()
      const baseUrl = app.globalData.baseUrl

      wx.uploadFile({
        url: `${baseUrl}/chat/upload/voice`,
        filePath: filePath,
        name: 'file',
        header: {
          Authorization: `Bearer ${token}`,
        },
        success: res => {
          try {
            const data = JSON.parse(res.data)
            resolve({ data })
          } catch (error) {
            reject(error)
          }
        },
        fail: reject,
      })
    })
  },

  // 获取用户通知列表
  getNotificationList: params => post('/chat/notifications', params),

  // 标记通知为已读（单条或全部）
  // notificationId 为空时标记全部已读，有值时标记单条已读
  markNotificationRead: notificationId => {
    const url = notificationId
      ? `/chat/notifications/read?notificationId=${notificationId}`
      : '/chat/notifications/read'
    return put(url)
  },

  // 处理邀请（同意/拒绝）
  // data: { invitationId, notificationId, agree }
  handleInvitation: data => post('/chat/invitation/handle', data),
}

// ==================== 首页文章相关接口 ====================
const homeArticleApi = {
  // 分页查询首页文章列表
  getPage: params => post('/home/articles', params),
  // 分页查询本人创建的文章列表
  getMyArticlePage: params => post('/home-page-article/my-page', params),
  // 新增首页文章
  createArticle: data => post('/home-page-article/create', data),
  // 根据ID查询文章详情
  getHomeArticleDetail: id => get(`/home-page-article/${id}`),
  // 更新首页文章
  updateArticle: data => put('/home-page-article/update', data),
  // 删除文章
  deleteArticle: id => del(`/home-page-article/${id}`),
  // 获取管理的组织列表
  getManagedOrganizations: params => post('/home-page-article/managed-organizations', params),
}

// ==================== 文章审核相关接口 ====================
const articleApplyApi = {
  // 获取待审核记录列表（分页）
  getPendingList: params => post('/home-page-article-apply/pending/page', params),
  // 获取已审核记录列表（分页）
  getApprovedList: params => post('/home-page-article-apply/approved/page', params),
  // 审核文章
  approveArticle: data => post('/home-page-article-apply/approve', data),
  // 获取审核记录列表（分页）
  getApplyPage: params => post('/home-page-article-apply/page', params),
}

// ==================== 轮播图相关接口 ====================
const bannerApi = {
  // 获取首页轮播图列表
  getBannerList: () => get('/home/banners'),
  // 分页查询轮播图列表
  getBannerPage: params => post('/banner-management/page', params),
  // 新增轮播图
  createBanner: data => post('/banner-management/create', data),
  // 更新轮播图
  updateBanner: data => put('/banner-management/update', data),
  // 删除轮播图
  deleteBanner: id => del(`/banner-management/${id}`),
}

// ==================== 企业/场所相关接口 ====================
const placeApi = {
  // 获取我的企业/场所列表
  getMyPlaces: () => get('/alumni-place/my-list'),
  // 获取企业/场所详情
  getPlaceDetail: id => get(`/alumni-place/${id}`),
  // 申请企业/场所
  applyForPlace: params => post('/alumni-place/apply', params),
  // 更新企业/场所信息
  updatePlace: params => post('/alumni-place/management/update', params),
  // 获取企业详情（管理端）
  getPlaceManagementDetail: id => get(`/alumni-place/management/${id}`),
  // 获取企业申请列表
  getPlaceApplicationPage: params => post('/alumni-place/management/application/page', params),
  // 审核企业申请
  approvePlaceApplication: data => post('/alumni-place/management/application/approve', data),
}

// ==================== 校促会管理相关接口 ====================
const localPlatformManagementApi = {
  // 获取校促会成员列表
  getMemberPage: localPlatformId => post('/localPlatform/members/page', { localPlatformId }),
  // 获取校促会角色列表
  getRoleList: (organizeId, organizeType = 1) =>
    post('/localPlatformManagement/role/list', { organizeId, organizeType }),
  // 新增校促会角色
  addRole: data => post('/localPlatformManagement/role/add', data),
  // 更新校促会角色
  updateRole: data => put('/localPlatformManagement/role/update', data),
  // 删除校促会角色
  deleteRole: (roleOrId, organizeId) =>
    del('/localPlatformManagement/role/delete', { roleOrId, organizeId }),
  // 邀请校促会成员
  inviteMember: (
    localPlatformId,
    wxId,
    roleOrId,
    username,
    roleName,
    contactInformation,
    socialDuties,
    isShow,
    sort
  ) =>
    post('/localPlatformManagement/inviteMember', {
      localPlatformId,
      wxId,
      roleOrId,
      username,
      roleName,
      contactInformation,
      socialDuties,
      isShow,
      sort,
    }),
  // 删除校促会成员
  deleteMember: (localPlatformId, wxId) =>
    del('/localPlatformManagement/deleteMember', { localPlatformId, wxId }),
  // 更新校促会成员角色
  updateMemberRole: (
    localPlatformId,
    wxId,
    roleOrId,
    username,
    roleName,
    contactInformation,
    socialDuties,
    isShow,
    sort
  ) =>
    put('/localPlatformManagement/updateMemberRole', {
      localPlatformId,
      wxId,
      roleOrId,
      username,
      roleName,
      contactInformation,
      socialDuties,
      isShow,
      sort,
    }),
  // 添加预设成员
  addPresetMember: (
    localPlatformId,
    username,
    roleName,
    roleOrId,
    contactInformation,
    socialDuties,
    isShow,
    sort
  ) =>
    post('/localPlatformManagement/addPresetMember', {
      localPlatformId,
      username,
      roleName,
      roleOrId,
      contactInformation,
      socialDuties,
      isShow,
      sort,
    }),
  // 更新预设成员信息
  updatePresetMemberInfo: (
    memberId,
    username,
    roleName,
    contactInformation,
    socialDuties,
    isShow,
    sort
  ) =>
    put('/localPlatformManagement/updatePresetMemberInfo', {
      memberId,
      username,
      roleName,
      contactInformation,
      socialDuties,
      isShow,
      sort,
    }),
  // 删除预设成员
  deletePresetMember: memberId => del('/localPlatformManagement/deletePresetMember', { memberId }),
  // 更新预设成员（关联注册用户）
  updatePresetMember: (memberId, wxId) =>
    put('/localPlatformManagement/updatePresetMember', { memberId, wxId }),
  // 为校促会架构添加成员
  addMemberToStructure: data => post('/localPlatformManagement/member/addToStructure', data),
  // 从校促会架构移除成员
  removeMemberFromStructure: data =>
    del('/localPlatformManagement/member/removeFromStructure', data),
  // 获取校促会成员列表（用于添加到架构）
  getMemberList: localPlatformId => get(`/localPlatformManagement/member/list/${localPlatformId}`),
}

// ==================== 校友会管理相关接口 ====================
const alumniAssociationManagementApi = {
  // 获取校友会成员列表（支持分页与 keyword 搜索）
  getMemberList: (alumniAssociationId, keyword, current = 1, pageSize = 20) =>
    post('/alumniAssociationManagement/queryMemberList', {
      alumniAssociationId,
      keyword,
      current,
      pageSize,
    }),
  // 获取校友会角色列表
  getRoleList: organizeId => post('/alumniAssociationManagement/role/list', { organizeId }),
  // 新增校友会角色
  addRole: data => post('/alumniAssociationManagement/role/add', data),
  // 更新校友会角色
  updateRole: data => put('/alumniAssociationManagement/role/update', data),
  // 删除校友会角色
  deleteRole: (roleOrId, organizeId) =>
    del('/alumniAssociationManagement/role/delete', { roleOrId, organizeId }),
  // 邀请校友会成员（roleOrId 可选）
  inviteMember: (alumniAssociationId, wxId, roleOrId) => {
    const data = { alumniAssociationId, wxId }
    if (roleOrId) {
      data.roleOrId = roleOrId
    }
    return post('/alumniAssociationManagement/inviteMember', data)
  },
  // 删除校友会成员
  deleteMember: (alumniAssociationId, id, wxId) =>
    del('/alumniAssociationManagement/deleteMember', { alumniAssociationId, id, wxId }),
  // 更新校友会成员角色
  updateMemberRole: (alumniAssociationId, wxId, roleOrId) =>
    put('/alumniAssociationManagement/updateMemberRole', { alumniAssociationId, wxId, roleOrId }),
  // 添加成员到分支（组织架构角色）
  addMemberToBranch: (alumniAssociationId, wxId, roleOrId) =>
    post('/alumniAssociationManagement/addMemberToBranch', { alumniAssociationId, wxId, roleOrId }),
  // 从分支移除成员
  removeMemberFromBranch: (alumniAssociationId, wxId) =>
    del('/alumniAssociationManagement/removeMemberFromBranch', { alumniAssociationId, wxId }),
  // 更新校友会成员信息（所有字段除id外都是可选的）
  // data: { id, username?, roleName?, userPhone?, userAffiliation?, isShowOnHome? }
  updateMemberInfo: data => put('/alumniAssociationManagement/updateMemberInfo', data),
  // 添加未注册成员到校友会
  // data: { alumniAssociationId, username, roleName, userPhone, userAffiliation }
  addUnregisteredMember: data => post('/alumniAssociationManagement/addUnregisteredMember', data),
  // 发布活动
  publishActivity: data => post('/alumniAssociationManagement/activity/publish', data),
  // 获取活动详情
  getActivityDetail: activityId =>
    get(`/alumniAssociationManagement/activity/detail/${activityId}`),
  // 更新活动（保留空字符串字段，支持清空地址等信息）
  updateActivity: data =>
    request({ url: '/alumniAssociationManagement/activity/update', method: 'PUT', data }),
  // 删除活动
  deleteActivity: activityId => del(`/alumniAssociationManagement/activity/delete/${activityId}`),
  // 获取校友会活动列表
  getActivities: alumniAssociationId =>
    get(`/alumniAssociationManagement/activities/${alumniAssociationId}`),
}

// ==================== 校友会加入申请相关接口 ====================
const joinApplicationApi = {
  // 获取加入申请列表
  getApplicationPage: params => post('/AlumniAssociationJoinApplication/page', params),
  // 审核加入申请
  reviewApplication: params => post('/AlumniAssociationJoinApplication/review', params),
  // 根据ID获取加入申请详情
  getApplicationDetail: applicationId =>
    get(`/AlumniAssociationJoinApplication/detailById/${applicationId}`),
}

// ==================== 用户反馈相关接口 ====================
const feedbackApi = {
  // 提交用户反馈
  submit: data => post('/feedback/submit', data),
}

// ==================== 审核统计相关接口 ====================
const auditApi = {
  // 获取审核待办数量统计
  getTodoCount: () => get('/audit/statistics/todoCount'),
}

// ==================== 我的申请记录（创建校友会 / 加入校友会 / 加入校促会 聚合） ====================
const myApplicationRecordApi = {
  queryPage: params => post('/users/my-application-records/page', params),
  update: data => put('/users/my-application-records/update', data),
  cancel: data => put('/users/my-application-records/cancel', data),
}

module.exports = {
  schoolApi,
  associationApi,
  unionApi,
  localPlatformApi,
  localPlatformManagementApi,
  alumniApi,
  couponApi,
  circleApi,
  merchantApi,
  shopApi,
  nearbyApi,
  activityApi,
  alumniAssociationManagementApi,
  joinApplicationApi,
  userApi,
  searchApi,
  followApi,
  authApi,
  fileApi,
  chatApi,
  homeArticleApi,
  articleApplyApi,
  bannerApi,
  placeApi,
  feedbackApi,
  auditApi,
  myApplicationRecordApi,
}
