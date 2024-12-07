<script setup>
import { defineProps, ref, watch, computed, defineEmits, onMounted } from "vue"
import { useStatusStore } from "../../stores/statusStore"
import { useLimitStore } from "../../stores/limitStore"
import { useTaskStore } from "../../stores/taskStore"
import { previewBinaryFile } from "../../libs/previewBinary"
import { useRoute } from "vue-router"
import { downloadAttachment, getAttachment } from "@/libs/fetchUtils"
import previewFile from "./previewFile.vue"

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
  attachment: "",
})

// Files
const uploadedFilesData = ref([])
const attachmentFile = ref(props.getAttactment)
const selectedFile = ref(null) // Selected file for the preview modal
const deleteFiles = ref([])
const MAX_FILES = 10
const MAX_FILE_SIZE = 20 * 1024 * 1024

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
    uploadedFilesData.value = []
    deleteFiles.value = []
    errorTask.value.attachment = ""
  }, 1500)

  console.log(uploadedFilesData.value)

  emits("saveAddEdit", editedTask, uploadedFilesData.value, deleteFiles.value)
}

const closeModal = () => {
  editMode.value = false
  // Reset arrays
  uploadedFilesData.value = []
  deleteFiles.value = []
  errorTask.value.attachment = ""
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
    attachments:
      props.getAttactment?.map((attachment) => attachment.filename) || [],
  }

  const newTitle = trimAndCheckNull(newTask.value.title)
  const newDescription = trimAndCheckNull(newTask.value.description)
  const newAssignees = trimAndCheckNull(newTask.value.assignees)
  const newStatus = newTask.value.status
  const newAttachments = uploadedFilesData.value.map((file) => file.filename)

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

  const isAttachmentsUnchanged =
    oldTask.attachments.length === newAttachments.length &&
    oldTask.attachments.every((filename) => newAttachments.includes(filename))

  const isUnchanged =
    oldTask.title === newTitle &&
    oldTask.description === newDescription &&
    oldTask.assignees === newAssignees &&
    oldTask.status === newStatus &&
    isAttachmentsUnchanged

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
  errorTask.value.attachment = ""

  const notAddedFiles = {
    exceededMaxFiles: [],
    exceededMaxSize: [],
    duplicateFilenames: [],
  }
  console.log(files)

  Array.from(files).forEach(async (file, index) => {
    // ไฟล์ที่ชื่อซ้ำ
    if (uploadedFilesData.value.some((f) => f.filename === file.name)) {
      notAddedFiles.duplicateFilenames.push(file.name)
      return
    }

    // ไฟล์ที่ขนาดเกิน
    if (file.size > MAX_FILE_SIZE) {
      notAddedFiles.exceededMaxSize.push(file.name)
      return
    }

    // เกินจำนวนไฟล์ที่กำหนด
    if (uploadedFilesData.value.length >= MAX_FILES) {
      notAddedFiles.exceededMaxFiles.push(file.name)
      return
    }

    console.log(file)
    const previewData = {
      filename: file.name,
      files: file,
      type: "document",
      url: "",
      content: null,
    }
    if (
      file.name.endsWith(".png") ||
      file.name.endsWith(".jpg") ||
      file.name.endsWith(".jpeg") ||
      file.name.endsWith(".gif")
    ) {
      previewData.type = "media"
      previewData.url = previewBinaryFile(file)
    } else if (file.name.endsWith(".mp4") || file.name.endsWith(".avi")) {
      previewData.type = "video"
      previewData.url = previewBinaryFile(file)
    } else if (file.name.endsWith(".pdf")) {
      previewData.type = "PDF"
      previewData.url = previewBinaryFile(file)
    } else if (file.name.endsWith(".txt")) {
      const reader = new FileReader()
      reader.onload = (event) => {
        previewData.type = "txt"
        previewData.content = event.target.result || "This file is empty."
        uploadedFilesData.value = [...uploadedFilesData.value, previewData]
      }
      reader.readAsText(file)
      return
    } else {
      previewData.type = "document"
      previewData.url = previewBinaryFile(file)
    }

    console.log(previewData)
    uploadedFilesData.value.push(previewData)
  })

  // ตรวจสอบและอัปเดตข้อความแจ้งเตือน
  if (notAddedFiles.exceededMaxFiles.length > 0) {
    errorTask.value.attachment = `- Each task can have at most ${MAX_FILES} files.<br>`
  }

  if (notAddedFiles.exceededMaxSize.length > 0) {
    errorTask.value.attachment += `- Each file cannot be larger than ${
      MAX_FILE_SIZE / (1024 * 1024)
    } MB.<br>`
  }

  if (notAddedFiles.duplicateFilenames.length > 0) {
    errorTask.value.attachment += `- File with the same filename cannot be added or updated to the attachments. Please delete the attachment and add again to update the file.<br>`
  }

  if (
    notAddedFiles.exceededMaxFiles.length > 0 ||
    notAddedFiles.exceededMaxSize.length > 0
  ) {
    errorTask.value.attachment += `The following files are not added: ${[
      ...notAddedFiles.exceededMaxFiles,
      ...notAddedFiles.exceededMaxSize,
      ...notAddedFiles.duplicateFilenames, //ยังไม่แน่ใจอันนี้ต้องรวมด้วยไหม
    ].join(", ")}`
  }
}

