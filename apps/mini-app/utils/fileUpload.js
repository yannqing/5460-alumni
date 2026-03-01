// utils/fileUpload.js
// 文件上传和下载工具 - 严格区分文件类型
// 支持云托管模式和传统模式

const config = require('./config.js')

/**
 * 获取请求头（包含 token）
 */
function getHeaders() {
  const headers = {}

  // 获取 token
  let token = wx.getStorageSync('token') || ''
  if (!token) {
    const uinfo = wx.getStorageSync('userInfo') || {}
    token = uinfo.token || ''
  }

  if (token) {
    headers.token = token
  }

  // 云托管模式需要添加服务标识
  if (config.IS_CLOUD_HOST) {
    headers['X-WX-SERVICE'] = config.cloud.serviceName
  }

  return headers
}

/**
 * 获取文件上传的 baseUrl
 * 云托管模式使用云托管公网地址，传统模式使用 app.globalData.baseUrl
 */
function getUploadBaseUrl() {
  // 云托管模式：使用云托管公网访问地址
  if (config.IS_CLOUD_HOST && config.cloud.publicUrl) {
    return config.cloud.publicUrl
  }

  // 传统模式：使用 app.globalData.baseUrl
  try {
    const app = getApp()
    if (app && app.globalData && app.globalData.baseUrl) {
      return app.globalData.baseUrl
    }
  } catch (error) {
    // getApp() 可能失败
  }

  // 如果获取失败，返回默认值或从配置中读取
  return ''
}

/**
 * 获取 baseUrl（兼容旧代码）
 */
function getBaseUrl() {
  return getUploadBaseUrl()
}

/**
 * 通用文件上传方法
 * @param {string} filePath 文件路径
 * @param {string} uploadUrl 上传接口URL
 * @param {string} fileFieldName 文件字段名（如 'image', 'audio', 'video' 等）
 * @param {string} originalName 原始文件名（可选）
 * @param {object} extraFormData 额外的表单数据（可选）
 * @returns {Promise} 返回上传结果
 */
