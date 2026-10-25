package com.fresh.modules.evaluation.vo;

import com.fresh.modules.evaluation.entity.Evaluation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评价展示对象：评价字段 + 用户昵称/头像
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品评价展示对象")
public class EvaluationVO extends Evaluation {

    /** 用户昵称 */
    @Schema(description = "用户昵称")
    private String userName;

    /** 用户头像 */
    @Schema(description = "用户头像")
    private String avatar;
}
