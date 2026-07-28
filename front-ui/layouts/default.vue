<template>
  <div :class="[currentTheme, 'min-h-screen flex flex-col font-sans transition-colors duration-500']">
    <!-- Navbar -->
    <header class="sticky top-0 z-50 w-full backdrop-blur-md border-b theme-header">
      <div class="container mx-auto px-4 h-16 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center font-bold text-white shadow-lg shadow-indigo-500/30">
            A
          </div>
          <span class="text-xl font-bold logo-text">
            ACG Space
          </span>
        </div>
        
        <nav class="hidden md:flex items-center gap-8 text-sm font-medium nav-links">
          <NuxtLink to="/" class="hover:text-indigo-400 transition-colors" active-class="active-link">首页</NuxtLink>
          <NuxtLink to="/anime" class="hover:text-indigo-400 transition-colors" active-class="active-link">番剧库</NuxtLink>
          <NuxtLink to="/follows" class="hover:text-indigo-400 transition-colors" active-class="active-link">我的追番</NuxtLink>
          <NuxtLink to="/community" class="hover:text-indigo-400 transition-colors" active-class="active-link">社区</NuxtLink>
          <NuxtLink to="/gacha" class="hover:text-amber-400 transition-colors" active-class="active-link text-amber-400">抽赏</NuxtLink>
          <NuxtLink to="/agent" class="hover:text-indigo-400 transition-colors" active-class="active-link">AI 助手</NuxtLink>
        </nav>

        <div class="flex items-center gap-6">
          <!-- Theme Switcher -->
          <div class="flex bg-slate-800/20 p-1 rounded-full border border-slate-700/30 theme-switcher">
            <button 
              v-for="t in themes" 
              :key="t.id"
              @click="setTheme(t.id)"
              class="w-8 h-8 rounded-full flex items-center justify-center transition-all"
              :class="currentThemeId === t.id ? 'bg-white shadow-lg scale-110' : 'hover:scale-105 opacity-80'"
              :title="t.name"
            >
              <div :class="['w-4 h-4 rounded-full border', t.colorClass, t.borderClass]"></div>
            </button>
          </div>

          <!-- Auth -->
          <div v-if="userStore.isLoggedIn" class="flex items-center gap-3">
            <!-- 写文章 -->
            <NuxtLink to="/article/create"
              class="hidden sm:inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-medium transition-all"
              :class="['theme-btn-write']">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              写文章
            </NuxtLink>
            <!-- 消息 -->
            <NuxtLink to="/messages" class="relative text-slate-400 hover:text-slate-200 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
              <span v-if="unreadCount > 0" class="absolute -top-1.5 -right-1.5 w-4 h-4 bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                {{ unreadCount > 9 ? '9+' : unreadCount }}
              </span>
            </NuxtLink>
            <!-- 用户头像（点进个人主页） -->
            <NuxtLink :to="`/user/${userStore.userInfo?.id}`" class="flex items-center gap-2 nav-user group">
              <div class="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center overflow-hidden group-hover:ring-2 group-hover:ring-indigo-500/50 transition-all">
                <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="w-full h-full object-cover" />
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </div>
              <span class="text-sm font-medium hidden sm:block text-slate-300 group-hover:text-white transition-colors">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </NuxtLink>
            <button @click="handleLogout" class="text-xs text-slate-500 hover:text-rose-400 transition-colors logout-btn">退出</button>
          </div>
          <NuxtLink v-else :to="`/login?redirect=${route.fullPath}`" class="login-btn bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-full text-sm font-bold transition-all shadow-lg shadow-indigo-600/20 active:scale-95">
            登录 / 注册
          </NuxtLink>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1">
      <slot />
    </main>

    <!-- Global Message Notification -->
    <transition
      enter-active-class="transform ease-out duration-300 transition"
      enter-from-class="translate-y-[-100%] opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="appStore.message" class="fixed top-20 left-1/2 -translate-x-1/2 z-[100] px-6 py-3 rounded-2xl shadow-2xl backdrop-blur-xl border flex items-center gap-3 min-w-[200px] justify-center"
        :class="appStore.message.type === 'success' ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-500' : 'bg-rose-500/10 border-rose-500/20 text-rose-500'">
        <svg v-if="appStore.message.type === 'success'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <span class="font-bold text-sm">{{ appStore.message.text }}</span>
      </div>
    </transition>

    <!-- Footer -->
    <footer class="border-t theme-footer py-12 mt-20">
      <div class="container mx-auto px-4 text-center text-slate-500 text-sm">
        <p>© 2026 ACG Space. All rights reserved.</p>
        <p class="mt-2">专注于高质量动漫分享与讨论的纯粹社区。</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '~/stores/user'
import { useAppStore } from '~/stores/app'
import { fetchUnreadCount } from '~/composables/useApi'

const userStore = useUserStore()
const appStore = useAppStore()
const route = useRoute()
const router = useRouter()
const unreadCount = ref(0)

const themes = [
  { id: 'dark', name: '深色模式', colorClass: 'bg-black', borderClass: 'border-slate-900', class: 'theme-dark' },
  { id: 'light', name: '浅色模式', colorClass: 'bg-slate-100', borderClass: 'border-slate-300', class: 'theme-light' },
  { id: 'pink', name: '粉色模式', colorClass: 'bg-pink-300', borderClass: 'border-pink-400', class: 'theme-pink' },
]

const themeCookie = useCookie('acg_theme', { default: () => 'dark', watch: true })
const currentThemeId = ref(themeCookie.value || 'dark')

// 定义主题对应的 CSS Class
const themeClass = computed(() => `theme-${currentThemeId.value}`)
const currentTheme = computed(() => themes.find(t => t.id === currentThemeId.value)?.class || 'theme-dark')

// 切换主题并保存
const setTheme = (id: string) => {
  currentThemeId.value = id
  themeCookie.value = id
}

// 注入防止闪烁的脚本 (从 Cookie 读取)
useHead({
  htmlAttrs: {
    class: themeClass
  },
  script: [
    {
      innerHTML: `
        (function() {
          try {
            const match = document.cookie.match(new RegExp('(^| )acg_theme=([^;]+)'));
            const savedTheme = match ? match[2] : 'dark';
            if (savedTheme) {
              document.documentElement.classList.remove('theme-dark', 'theme-light', 'theme-pink');
              document.documentElement.classList.add('theme-' + savedTheme);
            }
          } catch (e) {}
        })();
      `,
      type: 'text/javascript'
    }
  ]
})

