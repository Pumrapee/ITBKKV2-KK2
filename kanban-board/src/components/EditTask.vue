<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { editItem } from "../libs/fetchUtils"
import { useTaskStore } from "@/stores/taskStore"

const { showModal, task } = defineProps({
  showModal: Boolean,
  task: Object,
})
const emits = defineEmits(["closeModal"])
const editPass = ref(false)
let oldTask;

const myTask = useTaskStore()
const editSave = async (task) => {
  const editedTask = {...task}
  editedTask.title = editedTask.title.trim()
  editedTask.description = editedTask.description.trim()
  editedTask.assignees = editedTask.assignees.trim()
  const editedItem = await editItem(
    import.meta.env.VITE_BASE_URL,
    editedTask.id, 
    {
      title: editedTask.title, 
      description: editedTask.description, 
      assignees: editedTask.assignees, 
      status: editedTask.status
    }
  )

  myTask.updateTask(
    editedTask.id,
    editedTask.title,
    editedTask.description,
    editedTask.assignees,
    editedTask.status,
    editedTask.createdTime,
    editedTask.updatedTime
  )

  emits("closeModal")
  editPass.value = true
}

const changeTask = computed(() => {})
</script>

<template>
  <!-- Alert Pass Edit-->
  <div v-if="editPass" class="flex justify-center mt-3">
    <div role="alert" class="alert alert-success w-2/3">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6"
        fill="none"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <span>The task has been updated</span>
      <button @click="editPass = false">X</button>
    </div>
  </div>

  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="grid grid-rows-6 grid-cols-4 gap-2 bg-white p-10 rounded-lg w-4/5"
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
            v-model="task.description"
            class="itbkk-description textarea textarea-ghost p-4 h-3/5 w-11/12 ml-9"
            :class="
              task.description ? 'bg-white text-black' : 'italic text-gray-500'
            "
            placeholder="No Description Provided"
          ></textarea>
        </div>

        <div
          class="border-2 border-blue-400 col-start-4 row-start-2 row-end-4 rounded-lg"
        >
          <p class="p-3 font-bold text-blue-400">Assignees</p>
          <textarea
            v-model="task.assignees"
            class="itbkk-assignees pl-5 textarea textarea-ghost h-5/5 w-11/12 ml-2"
            :class="
              task.assignees ? 'bg-white text-black' : 'italic text-gray-500'
            "
            placeholder="Unassigned"
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
          <button
            class="btn mr-3 bg-green-400 disabled:bg-green-200"
            @click="editSave(task)"
            :disabled="changeTask"
          >
            Save
          </button>
          <button class="btn" @click="$emit('closeModal')">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
