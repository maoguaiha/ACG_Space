/**
 * 统一 API 请求封装
 * SSR 服务端阶段：直连后端内网地址（避免 127.0.0.1 回环问题）
 * CSR 客户端阶段：通过 Nitro devProxy 代理路径访问，解决跨域
 */

// ====== 响应体类型定义 ======

/** 后端统一响应结构 */
export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 番剧实体 */
export interface BizAnime {
  id: string
  bgmId: number
  title: string
  titleOriginal: string
  coverUrl: string
  summary: string
  totalEpisodes: number
  publishYear: number
  /** 0连载中 1已完结 2未开播 */
  status: 0 | 1 | 2
  rating: number
  createTime: string
}

/** 评论实体 */
export interface BizComment {
  id: string
  animeId: string
  userId: string
  content: string
  createTime: string
}

export interface CommentPageItem {
  id: string
  animeId: string
  userId: string
  content: string
  parentId: string
  replyToUserId?: string
  replyToNickname?: string
  likes: number
  createTime: string
  username?: string
  nickname?: string
  avatar?: string
  replyCount?: number
  replies?: CommentPageItem[]
}

/** 发表评论请求体 */
export interface CommentRequestDTO {
  animeId: string
  userId: string
  content: string
}

// ====== 核心请求方法 ======

/**
 * 获取请求基础 URL
 * 在 SSR 阶段使用内网地址，CSR 阶段使用代理路径
 */
function getBaseUrl(): string {
  const config = useRuntimeConfig()
  // import.meta.server 在 SSR 环境为 true，是 Nuxt 3 / Vite 推荐写法，不需要 @types/node
  if (import.meta.server) {
    return `${config.apiInternalBase}/api`
  }
  return config.public.apiBase
}

/**
 * SSR 请求超时时间（毫秒）
 * Railway 环境中后端可能未就绪，必须设置超时防止 SSR 无限 hanging
 */
const SSR_FETCH_TIMEOUT = 8000

/**
 * 通用 GET 请求
 * 配合 useAsyncData/useFetch 使用，支持 SSR 数据预取
 */
export async function apiFetch<T>(
  path: string,
  options?: Parameters<typeof $fetch>[1]
): Promise<T> {
  const baseUrl = getBaseUrl()
  const userStore = useUserStore()

  // SSR 阶段必须设置超时，防止后端不可达时无限 hanging
  const fetchOptions: any = {
    ...options,
    headers: {
      ...options?.headers,
      ...(userStore.token ? { 'Authorization': `Bearer ${userStore.token}` } : {})
    },
    onResponseError({ response }) {
      console.error(`[API Error] ${path} -> ${response.status}: ${response._data?.msg}`)
      if (response.status === 401) {
        userStore.logout() // Token 失效，退出登录
      }
    }
  }

  // Nitro $fetch 在服务端基于 ofetch，ofetch 支持 timeout 选项
  if (import.meta.server) {
    fetchOptions.timeout = SSR_FETCH_TIMEOUT
    // retry 设为 0，避免超时后自动重试延长 hang 时间
    fetchOptions.retry = 0
  }

  const result = await $fetch<ApiResult<T>>(`${baseUrl}${path}`, fetchOptions)

  if (result.code !== 200) {
    throw new Error(result.msg || '服务端返回错误')
  }
  return result.data
}

// ====== 番剧相关 API ======

/** 获取番剧列表（社区番剧库全量） */
export function fetchAnimeList() {
  return apiFetch<BizAnime[]>('/anime/list')
}

export interface AnimeLibraryPageQuery {
  pageNum: number
  pageSize: number
  title?: string
  status?: 0 | 1 | 2
  publishYear?: number
  genres?: string[]
  sortBy?: 'default' | 'rating' | 'year'
}

/** 获取社区番剧库分页列表 */
export function fetchAnimeLibraryPage(query: AnimeLibraryPageQuery) {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum))
  params.set('pageSize', String(query.pageSize))
  if (query.title) params.set('title', query.title)
  if (query.status !== undefined) params.set('status', String(query.status))
  if (query.publishYear !== undefined) params.set('publishYear', String(query.publishYear))
  if (query.genres && query.genres.length > 0) params.set('genres', query.genres.join(','))
  if (query.sortBy) params.set('sortBy', query.sortBy)
  return apiFetch<PageResult<BizAnime>>(`/anime/library/page?${params.toString()}`)
}

/** 获取当前用户信息 */
export function fetchMe() {
  return apiFetch<any>('/auth/me')
}

