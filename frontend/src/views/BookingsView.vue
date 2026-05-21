<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Bookings</h2>
      <button @click="showModal = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">
        + New Booking
      </button>
    </div>

    <!-- Status Filter -->
    <div class="flex gap-2 mb-6">
      <button
        v-for="s in ['ALL', 'RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED']"
        :key="s"
        @click="filter = s"
        :class="['px-4 py-2 rounded-lg text-sm font-medium transition',
          filter === s ? 'bg-indigo-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50']"
      >{{ s }}</button>
    </div>

    <!-- Bookings Table -->
    <div class="bg-white rounded-xl shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Booking #</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Guest</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Room</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Check-in</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Check-out</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Amount</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Status</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr v-for="b in filteredBookings" :key="b.id" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium">{{ b.bookingNumber }}</td>
            <td class="px-4 py-3 text-sm">{{ b.guestName }}</td>
            <td class="px-4 py-3 text-sm">{{ b.roomNumber }} ({{ b.roomTypeName }})</td>
            <td class="px-4 py-3 text-sm">{{ b.checkInDate }}</td>
            <td class="px-4 py-3 text-sm">{{ b.checkOutDate }}</td>
            <td class="px-4 py-3 text-sm font-medium">₹{{ b.totalAmount }}</td>
            <td class="px-4 py-3">
              <span :class="['px-2 py-1 rounded text-xs font-medium', bookingBadge(b.status)]">
                {{ b.status }}
              </span>
            </td>
            <td class="px-4 py-3">
              <div class="flex gap-1">
                <button
                  v-if="b.status === 'RESERVED'"
                  @click="handleCheckIn(b.id)"
                  class="bg-green-500 text-white px-2 py-1 rounded text-xs hover:bg-green-600"
                >Check In</button>
                <button
                  v-if="b.status === 'CHECKED_IN'"
                  @click="handleCheckOut(b.id)"
                  class="bg-blue-500 text-white px-2 py-1 rounded text-xs hover:bg-blue-600"
                >Check Out</button>
                <button
                  v-if="b.status === 'RESERVED'"
                  @click="handleCancel(b.id)"
                  class="bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600"
                >Cancel</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="filteredBookings.length === 0" class="text-center py-8 text-gray-400">
        No bookings found
      </div>
    </div>

    <!-- New Booking Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-4">New Booking</h3>
        <form @submit.prevent="handleCreate" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Guest</label>
            <select v-model="newBooking.guestId" required class="w-full border rounded-lg px-3 py-2 mt-1">
              <option v-for="g in guests" :key="g.id" :value="g.id">{{ g.firstName }} {{ g.lastName }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Room</label>
            <select v-model="newBooking.roomId" required class="w-full border rounded-lg px-3 py-2 mt-1">
              <option v-for="r in availableRooms" :key="r.id" :value="r.id">
                {{ r.roomNumber }} - {{ r.roomTypeName }} (₹{{ r.basePrice }}/night)
              </option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Check-in Date</label>
              <input v-model="newBooking.checkInDate" type="date" required class="w-full border rounded-lg px-3 py-2 mt-1" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Check-out Date</label>
              <input v-model="newBooking.checkOutDate" type="date" required class="w-full border rounded-lg px-3 py-2 mt-1" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Number of Guests</label>
            <input v-model.number="newBooking.numGuests" type="number" min="1" class="w-full border rounded-lg px-3 py-2 mt-1" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Special Requests</label>
            <textarea v-model="newBooking.specialRequests" rows="2" class="w-full border rounded-lg px-3 py-2 mt-1"></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button type="button" @click="showModal = false" class="px-4 py-2 text-gray-600">Cancel</button>
            <button type="submit" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">Create Booking</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBookingStore } from '../stores/bookings'
import { useRoomStore } from '../stores/rooms'
import api from '../api'

const bookingStore = useBookingStore()
const roomStore = useRoomStore()
const filter = ref('ALL')
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

onMounted(async () => {
  bookingStore.fetchBookings()
  roomStore.fetchRooms()
  const res = await api.get('/guests')
  guests.value = res.data
})

const availableRooms = computed(() =>
  roomStore.rooms.filter((r) => r.status === 'AVAILABLE')
)

const filteredBookings = computed(() => {
  if (filter.value === 'ALL') return bookingStore.bookings
  return bookingStore.bookings.filter((b) => b.status === filter.value)
})

function bookingBadge(status) {
  const badges = {
    RESERVED: 'bg-blue-100 text-blue-700',
    CHECKED_IN: 'bg-green-100 text-green-700',
    CHECKED_OUT: 'bg-gray-100 text-gray-700',
    CANCELLED: 'bg-red-100 text-red-700',
  }
  return badges[status] || 'bg-gray-100'
}

async function handleCreate() {
  try {
    await bookingStore.createBooking(newBooking.value)
    showModal.value = false
    newBooking.value = { guestId: null, roomId: null, checkInDate: '', checkOutDate: '', numGuests: 1, specialRequests: '' }
    roomStore.fetchRooms()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to create booking')
  }
}

async function handleCheckIn(id) {
  try {
    await bookingStore.checkIn(id)
    roomStore.fetchRooms()
  } catch (err) {
    alert(err.response?.data?.message || 'Check-in failed')
  }
}

async function handleCheckOut(id) {
  try {
    await bookingStore.checkOut(id)
    roomStore.fetchRooms()
  } catch (err) {
    alert(err.response?.data?.message || 'Check-out failed')
  }
}

async function handleCancel(id) {
  if (confirm('Cancel this booking?')) {
    try {
      await bookingStore.cancelBooking(id)
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel')
    }
  }
}
</script>
