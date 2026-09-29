import { ChevronRight } from 'lucide-react'
import { motion, useScroll, useTransform } from 'motion/react'
import { Link } from 'react-router'
import Logo from '../../components/brand/Logo'
import NotificationBell from '../../features/notifications/NotificationBell'
import { useRouteTitle } from '../../hooks/useRouteTitle'
import UserMenu from './UserMenu'
import { useBreadcrumb } from './useBreadcrumb'

// Felső sáv: mobilon a logó, asztali nézetben a menübeli hely ("Pénzmozgás › Utalás"); jobbra a csengő és a profilmenü.
// Az oldal tetején átlátszó (mögötte a lila fejlécsáv látszik), görgetéskor fokozatosan saját lila üveghátteret kap.
export default function TopBar() {
  const breadcrumb = useBreadcrumb()
  const title = useRouteTitle()
  const { scrollY } = useScroll()
  const backdropOpacity = useTransform(scrollY, [0, 48], [0, 1])

  return (
    <header className="sticky top-0 z-30 px-4 text-white sm:px-6 lg:px-10">
      <motion.div
        aria-hidden
        style={{ opacity: backdropOpacity }}
        className="absolute inset-0 border-b border-white/15 bg-[linear-gradient(135deg,rgb(124_69_209/0.9),rgb(85_45_142/0.9))] shadow-[0_12px_32px_-20px_rgb(40_16_90/0.8)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(135deg,rgb(35_20_64/0.88),rgb(21_12_42/0.88))]"
      />

      <div className="relative mx-auto flex h-16 w-full max-w-6xl items-center gap-3 lg:h-20">
        <Link to="/" aria-label="FineX főoldal" className="rounded-2xl focus-visible:ring-4 focus-visible:ring-white/40 focus-visible:outline-none lg:hidden">
          <Logo tone="light" size="sm" />
        </Link>

        <p className="hidden items-center gap-1.5 text-sm lg:flex">
          {breadcrumb ? (
            <>
              <span className="text-white/65">{breadcrumb.section}</span>
              <ChevronRight aria-hidden className="size-4 text-white/45" />
              <span className="font-semibold">{breadcrumb.label}</span>
            </>
          ) : (
            <span className="font-semibold">{title}</span>
          )}
        </p>

        <div className="ml-auto flex items-center gap-2 sm:gap-3">
          <NotificationBell />
          <UserMenu />
        </div>
      </div>
    </header>
  )
}
