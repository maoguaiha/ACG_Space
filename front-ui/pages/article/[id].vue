<template>
  <div class="min-h-screen">
    <div v-if="pending" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center">
      <div class="w-12 h-12 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin mb-4" style="border-top-color: var(--accent, #6366F1); border-color: rgba(99, 102, 241, 0.15);"></div>
      <p class="text-slate-400">加载文章中…</p>
    </div>

    <div v-else-if="error || !article" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-6 text-slate-600"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <h2 class="text-xl font-bold mb-2 text-slate-200">文章不存在或加载失败</h2>
      <p class="text-slate-500 mb-6">{{ error?.message || '无法获取文章数据' }}</p>
      <NuxtLink to="/community" class="px-6 py-2 rounded-full transition-colors force-white" style="background: var(--hero-btn-bg, rgba(99, 102, 241, 0.8)); color: var(--text-main, #fff);">
        返回社区
      </NuxtLink>
    </div>

    <template v-else>
      <!-- 文章封面 -->
      <div class="relative w-full h-[350px] overflow-hidden">
        <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" class="w-full h-full object-cover" />
        <div class="absolute inset-0 theme-article-cover" :style="{ background: 'var(--hero-overlay-gradient, linear-gradient(to top, rgba(15,23,42,0.9), rgba(15,23,42,0.3), transparent))' }"></div>
      </div>

      <div class="container mx-auto px-4 py-8">
        <div class="max-w-4xl mx-auto">
          <div class="mb-6">
            <NuxtLink to="/community" class="inline-flex items-center gap-2 text-sm transition-colors" :class="['theme-back-link']">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
              返回社区
            </NuxtLink>
          </div>

          <div class="backdrop-blur-xl rounded-3xl border p-8 md:p-12" :class="['theme-article-card']">
            <!-- 互动区域 -->
            <div class="flex items-center justify-between pb-6 border-b mb-8" :class="['theme-tab-nav']">
              <NuxtLink :to="`/user/${article.authorId}`" class="flex items-center gap-4 group">
                <div class="w-12 h-12 rounded-full overflow-hidden group-hover:ring-2 transition-all duration-200" :class="['theme-avatar-placeholder']">
                  <img v-if="article.authorAvatar" :src="article.authorAvatar" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full flex items-center justify-center text-white font-bold text-lg">
                    {{ (article.authorNickname || '匿')[0] }}
                  </div>
                </div>
                <div>
                  <div class="font-bold transition-colors" :class="['theme-author-name']">{{ article.authorNickname || '匿名用户' }}</div>
                  <div class="text-sm" :class="['theme-author-meta']">{{ formatDate(article.createTime) }}</div>
                </div>
              </NuxtLink>
              <div class="flex items-center gap-4">
                <span class="flex items-center gap-2 text-sm px-3 py-2 rounded-xl" :class="['theme-view-count']">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  {{ article.viewCount || 0 }}
                </span>
              </div>
            </div>

            <div class="prose prose-lg max-w-none">
              <div class="leading-relaxed" :class="['theme-article-content']" v-html="article.content || article.summary || '<p>暂无内容</p>'"></div>
            </div>

            <div v-if="article.tags" class="mt-8 pt-8 border-t" :class="['theme-tab-nav']">
              <div class="flex items-center gap-3 flex-wrap">
                <span class="text-sm font-medium" :class="['theme-text-muted']">标签：</span>
                <span
                  v-for="tag in article.tags.split(',')"
                  :key="tag"
                  class="px-4 py-2 text-sm rounded-xl border transition-all duration-200" :class="['theme-badge-tag']"
                >
                  {{ tag.trim() }}
                </span>
              </div>
            </div>

            <div class="mt-8 flex items-center gap-4">
              <button
                @click="handleArticleReaction(1)"
                :disabled="reactionLoading"
                class="group flex items-center gap-2 px-6 py-3 rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                :class="articleReaction === 1 ? 'theme-btn-like-lg active' : 'theme-btn-like-lg'"
              >
                <svg class="w-5 h-5 transition-transform group-hover:scale-110" :fill="articleReaction === 1 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                </svg>
                <span class="font-medium">{{ articleReaction === 1 ? '已赞' : '赞' }}</span>
                <span class="text-sm opacity-75">{{ article.likeCount || 0 }}</span>
              </button>
              <button
                @click="handleArticleReaction(2)"
                :disabled="reactionLoading"
                class="group flex items-center gap-2 px-6 py-3 rounded-xl transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                :class="articleReaction === 2 ? 'theme-btn-dislike-lg active' : 'theme-btn-dislike-lg'"
              >
                <svg class="w-5 h-5 transition-transform group-hover:scale-110" :fill="articleReaction === 2 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h-4.764a2 2 0 01-1.789-2.894l3.5-7A2 2 0 018.737 3h4.018a2 2 0 01.485.06l3.76.94m-7 10v5a2 2 0 002 2h.096c.5 0 .905-.405.905-.904 0-.715.211-1.413.608-2.008L17 13V4m-7 10h2m5-10h2a2 2 0 012 2v6a2 2 0 01-2 2h-2.5" />
                </svg>
                <span class="font-medium">{{ articleReaction === 2 ? '已踩' : '踩' }}</span>
                <span class="text-sm opacity-75">{{ article.dislikeCount || 0 }}</span>
              </button>
              <button
                @click="shareArticle"
                class="ml-auto flex items-center gap-2 px-6 py-3 rounded-xl transition-all duration-200" :class="['theme-btn-share']"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
                <span class="font-medium">分享</span>
              </button>
            </div>

            <!-- 文章点踩理由弹窗 -->
            <Teleport to="body">
              <div v-if="showDislikeDialog" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[100]" style="top: 0; left: 0;" @click.self="showDislikeDialog = false">
                <div class="bg-slate-800 rounded-2xl p-6 w-96 border border-slate-700 shadow-2xl" style="position: relative;">
                  <h3 class="text-lg font-bold text-white mb-4">选择点踩理由</h3>
                  <div class="space-y-2">
                    <button
                      v-for="reason in dislikeReasons"
                      :key="reason"
                      @click="confirmDislike(reason)"
                      class="w-full text-left px-4 py-3 rounded-xl text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
                      :class="selectedReason === reason ? 'theme-btn-filter-active' : ''"
                    >
                      {{ reason }}
                    </button>
                  </div>
                  <div class="mt-4 flex justify-end">
                    <button @click="cancelDislike" class="px-4 py-2 text-slate-400 hover:text-white transition-colors">取消</button>
                  </div>
                </div>
              </div>
            </Teleport>

            <!-- 评论点踩理由弹窗 -->
            <Teleport to="body">
              <div v-if="showCommentDislikeDialog" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[100]" style="top: 0; left: 0;" @click.self="showCommentDislikeDialog = false">
                <div class="bg-slate-800 rounded-2xl p-6 w-96 border border-slate-700 shadow-2xl" style="position: relative;">
                  <h3 class="text-lg font-bold text-white mb-4">选择点踩理由</h3>
                  <div class="space-y-2">
                    <button
                      v-for="reason in dislikeReasons"
                      :key="reason"
                      @click="confirmCommentDislike(reason)"
                      class="w-full text-left px-4 py-3 rounded-xl text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
                    >
                      {{ reason }}
                    </button>
                  </div>
                  <div class="mt-4 flex justify-end">
                    <button @click="cancelCommentDislike" class="px-4 py-2 text-slate-400 hover:text-white transition-colors">取消</button>
                  </div>
                </div>
              </div>
            </Teleport>
          </div>

          <div class="mt-8 rounded-3xl border p-8" :class="['theme-comment-card']">
            <h3 class="text-xl font-bold mb-6" :class="['theme-comment-title']">评论区</h3>

            <div v-if="!userStore.isLoggedIn" class="text-center py-8">
              <p class="mb-4" :class="['theme-text-muted']">登录后参与讨论</p>
              <NuxtLink to="/login" class="px-6 py-2 rounded-xl transition-colors" :class="['theme-btn-primary']">
                前往登录
              </NuxtLink>
            </div>

            <div v-else>
              <div class="flex gap-4">
                <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="w-10 h-10 rounded-full" />
                <div v-else class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold" :class="['theme-avatar-placeholder']">
                  {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U')[0] }}
                </div>
                <div class="flex-1">
                  <textarea
                    v-model="commentContent"
                    placeholder="写下你的评论..."
                    rows="3"
                    class="w-full rounded-xl px-4 py-3 resize-none focus:outline-none"
                    :class="['theme-comment-input']"
                    maxlength="500"
                  ></textarea>
                  <div class="mt-3 flex items-center justify-between">
                    <span class="text-xs" :class="['theme-text-muted']">{{ commentContent.length }}/500</span>
                    <button
                      @click="submitComment"
                      :disabled="!commentContent.trim() || submitting || commentContent.length < 5"
                      class="px-6 py-2 rounded-xl transition-colors"
                      :class="['theme-btn-primary', { 'theme-btn-disabled': !commentContent.trim() || submitting || commentContent.length < 5 }]"
                    >
                      {{ submitting ? '发送中...' : '发表评论' }}
                    </button>
                  </div>
                  <p v-if="commentError" class="text-xs text-red-500 mt-2">{{ commentError }}</p>
                  <p v-if="commentSuccess" class="text-xs text-green-500 mt-2">评论发表成功！</p>
                </div>
              </div>
            </div>

            <div v-if="commentsLoading" class="mt-8 space-y-4">
              <div v-for="i in 3" :key="i" class="animate-pulse">
                <div class="h-12 rounded w-12 mb-2" :class="['theme-skeleton']"></div>
                <div class="h-4 rounded w-3/4 mb-2" :class="['theme-skeleton']"></div>
                <div class="h-4 rounded w-1/2" :class="['theme-skeleton']"></div>
              </div>
            </div>

            <div v-else-if="comments.length === 0" class="mt-8 text-center py-8" :class="['theme-text-muted']">
              暂无评论，来发表第一条评论吧
            </div>

            <div v-else class="mt-8 space-y-6">
              <div v-for="comment in comments" :key="comment.id" class="flex gap-4">
                <NuxtLink :to="`/user/${comment.userId}`" class="shrink-0">
                  <img v-if="comment.avatar" :src="comment.avatar" class="w-10 h-10 rounded-full transition-all" :class="['theme-comment-avatar-hover']" />
                  <div v-else class="w-10 h-10 rounded-full flex items-center justify-center font-bold transition-all" :class="['theme-comment-avatar-default', 'theme-comment-avatar-hover']">
                    {{ (comment.nickname || comment.username || 'U')[0] }}
                  </div>
                </NuxtLink>
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="font-bold" :class="['theme-text-main']">{{ comment.nickname || comment.username || '匿名用户' }}</span>
                    <span class="text-xs" :class="['theme-text-muted']">{{ formatDate(comment.createTime) }}</span>
                  </div>
                  <p class="mb-2" :class="['theme-comment-content']">{{ comment.content }}</p>
                  <!-- 操作区 -->
                  <div class="flex items-center gap-5 text-sm">
                    <button
                      @click="handleCommentReaction(comment.id.toString(), 1)"
                      class="group flex items-center gap-1.5 transition-colors"
                      :class="commentReactionStatus[comment.id.toString()] === 1 ? 'text-rose-400' : 'text-slate-500 hover:text-rose-400'"
                      :style="commentReactionStatus[comment.id.toString()] === 1 ? { color: 'var(--accent, #EC4899)' } : {}"
                    >
                      <svg class="w-4 h-4 transition-transform group-hover:scale-110" :fill="commentReactionStatus[comment.id.toString()] === 1 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                      </svg>
                      <span>{{ comment.likes || 0 }}</span>
                    </button>
                    <button
                      @click="handleCommentReaction(comment.id.toString(), 2)"
                      class="group flex items-center gap-1.5 transition-colors"
                      :class="commentReactionStatus[comment.id.toString()] === 2 ? 'text-slate-400' : 'text-slate-500 hover:text-slate-400'"
                      :style="commentReactionStatus[comment.id.toString()] === 2 ? { color: 'var(--text-muted, #94a3b8)' } : {}"
                    >
                      <svg class="w-4 h-4 transition-transform group-hover:scale-110" :fill="commentReactionStatus[comment.id.toString()] === 2 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h-4.764a2 2 0 01-1.789-2.894l3.5-7A2 2 0 018.737 3h4.018a2 2 0 01.485.06l3.76.94m-7 10v5a2 2 0 002 2h.096c.5 0 .905-.405.905-.904 0-.715.211-1.413.608-2.008L17 13V4m-7 10h2m5-10h2a2 2 0 012 2v6a2 2 0 01-2 2h-2.5" />
                      </svg>
                      <span>{{ comment.dislikes || 0 }}</span>
                    </button>
                    <button @click="toggleReplyInput(comment)" class="group flex items-center gap-1.5 transition-colors" :class="['theme-reply-btn']">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                      <span>{{ comment.replyCount || 0 }}</span>
                    </button>
                  </div>
                  <!-- 回复输入框 -->
                  <div v-if="replyTargetId === comment.id" class="mt-4 rounded-xl p-4 border" :class="['theme-comment-card', 'theme-comment-reply-box']">
                    <div class="flex items-start gap-3">
                      <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="w-8 h-8 rounded-full" />
                      <div v-else class="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-bold" :class="['theme-avatar-placeholder']">
                        {{ (userStore.userInfo?.nickname || 'U')[0] }}
                      </div>
                      <div class="flex-1">
                        <div class="text-xs mb-2" :class="['theme-text-muted']">回复 {{ comment.nickname || comment.username }}</div>
                        <textarea
                          v-model="replyContent"
                          placeholder="写下你的回复..."
                          rows="2"
                          class="w-full rounded-lg px-3 py-2 text-sm resize-none focus:outline-none"
                          :class="['theme-comment-input', 'theme-comment-input-sm']"
                          maxlength="200"
                          @keyup.ctrl.enter="submitReply(comment)"
                        ></textarea>
                        <div class="mt-2 flex items-center justify-between">
                          <span class="text-xs" :class="['theme-text-muted']">{{ replyContent.length }}/200</span>
                          <div class="flex gap-2">
                            <button @click="replyTargetId = null; replyContent = ''" class="px-3 py-1.5 text-sm rounded-lg transition-colors" :class="['theme-btn-cancel']">取消</button>
                            <button @click="submitReply(comment)" :disabled="!replyContent.trim()" class="px-4 py-1.5 text-sm rounded-lg transition-colors" :class="['theme-btn-primary', 'theme-btn-sm', { 'theme-btn-disabled': !replyContent.trim() }]">发送</button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 回复预览 -->
                  <div v-if="comment.replyCount && comment.replyCount > 0" class="mt-2">
                    <button @click="toggleReplies(comment)" class="text-xs transition-colors" :class="['theme-link-hover']">
                      {{ showingReplies.has(comment.id) ? '收起回复' : `共 ${comment.replyCount} 条回复 ▾` }}
                    </button>
                    <div v-if="showingReplies.has(comment.id)" class="mt-2 space-y-2 pl-4 border-l-2" :class="['theme-tab-nav']">
                      <div v-for="reply in commentRepliesMap[comment.id] || []" :key="reply.id" class="flex gap-3">
                        <NuxtLink :to="`/user/${reply.userId}`" class="shrink-0">
                          <div class="w-7 h-7 rounded-full overflow-hidden" :class="['theme-comment-avatar-default']">
                            <img v-if="reply.avatar" :src="reply.avatar" class="w-full h-full object-cover" />
                            <span v-else class="text-xs flex items-center justify-center h-full">{{ (reply.nickname || '?')[0] }}</span>
                          </div>
                        </NuxtLink>
                        <div class="flex-1">
                          <div class="flex items-center gap-1 text-xs">
                            <span class="font-medium" :class="['theme-text-main']">{{ reply.nickname || reply.username }}</span>
                            <span v-if="reply.replyToNickname" :class="['theme-text-muted']">→ @{{ reply.replyToNickname }}</span>
                          </div>
                          <p class="text-sm" :class="['theme-comment-content']">{{ reply.content }}</p>
                          <div class="flex items-center gap-4 mt-2 text-xs">
                            <button
                              @click="handleReplyReaction(reply.id.toString(), 1)"
                              class="group flex items-center gap-1 transition-colors"
                              :class="replyReactionStatus[reply.id.toString()] === 1 ? 'text-rose-400' : 'text-slate-500 hover:text-rose-400'"
                              :style="replyReactionStatus[reply.id.toString()] === 1 ? { color: 'var(--accent, #EC4899)' } : {}"
                            >
                              <svg class="w-3.5 h-3.5" :fill="replyReactionStatus[reply.id.toString()] === 1 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                              </svg>
                              <span>{{ reply.likes || 0 }}</span>
                            </button>
                            <button
                              @click="handleReplyReaction(reply.id.toString(), 2)"
                              class="group flex items-center gap-1 transition-colors"
                              :class="replyReactionStatus[reply.id.toString()] === 2 ? 'text-slate-400' : 'text-slate-500 hover:text-slate-400'"
                            >
                              <svg class="w-3.5 h-3.5" :fill="replyReactionStatus[reply.id.toString()] === 2 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h-4.764a2 2 0 01-1.789-2.894l3.5-7A2 2 0 018.737 3h4.018a2 2 0 01.485.06l3.76.94m-7 10v5a2 2 0 002 2h.096c.5 0 .905-.405.905-.904 0-.715.211-1.413.608-2.008L17 13V4m-7 10h2m5-10h2a2 2 0 012 2v6a2 2 0 01-2 2h-2.5" />
                              </svg>
                              <span>{{ reply.dislikes || 0 }}</span>
                            </button>
                            <button @click="toggleReplyToReply(comment, reply)" class="flex items-center gap-1 text-slate-500 hover:text-indigo-400 transition-colors">
                              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                              <span>回复</span>
                            </button>
                          </div>
                          <!-- 回复回复的输入框 -->
                          <div v-if="replyReplyTargetId === reply.id" class="mt-2 bg-slate-800/40 rounded-lg p-3 border border-slate-700/30">
                            <div class="flex items-start gap-2">
                              <div class="w-6 h-6 rounded-full bg-indigo-600 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                                {{ (userStore.userInfo?.nickname || 'U')[0] }}
                              </div>
                              <div class="flex-1">
                                <div class="text-xs text-slate-500 mb-1">回复 {{ reply.nickname || reply.username }}</div>
                                <textarea
                                  v-model="replyReplyContent"
                                  placeholder="写下你的回复..."
                                  rows="2"
                                  class="w-full bg-slate-700/50 border border-slate-600 rounded-md px-2 py-1.5 text-white text-xs placeholder-slate-500 resize-none focus:outline-none focus:border-indigo-500"
                                  maxlength="200"
                                ></textarea>
                                <div class="mt-1.5 flex items-center justify-between">
                                  <span class="text-xs text-slate-500">{{ replyReplyContent.length }}/200</span>
                                  <div class="flex gap-1.5">
                                    <button @click="replyReplyTargetId = null; replyReplyContent = ''" class="px-2 py-1 text-slate-400 hover:text-white text-xs rounded transition-colors">取消</button>
                                    <button @click="submitReplyToReply(comment, reply)" :disabled="!replyReplyContent.trim()" class="px-3 py-1 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 disabled:cursor-not-allowed text-white text-xs rounded transition-colors">发送</button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="commentPages > 1" class="mt-6 flex items-center justify-center gap-2">
              <button
                @click="goToCommentPage(commentPage - 1)"
                :disabled="commentPage === 1"
                class="px-3 py-2 rounded-xl bg-slate-700/50 border border-slate-600 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
              >
                上一页
              </button>
              <span class="text-sm text-slate-400">{{ commentPage }} / {{ commentPages }}</span>
              <button
                @click="goToCommentPage(commentPage + 1)"
                :disabled="commentPage === commentPages"
                class="px-3 py-2 rounded-xl bg-slate-700/50 border border-slate-600 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from .vue.
import { useUserStore } from '~/stores/user'
import { fetchArticleDetail, publishArticleComment, fetchArticleCommentPage, reactArticle, reactArticleComment, getArticleReactionStatus, getArticleCommentReactionStatus, replyArticleComment, fetchArticleCommentReplies, type ArticleDetail, type ArticleCommentVO } from '~/composables/useApi'

const route = useRoute()
const userStore = useUserStore()

const pending = ref(true)
const error = ref<Error | null>(null)
const article = ref<ArticleDetail | null>(null)
const commentContent = ref('')
const submitting = ref(false)
const commentError = ref('')
const commentSuccess = ref(false)
const comments = ref<ArticleCommentVO[]>([])
const commentsLoading = ref(false)
const commentPage = ref(1)
const commentPageSize = 10
const commentPages = ref(1)
const showDislikeDialog = ref(false)
const commentReactionStatus = ref<Record<string, number>>({})
const reactionLoading = ref(false)
const articleReaction = ref<number | null>(null)
const selectedReason = ref('')
const dislikeReasons = ['违规内容', '垃圾信息', '引战内容', '抄袭内容', '其他']

// 评论区点赞/回复
const replyTargetId = ref<string | null>(null)
const replyContent = ref('')
const showingReplies = ref<Set<string>>(new Set())
const commentRepliesMap = ref<Record<string, ArticleCommentVO[]>>({})

// 回复的反应状态
const replyReactionStatus = ref<Record<string, number>>({})

// 回复回复相关
const replyReplyTargetId = ref<string | null>(null)
const replyReplyContent = ref('')

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

async function loadArticle() {
  pending.value = true
  error.value = null
  try {
    const id = route.params.id as string
    article.value = await fetchArticleDetail(id)
  } catch (e) {
    error.value = e as Error
    console.error('加载文章失败:', e)
  } finally {
    pending.value = false
  }
}

async function loadArticleReaction() {
  if (!article.value?.id) return
  try {
    const status = await getArticleReactionStatus(article.value.id.toString()]
    articleReaction.value = status ?? null
  } catch (e) {
    console.error('加载文章反应状态失败', e)
  }
}

async function handleArticleReaction(reactionType: number) {
  if (!article.value || reactionLoading.value) return
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }

  // 点踩：如果已点踩则直接取消，否则弹出理由选择
  if (reactionType === 2) {
    // 如果已点踩，直接取消
    if (articleReaction.value === 2) {
      await performReaction(reactionType)
      return
    }
    // 否则弹出理由选择
    reactionLoading.value = true
    showDislikeDialog.value = true
    return
  }

  await performReaction(reactionType)
}

