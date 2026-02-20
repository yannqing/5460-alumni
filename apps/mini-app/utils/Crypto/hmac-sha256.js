// utils/Crypto/hmac-sha256.js
// HMAC-SHA256 实现 - 使用 crypto-js 库

const CryptoJS = require('./crypto-js.js');

/**
 * HMAC-SHA256 实现
 * 根据《前端API签名验证对接文档》要求，使用 HMAC-SHA256 算法，返回十六进制小写字符串
 * @param {string} message - 待签名字符串
 * @param {string} secret - 签名密钥
 * @returns {string} - HMAC-SHA256 签名值（十六进制小写）
 */
function hmacSha256(message, secret) {
  return CryptoJS.HmacSHA256(message, secret).toString(CryptoJS.enc.Hex);
}

/**
 * SHA256 哈希函数
 * @param {string} str - 待哈希字符串
 * @returns {string} - SHA256 哈希值（十六进制小写）
 */
function sha256(str) {
  return CryptoJS.SHA256(str).toString(CryptoJS.enc.Hex);
}

module.exports = {
  hmacSha256,
  sha256
};

