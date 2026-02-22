// utils/Crypto/index.js
const md5 = require('./md5.min');
const base64 = require('./base64.min');
import { RandomStr } from './RandomStr'

/**
 * 参数加密函数
 * 将请求参数进行 base64 编码 + MD5 签名
 */
export function crypto_param($param, $publicKey) {
  if (!isPlainObject($param)) { return false; }

  // 添加时间戳
  $param.timestamp = parseInt(new Date().getTime().toString());

  // 添加随机字符串
  const nonce_str = RandomStr(20);
  $param.nonce_str = nonce_str;

  // 克隆参数
  const _data = cloneObj($param);

  // base64 编码
  const $param_str = base64.encode(JSON.stringify(_data));

  // MD5 签名
  const $sign_str = md5($param_str);

  return {
    'params': _data,       // 原始参数
    'key_str': $param_str, // base64 编码后的字符串
    'sign_str': $sign_str  // MD5 签名
  };
}

// 判断是否为纯粹对象
function isPlainObject(obj) {
  if (!obj || obj.toString() !== "[object Object]" || obj.nodeType || obj.setInterval) {
    return false;
  }
  if (obj.constructor && !obj.hasOwnProperty("constructor") && !obj.constructor.prototype.hasOwnProperty("isPrototypeOf")) {
    return false;
  }
  for (var key in obj) { }
  return key === undefined || obj.hasOwnProperty(key);
}

// 深拷贝对象
function cloneObj(obj) {
  if (!isPlainObject(obj)) { return false; }
  return JSON.parse(JSON.stringify(obj));
}
