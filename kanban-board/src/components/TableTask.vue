<script setup>
import { computed, ref, watch } from "vue"
import EditTask from "../components/EditTask.vue"
import { getItemById, getItems } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"
import { useStatusStore } from "../stores/statusStore"
import router from "@/router"
import Delete from "../components/DeleteTask.vue"
import { RouterLink } from "vue-router"
import AlertComponent from "./Alert.vue"
import { defineProps } from "vue"
import LimitTasks from "./LimitTasks.vue"

const showEditModal = ref(false)
const showDeleteModal = ref(false)
const listdelete = ref()
const task = ref()
const myStatus = useStatusStore()
const myTask = useTaskStore()
const modalAlert = ref({ message: "", type: "", modal: false })
const { showbtnDelete } = defineProps({
  showbtnDelete: Boolean,
})

//โชว์ add delete บน navbar
myTask.showNavbar = true

const closeCancle = () => {
  if (showEditModal.value === true) {
    showEditModal.value = false
    router.go(-1)
  }
  if (showDeleteModal.value === true) showDeleteModal.value = false
}
const closeEditModal = (statusCode) => {
  if (statusCode === 200) {
    myTask.updateTask(
      task.id,
      task.title,
      task.description,
      task.assignees,
      task.status,
      task.createdTime,
      task.updatedTime
    )
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "The task has been updated",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 400) {
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 404) {
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }
}

const openModal = async (taskId) => {
  if (taskId) {
    const data = await getItemById(
      `${import.meta.env.VITE_API_URL}tasks`,
      taskId
    )
    if (data.status === 404) {
      modalAlert.value = {
        message: "An error has occurred, the task does not exist.",
        type: "error",
        modal: true,
      }
      setTimeout(() => {
        modalAlert.value.modal = false
      }, "4000")
      myTask.removeTasks(modal.deleteId)
      router.go(-1)
    } else {
      task.value = data
      showEditModal.value = true
    }
  }
}

const openDeleteModal = (id, title, index) => {
  showDeleteModal.value = true
  listdelete.value = {
    id: id,
    title: title,
    index: index + 1,
  }
}

const closeDeleteModal = (statusCode) => {
  if (statusCode === 200) {
    showDeleteModal.value = false
    modalAlert.value = {
      message: "The task has been deleted",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 404) {
    showDeleteModal.value = false
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }
}

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
    const listTask = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
    listTaskStore.value = listTask

    sortStatus.value = "default"
  }
}

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

watch(
  () => myTask.getTasks(),
  (newTasks) => {
    listTaskStore.value = newTasks
  },
  { immediate: true }
)

const showLimitModal = ref(false)

const openLimitModal = () => {
  showLimitModal.value = true
}

const closeLimitModal = () => {
  showLimitModal.value = false
}
</script>

