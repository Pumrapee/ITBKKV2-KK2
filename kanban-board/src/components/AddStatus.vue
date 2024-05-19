<script setup>
import { defineProps, defineEmits, ref, computed } from "vue"
import { useStatusStore } from "@/stores/statusStore"
import { addItem } from "@/libs/fetchUtils"

const { showAddStatus } = defineProps({
  showAddStatus: Boolean,
})

const emits = defineEmits(["closeAddStatus", "closeCancelStatus"])

const newStatus = ref({
  name: "",
  description: "",
  color: "#FFFFFF",
})

const errorStatus = ref({
  name: "",
  description: "",
})

const myStatus = useStatusStore()
const saveStatus = async () => {
  // Trim
  newStatus.value.name = newStatus.value.name?.trim()
  newStatus.value.description = newStatus.value.description?.trim()

  // Replace empty strings with null
  if (newStatus.value.name === "") {
    newStatus.value.name = null
  }
  if (newStatus.value.description === "") {
    newStatus.value.description = null
  }

  const { newTask, statusCode } = await addItem(
    `${import.meta.env.VITE_API_URL}statuses`,
    newStatus.value
  )

  if (statusCode === 201) {
    myStatus.addOneStatus(
      newTask.id,
      newTask.name,
      newTask.description,
      newTask.color
    )

    newStatus.value.name = ""
    newStatus.value.description = ""
    newStatus.value.color = "#FFFFFF"
    emits("closeAddStatus", statusCode)
  }
  if (statusCode === 400 || statusCode === 500) {
    emits("closeAddStatus", statusCode)
  }
}

const cancelStatus = () => {
  newStatus.value.name = ""
  newStatus.value.description = ""
  newStatus.value.color = "#FFFFFF"
  emits("closeCancelStatus")
}

const changeStatus = computed(() => {
  const trimmedNameLength = newStatus.value.name?.length > 50
  const trimmedNameEmpthy = newStatus.value.name?.length === 0
  const trimmedDescriptionLength = newStatus.value.description?.length > 200

  const nameUnique = myStatus.getStatus().some((listStatus) => {
    return listStatus.name.toLowerCase() === newStatus.value.name.toLowerCase()
  })

  if (trimmedNameLength) {
    errorStatus.value.name = "Name exceeds the limit of 50 characters."
  } else if (trimmedNameEmpthy) {
    errorStatus.value.name = "Name is require."
  } else if (nameUnique) {
    errorStatus.value.name = "Status name must be uniques, please choose another name."
  } else {
    errorStatus.value.name = ""
  }

  if (trimmedDescriptionLength) {
    errorStatus.value.description =
      "Description exceeds the limit of 200 characters."
  } else {
    errorStatus.value.description = ""
  }

  return (
    trimmedNameLength ||
    trimmedDescriptionLength ||
    trimmedNameEmpthy ||
    nameUnique
  )
})
</script>

<template>
  <!-- Modal window -->
  <div
    v-if="showAddStatus"
    class="fixed z-10 inset-0 overflow-y-auto flex items-center justify-center bg-black/[.15]"
  >
    <div class="bg-white p-6 rounded-lg w-11/12 max-w-xl">
      <h2 class="text-2xl font-bold text-blue-400 mb-4 border-b-2">
        Add Status
      </h2>

      <div class="itbkk-modal-status mb-4">
        <label for="name" class="block text-blue-400 font-bold mb-2"
          >Name</label
        >
        <input
          type="text"
          v-model="newStatus.name"
          max="50"
          id="name"
          class="itbkk-status-name w-full border border-blue-400 rounded-lg py-2 px-3 input input-ghost"
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

      <div>
        <label for="description" class="block text-blue-400 font-bold mb-2"
          >Description</label
        >
        <textarea
          id="description"
          v-model="newStatus.description"
          class="itbkk-status-description w-full border border-blue-400 rounded-lg py-3 px-3 h-44 textarea textarea-ghost"
        ></textarea>
        <div class="flex justify-between items-center">
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
          @click="saveStatus"
          class="itbkk-button-confirm btn bg-green-400 text-white rounded-lg py-2 px-4 mr-2 disabled:bg-green-200"
          :disabled="changeStatus"
        >
          Save
        </button>
        <button
          @click="cancelStatus"
          class="itbkk-button-cancel btn bg-gray-300 text-gray-700 rounded-lg py-2 px-4"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
