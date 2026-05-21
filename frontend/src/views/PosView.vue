<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Point of Sale</h2>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Menu Items (left 2 cols) -->
      <div class="lg:col-span-2">
        <!-- Category filter -->
        <div class="flex gap-2 mb-4 flex-wrap">
          <button
            @click="selectedCategory = null"
            :class="['px-4 py-2 rounded-lg text-sm font-medium transition',
              !selectedCategory ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50']"
          >All</button>
          <button
            v-for="cat in posStore.categories"
            :key="cat.id"
            @click="selectedCategory = cat.id"
            :class="['px-4 py-2 rounded-lg text-sm font-medium transition',
              selectedCategory === cat.id ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50']"
          >{{ cat.name }}</button>
        </div>

        <!-- Menu Grid -->
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            @click="posStore.addToCart(item)"
            class="bg-white rounded-xl shadow p-4 cursor-pointer hover:shadow-lg transition border hover:border-indigo-300"
          >
            <h4 class="font-semibold text-gray-800">{{ item.name }}</h4>
            <p class="text-xs text-gray-500">{{ item.categoryName }}</p>
            <p class="text-lg font-bold text-indigo-600 mt-2">₹{{ item.price }}</p>
            <p v-if="item.stockQuantity <= 5" class="text-xs text-red-500 mt-1">Low stock: {{ item.stockQuantity }}</p>
          </div>
        </div>
      </div>

      <!-- Cart (right col) -->
      <div class="bg-white rounded-xl shadow p-5">
        <h3 class="text-lg font-bold mb-4">Order Cart</h3>

        <!-- Order Type -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Order Type</label>
          <select v-model="orderData.orderType" class="w-full border rounded-lg px-3 py-2">
            <option value="WALKIN">Walk-in</option>
            <option value="ROOM_SERVICE">Room Service</option>
            <option value="RESTAURANT">Restaurant</option>
          </select>
        </div>

        <!-- Room Number (for room service) -->
        <div v-if="orderData.orderType === 'ROOM_SERVICE'" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Room Number</label>
          <input v-model="orderData.roomNumber" class="w-full border rounded-lg px-3 py-2" placeholder="e.g. 101" />
        </div>

        <!-- Walk-in guest name -->
        <div v-if="orderData.orderType === 'WALKIN'" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Guest Name</label>
          <input v-model="orderData.guestName" class="w-full border rounded-lg px-3 py-2" placeholder="Walk-in guest name" />
        </div>

        <!-- Cart Items -->
        <div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
          <div v-if="posStore.cart.length === 0" class="text-center text-gray-400 py-8">
            Cart is empty
          </div>
          <div v-for="item in posStore.cart" :key="item.menuItemId" class="flex items-center justify-between border-b pb-2">
            <div class="flex-1">
              <p class="text-sm font-medium">{{ item.name }}</p>
              <p class="text-xs text-gray-500">₹{{ item.price }} each</p>
            </div>
            <div class="flex items-center gap-2">
              <button @click="posStore.updateCartItemQty(item.menuItemId, item.quantity - 1)"
                class="w-7 h-7 rounded bg-gray-200 text-gray-700 hover:bg-gray-300 text-sm">-</button>
              <span class="text-sm font-medium w-6 text-center">{{ item.quantity }}</span>
              <button @click="posStore.updateCartItemQty(item.menuItemId, item.quantity + 1)"
                class="w-7 h-7 rounded bg-gray-200 text-gray-700 hover:bg-gray-300 text-sm">+</button>
            </div>
            <p class="text-sm font-bold ml-3 w-16 text-right">₹{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
        </div>

        <!-- Total -->
        <div class="border-t pt-3 mb-4">
          <div class="flex justify-between text-lg font-bold">
            <span>Total</span>
            <span class="text-indigo-600">₹{{ posStore.cartTotal.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="space-y-2">
          <button
            @click="handlePlaceOrder"
            :disabled="posStore.cart.length === 0"
            class="w-full bg-green-600 text-white py-3 rounded-lg font-medium hover:bg-green-700 disabled:opacity-50 transition"
          >
            Place Order
          </button>
          <button
            @click="posStore.clearCart()"
            class="w-full bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition"
          >
            Clear Cart
          </button>
        </div>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="mt-8">
      <h3 class="text-lg font-bold text-gray-800 mb-4">Recent Orders</h3>
      <div class="bg-white rounded-xl shadow overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Order #</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Type</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Guest / Room</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Total</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Status</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-for="o in posStore.orders" :key="o.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 text-sm font-medium">{{ o.orderNumber }}</td>
              <td class="px-4 py-3 text-sm">{{ o.orderType }}</td>
              <td class="px-4 py-3 text-sm">{{ o.guestName || '-' }} {{ o.roomNumber ? '(Room ' + o.roomNumber + ')' : '' }}</td>
              <td class="px-4 py-3 text-sm font-medium">₹{{ o.totalAmount }}</td>
              <td class="px-4 py-3">
                <span :class="['px-2 py-1 rounded text-xs font-medium', orderBadge(o.status)]">
                  {{ o.status }}
                </span>
              </td>
              <td class="px-4 py-3">
                <select
                  v-if="o.status !== 'CANCELLED'"
                  @change="handleStatusUpdate(o.id, $event.target.value)"
                  :value="o.status"
                  class="text-sm border rounded px-2 py-1"
                >
                  <option value="PENDING">Pending</option>
                  <option value="PREPARING">Preparing</option>
                  <option value="SERVED">Served</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePosStore } from '../stores/pos'

const posStore = usePosStore()
const selectedCategory = ref(null)
const orderData = ref({
  orderType: 'WALKIN',
  guestName: '',
  roomNumber: '',
})

onMounted(() => {
  posStore.fetchMenuItems()
  posStore.fetchCategories()
  posStore.fetchOrders()
})

const filteredItems = computed(() => {
  if (!selectedCategory.value) return posStore.menuItems
  return posStore.menuItems.filter((i) => i.categoryId === selectedCategory.value)
})

function orderBadge(status) {
  const badges = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    PREPARING: 'bg-blue-100 text-blue-700',
    SERVED: 'bg-green-100 text-green-700',
    CANCELLED: 'bg-red-100 text-red-700',
  }
  return badges[status] || 'bg-gray-100'
}

async function handlePlaceOrder() {
  try {
    await posStore.placeOrder(orderData.value)
    orderData.value = { orderType: 'WALKIN', guestName: '', roomNumber: '' }
    alert('Order placed successfully!')
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to place order')
  }
}

async function handleStatusUpdate(id, status) {
  try {
    await posStore.updateOrderStatus(id, status)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update status')
  }
}
</script>
