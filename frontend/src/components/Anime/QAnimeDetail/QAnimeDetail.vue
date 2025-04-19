<template>
  <v-row>
    <v-col cols="12" md="5" lg="4">
      <q-file-upload v-if="anime?.posterURL" ref="fileUpload" :poster-url="anime.posterURL" />
      <q-file-upload v-else ref="fileUpload" />
    </v-col>
    <v-col cols="12" md="7" lg="8" class="ff-verdana">
      <v-form>
        <v-text-field v-if="anime?.id" label="ИД"
                      v-model="anime.id"
                      variant="underlined"
                      disabled
        />
        <v-text-field label="Название на русском"
                      v-model="form.rusName"
                      variant="underlined"
                      :error-messages="v$.form.rusName.$errors.map(e => e.$message)"
        />
        <v-text-field label="Название на ромадзи"
                      v-model="form.romName"
                      variant="underlined"
                      :error-messages="v$.form.romName.$errors.map(e => e.$message)"
        />
        <v-autocomplete
            v-for="field in autocompleteFormFields"
            :key="field"
            :label="field.label"
            v-model="form[field.model].value"
            :items="getEnchantedList(field.model)"
            item-value="id"
            item-title="name"
            variant="underlined"
            :multiple="field.multiple"
            density="compact"
            :error-messages="v$.form[field.model].value.$errors.map(e => e.$message)"
            @update:modelValue="handleSelection(field.model, $event)"
        >
          <template #item="{ props, item }">
            <v-list-item
                v-bind="props"
                :title="item.raw.name"
                :prepend-icon="item.raw.id === 'add-new' ? 'mdi-plus' : ''"
                :class="{ 'text-primary': item.raw.id === 'add-new' }"
            />
          </template>
        </v-autocomplete>
        <v-text-field label="Количество эпизодов"
                      v-model="form.episodeCount"
                      variant="underlined"
                      :error-messages="v$.form.episodeCount.$errors.map(e => e.$message)"
        />
        <v-text-field label="Продолжительность эпизода (в минутах)"
                      v-model="form.episodeDuration"
                      variant="underlined"
                      :error-messages="v$.form.episodeDuration.$errors.map(e => e.$message)"
        />
        <q-vue-date-picker :date="form.period"
                           @update:date="updateDate"
                           placeholder="Выберите период выпуска"
                           :error-messages="v$.form.period.$errors.map(e => e.$message)"
        />
        <q-link-field :links="form.links" @update:links="updateLinks"/>
        <v-autocomplete label="Метки"
                        v-model="form.marks.value"
                        :items="form.marks.list"
                        item-value="id" item-title="name"
                        variant="underlined"
                        density="compact"
                        clearable
                        multiple
        />
        <q-jodit-editor :text="form.description" @update:text="updateDescription" />
        <div class="py-4 text-end">
          <v-btn color="primary"
                 min-width="92"
                 variant="outlined"
                 class="ml-3"
                 @click="anime ? saveAnime() : createAnime()"
          >
            {{ anime ? 'Сохранить' : 'Создать' }}
          </v-btn>
        </div>
      </v-form>
      <TypeModal
          v-model="visibleModal.type"
          v-if="selectedModal === 'type'"
          :edited-item="editedItem"
          @saved="onSaved"
          @created="onCreated"
      />
      <GenreModal
          v-model="visibleModal.genre"
          v-if="selectedModal === 'genre'"
          :edited-item="editedItem"
          @saved="onSaved"
          @created="onCreated"
      />
      <StudioModal
          v-model="visibleModal.studio"
          v-if="selectedModal === 'studio'"
          :edited-item="editedItem"
          @saved="onSaved"
          @created="onCreated"
      />
      <StatusModal
          v-model="visibleModal.status"
          v-if="selectedModal === 'status'"
          :edited-item="editedItem"
          @saved="onSaved"
          @created="onCreated"
      />
    </v-col>
  </v-row>
</template>

<script>
import {useVuelidate} from '@vuelidate/core'
import {helpers, required} from '@vuelidate/validators'
import QLinkField from "../../QLinkField/QLinkField.vue";
import QJoditEditor from "../../QJoditEditor/QJoditEditor.vue";
import QVueDatePicker from "../../QVueDatePicker/QVueDatePicker.vue";
import QFileUpload from "../../QFileUpload/QFileUpload.vue";
import {encodeImage, getMarks, isNotEmpty} from "../../../utils/utils.js";
import {getGenres} from "../../../axios/api/genres.js";
import {getTypes} from "../../../axios/api/types.js";
import {getStatuses} from "../../../axios/api/statuses.js";
import {getStudios} from "../../../axios/api/studios.js";
import axios from '/src/axios/http-common'
import TypeModal from "../../../views/Admin/Type/TypeModal.vue";
import GenreModal from "../../../views/Admin/Genre/GenreModal.vue";
import StudioModal from "../../../views/Admin/Studio/StudioModal.vue";
import StatusModal from "../../../views/Admin/Status/StatusModal.vue";