function uploadFile(filePath, uploadUrl, fileFieldName, originalName = '', extraFormData = {}) {
  const baseUrl = getUploadBaseUrl()

  console.log('[FileUpload] ========== 文件上传调试信息 ==========')
  console.log('[FileUpload] 云托管模式:', config.IS_CLOUD_HOST)
  console.log('[FileUpload] baseUrl:', baseUrl)
  console.log('[FileUpload] uploadUrl:', uploadUrl)
  console.log('[FileUpload] filePath:', filePath)
  console.log('[FileUpload] fileFieldName:', fileFieldName)

  if (!baseUrl) {
    const errorMsg = config.IS_CLOUD_HOST
      ? 'baseUrl 为空，请在 config.js 中配置 cloud.publicUrl（云托管公网访问地址）'
      : 'baseUrl 未配置，请检查 app.globalData.baseUrl'
    console.error('[FileUpload] ' + errorMsg)
    return Promise.reject({
      code: -1,
      msg: errorMsg
    })
  }

  const url = baseUrl + uploadUrl
  console.log('[FileUpload] 完整上传URL:', url)

  const headers = getHeaders()
  console.log('[FileUpload] 请求头:', headers)

  // 构建表单数据
  const formData = {
    ...extraFormData
  }

  if (originalName) {
    formData.originalName = originalName
  }

  // 云托管模式：使用 wx.cloud.callContainer (情况A)
  if (config.IS_CLOUD_HOST) {
    console.log('[FileUpload] 云托管模式：使用 wx.cloud.callContainer 上传文件')
    const boundary = '----WebKitFormBoundary' + Math.random().toString(36).substring(2, 12)

    return new Promise((resolve, reject) => {
      try {
        const fs = wx.getFileSystemManager()
        const fileContent = fs.readFileSync(filePath)

        // 获取文件后缀名推导 Content-Type
        const ext = filePath.split('.').pop().toLowerCase()
        const mimeMap = {
          'jpg': 'image/jpeg', 'jpeg': 'image/jpeg', 'png': 'image/png', 'gif': 'image/gif',
          'mp3': 'audio/mpeg', 'wav': 'audio/wav', 'mp4': 'video/mp4'
        }
        const contentType = mimeMap[ext] || 'application/octet-stream'

        // 构造二进制 body
        const body = buildMultipartBody(
          formData,
          [{
            name: fileFieldName,
            buffer: fileContent,
            fileName: originalName || `file.${ext}`,
            contentType: contentType
          }],
          boundary
        )

        wx.cloud.callContainer({
          config: {
            env: config.cloud.env
          },
          path: uploadUrl,
          method: 'POST',
          header: {
            'X-WX-SERVICE': config.cloud.serviceName,
            ...headers,
            'content-type': `multipart/form-data; boundary=${boundary}`
          },
          data: body,
          success: (res) => {
            console.log('[FileUpload] wx.cloud.callContainer success 回调')
            console.log('[FileUpload] 状态码:', res.statusCode)
            console.log('[FileUpload] 响应数据:', res.data)

            if (res.statusCode === 200) {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if (data.code === 200) {
                console.log('[FileUpload] 上传成功!')
                resolve(data)
              } else {
                console.error('[FileUpload] 业务错误:', data)
                reject({
                  code: data.code,
                  msg: data.msg || '上传失败',
                  data: data
                })
              }
            } else {
              const error = {
                code: res.statusCode,
                msg: `上传失败，状态码：${res.statusCode}`
              }
              console.error('[FileUpload] 状态码错误:', error)
              reject(error)
            }
          },
          fail: (error) => {
            console.error('[FileUpload] wx.cloud.callContainer fail 回调')
            console.error('[FileUpload] 错误信息:', error)
            reject({
              code: -1,
              msg: '云托管上传失败，请检查网络',
              error: error
            })
          }
        })
      } catch (err) {
        console.error('[FileUpload] 构建上传数据失败:', err)
        reject({
          code: -1,
          msg: '构建上传数据失败',
          error: err
        })
      }
    })
  }

  // 传统模式：使用 wx.uploadFile
  console.log('[FileUpload] 传统模式：开始调用 wx.uploadFile...')
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: url,
      filePath: filePath,
      name: fileFieldName,
      formData: formData,
      header: headers,
      success: (res) => {
        console.log('[FileUpload] wx.uploadFile success 回调')
        console.log('[FileUpload] HTTP状态码:', res.statusCode)
        console.log('[FileUpload] 响应数据:', res.data)

        // 检查 HTTP 状态码
        if (res.statusCode !== 200) {
          const error = {
            code: res.statusCode,
            msg: `上传失败，HTTP状态码：${res.statusCode}`
          }
          console.error('[FileUpload] HTTP状态码错误:', error)
          reject(error)
          return
        }

        try {
          const data = JSON.parse(res.data)
          console.log('[FileUpload] 解析后的数据:', data)

          if (data.code === 200) {
            console.log('[FileUpload] 上传成功!')
            resolve(data)
          } else {
            // 返回错误信息，不在这里显示 toast，由调用方处理
            console.error('[FileUpload] 业务错误:', data)
            reject({
              code: data.code,
              msg: data.msg || '上传失败',
              data: data
            })
          }
        } catch (error) {
          // JSON 解析失败
          console.error('[FileUpload] JSON解析失败:', error)
          reject({
            code: -1,
            msg: '响应解析失败',
            error: error,
            rawData: res.data
          })
        }
      },
      fail: (error) => {
        // 网络错误，返回错误信息，不在这里显示 toast
        console.error('[FileUpload] wx.uploadFile fail 回调')
        console.error('[FileUpload] 错误信息:', error)
        reject({
          code: -1,
          msg: '网络错误，请检查网络连接',
          error: error
        })
      }
    })
  })
}

/**
 * 上传图片文件（通用方法，接收接口路径）
 * @param {string} filePath 图片文件路径
 * @param {string} uploadPath 上传接口路径（如 '/file/upload/images'）
 * @param {string} originalName 原始文件名（可选）
 * @returns {Promise} 返回上传结果，包含 fileUrl
 */
function uploadImage(filePath, uploadPath, originalName = '') {
  return uploadFile(filePath, uploadPath, 'image', originalName)
}

/**
 * 上传音频文件（通用方法，接收接口路径）
 * @param {string} filePath 音频文件路径
 * @param {string} uploadPath 上传接口路径（如 '/file/upload/audio'）
 * @param {string} originalName 原始文件名（可选）
 * @returns {Promise} 返回上传结果，包含 fileUrl
 */
function uploadAudio(filePath, uploadPath, originalName = '') {
  return uploadFile(filePath, uploadPath, 'audio', originalName)
}

/**
 * 上传视频文件（通用方法，接收接口路径）
 * @param {string} filePath 视频文件路径
 * @param {string} uploadPath 上传接口路径（如 '/file/upload/video'）
 * @param {string} originalName 原始文件名（可选）
 * @returns {Promise} 返回上传结果，包含 fileUrl
 */
function uploadVideo(filePath, uploadPath, originalName = '') {
  return uploadFile(filePath, uploadPath, 'video', originalName)
}

