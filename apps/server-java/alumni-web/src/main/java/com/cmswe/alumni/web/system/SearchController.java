package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.search.AlumniSearchService;
import com.cmswe.alumni.api.search.AssociationSearchService;
import com.cmswe.alumni.api.search.MerchantSearchService;
import com.cmswe.alumni.api.search.SchoolSearchService;
import com.cmswe.alumni.api.search.UnifiedSearchService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.search.SearchFilter;
import com.cmswe.alumni.common.dto.search.UnifiedSearchRequest;
import com.cmswe.alumni.common.enums.SearchType;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.search.SearchResultItem;
import com.cmswe.alumni.common.vo.search.SuggestResponse;
import com.cmswe.alumni.common.vo.search.UnifiedSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 * 提供校友、校友会、商户等多维度搜索功能
 *
 * @author CNI Alumni System
 */
@Slf4j
@Tag(name = "搜索服务")
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    @Qualifier("unifiedSearchService")
    private UnifiedSearchService unifiedSearchService;

    @Autowired
    @Qualifier("alumniSearchService")
    private AlumniSearchService alumniSearchService;

    @Autowired
    @Qualifier("associationSearchService")
    private AssociationSearchService associationSearchService;

    @Autowired
    @Qualifier("merchantSearchService")
    private MerchantSearchService merchantSearchService;

    @Autowired
    @Qualifier("schoolSearchService")
    private SchoolSearchService schoolSearchService;

    // ==================== 统一搜索接口 ====================

    /**
     * 统一搜索入口
     * 支持校友、校友会、商户的统一搜索，可多选类型并聚合结果
     */
    @PostMapping("/unified")
    @Operation(summary = "统一搜索", description = "支持多类型聚合搜索，返回统一的搜索结果")
    public BaseResponse<UnifiedSearchResponse> unifiedSearch(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UnifiedSearchRequest request) {

        log.info("统一搜索请求: keyword={}, types={}, user={}",
                request.getKeyword(), request.getTypes(),
                securityUser != null ? securityUser.getWxUser().getWxId() : "anonymous");

        UnifiedSearchResponse response = unifiedSearchService.search(request);

        // 如果用户已登录，保存搜索历史
        if (securityUser != null && request.getTypes() != null && !request.getTypes().isEmpty()) {
            try {
                unifiedSearchService.saveSearchHistory(
                        securityUser.getWxUser().getWxId(),
                        request.getKeyword(),
                        request.getTypes().get(0));
            } catch (Exception e) {
                log.warn("保存搜索历史失败: {}", e.getMessage());
            }
        }

        return ResultUtils.success(Code.SUCCESS, response, "搜索成功");
    }

    // ==================== 校友搜索接口 ====================

    /**
     * 校友搜索（快捷入口）
     * 支持按关键词、学校、地区等条件搜索校友
     */
    @PostMapping("/alumni")
    @Operation(summary = "校友搜索", description = "快捷搜索校友信息，支持多维度过滤")
    public BaseResponse<Page<SearchResultItem>> searchAlumni(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,

            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,

            @Parameter(description = "省份") @RequestParam(required = false) String province,

            @Parameter(description = "城市") @RequestParam(required = false) String city,

            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("校友搜索: keyword={}, schoolId={}, province={}, city={}",
                keyword, schoolId, province, city);

        // 构建过滤条件
        SearchFilter filter = new SearchFilter();
        filter.setSchoolId(schoolId);
        filter.setProvince(province);
        filter.setCity(city);

        Page<SearchResultItem> result = alumniSearchService.searchAlumni(
                keyword, filter, pageNum, pageSize, true);

        return ResultUtils.success(Code.SUCCESS, result, "查询成功");
    }

    /**
     * 附近的校友（地理位置搜索）
     * 基于用户当前位置搜索附近的校友
     */
    @GetMapping("/alumni/nearby")
    @Operation(summary = "附近的校友", description = "基于地理位置搜索附近的校友")
    public BaseResponse<Page<SearchResultItem>> searchNearbyAlumni(
            @Parameter(description = "纬度", required = true, example = "39.9042") @RequestParam Double latitude,

            @Parameter(description = "经度", required = true, example = "116.4074") @RequestParam Double longitude,

            @Parameter(description = "半径（公里）", example = "50") @RequestParam(defaultValue = "50") Integer radius,

            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("附近的校友: lat={}, lon={}, radius={}km", latitude, longitude, radius);

        Page<SearchResultItem> result = alumniSearchService.searchNearbyAlumni(
                latitude, longitude, radius, pageNum, pageSize);

        return ResultUtils.success(Code.SUCCESS, result, "查询成功");
    }

    // ==================== 搜索建议与历史 ====================

    /**
     * 搜索建议（自动补全）
     * 根据输入前缀提供搜索建议，用于自动补全功能
     */
    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "根据输入前缀提供搜索建议")
    public BaseResponse<SuggestResponse> suggest(
            @Parameter(description = "输入前缀", required = true, example = "张") @RequestParam String prefix,

            @Parameter(description = "搜索类型", example = "ALUMNI") @RequestParam(defaultValue = "ALUMNI") SearchType type,

            @Parameter(description = "返回数量", example = "10") @RequestParam(defaultValue = "10") Integer size) {

        log.debug("搜索建议: prefix={}, type={}", prefix, type);

        SuggestResponse response = unifiedSearchService.suggest(prefix, type, size);

        return ResultUtils.success(Code.SUCCESS, response, "查询成功");
    }

    /**
     * 获取搜索历史
     * 获取当前用户的搜索历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "获取当前用户的搜索历史记录")
    public BaseResponse<Page<String>> getSearchHistory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Parameter(description = "返回数量", example = "20") @RequestParam(defaultValue = "20") Integer limit) {

        if (securityUser == null) {
            return ResultUtils.failure(Code.FAILURE, null, "请先登录");
        }

        log.debug("获取搜索历史: wxId={}", securityUser.getWxUser().getWxId());

        Page<String> history = unifiedSearchService.getSearchHistory(
                securityUser.getWxUser().getWxId(), limit);

        return ResultUtils.success(Code.SUCCESS, history, "查询成功");
    }

    /**
     * 删除搜索历史
     * 删除指定关键词或全部搜索历史
     */
    @DeleteMapping("/history")
    @Operation(summary = "删除搜索历史", description = "删除指定关键词或全部搜索历史")
    public BaseResponse<?> deleteSearchHistory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Parameter(description = "关键词（不传则删除全部）") @RequestParam(required = false) String keyword) {

        if (securityUser == null) {
            return ResultUtils.failure(Code.FAILURE, null, "请先登录");
        }

        log.info("删除搜索历史: wxId={}, keyword={}",
                securityUser.getWxUser().getWxId(), keyword);

        unifiedSearchService.deleteSearchHistory(
                securityUser.getWxUser().getWxId(), keyword);

        String message = keyword != null ? "删除成功" : "清空历史成功";
        return ResultUtils.success(Code.SUCCESS, null, message);
    }

    // ==================== 热搜榜 ====================

    /**
     * 热搜榜
     * 获取热门搜索关键词排行榜
     */
    @GetMapping("/hot")
    @Operation(summary = "热搜榜", description = "获取热门搜索关键词排行榜")
    public BaseResponse<Page<String>> getHotSearch(
            @Parameter(description = "搜索类型（不传则返回全部）") @RequestParam(required = false) SearchType type,

            @Parameter(description = "返回数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {

        log.debug("获取热搜榜: type={}, limit={}", type, limit);

        Page<String> hotKeywords = unifiedSearchService.getHotSearch(type, limit);

        return ResultUtils.success(Code.SUCCESS, hotKeywords, "查询成功");
    }

    // ==================== 管理员接口 ====================

    /**
     * 手动触发索引更新（管理员接口）
     * 管理员手动触发单个校友的索引更新
     */
    @PostMapping("/admin/index/alumni/{alumniId}")
    @Operation(summary = "索引单个校友", description = "管理员手动触发校友索引更新")
    public BaseResponse<?> indexAlumni(
            @Parameter(description = "校友ID", required = true) @PathVariable Long alumniId) {

        log.info("手动索引校友: alumniId={}", alumniId);

        try {
            alumniSearchService.indexAlumni(alumniId);
            return ResultUtils.success(Code.SUCCESS, null, "索引更新成功");
        } catch (Exception e) {
            log.error("索引校友失败: alumniId={}", alumniId, e);
            return ResultUtils.failure(Code.FAILURE, null, "索引更新失败: " + e.getMessage());
        }
    }

    /**
     * 全量重建索引（管理员接口）
     * 管理员触发全量索引重建，慎用！会清空现有索引
     *
     * 注意：此操作为异步执行，避免接口超时
     */
    @PostMapping("/admin/index/rebuild")
    @Operation(summary = "全量重建索引", description = "管理员触发全量索引重建（校友+校友会+商户+母校），慎用！")
    public BaseResponse<?> rebuildIndex() {

        log.warn("========================================");
        log.warn("开始全量重建所有搜索索引（校友、校友会、商户、母校）");
        log.warn("========================================");

        // 异步执行，避免接口超时
        new Thread(() -> {
            try {
                // 1. 重建校友索引
                log.info(">>> 第 1/4 步：重建校友索引");
                alumniSearchService.rebuildIndex();
                log.info(">>> 校友索引重建完成");

                // 2. 重建校友会索引
                log.info(">>> 第 2/4 步：重建校友会索引");
                associationSearchService.rebuildIndex();
                log.info(">>> 校友会索引重建完成");

                // 3. 重建商户索引
                log.info(">>> 第 3/4 步：重建商户索引");
                merchantSearchService.rebuildIndex();
                log.info(">>> 商户索引重建完成");

                // 4. 重建母校索引
                log.info(">>> 第 4/4 步：重建母校索引");
                schoolSearchService.rebuildIndex();
                log.info(">>> 母校索引重建完成");

                log.warn("========================================");
                log.warn("所有搜索索引重建完成！");
                log.warn("========================================");

            } catch (Exception e) {
                log.error("========================================");
                log.error("全量重建索引失败", e);
                log.error("========================================");
            }
        }).start();

        return ResultUtils.success(Code.SUCCESS, null, "索引重建任务已提交（校友+校友会+商户+母校），请稍后查看日志");
    }
}
