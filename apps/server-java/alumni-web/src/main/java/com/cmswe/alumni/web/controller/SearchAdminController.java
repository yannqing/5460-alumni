package com.cmswe.alumni.web.controller;

import com.cmswe.alumni.api.search.AlumniSearchService;
import com.cmswe.alumni.api.search.AssociationSearchService;
import com.cmswe.alumni.api.search.MerchantSearchService;
import com.cmswe.alumni.api.search.SchoolSearchService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.search.repository.AlumniDocumentRepository;
import com.cmswe.alumni.search.repository.AssociationDocumentRepository;
import com.cmswe.alumni.search.repository.MerchantDocumentRepository;
import com.cmswe.alumni.search.repository.SchoolDocumentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch 索引管理接口
 *
 * <p>功能：
 * <ul>
 *   <li>查看索引状态</li>
 *   <li>全量重建索引</li>
 *   <li>用于首次部署或数据修复</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>首次部署时初始化 ES 数据</li>
 *   <li>ES 数据损坏需要修复</li>
 *   <li>索引结构变更后重建</li>
 * </ul>
 *
 * <p>注意事项：
 * <ul>
 *   <li>生产环境建议添加权限控制</li>
 *   <li>重建索引会清空现有数据</li>
 *   <li>重建过程中搜索功能会受影响</li>
 * </ul>
 *
 * @author CNI Alumni System
 */
@Slf4j
@RestController
@RequestMapping("/admin/search")
@Tag(name = "搜索管理", description = "Elasticsearch 索引管理接口")
public class SearchAdminController {

    @Autowired
    private AlumniSearchService alumniSearchService;

    @Autowired
    private AssociationSearchService associationSearchService;

    @Autowired
    private MerchantSearchService merchantSearchService;

    @Autowired
    private SchoolSearchService schoolSearchService;

    @Autowired
    private AlumniDocumentRepository alumniRepository;

    @Autowired
    private AssociationDocumentRepository associationRepository;

    @Autowired
    private MerchantDocumentRepository merchantRepository;

    @Autowired
    private SchoolDocumentRepository schoolRepository;