const removeFile = async (index) => {
  const fileToRemove = uploadedFilesData.value[index].filename
  const attachment = attachmentFile.value
    ? attachmentFile.value.find((file) => file.filename === fileToRemove)
    : null

  const attachmentId = attachment?.id

  if (!attachmentId) {
    // ลบไฟล์ที่เพิ่มใหม่ (local files)
    uploadedFilesData.value.splice(index, 1)
    return
  }

  if (attachmentId) {
    // ลบไฟล์ที่ BE
    uploadedFilesData.value.splice(index, 1)
    deleteFiles.value.push(attachmentId)
  }
}

const previewAdded = async () => {
  console.log(attachmentFile.value)

  for (const attachment of attachmentFile.value) {
    const previewUrl = await downloadAttachment(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks/${
        taskId.value
      }/attachments/${attachment.id}/download`
    )
    console.log(previewUrl)

    const thumbnails = await downloadAttachment(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks/${
        taskId.value
      }/attachments/${attachment.id}/thumbnail`
    )
    console.log(thumbnails)

    const previewData = {
      filename: attachment.filename,
      files: null,
      type: "document",
      url: thumbnails,
      download: previewUrl,
      content: null,
    }

    if (
      attachment.filename.endsWith(".png") ||
      attachment.filename.endsWith(".jpg") ||
      attachment.filename.endsWith(".jpeg") ||
      attachment.filename.endsWith(".gif")
    ) {
      previewData.type = "media"
    } else if (
      attachment.filename.endsWith(".mp4") ||
      attachment.filename.endsWith(".avi")
    ) {
      previewData.type = "video"
    } else if (attachment.filename.endsWith(".pdf")) {
      previewData.type = "PDF"
    } else if (attachment.filename.endsWith(".txt")) {
      try {
        const response = await fetch(previewUrl)
        if (response.ok) {
          const blob = await response.blob()

          const textContent = await new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.onload = (event) => resolve(event.target.result)
            reader.onerror = (error) => reject(error)
            reader.readAsText(blob)
          })

          previewData.type = "txt"
          previewData.content = textContent
        }
      } catch (error) {
        console.error("Error reading .txt file:", error)
      }
    } else {
      previewData.type = "document"
    }

    uploadedFilesData.value.push(previewData)
  }
}

const openPreview = (file) => {
  selectedFile.value = {
    filename: file.filename,
    type: file.type,
    content: file.content || null,
    url: file.download || file.url, 
  }
}

