package com.ruoyi.project.service;

import com.ruoyi.project.domain.entity.BizSynthesizeRule;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.impl.BizSynthesizeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 合成服务单元测试 — 校验分布式锁核心行为（加锁/解锁/锁粒度/异常处理）。
 *
 * 条件更新（LambdaUpdateWrapper + 状态机防双花）依赖 MyBatis-Plus TableInfoHelper，
 * 该组件需 Spring 容器初始化，建议用 @SpringBootTest + 真实 DB 做集成测试覆盖：
 *   1. 同一用户 + 同一批 assetIds 并发两个合成请求 → 仅 1 条成功
 *   2. 查库确认 material status=4 无重复
 *   3. redis-cli 确认锁 key 存在/自动释放
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("合成服务测试")
class BizSynthesizeServiceTest {

    @Mock
    private BizUserAssetMapper userAssetMapper;

    @Mock
    private IBizItemService itemService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock mockLock;

    private BizSynthesizeServiceImpl synthesizeService;

    @BeforeEach
    void setUp() {
        synthesizeService = spy(new BizSynthesizeServiceImpl(userAssetMapper, itemService, redissonClient));
    }

    // ─── lock acquisition failure ──────────────────────────────

    @Test
    @DisplayName("synthesize - 锁获取失败抛「操作过于频繁」")
    void synthesize_LockFailure() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesize(1L, "SR", 1));

        assertTrue(ex.getMessage().contains("操作过于频繁"));
        verify(mockLock, never()).unlock();
    }

    @Test
    @DisplayName("synthesizeByIds - 锁获取失败抛异常")
    void synthesizeByIds_LockFailure() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        List<Long> assetIds = Collections.nCopies(10, 100L);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByIds(1L, "SR", assetIds));

        assertTrue(ex.getMessage().contains("操作过于频繁"));
        verify(mockLock, never()).unlock();
    }

    @Test
    @DisplayName("synthesizeByItems - 锁获取失败抛异常")
    void synthesizeByItems_LockFailure() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByItems(1L, "SR", Collections.emptyList(), 1));

        assertTrue(ex.getMessage().contains("操作过于频繁"));
        verify(mockLock, never()).unlock();
    }

    // ─── interrupt handling ────────────────────────────────────

    @Test
    @DisplayName("InterruptedException 被正确处理 + 中断标志保持")
    void synthesize_InterruptedException() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesize(1L, "SR", 1));

        assertTrue(ex.getMessage().contains("合成被中断"));
        verify(mockLock, never()).unlock();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // clear for other tests
    }

    // ─── lock released in finally on business error ────────────

    @Test
    @DisplayName("业务异常时 finally 仍释放锁 (synthesize)")
    void synthesize_LockReleasedOnBusinessException() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        // getOne returns null → trigger "合成规则不存在" inside lock
        doReturn(null).when(synthesizeService).getOne(any());

        assertThrows(RuntimeException.class, () -> synthesizeService.synthesize(1L, "SR", 1));
        verify(mockLock).unlock();
    }

    @Test
    @DisplayName("参数校验失败时 finally 仍释放锁 (synthesizeByIds)")
    void synthesizeByIds_LockReleasedOnValidationFailure() throws InterruptedException {
        when(redissonClient.getLock("synthesize:lock:user:1")).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        // 只传9个 → 校验失败在锁内
        List<Long> assetIds = Collections.nCopies(9, 100L);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByIds(1L, "SR", assetIds));

        assertTrue(ex.getMessage().contains("请选择10个物品"));
        verify(mockLock).unlock();
    }

    // ─── lock key is per-user (not global) ─────────────────────

    @Test
    @DisplayName("三个方法均使用按用户粒度的锁 key")
    void lockKey_IsPerUser() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        // Make getOne throw after lock is acquired to verify lock key without needing
        // MyBatis-Plus LambdaUpdateWrapper (which requires TableInfoHelper init)
        doReturn(null).when(synthesizeService).getOne(any());

        // synthesize
        assertThrows(RuntimeException.class, () -> synthesizeService.synthesize(1L, "SR", 1));
        verify(redissonClient).getLock("synthesize:lock:user:1");

        // synthesizeByIds
        assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByIds(1L, "SR", Collections.nCopies(10, 1L)));
        verify(redissonClient, atLeast(2)).getLock("synthesize:lock:user:1");

        // synthesizeByItems
        assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByItems(1L, "SR", Collections.emptyList(), 1));
        verify(redissonClient, atLeast(3)).getLock("synthesize:lock:user:1");

        // Never a global lock key
        verify(redissonClient, never()).getLock(eq("synthesize:lock:global"));
    }

    @Test
    @DisplayName("不同用户使用不同锁 key")
    void lockKey_DifferentPerUser() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(null).when(synthesizeService).getOne(any());

        assertThrows(RuntimeException.class, () -> synthesizeService.synthesize(1L, "SR", 1));
        verify(redissonClient).getLock("synthesize:lock:user:1");

        assertThrows(RuntimeException.class, () -> synthesizeService.synthesize(2L, "SR", 1));
        verify(redissonClient).getLock("synthesize:lock:user:2");
    }

    // ─── lock is single RLock, not MultiLock ────────────────────

    @Test
    @DisplayName("使用单 RLock getLock() 而非 getMultiLock()")
    void lock_SingleRLockNotMultiLock() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        doReturn(null).when(synthesizeService).getOne(any());

        // All three throw business exceptions (getOne→null→规则不存在 / emptyList→参数校验),
        // but the lock+unlock pattern is still exercised
        assertThrows(RuntimeException.class, () -> synthesizeService.synthesize(1L, "SR", 1));
        assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByIds(1L, "SR", Collections.nCopies(10, 1L)));
        assertThrows(RuntimeException.class,
                () -> synthesizeService.synthesizeByItems(1L, "SR", Collections.emptyList(), 1));

        // all three use getLock (single RLock)
        verify(redissonClient, times(3)).getLock(anyString());
        // none use MultiLock
        verify(redissonClient, never()).getMultiLock(any());
    }
}
