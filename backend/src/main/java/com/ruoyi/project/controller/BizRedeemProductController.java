package com.ruoyi.project.controller;

import com.ruoyi.project.common.api.Result;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizRedeemProduct;
import com.ruoyi.project.service.IBizRedeemProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/redeem-product")
@RequiredArgsConstructor
@Validated
public class BizRedeemProductController {

    private final IBizRedeemProductService redeemProductService;

    @GetMapping("/list")
    public Result<List<BizRedeemProduct>> list() {
        List<BizRedeemProduct> products = redeemProductService.getActiveProducts();
        return Result.success(products);
    }

    @PostMapping("/redeem")
    public Result<RedeemResult> redeem(@RequestBody @Valid RedeemRequest request) {
        Long userId = getCurrentUserId();
        log.info("【兑换接口】收到请求: userId={}, productId={}, receiver={}, phone={}", 
                userId, request.getProductId(), request.getReceiver(), request.getPhone());
        try {
            boolean success = redeemProductService.redeemProduct(
                    userId,
                    parseProductId(request.getProductId()),
                    request.getReceiver(),
                    request.getPhone(),
                    request.getProvince(),
                    request.getCity(),
                    request.getDistrict(),
                    request.getAddress()
            );
            log.info("【兑换接口】兑换结果: success={}", success);
            if (success) {
                RedeemResult result = new RedeemResult();
                result.setSuccess(true);
                result.setMessage("兑换成功，请等待发货");
                return Result.success(result);
            }
            return Result.error("兑换失败");
        } catch (RuntimeException e) {
            log.error("【兑换接口】异常: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解析商品ID，支持字符串和数字类型
     * 解决雪花ID在前端精度丢失问题
     */
    private Long parseProductId(Object productIdObj) {
        if (productIdObj == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        if (productIdObj instanceof Long) {
            return (Long) productIdObj;
        }
        if (productIdObj instanceof Integer) {
            return ((Integer) productIdObj).longValue();
        }
        if (productIdObj instanceof String) {
            try {
                return Long.parseLong((String) productIdObj);
            } catch (NumberFormatException e) {
                throw new RuntimeException("商品ID格式错误");
            }
        }
        if (productIdObj instanceof Number) {
            return ((Number) productIdObj).longValue();
        }
        throw new RuntimeException("商品ID格式错误");
    }

    private Long getCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    @Data
    public static class RedeemRequest {
        private Object productId;
        private String receiver;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String address;
    }

    @Data
    public static class RedeemResult {
        private Boolean success;
        private String message;
    }
}
