<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800 dark:text-white">Dashboard</h2>
      <span class="text-sm text-gray-500 dark:text-gray-400">Last updated: {{ lastUpdate }}</span>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <KpiCard title="Total Rooms" :value="stats.totalRooms || 0" color="indigo">
        <template #icon><span class="text-2xl">🏨</span></template>
      </KpiCard>
      <KpiCard title="Occupied" :value="stats.occupiedRooms || 0" color="red" :subtitle="`${stats.occupancyRate || 0}% occupancy`">
        <template #icon><span class="text-2xl">🛏️</span></template>
      </KpiCard>
      <KpiCard title="Today's Check-ins" :value="stats.todaysCheckIns || 0" color="green">
        <template #icon><span class="text-2xl">📥</span></template>
      </KpiCard>
      <KpiCard title="Daily Revenue" :value="formatAmount(stats.dailyRevenue)" prefix="₹" color="teal">
        <template #icon><span class="text-2xl">💰</span></template>
      </KpiCard>
    </div>

    <!-- Second Row KPIs -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
      <KpiCard title="Available Rooms" :value="stats.availableRooms || 0" color="green">
        <template #icon><span class="text-2xl">✅</span></template>
      </KpiCard>
      <KpiCard title="Check-outs Today" :value="stats.todaysCheckOuts || 0" color="amber">
        <template #icon><span class="text-2xl">📤</span></template>
      </KpiCard>
      <KpiCard title="POS Revenue" :value="formatAmount(stats.posRevenue)" prefix="₹" color="purple">
        <template #icon><span class="text-2xl">💳</span></template>
      </KpiCard>
      <KpiCard title="Total Bookings" :value="stats.totalBookings || 0" color="blue">
        <template #icon><span class="text-2xl">📋</span></template>
      </KpiCard>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
      <!-- Daily Revenue Chart -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Daily Revenue (7 Days)</h3>
        <Line v-if="revenueChartData" :data="revenueChartData" :options="lineChartOptions" />
        <p v-else class="text-gray-400 text-center py-10">Loading chart...</p>
      </div>

      <!-- Occupancy Chart -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Room Occupancy</h3>
        <div class="flex items-center justify-center" style="height: 250px">
          <Doughnut v-if="occupancyChartData" :data="occupancyChartData" :options="doughnutOptions" />
          <p v-else class="text-gray-400">Loading chart...</p>
        </div>
      </div>
    </div>

    <!-- POS Sales Chart -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 mb-8">
      <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">POS Sales (7 Days)</h3>
      <Bar v-if="posSalesChartData" :data="posSalesChartData" :options="barChartOptions" />
      <p v-else class="text-gray-400 text-center py-10">Loading chart...</p>
    </div>

    <!-- Room Status Grid -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Room Status Overview</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-xl border border-green-200 dark:border-green-800">
          <p class="text-2xl font-bold text-green-600">{{ stats.availableRooms || 0 }}</p>
          <p class="text-sm text-green-700 dark:text-green-400">Available</p>
        </div>
        <div class="text-center p-4 bg-red-50 dark:bg-red-900/20 rounded-xl border border-red-200 dark:border-red-800">
          <p class="text-2xl font-bold text-red-600">{{ stats.occupiedRooms || 0 }}</p>
          <p class="text-sm text-red-700 dark:text-red-400">Occupied</p>
        </div>
        <div class="text-center p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-xl border border-yellow-200 dark:border-yellow-800">
          <p class="text-2xl font-bold text-yellow-600">{{ stats.todaysCheckOuts || 0 }}</p>
          <p class="text-sm text-yellow-700 dark:text-yellow-400">Check-outs Today</p>
        </div>
        <div class="text-center p-4 bg-blue-50 dark:bg-blue-900/20 rounded-xl border border-blue-200 dark:border-blue-800">
          <p class="text-2xl font-bold text-blue-600">₹{{ formatAmount(stats.posRevenue) }}</p>
          <p class="text-sm text-blue-700 dark:text-blue-400">POS Revenue</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Line, Bar, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, BarElement, ArcElement, Title, Tooltip, Legend, Filler
} from 'chart.js'
import api from '../api'
import KpiCard from '../components/KpiCard.vue'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend, Filler)

const stats = ref({})
const revenueChartData = ref(null)
const occupancyChartData = ref(null)
const posSalesChartData = ref(null)
const lastUpdate = ref(new Date().toLocaleTimeString())

const lineChartOptions = {
  responsive: true,
  plugins: { legend: { position: 'bottom' } },
  scales: { y: { beginAtZero: true } },
}

const barChartOptions = {
  responsive: true,
  plugins: { legend: { position: 'bottom' } },
  scales: { y: { beginAtZero: true } },
}

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' } },
}

onMounted(async () => {
  await loadDashboard()
})

async function loadDashboard() {
  try {
    const [dashRes, revRes, posRes, occRes] = await Promise.all([
      api.get('/reports/dashboard'),
      api.get('/reports/revenue?days=7'),
      api.get('/reports/pos-sales?days=7'),
      api.get('/reports/occupancy'),
    ])

    stats.value = dashRes.data
    lastUpdate.value = new Date().toLocaleTimeString()

    // Revenue Line Chart
    const revData = revRes.data
    revenueChartData.value = {
      labels: revData.map((d) => d.date),
      datasets: [
        {
          label: 'Invoice Revenue',
          data: revData.map((d) => Number(d.invoiceRevenue)),
          borderColor: '#4f46e5',
          backgroundColor: 'rgba(79,70,229,0.1)',
          fill: true,
          tension: 0.4,
        },
        {
          label: 'POS Revenue',
          data: revData.map((d) => Number(d.posRevenue)),
          borderColor: '#10b981',
          backgroundColor: 'rgba(16,185,129,0.1)',
          fill: true,
          tension: 0.4,
        },
      ],
    }

    // POS Sales Bar Chart
    const posData = posRes.data
    posSalesChartData.value = {
      labels: posData.map((d) => d.date),
      datasets: [
        {
          label: 'Orders',
          data: posData.map((d) => d.totalOrders),
          backgroundColor: '#8b5cf6',
          borderRadius: 6,
        },
        {
          label: 'Revenue (₹)',
          data: posData.map((d) => Number(d.totalRevenue)),
          backgroundColor: '#06b6d4',
          borderRadius: 6,
        },
      ],
    }

    // Occupancy Doughnut
    const occ = occRes.data
    occupancyChartData.value = {
      labels: Object.keys(occ.statusBreakdown || {}),
      datasets: [{
        data: Object.values(occ.statusBreakdown || {}),
        backgroundColor: ['#10b981', '#ef4444', '#f59e0b', '#6366f1', '#8b5cf6'],
        borderWidth: 0,
      }],
    }
  } catch (err) {
    console.error('Failed to load dashboard:', err)
  }
}

function formatAmount(val) {
  if (!val) return '0'
  return Number(val).toLocaleString('en-IN')
}
</script>
