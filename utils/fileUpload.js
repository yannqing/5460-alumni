// utils/fileUpload.js
// 文件上传和下载工具 - 严格区分文件类型

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
  
  return headers
}

/**
 * 获取 baseUrl
 */
function getBaseUrl() {
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
 * 通用文件上传方法
 * @param {string} filePath 文件路径
 * @param {string} uploadUrl 上传接口URL
 * @param {string} fileFieldName 文件字段名（如 'image', 'audio', 'video' 等）
 * @param {string} originalName 原始文件名（可选）
 * @param {object} extraFormData 额外的表单数据（可选）
 * @returns {Promise} 返回上传结果
 */
function uploadFile(filePath, uploadUrl, fileFieldName, originalName = '', extraFormData = {}) {
  const baseUrl = getBaseUrl()
  if (!baseUrl) {
    return Promise.reject({
      code: -1,
      msg: 'baseUrl 未配置，请检查 app.globalData.baseUrl'
    })
  }
  
  const url = baseUrl + uploadUrl
  
  const headers = getHeaders()
  
  // 构建表单数据
  const formData = {
    ...extraFormData
  }
  
  if (originalName) {
    formData.originalName = originalName
  }
  
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: url,
      filePath: filePath,
      name: fileFieldName,
      formData: formData,
      header: headers,
      success: (res) => {
        // 检查 HTTP 状态码
        if (res.statusCode !== 200) {
          const error = {
            code: res.statusCode,
            msg: `上传失败，HTTP状态码：${res.statusCode}`
          }
          reject(error)
          return
        }

        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data)
          } else {
            // 返回错误信息，不在这里显示 toast，由调用方处理
            reject({
              code: data.code,
              msg: data.msg || '上传失败',
              data: data
            })
          }
        } catch (error) {
          // JSON 解析失败
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

