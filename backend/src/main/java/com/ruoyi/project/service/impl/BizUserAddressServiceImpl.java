package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.domain.entity.BizUserAddress;
import com.ruoyi.project.mapper.BizUserAddressMapper;
import com.ruoyi.project.service.IBizUserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizUserAddressServiceImpl extends ServiceImpl<BizUserAddressMapper, BizUserAddress> implements IBizUserAddressService {

    @Override
    public List<BizUserAddress> listByUserId(Long userId) {
        LambdaQueryWrapper<BizUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserAddress::getDelFlag, 0)
                .eq(BizUserAddress::getUserId, userId)
                .orderByDesc(BizUserAddress::getIsDefault)
                .orderByDesc(BizUserAddress::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAddress(BizUserAddress address) {
        address.setDelFlag(0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefaultForUser(address.getUserId());
        }

        return this.save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(BizUserAddress address) {
        address.setUpdateTime(LocalDateTime.now());

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefaultForUser(address.getUserId());
        }

        return this.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long id) {
        BizUserAddress address = this.getById(id);
        if (address != null) {
            address.setDelFlag(2);
            address.setUpdateTime(LocalDateTime.now());
            return this.updateById(address);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long id, Long userId) {
        clearDefaultForUser(userId);

        BizUserAddress address = this.getById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            address.setUpdateTime(LocalDateTime.now());
            return this.updateById(address);
        }
        return false;
    }

    private void clearDefaultForUser(Long userId) {
        LambdaQueryWrapper<BizUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizUserAddress::getUserId, userId)
                .eq(BizUserAddress::getIsDefault, 1);

        List<BizUserAddress> defaultAddresses = this.list(wrapper);
        for (BizUserAddress address : defaultAddresses) {
            address.setIsDefault(0);
            address.setUpdateTime(LocalDateTime.now());
            this.updateById(address);
        }
    }
}
