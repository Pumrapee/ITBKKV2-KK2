<script setup>
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { useLimitStore } from "@/stores/limitStore"
import {
  getItemById,
  editItem,
  addItem,
  deleteItemById,
  getItems,
  getStatusLimits,
} from "@/libs/fetchUtils"
import { defineEmits, computed, ref, watch, onMounted } from "vue"
import AddEditTask from "./AddEditTask.vue"
import DeleteTask from "./DeleteTask.vue"
import LimitTask from "./LimitTask.vue"
import router from "@/router"
import AlertComponent from "../toast/Alert.vue"
import { useRoute } from "vue-router"

const myTask = useTaskStore()
const myStatus = useStatusStore()
const myLimit = useLimitStore()
const openModal = ref(false)
const tasks = ref()
const editMode = ref(false)
const showDeleteModal = ref(false)
const route = useRoute()
const listDelete = ref()
const editDrop = ref(false)
const boardId = ref()
const modalAlert = ref({ message: "", type: "", modal: false })
const emits = defineEmits(["closeAddModal"])
console.log(boardId.value)

onMounted(async () => {
  //Task
  if (myTask.getTasks().length === 0) {
    const listTasks = await getItems(
      `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`
    )
    myTask.addTasks(listTasks)
  }
  //Status
  if (myStatus.getStatus().length === 0) {
    const listStatus = await getItems(
      `${import.meta.env.VITE_API_URL}boards/${boardId.value}/statuses`
    )
    myStatus.addStatus(listStatus)
  }
  //Limit
  const limitStatus = await getStatusLimits(
    `${import.meta.env.VITE_API_URL}statuses`
  )
  myLimit.addLimit(limitStatus)
})

//Alert
const showAlert = (message, type) => {
  modalAlert.value = {
    message,
    type,
    modal: true,
  }
  setTimeout(() => {
    modalAlert.value.modal = false
  }, 4000)
}

//Open Modal
// Edit Modal
const openModalEdit = async (id, boolean) => {
  if (boolean) {
    editMode.value = true
    editDrop.value = true
  } else {
    editDrop.value = false
  }

  const taskDetail = await getItemById(
    `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`,
    id
  )
  tasks.value = taskDetail

  if (taskDetail.status === 404) {
    router.push({ name: "TaskNotFound" })
    myTask.removeTasks(id)
    router.go(-1)
  } else {
    openModal.value = true
    editMode.value = false
  }
}

// Add Modal
const openModalAdd = () => {
  openModal.value = true
  editMode.value = true
  const selected = ref()
  selected.value = "No Status"

  tasks.value = {
    id: undefined,
    title: "",
    description: "",
    assignees: "",
    status: selected.value,
  }
}

//Delete Modal
const openDeleteModal = (id, title, index) => {
  showDeleteModal.value = true
  listDelete.value = {
    id: id,
    title: title,
    index: index + 1,
  }
}

//Close modal
// Add Edit Modal
const closeAddEdit = async (task) => {
  if (task.id !== undefined) {
    const { editedItem, statusCode } = await editItem(
      `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`,
      task.id,
      {
        title: task.title,
        description: task.description,
        assignees: task.assignees,
        status: task.status,
      }
    )

    if (statusCode === 200) {
      myTask.updateTask(editedItem)
      showAlert("The task has been updated", "success")
    }

    if (statusCode === 400 || statusCode === 404) {
      myTask.removeTasks(task.id)
      showAlert("An error has occurred, the task does not exist.", "error")
    }
  }

  if (task.id === undefined) {
    console.log(boardId.value)

    const { newTask, statusCode } = await addItem(
      `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`,
      task
    )

    if (statusCode === 201) {
      myTask.addTask(newTask)
      showAlert("The task has been successfully added", "success")
    }

    //Alert status
    if (statusCode === 400) {
      showAlert("An error has occurred, the task does not exist.", "error")
    }
  }

  openModal.value = false
  router.push({ name: "task" })
  editMode.value = false
}

// Delete Modal
const closeDeleteModal = async (id) => {
  const deleteItem = await deleteItemById(
    `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`,
    id
  )

  if (deleteItem === 200) {
    myTask.removeTasks(id)
    showAlert("The task has been deleted", "success")
  }

  if (deleteItem === 400) {
    myTask.removeTasks(id)
    showAlert("An error has occurred, the task does not exist.", "error")
  }
  showDeleteModal.value = false
}

// Limit model
const closeLimitModal = (maxLimit, limitBoolean) => {
  if (limitBoolean === false) {
    showAlert(
      `The Kanban board has disabled the task limit in each status.`,
      "success"
    )
  }
  if (limitBoolean === true) {
    showAlert(
      `The Kanban board now limits ${maxLimit} tasks in each status.`,
      "success"
    )
  }

  showLimitModal.value = false
}

