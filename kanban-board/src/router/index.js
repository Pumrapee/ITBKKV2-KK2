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
import { getToken } from "@/libs/fetchUtils"

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
      children: [
        {
          path: "task",
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
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = localStorage.getItem("token")

  if (!authStore.isAuthenticated && !token && to.name !== "login") {
    next({ name: "login" })
  } else {
    if (token) {
      authStore.isAuthenticated = true
      getToken()
      // authStore.token = token
    }
    next()
  }
})

export default router
