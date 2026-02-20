package com.cmswe.alumni.web;

import com.cmswe.alumni.api.association.AlumniHeadquartersService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.AlumniHeadquartersDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "校友总会")
@RestController
@RequestMapping("/AlumniHeadquarters")
public class AlumniHeadquartersController {

    @Resource
    private AlumniHeadquartersService alumniHeadquartersService;

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询校友总会详情")
    public BaseResponse<AlumniHeadquartersDetailVo> getAlumniHeadquartersDetailById(@PathVariable Long id) {
        AlumniHeadquartersDetailVo detailVo = alumniHeadquartersService.getAlumniHeadquartersDetailById(id);
        return ResultUtils.success(Code.SUCCESS, detailVo, "查询成功");
    }
}
