<template>
  <div class="min-h-screen flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-gray-900 text-white flex flex-col">
      <div class="p-6 border-b border-gray-700">
        <h1 class="text-xl font-bold">PMS</h1>
        <p class="text-gray-400 text-sm">Property Management</p>
      </div>

      <nav class="flex-1 p-4 space-y-1">
        <router-link
          v-for="item in filteredNav"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-4 py-3 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition"
          active-class="bg-indigo-600 text-white"
        >
          <span class="text-lg mr-3">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-gray-700">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium">{{ auth.user?.fullName }}</p>
            <p class="text-xs text-gray-400">{{ auth.userRole }}</p>
          </div>
          <button
            @click="handleLogout"
            class="text-gray-400 hover:text-red-400 text-sm"
          >
            Logout
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="flex-1 bg-gray-100 overflow-auto">
      <div class="p-8">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const navItems = [
  { path: '/', label: 'Dashboard', icon: '📊', roles: ['ADMIN', 'FRONT_DESK', 'RESTAURANT_STAFF'] },
  { path: '/rooms', label: 'Rooms', icon: '🏨', roles: ['ADMIN', 'FRONT_DESK'] },
  { path: '/bookings', label: 'Bookings', icon: '📋', roles: ['ADMIN', 'FRONT_DESK'] },
  { path: '/guests', label: 'Guests', icon: '👥', roles: ['ADMIN', 'FRONT_DESK'] },
  { path: '/pos', label: 'POS', icon: '💳', roles: ['ADMIN', 'RESTAURANT_STAFF'] },
  { path: '/reports', label: 'Reports', icon: '📈', roles: ['ADMIN'] },
]

const filteredNav = computed(() =>
  navItems.filter((item) => item.roles.includes(auth.userRole))
)

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>
