<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Room Management</h2>
      <button
        @click="showModal = true"
        class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition"
      >
        + Add Room
      </button>
    </div>

    <!-- Room Status Filter -->
    <div class="flex gap-2 mb-6">
      <button
        v-for="status in ['ALL', 'AVAILABLE', 'OCCUPIED', 'CLEANING', 'MAINTENANCE']"
        :key="status"
        @click="filter = status"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition',
          filter === status
            ? 'bg-indigo-600 text-white'
            : 'bg-white text-gray-600 hover:bg-gray-50',
        ]"
      >
        {{ status }}
      </button>
    </div>

    <!-- Room Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="room in filteredRooms"
        :key="room.id"
        :class="[
          'rounded-xl shadow p-5 border-l-4 transition hover:shadow-lg',
          statusColor(room.status),
        ]"
      >
        <div class="flex justify-between items-start">
          <div>
            <h3 class="text-xl font-bold text-gray-800">Room {{ room.roomNumber }}</h3>
            <p class="text-sm text-gray-500">{{ room.roomTypeName }} | Floor {{ room.floor }}</p>
            <p class="text-lg font-semibold text-indigo-600 mt-1">₹{{ room.basePrice }}/night</p>
          </div>
          <span
            :class="[
              'px-2 py-1 rounded text-xs font-medium',
              statusBadge(room.status),
            ]"
          >
            {{ room.status }}
          </span>
        </div>

        <div class="mt-4 flex gap-2">
          <select
            @change="changeStatus(room.id, $event.target.value)"
            :value="room.status"
            class="text-sm border rounded px-2 py-1 flex-1"
          >
            <option value="AVAILABLE">Available</option>
            <option value="OCCUPIED">Occupied</option>
            <option value="CLEANING">Cleaning</option>
            <option value="MAINTENANCE">Maintenance</option>
          </select>
          <button
            @click="handleDelete(room.id)"
            class="text-red-500 hover:text-red-700 text-sm px-2"
          >
            Delete
          </button>
        </div>
      </div>
    </div>

    <!-- Add Room Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold mb-4">Add New Room</h3>
        <form @submit.prevent="handleCreate" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Room Number</label>
            <input v-model="newRoom.roomNumber" required class="w-full border rounded-lg px-3 py-2 mt-1" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Room Type</label>
            <select v-model="newRoom.roomTypeId" required class="w-full border rounded-lg px-3 py-2 mt-1">
              <option v-for="rt in roomStore.roomTypes" :key="rt.id" :value="rt.id">
                {{ rt.name }} (₹{{ rt.basePrice }})
              </option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Floor</label>
            <input v-model.number="newRoom.floor" type="number" min="1" class="w-full border rounded-lg px-3 py-2 mt-1" />
          </div>
          <div class="flex gap-3 justify-end">
            <button type="button" @click="showModal = false" class="px-4 py-2 text-gray-600 hover:text-gray-800">Cancel</button>
            <button type="submit" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">Create</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoomStore } from '../stores/rooms'

const roomStore = useRoomStore()
const filter = ref('ALL')
const showModal = ref(false)
const newRoom = ref({ roomNumber: '', roomTypeId: null, floor: 1 })

onMounted(() => {
  roomStore.fetchRooms()
  roomStore.fetchRoomTypes()
})

const filteredRooms = computed(() => {
  if (filter.value === 'ALL') return roomStore.rooms
  return roomStore.rooms.filter((r) => r.status === filter.value)
})

function statusColor(status) {
  const colors = {
    AVAILABLE: 'bg-white border-green-500',
    OCCUPIED: 'bg-white border-red-500',
    CLEANING: 'bg-white border-yellow-500',
    MAINTENANCE: 'bg-white border-gray-500',
  }
  return colors[status] || 'bg-white border-gray-300'
}

function statusBadge(status) {
  const badges = {
    AVAILABLE: 'bg-green-100 text-green-700',
    OCCUPIED: 'bg-red-100 text-red-700',
    CLEANING: 'bg-yellow-100 text-yellow-700',
    MAINTENANCE: 'bg-gray-100 text-gray-700',
  }
  return badges[status] || 'bg-gray-100 text-gray-700'
}

async function changeStatus(id, status) {
  try {
    await roomStore.updateRoomStatus(id, status)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update status')
  }
}

async function handleCreate() {
  try {
    await roomStore.createRoom(newRoom.value)
    showModal.value = false
    newRoom.value = { roomNumber: '', roomTypeId: null, floor: 1 }
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to create room')
  }
}

async function handleDelete(id) {
  if (confirm('Are you sure you want to delete this room?')) {
    try {
      await roomStore.deleteRoom(id)
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete room')
    }
  }
}
</script>
