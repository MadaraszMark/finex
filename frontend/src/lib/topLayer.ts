// A <dialog> a top layerbe kerül, ezért ablaknyitáskor a buborékok rétege is újra a tetejére lép

const RAISE_EVENT = 'finex:raise-toasts'

export function raiseToasts() {
  window.dispatchEvent(new Event(RAISE_EVENT))
}

export function onRaiseToasts(listener: () => void): () => void {
  window.addEventListener(RAISE_EVENT, listener)
  return () => window.removeEventListener(RAISE_EVENT, listener)
}

// Nyitott ablaknál a buborékra kattintás a háttérre esik át; ilyenkor nem zárjuk be az ablakot
export function isOverToast(x: number, y: number): boolean {
  return [...document.querySelectorAll('[data-sonner-toast]')].some((toast) => {
    const rect = toast.getBoundingClientRect()
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
  })
}
