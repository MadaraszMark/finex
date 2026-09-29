import { useEffect } from 'react'
import { Outlet, ScrollRestoration, useNavigation } from 'react-router'
import { useRouteTitle } from '../hooks/useRouteTitle'
import { SHY } from '../lib/format'
import NavigationProgress from './NavigationProgress'

// Minden oldal közös gyökere: böngészőfül címe, betöltésjelző csík és görgetési pozíció visszaállítása
export default function RootLayout() {
  const title = useRouteTitle()
  const navigation = useNavigation()

  useEffect(() => {
    // A feltételes elválasztójelek (SHY) a fül címében nem kellenek
    document.title = title ? `${title.replaceAll(SHY, '')} · FineX` : 'FineX'
  }, [title])

  return (
    <>
      <NavigationProgress active={navigation.state === 'loading'} />
      <Outlet />
      <ScrollRestoration />
    </>
  )
}
