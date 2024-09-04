<script setup>
import router from "@/router"
import { ref, onMounted, watch } from "vue"
import AddBoard from "./AddBoard.vue"
import { addItem, getItems } from "@/libs/fetchUtils"
import { useBoardStore } from "@/stores/boardStore.js"
import { useRoute } from "vue-router"

const openModal = ref()
const myBoard = useBoardStore()
const route = useRoute()

const openModalAdd = () => {
  openModal.value = true
}

onMounted(async () => {
  const listBoard = await getItems(`${import.meta.env.VITE_API_URL}boards`)
  myBoard.addBoards(listBoard)
  console.table(listBoard)
  console.table(myBoard.getBoards()[0].id)
})

const closeAdd = async (nameBoard) => {
  console.log(nameBoard)
  const { newTask, statusCode } = await addItem(
    `${import.meta.env.VITE_API_URL}boards`,
    nameBoard
  )

  console.log(newTask)

  if (statusCode === 201) {
    myBoard.addBoard(newTask)

    console.log(myBoard.getBoards())
    // showAlert("The task has been successfully added", "success")
  }

  openModal.value = false
  router.go(-1)
}

const closeModal = () => {
  openModal.value = false
  router.go(-1)
}

// watch(
//   () => myBoard.getBoards(),
//   (newBoards) => {
//     if (newBoards.length > 0) {
//       const boardId = newBoards[0].id
//       console.log(boardId)
//       router.push({ name: "task", params: { id: boardId } })
//     }
//   },
//   { immediate: true } // ตรวจสอบค่าทันทีเมื่อโหลด component
// )

// router.beforeEach((to, from, next) => {
//   if (to.name === "board" && myBoard.getBoards().length > 0) {
//     const boardId = myBoard.getBoards()[0].id
//     console.log(boardId)
//     next({ name: "task", params: { id: boardId } })
//   } else {
//     next()
//   }
// })
</script>

<template>
  <div class="flex flex-col items-center mt-16 mb-20 ml-60">
    <div class="flex justify-between w-3/5">
      <div class="font-bold text-4xl m-2">Board list</div>
      <div>
        <router-link :to="{ name: 'addBoard' }">
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
            <th class="pl-15">Name</th>
            <th class="pl-20">Action</th>
          </tr>
        </thead>
        <tbody class="bg-white" v-if="myBoard.getBoards().length > 0">
          <tr v-for="(board, index) in myBoard.getBoards()">
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

<style scoped></style>
