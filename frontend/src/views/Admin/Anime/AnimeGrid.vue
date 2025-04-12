<template>
  <v-card-actions class="justify-center">
    <v-btn variant="outlined"
           v-text="'Создать'"
           width="200px"
           :href="ADMIN_ROUTE.ANIME_NEW.path"
    />
  </v-card-actions>
  <v-row>
    <v-col v-for="anime in animes"
           :key="anime.id"
           :cols="computedColumnCount"
    >
      <router-link :to="{ name: ANIME_DETAIL_ROUTE(), params: { id: anime.id } }" class="text-decoration-none">
        <v-card color="transparent" variant="flat">
          <v-img v-if="anime.posterURL"
                 :src="anime.posterURL"
                 class="rounded"
                 :max-height="css.imgHeight"
          />
          <v-card-text class="px-0 pt-1">
            <h3 class="max-line-2">{{anime.ruName}}</h3>
            <div class="container">
              <div class="text-left">{{anime.typeName}}</div>
              <div class="text-right">{{anime.startDate ? new Date(anime.startDate).getFullYear() : ''}}</div>
            </div>
          </v-card-text>
        </v-card>
      </router-link>
    </v-col>
  </v-row>
</template>

<script>
import axios from "/src/axios/http-common.js";
import {ADMIN_ROUTE, ANIME_DETAIL_ROUTE} from "../../../router/routeConstants.js";

export default {
  data() {
    return {
      animes: [],
      css: {
        imgHeight: '490px',
        imgWidth: 'auto'
      }
    }
  },
  methods: {
    ANIME_DETAIL_ROUTE() {
      return ANIME_DETAIL_ROUTE;
    }
  },
  async created() {
    const animes = await axios.get('anime');//todo вынести запрос
    if (!!animes.data) {
      this.animes = animes.data;
    }
    else {
      console.log("Ничего не найдено");
    }
  },
  computed: {
    ADMIN_ROUTE() {
      return ADMIN_ROUTE
    },
    computedColumnCount() {
      if (this.$vuetify.display.lgAndUp) { // если экран большой
        return 2;
      }
      else if (this.$vuetify.display.md || this.$vuetify.display.sm) { // если экран средний
        return 3;
      }
      else if (this.$vuetify.display.xs) {
        return 12;
      }
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

