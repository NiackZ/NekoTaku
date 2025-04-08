<template>
  <EntityDialog
      v-model="dialog"
      :editedItem="editedItem"
      :createApi="createStudio"
      :saveApi="saveStudio"
      titleNew="Новая студия"
      titleEdit="Редактирование студии"
  />
</template>

<script>
import EntityDialog from "../EntityDialog.vue";
import {createStudio, saveStudio} from "../../../axios/api/studios.js";

export default {
  components: { EntityDialog },
  data() {
    return {
      dialog: false,
      editedItem: { id: null, name: "" },
    };
  },
  methods: {
    createStudio,
    saveStudio
  },
};
</script>
