package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizUserAddress;

import java.util.List;

public interface IBizUserAddressService extends IService<BizUserAddress> {

    List<BizUserAddress> listByUserId(Long userId);

    boolean createAddress(BizUserAddress address);

    boolean updateAddress(BizUserAddress address);

    boolean deleteAddress(Long id);

    boolean setDefaultAddress(Long id, Long userId);
}
