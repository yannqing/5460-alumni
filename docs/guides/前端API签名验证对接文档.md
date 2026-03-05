# CNI Alumni 前端 API 签名验证对接文档

## 📋 目录

1. [概述](#概述)
2. [签名验证原理](#签名验证原理)
3. [签名生成步骤](#签名生成步骤)
4. [前端实现指南](#前端实现指南)
5. [代码示例](#代码示例)
6. [常见问题](#常见问题)
7. [测试与调试](#测试与调试)

---

## 概述

### 什么是签名验证？

签名验证是一种安全机制,用于确保 API 请求的**真实性**和**完整性**,防止:
- **数据篡改**: 请求参数在传输过程中被修改
- **重放攻击**: 恶意用户截获请求并重复发送
- **伪造请求**: 未授权的客户端伪造请求

### 签名验证流程

```
┌─────────┐                                    ┌─────────┐
│  前端    │                                    │  后端    │
└────┬────┘                                    └────┬────┘
     │                                               │
     │ 1. 生成 timestamp 和 nonce                    │
     │ 2. 计算签名 signature                         │
     │ 3. 发送请求（携带 timestamp, nonce, signature）│
     ├──────────────────────────────────────────────>│
     │                                               │
     │                          4. 验证 timestamp    │
     │                          5. 验证 nonce 未使用 │
     │                          6. 重新计算签名并比对 │
     │                          7. 验证通过/失败      │
     │<──────────────────────────────────────────────┤
     │                                               │
     │ 8. 返回响应                                   │
     │                                               │
```

---

## 签名验证原理

### 核心参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `timestamp` | Long | ✅ | 请求时间戳(毫秒),用于防止重放攻击 |
| `nonce` | String | ✅ | 随机字符串(UUID),确保每次请求唯一 |
| `signature` | String | ✅ | 请求签名,用于验证请求完整性 |

### 签名密钥

**⚠️ 重要**: 签名密钥是前后端共享的密钥,**绝对不能泄露**!

| 环境 | 密钥 | 说明 |
|------|------|------|
| **开发环境** | `cni-alumni-signature-secret-2024` | 本地开发使用 |
| **生产环境** | 通过环境变量配置 | 运维人员提供,不在代码中硬编码 |

### 时间窗口

后端只接受 **±5 分钟** 内的请求,超出时间窗口的请求会被拒绝。

```
当前时间: 2025-11-30 10:00:00
有效请求时间范围: 2025-11-30 09:55:00 ~ 10:05:00
```

### Nonce 防重放

每个 `nonce` 只能使用一次,后端会将已使用的 `nonce` 记录到 Redis 中(有效期 10 分钟)。

---

## 签名生成步骤

### 第 1 步: 准备参数

收集所有请求参数(包括 URL 参数、Header 参数、Body 参数)。

**示例请求**:

```http
POST /api/school/page
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "name": "北京大学"
}
```

**准备的参数 Map**:

```javascript
{
  "current": "1",
  "size": "10",
  "name": "北京大学",
  "timestamp": "1701331200000",  // 当前时间戳(毫秒)
  "nonce": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"  // UUID
}
```

### 第 2 步: 参数排序

将所有参数(除了 `signature`)按照 **key 的字典序** 升序排列。

**排序后**:

```
current → 1
name → 北京大学
nonce → a1b2c3d4-e5f6-7890-abcd-ef1234567890
size → 10
timestamp → 1701331200000
```

### 第 3 步: 拼接参数

将排序后的参数拼接成 `key1=value1&key2=value2` 格式。

**拼接结果**:

```
current=1&name=北京大学&nonce=a1b2c3d4-e5f6-7890-abcd-ef1234567890&size=10&timestamp=1701331200000
```

### 第 4 步: 追加密钥

在拼接后的字符串末尾追加 `&key=SECRET`。

**追加密钥后**:

```
current=1&name=北京大学&nonce=a1b2c3d4-e5f6-7890-abcd-ef1234567890&size=10&timestamp=1701331200000&key=cni-alumni-signature-secret-2024
```

### 第 5 步: 计算 HMAC-SHA256

使用 **HMAC-SHA256** 算法计算签名,并转换为 **十六进制小写** 字符串。

**计算签名**:

```javascript
signature = hmacSha256(signContent, secret)
// 结果示例: "a3f5d8c2b1e9f4a7c6d5e8b2a1f9c4d7e8b5a2f1c9d4e7b8a5f2c1d9e4b7a8f5"
```

### 第 6 步: 发送请求

将 `timestamp`、`nonce`、`signature` 添加到请求中。

**推荐方式**: 放在 **HTTP Header** 中(不污染业务参数)

```http
POST /api/school/page
Content-Type: application/json
X-Timestamp: 1701331200000
X-Nonce: a1b2c3d4-e5f6-7890-abcd-ef1234567890
X-Signature: a3f5d8c2b1e9f4a7c6d5e8b2a1f9c4d7e8b5a2f1c9d4e7b8a5f2c1d9e4b7a8f5

{
  "current": 1,
  "size": 10,
  "name": "北京大学"
}
```

**备选方式 1**: 放在 **URL 参数** 中

```http
POST /api/school/page?timestamp=1701331200000&nonce=xxx&signature=xxx
```

**备选方式 2**: 放在 **JSON Body** 中

```json
{
  "current": 1,
  "size": 10,
  "name": "北京大学",
  "timestamp": "1701331200000",
  "nonce": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "signature": "a3f5d8c2b1e9f4a7c6d5e8b2a1f9c4d7e8b5a2f1c9d4e7b8a5f2c1d9e4b7a8f5"
}
```

---

## 前端实现指南

### JavaScript 实现

#### 1. 安装依赖

```bash
npm install crypto-js uuid
```

#### 2. 创建签名工具类

```javascript
// src/utils/signature.js

import CryptoJS from 'crypto-js';
import { v4 as uuidv4 } from 'uuid';

/**
 * API 签名工具类
 */
class SignatureUtil {

  /**
   * 签名密钥(⚠️ 生产环境请使用环境变量)
   */
  static SECRET = process.env.VUE_APP_API_SECRET || 'cni-alumni-signature-secret-2024';

  /**
   * 生成请求签名
   * @param {Object} params - 请求参数(包括 URL 参数、Body 参数等)
   * @returns {Object} - 包含 timestamp, nonce, signature 的对象
   */
  static generateSignature(params = {}) {
    // 1. 生成 timestamp 和 nonce
    const timestamp = Date.now().toString();
    const nonce = uuidv4();

    // 2. 合并参数
    const allParams = {
      ...params,
      timestamp,
      nonce
    };

    // 3. 移除 signature 参数(如果存在)
    delete allParams.signature;

    // 4. 参数排序
    const sortedKeys = Object.keys(allParams).sort();

    // 5. 拼接参数
    let signContent = '';
    sortedKeys.forEach(key => {
      const value = allParams[key];

      // 跳过空值
      if (value === null || value === undefined || value === '') {
        return;
      }

      signContent += `${key}=${value}&`;
    });

    // 6. 追加密钥
    signContent += `key=${this.SECRET}`;

    // 7. 计算 HMAC-SHA256
    const signature = CryptoJS.HmacSHA256(signContent, this.SECRET).toString();

    console.log('[签名调试] 待签名字符串:', signContent);
    console.log('[签名调试] 计算结果:', signature);

    return {
      timestamp,
      nonce,
      signature
    };
  }

  /**
   * 为请求添加签名(推荐使用 Header 方式)
   * @param {Object} config - Axios 请求配置
   * @returns {Object} - 添加签名后的请求配置
   */
  static signRequest(config) {
    // 收集所有参数
    let params = {};

    // 1. URL 参数
    if (config.params) {
      params = { ...params, ...config.params };
    }

    // 2. Body 参数(仅 POST/PUT)
    if (config.data && typeof config.data === 'object') {
      params = { ...params, ...config.data };
    }

    // 生成签名
    const { timestamp, nonce, signature } = this.generateSignature(params);

    // 添加到 Header(推荐)
    config.headers = config.headers || {};
    config.headers['X-Timestamp'] = timestamp;
    config.headers['X-Nonce'] = nonce;
    config.headers['X-Signature'] = signature;

    return config;
  }
}

export default SignatureUtil;
```

#### 3. 集成到 Axios 拦截器

```javascript
// src/utils/request.js

import axios from 'axios';
import SignatureUtil from './signature';

// 创建 Axios 实例
const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000
});

// 请求拦截器 - 自动添加签名
request.interceptors.request.use(
  config => {
    // 自动为所有请求添加签名
    SignatureUtil.signRequest(config);

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    if (error.response) {
      const { status, data } = error.response;

      // 签名验证失败
      if (status === 401 && data.code === 401001) {
        console.error('签名验证失败:', data.message);
        // 可以在这里触发重新登录或提示用户
      }
    }

    return Promise.reject(error);
  }
);

export default request;
```

#### 4. 使用示例

```javascript
// src/api/school.js

import request from '@/utils/request';

/**
 * 查询学校分页列表
 */
export function getSchoolPage(params) {
  return request({
    url: '/api/school/page',
    method: 'POST',
    data: params  // 自动添加签名,无需手动处理
  });
}

// 使用
getSchoolPage({ current: 1, size: 10, name: '北京大学' })
  .then(res => {
    console.log('查询结果:', res);
  })
  .catch(err => {
    console.error('查询失败:', err);
  });
```

### TypeScript 实现

```typescript
// src/utils/signature.ts

import CryptoJS from 'crypto-js';
import { v4 as uuidv4 } from 'uuid';

interface SignatureResult {
  timestamp: string;
  nonce: string;
  signature: string;
}

/**
 * API 签名工具类
 */
class SignatureUtil {

  private static readonly SECRET: string =
    process.env.VUE_APP_API_SECRET || 'cni-alumni-signature-secret-2024';

  /**
   * 生成请求签名
   */
  static generateSignature(params: Record<string, any> = {}): SignatureResult {
    const timestamp = Date.now().toString();
    const nonce = uuidv4();

    const allParams: Record<string, any> = {
      ...params,
      timestamp,
      nonce
    };

    delete allParams.signature;

    const sortedKeys = Object.keys(allParams).sort();

    let signContent = '';
    sortedKeys.forEach(key => {
      const value = allParams[key];

      if (value === null || value === undefined || value === '') {
        return;
      }

      signContent += `${key}=${value}&`;
    });

    signContent += `key=${this.SECRET}`;

    const signature = CryptoJS.HmacSHA256(signContent, this.SECRET).toString();

    return { timestamp, nonce, signature };
  }

  /**
   * 为 Axios 请求添加签名
   */
  static signRequest(config: any): any {
    let params: Record<string, any> = {};

    if (config.params) {
      params = { ...params, ...config.params };
    }

    if (config.data && typeof config.data === 'object') {
      params = { ...params, ...config.data };
    }

    const { timestamp, nonce, signature } = this.generateSignature(params);

    config.headers = config.headers || {};
    config.headers['X-Timestamp'] = timestamp;
    config.headers['X-Nonce'] = nonce;
    config.headers['X-Signature'] = signature;

    return config;
  }
}

export default SignatureUtil;
```

### 微信小程序实现

```javascript
// utils/signature.js

const CryptoJS = require('crypto-js');

/**
 * 生成 UUID
 */
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

/**
 * API 签名工具
 */
const SignatureUtil = {

  SECRET: 'cni-alumni-signature-secret-2024',

  /**
   * 生成签名
   */
  generateSignature(params = {}) {
    const timestamp = Date.now().toString();
    const nonce = generateUUID();

    const allParams = {
      ...params,
      timestamp,
      nonce
    };

    delete allParams.signature;

    const sortedKeys = Object.keys(allParams).sort();

    let signContent = '';
    sortedKeys.forEach(key => {
      const value = allParams[key];

      if (value === null || value === undefined || value === '') {
        return;
      }

      signContent += `${key}=${value}&`;
    });

    signContent += `key=${this.SECRET}`;

    const signature = CryptoJS.HmacSHA256(signContent, this.SECRET).toString();

    return { timestamp, nonce, signature };
  },

  /**
   * 为微信请求添加签名
   */
  signRequest(options) {
    let params = {};

    if (options.data) {
      params = { ...params, ...options.data };
    }

    const { timestamp, nonce, signature } = this.generateSignature(params);

    options.header = options.header || {};
    options.header['X-Timestamp'] = timestamp;
    options.header['X-Nonce'] = nonce;
    options.header['X-Signature'] = signature;

    return options;
  }
};

module.exports = SignatureUtil;
```

**使用示例**:

```javascript
const SignatureUtil = require('@/utils/signature');

wx.request({
  url: 'https://api.example.com/school/page',
  method: 'POST',
  data: {
    current: 1,
    size: 10
  },
  success: res => {
    console.log('成功:', res);
  },
  fail: err => {
    console.error('失败:', err);
  },
  // 添加签名
  ...SignatureUtil.signRequest({
    data: { current: 1, size: 10 }
  })
});
```

---

## 代码示例

### 示例 1: GET 请求(URL 参数)

```javascript
// 请求
axios.get('/api/user/info', {
  params: { userId: 123 }
});

// 签名生成过程
const params = { userId: '123' };
const { timestamp, nonce, signature } = SignatureUtil.generateSignature(params);

// 实际发送的请求
GET /api/user/info
Headers:
  X-Timestamp: 1701331200000
  X-Nonce: a1b2c3d4-e5f6-7890-abcd-ef1234567890
  X-Signature: abc123...
```

### 示例 2: POST 请求(JSON Body)

```javascript
// 请求
axios.post('/api/school/page', {
  current: 1,
  size: 10,
  name: '北京大学'
});

// 签名生成过程
const params = {
  current: '1',
  size: '10',
  name: '北京大学'
};

// 待签名字符串
current=1&name=北京大学&nonce=xxx&size=10&timestamp=1701331200000&key=cni-alumni-signature-secret-2024

// 实际发送的请求
POST /api/school/page
Headers:
  Content-Type: application/json
  X-Timestamp: 1701331200000
  X-Nonce: a1b2c3d4-e5f6-7890-abcd-ef1234567890
  X-Signature: def456...

Body:
{
  "current": 1,
  "size": 10,
  "name": "北京大学"
}
```

### 示例 3: 混合参数(URL + Body)

```javascript
// 请求
axios.post('/api/school/update?id=1', {
  name: '清华大学',
  address: '北京市海淀区'
});

// 签名生成需要合并所有参数
const params = {
  id: '1',           // 来自 URL
  name: '清华大学',   // 来自 Body
  address: '北京市海淀区'  // 来自 Body
};

// 待签名字符串
address=北京市海淀区&id=1&name=清华大学&nonce=xxx&timestamp=xxx&key=SECRET
```

---

## 常见问题

### Q1: 签名验证失败,错误码 401001

**可能原因**:

1. **密钥不一致**: 前端使用的密钥与后端不同
   ```javascript
   // ❌ 错误
   SECRET = 'wrong-secret';

   // ✅ 正确
   SECRET = 'cni-alumni-signature-secret-2024';
   ```

2. **参数遗漏**: 签名计算时遗漏了某些参数
   ```javascript
   // ❌ 错误 - 只计算了 Body 参数
   const params = config.data;

   // ✅ 正确 - 需要合并所有参数
   const params = { ...config.params, ...config.data };
   ```

3. **参数类型错误**: 所有参数必须转为字符串
   ```javascript
   // ❌ 错误
   { current: 1, size: 10 }

   // ✅ 正确
   { current: '1', size: '10' }
   ```

4. **排序错误**: 未按 key 字典序排序
   ```javascript
   // ❌ 错误 - 原始顺序
   size=10&current=1&nonce=xxx

   // ✅ 正确 - 字典序
   current=1&nonce=xxx&size=10
   ```

5. **签名算法错误**: 使用了错误的加密算法
   ```javascript
   // ❌ 错误
   CryptoJS.SHA256(signContent)

   // ✅ 正确
   CryptoJS.HmacSHA256(signContent, SECRET)
   ```

### Q2: 时间戳超出允许范围

**原因**: 客户端时间与服务器时间相差超过 5 分钟

**解决方案**:

1. **同步客户端时间**: 确保客户端系统时间正确
2. **使用服务器时间**: 首次请求时从服务器获取时间,计算时间差
   ```javascript
   let timeDiff = 0;  // 客户端与服务器的时间差

   // 获取服务器时间
   axios.get('/api/server-time').then(res => {
     const serverTime = res.data.timestamp;
     const clientTime = Date.now();
     timeDiff = serverTime - clientTime;
   });

   // 生成签名时使用矫正后的时间
   const timestamp = (Date.now() + timeDiff).toString();
   ```

### Q3: 检测到重放攻击,nonce 已使用

**原因**: 同一个 `nonce` 被使用了多次

**解决方案**:

1. **确保每次请求生成新的 UUID**
   ```javascript
   // ❌ 错误 - 使用固定值
   const nonce = 'fixed-nonce';

   // ✅ 正确 - 每次生成新 UUID
   const nonce = uuidv4();
   ```

2. **不要重试相同的请求**: 如果请求失败,重新生成签名

### Q4: 中文参数乱码

**解决方案**: 确保使用 UTF-8 编码

```javascript
// ❌ 可能出现问题
const signContent = '...&name=北京大学&...';

// ✅ 推荐 - 显式指定编码
CryptoJS.HmacSHA256(signContent, SECRET).toString(CryptoJS.enc.Hex);
```

### Q5: 白名单接口是否需要签名?

**答案**: 不需要

以下路径在白名单中,**无需签名**:

```
/api/auth/wx_init       # 微信初始化
/api/health             # 健康检查
/actuator/**            # 监控端点
/swagger-ui/**          # Swagger UI
/doc.html               # API 文档
/v3/api-docs/**         # OpenAPI 规范
/webjars/**             # 静态资源
/druid/**               # Druid 监控
```

---

## 测试与调试

### 开发环境快速调试

在开发环境,可以使用特殊的 `nonce` 值跳过签名验证:

```javascript
// 开发模式 - 跳过签名验证
const params = {
  current: 1,
  size: 10,
  nonce: 'mock'  // ✅ 使用特殊 nonce,后端会跳过验证
};

// 无需计算 signature
axios.post('/api/school/page', params, {
  headers: {
    'X-Nonce': 'mock'  // 或者在 Header 中指定
  }
});
```

**支持的特殊 nonce 值**:
- `mock`
- `dev`
- `test`
- `local-dev`

**⚠️ 注意**: 这些特殊值仅在开发环境有效(`dev-mode: true`),生产环境会强制验证签名!

### 签名调试工具

```javascript
/**
 * 签名调试工具
 */
function debugSignature(params) {
  const timestamp = Date.now().toString();
  const nonce = uuidv4();

  const allParams = { ...params, timestamp, nonce };
  delete allParams.signature;

  const sortedKeys = Object.keys(allParams).sort();

  console.log('===== 签名调试 =====');
  console.log('1. 原始参数:', params);
  console.log('2. timestamp:', timestamp);
  console.log('3. nonce:', nonce);
  console.log('4. 排序后的 keys:', sortedKeys);

  let signContent = '';
  sortedKeys.forEach(key => {
    const value = allParams[key];
    if (value !== null && value !== undefined && value !== '') {
      console.log(`   ${key} = ${value}`);
      signContent += `${key}=${value}&`;
    }
  });

  signContent += `key=${SignatureUtil.SECRET}`;

  console.log('5. 待签名字符串:', signContent);

  const signature = CryptoJS.HmacSHA256(signContent, SignatureUtil.SECRET).toString();

  console.log('6. 签名结果:', signature);
  console.log('====================');

  return { timestamp, nonce, signature };
}

// 使用
debugSignature({ current: 1, size: 10, name: '北京大学' });
```

### Postman 测试

**Pre-request Script**:

```javascript
const CryptoJS = require('crypto-js');

// 配置
const SECRET = 'cni-alumni-signature-secret-2024';

// 生成 timestamp 和 nonce
const timestamp = Date.now().toString();
const nonce = pm.variables.replaceIn('{{$guid}}');

// 获取请求参数
let params = {};

// URL 参数
if (pm.request.url.query) {
  pm.request.url.query.each(param => {
    params[param.key] = param.value;
  });
}

// Body 参数
if (pm.request.body && pm.request.body.mode === 'raw') {
  const bodyParams = JSON.parse(pm.request.body.raw);
  params = { ...params, ...bodyParams };
}

// 添加 timestamp 和 nonce
params.timestamp = timestamp;
params.nonce = nonce;

// 排序
const sortedKeys = Object.keys(params).sort();

// 拼接
let signContent = '';
sortedKeys.forEach(key => {
  const value = params[key];
  if (value !== null && value !== undefined && value !== '') {
    signContent += `${key}=${value}&`;
  }
});

signContent += `key=${SECRET}`;

// 计算签名
const signature = CryptoJS.HmacSHA256(signContent, SECRET).toString();

// 设置 Header
pm.request.headers.add({
  key: 'X-Timestamp',
  value: timestamp
});

pm.request.headers.add({
  key: 'X-Nonce',
  value: nonce
});

pm.request.headers.add({
  key: 'X-Signature',
  value: signature
});

console.log('待签名字符串:', signContent);
console.log('签名结果:', signature);
```

### cURL 测试

```bash
#!/bin/bash

# 配置
SECRET="cni-alumni-signature-secret-2024"
URL="http://localhost:8080/api/school/page"

# 生成 timestamp 和 nonce
TIMESTAMP=$(date +%s%3N)
NONCE=$(uuidgen)

# 请求参数
PARAMS="current=1&size=10"

# 拼接待签名字符串
SIGN_CONTENT="${PARAMS}&nonce=${NONCE}&timestamp=${TIMESTAMP}&key=${SECRET}"

# 计算签名
SIGNATURE=$(echo -n "$SIGN_CONTENT" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')

# 发送请求
curl -X POST "$URL" \
  -H "Content-Type: application/json" \
  -H "X-Timestamp: $TIMESTAMP" \
  -H "X-Nonce: $NONCE" \
  -H "X-Signature: $SIGNATURE" \
  -d '{"current": 1, "size": 10}'

echo ""
echo "待签名字符串: $SIGN_CONTENT"
echo "签名结果: $SIGNATURE"
```

---

## 附录

### 错误码对照表

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| `401001` | 签名验证失败 | 检查密钥、参数、算法是否正确 |
| `401002` | 时间戳超出范围 | 同步客户端时间 |
| `401003` | 检测到重放攻击 | 确保每次请求使用新的 nonce |
| `400001` | 缺少必需参数 | 检查是否传递了 timestamp, nonce, signature |

### 参考资料

- **HMAC-SHA256 算法**: [RFC 2104](https://tools.ietf.org/html/rfc2104)
- **UUID 规范**: [RFC 4122](https://tools.ietf.org/html/rfc4122)
- **CryptoJS 文档**: [https://cryptojs.gitbook.io/](https://cryptojs.gitbook.io/)

### 技术支持

如有问题,请联系后端开发团队或查看:
- 后端签名验证实现: `alumni-auth/src/main/java/com/cmswe/alumni/auth/signature/SignatureValidator.java`
- 开发模式说明文档: `开发模式签名验证说明.md`

---

**文档版本**: v1.0
**最后更新**: 2025-11-30
**维护者**: CNI Alumni 开发团队
