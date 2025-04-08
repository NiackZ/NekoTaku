<template>
  <EntityDialog
      v-model="dialog"
      :editedItem="editedItem"
      :createApi="createType"
      :saveApi="saveType"
      titleNew="Новый тип"
      titleEdit="Редактирование типа"
  />
</template>

<script>
import EntityDialog from "../EntityDialog.vue";
import {createType, saveType} from "../../../axios/api/types.js";

export default {
  components: {EntityDialog},
  data() {
    return {
      dialog: false,
      editedItem: {id: null, name: ""},
    };
  },
  methods: {
    saveType,
    createType
  },
};
</script>
