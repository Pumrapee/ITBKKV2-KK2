<script setup>
import { defineProps, ref, watch, computed, defineEmits, onMounted } from "vue"
import { useStatusStore } from "../../stores/statusStore"
import { useLimitStore } from "../../stores/limitStore"
import { useTaskStore } from "../../stores/taskStore"
import { previewBinaryFile } from "../../libs/previewBinary"
import { useRoute } from "vue-router"
import { getItems, downloadAttachment } from "@/libs/fetchUtils"

const props = defineProps({
  showModal: Boolean,
  task: Object,
  getAttactment: Array,
  ownerBoard: String,
  editModeModal: Boolean,
  editDrop: Boolean,
})
const emits = defineEmits(["saveAddEdit", "closeModal"])
const myStatus = useStatusStore()
const myLimit = useLimitStore()
const myTask = useTaskStore()
const editMode = ref(props.editModeModal)
const newTask = ref({}) // Create a local copy of the task object
const route = useRoute()

const boardId = ref(route.params.id)
const taskId = ref()
const errorTask = ref({
  title: "",
  description: "",
  assignees: "",
  status: "",
})

// onMounted(() => {
//   if (Array.isArray(props.getAttactment) && props.getAttactment.length > 0) {
//     previewAdded(props.getAttactment)
//   }
// })

// Files
const uploadedFileNames = ref([])
const previewDocURLs = ref([])
const previewImageURLs = ref([])
const uploadedFiles = ref([])
const attachmentFile = ref(props.getAttactment)

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

  //เพิ่มเพราะเหมือนจะไปติดอะไรสักอย่างตอน limit
  setTimeout(() => {
    editMode.value = false
  }, 500)
  emits("saveAddEdit", editedTask, uploadedFiles.value)
}

const closeModal = () => {
  editMode.value = false
  // Reset arrays
  previewDocURLs.value = []
  previewImageURLs.value = []
  uploadedFileNames.value = []
  emits("closeModal")
}

// สถานะของโหมดแก้ไข
const enableEditMode = () => {
  editMode.value = true
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
    taskId.value = newTask.value.id
    console.log(newTask.value)
  }
  if (props.editModeModal) {
    editMode.value = props.editModeModal
  }
})
const canEdit = computed(() => {
  const userName = localStorage.getItem("user")
  return userName === props.ownerBoard
})

const preview = (event) => {
  const files = event.target.files
  console.log(files)

  Array.from(files).forEach(async (file) => {
    uploadedFiles.value.push(file)

    // Push file name to the list
    if (!uploadedFileNames.value.includes(file.name)) {
      uploadedFileNames.value.push(file.name)

      // Check file type and generate preview
      if (file.name.endsWith(".pdf")) {
        previewDocURLs.value.push(previewBinaryFile(file)) // Generate document URL
        console.log(previewDocURLs.value)
      } else {
        previewImageURLs.value.push(previewBinaryFile(file)) // Generate image preview URL
        previewDocURLs.value.push(previewBinaryFile(file)) // Generate document URL
        console.log(previewImageURLs.value)
      }
    }
  })
}

const removeFile = (index) => {
  uploadedFileNames.value.splice(index, 1) // Remove file name
  previewDocURLs.value.splice(index, 1) // Remove document preview (if exists)
  previewImageURLs.value.splice(index, 1) // Remove image preview (if exists)
}

