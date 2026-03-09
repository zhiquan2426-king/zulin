package com.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.Account;
import com.example.entity.Backup;
import com.example.entity.vo.request.SystemLogQueryVO;
import com.example.entity.vo.request.SystemSettingsUpdateVO;
import com.example.entity.vo.response.BackupVO;
import com.example.entity.vo.response.DashboardVO;
import com.example.entity.vo.response.SystemLogVO;
import com.example.entity.vo.response.SystemSettingsVO;
import com.example.service.BackupService;
import com.example.service.DashboardService;
import com.example.service.SystemLogService;
import com.example.service.SystemSettingsService;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@Tag(name = "系统管理", description = "系统管理接口（系统管理员）")
public class SystemController {

    @Resource
    private DashboardService dashboardService;
    
    @Resource
    private SystemLogService systemLogService;
    
    @Resource
    private SystemSettingsService systemSettingsService;
    
    @Resource
    private BackupService backupService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘数据")
    public RestBean<DashboardVO> getDashboard(HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("device_admin") && !account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        DashboardVO dashboard = dashboardService.getDashboardData();
        return RestBean.success(dashboard);
    }

    @GetMapping("/logs")
    @Operation(summary = "获取系统日志列表")
    public RestBean<IPage<SystemLogVO>> getSystemLogs(@Valid SystemLogQueryVO query, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        IPage<SystemLogVO> page = systemLogService.getLogPage(query);
        return RestBean.success(page);
    }

    @PostMapping("/logs/clear")
    @Operation(summary = "清空系统日志")
    public RestBean<String> clearLogs(HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        systemLogService.clearLogs();
        return RestBean.success("日志已清空");
    }

    @GetMapping("/settings")
    @Operation(summary = "获取系统设置列表")
    public RestBean<List<SystemSettingsVO>> getSystemSettings(HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        List<SystemSettingsVO> settings = systemSettingsService.getAllSettings();
        return RestBean.success(settings);
    }

    @GetMapping("/settings/{key}")
    @Operation(summary = "获取单个系统设置")
    public RestBean<SystemSettingsVO> getSystemSetting(@PathVariable String key, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        SystemSettingsVO setting = systemSettingsService.getSettingByKey(key);
        return RestBean.success(setting);
    }

    @PutMapping("/settings/update")
    @Operation(summary = "更新系统设置")
    public RestBean<String> updateSystemSetting(@Valid @RequestBody SystemSettingsUpdateVO vo, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = systemSettingsService.updateSetting(vo);
        return result ? RestBean.success("设置已更新") : RestBean.failure(500, "更新失败");
    }

    @GetMapping("/backups")
    @Operation(summary = "获取备份列表")
    public RestBean<IPage<BackupVO>> getBackups(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        Page<BackupVO> pageObj = new Page<>(page, size);
        IPage<BackupVO> result = backupService.getBackupPage(pageObj);
        return RestBean.success(result);
    }

    @PostMapping("/backup/create")
    @Operation(summary = "创建数据备份")
    public RestBean<String> createBackup(@RequestBody Backup backup, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = backupService.createBackup(backup, account.getId());
        return result ? RestBean.success("备份创建成功") : RestBean.failure(500, "备份创建失败");
    }

    @DeleteMapping("/backup/delete/{id}")
    @Operation(summary = "删除备份")
    public RestBean<String> deleteBackup(@PathVariable Integer id, HttpServletRequest request) {
        Account account = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!account.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        boolean result = backupService.deleteBackup(id);
        return result ? RestBean.success("备份已删除") : RestBean.failure(500, "删除失败");
    }
}
