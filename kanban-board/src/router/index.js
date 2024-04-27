import { createRouter, createWebHistory } from "vue-router"
import HomeView from "@/views/HomeView.vue"
import AddEditTask from "@/components/AddEditTask.vue"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/task",
      name: "task",
      component: HomeView,
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
