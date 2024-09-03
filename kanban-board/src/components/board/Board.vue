<script setup>
import router from "@/router"
import { ref, onMounted } from "vue"
import AddBoard from "./AddBoard.vue"
import { addItem, getItems } from "@/libs/fetchUtils"
import { useBoardStore } from "@/stores/boardStore.js"

const openModal = ref()
const useBoard = useBoardStore()
const openModalAdd = () => {
  openModal.value = true
}

onMounted(async () => {
  const listBoard = await getItems(`${import.meta.env.VITE_API_URL}boards`)
  useBoard.addBoards(listBoard)
})

const closeAdd = async (nameBoard) => {
  console.log(nameBoard)
  const { newTask, statusCode } = await addItem(
    `${import.meta.env.VITE_API_URL}boards`,
    nameBoard
  )

  console.log(newTask)

  if (statusCode === 201) {
    useBoard.addBoard(newTask)

    console.log(useBoard.getBoards())
    // showAlert("The task has been successfully added", "success")
  }

  openModal.value = false
  router.go(-1)
}

const closeModal = () => {
  openModal.value = false
  router.go(-1)
}
</script>

<template>
  <div class="flex flex-col items-center mt-16 mb-20 ml-52">
    <div class="flex justify-between w-3/5">
      <div class="bounce-in-top font-bold text-4xl m-2 ">Board list</div>
      <div>
        <router-link :to="{ name: 'addBoard' }">
          <button
            @click="openModalAdd"
            class="bounce-in-top itbkk-button-add btn btn-circle border-black0 bg-black text-white ml-2"
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
            <th class="pl-20">No.</th>
            <th class="pl-15">Name</th>
            <th class="pl-20">Action</th>
          </tr>
        </thead>
        <tbody v-if="useBoard.getBoards().length > 0">
          <tr v-for="(board, index) in useBoard.getBoards()">
            <th class="text-black pl-20">{{ index + 1 }}</th>

            <th>
              <router-link :to="{ name: 'task', params: { id: board.id } }">
                <button class="btn btn-ghost h-2">{{ board.name }}</button>
              </router-link>
            </th>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">
              No board
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddBoard
    :showModal="openModal"
    @closeModal="closeModal"
    @saveAdd="closeAdd"
  />
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
