package com.ruoyi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.domain.entity.BizCommentReaction;
import org.apache.ibatis.annotations.Insert;

public interface BizCommentReactionMapper extends BaseMapper<BizCommentReaction> {

    @Insert("INSERT INTO biz_comment_reaction (id, comment_id, user_id, reaction_type, create_time) "
            + "VALUES (#{id}, #{commentId}, #{userId}, #{reactionType}, NOW()) "
            + "ON DUPLICATE KEY UPDATE reaction_type = VALUES(reaction_type)")
    int upsert(BizCommentReaction reaction);
}
