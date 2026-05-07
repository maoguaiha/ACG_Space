package com.ruoyi.project.domain.dto;

import lombok.Data;

/**
 * 奖池奖品DTO
 */
@Data
public class GachaPrizeDTO {
    private Long id;
    private Long poolId;
    private String itemName;
    private String itemImage;
    private String rarity;
    private Integer quantity;
    private Integer weight;
}