    /**
     * 获取 Elasticsearch 索引状态
     *
     * @return 索引状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取索引状态", description = "查看当前 ES 索引中的文档数量等信息")
    public BaseResponse<Map<String, Object>> getStatus() {
        try {
            long alumniCount = alumniRepository.count();
            long associationCount = associationRepository.count();
            long merchantCount = merchantRepository.count();
            long schoolCount = schoolRepository.count();
            long totalCount = alumniCount + associationCount + merchantCount + schoolCount;

            Map<String, Object> status = new HashMap<>();

            // 总体统计
            status.put("totalDocuments", totalCount);
            status.put("isEmpty", totalCount == 0);
            status.put("timestamp", System.currentTimeMillis());

            // 各索引详情
            Map<String, Object> indices = new HashMap<>();

            Map<String, Object> alumniIndex = new HashMap<>();
            alumniIndex.put("indexName", "alumni_index_v1");
            alumniIndex.put("documentCount", alumniCount);
            alumniIndex.put("isEmpty", alumniCount == 0);
            indices.put("alumni", alumniIndex);

            Map<String, Object> associationIndex = new HashMap<>();
            associationIndex.put("indexName", "association_index_v1");
            associationIndex.put("documentCount", associationCount);
            associationIndex.put("isEmpty", associationCount == 0);
            indices.put("association", associationIndex);

            Map<String, Object> merchantIndex = new HashMap<>();
            merchantIndex.put("indexName", "merchant_index_v1");
            merchantIndex.put("documentCount", merchantCount);
            merchantIndex.put("isEmpty", merchantCount == 0);
            indices.put("merchant", merchantIndex);

            Map<String, Object> schoolIndex = new HashMap<>();
            schoolIndex.put("indexName", "school_index_v1");
            schoolIndex.put("documentCount", schoolCount);
            schoolIndex.put("isEmpty", schoolCount == 0);
            indices.put("school", schoolIndex);

            status.put("indices", indices);

            log.info("查询索引状态 - 校友: {}, 校友会: {}, 商户: {}, 母校: {}, 总计: {}",
                    alumniCount, associationCount, merchantCount, schoolCount, totalCount);
            return ResultUtils.success(Code.SUCCESS, status);

        } catch (Exception e) {
            log.error("查询索引状态失败", e);
            return ResultUtils.failure("查询索引状态失败: " + e.getMessage());
        }
    }

    /**
     * 全量重建 Elasticsearch 索引
     *
     * <p>操作步骤：
     * <ol>
     *   <li>清空现有索引数据</li>
     *   <li>从数据库查询所有数据（校友、校友会、商户）</li>
     *   <li>批量同步到 ES</li>
     *   <li>清除搜索缓存</li>
     * </ol>
     *
     * <p>注意：
     * <ul>
     *   <li>重建过程可能需要几秒到几分钟（取决于数据量）</li>
     *   <li>重建期间搜索功能可能返回不完整的结果</li>
     *   <li>建议在业务低峰期执行</li>
     * </ul>
     *
     * @return 重建结果
     */
    @PostMapping("/rebuild")
    @Operation(summary = "重建索引", description = "全量重建 ES 索引（校友+校友会+商户+母校，会清空现有数据并从数据库重新同步）")
    public BaseResponse<Map<String, Object>> rebuildIndex() {
        log.warn("========================================");
        log.warn("收到全量重建索引请求（校友、校友会、商户、母校）");
        log.warn("========================================");

        long overallStartTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> details = new HashMap<>();

        try {
            // 1. 重建校友索引
            log.info(">>> 第 1/3 步：重建校友索引");
            long startTime = System.currentTimeMillis();
            alumniSearchService.rebuildIndex();
            long alumniCount = alumniRepository.count();
            long alumniDuration = (System.currentTimeMillis() - startTime) / 1000;

            Map<String, Object> alumniResult = new HashMap<>();
            alumniResult.put("documentCount", alumniCount);
            alumniResult.put("durationSeconds", alumniDuration);
            alumniResult.put("success", true);
            details.put("alumni", alumniResult);
            log.info(">>> 校友索引重建完成 - 文档数: {}, 耗时: {}秒", alumniCount, alumniDuration);

            // 2. 重建校友会索引
            log.info(">>> 第 2/3 步：重建校友会索引");
            startTime = System.currentTimeMillis();
            associationSearchService.rebuildIndex();
            long associationCount = associationRepository.count();
            long associationDuration = (System.currentTimeMillis() - startTime) / 1000;

            Map<String, Object> associationResult = new HashMap<>();
            associationResult.put("documentCount", associationCount);
            associationResult.put("durationSeconds", associationDuration);
            associationResult.put("success", true);
            details.put("association", associationResult);
            log.info(">>> 校友会索引重建完成 - 文档数: {}, 耗时: {}秒", associationCount, associationDuration);

            // 3. 重建商户索引
            log.info(">>> 第 3/4 步：重建商户索引");
            startTime = System.currentTimeMillis();
            merchantSearchService.rebuildIndex();
            long merchantCount = merchantRepository.count();
            long merchantDuration = (System.currentTimeMillis() - startTime) / 1000;

            Map<String, Object> merchantResult = new HashMap<>();
            merchantResult.put("documentCount", merchantCount);
            merchantResult.put("durationSeconds", merchantDuration);
            merchantResult.put("success", true);
            details.put("merchant", merchantResult);
            log.info(">>> 商户索引重建完成 - 文档数: {}, 耗时: {}秒", merchantCount, merchantDuration);

            // 4. 重建母校索引
            log.info(">>> 第 4/4 步：重建母校索引");
            startTime = System.currentTimeMillis();
            schoolSearchService.rebuildIndex();
            long schoolCount = schoolRepository.count();
            long schoolDuration = (System.currentTimeMillis() - startTime) / 1000;

            Map<String, Object> schoolResult = new HashMap<>();
            schoolResult.put("documentCount", schoolCount);
            schoolResult.put("durationSeconds", schoolDuration);
            schoolResult.put("success", true);
            details.put("school", schoolResult);
            log.info(">>> 母校索引重建完成 - 文档数: {}, 耗时: {}秒", schoolCount, schoolDuration);

            // 汇总结果
            long totalDuration = (System.currentTimeMillis() - overallStartTime) / 1000;
            long totalCount = alumniCount + associationCount + merchantCount + schoolCount;

            result.put("success", true);
            result.put("totalDocuments", totalCount);
            result.put("totalDurationSeconds", totalDuration);
            result.put("details", details);
            result.put("timestamp", System.currentTimeMillis());

            log.warn("========================================");
            log.warn("所有索引重建完成！");
            log.warn("校友: {} 条, 校友会: {} 条, 商户: {} 条, 母校: {} 条, 总计: {} 条",
                    alumniCount, associationCount, merchantCount, schoolCount, totalCount);
            log.warn("总耗时: {} 秒", totalDuration);
            log.warn("========================================");

            return ResultUtils.success(Code.SUCCESS, result,
                    String.format("索引重建成功！总文档数：%d（校友:%d, 校友会:%d, 商户:%d, 母校:%d）",
                            totalCount, alumniCount, associationCount, merchantCount, schoolCount));

        } catch (Exception e) {
            log.error("========================================");
            log.error("索引重建失败", e);
            log.error("========================================");

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("details", details);

            return ResultUtils.failure("索引重建失败: " + e.getMessage());
        }
    }

