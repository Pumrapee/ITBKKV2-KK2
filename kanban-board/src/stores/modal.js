// import { values } from "cypress/types/lodash"
import { defineStore } from "pinia"

export const useModalStore = defineStore("modals", () => {
  const modal = ref()

  const modalOpen = (boolean) => {
    modal.value = boolean
  }
  return { modalOpen }
})
