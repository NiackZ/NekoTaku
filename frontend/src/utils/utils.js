import axios from '/src/axios/http-common'

export async function checkRights(userId, rights) {
    return await axios.post('/rights/check-rights', {userId, rights});
}

export async function getMarks() {
    return await axios.get("/marks");
}

export function getAnimeInfoForGrid() {
    return axios.get("/anime/compact");
}

export async function getAnimeInfo(id) {
    return await axios.get(`/anime/${id}`);
}

export async function updateUser(data) {
    return await axios.put(`/users/${data.id}`, data);
}

export function isEmpty(obj) {
    return obj === null || obj === undefined || obj?.length === 0;
}

export function isNotEmpty(obj) {
    return !isEmpty(obj);
}
