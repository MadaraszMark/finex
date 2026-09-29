import { motion } from 'motion/react'
import { useState } from 'react'
import { Outlet, useLocation } from 'react-router'
import AmbientBackground from './AmbientBackground'
import BottomNav from './BottomNav'
import MenuSheet from './MenuSheet'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

// A bejelentkezés utáni oldalak kerete: asztali nézetben oldalsáv, mobilon alsó menüsor, felül a fejléc.
// Az oldalak tetején a lila fejlécsávot a PageHeader rajzolja; a tartalmi oszlop vízszintesen levágja (overflow-x-clip).
export default function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()

  return (
    <div className="relative isolate min-h-dvh lg:flex">
      {/* Billentyűzettel navigálóknak: a menü átugorható */}
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:fixed focus:top-3 focus:left-3 focus:z-50 focus:rounded-xl focus:bg-card focus:px-4 focus:py-2 focus:font-semibold focus:shadow-lg"
      >
        Ugrás a tartalomra
      </a>

      <AmbientBackground />
      <Sidebar />

      <div className="relative flex min-w-0 flex-1 flex-col overflow-x-clip">
        {/* Lila csík a felső sáv mögött arra az esetre, ha egy oldal (pl. betöltés közben) még nem rajzolt fejlécsávot */}
        <div
          aria-hidden
          className="pointer-events-none absolute inset-x-0 top-0 -z-10 h-16 bg-[linear-gradient(135deg,#a878f5_0%,#8a4fe0_45%,#5f33b5_100%)] lg:h-20 dark:bg-[linear-gradient(135deg,#231440_0%,#150c2a_50%,#0d0a16_100%)]"
        />
        <TopBar />
        <main id="main" tabIndex={-1} className="flex-1 px-4 pt-6 pb-32 outline-none sm:px-6 lg:px-10 lg:pt-8 lg:pb-12">
          {/* Oldalváltáskor az új oldal finoman a helyére csúszik (a cím és a kártyák saját animációval úsznak be) */}
          <motion.div
            key={location.pathname}
            initial={{ y: 14 }}
            animate={{ y: 0 }}
            transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
            className="mx-auto w-full max-w-6xl"
          >
            <Outlet />
          </motion.div>
        </main>
      </div>

      <BottomNav onOpenMenu={() => setMenuOpen(true)} />
      <MenuSheet open={menuOpen} onClose={() => setMenuOpen(false)} />
    </div>
  )
}
