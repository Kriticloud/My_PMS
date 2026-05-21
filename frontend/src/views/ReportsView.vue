<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-800 dark:text-white mb-6">Reports & Analytics</h2>

    <!-- Report Tabs -->
    <div class="flex gap-2 mb-6 flex-wrap">
      <button
        v-for="report in reports"
        :key="report.id"
        @click="activeReport = report.id"
        :class="['px-5 py-3 rounded-xl text-sm font-medium transition flex items-center gap-2',
          activeReport === report.id
            ? 'bg-indigo-600 text-white shadow-lg'
            : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-700']"
      >
        <span class="text-lg">{{ report.icon }}</span>
        {{ report.title }}
      </button>
    </div>

    <!-- Occupancy Report -->
    <div v-if="activeReport === 'occupancy' && occupancy">
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <KpiCard title="Total Rooms" :value="occupancy.totalRooms" color="gray" />
        <KpiCard title="Available" :value="occupancy.availableRooms" color="green" />
        <KpiCard title="Occupied" :value="occupancy.occupiedRooms" color="red" />
        <KpiCard title="Cleaning" :value="occupancy.cleaningRooms" color="yellow" />
        <KpiCard title="Maintenance" :value="occupancy.maintenanceRooms" color="gray" />
        <KpiCard title="Occupancy Rate" :value="occupancy.occupancyRate + '%'" color="indigo" />
      </div>
      <!-- Occupancy Doughnut Chart -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Room Status Distribution</h3>
          <div class="max-w-xs mx-auto">
            <Doughnut :data="occupancyChartData" :options="doughnutOptions" />
          </div>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Occupancy by Room Type</h3>
          <div v-if="occupancy.byRoomType" class="space-y-4">
            <div v-for="(data, type) in occupancy.byRoomType" :key="type">
              <div class="flex justify-between text-sm mb-1">
                <span class="text-gray-700 dark:text-gray-300">{{ type }}</span>
                <span class="font-medium dark:text-white">{{ data.occupied }}/{{ data.total }}</span>
              </div>
              <div class="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2.5">
                <div class="bg-indigo-600 h-2.5 rounded-full" :style="{ width: (data.total ? (data.occupied / data.total * 100) : 0) + '%' }"></div>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-400 text-center py-8">No room type data available</p>
        </div>
      </div>
    </div>

    <!-- Revenue Report -->
    <div v-if="activeReport === 'revenue'">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6" v-if="revenue">
        <KpiCard title="Room Revenue" :value="'₹' + formatAmount(revenue.invoiceRevenue)" color="blue" />
        <KpiCard title="POS Revenue" :value="'₹' + formatAmount(revenue.posRevenue)" color="purple" />
        <KpiCard title="Total Revenue" :value="'₹' + formatAmount(revenue.totalRevenue)" color="green" />
      </div>
      <!-- Revenue Trend Chart -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white">Revenue Trend (Last {{ revenueDays }} Days)</h3>
          <select v-model="revenueDays" @change="loadReport('revenue')"
                  class="border dark:border-gray-600 rounded-lg px-3 py-1.5 text-sm dark:bg-gray-700 dark:text-white">
            <option :value="7">7 Days</option>
            <option :value="14">14 Days</option>
            <option :value="30">30 Days</option>
          </select>
        </div>
        <Line v-if="revenueChartData" :data="revenueChartData" :options="lineOptions" />
        <p v-else class="text-gray-400 text-center py-12">Loading revenue data...</p>
      </div>
    </div>

    <!-- POS Sales Report -->
    <div v-if="activeReport === 'pos-sales'">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6" v-if="posSales">
        <KpiCard title="Total Orders" :value="posSales.totalOrders" color="indigo" />
        <KpiCard title="Total Revenue" :value="'₹' + formatAmount(posSales.revenue)" color="green" />
        <KpiCard title="Avg Order Value" :value="'₹' + formatAmount(posSales.totalOrders ? (posSales.revenue / posSales.totalOrders) : 0)" color="blue" />
        <KpiCard title="Top Category" :value="posSales.topCategory || '-'" color="purple" />
      </div>
      <!-- POS Sales Chart -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Daily POS Sales</h3>
          <Bar v-if="posSalesChartData" :data="posSalesChartData" :options="barOptions" />
          <p v-else class="text-gray-400 text-center py-12">Loading sales data...</p>
        </div>
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Sales by Category</h3>
          <div class="max-w-xs mx-auto">
            <Doughnut v-if="posCategoryChartData" :data="posCategoryChartData" :options="doughnutOptions" />
          </div>
          <p v-if="!posCategoryChartData" class="text-gray-400 text-center py-12">No category data</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Line, Bar, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale, LinearScale, PointElement, LineElement,
  BarElement, ArcElement, Title, Tooltip, Legend, Filler
} from 'chart.js'
import { useDarkMode } from '../composables/useDarkMode'
import KpiCard from '../components/KpiCard.vue'
import api from '../api'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend, Filler)

