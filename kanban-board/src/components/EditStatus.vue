<script setup>
import { defineProps, defineEmits, computed } from "vue"
import { watch, ref } from "vue"
import { editItem, getItems } from "@/libs/fetchUtils"
import { useStatusStore } from "@/stores/statusStore"
import { useTaskStore } from "@/stores/taskStore"

const props = defineProps({
  showEditStatus: Boolean,
  taskStatus: Object,
})
const emits = defineEmits(["closeEditStatus", "closeCancleStatus"])

const newStatus = ref({})
const myStatus = useStatusStore()
const myTask = useTaskStore()
const errorStatus = ref({
  name: "",
  description: "",
})

const changeStatus = computed(() => {
  const trimAndCheckNull = (value) => {
    if (value === null) return null
    else {
      return value?.trim().length === 0 ? null : value?.trim()
    }
  }
  const oldStatus = {
    name: props.taskStatus.name,
    description: props.taskStatus.description,
    color: props.taskStatus.color,
  }

  const newName = trimAndCheckNull(newStatus.value.name)
  const newDescription = trimAndCheckNull(newStatus.value.description)

  newStatus.value.name?.length > 50
    ? (errorStatus.value.name = "Name exceeds the limit of 50 characters.")
    : newStatus.value.name?.length === 0
    ? (errorStatus.value.name = "Name is require.")
    : (errorStatus.value.name = "")

  newStatus.value.description?.length > 200
    ? (errorStatus.value.description =
        "Description exceeds the limit of 200 characters.")
    : (errorStatus.value.description = "")

  return (
    (oldStatus.name === newName &&
      oldStatus.description === newDescription &&
      oldStatus.color === newStatus.value.color) ||
    newName === null
  )
})

const editStatusSave = async (status) => {
  const editStatus = { ...status }
  editStatus.name = editStatus.name?.trim()
  editStatus.description = editStatus.description?.trim()

  if (editStatus.name === "") {
    editStatus.name = null
  }
  if (editStatus.description === "") {
    editStatus.description = null
  }

  const { editedItem, statusCode } = await editItem(
    `${import.meta.env.VITE_API_URL}statuses`,
    editStatus.id,
    {
      name: editStatus.name,
      description: editStatus.description,
      color: editStatus.color,
    }
  )

  if (statusCode === 200) {
    myStatus.updateStatus(
      editedItem.id,
      editedItem.name,
      editedItem.description,
      editedItem.color
    )
    const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
    myTask.clearTask()
    myTask.addTasks(listTasks)

    emits("closeEditStatus", statusCode)
  }

  if (statusCode === 400) {
    emits("closeEditStatus", statusCode)
  }

  if (statusCode === 404) {
    myStatus.removeStatus(editedItem.id)
    emits("closeEditStatus", statusCode)
  }
}

watch(props, () => {
  if (props.showEditStatus) {
    Object.assign(newStatus.value, props.taskStatus)
  }
})
</script>

<template>
  <!-- Modal window -->
  <div
    v-if="showEditStatus"
    class="fixed z-10 inset-0 overflow-y-auto flex items-center justify-center bg-black/[.15]"
  >
    <div class="bg-white p-6 rounded-lg w-11/12 max-w-xl">
      <h2 class="text-2xl font-bold text-blue-400 mb-4 border-b-2">
        Edit Status
      </h2>

      <div class="mb-4">
        <label for="name" class="block text-blue-400 font-bold mb-2"
          >Name</label
        >
        <input
          type="text"
          id="name"
          v-model="newStatus.name"
          class="w-full border border-blue-400 rounded-lg py-2 px-3 input input-ghost"
          placeholder="Enter Name here..."
        />
        <p class="text-red-400">
          {{ errorStatus.name }}
        </p>
      </div>

      <div class="mb-6">
        <label for="description" class="block text-blue-400 font-bold mb-2"
          >Description</label
        >
        <textarea
          id="description"
          v-model="newStatus.description"
          class="w-full border border-blue-400 rounded-lg py-3 px-3 h-44 textarea textarea-ghost"
          :class="
            newStatus.description
              ? 'bg-white text-black'
              : 'italic text-gray-500'
          "
          placeholder="No Description Provided"
        ></textarea>
        <p class="text-red-400">
          {{ errorStatus.description }}
        </p>
      </div>

      <div class="mb-6 flex">
        <label for="color" class="block text-blue-400 font-bold mb-2"
          >Color:</label
        >
        <input v-model="newStatus.color" class="ml-3" type="color" />
      </div>

      <div class="flex justify-end">
        <button
          @click="editStatusSave(newStatus)"
          class="bg-green-400 text-white rounded-lg py-2 px-4 mr-2 disabled:bg-green-200"
          :disabled="changeStatus"
        >
          Save
        </button>
        <button
          @click="$emit('closeCancleStatus')"
          class="bg-gray-300 text-gray-700 rounded-lg py-2 px-4"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
