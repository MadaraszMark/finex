import type { ComponentType } from 'react'
import { createBrowserRouter, type RouteObject } from 'react-router'
import ComingSoonPage from '../features/placeholder/ComingSoonPage'
import { plannedPages } from '../features/placeholder/plannedPages'
import type { RouteHandle } from '../hooks/useRouteTitle'
import { GuestOnly, RequireAdmin, RequireAuth } from '../session/guards'
import AppLayout from './layout/AppLayout'
import { findNavItem } from './navigation'
import RootLayout from './RootLayout'
import RouteErrorPage from './RouteErrorPage'
import SplashScreen from './SplashScreen'

// Az oldalak kódja csak akkor töltődik le, amikor a felhasználó először megnyitja őket (külön fájl minden oldalnak)
function page(load: () => Promise<{ default: ComponentType }>) {
  return async () => ({ Component: (await load()).default })
}

// Még el nem készült oldal: a menü címével, ikonjával és a tervezett tartalommal (features/placeholder).
// Útvonal nélkül a szülő alapoldala (index) lesz.
function planned(fullPath: string, routePath?: string): RouteObject {
  const item = findNavItem(fullPath)
  const plan = plannedPages[fullPath]
  if (!item || !plan) {
    throw new Error(`Hiányzó menüpont vagy terv: ${fullPath}`)
  }

  const route = {
    element: <ComingSoonPage title={item.label} icon={item.icon} {...plan} />,
    handle: { title: item.label } satisfies RouteHandle,
  }
  return routePath === undefined ? { index: true, ...route } : { path: routePath, ...route }
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    errorElement: <RouteErrorPage />,
    // Az első oldal kódjának letöltése alatt látszik
    hydrateFallbackElement: <SplashScreen />,
    children: [
      // Csak kijelentkezve (bejelentkezett felhasználót továbbirányít)
      {
        element: <GuestOnly />,
        children: [
          { path: 'welcome', lazy: page(() => import('../features/auth/pages/WelcomePage')), handle: { title: 'Üdvözlünk' } },
          // A belépés és a regisztráció közös kerete: váltáskor a háttér helyben marad, csak az űrlap cserélődik
          {
            lazy: page(() => import('../features/auth/AuthLayout')),
            children: [
              { path: 'login', lazy: page(() => import('../features/auth/pages/LoginPage')), handle: { title: 'Bejelentkezés' } },
              { path: 'register', lazy: page(() => import('../features/auth/pages/RegisterPage')), handle: { title: 'Regisztráció' } },
            ],
          },
        ],
      },

      // Csak bejelentkezve, az alkalmazás keretén (oldalsáv, fejléc, alsó menüsor) belül
      {
        element: <RequireAuth />,
        children: [
          {
            element: <AppLayout />,
            children: [
              { index: true, lazy: page(() => import('../features/home/HomePage')), handle: { title: 'Főoldal' } },

              planned('/accounts', 'accounts'),
              planned('/cards', 'cards'),
              planned('/transfer', 'transfer'),
              planned('/transactions', 'transactions'),
              planned('/beneficiaries', 'beneficiaries'),
              planned('/standing-orders', 'standing-orders'),
              planned('/savings', 'savings'),
              planned('/statistics', 'statistics'),
              planned('/notifications', 'notifications'),
              planned('/support', 'support'),
              planned('/profile', 'profile'),

              // Adminisztráció: csak ADMIN szerepkörrel (a backend /admin/** végpontjai is ezt követelik meg)
              {
                path: 'admin',
                element: <RequireAdmin />,
                children: [
                  planned('/admin'),
                  planned('/admin/users', 'users'),
                  planned('/admin/accounts', 'accounts'),
                  planned('/admin/support-tickets', 'support-tickets'),
                  planned('/admin/login-logs', 'login-logs'),
                  planned('/admin/categories', 'categories'),
                ],
              },

              { path: '*', lazy: page(() => import('../features/errors/NotFoundPage')), handle: { title: 'Nem található' } },
            ],
          },
        ],
      },
    ],
  },
])
