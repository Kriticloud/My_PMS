import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue'),
      },
      {
        path: 'rooms',
        name: 'Rooms',
        component: () => import('../views/RoomsView.vue'),
        meta: { roles: ['ADMIN', 'FRONT_DESK'] },
      },
      {
        path: 'bookings',
        name: 'Bookings',
        component: () => import('../views/BookingsView.vue'),
        meta: { roles: ['ADMIN', 'FRONT_DESK'] },
      },
      {
        path: 'guests',
        name: 'Guests',
        component: () => import('../views/GuestsView.vue'),
        meta: { roles: ['ADMIN', 'FRONT_DESK'] },
      },
      {
        path: 'pos',
        name: 'POS',
        component: () => import('../views/PosView.vue'),
        meta: { roles: ['ADMIN', 'RESTAURANT_STAFF'] },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('../views/ReportsView.vue'),
        meta: { roles: ['ADMIN'] },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard for auth & RBAC
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'Login') {
      return next({ name: 'Dashboard' })
    }
    return next()
  }

  if (!auth.isAuthenticated) {
    return next({ name: 'Login' })
  }

  if (to.meta.roles && !to.meta.roles.includes(auth.userRole)) {
    return next({ name: 'Dashboard' })
  }

  next()
})

export default router
