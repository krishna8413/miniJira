import useAuthStore from '../store/authStore'

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const clearAuth = useAuthStore((s) => s.clearAuth)

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-5xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
            <p className="text-gray-500 text-sm">Welcome back, {user?.fullName}</p>
          </div>
          <button
            onClick={clearAuth}
            className="text-sm text-gray-500 hover:text-red-600 transition"
          >
            Logout
          </button>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-6 text-center text-gray-400">
          Your workspaces will appear here.
        </div>
      </div>
    </div>
  )
}
