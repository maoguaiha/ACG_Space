import { VueCropper } from 'vue-cropper'
export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.component('VueCropper', VueCropper)
})
