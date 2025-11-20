import type { App } from 'vue';
import { router } from "/@/router";
import type { RouteRecordRaw } from "vue-router";
import { LAYOUT } from "@/router/constant";

const ChatRoutes: RouteRecordRaw[] = [
  {
    path: "/ai/app/chat/:appId",
    name: "ai-chat-@appId-@modeType",
    component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
    meta: {
      title: 'AI Chat',
      ignoreAuth: true,
    },
  },  
  {
    path: "/ai/app/chatIcon/:appId",
    name: "ai-chatIcon-@appId",
    component: () => import("/@/views/super/airag/aiapp/chat/AiChatIcon.vue"),
    meta: {
      title: 'AI Chat',
      ignoreAuth: true,
    },
  },
  {
    path: '/ai/chat',
    name: 'aiChat',
    component: LAYOUT,
    meta: {
      title: 'AI Chat',
    },
    children: [
      {
        path: "/ai/chat/:appId",
        name: "ai-chat-@appId",
        component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
        meta: {
          title:'AI Assistant',
          ignoreAuth: false,
        },
      },
      {
        path: "/ai/chat",
        name: "ai-chat",
        component: () => import("/@/views/super/airag/aiapp/chat/AiChat.vue"),
        meta: {
          title:'AI Assistant',
          ignoreAuth: false,
        },
      }
    ],
  },
]

/** Register routes */
export async function register(app: App) {
  await registerMyAppRouter(app);
  console.log('[Chat routes] Registration completed!');
}

async function registerMyAppRouter(_: App) {
  for(let appRoute of ChatRoutes){
    await router.addRoute(appRoute);
  }
}
