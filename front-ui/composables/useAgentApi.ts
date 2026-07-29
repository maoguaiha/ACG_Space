/**
 * AI 助手 API 工具（composable）。
 *
 * 职责：
 *   - SSE 流式对话（原生 fetch + ReadableStream，与 Python→Java→Nuxt 链路一致）
 *   - 会话 CRUD 复用项目现有 apiFetch（Bearer token 认证 + 统一 error handling）
 */
import { apiFetch } from './useApi'
import { useUserStore } from '~/stores/user'

// ======================== 类型 ========================

export interface ConversationItem {
  id: string
  title: string
  updateTime: string
  /** 是否置顶（0/1）— V2.4 千问式侧边栏 */
  pinned?: number
  /** 所属分组 ID（NULL = 最近对话未分组）— V2.4 */
  groupId?: number | null
}

/** 后端 AgentMessage 实体（与 Java AgentMessage 字段对应） */
export interface AgentMessageItem {
  id: number
  role: 'user' | 'assistant' | string
  content: string
  tokens?: number | null
  createTime?: string
}

/** 会话分组实体 — V2.4 */
export interface GroupItem {
  id: string
  name: string
  sortOrder?: number
  createTime?: string
  updateTime?: string
}

// ======================== 会话 CRUD（走 api-proxy 中间件） ========================

/** 当前用户的会话列表 */
export async function fetchConversations(): Promise<ConversationItem[]> {
  return apiFetch<ConversationItem[]>('/agent/conversations')
}

/** 查询指定会话的消息历史（按时间正序） */
export async function fetchMessages(conversationId: string): Promise<AgentMessageItem[]> {
  return apiFetch<AgentMessageItem[]>(`/agent/conversations/${conversationId}/messages`)
}

/** 新建会话，返回会话 ID（字符串，避免前端 Number 精度丢失） */
export async function createConversation(): Promise<string> {
  return apiFetch<string>('/agent/conversations', { method: 'POST' })
}

/** 删除会话（逻辑删除） */
export async function deleteConversation(id: string): Promise<boolean> {
  return apiFetch<boolean>(`/agent/conversations/${id}`, { method: 'DELETE' })
}

/** 重命名会话标题 */
export async function renameConversation(id: string, title: string): Promise<boolean> {
  return apiFetch<boolean>(`/agent/conversations/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ title }),
  })
}

/** 清空当前用户的所有会话（级联删除消息） */
export async function clearAllConversations(): Promise<boolean> {
  return apiFetch<boolean>('/agent/conversations', { method: 'DELETE' })
}

// ======================== V2.4 千问式侧边栏 ========================

/** 置顶 / 取消置顶会话 */
export async function pinConversation(id: string, pinned: boolean): Promise<boolean> {
  return apiFetch<boolean>(`/agent/conversations/${id}/pin`, {
    method: 'PUT',
    body: JSON.stringify({ pinned }),
  })
}

/** 移动会话到指定分组（groupId 为 null 表示移回最近对话） */
export async function moveConversationToGroup(id: string, groupId: number | null): Promise<boolean> {
  return apiFetch<boolean>(`/agent/conversations/${id}/group`, {
    method: 'PUT',
    body: JSON.stringify({ groupId }),
  })
}

/** 批量删除会话（仅本人会话生效），返回实际删除条数 */
export async function batchDeleteConversations(ids: string[]): Promise<number> {
  return apiFetch<number>('/agent/conversations/batch', {
    method: 'DELETE',
    body: JSON.stringify({ ids }),
  })
}

/** 当前用户的会话分组列表（按 sortOrder ASC） */
export async function fetchGroups(): Promise<GroupItem[]> {
  return apiFetch<GroupItem[]>('/agent/groups')
}

/** 新建分组，返回分组 ID（字符串） */
export async function createGroup(name: string, sortOrder?: number): Promise<string> {
  return apiFetch<string>('/agent/groups', {
    method: 'POST',
    body: JSON.stringify({ name, sortOrder: sortOrder ?? 0 }),
  })
}

/** 重命名分组 */
export async function renameGroup(id: string, name: string): Promise<boolean> {
  return apiFetch<boolean>(`/agent/groups/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ name }),
  })
}

/** 删除分组（其下会话 group_id 自动归 NULL） */
export async function deleteGroup(id: string): Promise<boolean> {
  return apiFetch<boolean>(`/agent/groups/${id}`, { method: 'DELETE' })
}

// ======================== SSE 流式对话 ========================

/**
 * SSE 流式对话——通过 Nuxt API 路由（server/api/agent/chat.post.ts）代理到 Java 门面。
 *
 * @param message        用户本轮消息
 * @param conversationId 会话 ID（首次可传 null，Java 端自动创建）
 * @param onToken        收到一个 token 时回调
 * @param onError        收到 error 帧时回调
 * @param onDone         流正常结束时回调
 * @param signal         AbortSignal，用于停止生成
 */
export async function streamChat(
  message: string,
  conversationId: string | null,
  onToken: (content: string) => void,
  onError: (content: string) => void,
  onDone: () => void,
  signal?: AbortSignal,
): Promise<void> {
  // 透传 Bearer token——与 apiFetch 一致，Java 侧通过 SecurityUtils.getUserId() 鉴权
  const userStore = useUserStore()

  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (userStore.token) {
    headers['Authorization'] = `Bearer ${userStore.token}`
  }

  const response = await fetch('/api/agent/chat', {
    method: 'POST',
    headers,
    body: JSON.stringify({ message, conversationId }),
    signal,
  })

  if (!response.ok) {
    if (response.status === 401) {
      userStore.logout()
      throw new Error('登录已过期，请重新登录')
    }
    const text = await response.text().catch(() => '')
    throw new Error(text || `服务异常 (${response.status})`)
  }

  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let finished = false  // 防止 done + 异常路径双重调用 onDone

  /** 安全调用 onDone（仅一次） */
  const finishOnce = () => {
    if (finished) return
    finished = true
    onDone()
  }

  const pump = async (): Promise<void> => {
    try {
      const { done, value } = await reader.read()
      if (done) {
        finishOnce()
        return
      }

      buffer += decoder.decode(value, { stream: true })

      // Python SSE 帧以 \n\n 分隔；保留未完整接收的尾部
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''

      for (const part of parts) {
        const line = part.trim()
        if (line.startsWith('data:')) {
          try {
            const json = JSON.parse(line.slice(5).trim())
            if (json.type === 'token' && json.content) {
              onToken(json.content)
            } else if (json.type === 'error') {
              onError(json.content || '未知错误')
            }
            // type === 'done' 由流关闭自然触发，不单独回调
          } catch {
            // 非 JSON data 帧（如 Server-Sent Events ping）忽略
          }
        }
      }

      return pump()
    } catch (e: any) {
      // 兜底：reader 抛错（如网络抖动 / proxy EOF 漏传）也必须通知调用方结束，
      // 否则 isStreaming 永远 true、停止按钮卡死、用户无法继续发消息
      finishOnce()
      throw e
    }
  }

  return pump()
}
