import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useBoardStore = defineStore("board", () => {
  // state
  const board = ref([])
  const navBoard = ref(false)
  const boardName = ref("")
  const boardOwnerId = ref(null) // เพิ่มฟิลด์สำหรับเก็บ ID เจ้าของบอร์ด

  // view
  const getBoards = () => {
    return board.value
  }

  const getBoardOwnerId = () => {
    return boardOwnerId.value // ฟังก์ชันเพื่อดึง ownerId ของบอร์ด
  }

  // actions
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

  const setBoardOwner = (ownerId) => {
    boardOwnerId.value = ownerId // ฟังก์ชันสำหรับตั้งค่า ownerId ของบอร์ด
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
    getBoardOwnerId, // เพิ่มการคืนค่า getBoardOwnerId
    setBoardOwner, // เพิ่มฟังก์ชันสำหรับตั้งค่าเจ้าของบอร์ด
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBoardStore, import.meta.hot))
}
