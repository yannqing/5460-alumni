/**
 * 开发中入口总开关（临时）
 *
 * constructionNavBlocked === true：禁止跳转，显示「开发中」角标（与当前线上一致）
 * constructionNavBlocked === false：允许跳转，隐藏角标
 *
 * 全量上线后：删除本文件，并全局搜索 constructionNavBlocked、feature-flags 清理引用。
 */
module.exports = {
  constructionNavBlocked: false,
}
