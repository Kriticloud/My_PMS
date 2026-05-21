<template>
  <div class="ag-grid-wrapper">
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center gap-2">
        <input
          v-model="quickFilter"
          type="text"
          placeholder="Quick filter..."
          class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm
                 bg-white dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-primary-500"
        />
        <slot name="toolbar" />
      </div>
      <div class="flex items-center gap-2">
        <button
          v-if="showExport"
          @click="exportExcel"
          class="px-3 py-2 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition flex items-center gap-1"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          Export
        </button>
        <button
          @click="resetColumns"
          class="px-3 py-2 text-sm bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-white rounded-lg hover:bg-gray-300 dark:hover:bg-gray-500 transition"
        >
          Reset Columns
        </button>
      </div>
    </div>
    <ag-grid-vue
      :style="{ height: gridHeight, width: '100%' }"
      :class="gridThemeClass"
      :columnDefs="columnDefs"
      :rowData="rowData"
      :defaultColDef="mergedDefaultColDef"
      :pagination="pagination"
      :paginationPageSize="pageSize"
      :paginationPageSizeSelector="[10, 20, 50, 100]"
      :quickFilterText="quickFilter"
      :rowSelection="rowSelection"
      :animateRows="true"
      :suppressCellFocus="true"
      :masterDetail="masterDetail"
      :detailCellRendererParams="detailCellRendererParams"
      @grid-ready="onGridReady"
      @row-clicked="$emit('row-clicked', $event)"
      @selection-changed="$emit('selection-changed', $event)"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import 'ag-grid-community/styles/ag-grid.css'
import 'ag-grid-community/styles/ag-theme-alpine.css'
import * as XLSX from 'xlsx'

const props = defineProps({
  columnDefs: { type: Array, required: true },
  rowData: { type: Array, default: () => [] },
  defaultColDef: { type: Object, default: () => ({}) },
  pagination: { type: Boolean, default: true },
  pageSize: { type: Number, default: 20 },
  gridHeight: { type: String, default: '600px' },
  rowSelection: { type: String, default: undefined },
  showExport: { type: Boolean, default: true },
  masterDetail: { type: Boolean, default: false },
  detailCellRendererParams: { type: Object, default: undefined },
  darkMode: { type: Boolean, default: false },
})

defineEmits(['row-clicked', 'selection-changed'])

const quickFilter = ref('')
const gridApi = ref(null)

const gridThemeClass = computed(() =>
  props.darkMode ? 'ag-theme-alpine-dark' : 'ag-theme-alpine'
)

const mergedDefaultColDef = computed(() => ({
  sortable: true,
  filter: true,
  resizable: true,
  floatingFilter: true,
  minWidth: 100,
  ...props.defaultColDef,
}))

function onGridReady(params) {
  gridApi.value = params.api
  params.api.sizeColumnsToFit()
}

function resetColumns() {
  if (gridApi.value) {
    gridApi.value.resetColumnState()
    gridApi.value.sizeColumnsToFit()
  }
}

function exportExcel() {
  if (!gridApi.value) return

  const rowData = []
  gridApi.value.forEachNodeAfterFilterAndSort((node) => {
    rowData.push(node.data)
  })

  const ws = XLSX.utils.json_to_sheet(rowData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Data')
  XLSX.writeFile(wb, 'export.xlsx')
}

watch(() => props.rowData, () => {
  if (gridApi.value) {
    gridApi.value.sizeColumnsToFit()
  }
})
</script>

<style scoped>
.ag-grid-wrapper {
  width: 100%;
}
</style>
