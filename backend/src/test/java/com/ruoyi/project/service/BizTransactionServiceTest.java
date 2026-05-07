package com.ruoyi.project.service;

import com.ruoyi.project.domain.entity.BizMarketItem;
import com.ruoyi.project.domain.entity.BizTransaction;
import com.ruoyi.project.domain.entity.BizTransactionLog;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizMarketItemMapper;
import com.ruoyi.project.mapper.BizTransactionLogMapper;
import com.ruoyi.project.mapper.BizTransactionMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.impl.BizTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 交易服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交易服务测试")
class BizTransactionServiceTest {

    @Mock
    private BizTransactionMapper transactionMapper;

    @Mock
    private BizTransactionLogMapper transactionLogMapper;

    @Mock
    private BizMarketItemMapper marketItemMapper;

    @Mock
    private BizUserAssetMapper userAssetMapper;

    @InjectMocks
    private BizTransactionServiceImpl transactionService;

    private BizMarketItem marketItem;
    private BizUserAsset sellerAsset;
    private BizUserAsset buyerAsset;
    private BizTransaction transaction;

    @BeforeEach
    void setUp() {
        // 市场物品
        marketItem = new BizMarketItem();
        marketItem.setId(1L);
        marketItem.setItemId(100L);
        marketItem.setItemName("交易物品");
        marketItem.setPrice(1000);
        marketItem.setSellerId(1L);
        marketItem.setStatus(1);
        marketItem.setDelFlag(0);

        // 卖家资产
        sellerAsset = new BizUserAsset();
        sellerAsset.setId(1L);
        sellerAsset.setUserId(1L);
        sellerAsset.setItemId(100L);
        sellerAsset.setItemName("交易物品");
        sellerAsset.setQuantity(1);
        sellerAsset.setDelFlag(0);

        // 买家资产（空）
        buyerAsset = new BizUserAsset();
        buyerAsset.setId(2L);
        buyerAsset.setUserId(2L);
        buyerAsset.setItemId(100L);
        buyerAsset.setItemName("交易物品");
        buyerAsset.setQuantity(0);
        buyerAsset.setDelFlag(0);

        // 交易记录
        transaction = new BizTransaction();
        transaction.setId(1L);
        transaction.setMarketItemId(1L);
        transaction.setBuyerId(2L);
        transaction.setSellerId(1L);
        transaction.setPrice(1000);
        transaction.setStatus(0);
        transaction.setDelFlag(0);
        transaction.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建交易 - 成功")
    void createTransaction_Success() {
        when(transactionMapper.insert(any(BizTransaction.class))).thenReturn(1);
        when(transactionLogMapper.insert(any(BizTransactionLog.class))).thenReturn(1);

        boolean result = transactionService.createTransaction(transaction);

        assertTrue(result);
        verify(transactionMapper, times(1)).insert(any(BizTransaction.class));
        verify(transactionLogMapper, times(1)).insert(any(BizTransactionLog.class));
    }

    @Test
    @DisplayName("创建交易 - 失败")
    void createTransaction_Failure() {
        when(transactionMapper.insert(any(BizTransaction.class))).thenReturn(0);

        boolean result = transactionService.createTransaction(transaction);

        assertFalse(result);
        verify(transactionMapper, times(1)).insert(any(BizTransaction.class));
        verify(transactionLogMapper, never()).insert(any(BizTransactionLog.class));
    }

    @Test
    @DisplayName("更新交易状态 - 成功")
    void updateTransactionStatus_Success() {
        when(transactionMapper.selectById(1L)).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);

        boolean result = transactionService.updateTransactionStatus(1L, 1);

        assertTrue(result);
        assertEquals(1, transaction.getStatus());
        verify(transactionMapper, times(1)).selectById(1L);
        verify(transactionMapper, times(1)).updateById(any(BizTransaction.class));
    }

    @Test
    @DisplayName("更新交易状态 - 交易不存在")
    void updateTransactionStatus_NotFound() {
        when(transactionMapper.selectById(1L)).thenReturn(null);

        boolean result = transactionService.updateTransactionStatus(1L, 1);

        assertFalse(result);
        verify(transactionMapper, times(1)).selectById(1L);
        verify(transactionMapper, never()).updateById(any(BizTransaction.class));
    }

    @Test
    @DisplayName("检查交易是否已处理 - 未处理")
    void isTransactionProcessed_NotProcessed() {
        when(transactionMapper.selectById(1L)).thenReturn(null);

        boolean result = transactionService.isTransactionProcessed(1L);

        assertFalse(result);
        verify(transactionMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("检查交易是否已处理 - 已处理")
    void isTransactionProcessed_Processed() {
        transaction.setStatus(1);
        when(transactionMapper.selectById(1L)).thenReturn(transaction);

        boolean result = transactionService.isTransactionProcessed(1L);

        assertTrue(result);
        verify(transactionMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("完成交易 - 成功")
    void completeTransaction_Success() {
        when(transactionMapper.selectById(1L)).thenReturn(transaction);
        when(marketItemMapper.selectById(1L)).thenReturn(marketItem);
        when(userAssetMapper.selectOne(any())).thenReturn(sellerAsset);
        when(userAssetMapper.updateById(any())).thenReturn(1);
        when(userAssetMapper.insert(any())).thenReturn(1);
        when(marketItemMapper.updateById(any())).thenReturn(1);

        boolean result = transactionService.completeTransaction(1L);

        assertTrue(result);
        verify(userAssetMapper, atLeastOnce()).updateById(any());
        verify(marketItemMapper, times(1)).updateById(any());
    }

    @Test
    @DisplayName("完成交易 - 交易不存在")
    void completeTransaction_TransactionNotFound() {
        when(transactionMapper.selectById(1L)).thenReturn(null);

        boolean result = transactionService.completeTransaction(1L);

        assertFalse(result);
        verify(marketItemMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("完成交易 - 市场物品不存在")
    void completeTransaction_MarketItemNotFound() {
        when(transactionMapper.selectById(1L)).thenReturn(transaction);
        when(marketItemMapper.selectById(1L)).thenReturn(null);

        boolean result = transactionService.completeTransaction(1L);

        assertFalse(result);
        verify(userAssetMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("取消交易 - 成功")
    void cancelTransaction_Success() {
        when(transactionMapper.selectById(1L)).thenReturn(transaction);
        when(transactionMapper.updateById(any(BizTransaction.class))).thenReturn(1);

        boolean result = transactionService.cancelTransaction(1L);

        assertTrue(result);
        assertEquals(2, transaction.getStatus());
        verify(transactionMapper, times(1)).selectById(1L);
        verify(transactionMapper, times(1)).updateById(any(BizTransaction.class));
    }

    @Test
    @DisplayName("取消交易 - 交易不存在")
    void cancelTransaction_NotFound() {
        when(transactionMapper.selectById(1L)).thenReturn(null);

        boolean result = transactionService.cancelTransaction(1L);

        assertFalse(result);
        verify(transactionMapper, times(1)).selectById(1L);
        verify(transactionMapper, never()).updateById(any(BizTransaction.class));
    }
}