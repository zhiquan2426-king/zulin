package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EquipmentAddVO;
import com.example.entity.vo.request.EquipmentQueryVO;
import com.example.entity.vo.response.EquipmentVO;
import com.example.service.EquipmentService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@Tag(name = "设备管理", description = "设备相关接口")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    @GetMapping("/list")
    @Operation(summary = "获取设备列表（分页）")
    public RestBean<IPage<EquipmentVO>> getEquipmentList(@Valid EquipmentQueryVO query) {
        IPage<EquipmentVO> page = equipmentService.getEquipmentPage(query);
        return RestBean.success(page);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取设备详情")
    public RestBean<EquipmentVO> getEquipmentDetail(@PathVariable Integer id) {
        EquipmentVO equipment = equipmentService.getEquipmentById(id);
        if (equipment == null) {
            return RestBean.failure(404, "设备不存在");
        }
        return RestBean.success(equipment);
    }

    @PostMapping("/add")
    @Operation(summary = "添加设备（设备管理员）")
    public RestBean<String> addEquipment(@Valid @RequestBody EquipmentAddVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = equipmentService.addEquipment(vo, account.getId());
        return result ? RestBean.success("添加成功") : RestBean.failure(500, "添加失败");
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "更新设备（设备管理员）")
    public RestBean<String> updateEquipment(@PathVariable Integer id, @Valid @RequestBody EquipmentAddVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = equipmentService.updateEquipment(id, vo, account.getId());
        return result ? RestBean.success("更新成功") : RestBean.failure(500, "更新失败");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除设备（设备管理员）")
    public RestBean<String> deleteEquipment(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = equipmentService.deleteEquipment(id);
        return result ? RestBean.success("删除成功") : RestBean.failure(500, "删除失败");
    }

    @PutMapping("/status/{id}/{status}")
    @Operation(summary = "更新设备状态（设备管理员）")
    public RestBean<String> updateEquipmentStatus(@PathVariable Integer id, @PathVariable Integer status, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = equipmentService.updateEquipmentStatus(id, status, account.getId());
        return result ? RestBean.success("状态更新成功") : RestBean.failure(500, "状态更新失败");
    }
}
