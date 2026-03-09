package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EvaluationAddVO;
import com.example.entity.vo.response.EvaluationVO;
import com.example.service.EvaluationService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation")
@Tag(name = "评价管理", description = "评价相关接口")
public class EvaluationController {

    @Resource
    private EvaluationService evaluationService;

    @GetMapping("/list")
    @Operation(summary = "获取评价列表")
    public RestBean<IPage<EvaluationVO>> getEvaluationList(@RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer size,
                                                             @RequestParam(required = false) Integer equipmentId) {
        Page<EvaluationVO> pageObj = new Page<>(page, size);
        IPage<EvaluationVO> result = evaluationService.getEvaluationPage(pageObj, equipmentId);
        return RestBean.success(result);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取评价详情")
    public RestBean<EvaluationVO> getEvaluationDetail(@PathVariable Integer id) {
        EvaluationVO evaluation = evaluationService.getEvaluationById(id);
        if (evaluation == null) {
            return RestBean.failure(404, "评价不存在");
        }
        return RestBean.success(evaluation);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单ID获取评价")
    public RestBean<EvaluationVO> getEvaluationByOrderId(@PathVariable Integer orderId) {
        EvaluationVO evaluation = evaluationService.getEvaluationByOrderId(orderId);
        if (evaluation == null) {
            return RestBean.success(null);
        }
        return RestBean.success(evaluation);
    }

    @PostMapping("/add")
    @Operation(summary = "添加评价")
    public RestBean<String> addEvaluation(@Valid @RequestBody EvaluationAddVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = evaluationService.addEvaluation(vo, account.getId());
        return result ? RestBean.success("评价成功") : RestBean.failure(500, "评价失败");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除评价")
    public RestBean<String> deleteEvaluation(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = evaluationService.deleteEvaluation(id, account.getId());
        return result ? RestBean.success("删除成功") : RestBean.failure(500, "删除失败");
    }
}
