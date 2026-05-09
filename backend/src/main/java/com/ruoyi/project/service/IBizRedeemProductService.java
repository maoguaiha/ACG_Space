package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizRedeemProduct;

import java.util.List;

public interface IBizRedeemProductService extends IService<BizRedeemProduct> {

    Page<BizRedeemProduct> pageProducts(long pageNum, long pageSize, Integer status);

    List<BizRedeemProduct> getActiveProducts();

    boolean redeemProduct(Long userId, Long productId, String receiver, String phone, String province, String city, String district, String address);
}
