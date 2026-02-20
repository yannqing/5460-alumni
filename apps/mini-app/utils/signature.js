// utils/signature.js
// API 签名工具类 - 根据《前端API签名验证对接文档》实现

/**
 * 生成 UUID
 */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16);
  });
}

/**
 * HMAC-SHA256 加密
 * 根据文档要求，使用 HMAC-SHA256 算法，返回十六进制小写字符串
 */
function hmacSha256(message, secret) {
  const { hmacSha256: hmac } = require('./Crypto/hmac-sha256.js');
  return hmac(message, secret);
}

/**
 * 将值转换为接近 Java Map/List 的 toString 结果
 * 目的：与后端 Jackson 反序列化后的 value.toString() 保持一致
 *
 * 规则（对照后端）
 * - null / undefined：不参与签名（在调用处直接过滤）
 * - 原始类型：String(value)
 * - 数组：类似 Java List -> [v1, v2]
 * - 对象：类似 Java Map  -> {k1=v1, k2=v2}
 */
function javaStyleToString(value) {
  if (value === null || value === undefined) {
    return '';
  }

  const type = typeof value;

  // 原始类型：数字 / 布尔 / 字符串，直接 String
  if (type === 'number' || type === 'boolean' || type === 'string') {
    return String(value);
  }

  // 数组 => [elem1, elem2]
  if (Array.isArray(value)) {
    const items = value.map((item) => javaStyleToString(item));
    return '[' + items.join(', ') + ']';
  }

  // 普通对象 => {k1=v1, k2=v2}
  if (type === 'object') {
    const keys = Object.keys(value);
    const parts = keys.map((key) => {
      const v = javaStyleToString(value[key]);
      return key + '=' + v;
    });
    return '{' + parts.join(', ') + '}';
  }

  // 兜底
  return String(value);
}

/**
 * API 签名工具
 */
const SignatureUtil = {
  // 签名密钥（开发环境）
  SECRET: 'cni-alumni-signature-secret-2025',

  /**
   * 检查接口是否需要签名
   * @param {string} url - 请求 URL
   * @returns {boolean} - true 表示需要签名，false 表示不需要
   */
  needSignature(url) {
    if (!url) return true;
    
    // 白名单接口不需要签名
    const whitelist = [
      '/api/auth/wx_init',
      '/api/auth/login',  // 登录接口（兼容）
      '/auth/login',      // 登录接口
      '/api/health',
      '/actuator',
      '/swagger-ui',
      '/doc.html',
      '/v3/api-docs',
      '/webjars',
      '/druid'
    ];
    
    // 检查是否在白名单中
    for (let i = 0; i < whitelist.length; i++) {
      if (url.includes(whitelist[i])) {
        return false;
      }
    }
    
    return true;
  },

  /**
   * 生成签名
   * @param {Object} params - 请求参数（包括 URL 参数、Body 参数等）
   * @returns {Object} - 包含 timestamp, nonce, signature 的对象
   */
  generateSignature(params = {}) {
    // 1. 生成 timestamp 和 nonce（业务参数不参与签名，只使用时间戳与随机串）
    const timestamp = Date.now().toString();
    const nonce = generateUUID();

    // 2. 仅使用安全相关参数参与签名
    const allParams = {
      timestamp,
      nonce
    };

    // 3. 移除 signature 参数（如果存在）
    delete allParams.signature;

    // 4. 参数排序（按 key 字典序）
    const sortedKeys = Object.keys(allParams).sort();

    // 5. 拼接参数
    let signContent = '';
    sortedKeys.forEach(key => {
      const rawValue = allParams[key];

      // 跳过空值（与后端一致：null 不参与签名，空字符串也跳过）
      if (rawValue === null || rawValue === undefined || rawValue === '') {
        return;
      }

      // 使用接近 Java Map/List 的 toString 规则进行序列化
      const strValue = javaStyleToString(rawValue);

      // 如果序列化结果为空，同样跳过
      if (strValue === '') {
        return;
      }

      signContent += `${key}=${strValue}&`;
    });

    // 6. 追加密钥
    signContent += `key=${this.SECRET}`;

    // 7. 计算 HMAC-SHA256
    const signature = hmacSha256(signContent, this.SECRET);

    return {
      timestamp,
      nonce,
      signature
    };
  },

  /**
   * 为请求添加签名
   * @param {Object} options - 请求选项
   * @param {string} options.url - 请求 URL（相对路径，用于白名单判断）
   * @param {string} options.fullUrl - 完整 URL（用于提取 URL 参数）
   * @param {Object} options.data - 请求数据（Body 参数或 URL 参数）
   * @param {Object} options.header - 请求头
   * @param {string} options.method - 请求方法
   * @returns {Object} - 添加签名后的请求选项
   */
  signRequest(options) {
    const { url, fullUrl, data = {}, header = {}, method = 'POST' } = options;

    // 检查是否需要签名（使用相对路径判断）
    if (!this.needSignature(url)) {
      return options;
    }

    // 收集所有参数
    let params = {};

    // 1. 提取 URL 参数（GET 请求或 URL 中有参数的情况）
    if (fullUrl && fullUrl.includes('?')) {
      const queryString = fullUrl.split('?')[1];
      if (queryString) {
        const pairs = queryString.split('&');
        pairs.forEach(pair => {
          if (!pair) return;

          const [rawKey, rawValue = ''] = pair.split('=');
          if (rawKey) {
            // 与后端 Servlet 行为保持一致：先把 '+' 还原为空格，再做 URL 解码
            const key = decodeURIComponent(rawKey.replace(/\+/g, ' '));
            const valueDecoded = decodeURIComponent(rawValue.replace(/\+/g, ' '));
            params[key] = valueDecoded;
          }
        });
      }
    }

    // 2. Body 参数（POST/PUT 请求）
    if (data && typeof data === 'object' && !data.isAllUrl) {
      // 深拷贝，避免修改原对象
      const bodyParams = JSON.parse(JSON.stringify(data));
      // 合并到 params（Body 参数优先级高于 URL 参数）
      params = { ...params, ...bodyParams };
    }

    // 生成签名
    const { timestamp, nonce, signature } = this.generateSignature(params);

    // 添加到 Header（根据文档要求）
    header['X-Timestamp'] = timestamp;
    header['X-Nonce'] = nonce;
    header['X-Signature'] = signature;

    return {
      ...options,
      header
    };
  }
};

module.exports = SignatureUtil;