const closePreview = () => {
  selectedFile.value = null
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
  { immediate: true }
)
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div
      class="flex items-center justify-center min-h-screen bg-black/[.15] px-4"
    >
      <div
        class="itbkk-modal-task bg-white p-4 sm:p-6 rounded-lg w-full sm:w-11/12 max-w-3xl scrollbar-hidden"
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
          <div class="flex">
            <input
              type="file"
              accept="image/*,.pdf,.txt,.doc,.docx,.xlsx,.csv,.mp4,.avi"
              multiple
              :disabled="!editMode || uploadedFilesData.length === 10"
              class="file-input file-input-bordered file-input-sm w-full max-w-xs"
              @change="preview"
            />
            <p
              v-html="errorTask.attachment"
              class="text-red-400 text-sm w-full pl-2 pt-1"
            ></p>
          </div>

          <!-- Preview thumnail -->
          <div
            v-if="uploadedFilesData.length > 0"
            class="flex space-x-4 overflow-x-auto mt-4 p-2 border rounded-md"
          >
            <div
              v-for="(file, index) in uploadedFilesData"
              :key="index"
              class="relative flex flex-col items-center min-w-[120px] space-y-2"
            >
              <!-- Image Preview -->
              <div class="relative w-20 h-20">
                <img
                  v-if="file.type === 'media'"
                  :src="file.url"
                  :alt="file.filename"
                  class="w-full h-full rounded-md border border-gray-300 object-cover"
                  @click="openPreview(file)"
                />
                <video
                  v-if="file.type === 'video'"
                  :src="file.url"
                  class="w-full h-full rounded-md border border-gray-300 object-cover"
                  @click="openPreview(file)"
                ></video>

                <div
                  v-if="file.type === 'txt'"
                  class="w-20 h-20 flex items-center justify-center border rounded-md cursor-pointer"
                  @click="openPreview(file)"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 64 64"
                    fill="none"
                    class="w-14 h-14"
                  >
                    <!-- Background Rectangle -->
                    <rect width="64" height="64" rx="8" fill="#E6EAF0" />

                    <!-- Document Icon -->
                    <path
                      d="M20 16C20 14.8954 20.8954 14 22 14H34L42 22V48C42 49.1046 41.1046 50 40 50H22C20.8954 50 20 49.1046 20 48V16Z"
                      fill="white"
                    />
                    <path
                      d="M34 14V22H42"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <text
                      x="32"
                      y="36"
                      text-anchor="middle"
                      fill="#A0AEC0"
                      font-size="10"
                      font-family="Arial, sans-serif"
                    >
                      TXT
                    </text>
                  </svg>
                </div>

                <div
                  v-if="file.type === 'PDF'"
                  class="w-20 h-20 flex items-center justify-center border rounded-md"
                  @click="openPreview(file)"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 64 64"
                    fill="none"
                    class="w-14 h-14"
                  >
                    <!-- Background Rectangle -->
                    <rect width="64" height="64" rx="8" fill="#E6EAF0" />

                    <!-- Document Icon -->
                    <path
                      d="M20 16C20 14.8954 20.8954 14 22 14H34L42 22V48C42 49.1046 41.1046 50 40 50H22C20.8954 50 20 49.1046 20 48V16Z"
                      fill="white"
                    />
                    <path
                      d="M34 14V22H42"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <text
                      x="32"
                      y="36"
                      text-anchor="middle"
                      fill="#A0AEC0"
                      font-size="10"
                      font-family="Arial, sans-serif"
                    >
                      PDF
                    </text>
                  </svg>
                </div>

                <!-- Document Download -->
                <a
                  v-if="file.type === 'document'"
                  :href="file.url"
                  :download="file.filename"
                  class="w-20 h-20 flex items-center justify-center border rounded-md"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 64 64"
                    fill="none"
                    class="w-14 h-14"
                  >
                    <!-- Background Rectangle -->
                    <rect width="64" height="64" rx="8" fill="#E6EAF0" />

                    <!-- Document Shape -->
                    <path
                      d="M20 16C20 14.8954 20.8954 14 22 14H34L42 22V48C42 49.1046 41.1046 50 40 50H22C20.8954 50 20 49.1046 20 48V16Z"
                      fill="white"
                    />
                    <path
                      d="M34 14V22H42"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />

                    <!-- Lines in the document -->
                    <line
                      x1="25"
                      y1="30"
                      x2="37"
                      y2="30"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                    />
                    <line
                      x1="25"
                      y1="36"
                      x2="37"
                      y2="36"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                    />
                    <line
                      x1="25"
                      y1="42"
                      x2="33"
                      y2="42"
                      stroke="#A0AEC0"
                      stroke-width="2"
                      stroke-linecap="round"
                    />
                  </svg>
                </a>
                <!-- Delete Button -->
                <button
                  v-if="editMode"
                  class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center hover:bg-red-600"
                  @click="removeFile(index, name)"
                >
                  <img src="/icons/delete.png" class="w-3 h-3" />
                </button>

                <!-- Download Button-->
                <a
                  v-if="!editMode"
                  class="absolute top-1 right-1 text-white rounded-full w-5 h-5 flex items-center justify-center"
                  :href="file.url"
                  :download="file.filename"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 64 64"
                    fill="none"
                    class="w-8 h-8"
                  >
                    <!-- Background Circle -->
                    <circle cx="32" cy="32" r="30" fill="#f0f4ff" />
                    <!-- Arrow -->
                    <path
                      d="M32 16v20"
                      stroke="#4f46e5"
                      stroke-width="3"
                      stroke-linecap="round"
                    />
                    <path
                      d="M24 32l8 8 8-8"
                      stroke="#4f46e5"
                      stroke-width="3"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <!-- Download Bar -->
                    <line
                      x1="20"
                      y1="46"
                      x2="44"
                      y2="46"
                      stroke="#4f46e5"
                      stroke-width="3"
                      stroke-linecap="round"
                    />
                  </svg>
                </a>
              </div>

              <a target="_blank" class="italic text-xs text-center block">
                <p class="break-words w-24">{{ file.filename }}</p>
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

  <previewFile :filePre="selectedFile" @closePreview="closePreview" />
</template>

<style scoped>
.itbkk-modal-task {
  max-height: 90vh; /* Limit height of modal */
  overflow-y: auto; /* Enable vertical scrolling */
}

.scrollbar-hidden {
  overflow-x: auto;
  scrollbar-width: none; /* For Firefox */
}

.scrollbar-hidden::-webkit-scrollbar {
  display: none; /* For Chrome, Safari, and Opera */
}

.text-preview {
  white-space: pre-wrap; /* Preserve line breaks */
  font-family: monospace; /* ใช้ฟอนต์ที่เหมาะสำหรับแสดงข้อความ */
  padding: 1rem; /* เพิ่มระยะห่างภายใน */
  border: 1px solid #ddd; /* เส้นขอบ */
  background: #f9f9f9; /* สีพื้นหลัง */
  max-height: 85vh; /* ขนาดสูงสุดที่ 85% ของความสูงหน้าจอ */
  max-width: 90vw; /* ขนาดกว้างสุดที่ 90% ของความกว้างหน้าจอ */
  overflow: auto; /* แสดง scroll bar หากเนื้อหาเกินขอบเขต */
  width: auto; /* ปรับขนาดตามเนื้อหา */
  height: auto; /* ปรับขนาดตามเนื้อหา */
  box-sizing: border-box; /* รวม padding และ border ในขนาด */
}

.pdf-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.fixed {
  max-height: 100vh; /* Prevent the modal from exceeding viewport height */
}
</style>
