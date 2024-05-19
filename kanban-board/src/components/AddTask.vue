<script setup>
import { defineProps, defineEmits, ref, computed, onMounted } from "vue"
import { addItem, getItems } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { useLimitStore } from "../stores/limitStore"

const { showAdd } = defineProps({
  showAdd: Boolean,
})
const myLimit = useLimitStore()
const myStatus = useStatusStore()

const emits = defineEmits(["closeAddModal", "closeCancel"])

const selected = ref()
selected.value = "No Status"

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
  listNewTask.value.title = listNewTask.value.title?.trim()
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
    `${import.meta.env.VITE_API_URL}tasks`,
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
    emits("closeAddModal", statusCode)
  }

  if (statusCode === 400) {
    const listStatus = await getItems(`${import.meta.env.VITE_API_URL}statuses`)
    myStatus.clearStatus()
    myStatus.addStatus(listStatus)

    listNewTask.value.title = ""
    listNewTask.value.description = ""
    listNewTask.value.assignees = ""
    listNewTask.value.status = selected.value

    emits("closeAddModal", statusCode)
  }

  if (statusCode === 507) {
    emits("closeAddModal", statusCode, listNewTask.value.status)
  }
}

const cancelModal = () => {
  // ทำการเคลียร์ค่าในฟอร์ม
  listNewTask.value.title = ""
  listNewTask.value.description = ""
  listNewTask.value.assignees = ""
  listNewTask.value.status = selected.value

  // ปิด Modal
  emits("closeCancel")
}

const changeTitle = computed(() => {
  const trimmedTitleLength = listNewTask.value.title?.trim()?.length > 100
  const trimmedTitleEmpthy = listNewTask.value.title?.trim()?.length === 0
  const trimmedDescriptionLength =
    listNewTask.value.description?.trim()?.length > 500
  const trimmedAssigneesLength =
    listNewTask.value.assignees?.trim()?.length > 30

  // ตรวจสอบว่า title มีความยาวมากกว่า 100 หรือมีช่องว่างหรือไม่
  if (trimmedTitleLength) {
    errorTask.value.title = "Title exceeds the limit of 100 characters."
  } else if (trimmedTitleEmpthy) {
    errorTask.value.title = "Title is required."
  } else {
    errorTask.value.title = ""
  }

  if (trimmedDescriptionLength) {
    errorTask.value.description =
      "Description exceeds the limit of 500 characters."
  } else {
    errorTask.value.description = ""
  }

  if (trimmedAssigneesLength) {
    errorTask.value.assignees = "Assignees exceeds the limit of 30 characters."
  } else {
    errorTask.value.assignees = ""
  }

  // หากมีเงื่อนไขใดเป็นจริง ให้คืนค่าเป็นจริงเพื่อระบุว่าควรปิดใช้งานปุ่มนั้น
  return (
    trimmedTitleLength ||
    trimmedDescriptionLength ||
    trimmedAssigneesLength ||
    trimmedTitleEmpthy
  )
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showAdd" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-6 rounded-lg w-11/12 max-w-3xl">
        <div class="flex justify-between items-center mb-4 border-b-2">
          <h2 class="text-2xl font-bold text-blue-400 mb-2">Add Task</h2>
          <div class="mb-2">
            <span
              v-if="myLimit.getLimit().taskLimitEnabled === true"
              class="text-green-500 font-light text-sm border border-green-500 rounded px-2 py-1"
              >The Limit Enable state</span
            >
            <span
              v-else
              class="text-red-500 font-light text-sm border border-red-500 rounded px-2 py-1"
              >The Limit Disable state</span
            >
          </div>
        </div>

        <div class="mb-4">
          <label for="title" class="block text-blue-400 font-bold mb-2"
            >Title</label
          >
          <input
            type="text"
            id="title"
            v-model="listNewTask.title"
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
                  listNewTask.title?.trim()?.length > 100 ||
                  listNewTask.title?.trim()?.length === 0,
              }"
            >
              {{ listNewTask.title?.trim()?.length || 0 }} / 100
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
              v-model="listNewTask.description"
              class="itbkk-description w-full border border-blue-400 rounded-lg py-3 px-3 h-72 textarea textarea-ghost"
            ></textarea>
            <div class="flex justify-between">
              <p class="text-red-400">
                {{ errorTask.description }}
              </p>
              <p
                class="text-gray-300 text-sm text-end"
                :class="{
                  'text-red-400': listNewTask.description?.trim()?.length > 500,
                }"
              >
                {{ listNewTask.description?.trim()?.length || 0 }} / 500
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
                v-model="listNewTask.assignees"
                class="itbkk-assignees w-full border border-blue-400 rounded-lg py-3 px-3 h-42 textarea textarea-ghost"
              ></textarea>
              <div class="flex justify-between">
                <p class="text-red-400 text-sm w-40">
                  {{ errorTask.assignees }}
                </p>
                <p
                  class="text-gray-300 text-sm text-end"
                  :class="{
                    'text-red-400': listNewTask.assignees?.trim()?.length > 30,
                  }"
                >
                  {{ listNewTask.assignees?.trim()?.length || 0 }} / 30
                </p>
              </div>
            </div>
            <div>
              <label for="status" class="block text-blue-400 font-bold mb-2"
                >Status</label
              >
              <select
                v-model="listNewTask.status"
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
            <div></div>
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <div>
            <button
              class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
              @click="saveNewTask"
              :disabled="changeTitle"
            >
              Save
            </button>
            <button class="itbkk-button-cancel btn" @click="cancelModal">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
