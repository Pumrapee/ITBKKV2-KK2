<script setup>
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { useLimitStore } from "@/stores/limitStore"
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import {
  getItemById,
  editItem,
  addItem,
  deleteItemById,
  getItems,
  getStatusLimits,
  Visibility,
  checkAndRefreshToken,
  editLimitStatus,
} from "@/libs/fetchUtils"
import { defineEmits, computed, ref, watch, onMounted } from "vue"
import AddEditTask from "./AddEditTask.vue"
import DeleteTask from "./DeleteTask.vue"
import LimitTask from "./LimitTask.vue"
import ExpireToken from "../toast/ExpireToken.vue"
import router from "@/router"
import AlertComponent from "../toast/Alert.vue"
import { useRoute } from "vue-router"
import ModalVisibility from "../toast/ModalVisibility.vue"

//store
const myTask = useTaskStore()
const myStatus = useStatusStore()
const myLimit = useLimitStore()
const myUser = useAuthStore()
const myBoard = useBoardStore()

const openModal = ref(false)
const tasks = ref()
const editMode = ref(false)
const showDeleteModal = ref(false)
const route = useRoute()
const listDelete = ref()
const editDrop = ref(false)
const boardId = ref(route.params.id)
const modalAlert = ref({ message: "", type: "", modal: false })
const expiredToken = ref(false)
const boardName = ref()
const refreshToken = ref(localStorage.getItem("refreshToken"))
const emits = defineEmits(["closeAddModal"])
const disabledIfnotOwner = ref(false)
const nameOwnerBoard = ref()
const disabledVisibility = ref()

// user name login
const userName = localStorage.getItem("user")

// visibility
const isPublic = ref(false)
const showModalVisibility = ref(false)
const originalIsPublic = ref(isPublic.value)

