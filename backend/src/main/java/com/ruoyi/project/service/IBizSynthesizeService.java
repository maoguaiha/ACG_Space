package com.ruoyi.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.project.domain.entity.BizSynthesizeRecipe;
import com.ruoyi.project.domain.entity.BizSynthesizeRecord;

/**
 * 合成服务接口
 */
public interface IBizSynthesizeService extends IService<BizSynthesizeRecipe> {

    /**
     * 获取启用的配方列表
     */
    Page<BizSynthesizeRecipe> pageRecipes(long pageNum, long pageSize, String name);

    /**
     * 执行合成
     * @param userId 用户ID
     * @param recipeId 配方ID
     * @return 合成结果
     */
    SynthesizeResultDTO synthesize(Long userId, Long recipeId);

    /**
     * 获取用户合成记录
     */
    Page<BizSynthesizeRecord> pageRecords(long pageNum, long pageSize, Long userId);

    /**
     * 合成结果 DTO
     */
    class SynthesizeResultDTO {
        private Boolean success;
        private String message;
        private Long assetId;
        private String itemName;
        private String itemImage;
        private String itemRarity;

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getItemImage() { return itemImage; }
        public void setItemImage(String itemImage) { this.itemImage = itemImage; }
        public String getItemRarity() { return itemRarity; }
        public void setItemRarity(String itemRarity) { this.itemRarity = itemRarity; }
    }
}