import { PiggyBank, ShieldCheck, Zap } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { Link, useLocation, useOutlet } from 'react-router'
import AuroraBackground from '../../components/brand/AuroraBackground'
import Logo from '../../components/brand/Logo'
import AnimatedHeight from '../../components/ui/AnimatedHeight'
import { usePointerParallax } from '../../hooks/usePointerParallax'
import { headlineWord, staggerContainer } from '../../lib/motion'
import { ThemeToggleButton } from '../../theme/ThemeControls'
import CardShowcase from './components/CardShowcase'
import FloatingDecorations from './components/FloatingDecorations'

interface Copy {
  title: [string, string]
  subtitle: string
}

// Oldalanként más cím; a második sor animált színátmenetes kiemelés
const copies: Record<string, Copy> = {
  '/login': {
    title: ['Örülünk, hogy', 'újra itt vagy!'],
    subtitle: 'Lépj be, és kezeld a számláidat, kártyáidat és megtakarításaidat egy helyen.',
  },
  '/register': {
    title: ['Nyiss fiókot', 'pár perc alatt!'],
    subtitle: 'Regisztráció után azonnal kapsz egy forint folyószámlát és egy bankkártyát.',
  },
}

const highlights = [
  { icon: <Zap />, text: 'Azonnali utalás FineX-es számlák között' },
  { icon: <PiggyBank />, text: 'Megtakarítási célok havi kamattal' },
  { icon: <ShieldCheck />, text: 'Kártyáid és a biztonságod egy helyen' },
]

// A cím szavanként, egymás után élesedik ki; oldalváltáskor az új cím úszik be
function Headline({ copy, pathname }: { copy: Copy; pathname: string }) {
  return (
    <AnimatePresence mode="wait">
      <motion.div key={pathname} variants={staggerContainer} initial="hidden" animate="show" exit="exit">
        <h1 className="text-4xl leading-[1.05] font-extrabold tracking-tight sm:text-5xl xl:text-6xl">
          <span className="block">
            {copy.title[0].split(' ').map((word, index) => (
              <motion.span key={`${index}-${word}`} variants={headlineWord} className="mr-[0.25em] inline-block">
                {word}
              </motion.span>
            ))}
          </span>
          <span className="block">
            {copy.title[1].split(' ').map((word, index) => (
              <motion.span
                key={`${index}-${word}`}
                variants={headlineWord}
                className="mr-[0.25em] inline-block bg-[linear-gradient(90deg,#ffffff,#f5d0fe,#ddd6fe,#ffffff)] bg-[length:200%_auto] bg-clip-text pb-1 text-transparent motion-safe:animate-gradient-x"
              >
                {word}
              </motion.span>
            ))}
          </span>
        </h1>
        <motion.p variants={headlineWord} className="mt-5 hidden max-w-md text-lg text-white/80 sm:block">
          {copy.subtitle}
        </motion.p>
      </motion.div>
    </AnimatePresence>
  )
}

// A belépés és a regisztráció közös kerete (útvonal-szintű layout): a háttér, a cím és a kártya helyben marad,
// oldalváltáskor csak a kártya tartalma cserélődik, a kártya magassága pedig simán igazodik.
export default function AuthLayout() {
  const location = useLocation()
  const outlet = useOutlet()
  const pointer = usePointerParallax()
  const copy = copies[location.pathname] ?? copies['/login']

  return (
    <div className="relative isolate flex min-h-dvh flex-col overflow-hidden text-white">
      <AuroraBackground />
      <FloatingDecorations pointer={pointer} />

      <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 pt-5 sm:px-10 sm:pt-6">
        <Link to="/welcome" aria-label="FineX nyitóoldal" className="rounded-2xl focus-visible:ring-4 focus-visible:ring-white/50 focus-visible:outline-none">
          <Logo tone="light" />
        </Link>
        <ThemeToggleButton tone="light" />
      </header>

      <main className="mx-auto grid w-full max-w-6xl flex-1 grid-cols-1 items-center gap-10 px-4 py-10 sm:px-10 lg:grid-cols-[minmax(0,1fr)_28rem] lg:gap-16">
        <section>
          <Headline copy={copy} pathname={location.pathname} />

          {/* Közepes képernyőn a kiemelt funkciók, nagy képernyőn a bankkártyás "kirakat" */}
          <motion.ul
            variants={staggerContainer}
            initial="hidden"
            animate="show"
            className="mt-10 hidden space-y-3 lg:block xl:hidden"
          >
            {highlights.map((highlight) => (
              <motion.li
                key={highlight.text}
                variants={headlineWord}
                className="flex w-fit items-center gap-3 rounded-2xl border border-white/25 bg-white/10 px-4 py-3 text-sm font-medium backdrop-blur-md [&_svg]:size-5"
              >
                {highlight.icon}
                {highlight.text}
              </motion.li>
            ))}
          </motion.ul>
          <CardShowcase pointer={pointer} className="mt-16 hidden xl:block" />
        </section>

        <motion.section
          initial={{ opacity: 0, y: 32, scale: 0.97 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ type: 'spring', stiffness: 110, damping: 18 }}
          className="relative w-full max-w-md justify-self-center rounded-[2rem] border border-white/50 bg-card/90 p-6 text-foreground shadow-[0_40px_80px_-30px_rgb(20_8_45/0.6)] backdrop-blur-2xl sm:p-9 lg:justify-self-end dark:border-white/10 dark:bg-card/80"
        >
          {/* Halvány lila fénycsík a kártya tetején */}
          <div aria-hidden className="absolute inset-x-10 -top-px h-px bg-linear-to-r from-transparent via-brand-400 to-transparent" />

          <AnimatedHeight>
            <AnimatePresence mode="wait" initial={false}>
              <motion.div
                key={location.pathname}
                initial={{ opacity: 0, x: 24 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -24 }}
                transition={{ duration: 0.25, ease: 'easeOut' }}
              >
                {outlet}
              </motion.div>
            </AnimatePresence>
          </AnimatedHeight>
        </motion.section>
      </main>

      <footer className="mx-auto w-full max-w-6xl px-4 pb-6 text-xs text-white/60 sm:px-10">FineX · szakdolgozati projekt</footer>
    </div>
  )
}
