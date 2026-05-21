<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Guests</h2>
      <button @click="showModal = true" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">
        + Add Guest
      </button>
    </div>

    <!-- Search -->
    <div class="mb-6">
      <input
        v-model="search"
        type="text"
        placeholder="Search guests by name..."
        class="w-full max-w-md border rounded-lg px-4 py-2"
        @input="handleSearch"
      />
    </div>

    <!-- Guest Table -->
    <div class="bg-white rounded-xl shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Name</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Email</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Phone</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">ID Type</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">ID Number</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr v-for="g in displayGuests" :key="g.id" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium">{{ g.firstName }} {{ g.lastName }}</td>
            <td class="px-4 py-3 text-sm">{{ g.email || '-' }}</td>
            <td class="px-4 py-3 text-sm">{{ g.phone || '-' }}</td>
            <td class="px-4 py-3 text-sm">{{ g.idType || '-' }}</td>
            <td class="px-4 py-3 text-sm">{{ g.idNumber || '-' }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <button @click="editGuest(g)" class="text-indigo-600 hover:text-indigo-800 text-sm">Edit</button>
                <button @click="handleDelete(g.id)" class="text-red-600 hover:text-red-800 text-sm">Delete</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="displayGuests.length === 0" class="text-center py-8 text-gray-400">No guests found</div>
    </div>

    <!-- Add/Edit Guest Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold mb-4">{{ editingId ? 'Edit' : 'Add' }} Guest</h3>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">First Name</label>
              <input v-model="form.firstName" required class="w-full border rounded-lg px-3 py-2 mt-1" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Last Name</label>
              <input v-model="form.lastName" required class="w-full border rounded-lg px-3 py-2 mt-1" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Email</label>
            <input v-model="form.email" type="email" class="w-full border rounded-lg px-3 py-2 mt-1" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Phone</label>
            <input v-model="form.phone" class="w-full border rounded-lg px-3 py-2 mt-1" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">ID Type</label>
              <select v-model="form.idType" class="w-full border rounded-lg px-3 py-2 mt-1">
                <option value="">Select</option>
                <option>Aadhaar</option>
                <option>Passport</option>
                <option>Driver License</option>
                <option>Voter ID</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">ID Number</label>
              <input v-model="form.idNumber" class="w-full border rounded-lg px-3 py-2 mt-1" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Address</label>
            <textarea v-model="form.address" rows="2" class="w-full border rounded-lg px-3 py-2 mt-1"></textarea>
          </div>
          <div class="flex gap-3 justify-end">
            <button type="button" @click="closeModal" class="px-4 py-2 text-gray-600">Cancel</button>
            <button type="submit" class="bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700">
              {{ editingId ? 'Update' : 'Create' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useGuestStore } from '../stores/guests'

const guestStore = useGuestStore()
const showModal = ref(false)
const search = ref('')
const searchResults = ref(null)
const editingId = ref(null)

const emptyForm = { firstName: '', lastName: '', email: '', phone: '', idType: '', idNumber: '', address: '' }
const form = ref({ ...emptyForm })

onMounted(() => guestStore.fetchGuests())

const displayGuests = computed(() => searchResults.value || guestStore.guests)

let searchTimeout
function handleSearch() {
  clearTimeout(searchTimeout)
  if (!search.value.trim()) {
    searchResults.value = null
    return
  }
  searchTimeout = setTimeout(async () => {
    searchResults.value = await guestStore.searchGuests(search.value)
  }, 300)
}

function editGuest(g) {
  editingId.value = g.id
  form.value = { ...g }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingId.value = null
  form.value = { ...emptyForm }
}

async function handleSubmit() {
  try {
    if (editingId.value) {
      await guestStore.updateGuest(editingId.value, form.value)
    } else {
      await guestStore.createGuest(form.value)
    }
    closeModal()
  } catch (err) {
    alert(err.response?.data?.message || 'Operation failed')
  }
}

async function handleDelete(id) {
  if (confirm('Delete this guest?')) {
    try {
      await guestStore.deleteGuest(id)
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete guest')
    }
  }
}
</script>