async function performReaction(reactionType: number, reason?: string) {
  if (!article.value) return
  reactionLoading.value = true
  try {
    const newStatus = await reactArticle(article.value.id, { reactionType, reason })
    articleReaction.value = newStatus ?? null
    await loadArticle()
    showDislikeDialog.value = false
    selectedReason.value = ''
  } catch (e: any) {
    console.error('操作失败:', e)
  } finally {
    reactionLoading.value = false
  }
}

function cancelDislike() {
  showDislikeDialog.value = false
  reactionLoading.value = false
  selectedReason.value = ''
}

function confirmDislike(reason: string) {
  selectedReason.value = reason
  performReaction(2, reason)
}

async function loadComments() {
  if (!article.value?.id) return
  commentsLoading.value = true
  try {
    const page = await fetchArticleCommentPage(article.value.id, commentPage.value, commentPageSize)
    comments.value = page.records
    commentPages.value = Math.max(1, page.pages || 1)
    // 加载用户反应状态
    await loadCommentReactionStatus()
  } catch (e) {
    console.error('加载评论失败:', e)
  } finally {
    commentsLoading.value = false
  }
}

async function loadCommentReactionStatus() {
  for (const comment of comments.value) {
    try {
      const status = await getArticleCommentReactionStatus(comment.id.toString()]
      if (status !== null) {
        commentReactionStatus.value[comment.id.toString()] = status
      }
      // 同时加载回复的反应状态
      if (comment.replies) {
        for (const reply of comment.replies) {
          const replyStatus = await getArticleCommentReactionStatus(reply.id.toString()]
          if (replyStatus !== null) {
            replyReactionStatus.value[reply.id.toString()] = replyStatus
          }
        }
      }
    } catch (e) {
      console.error('加载反应状态失败', e)
    }
  }
}

