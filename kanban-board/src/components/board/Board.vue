<script setup>
import router from "@/router"
import { ref, onMounted } from "vue"
import AddBoard from "./AddBoard.vue"
import DeleteBoard from "./DeleteBoard.vue"
import RemoveCollaborator from "../collab/RemoveCollaborator.vue"
import { showAlert } from "../../libs/alertUtils"
import {
  addItem,
  deleteItemById,
  checkAndRefreshToken,
  getBoardItems,
} from "@/libs/fetchUtils"
import { useBoardStore } from "@/stores/boardStore.js"
import { useAuthStore } from "@/stores/loginStore"
import ExpireToken from "../toast/ExpireToken.vue"

// Store
const myBoard = useBoardStore()
const myUser = useAuthStore()

const openModal = ref()
const expiredToken = ref(false)
const showDeleteModal = ref(false)
const showLeaveModal = ref(false)
const boardIdDelete = ref("")
const collabsName = ref("")
const ownerIdBoard = ref(localStorage.getItem("oid"))
const refreshToken = ref(localStorage.getItem("refreshToken"))

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
      if (!listBoard || listBoard === 401) {
        expiredToken.value = true
      } else {
        //200
        const listBoardSort = listBoard.owner.sort(
          (a, b) => new Date(a.createdOn) - new Date(b.createdOn)
        )
        myBoard.addBoards(listBoardSort)

        //Collabs
        const listCollabSort = listBoard.collab.sort(
          (a, b) => new Date(a.createdOn) - new Date(b.createdOn)
        )
        myBoard.addBoardsCollab(listCollabSort)
      }
    } else {
      //Collabs
      const listCollabSort = listBoard.collab.sort(
        (a, b) => new Date(a.createdOn) - new Date(b.createdOn)
      )
      myBoard.addBoardsCollab(listCollabSort)
    }

    // Route Board
    if (myBoard.navBarCollab && myBoard.navBoard) {
      //กด collab ที่ side bar
      activeTab.value = "collab"
      myBoard.navBarCollab = false
      myBoard.navBoard = false
    } else if (
      myBoard.getBoards().length === 1 &&
      !myBoard.navBoard &&
      myBoard.getBoardCollab().length === 0
    ) {
      //ถ้ามี Board 1 อัน และไม่มี Collab board และ ไม่ใช่การกดจาก side bar ให้ไป Board แรกที่มี
      router.push({ name: "task", params: { id: myBoard.getBoards()[0].id } })
    } else if (myBoard.navBoard) {
      //กด All ที่ side bar
      router.push({ name: "board" }) // นำทางไปยังหน้า board เมื่อค่า navBoard เป็น true
      myBoard.navBoard = false
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
})

//Open Modal
//Add
const openModalAdd = () => {
  openModal.value = true
}

//Leave
const openLeaveModal = async (boardId, collabName) => {
  showLeaveModal.value = true
  boardIdDelete.value = boardId
  collabsName.value = collabName
}

//Delete
const openDeleteModal = (id) => {
  showDeleteModal.value = true
  boardIdDelete.value = id
}

//Invitation
const openInvitations = (collabId) => {
  router.push({ name: "invitations", params: { id: collabId } })
}

//Close Modal
//Add
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

//Delete
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
    } else if (deleteBoard === 401) {
      expiredToken.value = true
      showDeleteModal.value = false
    }

    showDeleteModal.value = false
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showDeleteModal.value = false
  }
}

