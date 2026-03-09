package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.OrderCreateVO;
import com.example.entity.vo.request.OrderQueryVO;
import com.example.entity.vo.response.OrderVO;
import com.example.service.OrderService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/my-orders")
    @Operation(summary = "获取我的订单列表")
    public RestBean<IPage<OrderVO>> getMyOrders(@Valid OrderQueryVO query, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        IPage<OrderVO> page = orderService.getUserOrderPage(account.getId(), query);
        return RestBean.success(page);
    }

    @GetMapping("/all-orders")
    @Operation(summary = "获取所有订单（管理员）")
    public RestBean<IPage<OrderVO>> getAllOrders(@Valid OrderQueryVO query, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        IPage<OrderVO> page = orderService.getAllOrderPage(query);
        return RestBean.success(page);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取订单详情")
    public RestBean<OrderVO> getOrderDetail(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        OrderVO order = orderService.getOrderById(id);
        if (order == null) {
            return RestBean.failure(404, "订单不存在");
        }
        // 检查权限
        if (!order.getUserId().equals(account.getId()) &&
            !account.getRole().equals("device_admin") &&
            !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限查看该订单");
        }
        return RestBean.success(order);
    }

    @GetMapping("/detail-by-no/{orderNo}")
    @Operation(summary = "根据订单编号获取订单详情")
    public RestBean<OrderVO> getOrderDetailByNo(@PathVariable String orderNo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        OrderVO order = orderService.getOrderNo(orderNo);
        if (order == null) {
            return RestBean.failure(404, "订单不存在");
        }
        // 检查权限
        if (!order.getUserId().equals(account.getId()) &&
            !account.getRole().equals("device_admin") &&
            !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限查看该订单");
        }
        return RestBean.success(order);
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public RestBean<String> createOrder(@Valid @RequestBody OrderCreateVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = orderService.createOrder(vo, account.getId());
        return result ? RestBean.success("订单创建成功") : RestBean.failure(500, "订单创建失败");
    }

    @PostMapping("/pay/{id}")
    @Operation(summary = "支付订单")
    public RestBean<String> payOrder(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = orderService.payOrder(id);
        return result ? RestBean.success("支付成功") : RestBean.failure(500, "支付失败");
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    public RestBean<String> cancelOrder(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        boolean result = orderService.cancelOrder(id, account.getId());
        return result ? RestBean.success("订单已取消") : RestBean.failure(500, "取消失败");
    }

    @PostMapping("/complete/{id}")
    @Operation(summary = "完成订单（设备管理员）")
    public RestBean<String> completeOrder(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = orderService.completeOrder(id, account.getId());
        return result ? RestBean.success("订单已完成") : RestBean.failure(500, "操作失败");
    }

    @PostMapping("/return/{id}")
    @Operation(summary = "归还设备（设备管理员）")
    public RestBean<String> returnEquipment(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = orderService.returnEquipment(id, account.getId());
        return result ? RestBean.success("设备已归还") : RestBean.failure(500, "归还失败");
    }
}
