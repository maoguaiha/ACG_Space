package com.ruoyi.project.service;

import com.ruoyi.project.domain.entity.BizTransaction;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizTransactionMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.impl.BizTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * 交易服务单元测试
 * <p>
 * 覆盖真实 BizTransactionServiceImpl 的 API：
 * - createTransaction(...) 生成订单号(TXN...)并计算手续费/卖家实得
 * - isTransactionCompleted(orderId) 判断完成(状态1)或失败(状态2)
 * - handleTransactionSuccess / handleTransactionFailed 回调
 * - compensate / retry 补偿与重试
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交易服务测试")
class BizTransactionServiceTest {

    @Mock
    private BizTransactionMapper transactionMapper;

    @Mock
    private BizUserAssetMapper userAssetMapper;

    @InjectMocks
    private BizTransactionServiceImpl transactionService;

    private BizTransaction transaction;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl stores the mapper in an inherited generic field
        // `baseMapper`; Mockito @InjectMocks cannot wire generic-typed fields, so we
        // inject it explicitly. Test-only wiring, no production code changed.
        ReflectionTestUtils.setField(transactionService, "baseMapper", transactionMapper);

        transaction = new BizTransaction();
        transaction.setId(1L);
        transaction.setOrderId("TXN202401010000012345");
        transaction.setBuyerId(2L);
        transaction.setSellerId(1L);
        transaction.setAssetId(100L);
        transaction.setItemId(100L);
        transaction.setAmount(1000);
        transaction.setStatus(0);
        transaction.setDelFlag(0);
        transaction.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建交易 - 成功并生成订单号，手续费=1%")
    void createTransaction_Success() {
        when(transactionMapper.insert(any(BizTransaction.class))).thenReturn(1);

        String orderId = transactionService.createTransaction(
                2L, 1L, 100L, 100L, "交易物品", "http://img", "SSR", 1000);

        assertNotNull(orderId);
        assertTrue(orderId.startsWith("TXN"), "订单号应以TXN开头");

        ArgumentCaptor<BizTransaction> captor = ArgumentCaptor.forClass(BizTransaction.class);
        verify(transactionMapper, times(1)).insert(captor.capture());
        BizTransaction saved = captor.getValue();
        assertEquals(1000, saved.getAmount());
        assertEquals(10, saved.getFee(), "手续费应为金额1%");
        assertEquals(990, saved.getSellerAmount(), "卖家实得=金额-手续费");
        assertEquals(0, saved.getStatus(), "新交易状态应为处理中(0)");
    }

    @Test
    @DisplayName("创建交易 - 0金额订单号仍生成且手续费为0")
    void createTransaction_ZeroAmount() {
        when(transactionMapper.insert(any(BizTransaction.class))).thenReturn(1);

        String orderId = transactionService.createTransaction(
                2L, 1L, 100L, 100L, "免费物品", "http://img", "N", 0);

        assertNotNull(orderId);
        ArgumentCaptor<BizTransaction> captor = ArgumentCaptor.forClass(BizTransaction.class);
        verify(transactionMapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getFee());
        assertEquals(0, captor.getValue().getSellerAmount());
    }

    @Test
    @DisplayName("检查交易是否完成 - 不存在")
    void isTransactionCompleted_NotFound() {
        when(transactionMapper.selectById(1L)).thenReturn(null);

        assertFalse(transactionService.isTransactionCompleted(1L));
        verify(transactionMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("检查交易是否完成 - 已成功(状态1)")
    void isTransactionCompleted_SuccessStatus() {
        transaction.setStatus(1);
        when(transactionMapper.selectById(1L)).thenReturn(transaction);

        assertTrue(transactionService.isTransactionCompleted(1L));
        verify(transactionMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("检查交易是否完成 - 已失败(状态2)")
    void isTransactionCompleted_FailedStatus() {
        transaction.setStatus(2);
        when(transactionMapper.selectById(1L)).thenReturn(transaction);

        assertTrue(transactionService.isTransactionCompleted(1L));
    }

    @Test
    @DisplayName("处理交易成功回调 - 成功")
    void handleTransactionSuccess_Success() {
        when(transactionMapper.selectOne(any(), anyBoolean())).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);
        BizUserAsset asset = new BizUserAsset();
        asset.setId(100L);
        asset.setUserId(1L);
        asset.setStatus(0);
        when(userAssetMapper.selectById(100L)).thenReturn(asset);
        when(userAssetMapper.updateById(any(BizUserAsset.class))).thenReturn(1);

        boolean result = transactionService.handleTransactionSuccess("TXN202401010000012345", "rmq-1");

        assertTrue(result);
        assertEquals(1, transaction.getStatus());
        assertNotNull(transaction.getCompleteTime());
        verify(transactionMapper).updateById(any(BizTransaction.class));
        verify(userAssetMapper).updateById(any(BizUserAsset.class));
    }

    @Test
    @DisplayName("处理交易成功回调 - 订单不存在")
    void handleTransactionSuccess_NotFound() {
        when(transactionMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        boolean result = transactionService.handleTransactionSuccess("TXN_NOT_EXIST", "rmq-1");

        assertFalse(result);
        verify(transactionMapper, never()).updateById(any(BizTransaction.class));
        verify(userAssetMapper, never()).updateById(any(BizUserAsset.class));
    }

    @Test
    @DisplayName("处理交易失败回调 - 成功")
    void handleTransactionFailed_Success() {
        when(transactionMapper.selectOne(any(), anyBoolean())).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);

        boolean result = transactionService.handleTransactionFailed("TXN202401010000012345", "余额不足");

        assertTrue(result);
        assertEquals(2, transaction.getStatus());
        assertEquals("余额不足", transaction.getErrorMsg());
        verify(transactionMapper).updateById(any(BizTransaction.class));
    }

    @Test
    @DisplayName("补偿交易 - 成功")
    void compensate_Success() {
        when(transactionMapper.selectOne(any(), anyBoolean())).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);
        BizUserAsset asset = new BizUserAsset();
        asset.setId(100L);
        when(userAssetMapper.selectById(100L)).thenReturn(asset);
        when(userAssetMapper.updateById(any(BizUserAsset.class))).thenReturn(1);

        boolean result = transactionService.compensate("TXN202401010000012345");

        assertTrue(result);
        assertEquals(1, transaction.getStatus());
        verify(transactionMapper).updateById(any(BizTransaction.class));
    }

    @Test
    @DisplayName("重试交易 - 成功")
    void retry_Success() {
        when(transactionMapper.selectOne(any(), anyBoolean())).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);

        boolean result = transactionService.retry("TXN202401010000012345");

        assertTrue(result);
        assertEquals(0, transaction.getStatus());
        assertNull(transaction.getErrorMsg());
        verify(transactionMapper).updateById(any(BizTransaction.class));
    }
}
