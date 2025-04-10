<template>
  <v-row v-if="animeData">
    <v-col cols="12" md="5" lg="4" xxl="3">
      <v-img :src="animeData.posterURL"></v-img>
    </v-col>
    <v-col cols="12" md="7" lg="8" xxl="9" class="ff-verdana fs12pt">
        <div class="fs20pt">
          <span class="rus_name deep-purple" v-text="animeData.ruName"></span> / <span class="rom_name deep-purple" v-text="animeData.romajiName"></span>
        </div>

        <div v-if="animeData.type?.name" class="mr2-0">
          <span class="deep-purple">Тип:</span>
          <span class="span-tag" v-text="animeData.type.name"></span>
        </div>

        <div v-if="animeData.genres?.length > 0" class="mr2-0">
          <span class="deep-purple">Жанр:</span>
          <span v-for="genre in animeData.genres"
                :key="genre.id" class="span-tag" v-text="genre.name"></span>
        </div>

        <div v-if="animeData.studios?.length > 0" class="mr2-0">
          <span class="deep-purple">Студия:</span>
          <span v-for="studio in animeData.studios"
                :key="studio.id" class="span-tag" v-text="studio.name"></span>
        </div>

      <div v-if="animeData.status?.name" class="mr2-0">
        <span class="deep-purple">Тип:</span>
        <span class="span-tag" v-text="animeData.status.name"></span>
      </div>

        <div v-if="animeData.startDate || animeData.endDate" class="mr2-0">
          <span class="deep-purple" v-text="animeData.endDate ? 'Сезон:' : 'Дата выхода:'"></span>
          <span class="mrl5" v-text="new Date(animeData.startDate).toLocaleDateString() + (animeData.endDate ? ' - ' + new Date(animeData.endDate).toLocaleDateString() : '')">
            </span>
        </div>

        <div v-if="animeData.episodeCount || animeData.episodeDuration" class="mr2-0">
          <span class="deep-purple">Продолжительность:</span>
          <span class="mrl5" v-text="animeData.episodeCount > 1
              ? `${animeData.episodeCount} эп. по ~ ${animeData.episodeDuration} мин.`
              : `${animeData.episodeDuration} мин.`"></span>
        </div>

        <div v-if="animeData.links?.length > 0" class="mr2-0">
          <span class="deep-purple">Ссылки:</span>
          <span v-for="link in animeData.links"
                :key="link.id" class="mrl5">
                  <a class="item-link" :href=link.url v-text="link.name" target="_blank"></a>
                </span>
        </div>

        <div v-if="animeData.marks?.length > 0" class="item_marks">
          <span class="deep-purple">Метки:</span>
          <span v-for="mark in animeData.marks"
                :key="mark.id" v-text="mark.name"
                class="span-tag"></span>
        </div>

        <span class="d-block deep-purple" style="margin-top:20px;">Описание:</span>
        <div class="d-block text-justify" v-html="animeData.description">
        </div>
    </v-col>
  </v-row>
</template>

<script>
import {getAnimeInfo} from "../../utils/utils.js";

export default {
  name: 'QContentItem',
  props: {
    itemId: Number
  },
  data() {
    return {
      animeData: null
    }
  },
  async mounted() {
    const animeId = this.$props.itemId;
    if (animeId) {
      this.animeData = (await getAnimeInfo(animeId)).data;
      document.title = this.animeData.romajiName; // устанавливаем заголовок страницы
    }
  }
}
</script>

<style scoped>
.v-img {
  border-radius: 3px;
}
.fs20pt {
  font-size: 20pt;
}
.fs12pt {
  font-size: 12pt;
}
.ff-verdana {
  font-family: Verdana,serif;
}
.mr2-0 {
  margin: 2px 0;
}
.mrl5 {
  margin-left: 5px;
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