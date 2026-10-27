import { ArrowRight, ChartColumn, CreditCard, PiggyBank, Send } from 'lucide-react'
import { motion } from 'motion/react'
import AuroraBackground from '../../../components/brand/AuroraBackground'
import Logo, { LogoGlyph } from '../../../components/brand/Logo'
import ButtonLink from '../../../components/ui/ButtonLink'
import { usePointerParallax } from '../../../hooks/usePointerParallax'
import { useSpotlight } from '../../../hooks/useSpotlight'
import { headlineWord, staggerContainer, staggerItem } from '../../../lib/motion'
import { ThemeToggleButton } from '../../../theme/ThemeControls'
import CardShowcase from '../components/CardShowcase'
import FloatingDecorations from '../components/FloatingDecorations'
import HeadlineWords from '../components/HeadlineWords'

// A valódi funkciók rövid leírása, számok és kitalált adatok nélkül
const features = [
  { icon: CreditCard, title: 'Számlák és kártyák', text: 'Forint-, euró- és dollárszámla; kártyatiltás és limitek egy érintéssel.' },
  { icon: Send, title: 'Gyors utalás', text: 'Lépésről lépésre, mentett kedvezményezettekkel; FineX-es számlára azonnal megérkezik.' },
  { icon: PiggyBank, title: 'Megtakarítási célok', text: 'Tűzz ki célt, kövesd a haladásod, a megtakarításod pedig havonta kamatozik.' },
  { icon: ChartColumn, title: 'Átlátható költések', text: 'Havi bevétel és kiadás, költés kategóriánként, a legnagyobb tételek egy pillantással.' },
]

// Nyitóképernyő a belépéssel közös aurora / üveg stílusban; nagy képernyőn mellette a bankkártya
export default function WelcomePage() {
  const pointer = usePointerParallax()
  useSpotlight()

  return (
    <div className="relative isolate flex min-h-dvh flex-col overflow-hidden text-white">
      <AuroraBackground />
      <FloatingDecorations pointer={pointer} />

      <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 pt-5 sm:px-10 sm:pt-6">
        <Logo tone="light" />
        <ThemeToggleButton tone="light" />
      </header>

      <main className="mx-auto grid w-full max-w-6xl flex-1 grid-cols-1 gap-10 px-4 pt-8 pb-8 sm:items-center sm:px-10 sm:py-10 xl:grid-cols-[minmax(0,1fr)_30rem] xl:gap-16">
        <section className="flex flex-col text-center xl:text-left">
          <motion.div
            variants={staggerContainer}
            initial="hidden"
            animate="show"
            className="flex flex-1 flex-col items-center justify-center sm:flex-none xl:items-start"
          >
            <motion.p
              variants={staggerItem}
              className="inline-flex items-center gap-2 rounded-full border border-white/30 bg-white/10 py-1.5 pr-4 pl-2 text-sm font-semibold backdrop-blur-md"
            >
              <LogoGlyph className="size-6" />
              Üdvözöl a FineX
            </motion.p>
            <h1 className="mt-6 text-4xl leading-[1.05] font-extrabold tracking-tight min-[400px]:text-5xl sm:text-6xl xl:text-7xl">
              <HeadlineWords text="A pénzügyeid" />
              <HeadlineWords text="egy helyen." highlight />
            </h1>
            <motion.p variants={headlineWord} className="mt-5 max-w-md text-base text-white/80 sm:text-lg">
              Számlák, kártyák, utalások és megtakarítások – gyorsan, átláthatóan, biztonságosan.
            </motion.p>
          </motion.div>

          {/* A gombok maguk úsznak be: szülő animációja alatt nem látszana az üveghatásuk */}
          <div className="mt-10 flex flex-col gap-4 sm:flex-row sm:justify-center xl:justify-start">
            <ButtonLink
              to="/register"
              variant="deep"
              size="lg"
              rightIcon={<ArrowRight />}
              style={{ animationDelay: '450ms' }}
              className="justify-between motion-safe:animate-fade-up sm:w-60"
            >
              Regisztráció
            </ButtonLink>
            <div aria-hidden className="h-px bg-white/20 sm:hidden" />
            <ButtonLink
              to="/login"
              variant="glass"
              size="lg"
              rightIcon={<ArrowRight />}
              style={{ animationDelay: '550ms' }}
              className="justify-between motion-safe:animate-fade-up sm:w-60"
            >
              Bejelentkezés
            </ButtonLink>
          </div>
        </section>

        <CardShowcase pointer={pointer} className="hidden justify-self-end xl:block" />
      </main>

      <motion.ul
        variants={staggerContainer}
        initial="hidden"
        animate="show"
        className="mx-auto hidden w-full max-w-6xl grid-cols-2 gap-4 px-4 pb-8 sm:grid sm:px-10 lg:grid-cols-4 lg:gap-6"
      >
        {features.map((feature) => (
          <motion.li
            key={feature.title}
            variants={staggerItem}
            whileHover={{ y: -4 }}
            className="spotlight flex flex-col rounded-3xl border border-white/25 bg-white/10 p-5 backdrop-blur-md [--spot-color:rgb(255_255_255/0.12)]"
          >
            <span className="flex size-11 items-center justify-center rounded-2xl border border-white/30 bg-white/15">
              <feature.icon aria-hidden className="size-5" />
            </span>
            <p className="mt-4 font-bold">{feature.title}</p>
            <p className="mt-1 text-sm text-white/75">{feature.text}</p>
          </motion.li>
        ))}
      </motion.ul>

      <footer className="mx-auto w-full max-w-6xl px-4 pb-[calc(1.5rem+env(safe-area-inset-bottom))] text-xs text-white/60 sm:px-10">
        FineX · szakdolgozati projekt
      </footer>
    </div>
  )
}
