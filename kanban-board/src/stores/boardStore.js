import { ref } from "vue"
import { defineStore, acceptHMRUpdate } from "pinia"

export const useBoardStore = defineStore("board", () => {
  // state
  //Board
  const board = ref([])
  const boardCollab = ref([])
  const navBoard = ref(false)
  const navBarCollab = ref(false)
  const boardName = ref("")
  const collaborator = ref([])

  // view
  const getBoards = () => {
    return board.value
  }

  const getBoardCollab = () => {
    return boardCollab.value
  }

  const getCollabs = () => {
    return collaborator.value
  }

  // actions
  const addBoards = (newBoard) => {
    newBoard.forEach(addBoard)
  }

  const addBoard = (obj) => {
    board.value.push({ ...obj })
  }

  const addBoardsCollab = (newBoardCollab) => {
    newBoardCollab.forEach(addBoardCollab)
  }

  const addBoardCollab = (obj) => {
    boardCollab.value.push({ ...obj })
  }

  const addCollabs = (newCollab) => {
    newCollab.forEach(addCollab)
  }

  const addCollab = (obj) => {
    collaborator.value.push({ ...obj })
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

  return {
    getBoards,
    addBoards,
    addBoard,
    updateBoard,
    removeBoards,
    clearBoard,
    navBoard,
    navBarCollab,
    boardName,
    addCollabs,
    addCollab,
    getCollabs,
    removeCollab,
    removeBoardCollab,
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
