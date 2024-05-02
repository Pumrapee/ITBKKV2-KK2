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
    newTasks.forEach((newTask) =>
      addTask(
        newTask.id,
        newTask.title,
        newTask.description,
        newTask.assignees,
        newTask.status,
        newTask.createdOn,
        newTask.updatedOn
      )
    )
  }
  const addTask = (id, title, desc, assign, sta, createdTime, updatedTime) => {
    task.value.push({
      id: id,
      title: title,
      description: desc,
      assignees: assign,
      status: sta,
      createdOn: createdTime,
      updatedOn: updatedTime,
    })
  }

  return { getTasks, addTasks, addTask }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useTaskStore, import.meta.hot))
}
