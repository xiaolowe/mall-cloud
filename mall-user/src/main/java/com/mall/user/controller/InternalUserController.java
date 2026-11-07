package com.mall.user.controller;

import com.mall.common.core.Result;
import com.mall.user.dto.AddressVO;
import com.mall.user.dto.UserVO;
import com.mall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints. The gateway only routes /api/**, so these are not exposed publicly.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    @GetMapping("/{id}/addresses/default")
    public Result<AddressVO> defaultAddress(@PathVariable Long id) {
        return Result.ok(userService.getDefaultAddress(id));
    }
}