// 评论点踩理由选择
const showCommentDislikeDialog = ref(false)
const selectedCommentId = ref('')

async function handleCommentReaction(commentId: string, reactionType: number) {
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  
  // 点踩：如果已点踩则直接取消，否则弹出理由选择
  if (reactionType === 2) {
    const currentStatus = commentReactionStatus.value[commentId]
    if (currentStatus === 2) {
      // 已点踩，直接取消
      await performCommentReaction(commentId, reactionType)
      return
    }
    // 否则弹出理由选择
    selectedCommentId.value = commentId
    showCommentDislikeDialog.value = true
    return
  }
  
  await performCommentReaction(commentId, reactionType)
}

async function performCommentReaction(commentId: string, reactionType: number, reason?: string) {
  try {
    const newStatus = await reactArticleComment(commentId, reactionType)
    commentReactionStatus.value[commentId] = newStatus
    await loadComments()
  } catch (e: any) {
    console.error('反应失败:', e)
  }
}

function cancelCommentDislike() {
  showCommentDislikeDialog.value = false
  selectedCommentId.value = ''
}

function confirmCommentDislike(reason: string) {
  if (selectedCommentId.value) {
    performCommentReaction(selectedCommentId.value, 2, reason)
    showCommentDislikeDialog.value = false
    selectedCommentId.value = ''
  } else if (selectedReplyId.value) {
    performReplyReaction(selectedReplyId.value, 2, reason)
    showCommentDislikeDialog.value = false
    selectedReplyId.value = ''
  }
}

