<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { editItem } from "../libs/fetchUtils"
import { useTaskStore } from "@/stores/taskStore"

const props = defineProps({
  showModal: Boolean,
  task: Object,
})
const emits = defineEmits(["closeModal"])
const editPass = ref(false)
const newTask = ref({})
const errorTask = ref({
  title: "",
  description: "",
  assignees: "",
})

const changeTask = computed(() => {
  const trimAndCheckNull = (value) => {
    if (value === null) return null
    else {
      return value?.trim().length === 0 ? null : value?.trim()
    }
  }

  const oldTask = {
    title: props.task.title,
    description: props.task.description,
    assignees: props.task.assignees,
    status: props.task.status,
  }

  const newTitle = trimAndCheckNull(newTask.value.title)
  const newDescription = trimAndCheckNull(newTask.value.description)
  const newAssignees = trimAndCheckNull(newTask.value.assignees)
  const newStatus = newTask.value.status

  newTask.value.title?.length > 100
    ? (errorTask.value.title = "Title exceeds the limit of 100 characters.")
    : (errorTask.value.title = "")

  newTask.value.description?.length > 500
    ? (errorTask.value.description =
        "Description exceeds the limit of 500 characters.")
    : (errorTask.value.description = "")

  newTask.value.assignees?.length > 30
    ? (errorTask.value.assignees =
        "Assignees exceeds the limit of 30 characters.")
    : (errorTask.value.assignees = "")

  return (
    (oldTask.title === newTitle &&
      oldTask.description === newDescription &&
      oldTask.assignees === newAssignees &&
      oldTask.status === newStatus) ||
    newTitle === null
  )
})

const myTask = useTaskStore()
const editSave = async (task) => {
  const editedTask = { ...task }
  editedTask.title = editedTask.title?.trim()
  editedTask.description = editedTask.description?.trim()
  editedTask.assignees = editedTask.assignees?.trim()

  if (editedTask.title === "") {
    editedTask.title = null
  }
  if (editedTask.description === "") {
    editedTask.description = null
  }
  if (editedTask.assignees === "") {
    editedTask.assignees = null
  }

  const { editedItem, statusCode } = await editItem(
    import.meta.env.VITE_BASE_URL,
    editedTask.id,
    {
      title: editedTask.title,
      description: editedTask.description,
      assignees: editedTask.assignees,
      status: editedTask.status,
    }
  )

  if (statusCode === 200) {
    myTask.updateTask(
      editedItem.id,
      editedItem.title,
      editedItem.description,
      editedItem.assignees,
      editedItem.status,
      editedItem.createdTime,
      editedItem.updatedTime
    )

    emits("closeModal")
    editPass.value = true
  }
  if (statusCode === 400) {
    alert("There are some fields that exceed the limit.")
  }
}

watch(props, () => {
  if (props.showModal) {
    Object.assign(newTask.value, props.task)
  }
})
</script>

<template>
  <!-- Alert Pass Edit-->
  <div v-if="editPass" class="flex justify-center mt-3">
    <div role="alert" class="alert alert-success shadow-lg w-2/5">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6 text-white"
        fill="none"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <span class="text-white">The task has been updated</span>
      <button class="text-white" @click="editPass = false">X</button>
    </div>
  </div>

  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="grid grid-rows-6 grid-cols-4 gap-2 bg-white p-10 rounded-lg w-4/5"
      >
        <input
          type="text"
          className="itbkk-title input pl-2 col-span-4 font-semibold text-3xl text-blue-400 rounded-lg "
          v-model="newTask.title"
          placeholder="Enter Title here..."
        />

        <div class="border-2 border-blue-400 row-span-4 col-span-3 rounded-lg">
          <p class="p-5 font-bold text-blue-400">Description</p>
          <textarea
            v-model="newTask.description"
            class="itbkk-description textarea textarea-ghost p-4 h-3/5 w-11/12 ml-9"
            :class="
              newTask.description
                ? 'bg-white text-black'
                : 'italic text-gray-500'
            "
            placeholder="No Description Provided"
          ></textarea>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 row-start-2 row-end-4 rounded-lg"
        >
          <p class="p-3 font-bold text-blue-400">Assignees</p>
          <textarea
            v-model="newTask.assignees"
            class="itbkk-assignees pl-5 textarea textarea-ghost h-5/5 w-11/12 ml-2"
            :class="
              newTask.assignees ? 'bg-white text-black' : 'italic text-gray-500'
            "
            placeholder="Unassigned"
          ></textarea>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 p-2 row-start-4 row-end-5 rounded-lg"
        >
          <label for="status" class="p-2 font-bold text-blue-400">Status</label>
          <select
            v-model="newTask.status"
            class="itbkk-status pl-5 border-2 rounded-md h-10 pr-5"
          >
            <option value="NO_STATUS">No Status</option>
            <option value="TO_DO">To Do</option>
            <option value="DOING">Doing</option>
            <option value="DONE">Done</option>
          </select>
        </div>

        <div class="col-start-4 rounded-lg">
          <p class="itbkk-timezone pl-3 font-semibold text-sm text-blue-400">
            Time Zone : {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
          </p>
          <p class="itbkk-created-on pl-3 font-semibold text-sm text-blue-400">
            Created On :
            {{ new Date(task.createdOn).toLocaleString("en-GB") }}
          </p>
          <p class="itbkk-updated-on pl-3 font-semibold text-sm text-blue-400">
            Updated On :
            {{ new Date(task.updatedOn).toLocaleString("en-GB") }}
          </p>
        </div>

        <div class="col-start-1 row-start-6 col-span-2">
          <p class="text-red-500">
            {{
              errorTask.title || errorTask.description || errorTask.assignees
            }}
          </p>
        </div>

        <div class="col-span-4 place-self-end rounded-lg">
          <button
            class="btn mr-3 bg-green-400 disabled:bg-green-200"
            @click="editSave(newTask)"
            :disabled="changeTask"
          >
            Save
          </button>
          <button class="btn" @click="$emit('closeModal')">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
