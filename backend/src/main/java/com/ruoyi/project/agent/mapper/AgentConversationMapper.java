package com.ruoyi.project.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.agent.domain.entity.AgentConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 助手会话 Mapper（注解式，无 XML）。
 */
@Mapper
public interface AgentConversationMapper extends BaseMapper<AgentConversation> {
}
