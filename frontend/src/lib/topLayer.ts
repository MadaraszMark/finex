// A felugró ablakok (natív <dialog>) a böngésző legfelső rétegébe (top layer) kerülnek, minden más elem fölé.
// Az értesítő buborékok rétege ezért minden ablaknyitáskor újra a legfelső rétegbe lép, így a visszajelzés
// (pl. "Mentettük a módosítást") akkor is látszik, ha az ablak nyitva marad.

const RAISE_EVENT = 'finex:raise-toasts'

export function raiseToasts() {
  window.dispatchEvent(new Event(RAISE_EVENT))
}

export function onRaiseToasts(listener: () => void): () => void {
  window.addEventListener(RAISE_EVENT, listener)
  return () => window.removeEventListener(RAISE_EVENT, listener)
}

// Nyitott ablak mellett a buborékok nem kattinthatók (a modális ablak minden mást letilt), a kattintás "átesik" rajtuk
// az ablak hátterére. A háttér kattintáskezelője ezzel ismeri fel, hogy ilyenkor ne zárja be az ablakot.
export function isOverToast(x: number, y: number): boolean {
  return [...document.querySelectorAll('[data-sonner-toast]')].some((toast) => {
    const rect = toast.getBoundingClientRect()
    return x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom
  })
}