const closeModal = () => {
  openModal.value = false
  showDeleteModal.value = false
  showLimitModal.value = false
  editMode.value = false

  router.push({ name: "task" })
}

//Sort status
const sortStatus = ref("default")
const listTaskStore = ref(myTask.getTasks())

const handleSortChange = async (status) => {
  // default -> asc -> desc
  // ถ้าเป็น default -> asc
  if (status === "default") {
    listTaskStore.value = myTask
      .getTasks()
      .sort((a, b) => a.status.localeCompare(b.status))
    sortStatus.value = "asc"
  }
  // ถ้าเป็น asc -> desc
  if (status === "asc") {
    listTaskStore.value = myTask
      .getTasks()
      .sort((a, b) => b.status.localeCompare(a.status))
    sortStatus.value = "desc"
  }
  // ถ้าเป็น desc -> default
  if (status === "desc") {
    listTaskStore.value = myTask.getTasks().sort((a, b) => a.id - b.id)
    sortStatus.value = "default"
  }
}

//Filter status
const filterStatus = ref([])
const filteredTasks = computed(() => {
  if (filterStatus.value.length === 0) {
    return listTaskStore.value
  } else {
    return listTaskStore.value.filter((task) =>
      filterStatus.value.includes(task.status)
    )
  }
})

const clearFilter = () => {
  filterStatus.value = []
}

// Limit
const showLimitModal = ref(false)

const openLimitModal = () => {
  showLimitModal.value = true
}

//ตอน Edit status จะเปลี่ยนเมื่อมีการเปลี่ยนแปลง
watch(
  () => myTask.getTasks(),
  (newTasks) => {
    listTaskStore.value = newTasks
  },
  { immediate: true }
)

// route path ถ้าไม่มี id นั้น
watch(
  () => route.params.taskId,
  async (newId, oldId) => {
    if (newId !== undefined) {
      const res = await getItemById(
        `${import.meta.env.VITE_API_URL}boards/${boardId.value}/tasks`,
        newId
      )
      if (res.status === 404) {
        router.push({ name: "TaskNotFound" })
      }
    }
  },
  { immediate: true }
)

watch(
  () => route.params.id,
  (newId) => {
    boardId.value = newId
  },
  { immediate: true }
)
</script>

