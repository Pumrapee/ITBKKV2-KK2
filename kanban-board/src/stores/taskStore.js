import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"
// import { assign } from "cypress/types/lodash"

export const useTaskStore = defineStore("task", () => {
  //state
  const task = ref([])
  //view
  const getTasks = () => {
    return task.value
  }
  //actions
  const addTasks = (newTasks) => {
    // loop array
    newTasks.forEach(addTask)
  }

  const addTask = (obj) => {
    // push object เข้า task
    task.value.push({ ...obj })
  }

  const updateTask = (updatedTask) => {
    task.value = task.value.map((todo) => {
      //จะสร้าง object ใหม่ที่รวม properties ของ todo และ updatedTask เข้าด้วยกัน
      return todo.id === updatedTask.id ? { ...todo, ...updatedTask } : todo
    })
  }

  const updateTaskStatus = (updateStatus) => {
    task.value = task.value.map((todo) => {
      return todo.status !== updateStatus.name
        ? { ...todo, ...updateStatus }
        : todo
    })
  }

  const removeTasks = (removeId) => {
    task.value.splice(
      task.value.findIndex((todo) => todo.id === removeId),
      1
    )
  }

  const matchStatus = (statusName) => {
    return task.value.filter((todo) => todo.status === statusName)
  }

  const clearTask = () => {
    return (task.value = [])
  }
  return {
    getTasks,
    addTasks,
    addTask,
    updateTask,
    removeTasks,
    updateTaskStatus,
    clearTask,
    matchStatus,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useTaskStore, import.meta.hot))
}
