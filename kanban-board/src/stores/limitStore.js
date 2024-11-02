import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useLimitStore = defineStore("limitStatus", () => {
  const limitisStatus = ref({})

  const getLimit = () => {
    return limitisStatus.value
  }

  const editLimit = (limit) => {
    return (limitisStatus.value = limit)
  }

  const clearLimit = () => {
    return limitisStatus.value = {}
  }

  return { editLimit, getLimit , clearLimit }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useLimitStore, import.meta.hot))
}