    /**
     * 检查索引健康状态（用于健康检查）
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查 ES 索引是否可用")
    public BaseResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> indices = new HashMap<>();
        boolean allHealthy = true;

        try {
            // 检查校友索引
            try {
                long alumniCount = alumniRepository.count();
                Map<String, Object> alumniHealth = new HashMap<>();
                alumniHealth.put("status", "UP");
                alumniHealth.put("documentCount", alumniCount);
                alumniHealth.put("hasData", alumniCount > 0);
                indices.put("alumni", alumniHealth);
            } catch (Exception e) {
                Map<String, Object> alumniHealth = new HashMap<>();
                alumniHealth.put("status", "DOWN");
                alumniHealth.put("error", e.getMessage());
                indices.put("alumni", alumniHealth);
                allHealthy = false;
            }

            // 检查校友会索引
            try {
                long associationCount = associationRepository.count();
                Map<String, Object> associationHealth = new HashMap<>();
                associationHealth.put("status", "UP");
                associationHealth.put("documentCount", associationCount);
                associationHealth.put("hasData", associationCount > 0);
                indices.put("association", associationHealth);
            } catch (Exception e) {
                Map<String, Object> associationHealth = new HashMap<>();
                associationHealth.put("status", "DOWN");
                associationHealth.put("error", e.getMessage());
                indices.put("association", associationHealth);
                allHealthy = false;
            }

            // 检查商户索引
            try {
                long merchantCount = merchantRepository.count();
                Map<String, Object> merchantHealth = new HashMap<>();
                merchantHealth.put("status", "UP");
                merchantHealth.put("documentCount", merchantCount);
                merchantHealth.put("hasData", merchantCount > 0);
                indices.put("merchant", merchantHealth);
            } catch (Exception e) {
                Map<String, Object> merchantHealth = new HashMap<>();
                merchantHealth.put("status", "DOWN");
                merchantHealth.put("error", e.getMessage());
                indices.put("merchant", merchantHealth);
                allHealthy = false;
            }

            // 检查母校索引
            try {
                long schoolCount = schoolRepository.count();
                Map<String, Object> schoolHealth = new HashMap<>();
                schoolHealth.put("status", "UP");
                schoolHealth.put("documentCount", schoolCount);
                schoolHealth.put("hasData", schoolCount > 0);
                indices.put("school", schoolHealth);
            } catch (Exception e) {
                Map<String, Object> schoolHealth = new HashMap<>();
                schoolHealth.put("status", "DOWN");
                schoolHealth.put("error", e.getMessage());
                indices.put("school", schoolHealth);
                allHealthy = false;
            }

            health.put("status", allHealthy ? "UP" : "PARTIAL");
            health.put("indices", indices);

            if (allHealthy) {
                return ResultUtils.success(health);
            } else {
                return ResultUtils.success(Code.SUCCESS, health, "部分索引不可用");
            }

        } catch (Exception e) {
            log.error("健康检查失败", e);

            health.put("status", "DOWN");
            health.put("error", e.getMessage());

            return ResultUtils.failure(Code.FAILURE, health, "ES 不可用");
        }
    }
}
