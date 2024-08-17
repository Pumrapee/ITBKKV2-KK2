import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useStatusStore = defineStore("status", () => {
  const listStatus = ref([])

  const getStatus = () => {
    return listStatus.value
  }
  //actions
  const addStatus = (newTasks) => {
    newTasks.forEach(addOneStatus)
  }
  const addOneStatus = (obj) => {
    listStatus.value.push({ ...obj })
  }

  const getStatusColor = (nameStatus) => {
    const status = listStatus.value.find((sta) => sta.name === nameStatus)
    return status ? status.color : null
  }

  const updateStatus = (upStatus) => {
    listStatus.value = listStatus.value.map((todo) => {
      return todo.id === upStatus.id
        ? {
            ...todo,
            ...upStatus,
          }
        : todo
    })
  }

  const removeStatus = (removeId) => {
    listStatus.value.splice(
      listStatus.value.findIndex((todo) => todo.id === removeId),
      1
    )
  }

  const clearStatus = () => {
    listStatus.value = []
  }

  return {
    getStatus,
    addStatus,
    addOneStatus,
    removeStatus,
    updateStatus,
    getStatusColor,
    clearStatus,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useStatusStore, import.meta.hot))
}
