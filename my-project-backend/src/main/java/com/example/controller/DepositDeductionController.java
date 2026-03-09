package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.DepositDeductionCreateVO;
import com.example.entity.vo.response.DepositDeductionVO;
import com.example.service.DepositDeductionService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposit-deduction")
@Tag(name = "押金扣除管理", description = "押金扣除相关接口")
public class DepositDeductionController {

    @Resource
    private DepositDeductionService deductionService;

    @GetMapping("/my-deductions")
    @Operation(summary = "获取我的押金扣除记录")
    public RestBean<IPage<DepositDeductionVO>> getMyDeductions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        IPage<DepositDeductionVO> result = deductionService.getUserDeductionPage(account.getId(), status, page, size);
        return RestBean.success(result);
    }

    @GetMapping("/all-deductions")
    @Operation(summary = "获取所有押金扣除记录（管理员）")
    public RestBean<IPage<DepositDeductionVO>> getAllDeductions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        IPage<DepositDeductionVO> result = deductionService.getAllDeductionPage(status, page, size);
        return RestBean.success(result);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取扣除记录详情")
    public RestBean<DepositDeductionVO> getDeductionDetail(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        DepositDeductionVO deduction = deductionService.getDeductionById(id);
        if (deduction == null) {
            return RestBean.failure(404, "记录不存在");
        }
        // 检查权限
        if (!deduction.getUserId().equals(account.getId()) && 
            !account.getRole().equals("device_admin") && 
            !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限查看该记录");
        }
        return RestBean.success(deduction);
    }

    @PostMapping("/create")
    @Operation(summary = "创建押金扣除申请（管理员）")
    public RestBean<String> createDeduction(
            @Valid @RequestBody DepositDeductionCreateVO vo,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = deductionService.createDeduction(vo, account.getId());
        return result ? RestBean.success("押金扣除申请已提交") : RestBean.failure(500, "申请失败");
    }

    @PostMapping("/approve/{id}")
    @Operation(summary = "批准押金扣除（管理员）")
    public RestBean<String> approveDeduction(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = deductionService.approveDeduction(id, account.getId());
        return result ? RestBean.success("押金扣除已批准") : RestBean.failure(500, "操作失败");
    }

    @PostMapping("/reject/{id}")
    @Operation(summary = "拒绝押金扣除（管理员）")
    public RestBean<String> rejectDeduction(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = deductionService.rejectDeduction(id, account.getId());
        return result ? RestBean.success("押金扣除已拒绝") : RestBean.failure(500, "操作失败");
    }
}
