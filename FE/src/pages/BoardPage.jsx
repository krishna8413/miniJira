import { useParams } from 'react-router-dom'

export default function BoardPage() {
  const { projectId } = useParams()

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Kanban Board</h1>
        <p className="text-gray-400 text-sm">Project {projectId} — Board coming soon.</p>
      </div>
    </div>
  )
}