onMounted(async () => {
  myUser.setToken()
  expiredToken.value = false

  // const checkToken = await checkAndRefreshToken(
  //   `${import.meta.env.VITE_API_URL}token`,
  //   myUser.token,
  //   refreshToken.value
  // )

  // if (checkToken.statusCode === 200) {
  //   //กรณีที่ token หมดอายุ ให้ต่ออายุ token
  //   myUser.setNewToken(checkToken.accessNewToken)
  //   expiredToken.value = false

  //   //Task
  //   if (myTask.getTasks().length === 0) {
  //     const listTasks = await getItems(
  //       `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`
  //     )

  //     if (listTasks === 401) {
  //       expiredToken.value = true
  //     } else if (listTasks.status === 404) {
  //       router.push({ name: "TaskNotFound" })
  //     } else {
  //       myTask.addTasks(listTasks)
  //     }
  //   }

  //   //Board
  //   const boardIdNumber = await getItemById(
  //     `${import.meta.env.VITE_API_URL}v3/boards`,
  //     boardId.value
  //   )

  //   nameOwnerBoard.value = boardIdNumber.owner.name

  //   boardName.value = boardIdNumber.name

  //   if (boardIdNumber.visibility === "PRIVATE") {
  //     isPublic.value = false // Private จะเป็นค่า false
  //   } else if (boardIdNumber.visibility === "PUBLIC") {
  //     isPublic.value = true // Public จะเป็นค่า true
  //   }

  //   //Status
  //   if (myStatus.getStatus().length === 0) {
  //     const listStatus = await getItems(
  //       `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`
  //     )
  //     if (listStatus === 401) {
  //       expiredToken.value = true
  //     } else if (listStatus.status === 404) {
  //       router.push({ name: "TaskNotFound" })
  //     } else {
  //       myStatus.addStatus(listStatus)
  //     }
  //   }

  //   //Limit
  //   const limitStatus = await getStatusLimits(
  //     `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`
  //   )
  //   if (limitStatus === 401) {
  //     expiredToken.value = true
  //   } else if (limitStatus.status === 404) {
  //     router.push({ name: "TaskNotFound" })
  //   } else {
  //     myLimit.editLimit(limitStatus)
  //   }

  //   //Collab
  //   if (myBoard.getCollabs().length === 0) {
  //     const collabList = await getItems(
  //       `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
  //     )
  //     if (collabList === 401) {
  //       expiredToken.value = true
  //     } else {
  //       if (myBoard.getCollabs().length === 0) {
  //         collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))

  //         myBoard.addCollabs(collabList)
  //       }
  //     }
  //   }

  //   function validateBoardAccess(isOwner, userOid) {
  //     if (isOwner) {
  //       return false
  //     }

  //     const collab = myBoard
  //       .getCollabs()
  //       .find((collab) => collab.oid === userOid)
  //     let accessRight

  //     if (collab !== undefined) {
  //       accessRight = collab.accessRight
  //     }
  //     if (accessRight !== undefined) {
  //       // If the user has WRITE access, they can manage tasks and statuses
  //       if (accessRight === "WRITE") {
  //         return false
  //       }
  //     }
  //     return true
  //   }

  //   myBoard.clearCollaborator()
  //   const collabList = await getItems(
  //     `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
  //   )
  //   collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))
  //   myBoard.addCollabs(collabList)

  //   if (
  //     validateBoardAccess(
  //       nameOwnerBoard.value === userName,
  //       localStorage.getItem("oid")
  //     )
  //   ) {
  //     disabledIfnotOwner.value = true
  //   }

  //   if (nameOwnerBoard.value !== userName) {
  //     disabledVisibility.value = true
  //   }
  // }

  // if (checkToken.statusCode === 401) {
  //   expiredToken.value = true
  // }
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
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    if (boolean) {
      editMode.value = true
      editDrop.value = true
    } else {
      editDrop.value = false
    }

    const taskDetail = await getItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`,
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

    if (taskDetail === 401) {
      openModal.value = false
      expiredToken.value = true
    }
  }

  if (checkToken.statusCode === 401) {
    openModal.value = false
    expiredToken.value = true
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

// Limit Modal
const showLimitModal = ref(false)

const openLimitModal = () => {
  showLimitModal.value = true
}

//Close modal
// Add Edit Modal
const closeAddEdit = async (task) => {
  myUser.setToken()
  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    if (task.id !== undefined) {
      const { editedItem, statusCode } = await editItem(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`,
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

      if (statusCode === 401) {
        expiredToken.value = true
      }
    } else {
      openModal.value = false
      router.push({ name: "task" })
      editMode.value = false
    }

    if (task.id === undefined) {
      const { newTask, statusCode } = await addItem(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`,
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

      if (statusCode === 401) {
        expiredToken.value = true
      }
    } else {
      openModal.value = false
      router.push({ name: "task" })
      editMode.value = false
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
    editMode.value = false
  }
}

// Delete Modal
const closeDeleteModal = async (id) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    const deleteItem = await deleteItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`,
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

    if (deleteItem === 401) {
      expiredToken.value = true
    }
    showDeleteModal.value = false
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showDeleteModal.value = false
  }
}

// Limit model
const closeLimitModal = async (maxLimit, limitBoolean, expiredToken) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    if (limitBoolean === true) {
      const { editedLimit, status } = await editLimitStatus(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        limitBoolean,
        maxLimit
      )

      if (status === 200) {
        myLimit.editLimit(editedLimit)

        showAlert(
          `The Kanban board now limits ${maxLimit} tasks in each status.`,
          "success"
        )
      } else if (status === 401) {
        expiredToken.value = true
      }
    }

    if (limitBoolean === false) {
      const { editedLimit, status } = await editLimitStatus(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        limitBoolean,
        maxLimit
      )
      if (status === 200) {
        myLimit.editLimit(editedLimit)
        showAlert(
          `The Kanban board has disabled the task limit in each status.`,
          "success"
        )
      } else if (status === 401) {
        expiredToken.value = true
      }
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showLimitModal.value = false
  }

  showLimitModal.value = false
}

//All close modal
const closeModal = () => {
  openModal.value = false
  showDeleteModal.value = false
  showLimitModal.value = false
  editMode.value = false

  router.push({ name: "task" })
}

// Visibility modal
const openModalVisibility = () => {
  originalIsPublic.value = isPublic.value
  showModalVisibility.value = true
}

