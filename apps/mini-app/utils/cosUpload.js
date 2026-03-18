// utils/cosUpload.js
// COS 前端直传工具 - 绕过云托管网关大小限制
// 使用 cos-wx-sdk-v5 直接上传文件到腾讯云对象存储

const COS = require('../libs/cos-wx-sdk-v5.js')
const config = require('./config.js')
const { get, post } = require('./request.js')

// 缓存凭证，避免重复请求
let cachedCredential = null
let credentialExpireTime = 0

/**
 * 从后端获取 COS 临时凭证
 * @returns {Promise<Object>} 凭证信息
 */
async function fetchCosCredential() {
  const now = Math.floor(Date.now() / 1000)

  // 凭证还有 5 分钟以上有效期，直接使用缓存
  if (cachedCredential && credentialExpireTime - now > 300) {
    console.log('[CosUpload] 使用缓存的临时凭证')
    return cachedCredential
  }

  console.log('[CosUpload] 从后端获取 COS 临时凭证')
  const response = await get('/file/cos/credential')
  const res = response.data || response

  if (res.code !== 200 || !res.data) {
    throw new Error(res.msg || '获取 COS 临时凭证失败')
  }

  cachedCredential = res.data
  credentialExpireTime = res.data.expiredTime || 0

  console.log('[CosUpload] 凭证获取成功，过期时间:', credentialExpireTime)
  return cachedCredential
}

/**
 * 创建 COS 实例
 * @returns {Object} COS SDK 实例
 */
function createCosInstance() {
  return new COS({
    getAuthorization: function (options, callback) {
      fetchCosCredential()
        .then(credential => {
          callback({
            TmpSecretId: credential.tmpSecretId,
            TmpSecretKey: credential.tmpSecretKey,
            SecurityToken: credential.token,
            StartTime: Math.floor(Date.now() / 1000),
            ExpiredTime: credential.expiredTime,
          })
        })
        .catch(err => {
          console.error('[CosUpload] 获取凭证失败:', err)
          callback({ err: err })
        })
    },
  })
}

/**
 * 生成基于日期的存储路径
 * @param {string} subPath 子路径（如 images、audios）
 * @param {string} fileName 文件名
 * @returns {string} 完整的 COS Key
 */
function generateCosKey(subPath, fileName) {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')

  // 与后端 CosUtils 保持一致: uploadPath/subPath/yyyy/MM/dd/fileName
  const uploadPath = cachedCredential ? cachedCredential.uploadPath : 'cni-alumni'
  return `${uploadPath}/${subPath}/${year}/${month}/${day}/${fileName}`
}

/**
 * 生成 UUID 文件名
 * @param {string} originalName 原始文件名
 * @returns {string} UUID 文件名（保留扩展名）
 */
function generateUuidFileName(originalName) {
  const ext = getFileExtension(originalName)
  const uuid = generateUUID()
  return ext ? `${uuid}.${ext}` : uuid
}

/**
 * 获取文件扩展名
 * @param {string} fileName 文件名
 * @returns {string} 扩展名（不含点号）
 */
function getFileExtension(fileName) {
  if (!fileName) return ''
  const lastDot = fileName.lastIndexOf('.')
  if (lastDot === -1) return ''
  return fileName.substring(lastDot + 1).toLowerCase()
}

/**
 * 生成简单 UUID
 */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 根据扩展名获取 MIME 类型
 */
function getMimeType(ext) {
  const mimeMap = {
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    gif: 'image/gif',
    bmp: 'image/bmp',
    webp: 'image/webp',
    svg: 'image/svg+xml',
    ico: 'image/x-icon',
    tiff: 'image/tiff',
    tif: 'image/tiff',
    mp3: 'audio/mpeg',
    wav: 'audio/wav',
    flac: 'audio/flac',
    aac: 'audio/aac',
    ogg: 'audio/ogg',
    mp4: 'video/mp4',
    pdf: 'application/pdf',
    doc: 'application/msword',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    xls: 'application/vnd.ms-excel',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  }
  return mimeMap[ext] || 'application/octet-stream'
}

/**
 * 通用 COS 直传方法
 * 流程：选图 → 获取凭证 → 直传 COS → 调用后端保存文件记录 → 返回 FilesVo
 *
 * @param {string} tempFilePath 本地临时文件路径
 * @param {string} originalName 原始文件名
 * @param {string} fileType 文件类型（image、audio、document）
 * @param {string} subPath 存储子路径（images、audios、documents）
 * @param {number} fileSize 文件大小（字节）
 * @returns {Promise<Object>} 后端返回的 FilesVo（含 fileId、fileUrl 等）
 */
