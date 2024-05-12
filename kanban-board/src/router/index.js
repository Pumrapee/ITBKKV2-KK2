import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddTask from "@/components/AddTask.vue"
import EditTask from "@/components/EditTask.vue"
import StatusView from "@/views/StatusView.vue"
import AddStatus from "@/components/AddStatus.vue"
import EditStatus from "@/components/EditStatus.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/task",
      name: "task",
      component: HomeView,
      children: [
        {
          path: "add",
          name: "add",
          component: AddTask,
        },
        {
          path: ":id/edit",
          name: "edit",
          component: EditTask,
        },
      ],
    },
    {
      path: "/status/manage",
      name: "tableStatus",
      component: StatusView,
      children: [
        {
          path: "/status/add",
          name: "AddStatus",
          component: AddStatus,
        },
        {
          path: "/status/:id/edit",
          name: "EditStatus",
          component: EditStatus,
        },
      ],
    },

    {
      path: "/",
      redirect: {name: "task"},
    },
    {
      path: "/:catchAll(.*)",
      name: "404",
      redirect: {name: "task"},
    },
  ],
})

export default router