onMounted(async () => {
  if (themeCookie.value) {
    currentThemeId.value = themeCookie.value
  }
  if (userStore.isLoggedIn) {
    try {
      unreadCount.value = await fetchUnreadCount()
    } catch (e) {
      console.error('获取未读消息数失败:', e)
    }
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style>
/* 默认深色变量 (Dark) - 蓝紫科技风 */
.theme-dark {
  --bg-main: #0f172a;
  --bg-main-rgb: 15, 23, 42;
  --theme-border-color: rgba(255, 255, 255, 0.08);
  --bg-secondary: #1e293b;
  --bg-card: rgba(30, 41, 59, 0.6);
  --bg-header: rgba(15, 23, 42, 0.85);
  --hero-bg-from: #020617;
  --hero-bg-to: rgba(15, 23, 42, 0);
  --border-color: #334155;
  --text-main: #f8fafc;
  --text-muted: #94a3b8;
  --text-dim: #64748b;
  --accent: #818cf8;
  --accent-hover: #6366f1;
  --accent-secondary: #a78bfa;
  --success: #22c55e;
  --warning: #f59e0b;
  --danger: #ef4444;
  --hero-overlay: rgba(15, 23, 42, 0.85);
  --gacha-hero-from: rgba(88, 28, 135, 0.3);
  --gacha-hero-via: rgba(15, 23, 42, 1);
  --gacha-hero-to: rgba(15, 23, 42, 1);
  --hero-overlay-gradient: linear-gradient(to bottom, rgba(15, 23, 42, 0.3) 0%, rgba(15, 23, 42, 1) 100%);
}

/* 浅色变量 (Light) - 清透白昼风 */
.theme-light {
  --bg-main: #F8FAFC;
  --bg-main-rgb: 248, 250, 252;
  --theme-border-color: rgba(0, 0, 0, 0.06);
  --bg-secondary: #ffffff;
  --bg-card: rgba(255, 255, 255, 0.9);
  --bg-header: rgba(255, 255, 255, 0.95);
  --hero-bg-from: #f1f5f9;
  --hero-bg-to: rgba(248, 250, 252, 0);
  --border-color: #e2e8f0;
  --text-main: #0F172A;
  --text-muted: #64748b;
  --text-dim: #94a3b8;
  --accent: #6366F1;
  --accent-hover: #4F46E5;
  --accent-secondary: #3B82F6;
  --success: #22c55e;
  --warning: #f59e0b;
  --danger: #ef4444;
  --hero-overlay: rgba(15, 23, 42, 0.92);
  --gacha-hero-from: rgba(99, 102, 241, 0.15);
  --gacha-hero-via: rgba(255, 255, 255, 1);
  --gacha-hero-to: rgba(239, 246, 255, 1);
  /* gacha 标题 & 主按钮渐变 */
  --gacha-ten-bg: linear-gradient(135deg, #6366F1 0%, #3B82F6 100%);
  --gacha-title-grad: linear-gradient(135deg, #6366F1 0%, #3B82F6 100%);
  /* gacha Banner 卡片 */
  --gacha-stellar-bg: linear-gradient(to bottom, #f5f3ff, #ffffff);
  --gacha-normal-bg: linear-gradient(to bottom, #eff6ff, #ffffff);
  --banner-stellar-title: #312E81;
  --banner-normal-title: #0C4A6E;
  --gacha-card-text: #334155;
  --gacha-card-muted: #64748b;
  /* gacha 按钮 */
  --gacha-single-bg: #ffffff;
  --gacha-single-text: #475569;
  --gacha-single-border: #e2e8f0;
  /* 积分 & 环形图 */
  --points-bg: #F1F5F9;
  --points-bg-icon: linear-gradient(135deg, #6366F1, #3B82F6);
  --chart-ring-outer: #A78BFA;
  --chart-ring-inner: #e2e8f0;
  /* 进度条 */
  --gacha-progress-track: #e2e8f0;
  --hero-overlay-gradient: linear-gradient(to bottom, rgba(248, 250, 252, 0.3) 0%, #F8FAFC 100%);
  --hero-btn-bg: rgba(255, 255, 255, 0.4);
  --hero-btn-text: #1E293B;
  --hero-btn-hover: rgba(255, 255, 255, 0.7);
}
/* 浅色主题 hero 按钮 hover */
.theme-light .hero-btn-bangumi:hover,
.theme-light .hero-btn-follow:not(.hero-btn-followed):hover,
.theme-light .hero-btn-back:hover {
  background: var(--hero-btn-hover) !important;
}
.theme-pink .hero-btn-bangumi:hover,
.theme-pink .hero-btn-follow:not(.hero-btn-followed):hover,
.theme-pink .hero-btn-back:hover {
  background: var(--hero-btn-hover) !important;
}

/* 粉色变量 (Pink) - 温馨甜美风 */
.theme-pink {
  --bg-main: #fdf2f8;
  --bg-main-rgb: 253, 242, 248;
  --theme-border-color: rgba(0, 0, 0, 0.06);
  --bg-secondary: #ffffff;
  --bg-card: rgba(255, 255, 255, 0.8);
  --bg-header: rgba(255, 241, 242, 0.95);
  --hero-bg-from: #fce7f3;
  --hero-bg-to: rgba(253, 242, 248, 0);
  --border-color: #fbcfe8;
  --text-main: #831843;
  --text-muted: #be185d;
  --text-dim: #db2777;
  --accent: #EC4899;
  --accent-hover: #DB2777;
  --accent-secondary: #F472B6;
  --success: #22c55e;
  --warning: #f59e0b;
  --danger: #ef4444;
  --hero-overlay: rgba(136, 19, 55, 0.92);
  --gacha-hero-from: rgba(236, 72, 153, 0.2);
  --gacha-hero-via: rgba(255, 255, 255, 1);
  --gacha-hero-to: rgba(255, 241, 242, 1);
  /* 粉色 gacha 卡片专用 */
  --gacha-stellar-bg: rgba(252, 231, 243, 0.4);
  --gacha-normal-bg: rgba(224, 242, 254, 0.4);
  --gacha-card-text: #831843;
  --gacha-card-muted: #be185d;
  --gacha-ten-bg-start: #F472B6;
  --gacha-ten-bg-end: #FB7185;
  --gacha-single-bg: #fff1f2;
  --gacha-title-grad: linear-gradient(135deg, #EC4899 0%, #F472B6 50%, #FB7185 100%);
  --gacha-ten-bg: linear-gradient(135deg, #F472B6 0%, #FB7185 100%);
  --gacha-progress-ring: #d8b4fe;
  --gacha-progress-track: #e9d5ff;
  --banner-stellar-title: #831843;
  --banner-normal-title: #be185d;
  --points-bg: #fdf2f8;
  --points-bg-icon: linear-gradient(135deg, #EC4899, #F472B6);
  --chart-ring-outer: #fbcfe8;
  --chart-ring-inner: #ec4899;
  --hero-overlay-gradient: linear-gradient(to bottom, rgba(250, 250, 250, 0.3) 0%, #FAFAFA 100%);
  --hero-btn-bg: rgba(255, 255, 255, 0.5);
  --hero-btn-text: #BE185D;
  --hero-btn-hover: rgba(255, 255, 255, 0.8);
}

/* 应用变量 */
.theme-dark, .theme-light, .theme-pink {
  background-color: var(--bg-main);
  color: var(--text-main);
}

.theme-header {
  background-color: var(--bg-header);
  border-color: var(--border-color);
}

.theme-footer {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
}

.logo-text {
  color: var(--text-main) !important;
  font-weight: 900;
  background: none !important;
  -webkit-text-fill-color: initial !important;
}

.nav-links a {
  color: var(--text-muted);
}

.nav-links a.active-link {
  color: var(--accent);
  font-weight: 700;
}

.nav-user span {
  color: var(--text-main);
}

/* 按钮与交互元素适配 */
.login-btn, button[type="submit"], .btn-accent {
  background-color: var(--accent) !important;
  color: white !important;
}

.login-btn:hover, button[type="submit"]:hover, .btn-accent:hover {
  background-color: var(--accent-hover) !important;
}

/* 输入框与下拉选择框适配 */
.theme-light input, .theme-pink input,
.theme-light select, .theme-pink select,
.theme-light textarea, .theme-pink textarea {
  background-color: var(--bg-secondary) !important;
  color: var(--text-main) !important;
  border-color: var(--border-color) !important;
}

/* 卡片背景适配 */
.theme-light .bg-slate-800, .theme-pink .bg-slate-800,
.theme-light .bg-slate-900, .theme-pink .bg-slate-900,
.theme-light .bg-slate-900\/50, .theme-pink .bg-slate-900\/50,
.theme-light .bg-slate-900\/40, .theme-pink .bg-slate-900\/40,
.theme-light .bg-slate-900\/30, .theme-pink .bg-slate-900\/30,
.theme-light .bg-slate-800\/40, .theme-pink .bg-slate-800\/40,
.theme-light .bg-slate-800\/30, .theme-pink .bg-slate-800\/30,
.theme-light .bg-slate-800\/60, .theme-pink .bg-slate-800\/60,
.theme-light .bg-slate-800\/50, .theme-pink .bg-slate-800\/50 {
  background-color: var(--bg-card) !important;
  border-color: var(--border-color) !important;
}

/* 标签页/选项卡适配 */
.theme-light .bg-slate-800\/40, .theme-pink .bg-slate-800\/40 {
  background-color: rgba(255, 255, 255, 0.8) !important;
}

/* 排除掉已经选中的高亮状态以及需要强制白色的元素 */
.theme-light .bg-indigo-600, .theme-pink .bg-indigo-600,
.theme-light .bg-gradient-to-r, .theme-pink .bg-gradient-to-r {
  background: linear-gradient(to right, var(--accent), var(--accent-secondary)) !important;
}

.theme-light .text-white:not(.force-white), .theme-pink .text-white:not(.force-white) {
  color: var(--text-main) !important;
}

/* text-[color]-* 在浅色/粉色下替换为 --accent */
.theme-light .text-indigo-400, .theme-light .text-indigo-500, .theme-light .text-indigo-600,
.theme-light .text-purple-400, .theme-light .text-purple-500, .theme-light .text-purple-600,
.theme-light .text-amber-400, .theme-light .text-cyan-400, .theme-light .text-blue-400, .theme-light .text-rose-400,
.theme-pink .text-indigo-400, .theme-pink .text-indigo-500, .theme-pink .text-indigo-600,
.theme-pink .text-purple-400, .theme-pink .text-purple-500, .theme-pink .text-purple-600,
.theme-pink .text-amber-400, .theme-pink .text-cyan-400, .theme-pink .text-blue-400, .theme-pink .text-rose-400 {
  color: var(--accent) !important;
}

/* 强制让高亮按钮内的文字保持白色 */
.theme-light .bg-indigo-600 .text-white, .theme-pink .bg-indigo-600 .text-white,
.theme-light .bg-indigo-600 span, .theme-pink .bg-indigo-600 span,
.theme-light button.bg-indigo-600, .theme-pink button.bg-indigo-600,
.theme-light .force-white, .theme-pink .force-white,
.theme-light .bg-gradient-to-r .text-white, .theme-pink .bg-gradient-to-r .text-white,
.theme-light .bg-gradient-to-r span, .theme-pink .bg-gradient-to-r span {
  color: #ffffff !important;
}

/* 修复海报下的文字颜色 */
.theme-light .text-slate-100, .theme-pink .text-slate-100,
.theme-light h3, .theme-pink h3,
.theme-light h4, .theme-pink h4 {
  color: var(--text-main) !important;
}

.theme-light .text-slate-400, .theme-pink .text-slate-400,
.theme-light .text-slate-500, .theme-pink .text-slate-500,
.theme-light .text-slate-600, .theme-pink .text-slate-600 {
  color: var(--text-muted) !important;
}

.theme-text-main {
  color: var(--text-main);
}

.theme-text-muted {
  color: var(--text-muted);
}

/* 下拉窗适配 */
.theme-light .backdrop-blur-xl, .theme-pink .backdrop-blur-xl {
  background-color: rgba(255, 255, 255, 0.95) !important;
}

/* 时间表项 hover 效果 */
.theme-light .hover\:bg-slate-800\/60:hover, .theme-pink .hover\:bg-slate-800\/60:hover {
  background-color: var(--bg-secondary) !important;
  border-color: var(--accent) !important;
}

/* 主题切换器适配 */
.theme-switcher {
  background-color: var(--bg-secondary) !important;
  border: 1.5px solid var(--border-color) !important;
}

.theme-dark .theme-switcher {
  background-color: #1e293b !important;
  border-color: #334155 !important;
}

.theme-switcher button {
  opacity: 1 !important;
  background-color: transparent;
  transform: scale(0.85);
}

.theme-switcher button:hover {
  transform: scale(1);
}

/* 选中项的状态 - 使用主题强调色作为滑块背景 */
.theme-switcher button.bg-white {
  background-color: var(--accent) !important;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  border-radius: 9999px;
}

/* 内部小圆点增加对比描边 */
.theme-switcher button div {
  border: 1px solid rgba(0, 0, 0, 0.1) !important;
}

.theme-dark .theme-switcher button div {
  border-color: rgba(255, 255, 255, 0.2) !important;
}

/* 选中后的圆点变为纯白，确保在任何强调色上都清晰 */
.theme-switcher button.bg-white div {
  background-color: #ffffff !important;
  border-color: #ffffff !important;
}

/* ═══════════════════════════════════════════
   渐变按钮全局覆盖 - 浅色/粉色统一
   所有 from-indigo/purple/blue/amber/cyan/emerald 渐变 → accent 色
   ═══════════════════════════════════════════ */

/* 清除背景 — 防止旧色叠加 */
.theme-light .from-indigo-500, .theme-light .from-indigo-600, .theme-light .to-indigo-500, .theme-light .to-indigo-600,
.theme-light .from-blue-500, .theme-light .from-blue-600, .theme-light .to-blue-500, .theme-light .to-blue-600,
.theme-light .from-purple-500, .theme-light .from-purple-600, .theme-light .to-purple-500, .theme-light .to-purple-600,
.theme-light .to-pink-500, .theme-light .to-pink-600,
.theme-light .from-amber-500, .theme-light .from-amber-400, .theme-light .to-orange-500, .theme-light .to-orange-400,
.theme-light .from-cyan-500, .theme-light .from-cyan-400, .theme-light .to-blue-500, .theme-light .to-blue-400,
.theme-light .from-emerald-500, .theme-light .to-teal-500,
.theme-pink .from-indigo-500, .theme-pink .from-indigo-600, .theme-pink .to-indigo-500, .theme-pink .to-indigo-600,
.theme-pink .from-blue-500, .theme-pink .from-blue-600, .theme-pink .to-blue-500, .theme-pink .to-blue-600,
.theme-pink .from-purple-500, .theme-pink .from-purple-600, .theme-pink .to-purple-500, .theme-pink .to-purple-600,
.theme-pink .to-pink-500, .theme-pink .to-pink-600,
.theme-pink .from-amber-500, .theme-pink .from-amber-400, .theme-pink .to-orange-500, .theme-pink .to-orange-400,
.theme-pink .from-cyan-500, .theme-pink .from-cyan-400, .theme-pink .to-blue-500, .theme-pink .to-blue-400,
.theme-pink .from-emerald-500, .theme-pink .to-teal-500 {
  background-color: transparent !important;
}
/* 浅色主题 → 星空蓝 */
.theme-light .bg-gradient-to-r.from-indigo-500.to-purple-500,
.theme-light .bg-gradient-to-r.from-indigo-600.to-purple-600,
.theme-light .bg-gradient-to-r.from-purple-500.to-pink-500,
.theme-light .bg-gradient-to-r.from-purple-600.to-pink-600,
.theme-light .bg-gradient-to-r.from-blue-500.to-indigo-500,
.theme-light .bg-gradient-to-r.from-blue-600.to-indigo-600,
.theme-light .bg-gradient-to-r.from-cyan-500.to-blue-500,
.theme-light .bg-gradient-to-r.from-amber-500.to-orange-500,
.theme-light .bg-gradient-to-r.from-blue-400.to-cyan-500,
.theme-light .bg-gradient-to-r.from-cyan-400.to-blue-500,
.theme-light .bg-gradient-to-r.from-emerald-500.to-teal-500,
.theme-light .bg-gradient-to-r.from-amber-400.to-orange-500,
.theme-light .bg-gradient-to-br.from-indigo-500.to-purple-500,
.theme-light .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-light .bg-gradient-to-br.from-purple-500.to-pink-500,
.theme-light .bg-gradient-to-br.from-purple-600.to-pink-600,
.theme-light .bg-gradient-to-br.from-blue-500.to-indigo-500,
.theme-light .bg-gradient-to-br.from-blue-600.to-indigo-600,
.theme-light .bg-gradient-to-br.from-amber-400.to-orange-500,
.theme-light .bg-gradient-to-br.from-cyan-400.to-blue-500,
.theme-light .bg-gradient-to-br.from-blue-400.to-cyan-500,
.theme-light .bg-gradient-to-br.from-purple-400.to-pink-500 {
  background: linear-gradient(135deg, #6366F1 0%, #3B82F6 100%) !important;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3) !important;
}
/* 粉色主题 → 粉色 */
.theme-pink .bg-gradient-to-r.from-indigo-500.to-purple-500,
.theme-pink .bg-gradient-to-r.from-indigo-600.to-purple-600,
.theme-pink .bg-gradient-to-r.from-purple-500.to-pink-500,
.theme-pink .bg-gradient-to-r.from-purple-600.to-pink-600,
.theme-pink .bg-gradient-to-r.from-blue-500.to-indigo-500,
.theme-pink .bg-gradient-to-r.from-blue-600.to-indigo-600,
.theme-pink .bg-gradient-to-r.from-cyan-500.to-blue-500,
.theme-pink .bg-gradient-to-r.from-amber-500.to-orange-500,
.theme-pink .bg-gradient-to-r.from-blue-400.to-cyan-500,
.theme-pink .bg-gradient-to-r.from-cyan-400.to-blue-500,
.theme-pink .bg-gradient-to-r.from-emerald-500.to-teal-500,
.theme-pink .bg-gradient-to-r.from-amber-400.to-orange-500,
.theme-pink .bg-gradient-to-br.from-indigo-500.to-purple-500,
.theme-pink .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-pink .bg-gradient-to-br.from-purple-500.to-pink-500,
.theme-pink .bg-gradient-to-br.from-purple-600.to-pink-600,
.theme-pink .bg-gradient-to-br.from-blue-500.to-indigo-500,
.theme-pink .bg-gradient-to-br.from-blue-600.to-indigo-600,
.theme-pink .bg-gradient-to-br.from-amber-400.to-orange-500,
.theme-pink .bg-gradient-to-br.from-cyan-400.to-blue-500,
.theme-pink .bg-gradient-to-br.from-blue-400.to-cyan-500,
.theme-pink .bg-gradient-to-br.from-purple-400.to-pink-500 {
  background: linear-gradient(to right, var(--accent), var(--accent-secondary)) !important;
  box-shadow: 0 4px 15px rgba(236, 72, 153, 0.3) !important;
}

.theme-light .bg-gradient-to-br.from-indigo-500.to-purple-500,
.theme-light .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-light .bg-gradient-to-br.from-purple-500.to-pink-500,
.theme-light .bg-gradient-to-br.from-purple-600.to-pink-600,
.theme-light .bg-gradient-to-br.from-blue-500.to-indigo-500,
.theme-light .bg-gradient-to-br.from-blue-600.to-indigo-600,
.theme-pink .bg-gradient-to-br.from-indigo-500.to-purple-500,
.theme-pink .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-pink .bg-gradient-to-br.from-purple-500.to-pink-500,
.theme-pink .bg-gradient-to-br.from-purple-600.to-pink-600,
.theme-pink .bg-gradient-to-br.from-blue-500.to-indigo-500,
.theme-pink .bg-gradient-to-br.from-blue-600.to-indigo-600 {
  background: linear-gradient(to bottom right, var(--accent), var(--accent-secondary)) !important;
}

/* 头像渐变背景适配 */
.theme-light .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-pink .bg-gradient-to-br.from-indigo-600.to-purple-600,
.theme-light .bg-gradient-to-br.from-indigo-500.to-purple-500,
.theme-pink .bg-gradient-to-br.from-indigo-500.to-purple-500 {
  background: linear-gradient(to bottom right, var(--accent), var(--accent-secondary)) !important;
}

/* ═══════════════════════════════════════════
   浅色主题 gacha 页 — 蓝紫白昼风
   ═══════════════════════════════════════════ */
.theme-light .gacha-points-bg {
  background: var(--points-bg, #f1f5f9) !important;
  border-color: var(--border-color, #e2e8f0) !important;
}
.theme-light .gacha-points-icon {
  background: var(--points-bg-icon) !important;
}
.theme-light .gacha-banner-stellar { color: var(--banner-stellar-title, #312E81) !important; }
.theme-light .gacha-banner-normal { color: var(--banner-normal-title, #0C4A6E) !important; }
.theme-light .gacha-tab-active {
  background: #EEF2FF !important;
  color: #4338CA !important;
}
.theme-dark .gacha-tab-active {
  background: rgba(99, 102, 241, 0.18) !important;
  color: #f8fafc !important;
}
.theme-pink .gacha-tab-active {
  background: rgba(236, 72, 153, 0.12) !important;
  color: #831843 !important;
}
.theme-light .gacha-navbar {
  background: rgba(255,255,255,0.88) !important;
  border-color: #e2e8f0 !important;
  backdrop-filter: blur(16px) !important;
}
.theme-light .gacha-card-body {
  background: rgba(255,255,255,0.85) !important;
  backdrop-filter: blur(12px) !important;
}
.theme-light .gacha-banner-overlay {
  background: linear-gradient(to top, rgba(255,255,255,0.9), transparent) !important;
}
.theme-light .gacha-banner-title { text-shadow: 0 1px 3px rgba(255,255,255,0.6) !important; }
.theme-light .gacha-banner-muted { color: #64748b !important; }
.theme-light .gacha-progress-track { background: #e2e8f0 !important; }
.theme-light .gacha-progress-stellar {
  background: linear-gradient(to right, #6366F1, #8B5CF6) !important;
}
.theme-light .gacha-progress-normal {
  background: linear-gradient(to right, #3B82F6, #60A5FA) !important;
}
.theme-light .gacha-ring {
  border-color: #e2e8f0 !important;
  background: linear-gradient(135deg, #eff6ff, #e0e7ff, #eff6ff) !important;
}
.theme-light .gacha-ring-inner {
  background: linear-gradient(135deg, #c7d2fe, #a5b4fc) !important;
}
.theme-light .gacha-btn-single {
  background: #ffffff !important;
  border: 1px solid #e2e8f0 !important;
  color: #475569 !important;
  box-shadow: none !important;
  font-weight: 600 !important;
}
.theme-light .gacha-btn-single:hover {
  background: #f8fafc !important;
  border-color: #cbd5e1 !important;
}
.theme-light .gacha-badge {
  background: rgba(99, 102, 241, 0.08) !important;
  color: #6366F1 !important;
  border-color: rgba(99, 102, 241, 0.2) !important;
}
.theme-light .gacha-btn-recharge {
  background: #EFF6FF !important;
  color: #3B82F6 !important;
  border: 1px solid #BFDBFE !important;
  box-shadow: none !important;
}
.theme-light .gacha-btn-recharge:hover {
  background: #DBEAFE !important;
}
.theme-light .gacha-pool-card .ring-slate-700 { border-color: #e2e8f0 !important; }

/* ═══════════════════════════════════════════
   粉色主题 gacha 页 — 全粉化（非紫）
   ═══════════════════════════════════════════ */
.theme-pink .gacha-points-bg {
  background: var(--points-bg, #fdf2f8) !important;
  border-color: var(--border-color, #fbcfe8) !important;
}
.theme-pink .gacha-points-icon {
  background: var(--points-bg-icon) !important;
}
.theme-pink .gacha-banner-stellar { color: var(--banner-stellar-title, #831843) !important; }
.theme-pink .gacha-banner-normal { color: var(--banner-normal-title, #be185d) !important; }
.theme-pink .gacha-navbar {
  background: rgba(255,255,255,0.88) !important;
  border-color: #fbcfe8 !important;
  backdrop-filter: blur(16px) !important;
}
.theme-pink .gacha-card-body {
  background: rgba(255,255,255,0.85) !important;
  backdrop-filter: blur(12px) !important;
}
.theme-pink .gacha-banner-overlay {
  background: linear-gradient(to top, rgba(255,255,255,0.9), transparent) !important;
}
.theme-pink .gacha-banner-title { text-shadow: 0 1px 3px rgba(255,255,255,0.6) !important; }.theme-pink .gacha-banner-muted { color: #be185d !important; }
.theme-pink .gacha-progress-track { background: #fce7f3 !important; }
.theme-pink .gacha-progress-stellar {
  background: linear-gradient(to right, #EC4899, #F472B6) !important;
}
.theme-pink .gacha-progress-normal {
  background: linear-gradient(to right, #7DD3FC, #BAE6FD) !important;
}
.theme-pink .gacha-ring {
  border-color: #fbcfe8 !important;
  background: linear-gradient(135deg, #fdf2f8, #fce7f3, #fdf2f8) !important;
}
.theme-pink .gacha-ring-inner {
  background: linear-gradient(135deg, #fbcfe8, #f9a8d4) !important;
}
.theme-pink .gacha-btn-single {
  background: #fff1f2 !important;
  border-color: #fecdd3 !important;
  color: #E11D48 !important;
  box-shadow: none !important;
  font-weight: 600 !important;
}
.theme-pink .gacha-btn-single:hover {
  background: #ffe4e6 !important;
  border-color: #fda4af !important;
}
.theme-pink .gacha-badge {
  background: rgba(236, 72, 153, 0.08) !important;
  color: #EC4899 !important;
  border-color: rgba(236, 72, 153, 0.2) !important;
}
.theme-pink .gacha-btn-recharge {
  background: #fdf2f8 !important;
  color: #DB2777 !important;
  border: 1px solid #fbcfe8 !important;
  box-shadow: none !important;
}
.theme-pink .gacha-btn-recharge:hover {
  background: #fce7f3 !important;
}
.theme-pink .gacha-pool-card .ring-slate-700 { border-color: #fbcfe8 !important; color: #fbcfe8 !important; }

/* 消息气泡适配 */
.theme-light .bg-slate-800\/90, .theme-pink .bg-slate-800\/90 {
  background-color: var(--bg-card) !important;
}

/* 深色主题下的文本框 */
.theme-dark input, .theme-dark select, .theme-dark textarea {
  background-color: var(--bg-secondary) !important;
  color: var(--text-main) !important;
  border-color: var(--border-color) !important;
}

/* === 个人主页主题样式 === */

/* 用户头部背景 */
.theme-dark .theme-user-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  border-color: #334155;
}
.theme-light .theme-user-header {
  background: linear-gradient(135deg, #ffffff 0%, #f1f5f9 50%, #ffffff 100%);
  border-color: #e2e8f0;
}
.theme-pink .theme-user-header {
  background: linear-gradient(135deg, #ffffff 0%, #fce7f3 50%, #ffffff 100%);
  border-color: #fbcfe8;
}

/* 光晕效果 */
.theme-dark .theme-glow-primary {
  background-color: rgba(99, 102, 241, 0.2);
}
.theme-dark .theme-glow-secondary {
  background-color: rgba(147, 51, 234, 0.2);
}
.theme-light .theme-glow-primary {
  background-color: rgba(79, 70, 229, 0.15);
}
.theme-light .theme-glow-secondary {
  background-color: rgba(99, 102, 241, 0.15);
}
.theme-pink .theme-glow-primary {
  background-color: rgba(236, 72, 153, 0.2);
}
.theme-pink .theme-glow-secondary {
  background-color: rgba(244, 114, 182, 0.2);
}

/* 头像光晕 */
.theme-dark .theme-avatar-glow {
  background: linear-gradient(90deg, #6366f1, #a78bfa);
}
.theme-light .theme-avatar-glow {
  background: linear-gradient(90deg, #4f46e5, #6366f1);
}
.theme-pink .theme-avatar-glow {
  background: linear-gradient(90deg, #ec4899, #f472b6);
}

/* 头像边框 */
.theme-dark .theme-avatar-ring {
  --tw-ring-color: rgba(255, 255, 255, 0.1);
}
.theme-light .theme-avatar-ring {
  --tw-ring-color: rgba(79, 70, 229, 0.2);
}
.theme-pink .theme-avatar-ring {
  --tw-ring-color: rgba(236, 72, 153, 0.3);
}

/* 头像占位符 */
.theme-dark .theme-avatar-placeholder {
  background: linear-gradient(135deg, #6366f1, #a78bfa);
}
.theme-light .theme-avatar-placeholder {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
}
.theme-pink .theme-avatar-placeholder {
  background: linear-gradient(135deg, #ec4899, #f472b6);
}

/* 用户名标签 */
.theme-dark .theme-username-tag {
  background-color: rgba(30, 41, 59, 0.8);
  color: #94a3b8;
}
.theme-light .theme-username-tag {
  background-color: rgba(100, 116, 139, 0.15);
  color: #64748b;
}
.theme-pink .theme-username-tag {
  background-color: rgba(236, 72, 153, 0.1);
  color: #be185d;
}

/* 积分徽章 */
.theme-dark .theme-badge-points {
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.15), rgba(245, 158, 11, 0.15));
  color: #fbbf24;
  border-color: rgba(234, 179, 8, 0.3);
}
.theme-light .theme-badge-points {
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.1), rgba(245, 158, 11, 0.1));
  color: #d97706;
  border-color: rgba(234, 179, 8, 0.2);
}
.theme-pink .theme-badge-points {
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.1), rgba(245, 158, 11, 0.1));
  color: #b45309;
  border-color: rgba(234, 179, 8, 0.2);
}

/* 头衔徽章 */
.theme-dark .theme-badge-title {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(147, 51, 234, 0.15));
  color: #a5b4fc;
  border-color: rgba(99, 102, 241, 0.3);
}
.theme-light .theme-badge-title {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(99, 102, 241, 0.1));
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.2);
}
.theme-pink .theme-badge-title {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(244, 114, 182, 0.1));
  color: #db2777;
  border-color: rgba(236, 72, 153, 0.2);
}

/* 统计数字分隔线 */
.theme-dark .theme-stat-divider {
  background-color: #334155;
}
.theme-light .theme-stat-divider {
  background-color: #e2e8f0;
}
.theme-pink .theme-stat-divider {
  background-color: #fbcfe8;
}

/* 统计按钮 hover */
.theme-dark .theme-stat-button:hover {
  color: #818cf8;
}
.theme-light .theme-stat-button:hover {
  color: #4f46e5;
}
.theme-pink .theme-stat-button:hover {
  color: #ec4899;
}

/* 主按钮 */
.theme-dark .theme-btn-primary {
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  color: white;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
}
.theme-dark .theme-btn-primary:hover {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
}
.theme-light .theme-btn-primary {
  background: linear-gradient(135deg, #6366F1, #3B82F6) !important;
  color: white !important;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3) !important;
}
.theme-light .theme-btn-primary:hover {
  background: linear-gradient(135deg, #4F46E5, #2563EB) !important;
}
.theme-pink .theme-btn-primary {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.25);
}
.theme-pink .theme-btn-primary:hover {
  background: linear-gradient(135deg, #db2777, #ec4899);
}

/* 关注按钮 */
.theme-dark .theme-btn-follow {
  background-color: #6366f1;
  color: white;
}
.theme-dark .theme-btn-follow:hover {
  background-color: #4f46e5;
}
.theme-light .theme-btn-follow {
  background-color: #4f46e5;
  color: white;
}
.theme-light .theme-btn-follow:hover {
  background-color: #4338ca;
}
.theme-pink .theme-btn-follow {
  background-color: #ec4899;
  color: white;
}
.theme-pink .theme-btn-follow:hover {
  background-color: #db2777;
}

/* 已关注按钮 */
.theme-dark .theme-btn-followed {
  background-color: rgba(239, 68, 68, 0.15);
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.3);
}
.theme-dark .theme-btn-followed:hover {
  background-color: rgba(239, 68, 68, 0.3);
}
.theme-light .theme-btn-followed {
  background-color: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border-color: rgba(239, 68, 68, 0.2);
}
.theme-light .theme-btn-followed:hover {
  background-color: rgba(239, 68, 68, 0.2);
}
.theme-pink .theme-btn-followed {
  background-color: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border-color: rgba(239, 68, 68, 0.2);
}
.theme-pink .theme-btn-followed:hover {
  background-color: rgba(239, 68, 68, 0.2);
}

/* Tab 导航 */
.theme-dark .theme-tab-nav {
  border-color: #334155;
}
.theme-light .theme-tab-nav {
  border-color: #e2e8f0;
}
.theme-pink .theme-tab-nav {
  border-color: #fbcfe8;
}

.theme-dark .theme-tab-active {
  color: #818cf8;
}
.theme-light .theme-tab-active {
  color: #3B82F6;
}
.theme-pink .theme-tab-active {
  color: #ec4899;
}

.theme-dark .theme-tab-active::after {
  background-color: #818cf8;
}
.theme-light .theme-tab-active::after {
  background-color: #3B82F6;
}
.theme-pink .theme-tab-active::after {
  background-color: #ec4899;
}

.theme-dark .theme-tab-inactive:hover {
  color: #94a3b8;
}
.theme-light .theme-tab-inactive:hover {
  color: #64748b;
}
.theme-pink .theme-tab-inactive:hover {
  color: #be185d;
}

/* 卡片样式 */
.theme-dark .theme-card {
  background-color: rgba(30, 41, 59, 0.6);
  border-color: #334155;
}
.theme-light .theme-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: #e2e8f0;
}
.theme-pink .theme-card {
  background-color: rgba(255, 255, 255, 0.8);
  border-color: #fbcfe8;
}

/* 卡片 hover 效果 */
.theme-dark .theme-card-hover:hover {
  border-color: rgba(99, 102, 241, 0.5);
}
.theme-light .theme-card-hover:hover {
  border-color: rgba(79, 70, 229, 0.4);
}
.theme-pink .theme-card-hover:hover {
  border-color: rgba(236, 72, 153, 0.4);
}

/* 骨架屏样式 */
.theme-dark .theme-skeleton {
  background-color: #334155;
}
.theme-light .theme-skeleton {
  background-color: #e2e8f0;
}
.theme-pink .theme-skeleton {
  background-color: #fbcfe8;
}

/* === 社区页面主题样式 === */

/* 搜索框 */
.theme-dark .theme-search-input {
  background-color: rgba(15, 23, 42, 0.85);
  border-color: #475569;
  color: #ffffff;
  caret-color: #6366f1;
}
.theme-dark .theme-search-input::placeholder {
  color: #94a3b8;
  opacity: 1;
}
.theme-dark .theme-search-input:focus {
  border-color: rgba(99, 102, 241, 0.7);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}
.theme-light .theme-search-input {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: #e2e8f0;
  color: #1e293b;
}
.theme-light .theme-search-input::placeholder {
  color: #94a3b8;
  opacity: 1;
}
.theme-light .theme-search-input:focus {
  border-color: rgba(79, 70, 229, 0.4);
}
.theme-pink .theme-search-input {
  background-color: rgba(255, 255, 255, 0.8);
  border-color: #fbcfe8;
  color: #831843;
}
.theme-pink .theme-search-input::placeholder {
  color: #db2777;
  opacity: 1;
}
.theme-pink .theme-search-input:focus {
  border-color: rgba(236, 72, 153, 0.5);
}

/* Tab 激活条 */
.theme-dark .theme-tab-active-bar {
  background-color: #818cf8;
}
.theme-light .theme-tab-active-bar {
  background-color: #3B82F6;
}
.theme-pink .theme-tab-active-bar {
  background-color: #ec4899;
}

/* 筛选按钮 */
.theme-dark .theme-btn-filter {
  background-color: rgba(30, 41, 59, 0.8);
  color: #94a3b8;
  border-color: #334155;
}
.theme-dark .theme-btn-filter:hover {
  background-color: rgba(46, 58, 79, 0.8);
  color: #f8fafc;
  border-color: rgba(99, 102, 241, 0.3);
}
.theme-light .theme-btn-filter {
  background-color: rgba(255, 255, 255, 0.8);
  color: #64748b;
  border-color: #e2e8f0;
}
.theme-light .theme-btn-filter:hover {
  background-color: #ffffff;
  color: #1e293b;
  border-color: rgba(79, 70, 229, 0.3);
}
.theme-pink .theme-btn-filter {
  background-color: rgba(255, 255, 255, 0.7);
  color: #be185d;
  border-color: #fbcfe8;
}
.theme-pink .theme-btn-filter:hover {
  background-color: #ffffff;
  color: #831843;
  border-color: rgba(236, 72, 153, 0.3);
}

/* 筛选按钮激活状态 */
.theme-dark .theme-btn-filter-active {
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  color: white;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
}
.theme-light .theme-btn-filter-active {
  background: rgba(59, 130, 246, 0.12) !important;
  color: #3B82F6 !important;
  border-color: rgba(59, 130, 246, 0.2) !important;
  box-shadow: none !important;
}
.theme-pink .theme-btn-filter-active {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.25);
}

/* 分类徽章 */
.theme-dark .theme-badge-category {
  background-color: rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
}
.theme-light .theme-badge-category {
  background-color: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
}
.theme-pink .theme-badge-category {
  background-color: rgba(236, 72, 153, 0.1);
  color: #db2777;
}

/* 卡片占位图 */
.theme-dark .theme-card-placeholder {
  background-color: rgba(46, 58, 79, 0.5);
}
.theme-light .theme-card-placeholder {
  background-color: rgba(203, 213, 224, 0.5);
}
.theme-pink .theme-card-placeholder {
  background-color: rgba(251, 207, 232, 0.5);
}

/* 迷你头像 */
.theme-dark .theme-avatar-mini {
  background: linear-gradient(135deg, #6366f1, #a78bfa);
}
.theme-light .theme-avatar-mini {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
}
.theme-pink .theme-avatar-mini {
  background: linear-gradient(135deg, #ec4899, #f472b6);
}

/* 链接 hover */
.theme-dark .theme-link-hover:hover {
  color: #818cf8;
}
.theme-light .theme-link-hover:hover {
  color: #4f46e5;
}
.theme-pink .theme-link-hover:hover {
  color: #ec4899;
}

/* === 文章详情页封面样式 === */

/* 封面容器 - 更自然的颜色过渡 */
.theme-dark .theme-article-cover {
  background: linear-gradient(to top, #0f172a 0%, rgba(99, 102, 241, 0.2) 30%, rgba(147, 51, 234, 0.1) 60%, rgba(15, 23, 42, 0.6) 100%);
}
.theme-light .theme-article-cover {
  background: linear-gradient(to top, #f8fafc 0%, rgba(79, 70, 229, 0.08) 30%, rgba(147, 51, 234, 0.05) 60%, rgba(248, 250, 252, 0.8) 100%);
}
.theme-pink .theme-article-cover {
  background: linear-gradient(to top, #fdf2f8 0%, rgba(236, 72, 153, 0.25) 30%, rgba(244, 114, 182, 0.15) 60%, rgba(253, 242, 248, 0.7) 100%);
}

/* 文章卡片背景 */
.theme-dark .theme-article-card {
  background-color: rgba(30, 41, 59, 0.8);
  border-color: #334155;
}
.theme-light .theme-article-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-color: #e2e8f0;
}
.theme-pink .theme-article-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: #fbcfe8;
}

/* 文章内容区域 */
.theme-dark .theme-article-content {
  color: #e2e8f0;
}
.theme-light .theme-article-content {
  color: #334155;
}
.theme-pink .theme-article-content {
  color: #581c3a;
}

/* 返回链接 */
.theme-dark .theme-back-link {
  color: #94a3b8;
}
.theme-dark .theme-back-link:hover {
  color: #f8fafc;
}
.theme-light .theme-back-link {
  color: #64748b;
}
.theme-light .theme-back-link:hover {
  color: #1e293b;
}
.theme-pink .theme-back-link {
  color: #be185d;
}
.theme-pink .theme-back-link:hover {
  color: #831843;
}

/* 文章标题 */
.theme-dark .theme-article-title {
  color: #f8fafc;
}
.theme-light .theme-article-title {
  color: #1e293b;
}
.theme-pink .theme-article-title {
  color: #831843;
}

/* 文章作者信息 */
.theme-dark .theme-author-name {
  color: #f8fafc;
}
.theme-dark .theme-author-name:hover {
  color: #818cf8;
}
.theme-light .theme-author-name {
  color: #1e293b;
}
.theme-light .theme-author-name:hover {
  color: #4f46e5;
}
.theme-pink .theme-author-name {
  color: #831843;
}
.theme-pink .theme-author-name:hover {
  color: #ec4899;
}

.theme-dark .theme-author-meta {
  color: #94a3b8;
}
.theme-light .theme-author-meta {
  color: #64748b;
}
.theme-pink .theme-author-meta {
  color: #be185d;
}

/* 浏览量标签 */
.theme-dark .theme-view-count {
  background-color: rgba(30, 41, 59, 0.6);
  color: #cbd5e1;
}
.theme-light .theme-view-count {
  background-color: rgba(203, 213, 224, 0.5);
  color: #475569;
}
.theme-pink .theme-view-count {
  background-color: rgba(236, 72, 153, 0.15);
  color: #581c3a;
}

/* 点赞按钮 */
.theme-dark .theme-btn-like {
  background-color: rgba(30, 41, 59, 0.6);
  color: #cbd5e1;
  border-color: #334155;
}
.theme-dark .theme-btn-like:hover {
  background-color: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  border-color: rgba(99, 102, 241, 0.3);
}
.theme-dark .theme-btn-like.active {
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  color: white;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
}
.theme-light .theme-btn-like {
  background-color: rgba(203, 213, 224, 0.5);
  color: #475569;
  border-color: #e2e8f0;
}
.theme-light .theme-btn-like:hover {
  background-color: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.3);
}
.theme-light .theme-btn-like.active {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: white;
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.2);
}
.theme-pink .theme-btn-like {
  background-color: rgba(236, 72, 153, 0.15);
  color: #581c3a;
  border-color: rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-like:hover {
  background-color: rgba(236, 72, 153, 0.1);
  color: #ec4899;
  border-color: rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-like.active {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.25);
}

/* 点踩按钮 */
.theme-dark .theme-btn-dislike {
  background-color: rgba(30, 41, 59, 0.6);
  color: #cbd5e1;
  border-color: #334155;
}
.theme-dark .theme-btn-dislike:hover {
  background-color: rgba(100, 116, 139, 0.3);
  color: #94a3b8;
}
.theme-dark .theme-btn-dislike.active {
  background: linear-gradient(135deg, #475569, #64748b);
  color: white;
}
.theme-light .theme-btn-dislike {
  background-color: rgba(203, 213, 224, 0.5);
  color: #64748b;
  border-color: #e2e8f0;
}
.theme-light .theme-btn-dislike:hover {
  background-color: rgba(100, 116, 139, 0.2);
  color: #475569;
}
.theme-light .theme-btn-dislike.active {
  background: linear-gradient(135deg, #64748b, #94a3b8);
  color: white;
}
.theme-pink .theme-btn-dislike {
  background-color: rgba(236, 72, 153, 0.15);
  color: #581c3a;
  border-color: rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-dislike:hover {
  background-color: rgba(236, 72, 153, 0.2);
  color: #be185d;
}
.theme-pink .theme-btn-dislike.active {
  background: linear-gradient(135deg, #78716c, #a8a29e);
  color: white;
}

/* 标签徽章 */
.theme-dark .theme-badge-tag {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(147, 51, 234, 0.2));
  color: #a5b4fc;
  border-color: rgba(99, 102, 241, 0.3);
}
.theme-dark .theme-badge-tag:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(147, 51, 234, 0.3));
  color: #c7d2fe;
}
.theme-light .theme-badge-tag {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(99, 102, 241, 0.1));
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.2);
}
.theme-light .theme-badge-tag:hover {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.2), rgba(99, 102, 241, 0.2));
  color: #4338ca;
}
.theme-pink .theme-badge-tag {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(244, 114, 182, 0.1));
  color: #db2777;
  border-color: rgba(236, 72, 153, 0.2);
}
.theme-pink .theme-badge-tag:hover {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.2), rgba(244, 114, 182, 0.2));
  color: #be185d;
}

/* === 评论区样式 === */
.theme-dark .theme-comment-card {
  background-color: rgba(30, 41, 59, 0.6);
  border-color: #334155;
}
.theme-light .theme-comment-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-color: #e2e8f0;
}
.theme-pink .theme-comment-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: rgba(236, 72, 153, 0.3);
}

.theme-dark .theme-comment-title {
  color: #f8fafc;
}
.theme-light .theme-comment-title {
  color: #1e293b;
}
.theme-pink .theme-comment-title {
  color: #831843;
}

.theme-dark .theme-comment-content {
  color: #cbd5e1;
}
.theme-light .theme-comment-content {
  color: #475569;
}
.theme-pink .theme-comment-content {
  color: #581c3a;
}

.theme-dark .theme-comment-input {
  background-color: rgba(30, 41, 59, 0.9);
  border: 2px solid #475569;
  color: #f1f5f9;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
}
.theme-dark .theme-comment-input::placeholder {
  color: #64748b;
}
.theme-dark .theme-comment-input:focus {
  border-color: #6366f1;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2), 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.theme-light .theme-comment-input {
  background-color: #ffffff;
  border: 2px solid #cbd5e1;
  color: #1e293b;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}
.theme-light .theme-comment-input::placeholder {
  color: #94a3b8;
}
.theme-light .theme-comment-input:focus {
  border-color: #4f46e5;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05), 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.theme-pink .theme-comment-input {
  background-color: #ffffff;
  border: 2px solid rgba(236, 72, 153, 0.5);
  color: #581c3a;
  box-shadow: inset 0 2px 4px rgba(236, 72, 153, 0.1);
}
.theme-pink .theme-comment-input::placeholder {
  color: #be185d;
}
.theme-pink .theme-comment-input:focus {
  border-color: #ec4899;
  box-shadow: inset 0 2px 4px rgba(236, 72, 153, 0.1), 0 0 0 3px rgba(236, 72, 153, 0.15);
}

.theme-comment-input-sm {
  padding: 0.5rem 0.75rem !important;
}

.theme-dark .theme-comment-avatar-default {
  background-color: #475569;
  color: #94a3b8;
}
.theme-light .theme-comment-avatar-default {
  background-color: #e2e8f0;
  color: #64748b;
}
.theme-pink .theme-comment-avatar-default {
  background-color: rgba(236, 72, 153, 0.2);
  color: #be185d;
}

.theme-dark .theme-comment-avatar-hover:hover {
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.5);
}
.theme-light .theme-comment-avatar-hover:hover {
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.5);
}
.theme-pink .theme-comment-avatar-hover:hover {
  box-shadow: 0 0 0 2px rgba(236, 72, 153, 0.5);
}

.theme-dark .theme-reply-btn {
  color: #64748b;
}
.theme-dark .theme-reply-btn:hover {
  color: #818cf8;
}
.theme-light .theme-reply-btn {
  color: #64748b;
}
.theme-light .theme-reply-btn:hover {
  color: #4f46e5;
}
.theme-pink .theme-reply-btn {
  color: #be185d;
}
.theme-pink .theme-reply-btn:hover {
  color: #ec4899;
}

.theme-dark .theme-comment-reply-box {
  background-color: rgba(15, 23, 42, 0.8) !important;
  border-color: #475569 !important;
}
.theme-light .theme-comment-reply-box {
  background-color: #f8fafc !important;
  border-color: #cbd5e1 !important;
}
.theme-pink .theme-comment-reply-box {
  background-color: rgba(253, 242, 248, 0.9) !important;
  border-color: rgba(236, 72, 153, 0.3) !important;
}

.theme-dark .theme-btn-cancel {
  color: #94a3b8;
}
.theme-dark .theme-btn-cancel:hover {
  color: #f1f5f9;
}
.theme-light .theme-btn-cancel {
  color: #64748b;
}
.theme-light .theme-btn-cancel:hover {
  color: #1e293b;
}
.theme-pink .theme-btn-cancel {
  color: #be185d;
}
.theme-pink .theme-btn-cancel:hover {
  color: #831843;
}

/* === 番剧库页面样式 === */
.theme-dark .theme-primary-bg {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
}
.theme-light .theme-primary-bg {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.2);
}
.theme-pink .theme-primary-bg {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.3);
}

.theme-dark .theme-search-input {
  background-color: rgba(30, 41, 59, 0.9);
  border-color: #475569;
  color: #f1f5f9;
}
.theme-dark .theme-search-input::placeholder {
  color: #64748b;
}
.theme-light .theme-search-input {
  background-color: white;
  border-color: #e2e8f0;
  color: #1e293b;
}
.theme-light .theme-search-input::placeholder {
  color: #94a3b8;
}
.theme-pink .theme-search-input {
  background-color: white;
  border-color: rgba(236, 72, 153, 0.3);
  color: #581c3a;
}
.theme-pink .theme-search-input::placeholder {
  color: #be185d;
}

.theme-dark .theme-input {
  background-color: rgba(30, 41, 59, 0.8);
  border-color: #475569;
  color: #f1f5f9;
}
.theme-light .theme-input {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  color: #1e293b;
}
.theme-pink .theme-input {
  background-color: rgba(236, 72, 153, 0.08);
  border-color: rgba(236, 72, 153, 0.3);
  color: #581c3a;
}

.theme-dark .theme-select {
  background-color: rgba(30, 41, 59, 0.8);
  border-color: #475569;
  color: #f1f5f9;
}
.theme-light .theme-select {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  color: #1e293b;
}
.theme-pink .theme-select {
  background-color: rgba(236, 72, 153, 0.08);
  border-color: rgba(236, 72, 153, 0.3);
  color: #581c3a;
}

.theme-dark .theme-btn-secondary {
  background-color: #475569;
  color: #cbd5e1;
}
.theme-dark .theme-btn-secondary:hover {
  background-color: #64748b;
}
.theme-light .theme-btn-secondary {
  background-color: var(--bg-secondary, #f1f5f9) !important;
  color: var(--text-main, #334155) !important;
  border: 1px solid var(--border-color, #E2E8F0) !important;
}
.theme-light .theme-btn-secondary:hover {
  background-color: #cbd5e1;
}
.theme-pink .theme-btn-secondary {
  background-color: rgba(236, 72, 153, 0.2);
  color: #be185d;
}
.theme-pink .theme-btn-secondary:hover {
  background-color: rgba(236, 72, 153, 0.3);
}

.theme-btn-sm {
  padding: 0.375rem 1rem !important;
}

.theme-btn-disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
}

/* === 番剧库搜索结果卡片样式 === */
.theme-dark .theme-anime-card {
  background-color: #1e293b;
  border-color: #334155;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}
.theme-dark .theme-anime-card:hover {
  border-color: rgba(99, 102, 241, 0.6);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.15);
}
.theme-light .theme-anime-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: #e2e8f0;
}
.theme-light .theme-anime-card:hover {
  border-color: rgba(79, 70, 229, 0.4);
}
.theme-pink .theme-anime-card {
  background-color: rgba(255, 255, 255, 0.85);
  border-color: rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-anime-card:hover {
  border-color: rgba(236, 72, 153, 0.5);
}

/* 番剧卡片标题 */
.theme-dark .theme-anime-title {
  color: #f8fafc;
}
.theme-light .theme-anime-title {
  color: #1e293b;
}
.theme-pink .theme-anime-title {
  color: #831843;
}

/* 番剧卡片简介 */
.theme-dark .theme-anime-summary {
  color: #94a3b8;
}
.theme-light .theme-anime-summary {
  color: #64748b;
}
.theme-pink .theme-anime-summary {
  color: #be185d;
}

/* 番剧卡片底部边框 */
.theme-dark .theme-anime-divider {
  border-color: #334155;
}
.theme-light .theme-anime-divider {
  border-color: #e2e8f0;
}
.theme-pink .theme-anime-divider {
  border-color: rgba(236, 72, 153, 0.2);
}

/* 番剧卡片状态文字 */
.theme-dark .theme-anime-status {
  color: #64748b;
}
.theme-light .theme-anime-status {
  color: #94a3b8;
}
.theme-pink .theme-anime-status {
  color: #db2777;
}

/* === 链接文字链接 */
.theme-dark .theme-text-link {
  color: #818cf8;
}
.theme-light .theme-text-link {
  color: #4f46e5;
}
.theme-pink .theme-text-link {
  color: #db2777;
}
.theme-text-link:hover {
  text-decoration: underline;
}

/* === 加载卡片 */
.theme-dark .theme-card-loading {
  background-color: #1e293b;
}
.theme-light .theme-card-loading {
  background-color: #e2e8f0;
}
.theme-pink .theme-card-loading {
  background-color: #fbcfe8;
}

/* === 筛选按钮样式 === */
.theme-dark .theme-btn-filter {
  background-color: rgba(30, 41, 59, 0.8);
  border-color: #475569;
  color: #cbd5e1;
}
.theme-dark .theme-btn-filter:hover {
  border-color: #64748b;
}
.theme-light .theme-btn-filter {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  color: #475569;
}
.theme-light .theme-btn-filter:hover {
  border-color: #cbd5e1;
}
.theme-pink .theme-btn-filter {
  background-color: rgba(236, 72, 159, 0.1);
  border-color: rgba(236, 72, 159, 0.3);
  color: #be185d;
}
.theme-pink .theme-btn-filter:hover {
  border-color: rgba(236, 72, 159, 0.5);
}

.theme-dark .theme-btn-filter-active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-color: #6366f1;
  color: #f8fafc;
}
.theme-light .theme-btn-filter-active {
  background: rgba(59, 130, 246, 0.12) !important;
  color: #3B82F6 !important;
  border-color: rgba(59, 130, 246, 0.2) !important;
}
.theme-pink .theme-btn-filter-active {
  background: rgba(236, 72, 153, 0.12) !important;
  color: #DB2777 !important;
  border-color: rgba(236, 72, 153, 0.2) !important;
}

/* === 大尺寸点赞/点踩按钮 === */
.theme-dark .theme-btn-like-lg {
  background-color: rgba(30, 41, 59, 0.8);
  color: #94a3b8;
  border: 1px solid #475569;
}
.theme-dark .theme-btn-like-lg:hover {
  background-color: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  border-color: rgba(99, 102, 241, 0.3);
}
.theme-dark .theme-btn-like-lg.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.3);
  transform: scale(1.05);
}

.theme-light .theme-btn-like-lg {
  background-color: rgba(203, 213, 224, 0.5);
  color: #64748b;
  border: 1px solid #e2e8f0;
}
.theme-light .theme-btn-like-lg:hover {
  background-color: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  border-color: rgba(79, 70, 229, 0.3);
}
.theme-light .theme-btn-like-lg.active {
  background: linear-gradient(135deg, #3B82F6, #6366F1) !important;
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
  transform: scale(1.05);
}

.theme-pink .theme-btn-like-lg {
  background-color: rgba(236, 72, 153, 0.15);
  color: #581c3a;
  border: 1px solid rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-like-lg:hover {
  background-color: rgba(236, 72, 153, 0.2);
  color: #ec4899;
  border-color: rgba(236, 72, 153, 0.4);
}
.theme-pink .theme-btn-like-lg.active {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.25);
  transform: scale(1.05);
}

/* 大尺寸点踩按钮 */
.theme-dark .theme-btn-dislike-lg {
  background-color: rgba(30, 41, 59, 0.8);
  color: #94a3b8;
  border: 1px solid #475569;
}
.theme-dark .theme-btn-dislike-lg:hover {
  background-color: rgba(100, 116, 139, 0.2);
  color: #cbd5e1;
  border-color: #64748b;
}
.theme-dark .theme-btn-dislike-lg.active {
  background: linear-gradient(135deg, #475569, #64748b);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(71, 85, 105, 0.3);
  transform: scale(1.05);
}

.theme-light .theme-btn-dislike-lg {
  background-color: rgba(203, 213, 224, 0.5);
  color: #64748b;
  border: 1px solid #e2e8f0;
}
.theme-light .theme-btn-dislike-lg:hover {
  background-color: rgba(100, 116, 139, 0.15);
  color: #475569;
  border-color: #cbd5e1;
}
.theme-light .theme-btn-dislike-lg.active {
  background: linear-gradient(135deg, #64748b, #94a3b8);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(100, 116, 139, 0.2);
  transform: scale(1.05);
}

.theme-pink .theme-btn-dislike-lg {
  background-color: rgba(236, 72, 153, 0.15);
  color: #581c3a;
  border: 1px solid rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-dislike-lg:hover {
  background-color: rgba(100, 116, 139, 0.15);
  color: #64748b;
  border-color: rgba(100, 116, 139, 0.3);
}
.theme-pink .theme-btn-dislike-lg.active {
  background: linear-gradient(135deg, #78716c, #a8a29e);
  color: white;
  border-color: transparent;
  box-shadow: 0 10px 25px rgba(120, 113, 108, 0.2);
  transform: scale(1.05);
}

/* 分享按钮 */
.theme-dark .theme-btn-share {
  background: linear-gradient(135deg, #6366f1, #a78bfa) !important;
  color: white !important;
  border: 1px solid transparent !important;
}
.theme-dark .theme-btn-share:hover {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6) !important;
  color: white !important;
  border-color: transparent !important;
}

.theme-light .theme-btn-share {
  background: linear-gradient(135deg, #3B82F6, #6366F1) !important;
  color: white !important;
  border: 1px solid transparent !important;
}
.theme-light .theme-btn-share:hover {
  background: linear-gradient(135deg, #2563EB, #4F46E5) !important;
  color: white !important;
  border-color: transparent !important;
}

.theme-pink .theme-btn-share {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  border: 1px solid rgba(236, 72, 153, 0.5);
  box-shadow: 0 10px 25px rgba(236, 72, 153, 0.25);
}
.theme-pink .theme-btn-share:hover {
  background: linear-gradient(135deg, #db2777, #ec4899);
  border-color: #db2777;
}

/* === 写文章按钮 === */
.theme-dark .theme-btn-write {
  background-color: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  border: 1px solid rgba(99, 102, 241, 0.3);
}
.theme-dark .theme-btn-write:hover {
  background-color: rgba(99, 102, 241, 0.3);
  color: #a5b4fc;
  border-color: rgba(99, 102, 241, 0.5);
}

.theme-light .theme-btn-write {
  background-color: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  border: 1px solid rgba(79, 70, 229, 0.2);
}
.theme-light .theme-btn-write:hover {
  background-color: rgba(79, 70, 229, 0.2);
  color: #6366f1;
  border-color: rgba(79, 70, 229, 0.4);
}

.theme-pink .theme-btn-write {
  background-color: rgba(236, 72, 153, 0.2);
  color: #db2777;
  border: 1px solid rgba(236, 72, 153, 0.3);
}
.theme-pink .theme-btn-write:hover {
  background-color: rgba(236, 72, 153, 0.3);
  color: #ec4899;
  border-color: rgba(236, 72, 153, 0.5);
}

/* === 写文章页面样式 === */
.theme-dark .theme-btn-back {
  background-color: #334155;
  color: #94a3b8;
}
.theme-dark .theme-btn-back:hover {
  background-color: #475569;
  color: #cbd5e1;
}

.theme-light .theme-btn-back {
  background-color: var(--bg-secondary, #F1F5F9) !important;
  color: var(--text-muted, #64748B) !important;
}
.theme-light .theme-btn-back:hover {
  background-color: var(--border-color, #CBD5E1) !important;
  color: var(--text-main, #1E293B) !important;
}

.theme-pink .theme-btn-back {
  background-color: rgba(236, 72, 153, 0.15);
  color: #db2777;
}
.theme-pink .theme-btn-back:hover {
  background-color: rgba(236, 72, 153, 0.25);
  color: #ec4899;
}

/* 输入框字段 */
.theme-dark .theme-input-field {
  background-color: #1e293b;
  border-color: #334155;
  color: #e2e8f0;
}
.theme-dark .theme-input-field::placeholder {
  color: #64748b;
}
.theme-dark .theme-input-field:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.theme-light .theme-input-field {
  background-color: #ffffff;
  border-color: #e2e8f0;
  color: #1e293b;
}
.theme-light .theme-input-field::placeholder {
  color: #94a3b8;
}
.theme-light .theme-input-field:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}

.theme-pink .theme-input-field {
  background-color: #ffffff;
  border-color: rgba(236, 72, 153, 0.3);
  color: #581c3a;
}
.theme-pink .theme-input-field::placeholder {
  color: #a78bfa;
}
.theme-pink .theme-input-field:focus {
  border-color: #ec4899;
  box-shadow: 0 0 0 2px rgba(236, 72, 153, 0.15);
}

/* 提交按钮 */
.theme-dark .theme-btn-submit {
  background: linear-gradient(135deg, #6366f1, #a78bfa) !important;
  color: white !important;
  border: none !important;
}
.theme-dark .theme-btn-submit:hover {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6) !important;
}
.theme-light .theme-btn-submit {
  background: linear-gradient(135deg, #6366F1, #3B82F6) !important;
  color: white !important;
  border: none !important;
}
.theme-light .theme-btn-submit:hover {
  background: linear-gradient(135deg, #4F46E5, #2563EB) !important;
}
.theme-pink .theme-btn-submit {
  background: linear-gradient(135deg, #ec4899, #f472b6) !important;
  color: white !important;
  border: none !important;
}
.theme-pink .theme-btn-submit:hover {
  background: linear-gradient(135deg, #db2777, #f43f5e) !important;
}

.theme-dark .theme-btn-draft {
  background-color: rgba(71, 85, 105, 0.5) !important;
  color: #cbd5e1 !important;
  border: 1px solid rgba(100, 116, 139, 0.3) !important;
}
.theme-light .theme-btn-draft {
  background-color: rgba(241, 245, 249, 0.8) !important;
  color: var(--text-main, #334155) !important;
  border: 1px solid var(--border-color, #E2E8F0) !important;
}
.theme-pink .theme-btn-draft {
  background-color: rgba(253, 242, 248, 0.8) !important;
  color: var(--text-main, #334155) !important;
  border: 1px solid rgba(236, 72, 153, 0.2) !important;
}

/* === 聊天页面样式 === */
.theme-dark .theme-chat-bg {
  background-color: #0f172a;
}
.theme-light .theme-chat-bg {
  background-color: #f1f5f9;
}
.theme-pink .theme-chat-bg {
  background-color: #fdf2f8;
}

.theme-dark .theme-chat-header {
  background-color: #1e293b;
  border-color: #334155;
}
.theme-light .theme-chat-header {
  background-color: #ffffff;
  border-color: #e2e8f0;
}
.theme-pink .theme-chat-header {
  background-color: #fdf2f8;
  border-color: rgba(236, 72, 153, 0.2);
}

/* 自己的消息 */
.theme-dark .theme-chat-message-self {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 1rem 1rem 0.25rem 1rem;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}
.theme-light .theme-chat-message-self {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: white;
  border-radius: 1rem 1rem 0.25rem 1rem;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.2);
}
.theme-pink .theme-chat-message-self {
  background: linear-gradient(135deg, #ec4899, #f472b6);
  color: white;
  border-radius: 1rem 1rem 0.25rem 1rem;
  box-shadow: 0 4px 15px rgba(236, 72, 153, 0.25);
}

/* 对方的消息 */
.theme-dark .theme-chat-message-other {
  background-color: #1e293b;
  color: #e2e8f0;
  border-radius: 1rem 1rem 1rem 0.25rem;
  border: 1px solid #334155;
}
.theme-light .theme-chat-message-other {
  background-color: #ffffff;
  color: #334155;
  border-radius: 1rem 1rem 1rem 0.25rem;
  border: 1px solid #e2e8f0;
}
.theme-pink .theme-chat-message-other {
  background-color: #ffffff;
  color: #581c3a;
  border-radius: 1rem 1rem 1rem 0.25rem;
  border: 1px solid rgba(236, 72, 153, 0.2);
}

/* 输入区域 - 简洁无背景 */
.theme-dark .theme-chat-input-area {
  background: transparent;
  border-color: transparent;
}
.theme-light .theme-chat-input-area {
  background: transparent;
  border-color: transparent;
}
.theme-pink .theme-chat-input-area {
  background: transparent;
  border-color: transparent;
}

/* 聊天输入框 */
.theme-dark .theme-chat-input {
  background-color: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(99, 102, 241, 0.3);
  color: #e2e8f0;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}
.theme-dark .theme-chat-input::placeholder {
  color: #64748b;
}
.theme-dark .theme-chat-input:focus {
  border-color: #6366f1;
  background-color: rgba(30, 41, 59, 0.8);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15), inset 0 2px 8px rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
}

.theme-light .theme-chat-input {
  background-color: #ffffff;
  border: 1px solid rgba(79, 70, 229, 0.25);
  color: #1e293b;
  box-shadow: inset 0 2px 6px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}
.theme-light .theme-chat-input::placeholder {
  color: #94a3b8;
}
.theme-light .theme-chat-input:focus {
  border-color: #4f46e5;
  background-color: #fafafe;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12), inset 0 2px 6px rgba(0, 0, 0, 0.04);
  transform: translateY(-1px);
}

.theme-pink .theme-chat-input {
  background-color: #ffffff;
  border: 1px solid rgba(236, 72, 153, 0.35);
  color: #581c3a;
  box-shadow: inset 0 2px 6px rgba(236, 72, 153, 0.08);
  transition: all 0.3s ease;
}
.theme-pink .theme-chat-input::placeholder {
  color: #f472b6;
  opacity: 0.7;
}
.theme-pink .theme-chat-input:focus {
  border-color: #ec4899;
  background-color: #fffafc;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.15), inset 0 2px 6px rgba(236, 72, 153, 0.05);
  transform: translateY(-1px);
}

/* 发送按钮 */
.theme-dark .theme-btn-send {
  background: linear-gradient(135deg, #6366f1, #8b5cf6, #a78bfa);
  color: white;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4), 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.theme-dark .theme-btn-send:hover:not(:disabled) {
  background: linear-gradient(135deg, #4f46e5, #7c3aed, #8b5cf6);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5), 0 3px 6px rgba(0, 0, 0, 0.25);
}
.theme-dark .theme-btn-send:active:not(:disabled) {
  transform: translateY(0);
}

.theme-light .theme-btn-send {
  background: linear-gradient(135deg, #4f46e5, #6366f1, #818cf8);
  color: white;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3), 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}
.theme-light .theme-btn-send:hover:not(:disabled) {
  background: linear-gradient(135deg, #4338ca, #4f46e5, #6366f1);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4), 0 3px 6px rgba(0, 0, 0, 0.15);
}
.theme-light .theme-btn-send:active:not(:disabled) {
  transform: translateY(0);
}

.theme-pink .theme-btn-send {
  background: linear-gradient(135deg, #ec4899, #f472b6, #fb7185);
  color: white;
  box-shadow: 0 4px 15px rgba(236, 72, 153, 0.35), 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}
.theme-pink .theme-btn-send:hover:not(:disabled) {
  background: linear-gradient(135deg, #db2777, #ec4899, #f472b6);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(236, 72, 153, 0.45), 0 3px 6px rgba(0, 0, 0, 0.15);
}
.theme-pink .theme-btn-send:active:not(:disabled) {
  transform: translateY(0);
}

/* === 弹窗样式 === */
.theme-dark .theme-modal-overlay {
  background-color: rgba(0, 0, 0, 0.6);
}
.theme-light .theme-modal-overlay {
  background-color: rgba(0, 0, 0, 0.4);
}
.theme-pink .theme-modal-overlay {
  background-color: rgba(0, 0, 0, 0.4);
}

.theme-dark .theme-modal {
  background-color: #1e293b;
  border-color: #334155;
}
.theme-light .theme-modal {
  background-color: #ffffff;
  border-color: #e2e8f0;
}
.theme-pink .theme-modal {
  background-color: #ffffff;
  border-color: rgba(236, 72, 153, 0.3);
}

/* === 富文本编辑器样式 === */
.theme-dark .theme-rich-editor {
  border: 1px solid #334155;
  border-radius: 8px;
  overflow: hidden;
  background: #1e293b;
}

.theme-light .theme-rich-editor {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
}

.theme-pink .theme-rich-editor {
  border: 1px solid rgba(236, 72, 153, 0.3);
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
}

.theme-dark .theme-editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #0f172a;
  border-bottom: 1px solid #334155;
  flex-wrap: wrap;
  gap: 8px;
}

.theme-light .theme-editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  flex-wrap: wrap;
  gap: 8px;
}

.theme-pink .theme-editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fdf2f8;
  border-bottom: 1px solid rgba(236, 72, 153, 0.2);
  flex-wrap: wrap;
  gap: 8px;
}

.theme-dark .theme-mode-switch {
  padding: 4px 10px;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-mode-switch:hover {
  background: #4f46e5;
}

.theme-light .theme-mode-switch {
  padding: 4px 10px;
  background: linear-gradient(135deg, #6366F1, #3B82F6) !important;
  color: white !important;
  border: none !important;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.theme-light .theme-mode-switch:hover {
  background: linear-gradient(135deg, #4F46E5, #2563EB) !important;
}

.theme-pink .theme-mode-switch {
  padding: 4px 10px;
  background: #ec4899;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-mode-switch:hover {
  background: #db2777;
}

.theme-dark .theme-toolbar-divider {
  width: 1px;
  height: 20px;
  background: #334155;
  margin: 0 4px;
}

.theme-light .theme-toolbar-divider {
  width: 1px;
  height: 20px;
  background: #e2e8f0;
  margin: 0 4px;
}

.theme-pink .theme-toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(236, 72, 153, 0.2);
  margin: 0 4px;
}

.theme-dark .theme-toolbar-btn {
  padding: 6px 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-dark .theme-toolbar-btn:hover {
  background: #334155;
  color: white;
}

.theme-light .theme-toolbar-btn {
  padding: 6px 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-light .theme-toolbar-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.theme-pink .theme-toolbar-btn {
  padding: 6px 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #be185d;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-pink .theme-toolbar-btn:hover {
  background: rgba(236, 72, 153, 0.15);
  color: #831843;
}

.theme-dark .theme-file-upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #334155;
  border: none;
  border-radius: 4px;
  color: #cbd5e1;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-file-upload-btn:hover {
  background: #475569;
  color: white;
}

.theme-light .theme-file-upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #e2e8f0;
  border: none;
  border-radius: 4px;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-light .theme-file-upload-btn:hover {
  background: #cbd5e1;
  color: #1e293b;
}

.theme-pink .theme-file-upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(236, 72, 153, 0.15);
  border: none;
  border-radius: 4px;
  color: #be185d;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-file-upload-btn:hover {
  background: rgba(236, 72, 153, 0.25);
  color: #831843;
}

.theme-dark .theme-markdown-textarea {
  flex: 1;
  width: 100%;
  height: 100%;
  padding: 16px;
  background: #0f172a;
  border: none;
  color: #e2e8f0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
}

.theme-light .theme-markdown-textarea {
  flex: 1;
  width: 100%;
  height: 100%;
  padding: 16px;
  background: #ffffff;
  border: none;
  color: #1e293b;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
}

.theme-pink .theme-markdown-textarea {
  flex: 1;
  width: 100%;
  height: 100%;
  padding: 16px;
  background: #ffffff;
  border: none;
  color: #581c3a;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
}

.theme-dark .theme-editor-preview {
  flex: 1;
  padding: 16px;
  background: #1e293b;
  border-left: 1px solid #334155;
  color: #e2e8f0;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.theme-dark .theme-editor-preview h1,
.theme-dark .theme-editor-preview h2,
.theme-dark .theme-editor-preview h3 {
  color: #f8fafc;
  margin: 16px 0 8px;
}

.theme-dark .theme-editor-preview blockquote {
  border-left: 3px solid #6366f1;
  padding-left: 12px;
  color: #94a3b8;
  margin: 8px 0;
}

.theme-dark .theme-editor-preview code {
  background: #334155;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.theme-dark .theme-editor-preview pre {
  background: #0f172a;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.theme-light .theme-editor-preview {
  flex: 1;
  padding: 16px;
  background: #f8fafc;
  border-left: 1px solid #e2e8f0;
  color: #334155;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.theme-light .theme-editor-preview h1,
.theme-light .theme-editor-preview h2,
.theme-light .theme-editor-preview h3 {
  color: #1e293b;
  margin: 16px 0 8px;
}

.theme-light .theme-editor-preview blockquote {
  border-left: 3px solid #4f46e5;
  padding-left: 12px;
  color: #64748b;
  margin: 8px 0;
}

.theme-light .theme-editor-preview code {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.theme-light .theme-editor-preview pre {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.theme-pink .theme-editor-preview {
  flex: 1;
  padding: 16px;
  background: #fdf2f8;
  border-left: 1px solid rgba(236, 72, 153, 0.2);
  color: #581c3a;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.theme-pink .theme-editor-preview h1,
.theme-pink .theme-editor-preview h2,
.theme-pink .theme-editor-preview h3 {
  color: #831843;
  margin: 16px 0 8px;
}

.theme-pink .theme-editor-preview blockquote {
  border-left: 3px solid #ec4899;
  padding-left: 12px;
  color: #be185d;
  margin: 8px 0;
}

.theme-pink .theme-editor-preview code {
  background: rgba(236, 72, 153, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.theme-pink .theme-editor-preview pre {
  background: rgba(236, 72, 153, 0.1);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.theme-dark .theme-contenteditable-editor {
  min-height: 400px;
  padding: 16px;
  background: #0f172a;
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  overflow-y: auto;
}

.theme-dark .theme-contenteditable-editor:empty::before {
  content: attr(placeholder);
  color: #64748b;
  pointer-events: none;
}

.theme-dark .theme-contenteditable-editor img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.theme-dark .theme-contenteditable-editor a {
  color: #818cf8;
  text-decoration: underline;
}

.theme-dark .theme-contenteditable-editor blockquote {
  border-left: 3px solid #6366f1;
  padding-left: 12px;
  color: #94a3b8;
  margin: 8px 0;
}

.theme-dark .theme-contenteditable-editor ul,
.theme-dark .theme-contenteditable-editor ol {
  padding-left: 24px;
  margin: 8px 0;
}

.theme-light .theme-contenteditable-editor {
  min-height: 400px;
  padding: 16px;
  background: #ffffff;
  color: #1e293b;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  overflow-y: auto;
}

.theme-light .theme-contenteditable-editor:empty::before {
  content: attr(placeholder);
  color: #94a3b8;
  pointer-events: none;
}

.theme-light .theme-contenteditable-editor img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.theme-light .theme-contenteditable-editor a {
  color: #4f46e5;
  text-decoration: underline;
}

.theme-light .theme-contenteditable-editor blockquote {
  border-left: 3px solid #4f46e5;
  padding-left: 12px;
  color: #64748b;
  margin: 8px 0;
}

.theme-light .theme-contenteditable-editor ul,
.theme-light .theme-contenteditable-editor ol {
  padding-left: 24px;
  margin: 8px 0;
}

.theme-pink .theme-contenteditable-editor {
  min-height: 400px;
  padding: 16px;
  background: #ffffff;
  color: #581c3a;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  overflow-y: auto;
}

.theme-pink .theme-contenteditable-editor:empty::before {
  content: attr(placeholder);
  color: #f472b6;
  opacity: 0.7;
  pointer-events: none;
}

.theme-pink .theme-contenteditable-editor img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.theme-pink .theme-contenteditable-editor a {
  color: #ec4899;
  text-decoration: underline;
}

.theme-pink .theme-contenteditable-editor blockquote {
  border-left: 3px solid #ec4899;
  padding-left: 12px;
  color: #be185d;
  margin: 8px 0;
}

.theme-pink .theme-contenteditable-editor ul,
.theme-pink .theme-contenteditable-editor ol {
  padding-left: 24px;
  margin: 8px 0;
}

/* === 标签选择器样式 === */
.theme-dark .theme-selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  border-radius: 9999px;
  font-size: 14px;
}

.theme-dark .theme-remove-tag-btn {
  color: #94a3b8;
  cursor: pointer;
}

.theme-dark .theme-remove-tag-btn:hover {
  color: white;
}

.theme-light .theme-selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  border-radius: 9999px;
  font-size: 14px;
}

.theme-light .theme-remove-tag-btn {
  color: #64748b;
  cursor: pointer;
}

.theme-light .theme-remove-tag-btn:hover {
  color: #1e293b;
}

.theme-pink .theme-selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(236, 72, 153, 0.15);
  color: #ec4899;
  border-radius: 9999px;
  font-size: 14px;
}

.theme-pink .theme-remove-tag-btn {
  color: #be185d;
  cursor: pointer;
}

.theme-pink .theme-remove-tag-btn:hover {
  color: #831843;
}

.theme-dark .theme-custom-input-area {
  padding: 12px;
  background: #1e293b;
  border-radius: 8px;
  border: 1px solid #334155;
  margin-bottom: 12px;
}

.theme-light .theme-custom-input-area {
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin-bottom: 12px;
}

.theme-pink .theme-custom-input-area {
  padding: 12px;
  background: rgba(253, 242, 248, 0.9);
  border-radius: 8px;
  border: 1px solid rgba(236, 72, 153, 0.2);
  margin-bottom: 12px;
}

.theme-dark .theme-custom-tag-input {
  flex: 1;
  padding: 8px 12px;
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 6px;
  color: #e2e8f0;
  font-size: 14px;
  outline: none;
}

.theme-dark .theme-custom-tag-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.theme-light .theme-custom-tag-input {
  flex: 1;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid var(--border-color, #E2E8F0) !important;
  border-radius: 6px;
  color: var(--text-main, #1E293B) !important;
  font-size: 14px;
  outline: none;
}
.theme-light .theme-custom-tag-input:focus {
  border-color: var(--theme-primary, #6366F1) !important;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15) !important;
}

.theme-pink .theme-custom-tag-input {
  flex: 1;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid rgba(236, 72, 153, 0.3);
  border-radius: 6px;
  color: #581c3a;
  font-size: 14px;
  outline: none;
}

.theme-pink .theme-custom-tag-input:focus {
  border-color: #ec4899;
  box-shadow: 0 0 0 2px rgba(236, 72, 153, 0.2);
}

.theme-dark .theme-add-tag-btn {
  padding: 8px 16px;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-add-tag-btn:hover {
  background: #4f46e5;
}

.theme-dark .theme-add-tag-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theme-light .theme-add-tag-btn {
  padding: 8px 16px;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-light .theme-add-tag-btn:hover {
  background: #4338ca;
}

.theme-light .theme-add-tag-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theme-pink .theme-add-tag-btn {
  padding: 8px 16px;
  background: #ec4899;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-add-tag-btn:hover {
  background: #db2777;
}

.theme-pink .theme-add-tag-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theme-dark .theme-cancel-tag-btn {
  padding: 8px 16px;
  background: #334155;
  color: #94a3b8;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-cancel-tag-btn:hover {
  background: #475569;
  color: white;
}

.theme-light .theme-cancel-tag-btn {
  padding: 8px 16px;
  background: #e2e8f0;
  color: #64748b;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-light .theme-cancel-tag-btn:hover {
  background: #cbd5e1;
  color: #1e293b;
}

.theme-pink .theme-cancel-tag-btn {
  padding: 8px 16px;
  background: rgba(236, 72, 153, 0.15);
  color: #be185d;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-cancel-tag-btn:hover {
  background: rgba(236, 72, 153, 0.25);
  color: #831843;
}

.theme-dark .theme-preset-tag {
  padding: 4px 10px;
  background: #334155;
  color: #94a3b8;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-preset-tag:hover {
  background: #475569;
  color: white;
}

.theme-light .theme-preset-tag {
  padding: 4px 10px;
  background: var(--bg-secondary, #F1F5F9) !important;
  color: var(--text-muted, #64748B) !important;
  border: 1px solid var(--border-color, #E2E8F0) !important;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.theme-light .theme-preset-tag:hover {
  background: rgba(99, 102, 241, 0.1) !important;
  color: var(--theme-primary, #6366F1) !important;
  border-color: rgba(99, 102, 241, 0.2) !important;
}

.theme-pink .theme-preset-tag {
  padding: 4px 10px;
  background: rgba(236, 72, 153, 0.15);
  color: #be185d;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-preset-tag:hover {
  background: rgba(236, 72, 153, 0.25);
  color: #831843;
}

.theme-dark .theme-other-tag-btn,
.theme-dark .theme-add-custom-tag-btn {
  padding: 4px 10px;
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-dark .theme-other-tag-btn:hover,
.theme-dark .theme-add-custom-tag-btn:hover {
  background: rgba(245, 158, 11, 0.25);
}

.theme-light .theme-other-tag-btn,
.theme-light .theme-add-custom-tag-btn {
  padding: 4px 10px;
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-light .theme-other-tag-btn:hover,
.theme-light .theme-add-custom-tag-btn:hover {
  background: rgba(245, 158, 11, 0.2);
}

.theme-pink .theme-other-tag-btn,
.theme-pink .theme-add-custom-tag-btn {
  padding: 4px 10px;
  background: rgba(245, 158, 11, 0.15);
  color: #d97706;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-pink .theme-other-tag-btn:hover,
.theme-pink .theme-add-custom-tag-btn:hover {
  background: rgba(245, 158, 11, 0.25);
}

/* === 首页轮播组件样式 === */
/* 轮播标签 */
.theme-dark .theme-carousel-tag {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.9), rgba(139, 92, 246, 0.9));
  color: white;
  border: 1px solid rgba(99, 102, 241, 0.5);
}

.theme-light .theme-carousel-tag {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.9), rgba(99, 102, 241, 0.9));
  color: white;
  border: 1px solid rgba(79, 70, 229, 0.3);
}

.theme-pink .theme-carousel-tag {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.9), rgba(244, 114, 182, 0.9));
  color: white;
  border: 1px solid rgba(236, 72, 153, 0.4);
}

/* 轮播标题 */
.theme-dark .theme-carousel-title {
  color: #ffffff;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.5), 0 4px 8px rgba(0, 0, 0, 0.3);
}

.theme-light .theme-carousel-title {
  color: #1e293b;
  text-shadow: 0 2px 15px rgba(0, 0, 0, 0.15), 0 4px 8px rgba(0, 0, 0, 0.1);
}

.theme-pink .theme-carousel-title {
  color: #831843;
  text-shadow: 0 2px 15px rgba(236, 72, 153, 0.3), 0 4px 8px rgba(236, 72, 153, 0.2);
}

/* 轮播描述 */
.theme-dark .theme-carousel-desc {
  color: #f1f5f9;
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.4);
}

.theme-light .theme-carousel-desc {
  color: #334155;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.1);
}

.theme-pink .theme-carousel-desc {
  color: #581c3a;
  text-shadow: 0 1px 8px rgba(236, 72, 153, 0.15);
}

/* ═══════════════════════════════════════════
   AI 助手聊天页 — 细节质感优化（4 项）
   ═══════════════════════════════════════════ */

/* —— 任务 1：彻底隐藏滚动条但保留原生滚动（聊天区 & 侧边栏通用）——
   参考 Gemini 沉浸式体验：滚动条不可见，滚轮 / 触控滑动照常工作。 */
.hide-scrollbar-container {
  overflow-y: auto;
  scrollbar-width: none;          /* Firefox */
  -ms-overflow-style: none;       /* IE / 旧 Edge */
}
.hide-scrollbar-container::-webkit-scrollbar {
  display: none;                  /* WebKit (Chrome / Safari / 新版 Edge) */
  width: 0;
  height: 0;
}

/* —— 任务 2：侧边栏与聊天区的极细微分割线（深色模式为微弱高光线） —— */
.agent-sidebar {
  border-right: 1px solid var(--theme-border-color);
}

/* —— 任务 2：顶部 Header 毛玻璃 + 边界感 —— */
.agent-header {
  background-color: rgba(var(--bg-main-rgb), 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(128, 128, 128, 0.1);
}

/* —— 任务 3：清空按钮 Hover 圆形背景强化 —— */
.agent-clear-btn {
  border-radius: 9999px;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.theme-dark .agent-clear-btn:hover {
  background-color: rgba(99, 102, 241, 0.15);
  color: #818cf8;
}
.theme-light .agent-clear-btn:hover {
  background-color: rgba(79, 70, 229, 0.12);
  color: #4f46e5;
}
.theme-pink .agent-clear-btn:hover {
  background-color: rgba(236, 72, 153, 0.12);
  color: #ec4899;
}

/* —— 任务 3：清空按钮 + 输入框 聚合为统一胶囊外底板 —— */
.agent-input-capsule {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.agent-input-capsule:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent) 15%, transparent);
}
/* 覆盖默认主题对 textarea 的强制背景（保持胶囊内透明） */
.agent-input-capsule textarea.agent-textarea {
  background-color: transparent !important;
  border-color: transparent !important;
  color: var(--text-main);
}
.agent-input-capsule textarea.agent-textarea::placeholder {
  opacity: 0.4;
}

/* —— 任务 4：空状态视觉重心上移（物理中心略偏上） —— */
.agent-empty-state {
  transform: translateY(-8%);
}
</style>
