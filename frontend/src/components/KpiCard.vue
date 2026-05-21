<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-5 transition-all hover:shadow-md">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm font-medium text-gray-500 dark:text-gray-400">{{ title }}</p>
        <p class="mt-1 text-2xl font-bold text-gray-900 dark:text-white">{{ formattedValue }}</p>
        <p v-if="subtitle" class="mt-1 text-xs text-gray-400 dark:text-gray-500">{{ subtitle }}</p>
      </div>
      <div :class="['p-3 rounded-xl', colorClasses]">
        <slot name="icon">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
          </svg>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [Number, String], required: true },
  subtitle: { type: String, default: '' },
  color: { type: String, default: 'indigo' },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
})

const colorMap = {
  indigo: 'bg-indigo-500',
  green: 'bg-green-500',
  blue: 'bg-blue-500',
  amber: 'bg-amber-500',
  red: 'bg-red-500',
  purple: 'bg-purple-500',
  teal: 'bg-teal-500',
  orange: 'bg-orange-500',
}

const colorClasses = computed(() => colorMap[props.color] || colorMap.indigo)

const formattedValue = computed(() => {
  let val = props.value
  if (typeof val === 'number') {
    val = val.toLocaleString()
  }
  return `${props.prefix}${val}${props.suffix}`
})
</script>