async function uploadToCos(tempFilePath, originalName, fileType, subPath, fileSize) {
  // 如果没有传 originalName 或 originalName 没有扩展名，从 tempFilePath 中提取
  if (!originalName || !originalName.includes('.')) {
    const pathParts = tempFilePath.replace(/\\/g, '/').split('/')
    const fileNameFromPath = pathParts[pathParts.length - 1] || ''
    if (fileNameFromPath.includes('.')) {
      originalName = fileNameFromPath
    } else {
      // 都没有扩展名，根据 fileType 补充默认扩展名
      const defaultExtMap = { image: '.jpg', audio: '.mp3', document: '.pdf' }
      const ext = defaultExtMap[fileType] || '.jpg'
      originalName = (originalName || 'unknown') + ext
    }
  }

  // 1. 先获取凭证（确保 cachedCredential 中有 bucket、region 等信息）
  const credential = await fetchCosCredential()

  // 2. 生成 UUID 文件名和 COS Key
  const uuidFileName = generateUuidFileName(originalName)
  const cosKey = generateCosKey(subPath, uuidFileName)
  const fileExtension = getFileExtension(originalName)
  const mimeType = getMimeType(fileExtension)

  console.log('[CosUpload] 开始上传文件到 COS')
  console.log('[CosUpload] Bucket:', credential.bucket)
  console.log('[CosUpload] Region:', credential.region)
  console.log('[CosUpload] Key:', cosKey)

  // 3. 使用 COS SDK 直传
  const cos = createCosInstance()

  const cosResult = await new Promise((resolve, reject) => {
    cos.uploadFile(
      {
        Bucket: credential.bucket,
        Region: credential.region,
        Key: cosKey,
        FilePath: tempFilePath,
        onProgress: function (progressData) {
          console.log('[CosUpload] 上传进度:', JSON.stringify(progressData))
        },
      },
      function (err, data) {
        if (err) {
          console.error('[CosUpload] COS 上传失败:', err)
          reject(err)
        } else {
          console.log('[CosUpload] COS 上传成功:', data)
          resolve(data)
        }
      }
    )
  })

  // 4. 构建文件路径和 URL
  const filePath = '/' + cosKey
  const baseUrl = credential.baseUrl
  const fileUrl = baseUrl.endsWith('/') ? baseUrl + cosKey : baseUrl + '/' + cosKey

  console.log('[CosUpload] 文件路径:', filePath)
  console.log('[CosUpload] 文件 URL:', fileUrl)

  // 5. 调用后端接口保存文件记录到 Files 表
  const saveResponse = await post('/file/cos/saveRecord', {
    fileName: uuidFileName,
    originalName: originalName,
    fileExtension: fileExtension,
    fileType: fileType,
    filePath: filePath,
    fileUrl: fileUrl,
    fileSize: fileSize || 0,
    mimeType: mimeType,
  })
  const saveRes = saveResponse.data || saveResponse

  if (saveRes.code !== 200 || !saveRes.data) {
    console.error('[CosUpload] 保存文件记录失败:', saveRes)
    throw new Error(saveRes.msg || '保存文件记录失败')
  }

  console.log('[CosUpload] 文件记录保存成功，fileId:', saveRes.data.fileId)
  return saveRes.data
}

/**
 * 上传图片到 COS（便捷方法）
 */
function uploadImageToCos(tempFilePath, originalName, fileSize) {
  return uploadToCos(tempFilePath, originalName, 'image', 'images', fileSize)
}

/**
 * 上传音频到 COS（便捷方法）
 */
function uploadAudioToCos(tempFilePath, originalName, fileSize) {
  return uploadToCos(tempFilePath, originalName, 'audio', 'audios', fileSize)
}

/**
 * 上传文档到 COS（便捷方法）
 */
function uploadDocumentToCos(tempFilePath, originalName, fileSize) {
  return uploadToCos(tempFilePath, originalName, 'document', 'documents', fileSize)
}

module.exports = {
  uploadToCos,
  uploadImageToCos,
  uploadAudioToCos,
  uploadDocumentToCos,
  fetchCosCredential,
}
