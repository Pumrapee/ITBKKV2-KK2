<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { useTaskStore } from "@/stores/taskStore"
import { editLimitStatus } from "../../libs/fetchUtils"
import { useLimitStore } from "../../stores/limitStore"
import { useAuthStore } from "@/stores/loginStore"
import { useRoute } from "vue-router"
import { checkAndRefreshToken } from "@/libs/fetchUtils"
import ExpireToken from "../toast/ExpireToken.vue"

//store
const myTask = useTaskStore()
const myLimit = useLimitStore()
const myUser = useAuthStore()

const props = defineProps({
  showLimitModal: Boolean,
})
const boardId = ref()
const expiredToken = ref(false)
const isLimitEnabled = ref(myLimit.getLimit().taskLimitEnabled)
const maxTasks = ref(myLimit.getLimit().maxTasksPerStatus || 10)
const showLimitStatus = ref()
const showWarning = ref()
const statusShow = ref()
const refreshToken = ref(localStorage.getItem("refreshToken"))
const emits = defineEmits(["closeLimitModal", "closeCancel"])
const route = useRoute()

const closelimitModal = async (maxlimit) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    if (isLimitEnabled.value === true) {
      const statusNotStatus = Object.entries(
        myTask.getTasks().reduce((taskacc, task) => {
          if (task.status !== "No Status" && task.status !== "Done") {
            taskacc[task.status] = (taskacc[task.status] || 0) + 1
          }
          return taskacc
        }, {})
      ).map(([name, count]) => ({ name, count }))

      // โชว์จำนวน Task ที่เกินค่า limit
      statusShow.value = statusNotStatus.filter(
        (taskStatus) => taskStatus.count > maxTasks.value
      )

      if (statusShow.value.length > 0) {
        emits("closeLimitModal")
        showWarning.value = true
      }
      emits(
        "closeLimitModal",
        maxlimit,
        isLimitEnabled.value,
        expiredToken.value
      )
    }

    if (isLimitEnabled.value === false) {
      emits(
        "closeLimitModal",
        maxlimit,
        isLimitEnabled.value,
        expiredToken.value
      )
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    emits("closeLimitModal")
  }
}

const errorLimit = ref("")

const changeLimit = computed(() => {
  const limitMore = maxTasks.value > 30
  const limitLess = maxTasks.value < 1

  errorLimit.value = limitMore
    ? "Limit status not more than 30"
    : limitLess
    ? "Limit status not less than 1"
    : ""

  return limitMore || limitLess
})

const closeCancel = async () => {
  showWarning.value = false
  emits("closeCancel")
}

watch(
  () => route.params.id,
  (newId) => {
    boardId.value = newId
  },
  { immediate: true }
)

//ถ้าเปิด modal มาให้เซ็ตค่าตรงกับใน storeLimit
watch(
  () => props.showLimitModal,
  (newVal) => {
    // newVal เป็น เป็นค่าปัจจุบันของ props.showLimitModal
    if (newVal) {
      isLimitEnabled.value = myLimit.getLimit().taskLimitEnabled
      maxTasks.value = myLimit.getLimit().maxTasksPerStatus || 10
    }
  }
)
</script>

<template>
  <!-- Modal limit -->
  <div v-if="showLimitModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15] px-4">
      <div class="itbkk-modal-setting modal-box w-full max-w-lg sm:w-3/4 md:w-2/3 lg:w-1/3 p-6">
        <h3 class="font-bold text-lg text-slate-700">Status Settings</h3>
        <p class="py-4">
          Users can limit the number of tasks in a status by setting the Maximum
          tasks in each status
          <span class="text-red-500"
            >(except "No Status" and "Done" statuses).</span
          >
        </p>
        <div class="form-control">
          <label class="cursor-pointer label">
            <span class="label-text text-black font-bold">Enable Limit</span>
            <input
              type="checkbox"
              v-model="isLimitEnabled"
              class="itbkk-limit-task toggle toggle-pink"
            />
          </label>
        </div>

        <div v-if="isLimitEnabled === true" class="form-control mt-4">
          <label class="label">
            <span class="label-text text-black font-bold">Maximum Tasks</span>
          </label>
          <input
            type="number"
            v-model="maxTasks"
            class="itbkk-max-task input input-bordered w-full"
            placeholder="Enter max tasks"
            min="0"
            max="30"
          />
        </div>

        <div class="modal-action mt-6 flex flex-col md:flex-row md:justify-end space-y-4 md:space-y-0 md:space-x-4">
          <button
            @click="closelimitModal(maxTasks)"
            class="itbkk-button-confirm btn bg-green-400 text-white disabled:bg-green-200 disabled:text-white px-4 py-2 rounded-md hover:bg-green-500 w-full md:w-auto"
          >
            Save
          </button>
          <button @click="closeCancel()" class="itbkk-button-cancel btn bg-gray-300 text-black px-4 py-2 rounded-md hover:bg-gray-400 w-full md:w-auto">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Modal warning -->
  <div v-if="showWarning" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15] px-4">
      <div class="itbkk-modal-setting modal-box w-full max-w-lg sm:w-3/4 md:w-2/3 lg:w-1/3 p-6">
        <div class="flex justify-center">
          <img src="/icons/caution.png" alt="caution" class="h-28" />
        </div>

        <div class="pt-5 text-red-500 text-center">
          These statuses that have reached the task limit. No additional tasks
          can be added to these statuses.
        </div>

        <table class="table mt-5 w-full text-xs md:text-sm">
          <thead class="bg-blue-400">
            <tr class="text-white">
              <th>Status Name</th>
              <th>Number of Tasks</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(taskStatus, index) in statusShow" :key="index">
              <td>
                {{ taskStatus.name }}
              </td>
              <td>
                {{ taskStatus.count }}
              </td>
            </tr>
          </tbody>
        </table>

        <div>
        <div class="pt-5" v-if="isLimitEnabled">
          <p v-for="(status, index) in showLimitStatus" :key="index">
            <template v-if="status.excessCount > 0">
              {{ status.name }}: {{ status.excessCount }}
            </template>
          </p>
        </div>

        <div class="modal-action mt-6 flex flex-col md:flex-row md:justify-end space-y-4 md:space-y-0 md:space-x-4">
          <button @click="closeCancel()" class="itbkk-button-cancel btn bg-gray-300 text-black px-4 py-2 rounded-md hover:bg-gray-400 w-full md:w-auto">
            Cancel
          </button>
        </div>
      </div>
    </div>
    </div>
  </div>

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped>
@media (max-width: 768px) {
  /* ปรับแต่งให้เหมาะกับมือถือ */
  .modal-box {
    width: 90%;
    padding: 1.5rem;
  }
  .modal-action {
    flex-direction: column;
    align-items: stretch;
  }
  .btn {
    width: 100%;
    margin-bottom: 0.5rem;
  }
}
.modal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.modal-box {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  width: 400px;
  max-width: 100%;
}

.toggle-pink:checked {
  background-color: #f472b6; /* สีชมพู 400 ใน Tailwind CSS */
  border-color: #f472b6;
}
</style>
