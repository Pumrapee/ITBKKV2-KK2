<script setup>
import router from "@/router"
import { ref, onMounted } from "vue"
import AddBoard from "./AddBoard.vue"
import DeleteBoard from "./DeleteBoard.vue"
import Alert from "../toast/Alert.vue"
import {
  addItem,
  getItems,
  deleteItemById,
  checkAndRefreshToken,
  getBoardItems,
  getItemById,
} from "@/libs/fetchUtils"
import { useBoardStore } from "@/stores/boardStore.js"
import { useAuthStore } from "@/stores/loginStore"
import ExpireToken from "../toast/ExpireToken.vue"

const openModal = ref()
const myBoard = useBoardStore()
const myUser = useAuthStore()
const expiredToken = ref(false)
const showDeleteModal = ref(false)
const boardIdDelete = ref("")
const refreshToken = ref(localStorage.getItem("refreshToken"))
const modalAlert = ref({ message: "", type: "", modal: false })

const openModalAdd = () => {
  openModal.value = true
}

onMounted(async () => {
  myUser.setToken()
  expiredToken.value = false

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    expiredToken.value = false

    const listBoard = await getBoardItems(
      `${import.meta.env.VITE_API_URL}v3/boards`
    )
    if (myBoard.getBoards().length === 0) {
      //401
      if (listBoard === 401) {
        expiredToken.value = true
      } else {
        listBoard.sort((a, b) => new Date(a.createdOn) - new Date(b.createdOn))

        const ownerBoard = listBoard.filter((boards) => {
          return boards.role === "OWNER"
        })
        myBoard.addBoards(ownerBoard)

        //Collabs
        const collabBoard = listBoard.filter((boards) => {
          return boards.role === "COLLABORATOR"
        })
        myBoard.addBoardsCollab(collabBoard)
      }
    }

    if (
      myBoard.getBoards().length === 1 &&
      !myBoard.navBoard &&
      myBoard.getBoardCollab().length === 0
    ) {
      router.push({ name: "task", params: { id: myBoard.getBoards()[0].id } })
    } else if (
      myBoard.getBoards().length === 1 &&
      myBoard.navBoard &&
      myBoard.getBoardCollab().length === 0
    ) {
      router.push({ name: "task", params: { id: myBoard.getBoards()[0].id } })
    } else if (myBoard.navBoard) {
      router.push({ name: "board" }) // นำทางไปยังหน้า board เมื่อค่า navBoard เป็น true
      myBoard.navBoard = false
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
})

const closeAdd = async (nameBoard) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    const { newTask, statusCode } = await addItem(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      nameBoard
    )

    if (statusCode === 201) {
      myBoard.addBoard(newTask)
      router.push({ name: "task", params: { id: newTask.id } })
      myBoard.boardName = newTask.name
      // showAlert("The board has been updated", "success")
    }

    if (statusCode === 401) {
      alert("There is a problem. Please try again later.")
      expiredToken.value = true
      openModal.value = false
    }
    openModal.value = false
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
  }
}

const closeModal = () => {
  openModal.value = false
  showDeleteModal.value = false
  router.push({ name: "board" })
}

const openDeleteModal = (id) => {
  showDeleteModal.value = true
  boardIdDelete.value = id
}

const closeDeleteModal = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    const deleteBoard = await deleteItemById(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      boardIdDelete.value
    )

    if (deleteBoard === 200) {
      myBoard.removeBoards(boardIdDelete.value)
      showAlert("The board has been deleted", "success")
    }
    showDeleteModal.value = false
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showDeleteModal.value = false
  }
}

const openLeaveModal = async (boardId) => {}

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

const activeTab = ref("personal") // ค่าเริ่มต้นเป็น 'profile'
</script>

