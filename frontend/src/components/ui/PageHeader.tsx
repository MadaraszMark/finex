import { motion, useTransform } from 'motion/react'
import type { ReactNode } from 'react'
import { usePointerParallax } from '../../hooks/usePointerParallax'
import { cn } from '../../lib/cn'
import { headlineWord, staggerContainer } from '../../lib/motion'
import AuroraBackground from '../brand/AuroraBackground'

interface PageHeaderProps {
  title: string
  // A cím végén színátmenetesen hullámzó kiemelés (pl. a felhasználó keresztneve)
  highlight?: string
  description?: string
  eyebrow?: string
  actions?: ReactNode
  className?: string
}

// Az oldalak fejléce a belépő oldalak lila "aurora" hátterén. A sáv képernyőszélességű (a tartalmi oszlop levágja),
// felfelé a felső sáv mögé fut ki, alul pedig beleolvad az oldal hátterébe, így az első kártya félig "rácsúszik".
// A cím szavanként élesedik ki, a díszítések az egér mozgására finoman elmozdulnak.
export default function PageHeader({ title, highlight, description, eyebrow, actions, className }: PageHeaderProps) {
  const pointer = usePointerParallax()
  const nearX = useTransform(pointer.x, (value) => value * -36)
  const nearY = useTransform(pointer.y, (value) => value * -36)
  const farX = useTransform(pointer.x, (value) => value * 20)
  const farY = useTransform(pointer.y, (value) => value * 20)

  return (
    <div className={cn('relative mb-8 lg:mb-10', className)}>
      <div
        aria-hidden
        className="pointer-events-none absolute -top-40 -bottom-32 left-[calc(50%-50vw)] isolate -z-10 w-screen mask-fade-bottom"
      >
        <AuroraBackground variant="band" />
      </div>
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10 hidden md:block">
        <motion.div style={{ x: farX, y: farY }} className="absolute -top-10 -right-28 size-72">
          <div className="size-full rounded-full border-2 border-dashed border-white/15 motion-safe:animate-spin-slow" />
        </motion.div>
        <motion.div style={{ x: nearX, y: nearY }} className="absolute -top-4 right-[30%] size-14 rounded-full bg-white/10" />
      </div>

      <motion.div
        variants={staggerContainer}
        initial="hidden"
        animate="show"
        className="flex flex-col gap-4 text-white sm:flex-row sm:items-end sm:justify-between"
      >
        <div className="min-w-0">
          {eyebrow && (
            <motion.p variants={headlineWord} className="mb-2 text-sm font-semibold tracking-wide text-white/75">
              {eyebrow}
            </motion.p>
          )}
          <h1 className="text-3xl leading-tight font-extrabold tracking-tight sm:text-4xl lg:text-5xl">
            {title.split(' ').map((word, index) => (
              <motion.span key={`${index}-${word}`} variants={headlineWord} className="mr-[0.25em] inline-block">
                {word}
              </motion.span>
            ))}
            {highlight && (
              <motion.span
                variants={headlineWord}
                className="inline-block bg-[linear-gradient(90deg,#ffffff,#f5d0fe,#ddd6fe,#ffffff)] bg-[length:200%_auto] bg-clip-text pb-1 text-transparent motion-safe:animate-gradient-x"
              >
                {highlight}
              </motion.span>
            )}
          </h1>
          {description && (
            <motion.p variants={headlineWord} className="mt-3 max-w-2xl text-white/80">
              {description}
            </motion.p>
          )}
        </div>
        {actions && (
          <motion.div variants={headlineWord} className="flex shrink-0 flex-wrap gap-2">
            {actions}
          </motion.div>
        )}
      </motion.div>
    </div>
  )
}
