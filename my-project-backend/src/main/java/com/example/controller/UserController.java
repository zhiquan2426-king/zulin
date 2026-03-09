package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.utils.Const;
import com.example.entity.RestBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserController {

    @Resource
    private AccountMapper accountMapper;
    
    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    public RestBean<IPage<Account>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String role,
                                                 HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!currentAccount.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        
        Page<Account> pageObj = new Page<>(page, size);
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        
        wrapper.orderByDesc("register_time");
        
        IPage<Account> result = accountMapper.selectPage(pageObj, wrapper);
        return RestBean.success(result);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取用户详情")
    public RestBean<Account> getUserDetail(@PathVariable Integer id, HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!currentAccount.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        
        Account account = accountMapper.selectById(id);
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        // 隐藏密码
        account.setPassword(null);
        return RestBean.success(account);
    }

    @PutMapping("/update-role/{id}")
    @Operation(summary = "更新用户角色")
    public RestBean<String> updateUserRole(@PathVariable Integer id, 
                                            @RequestParam String role, 
                                            HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!currentAccount.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        
        Account account = accountMapper.selectById(id);
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        if (account.getId().equals(currentAccount.getId())) {
            return RestBean.failure(400, "不能修改自己的角色");
        }
        
        if (!role.equals("user") && !role.equals("device_admin") && !role.equals("system_admin")) {
            return RestBean.failure(400, "角色不正确");
        }
        
        account.setRole(role);
        boolean result = accountMapper.updateById(account) > 0;
        return result ? RestBean.success("角色更新成功") : RestBean.failure(500, "更新失败");
    }

    @PutMapping("/update-status/{id}")
    @Operation(summary = "更新用户状态")
    public RestBean<String> updateUserStatus(@PathVariable Integer id, 
                                             @RequestParam Integer status, 
                                             HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!currentAccount.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        
        Account account = accountMapper.selectById(id);
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        if (account.getId().equals(currentAccount.getId())) {
            return RestBean.failure(400, "不能禁用自己");
        }
        
        if (status != 0 && status != 1) {
            return RestBean.failure(400, "状态不正确");
        }
        
        account.setStatus(status);
        boolean result = accountMapper.updateById(account) > 0;
        return result ? RestBean.success("状态更新成功") : RestBean.failure(500, "更新失败");
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息")
    public RestBean<Map<String, Object>> getUserStatistics(HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (!currentAccount.getRole().equals("system_admin")) {
            return RestBean.failure(403, "无权限操作");
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", accountMapper.selectCount(null).intValue());
        stats.put("activeUsers", accountMapper.selectCount(new QueryWrapper<Account>().eq("status", 1)).intValue());
        stats.put("normalUsers", accountMapper.selectCount(new QueryWrapper<Account>().eq("role", "user")).intValue());
        stats.put("deviceAdmins", accountMapper.selectCount(new QueryWrapper<Account>().eq("role", "device_admin")).intValue());
        stats.put("systemAdmins", accountMapper.selectCount(new QueryWrapper<Account>().eq("role", "system_admin")).intValue());
        
        return RestBean.success(stats);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户个人信息")
    public RestBean<Account> getCurrentUserProfile(HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (currentAccount == null) {
            return RestBean.failure(401, "未登录");
        }
        
        // 从数据库重新获取最新信息
        Account account = accountMapper.selectById(currentAccount.getId());
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        // 隐藏密码
        account.setPassword(null);
        return RestBean.success(account);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新当前用户个人信息")
    public RestBean<Account> updateCurrentUserProfile(@RequestBody Account profile, HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (currentAccount == null) {
            return RestBean.failure(401, "未登录");
        }
        
        // 从数据库获取用户信息
        Account account = accountMapper.selectById(currentAccount.getId());
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        // 只允许更新特定字段
        if (profile.getUsername() != null && !profile.getUsername().isEmpty()) {
            // 检查用户名是否已被其他人使用
            Account existingAccount = accountMapper.selectOne(
                new QueryWrapper<Account>().eq("username", profile.getUsername()).ne("id", account.getId())
            );
            if (existingAccount != null) {
                return RestBean.failure(400, "该用户名已被使用");
            }
            account.setUsername(profile.getUsername());
        }
        
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            // 检查邮箱是否已被其他人使用
            Account existingAccount = accountMapper.selectOne(
                new QueryWrapper<Account>().eq("email", profile.getEmail()).ne("id", account.getId())
            );
            if (existingAccount != null) {
                return RestBean.failure(400, "该邮箱已被使用");
            }
            account.setEmail(profile.getEmail());
        }
        
        if (profile.getPhone() != null) {
            account.setPhone(profile.getPhone());
        }
        
        if (profile.getAvatar() != null) {
            account.setAvatar(profile.getAvatar());
        }
        
        // 更新数据库
        int result = accountMapper.updateById(account);
        if (result > 0) {
            account.setPassword(null);
            return RestBean.success(account);
        } else {
            return RestBean.failure(500, "更新失败");
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改当前用户密码")
    public RestBean<String> changePassword(@RequestBody Map<String, String> passwordData, HttpServletRequest request) {
        Account currentAccount = (Account) request.getAttribute(Const.ATTR_USER_ACCOUNT);
        if (currentAccount == null) {
            return RestBean.failure(401, "未登录");
        }
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return RestBean.failure(400, "请输入当前密码");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return RestBean.failure(400, "请输入新密码");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return RestBean.failure(400, "新密码长度必须在6到20个字符之间");
        }
        
        // 从数据库获取用户信息
        Account account = accountMapper.selectById(currentAccount.getId());
        if (account == null) {
            return RestBean.failure(404, "用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            return RestBean.failure(400, "当前密码不正确");
        }
        
        // 更新密码
        account.setPassword(passwordEncoder.encode(newPassword));
        int result = accountMapper.updateById(account);
        
        return result > 0 ? RestBean.success("密码修改成功") : RestBean.failure(500, "密码修改失败");
    }
}
