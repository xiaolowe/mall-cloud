package com.mall.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.user.dto.UserVO;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        query.orderByDesc(User::getId);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), query);
        return Result.ok(PageResult.of(result.getTotal(), result.getRecords().stream()
                .map(u -> new UserVO(u.getId(), u.getUsername(), u.getNickname(), u.getPhone()))
                .toList()));
    }

    @GetMapping("/stats/users")
    public Result<Map<String, Object>> userStats() {
        return Result.ok(Map.of("totalUsers", userMapper.selectCount(null)));
    }
}
