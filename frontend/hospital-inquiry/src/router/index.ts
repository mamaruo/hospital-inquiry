import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '@/views/LoginPage.vue'
import SignUpPage from '@/views/SignUpPage.vue'
import HomeView from '@/views/HomeView.vue'
import VisitorDashboard from '@/views/VisitorDashboard.vue'
import { useAuthStore } from '@/stores/auth'
import { pinia } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => {
        const authStore = useAuthStore(pinia)
        return authStore.isAuthenticated ? '/visitor' : '/login'
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignUpPage,
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/visitor',
      name: 'visitor-dashboard',
      component: VisitorDashboard,
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore(pinia)
  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth)
  const isAuthRoute = to.name === 'login' || to.name === 'signup'

  if (requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' })
    return
  }

  if (isAuthRoute && authStore.isAuthenticated) {
    next({ path: '/visitor' })
    return
  }

  next()
})

export default router