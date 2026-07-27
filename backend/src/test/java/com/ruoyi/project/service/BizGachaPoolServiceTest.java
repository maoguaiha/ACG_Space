package com.ruoyi.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.project.domain.entity.BizGachaPool;
import com.ruoyi.project.mapper.BizGachaPoolMapper;
import com.ruoyi.project.service.impl.BizGachaPoolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 抽赏奖池服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("抽赏奖池服务测试")
class BizGachaPoolServiceTest {

    @Mock
    private BizGachaPoolMapper gachaPoolMapper;

    @InjectMocks
    private BizGachaPoolServiceImpl gachaPoolService;

    private BizGachaPool testPool;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl stores the mapper in an inherited generic field
        // `baseMapper`; Mockito @InjectMocks cannot wire generic-typed fields, so we
        // inject it explicitly. Test-only wiring, no production code changed.
        ReflectionTestUtils.setField(gachaPoolService, "baseMapper", gachaPoolMapper);

        testPool = new BizGachaPool();
        testPool.setId(1L);
        testPool.setName("测试奖池");
        testPool.setTotalStock(100);
        testPool.setRemainingStock(50);
        testPool.setStatus(1);
        testPool.setDelFlag(0);
        testPool.setStartTime(LocalDateTime.now().minusDays(1));
        testPool.setEndTime(LocalDateTime.now().plusDays(7));
        testPool.setCreateTime(LocalDateTime.now());
        testPool.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建奖池 - 成功")
    void createPool_Success() {
        when(gachaPoolMapper.insert(any(BizGachaPool.class))).thenReturn(1);

        boolean result = gachaPoolService.createPool(testPool);

        assertTrue(result);
        assertEquals(0, testPool.getDelFlag());
        assertEquals(0, testPool.getStatus());
        assertEquals(testPool.getTotalStock(), testPool.getRemainingStock());
        verify(gachaPoolMapper, times(1)).insert(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("创建奖池 - 失败")
    void createPool_Failure() {
        when(gachaPoolMapper.insert(any(BizGachaPool.class))).thenReturn(0);

        boolean result = gachaPoolService.createPool(testPool);

        assertFalse(result);
        verify(gachaPoolMapper, times(1)).insert(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("检查奖池可用性 - 可用")
    void isPoolAvailable_Available() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertTrue(result);
        verify(gachaPoolMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("检查奖池可用性 - 不存在")
    void isPoolAvailable_NotFound() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(null);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
        verify(gachaPoolMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("检查奖池可用性 - 已删除")
    void isPoolAvailable_Deleted() {
        testPool.setDelFlag(1);
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("检查奖池可用性 - 状态未激活")
    void isPoolAvailable_Inactive() {
        testPool.setStatus(0);
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("检查奖池可用性 - 未到开始时间")
    void isPoolAvailable_BeforeStartTime() {
        testPool.setStartTime(LocalDateTime.now().plusDays(1));
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("检查奖池可用性 - 已过期")
    void isPoolAvailable_Expired() {
        testPool.setEndTime(LocalDateTime.now().minusDays(1));
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("检查奖池可用性 - 库存不足")
    void isPoolAvailable_StockEmpty() {
        testPool.setRemainingStock(0);
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.isPoolAvailable(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("扣减库存 - 成功")
    void decrementStock_Success() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);
        when(gachaPoolMapper.updateById(any(BizGachaPool.class))).thenReturn(1);

        boolean result = gachaPoolService.decrementStock(1L, 10);

        assertTrue(result);
        assertEquals(40, testPool.getRemainingStock());
        verify(gachaPoolMapper, times(1)).selectById(1L);
        verify(gachaPoolMapper, times(1)).updateById(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("扣减库存 - 奖池不存在")
    void decrementStock_PoolNotFound() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(null);

        boolean result = gachaPoolService.decrementStock(1L, 10);

        assertFalse(result);
        verify(gachaPoolMapper, times(1)).selectById(1L);
        verify(gachaPoolMapper, never()).updateById(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("扣减库存 - 库存不足")
    void decrementStock_InsufficientStock() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);

        boolean result = gachaPoolService.decrementStock(1L, 100);

        assertFalse(result);
        verify(gachaPoolMapper, times(1)).selectById(1L);
        verify(gachaPoolMapper, never()).updateById(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("结束奖池 - 成功")
    void endPool_Success() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(testPool);
        when(gachaPoolMapper.updateById(any(BizGachaPool.class))).thenReturn(1);

        boolean result = gachaPoolService.endPool(1L);

        assertTrue(result);
        assertEquals(2, testPool.getStatus());
        verify(gachaPoolMapper, times(1)).selectById(1L);
        verify(gachaPoolMapper, times(1)).updateById(any(BizGachaPool.class));
    }

    @Test
    @DisplayName("结束奖池 - 奖池不存在")
    void endPool_PoolNotFound() {
        when(gachaPoolMapper.selectById(1L)).thenReturn(null);

        boolean result = gachaPoolService.endPool(1L);

        assertFalse(result);
        verify(gachaPoolMapper, times(1)).selectById(1L);
        verify(gachaPoolMapper, never()).updateById(any(BizGachaPool.class));
    }
}