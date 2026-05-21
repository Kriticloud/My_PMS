<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800 dark:text-white">Room Management</h2>
      <button
        @click="showModal = true"
        class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition flex items-center gap-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Add Room
      </button>
    </div>

    <!-- View Toggle -->
    <div class="flex items-center gap-3 mb-4">
      <button
        @click="viewMode = 'grid'"
        :class="['px-3 py-2 rounded-lg text-sm', viewMode === 'grid' ? 'bg-indigo-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300']"
      >Grid View</button>
      <button
        @click="viewMode = 'table'"
        :class="['px-3 py-2 rounded-lg text-sm', viewMode === 'table' ? 'bg-indigo-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300']"
      >Table View</button>

      <!-- Status Filter -->
      <div class="flex gap-2 ml-4">
        <button
          v-for="status in ['ALL', 'AVAILABLE', 'OCCUPIED', 'CLEANING', 'MAINTENANCE']"
          :key="status"
          @click="filter = status"
          :class="[
            'px-3 py-2 rounded-lg text-sm font-medium transition',
            filter === status
              ? 'bg-indigo-600 text-white'
              : 'bg-white dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600',
          ]"
        >
          {{ status }}
        </button>
      </div>
    </div>

    <!-- AG Grid Table View -->
    <div v-if="viewMode === 'table'">
      <DataGrid
        :columnDefs="columnDefs"
        :rowData="filteredRooms"
        gridHeight="500px"
        :darkMode="isDark"
      />
    </div>

    <!-- Card Grid View -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="room in filteredRooms"
        :key="room.id"
        :class="[
          'rounded-xl shadow-sm p-5 border-l-4 transition hover:shadow-lg dark:bg-gray-800',
          statusColor(room.status),
        ]"
      >
        <div class="flex justify-between items-start">
          <div>
            <h3 class="text-xl font-bold text-gray-800 dark:text-white">Room {{ room.roomNumber }}</h3>
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ room.roomTypeName }} | Floor {{ room.floor }}</p>
            <p class="text-lg font-semibold text-indigo-600 mt-1">₹{{ room.basePrice }}/night</p>
          </div>
          <span :class="['px-2 py-1 rounded text-xs font-medium', statusBadge(room.status)]">
            {{ room.status }}
          </span>
        </div>

        <div class="mt-4 flex gap-2">
          <select
            @change="changeStatus(room.id, $event.target.value)"
            :value="room.status"
            class="text-sm border dark:border-gray-600 rounded px-2 py-1 flex-1 dark:bg-gray-700 dark:text-white"
          >
            <option value="AVAILABLE">Available</option>
            <option value="OCCUPIED">Occupied</option>
            <option value="CLEANING">Cleaning</option>
            <option value="MAINTENANCE">Maintenance</option>
          </select>
          <button @click="handleDelete(room.id)" class="text-red-500 hover:text-red-700 text-sm px-2">
            Delete
          </button>
        </div>
      </div>
    </div>

    <!-- Add Room Modal -->
    <AppModal :show="showModal" title="Add New Room" @close="showModal = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Room Number</label>
          <input v-model="newRoom.roomNumber" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Room Type</label>
          <select v-model="newRoom.roomTypeId" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white">
            <option v-for="rt in roomStore.roomTypes" :key="rt.id" :value="rt.id">
              {{ rt.name }} (₹{{ rt.basePrice }})
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Floor</label>
          <input v-model.number="newRoom.floor" type="number" min="1" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
        </div>
      </form>
      <template #footer>
        <button @click="showModal = false" class="px-4 py-2 text-gray-600 dark:text-gray-300 hover:text-gray-800">Cancel</button>
        <button @click="handleCreate" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">Create</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoomStore } from '../stores/rooms'
import { useDarkMode } from '../composables/useDarkMode'
import { useToast } from 'vue-toastification'
import DataGrid from '../components/DataGrid.vue'
import AppModal from '../components/AppModal.vue'

const roomStore = useRoomStore()
const toast = useToast()
const { isDark } = useDarkMode()
const filter = ref('ALL')
const viewMode = ref('grid')
const showModal = ref(false)
const newRoom = ref({ roomNumber: '', roomTypeId: null, floor: 1 })

const columnDefs = [
  { headerName: 'Room #', field: 'roomNumber', width: 120 },
  { headerName: 'Type', field: 'roomTypeName', width: 150 },
  { headerName: 'Floor', field: 'floor', width: 100 },
  { headerName: 'Price/Night', field: 'basePrice', width: 130, valueFormatter: (p) => `₹${p.value}` },
  {
    headerName: 'Status', field: 'status', width: 140,
    cellRenderer: (params) => {
      const colors = { AVAILABLE: '#10b981', OCCUPIED: '#ef4444', CLEANING: '#f59e0b', MAINTENANCE: '#6b7280' }
      const color = colors[params.value] || '#6b7280'
      return `<span style="color:${color};font-weight:600">${params.value}</span>`
    },
  },
]

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
    AVAILABLE: 'bg-white dark:bg-gray-800 border-green-500',
    OCCUPIED: 'bg-white dark:bg-gray-800 border-red-500',
    CLEANING: 'bg-white dark:bg-gray-800 border-yellow-500',
    MAINTENANCE: 'bg-white dark:bg-gray-800 border-gray-500',
  }
  return colors[status] || 'bg-white dark:bg-gray-800 border-gray-300'
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
    toast.success(`Room status updated to ${status}`)
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to update status')
  }
}

async function handleCreate() {
  try {
    await roomStore.createRoom(newRoom.value)
    showModal.value = false
    newRoom.value = { roomNumber: '', roomTypeId: null, floor: 1 }
    toast.success('Room created successfully')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to create room')
  }
}

async function handleDelete(id) {
  if (confirm('Are you sure you want to delete this room?')) {
    try {
      await roomStore.deleteRoom(id)
      toast.success('Room deleted')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete room')
    }
  }
}
</script>
