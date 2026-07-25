// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-04-03',
  devtools: { enabled: true },

  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt'
  ],

  css: ['~/assets/css/tailwind.css'],

  // 运行时配置，区分服务端（SSR内部调用）和公开配置（客户端可读）
  // 注意：Nuxt 3 会自动将环境变量映射到 runtimeConfig：
  //   NUXT_API_INTERNAL_BASE  -> runtimeConfig.apiInternalBase
  //   NUXT_PUBLIC_API_BASE    -> runtimeConfig.public.apiBase
  // 生产部署时设置对应环境变量即可覆盖，无需修改此文件
  runtimeConfig: {
    // 服务端私有配置（SSR 直接访问后端内网地址，避免走公网）
    apiInternalBase: 'http://localhost:18083',
    // 客户端公开配置（CSR 通过 Nitro 代理路径访问，解决跨域）
    public: {
      apiBase: '/api-proxy'
    }
  },

  // 显式指定 Nitro 监听地址与端口（Railway 部署必填）
  // Railway 通过 $PORT 分配端口，必须监听 0.0.0.0 且用该端口，否则健康检查连不上 → 502
  nitro: {
    server: {
      host: '0.0.0.0',
      port: Number(process.env.PORT) || 3000
    }
  },

  // 路由渲染策略：混合模式 SSR + SWR + ISR
  routeRules: {
    // 首页：服务端渲染 + SWR 60秒缓存，兼顾 SEO 与时效性
    '/': { ssr: true, swr: 60 },
    // 番剧列表：SWR 120秒
    '/anime': { ssr: true, swr: 120 },
    // 番剧详情页：ISR，预渲染后按需更新，有利于 SEO
    '/anime/**': { ssr: true, isr: 3600 },
    // /api-proxy 代理由 server/middleware/api-proxy.ts 动态处理
    // （从 runtimeConfig.apiInternalBase 读后端地址，支持 Railway 部署）
    '/api-proxy/**': { ssr: false }
  },

  app: {
    head: {
      title: 'ACG Space - 动漫分享与资讯社区',
      htmlAttrs: { lang: 'zh-CN' },
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: '为您提供最新最热的动漫资讯、番剧推荐与深度文章。' },
        { name: 'keywords', content: '动漫,番剧,ACG,二次元,漫画,资讯' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ]
    }
  }
})

