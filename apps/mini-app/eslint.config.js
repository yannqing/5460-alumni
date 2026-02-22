// apps/mini-app/eslint.config.js
import js from "@eslint/js";
import globals from "globals";

export default [
  js.configs.recommended,
  {
    // 这里替代了之前的 .eslintignore
    ignores: [
      "**/node_modules/**",
      "**/miniprogram_npm/**",
      "**/dist/**",
      "**/build/**",
      "**/*.min.js",
      "**/*.config.js",  // 忽略配置文件
      "project.config.json",
      // 忽略第三方加密库（crypto-js 有很多历史遗留问题）
      "**/utils/Crypto/**",
      // 忽略 app.js（混用了 import 和 require，微信小程序特殊情况）
      "app.js",
    ],
  },
  {
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "commonjs", // 使用 commonjs，支持 require 和 module.exports
      globals: {
        // 核心：注入小程序全局变量
        wx: "readonly",
        App: "readonly",
        Page: "readonly",
        Component: "readonly",
        getApp: "readonly",
        getCurrentPages: "readonly",
        Behavior: "readonly",
        requirePlugin: "readonly",
        cloud: "readonly",
        // 添加 Node.js 全局变量（包含 require, module, exports 等）
        ...globals.node,
        // 添加浏览器基础变量
        ...globals.browser,
        // AMD 模块系统全局变量（用于部分第三方库）
        define: "readonly",
      },
    },
    rules: {
      // 代码质量
      "no-unused-vars": "warn",
      "no-console": "off", // 小程序需要 console.log 调试
      "no-undef": "warn", // 未定义变量降为警告（可能是动态变量）

      // 最佳实践
      "eqeqeq": ["warn", "always"],
      "curly": "off", // 关闭强制使用大括号
      "no-var": "warn",
      "prefer-const": "warn",

      // 允许的模式（降低严格程度）
      "no-prototype-builtins": "warn",
      "no-useless-assignment": "warn",
      "no-redeclare": "warn",
      "no-dupe-keys": "warn", // 重复的对象键降为警告（已手动修复主要问题）
      "no-empty": "warn",
      "no-useless-escape": "warn", // 不必要的转义字符
      "no-shadow-restricted-names": "warn", // 覆盖保留字
      "preserve-caught-error": "off", // 关闭捕获错误检查（太严格）
    },
  },
];
