<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-900 via-indigo-950 to-gray-900 flex items-center justify-center p-6">
    <div class="max-w-5xl w-full">
      <div class="text-center mb-10">
        <h1 class="text-4xl font-bold text-white mb-2">Select Property</h1>
        <p class="text-gray-400">Choose a property to manage, or create a new one</p>
      </div>

      <!-- Property Cards Grid -->
      <div v-if="!showCreate" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <button
          v-for="prop in propertyStore.properties"
          :key="prop.id"
          @click="selectProperty(prop)"
          class="bg-gray-800/60 backdrop-blur border border-gray-700 rounded-2xl p-6 text-left hover:border-indigo-500 hover:bg-gray-800 transition-all duration-200 group"
        >
          <div class="text-3xl mb-3">{{ typeIcons[prop.propertyType] || '🏢' }}</div>
          <h3 class="text-lg font-semibold text-white group-hover:text-indigo-400 transition">{{ prop.name }}</h3>
          <span class="inline-block mt-2 text-xs font-medium px-2.5 py-1 rounded-full"
            :class="typeBadgeClass[prop.propertyType]">
            {{ prop.propertyType }}
          </span>
          <p class="text-gray-400 text-sm mt-2">{{ prop.address || 'No address' }}</p>
        </button>

        <!-- Add Property Card -->
        <button
          @click="showCreate = true"
          class="border-2 border-dashed border-gray-600 rounded-2xl p-6 flex flex-col items-center justify-center text-gray-400 hover:border-indigo-500 hover:text-indigo-400 transition-all duration-200"
        >
          <span class="text-4xl mb-2">+</span>
          <span class="font-medium">Add Property</span>
        </button>
      </div>

      <!-- Create Property Form -->
      <div v-if="showCreate" class="bg-gray-800/60 backdrop-blur border border-gray-700 rounded-2xl p-8 max-w-lg mx-auto">
        <h3 class="text-xl font-semibold text-white mb-6">Create New Property</h3>
        <form @submit.prevent="handleCreate" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Property Name</label>
            <input v-model="form.name" type="text" required
              class="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:border-indigo-500 focus:outline-none" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Property Type</label>
            <select v-model="form.propertyType" required
              class="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:border-indigo-500 focus:outline-none">
              <option v-for="t in types" :key="t" :value="t">{{ typeIcons[t] }} {{ t }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Address</label>
            <input v-model="form.address" type="text"
              class="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:border-indigo-500 focus:outline-none" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">Phone</label>
              <input v-model="form.contactPhone" type="text"
                class="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:border-indigo-500 focus:outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">Email</label>
              <input v-model="form.contactEmail" type="email"
                class="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:border-indigo-500 focus:outline-none" />
            </div>
          </div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showCreate = false" class="px-4 py-2 text-gray-400 hover:text-white transition">Cancel</button>
            <button type="submit" class="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition">Create</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePropertyStore } from '../stores/property'
import { useToast } from 'vue-toastification'

const propertyStore = usePropertyStore()
const router = useRouter()
const toast = useToast()
const showCreate = ref(false)

const types = ['HOTEL', 'HOSTEL', 'HOSPITAL', 'RENTAL', 'RESORT']

const typeIcons = {
  HOTEL: '🏨',
  HOSTEL: '🛏️',
  HOSPITAL: '🏥',
  RENTAL: '🏠',
  RESORT: '🏖️',
}

const typeBadgeClass = {
  HOTEL: 'bg-indigo-500/20 text-indigo-400',
  HOSTEL: 'bg-amber-500/20 text-amber-400',
  HOSPITAL: 'bg-red-500/20 text-red-400',
  RENTAL: 'bg-green-500/20 text-green-400',
  RESORT: 'bg-purple-500/20 text-purple-400',
}

const form = ref({
  name: '',
  propertyType: 'HOTEL',
  address: '',
  contactPhone: '',
  contactEmail: '',
})

function selectProperty(prop) {
  propertyStore.setActiveProperty(prop.id)
  router.push('/')
}

async function handleCreate() {
  try {
    const created = await propertyStore.createProperty(form.value)
    toast.success(`Property "${created.name}" created`)
    showCreate.value = false
    form.value = { name: '', propertyType: 'HOTEL', address: '', contactPhone: '', contactEmail: '' }
  } catch (err) {
    toast.error(err.response?.data?.message || 'Failed to create property')
  }
}

onMounted(() => {
  propertyStore.fetchProperties()
})
</script>
