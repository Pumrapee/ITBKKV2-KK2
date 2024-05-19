import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useLimitStore = defineStore("limitStatus", () => {
  const limitisStatus = ref({})

  const getLimit = () => {
    return limitisStatus.value
  }

  const addLimit = (limit) => {
    return (limitisStatus.value = limit)
  }

  const updateLimit = (limit) => {
    return (limitisStatus.value = limit)
  }

  return { addLimit, getLimit }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useLimitStore, import.meta.hot))
}
