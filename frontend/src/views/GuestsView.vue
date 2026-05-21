<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800 dark:text-white">Guests</h2>
      <button @click="showModal = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Add Guest
      </button>
    </div>

    <!-- AG Grid Table -->
    <DataGrid
      :columnDefs="columnDefs"
      :rowData="guestStore.guests"
      gridHeight="600px"
      :darkMode="isDark"
    />

    <!-- Add/Edit Guest Modal -->
    <AppModal :show="showModal" :title="editingId ? 'Edit Guest' : 'Add Guest'" @close="closeModal">
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">First Name</label>
            <input v-model="form.firstName" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Last Name</label>
            <input v-model="form.lastName" required class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Email</label>
          <input v-model="form.email" type="email" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Phone</label>
          <input v-model="form.phone" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">ID Type</label>
            <select v-model="form.idType" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white">
              <option value="">Select</option>
              <option>Aadhaar</option>
              <option>Passport</option>
              <option>Driver License</option>
              <option>Voter ID</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">ID Number</label>
            <input v-model="form.idNumber" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Address</label>
          <textarea v-model="form.address" rows="2" class="w-full border dark:border-gray-600 rounded-lg px-3 py-2 mt-1 dark:bg-gray-700 dark:text-white"></textarea>
        </div>
      </form>
      <template #footer>
        <button @click="closeModal" class="px-4 py-2 text-gray-600 dark:text-gray-300">Cancel</button>
        <button @click="handleSubmit" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">
          {{ editingId ? 'Update' : 'Create' }}
        </button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useGuestStore } from '../stores/guests'
import { useDarkMode } from '../composables/useDarkMode'
import { useToast } from 'vue-toastification'
import DataGrid from '../components/DataGrid.vue'
import AppModal from '../components/AppModal.vue'

const guestStore = useGuestStore()
const toast = useToast()
const { isDark } = useDarkMode()
const showModal = ref(false)
const editingId = ref(null)

const emptyForm = { firstName: '', lastName: '', email: '', phone: '', idType: '', idNumber: '', address: '' }
const form = ref({ ...emptyForm })

const columnDefs = [
  { headerName: 'First Name', field: 'firstName', width: 140 },
  { headerName: 'Last Name', field: 'lastName', width: 140 },
  { headerName: 'Email', field: 'email', width: 200 },
  { headerName: 'Phone', field: 'phone', width: 140 },
  { headerName: 'ID Type', field: 'idType', width: 130 },
  { headerName: 'ID Number', field: 'idNumber', width: 150 },
  {
    headerName: 'Actions', width: 150, sortable: false, filter: false,
    cellRenderer: (params) => {
      return `<button onclick="window.__pmsEditGuest(${params.data.id})" style="color:#4f46e5;margin-right:12px;font-size:13px">Edit</button>` +
             `<button onclick="window.__pmsDeleteGuest(${params.data.id})" style="color:#ef4444;font-size:13px">Delete</button>`
    },
  },
]

onMounted(() => {
  guestStore.fetchGuests()

  window.__pmsEditGuest = (id) => {
    const g = guestStore.guests.find((x) => x.id === id)
    if (g) {
      editingId.value = g.id
      form.value = { ...g }
      showModal.value = true
    }
  }
  window.__pmsDeleteGuest = async (id) => {
    if (confirm('Delete this guest?')) {
      try {
        await guestStore.deleteGuest(id)
        toast.success('Guest deleted')
      } catch (err) {
        toast.error(err.response?.data?.message || 'Failed to delete guest')
      }
    }
  }
})

function closeModal() {
  showModal.value = false
  editingId.value = null
  form.value = { ...emptyForm }
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await guestStore.updateGuest(editingId.value, form.value)
      toast.success('Guest updated')
    } else {
      await guestStore.createGuest(form.value)
      toast.success('Guest created')
    }
    closeModal()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Operation failed')
  }
}
</script>
