package com.mall.user.controller;

import com.mall.common.core.Result;
import com.mall.common.web.UserContext;
import com.mall.user.dto.AddressRequest;
import com.mall.user.dto.AddressVO;
import com.mall.user.dto.UserVO;
import com.mall.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.getUser(UserContext.requireUserId()));
    }

    @GetMapping("/me/addresses")
    public Result<List<AddressVO>> addresses() {
        return Result.ok(userService.listAddresses(UserContext.requireUserId()));
    }

    @PostMapping("/me/addresses")
    public Result<AddressVO> addAddress(@Valid @RequestBody AddressRequest request) {
        return Result.ok(userService.addAddress(UserContext.requireUserId(), request));
    }
}
