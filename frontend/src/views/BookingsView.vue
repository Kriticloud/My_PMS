<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800 dark:text-white">Bookings</h2>
      <button @click="showModal = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        New Booking
      </button>
    </div>

    <!-- AG Grid with sorting, filtering, pagination, grouping -->
    <DataGrid
      :columnDefs="columnDefs"
      :rowData="bookingStore.bookings"
      gridHeight="600px"
      :darkMode="isDark"
    >
      <template #toolbar>
        <div class="flex gap-2">
          <button
            v-for="s in ['ALL', 'RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED']"
            :key="s"
            @click="statusFilter = s"
            :class="['px-3 py-1.5 rounded text-xs font-medium transition',
              statusFilter === s ? 'bg-indigo-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300']"
          >{{ s }}</button>
        </div>
      </template>
    </DataGrid>

    <!-- New Booking Modal -->
    <AppModal :show="showModal" title="New Booking" size="lg" @close="showModal = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Guest</label>
          <select v-model="newBooking.guestId" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white">
            <option v-for="g in guests" :key="g.id" :value="g.id">{{ g.firstName }} {{ g.lastName }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Room</label>
          <select v-model="newBooking.roomId" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white">
            <option v-for="r in availableRooms" :key="r.id" :value="r.id">
              {{ r.roomNumber }} - {{ r.roomTypeName }} (₹{{ r.basePrice }}/night)
            </option>
          </select>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Check-in Date</label>
            <input v-model="newBooking.checkInDate" type="date" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Check-out Date</label>
            <input v-model="newBooking.checkOutDate" type="date" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Number of Guests</label>
          <input v-model.number="newBooking.numGuests" type="number" min="1" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Special Requests</label>
          <textarea v-model="newBooking.specialRequests" rows="2" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white"></textarea>
        </div>
      </form>
      <template #footer>
        <button @click="showModal = false" class="px-4 py-2 text-gray-600 dark:text-gray-300">Cancel</button>
        <button @click="handleCreate" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">Create Booking</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBookingStore } from '../stores/bookings'
import { useRoomStore } from '../stores/rooms'
import { useDarkMode } from '../composables/useDarkMode'
import { useToast } from 'vue-toastification'
import DataGrid from '../components/DataGrid.vue'
import AppModal from '../components/AppModal.vue'
import api from '../api'

const bookingStore = useBookingStore()
const roomStore = useRoomStore()
const toast = useToast()
const { isDark } = useDarkMode()
const statusFilter = ref('ALL')
const showModal = ref(false)
const guests = ref([])

const newBooking = ref({
  guestId: null,
  roomId: null,
  checkInDate: '',
  checkOutDate: '',
  numGuests: 1,
  specialRequests: '',
})

const columnDefs = [
  { headerName: 'Booking #', field: 'bookingNumber', width: 150 },
  { headerName: 'Guest', field: 'guestName', width: 160 },
  { headerName: 'Room', field: 'roomNumber', width: 100 },
  { headerName: 'Type', field: 'roomTypeName', width: 120 },
  { headerName: 'Check-in', field: 'checkInDate', width: 120 },
  { headerName: 'Check-out', field: 'checkOutDate', width: 120 },
  { headerName: 'Amount', field: 'totalAmount', width: 120, valueFormatter: (p) => `₹${p.value}` },
  {
    headerName: 'Status', field: 'status', width: 130,
    cellRenderer: (params) => {
      const colors = { RESERVED: '#3b82f6', CHECKED_IN: '#10b981', CHECKED_OUT: '#6b7280', CANCELLED: '#ef4444' }
      const color = colors[params.value] || '#6b7280'
      return `<span style="color:${color};font-weight:600">${params.value}</span>`
    },
  },
  {
    headerName: 'Actions', width: 220, sortable: false, filter: false,
    cellRenderer: (params) => {
      const btns = []
      if (params.data.status === 'RESERVED') {
        btns.push(`<button onclick="window.__pmsCheckIn(${params.data.id})" style="background:#10b981;color:white;padding:2px 8px;border-radius:4px;font-size:12px;margin-right:4px">Check In</button>`)
        btns.push(`<button onclick="window.__pmsCancel(${params.data.id})" style="background:#ef4444;color:white;padding:2px 8px;border-radius:4px;font-size:12px">Cancel</button>`)
      }
      if (params.data.status === 'CHECKED_IN') {
        btns.push(`<button onclick="window.__pmsCheckOut(${params.data.id})" style="background:#3b82f6;color:white;padding:2px 8px;border-radius:4px;font-size:12px;margin-right:4px">Check Out</button>`)
        btns.push(`<button onclick="window.__pmsInvoice(${params.data.id})" style="background:#8b5cf6;color:white;padding:2px 8px;border-radius:4px;font-size:12px">Invoice</button>`)
      }
      return btns.join('')
    },
  },
]

onMounted(async () => {
  bookingStore.fetchBookings()
  roomStore.fetchRooms()
  const res = await api.get('/guests')
  guests.value = res.data

  window.__pmsCheckIn = async (id) => {
    try {
      await bookingStore.checkIn(id)
      roomStore.fetchRooms()
      toast.success('Checked in successfully')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-in failed')
    }
  }
  window.__pmsCheckOut = async (id) => {
    try {
      await bookingStore.checkOut(id)
      roomStore.fetchRooms()
      toast.success('Checked out successfully')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Check-out failed')
    }
  }
  window.__pmsCancel = async (id) => {
    try {
      await bookingStore.cancelBooking(id)
      toast.success('Booking cancelled')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cancel failed')
    }
  }
  window.__pmsInvoice = async (id) => {
    try {
      await api.post(`/billing/invoices/generate/${id}`)
      toast.success('Invoice generated')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invoice generation failed')
    }
  }
})

const availableRooms = computed(() =>
  roomStore.rooms.filter((r) => r.status === 'AVAILABLE')
)

async function handleCreate() {
  try {
    await bookingStore.createBooking(newBooking.value)
    showModal.value = false
    newBooking.value = { guestId: null, roomId: null, checkInDate: '', checkOutDate: '', numGuests: 1, specialRequests: '' }
    roomStore.fetchRooms()
    toast.success('Booking created successfully')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to create booking')
  }
}
</script>