// 回复的反应处理
const selectedReplyId = ref('')

async function handleReplyReaction(replyId: string, reactionType: number) {
  if (!userStore.isLoggedIn) {
    navigateTo('/login')
    return
  }
  
  if (reactionType === 2) {
    const currentStatus = replyReactionStatus.value[replyId]
    if (currentStatus === 2) {
      await performReplyReaction(replyId, reactionType)
      return
    }
    selectedReplyId.value = replyId
    showCommentDislikeDialog.value = true
    return
  }
  
  await performReplyReaction(replyId, reactionType)
}

async function performReplyReaction(replyId: string, reactionType: number, reason?: string) {
  try {
    // 获取当前状态（切换前的状态）
    const currentStatus = replyReactionStatus.value[replyId]
    const newStatus = await reactArticleComment(replyId, reactionType)
    replyReactionStatus.value[replyId] = newStatus
    
    // 刷新回复列表
    for (const comment of comments.value) {
      if (commentRepliesMap.value[comment.id]) {
        const replies = commentRepliesMap.value[comment.id]
        const replyIndex = replies.findIndex(r => r.id.toString() === replyId)
        if (replyIndex !== -1) {
          // 如果之前有相反的反应，先取消
          if (currentStatus !== null && currentStatus !== newStatus) {
            if (currentStatus === 1) {
              replies[replyIndex].likes = Math.max(0, (replies[replyIndex].likes || 0) - 1)
            } else if (currentStatus === 2) {
              replies[replyIndex].dislikes = Math.max(0, (replies[replyIndex].dislikes || 0) - 1)
            }
          }
          // 应用新的反应
          if (reactionType === 1) {
            replies[replyIndex].likes = (replies[replyIndex].likes || 0) + (newStatus === 1 ? 1 : 0)
          } else {
            replies[replyIndex].dislikes = (replies[replyIndex].dislikes || 0) + (newStatus === 2 ? 1 : 0)
          }
        }
      }
    }
  } catch (e: any) {
    console.error('反应失败:', e)
  }
}

