<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue"
import { useTaskStore } from "@/stores/taskStore"
import { editLimitStatus } from "../../libs/fetchUtils"
import { useLimitStore } from "../../stores/limitStore"

const props = defineProps({
  showLimitModal: Boolean,
})

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

const myLimit = useLimitStore()
const isLimitEnabled = ref(myLimit.getLimit().taskLimitEnabled)
const maxTasks = ref(myLimit.getLimit().maxTasksPerStatus || 10)
const myTask = useTaskStore()
const showLimitStatus = ref()
const emits = defineEmits(["closeLimitModal", "closeCancel"])

const closelimitModal = async (maxlimit) => {
  //นับจำนวน status ที่ใช้ของแต่ละอัน ได้ค่าเป็น {}
  if (isLimitEnabled.value === true) {
    const lengthStatus = myTask.getTasks().reduce((taskacc, task) => {
      taskacc[task.status] = (taskacc[task.status] || 0) + 1 //object key
      return taskacc
    }, {})

    //แปลงค่า {} เป็น [{}] เพื่อใช้ array method
    const lengthStatusArray = Object.entries(lengthStatus).map(
      ([name, count]) => {
        return { name, count }
      }
    )

    //ไม่เอาค่าที่มี status name Done กับ No Status
    const statusNotStatus = lengthStatusArray.filter((status) => {
      return !(status.name === "No Status" || status.name === "Done")
    })

    //map ค่า count แล้วลบกับค่า maxlimit เพื่อได้ค่า status ที่เกิน limit
    const StatusIslimit = statusNotStatus.map(({ name, count }) => {
      const excessCount = count - maxlimit
      return { name, excessCount: excessCount }
    })

    showLimitStatus.value = StatusIslimit
    const editedLimit = await editLimitStatus(
      `${import.meta.env.VITE_API_URL}statuses`,
      isLimitEnabled.value,
      maxlimit
    )
    //เอาค่า fetch update ใน store
    myLimit.addLimit(editedLimit)

    //check ว่าค่า excessCount มีค่าเป็น 0 ไหม
    const statusIsNotLimit = StatusIslimit.every((status) => {
      return status.excessCount <= 0
    })
    emits("closeLimitModal", maxlimit, isLimitEnabled.value, statusIsNotLimit)
  }

  if (isLimitEnabled.value === false) {
    const editedLimit = await editLimitStatus(
      `${import.meta.env.VITE_API_URL}statuses`,
      isLimitEnabled.value,
      maxlimit
    )

    //เอาค่า fetch เก็บใน store
    myLimit.addLimit(editedLimit)
    //แปลกๆ
    emits(
      "closeLimitModal",
      maxlimit,
      isLimitEnabled.value,
      isLimitEnabled.value
    )
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
  emits("closeCancel")
}
</script>

<template>
  <div v-if="showLimitModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="itbkk-modal-setting modal-box">
        <h3 class="font-bold text-lg text-blue-400">Status Settings</h3>
        <p class="py-4">
          Users can limit the number of tasks in a status by setting the Maximum
          tasks in each status
          <span class="text-red-500"
            >(except "No Status" and "Done" statuses).</span
          >
        </p>
        <div class="form-control">
          <label class="cursor-pointer label">
            <span class="label-text text-blue-400">Enable Limit</span>
            <input
              type="checkbox"
              v-model="isLimitEnabled"
              class="itbkk-limit-task toggle toggle-pink"
            />
          </label>
        </div>

        <div v-if="isLimitEnabled === true" class="form-control mt-4">
          <label class="label">
            <span class="label-text text-blue-400">Maximum Tasks</span>
          </label>
          <input
            type="number"
            v-model="maxTasks"
            class="itbkk-max-task input input-bordered"
            placeholder="Enter max tasks"
            min="0"
            max="30"
          />
        </div>

        <div class="pt-2 text-red-500">
          {{ errorLimit }}
        </div>

        <div class="pt-5" v-if="isLimitEnabled">
          <p v-for="(status, index) in showLimitStatus" :key="index">
            <template v-if="status.excessCount > 0">
              {{ status.name }}: {{ status.excessCount }}
            </template>
          </p>
        </div>

        <div class="modal-action">
          <button
            @click="closelimitModal(maxTasks)"
            class="itbkk-button-confirm btn bg-green-400 text-white disabled:bg-green-200 disabled:text-white"
            :disabled="changeLimit"
          >
            Save
          </button>
          <button @click="closeCancel()" class="itbkk-button-cancel btn">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
