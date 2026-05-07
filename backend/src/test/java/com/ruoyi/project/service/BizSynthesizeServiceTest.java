package com.ruoyi.project.service;

import com.ruoyi.project.domain.entity.BizSynthesizeRecipe;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.mapper.BizSynthesizeRecipeMapper;
import com.ruoyi.project.mapper.BizSynthesizeRecordMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.service.impl.BizSynthesizeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 合成服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("合成服务测试")
class BizSynthesizeServiceTest {

    @Mock
    private BizSynthesizeRecipeMapper synthesizeRecipeMapper;

    @Mock
    private BizUserAssetMapper userAssetMapper;

    @Mock
    private BizSynthesizeRecordMapper synthesizeRecordMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock mockLock;

    @InjectMocks
    private BizSynthesizeServiceImpl synthesizeService;

    private BizSynthesizeRecipe testRecipe;
    private BizUserAsset testAsset1;
    private BizUserAsset testAsset2;
    private BizUserAsset resultAsset;

    @BeforeEach
    void setUp() {
        // 测试配方：2个碎片合成1个完整物品
        testRecipe = new BizSynthesizeRecipe();
        testRecipe.setId(1L);
        testRecipe.setRecipeName("测试合成配方");
        testRecipe.setResultItemId(100L);
        testRecipe.setResultItemName("合成物品");
        testRecipe.setConsumeItemIds("1,2");
        testRecipe.setConsumeCounts("2,1");
        testRecipe.setDelFlag(0);

        // 消耗物品1
        testAsset1 = new BizUserAsset();
        testAsset1.setId(1L);
        testAsset1.setUserId(1L);
        testAsset1.setItemId(1L);
        testAsset1.setItemName("碎片1");
        testAsset1.setQuantity(5);
        testAsset1.setDelFlag(0);

        // 消耗物品2
        testAsset2 = new BizUserAsset();
        testAsset2.setId(2L);
        testAsset2.setUserId(1L);
        testAsset2.setItemId(2L);
        testAsset2.setItemName("碎片2");
        testAsset2.setQuantity(3);
        testAsset2.setDelFlag(0);

        // 结果物品
        resultAsset = new BizUserAsset();
        resultAsset.setUserId(1L);
        resultAsset.setItemId(100L);
        resultAsset.setItemName("合成物品");
        resultAsset.setQuantity(0);
        resultAsset.setDelFlag(0);
    }

    @Test
    @DisplayName("获取合成配方列表 - 成功")
    void getRecipes_Success() {
        when(synthesizeRecipeMapper.selectList(any())).thenReturn(Arrays.asList(testRecipe));

        List<BizSynthesizeRecipe> recipes = synthesizeService.getRecipes();

        assertFalse(recipes.isEmpty());
        assertEquals(1, recipes.size());
        assertEquals("测试合成配方", recipes.get(0).getRecipeName());
        verify(synthesizeRecipeMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("获取合成配方列表 - 空列表")
    void getRecipes_Empty() {
        when(synthesizeRecipeMapper.selectList(any())).thenReturn(Arrays.asList());

        List<BizSynthesizeRecipe> recipes = synthesizeService.getRecipes();

        assertTrue(recipes.isEmpty());
        verify(synthesizeRecipeMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("获取用户可用配方 - 有可用配方")
    void getUserAvailableRecipes_HasAvailable() {
        when(synthesizeRecipeMapper.selectList(any())).thenReturn(Arrays.asList(testRecipe));
        when(userAssetMapper.selectList(any())).thenReturn(Arrays.asList(testAsset1, testAsset2));

        List<BizSynthesizeRecipe> recipes = synthesizeService.getUserAvailableRecipes(1L);

        assertFalse(recipes.isEmpty());
        verify(synthesizeRecipeMapper, times(1)).selectList(any());
        verify(userAssetMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("检查用户是否有足够材料 - 有足够材料")
    void hasEnoughMaterials_Enough() {
        when(userAssetMapper.selectOne(any())).thenReturn(testAsset1);

        boolean result = synthesizeService.hasEnoughMaterials(1L, 1L, 2);

        assertTrue(result);
        verify(userAssetMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("检查用户是否有足够材料 - 材料不足")
    void hasEnoughMaterials_Insufficient() {
        testAsset1.setQuantity(1);
        when(userAssetMapper.selectOne(any())).thenReturn(testAsset1);

        boolean result = synthesizeService.hasEnoughMaterials(1L, 1L, 2);

        assertFalse(result);
        verify(userAssetMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("检查用户是否有足够材料 - 没有该材料")
    void hasEnoughMaterials_NoMaterial() {
        when(userAssetMapper.selectOne(any())).thenReturn(null);

        boolean result = synthesizeService.hasEnoughMaterials(1L, 1L, 2);

        assertFalse(result);
        verify(userAssetMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("合成操作 - 锁获取成功")
    void synthesize_LockSuccess() throws InterruptedException {
        when(synthesizeRecipeMapper.selectById(1L)).thenReturn(testRecipe);
        when(userAssetMapper.selectOne(argThat(wrapper -> {
            try {
                return wrapper.getEntity().getItemId().equals(1L) && 
                       wrapper.getEntity().getUserId().equals(1L);
            } catch (Exception e) {
                return false;
            }
        }))).thenReturn(testAsset1);
        when(userAssetMapper.selectOne(argThat(wrapper -> {
            try {
                return wrapper.getEntity().getItemId().equals(2L) && 
                       wrapper.getEntity().getUserId().equals(1L);
            } catch (Exception e) {
                return false;
            }
        }))).thenReturn(testAsset2);
        when(userAssetMapper.selectOne(argThat(wrapper -> {
            try {
                return wrapper.getEntity().getItemId().equals(100L) && 
                       wrapper.getEntity().getUserId().equals(1L);
            } catch (Exception e) {
                return false;
            }
        }))).thenReturn(resultAsset);
        when(redissonClient.getMultiLock(any())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 60, TimeUnit.SECONDS)).thenReturn(true);
        when(userAssetMapper.updateById(any())).thenReturn(1);
        when(userAssetMapper.insert(any())).thenReturn(1);
        when(synthesizeRecordMapper.insert(any())).thenReturn(1);

        boolean result = synthesizeService.synthesize(1L, 1L);

        assertTrue(result);
        verify(mockLock, times(1)).tryLock(10, 60, TimeUnit.SECONDS);
        verify(mockLock, times(1)).unlock();
        verify(userAssetMapper, atLeastOnce()).updateById(any());
    }

    @Test
    @DisplayName("合成操作 - 锁获取失败")
    void synthesize_LockFailure() throws InterruptedException {
        when(redissonClient.getMultiLock(any())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 60, TimeUnit.SECONDS)).thenReturn(false);

        boolean result = synthesizeService.synthesize(1L, 1L);

        assertFalse(result);
        verify(mockLock, times(1)).tryLock(10, 60, TimeUnit.SECONDS);
        verify(mockLock, never()).unlock();
    }

    @Test
    @DisplayName("合成操作 - 配方不存在")
    void synthesize_RecipeNotFound() throws InterruptedException {
        when(synthesizeRecipeMapper.selectById(1L)).thenReturn(null);
        when(redissonClient.getMultiLock(any())).thenReturn(mockLock);
        when(mockLock.tryLock(10, 60, TimeUnit.SECONDS)).thenReturn(true);

        boolean result = synthesizeService.synthesize(1L, 1L);

        assertFalse(result);
        verify(mockLock, times(1)).tryLock(10, 60, TimeUnit.SECONDS);
        verify(mockLock, times(1)).unlock();
    }
}