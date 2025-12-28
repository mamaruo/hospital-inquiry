import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '@/views/LoginPage.vue'
import SignUpPage from '@/views/SignUpPage.vue'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'
import { pinia } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: () => {
        const authStore = useAuthStore(pinia)
        if (!authStore.isAuthenticated) return '/login'
        // 根据角色重定向
        switch (authStore.userRole) {
          case 'DOCTOR':
            return '/doctor'
          case 'ADMIN':
            return '/admin'
          default:
            return '/patient'
        }
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
    // 患者端路由
    {
      path: '/patient',
      name: 'patient-dashboard',
      component: () => import('@/views/patient/PatientDashboard.vue'),
      meta: { requiresAuth: true, role: 'PATIENT' },
      children: [
        {
          path: '',
          name: 'patient-home',
          component: () => import('@/views/patient/PatientHome.vue'),
        },
        {
          path: 'profiles',
          name: 'patient-profiles',
          component: () => import('@/views/patient/PatientProfiles.vue'),
        },
        {
          path: 'inquiries',
          name: 'patient-inquiries',
          component: () => import('@/views/patient/PatientInquiries.vue'),
        },
        {
          path: 'new-inquiry',
          name: 'new-inquiry',
          component: () => import('@/views/patient/NewInquiry.vue'),
        },
        {
          path: 'chat/:id',
          name: 'patient-chat',
          component: () => import('@/views/chat/ChatView.vue'),
        },
        {
          path: 'doctors',
          name: 'patient-doctors',
          component: () => import('@/views/patient/DoctorList.vue'),
        },
      ],
    },
    // 医生端路由
    {
      path: '/doctor',
      name: 'doctor-dashboard',
      component: () => import('@/views/doctor/DoctorDashboard.vue'),
      meta: { requiresAuth: true, role: 'DOCTOR' },
      children: [
        {
          path: '',
          name: 'doctor-home',
          component: () => import('@/views/doctor/DoctorHome.vue'),
        },
        {
          path: 'pending',
          name: 'doctor-pending',
          component: () => import('@/views/doctor/PendingInquiries.vue'),
        },
        {
          path: 'in-progress',
          name: 'doctor-in-progress',
          component: () => import('@/views/doctor/InProgressInquiries.vue'),
        },
        {
          path: 'chat/:id',
          name: 'doctor-chat',
          component: () => import('@/views/chat/ChatView.vue'),
        },
      ],
    },
    // 管理员端路由
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: '',
          name: 'admin-home',
          component: () => import('@/views/admin/AdminHome.vue'),
        },
        {
          path: 'departments',
          name: 'admin-departments',
          component: () => import('@/views/admin/DepartmentManagement.vue'),
        },
        {
          path: 'patients',
          name: 'admin-patients',
          component: () => import('@/views/admin/PatientManagement.vue'),
        },
        {
          path: 'doctors',
          name: 'admin-doctors',
          component: () => import('@/views/admin/DoctorManagement.vue'),
        },
      ],
    },
    // 兼容旧路由
    {
      path: '/visitor',
      redirect: '/patient',
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
    next({ path: '/' })
    return
  }

  next()
})

export default router