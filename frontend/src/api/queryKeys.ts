// A React Query gyorsítótár kulcsai egy helyen: így egy módosítás után pontosan tudjuk, mit kell újratölteni
// (pl. utalás után a számlákat és a tranzakciókat). Az előtagok hierarchikusak: a ['accounts'] érvénytelenítése
// a számlalistát, az egyes számlákat, a kivonatokat és az egyenlegtörténetet is frissíti.
export const queryKeys = {
  settings: ['settings'] as const,
  me: ['users', 'me'] as const,
  myLoginLogs: ['users', 'me', 'login-logs'] as const,
  unreadNotifications: ['notifications', 'unread-count'] as const,
  notifications: ['notifications'] as const,
  notificationList: ['notifications', 'list'] as const,

  tickets: ['support-tickets'] as const,
  ticket: (id: number) => ['support-tickets', id] as const,

  admin: ['admin'] as const,
  adminDashboard: ['admin', 'dashboard'] as const,
  adminUsers: (search: string, page: number) => ['admin', 'users', search, page] as const,
  adminUser: (id: number) => ['admin', 'users', 'detail', id] as const,
  adminAccounts: (status: string, page: number) => ['admin', 'accounts', status, page] as const,
  adminTickets: (status: string) => ['admin', 'tickets', status] as const,
  adminTicket: (id: number) => ['admin', 'tickets', 'detail', id] as const,
  adminLoginLogs: (filter: object, page: number) => ['admin', 'login-logs', filter, page] as const,

  accounts: ['accounts'] as const,
  account: (id: number) => ['accounts', id] as const,
  statement: (id: number, from: string, to: string) => ['accounts', id, 'statement', from, to] as const,
  balanceHistory: (id: number, from: string, to: string) => ['accounts', id, 'balance-history', from, to] as const,

  cards: ['cards'] as const,
  categories: ['categories'] as const,

  transactions: ['transactions'] as const,
  recentTransactions: (size: number) => ['transactions', 'recent', size] as const,
  transactionSearch: (filter: object) => ['transactions', 'search', filter] as const,
  transaction: (id: number) => ['transactions', 'detail', id] as const,
  transactionCategories: (id: number) => ['transactions', 'detail', id, 'categories'] as const,

  beneficiaries: ['beneficiaries'] as const,
  standingOrders: ['standing-orders'] as const,

  statistics: ['statistics'] as const,
  summary: (currency: string) => ['statistics', 'summary', currency] as const,
  monthlySummary: (currency: string, months: number) => ['statistics', 'monthly', currency, months] as const,
  categorySpending: (currency: string, from: string, to: string) => ['statistics', 'categories', currency, from, to] as const,
  // A tételek listájának része (egy pénzmozgás után ez is frissül)
  topExpenses: (currency: string, from: string, to: string) => ['transactions', 'top-expenses', currency, from, to] as const,

  savings: ['savings'] as const,
  savingsTransactions: (id: number) => ['savings', id, 'transactions'] as const,
}
