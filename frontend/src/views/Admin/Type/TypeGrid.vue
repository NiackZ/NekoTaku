<template>
  <v-card>
    <template v-slot:text>
      <v-text-field
          v-model="search"
          label="Поиск типа"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          clearable
          hide-details
          single-line
      ></v-text-field>
    </template>
    <v-data-table-virtual
        density="comfortable"
        hover
        :loading="isLoading"
        :items="showDeleted ? items : filteredItems"
        :row-props="rowFunc"
        :headers="headers"
        :search="search"
        :sort-by="[{ key: 'id', order: 'desc' }]"
    >
      <template v-slot:top>
        <v-toolbar density="compact" flat style="padding: 0 1rem; background-color: inherit;">
          <v-btn
              class=""
              color="primary"
              @click="openDialog(null)"
          >
            Добавить тип
          </v-btn>
          <v-checkbox
              class="q-checkbox"
              v-model="showDeleted"
              label="Показать удалённые"
              color="primary"
              :hide-details="true"
              density="comfortable"
          />
        </v-toolbar>
      </template>
      <template v-slot:item.actions="{ item }">
        <v-icon
            class="me-2"
            size="small"
            @click="openDialog(item)"
        >
          mdi-pencil
        </v-icon>
        <v-icon
            v-if="!item.isDeleted"
            size="small"
            @click="deleteItem(item)"
        >
          mdi-delete
        </v-icon>
        <v-icon
            v-else
            size="small"
            @click="restoreItem(item)"
        >
          mdi-restore
        </v-icon>
      </template>
      <template v-slot:loading>
        <v-skeleton-loader type="table-row"></v-skeleton-loader>
      </template>
    </v-data-table-virtual>
    <TypeModal
        v-model="dialogVisible"
        :edited-item="editedItem"
        @saved="onSaved"
        @created="onCreated"
    />
  </v-card>
</template>

<script>
import {getTypes, saveType, deleteType} from "../../../axios/api/types.js";
import TypeModal from "./TypeModal.vue";

export default {
  components: {TypeModal},
  data() {
    return {
      headers: [
        { title: 'ИД', key: 'id' },
        { title: 'Название', key: 'name' },
        { title: 'Действия', key: 'actions', sortable: false, width: '100px' }
      ],
      showDeleted: false,
      dialogVisible: false,
      isLoading: true,
      search: '',
      items: [],
      filteredItems: [],
      editedItem: {
        id: null,
        name: null,
        isDeleted: false
      }
    };
  },
  async created() {
    await this.reloadList();
  },
  methods: {
    rowFunc(row) {
      if (row.item.isDeleted) {
        return {
          class: 'deleted-row'
        }
      }
    },
    async reloadList() {
      try {
        this.isLoading = true;
        this.items = (await getTypes()).data.map(item => {
          return {
            id: item.id,
            name: item.name,
            isDeleted: item.isDeleted
          }
        });
        this.filteredItems = this.items.filter(item => !item.isDeleted);
        const _this = this;
        setTimeout(function () {
          _this.isLoading = false;
          console.log('list reloaded');
        }, 200)
      }
      catch (e) {
        this.isLoading = false;
      }
    },
    openDialog(item) {
      this.editedItem = item ? { ...item } : { id: null, name: null, isDeleted: false };
      this.dialogVisible = true;
    },
    async deleteItem(item) {
      await deleteType(item.id);
      await this.reloadList();
    },
    async restoreItem(item) {
      try {
        item.isDeleted = false;
        await saveType(item.id, item);
        await this.reloadList();
      } catch (e) {
        console.error(e.response.data);
      }
    },
    onSaved() {
      this.reloadList();
    },
    onCreated() {
      this.reloadList();
    },
  },
};
</script>

<style scoped>
.alert-container {
  position: fixed;
  transform: translateY(calc(100% + 180px)); /*Сомнительно*/
  width: 100%;
}
</style>