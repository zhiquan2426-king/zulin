package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.DeliveryCreateVO;
import com.example.entity.vo.response.DeliveryVO;
import com.example.service.DeliveryService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
@Tag(name = "配送管理", description = "配送相关接口")
public class DeliveryController {

    @Resource
    private DeliveryService deliveryService;

    @PostMapping("/create")
    @Operation(summary = "创建配送单")
    public RestBean<String> createDelivery(@Valid @RequestBody DeliveryCreateVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        String result = deliveryService.createDelivery(vo, account);
        if (result != null) {
            return RestBean.success(result);
        }
        return RestBean.failure(500, "创建配送单失败");
    }

    @GetMapping("/list")
    @Operation(summary = "获取配送单列表")
    public RestBean<IPage<DeliveryVO>> getDeliveryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        IPage<DeliveryVO> pageResult = deliveryService.getDeliveryList(page, size, status, orderNo, account);
        return RestBean.success(pageResult);
    }

    @GetMapping("/detail/{deliveryNo}")
    @Operation(summary = "获取配送单详情")
    public RestBean<DeliveryVO> getDeliveryDetail(@PathVariable String deliveryNo) {
        DeliveryVO delivery = deliveryService.getDeliveryByNo(deliveryNo);
        if (delivery == null) {
            return RestBean.failure(404, "配送单不存在");
        }
        return RestBean.success(delivery);
    }

    @PostMapping("/update-status")
    @Operation(summary = "更新配送状态（配送员）")
    public RestBean<String> updateDeliveryStatus(
            @RequestParam String deliveryNo,
            @RequestParam Integer status,
            @RequestParam(required = false) String location,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = deliveryService.updateDeliveryStatus(deliveryNo, status, location, account);
        return result ? RestBean.success("状态更新成功") : RestBean.failure(500, "状态更新失败");
    }

    @PostMapping("/assign")
    @Operation(summary = "指派配送员（管理员）")
    public RestBean<String> assignCourier(
            @RequestParam String deliveryNo,
            @RequestParam Integer courierId,
            HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = deliveryService.assignCourier(deliveryNo, courierId);
        return result ? RestBean.success("指派成功") : RestBean.failure(500, "指派失败");
    }

    @PostMapping("/cancel/{deliveryNo}")
    @Operation(summary = "取消配送单")
    public RestBean<String> cancelDelivery(@PathVariable String deliveryNo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = deliveryService.cancelDelivery(deliveryNo, account);
        return result ? RestBean.success("取消成功") : RestBean.failure(500, "取消失败");
    }

    @GetMapping("/courier/list")
    @Operation(summary = "获取可用配送员列表")
    public RestBean<Object> getAvailableCouriers() {
        return RestBean.success(deliveryService.getAvailableCouriers());
    }

    @GetMapping("/tracking/{deliveryNo}")
    @Operation(summary = "获取配送轨迹")
    public RestBean<Object> getDeliveryTracking(@PathVariable String deliveryNo) {
        return RestBean.success(deliveryService.getDeliveryTracking(deliveryNo));
    }

    @PostMapping("/start/{deliveryNo}")
    @Operation(summary = "开始配送（配送员）")
    public RestBean<String> startDelivery(@PathVariable String deliveryNo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = deliveryService.startDelivery(deliveryNo, account);
        return result ? RestBean.success("配送已开始") : RestBean.failure(500, "开始配送失败");
    }

    @PostMapping("/complete/{deliveryNo}")
    @Operation(summary = "完成配送（配送员）")
    public RestBean<String> completeDelivery(@PathVariable String deliveryNo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = deliveryService.completeDelivery(deliveryNo, account);
        return result ? RestBean.success("配送已完成") : RestBean.failure(500, "完成配送失败");
    }
}
