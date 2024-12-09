<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { useAuthStore } from "@/stores/loginStore"
import { jwtDecode } from "jwt-decode"

const props = defineProps({
  showModal: Boolean,
  collabs: Object,
})
const emits = defineEmits(["addCollab", "closeModal"])
const myUser = useAuthStore()

const newCollab = ref({})
const errorTask = ref({
  email: "",
})

const addCollab = async () => {
  emits("addCollab", newCollab)
}

const closeModal = () => {
  newCollab.value.email = ""
  newCollab.value.accessRight = "READ"
  emits("closeModal")
}

const changeCollab = computed(() => {
  const ownerEmail = jwtDecode(myUser.token)

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  if (ownerEmail.email === newCollab.value.email) {
    errorTask.value.email =
      "Board owner cannot be collaborator of his/her own board"
  } else if (!emailPattern.test(newCollab.value.email)) {
    errorTask.value.email = "Invalid email format"
  } else {
    errorTask.value.email = ""
  }

  return (
    newCollab.value.email?.trim() === "" ||
    newCollab.value.email?.length > 50 ||
    ownerEmail.email === newCollab.value.email ||
    !emailPattern.test(newCollab.value.email)
  )
})

watch(props, () => {
  if (props.showModal) {
    Object.assign(newCollab.value, props.collabs)
  }
})
</script>

<template>
  <!-- Modal window -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div
      class="flex items-center justify-center min-h-screen bg-black/[.15] px-4"
    >
      <div
        class="itbkk-modal-alert bg-white p-6 rounded-lg w-full max-w-3xl sm:w-full md:w-11/12 lg:w-12/12 xl:w-5/12"
      >
        <div class="flex justify-between items-center mb-4 border-b-2">
          <h2 class="text-xl md:text-2xl font-bold text-black mb-2">
            Add Collaborator
          </h2>
        </div>
        <div class="mb-4 flex flex-col md:flex-row md:space-x-4">
          <div class="w-full md:w-8/12 mb-4 md:mb-0">
            <label for="email" class="block text-black font-bold mb-2"
              >Collaborator e-mail</label
            >
            <input
              type="email"
              id="email"
              placeholder="Enter Email here..."
              v-model="newCollab.email"
              class="itbkk-collaborator-email border border-black rounded-lg py-2 px-3 input input-ghost w-64 sm:w-96"
            />
            <p class="text-red-400 pt-2 pl-2">
              {{ errorTask.email }}
            </p>
          </div>
          <div>
            <label for="title" class="block text-black font-bold mb-2 sm:ml-5"
              >Access Right</label
            >

            <select
              class="itbkk-access-right pl-10 border border-black rounded-lg h-10 pr-5 w-full sm:ml-4"
              v-model="newCollab.accessRight"
            >
              <option value="READ">READ</option>
              <option value="WRITE">WRITE</option>
            </select>
          </div>
        </div>
        <div class="flex justify-end mt-4 space-x-4">
          <button
            class="itbkk-button-confirm btn bg-green-400 disabled:bg-green-200"
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

<style scoped>
@media (max-width: 768px) {
  /* สำหรับหน้าจอมือถือ ให้ input และ select เรียงในแนวตั้ง */
  .itbkk-modal-alert {
    padding: 2rem 1rem;
  }
}
</style>
