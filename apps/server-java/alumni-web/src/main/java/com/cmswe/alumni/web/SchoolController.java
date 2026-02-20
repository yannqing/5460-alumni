package com.cmswe.alumni.web;

import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.api.association.SchoolService;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QuerySchoolListDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.SchoolListVo;
import com.cmswe.alumni.common.vo.SchoolDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "母校")
@RestController
@RequestMapping("/school")
public class SchoolController {

    @Resource
    private SchoolService schoolService;

    @PostMapping("/page")
    @Operation(summary = "分页查询母校列表")
    public BaseResponse<PageVo<SchoolListVo>> selectPage(@RequestBody QuerySchoolListDto schoolListDto) {
        PageVo<SchoolListVo> schoolVoPage = schoolService.selectByPage(schoolListDto);
        return ResultUtils.success(Code.SUCCESS, schoolVoPage, "分页查询成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询母校信息")
    public BaseResponse<SchoolDetailVo> getSchoolById(@PathVariable Long id) {
        SchoolDetailVo schoolDetailVo = schoolService.getSchoolDetailVoById(id);
        return ResultUtils.success(Code.SUCCESS, schoolDetailVo);
    }
}