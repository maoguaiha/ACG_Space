<template>
  <div class="min-h-screen">
    <div v-if="loading" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center">
      <div class="w-12 h-12 rounded-full border-4 border-indigo-500/30 border-t-indigo-500 animate-spin mb-4"></div>
      <p class="text-slate-400">加载中…</p>
    </div>

    <div v-else-if="error || !profile" class="container mx-auto px-4 py-20 flex flex-col items-center justify-center">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-6 text-slate-600"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <h2 class="text-xl font-bold mb-2 text-slate-200">用户不存在</h2>
      <p class="text-slate-500 mb-6">该用户可能已被删除或ID不正确</p>
      <NuxtLink to="/" class="px-6 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full transition-colors">返回首页</NuxtLink>
    </div>

    <template v-else>
      <!-- 返回按钮 -->
      <div class="container mx-auto px-4 py-4">
        <NuxtLink to="/community" class="inline-flex items-center gap-2 text-sm transition-colors" :class="['theme-back-link']">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
          返回社区
        </NuxtLink>
      </div>
      <!-- 用户信息头部 -->
      <div class="relative w-full border-b" :class="['theme-user-header']">
        <div class="absolute inset-0 opacity-30">
          <div class="absolute top-0 left-1/4 w-96 h-96 rounded-full blur-3xl" :class="['theme-glow-primary']"></div>
          <div class="absolute bottom-0 right-1/4 w-96 h-96 rounded-full blur-3xl" :class="['theme-glow-secondary']"></div>
        </div>
        <div class="container mx-auto px-4 py-12 relative z-10">
          <div class="flex flex-col md:flex-row items-center md:items-start gap-8">
            <!-- 头像 -->
            <div class="relative group">
              <div v-if="!showCropper" class="absolute -inset-1 rounded-full opacity-50 blur-lg group-hover:opacity-75 transition-opacity duration-300" :class="['theme-avatar-glow']"></div>
              <AvatarUploader
                v-if="profile.isSelf"
                v-model="newAvatar"
                @update:modelValue="handleAvatarUpdate"
                @cropper-show="showCropper = true"
                @cropper-hide="showCropper = false"
              />
              <div v-else class="relative w-28 h-28 md:w-32 md:h-32 rounded-full overflow-hidden ring-4" :class="['theme-avatar-ring']">
                <img v-if="profile.avatar" :src="profile.avatar" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center text-4xl font-bold text-white" :class="['theme-avatar-placeholder']">
                  {{ (profile.nickname || profile.username || '?')[0] }}
                </div>
              </div>
            </div>

            <!-- 用户信息 -->
            <div class="flex-1 text-center md:text-left">
              <div class="flex items-center justify-center md:justify-start gap-3 mb-3">
                <h1 class="text-3xl font-bold theme-text-main">{{ profile.nickname || profile.username }}</h1>
                <span class="text-sm px-2 py-1 rounded-lg" :class="['theme-username-tag']">@{{ profile.username }}</span>
              </div>
              <div v-if="profile.isSelf" class="text-sm mb-4 max-w-md mx-auto md:mx-0">
                <div v-if="!editingBio" @click="startEditBio" class="flex items-center gap-2 cursor-pointer group">
                  <span :class="['theme-text-muted']">{{ profile.bio || '这个人很懒，什么都没写…' }}</span>
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="opacity-0 group-hover:opacity-100 transition-opacity theme-text-muted">
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </div>
                <div v-else class="flex items-center gap-2">
                  <input v-model="editBioText" type="text" maxlength="200"
                    class="flex-1 bg-transparent border-b px-2 py-1 text-sm outline-none" :class="['theme-input']"
                    @keyup.enter="saveBio" @blur="cancelEditBio" ref="bioInput" />
                  <button @click="saveBio" class="text-xs px-2 py-1 rounded" :class="['theme-btn-primary']">保存</button>
                  <button @click="cancelEditBio" class="text-xs px-2 py-1 rounded" :class="['theme-btn-secondary']">取消</button>
                </div>
              </div>
              <p v-else class="text-sm mb-4 max-w-md mx-auto md:mx-0 theme-text-muted">{{ profile.bio || '这个人很懒，什么都没写…' }}</p>

              <!-- 积分 & 头衔 & VIP -->
              <div class="flex items-center justify-center md:justify-start gap-3 mb-5">
                <span class="inline-flex items-center gap-1.5 text-sm px-4 py-2 rounded-xl border" :class="['theme-badge-points']">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                  {{ profile.points || 0 }} 积分
                </span>
                <span class="inline-flex items-center gap-1.5 text-sm px-4 py-2 rounded-xl border" :class="['theme-badge-title']">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                  {{ getTitle(profile.points || 0) }}
                </span>
                <span v-if="profile.vipStatus && profile.vipStatus > 0"
                  class="inline-flex items-center gap-1.5 text-sm px-4 py-2 rounded-xl border"
                  :class="profile.vipStatus === 2 ? 'bg-gradient-to-r from-amber-500 to-orange-500 border-amber-500/50 text-white' : 'bg-gradient-to-r from-yellow-400 to-amber-400 border-yellow-400/50 text-white'">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/></svg>
                  {{ profile.vipStatus === 2 ? 'SVIP' : 'VIP' }}
                </span>
              </div>

              <!-- 等级进度条 -->
              <div class="flex items-center justify-center md:justify-start gap-3 mb-5">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium theme-text-main">Lv.{{ profile.userLevel || 1 }}</span>
                  <div class="w-32 h-2 bg-slate-700 rounded-full overflow-hidden">
                    <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full transition-all"
                      :style="{ width: `${Math.min(100, ((profile.levelExperience || 0) / ((profile.userLevel || 1) * 500)) * 100)}%` }"></div>
                  </div>
                  <span class="text-xs theme-text-muted">{{ profile.levelExperience || 0 }}/{{ (profile.userLevel || 1) * 500 }}</span>
                </div>
              </div>

              <!-- 粉丝/关注（可点击） -->
              <div class="flex items-center justify-center md:justify-start gap-8 text-sm mb-5">
                <button @click="showUserList('following')" class="group flex items-center gap-2 transition-all" :class="['theme-stat-button']">
                  <span class="text-xl font-bold theme-text-main">{{ profile.followingCount }}</span>
                  <span class="theme-text-muted group-hover:text-slate-300 transition-colors">关注</span>
                </button>
                <div class="w-px h-6" :class="['theme-stat-divider']"></div>
                <button @click="showUserList('followers')" class="group flex items-center gap-2 transition-all" :class="['theme-stat-button']">
                  <span class="text-xl font-bold theme-text-main">{{ profile.followerCount }}</span>
                  <span class="theme-text-muted group-hover:text-slate-300 transition-colors">粉丝</span>
                </button>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center justify-center md:justify-start gap-3">
                <template v-if="profile.isSelf">
                  <NuxtLink to="/orders"
                    class="px-6 py-2.5 text-sm font-medium rounded-xl transition-all shadow-lg flex items-center gap-2" :class="['theme-btn-secondary']">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></svg>
                    我的订单
                  </NuxtLink>
                  <button @click="showEditDialog = true"
                    class="px-6 py-2.5 text-sm font-medium rounded-xl transition-all shadow-lg" :class="['theme-btn-primary']">
                    编辑资料
                  </button>
                </template>
                <template v-else>
                  <button @click="handleMessage"
                    class="px-6 py-2.5 text-sm font-medium rounded-xl transition-all shadow-lg flex items-center gap-2" :class="['theme-btn-primary']">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                    私信
                  </button>
                  <button @click="handleFollow"
                    class="px-5 py-2 text-sm rounded-xl transition-all font-medium"
                    :class="profile.isFollowed ? 'theme-btn-followed' : 'theme-btn-follow'">
                    {{ profile.isFollowed ? '取消关注' : '关注' }}
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 内容 -->
      <div class="container mx-auto px-4 py-8">
        <div class="max-w-4xl mx-auto">
          <!-- Tab 导航 -->
          <div class="flex border-b mb-8" :class="['theme-tab-nav']">
            <button v-for="tab in tabs" :key="tab.key"
              @click="activeTab = tab.key"
              class="px-6 py-3 text-sm font-medium transition-colors relative"
              :class="activeTab === tab.key ? 'theme-tab-active' : 'theme-tab-inactive'">
              {{ tab.label }}
              <div v-if="activeTab === tab.key" class="absolute bottom-0 left-0 right-0 h-0.5 rounded-full" :class="['theme-tab-active::after']"></div>
            </button>
          </div>

          <!-- 文章列表 -->
          <div v-if="activeTab === 'articles'">
            <div v-if="articlesLoading" class="space-y-4">
              <div v-for="i in 3" :key="i" class="animate-pulse rounded-2xl p-6" :class="['theme-card']">
                <div class="h-4 rounded w-3/4 mb-3" :class="['theme-skeleton']"></div>
                <div class="h-3 rounded w-1/2" :class="['theme-skeleton']"></div>
              </div>
            </div>
            <div v-else-if="articles.length === 0" class="text-center py-12 theme-text-muted">
              {{ profile.isSelf ? '你还没有发表过文章' : '该用户还没有发表过文章' }}
            </div>
            <div v-else class="space-y-4">
              <div v-for="article in articles" :key="article.id"
                class="rounded-2xl border p-6 transition-colors" :class="['theme-card', 'theme-card-hover']">
                <NuxtLink :to="`/article/${article.id}`" class="block">
                  <div class="flex gap-4">
                    <div v-if="article.coverUrl" class="flex-shrink-0">
                      <img :src="article.coverUrl" class="w-24 h-20 object-cover rounded-xl" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <h3 class="font-bold mb-1 truncate theme-text-main">{{ article.title }}</h3>
                      <p class="text-sm line-clamp-2 theme-text-muted">{{ article.summary }}</p>
                      <div class="flex items-center gap-3 mt-3 text-xs theme-text-muted">
                        <span>{{ formatDate(article.createTime) }}</span>
                        <span>{{ article.viewCount || 0 }} 阅读</span>
                        <span>{{ article.likeCount || 0 }} 赞</span>
                      </div>
                    </div>
                  </div>
                </NuxtLink>
              </div>
              <div v-if="articlesPages > 1" class="flex justify-center gap-2 mt-6">
                <button @click="loadArticles(articlesPage - 1)" :disabled="articlesPage === 1"
                  class="px-3 py-2 rounded-xl bg-slate-700/50 text-sm disabled:opacity-40">上一页</button>
                <span class="px-3 py-2 text-sm text-slate-400">{{ articlesPage }} / {{ articlesPages }}</span>
                <button @click="loadArticles(articlesPage + 1)" :disabled="articlesPage === articlesPages"
                  class="px-3 py-2 rounded-xl bg-slate-700/50 text-sm disabled:opacity-40">下一页</button>
              </div>
            </div>
          </div>

          <!-- 追番列表 -->
          <div v-if="activeTab === 'follows'">
            <div v-if="followsLoading" class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div v-for="i in 4" :key="i" class="animate-pulse bg-slate-800/50 rounded-2xl p-4">
                <div class="h-32 bg-slate-700 rounded-xl mb-2"></div>
                <div class="h-3 bg-slate-700 rounded w-3/4"></div>
              </div>
            </div>
            <div v-else-if="follows.length === 0" class="text-center py-12 text-slate-500">
              {{ profile.isSelf ? '还没有追番' : '该用户还没有追番' }}
            </div>
            <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <NuxtLink v-for="anime in follows" :key="anime.id" :to="`/anime/${anime.id}`"
                class="bg-slate-800/40 rounded-2xl border border-slate-700/50 overflow-hidden hover:border-indigo-500/30 transition-colors group">
                <div class="aspect-[3/4] overflow-hidden">
                  <img v-if="anime.coverUrl" :src="anime.coverUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div v-else class="w-full h-full bg-slate-700 flex items-center justify-center text-slate-500">无封面</div>
                </div>
                <div class="p-3">
                  <p class="text-sm font-medium text-white truncate">{{ anime.title }}</p>
                </div>
              </NuxtLink>
            </div>
          </div>

          <!-- 评论列表 -->
          <div v-if="activeTab === 'comments'">
            <div v-if="commentsLoading" class="space-y-4">
              <div v-for="i in 3" :key="i" class="animate-pulse bg-slate-800/50 rounded-2xl p-6">
                <div class="h-3 bg-slate-700 rounded w-1/4 mb-3"></div>
                <div class="h-4 bg-slate-700 rounded w-3/4"></div>
              </div>
            </div>
            <div v-else-if="comments.length === 0" class="text-center py-12 text-slate-500">
              {{ profile.isSelf ? '你还没有发表过评论' : '该用户还没有发表过评论' }}
            </div>
            <div v-else class="space-y-3">
              <NuxtLink v-for="comment in comments" :key="comment.id"
                :to="comment.type === 1 ? '/anime/' + comment.targetId : '/article/' + comment.targetId"
                class="block">
                <div class="rounded-2xl border p-4 transition-colors" :class="['theme-card', 'theme-card-hover']">
                  <div class="flex items-center gap-3 mb-3">
                    <img v-if="comment.targetCover" :src="comment.targetCover" class="w-16 h-20 object-cover rounded-lg" />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium" :class="['theme-text-muted']">评论于 {{ comment.type === 1 ? '番剧' : '文章' }}</p>
                      <p class="text-base truncate" :class="['theme-text-main']">{{ comment.targetTitle }}</p>
                    </div>
                  </div>
                  <p class="mb-2" :class="['theme-text-content']">{{ comment.content }}</p>
                  <div class="flex items-center gap-4 text-xs" :class="['theme-text-muted']">
                    <span>{{ formatDate(comment.createTime) }}</span>
                    <span v-if="comment.likes > 0">{{ comment.likes }} 点赞</span>
                  </div>
                </div>
              </NuxtLink>
            </div>
          </div>

          <!-- 点赞历史 -->
          <div v-if="activeTab === 'likes'">
            <div v-if="likesLoading" class="space-y-4">
              <div v-for="i in 4" :key="i" class="animate-pulse bg-slate-800/50 rounded-2xl p-4">
                <div class="flex items-center gap-4">
                  <div class="w-16 h-20 bg-slate-700 rounded-lg"></div>
                  <div class="flex-1">
                    <div class="h-4 bg-slate-700 rounded w-3/4 mb-2"></div>
                    <div class="h-3 bg-slate-700 rounded w-1/2"></div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else-if="likes.length === 0" class="text-center py-12 text-slate-500">
              {{ profile.isSelf ? '你还没有点赞过任何评论' : '该用户还没有点赞过任何评论' }}
            </div>
            <div v-else class="space-y-3">
              <NuxtLink v-for="like in likes" :key="like.id"
                :to="like.type === 1 ? '/anime/' + like.targetId : '/article/' + like.targetId"
                class="block">
                <div class="rounded-2xl border p-4 transition-colors" :class="['theme-card', 'theme-card-hover']">
                  <div class="flex items-center gap-3">
                    <img v-if="like.targetCover" :src="like.targetCover" class="w-16 h-20 object-cover rounded-lg" />
                    <div v-else class="w-16 h-20 bg-slate-700 rounded-lg flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="text-slate-500"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium" :class="['theme-text-muted']">点赞了 {{ like.type === 1 ? '番剧' : '文章' }}评论</p>
                      <p class="text-base truncate" :class="['theme-text-main']">{{ like.targetTitle }}</p>
                      <p class="text-xs mt-1" :class="['theme-text-muted']">{{ formatDate(like.createTime) }}</p>
                    </div>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" class="text-red-500"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                  </div>
                </div>
              </NuxtLink>
            </div>
            <div v-if="likesPages > 1" class="flex justify-center gap-2 mt-6">
              <button @click="loadLikes(likesPage - 1)" :disabled="likesPage === 1"
                class="px-3 py-2 rounded-xl bg-slate-700/50 text-sm disabled:opacity-40">上一页</button>
              <span class="px-3 py-2 text-sm text-slate-400">{{ likesPage }} / {{ likesPages }}</span>
              <button @click="loadLikes(likesPage + 1)" :disabled="likesPage === likesPages"
                class="px-3 py-2 rounded-xl bg-slate-700/50 text-sm disabled:opacity-40">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 关注/粉丝列表弹窗 -->
    <div v-if="showUserListDialog" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4" @click.self="showUserListDialog = false">
      <div class="bg-slate-800 rounded-3xl border border-slate-700 p-6 max-w-md w-full max-h-[70vh] flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-white">{{ userListTitle }}</h3>
          <button @click="showUserListDialog = false" class="text-slate-500 hover:text-white transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div v-if="userListLoading" class="flex justify-center py-8">
          <div class="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
        </div>
        <div v-else-if="userList.length === 0" class="text-center py-8 text-slate-500">
          暂无数据
        </div>
        <div v-else class="space-y-2 overflow-y-auto flex-1">
          <NuxtLink v-for="u in userList" :key="u.id" :to="`/user/${u.id}`"
            @click="showUserListDialog = false"
            class="flex items-center gap-3 p-3 rounded-xl hover:bg-slate-700/50 transition-colors group">
            <img v-if="u.avatar" :src="u.avatar" class="w-10 h-10 rounded-full object-cover" />
            <div v-else class="w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-bold">
              {{ (u.nickname || u.username || '?')[0] }}
            </div>
            <div>
              <p class="font-medium text-white group-hover:text-indigo-400 transition-colors">{{ u.nickname || u.username }}</p>
              <p class="text-xs text-slate-500">@{{ u.username }}</p>
            </div>
          </NuxtLink>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="showEditDialog" class="fixed inset-0 flex items-center justify-center z-50 p-4" :class="['theme-modal-overlay']">
      <div class="rounded-3xl border p-8 max-w-md w-full" :class="['theme-modal']">
        <h3 class="text-xl font-bold mb-6" :class="['theme-text-main']">编辑资料</h3>
        <div class="space-y-4">
          <div>
            <label class="text-sm block mb-1" :class="['theme-text-muted']">昵称</label>
            <input v-model="editForm.nickname" type="text" maxlength="30"
              class="w-full border rounded-xl px-4 py-3 focus:outline-none" :class="['theme-input-field']" />
          </div>
          <div>
            <label class="text-sm block mb-1" :class="['theme-text-muted']">个人简介</label>
            <textarea v-model="editForm.bio" rows="3" maxlength="200"
              class="w-full border rounded-xl px-4 py-3 resize-none focus:outline-none" :class="['theme-input-field']"></textarea>
            <p class="text-xs mt-1 text-right" :class="['theme-text-muted']">{{ editForm.bio.length }}/200</p>
          </div>
          <div>
            <label class="text-sm block mb-1" :class="['theme-text-muted']">邮箱</label>
            <input v-model="editForm.email" type="email"
              class="w-full border rounded-xl px-4 py-3 focus:outline-none" :class="['theme-input-field']" />
          </div>
        </div>
        <div class="flex gap-3 justify-end mt-6">
          <button @click="showEditDialog = false"
            class="px-6 py-2 rounded-xl transition-colors" :class="['theme-btn-secondary']">取消</button>
          <button @click="saveProfile"
            class="px-6 py-2 rounded-xl transition-colors" :class="['theme-btn-primary']">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '~/stores/user'