const confirmVisibilityChange = async () => {
  showModalVisibility.value = false
  const newVisibility = isPublic.value ? "PUBLIC" : "PRIVATE"

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    const { responseBody, statusCode } = await Visibility(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      boardId.value,
      newVisibility
    )

    if (statusCode === 200) {
      myBoard.updateVisibility(boardId.value, responseBody)
      showAlert(`The Kanban board change visibility success.`, "success")
    } else if (statusCode === 401) {
      expiredToken.value = true
      showModalVisibility.value = false
      return
    } else if (statusCode === 403) {
      showAlert(
        "You do not have permission to change board visibility",
        "error"
      )
    } else {
      showAlert("There is a problem. Please try again later.", "error")
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showModalVisibility.value = false
  }
}

const cancelVisibilityChange = () => {
  isPublic.value = originalIsPublic.value
  showModalVisibility.value = false
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

const removeFilter = (index) => {
  filterStatus.value.splice(index, 1)
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
    myUser.setToken()

    const checkToken = await checkAndRefreshToken(
      `${import.meta.env.VITE_API_URL}token`,
      myUser.token,
      refreshToken.value
    )
    if (checkToken.statusCode === 200) {
      //กรณีที่ token หมดอายุ ให้ต่ออายุ token
      myUser.setNewToken(checkToken.accessNewToken)
      if (newId !== undefined) {
        const res = await getItemById(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`,
          newId
        )
        if (res.status === 404) {
          router.push({ name: "TaskNotFound" })
        }
      }
    }

    if (checkToken.statusCode === 401) {
      expiredToken.value = true
    }
  },
  { immediate: true }
)

watch(
  () => route.params.id,
  async (newId, oldId) => {
    if (newId !== oldId) {
      boardId.value = newId
      await fetchBoardData(newId) // เรียกฟังก์ชันดึงข้อมูลใหม่
    }
  },
  { immediate: true }
)

// ฟังก์ชันสำหรับดึงข้อมูลใหม่เมื่อ boardId เปลี่ยน
async function fetchBoardData(id) {
  myTask.clearTask()
  myStatus.clearStatus()
  myLimit.clearLimit()
  myBoard.clearBoardCollab()
  myBoard.clearCollaborator()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    expiredToken.value = false

    //Task
    if (myTask.getTasks().length === 0) {
      const listTasks = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${id}/tasks`
      )

      if (listTasks === 401) {
        expiredToken.value = true
      } else if (listTasks.status === 404) {
        router.push({ name: "TaskNotFound" })
      } else {
        myTask.addTasks(listTasks)
      }
    }

    //Board
    const boardIdNumber = await getItemById(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      boardId.value
    )

    nameOwnerBoard.value = boardIdNumber.owner.name

    boardName.value = boardIdNumber.name

    if (boardIdNumber.visibility === "PRIVATE") {
      isPublic.value = false // Private จะเป็นค่า false
    } else if (boardIdNumber.visibility === "PUBLIC") {
      isPublic.value = true // Public จะเป็นค่า true
    }

    //Status
    if (myStatus.getStatus().length === 0) {
      const listStatus = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${id}/statuses`
      )
      if (listStatus === 401) {
        expiredToken.value = true
      } else if (listStatus.status === 404) {
        router.push({ name: "TaskNotFound" })
      } else {
        myStatus.addStatus(listStatus)
      }
    }

    //Limit
    const limitStatus = await getStatusLimits(
      `${import.meta.env.VITE_API_URL}v3/boards/${id}/statuses`
    )
    if (limitStatus === 401) {
      expiredToken.value = true
    } else if (limitStatus.status === 404) {
      router.push({ name: "TaskNotFound" })
    } else {
      myLimit.editLimit(limitStatus)
    }

    //Collab
    if (myBoard.getCollabs().length === 0) {
      const collabList = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${id}/collabs`
      )
      if (collabList === 401) {
        expiredToken.value = true
      } else {
        if (myBoard.getCollabs().length === 0) {
          collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))

          myBoard.addCollabs(collabList)
        }
      }
    }

    function validateBoardAccess(isOwner, userOid) {
      if (isOwner) {
        return false
      }

      const collab = myBoard
        .getCollabs()
        .find((collab) => collab.oid === userOid)
      let accessRight

      if (collab !== undefined) {
        accessRight = collab.accessRight
      }
      if (accessRight !== undefined) {
        // If the user has WRITE access, they can manage tasks and statuses
        if (accessRight === "WRITE") {
          return false
        }
      }
      return true
    }

    myBoard.clearCollaborator()
    const collabList = await getItems(
      `${import.meta.env.VITE_API_URL}v3/boards/${id}/collabs`
    )
    collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))
    myBoard.addCollabs(collabList)

    if (
      validateBoardAccess(
        nameOwnerBoard.value === userName,
        localStorage.getItem("oid")
      )
    ) {
      disabledIfnotOwner.value = true
    }

    if (nameOwnerBoard.value !== userName) {
      disabledVisibility.value = true
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}
</script>