/** 获取番剧详情 */
export function fetchAnimeDetail(id: string) {
  return apiFetch<BizAnime>(`/anime/${id}`)
}

/** 根据 Bangumi ID 获取番剧详情 */
export function fetchAnimeDetailByBgmId(bgmId: number) {
  return apiFetch<BizAnime>(`/anime/bgm/${bgmId}`)
}

/**
 * 批量从 Bangumi 导入番剧到本地库
 */
export function importFromBangumi(bgmIds: number[]) {
  return apiFetch<BizAnime[]>('/anime/import', {
    method: 'POST',
    body: bgmIds
  })
}

/** 获取 Bangumi 每日放送表 (新番时间表) */
export function fetchBangumiCalendar() {
  // 返回类型定义为 any 数组，因为 Bangumi 数据结构较复杂
  return apiFetch<any[]>('/anime/calendar')
}

/** 获取首页轮播推荐番剧列表 */
export function fetchFeaturedAnime() {
  return apiFetch<BizAnime[]>('/anime/featured')
}

/** 在 Bangumi 中搜索番剧 */
export function searchBangumi(keywords: string) {
  return apiFetch<any>(`/anime/bangumi/search?keywords=${encodeURIComponent(keywords)}`)
}

// ====== 认证相关 API ======

/** 登录 */
export function loginApi(data: any) {
  return apiFetch<{ token: string }>('/auth/login', {
    method: 'POST',
    body: data
  })
}

/** 注册 */
export function registerApi(data: any) {
  return apiFetch<void>('/auth/register', {
    method: 'POST',
    body: data
  })
}

/** 获取个人信息 */
export function fetchUserInfo() {
  return apiFetch<any>('/auth/me')
}

// ====== 追番相关 API ======

/** 切换追番状态 */
export function toggleFollowApi(animeId: string) {
  return apiFetch<boolean>(`/follow/${animeId}`, {
    method: 'POST'
  })
}

/** 获取追番状态 */
export function fetchFollowStatus(animeId: string) {
  return apiFetch<boolean>(`/follow/status/${animeId}`)
}

/** 获取追番列表 */
export function fetchFollowList() {
  return apiFetch<BizAnime[]>('/follow/list')
}

/** 切换追番状态 (根据 Bangumi ID) */
export function toggleFollowBangumiApi(bgmId: number) {
  return apiFetch<boolean>(`/follow/bangumi/${bgmId}`, {
    method: 'POST'
  })
}

/** 获取追番状态 (根据 Bangumi ID) */
export function fetchFollowStatusByBgmId(bgmId: number) {
  return apiFetch<boolean>(`/follow/status/bangumi/${bgmId}`)
}

// ====== 评论相关 API ======

/** 发表评论 */
export function publishComment(dto: CommentRequestDTO) {
  return apiFetch<BizComment>('/comment/publish', {
    method: 'POST',
    body: dto
  })
}

/** 分页查询评论 */
export function fetchCommentPage(animeId: string, pageNum = 1, pageSize = 10) {
  return apiFetch<PageResult<CommentPageItem>>(
    `/comment/page?animeId=${encodeURIComponent(animeId)}&pageNum=${pageNum}&pageSize=${pageSize}`
  )
}

// ====== 文章相关 API ======

/** 文章列表项 VO */
export interface ArticleListItem {
  id: string
  title: string
  summary: string
  coverUrl: string
  authorId: string
  authorNickname: string
  authorAvatar: string
  category: string
  tags: string
  viewCount: number
  likeCount: number
  dislikeCount: number
  commentCount: number
  status: number
  isVipOnly: number
  isFeatured: number
  createTime: string
}

/** 文章详情 VO */
export interface ArticleDetail {
  id: string
  title: string
  summary: string
  content: string
  coverUrl: string
  authorId: string
  authorNickname: string
  authorAvatar: string
  category: string
  tags: string
  viewCount: number
  likeCount: number
  dislikeCount: number
  commentCount: number
  status: number
  isVipOnly: number
  isFeatured: number
  userReaction?: number // 1点赞 2点踩
  createTime: string
  updateTime: string
}

/** 文章反应请求 */
export interface ArticleReactionRequest {
  reactionType: number // 1点赞 2点踩
  reason?: string // 点踩理由
}

/** 文章反应 */
export function reactArticle(id: string | bigint, request: ArticleReactionRequest) {
  return apiFetch<number | null>(`/article/${id}/react`, {
    method: 'POST',
    body: request
  })
}

/** 获取用户对文章的反应状态 */
export function getArticleReactionStatus(id: string) {
  return apiFetch<number | null>(`/article/${id}/reaction-status`)
}

