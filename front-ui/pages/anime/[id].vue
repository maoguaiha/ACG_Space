<template>
  <div class="min-h-screen">
    <!-- 加载中 -->
    <div v-if="pending" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center theme-text-muted">
      <div class="w-12 h-12 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin mb-4"></div>
      <p>加载番剧详情中…</p>
    </div>

    <!-- 错误页 -->
    <div v-else-if="error || !anime" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center theme-text-muted">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-6 text-red-400/50"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <h2 class="text-xl font-bold mb-2 theme-text-main">番剧不存在或加载失败</h2>
      <p class="text-sm theme-text-muted mb-6">{{ error?.message || '无法获取番剧数据' }}</p>
      <NuxtLink to="/" class="px-6 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full transition-colors force-white">
        返回首页
      </NuxtLink>
    </div>

    <!-- 详情内容 -->
    <template v-else>
      <!-- Hero Banner 区域 -->
      <div class="relative w-full h-[420px] overflow-hidden">
        <!-- 返回按钮 -->
        <NuxtLink to="/anime" class="absolute top-4 left-4 z-20 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-bold transition-all border backdrop-blur-sm hero-btn-back"
          :style="{ background: 'var(--hero-btn-bg, rgba(255,255,255,0.4))', color: 'var(--hero-btn-text, #1E293B)', borderColor: 'var(--border-color, rgba(255,255,255,0.2))' }">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
          返回番剧库
        </NuxtLink>
        <!-- 模糊背景封面 -->
        <div class="absolute inset-0">
          <img
            v-if="anime.coverUrl"
            :src="anime.coverUrl"
            :alt="anime.title"
            referrerpolicy="no-referrer"
            class="w-full h-full object-cover blur-xl scale-110 opacity-40"
          />
          <!-- 遮罩层：主题变量 hero-overlay-gradient -->
          <div class="absolute inset-0" :style="{ background: 'var(--hero-overlay-gradient)' }"></div>
        </div>

        <!-- 内容层 -->
        <div class="container mx-auto px-4 h-full relative z-10 flex items-end pb-10">
          <div class="flex items-end gap-8">
            <!-- 封面图 -->
            <div class="flex-shrink-0 w-40 md:w-48 shadow-2xl shadow-black/50 rounded-xl overflow-hidden ring-4 ring-white/10">
              <img
                v-if="anime.coverUrl"
                :src="anime.coverUrl"
                :alt="anime.title"
                referrerpolicy="no-referrer"
                class="w-full aspect-[2/3] object-cover"
              />
              <div v-else class="w-full aspect-[2/3] bg-slate-800 flex items-center justify-center text-slate-500 text-sm">暂无封面</div>
            </div>

            <!-- 基本信息 -->
            <div class="flex-1 pb-2">
              <div class="flex items-center flex-wrap gap-2 mb-4">
                <span
                  class="text-xs font-bold px-3 py-1 rounded-full hero-meta-tag"
                  :class="animeStore.getStatusClass(anime.status)"
                  :style="{ color: 'var(--hero-btn-text, #1E293B)', textShadow: '0 1px 4px rgba(0,0,0,0.12)' }"
                >
                  {{ animeStore.getStatusLabel(anime.status) }}
                </span>
                <span v-if="anime.publishYear" class="text-xs px-3 py-1 rounded-full border hero-meta-tag"
                  :style="{ color: 'var(--hero-btn-text, #1E293B)', background: 'var(--hero-btn-bg, rgba(255,255,255,0.4))', borderColor: 'var(--border-color, rgba(255,255,255,0.2))', textShadow: '0 1px 4px rgba(0,0,0,0.12)' }">
                  {{ anime.publishYear }} 年
                </span>
                <span v-if="anime.totalEpisodes" class="text-xs px-3 py-1 rounded-full border hero-meta-tag"
                  :style="{ color: 'var(--hero-btn-text, #1E293B)', background: 'var(--hero-btn-bg, rgba(255,255,255,0.4))', borderColor: 'var(--border-color, rgba(255,255,255,0.2))', textShadow: '0 1px 4px rgba(0,0,0,0.12)' }">
                  共 {{ anime.totalEpisodes }} 集
                </span>
              </div>

              <!-- 标题 -->
              <h1 class="text-3xl md:text-5xl font-black text-white force-white leading-tight mb-2 drop-shadow-xl" style="text-shadow: 0 2px 10px rgba(0,0,0,0.15);">
                {{ anime.title }}
              </h1>
              <p v-if="anime.titleOriginal" class="text-white/60 force-white text-sm mb-6">{{ anime.titleOriginal }}</p>

              <!-- 评分 -->
              <div v-if="anime.rating" class="flex items-center gap-2 mb-6">
                <div class="flex items-center gap-1">
                  <svg
                    v-for="i in 5"
                    :key="i"
                    xmlns="http://www.w3.org/2000/svg"
                    width="18" height="18"
                    viewBox="0 0 24 24"
                    :fill="i <= Math.round(anime.rating / 2) ? '#facc15' : 'none'"
                    stroke="#facc15"
                    stroke-width="2.5"
                  >
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                </div>
                <span class="text-yellow-400 font-black text-2xl drop-shadow-md">{{ anime.rating }}</span>
                <span class="text-white/50 force-white text-sm">/ 10</span>
              </div>

              <!-- 交互按钮 -->
              <div class="flex items-center gap-4">
                <a
                  v-if="anime.bgmId"
                  :href="`https://bgm.tv/subject/${anime.bgmId}`"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all border backdrop-blur-md hero-btn-bangumi"
                  :style="{ background: 'var(--hero-btn-bg, rgba(255,255,255,0.4))', color: 'var(--hero-btn-text, #1E293B)', borderColor: 'var(--border-color, rgba(255,255,255,0.1))' }"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
                  在 Bangumi 查看
                </a>

                <!-- 追番按钮 -->
                <button
                  @click="handleFollow"
                  class="inline-flex items-center gap-2 px-8 py-2.5 rounded-xl text-sm font-black transition-all border shadow-xl active:scale-95 hero-btn-follow"
                  :style="{ background: isFollowed ? 'rgba(255,255,255,0.3)' : 'var(--hero-btn-bg, rgba(255,255,255,0.4))', color: 'var(--hero-btn-text, #1E293B)', borderColor: isFollowed ? 'var(--border-color, rgba(255,255,255,0.1))' : 'rgba(255,255,255,0.2)' }"
                >
                  <svg v-if="isFollowed" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/></svg>
                  {{ isFollowed ? '已在追番' : '加入追番' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详情主体 -->
      <div class="container mx-auto px-4 py-12">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-10">

          <!-- 左侧：简介 + 评论区 -->
          <div class="lg:col-span-2 space-y-10">
            <!-- 剧情简介 -->
            <div class="bg-slate-800/40 rounded-3xl border border-slate-700/50 p-8 shadow-sm">
              <h2 class="text-xl font-black theme-text-main mb-6 flex items-center gap-3">
                <div class="p-2 rounded-lg bg-indigo-500/10 text-indigo-500">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
                </div>
                剧情简介
              </h2>
              <p class="theme-text-main leading-relaxed whitespace-pre-line text-base opacity-90">
                {{ anime.summary || '暂无简介信息。' }}
              </p>
            </div>

            <!-- 评论区 -->
            <div class="bg-slate-800/40 rounded-3xl border border-slate-700/50 p-8 shadow-sm">
              <h2 class="text-xl font-black theme-text-main mb-8 flex items-center gap-3">
                <div class="p-2 rounded-lg bg-indigo-500/10 text-indigo-500">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                </div>
                用户评价
                <span class="text-xs font-normal theme-text-muted ml-2">（发表评论参与社区讨论）</span>
              </h2>

              <!-- 发表评论表单 -->
              <div v-if="userStore.isLoggedIn" class="mb-10">
                <form @submit.prevent="submitComment">
                  <div class="flex gap-4">
                    <div class="w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center flex-shrink-0 overflow-hidden">
                       <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="w-full h-full object-cover" />
                       <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="theme-text-muted"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    </div>
                    <div class="flex-1">
                      <textarea
                        v-model="commentContent"
                        placeholder="在此输入您的评论..."
                        class="w-full bg-slate-900/60 border border-slate-700 focus:border-indigo-500 rounded-2xl p-4 text-sm theme-text-main placeholder-slate-500 resize-none outline-none transition-all min-h-[100px] focus:ring-4 ring-indigo-500/10"
                        maxlength="500"
                      ></textarea>
                      <div class="flex items-center justify-between mt-3">
                        <span class="text-xs theme-text-muted">{{ commentContent.length }}/500</span>
                        <button
                          type="submit"
                          :disabled="commentSubmitting || commentContent.trim().length < 5"
                          class="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:cursor-not-allowed text-white force-white text-sm font-bold rounded-xl transition-all shadow-lg shadow-indigo-600/20 active:scale-95"
                        >
                          {{ commentSubmitting ? '发送中…' : '发表评论' }}
                        </button>
                      </div>
                      <p v-if="commentError" class="text-xs text-red-500 mt-2 font-bold">{{ commentError }}</p>
                      <transition enter-active-class="transition duration-300" enter-from-class="opacity-0 translate-y-1" enter-to-class="opacity-100 translate-y-0">
                        <p v-if="commentSuccess" class="text-xs text-green-500 mt-2 font-bold flex items-center gap-1">
                          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
                          评论发表成功！
                        </p>
                      </transition>
                    </div>
                  </div>
                </form>
              </div>
              <div v-else class="mb-10 bg-slate-900/40 border border-slate-700/50 rounded-3xl p-10 text-center backdrop-blur-sm">
                <div class="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-5 border border-slate-700 shadow-inner">
                  <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="theme-text-muted"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                </div>
                <p class="theme-text-muted text-sm mb-8 font-medium">登录后即可参与讨论、追番并获得社区奖励</p>
                <NuxtLink to="/login" class="inline-block px-12 py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white force-white rounded-full text-sm font-black transition-all shadow-2xl shadow-indigo-600/30 active:scale-95">
                  立即登录 / 注册
                </NuxtLink>
              </div>

              <!-- 评论列表 -->
              <div class="space-y-6 pt-6 border-t border-slate-700/40">
                <div v-if="commentListLoading" class="space-y-4">
                  <div v-for="i in 3" :key="`comment-loading-${i}`" class="h-20 rounded-2xl bg-slate-800/60 animate-pulse p-4"></div>
                </div>
                <template v-else>
                  <div v-if="commentList.length > 0" class="space-y-4">
                    <div
                      v-for="item in commentList"
                      :key="item.id"
                      class="bg-slate-800/60 border border-slate-700/40 rounded-2xl p-5 hover:border-indigo-500/30 transition-all duration-200 shadow-sm"
                    >
                      <div class="flex items-start gap-4">
                        <NuxtLink :to="'/user/' + item.userId" class="shrink-0">
                          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 overflow-hidden flex items-center justify-center hover:ring-2 hover:ring-indigo-400/50 transition-all duration-200">
                            <img v-if="item.avatar" :src="item.avatar" class="w-full h-full object-cover" />
                            <span v-else class="text-sm font-bold text-white">{{ getDisplayName(item).slice(0, 1) }}</span>
                          </div>
                        </NuxtLink>
                        <div class="flex-1 min-w-0">
                          <div class="flex items-center justify-between gap-3 mb-2">
                            <span class="text-sm font-bold text-white">{{ getDisplayName(item) }}</span>
                            <span class="text-xs text-slate-500 shrink-0">{{ formatCommentTime(item.createTime) }}</span>
                          </div>
                          <p class="text-sm text-slate-200 whitespace-pre-line leading-relaxed mb-3">{{ item.content }}</p>
                          <div class="flex items-center gap-6">
                            <button
                              @click="handleCommentReaction(item.id.toString(), 1)"
                              class="group flex items-center gap-2 text-sm transition-all duration-200"
                              :class="commentReactionStatus.get(item.id.toString()) === 1 ? 'text-rose-400' : 'text-slate-500 hover:text-rose-400'"
                            >
                              <svg class="w-5 h-5 transition-transform group-hover:scale-110" :fill="commentReactionStatus.get(item.id.toString()) === 1 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                              </svg>
                              <span>{{ item.likes || 0 }}</span>
                            </button>
                            <button
                              @click="handleCommentReaction(item.id.toString(), 2)"
                              class="group flex items-center gap-2 text-sm transition-all duration-200"
                              :class="commentReactionStatus.get(item.id.toString()) === 2 ? 'text-slate-300' : 'text-slate-500 hover:text-slate-300'"
                            >
                              <svg class="w-5 h-5 transition-transform group-hover:scale-110" :fill="commentReactionStatus.get(item.id.toString()) === 2 ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M14 10h-4.764a2 2 0 01-1.789-2.894l3.5-7A2 2 0 018.737 3h4.018a2 2 0 01.485.06l3.76.94m-7 10v5a2 2 0 002 2h.096c.5 0 .905-.405.905-.904 0-.715.211-1.413.608-2.008L17 13V4m-7 10h2m5-10h2a2 2 0 012 2v6a2 2 0 01-2 2h-2.5" />
                              </svg>
                              <span>{{ item.dislikes || 0 }}</span>
                            </button>
                            <button
                              v-if="item.replyCount !== undefined && item.replyCount > 0"
                              @click="replyingTo = replyingTo === item.id ? null : item.id"
                              class="group flex items-center gap-2 text-sm text-slate-500 hover:text-indigo-400 transition-colors"
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                              <span>{{ item.replyCount }}</span>
                            </button>
                            <button
                              @click="replyingTo = replyingTo === item.id ? null : item.id; replyContent = ''"
                              class="group flex items-center gap-2 text-sm text-slate-500 hover:text-indigo-400 transition-colors"
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                              <span>回复</span>
                            </button>
                          </div>
                        </div>
                      </div>
                      <div v-if="replyingTo === item.id" class="mt-4 ml-14 bg-slate-900/80 rounded-xl p-4 border border-slate-700/50">
                        <div class="flex items-start gap-3">
                          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 flex-shrink-0 flex items-center justify-center">
                            <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="w-full h-full object-cover rounded-full" />
                            <span v-else class="text-sm font-bold text-white">{{ (userStore.userInfo?.nickname || 'U')[0] }}</span>
                          </div>
                          <div class="flex-1">
                            <div class="text-xs text-indigo-400 mb-2 font-medium">回复 {{ item.nickname || item.username }}</div>
                            <textarea
                              v-model="replyContent"
                              placeholder="写下你的回复..."
                              rows="2"
                              class="w-full bg-slate-800/80 border border-slate-600 rounded-lg px-4 py-3 text-white text-sm placeholder-slate-500 resize-none focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500/50 transition-all"
                              @keyup.ctrl.enter="submitReply(item.id, item.userId, item.nickname || item.username)"
                            ></textarea>
                            <div class="mt-3 flex items-center justify-between">
                              <span class="text-xs text-slate-500">{{ replyContent.length }}/200</span>
                              <div class="flex gap-2">
                                <button @click="replyingTo = null; replyContent = ''" class="px-4 py-2 text-slate-400 hover:text-white text-sm rounded-lg transition-colors hover:bg-slate-700/50">取消</button>
                                <button
                                  @click="submitReply(item.id, item.userId, item.nickname || item.username)"
                                  :disabled="replySubmitting || !replyContent.trim()"
                                  class="px-5 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 disabled:from-slate-700 disabled:to-slate-700 disabled:cursor-not-allowed text-white text-sm rounded-lg transition-all"
                                >
                                  {{ replySubmitting ? '发送中...' : '发送' }}
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div v-if="replyingTo === item.id && item.replies && item.replies.length > 0" class="mt-4 ml-14 space-y-3 border-l-2 border-indigo-500/20 pl-4">
                        <div v-for="reply in item.replies" :key="reply.id" class="bg-slate-900/60 rounded-xl p-3 hover:bg-slate-800/80 transition-colors">
                          <div class="flex items-start gap-3">
                            <NuxtLink :to="'/user/' + reply.userId" class="shrink-0">
                              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-600 to-purple-600 overflow-hidden flex items-center justify-center hover:ring-2 hover:ring-indigo-400/50 transition-all">
                                <img v-if="reply.avatar" :src="reply.avatar" class="w-full h-full object-cover" />
                                <span v-else class="text-xs font-bold text-white">{{ getDisplayName(reply).slice(0, 1) }}</span>
                              </div>
                            </NuxtLink>
                            <div class="flex-1 min-w-0">
                              <div class="flex items-center justify-between gap-2 mb-1">
                                <span class="text-sm font-bold text-white truncate">{{ getDisplayName(reply) }}</span>
                                <span class="text-xs text-slate-500 shrink-0">{{ formatCommentTime(reply.createTime) }}</span>
                              </div>
                              <p class="text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                                <span v-if="reply.replyToNickname" class="text-indigo-400 font-medium">@{{ reply.replyToNickname }} </span>
                                {{ reply.content }}
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="flex flex-col items-center py-16 text-slate-500">
                    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-4 opacity-50"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    <p class="text-lg font-bold text-slate-400">暂无评论</p>
                    <p class="text-sm mt-2">快来发表第一条评论吧！</p>
                  </div>
                </template>

                <div v-if="commentPages > 1" class="pt-2 flex items-center justify-center gap-2">
                  <button
                    @click="goToCommentPage(commentPage - 1)"
                    :disabled="commentPage === 1"
                    class="px-3 py-2 rounded-xl bg-slate-800 border border-slate-700/50 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    上一页
                  </button>
                  <button
                    v-for="page in commentVisiblePages"
                    :key="`comment-page-${page}`"
                    @click="goToCommentPage(page)"
                    class="min-w-10 h-10 rounded-xl border text-sm font-bold transition-all"
                    :class="commentPage === page ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-slate-800 border-slate-700/50 text-slate-300 hover:border-indigo-500/60'"
                  >
                    {{ page }}
                  </button>
                  <button
                    @click="goToCommentPage(commentPage + 1)"
                    :disabled="commentPage === commentPages"
                    class="px-3 py-2 rounded-xl bg-slate-800 border border-slate-700/50 text-sm disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    下一页
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：元数据信息卡 -->
          <div class="space-y-6">
            <div class="bg-slate-800/40 rounded-3xl border border-slate-700/50 p-8 sticky top-28 shadow-sm">
              <h3 class="text-lg font-black theme-text-main mb-6 flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="text-indigo-400"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
                番剧详情
              </h3>
              <dl class="space-y-4 text-sm">
                <div class="flex justify-between items-center py-2 border-b border-slate-700/20">
                  <dt class="theme-text-muted font-bold uppercase tracking-wider text-[10px]">当前状态</dt>
                  <dd>
                    <span v-if="anime" class="text-white force-white font-black px-2.5 py-1 rounded-md text-[10px] uppercase" :class="animeStore.getStatusClass(anime.status)">
                      {{ animeStore.getStatusLabel(anime.status) }}
                    </span>
                  </dd>
                </div>
                <div v-if="anime?.publishYear" class="flex justify-between items-center py-2 border-b border-slate-700/20">
                  <dt class="theme-text-muted font-bold uppercase tracking-wider text-[10px]">放送年份</dt>
                  <dd class="theme-text-main font-bold">{{ anime.publishYear }} 年</dd>
                </div>
                <div v-if="anime?.totalEpisodes" class="flex justify-between items-center py-2 border-b border-slate-700/20">
                  <dt class="theme-text-muted font-bold uppercase tracking-wider text-[10px]">总集数</dt>
                  <dd class="theme-text-main font-bold">{{ anime.totalEpisodes }} 集</dd>
                </div>
                <div v-if="anime?.rating" class="flex justify-between items-center py-2 border-b border-slate-700/20">
                  <dt class="theme-text-muted font-bold uppercase tracking-wider text-[10px]">综合评分</dt>
                  <dd class="text-yellow-500 font-black text-base">{{ anime.rating }} <span class="theme-text-muted font-normal text-xs">/ 10</span></dd>
                </div>
                <div v-if="anime?.bgmId" class="flex justify-between items-center py-2">
                  <dt class="theme-text-muted font-bold uppercase tracking-wider text-[10px]">BGM ID</dt>
                  <dd>
                    <a
                      :href="`https://bgm.tv/subject/${anime.bgmId}`"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="text-indigo-400 hover:text-indigo-300 font-bold transition-colors underline underline-offset-4"
                    >
                      #{{ anime.bgmId }}
                    </a>
                  </dd>
                </div>
              </dl>

              <!-- 分隔线 -->
              <div class="border-t border-slate-700/50 my-8"></div>

              <!-- 返回首页 -->
              <NuxtLink
                to="/"
                class="flex items-center justify-center gap-2 w-full py-3 rounded-2xl bg-slate-800/50 border border-slate-700/50 theme-text-muted hover:theme-text-main hover:border-indigo-500 transition-all text-sm font-bold shadow-inner"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m15 18-6-6 6-6"/></svg>
                返回首页
              </NuxtLink>
            </div>
          </div>

        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAnimeStore } from '~/stores/anime'
import { useUserStore } from '~/stores/user'
import { useAppStore } from '~/stores/app'
import { fetchCommentPage, publishComment, replyComment, reactComment, getCommentReactionStatus, toggleFollowApi, fetchFollowStatus, toggleFollowBangumiApi, fetchFollowStatusByBgmId, type CommentPageItem } from '~/composables/useApi'

const route = useRoute()
const router = useRouter()
const animeStore = useAnimeStore()
const userStore = useUserStore()
const appStore = useAppStore()

// ====== ID 处理与数据加载 ======
// 支持 bgm-${id} 格式的跳转
const isBgmIdRoute = computed(() => (route.params.id as string).startsWith('bgm-'))
const bgmId = computed(() => isBgmIdRoute.value ? parseInt((route.params.id as string).replace('bgm-', '')) : null)

const { data: anime, pending, error } = await useAsyncData(
  `anime-detail-${route.params.id}`,
  async () => {
    const id = route.params.id as string
    if (id.startsWith('bgm-')) {
      const bId = parseInt(id.replace('bgm-', ''))
      // 尝试通过 bgmId 加载（后端会尝试静默同步）
      // 这里我们复用 followByBangumi 的后端逻辑，或者让 loadAnimeDetail 支持 bgmId
      // 简单起见，如果后端还未支持通过 bgmId 直接查询，我们可能需要先调用同步
      return await animeStore.loadAnimeDetail(id)
    }
    return await animeStore.loadAnimeDetail(id)
  },
  {
    watch: [() => route.params.id]
  }
)

// ====== SEO 动态元信息 ======
useSeoMeta({
  title: computed(() => anime.value ? `${anime.value.title} - ACG Space` : 'ACG Space'),
  description: computed(() => anime.value?.summary?.slice(0, 150) ?? '动漫番剧详情'),
  ogTitle: computed(() => anime.value?.title ?? 'ACG Space'),
  ogDescription: computed(() => anime.value?.summary?.slice(0, 150) ?? ''),
  ogImage: computed(() => anime.value?.coverUrl ?? ''),
})

// ====== 追番功能 ======
const isFollowed = ref(false)

// ====== 评论列表 ======
const commentList = ref<CommentPageItem[]>([])
const commentListLoading = ref(false)
const commentPage = ref(1)
const commentPageSize = 10
const commentPages = ref(1)

const commentVisiblePages = computed(() => {
  if (commentPages.value <= 7) {
    return Array.from({ length: commentPages.value }, (_, i) => i + 1)
  }
  const start = Math.max(1, commentPage.value - 3)
  const end = Math.min(commentPages.value, start + 6)
  const adjustedStart = Math.max(1, end - 6)
  return Array.from({ length: end - adjustedStart + 1 }, (_, i) => adjustedStart + i)
})

const getDisplayName = (item: CommentPageItem) => item.nickname || item.username || `用户${item.userId}`

const formatCommentTime = (value: string) => {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

// 存储每个评论的用户反应状态 { commentId: reactionType }
const commentReactionStatus = ref<Map<string, number>>(new Map())

const loadCommentPage = async () => {
  if (!anime.value?.id) return
  commentListLoading.value = true
  try {
    const page = await fetchCommentPage(anime.value.id.toString(), commentPage.value, commentPageSize)
    commentList.value = page.records
    commentPages.value = Math.max(1, page.pages || 1)
    // 加载用户反应状态
    await loadCommentReactionStatus()
  } catch (e) {
    console.error('加载评论失败', e)
  } finally {
    commentListLoading.value = false
  }
}

const loadCommentReactionStatus = async () => {
  if (!userStore.isLoggedIn) return
  for (const comment of commentList.value) {
    try {
      const status = await getCommentReactionStatus(comment.id.toString())
      if (status !== null) {
        commentReactionStatus.value.set(comment.id.toString(), status)
      }
    } catch (e) {
      console.error('加载反应状态失败', e)
    }
  }
}

const handleCommentReaction = async (commentId: string, reactionType: number) => {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  try {
    await reactComment(commentId, reactionType)
    // 更新本地状态
    const currentStatus = commentReactionStatus.value.get(commentId)
    if (currentStatus === reactionType) {
      commentReactionStatus.value.delete(commentId)
    } else {
      commentReactionStatus.value.set(commentId, reactionType)
    }
    // 刷新评论列表以更新计数
    await loadCommentPage()
  } catch (e: any) {
    appStore.showMessage(e.message || '操作失败', 'error')
  }
}

const goToCommentPage = async (page: number) => {
  if (page < 1 || page > commentPages.value || page === commentPage.value) return
  commentPage.value = page
  await loadCommentPage()
}

onMounted(async () => {
  if (userStore.isLoggedIn && anime.value) {
    try {
      // 如果是本地 ID
      if (anime.value.id) {
        isFollowed.value = await fetchFollowStatus(anime.value.id.toString())
      } else if (anime.value.bgmId) {
        isFollowed.value = await fetchFollowStatusByBgmId(anime.value.bgmId)
      }
    } catch (e) {
      console.error('获取追番状态失败', e)
    }
  }
  if (anime.value?.id) {
    await loadCommentPage()
  }
})

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  
  try {
    if (anime.value?.id) {
       const result = await toggleFollowApi(anime.value.id.toString())
       isFollowed.value = result
    } else if (bgmId.value) {
       const result = await toggleFollowBangumiApi(bgmId.value)
       isFollowed.value = result
    }
    if (isFollowed.value) appStore.showMessage('加入追番列表成功！')
    else appStore.showMessage('已取消追番', 'info')
  } catch (e: any) {
    appStore.showMessage(e.message || '操作失败', 'error')
  }
}

// ====== 评论功能 ======
const commentContent = ref('')
const commentSubmitting = ref(false)
const commentError = ref('')
const commentSuccess = ref(false)

const replyingTo = ref<string | null>(null)
const replyContent = ref('')
const replySubmitting = ref(false)

async function submitComment() {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }

  if (!anime.value?.id) {
    appStore.showMessage('该番剧尚未入库，无法发表评论。请先点击"加入追番"以同步数据。', 'error')
    return
  }

  commentError.value = ''
  commentSuccess.value = false

  if (commentContent.value.trim().length < 5) {
    commentError.value = '评论内容至少需要5个字'
    return
  }

  commentSubmitting.value = true
  try {
    await publishComment({
      animeId: anime.value.id.toString(),
      userId: userStore.userInfo?.id || '0',
      content: commentContent.value.trim()
    })
    commentSuccess.value = true
    commentContent.value = ''
    commentPage.value = 1
    await loadCommentPage()
    setTimeout(() => { commentSuccess.value = false }, 3000)
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '评论发表失败，请稍后重试'
  } finally {
    commentSubmitting.value = false
  }
}

async function submitReply(commentId: string, replyToUserId?: string, replyToNickname?: string) {
  if (!userStore.isLoggedIn) {
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  if (!replyContent.value.trim()) return

  replySubmitting.value = true
  try {
    await replyComment({
      commentId,
      userId: userStore.userInfo?.id || '0',
      content: replyContent.value.trim(),
      replyToUserId,
      replyToNickname
    })
    replyContent.value = ''
    replyingTo.value = null
    commentPage.value = 1
    await loadCommentPage()
    appStore.showMessage('回复成功！', 'success')
  } catch (e: any) {
    appStore.showMessage(e.message || '回复失败，请稍后重试', 'error')
  } finally {
    replySubmitting.value = false
  }
}
</script>
