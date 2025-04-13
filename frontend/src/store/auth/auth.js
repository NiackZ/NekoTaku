import axios from '/src/axios/http-common'
import { ACCESS_TOKEN, REFRESH_TOKEN } from "/src/constants/constants.js";
import {checkRights, isEmpty} from "../../utils/utils.js";

const auth = {
    namespaced: true,
    state: {
        isAuth: false,
        authLoading: false,
        user: null,
        error: null,
        authRequiredRoute: null, // Храним маршрут, куда пользователь хотел перейти
    },
    actions: {
        async enter({commit, dispatch}, data) {
            try {
                const {username, password} = data;
                if (isEmpty(username) || isEmpty(password)) return;
                const response = await axios.post('/auth/login', {username, password});
                console.log('OK', response.data);
                if (response.data.jwt && response.data.jwt[ACCESS_TOKEN] && response.data.jwt[REFRESH_TOKEN]) {
                    const accessToken = response.data.jwt[ACCESS_TOKEN];
                    const refreshToken = response.data.jwt[REFRESH_TOKEN];
                    const userData = response.data.user;
                    dispatch('saveTokens', {accessToken, refreshToken});
                    commit('setAuthState', true);
                    commit('setErrorState', null);
                    commit('setUserState', userData);
                }
            } catch (error) {
                const response = error.response;
                console.log('ERROR', response);
                commit('setErrorState', response.data.message);
                alert(response.data);
            }
        },
        async logout({commit, dispatch}) {
            try {
                await axios.post('/auth/logout')
                dispatch('deleteTokens');
                commit('setUserState', null);
                commit('setAuthState', false);
            } catch (error) {
                console.log(error);
            }
        },
        async validateToken({commit, dispatch}) {
            const token = localStorage.getItem(ACCESS_TOKEN);
            let result = false;
            if (!!token) {
                commit('setLoadingState', true)
                try {
                    const response = await axios.post('/auth/validate-token');
                    if (response.status === 200) {
                        commit('setAuthState', true);
                        commit('setUserState', response.data.user);
                        result = true;
                    }
                }
                catch (error) {
                    console.log('ERROR', error);
                    dispatch('deleteTokens');
                }
                commit('setLoadingState', false)
            }
            return result;
        },
        saveTokens({ commit }, { accessToken, refreshToken }) {
            localStorage.setItem(ACCESS_TOKEN, accessToken);
            localStorage.setItem(REFRESH_TOKEN, refreshToken);
        },
        deleteTokens() {
            localStorage.removeItem(ACCESS_TOKEN);
            localStorage.removeItem(REFRESH_TOKEN);
        },
        async checkAccess({ state }, requiredRights) {
            if (!state.user || !state.user.id || !requiredRights || requiredRights.length === 0) {
                return true;
            }
            try {
                const response = await checkRights(state.user?.id, requiredRights);
                return response.data;
            } catch (error) {
                console.error("Ошибка при проверке прав:", error);
                return false;
            }
        },
    },
    mutations: {
        setAuthState(state, isAuth) {
            state.isAuth = isAuth;
        },
        setLoadingState(state, loading) {
            state.authLoading = loading;
        },
        setErrorState(state, error) {
            state.error = error;
        },
        setUserState(state, user) {
            state.user = user;
        },
        setAuthRequiredRoute(state, route) {
            state.authRequiredRoute = route; // Сохраняем маршрут
        },
        clearAuthRequiredRoute(state) {
            state.authRequiredRoute = null; // Очищаем после успешной авторизации
        }
    }
}

export default auth