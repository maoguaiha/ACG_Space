package com.ruoyi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.domain.entity.BizArticleCommentReaction;
import org.apache.ibatis.annotations.Insert;

public interface BizArticleCommentReactionMapper extends BaseMapper<BizArticleCommentReaction> {

        @Insert("INSERT INTO biz_article_comment_reaction (id, article_comment_id, user_id, reaction_type, create_time) "
            + "VALUES (#{id}, #{articleCommentId}, #{userId}, #{reactionType}, NOW()) "
            + "ON DUPLICATE KEY UPDATE reaction_type = VALUES(reaction_type)")
    int upsert(BizArticleCommentReaction reaction);
}
