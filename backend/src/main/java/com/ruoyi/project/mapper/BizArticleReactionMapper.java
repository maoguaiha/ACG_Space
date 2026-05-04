package com.ruoyi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.domain.entity.BizArticleReaction;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BizArticleReactionMapper extends BaseMapper<BizArticleReaction> {

    /**
     * 原子写回：INSERT 或 ON DUPLICATE KEY UPDATE，利用 (article_id, user_id) 唯一约束
     * 解决并发点赞/点踩时的竞态条件
     */
    @Insert("INSERT INTO biz_article_reaction (id, article_id, user_id, reaction_type, reason, create_by, create_time, update_by, update_time, del_flag) "
            + "VALUES (#{id}, #{articleId}, #{userId}, #{reactionType}, #{reason}, #{createBy}, NOW(), #{updateBy}, NOW(), 0) "
            + "ON DUPLICATE KEY UPDATE reaction_type = VALUES(reaction_type), reason = VALUES(reason), update_time = NOW()")
    int upsert(BizArticleReaction reaction);

    /**
     * 查询用户对文章的反应记录（包含已逻辑删除的）
     */
    @Select("SELECT * FROM biz_article_reaction WHERE article_id = #{articleId} AND user_id = #{userId}")
    BizArticleReaction selectOneWithDeleted(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /**
     * 物理删除用户对文章的反应记录
     */
    @Delete("DELETE FROM biz_article_reaction WHERE article_id = #{articleId} AND user_id = #{userId}")
    int deletePhysically(@Param("articleId") Long articleId, @Param("userId") Long userId);
}
