import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const usePosStore = defineStore('pos', () => {
  const menuItems = ref([])
  const categories = ref([])
  const cart = ref([])
  const orders = ref([])
  const loading = ref(false)

  const cartTotal = computed(() =>
    cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  async function fetchMenuItems() {
    const res = await api.get('/menu/items/available')
    menuItems.value = res.data
  }

  async function fetchCategories() {
    const res = await api.get('/menu/categories')
    categories.value = res.data
  }

  async function fetchOrders() {
    loading.value = true
    try {
      const res = await api.get('/pos/orders')
      orders.value = res.data
    } finally {
      loading.value = false
    }
  }

  function addToCart(menuItem) {
    const existing = cart.value.find((i) => i.menuItemId === menuItem.id)
    if (existing) {
      existing.quantity++
    } else {
      cart.value.push({
        menuItemId: menuItem.id,
        name: menuItem.name,
        price: menuItem.price,
        quantity: 1,
        notes: '',
      })
    }
  }

  function removeFromCart(menuItemId) {
    cart.value = cart.value.filter((i) => i.menuItemId !== menuItemId)
  }

  function updateCartItemQty(menuItemId, qty) {
    const item = cart.value.find((i) => i.menuItemId === menuItemId)
    if (item) {
      if (qty <= 0) {
        removeFromCart(menuItemId)
      } else {
        item.quantity = qty
      }
    }
  }

  function clearCart() {
    cart.value = []
  }

  async function placeOrder(orderData) {
    const items = cart.value.map((item) => ({
      menuItemId: item.menuItemId,
      quantity: item.quantity,
      notes: item.notes,
    }))

    const payload = {
      ...orderData,
      items,
    }

    const res = await api.post('/pos/orders', payload)
    orders.value.unshift(res.data)
    clearCart()
    return res.data
  }

  async function updateOrderStatus(id, status) {
    const res = await api.patch(`/pos/orders/${id}/status?status=${status}`)
    const idx = orders.value.findIndex((o) => o.id === id)
    if (idx !== -1) orders.value[idx] = res.data
    return res.data
  }

  return {
    menuItems, categories, cart, orders, loading, cartTotal,
    fetchMenuItems, fetchCategories, fetchOrders,
    addToCart, removeFromCart, updateCartItemQty, clearCart,
    placeOrder, updateOrderStatus,
  }
})
