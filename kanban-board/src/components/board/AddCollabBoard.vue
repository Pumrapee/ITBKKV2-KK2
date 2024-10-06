<script setup>
import { ref, defineProps, defineEmits, computed } from "vue"
import { useAuthStore } from "@/stores/loginStore"
import { jwtDecode } from "jwt-decode"
import { useRoute } from "vue-router"

const props = defineProps({
  showModal: Boolean,
})
const emits = defineEmits(["addCollab", "closeModal"])
const myUser = useAuthStore()

const newCollab = ref({
  email: "",
  access_right: "READ",
})
const errorTask = ref({
  email: "",
})

const addCollab = async () => {
  emits("addCollab", newCollab)
}

const closeModal = () => {
  emits("closeModal")
}

const changeCollab = computed(() => {
  const ownerEmail = jwtDecode(myUser.token)

  if (ownerEmail.email === newCollab.value.email) {
    errorTask.value.email = "It is Owner email"
  } else {
    errorTask.value.email = ""
  }

  return (
    newCollab.value.email?.trim() === "" ||
    newCollab.value.email?.length > 50 ||
    ownerEmail.email === newCollab.value.email
  )
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="itbkk-modal-task bg-white p-6 rounded-lg w-12/12 max-w-3xl">
        <div class="flex justify-between items-center mb-4 border-b-2">
          <h2 class="text-2xl font-bold text-blue-400 mb-2">
            Add Collaborator
          </h2>
        </div>
        <div class="mb-4 flex">
          <div>
            <label for="email" class="block text-blue-400 font-bold mb-2"
              >Collaborator e-mail</label
            >
            <input
              type="email"
              id="email"
              placeholder="Enter Email here..."
              v-model="newCollab.email"
              class="itbkk-title border border-blue-400 rounded-lg py-2 px-3 input input-ghost w-96"
            />
            <p class="text-red-400 pt-2 pl-2">
              {{ errorTask.email }}
            </p>
          </div>
          <div>
            <label for="title" class="block text-blue-400 font-bold mb-2 ml-5"
              >Access Right</label
            >

            <select
              class="pl-10 border-2 rounded-lg h-10 pr-5 w-full ml-3 border-blue-400"
              v-model="newCollab.access_right"
            >
              <option value="READ">READ</option>
              <option value="WRITE">WRITE</option>
            </select>
          </div>
        </div>
        <div class="flex justify-end mt-4">
          <button
            class="itbkk-button-confirm btn mr-3 bg-green-400 disabled:bg-green-200"
            @click="addCollab(newTask)"
            :disabled="changeCollab"
          >
            Add
          </button>
          <button class="itbkk-button-cancel btn" @click="closeModal">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
