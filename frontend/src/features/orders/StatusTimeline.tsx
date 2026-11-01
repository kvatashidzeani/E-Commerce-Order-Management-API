import type { OrderStatusHistory } from '@/api/types'
import { formatDateTime } from '@/lib/formatters'

type StatusTimelineProps = {
  history: OrderStatusHistory[]
}

export function StatusTimeline({ history }: StatusTimelineProps) {
  const ordered = [...history].sort(
    (a, b) => new Date(a.changedAt).getTime() - new Date(b.changedAt).getTime(),
  )

  if (ordered.length === 0) {
    return <p className="text-sm text-slate-500">No status history yet.</p>
  }

  return (
    <ol className="space-y-3 border-l border-slate-200 pl-4">
      {ordered.map((entry, index) => (
        <li key={`${entry.changedAt}-${entry.toStatus}-${index}`} className="relative">
          <span className="absolute -left-[1.35rem] top-1.5 h-2.5 w-2.5 rounded-full bg-teal-700" />
          <p className="text-sm font-medium">
            {entry.fromStatus ? `${entry.fromStatus} → ${entry.toStatus}` : entry.toStatus}
          </p>
          <p className="text-xs text-slate-500">{formatDateTime(entry.changedAt)}</p>
          {entry.comment && <p className="text-sm text-slate-600">{entry.comment}</p>}
        </li>
      ))}
    </ol>
  )
}
