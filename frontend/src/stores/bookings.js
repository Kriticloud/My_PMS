import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useBookingStore = defineStore('bookings', () => {
  const bookings = ref([])
  const loading = ref(false)

  async function fetchBookings() {
    loading.value = true
    try {
      const res = await api.get('/bookings')
      bookings.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createBooking(booking) {
    const res = await api.post('/bookings', booking)
    bookings.value.push(res.data)
    return res.data
  }

  async function checkIn(id) {
    const res = await api.post(`/bookings/${id}/check-in`)
    updateLocal(id, res.data)
    return res.data
  }

  async function checkOut(id) {
    const res = await api.post(`/bookings/${id}/check-out`)
    updateLocal(id, res.data)
    return res.data
  }

  async function cancelBooking(id) {
    const res = await api.post(`/bookings/${id}/cancel`)
    updateLocal(id, res.data)
    return res.data
  }

  async function getBookingsByGuest(guestId) {
    const res = await api.get(`/bookings/guest/${guestId}`)
    return res.data
  }

  function updateLocal(id, data) {
    const idx = bookings.value.findIndex((b) => b.id === id)
    if (idx !== -1) bookings.value[idx] = data
  }

  return { bookings, loading, fetchBookings, createBooking, checkIn, checkOut, cancelBooking, getBookingsByGuest }
})
