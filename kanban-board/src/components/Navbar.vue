<script setup>
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { ref } from "vue"
import { getItems, getItemById, checkAndRefreshToken } from "@/libs/fetchUtils"

const authStore = useAuthStore()
const myBoard = useBoardStore()
const myStatus = useStatusStore()
const myTask = useTaskStore()
const userName = localStorage.getItem("user")
const isDropdownOpen = ref(false)
const refreshToken = ref(localStorage.getItem("refreshToken"))

const logout = () => {
  authStore.logout()
}

const boardAll = () => {
  // myBoard.clearBoard()
  myStatus.clearStatus()
  myTask.clearTask()
  myBoard.navBoard = true
}

const boardById = async () => {
  myStatus.clearStatus()
  myTask.clearTask()
}

function toggleDropdown() {
  isDropdownOpen.value = !isDropdownOpen.value
}
</script>

<template>
  <!-- Navbar -->
  <div class="navbar bg-white border-b border-gray z-[2] fixed w-full top-0">
    <!-- Navbar Content -->
    <div class="navbar-start font-custom" @click="boardAll">
      <button
        class="btn btn-ghost bg-white pl-5 flex items-center text-2xl text-black"
      >
        <!-- Logo -->
        <img src="/icons/logonavbar2.png" class="w-10 m-2" />
        <RouterLink :to="{ name: 'board' }">
          <span>KRADAN KANBAN</span>
        </RouterLink>
      </button>
    </div>

    <!-- user name -->
    <div class="itbkk-fullname navbar-end pr-5">Hi, {{ userName }}</div>
    <div class="cursor-pointer dropdown dropdown-end">
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
    class="slide-in-left backdrop-blur fixed top-0 left-0 z-[1] w-64 h-screen transition-transform -translate-x-full sm:translate-x-0 shadow-lg"
    aria-label="Sidebar"
  >
    <div
      class="h-full px-3 py-4 overflow-y-auto backdrop-blur-lg dark:bg-gray-800"
    >
      <ul class="space-y-2 font-medium mt-16">
        <li>
          <!-- Toggle button for the dropdown -->
          <button
            type="button"
            class="flex items-center w-full p-2 text-base text-gray-900 transition duration-75 rounded-lg group hover:bg-gray-100 dark:text-white dark:hover:bg-gray-700"
            @click="toggleDropdown"
          >
            <svg
              class="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
              aria-hidden="true"
              xmlns="http://www.w3.org/2000/svg"
              fill="currentColor"
              viewBox="0 0 18 18"
            >
              <path
                d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0ZM16.143 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286C17.169 8 18 7.169 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0ZM6.143 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286C7.169 18 8 17.169 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10ZM16.143 10h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286C17.169 18 18 17.169 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z"
              />
            </svg>
            <span class="flex-1 ms-3 text-left rtl:text-right whitespace-nowrap"
              >Personal Boards</span
            >
            <!-- Dropdown arrow icon -->
            <svg
              :class="{ 'rotate-180': isDropdownOpen }"
              class="w-3 h-3 transition-transform"
              aria-hidden="true"
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
              v-for="board in myBoard.getBoards()"
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
          <!-- <router-link :to="{ name: 'tableStatus' }"> -->
          <div
            class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
          >
            <svg
              class="flex-shrink-0 w-5 h-5 text-gray-500 transition duration-75 dark:text-gray-400 group-hover:text-gray-900 dark:group-hover:text-white"
              aria-hidden="true"
              xmlns="http://www.w3.org/2000/svg"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                d="M5 5V.13a2.96 2.96 0 0 0-1.293.749L.879 3.707A2.96 2.96 0 0 0 .13 5H5Z"
              />
              <path
                d="M6.737 11.061a2.961 2.961 0 0 1 .81-1.515l6.117-6.116A4.839 4.839 0 0 1 16 2.141V2a1.97 1.97 0 0 0-1.933-2H7v5a2 2 0 0 1-2 2H0v11a1.969 1.969 0 0 0 1.933 2h12.134A1.97 1.97 0 0 0 16 18v-3.093l-1.546 1.546c-.413.413-.94.695-1.513.81l-3.4.679a2.947 2.947 0 0 1-1.85-.227 2.96 2.96 0 0 1-1.635-3.257l.681-3.397Z"
              />
              <path
                d="M8.961 16a.93.93 0 0 0 .189-.019l3.4-.679a.961.961 0 0 0 .49-.263l6.118-6.117a2.884 2.884 0 0 0-4.079-4.078l-6.117 6.117a.96.96 0 0 0-.263.491l-.679 3.4A.961.961 0 0 0 8.961 16Zm7.477-9.8a.958.958 0 0 1 .68-.281.961.961 0 0 1 .682 1.644l-.315.315-1.36-1.36.313-.318Zm-5.911 5.911 4.236-4.236 1.359 1.359-4.236 4.237-1.7.339.341-1.699Z"
              />
            </svg>
            <span class="flex-1 ms-3 whitespace-nowrap">Collab Boards</span>
          </div>

          <!-- </router-link> -->
        </li>
      </ul>
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
</style>
