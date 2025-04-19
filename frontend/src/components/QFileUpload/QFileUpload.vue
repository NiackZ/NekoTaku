<template>
  <v-radio-group v-if="withUrl" v-model="uploadValue">
    <v-radio :value="UPLOAD_METHOD.FILE" label="Загрузить с ПК"/>
    <v-radio :value="UPLOAD_METHOD.URL" label="Загрузить по ссылке"/>
  </v-radio-group>
  <v-file-input
      v-if="uploadValue === UPLOAD_METHOD.FILE"
      variant="underlined"
      label="Выберите файл"
      v-model="file"
      :show-size="true"
      :show-progress="true"
      :accept="fileExtensions"
      @click:clear="clearFile"
  />
  <v-text-field
      v-if="uploadValue === UPLOAD_METHOD.URL"
      v-model="imageUrl"
      label="Введите URL-адрес изображения"
      @input="previewImage"
      prepend-icon="mdi-link-variant"
      clearable
      variant="underlined"
  />
  <v-img v-if="withPreview" :src="imageSrc" contain/>
</template>
<script>
import {UPLOAD_METHOD} from "../../constants/constants.js";

export default {
  name: "QFileUpload",
  props: {
    fileExt: {
      type: Array,
      default: ['.jpg', '.jpeg', '.png', '.gif']
    },
    withPreview: {
      type: Boolean,
      default: true
    },
    posterUrl: {
      type: String,
      default: null
    },
    withUrl: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      file: null,
      imageUrl: "",
      previewUrl: "",
      uploadValue: UPLOAD_METHOD.FILE
    }
  },
  computed: {
    fileExtensions() {
      return this.fileExt.map(item => (item.startsWith('.') ? item : `.${item}`)).join(', ');
    },
    UPLOAD_METHOD() {
      return UPLOAD_METHOD;
    },
    imageSrc() {
      if (this.uploadValue === UPLOAD_METHOD.FILE && this.file) {
        return URL.createObjectURL(this.file);
      } else if (this.uploadValue === UPLOAD_METHOD.URL && this.previewUrl) {
        return this.previewUrl;
      }
      return this.posterUrl ? this.posterUrl : null;
    }
  },
  methods: {
    getFile() {
      return this.file;
    },
    getImageURL() {
      return this.imageUrl;
    },
    clearFile() {
      this.file = null;
    },
    previewImage() {
      this.previewUrl = this.imageUrl;
    }
  }
}
</script>
<style>
.v-field__field {
  overflow: hidden !important;
}
</style>