<template>
  <!-- Head -->
  <div class="flex flex-col items-center mt-28 ml-4 md:mt-28 md:ml-60">
    <div class="font-bold text-2xl md:text-4xl text-black self-center pb-3 md:pb-5 flex items-center justify-between md:ml-20">
      <span>{{ boardName }}</span>
    </div>

    <!-- Filter Search -->
    <div class="flex flex-col md:flex-row justify-between w-full md:w-4/5 px-2">
      <div class="flex justify-center items-center mt-4 w-full md:w-auto">
        <div class="relative w-full md:w-auto max-w-2xl">
          <div class="dropdown">
            <div class="flex items-center border p-1 mb-4 bg-white rounded-xl max-h-40 flex-nowrap overflow-x-auto w-full md:w-full scrollbar-hidden">
              <!-- Filter Chips -->
              <span v-for="(filter, index) in filterStatus" :key="index" class="bg-gray-200 text-gray-700 px-3 py-1 rounded-full mr-2 flex items-center whitespace-nowrap">
                {{ filter }}
                <button @click="removeFilter(index)" class="ml-2 text-red-500">x</button>
              </span>

              <div tabindex="0" role="button" class="p-1 text-gray-400 flex-grow min-w-[120px] outline-none cursor-pointer">
                {{ filterStatus.length > 0 ? "" : "Filter Enter..." }}
              </div>
            </div>
            <ul tabindex="0" class="overflow-y-auto h-64 dropdown-content z-[1] p-2 shadow bg-base-100 rounded-box w-full md:w-full">
              <li v-for="status in myStatus.getStatus()" class="itbkk-status-choice">
                <div class="form-control" style="word-wrap: break-word">
                  <label class="label cursor-pointer">
                    <input type="checkbox" :id="status.name" :value="status.name" class="checkbox mr-2" v-model="filterStatus" />
                    <div class="shadow-md rounded-full font-medium p-2 text-black w-full md:w-full h-10 text-center mb-2 break-all" :style="{ backgroundColor: myStatus.getStatusColor(status.name) }">
                      <span class="label-text">{{ status.name }}</span>
                    </div>
                  </label>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div v-show="filterStatus.length > 0" class="ml-3 mb-5" @click="clearFilter">
          <svg xmlns="http://www.w3.org/2000/svg" class="-ml-1 mr-3 h-5 w-5 text-gray-400 hover:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
      </div>

      <!-- Toggle Visibility -->
      <div class="itbkk-board-visibility form-control relative group mb-3 md:mb-0 md:ml-3">
        <label class="label cursor-pointer flex flex-col items-center">
          <span class="label-text font-bold">{{ isPublic ? "Public" : "Private" }}</span>
          <input :disabled="disabledVisibility" type="checkbox" class="toggle m-1" v-model="isPublic" @click="openModalVisibility" />
        </label>
        <!-- Tooltip -->
        <div v-if="disabledVisibility" class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10">
          You need to be board owner to perform this action.
        </div>
      </div>

      <!-- Navigation Buttons Dropdown for Mobile -->
      <div class="relative group mb-3 md:hidden w-full flex justify-center">
        <button tabindex="0" role="button" class="btn bg-black text-white w-full flex justify-between items-center">
          Options
          <svg xmlns="http://www.w3.org/2000/svg" class="ml-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" />
          </svg>
        </button>
        <ul tabindex="0" class="dropdown-content menu bg-base-100 rounded-box w-full mt-1 z-[1] shadow">
          <!-- Collaborator -->
          <router-link :to="{ name: 'collabBoard', params: { id: boardId } }">
            <li><a class="block px-4 py-2 hover:bg-gray-200 transition-colors">Manage Collaborator</a></li>
          </router-link>
          <!-- Status -->
          <router-link :to="{ name: 'tableStatus', params: { id: boardId } }">
            <li><a class="block px-4 py-2 hover:bg-gray-200 transition-colors">Status</a></li>
          </router-link>
          <!-- Limit Button -->
          <li>
            <button @click="openLimitModal" :disabled="disabledIfnotOwner" class="block w-full text-left px-4 py-2 hover:bg-gray-200 transition-colors disabled:bg-gray-300">
              Limit
            </button>
          </li>
          <!-- Add Task Button -->
          <router-link :to="{ name: 'addTask' }">
            <li>
              <button @click="openModalAdd" :disabled="disabledIfnotOwner" :class="{ disabled: disabledIfnotOwner }" class="block w-full text-left px-4 py-2 hover:bg-gray-200 transition-colors">
                Add Task
              </button>
            </li>
          </router-link>
        </ul>
      </div>

      <!-- Navigation Buttons for Desktop -->
      <div class="hidden md:flex flex-wrap justify-end items-center mt-4 w-full md:w-auto">
        <!-- Collaborator -->
        <router-link :to="{ name: 'collabBoard', params: { id: boardId } }">
          <button class="itbkk-manage-collaborator btn text-sm md:text-l bg-black text-white mb-3 md:mb-0 md:ml-1">
            Manage Collaborator
          </button>
        </router-link>

        <!-- Status -->
        <router-link :to="{ name: 'tableStatus', params: { id: boardId } }">
          <button class="itbkk-manage-status btn text-sm md:text-l bg-black text-white mb-3 md:mb-0 md:ml-1">
            Status
          </button>
        </router-link>

        <!-- Limit Button -->
        <div class="relative group mb-3 md:mb-0 md:ml-1">
          <button @click="openLimitModal" :disabled="disabledIfnotOwner" class="flex btn text-sm md:text-l bg-black text-white">
            <img src="/icons/limit.png" class="w-4 md:w-6" />
            Limit
          </button>
          <!-- Tooltip -->
          <div v-if="disabledIfnotOwner" class="absolute bottom-full mb-2 hidden px-2 py-1 text-xs text-white bg-gray-700 rounded opacity-0 group-hover:opacity-100 group-hover:block transition-opacity duration-300">
            You need to be board owner to perform this action.
          </div>
        </div>

        <!-- Add Button -->
        <div class="relative group mb-3 md:mb-0 md:ml-1">
          <router-link :to="{ name: 'addTask' }">
            <button @click="openModalAdd" :disabled="disabledIfnotOwner" :class="{ disabled: disabledIfnotOwner }" class="itbkk-button-add btn btn-circle border-black0 bg-black text-white">
              <img src="/icons/plus.png" class="w-4" />
            </button>
          </router-link>
          <!-- Tooltip -->
          <div v-if="disabledIfnotOwner" class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10">
            You need to be board owner to perform this action.
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="border border-black rounded-md w-full md:w-4/5 mt-4 overflow-x-auto">
      <table class="table w-full">
        <!-- head -->
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-4 md:pl-20">No.</th>
            <th class="pl-4 md:pl-15">Title</th>
            <th class="pl-4 md:pl-20">Assignees</th>
            <th class="pl-4 md:pl-20">Status</th>
            <th></th>
          </tr>
        </thead>

        <tbody v-if="myTask.getTasks().length > 0">
          <!-- row -->
          <tr v-for="(task, index) in filteredTasks" :key="task.id" class="itbkk-item">
            <th class="text-black pl-4 md:pl-20">{{ index + 1 }}</th>
            <td class="itbkk-title itbkk-button-edit pl-4 md:pl-15">
              <router-link :to="{ name: 'detailTask', params: { taskId: task.id } }">
                <button @click="openModalEdit(task.id)" class="btn btn-ghost h-2">
                  {{ task.title }}
                </button>
              </router-link>
            </td>
            <td class="itbkk-assignees pl-4 md:pl-20">
              <p v-if="task.assignees">
                {{ task.assignees }}
              </p>
              <p v-else class="text-gray-500 font-medium italic">Unassigned</p>
            </td>
            <td class="itbkk-status pl-4 md:pl-20">
              <div class="shadow-md rounded-full p-2 text-black w-full md:w-36 text-center font-medium" :style="{ 'background-color': myStatus.getStatusColor(task.status) }">
                {{ task.status }}
              </div>
            </td>
            <td>
              <div v-if="!disabledIfnotOwner" class="dropdown dropdown-right mr-4 md:mr-10 itbkk-button-action">
                <div tabindex="0" role="button" class="m-1">
                  <svg class="h-4" fill="#000000" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                    <g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g>
                    <g id="SVGRepo_iconCarrier">
                      <path d="M388.8 896.4v-27.198c.6-2.2 1.6-4.2 2-6.4 8.8-57.2 56.4-102.4 112.199-106.2 62.4-4.4 115.2 31.199 132.4 89.199 2.2 7.6 3.8 15.6 5.8 23.4v27.2c-.6 1.8-1.6 3.399-1.8 5.399-8.6 52.8-46.6 93-98.6 104.4-4 .8-8 2-12 3h-27.2c-1.8-.6-3.6-1.6-5.4-1.8-52-8.4-91.599-45.4-103.6-96.8-1.2-5-2.6-9.6-3.8-14.2zm252.4-768.797l-.001 27.202c-.6 2.2-1.6 4.2-1.8 6.4-9 57.6-56.8 102.6-113.2 106.2-62.2 4-114.8-32-131.8-90.2-2.2-7.401-3.8-15-5.6-22.401v-27.2c.6-1.8 1.6-3.4 2-5.2 9.6-52 39.8-86 90.2-102.2 6.6-2.2 13.6-3.4 20.4-5.2h27.2c1.8.6 3.6 1.6 5.4 1.8 52.2 8.6 91.6 45.4 103.6 96.8 1.201 4.8 2.401 9.4 3.601 13.999zm-.001 370.801v27.2c-.6 2.2-1.6 4.2-2 6.4-9 57.4-58.6 103.6-114.6 106-63 2.8-116.4-35.2-131.4-93.8-1.6-6.2-3-12.4-4.4-18.6v-27.2c.6-2.2 1.6-4.2 2-6.4 8.8-57.4 58.6-103.601 114.6-106.2 63-3 116.4 35.2 131.4 93.8 1.6 6.4 3 12.6 4.4 18.8z"></path>
                    </g>
                  </svg>
                </div>
                <!-- dropdown -->
                <ul tabindex="0" class="itbkk-button-action z-[100]   dropdown-content menu bg-base-100 rounded-box  w-36 p-2 shadow">
                  <router-link :to="{ name: 'editTask', params: { taskId: task.id } }">
                    <li @click="openModalEdit(task.id, true)" class="itbkk-button-edit">
                      <a>Edit<img src="/icons/pen.png" class="w-4" /></a>
                    </li>
                  </router-link>

                  <li @click="openDeleteModal(task.id, task.title, index)" class="itbkk-button-delete">
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
  <AddEditTask :showModal="openModal" :task="tasks" :editModeModal="editMode" :ownerBoard="nameOwnerBoard" @saveAddEdit="closeAddEdit" @closeModal="closeModal" />

  <DeleteTask :showDelete="showDeleteModal" :detailDelete="listDelete" @cancelDelete="closeModal" @closeDeleteTask="closeDeleteModal" />

  <LimitTask @closeLimitModal="closeLimitModal" @closeCancel="closeModal" :showLimitModal="showLimitModal" />

  <!-- Toast -->
  <AlertComponent :message="modalAlert.message" :type="modalAlert.type" :showAlert="modalAlert.modal" />

  <ExpireToken v-if="expiredToken" />

  <!-- Use VisibilityModal -->
  <ModalVisibility :isPublic="isPublic" :showModalVisibility="showModalVisibility" @confirm="confirmVisibilityChange" @cancel="cancelVisibilityChange" />
</template>

<style scoped>
.scrollbar-hidden {
  overflow-x: auto;
  scrollbar-width: none; /* For Firefox */
}

.scrollbar-hidden::-webkit-scrollbar {
  display: none; /* For Chrome, Safari, and Opera */
}

.itbkk-title {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 600px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรท (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
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