const previewAdded = () => {
  attachmentFile.value.forEach(async (attachment) => {
    const previewUrl = await downloadAttachment(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks/${
        taskId.value
      }/attachments/${attachment.id}/download`
    )

    console.log(previewUrl)
    uploadedFileNames.value.push(attachment.filename)

    if (
      attachment.filename.endsWith(".png") ||
      attachment.filename.endsWith(".jpg") ||
      attachment.filename.endsWith(".jpeg") ||
      attachment.filename.endsWith(".gif")
    ) {
      previewImageURLs.value.push(previewUrl)
      previewDocURLs.value.push(previewUrl)

      console.log(previewImageURLs.value)
    } else if (attachment.filename.endsWith(".pdf")) {
      previewDocURLs.value.push(previewUrl)
      console.log(previewDocURLs.value)
    }
  })
}

watch(
  () => props.getAttactment,
  (newValue) => {
    if (Array.isArray(newValue)) {
      attachmentFile.value = newValue
      taskId.value = route.params.taskId
      previewAdded(newValue)
    }
  },
  { immediate: true } // Run immediately on mount
)
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div
      class="flex items-center justify-center min-h-screen bg-black/[.15] px-4"
    >
      <div
        class="itbkk-modal-task bg-white p-4 sm:p-6 rounded-lg w-full sm:w-11/12 max-w-3xl"
      >
        <div
          class="flex flex-col sm:flex-row justify-between items-center mb-4 border-b-2 pb-2"
        >
          <h2 class="text-xl sm:text-2xl font-bold text-black mb-2 sm:mb-0">
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
          <label for="title" class="block text-black font-bold mb-2"
            >Title</label
          >
          <input
            type="text"
            id="title"
            v-model="newTask.title"
            :readonly="!editMode"
            placeholder="Enter Title here..."
            class="itbkk-title w-full border border-black rounded-lg py-2 px-3 input input-ghost"
          />
          <div v-if="editMode" class="flex justify-between mt-1">
            <p class="text-red-400">{{ errorTask.title }}</p>
            <p
              class="text-gray-300 whitespace-nowrap text-sm text-end"
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

        <div class="flex flex-col sm:flex-row gap-4">
          <div class="w-full sm:w-2/3">
            <label for="description" class="block text-black font-bold mb-2"
              >Description</label
            >
            <textarea
              id="description"
              :readonly="!editMode"
              v-model="newTask.description"
              class="itbkk-description w-full border border-black rounded-lg py-3 px-3 h-40 sm:h-60 textarea textarea-ghost scrollbar-hidden"
              :class="
                newTask.description
                  ? 'bg-white text-black'
                  : 'italic text-gray-500'
              "
              placeholder="No Description Provided"
            >
            {{
                newTask.description
                  ? newTask.description
                  : "No Description Provided"
              }}
          </textarea
            >
            <div v-if="editMode" class="flex justify-between mt-1">
              <p class="text-red-400">{{ errorTask.description }}</p>
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

          <div class="w-full sm:w-1/3">
            <div class="mb-4">
              <label for="assignees" class="block text-black font-bold mb-2"
                >Assignees</label
              >
              <textarea
                id="assignees"
                :readonly="!editMode"
                v-model="newTask.assignees"
                class="itbkk-assignees w-full border border-black rounded-lg py-2 px-3 h-14 textarea textarea-ghost"
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

            <div class="mb-4">
              <label for="status" class="block text-black font-bold"
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

            <p v-if="editMode" class="text-red-400">{{ errorTask.status }}</p>

            <div v-if="task?.id">
              <p class="itbkk-timezone pl-3 font-semibold text-sm text-black">
                Time Zone :
                {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
              </p>
              <p class="itbkk-created-on pl-3 font-semibold text-sm text-black">
                Created On :
                {{ new Date(task.createdOn).toLocaleString("en-GB") }}
              </p>
              <p class="itbkk-updated-on pl-3 font-semibold text-sm text-black">
                Updated On :
                {{ new Date(task.updatedOn).toLocaleString("en-GB") }}
              </p>
            </div>
            <div v-else class="mt-5"></div>
          </div>
        </div>

        <div class="mb-6" v-if="task?.id">
          <label class="block font-bold mb-1">Attachments</label>
          <input
            type="file"
            accept="image/*,.pdf,.txt,.doc,.docx,.xlsx,.csv,.mp4,.avi"
            multiple
            :disabled="!editMode"
            class="file-input file-input-bordered file-input-sm w-full max-w-xs"
            @change="preview"
          />

          <div
            v-if="uploadedFileNames.length > 0"
            class="flex space-x-4 overflow-x-auto mt-4 p-2 border rounded-md"
          >
            <div
              v-for="(name, index) in uploadedFileNames"
              :key="index"
              class="relative flex flex-col items-center min-w-[120px] space-y-2"
            >
              <!-- Image Preview -->
              <div class="relative w-16 h-16">
                <img
                  v-if="previewImageURLs[index]"
                  :src="previewImageURLs[index]"
                  :alt="name"
                  class="w-full h-full rounded-md border border-gray-300 object-cover"
                />
                <!-- Delete Button -->
                <button
                  v-if="editMode"
                  class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center hover:bg-red-600"
                  @click="removeFile(index)"
                >
                  <img src="/icons/delete.png" class="w-3 h-3" />
                </button>
              </div>

              <!-- Document Preview -->
              <a
                v-if="previewDocURLs[index]"
                :href="previewDocURLs[index]"
                target="_blank"
                class="italic hover:text-blue-400 text-xs text-center block"
              >
                <p class="break-words w-24">{{ name }}</p>
              </a>
            </div>
          </div>
        </div>

        <div class="flex justify-end mt-4">
          <div v-if="canEdit">
            <router-link
              v-if="task?.id"
              :to="{
                name: 'editTask',
                params: { taskId: task.id },
              }"
            >
              <button
                v-if="!editMode"
                @click="enableEditMode"
                class="btn bg-blue-400 text-white mr-2"
              >
                Edit
              </button>
            </router-link>
          </div>

          <button
            v-show="editMode"
            class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200 mb-2"
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
</template>

<style scoped>
.scrollbar-hidden {
  overflow-x: auto;
  scrollbar-width: none; /* For Firefox */
}

.scrollbar-hidden::-webkit-scrollbar {
  display: none; /* For Chrome, Safari, and Opera */
}
</style>
