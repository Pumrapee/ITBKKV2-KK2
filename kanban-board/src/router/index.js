import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddEditTask from "@/components/task/AddEditTask.vue"
import StatusView from "@/views/StatusView.vue"
import NotFoundView from "../views/NotFoundView.vue"
import AddEditStatus from "@/components/status/AddEditStatus.vue"
import LoginPage from "@/components/LoginPage.vue"
import BoardView from "@/views/BoardView.vue"
import AddBoard from "@/components/board/AddBoard.vue"
import { useAuthStore } from "@/stores/loginStore"
import { getToken, getItems, getBoardItems } from "@/libs/fetchUtils"
import ForbiddenView from "@/views/ForbiddenView.vue"

const checkBoardAccess = async (to, from, next) => {
  const { id: boardId } = to.params
  try {
    const response = await getItems(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId}`
    )

    if (response.status === 403) {
      next({ name: "forbidden" })
    } else {
      next()
    }

  } catch (error) {
    next({ name: "TaskNotFound" })
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: LoginPage,
    },
    {
      path: "/board",
      name: "board",
      component: BoardView,
      children: [
        {
          path: "add",
          name: "addBoard",
          component: AddBoard,
        },
      ],
    },

    {
      path: "/board/:id",
      name: "task",
      component: HomeView,
      beforeEnter: checkBoardAccess,
      children: [
        {
          path: "task/:taskId",
          name: "detailTask",
          component: AddEditTask,
        },
        {
          path: "task/:taskId/edit",
          name: "editTask",
          component: AddEditTask,
        },
        {
          path: "task/add",
          name: "addTask",
          component: AddEditTask,
        },
      ],
    },
    {
      path: "/board/:id/status",
      name: "tableStatus",
      component: StatusView,
      beforeEnter: checkBoardAccess,
      children: [
        {
          path: "add",
          name: "AddStatus",
          component: AddEditStatus,
        },
        {
          path: ":statusId/edit",
          name: "EditStatus",
          component: AddEditStatus,
        },
      ],
    },
    {
      path: "/",
      redirect: { name: "login" },
    },
    {
      path: "/notfound",
      name: "TaskNotFound",
      component: NotFoundView,
    },
    {
      path: "/:pathMatch(.*)*",
      name: "notFound",
      component: NotFoundView,
    },
    {
      path: "/forbidden",
      name: "forbidden",
      component: ForbiddenView,
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const token = localStorage.getItem("token")

  // ตรวจสอบเส้นทาง task หรือ tableStatus ที่มี id
  if (
    (to.name === "task" && to.params.id) ||
    (to.name === "tableStatus" && to.params.id)
  ) {
    const boardId = to.params.id
    console.log(boardId)

    // เรียก API เพื่อเช็คว่า board เป็น public หรือ private
    const board = await getItems(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId}`
    )

    console.log(board)
    console.log(board.status)
    console.log(board.visibility)
    if (board.status === 403) {
      return next({ name: "forbidden" })
    }

    if (board) {
      // ถ้าเป็น public สามารถเข้าถึงได้โดยไม่ต้องมี token
      if (board.visibility === "PUBLIC") {
        return next()
      }

      // ถ้าเป็น private และไม่มี token ให้ไปหน้า login
      if (board.visibility === "PRIVATE" && !token) {
        return next({ name: "login" })
      }
    }
  }

  if ((to.name === "board" && !token) || (to.name === "board" && token === "null")) {
    return next({ name: "login" })
  }

  // ถ้ามี token ให้ทำการ authenticate
  if (token) {
    authStore.isAuthenticated = true
    getToken()
  }

  // ถ้าไม่มีปัญหาใด ๆ ให้ไปต่อใน route
  next()
})

export default router
