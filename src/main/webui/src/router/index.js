import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'ativos',
          name: 'ativos',
          component: () => import('../views/AtivosView.vue')
        },
        {
          path: 'exchanges',
          name: 'exchanges',
          component: () => import('../views/ExchangesView.vue')
        },
        {
          path: 'redes',
          name: 'redes',
          component: () => import('../views/RedesView.vue')
        },
        {
          path: 'parametrizacoes',
          name: 'parametrizacoes',
          component: () => import('../views/ParametrizacoesView.vue')
        },
        {
          path: 'historicos',
          name: 'historicos',
          component: () => import('../views/HistoricosView.vue')
        }
      ]
    }
  ]
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else if (to.name === 'login' && token) {
    next('/dashboard');
  } else {
    next();
  }
})

export default router
