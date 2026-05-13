import axios from 'axios'

const baseURL = '/api/auth'

export const registerUser = (data) =>
  axios.post(`${baseURL}/register`, data).then((r) => r.data)

export const loginUser = (data) =>
  axios.post(`${baseURL}/login`, data).then((r) => r.data)

export const refreshToken = (token) =>
  axios.post(`${baseURL}/refresh`, { refreshToken: token }).then((r) => r.data)

export const logoutUser = (token) =>
  axios.post(`${baseURL}/logout`, { refreshToken: token })
