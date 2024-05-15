<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { editItem } from "../libs/fetchUtils"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"

const props = defineProps({
  showModal: Boolean,
  task: Object,
})
const emits = defineEmits(["closeModal", "closeEditTask"])
const newTask = ref({})
const myStatus = useStatusStore()
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

  // ตรวจสอบความยาวของ title, description, และ assignees
  const isTitleTooLong = newTitle?.length > 100
  const isTitleTooEmpthy = newTitle?.length === 0
  const isDescriptionTooLong = newDescription?.length > 500
  const isAssigneesTooLong = newAssignees?.length > 30

  if (isTitleTooLong) {
    errorTask.value.title = "Title exceeds the limit of 100 characters."
  } else if (isTitleTooEmpthy) {
    errorTask.value.title = "Title is required."
  } else {
    errorTask.value.title = ""
  }

  if (isDescriptionTooLong) {
    errorTask.value.description =
      "Description exceeds the limit of 500 characters."
  } else {
    errorTask.value.description = ""
  }

  if (isAssigneesTooLong) {
    errorTask.value.assignees = "Assignees exceeds the limit of 30 characters."
  } else {
    errorTask.value.assignees = ""
  }

  // ตรวจสอบเงื่อนไขทั้งหมดรวมถึงการเปลี่ยนแปลงของข้อมูล
  return (
    isTitleTooLong ||
    isDescriptionTooLong ||
    isAssigneesTooLong ||
    newTitle === null ||
    (oldTask.title === newTitle &&
      oldTask.description === newDescription &&
      oldTask.assignees === newAssignees &&
      oldTask.status === newStatus)
  )
})
const myTask = useTaskStore()
console.log(myTask.getTasks())
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
    `${import.meta.env.VITE_API_URL}tasks`,
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

    emits("closeEditTask", statusCode)
  }
  if (statusCode === 400) {
    emits("closeEditTask", statusCode)
  }
  if (statusCode === 404) {
    myTask.removeTasks(editedItem.id)
    emits("closeEditTask", statusCode)
  }
}

watch(props, () => {
  if (props.showModal) {
    Object.assign(newTask.value, props.task)
  }
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-6 rounded-lg w-11/12 max-w-3xl">
        <h2 class="text-2xl font-bold text-blue-400 mb-4 border-b-2">
          Edit Task
        </h2>
        <div class="mb-4">
          <label for="title" class="block text-blue-400 font-bold mb-2"
            >Title</label
          >
          <input
            type="text"
            id="title"
            v-model="newTask.title"
            placeholder="Enter Title here..."
            class="itbkk-title w-full border border-blue-400 rounded-lg py-2 px-3 input input-ghost"
          />
          <div class="flex justify-between">
            <p class="text-red-400">
              {{ errorTask.title }}
            </p>
            <p
              class="text-gray-300 whitespace-nowrap text-sm text-end mt-1"
              :class="{
                'text-red-400':
                  newTask.title?.length > 100 || newTask.title?.length === 0,
              }"
            >
              {{ newTask.title?.trim()?.length || 0 }} / 100
            </p>
          </div>
        </div>
        <div class="flex">
          <div class="w-2/3 mr-2">
            <label for="description" class="block text-blue-400 font-bold mb-2"
              >Description</label
            >
            <textarea
              id="description"
              v-model="newTask.description"
              class="itbkk-description w-full border border-blue-400 rounded-lg py-3 px-3 h-72 textarea textarea-ghost"
              :class="
                newTask.description
                  ? 'bg-white text-black'
                  : 'italic text-gray-500'
              "
              placeholder="No Description Provided"
            ></textarea>
            <div class="flex justify-between">
              <p class="text-red-400">
                {{ errorTask.description }}
              </p>
              <p
                class="text-gray-300 text-sm text-end"
                :class="{
                  'text-red-400': newTask.description?.trim()?.length > 500,
                }"
              >
                {{ newTask.description?.trim()?.length || 0 }} / 500
              </p>
            </div>
          </div>
          <div class="w-1/3">
            <div>
              <label for="assignees" class="block text-blue-400 font-bold mb-2"
                >Assignees</label
              >
              <textarea
                id="assignees"
                v-model="newTask.assignees"
                class="itbkk-assignees w-full border border-blue-400 rounded-lg py-3 px-3 h-42 textarea textarea-ghost"
                :class="
                  newTask.assignees
                    ? 'bg-white text-black'
                    : 'italic text-gray-500'
                "
                placeholder="Unassigned"
              ></textarea>
              <div class="flex justify-between">
                <p class="text-red-400 text-sm w-40">
                  {{ errorTask.assignees }}
                </p>
                <p
                  class="text-gray-300 text-sm text-end"
                  :class="{
                    'text-red-400': newTask.assignees?.trim()?.length > 30,
                  }"
                >
                  {{ newTask.assignees?.trim()?.length || 0 }} / 30
                </p>
              </div>
            </div>
            <div>
              <label for="status" class="block text-blue-400 font-bold mb-2"
                >Status</label
              >
              <select
                v-model="newTask.status"
                class="itbkk-status pl-5 border-2 rounded-md h-10 pr-5 w-full"
              >
                <option
                  v-for="(status, index) in myStatus.getStatus()"
                  :key="index"
                  :value="status.name"
                >
                  {{ status.name }}
                </option>
              </select>
            </div>
            <div class="mt-5">
              <p
                class="itbkk-timezone pl-3 font-semibold text-sm text-blue-400"
              >
                Time Zone :
                {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
              </p>
              <p
                class="itbkk-created-on pl-3 font-semibold text-sm text-blue-400"
              >
                Created On :
                {{ new Date(task.createdOn).toLocaleString("en-GB") }}
              </p>
              <p
                class="itbkk-updated-on pl-3 font-semibold text-sm text-blue-400"
              >
                Updated On :
                {{ new Date(task.updatedOn).toLocaleString("en-GB") }}
              </p>
            </div>
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <div>
            <button
              class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
              @click="editSave(newTask)"
              :disabled="changeTask"
            >
              Save
            </button>
            <button
              class="itbkk-button-cancle btn"
              @click="$emit('closeModal')"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
