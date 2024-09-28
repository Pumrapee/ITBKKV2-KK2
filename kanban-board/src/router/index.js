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
import { getToken, getItems } from "@/libs/fetchUtils"
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

    console.log(response.status)
    console.log(typeof response === "object")
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

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = sessionStorage.getItem("token")

  if (!authStore.isAuthenticated && !token && to.name !== "login") {
    next({ name: "login" })
  } else {
    if (token) {
      authStore.isAuthenticated = true
      getToken()
    }
    next()
  }
})

export default router
