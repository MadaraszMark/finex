import { motion } from 'motion/react'
import { cn } from '../../../lib/cn'
import { headlineWord } from '../../../lib/motion'

// A cím egy sora: a szavak egymás után élesednek ki (a szülő staggerContainer indítja)
export default function HeadlineWords({ text, highlight = false }: { text: string; highlight?: boolean }) {
  return (
    <span className="block">
      {text.split(' ').map((word, index) => (
        <motion.span
          key={`${index}-${word}`}
          variants={headlineWord}
          className={cn(
            'mr-[0.25em] inline-block last:mr-0',
            highlight &&
              'bg-[linear-gradient(90deg,#ffffff,#f5d0fe,#ddd6fe,#ffffff)] bg-[length:200%_auto] bg-clip-text pb-1 text-transparent motion-safe:animate-gradient-x',
          )}
        >
          {word}
        </motion.span>
      ))}
    </span>
  )
}
