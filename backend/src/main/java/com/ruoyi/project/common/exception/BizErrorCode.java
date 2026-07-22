package com.ruoyi.project.common.exception;

import lombok.Getter;

/**
 * 业务错误码枚举
 * <p>
 * 统一管理项目中所有业务错误码和错误信息，避免硬编码字符串。
 * 面试时可展示：规范的错误码体系，便于前端统一处理和国际化扩展。
 * </p>
 *
 * @author ACG_Space Team
 */
@Getter
public enum BizErrorCode {

    // ========== 通用错误 ==========
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMITED(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用，请稍后重试"),

    // ========== 用户认证 ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    PASSWORD_EMPTY(1004, "密码不能为空"),
    TOKEN_INVALID(1005, "Token无效或已过期"),
    TOKEN_EXPIRED(1006, "Token已过期，请重新登录"),

    // ========== 用户资产 ==========
    ASSET_NOT_FOUND(2001, "资产不存在"),
    ASSET_NOT_OWNED(2002, "无权操作此资产"),
    ASSET_STATUS_INVALID(2003, "资产状态不可操作"),
    INSUFFICIENT_POINTS(2004, "积分不足"),

    // ========== 抽赏模块 ==========
    GACHA_POOL_NOT_FOUND(3001, "奖池不存在"),
    GACHA_POOL_UNAVAILABLE(3002, "奖池不可用或已结束"),
    GACHA_POOL_FULL(3003, "奖池已满"),
    GACHA_STOCK_INSUFFICIENT(3004, "库存不足"),
    GACHA_PRIZE_NOT_FOUND(3005, "奖品不存在"),
    GACHA_DRAW_FAILED(3006, "抽赏失败"),

    // ========== 市场模块 ==========
    MARKET_ITEM_NOT_FOUND(4001, "商品不存在"),
    MARKET_ITEM_SOLD(4002, "商品已下架或已售出"),
    MARKET_CANNOT_BUY_OWN(4003, "不能购买自己上架的商品"),
    MARKET_LIST_FAILED(4004, "上架失败"),
    MARKET_DELIST_FAILED(4005, "下架失败"),
    MARKET_ORDER_FAILED(4006, "下单失败"),

    // ========== 交易模块 ==========
    TRANSACTION_NOT_FOUND(5001, "交易记录不存在"),
    TRANSACTION_ALREADY_COMPLETED(5002, "交易已完成，不可重复操作"),
    TRANSACTION_FAILED(5003, "交易失败"),

    // ========== 合成模块 ==========
    SYNTHESIZE_LOCK_FAILED(6001, "合成操作过于频繁，请稍后再试"),
    SYNTHESIZE_MATERIAL_INSUFFICIENT(6002, "合成材料不足"),
    SYNTHESIZE_RECIPE_NOT_FOUND(6003, "合成配方不存在"),

    // ========== 充值模块 ==========
    RECHARGE_PACKAGE_NOT_FOUND(7001, "充值套餐不存在"),
    RECHARGE_ORDER_FAILED(7002, "充值下单失败"),
    RECHARGE_ALREADY_PAID(7003, "订单已支付，不可重复支付"),

    // ========== 碎片模块 ==========
    FRAGMENT_INSUFFICIENT(8001, "碎片数量不足"),
    FRAGMENT_EXCHANGE_FAILED(8002, "碎片兑换失败"),

    // ========== 兑换模块 ==========
    REDEEM_PRODUCT_NOT_FOUND(9001, "兑换商品不存在"),
    REDEEM_STOCK_INSUFFICIENT(9002, "兑换库存不足"),

    // ========== 评论/文章 ==========
    COMMENT_CONTENT_EMPTY(10001, "评论内容不能为空"),
    ARTICLE_NOT_FOUND(10002, "文章不存在"),
    ARTICLE_STATUS_INVALID(10003, "文章状态异常");

    /**
     * 业务错误码
     */
    private final int code;

    /**
     * 中文错误信息
     */
    private final String message;

    BizErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
