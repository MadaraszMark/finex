import { Circle, CircleCheck, CircleX, LoaderCircle } from 'lucide-react'
import type { Check, CheckStatus } from '../lib/checks'

function StatusIcon({ status }: { status: CheckStatus }) {
  switch (status) {
    case 'ok':
      return <CircleCheck className="size-5 shrink-0 text-emerald-500" aria-label="Rendben" />
    case 'error':
      return <CircleX className="size-5 shrink-0 text-red-500" aria-label="Hiba" />
    case 'pending':
      return <LoaderCircle className="size-5 shrink-0 animate-spin text-brand-500" aria-label="Folyamatban" />
    case 'idle':
      return <Circle className="size-5 shrink-0 text-slate-300" aria-label="Még nem ellenőrzött" />
  }
}

export default function SystemCheck({ title, checks }: { title: string; checks: Check[] }) {
  return (
    <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <h2 className="text-xs font-semibold tracking-wide text-slate-500 uppercase">{title}</h2>
      <ul className="mt-4 space-y-3">
        {checks.map((check) => (
          <li key={check.label} className="flex items-start gap-3">
            <StatusIcon status={check.status} />
            <div className="min-w-0">
              <p className="text-sm font-medium text-slate-800">{check.label}</p>
              {check.detail && <p className="text-xs break-words text-slate-500">{check.detail}</p>}
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
