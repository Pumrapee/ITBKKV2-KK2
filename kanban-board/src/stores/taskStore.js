import { ref } from "vue"
import { defineStore } from "pinia"
import { assign } from "cypress/types/lodash"

export const useTaskStore = defineStore("task", () => {
  //state
  const task = ref([])
  //view
  const getTasks = () => {
    return todos.value
  }
  //actions
  const addTasks = (newTasks) => {
    newTasks.forEach((newTask) =>
      addTask(
        newTask.id,
        newTask.title,
        newTask.description,
        newTask.assigness,
        newTask.status,
        newTask,
        createdOn,
        updatedOn
      )
    )
  }
  const addTask = (id, title, desc, assign, sta, createdTime, updatedTime) => {
    todos.value.push({
      id: id,
      title: title,
      description: desc,
      assigness: assign,
      status: sta,
      createdOn: createdTime,
      updatedOn: updatedTime,
    })
  }

  return { getTasks, addTasks, addTask }
})