const ADD_NEW = 'add-new';

const MODEL = {
  TYPE: 'type',
  GENRE: 'genre',
  STUDIO: 'studio',
  STATUS: 'status'
}

export default {
  name: 'QAnimeDetail',
  components: {StatusModal, StudioModal, GenreModal, TypeModal, QFileUpload, QVueDatePicker, QJoditEditor, QLinkField},
  props: {
    id: Number
  },
  data() {
    return {
      form: {
        rusName: "",
        romName: "",
        type: {
          value: null,
          list: []
        },
        genre: {
          value: [],
          list: []
        },
        studio: {
          value: [],
          list: []
        },
        status: {
          value: null,
          list: []
        },
        episodeCount: 0,
        episodeDuration: 0,
        period: null,
        links: [],
        marks: {
          value: [],
          list: []
        },
        description: ''
      },
      anime: null,
      v$: useVuelidate({$scope: 'form'}),
      autocompleteFormFields: [
        {
          label: 'Тип',
          model: MODEL.TYPE,
          multiple: false,
        },
        {
          label: 'Жанр',
          model: MODEL.GENRE,
          multiple: true,
        },
        {
          label: 'Студия',
          model: MODEL.STUDIO,
          multiple: true,
        },
        {
          label: 'Статус',
          model: MODEL.STATUS,
          multiple: false,
        }
      ],
      editedItem: { id: null, name: '', isDeleted: false }, // Данные для редактирования/создания типа
      selectedModal: null,
      visibleModal: {
        type: false,
        genre: false,
        studio: false,
        status: false
      }
    }
  },
  computed: {
    getEnchantedList() {
      return (field) => [
        { id: ADD_NEW, name: 'Добавить новый' },
        ...this.form[field].list,
      ];
    }
  },
  async created() {
    const animeId = this.$props.id;
    let animePromise = null;
    if (animeId) {
      animePromise = axios.get(`/anime/${animeId}`);
    }
    const typesPromise = getTypes();
    const genresPromise = getGenres();
    const studiosPromise = getStudios();
    const statusesPromise = getStatuses();
    const marksPromise = getMarks();

    this.form.type.list = (await typesPromise).data.filter(item => !item.isDeleted);
    this.form.genre.list = (await genresPromise).data.filter(item => !item.isDeleted);
    this.form.studio.list = (await studiosPromise).data.filter(item => !item.isDeleted);
    this.form.status.list = (await statusesPromise).data.filter(item => !item.isDeleted);
    this.form.marks.list = (await marksPromise).data.filter(item => !item.isDeleted);

    if (animePromise !== null) {
      this.anime = (await animePromise).data;
      console.log("Аниме получено из БД");
      this.form.rusName = this.anime.ruName;
      this.form.romName = this.anime.romajiName;
      this.form.type.value = this.anime.type.id;
      this.form.genre.value.push(...this.anime.genres.map(genre => genre.id));
      this.form.studio.value.push(...this.anime.studios.map(studio => studio.id));
      this.form.status.value = this.anime.status.id;
      this.form.rusName = this.anime.ruName;
      this.form.romName = this.anime.romajiName;
      this.form.episodeCount = this.anime.episodeCount;
      this.form.episodeDuration = this.anime.episodeDuration;
      const startDate = this.anime.startDate ? new Date(this.anime.startDate) : null;
      const endDate = this.anime.endDate ? new Date(this.anime.endDate) : null;
      if (startDate) {
        this.form.period = endDate
            ? [startDate, endDate]
            : startDate
      }
      this.form.links = this.anime.links;
      this.form.marks.value = this.anime.marks;
      this.form.description = this.anime.description;
    }
  },
  mounted() {
    document.title = this.$props.id ? 'Редактирование аниме' : 'Добавление нового аниме';
  },
  methods: {
    handleSelection(field, value) {
      // Проверяем, является ли поле множественным (multiple)
      if (Array.isArray(value)) {
        // Если выбран "Добавить новый" среди выбранных значений
        if (value.includes(ADD_NEW)) {
          // Удаляем 'add-new' из массива выбранных значений
          this.form[field].value = value.filter(item => item !== ADD_NEW);
          this.openModal(field); // Открываем модалку для выбранного поля
        } else {
          console.log(`Выбраны ${field}:`, value);
        }
      } else {
        // Обработка для одиночного выбора
        if (value === ADD_NEW) {
          this.form[field].value = null; // Очищаем текущее значение
          this.openModal(field); // Открываем модалку для выбранного поля
        } else {
          console.log(`Выбран ${field}:`, value);
        }
      }
    },
    openModal(field) {
      this.editedItem = { id: null, name: '', isDeleted: false }; // Подготавливаем пустой объект для создания нового элемента
      this.visibleModal[field] = true;
      this.selectedModal = field; // Сохраняем текущее поле для дальнейшей обработки
    },
    onSaved(newType) {
      this.reloadList(this.selectedModal);
      this.visibleModal[this.selectedModal] = false;
      this.selectedModal = null;
    },
    onCreated(newType) {
      this.reloadList(this.selectedModal);
      this.visibleModal[this.selectedModal] = false;
      this.selectedModal = null;
    },
    async reloadList(list) {
      switch (list) {
        case MODEL.TYPE:
          this.form[list].list = (await getTypes()).data.filter(item => !item.isDeleted);
          break;
        case MODEL.GENRE:
          this.form[list].list = (await getGenres()).data.filter(item => !item.isDeleted);
          break;
        case MODEL.STUDIO:
          this.form[list].list = (await getStudios()).data.filter(item => !item.isDeleted);
          break;
        case MODEL.STATUS:
          this.form[list].list = (await getStatuses()).data.filter(item => !item.isDeleted);
          break;
        default:
          console.error("ERROR")
      }
    },
    updateDate(newValue) {
      this.form.period = newValue;
    },
    updateDescription(newValue) {
      this.form.description = newValue;
    },
    updateLinks(newValue) {
      this.form.links = newValue;
    },
    async isValid(){
      return await this.v$.$validate();
    },
    getFormData() {
      const period = this.form.period;
      return {
        id: this.anime?.id,
        rusName: this.form.rusName,
        romName: this.form.romName,
        typeId: this.form.type.value,
        genreIds: this.form.genre.value,
        studioIds: this.form.studio.value,
        statusId: this.form.status.value,
        episodeCount: this.form.episodeCount,
        episodeDuration: this.form.episodeDuration,
        period: isNotEmpty(period[0]) ? period : [period], // суть в том, чтобы в любом случае передать массив
        linkList: this.form.links,
        markIds: this.form.marks.value,
        description: this.form.description
      };
    },
    async createAnime() {
      if (!await this.isValid()) return;
      const formData = new FormData();

      formData.append('poster', this.$refs.fileUpload.getFile());
      formData.append('anime', new Blob([JSON.stringify(this.getFormData())], { type: 'application/json' }));

      try {
        const response = await axios.post("/anime", formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        console.log('Anime успешно создано: ', response.data);
      }
      catch (error) {
        const id = error.response.data?.id;
        const msg = error.response.data?.message;
        if (!!id) {
          console.info('Аниме сохранено с ИД', id);
          this.$router.push(`/admin/anime/${id}`);
        }
        if (!!msg) {
          console.error(msg);
        }
      }
    },
    async saveAnime() {
      try {
        const formData = new FormData();
        formData.append('poster', this.$refs.fileUpload.getFile());
        formData.append('animeJson', new Blob([JSON.stringify(this.getFormData())], { type: 'application/json' }));

        await axios.put(`/anime/${this.anime.id}`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        console.log('Anime успешно сохранено');
      }
      catch (error) {
        const id = error.response.data?.id;
        const msg = error.response.data?.message;
        if (!!id) {
          console.info('Аниме сохранено с ИД', id);
        }
        if (!!msg) {
          console.error(msg);
        }
      }
    }
  },
  validations () {
    return {
      form: {
        rusName: {
          required: helpers.withMessage(`Заполните русское название.`, required)
        },
        romName: {
          required: helpers.withMessage(`Заполните название на ромадзи.`, required)
        },
        type: {
          value: {
            required: helpers.withMessage(`Выберите тип.`, required)
          }
        },
        genre: {
          value: {
            required: helpers.withMessage(`Выберите жанр.`, required)
          }
        },
        studio: {
          value: {
            required: helpers.withMessage(`Выберите студию.`, required)
          }
        },
        status: {
          value: {
            required: helpers.withMessage(`Выберите статус.`, required)
          }
        },
        episodeCount: {
          required: helpers.withMessage(`Заполните количество эпизодов.`, required)
        },
        episodeDuration: {
          required: helpers.withMessage(`Заполните продолжительность эпизода.`, required)
        },
        period: {
          required: helpers.withMessage(`Выберите период.`, required)
        }
      }
    }
  }
}
</script>

<style scoped>
.v-img {
  border-radius: 3px;
}
.ff-verdana {
  font-family: Verdana, serif;
}
.mr2-0{
  margin: 2px 0;
}
.mrl5{
  margin-left: 5px;
}
.mr10-0{
  margin: 10px 0;
}
.span-tag {
  margin-top: 5px;
  margin-left: 5px;
  cursor: pointer;
  display: inline-block;
  padding: 0 5px;
  border-radius: 3px;
  background-color: rgba(25, 88, 255, 0.5);
  transition: 0.1s;
}
.span-tag:hover {
  color: gold;
}
.item-link{
  color: gold;
  text-decoration: none;
}
.item-link:hover{
  text-decoration: underline;
}
.item_description{

}
</style>