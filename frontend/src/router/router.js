import {createRouter, createWebHistory} from "vue-router";
import Main from "../pages/Main.vue";
import Admin from "../pages/Admin/Admin.vue";
import CreateTitle from "../pages/Admin/CreateTitle.vue";
import Item from "../pages/Content/Item.vue";
import Profile from "../pages/User/Profile.vue";

const routes = [
    {
        path: '/',
        component: Main
    },
    {
        path: '/admin',
        component: Admin
    },
    {
        path: '/admin/create/title',
        component: CreateTitle
    },
    {
        path: '/item/:id',
        component: Item,
    },
    {
        path: '/profile',
        component: Profile
    }
]

const router = createRouter({
    routes,
    history: createWebHistory(import.meta.env.BASE_URL)
})

export default router