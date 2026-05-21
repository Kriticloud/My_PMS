import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

export function useWebSocket() {
  const connected = ref(false)
  const client = ref(null)
  const subscriptions = ref([])

  function connect() {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        connected.value = true
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      },
    })

    stompClient.activate()
    client.value = stompClient
  }

  function subscribe(topic, callback) {
    if (!client.value) return

    const waitForConnection = () => {
      if (connected.value && client.value?.connected) {
        const sub = client.value.subscribe(topic, (message) => {
          const body = JSON.parse(message.body)
          callback(body)
        })
        subscriptions.value.push(sub)
      } else {
        setTimeout(waitForConnection, 500)
      }
    }
    waitForConnection()
  }

  function disconnect() {
    subscriptions.value.forEach((sub) => sub.unsubscribe())
    subscriptions.value = []
    if (client.value) {
      client.value.deactivate()
      client.value = null
    }
    connected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return { connected, connect, subscribe, disconnect }
}
