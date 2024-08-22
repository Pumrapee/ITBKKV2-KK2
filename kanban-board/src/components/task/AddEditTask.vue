<script setup>
import { defineProps, ref, watch, computed, defineEmits } from "vue"
import { useStatusStore } from "../../stores/statusStore"
import { useLimitStore } from "../../stores/limitStore"
import { useTaskStore } from "../../stores/taskStore"
import router from "@/router"

const props = defineProps({
  showModal: Boolean,
  task: Object,
  editModeModal: Boolean,
  editDrop: Boolean,
})
const emits = defineEmits(["saveAddEdit", "closeModal"])
const myStatus = useStatusStore()
const myLimit = useLimitStore()
const myTask = useTaskStore()
const editMode = ref(props.editModeModal)
const newTask = ref({}) // Create a local copy of the task object
const errorTask = ref({
  title: "",
  description: "",
  assignees: "",
  status: "",
})

const addEditSave = (editTask) => {
  const editedTask = { ...editTask }
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

  editMode.value = false
  emits("saveAddEdit", editedTask)
}

const closeModal = () => {
  editMode.value = false
  emits("closeModal")
}

// สถานะของโหมดแก้ไข
const enableEditMode = () => {
  editMode.value = true
  if (editMode.value === true) {
    router.push({ name: "editTask" })
  } else {
    router.go(-1)
  }
}

//disable ปุ่ม save
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
  const newStatus = newTask.value.status // รับค่า newStatus จากการเลือกของผู้ใช้

  // ตรวจสอบความยาวของ title, description, และ assignees
  const isTitleTooLong = newTitle?.length > 100
  const isTitleEmpty = newTitle === null
  const isDescriptionTooLong = newDescription?.length > 500
  const isAssigneesTooLong = newAssignees?.length > 30

  errorTask.value.title = isTitleTooLong
    ? "Title exceeds the limit of 100 characters."
    : isTitleEmpty
    ? "Title is required."
    : ""
  errorTask.value.description = isDescriptionTooLong
    ? "Description exceeds the limit of 500 characters."
    : ""
  errorTask.value.assignees = isAssigneesTooLong
    ? "Assignees exceeds the limit of 30 characters."
    : ""

  const isUnchanged =
    oldTask.title === newTitle &&
    oldTask.description === newDescription &&
    oldTask.assignees === newAssignees &&
    oldTask.status === newStatus

  if (myLimit.getLimit().taskLimitEnabled === true) {
    const statusCount = myTask.matchStatus(newStatus).length
    const statusLimit = myLimit.getLimit().maxTasksPerStatus

    // เงื่อนไขพิเศษสำหรับสถานะ "No Status" และ "Done"
    if (newStatus !== "No Status" && newStatus !== "Done") {
      if (statusCount === statusLimit) {
        // กรณีที่จำนวนเท่ากับ limit, ตรวจสอบว่าเป็นการเปลี่ยนสถานะหรือไม่
        if (props.task.status !== newStatus) {
          errorTask.value.status = `The status "${newStatus}" will have too many tasks. Please make progress and update status of existing tasks first.`
        } else {
          errorTask.value.status = "" // ถ้าเป็นสถานะเดิมไม่แสดงข้อความเตือน
        }
      } else if (statusCount > statusLimit) {
        // ถ้าจำนวนงานเกิน limit
        errorTask.value.status = `The status "${newStatus}" will have too many tasks. Please make progress and update status of existing tasks first.`
      } else {
        errorTask.value.status = "" // กรณีอื่น ๆ ไม่มีการเตือน
      }
    } else {
      errorTask.value.status = "" // ไม่แสดงข้อความเตือนสำหรับ "No Status" และ "Done"
    }
  } else {
    errorTask.value.status = ""
  }

  const isStatusExceeded = errorTask.value.status !== ""

  // ตรวจสอบเงื่อนไขทั้งหมดรวมถึงการเปลี่ยนแปลงของข้อมูล
  return (
    isTitleTooLong ||
    isDescriptionTooLong ||
    isAssigneesTooLong ||
    isTitleEmpty ||
    newTitle === null ||
    isUnchanged ||
    isStatusExceeded
  )
})

watch(props, () => {
  if (props.showModal) {
    Object.assign(newTask.value, props.task)
  }
  if (props.editModeModal) {
    editMode.value = props.editModeModal
  }
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-6 rounded-lg w-11/12 max-w-3xl">
        <div class="flex justify-between items-center mb-4 border-b-2">
          <h2 class="text-2xl font-bold text-blue-400 mb-2">
            {{
              task?.id === undefined
                ? "Add Task"
                : editMode
                ? "Edit Task"
                : "Task"
            }}
          </h2>
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
            v-model="newTask.title"
            :readonly="!editMode"
            placeholder="Enter Title here..."
            class="itbkk-title w-full border border-blue-400 rounded-lg py-2 px-3 input input-ghost"
          />
          <div v-if="editMode" class="flex justify-between">
            <p class="text-red-400">
              {{ errorTask.title }}
            </p>
            <p
              class="text-gray-300 whitespace-nowrap text-sm text-end mt-1"
              :class="{
                'text-red-400':
                  newTask.title?.trim()?.length > 100 ||
                  newTask.title?.trim()?.length === 0,
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
              :readonly="!editMode"
              v-model="newTask.description"
              class="itbkk-description w-full border border-blue-400 rounded-lg py-3 px-3 h-72 textarea textarea-ghost"
              :class="
                newTask.description
                  ? 'bg-white text-black'
                  : 'italic text-gray-500'
              "
              placeholder="No Description Provided"
            ></textarea>
            <div v-if="editMode" class="flex justify-between">
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
                :readonly="!editMode"
                v-model="newTask.assignees"
                class="itbkk-assignees w-full border border-blue-400 rounded-lg py-3 px-3 h-42 textarea textarea-ghost"
                :class="
                  newTask.assignees
                    ? 'bg-white text-black'
                    : 'italic text-gray-500'
                "
                placeholder="Unassigned"
              ></textarea>
              <div v-if="editMode" class="flex justify-between">
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
                :disabled="!editMode"
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
            <p class="text-red-400">
              {{ errorTask.status }}
            </p>

            <div v-if="task?.id" class="mt-5">
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
            <div v-else class="mt-5"></div>
          </div>
        </div>

        <div class="flex justify-end mt-4">
          <div>
            <button
              v-show="!editMode"
              @click="enableEditMode"
              class="btn btn-info h-2 mr-3"
            >
              Edit
            </button>
            <button
              v-show="editMode"
              class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
              @click="addEditSave(newTask)"
              :disabled="changeTask"
            >
              Save
            </button>
            <button class="itbkk-button-cancel btn" @click="closeModal">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
