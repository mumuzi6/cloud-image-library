<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://github.com/mumuzi6" target="_blank"> Kryos </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from "@/components/GlobalSider.vue";
</script>

<style scoped>
#basicLayout {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

#basicLayout .header {
  padding-inline: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: unset;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

#basicLayout .header:hover {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

#basicLayout .sider {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-right: 1px solid rgba(230, 230, 230, 0.5);
  padding-top: 20px;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.08);
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
  background: transparent;
}

#basicLayout :deep(.ant-menu-item) {
  border-radius: 8px;
  margin: 4px 8px;
  transition: all 0.3s ease;
}

#basicLayout :deep(.ant-menu-item:hover) {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  transform: translateX(4px);
}

#basicLayout :deep(.ant-menu-item-selected) {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
  color: #667eea;
  font-weight: 600;
}

#basicLayout .content {
  padding: 0;
  background: transparent;
  margin-bottom: 0;
  min-height: calc(100vh - 120px);
}

#basicLayout .footer {
  background: rgba(239, 239, 239, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 16px;
  text-align: center;
  border-top: 1px solid rgba(230, 230, 230, 0.5);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
}

#basicLayout .footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

#basicLayout .footer a:hover {
  color: #764ba2;
  text-shadow: 0 2px 4px rgba(118, 75, 162, 0.3);
}

/* 滚动条美化 */
#basicLayout :deep(::-webkit-scrollbar) {
  width: 8px;
}

#basicLayout :deep(::-webkit-scrollbar-track) {
  background: rgba(240, 240, 240, 0.5);
  border-radius: 4px;
}

#basicLayout :deep(::-webkit-scrollbar-thumb) {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: all 0.3s ease;
}

#basicLayout :deep(::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}
</style>
