import { Circle, CircleCheck } from 'lucide-react'
import { motion } from 'motion/react'
import { cn } from '../../../lib/cn'
import { passwordRules } from '../schemas'

// Gépelés közben mutatja, mely jelszó-követelmények teljesülnek már (a pipa "felugrik", amikor teljesül)
export default function PasswordChecklist({ password }: { password: string }) {
  return (
    <ul aria-label="Jelszó követelmények" className="mt-2.5 flex flex-wrap gap-x-4 gap-y-1.5">
      {passwordRules.map((rule) => {
        const satisfied = rule.test(password)
        return (
          <li
            key={rule.id}
            className={cn('flex items-center gap-1.5 text-xs font-semibold transition-colors', satisfied ? 'text-success' : 'text-muted-foreground')}
          >
            <motion.span
              key={String(satisfied)}
              initial={{ scale: 0.4, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ type: 'spring', stiffness: 500, damping: 20 }}
              className="flex"
            >
              {satisfied ? <CircleCheck aria-hidden className="size-4" /> : <Circle aria-hidden className="size-4" />}
            </motion.span>
            {rule.label}
            <span className="sr-only">{satisfied ? '(teljesül)' : '(még nem teljesül)'}</span>
          </li>
        )
      })}
    </ul>
  )
}
