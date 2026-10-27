import { useQuery } from '@tanstack/react-query'
import { useEffect } from 'react'
import { Outlet, ScrollRestoration, useNavigation } from 'react-router'
import { getUnreadCount } from '../api/notifications'
import { queryKeys } from '../api/queryKeys'
import { useRouteTitle } from '../hooks/useRouteTitle'
import { formatTabTitle } from '../lib/format'
import NavigationProgress from './NavigationProgress'

// Közös gyökér: fülcím, betöltésjelző csík és görgetési pozíció visszaállítása
export default function RootLayout() {
  const title = useRouteTitle()
  const navigation = useNavigation()
  // Csak a csengő által már betöltött számot olvassa, kijelentkezve nem kérdez le semmit
  const { data: unread = 0 } = useQuery({ queryKey: queryKeys.unreadNotifications, queryFn: getUnreadCount, enabled: false })

  useEffect(() => {
    document.title = formatTabTitle(title, unread)
  }, [title, unread])

  return (
    <>
      <NavigationProgress active={navigation.state === 'loading'} />
      <Outlet />
      <ScrollRestoration />
    </>
  )
}
