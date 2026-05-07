package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizUserAddress;
import com.ruoyi.project.service.IBizUserAddressService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Validated
public class BizAddressController {

    private final IBizUserAddressService addressService;

    @GetMapping("/list")
    public Result<List<BizUserAddress>> list() {
        Long userId = getCurrentUserId();
        List<BizUserAddress> list = addressService.listByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<BizUserAddress> getById(@PathVariable Long id) {
        BizUserAddress address = addressService.getById(id);
        if (address == null) {
            return Result.error("地址不存在");
        }
        return Result.success(address);
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Validated AddressRequest request) {
        Long userId = getCurrentUserId();
        BizUserAddress address = new BizUserAddress();
        address.setUserId(userId);
        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault() ? 1 : 0);
        address.setStatus(1);
        boolean success = addressService.createAddress(address);
        if (success) {
            return Result.success(address.getId());
        }
        return Result.error("创建失败");
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody @Validated AddressRequest request) {
        if (request.getId() == null) {
            return Result.error("地址ID不能为空");
        }
        BizUserAddress address = addressService.getById(request.getId());
        if (address == null) {
            return Result.error("地址不存在");
        }
        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault() ? 1 : 0);
        }
        boolean success = addressService.updateAddress(address);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = addressService.deleteAddress(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{id}/default")
    public Result<Boolean> setDefault(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        boolean success = addressService.setDefaultAddress(id, userId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("设置失败");
    }

    private Long getCurrentUserId() {
        try {
            Long userId = SecurityUtils.getUserId();
            return userId != null ? userId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class AddressRequest {
        private Long id;
        private String receiver;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detailAddress;
        private String postalCode;
        private Boolean isDefault;
    }
}