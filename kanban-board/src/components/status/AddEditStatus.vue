<script setup>
import { defineProps, defineEmits, watch, ref, computed } from "vue"
import { useStatusStore } from "@/stores/statusStore"

const props = defineProps({
  showModal: Boolean,
  taskStatus: Object,
  editModeModal: Boolean,
})
const emits = defineEmits(["closeModal", "saveAddEdit"])

const newStatus = ref({})
const myStatus = useStatusStore()
const errorStatus = ref({
  name: "",
  description: "",
})

const closeModal = () => {
  emits("closeModal")
}

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

  const isNameTooLong = newName?.length > 50
  const isNameEmpty = newName === null
  const isDescriptionTooLong = newDescription?.length > 200

  const nameNot = myStatus.getStatus().filter((list) => {
    return list.name?.toLowerCase() !== oldStatus.name?.toLowerCase()
  })

  const isNameUnique = nameNot.some(
    (listStatus) => listStatus.name?.toLowerCase() === newName?.toLowerCase()
  )

  // จัดการข้อความข้อผิดพลาด
    errorStatus.value.name = isNameTooLong
      ? "Name exceeds the limit of 50 characters."
      : isNameEmpty
      ? "Name is required."
      : isNameUnique
      ? "Status name must be unique, please choose another name."
      : ""

  errorStatus.value.description = isDescriptionTooLong
    ? "Description exceeds the limit of 200 characters."
    : ""

  // ตรวจสอบเงื่อนไขทั้งหมดรวมถึงการเปลี่ยนแปลงของข้อมูล
  const isUnchanged =
    oldStatus.name === newName &&
    oldStatus.description === newDescription &&
    oldStatus.color === newStatus.value.color

  return (
    isNameEmpty ||
    isNameTooLong ||
    isDescriptionTooLong ||
    isNameUnique ||
    newName === null ||
    isUnchanged
  )
})

const addEditSave = (newStatus) => {
  const editStatus = { ...newStatus }
  editStatus.name = editStatus.name?.trim()
  editStatus.description = editStatus.description?.trim()

  if (editStatus.name === "") {
    editStatus.name = null
  }
  if (editStatus.description === "") {
    editStatus.description = null
  }

  emits("saveAddEdit", newStatus)
}

watch(props, () => {
  if (props.showModal) {
    Object.assign(newStatus.value, props.taskStatus)
  }
})
</script>

<template>
  <!-- Modal window -->
  <div
    v-if="showModal"
    class="fixed z-20 inset-0 overflow-y-auto flex items-center justify-center bg-black/[.15]"
  >
    <div class="bg-white p-6 rounded-lg w-11/12 max-w-xl">
      <h2 class="text-2xl font-bold text-black mb-4 border-b-2">
        {{ editModeModal === true ? "Edit Status" : "Add Status" }}
      </h2>

      <div class="itbkk-modal-status mb-4">
        <label for="name" class="block text-black font-bold mb-2"
          >Name</label
        >
        <input
          type="text"
          id="name"
          v-model="newStatus.name"
          class="itbkk-status-name w-full border border-black rounded-lg py-2 px-3 input input-ghost"
          placeholder="Enter Name here..."
        />
        <div class="flex justify-between items-center">
          <p class="text-red-400">
            {{ errorStatus.name }}
          </p>
          <p
            class="text-gray-300 pb-4 text-sm"
            :class="{
              'text-red-400':
                newStatus.name?.trim()?.length > 50 ||
                newStatus.name?.trim()?.length === 0,
            }"
          >
            {{ newStatus.name?.trim()?.length || 0 }}/50
          </p>
        </div>
      </div>

      <div class="mb-6">
        <label for="description" class="block text-black font-bold mb-2"
          >Description</label
        >
        <textarea
          id="description"
          v-model="newStatus.description"
          class="itbkk-status-description w-full border border-black rounded-lg py-3 px-3 h-44 textarea textarea-ghost"
          :class="
            newStatus.description
              ? 'bg-white text-black'
              : 'italic text-gray-500'
          "
          placeholder="No Description Provided"
        ></textarea>
        <div class="flex justify-between items-center mt-2">
          <p class="text-red-400">{{ errorStatus.description }}</p>
          <p
            class="text-gray-300 self-end text-sm pb-3"
            :class="{
              'text-red-400': newStatus.description?.trim()?.length > 200,
            }"
          >
            {{ newStatus.description?.trim()?.length || 0 }}/200
          </p>
        </div>
      </div>

      <div class="mb-6 flex">
        <label for="color" class="block text-blue-400 font-bold mb-2"
          >Color:</label
        >
        <input v-model="newStatus.color" class="ml-3" type="color" />
      </div>

      <div class="flex justify-end">
        <button
          @click="addEditSave(newStatus)"
          class="itbkk-button-confirm bg-green-400 text-white rounded-lg py-2 px-4 mr-2 disabled:bg-green-200"
          :disabled="changeStatus"
        >
          Save
        </button>
        <button
          @click="closeModal"
          class="itbkk-button-cancel bg-gray-300 text-gray-700 rounded-lg py-2 px-4"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
