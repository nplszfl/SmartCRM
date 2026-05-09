<template>
  <div id="app" class="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <h1 class="app-title">SmartITSM</h1>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="currentRoute"
          router
          class="app-menu"
        >
          <el-menu-item index="/tickets">
            <el-icon><Ticket /></el-icon>
            <span>Tickets</span>
          </el-menu-item>
          <el-menu-item index="/kanban">
            <el-icon><Menu /></el-icon>
            <span>Kanban</span>
          </el-menu-item>
          <el-menu-item index="/ai-insights">
            <el-icon><MagicStick /></el-icon>
            <span>AI Insights</span>
          </el-menu-item>
          <el-menu-item index="/admin">
            <el-icon><Setting /></el-icon>
            <span>Admin</span>
          </el-menu-item>
        </el-menu>
        <div class="header-right">
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar :size="32">U</el-avatar>
              <span class="username">User</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>Profile</el-dropdown-item>
                <el-dropdown-item>Settings</el-dropdown-item>
                <el-dropdown-item divided>Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  height: 100%;
}

.el-container {
  height: 100%;
}

.app-header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.app-title {
  font-size: 20px;
  font-weight: 700;
  color: #409EFF;
  margin: 0;
}

.app-menu {
  flex: 1;
  border-bottom: none;
}

.app-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
}

.app-menu .el-menu-item .el-icon {
  margin-right: 4px;
}

.header-right {
  margin-left: auto;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.app-main {
  background: #f5f7fa;
  padding: 0;
  overflow-y: auto;
}
</style>
