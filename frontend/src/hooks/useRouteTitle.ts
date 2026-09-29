import { useMatches } from 'react-router'

// Az útvonalakhoz csatolt adatok (router.tsx "handle" mezője)
export interface RouteHandle {
  title?: string
}

// A legmélyebb illeszkedő útvonal címe (böngészőfül címéhez és a fejléchez)
export function useRouteTitle(): string | undefined {
  const matches = useMatches()

  for (let index = matches.length - 1; index >= 0; index--) {
    const handle = matches[index].handle as RouteHandle | undefined
    if (handle?.title) {
      return handle.title
    }
  }
  return undefined
}
