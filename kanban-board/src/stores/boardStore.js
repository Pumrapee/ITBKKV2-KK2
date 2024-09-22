import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useBoardStore = defineStore("board", () => {
  //state
  const board = ref([])
  const navBoard = ref(false)
  const boardName = ref("")
  //view
  const getBoards = () => {
    return board.value
  }
  //actions
  const addBoards = (newBoard) => {
    // loop array
    newBoard.forEach(addBoard)
  }

  const addBoard = (obj) => {
    // push object เข้า task
    board.value.push({ ...obj })
  }

  const updateBoard = (updatedBoard) => {
    board.value = board.value.map((todo) => {
      //จะสร้าง object ใหม่ที่รวม properties ของ todo และ updatedTask เข้าด้วยกัน
      return todo.id === updatedBoard.id ? { ...todo, ...updatedBoard } : todo
    })
  }

  const removeBoards = (removeId) => {
    board.value.splice(
      board.value.findIndex((todo) => todo.id === removeId),
      1
    )
  }

  const clearBoard = () => {
    return (board.value = [])
  }
  return {
    getBoards,
    addBoards,
    addBoard,
    updateBoard,
    removeBoards,
    clearBoard,
    navBoard,
    boardName,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBoardStore, import.meta.hot))
}
