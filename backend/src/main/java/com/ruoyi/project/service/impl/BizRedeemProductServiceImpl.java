package com.ruoyi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.project.common.utils.LuaScriptExecutor;
import com.ruoyi.project.common.utils.SecurityUtils;
import com.ruoyi.project.domain.entity.BizRedeemOrder;
import com.ruoyi.project.domain.entity.BizRedeemProduct;
import com.ruoyi.project.domain.entity.BizUserAsset;
import com.ruoyi.project.domain.entity.BizUserFragment;
import com.ruoyi.project.mapper.BizRedeemOrderMapper;
import com.ruoyi.project.mapper.BizRedeemProductMapper;
import com.ruoyi.project.mapper.BizUserAssetMapper;
import com.ruoyi.project.mapper.BizUserFragmentMapper;
import com.ruoyi.project.service.IBizRedeemOrderService;
import com.ruoyi.project.service.IBizRedeemProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizRedeemProductServiceImpl extends ServiceImpl<BizRedeemProductMapper, BizRedeemProduct> implements IBizRedeemProductService {

    private final BizUserAssetMapper userAssetMapper;
    private final BizUserFragmentMapper userFragmentMapper;
    private final LuaScriptExecutor luaScriptExecutor;
    private final IBizRedeemOrderService redeemOrderService;

    @Override
    public Page<BizRedeemProduct> pageProducts(long pageNum, long pageSize, Integer status) {
        log.info("Service层查询: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        LambdaQueryWrapper<BizRedeemProduct> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BizRedeemProduct::getStatus, status);
            log.info("添加状态过滤: status={}", status);
        }
        wrapper.orderByAsc(BizRedeemProduct::getSortOrder)
               .orderByDesc(BizRedeemProduct::getCreateTime);
        Page<BizRedeemProduct> result = page(new Page<>(pageNum, pageSize), wrapper);
        log.info("查询结果: 记录数={}, 总数={}", result.getRecords().size(), result.getTotal());
        // 打印第一条记录的详细信息
        if (!result.getRecords().isEmpty()) {
            BizRedeemProduct first = result.getRecords().get(0);
            log.info("第一条记录: id={}, name={}, image={}, status={}, delFlag={}", 
                    first.getId(), first.getName(), 
                    first.getImage() != null ? first.getImage().substring(0, Math.min(50, first.getImage().length())) : "null",
                    first.getStatus(), first.getDelFlag());
        }
        return result;
    }

    @Override
    public List<BizRedeemProduct> getActiveProducts() {
        return list(new LambdaQueryWrapper<BizRedeemProduct>()
                .eq(BizRedeemProduct::getStatus, 1)
                .orderByAsc(BizRedeemProduct::getSortOrder)
                .orderByDesc(BizRedeemProduct::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean redeemProduct(Long userId, Long productId, String receiver, String phone, String province, String city, String district, String address) {
        log.info("========== 开始兑换流程 ==========");
        log.info("兑换参数: userId={}, productId={}, receiver={}, phone={}", userId, productId, receiver, phone);
        
        log.info("【商品查询】尝试通过ID查询商品: productId={}", productId);
        BizRedeemProduct product = getById(productId);
        log.info("【商品查询】查询结果: product={}, id={}, name={}, status={}, stock={}, delFlag={}", 
                product != null ? "存在" : "不存在",
                product != null ? product.getId() : "null",
                product != null ? product.getName() : "null", 
                product != null ? product.getStatus() : "null",
                product != null ? product.getStock() : "null",
                product != null ? product.getDelFlag() : "null");
        
        if (product == null) {
            log.error("【商品查询】商品不存在，productId={}", productId);
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() != 1) {
            log.error("【商品查询】商品已下架: status={}", product.getStatus());
            throw new RuntimeException("商品已下架");
        }
        if (product.getStock() != null && product.getStock() <= 0) {
            log.error("【商品查询】商品库存不足: stock={}", product.getStock());
            throw new RuntimeException("商品库存不足");
        }

        int urFragmentCost = product.getUrFragmentCost() != null ? product.getUrFragmentCost() : 0;
        int pointsCost = product.getPointsCost() != null ? product.getPointsCost() : 0;
        log.info("兑换成本: urFragmentCost={}, pointsCost={}", urFragmentCost, pointsCost);

        if (urFragmentCost == 0 && pointsCost == 0) {
            throw new RuntimeException("商品兑换条件未设置");
        }

        if (urFragmentCost > 0) {
            log.info("【步骤1】开始扣除UR碎片: userId={}, cost={}", userId, urFragmentCost);
            
            BizUserAsset fragmentAsset = userAssetMapper.selectOne(new LambdaQueryWrapper<BizUserAsset>()
                    .eq(BizUserAsset::getUserId, userId)
                    .eq(BizUserAsset::getItemRarity, "UR")
                    .eq(BizUserAsset::getItemType, "fragment")
                    .eq(BizUserAsset::getStatus, 1)
                    .eq(BizUserAsset::getDelFlag, 0));

            if (fragmentAsset == null) {
                log.error("【步骤1-失败】未找到UR碎片资产: userId={}", userId);
                throw new RuntimeException("UR碎片不足，需要 " + urFragmentCost + " 个");
            }
            
            log.info("【步骤1-成功】找到UR碎片资产: assetId={}, quantity={}, status={}", 
                    fragmentAsset.getId(), fragmentAsset.getQuantity(), fragmentAsset.getStatus());
            
            if (fragmentAsset.getQuantity() == null || fragmentAsset.getQuantity() < urFragmentCost) {
                log.error("【步骤1-失败】碎片数量不足: need={}, has={}", 
                        urFragmentCost, fragmentAsset.getQuantity());
                throw new RuntimeException("UR碎片不足，需要 " + urFragmentCost + " 个，当前有 " + (fragmentAsset.getQuantity() != null ? fragmentAsset.getQuantity() : 0) + " 个");
            }

            int newQty = fragmentAsset.getQuantity() - urFragmentCost;
            log.info("【步骤2】准备更新碎片数量: oldQty={}, newQty={}, setStatus={}", 
                    fragmentAsset.getQuantity(), newQty, newQty <= 0 ? 4 : "不变");
            
            if (newQty <= 0) {
                fragmentAsset.setStatus(4);
                fragmentAsset.setQuantity(0);
                log.info("【步骤2】碎片用完，设置status=4, quantity=0");
            } else {
                fragmentAsset.setQuantity(newQty);
                log.info("【步骤2】设置新数量: {}", newQty);
            }
            fragmentAsset.setUpdateTime(LocalDateTime.now());
            
            log.info("【步骤3】执行数据库更新: assetId={}", fragmentAsset.getId());
            int updateResult = userAssetMapper.updateById(fragmentAsset);
            log.info("【步骤3-结果】更新影响行数: rows={}", updateResult);
            
            if (updateResult <= 0) {
                log.error("【步骤3-失败】数据库更新失败，影响行数为0");
                throw new RuntimeException("扣除UR碎片失败，请稍后重试");
            }
            
            log.info("【步骤4】验证更新结果: 重新查询数据库");
            BizUserAsset verifyAsset = userAssetMapper.selectById(fragmentAsset.getId());
            if (verifyAsset != null) {
                log.info("【步骤4-验证】更新后数量: quantity={}, status={}", 
                        verifyAsset.getQuantity(), verifyAsset.getStatus());
            } else {
                log.error("【步骤4-验证】资产不存在！");
            }
        } else {
            log.info("【步骤1】跳过UR碎片扣除: urFragmentCost=0");
        }

        if (pointsCost > 0) {
            long currentPoints = luaScriptExecutor.getUserPoints(userId);
            if (currentPoints < pointsCost) {
                throw new RuntimeException("积分不足，需要 " + pointsCost + " 积分");
            }
            luaScriptExecutor.deductUserPoints(userId, pointsCost);
        }

        BizRedeemOrder order = new BizRedeemOrder();
        order.setOrderNo("RD" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000)));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setProductImage(product.getImage());
        order.setUrFragmentCost(urFragmentCost);
        order.setPointsCost(pointsCost);
        order.setReceiver(receiver);
        order.setPhone(phone);
        order.setProvince(province);
        order.setCity(city);
        order.setDistrict(district);
        order.setAddress(address);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDelFlag(0);
        redeemOrderService.save(order);

        product.setExchangedCount((product.getExchangedCount() != null ? product.getExchangedCount() : 0) + 1);
        if (product.getStock() != null) {
            product.setStock(product.getStock() - 1);
        }
        product.setUpdateTime(LocalDateTime.now());
        updateById(product);

        log.info("用户 {} 兑换商品 {} 成功，消耗UR碎片 {} 个，积分 {} 个", userId, product.getName(), urFragmentCost, pointsCost);
        return true;
    }
}
