<template>
  <EntityDialog
      v-model="dialog"
      :editedItem="editedItem"
      :createApi="createGenre"
      :saveApi="saveGenre"
      titleNew="Новый жанр"
      titleEdit="Редактирование жанр"
  />
</template>

<script>
import EntityDialog from "../EntityDialog.vue";
import {createGenre, saveGenre} from "../../../axios/api/genres.js";

export default {
  components: {EntityDialog},
  data() {
    return {
      dialog: false,
      editedItem: {id: null, name: ""},
    };
  },
  methods: {
    createGenre,
    saveGenre
  },
};
</script>