/** 切换回复回复输入框 */
function toggleReplyToReply(comment: ArticleCommentVO, reply: ArticleCommentVO) {
  if (!userStore.isLoggedIn) { navigateTo('/login'); return }
  replyReplyTargetId.value = replyReplyTargetId.value === reply.id ? null : reply.id
  replyReplyContent.value = ''
}

/** 提交回复回复 */
async function submitReplyToReply(comment: ArticleCommentVO, reply: ArticleCommentVO) {
  if (!replyReplyContent.value.trim() || !userStore.userInfo?.id) return
  try {
    await replyArticleComment({
      commentId: comment.id,
      userId: String(userStore.userInfo.id),
      content: replyReplyContent.value.trim(),
      replyToUserId: String(reply.userId),
      replyToNickname: reply.nickname || reply.username,
    })
    replyReplyContent.value = ''
    replyReplyTargetId.value = null
    await loadReplies(comment)
  } catch (e) {
    console.error('回复失败:', e)
  }
}

/** 切换回复输入框 */
function toggleReplyInput(comment: ArticleCommentVO) {
  if (!userStore.isLoggedIn) { navigateTo('/login'); return }
  replyTargetId.value = replyTargetId.value === comment.id ? null : comment.id
  replyContent.value = ''
}

/** 提交回复 */
async function submitReply(comment: ArticleCommentVO) {
  if (!replyContent.value.trim() || !userStore.userInfo?.id) return
  try {
    await replyArticleComment({
      commentId: comment.id,
      userId: String(userStore.userInfo.id),
      content: replyContent.value.trim(),
      replyToUserId: String(comment.userId),
      replyToNickname: comment.nickname || comment.username,
    })
    replyContent.value = ''
    replyTargetId.value = null
    await loadReplies(comment)
  } catch (e) {
    console.error('回复失败:', e)
  }
}

