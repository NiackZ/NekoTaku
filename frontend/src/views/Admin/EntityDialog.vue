<template>
  <v-dialog v-model="dialogVisible" max-width="500px" scrim="black">
    <v-card density="compact">
      <v-card-title class="text-center">
        <span class="text-h5">{{ formTitle }}</span>
      </v-card-title>
      <v-card-text style="padding: 0">
        <v-container>
          <v-row>
            <v-col cols="12">
              <v-text-field
                  v-model="editedItemCopy.name"
                  variant="outlined"
                  autofocus
                  :rules="[required]"
                  label="Название"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="red" variant="text" @click="close">Закрыть</v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="saveOrCreate">Сохранить</v-btn>
      </v-card-actions>
    </v-card>
    <v-fade-transition>
      <v-alert
          class="alert-container"
          :text="alertText"
          type="error"
          density="compact"
          v-model="showAlert"
      ></v-alert>
    </v-fade-transition>
  </v-dialog>
</template>

<script>
export default {
  name: "EntityDialog",
  props: {
    modelValue: Boolean,
    editedItem: {
      type: Object,
      required: true,
    },
    createApi: {
      type: Function,
      required: true,
    },
    saveApi: {
      type: Function,
      required: true,
    },
    titleNew: {
      type: String,
      default: "Новая запись",
    },
    titleEdit: {
      type: String,
      default: "Редактирование записи",
    },
  },
  emits: ["update:modelValue", "saved", "created"],
  data() {
    return {
      showAlert: false,
      alertText: null,
      editedItemCopy: { ...this.editedItem }, // локальная копия для работы
    };
  },
  watch: {
    editedItem: {
      handler(newVal) {
        this.editedItemCopy = { ...newVal };
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {
    dialogVisible: {
      get() {
        return this.modelValue;
      },
      set(value) {
        this.$emit("update:modelValue", value);
      },
    },
    formTitle() {
      return this.editedItemCopy.id === null ? this.titleNew : this.titleEdit;
    },
  },
  methods: {
    required(v) {
      return !!v || "Заполните название";
    },
    close() {
      this.dialogVisible = false;
    },
    async saveOrCreate() {
      try {
        if (!this.editedItemCopy.name) return;

        if (this.editedItemCopy.id === null) {
          const response = await this.createApi({ name: this.editedItemCopy.name });
          this.$emit("created", response.data);
        } else {
          await this.saveApi(this.editedItemCopy.id, { name: this.editedItemCopy.name });
          this.$emit("saved");
        }
        this.close();
      } catch (e) {
        this.alertText = e.response?.data || "Произошла ошибка";
        this.showAlert = true;
        setTimeout(() => (this.showAlert = false), 5000);
      }
    },
  },
};
</script>

<style scoped>
.alert-container {
  position: fixed;
  transform: translateY(calc(100% + 180px));
  width: 100%;
}
</style>