/**
 * 上传其他格式文件（通用方法，接收接口路径）
 * @param {string} filePath 文件路径
 * @param {string} uploadPath 上传接口路径（如 '/file/upload/other'）
 * @param {string} originalName 原始文件名（可选）
 * @returns {Promise} 返回上传结果，包含 fileUrl
 */
function uploadOtherFile(filePath, uploadPath, originalName = '') {
  return uploadFile(filePath, uploadPath, 'file', originalName)
}

/**
 * 下载文件（通用方法，接收接口路径）
 * @param {string|number} fileId 文件ID
 * @param {string} downloadPath 下载接口路径模板（如 '/file/download/{fileId}'，{fileId} 会被替换）
 * @param {string} savePath 保存路径（可选，不传则使用临时路径）
 * @returns {Promise} 返回下载结果，包含文件路径
 */
function downloadFile(fileId, downloadPath, savePath = '') {
  const baseUrl = getBaseUrl()
  if (!baseUrl) {
    return Promise.reject({
      code: -1,
      msg: 'baseUrl 未配置，请检查 app.globalData.baseUrl'
    })
  }

  // 替换路径中的 {fileId} 占位符
  const url = baseUrl + downloadPath.replace('{fileId}', fileId)

  const headers = getHeaders()

  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url: url,
      header: headers,
      filePath: savePath, // 如果为空，系统会自动生成临时路径
      success: (res) => {
        if (res.statusCode === 200) {
          resolve({
            filePath: res.tempFilePath || res.filePath,
            statusCode: res.statusCode
          })
        } else {
          wx.showToast({
            title: '下载失败',
            icon: 'none'
          })
          reject(new Error(`下载失败，状态码：${res.statusCode}`))
        }
      },
      fail: (error) => {
        wx.showToast({
          title: '下载失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

/**
 * 构造 multipart/form-data 二进制数据
 * @param {Object} fields 文本字段
 * @param {Array} files 文件列表 [{ name, buffer, fileName, contentType }]
 * @param {string} boundary 边界字符串
 */
function buildMultipartBody(fields, files, boundary) {
  const binaryParts = []

  // 模拟 TextEncoder 的简单实现（处理 ASCII/UTF-8）
  const strToUint8Array = (str) => {
    const arr = []
    for (let i = 0; i < str.length; i++) {
      const code = str.charCodeAt(i)
      if (code < 0x80) {
        arr.push(code)
      } else if (code < 0x800) {
        arr.push(0xc0 | (code >> 6))
        arr.push(0x80 | (code & 0x3f))
      } else {
        arr.push(0xe0 | (code >> 12))
        arr.push(0x80 | ((code >> 6) & 0x3f))
        arr.push(0x80 | (code & 0x3f))
      }
    }
    return new Uint8Array(arr)
  }

  // 添加文本字段
  for (const key in fields) {
    if (fields[key] !== undefined && fields[key] !== null) {
      binaryParts.push(strToUint8Array(`--${boundary}\r\n`))
      binaryParts.push(strToUint8Array(`Content-Disposition: form-data; name="${key}"\r\n\r\n`))
      binaryParts.push(strToUint8Array(`${fields[key]}\r\n`))
    }
  }

  // 添加文件字段
  files.forEach(file => {
    const header = `--${boundary}\r\n` +
      `Content-Disposition: form-data; name="${file.name}"; filename="${file.fileName}"\r\n` +
      `Content-Type: ${file.contentType || 'application/octet-stream'}\r\n\r\n`

    binaryParts.push(strToUint8Array(header))
    binaryParts.push(new Uint8Array(file.buffer))
    binaryParts.push(strToUint8Array('\r\n'))
  })

  // 结束边界
  binaryParts.push(strToUint8Array(`--${boundary}--\r\n`))

  // 合并所有内容
  let totalLength = 0
  binaryParts.forEach(p => totalLength += p.length)

  const result = new Uint8Array(totalLength)
  let offset = 0
  binaryParts.forEach(p => {
    result.set(p, offset)
    offset += p.length
  })

  return result.buffer
}

/**
 * 保存文件到本地（从下载的文件保存到用户指定的位置）
 * @param {string} tempFilePath 临时文件路径（通常是下载得到的路径）
 * @returns {Promise} 返回保存结果
 */
function saveFileToLocal(tempFilePath) {
  return new Promise((resolve, reject) => {
    wx.saveFile({
      tempFilePath: tempFilePath,
      success: (res) => {
        resolve({
          savedFilePath: res.savedFilePath
        })
      },
      fail: (error) => {
        wx.showToast({
          title: '保存失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

module.exports = {
  uploadImage,
  uploadAudio,
  uploadVideo,
  uploadOtherFile,
  downloadFile,
  saveFileToLocal
}

