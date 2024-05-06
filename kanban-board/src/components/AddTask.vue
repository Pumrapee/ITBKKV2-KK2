<script setup>
import { defineProps, defineEmits, ref, computed, watch } from "vue"
import { addItem } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"

const { showAdd } = defineProps({
  showAdd: Boolean,
})

const emits = defineEmits(["closeAddModal"])

const selected = ref("NO_STATUS")

const addPass = ref(false)

const listNewTask = ref({
  title: "",
  description: "",
  assignees: "",
  status: selected.value,
})

const errorTask = ref({
  title: "",
  description: "",
  assignees: "",
})

const myTask = useTaskStore()
const saveNewTask = async () => {
  // Trim
  listNewTask.value.title = listNewTask.value.title.trim()
  listNewTask.value.description = listNewTask.value.description.trim()
  listNewTask.value.assignees = listNewTask.value.assignees.trim()

  // Replace empty strings with null
  if (listNewTask.value.title === "") {
    listNewTask.value.title = null
  }
  if (listNewTask.value.description === "") {
    listNewTask.value.description = null
  }
  if (listNewTask.value.assignees === "") {
    listNewTask.value.assignees = null
  }

  const { newTask, statusCode } = await addItem(
    import.meta.env.VITE_BASE_URL,
    listNewTask.value
  )

  if (statusCode === 201) {
    myTask.addTask(
      newTask.id,
      newTask.title,
      newTask.description,
      newTask.assignees,
      newTask.status,
      newTask.createdTime,
      newTask.updatedTime
    )

    listNewTask.value.title = ""
    listNewTask.value.description = ""
    listNewTask.value.assignees = ""
    listNewTask.value.status = selected.value
    emits("closeAddModal")

    addPass.value = true
  }

  if (statusCode === 400) {
    alert("There are some fields that exceed the limit.")
    listNewTask.value.title = ""
    listNewTask.value.description = ""
    listNewTask.value.assignees = ""
    listNewTask.value.status = selected.value
  }
}

const closeAddModal = () => {
  // ทำการเคลียร์ค่าในฟอร์ม
  listNewTask.value.title = ""
  listNewTask.value.description = ""
  listNewTask.value.assignees = ""
  listNewTask.value.status = selected.value

  // ปิด Modal
  emits("closeAddModal")
}

const changeTitle = computed(() => {
  listNewTask.value.title?.length > 100
    ? (errorTask.value.title = "Title exceeds the limit of 100 characters.")
    : (errorTask.value.title = "")

  listNewTask.value.description?.length > 500
    ? (errorTask.value.description =
        "Description exceeds the limit of 500 characters.")
    : (errorTask.value.description = "")

  listNewTask.value.assignees?.length > 30
    ? (errorTask.value.assignees =
        "Assignees exceeds the limit of 30 characters.")
    : (errorTask.value.assignees = "")

  return !listNewTask.value.title
})
</script>

<template>
  <!-- Alert Pass Add-->
  <div v-show="addPass" class="flex justify-center mt-3">
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
      <span class="text-white">The task has been successfully added!!</span>
      <button class="text-white" @click="addPass = false">X</button>
    </div>
  </div>

  <!-- Modal window -->
  <div v-if="showAdd" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="grid grid-rows-6 grid-cols-4 gap-2 bg-white p-10 rounded-lg w-2/3"
      >
        <div class="flex items-center col-span-4">
          <input
            type="text"
            className="itbkk-title input pl-2 font-semibold text-3xl text-blue-400 rounded-lg w-11/12"
            v-model="listNewTask.title"
            placeholder="Enter Title here..."
          />
          <p
            class="text-gray-300 p-2 ml-2 whitespace-nowrap text-sm"
            :class="{ 'text-red-400': listNewTask.title?.length > 100 }"
          >
            {{ listNewTask.title?.length }} / 100
          </p>
        </div>

        <div
          class="border-2 border-blue-400 row-span-4 col-span-3 rounded-lg flex flex-col justify-between"
        >
          <p class="p-5 font-bold text-blue-400">Description</p>
          <textarea
            v-model="listNewTask.description"
            class="itbkk-description textarea textarea-ghost p-4 h-3/5 w-11/12 ml-9"
          ></textarea>
          <p
            class="text-gray-300 p-4 self-end text-sm"
            :class="{ 'text-red-400': listNewTask.description?.length > 500 }"
          >
            {{ listNewTask.description?.length }} / 500
          </p>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 row-start-2 row-end-4 rounded-lg flex flex-col justify-between"
        >
          <p class="p-3 font-bold text-blue-400">Assignees</p>
          <textarea
            v-model="listNewTask.assignees"
            class="itbkk-assignees pl-5 textarea textarea-ghost h-5/5 w-11/12 ml-2"
          ></textarea>
          <p
            class="text-gray-300 p-4 self-end text-sm"
            :class="{ 'text-red-400': listNewTask.assignees?.length > 30 }"
          >
            {{ listNewTask.assignees?.length }} / 30
          </p>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 p-2 row-start-4 row-end-5 rounded-lg"
        >
          <label for="status" class="p-2 font-bold text-blue-400">Status</label>
          <select
            v-model="listNewTask.status"
            class="itbkk-status pl-5 border-2 rounded-md h-10 pr-5"
          >
            <option value="NO_STATUS">No Status</option>
            <option value="TO_DO">To Do</option>
            <option value="DOING">Doing</option>
            <option value="DONE">Done</option>
          </select>
        </div>

        <div class="col-start-1 row-start-6 col-span-2">
          <p class="text-red-500">
            {{
              errorTask.title || errorTask.description || errorTask.assignees
            }}
          </p>
        </div>

        <div class="row-start-6 col-span-4 place-self-end rounded-lg">
          <button
            class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
            @click="saveNewTask"
            :disabled="changeTitle"
          >
            Save
          </button>
          <button class="itbkk-button-cancel btn" @click="closeAddModal">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
