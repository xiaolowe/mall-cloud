package com.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.core.BizException;
import com.mall.user.dto.AddressRequest;
import com.mall.user.dto.AddressVO;
import com.mall.user.dto.UserVO;
import com.mall.user.entity.User;
import com.mall.user.entity.UserAddress;
import com.mall.user.mapper.UserAddressMapper;
import com.mall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;

    public UserVO getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return AuthService.toVO(user);
    }

    public List<AddressVO> listAddresses(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public AddressVO addAddress(Long userId, AddressRequest request) {
        Long count = addressMapper.selectCount(
                new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
        boolean isDefault = Boolean.TRUE.equals(request.isDefault()) || count == 0;
        if (isDefault) {
            addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0));
        }
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(request.receiverName());
        address.setReceiverPhone(request.receiverPhone());
        address.setDetail(request.detail());
        address.setIsDefault(isDefault ? 1 : 0);
        address.setCreatedAt(LocalDateTime.now());
        addressMapper.insert(address);
        return toVO(address);
    }

    public AddressVO getDefaultAddress(Long userId) {
        UserAddress address = addressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("limit 1"));
        if (address == null) {
            address = addressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getId)
                    .last("limit 1"));
        }
        if (address == null) {
            throw new BizException("请先添加收货地址");
        }
        return toVO(address);
    }

    private AddressVO toVO(UserAddress address) {
        return new AddressVO(address.getId(), address.getReceiverName(), address.getReceiverPhone(),
                address.getDetail(), address.getIsDefault() != null && address.getIsDefault() == 1);
    }
}