<template>
  <EditTask
    @closeModal="closeCancle"
    @closeEditTask="closeEditModal"
    :showModal="showEditModal"
    :task="task"
  />
  <Delete
    :showDelete="showDeleteModal"
    :detailDelete="listdelete"
    @cancleDelete="closeCancle"
    @closeDeleteTask="closeDeleteModal"
  />

  <LimitTasks
    @closeLimitModal="closeLimitModal"
    :showLimitModal="showLimitModal"
  />

  <!-- Task Table -->
  <div class="flex flex-col items-center mt-16 mb-20">
    <div class="flex justify-between w-4/5">
      <div class="font-bold text-4xl text-blue-400 m-2">My Task</div>

      <div class="flex items-center">
        <details className="dropdown dropdown-end ">
          <summary className="mr-1 btn bg-pink-400 text-white">
            <img src="/icons/filter.png" class="w-6" />
            Filter
          </summary>
          <ul
            tabIndex="{0}"
            className="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-30"
          >
            <li v-for="status in myStatus.getStatus()">
              <div className="form-control">
                <label className="label cursor-pointer">
                  <input
                    type="checkbox"
                    :id="status.name"
                    :value="status.name"
                    className="checkbox mr-2"
                    v-model="filterStatus"
                  />
                  <span className="label-text">{{ status.name }}</span>
                </label>
              </div>
            </li>
          </ul>
        </details>

        <RouterLink :to="{ name: 'tableStatus' }">
          <button class="itbkk-manage-status btn text-l bg-pink-400 text-white">
            Manage Status
          </button>
        </RouterLink>

        <button
          @click="openLimitModal"
          class="itbkk-manage-status btn text-l ml-1 bg-pink-400 text-white"
        >
          Limit Tasks in Status
        </button>
      </div>
    </div>

    <div class="overflow-x-auto border border-blue-400 rounded-md w-4/5 mt-4">
      <table class="table">
        <thead class="bg-blue-400">
          <tr class="text-white text-sm">
            <th></th>
            <th class="pl-20">Title</th>
            <th class="pl-20">Assignees</th>
            <th class="pl-20">
              <button
                @click="handleSortChange(sortStatus)"
                class="btn btn-ghost"
              >
                <div class="">Status</div>
                <div class="">
                  <svg
                    v-if="sortStatus === 'default'"
                    xmlns="http://www.w3.org/2000/svg"
                    class="text-blue-200"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                  >
                    <g fill="none" fill-rule="evenodd">
                      <path
                        d="M24 0v24H0V0zM12.593 23.258l-.011.002l-.071.035l-.02.004l-.014-.004l-.071-.035c-.01-.004-.019-.001-.024.005l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427c-.002-.01-.009-.017-.017-.018m.265-.113l-.013.002l-.185.093l-.01.01l-.003.011l.018.43l.005.012l.008.007l.201.093c.012.004.023 0 .029-.008l.004-.014l-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 0 0-.027.006l-.006.014l-.034.614c0 .012.007.02.017.024l.015-.002l.201-.093l.01-.008l.004-.011l.017-.43l-.003-.012l-.01-.01z"
                      />
                      <path
                        fill="currentColor"
                        d="M10.759 13c.94 0 1.43 1.092.855 1.792l-.078.086L7.414 19H11a1 1 0 0 1 .117 1.993L11 21H5.241c-.94 0-1.43-1.092-.855-1.792l.078-.086L8.586 15H5a1 1 0 0 1-.117-1.993L5 13zM17 4a1 1 0 0 1 1 1v12.414l1.121-1.121a1 1 0 0 1 1.415 1.414l-2.829 2.828a1 1 0 0 1-1.414 0l-2.828-2.828a1 1 0 0 1 1.414-1.414L16 17.414V5a1 1 0 0 1 1-1M8 3c.674 0 1.28.396 1.556 1.002l.054.133l2.332 6.529a1 1 0 0 1-1.838.78l-.046-.108L9.581 10H6.419l-.477 1.336a1 1 0 0 1-1.917-.56l.033-.112l2.332-6.53A1.71 1.71 0 0 1 8 3m0 2.573L7.133 8h1.734z"
                      />
                    </g>
                  </svg>
                  <svg
                    v-if="sortStatus === 'asc'"
                    xmlns="http://www.w3.org/2000/svg"
                    class=""
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                  >
                    <g fill="none" fill-rule="evenodd">
                      <path
                        d="M24 0v24H0V0zM12.593 23.258l-.011.002l-.071.035l-.02.004l-.014-.004l-.071-.035c-.01-.004-.019-.001-.024.005l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427c-.002-.01-.009-.017-.017-.018m.265-.113l-.013.002l-.185.093l-.01.01l-.003.011l.018.43l.005.012l.008.007l.201.093c.012.004.023 0 .029-.008l.004-.014l-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 0 0-.027.006l-.006.014l-.034.614c0 .012.007.02.017.024l.015-.002l.201-.093l.01-.008l.004-.011l.017-.43l-.003-.012l-.01-.01z"
                      />
                      <path
                        fill="currentColor"
                        d="M10.759 13c.94 0 1.43 1.092.855 1.792l-.078.086L7.414 19H11a1 1 0 0 1 .117 1.993L11 21H5.241c-.94 0-1.43-1.092-.855-1.792l.078-.086L8.586 15H5a1 1 0 0 1-.117-1.993L5 13zM17 4a1 1 0 0 1 1 1v12.414l1.121-1.121a1 1 0 0 1 1.415 1.414l-2.829 2.828a1 1 0 0 1-1.414 0l-2.828-2.828a1 1 0 0 1 1.414-1.414L16 17.414V5a1 1 0 0 1 1-1M8 3c.674 0 1.28.396 1.556 1.002l.054.133l2.332 6.529a1 1 0 0 1-1.838.78l-.046-.108L9.581 10H6.419l-.477 1.336a1 1 0 0 1-1.917-.56l.033-.112l2.332-6.53A1.71 1.71 0 0 1 8 3m0 2.573L7.133 8h1.734z"
                      />
                    </g>
                  </svg>
                  <svg
                    v-if="sortStatus === 'desc'"
                    xmlns="http://www.w3.org/2000/svg"
                    class=""
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
                        d="M4.664 11.942a1 1 0 0 0 1.278-.606L6.419 10h3.162l.477 1.336a1 1 0 0 0 1.884-.672L9.61 4.134a1.71 1.71 0 0 0-3.22 0l-2.332 6.53a1 1 0 0 0 .606 1.278M8 5.573L8.867 8H7.133zm8.293-1.28a1 1 0 0 1 1.414 0l2.829 2.828a1 1 0 0 1-1.415 1.415L18 7.414V20a1 1 0 1 1-2 0V7.414l-1.121 1.122a1 1 0 1 1-1.415-1.415zM5 13a1 1 0 1 0 0 2h3.586l-4.122 4.122C3.77 19.815 4.26 21 5.24 21H11a1 1 0 1 0 0-2H7.414l4.122-4.122c.693-.693.203-1.878-.777-1.878z"
                      />
                    </g>
                  </svg>
                </div>
              </button>
            </th>
            <th></th>
          </tr>
        </thead>
        <tbody class="bg-white" v-if="myTask.getTasks().length > 0">
          <tr
            v-for="(task, index) in filteredTasks"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-blue-400 pl-20">{{ index + 1 }}</th>
            <td class="itbkk-title itbkk-button-action itbkk-button-edit pl-20">
              <router-link :to="{ name: 'edit', params: { id: task.id } }">
                <button
                  @click="openModal(task.id)"
                  class="btn btn-ghost h-auto"
                  style="text-align: left"
                >
                  {{ task.title }}<img src="/icons/pen.png" class="w-4" />
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
                class="rounded-md p-2 text-black w-36 text-center"
                :style="{
                  'background-color': myStatus.getStatusColor(task.status),
                }"
              >
                {{ task.status }}
              </div>
            </td>
            <td>
              <button
                @click="openDeleteModal(task.id, task.title, index)"
                v-if="showbtnDelete"
                class="itbkk-button-delete btn bg-red-500"
              >
                <img src="/icons/delete.png" class="w-4" />
              </button>
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

  <!-- Alert -->
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
</style>
