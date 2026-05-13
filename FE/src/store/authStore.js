import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,

      setAuth: (data) => set({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: {
          id: data.userId,
          email: data.email,
          fullName: data.fullName,
        },
      }),

      clearAuth: () => set({
        accessToken: null,
        refreshToken: null,
        user: null,
      }),
    }),
    { name: 'minijira-auth' }
  )
)

export default useAuthStore
