<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 dark:text-white mb-6">Point of Sale</h2>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Menu Items (left 2 cols) -->
      <div class="lg:col-span-2">
        <!-- Category filter -->
        <div class="flex gap-2 mb-4 flex-wrap">
          <button
            @click="selectedCategory = null"
            :class="['px-4 py-2 rounded-lg text-sm font-medium transition',
              !selectedCategory ? 'bg-indigo-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600']"
          >All</button>
          <button
            v-for="cat in posStore.categories"
            :key="cat.id"
            @click="selectedCategory = cat.id"
            :class="['px-4 py-2 rounded-lg text-sm font-medium transition',
              selectedCategory === cat.id ? 'bg-indigo-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600']"
          >{{ cat.name }}</button>
        </div>

        <!-- Menu Grid -->
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            @click="posStore.addToCart(item)"
            class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-4 cursor-pointer hover:shadow-lg transition
                   border border-gray-200 dark:border-gray-700 hover:border-indigo-400 dark:hover:border-indigo-500
                   transform hover:scale-[1.02]"
          >
            <h4 class="font-semibold text-gray-800 dark:text-white">{{ item.name }}</h4>
            <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.categoryName }}</p>
            <p class="text-lg font-bold text-indigo-600 mt-2">₹{{ item.price }}</p>
            <p v-if="item.stockQuantity !== undefined && item.stockQuantity <= 5"
               class="text-xs text-red-500 mt-1 flex items-center gap-1">
              ⚠️ Low stock: {{ item.stockQuantity }}
            </p>
          </div>
        </div>
      </div>

      <!-- Cart (right col) -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-5 sticky top-4">
        <h3 class="text-lg font-bold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
          🛒 Order Cart
          <span v-if="posStore.cart.length" class="bg-indigo-600 text-white text-xs px-2 py-0.5 rounded-full">
            {{ posStore.cart.length }}
          </span>
        </h3>

        <!-- Order Type -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Order Type</label>
          <select v-model="orderData.orderType" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 dark:bg-gray-700 dark:text-white">
            <option value="WALKIN">Walk-in</option>
            <option value="ROOM_SERVICE">Room Service</option>
            <option value="RESTAURANT">Restaurant</option>
          </select>
        </div>

        <!-- Room Number (for room service) -->
        <div v-if="orderData.orderType === 'ROOM_SERVICE'" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Room Number</label>
          <input v-model="orderData.roomNumber" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 dark:bg-gray-700 dark:text-white" placeholder="e.g. 101" />
        </div>

        <!-- Walk-in guest name -->
        <div v-if="orderData.orderType === 'WALKIN'" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Guest Name</label>
          <input v-model="orderData.guestName" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 dark:bg-gray-700 dark:text-white" placeholder="Walk-in guest name" />
        </div>

        <!-- Cart Items -->
        <div class="space-y-3 mb-4 max-h-72 overflow-y-auto">
          <div v-if="posStore.cart.length === 0" class="text-center text-gray-400 dark:text-gray-500 py-10">
            <p class="text-3xl mb-2">🛒</p>
            <p>Cart is empty</p>
            <p class="text-xs mt-1">Click on menu items to add</p>
          </div>
          <div v-for="item in posStore.cart" :key="item.menuItemId"
               class="flex items-center justify-between border-b border-gray-100 dark:border-gray-700 pb-2">
            <div class="flex-1">
              <p class="text-sm font-medium text-gray-800 dark:text-white">{{ item.name }}</p>
              <p class="text-xs text-gray-500 dark:text-gray-400">₹{{ item.price }} each</p>
            </div>
            <div class="flex items-center gap-1">
              <button @click="posStore.updateCartItemQty(item.menuItemId, item.quantity - 1)"
                class="w-7 h-7 rounded-lg bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-white hover:bg-gray-300 dark:hover:bg-gray-500 text-sm font-bold">-</button>
              <span class="text-sm font-medium w-6 text-center dark:text-white">{{ item.quantity }}</span>
              <button @click="posStore.updateCartItemQty(item.menuItemId, item.quantity + 1)"
                class="w-7 h-7 rounded-lg bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-white hover:bg-gray-300 dark:hover:bg-gray-500 text-sm font-bold">+</button>
            </div>
            <p class="text-sm font-bold ml-3 w-16 text-right text-gray-800 dark:text-white">₹{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>

        <!-- Total -->
        <div class="border-t border-gray-200 dark:border-gray-700 pt-3 mb-4">
          <div class="flex justify-between text-lg font-bold">
            <span class="dark:text-white">Total</span>
            <span class="text-indigo-600">₹{{ posStore.cartTotal.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="space-y-2">
          <button
            @click="handlePlaceOrder"
            :disabled="posStore.cart.length === 0"
            class="w-full bg-green-600 text-white py-3 rounded-lg font-medium hover:bg-green-700
                   disabled:opacity-50 transition flex items-center justify-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            Place Order
          </button>
          <button
            @click="posStore.clearCart()"
            class="w-full bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-white py-2 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-500 transition"
          >
            Clear Cart
          </button>
        </div>
      </div>
    </div>

    <!-- Recent Orders with AG Grid -->
    <div class="mt-8">
      <h3 class="text-lg font-bold text-gray-800 dark:text-white mb-4">Recent Orders</h3>
      <DataGrid
        :columnDefs="orderColumnDefs"
        :rowData="posStore.orders"
        gridHeight="400px"
        :darkMode="isDark"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePosStore } from '../stores/pos'
import { useDarkMode } from '../composables/useDarkMode'
import { useToast } from 'vue-toastification'
import DataGrid from '../components/DataGrid.vue'

const posStore = usePosStore()
const toast = useToast()
const { isDark } = useDarkMode()
const selectedCategory = ref(null)
const orderData = ref({
  orderType: 'WALKIN',
  guestName: '',
  roomNumber: '',
})

const orderColumnDefs = [
  { headerName: 'Order #', field: 'orderNumber', width: 150 },
  { headerName: 'Type', field: 'orderType', width: 130 },
  { headerName: 'Guest', field: 'guestName', width: 150 },
  { headerName: 'Room', field: 'roomNumber', width: 100 },
  { headerName: 'Total', field: 'totalAmount', width: 120, valueFormatter: (p) => `₹${p.value}` },
  {
    headerName: 'Status', field: 'status', width: 130,
    cellRenderer: (params) => {
      const colors = { PENDING: '#f59e0b', PREPARING: '#3b82f6', SERVED: '#10b981', CANCELLED: '#ef4444' }
      const color = colors[params.value] || '#6b7280'
      return `<span style="color:${color};font-weight:600">${params.value}</span>`
    },
  },
  { headerName: 'Created', field: 'createdAt', width: 180 },
]

onMounted(() => {
  posStore.fetchMenuItems()
  posStore.fetchCategories()
  posStore.fetchOrders()
})

const filteredItems = computed(() => {
  if (!selectedCategory.value) return posStore.menuItems
  return posStore.menuItems.filter((i) => i.categoryId === selectedCategory.value)
})

async function handlePlaceOrder() {
  try {
    await posStore.placeOrder(orderData.value)
    orderData.value = { orderType: 'WALKIN', guestName: '', roomNumber: '' }
    toast.success('Order placed successfully!')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to place order')
  }
}
</script>