async function loadReplies(comment: ArticleCommentVO) {
  try {
    const replies = await fetchArticleCommentReplies(comment.id)
    commentRepliesMap.value[comment.id] = replies
    comment.replyCount = replies.length
  } catch (e) {
    console.error('加载回复失败:', e)
  }
}

async function toggleReplies(comment: ArticleCommentVO) {
  if (showingReplies.value.has(comment.id)) {
    showingReplies.value.delete(comment.id)
    showingReplies.value = new Set(showingReplies.value)
  } else {
    showingReplies.value.add(comment.id)
    showingReplies.value = new Set(showingReplies.value)
    if (!commentRepliesMap.value[comment.id]) {
      await loadReplies(comment)
    }
  }
}

function shareArticle() {
  if (navigator.share) {
    navigator.share({
      title: article.value?.title,
      text: article.value?.summary,
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(window.location.href)
  }
}

async function submitComment() {
  if (!commentContent.value.trim() || submitting.value) return

  if (commentContent.value.trim().length < 5) {
    commentError.value = '评论内容至少需要5个字'
    return
  }

  commentError.value = ''
  commentSuccess.value = false
  submitting.value = true

  try {
    await publishArticleComment({
      articleId: article.value!.id,
      userId: userStore.userInfo?.id || 0,
      content: commentContent.value.trim()
    })
    commentSuccess.value = true
    commentContent.value = ''
    commentPage.value = 1
    await loadComments()
    setTimeout(() => { commentSuccess.value = false }, 3000)
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '评论发表失败'
  } finally {
    submitting.value = false
  }
}

async function goToCommentPage(page: number) {
  if (page < 1 || page > commentPages.value || page === commentPage.value) return
  commentPage.value = page
  await loadComments()
}

onMounted(async () => {
  await loadArticle()
  if (article.value?.id) {
    await loadComments()
    await loadArticleReaction()
  }
})

// 用户登录态变化后重新加载点赞状态
watch(() => userStore.isLoggedIn, (val) => {
  if (val && article.value?.id) {
    loadArticleReaction()
    loadCommentReactionStatus()
  }
})

useHead({
  title: article.value?.title ? `${article.value.title} - ACG Space` : '文章详情 - ACG Space'
})
</script>