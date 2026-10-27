import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { MotionGlobalConfig } from 'motion/react'
import { afterEach } from 'vitest'

// jsdom: az animációk azonnal véget érnek, a hiányzó böngésző-API-k egyszerű helyettesítőt kapnak
MotionGlobalConfig.skipAnimations = true

if (!window.matchMedia) {
  window.matchMedia = (query: string) =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => undefined,
      removeListener: () => undefined,
      addEventListener: () => undefined,
      removeEventListener: () => undefined,
      dispatchEvent: () => false,
    }) as MediaQueryList
}

if (typeof window.ResizeObserver === 'undefined') {
  window.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
}

if (!Element.prototype.getAnimations) {
  Element.prototype.getAnimations = () => []
}

window.scrollTo = () => undefined
Element.prototype.scrollIntoView = () => undefined

afterEach(() => {
  cleanup()
  localStorage.clear()
})
