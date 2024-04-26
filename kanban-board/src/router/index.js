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
      children: [{ path: "/task/:id", component: AddEditTask }],
    },
    {
      path: "/",
      redirect: "/task",
    },
  ],
})

export default router
