import { CircleCheck } from 'lucide-react'

export default function Logo({ light = false }: { light?: boolean }) {
  return (
    <div className="relative flex items-center gap-2">
      <span
        className={`flex size-9 items-center justify-center rounded-xl text-white ${light ? 'bg-white/20' : 'bg-brand-600'}`}
      >
        <CircleCheck className="size-5" />
      </span>
      <span className={`text-xl font-bold tracking-tight ${light ? 'text-white' : 'text-slate-900'}`}>FineX</span>
    </div>
  )
}
