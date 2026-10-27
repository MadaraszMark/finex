import type { ComponentType } from 'react'
import { createBrowserRouter } from 'react-router'
import { PageSkeleton, SkeletonCard } from '../components/skeletons/SkeletonKit'
import { AccountDetailSkeleton, AccountsSkeleton } from '../features/accounts/components/AccountSkeletons'
import { AdminRouteSkeleton } from '../features/admin/components/AdminSkeletons'
import { AuthSkeleton, WelcomeSkeleton } from '../features/auth/components/AuthSkeletons'
import BeneficiariesSkeleton from '../features/beneficiaries/components/BeneficiariesSkeleton'
import CardsSkeleton from '../features/cards/components/CardsSkeleton'
import HomeSkeleton from '../features/home/components/HomeSkeleton'
import NotificationsSkeleton from '../features/notifications/components/NotificationsSkeleton'
import ProfileSkeleton from '../features/profile/components/ProfileSkeleton'
import SavingsSkeleton from '../features/savings/components/SavingsSkeleton'
import StandingOrdersSkeleton from '../features/standing-orders/components/StandingOrdersSkeleton'
import StatisticsSkeleton from '../features/statistics/components/StatisticsSkeleton'
import SupportSkeleton from '../features/support/components/SupportSkeleton'
import TransactionsSkeleton from '../features/transactions/components/TransactionsSkeleton'
import TransferSkeleton from '../features/transfer/components/TransferSkeleton'
import { GuestOnly, RequireAdmin, RequireAuth } from '../session/guards'
import AppLayout from './layout/AppLayout'
import RootLayout from './RootLayout'
import RouteErrorPage from './RouteErrorPage'

// Lazy route: az oldal kódja csak az első megnyitáskor töltődik le
function page(load: () => Promise<{ default: ComponentType }>) {
  return async () => ({ Component: (await load()).default })
}

