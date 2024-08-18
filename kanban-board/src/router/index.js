import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddEditTask from "@/components/task/AddEditTask.vue"
import StatusView from "@/views/StatusView.vue"
import NotFoundView from "../views/NotFoundView.vue"
import AddEditStatus from "@/components/status/AddEditStatus.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
      component: StatusView,
      children: [
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

export default router
