import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useBoardStore = defineStore("board", () => {
  // state
  //Board
  const board = ref([])
  const boardCollab = ref([])
  const navBoard = ref(false)
  const boardName = ref("")
  const boardOwnerId = ref(null) // เพิ่มฟิลด์สำหรับเก็บ ID เจ้าของบอร์ด
  const collaborator = ref([])

  // view
  const getBoards = () => {
    return board.value
  }

  const getBoardCollab = () => {
    return boardCollab.value
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

  const addBoardsCollab = (newBoardCollab) => {
    // loop array
    newBoardCollab.forEach(addBoardCollab)
  }

  const addBoardCollab = (obj) => {
    // push object เข้า task
    boardCollab.value.push({ ...obj })
  }

  const updateBoard = (updatedBoard) => {
    board.value = board.value.map((todo) => {
      //จะสร้าง object ใหม่ที่รวม properties ของ todo และ updatedTask เข้าด้วยกัน
      return todo.id === updatedBoard.id ? { ...todo, ...updatedBoard } : todo
    })
  }

  const updateVisibility = (boardId, updateVis) => {
    board.value = board.value.map((todo) => {
      return todo.id === boardId ? { ...todo, ...updateVis } : todo
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

  const removeCollab = (removeId) => {
    collaborator.value.splice(
      collaborator.value.findIndex((todo) => todo.oid === removeId),
      1
    )
  }

  const removeBoardCollab = (removeId) => {
    boardCollab.value.splice(
      boardCollab.value.findIndex((todo) => todo.owner.oid === removeId),
      1
    )
  }

  const clearBoard = () => {
    return (board.value = [])
  }

  const clearBoardCollab = () => {
    return (boardCollab.value = [])
  }

  const clearCollaborator = () => {
    return (collaborator.value = [])
  }

  const getCollabs = () => {
    return collaborator.value
  }

  const addCollabs = (newCollab) => {
    // loop array
    newCollab.forEach(addCollab)
  }

  const addCollab = (obj) => {
    // push object เข้า task
    collaborator.value.push({ ...obj })
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
    addCollabs,
    addCollab,
    getCollabs,
    removeCollab,
    removeBoardCollab,
    getBoardOwnerId, // เพิ่มการคืนค่า getBoardOwnerId
    setBoardOwner, // เพิ่มฟังก์ชันสำหรับตั้งค่าเจ้าของบอร์ด
    updateVisibility,
    addBoardsCollab,
    getBoardCollab,
    clearBoardCollab,
    clearCollaborator,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBoardStore, import.meta.hot))
}
