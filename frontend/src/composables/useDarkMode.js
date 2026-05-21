import { ref, onMounted } from 'vue'

export function useDarkMode() {
  const isDark = ref(localStorage.getItem('darkMode') === 'true')

  function toggleDarkMode() {
    isDark.value = !isDark.value
    localStorage.setItem('darkMode', isDark.value)
    applyDarkMode()
  }

  function applyDarkMode() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  onMounted(() => {
    applyDarkMode()
  })

  return { isDark, toggleDarkMode }
}