<template>
  <!-- Head -->
  <div class="flex flex-col items-center mt-16 mb-20">
    <div class="font-bold text-4xl text-black m-2 self-start pl-72">
      My Task
    </div>
    <div class="flex justify-between w-3/5">
      <div class="flex justify-start items-center">
        <div v-if="filterStatus.length">
          <button
            class="btn btn-outline text-black font-light hover:bg-black"
            @click="clearFilter"
          >
            <p class="font-bold">X</p>
          </button>
        </div>
        <details class="dropdown dropdown-end itbkk-status-filter">
          <summary class="m-1 btn bg-black text-white">
            <img src="/icons/filter.png" class="w-6" />
            Filter
          </summary>
          <ul
            tabindex="0"
            class="overflow-y-auto h-64 dropdown-content z-[1] p-2 shadow bg-base-100 rounded-box w-30"
          >
            <li
              v-for="status in myStatus.getStatus()"
              class="itbkk-status-choice"
            >
              <div class="form-control" style="word-wrap: break-word">
                <label class="label cursor-pointer">
                  <input
                    type="checkbox"
                    :id="status.name"
                    :value="status.name"
                    class="checkbox mr-2"
                    v-model="filterStatus"
                  />
                  <div
                    class="shadow-md rounded-full font-medium p-2 text-black w-36 text-center mb-2 break-all"
                    :style="{
                      backgroundColor: myStatus.getStatusColor(status.name),
                    }"
                  >
                    <span class="label-text">{{ status.name }}</span>
                  </div>
                </label>
              </div>
            </li>
          </ul>
        </details>

        <router-link :to="{ name: 'tableStatus', params: { id: boardId } }">
          <button
            class="itbkk-manage-status btn text-l bg-black text-white ml-1"
          >
            Status
          </button>
        </router-link>
      </div>

      <div class="flex justify-end items-center">
        <button
          @click="openLimitModal"
          class="flex itbkk-manage-status btn text-l ml-1 bg-black text-white"
        >
          <img src="/icons/limit.png" class="w-6" />
          Limit
        </button>

        <router-link :to="{ name: 'addTask' }">
          <button
            @click="openModalAdd"
            class="itbkk-button-add btn btn-circle border-black0 bg-black text-white ml-2"
          >
            <img src="/icons/plus.png" class="w-4" />
          </button>
        </router-link>
      </div>
    </div>

    <!-- Table -->
    <div class="border border-black rounded-md w-3/5 mt-4">
      <table class="table">
        <!-- head -->
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-20">No.</th>
            <th class="pl-15">Title</th>
            <th class="pl-20">Assignees</th>
            <th class="pl-20">
              <button
                @click="handleSortChange(sortStatus)"
                class="btn btn-ghost itbkk-status-sort"
              >
                <div>Status</div>
                <svg
                  v-if="sortStatus"
                  xmlns="http://www.w3.org/2000/svg"
                  :class="{
                    'text-blue-500': sortStatus === 'default',
                    '': sortStatus !== 'default',
                  }"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                >
                  <g fill="none" fill-rule="evenodd">
                    <path
                      d="M24 0v24H0V0zM12.594 23.258l-.012.002l-.071.035l-.02.004l-.014-.004l-.071-.036c-.01-.003-.019 0-.024.006l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427c-.002-.01-.009-.017-.016-.018m.264-.113l-.014.002l-.184.093l-.01.01l-.003.011l.018.43l.005.012l.008.008l.201.092c.012.004.023 0 .029-.008l.004-.014l-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 0 0-.027.006l-.006.014l-.034.614c0 .012.007.02.017.024l.015-.002l.201-.093l.01-.008l.003-.011l.018-.43l-.003-.012l-.01-.01z"
                    />
                    <path
                      fill="currentColor"
                      :d="
                        {
                          default:
                            'M10.759 13c.94 0 1.43 1.092.855 1.792l-.078.086L7.414 19H11a1 1 0 0 1 .117 1.993L11 21H5.241c-.94 0-1.43-1.092-.855-1.792l.078-.086L8.586 15H5a1 1 0 0 1-.117-1.993L5 13zM17 4a1 1 0 0 1 1 1v12.414l1.121-1.121a1 1 0 0 1 1.415 1.414l-2.829 2.828a1 1 0 0 1-1.414 0l-2.828-2.828a1 1 0 0 1 1.414-1.414L16 17.414V5a1 1 0 0 1 1-1M8 3c.674 0 1.28.396 1.556 1.002l.054.133l2.332 6.529a1 1 0 0 1-1.838.78l-.046-.108L9.581 10H6.419l-.477 1.336a1 1 0 0 1-1.917-.56l.033-.112l2.332-6.53A1.71 1.71 0 0 1 8 3m0 2.573L7.133 8h1.734z',
                          asc: 'M10.759 13c.94 0 1.43 1.092.855 1.792l-.078.086L7.414 19H11a1 1 0 0 1 .117 1.993L11 21H5.241c-.94 0-1.43-1.092-.855-1.792l.078-.086L8.586 15H5a1 1 0 0 1-.117-1.993L5 13zM17 4a1 1 0 0 1 1 1v12.414l1.121-1.121a1 1 0 0 1 1.415 1.414l-2.829 2.828a1 1 0 0 1-1.414 0l-2.828-2.828a1 1 0 0 1 1.414-1.414L16 17.414V5a1 1 0 0 1 1-1M8 3c.674 0 1.28.396 1.556 1.002l.054.133l2.332 6.529a1 1 0 0 1-1.838.78l-.046-.108L9.581 10H6.419l-.477 1.336a1 1 0 0 1-1.917-.56l.033-.112l2.332-6.53A1.71 1.71 0 0 1 8 3m0 2.573L7.133 8h1.734z',
                          desc: 'M4.664 11.942a1 1 0 0 0 1.278-.606L6.419 10h3.162l.477 1.336a1 1 0 0 0 1.884-.672L9.61 4.134a1.71 1.71 0 0 0-3.22 0l-2.332 6.53a1 1 0 0 0 .606 1.278M8 5.573L8.867 8H7.133zm8.293-1.28a1 1 0 0 1 1.414 0l2.829 2.828a1 1 0 0 1-1.415 1.415L18 7.414V20a1 1 0 1 1-2 0V7.414l-1.121 1.122a1 1 0 1 1-1.415-1.415zM5 13a1 1 0 1 0 0 2h3.586l-4.122 4.122C3.77 19.815 4.26 21 5.24 21H11a1 1 0 1 0 0-2H7.414l4.122-4.122c.693-.693.203-1.878-.777-1.878z',
                        }[sortStatus]
                      "
                    />
                  </g>
                </svg>
              </button>
            </th>
            <th></th>
          </tr>
        </thead>

        <tbody class="bg-white" v-if="myTask.getTasks().length > 0">
          <!-- row -->
          <tr v-for="(task, index) in filteredTasks" :key="task.id">
            <th class="text-black pl-20">{{ index + 1 }}</th>
            <td class="itbkk-title itbkk-button-edit pl-15">
              <router-link :to="{ name: 'detailTask' }">
                <button
                  @click="openModalEdit(task.id)"
                  class="btn btn-ghost h-2"
                >
                  {{ task.title }}
                </button>
              </router-link>
            </td>
            <td class="itbkk-assignees pl-20">
              <p v-if="task.assignees">
                {{ task.assignees }}
              </p>
              <p v-else class="text-gray-500 font-medium italic">Unassigned</p>
            </td>
            <td class="itbkk-status pl-20">
              <div
                class="shadow-md rounded-full p-2 text-black w-36 text-center font-medium"
                :style="{
                  'background-color': myStatus.getStatusColor(task.status),
                }"
              >
                {{ task.status }}
              </div>
            </td>
            <td>
              <div class="dropdown dropdown-right">
                <div tabindex="0" role="button" class="m-1">
                  <svg
                    class="h-4"
                    fill="#000000"
                    viewBox="0 0 1024 1024"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                    <g
                      id="SVGRepo_tracerCarrier"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    ></g>
                    <g id="SVGRepo_iconCarrier">
                      <path
                        d="M388.8 896.4v-27.198c.6-2.2 1.6-4.2 2-6.4 8.8-57.2 56.4-102.4 112.199-106.2 62.4-4.4 115.2 31.199 132.4 89.199 2.2 7.6 3.8 15.6 5.8 23.4v27.2c-.6 1.8-1.6 3.399-1.8 5.399-8.6 52.8-46.6 93-98.6 104.4-4 .8-8 2-12 3h-27.2c-1.8-.6-3.6-1.6-5.4-1.8-52-8.4-91.599-45.4-103.6-96.8-1.2-5-2.6-9.6-3.8-14.2zm252.4-768.797l-.001 27.202c-.6 2.2-1.6 4.2-1.8 6.4-9 57.6-56.8 102.6-113.2 106.2-62.2 4-114.8-32-131.8-90.2-2.2-7.401-3.8-15-5.6-22.401v-27.2c.6-1.8 1.6-3.4 2-5.2 9.6-52 39.8-86 90.2-102.2 6.6-2.2 13.6-3.4 20.4-5.2h27.2c1.8.6 3.6 1.6 5.4 1.8 52.2 8.6 91.6 45.4 103.6 96.8 1.201 4.8 2.401 9.4 3.601 13.999zm-.001 370.801v27.2c-.6 2.2-1.6 4.2-2 6.4-9 57.4-58.6 103.6-114.6 106-63 2.8-116.4-35.2-131.4-93.8-1.6-6.2-3-12.4-4.4-18.6v-27.2c.6-2.2 1.6-4.2 2-6.4 8.8-57.4 58.6-103.601 114.6-106.2 63-3 116.4 35.2 131.4 93.8 1.6 6.4 3 12.6 4.4 18.8z"
                      ></path>
                    </g>
                  </svg>
                </div>
                <ul
                  tabindex="0"
                  class="dropdown-content menu bg-base-100 rounded-box z-[1] w-36 p-2 shadow"
                >
                  <router-link
                    :to="{ name: 'editTask', params: { taskId: task.id } }"
                  >
                    <li @click="openModalEdit(task.id, true)">
                      <a>Edit<img src="/icons/pen.png" class="w-4" /></a>
                    </li>
                  </router-link>

                  <li @click="openDeleteModal(task.id, task.title, index)">
                    <a>Delete<img src="/icons/trash.png" class="w-6" /></a>
                  </li>
                </ul>
              </div>
            </td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">
              No task
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Details -->
  <AddEditTask
    :showModal="openModal"
    :task="tasks"
    :editModeModal="editMode"
    @saveAddEdit="closeAddEdit"
    @closeModal="closeModal"
  />

  <DeleteTask
    :showDelete="showDeleteModal"
    :detailDelete="listDelete"
    @cancelDelete="closeModal"
    @closeDeleteTask="closeDeleteModal"
  />

  <LimitTask
    @closeLimitModal="closeLimitModal"
    @closeCancel="closeModal"
    :showLimitModal="showLimitModal"
  />

  <!-- Toast -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
</template>

<style scoped>
.itbkk-title {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 600px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรทัด (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
}

.itbkk-status {
  max-width: 600px;
  word-break: break-all;
}

.button-container {
  position: relative;
  display: inline-block;
}
</style>