// Betöltés közben pörgő karika helyett az oldal váza látszik (hydrateFallbackElement)

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    errorElement: <RouteErrorPage />,
    // Tartalék, ha egy útvonalnak nincs saját váza
    hydrateFallbackElement: <div className="min-h-dvh bg-background" />,
    children: [
      // Csak kijelentkezve (bejelentkezett felhasználót továbbirányít)
      {
        element: <GuestOnly />,
        children: [
          {
            path: 'welcome',
            lazy: page(() => import('../features/auth/pages/WelcomePage')),
            handle: { title: 'Üdvözlünk' },
            hydrateFallbackElement: <WelcomeSkeleton />,
          },
          // Közös keret: belépés és regisztráció között csak az űrlap cserélődik, a háttér marad
          {
            lazy: page(() => import('../features/auth/AuthLayout')),
            hydrateFallbackElement: <AuthSkeleton />,
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
              {
                index: true,
                lazy: page(() => import('../features/home/HomePage')),
                handle: { title: 'Főoldal' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ eyebrow: true }}>
                    <HomeSkeleton />
                  </PageSkeleton>
                ),
              },

              {
                path: 'accounts',
                children: [
                  {
                    index: true,
                    lazy: page(() => import('../features/accounts/pages/AccountsPage')),
                    handle: { title: 'Számlák' },
                    hydrateFallbackElement: (
                      <PageSkeleton header={{ action: true }}>
                        <AccountsSkeleton />
                      </PageSkeleton>
                    ),
                  },
                  {
                    path: ':accountId',
                    lazy: page(() => import('../features/accounts/pages/AccountDetailPage')),
                    handle: { title: 'Számlaadatlap' },
                    hydrateFallbackElement: (
                      <PageSkeleton header={{ eyebrow: true, description: false, action: true }}>
                        <AccountDetailSkeleton />
                      </PageSkeleton>
                    ),
                  },
                ],
              },
              {
                path: 'cards',
                lazy: page(() => import('../features/cards/pages/CardsPage')),
                handle: { title: 'Kártyák' },
                hydrateFallbackElement: (
                  <PageSkeleton>
                    <CardsSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'transfer',
                lazy: page(() => import('../features/transfer/pages/TransferPage')),
                handle: { title: 'Utalás' },
                hydrateFallbackElement: (
                  <PageSkeleton>
                    <TransferSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'transactions',
                lazy: page(() => import('../features/transactions/pages/TransactionsPage')),
                handle: { title: 'Tranzakciók' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ action: true }}>
                    <TransactionsSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'beneficiaries',
                lazy: page(() => import('../features/beneficiaries/pages/BeneficiariesPage')),
                handle: { title: 'Kedvezményezettek' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ action: true }}>
                    <BeneficiariesSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'standing-orders',
                lazy: page(() => import('../features/standing-orders/pages/StandingOrdersPage')),
                handle: { title: 'Rendszeres átutalások' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ action: true }}>
                    <StandingOrdersSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'savings',
                lazy: page(() => import('../features/savings/pages/SavingsPage')),
                handle: { title: 'Megtakarítások' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ action: true }}>
                    <SavingsSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'statistics',
                lazy: page(() => import('../features/statistics/pages/StatisticsPage')),
                handle: { title: 'Statisztika' },
                hydrateFallbackElement: (
                  <PageSkeleton>
                    <StatisticsSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'notifications',
                lazy: page(() => import('../features/notifications/pages/NotificationsPage')),
                handle: { title: 'Értesítések' },
                hydrateFallbackElement: (
                  <PageSkeleton>
                    <NotificationsSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'support',
                lazy: page(() => import('../features/support/pages/SupportPage')),
                handle: { title: 'Ügyfélszolgálat' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ action: true }}>
                    <SupportSkeleton />
                  </PageSkeleton>
                ),
              },
              {
                path: 'profile',
                lazy: page(() => import('../features/profile/pages/ProfilePage')),
                handle: { title: 'Profil és biztonság' },
                hydrateFallbackElement: (
                  <PageSkeleton header={{ eyebrow: true }}>
                    <ProfileSkeleton />
                  </PageSkeleton>
                ),
              },

              // Adminisztráció: csak ADMIN szerepkörrel (a backend is ezt követeli meg)
              {
                path: 'admin',
                element: <RequireAdmin />,
                children: [
                  {
                    index: true,
                    lazy: page(() => import('../features/admin/pages/AdminDashboardPage')),
                    handle: { title: 'Admin áttekintés' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin" />,
                  },
                  {
                    path: 'users',
                    lazy: page(() => import('../features/admin/pages/AdminUsersPage')),
                    handle: { title: 'Felhasználók' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/users" />,
                  },
                  {
                    path: 'accounts',
                    lazy: page(() => import('../features/admin/pages/AdminAccountsPage')),
                    handle: { title: 'Számlák kezelése' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/accounts" />,
                  },
                  {
                    path: 'support-tickets',
                    lazy: page(() => import('../features/admin/pages/AdminTicketsPage')),
                    handle: { title: 'Ticketek' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/support-tickets" />,
                  },
                  {
                    path: 'login-logs',
                    lazy: page(() => import('../features/admin/pages/AdminLoginLogsPage')),
                    handle: { title: 'Belépési napló' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/login-logs" />,
                  },
                  {
                    path: 'audit-log',
                    lazy: page(() => import('../features/admin/pages/AdminAuditLogPage')),
                    handle: { title: 'Audit napló' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/audit-log" />,
                  },
                  {
                    path: 'categories',
                    lazy: page(() => import('../features/admin/pages/AdminCategoriesPage')),
                    handle: { title: 'Kategóriák' },
                    hydrateFallbackElement: <AdminRouteSkeleton pathname="/admin/categories" />,
                  },
                ],
              },

              {
                path: '*',
                lazy: page(() => import('../features/errors/NotFoundPage')),
                handle: { title: 'Nem található' },
                hydrateFallbackElement: (
                  <PageSkeleton>
                    <SkeletonCard className="h-72" />
                  </PageSkeleton>
                ),
              },
            ],
          },
        ],
      },
    ],
  },
])
