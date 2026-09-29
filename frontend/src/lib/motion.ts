import type { Variants } from 'motion/react'

// Az alkalmazás közös animációi: a tartalom elemei egymás után, kis késleltetéssel úsznak be

export const staggerContainer: Variants = {
  hidden: {},
  show: { transition: { staggerChildren: 0.06, delayChildren: 0.05 } },
}

export const staggerItem: Variants = {
  hidden: { opacity: 0, y: 14 },
  show: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 260, damping: 24 } },
}

// Kártya megjelenése: előbb maga a kártya úszik be, utána a benne lévő elemek (staggerItem) egymás után
export const cardReveal: Variants = {
  hidden: { opacity: 0, y: 24 },
  show: {
    opacity: 1,
    y: 0,
    transition: { type: 'spring', stiffness: 200, damping: 24, delay: 0.1, delayChildren: 0.2, staggerChildren: 0.05 },
  },
}

// A cím szavai elmosódásból élesednek ki, egymás után
export const headlineWord: Variants = {
  hidden: { opacity: 0, y: 24, filter: 'blur(8px)' },
  show: { opacity: 1, y: 0, filter: 'blur(0px)', transition: { type: 'spring', stiffness: 140, damping: 18 } },
  exit: { opacity: 0, y: -12, filter: 'blur(6px)', transition: { duration: 0.2 } },
}

// Hibás beküldésnél a kártya megrázkódik (mint egy rossz PIN-kódnál)
export const shakeKeyframes = { x: [0, -10, 10, -7, 7, -3, 0] }
