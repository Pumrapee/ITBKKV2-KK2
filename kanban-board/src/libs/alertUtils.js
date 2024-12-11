import { ref } from "vue"

export const listAlert = ref([])

// ฟังก์ชันเพื่อแสดง alert ใหม่
export const showAlert = (message, type) => {
  const newAlert = { message, type, modal: true }
  listAlert.value.push(newAlert)

  // ตั้งค่า timeout สำหรับ alert แต่ละตัว
  setTimeout(() => {
    const index = listAlert.value.indexOf(newAlert)
    if (index !== -1) {
      listAlert.value.splice(index, 1) // ลบ alert เมื่อหมดเวลา
    }
  }, 4000)
}
