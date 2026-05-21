import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useGuestStore = defineStore('guests', () => {
  const guests = ref([])
  const loading = ref(false)

  async function fetchGuests() {
    loading.value = true
    try {
      const res = await api.get('/guests')
      guests.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function searchGuests(name) {
    const res = await api.get(`/guests/search?name=${encodeURIComponent(name)}`)
    return res.data
  }

  async function createGuest(guest) {
    const res = await api.post('/guests', guest)
    guests.value.push(res.data)
    return res.data
  }

  async function updateGuest(id, guest) {
    const res = await api.put(`/guests/${id}`, guest)
    const idx = guests.value.findIndex((g) => g.id === id)
    if (idx !== -1) guests.value[idx] = res.data
    return res.data
  }

  async function deleteGuest(id) {
    await api.delete(`/guests/${id}`)
    guests.value = guests.value.filter((g) => g.id !== id)
  }

  return { guests, loading, fetchGuests, searchGuests, createGuest, updateGuest, deleteGuest }
})
