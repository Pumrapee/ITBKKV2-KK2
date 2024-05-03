import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddTask from "@/components/AddTask.vue"
import EditTask from "@/components/EditTask.vue"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/task",
      name: "task",
      component: HomeView,
      children: [
        {
          path: "add",
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
      path: "/",
      redirect: "/task",
    },
    {
      path: "/:catchAll(.*)",
      name: "404",
      redirect: "/task",
    },
  ],
})

export default router
