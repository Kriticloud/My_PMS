<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Reports</h2>

    <!-- Report Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <button
        v-for="report in reports"
        :key="report.id"
        @click="activeReport = report.id"
        :class="['bg-white rounded-xl shadow p-6 text-left hover:shadow-lg transition border-2',
          activeReport === report.id ? 'border-indigo-500' : 'border-transparent']"
      >
        <div class="text-3xl mb-2">{{ report.icon }}</div>
        <h3 class="text-lg font-bold text-gray-800">{{ report.title }}</h3>
        <p class="text-sm text-gray-500 mt-1">{{ report.description }}</p>
      </button>
    </div>

    <!-- Report Content -->
    <div class="bg-white rounded-xl shadow p-6">
      <!-- Occupancy Report -->
      <div v-if="activeReport === 'occupancy'">
        <h3 class="text-lg font-bold mb-4">Occupancy Report</h3>
        <div v-if="occupancy" class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div class="p-4 bg-gray-50 rounded-lg">
            <p class="text-sm text-gray-500">Total Rooms</p>
            <p class="text-2xl font-bold">{{ occupancy.totalRooms }}</p>
          </div>
          <div class="p-4 bg-green-50 rounded-lg">
            <p class="text-sm text-gray-500">Available</p>
            <p class="text-2xl font-bold text-green-600">{{ occupancy.availableRooms }}</p>
          </div>
          <div class="p-4 bg-red-50 rounded-lg">
            <p class="text-sm text-gray-500">Occupied</p>
            <p class="text-2xl font-bold text-red-600">{{ occupancy.occupiedRooms }}</p>
          </div>
          <div class="p-4 bg-yellow-50 rounded-lg">
            <p class="text-sm text-gray-500">Cleaning</p>
            <p class="text-2xl font-bold text-yellow-600">{{ occupancy.cleaningRooms }}</p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg">
            <p class="text-sm text-gray-500">Maintenance</p>
            <p class="text-2xl font-bold text-gray-600">{{ occupancy.maintenanceRooms }}</p>
          </div>
          <div class="p-4 bg-indigo-50 rounded-lg">
            <p class="text-sm text-gray-500">Occupancy Rate</p>
            <p class="text-2xl font-bold text-indigo-600">{{ occupancy.occupancyRate }}%</p>
          </div>
        </div>
      </div>

      <!-- Revenue Report -->
      <div v-if="activeReport === 'revenue'">
        <h3 class="text-lg font-bold mb-4">Daily Revenue Report</h3>
        <div v-if="revenue" class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="p-6 bg-blue-50 rounded-lg">
            <p class="text-sm text-gray-500">Invoice Revenue</p>
            <p class="text-3xl font-bold text-blue-600">₹{{ formatAmount(revenue.invoiceRevenue) }}</p>
          </div>
          <div class="p-6 bg-purple-50 rounded-lg">
            <p class="text-sm text-gray-500">POS Revenue</p>
            <p class="text-3xl font-bold text-purple-600">₹{{ formatAmount(revenue.posRevenue) }}</p>
          </div>
          <div class="p-6 bg-green-50 rounded-lg">
            <p class="text-sm text-gray-500">Total Revenue</p>
            <p class="text-3xl font-bold text-green-600">₹{{ formatAmount(revenue.totalRevenue) }}</p>
          </div>
        </div>
      </div>

      <!-- POS Sales Report -->
      <div v-if="activeReport === 'pos-sales'">
        <h3 class="text-lg font-bold mb-4">POS Sales Report</h3>
        <div v-if="posSales">
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="p-4 bg-indigo-50 rounded-lg">
              <p class="text-sm text-gray-500">Total Orders</p>
              <p class="text-2xl font-bold text-indigo-600">{{ posSales.totalOrders }}</p>
            </div>
            <div class="p-4 bg-green-50 rounded-lg">
              <p class="text-sm text-gray-500">Total Revenue</p>
              <p class="text-2xl font-bold text-green-600">₹{{ formatAmount(posSales.revenue) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import api from '../api'

const activeReport = ref('occupancy')
const occupancy = ref(null)
const revenue = ref(null)
const posSales = ref(null)

const reports = [
  { id: 'occupancy', title: 'Occupancy Report', description: 'Room occupancy statistics', icon: '🏨' },
  { id: 'revenue', title: 'Revenue Report', description: 'Daily revenue breakdown', icon: '💰' },
  { id: 'pos-sales', title: 'POS Sales Report', description: 'Point of sale statistics', icon: '📊' },
]

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('en-IN')
}

async function loadReport(type) {
  try {
    if (type === 'occupancy') {
      const res = await api.get('/reports/occupancy')
      occupancy.value = res.data
    } else if (type === 'revenue') {
      const res = await api.get('/reports/revenue')
      revenue.value = res.data
    } else if (type === 'pos-sales') {
      const res = await api.get('/reports/pos-sales')
      posSales.value = res.data
    }
  } catch (err) {
    console.error('Failed to load report:', err)
  }
}

watch(activeReport, (val) => loadReport(val))
onMounted(() => loadReport('occupancy'))
</script>
