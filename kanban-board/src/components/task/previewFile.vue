<script setup>
import { defineProps, defineEmits, ref, watch } from "vue"

const props = defineProps({
  filePre: Object,
})
const emits = defineEmits(["closePreview"])

const closePreview = () => {
  emits("closePreview")
}

let scale = ref(1)
let translateX = ref(0)
let translateY = ref(0)

const zoomIn = () => {
  scale.value = Math.min(scale.value + 0.1, 3) // ซูมสูงสุด 3 เท่า
}

const zoomOut = () => {
  scale.value = Math.max(scale.value - 0.1, 1) // ลดการซูมต่ำสุดที่ 1 เท่า
}

const resetZoom = () => {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

const handleWheel = (event) => {
  event.preventDefault()
  if (event.deltaY < 0) zoomIn()
  else zoomOut()
}

const handleDrag = (event) => {
  if (event.buttons === 1) {
    translateX.value += event.movementX
    translateY.value += event.movementY
  }
}

// ถ้าเปิด preview ใหม่จะ reset zoom เสมอ
watch(
  () => props.filePre,
  (newValue, oldValue) => {
    if (newValue !== oldValue) {
      resetZoom()
    }
  }
)
</script>
<template>
  <!-- Preview Modal -->
  <div
    v-if="
      props.filePre &&
      props.filePre.type !== 'PDF' &&
      props.filePre.type !== 'media'
    "
    class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50"
    @click="closePreview"
  >
    <div
      class="relative p-4 rounded-lg shadow-lg max-w-[90%] max-h-[90%] overflow-hidden"
      @click.stop
    >
      <!-- Video Preview -->
      <video
        v-if="props.filePre.type === 'video'"
        :src="props.filePre.url"
        class="w-auto h-auto max-w-5xl max-h-full object-contain"
        controls
        autoplay
      ></video>

      <!-- Text Preview -->
      <div
        v-else-if="props.filePre.type === 'txt'"
        class="overflow-auto max-h-full bg-white border rounded-lg shadow-md w-full max-w-5xl"
      >
        <pre
          v-if="props.filePre.content"
          class="p-4 text-sm text-gray-800 font-mono whitespace-pre-wrap leading-relaxed"
        >
    {{ props.filePre.content }}
  </pre
        >
        <div
          v-else
          class="w-64 h-64 flex items-center justify-center border border-gray-300 bg-gray-100"
        >
          <p class="text-gray-500 text-center">No content in this file.</p>
        </div>
      </div>

      <!-- Close Button -->
      <button
        class="fixed top-4 right-4 text-black rounded-full w-8 h-8 flex items-center justify-center bg-white shadow-lg z-50"
        @click="closePreview"
      >
        ✕
      </button>
    </div>
  </div>

  <div
    v-if="props.filePre && props.filePre.type === 'media'"
    class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50"
    @click="closePreview"
  >
    <div
      class="p-4 rounded-lg shadow-lg max-w-[90%] max-h-[90%] overflow-hidden"
      @click.stop
      @wheel="handleWheel"
      @mousemove="handleDrag"
    >
      <!-- Image Preview -->
      <img
        :src="props.filePre.url"
        alt="Selected Preview"
        class="w-auto h-auto max-w-5xl max-h-full object-contain"
        :style="{
          transform: `scale(${scale}) translate(${translateX}px, ${translateY}px)`,
          cursor: 'grab',
        }"
        draggable="false"
      />

      <!-- Zoom Controls -->
      <div
        class="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-2 z-10 mt-20"
      >
        <button
          class="bg-white rounded px-4 py-2 text-black font-semibold shadow-md"
          @click="zoomIn"
        >
          <svg
            fill="#000000"
            height="20px"
            width="20px"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 512 512"
            xmlns:xlink="http://www.w3.org/1999/xlink"
            enable-background="new 0 0 512 512"
          >
            <g>
              <g>
                <path
                  d="m495,466.1l-109.5-109.4c31.2-37.8 48.2-84.7 48.2-134.3 0-56.5-22-109.5-61.9-149.5-39.9-39.9-93-61.9-149.5-61.9-56.5,0-109.5,22-149.5,61.9-82.4,82.4-82.4,216.5 0,298.9 39.9,39.9 93,61.9 149.5,61.9 49.6,0 96.6-17 134.3-48.2l109.5,109.5c11.5,10.5 23.8,5.1 28.9,0 7.9-8 7.9-20.9 0-28.9zm-152.1-123.1c-32.2,32.2-75,50-120.6,50-45.6,0-88.4-17.7-120.6-50-66.5-66.5-66.5-174.7 0-241.2 32.2-32.2 75-49.9 120.6-49.9 45.6,0 88.4,17.7 120.6,49.9 32.2,32.2 50,75 50,120.6 0,45.5-17.8,88.4-50,120.6z"
                />
                <path
                  d="m319.6,202h-76.9v-77c0-11.3-9.1-20.4-20.4-20.4-11.3,0-20.4,9.1-20.4,20.4v77h-76.9c-11.3,0-20.4,9.1-20.4,20.4 0,11.3 9.1,20.4 20.4,20.4h76.9v76.9c0,11.3 9.1,20.4 20.4,20.4 11.3,0 20.4-9.1 20.4-20.4v-76.9h76.9c11.3,0 20.4-9.1 20.4-20.4 0.1-11.3-9.1-20.4-20.4-20.4z"
                />
              </g>
            </g>
          </svg>
        </button>
        <button
          class="bg-white rounded px-4 py-2 text-black font-semibold shadow-md"
          @click="zoomOut"
        >
          <svg
            fill="#000000"
            height="20px"
            width="20px"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 512 512"
            xmlns:xlink="http://www.w3.org/1999/xlink"
            enable-background="new 0 0 512 512"
          >
            <g>
              <g>
                <path
                  d="m495,466.1l-109.5-109.4c31.2-37.8 48.2-84.7 48.2-134.3 0-56.5-22-109.5-61.9-149.5-39.9-39.9-93-61.9-149.5-61.9-56.5,0-109.5,22-149.5,61.9-82.4,82.4-82.4,216.5 0,298.9 39.9,39.9 93,61.9 149.5,61.9 49.6,0 96.6-17 134.3-48.2l109.5,109.5c11.5,10.5 23.8,5.1 28.9,0 7.9-8 7.9-20.9 0-28.9zm-152.1-123.2c-32.2,32.2-75,50-120.6,50-45.6,0-88.4-17.7-120.6-50-66.5-66.5-66.5-174.7 0-241.2 32.2-32.2 75-49.9 120.6-49.9 45.6,0 88.4,17.7 120.6,49.9 32.2,32.2 50,75 50,120.6 0,45.6-17.8,88.4-50,120.6z"
                />
                <path
                  d="m319.6,201.9h-194.6c-11.3,0-20.4,9.1-20.4,20.4 0,11.3 9.1,20.4 20.4,20.4h194.6c11.3,0 20.4-9.1 20.4-20.4 0.1-11.2-9.1-20.4-20.4-20.4z"
                />
              </g>
            </g>
          </svg>
        </button>
        <button
          class="bg-white rounded px-4 py-2 text-black font-semibold shadow-md"
          @click="resetZoom"
        >
          Reset
        </button>
      </div>

      <!-- Close Button -->
      <button
        class="fixed top-4 right-4 text-black rounded-full w-8 h-8 flex items-center justify-center bg-white shadow-lg z-50"
        @click="closePreview"
      >
        ✕
      </button>
    </div>
  </div>

  <div
    v-if="props.filePre && props.filePre.type === 'PDF'"
    class="fixed inset-0 bg-black bg-opacity-75 z-50 flex items-center justify-center"
  >
    <!-- Modal Container -->
    <div class="bg-white w-[95%] h-[95%] relative rounded-lg shadow-lg">
      <!-- Toolbar -->
      <div class="flex justify-end items-center px-4 py-2 bg-gray-100 border-b">
        <button @click="closePreview" class="text-red-500 font-bold">✕</button>
      </div>

      <!-- PDF Viewer -->
      <div class="overflow-auto h-full">
        <div class="pdf-container w-full h-full">
          <iframe
            :src="props.filePre.url"
            class="w-full h-full"
            frameborder="0"
            type="application/pdf"
          ></iframe>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pdf-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.fixed {
  max-height: 100vh; /* Prevent the modal from exceeding viewport height */
}

img {
  transition: transform 0.2s ease-in-out;
}
</style>
