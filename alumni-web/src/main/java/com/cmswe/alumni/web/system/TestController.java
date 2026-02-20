package com.cmswe.alumni.web.system;

import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.common.dto.AddAlumniAssociationDto;
import com.cmswe.alumni.common.dto.AddLocalPlatformDto;
import com.cmswe.alumni.common.dto.CreateRoleDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.api.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "测试工具接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Resource
    private LocalPlatformService localPlatformService;

    /**
     * 测试接口：根据用户ID生成7天有效期的token
     * 仅用于Apifox后端测试
     * 
     * @param userId 用户ID
     * @return token信息
     */
    @Operation(summary = "模拟生成token")
    @PostMapping("/generate-token/{userId}")
    public BaseResponse<Map<String, Object>> generateTestToken(@PathVariable Long userId) {
        try {
            Map<String, Object> result = userService.generateTestToken(userId);
            return ResultUtils.success(Code.SUCCESS, result, "Token生成成功");
        } catch (Exception e) {
            return ResultUtils.failure("Token生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建角色")
    @PostMapping("/create-role")
    public BaseResponse<Boolean> createRole(@RequestBody CreateRoleDto createRoleDto) {
        boolean result = roleService.createRole(createRoleDto);
        return ResultUtils.success(Code.SUCCESS, result, "创建角色成功");
    }

    @Operation(summary = "创建校友会")
    @PostMapping("/create-alumni-association")
    public BaseResponse<Boolean> createAlumniAssociation(@RequestBody AddAlumniAssociationDto addAlumniAssociationDto) {
        boolean result = alumniAssociationService.insertAlumniAssociation(addAlumniAssociationDto);
        return ResultUtils.success(Code.SUCCESS, result, "创建校友会成功");
    }

    @Operation(summary = "创建校处会")
    @PostMapping("/create-local-platform")
    public BaseResponse<Boolean> createLocalPlatform(@RequestBody AddLocalPlatformDto addLocalPlatformDto) {
        boolean result = localPlatformService.insertLocalPlatform(addLocalPlatformDto);
        return ResultUtils.success(Code.SUCCESS, result, "创建校处会成功");
    }
}