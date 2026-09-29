import { Bell } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { Link } from 'react-router'
import { iconButtonStyles } from '../../components/ui/buttonStyles'
import { useUnreadCount } from './useUnreadCount'

// A koncepcióképek csengője (a lila felső sávon üveghatású): olvasatlan értesítésnél sárga jelvény mutatja a számukat
export default function NotificationBell() {
  const { data: count = 0 } = useUnreadCount()

  return (
    <Link
      to="/notifications"
      aria-label={count > 0 ? `Értesítések, ${count} olvasatlan` : 'Értesítések'}
      title="Értesítések"
      className={iconButtonStyles('light')}
    >
      <Bell />
      <AnimatePresence>
        {count > 0 && (
          <motion.span
            key="badge"
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            exit={{ scale: 0 }}
            transition={{ type: 'spring', bounce: 0.5, duration: 0.4 }}
            className="absolute -top-1.5 -right-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-bold text-brand-950 shadow-md shadow-brand-950/30"
          >
            {count > 9 ? '9+' : count}
          </motion.span>
        )}
      </AnimatePresence>
    </Link>
  )
}