//Leave
const closeLeaveModal = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    const statusCode = await deleteItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardIdDelete.value}/collabs`,
      ownerIdBoard.value
    )

    if (statusCode === 200) {
      myBoard.removeBoardCollab(ownerIdBoard.value) // ลบ collaborator ออกจาก list
      showLeaveModal.value = false
      showAlert("Collaborator removed successfully.", "success")
    } else if (statusCode === 403) {
      myBoard.removeBoardCollab(ownerIdBoard.value)
      showLeaveModal.value = false
      showAlert(
        "You do not have permission to leave board collaborator.",
        "error"
      )
    } else if (statusCode === 404) {
      myBoard.removeBoardCollab(ownerIdBoard.value)
      showLeaveModal.value = false
      showAlert(`${collabsName.value} is not a collaborator.`, "error")
    } else if (statusCode === 401) {
      expiredToken.value = true
      showLeaveModal.value = false
    } else {
      showAlert("There is a problem. Please try again later.", "error")
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showLeaveModal.value = false
  }
}

const closeModal = () => {
  openModal.value = false
  showDeleteModal.value = false
  showLeaveModal.value = false
  router.push({ name: "board" })
}
const activeTab = ref("personal") // ค่าเริ่มต้นเป็น 'profile'
</script>

<template>
  <div class="m-4 mr-24 mt-24">
    <div class="mb-4 border-b border-gray-200 dark:border-gray-700 ml-96 mt-16">
      <!-- Dropdown menu for small screens -->
      <div class="sm:hidden">
        <label for="tabs" class="sr-only">Select tab</label>
        <select
          id="tabs"
          class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
          @change="activeTab = $event.target.value"
        >
          <option value="personal" :selected="activeTab === 'personal'">
            Personal Boards
          </option>
          <option value="collab" :selected="activeTab === 'collab'">
            Collab Boards
          </option>
        </select>
      </div>

      <!-- Tabs for larger screens -->
      <ul
        class="hidden sm:flex flex-wrap text-sm font-medium text-center text-gray-500 rounded-lg shadow dark:divide-gray-700 dark:text-gray-400"
        role="tablist"
      >
        <li class="flex-1" role="presentation">
          <button
            @click="activeTab = 'personal'"
            :class="
              activeTab === 'personal'
                ? 'border-b-2 border-black text-gray-900 bg-gray-100 dark:bg-gray-700 dark:text-white'
                : 'bg-white dark:bg-gray-800 dark:hover:bg-gray-700'
            "
            class="itbkk-personal-board inline-block w-full p-4 rounded-s-lg"
            id="personal-tab"
            type="button"
            role="tab"
            aria-controls="personal"
            :aria-selected="activeTab === 'personal'"
          >
            Personal Boards
          </button>
        </li>
        <li class="flex-1" role="presentation">
          <button
            @click="activeTab = 'collab'"
            :class="
              activeTab === 'collab'
                ? 'border-b-2 border-black text-gray-900 bg-gray-100 dark:bg-gray-700 dark:text-white'
                : 'bg-white dark:bg-gray-800 dark:hover:bg-gray-700'
            "
            class="itbkk-collab-board inline-block w-full p-4 rounded-e-lg"
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
          <div class="flex justify-between w-full sm:w-3/5">
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
          <div
            class="border border-black rounded-md w-full sm:w-3/5 mt-4 overflow-x-auto"
          >
            <table class="table w-full">
              <thead class="bg-black">
                <tr class="text-white text-sm">
                  <th class="pl-4 sm:pl-16 hidden md:table-cell">No.</th>
                  <th class="pl-4 sm:pl-16">Name</th>
                  <th class="pl-4">Visibility</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody
                class="itbkk-personal-item"
                v-if="myBoard.getBoards().length > 0"
              >
                <tr v-for="(board, index) in myBoard.getBoards()">
                  <th class="text-black pl-4 sm:pl-20 hidden md:table-cell">
                    {{ index + 1 }}
                  </th>
                  <th>
                    <router-link
                      :to="{ name: 'task', params: { id: board.id } }"
                    >
                      <button class="itbkk-board-name btn btn-ghost h-2">
                        {{ board.name }}
                      </button>
                    </router-link>
                  </th>
                  <th>
                    <div
                      class="shadow-md rounded-full p-2 text-black w-20 text-center font-medium"
                      :class="{
                        'bg-green-400': board.visibility === 'PUBLIC',
                        'bg-red-300': board.visibility === 'PRIVATE',
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
    </div>

    <div v-if="activeTab === 'collab'" id="collab" role="tabpanel">
      <div class="flex flex-col items-center mt-16 mb-20 ml-60">
        <div class="flex justify-between w-full sm:w-3/5">
          <div class="font-bold text-4xl m-2">Collab list</div>
        </div>
        <!-- Table -->
        <div
          class="border border-black rounded-md w-full sm:w-3/5 mt-4 overflow-x-auto"
        >
          <table class="table w-full">
            <thead class="bg-black">
              <tr class="text-white text-sm">
                <th class="pl-4 sm:pl-16 hidden md:table-cell">No.</th>
                <th class="pl-4 sm:pl-16">Name</th>
                <th class="pl-4 sm:pl-16 hidden md:table-cell">Owner</th>
                <th class="pl-4 sm:pl-10">Access Right</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody
              class="itbkk-collab-item"
              v-if="myBoard.getBoardCollab().length > 0"
            >
              <tr v-for="(boardCollab, index) in myBoard.getBoardCollab()">
                <th class="text-black pl-4 sm:pl-20 hidden md:table-cell">
                  {{ index + 1 }}
                </th>

                <th>
                  <router-link
                    :to="{ name: 'task', params: { id: boardCollab.id } }"
                  >
                    <button class="itbkk-board-name btn btn-ghost h-2">
                      {{ boardCollab.name }}
                      <p
                        v-if="boardCollab.status === `PENDING`"
                        class="text-slate-400"
                      >
                        (Pending Invite)
                      </p>
                    </button>
                  </router-link>
                </th>
                <th class="hidden md:table-cell">
                  <p class="itbkk-owner-name h-2 mb-8 ml-2 sm:ml-12">
                    {{ boardCollab.owner.name }}
                  </p>
                </th>
                <th>
                  <p
                    class="itbkk-access-right shadow-md rounded-full h-auto max-w-20 font-medium text-center p-3 break-all bg-white ml-2 sm:ml-6"
                  >
                    {{ boardCollab.accessRight }}
                  </p>
                </th>

                <th v-if="boardCollab.status === `MEMBER`">
                  <div>
                    <button
                      class="itbkk-leave-board"
                      @click="openLeaveModal(boardCollab.id, boardCollab.name)"
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
                <th v-if="boardCollab.status === `PENDING`">
                  <div>
                    <button
                      class="itbkk-leave-board btn w-24"
                      @click="openInvitations(boardCollab.id)"
                    >
                      <p class="text-xs">Accept/Decline</p>
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
                  No collaborators
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

  <ExpireToken v-if="expiredToken" />

  <RemoveCollaborator
    :showLeave="showLeaveModal"
    :selectedCollabName="collabsName"
    @cancelDelete="closeModal"
    @confirmRemove="closeLeaveModal"
  />
</template>

<style scoped>
/* Mobile adjustments */
@media (max-width: 640px) {
  .m-4 {
    margin-left: 1rem;
    margin-right: 1rem;
  }
  .ml-60 {
    margin-left: 1rem;
  }
  .ml-96 {
    margin-left: 0;
  }
}
</style>
