<script setup>
import { ref, defineProps, defineEmits } from "vue"
import { useTaskStore } from "@/stores/taskStore"
import { editLimitStatus } from "../libs/fetchUtils"

const props = defineProps({
  showLimitModal: Boolean,
})

const isLimitEnabled = ref(false)
const maxTasks = ref(10)
const myTask = useTaskStore()
const showLimitStatus = ref()
const emits = defineEmits(["closeLimitModal", "closeCancle"])

const closelimitModal = async (maxlimit) => {
  //นับจำนวน status ที่ใช้ของแต่ละอัน ได้ค่าเป็น {}
  if (isLimitEnabled.value === true) {
    const lengthStatus = myTask.getTasks().reduce((taskacc, task) => {
      taskacc[task.status] = (taskacc[task.status] || 0) + 1
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

    console.log(statusNotStatus)

    //map ค่า count แล้วลบกับค่า maxlimit เพื่อได้ค่า status ที่เกิน limit
    const StatusIslimit = statusNotStatus.map(({ name, count }) => {
      const excessCount = count - maxlimit
      return { name, excessCount: excessCount }
    })

    showLimitStatus.value = StatusIslimit
    await editLimitStatus(
      `${import.meta.env.VITE_API_URL}statuses`,
      isLimitEnabled.value,
      maxlimit
    )
    console.log(lengthStatusArray)
    console.log(StatusIslimit)

    //check ว่าค่า excessCount มีค่าเป็น 0 ไหม
    const statusIsNotLimit = StatusIslimit.every((status) => {
      return status.excessCount <= 0
    })

    if (statusIsNotLimit) {
      emits("closeLimitModal", maxlimit, isLimitEnabled.value, statusIsNotLimit)
    }

    if (!statusIsNotLimit) {
      emits("closeLimitModal", maxlimit, isLimitEnabled.value, statusIsNotLimit)
    }
  }

  await editLimitStatus(
    `${import.meta.env.VITE_API_URL}statuses`,
    isLimitEnabled.value,
    maxlimit
  )
  //แปลกๆ
  emits("closeLimitModal", maxlimit, isLimitEnabled.value, isLimitEnabled.value)
}

const closeCancle = () => {
  emits("closeCancle")
}
</script>

<template>
  <div v-if="showLimitModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="modal-box">
        <h3 class="font-bold text-lg">Status Settings</h3>
        <p class="py-4">
          Users can limit the number of tasks in a status by setting the Maximum
          tasks in each status
          <span class="text-red-500"
            >(except "No Status" and "Done" statuses).</span
          >
        </p>
        <div class="form-control">
          <label class="cursor-pointer label">
            <span class="label-text">Enable Limit</span>
            <input type="checkbox" v-model="isLimitEnabled" class="toggle" />
          </label>
        </div>

        <div v-if="isLimitEnabled === true" class="form-control mt-4">
          <label class="label">
            <span class="label-text">Maximum Tasks</span>
          </label>
          <input
            type="number"
            v-model="maxTasks"
            class="input input-bordered"
            placeholder="Enter max tasks"
          />
        </div>

        <div class="pt-5" v-if="isLimitEnabled">
          <p v-for="(status, index) in showLimitStatus" :key="index">
            <template v-if="status.excessCount > 0">
              {{ status.name }}: {{ status.excessCount }}
            </template>
          </p>
        </div>

        <div class="modal-action">
          <button @click="closelimitModal(maxTasks)" class="btn">Save</button>
          <button @click="closeCancle()" class="btn">Close</button>
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
</style>
