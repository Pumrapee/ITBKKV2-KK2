<script setup>
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { ref, watch } from "vue"
import { useRoute } from "vue-router"
import { getBoardItems } from "@/libs/fetchUtils"

const authStore = useAuthStore()
const myBoard = useBoardStore()
const myStatus = useStatusStore()
const myTask = useTaskStore()
const userName = localStorage.getItem("user")
const isDropdownOpen = ref(false)
const route = useRoute()
const nowBoardId = ref(route.params.id)
const boardList = ref(myBoard.getBoards())

const logout = () => {
  authStore.logout()
}

const boardAll = () => {
  // myBoard.clearBoard()
  // myBoard.clearBoardCollab()
  myStatus.clearStatus()
  myTask.clearTask()
  myBoard.navBoard = true
}

const boardById = async (boardId) => {
  if (nowBoardId.value === boardId) {
    return
  } else {
    myStatus.clearStatus()
    myTask.clearTask()
  }
}

const collabBoard = () => {
  myBoard.navBarCollab = true
  myBoard.navBoard = true
}

function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value
}

watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId !== oldId) {
      nowBoardId.value = newId
    }
  },
  { immediate: true }
)

watch(
  () => boardList,
  async (newBoard) => {
    if (newBoard.value === undefined || boardList.value.length === 0) {
      const boardFetch = await getBoardItems(
        `${import.meta.env.VITE_API_URL}v3/boards`
      )
      const boardFetchSort = boardFetch.owner.sort(
        (a, b) => new Date(a.createdOn) - new Date(b.createdOn)
      )
      console.log(boardFetch)

      boardList.value = boardFetchSort
      console.log(boardList.value)
    }
  },
  { immediate: true }
)

const isSidebarOpen = ref(false)

function toggleSidebar() {
  isSidebarOpen.value = !isSidebarOpen.value
}
</script>

<template>
  <!-- Navbar -->
  <div class="navbar bg-white border-b border-gray z-[2] fixed w-full top-0 flex justify-between items-center p-3 sm:p-5">
    <div class="navbar-start font-custom flex items-center">
      <button
        class="btn btn-ghost bg-white pl-3 flex items-center text-xl sm:text-2xl text-black"
        @click="boardAll"
      >
        <img src="/icons/logonavbar2.png" class="w-8 sm:w-10 m-2" />
        <RouterLink :to="{ name: 'board' }">
          <span>KRADAN KANBAN</span>
        </RouterLink>
      </button>
    </div>
    <!-- Mobile Toggle Button -->
    <button
      class="block sm:hidden p-2"
      @click="toggleSidebar"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-6 w-6 text-black"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="2"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M4 6h16M4 12h16m-7 6h7"
        />
      </svg>
    </button>
    <!-- user name (Desktop only) -->
    <div class="hidden sm:block itbkk-fullname navbar-end pr-5">Hi, {{ userName }}</div>
    <div class="hidden sm:block cursor-pointer dropdown dropdown-end">
      <div tabindex="0">
        <img src="/icons/profile-user.png" alt="profile" class="h-8" />
      </div>
      <ul
        tabindex="0"
        class="menu dropdown-content bg-base-100 rounded-box z-[1] mt-4 w-40 p-2 shadow"
      >
        <li><a>Profile</a></li>
        <li @click="logout"><a>Logout</a></li>
      </ul>
    </div>
  </div>

  <!-- Sidebar -->
  <aside
    id="sidebar-multi-level-sidebar"
    :class="{
      'translate-x-0': isSidebarOpen,
      '-translate-x-full': !isSidebarOpen,
      'sm:translate-x-0': true
    }"
    class="backdrop-blur fixed top-0 left-0 z-[1] w-64 h-screen transition-transform sm:shadow-lg bg-white/20 dark:bg-gray-800 "
    aria-label="Sidebar"
  >
    <div class="h-full px-3 py-4 overflow-y-auto">
      

      <!-- Existing sidebar items -->
      <ul class="space-y-2 font-medium mt-24">
        <li>
          <button
            type="button"
            class="flex items-center w-full p-2 text-base text-gray-900 transition duration-75 rounded-lg group hover:bg-gray-100 dark:text-white dark:hover:bg-gray-700"
            @click="toggleDropdown"
          >
            <svg
              class="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="currentColor"
              viewBox="0 0 18 18"
            >
              <path d="..." />
            </svg>
            <span class="flex items-center ms-3 text-left whitespace-nowrap">Personal Boards</span>
            <svg
              :class="{ 'rotate-180': isDropdownOpen }"
              class="w-3 h-3 transition-transform ml-2"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 10 6"
            >
              <path
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="m1 1 4 4 4-4"
              />
            </svg>
          </button>
          <!-- Dropdown items (board list) -->
          <ul v-show="isDropdownOpen" class="py-2 space-y-2">
            <!-- Your existing dropdown items -->
            <li>
              <RouterLink :to="{ name: 'board' }">
                <div
                  class="flex items-center text-sm w-full p-2 text-gray-900 transition duration-75 rounded-lg pl-11 group hover:bg-gray-100 dark:text-white dark:hover:bg-gray-700"
                  @click="boardAll"
                >
                  All
                </div>
              </RouterLink>
            </li>
            <li
              v-for="board in boardList"
              :key="board.id"
              @click="boardById(board.id)"
            >
              <RouterLink :to="{ name: 'task', params: { id: board.id } }">
                <div
                  class="flex items-center text-sm w-full p-2 text-gray-900 transition duration-75 rounded-lg pl-11 group hover:bg-gray-100 dark:text-white dark:hover:bg-gray-700"
                >
                  {{ board.name }}
                </div>
              </RouterLink>
            </li>
          </ul>
        </li>
        <li>
          <router-link :to="{ name: 'board' }">
            <div
              class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
              @click="collabBoard"
            >
              <svg
                class="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path d="..." />
              </svg>
              <span class="flex-1 ms-3 whitespace-nowrap">Collab Boards</span>
            </div>
          </router-link>
        </li>
      </ul>
      <!-- Profile section for mobile -->
      <div class="block sm:hidden mb-4 mt-6">
        <div class="flex items-center space-x-4 mb-2">
          <img src="/icons/profile-user.png" alt="profile" class="h-8" />
          <span>Hi, {{ userName }}</span>
        </div>
        <ul class="menu bg-base-100 rounded-box w-full p-2 shadow">
          <li><a>Profile</a></li>
          <li @click="logout"><a>Logout</a></li>
        </ul>
      </div>
    </div>
  </aside>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");

body {
  font-family: "Open Sans", sans-serif;
}
.slide-in-left {
  -webkit-animation: slide-in-left 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)
    both;
  animation: slide-in-left 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
@-webkit-keyframes slide-in-left {
  0% {
    -webkit-transform: translateX(-1000px);
    transform: translateX(-1000px);
    opacity: 0;
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
    opacity: 1;
  }
}
@keyframes slide-in-left {
  0% {
    -webkit-transform: translateX(-1000px);
    transform: translateX(-1000px);
    opacity: 0;
  }
  100% {
    -webkit-transform: translateX(0);
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .navbar-start {
    padding-left: 1rem;
  }

  .itbkk-fullname {
    display: none;
  }

  .slide-in-left {
    transform: translateX(-100%);
  }

  aside {
    width: 100%;
  }
}
</style>
