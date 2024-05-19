<script setup>
import { defineProps, defineEmits, ref , computed } from "vue"
import { deleteItemById , getItems } from "@/libs/fetchUtils"
import { useStatusStore } from "../stores/statusStore"
import { useTaskStore } from "../stores/taskStore"
import { deleteItemByIdToNewId } from "@/libs/fetchUtils"


const props = defineProps({
  showDeleteStatus: Boolean,
  showTransferModal: Boolean,
  deatailStatus: Object
})
const myStatus = useStatusStore()
const myTask = useTaskStore()
const selectedStatus = ref()

const filteredStatus = computed(() => {
  return myStatus.getStatus().filter(status => status.id !== props.deatailStatus.id)
})

const emits = defineEmits(["closeDeleteStatus", "closeCancle" , "closeTransferStatus"])

const confirmDelete = async () => {
    const deleteItem = await deleteItemById(
      `${import.meta.env.VITE_API_URL}statuses`,
      props.deatailStatus.id
    )

    if (deleteItem === 200) {
      myStatus.removeStatus(props.deatailStatus.id)
      emits("closeDeleteStatus",deleteItem)
    }

    if (deleteItem === 404) {     
      myStatus.removeStatus(props.deatailStatus.id)  
        emits("closeDeleteStatus",deleteItem)

    }
}

const transferTasks = async() =>{
    const newStatus = await deleteItemByIdToNewId(`${import.meta.env.VITE_API_URL}statuses`,props.deatailStatus.id,selectedStatus.value)
    
    if(newStatus === 200){
        myStatus.removeStatus(props.deatailStatus.id)
        const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
        myTask.clearTask()
        myTask.addTasks(listTasks)
        emits("closeTransferStatus",newStatus)
    }
    if(newStatus === 404){
        myStatus.removeStatus(filteredStatus.id)
        const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
        myTask.clearTask()
        myTask.addTasks(listTasks)
        emits("closeTransferStatus",newStatus)
    }
}



</script>

<template>
  <!-- Modal Delete -->
  <div v-if="showDeleteStatus" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the <br />
            <span class="text-blue-400">{{ deatailStatus.name }}</span>
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
          <button class="itbkk-button-cancel btn" @click="$emit('closeCancle')">
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
            There are tasks associated with the "{{ deatailStatus.name }}" status.</br> Do you want to transfer them?
          </p>
        </div>

        <div class="mt-4">
          <label for="transferTo" class="block text-sm font-medium text-gray-700">Transfer to:</label>
          <select v-model="selectedStatus" id="transferTo" name="transferTo" class="itbkk-status mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
            <option v-for="status in filteredStatus" :key="status.id" :value="status.id">{{ status.name }}</option>
          </select>
        </div>

        <div class="mt-4 flex justify-end">
          <button class="itbkk-button-confirm btn mr-4 bg-blue-500 text-white" @click="transferTasks()">Transfer</button>
          <button class="itbkk-button-cancel btn" @click="$emit('closeCancle')">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
