<script setup>
import { defineProps, defineEmits, ref, computed, onMounted } from "vue"
import { addItem, getItems } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"

const { showAdd } = defineProps({
  showAdd: Boolean,
})

const myStatus = useStatusStore()

const emits = defineEmits(["closeAddModal", "closeCancle"])

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
    emits("closeAddModal", statusCode)
  }
}

const cancleModal = () => {
  // ทำการเคลียร์ค่าในฟอร์ม
  listNewTask.value.title = ""
  listNewTask.value.description = ""
  listNewTask.value.assignees = ""
  listNewTask.value.status = selected.value

  // ปิด Modal
  emits("closeCancle")
}

const changeTitle = computed(() => {
  const trimmedTitleLength = listNewTask.value.title.trim().length

  // ตรวจสอบว่า title มีความยาวมากกว่า 100 หรือมีช่องว่างหรือไม่
  if (trimmedTitleLength > 100 || trimmedTitleLength === 0) {
    errorTask.value.title =
      trimmedTitleLength === 0
        ? "Title is required."
        : "Title exceeds the limit of 100 characters."
    return true // ให้มีค่าเป็น true เมื่อ title มีความยาวเกิน 100 หรือมีช่องว่าง
  } else {
    // กรณีที่ไม่เข้าเงื่อนไขด้านบน ให้เคลียร์ข้อผิดพลาดของ title
    errorTask.value.title = ""
  }

  listNewTask.value.description?.trim()?.length > 500
    ? (errorTask.value.description =
        "Description exceeds the limit of 500 characters.")
    : (errorTask.value.description = "")

  listNewTask.value.assignees?.trim()?.length > 30
    ? (errorTask.value.assignees =
        "Assignees exceeds the limit of 30 characters.")
    : (errorTask.value.assignees = "")

  return !listNewTask.value.title
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showAdd" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-6 rounded-lg w-11/12 max-w-3xl">
        <h2 class="text-2xl font-bold text-blue-400 mb-4 border-b-2">
          Add Task
        </h2>
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
          <p
            class="text-gray-300 whitespace-nowrap text-sm text-end mt-1"
            :class="{
              'text-red-400':
                listNewTask.title?.length > 100 ||
                listNewTask.title?.length === 0,
            }"
          >
            {{ listNewTask.title?.trim()?.length }} / 100
          </p>
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
            <p
              class="text-gray-300 text-sm text-end"
              :class="{
                'text-red-400': listNewTask.description?.trim()?.length > 500,
              }"
            >
              {{ listNewTask.description?.trim()?.length }} / 500
            </p>
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
              <p
                class="text-gray-300 text-sm text-end"
                :class="{
                  'text-red-400': listNewTask.assignees?.trim()?.length > 30,
                }"
              >
                {{ listNewTask.assignees?.trim()?.length }} / 30
              </p>
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
        <div class="flex justify-between mt-4">
          <div>
            <p class="text-red-500">
              {{
                errorTask.title || errorTask.description || errorTask.assignees
              }}
            </p>
          </div>
          <div>
            <button
              class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
              @click="saveNewTask"
              :disabled="changeTitle"
            >
              Save
            </button>
            <button class="itbkk-button-cancle btn" @click="cancleModal">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
