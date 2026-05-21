import { defineStore } from 'pinia'
import api from '../api'

export const useBillingStore = defineStore('billing', {
  state: () => ({
    invoices: [],
    currentInvoice: null,
  }),
  actions: {
    async fetchInvoices() {
      const res = await api.get('/billing/invoices')
      this.invoices = res.data
    },
    async generateInvoice(bookingId) {
      const res = await api.post(`/billing/invoices/generate/${bookingId}`)
      this.currentInvoice = res.data
      await this.fetchInvoices()
      return res.data
    },
    async getInvoice(id) {
      const res = await api.get(`/billing/invoices/${id}`)
      this.currentInvoice = res.data
      return res.data
    },
    async downloadPdf(invoiceId) {
      const res = await api.get(`/billing/invoices/${invoiceId}/pdf`, { responseType: 'blob' })
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const link = document.createElement('a')
      link.href = url
      link.download = `invoice-${invoiceId}.pdf`
      link.click()
      window.URL.revokeObjectURL(url)
    },
    async processPayment(invoiceId, payment) {
      const res = await api.post('/billing/payments', { ...payment, invoiceId })
      await this.fetchInvoices()
      return res.data
    },
    async splitPayment(invoiceId, payments) {
      const res = await api.post('/billing/payments/split', payments)
      await this.fetchInvoices()
      return res.data
    },
  },
})
