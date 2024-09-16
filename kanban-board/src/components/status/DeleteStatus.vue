<script setup>
import { defineProps , defineEmits , computed , watch } from "vue"
import { useStatusStore } from "../../stores/statusStore"
import { useLimitStore } from "../../stores/limitStore"
import { useTaskStore } from "@/stores/taskStore"
import { ref } from "vue"

const props = defineProps({
    showDeleteStatus: Boolean,
    showTransferModal: Boolean,
  detailStatus: Object,
})
const emits = defineEmits(["closeModal" , "closeDeleteStatus"])

const myStatus = useStatusStore()
const myLimit = useLimitStore()
const myTask = useTaskStore()
const selectedStatus = ref()
const errorAlert = ref({ status : ""})
const filteredStatus = computed(() => {
  return myStatus
    .getStatus()
    .filter((status) => status.id !== props.detailStatus.id)
})

const confirmDelete = async () => {
    emits("closeDeleteStatus")
}

const transferTasks = async() =>{
    emits("closeDeleteStatus",selectedStatus.value , filteredStatus.id)
    selectedStatus.value = ""
}

const closeModals = () => {
  selectedStatus.value = ""
  emits("closeModal")
}

//Disable ปุ่ม
const deleteButton = computed(() => {
    if (myLimit.getLimit().taskLimitEnabled) {
    const selected = filteredStatus.value.find(status => status.id === selectedStatus.value)
    const statusCount = myTask.matchStatus(selected?.name)?.length
    const statusLimit = myLimit.getLimit().maxTasksPerStatus

    if (selected?.name !== "No Status" && selected?.name !== "Done" && statusCount >= statusLimit) {
      errorAlert.value.status = `Cannot transfer to ${selected?.name} status since it will exceed the limit. Please choose another status to transfer to.`
      return true
    } else {
      errorAlert.value.status = ""
      return false
    }
  }

  
  errorAlert.value.status = ""
  return false
})

</script>

<template>
    <!-- Modal Delete -->
    <div v-if="showDeleteStatus" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the <br />
            <span class="text-blue-400">{{ detailStatus.name }}</span>
            <span> status?</span>
          </p>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            class="itbkk-button-confirm btn mr-4 bg-red-500 text-white"
            @click="confirmDelete()"
          >
            Confirm
          </button>
          <button class="itbkk-button-cancel btn" @click="closeModals">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>

    <!-- Modal Transfer -->
    <div v-if="showTransferModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/50">
      <div class="bg-white p-10 rounded-lg w-2/5">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            There are <span class="text-blue-400">{{ detailStatus.countTask }}</span> tasks in <span class="text-blue-400">"{{ detailStatus.name }}"</span> status.</br>  In order to delete this status, the system must transfer tasks in this status to existing status. Transfer tasks to 
            [<span class="text-blue-400" v-for="status in filteredStatus" :key="status.id" :value="status.id">{{ status.name }} ,  </span> ]
          </p>
        </div>
        <div class="mt-4">
          <label for="transferTo" class="block text-sm font-medium text-gray-700">Transfer to:</label>
          <select v-model="selectedStatus" id="transferTo" name="transferTo" class="itbkk-status mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
            <option v-for="status in filteredStatus" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>

        <div>
          <p class="text-red-400" v-if="errorAlert.status">
         {{ errorAlert.status }}
          </p>
        </div>

        <div class="mt-4 flex justify-end">
          <button class="itbkk-button-confirm btn mr-4 bg-blue-500 text-white" @click="transferTasks()" :disabled="deleteButton">Transfer</button>
          <button class="itbkk-button-cancel btn" @click="closeModals">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