/** 获取文章列表 */
export function fetchArticleList(query: { pageNum?: number; pageSize?: number; keyword?: string; category?: string; sortBy?: string }) {
  const params = new URLSearchParams()
  params.set('pageNum', String(query.pageNum || 1))
  params.set('pageSize', String(query.pageSize || 10))
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.category) params.set('category', query.category)
  if (query.sortBy) params.set('sortBy', query.sortBy)
  return apiFetch<PageResult<ArticleListItem>>(`/article/list?${params.toString()}`)
}

/** 获取文章详情 */
export function fetchArticleDetail(id: string | bigint) {
  return apiFetch<ArticleDetail>(`/article/${id}`)
}

/** 创建文章 */
export interface CreateArticleRequest {
  title: string
  summary: string
  content: string
  coverUrl?: string
  category?: string
  tags?: string
  status?: number
}

export function createArticle(data: CreateArticleRequest) {
  return apiFetch<string>('/article/create', {
    method: 'POST',
    body: data
  })
}

/** 文章评论请求体 */
export interface ArticleCommentRequestDTO {
  articleId: string
  userId: number
  content: string
  parentId?: number
}

/** 文章评论项 VO */
export interface ArticleCommentVO {
  id: string
  articleId: string
  userId: number
  content: string
  parentId: number
  replyToUserId?: number
  replyToNickname?: string
  likes: number
  createTime: string
  username?: string
  nickname?: string
  avatar?: string
  replyCount?: number
  replies?: ArticleCommentVO[]
}

/** 发布文章评论 */
export function publishArticleComment(dto: ArticleCommentRequestDTO) {
  return apiFetch<any>('/article/comment/publish', {
    method: 'POST',
    body: dto
  })
}

/** 分页查询文章评论 */
export function fetchArticleCommentPage(articleId: string, pageNum = 1, pageSize = 10) {
  return apiFetch<PageResult<ArticleCommentVO>>(
    `/article/comment/page?articleId=${articleId}&pageNum=${pageNum}&pageSize=${pageSize}`
  )
}

/** 搜索文章（社区页用） */
export function searchArticles(keyword: string, pageNum = 1, pageSize = 10, sortBy = 'time') {
  const params = new URLSearchParams()
  params.set('pageNum', String(pageNum))
  params.set('pageSize', String(pageSize))
  params.set('sortBy', sortBy)
  if (keyword) params.set('keyword', keyword)
  return apiFetch<PageResult<ArticleListItem>>(`/article/list?${params.toString()}`)
}

/** 获取文章分类列表 */
export function fetchArticleCategories() {
  return apiFetch<string[]>('/article/categories')
}

// ====== 评论回复 API ======

/** 回复评论请求体 */
export interface CommentReplyRequest {
  commentId: string
  userId: string
  content: string
  replyToUserId?: string
  replyToNickname?: string
}

/** 回复一条评论 */
export function replyComment(dto: CommentReplyRequest) {
  return apiFetch<any>('/comment/reply', { method: 'POST', body: dto })
}

/** 获取评论下的所有回复 */
export function fetchCommentReplies(commentId: string) {
  return apiFetch<CommentPageItem[]>(`/comment/${commentId}/replies`)
}

/** 回复一条文章评论 */
export function replyArticleComment(dto: CommentReplyRequest) {
  return apiFetch<any>('/article/comment/reply', { method: 'POST', body: dto })
}

/** 获取文章评论下的所有回复 */
export function fetchArticleCommentReplies(commentId: string) {
  return apiFetch<ArticleCommentVO[]>(`/article/comment/${commentId}/replies`)
}

// ====== 评论点赞点踩 API ======

/** 点赞/点踩番剧评论 */
export function reactComment(commentId: string, reactionType: number) {
  return apiFetch<void>(`/comment/${commentId}/react`, {
    method: 'POST',
    body: { reactionType }
  })
}

/** 点赞/点踩文章评论 */
export function reactArticleComment(commentId: string, reactionType: number) {
  return apiFetch<number | null>(`/article/comment/${commentId}/react`, {
    method: 'POST',
    body: { reactionType }
  })
}

/** 获取用户对番剧评论的反应状态 */
export function getCommentReactionStatus(commentId: string) {
  return apiFetch<number | null>(`/comment/${commentId}/reaction-status`)
}

/** 获取用户对文章评论的反应状态 */
export function getArticleCommentReactionStatus(commentId: string) {
  return apiFetch<number | null>(`/article/comment/${commentId}/reaction-status`)
}

// ====== 用户资料 & 社区相关 API ======

