<template>
  <v-tabs
      v-model="tabs"
      color="primary"
      align-tabs="center"
  >
    <v-tab value="1">Вход</v-tab>
    <v-tab value="2">Регистрация</v-tab>
  </v-tabs>
  <v-window v-model="tabs" class="pa-4">
    <v-window-item value="1">
      <v-form v-if="!loginForm.forgotPass">
        <v-text-field
            v-model="loginForm.username"
            label="Имя пользователя"
            variant="outlined"
            placeholder="Введите имя пользователя"
            prepend-inner-icon="mdi-account"
            :hide-details="true"
            :class="calcClasses"
        />
        <v-text-field
            v-model="loginForm.password"
            label="Пароль"
            variant="outlined"
            prepend-inner-icon="mdi-lock-outline"
            :error-messages="error == null ? '' : error"
        />
        <v-checkbox
            class="q-checkbox"
            v-model="loginForm.rememberMe"
            label="Запомнить меня"
            color="primary"
            :hide-details="true"
            density="comfortable"
        />
        <div class="text-center forgot-pass mb-2">
          <span @click="loginForm.forgotPass = true">Забыл пароль</span>
        </div>
        <v-btn variant="tonal"
               color="primary"
               :block="true"
               @click="enter(this.loginForm)"
        >
          Войти
        </v-btn>
      </v-form>
      <v-form v-else>
        <v-text-field
            v-model="loginForm.email"
            label="Email"
            variant="outlined"
            type="email"
            placeholder="Введите email"
            prepend-inner-icon="mdi-email-outline"
        />
        <div class="text-center forgot-pass mb-2">
          <span @click="loginForm.forgotPass = false">Вспомнил пароль</span>
        </div>
        <v-btn variant="tonal"
               color="primary"
               :block="true"
        >
          Восстановить
        </v-btn>
      </v-form>
    </v-window-item>
    <v-window-item value="2">
      <v-form>
        <v-text-field
            v-model="registrationForm.username"
            label="Имя пользователя"
            variant="outlined"
            placeholder="Введите имя пользователя"
            prepend-inner-icon="mdi-account"
        />
        <v-text-field
            v-model="registrationForm.email"
            label="Email"
            variant="outlined"
            type="email"
            placeholder="Введите email"
            prepend-inner-icon="mdi-email-outline"
        />
        <v-text-field
            v-model="registrationForm.password"
            label="Пароль"
            variant="outlined"
            type="password"
            prepend-inner-icon="mdi-lock-outline"
        />
        <v-text-field
            v-model="registrationForm.confirmPassword"
            label="Повторите пароль"
            variant="outlined"
            type="password"
            prepend-inner-icon="mdi-lock-outline"
        />
        <v-btn variant="tonal"
               color="primary"
               :block="true"
               @click="registration"
        >
          Зарегистрироваться
        </v-btn>
      </v-form>
    </v-window-item>
  </v-window>
</template>

<script>
import {mapActions, mapMutations, mapState} from "vuex";
import axios from "../../axios/http-common.js";

export default {
  name: 'QLogin',
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        forgotPass: false,
        email: ""
      },
      registrationForm: {
        username: "",
        email: "",
        password: "",
        confirmPassword: ""
      },
      tabs: null,
      dialog: false,
      loaded: false,
      authLoading: true,
    }
  },
  watch:{
    dialog: function(newVal, oldVal) {
      if (!oldVal) this.clearModal();
    },
    isAuth: function(newVal, oldVal) {
      if (newVal) {
        this.dialog = false;
        this.clearModal();
      }
    }
  },
  computed: {
    ...mapState({
      isAuth: state => state.auth.isAuth,
      error: state => state.auth.error
    }),
    calcClasses() {
      if (this.error != null) {
        return "mb-4 v-field--error";
      }
      else return "mb-4";
    }
  },
  methods: {
    ...mapActions('auth', ['enter']),
    ...mapMutations(['auth/setErrorState']),
    async registration() {
      try{
        const data = {
          username: this.registrationForm.username,
          email: this.registrationForm.email,
          password: this.registrationForm.password,
          confirmPassword: this.registrationForm.confirmPassword
        }
        const response = await axios.post('/auth/registration', data);
        console.log('OK', response.data);
      } catch (error) {
        const response = error.response.data;
        console.log('ERROR', response);
      }
    },
    clearModal() {
      this.loginForm.username = '';
      this.loginForm.email = '';
      this.loginForm.password = '';
      this.loginForm.rememberMe = false;
      this.loginForm.forgotPass = false;
      this["auth/setErrorState"](null);
    }
  }
}
</script>

<style scoped>

</style>