<template>
  <div class="mb-4 border-b border-gray-200 dark:border-gray-700 ml-96 mt-16">
    <ul
      class="flex flex-wrap text-sm font-medium text-center ml-80"
      role="tablist"
    >
      <li class="me-2" role="presentation">
        <button
          @click="activeTab = 'personal'"
          :class="activeTab === 'personal' ? 'border-b-2 border-blue-500' : ''"
          class="itbkk-personal-board inline-block p-4 border-b-2 rounded-t-lg text-lg"
          id="personal-tab"
          type="button"
          role="tab"
          aria-controls="personal"
          :aria-selected="activeTab === 'personal'"
        >
          Personal Boards
        </button>
      </li>
      <li class="me-2" role="presentation">
        <button
          @click="activeTab = 'collab'"
          :class="activeTab === 'collab' ? 'border-b-2 border-blue-500' : ''"
          class="itbkk-collab-board inline-block p-4 border-b-2 rounded-t-lg text-lg hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300"
          id="collab-tab"
          type="button"
          role="tab"
          aria-controls="collab"
          :aria-selected="activeTab === 'collab'"
        >
          Collab Boards
        </button>
      </li>
    </ul>
  </div>

  <div id="default-tab-content">
    <div v-if="activeTab === 'personal'" id="personal" role="tabpanel">
      <!-- Add board -->
      <div class="flex flex-col items-center mt-16 mb-20 ml-60">
        <div class="bounce-in-top flex justify-between w-3/5">
          <div class="font-bold text-4xl m-2">Board list</div>
          <div>
            <router-link :to="{ name: 'addBoard' }">
              <button
                @click="openModalAdd"
                class="itbkk-button-create btn btn-circle border-black0 bg-black text-white ml-2"
              >
                <img src="/icons/plus.png" class="w-4" />
              </button>
            </router-link>
          </div>
        </div>

        <!-- Table -->
        <div class="bounce-in-top border border-black rounded-md w-3/5 mt-4">
          <table class="table">
            <!-- head -->
            <thead class="bg-black">
              <tr class="text-white text-sm">
                <th class="pl-16">No.</th>
                <th class="pl-36">Name</th>
                <th class="pl-4">Visibility</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody
              class="itbkk-personal-item"
              v-if="myBoard.getBoards().length > 0"
            >
              <tr v-for="(board, index) in myBoard.getBoards()">
                <th class="text-black pl-20">{{ index + 1 }}</th>

                <th>
                  <router-link :to="{ name: 'task', params: { id: board.id } }">
                    <button class="itbkk-board-name btn btn-ghost h-2">
                      {{ board.name }}
                    </button>
                  </router-link>
                </th>
                <th>
                  <div
                    class="itbkk-board-visibility shadow-md rounded-full p-2 text-black w-20 text-center font-medium"
                    :class="{
                      'bg-green-500': board.visibility === 'PUBLIC',
                      'bg-orange-300': board.visibility === 'PRIVATE',
                    }"
                  >
                    {{ board.visibility }}
                  </div>
                </th>

                <th>
                  <div>
                    <button
                      class="itbkk-button-delete btn bg-red-500"
                      @click="openDeleteModal(board.id)"
                    >
                      <img src="/icons/delete.png" class="w-4" />
                    </button>
                  </div>
                </th>
              </tr>
            </tbody>

            <tbody v-else>
              <tr>
                <td
                  colspan="5"
                  class="text-center py-4 text-gray-500 font-medium"
                >
                  No board
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div v-if="activeTab === 'collab'" id="collab" role="tabpanel">
      <div class="flex flex-col items-center mt-16 mb-20 ml-60">
        <div class="bounce-in-top flex justify-between w-3/5">
          <div class="font-bold text-4xl m-2">Collab list</div>
        </div>
        <!-- Table -->
        <div class="bounce-in-top border border-black rounded-md w-3/5 mt-4">
          <table class="table">
            <!-- head -->
            <thead class="bg-black">
              <tr class="text-white text-sm">
                <th class="pl-16">No.</th>
                <th class="pl-16">Name</th>
                <th class="pl-16">Owner</th>
                <th class="pl-10">Access Right</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody
              class="itbkk-collab-item"
              v-if="myBoard.getBoardCollab().length > 0"
            >
              <tr v-for="(boardCollab, index) in myBoard.getBoardCollab()">
                <th class="text-black pl-20">{{ index + 1 }}</th>

                <th>
                  <router-link
                    :to="{ name: 'task', params: { id: boardCollab.id } }"
                  >
                    <button class="itbkk-board-name btn btn-ghost h-2">
                      {{ boardCollab.name }}
                    </button>
                  </router-link>
                </th>
                <th>
                  <p class="itbkk-owner-name h-2 mb-3 ml-5">
                    {{ boardCollab.owner.name }}
                  </p>
                </th>
                <th>
                  <p
                    class="itbkk-access-right shadow-md rounded-full h-auto max-w-20 font-medium text-center p-3 break-all bg-white ml-6"
                  >
                    {{ boardCollab.accessRight }}
                  </p>
                </th>

                <th>
                  <div>
                    <button
                      class="itbkk-leave-board"
                      @click="openLeaveModal(boardCollab.id)"
                    >
                      <svg
                        class="w-6 h-6 text-gray-800 dark:text-white"
                        aria-hidden="true"
                        xmlns="http://www.w3.org/2000/svg"
                        width="24"
                        height="24"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke="currentColor"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M20 12H8m12 0-4 4m4-4-4-4M9 4H7a3 3 0 0 0-3 3v10a3 3 0 0 0 3 3h2"
                        />
                      </svg>
                    </button>
                  </div>
                </th>
              </tr>
            </tbody>

            <tbody v-else>
              <tr>
                <td
                  colspan="5"
                  class="text-center py-4 text-gray-500 font-medium"
                >
                  No board
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <AddBoard
    :showModal="openModal"
    @closeModal="closeModal"
    @saveAdd="closeAdd"
  />

  <DeleteBoard
    :showDelete="showDeleteModal"
    :boardId="boardIdDelete"
    @closeDeleteBoard="closeDeleteModal"
    @cancelDelete="closeModal"
  />

  <Alert
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped>
.bounce-in-top {
  -webkit-animation: bounce-in-top 1.1s both;
  animation: bounce-in-top 1.1s both;
}
@-webkit-keyframes bounce-in-top {
  0% {
    -webkit-transform: translateY(-500px);
    transform: translateY(-500px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
    opacity: 0;
  }
  38% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
    opacity: 1;
  }
  55% {
    -webkit-transform: translateY(-65px);
    transform: translateY(-65px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  72% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  81% {
    -webkit-transform: translateY(-28px);
    transform: translateY(-28px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  90% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  95% {
    -webkit-transform: translateY(-8px);
    transform: translateY(-8px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  100% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
}
@keyframes bounce-in-top {
  0% {
    -webkit-transform: translateY(-500px);
    transform: translateY(-500px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
    opacity: 0;
  }
  38% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
    opacity: 1;
  }
  55% {
    -webkit-transform: translateY(-65px);
    transform: translateY(-65px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  72% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  81% {
    -webkit-transform: translateY(-28px);
    transform: translateY(-28px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  90% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  95% {
    -webkit-transform: translateY(-8px);
    transform: translateY(-8px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  100% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
}
</style>