/** 用户公开资料 */
export interface UserProfile {
  id: string
  username: string
  nickname: string
  avatar: string
  email: string
  bio: string
  points: number
  followerCount: number
  followingCount: number
  vipStatus: number
  vipExpireTime: string
  userLevel: number
  levelExperience: number
  isSelf: boolean
  isFollowed: boolean
}

/** 获取用户公开资料 */
export function fetchUserProfile(id: string) {
  return apiFetch<UserProfile>(`/user/${id}/profile`)
}

/** 编辑当前用户资料 */
export function updateUserProfile(data: { nickname?: string; avatar?: string; bio?: string; email?: string }) {
  return apiFetch<void>('/user/profile', { method: 'PUT', body: data })
}

/** 关注/取关用户 */
export function toggleFollowUser(targetUserId: string) {
  return apiFetch<boolean>(`/user/follow/${targetUserId}`, { method: 'POST' })
}

/** 搜索用户 */
export function searchUsers(keyword: string, pageNum = 1, pageSize = 20) {
  const params = new URLSearchParams()
  params.set('pageNum', String(pageNum))
  params.set('pageSize', String(pageSize))
  if (keyword) params.set('keyword', keyword)
  return apiFetch<PageResult<UserProfile>>(`/user/search?${params.toString()}`)
}

/** 查看某用户发布的文章 */
export function fetchUserArticles(userId: string, pageNum = 1, pageSize = 10) {
  return apiFetch<PageResult<ArticleListItem>>(`/user/${userId}/articles?pageNum=${pageNum}&pageSize=${pageSize}`)
}

/** 用户评论项 */
export interface UserCommentItem {
  id: string
  content: string
  likes: number
  createTime: string
  type: number // 1-番剧评论，2-文章评论
  targetId: string
  targetTitle: string
  targetCover: string
}

/** 查看某用户发布的评论 */
export function fetchUserComments(userId: string, pageNum = 1, pageSize = 10) {
  return apiFetch<PageResult<UserCommentItem>>(`/user/${userId}/comments?pageNum=${pageNum}&pageSize=${pageSize}`)
}

/** 获取某用户的粉丝列表 */
export function fetchUserFollowers(userId: string) {
  return apiFetch<UserProfile[]>(`/user/${userId}/followers`)
}

/** 获取某用户的关注列表 */
export function fetchUserFollowing(userId: string) {
  return apiFetch<UserProfile[]>(`/user/${userId}/following`)
}

/** 查看某用户的追番列表 */
export function fetchUserFollows(userId: string, pageNum = 1, pageSize = 10) {
  return apiFetch<any[]>(`/user/${userId}/follows?pageNum=${pageNum}&pageSize=${pageSize}`)
}

/** 用户点赞历史项 */
export interface UserLikeHistoryItem {
  id: string
  type: number // 1-番剧评论点赞，2-文章评论点赞
  targetId: string
  targetTitle: string
  targetCover: string
  createTime: string
}

/** 查看某用户的点赞历史 */
export function fetchUserLikes(userId: string, pageNum = 1, pageSize = 20) {
  return apiFetch<PageResult<UserLikeHistoryItem>>(`/user/${userId}/likes?pageNum=${pageNum}&pageSize=${pageSize}`)
}

// ============ 私信相关 ============

export interface MessageVO {
  id: string
  fromUserId: string
  fromUsername: string
  fromNickname: string
  fromAvatar: string
  toUserId: string
  toUsername: string
  toNickname: string
  toAvatar: string
  content: string
  isRead: boolean
  createTime: string
}

export interface ConversationVO {
  userId: string
  username: string
  nickname: string
  avatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

/** 获取会话列表 */
export function fetchConversationList() {
  return apiFetch<ConversationVO[]>('/message/list')
}

/** 获取与某用户的聊天记录 */
export function fetchConversation(userId: string, page = 1, size = 50) {
  return apiFetch<MessageVO[]>(`/message/conversation/${userId}?page=${page}&size=${size}`)
}

/** 发送私信 */
export function sendMessage(toUserId: string, content: string) {
  return apiFetch('/message/send', {
    method: 'POST',
    body: JSON.stringify({ toUserId, content })
  })
}

/** 标记消息已读 */
export function markMessagesRead(userId: string) {
  return apiFetch(`/message/read/${userId}`, { method: 'PUT' })
}

/** 获取未读消息数 */
export function fetchUnreadCount() {
  return apiFetch<number>('/message/unread')
}

/** 领取注册积分奖励 */
export function claimRegistrationBonus() {
  return apiFetch<any>('/message/claim-bonus', { method: 'POST' })
}