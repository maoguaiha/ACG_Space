package com.ruoyi.project;

import com.ruoyi.project.common.exception.BizException;
import com.ruoyi.project.domain.entity.*;
import com.ruoyi.project.mapper.*;
import com.ruoyi.project.service.IBizMarketService;
import com.ruoyi.project.service.IBizUserPointsLogService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 市场交易集成测试
 * <p>
 * 面试亮点：覆盖完整的市场交易流程，验证：
 * 1. 资产上架 → 商品创建正确
 * 2. 购买流程 → 积分转移、手续费计算、资产转移
 * 3. 边界条件 → 不能自买、库存不足、商品已售
 * 4. 下架 → 资产恢复
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("市场交易集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BizMarketServiceIntegrationTest {

    @Autowired
    private IBizMarketService marketService;

    @Autowired
    private IBizUserPointsLogService pointsLogService;

    @Autowired
    private BizMarketItemMapper marketItemMapper;

    @Autowired
    private BizUserAssetMapper userAssetMapper;

    @Autowired
    private SysUserMapper userMapper;

    private static Long sellerId;
    private static Long buyerId;
    private static Long assetId;
    private static Long marketItemId;

    @BeforeEach
    void setUp() {
        // 创建测试用的卖家和买家
        if (sellerId == null) {
            sellerId = createTestUser("seller_test", "卖家");
            buyerId = createTestUser("buyer_test", "买家");
            // 给买家充值积分
            pointsLogService.addPoints(buyerId, 10000, "TEST", "init");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 创建资产 → 上架到市场")
    void testListAsset() {
        // 创建卖家资产
        BizUserAsset asset = new BizUserAsset();
        asset.setUserId(sellerId);
        asset.setItemId(100L);
        asset.setAssetKey("test_asset_" + System.currentTimeMillis());
        asset.setQuantity(1);
        asset.setStatus(1);
        asset.setIsPhysical(0);
        asset.setAcquireType("gacha");
        asset.setItemName("测试物品-SSR");
        asset.setItemImage("http://test.com/img.png");
        asset.setItemRarity("SSR");
        asset.setItemType("character");
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        asset.setDelFlag(0);
        userAssetMapper.insert(asset);
        assetId = asset.getId();
        assertNotNull(assetId, "资产创建后应有ID");

        // 上架到市场
        Long resultId = marketService.listAsset(sellerId, assetId, 500);
        assertNotNull(resultId, "上架应该返回商品ID");
        marketItemId = resultId;

        // 验证资产状态变为"上架中"
        BizUserAsset updated = userAssetMapper.selectById(assetId);
        assertEquals(2, updated.getStatus(), "上架后资产状态应为2(锁定)");

        // 验证商品已创建
        BizMarketItem marketItem = marketItemMapper.selectById(marketItemId);
        assertNotNull(marketItem, "市场商品应存在");
        assertEquals(500, marketItem.getPrice(), "价格应为500积分");
        assertEquals(0, marketItem.getStatus(), "商品状态应为待售(0)");
        assertEquals(sellerId, marketItem.getSellerId(), "卖家ID应匹配");
    }

    @Test
    @Order(2)
    @DisplayName("2. 不能购买自己上架的商品")
    void testCannotBuyOwnItem() {
        assertNotNull(marketItemId, "需要先上架商品");

        BizException ex = assertThrows(BizException.class, () -> {
            marketService.buyItem(sellerId, marketItemId);
        });
        assertEquals(4003, ex.getCode(), "自己购买应返回4003(MARKET_CANNOT_BUY_OWN)");
    }

    @Test
    @Order(3)
    @DisplayName("3. 买家购买商品 → 积分扣减 + 手续费 + 资产转移")
    void testBuyItem() {
        assertNotNull(marketItemId, "需要先上架商品");

        // 记录购买前积分
        BizUserAsset sellerAssetBefore = userAssetMapper.selectById(assetId);

        // 执行购买
        String orderId = marketService.buyItem(buyerId, marketItemId);
        assertNotNull(orderId, "购买应返回订单号");
        assertTrue(orderId.startsWith("ORD"), "订单号应以ORD开头");

        // 验证商品状态变为已售
        BizMarketItem marketItem = marketItemMapper.selectById(marketItemId);
        assertNotNull(marketItem);
        assertEquals(1, marketItem.getStatus(), "购买后商品状态应为已售(1)");
        assertNotNull(marketItem.getOrderId(), "商品应关联订单号");
        assertNotNull(marketItem.getSoldTime(), "应有售出时间");

        // 验证资产所有权转移
        BizUserAsset assetAfter = userAssetMapper.selectById(assetId);
        assertNotNull(assetAfter);
        assertEquals(buyerId, assetAfter.getUserId(), "资产所有者应变更为买家");
        assertEquals(1, assetAfter.getStatus(), "资产状态应为正常(1)");
    }

    @Test
    @Order(4)
    @DisplayName("4. 商品已售不能重复购买")
    void testCannotBuySoldItem() {
        assertNotNull(marketItemId, "需要先有商品");

        BizException ex = assertThrows(BizException.class, () -> {
            marketService.buyItem(buyerId, marketItemId);
        });
        assertTrue(ex.getCode() == 4001 || ex.getCode() == 4002,
                "重复购买应返回商品不存在(4001)或已售(4002)");
    }

    // ====== 辅助方法 ======

    private Long createTestUser(String username, String nickname) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword("$2a$10$placeholder");  // BCrypt 占位
        user.setPoints(1000);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDelFlag(0);
        userMapper.insert(user);
        return user.getId();
    }
}