import { fetchUserProfile, updateUserProfile, toggleFollowUser, searchArticles, fetchUserFollows, fetchUserFollowers, fetchUserFollowing, fetchUserArticles, fetchUserComments, fetchUserLikes, type UserProfile, type ArticleListItem, type UserCommentItem, type UserLikeHistoryItem } from '~/composables/useApi'
import AvatarUploader from '~/components/AvatarUploader.vue'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const error = ref(false)
const profile = ref<UserProfile | null>(null)
const activeTab = ref('articles')
const newAvatar = ref('')

const tabs = [
  { key: 'articles', label: '他的文章' },
  { key: 'comments', label: '他的评论' },
  { key: 'follows', label: '他的追番' },
  { key: 'likes', label: '点赞历史' },
]

// 编辑资料
const showEditDialog = ref(false)
const editForm = ref({ nickname: '', bio: '', email: '' })

// 编辑签名
const editingBio = ref(false)
const editBioText = ref('')
const bioInput = ref<HTMLInputElement | null>(null)

// 文章列表
const articles = ref<ArticleListItem[]>([])
const articlesLoading = ref(false)
const articlesPage = ref(1)
const articlesPages = ref(1)

// 追番列表
const follows = ref<any[]>([])
const followsLoading = ref(false)

// 评论列表
const comments = ref<UserCommentItem[]>([])
const commentsLoading = ref(false)

