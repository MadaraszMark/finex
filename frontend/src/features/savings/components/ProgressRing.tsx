import { motion, useReducedMotion } from 'motion/react'
import { useId } from 'react'

interface ProgressRingProps {
  // 0–100 (a célösszeg feletti gyűjtésnél 100-nál megáll a kör, a felirat a valós értéket mutatja)
  percent: number
  size?: number
  label: string
}

const STROKE = 10

// Körkörös haladásjelző a megtakarítási célokhoz: a márka színátmenetével töltődik fel, közepén a százalék.
// A felolvasóprogramnak meter szerepű elem adja át az értéket.
export default function ProgressRing({ percent, size = 112, label }: ProgressRingProps) {
  const reduceMotion = useReducedMotion()
  const gradientId = useId()
  const radius = (size - STROKE) / 2
  const circumference = 2 * Math.PI * radius
  const clamped = Math.min(Math.max(percent, 0), 100)

  return (
    <div
      role="meter"
      aria-label={label}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(clamped)}
      aria-valuetext={`${Math.round(percent)}%`}
      className="relative shrink-0"
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90" aria-hidden>
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
            <stop offset="0" stopColor="#b58af7" />
            <stop offset="1" stopColor="#6836b0" />
          </linearGradient>
        </defs>
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" strokeWidth={STROKE} className="stroke-brand-100 dark:stroke-brand-950" />
        <motion.circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={`url(#${gradientId})`}
          strokeWidth={STROKE}
          strokeLinecap="round"
          strokeDasharray={circumference}
          initial={{ strokeDashoffset: circumference }}
          animate={{ strokeDashoffset: circumference * (1 - clamped / 100) }}
          transition={reduceMotion ? { duration: 0 } : { duration: 1.2, ease: [0.22, 1, 0.36, 1] }}
        />
      </svg>
      <span className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-xl font-extrabold tracking-tight text-foreground">{Math.round(percent)}%</span>
        <span className="text-[11px] font-medium text-muted-foreground">a célból</span>
      </span>
    </div>
  )
}
