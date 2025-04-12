import Main from "../views/Main.vue";
import Item from "../views/Public/Content/Item.vue";
import {PATH, SITE_NAME} from "../constants/constants.js";

export const mainRoutes = [
    {
        path: '/',
        component: Main,
        meta: {
            title: SITE_NAME
        }
    },
    {
        path: `/${PATH.ANIME}/:id(\\d+)`,
        component: Item,
        props: true,
        meta: {
            title: "Anime"
        }
    }
];
