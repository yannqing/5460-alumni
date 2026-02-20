package com.cmswe.alumni.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnFollowRequest implements Serializable {

    @Schema(description = "目标类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer targetType;

    @Schema(description = "目标 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @Serial
    private static final long serialVersionUID = 1L;
}
