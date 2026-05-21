import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useRoomStore = defineStore('rooms', () => {
  const rooms = ref([])
  const roomTypes = ref([])
  const loading = ref(false)

  async function fetchRooms() {
    loading.value = true
    try {
      const res = await api.get('/rooms')
      rooms.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchRoomTypes() {
    const res = await api.get('/rooms/types')
    roomTypes.value = res.data
  }

  async function createRoom(room) {
    const res = await api.post('/rooms', room)
    rooms.value.push(res.data)
    return res.data
  }

  async function updateRoom(id, room) {
    const res = await api.put(`/rooms/${id}`, room)
    const idx = rooms.value.findIndex((r) => r.id === id)
    if (idx !== -1) rooms.value[idx] = res.data
    return res.data
  }

  async function updateRoomStatus(id, status) {
    const res = await api.patch(`/rooms/${id}/status?status=${status}`)
    const idx = rooms.value.findIndex((r) => r.id === id)
    if (idx !== -1) rooms.value[idx] = res.data
    return res.data
  }

  async function deleteRoom(id) {
    await api.delete(`/rooms/${id}`)
    rooms.value = rooms.value.filter((r) => r.id !== id)
  }

  return { rooms, roomTypes, loading, fetchRooms, fetchRoomTypes, createRoom, updateRoom, updateRoomStatus, deleteRoom }
})
