<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard</h2>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-xl shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">Total Rooms</p>
            <p class="text-3xl font-bold text-gray-800">{{ stats.totalRooms || 0 }}</p>
          </div>
          <div class="text-4xl">🏨</div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">Occupied Rooms</p>
            <p class="text-3xl font-bold text-indigo-600">{{ stats.occupiedRooms || 0 }}</p>
          </div>
          <div class="text-4xl">🛏️</div>
        </div>
        <p class="text-sm text-gray-400 mt-2">{{ stats.occupancyRate || 0 }}% occupancy</p>
      </div>

      <div class="bg-white rounded-xl shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">Today's Check-ins</p>
            <p class="text-3xl font-bold text-green-600">{{ stats.todaysCheckIns || 0 }}</p>
          </div>
          <div class="text-4xl">📥</div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow p-6">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">Daily Revenue</p>
            <p class="text-3xl font-bold text-emerald-600">₹{{ formatAmount(stats.totalDailyRevenue) }}</p>
          </div>
          <div class="text-4xl">💰</div>
        </div>
      </div>
    </div>

    <!-- Room Status Overview -->
    <div class="bg-white rounded-xl shadow p-6">
      <h3 class="text-lg font-semibold text-gray-800 mb-4">Room Status Overview</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="text-center p-4 bg-green-50 rounded-lg">
          <p class="text-2xl font-bold text-green-600">{{ stats.availableRooms || 0 }}</p>
          <p class="text-sm text-green-700">Available</p>
        </div>
        <div class="text-center p-4 bg-red-50 rounded-lg">
          <p class="text-2xl font-bold text-red-600">{{ stats.occupiedRooms || 0 }}</p>
          <p class="text-sm text-red-700">Occupied</p>
        </div>
        <div class="text-center p-4 bg-yellow-50 rounded-lg">
          <p class="text-2xl font-bold text-yellow-600">{{ stats.todaysCheckOuts || 0 }}</p>
          <p class="text-sm text-yellow-700">Today's Check-outs</p>
        </div>
        <div class="text-center p-4 bg-blue-50 rounded-lg">
          <p class="text-2xl font-bold text-blue-600">₹{{ formatAmount(stats.dailyPosRevenue) }}</p>
          <p class="text-sm text-blue-700">POS Revenue</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const stats = ref({})

onMounted(async () => {
  try {
    const res = await api.get('/reports/dashboard')
    stats.value = res.data
  } catch (err) {
    console.error('Failed to load dashboard stats:', err)
  }
})

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('en-IN')
}
</script>
