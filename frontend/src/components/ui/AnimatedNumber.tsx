import { animate, useMotionValue, useMotionValueEvent, useReducedMotion } from 'motion/react'
import { useEffect, useRef } from 'react'

interface AnimatedNumberProps {
  value: number
  format: (value: number) => string
  className?: string
}

// Pörgő összeg React-renderelés nélkül; a felolvasó csak a végleges értéket kapja meg
export default function AnimatedNumber({ value, format, className }: AnimatedNumberProps) {
  const reduceMotion = useReducedMotion()
  const motionValue = useMotionValue(reduceMotion ? value : 0)
  const textRef = useRef<HTMLSpanElement>(null)

  useMotionValueEvent(motionValue, 'change', (latest) => {
    if (textRef.current) {
      textRef.current.textContent = format(latest)
    }
  })

  useEffect(() => {
    if (reduceMotion) {
      motionValue.jump(value)
      return
    }
    const controls = animate(motionValue, value, { duration: 1.1, ease: [0.22, 1, 0.36, 1] })
    return () => controls.stop()
  }, [value, reduceMotion, motionValue])

  return (
    <span className={className}>
      <span className="sr-only">{format(value)}</span>
      <span ref={textRef} aria-hidden>
        {format(motionValue.get())}
      </span>
    </span>
  )
}