// 点赞历史列表
const likes = ref<UserLikeHistoryItem[]>([])
const likesLoading = ref(false)
const likesPage = ref(1)
const likesPages = ref(1)

// 关注/粉丝列表弹窗
const showUserListDialog = ref(false)
const userListTitle = ref('')
const userList = ref<UserProfile[]>([])
const userListLoading = ref(false)

function getTitle(points: number): string {
  if (points >= 500) return '领域大神'
  if (points >= 200) return '资深宅'
  if (points >= 50) return '活跃漫迷'
  return '新人漫迷'
}

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

async function loadProfile() {
  loading.value = true
  error.value = false
  try {
    const id = route.params.id as string
    profile.value = await fetchUserProfile(id)
    newAvatar.value = profile.value.avatar || ''
    editForm.value = {
      nickname: profile.value.nickname || '',
      bio: profile.value.bio || '',
      email: profile.value.email || '',
    }
  } catch (e) {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function loadArticles(page = 1) {
  if (!profile.value) return
  articlesLoading.value = true
  articlesPage.value = page
  try {
    const res = await fetchUserArticles(profile.value.id, page, 10)
    articles.value = res.records
    articlesPages.value = res.pages
  } finally {
    articlesLoading.value = false
  }
}

async function loadFollows() {
  if (!profile.value) return
  followsLoading.value = true
  try {
    const res = await fetchUserFollows(profile.value.id)
    follows.value = Array.isArray(res) ? res : (res.records || [])
  } finally {
    followsLoading.value = false
  }
}

async function loadComments(page = 1) {
  if (!profile.value) return
  commentsLoading.value = true
  try {
    const res = await fetchUserComments(String(profile.value.id), page, 10)
    comments.value = res.records || []
  } catch (e) {
    console.error('加载评论失败:', e)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function loadLikes(page = 1) {
  if (!profile.value) return
  likesLoading.value = true
  likesPage.value = page
  try {
    const res = await fetchUserLikes(String(profile.value.id), page, 20)
    likes.value = res.records || []
    likesPages.value = res.pages || 1
  } catch (e) {
    console.error('加载点赞历史失败:', e)
    likes.value = []
  } finally {
    likesLoading.value = false
  }
}

async function showUserList(type: 'followers' | 'following') {
  if (!profile.value) return
  showUserListDialog.value = true
  userListTitle.value = type === 'followers' ? '粉丝' : '关注'
  userListLoading.value = true
  try {
    const userId = String(profile.value.id)
    const fn = type === 'followers' ? fetchUserFollowers : fetchUserFollowing
    const res = await fn(userId)
    userList.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载失败:', e)
    userList.value = []
  } finally {
    userListLoading.value = false
  }
}

async function handleAvatarUpdate(avatarUrl: string) {
  if (!avatarUrl) return
  try {
    await updateUserProfile({ avatar: avatarUrl })
    if (profile.value) {
      profile.value.avatar = avatarUrl
    }
    if (userStore.userInfo) {
      userStore.userInfo.avatar = avatarUrl
    }
    newAvatar.value = avatarUrl
  } catch (e) {
    console.error('头像更新失败:', e)
  }
}

async function saveProfile() {
  try {
    await updateUserProfile({
      nickname: editForm.value.nickname,
      bio: editForm.value.bio,
      email: editForm.value.email,
    })
    showEditDialog.value = false
    if (profile.value) {
      profile.value.nickname = editForm.value.nickname
      profile.value.bio = editForm.value.bio
      profile.value.email = editForm.value.email
    }
  } catch (e) {
    console.error('保存失败:', e)
  }
}

function startEditBio() {
  if (!profile.value) return
  editBioText.value = profile.value.bio || ''
  editingBio.value = true
  setTimeout(() => {
    bioInput.value?.focus()
  }, 100)
}

function saveBio() {
  if (!profile.value) return
  const newBio = editBioText.value.trim()
  if (newBio === profile.value.bio) {
    editingBio.value = false
    return
  }
  updateUserProfile({ bio: newBio }).then(() => {
    profile.value!.bio = newBio
    editForm.value.bio = newBio
    editingBio.value = false
  }).catch(e => {
    console.error('保存签名失败:', e)
  })
}

function cancelEditBio() {
  editingBio.value = false
}

async function handleMessage() {
  if (!profile.value) return
  navigateTo(`/message/${profile.value.id}`)
}

async function handleFollow() {
  if (!profile.value) return
  try {
    const result = await toggleFollowUser(String(profile.value.id))
    if (profile.value) {
      profile.value.isFollowed = result
      if (result) {
        profile.value.followerCount = (profile.value.followerCount || 0) + 1
      } else {
        profile.value.followerCount = Math.max(0, (profile.value.followerCount || 0) - 1)
      }
    }
  } catch (e) {
    console.error('关注失败:', e)
  }
}

// 监听 tab 切换加载数据
watch(activeTab, (tab) => {
  if (tab === 'articles') loadArticles()
  if (tab === 'follows') loadFollows()
  if (tab === 'comments') loadComments()
  if (tab === 'likes') loadLikes()
})

onMounted(async () => {
  await loadProfile()
  if (profile.value) {
    await loadArticles()
  }
})

useHead({
  title: profile.value?.nickname ? `${profile.value.nickname} - ACG Space` : '用户 - ACG Space'
})
</script>