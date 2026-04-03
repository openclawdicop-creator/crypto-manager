<template>
  <div class="dashboard-layout">
    <!-- Overlay for mobile sidebar -->
    <div v-if="isSidebarOpen" class="sidebar-overlay" @click="closeSidebar"></div>

    <aside class="sidebar" :class="{ 'sidebar-open': isSidebarOpen }">
      <div class="sidebar-header">
        <h2>Crypto Manager</h2>
        <button class="close-sidebar-btn" @click="closeSidebar">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/dashboard" class="nav-item" active-class="active" @click="closeSidebar">Dashboard</router-link>
        <router-link to="/redes" class="nav-item" active-class="active" @click="closeSidebar">Redes</router-link>
        <router-link to="/exchanges" class="nav-item" active-class="active" @click="closeSidebar">Exchanges</router-link>
        <router-link to="/proxies" class="nav-item" active-class="active" @click="closeSidebar">Proxies</router-link>
        <router-link to="/ativos" class="nav-item" active-class="active" @click="closeSidebar">Ativos</router-link>
        <router-link to="/parametrizacoes" class="nav-item" active-class="active" @click="closeSidebar">Parametrizações</router-link>
        <router-link to="/agendamentos" class="nav-item" active-class="active" @click="closeSidebar">Agendamentos</router-link>
        <router-link to="/historicos" class="nav-item" active-class="active" @click="closeSidebar">Histórico</router-link>
        <router-link to="/sql-manager" class="nav-item" active-class="active" @click="closeSidebar">SQL Manager</router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="logout-btn">Sair da Conta</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="top-header-left">
          <button class="mobile-menu-btn" @click="toggleSidebar">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
          </button>
          <h1 class="page-title">{{ routeTitle }}</h1>
        </div>
        <div class="user-profile">
          <span>Olá, {{ user.username }}</span>
          <div class="avatar"></div>
        </div>
      </header>

      <!-- Aqui o Vue renderiza a página de acordo com a rota selecionada -->
      <router-view></router-view>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const user = ref({ username: 'Carregando...' })
const isSidebarOpen = ref(false)

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

const closeSidebar = () => {
  isSidebarOpen.value = false
}

// Compute the title dynamically based on path to put on the header
const routeTitle = computed(() => {
  if (route.meta?.title) return route.meta.title

  const path = route.path.replace('/', '')
  if (!path || path === 'dashboard') return 'Dashboard Geral'
  // Capitalizes first letter of path
  return path.charAt(0).toUpperCase() + path.slice(1)
})
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f8fafc;
  color: #334155;
  font-family: 'Inter', sans-serif;
  overflow-x: hidden;
}

.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(to right, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.close-sidebar-btn {
  display: none;
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 0;
}

.sidebar-nav {
  padding: 1.5rem 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  overflow-y: auto;
}

.nav-item {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  color: #64748b;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.nav-item.active {
  background: #e0f2fe;
  color: #0284c7;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.logout-btn {
  width: 100%;
  padding: 0.75rem;
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fef2f2;
  color: #ef4444;
  border-color: #fca5a5;
}

.main-content {
  flex: 1;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  overflow: hidden; /* prevents layout breaking from table overflow */
}

.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  color: #334155;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
}

.mobile-menu-btn:hover {
  background: #e2e8f0;
}

.page-title {
  margin: 0;
  font-size: 1.8rem;
  color: #0f172a;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-weight: 500;
  color: #475569;
}

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%);
  border-radius: 50%;
  box-shadow: 0 4px 6px rgba(79, 172, 254, 0.3);
}

.sidebar-overlay {
  display: none;
}

/* Responsividade Mobile */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
  }

  .sidebar.sidebar-open {
    transform: translateX(0);
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(15, 23, 42, 0.4);
    backdrop-filter: blur(2px);
    z-index: 999;
  }

  .close-sidebar-btn {
    display: block;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .main-content {
    padding: 1.5rem;
    gap: 1.5rem;
  }

  .page-title {
    font-size: 1.4rem;
  }

  .user-profile span {
    display: none; /* Hide username on small screens, keep avatar */
  }

  .avatar {
    width: 32px;
    height: 32px;
  }
}
</style>
