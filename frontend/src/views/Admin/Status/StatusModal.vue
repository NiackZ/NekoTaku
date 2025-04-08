<template>
  <EntityDialog
      v-model="dialog"
      :editedItem="editedItem"
      :createApi="createStatus"
      :saveApi="saveStatus"
      titleNew="Новый статус"
      titleEdit="Редактирование статуса"
  />
</template>

<script>
import EntityDialog from "../EntityDialog.vue";
import {createStatus, saveStatus} from "../../../axios/api/statuses.js";

export default {
  components: { EntityDialog },
  data() {
    return {
      dialog: false,
      editedItem: { id: null, name: "" },
    };
  },
  methods: {
    createStatus,
    saveStatus
  },
};
</script>
