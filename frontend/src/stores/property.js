import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const usePropertyStore = defineStore('property', () => {
  const properties = ref([])
  const propertyTypes = ref([])
  const activePropertyId = ref(null)
  const loading = ref(false)

  const activeProperty = computed(() =>
    properties.value.find((p) => p.id === activePropertyId.value) || null
  )

  const activePropertyType = computed(() => activeProperty.value?.propertyType || 'HOTEL')

  async function fetchProperties() {
    loading.value = true
    try {
      const res = await api.get('/properties')
      properties.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchPropertyTypes() {
    const res = await api.get('/properties/types')
    propertyTypes.value = res.data
  }

  async function createProperty(property) {
    const res = await api.post('/properties', property)
    properties.value.push(res.data)
    return res.data
  }

  async function updateProperty(id, property) {
    const res = await api.put(`/properties/${id}`, property)
    const idx = properties.value.findIndex((p) => p.id === id)
    if (idx !== -1) properties.value[idx] = res.data
    return res.data
  }

  function setActiveProperty(id) {
    activePropertyId.value = id
    localStorage.setItem('activePropertyId', id)
  }

  function loadActiveProperty() {
    const saved = localStorage.getItem('activePropertyId')
    if (saved) activePropertyId.value = Number(saved)
  }

  return {
    properties,
    propertyTypes,
    activePropertyId,
    activeProperty,
    activePropertyType,
    loading,
    fetchProperties,
    fetchPropertyTypes,
    createProperty,
    updateProperty,
    setActiveProperty,
    loadActiveProperty,
  }
})
