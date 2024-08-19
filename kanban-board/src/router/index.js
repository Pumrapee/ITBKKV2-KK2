import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddEditTask from "@/components/task/AddEditTask.vue"
import StatusView from "@/views/StatusView.vue"
import NotFoundView from "../views/NotFoundView.vue"
import AddEditStatus from "@/components/status/AddEditStatus.vue"
import { getItemById } from "@/libs/fetchUtils"
import LoginPage from "@/components/LoginPage.vue"
import { useAuthStore } from "@/stores/loginStore"
        
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "login",
      component: LoginPage,
    },
    {
      path: "/task",
      name: "task",
      component: HomeView,
      children: [
        {
          path: ":id",
          name: "detailTask",
          component: AddEditTask,
        },
        {
          path: ":id/edit",
          name: "editTask",
          component: AddEditTask,
        },
        {
          path: "add",
          name: "addTask",
          component: AddEditTask,
        },
      ],
    },
    {
      path: "/status",
      name: "tableStatus",
      redirect: { name: "homeStatus" },
      component: StatusView,
      children: [
        {
          path: "manage",
          name: "homeStatus",
          component: StatusView,
        },
        {
          path: "add",
          name: "AddStatus",
          component: AddEditStatus,
        },
        {
          path: ":id/edit",
          name: "EditStatus",
          component: AddEditStatus,
        },
      ],
    },
    {
      path: "/",
      redirect: { name: "task" },
    },
    {
      path: "/:catchAll(.*)",
      name: "notFound",
      component: NotFoundView,
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: "login" }); 
  } else {
    next()
  }
})

export default router
