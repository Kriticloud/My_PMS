<template>
  <div class="min-h-screen flex bg-gray-100 dark:bg-gray-900 transition-colors">
    <!-- Sidebar -->
    <aside class="w-64 bg-gray-900 dark:bg-gray-950 text-white flex flex-col shadow-xl">
      <div class="p-6 border-b border-gray-700">
        <h1 class="text-xl font-bold bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">PMS</h1>
        <p class="text-gray-400 text-sm">Property Management</p>
        <!-- Active Property Badge -->
        <router-link
          v-if="propertyStore.activeProperty"
          to="/properties"
          class="mt-2 flex items-center gap-2 px-3 py-1.5 rounded-lg bg-gray-800 hover:bg-gray-700 transition text-sm"
        >
          <span>{{ typeIcons[propertyStore.activeProperty.propertyType] }}</span>
          <span class="text-gray-200 truncate">{{ propertyStore.activeProperty.name }}</span>
          <span class="ml-auto text-gray-500 text-xs">Switch</span>
        </router-link>
      </div>

      <nav class="flex-1 p-4 space-y-1">
        <router-link
          v-for="item in filteredNav"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-4 py-3 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-all duration-200"
          active-class="!bg-indigo-600 !text-white shadow-lg shadow-indigo-500/20"
        >
          <span class="text-lg mr-3">{{ item.icon }}</span>
          <span class="font-medium">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="p-4 border-t border-gray-700 space-y-3">
        <!-- WebSocket Status -->
        <div class="flex items-center gap-2 text-xs text-gray-400">
          <span :class="['w-2 h-2 rounded-full', wsConnected ? 'bg-green-400' : 'bg-red-400']"></span>
          {{ wsConnected ? 'Live' : 'Offline' }}
        </div>

        <!-- Dark Mode Toggle -->
        <button
          @click="toggleDarkMode"
          class="w-full flex items-center justify-between px-3 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-sm transition"
        >
          <span>{{ isDark ? '☀️ Light' : '🌙 Dark' }}</span>
        </button>

        <!-- User Info -->
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium">{{ auth.user?.fullName }}</p>
            <p class="text-xs text-gray-400">{{ auth.userRole }}</p>
          </div>
          <button
            @click="handleLogout"
            class="text-gray-400 hover:text-red-400 text-sm transition"
          >
            Logout
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="flex-1 overflow-auto">
      <div class="p-6 lg:p-8">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { usePropertyStore } from '../stores/property'
import { useWebSocket } from '../composables/useWebSocket'
import { useDarkMode } from '../composables/useDarkMode'
import { useToast } from 'vue-toastification'

const auth = useAuthStore()
const propertyStore = usePropertyStore()
const router = useRouter()
const toast = useToast()
const { isDark, toggleDarkMode } = useDarkMode()
const { connected: wsConnected, connect, subscribe } = useWebSocket()

const typeIcons = {
  HOTEL: '🏨',
  HOSTEL: '🛏️',
  HOSPITAL: '🏥',
  RENTAL: '🏠',
  RESORT: '🏖️',
}

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

onMounted(() => {
  propertyStore.loadActiveProperty()
  propertyStore.fetchProperties()
  connect()

  subscribe('/topic/rooms', (data) => {
    toast.info(`Room ${data.roomNumber} → ${data.status}`)
  })

  subscribe('/topic/orders', (data) => {
    toast.success(`New POS Order: ${data.orderNumber}`)
  })

  subscribe('/topic/bookings', (data) => {
    toast.info(`Booking ${data.bookingNumber} → ${data.status}`)
  })
})
</script>