const { isDark } = useDarkMode()
const activeReport = ref('occupancy')
const occupancy = ref(null)
const revenue = ref(null)
const posSales = ref(null)
const revenueDays = ref(7)
const dailyRevenue = ref([])
const dailyPosSales = ref([])

const reports = [
  { id: 'occupancy', title: 'Occupancy', icon: '🏨' },
  { id: 'revenue', title: 'Revenue', icon: '💰' },
  { id: 'pos-sales', title: 'POS Sales', icon: '📊' },
]

const textColor = computed(() => isDark.value ? '#e5e7eb' : '#374151')
const gridColor = computed(() => isDark.value ? '#374151' : '#e5e7eb')

const lineOptions = computed(() => ({
  responsive: true,
  plugins: { legend: { labels: { color: textColor.value } } },
  scales: {
    x: { ticks: { color: textColor.value }, grid: { color: gridColor.value } },
    y: { ticks: { color: textColor.value, callback: (v) => '₹' + v.toLocaleString() }, grid: { color: gridColor.value } }
  }
}))

const barOptions = computed(() => ({
  responsive: true,
  plugins: { legend: { labels: { color: textColor.value } } },
  scales: {
    x: { ticks: { color: textColor.value }, grid: { color: gridColor.value } },
    y: { ticks: { color: textColor.value, callback: (v) => '₹' + v.toLocaleString() }, grid: { color: gridColor.value } }
  }
}))

const doughnutOptions = computed(() => ({
  responsive: true,
  plugins: { legend: { position: 'bottom', labels: { color: textColor.value } } }
}))

const occupancyChartData = computed(() => {
  if (!occupancy.value) return null
  return {
    labels: ['Available', 'Occupied', 'Cleaning', 'Maintenance'],
    datasets: [{
      data: [occupancy.value.availableRooms, occupancy.value.occupiedRooms, occupancy.value.cleaningRooms, occupancy.value.maintenanceRooms],
      backgroundColor: ['#10b981', '#ef4444', '#f59e0b', '#6b7280'],
    }]
  }
})

const revenueChartData = computed(() => {
  if (!dailyRevenue.value.length) return null
  return {
    labels: dailyRevenue.value.map(d => d.date),
    datasets: [
      { label: 'Revenue', data: dailyRevenue.value.map(d => d.amount), borderColor: '#6366f1', backgroundColor: 'rgba(99,102,241,0.1)', fill: true, tension: 0.4 },
    ]
  }
})

const posSalesChartData = computed(() => {
  if (!dailyPosSales.value.length) return null
  return {
    labels: dailyPosSales.value.map(d => d.date),
    datasets: [
      { label: 'POS Sales', data: dailyPosSales.value.map(d => d.amount), backgroundColor: '#8b5cf6' },
    ]
  }
})

const posCategoryChartData = computed(() => {
  if (!posSales.value?.categoryBreakdown) return null
  const entries = Object.entries(posSales.value.categoryBreakdown)
  if (!entries.length) return null
  const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
  return {
    labels: entries.map(e => e[0]),
    datasets: [{ data: entries.map(e => e[1]), backgroundColor: colors.slice(0, entries.length) }]
  }
})

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
      const [summary, daily] = await Promise.all([
        api.get('/reports/revenue'),
        api.get(`/reports/revenue?days=${revenueDays.value}`)
      ])
      revenue.value = summary.data
      dailyRevenue.value = daily.data.dailyBreakdown || []
    } else if (type === 'pos-sales') {
      const [summary, daily] = await Promise.all([
        api.get('/reports/pos-sales'),
        api.get('/reports/pos-sales?days=7')
      ])
      posSales.value = summary.data
      dailyPosSales.value = daily.data.dailyBreakdown || []
    }
  } catch (err) {
    console.error('Failed to load report:', err)
  }
}

watch(activeReport, (val) => loadReport(val))
onMounted(() => loadReport('occupancy'))
</script>
