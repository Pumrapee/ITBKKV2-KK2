<script setup>
import { ref, defineProps, defineEmits, onMounted } from "vue"
const { showModal, task } = defineProps({
  showModal: Boolean,
  task: Object,
})
defineEmits(["closeModal"])

const reformat = (status) => {
  const statusMap = {
    NO_STATUS: "No Status",
    TO_DO: "To Do",
    DOING: "Doing",
    DONE: "Done",
  }
  return statusMap[status] || status // ถ้าไม่มีค่าใน statusMap ให้ใช้ค่าเดิม
}
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="w-4/5 grid grid-rows-6 grid-cols-4 gap-2 bg-white p-10 rounded-lg"
      >
        <input
          type="text"
          className="itbkk-title input pl-2 col-span-4 font-semibold text-3xl text-blue-400 rounded-lg "
          v-model="task.title"
          placeholder="Enter Title here..."
        />

        <div class="border-2 border-blue-400 row-span-4 col-span-3 rounded-lg">
          <p class="p-5 font-bold text-blue-400">Description</p>
          <textarea
            class="itbkk-description textarea textarea-ghost p-4 h-3/4 w-11/12 ml-9"
            v-model="task.description"
            placeholder="No Description Provided"
            :class="
              task.description ? 'bg-white text-black' : 'italic text-gray-500'
            "
          >
 {{ task.description }}  </textarea
          >
          <!-- <p class="itbkk-description">No Description Provided</p> -->
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 row-start-2 row-end-4 rounded-lg"
        >
          <p class="p-3 font-bold text-blue-400">Assignees</p>
          <textarea
            v-model="task.assignees"
            class="itbkk-assignees pl-5 textarea textarea-ghost h-3/5 w-11/12 ml-2"
            :placeholder="task.assignees ? '' : 'Unassigned'"
            :class="
              task.assignees ? 'bg-white text-black' : 'italic text-gray-500'
            "
          ></textarea>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 p-2 row-start-4 row-end-5 rounded-lg"
        >
          <label for="status" class="p-2 font-bold text-blue-400">Status</label>
          <select
            v-model="task.status"
            class="itbkk-status pl-5 border-2 rounded-md h-10 pr-5"
          >
            <option value="NO_STATUS">No Status</option>
            <option value="TO_DO">To Do</option>
            <option value="DOING">Doing</option>
            <option value="DONE">Done</option>
          </select>
        </div>

        <div class="col-start-4 rounded-lg">
          <p class="itbkk-timezone pl-3 font-semibold text-sm text-blue-400">
            Time Zone : {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
          </p>
          <p class="itbkk-created-on pl-3 font-semibold text-sm text-blue-400">
            Created On :

            {{ new Date(task.createdOn).toLocaleString("en-GB") }}
          </p>
          <p class="itbkk-updated-on pl-3 font-semibold text-sm text-blue-400">
            Updated On :
            {{ new Date(task.updatedOn).toLocaleString("en-GB") }}
          </p>
        </div>

        <div class="col-span-4 place-self-end rounded-lg">
          <button class="btn m-3 bg-green-400 text-white">Save</button>
          <button class="btn" @click="$emit('closeModal', false)">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
