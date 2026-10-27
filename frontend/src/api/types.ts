// A backend DTO-i: összegek számként, időpontok ISO-szövegként, dátumok "ÉÉÉÉ-HH-NN" alakban

export type TransactionType = 'INCOME' | 'OUTCOME' | 'TRANSFER_IN' | 'TRANSFER_OUT'
export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED' | 'FROZEN'
export type AccountType = 'CURRENT' | 'SAVINGS' | 'CREDIT'
export type CardStatus = 'ACTIVE' | 'BLOCKED' | 'CANCELLED'
export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'BLOCKED'
export type NotificationType = 'TRANSACTION' | 'SECURITY' | 'SAVINGS' | 'SUPPORT' | 'SYSTEM'

// A GlobalExceptionHandler hibaválasza
export interface ApiError {
  timestamp: string
  status: number
  message: string
  violations?: { field: string; message: string }[] | null
}

export interface LoginRequest {
  email: string
  password: string
}

// A szerepkört (mindig USER) a backend állítja be
export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  phone?: string
  password: string
}

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string | null
  role: UserRole
  status: UserStatus
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  token: string
  expiresAt: string
  user: UserResponse
}

export type Currency = 'HUF' | 'EUR' | 'USD'

export interface AccountResponse {
  id: number
  userId: number
  name: string
  accountNumber: string
  balance: number
  currency: string
  accountType: AccountType
  status: AccountStatus
  createdAt: string
}

export interface CreateAccountRequest {
  name: string
  currency: Currency
}

export interface DepositRequest {
  amount: number
  message?: string
}

export interface StatementItemResponse {
  transactionId: number
  createdAt: string
  type: TransactionType
  partnerName: string | null
  message: string | null
  amount: number
  signedAmount: number
  runningBalance: number
}

// A from és a to napja is beleszámít
export interface StatementResponse {
  accountId: number
  accountNumber: string
  currency: string
  from: string
  to: string
  openingBalance: number
  closingBalance: number
  totalIncome: number
  totalOutcome: number
  items: StatementItemResponse[]
}

// Az egyenleg egy korábbi állapota (minden könyvelt tétel után keletkezik egy)
export interface BalanceHistoryItem {
  balance: number
  createdAt: string
}

// A teljes kártyaszám soha nem jön le, csak a maszkolt alak ("**** **** **** 1184")
export interface CardResponse {
  id: number
  accountId: number
  accountNumber: string
  maskedNumber: string
  holderName: string
  expiryDate: string
  status: CardStatus
  dailyLimit: number
  spentToday: number
  onlinePaymentEnabled: boolean
  createdAt: string
}

export interface UpdateCardLimitsRequest {
  dailyLimit: number
  onlinePaymentEnabled: boolean
}

export interface CardPaymentRequest {
  merchantName: string
  amount: number
  categoryId?: number
  online: boolean
}

// Az ikon a frontend ikonkészletének (lucide) neve, pl. "shopping-cart"
export interface CategoryResponse {
  id: number
  name: string
  icon: string | null
}

export interface TransactionListItem {
  id: number
  accountId: number
  type: TransactionType
  amount: number
  message: string | null
  partnerName: string | null
  currency: string
  categories: CategoryResponse[]
  createdAt: string
  // Sztornó tételnél az eredeti (semlegesített) tétel azonosítója
  reversalOfId: number | null
  reversed: boolean
}

export interface TransactionResponse {
  id: number
  accountId: number
  cardId: number | null
  type: TransactionType
  amount: number
  message: string | null
  partnerName: string | null
  fromAccount: string | null
  toAccount: string | null
  categories: CategoryResponse[]
  currency: string
  createdAt: string
  // Sztornó tételnél az eredeti (semlegesített) tétel azonosítója
  reversalOfId: number | null
  reversed: boolean
}

export interface TransactionSearchParams {
  accountId?: number
  type?: TransactionType
  from?: string
  to?: string
  search?: string
  categoryId?: number
  minAmount?: number
  maxAmount?: number
}

// Belső címzettnél azonnal jóváíródik, külső számlánál csak a terhelés történik meg
export interface TransferRequest {
  fromAccountId: number
  toAccountNumber: string
  partnerName: string
  amount: number
  message?: string
  categoryIds?: number[]
}

export interface TransferResponse {
  transactionId: number
  fromAccountId: number
  toAccountNumber: string
  partnerName: string
  amount: number
  currency: string
  message: string | null
  categories: CategoryResponse[]
  fromAccountNewBalance: number
  // FineX-es (belső) címzett: az összeg azonnal megérkezett
  internal: boolean
  createdAt: string
}

// Egy tranzakció és egy kategória kapcsolata; az id-vel törölhető a hozzárendelés
export interface TransactionCategoryLink {
  id: number
  categoryId: number
  categoryName: string
  categoryIcon: string | null
}

export interface BeneficiaryResponse {
  id: number
  name: string
  accountNumber: string
  note: string | null
  // A számla a FineX-ben vezetett: az utalás azonnal megérkezik
  internal: boolean
  createdAt: string
}

// Létrehozás és módosítás (CreateBeneficiaryRequest / UpdateBeneficiaryRequest)
export interface BeneficiaryRequest {
  name: string
  accountNumber: string
  note?: string
}

export type StandingOrderFrequency = 'WEEKLY' | 'MONTHLY'

export interface StandingOrderResponse {
  id: number
  accountId: number
  accountNumber: string
  toAccountNumber: string
  partnerName: string
  amount: number
  currency: string
  message: string | null
  frequency: StandingOrderFrequency
  nextExecutionDate: string
  active: boolean
  lastExecutionAt: string | null
  createdAt: string
}

export interface CreateStandingOrderRequest {
  accountId: number
  toAccountNumber: string
  partnerName: string
  amount: number
  message?: string
  frequency: StandingOrderFrequency
  firstExecutionDate: string
}

export interface UpdateStandingOrderRequest {
  amount: number
  message?: string
  frequency: StandingOrderFrequency
  nextExecutionDate: string
}

export type SavingsStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED'
export type SavingsTransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'INTEREST'

// A pénznem a forrás folyószámláé; a progressPercent csak célösszegnél van
export interface SavingsAccountResponse {
  id: number
  userId: number
  name: string
  balance: number
  currency: string
  // Éves kamatláb százalékban (pl. 3.5); havonta egyszer írjuk jóvá
  interestRate: number
  targetAmount: number | null
  progressPercent: number | null
  status: SavingsStatus
  createdAt: string
  updatedAt: string
}

export interface CreateSavingsAccountRequest {
  name: string
  accountId: number
  initialDeposit: number
  targetAmount?: number
}

export interface UpdateSavingsAccountRequest {
  name: string
  targetAmount?: number
}

// Befizetés a folyószámláról, illetve kivét a folyószámlára (a két számla pénzneme egyezik)
export interface SavingsTransferRequest {
  accountId: number
  amount: number
  message?: string
}

export interface SavingsTransferResponse {
  savingsAccountId: number
  accountId: number
  savingsNewBalance: number
  accountNewBalance: number
  message: string | null
  createdAt: string
}

export interface SavingsTransactionResponse {
  id: number
  type: SavingsTransactionType
  amount: number
  balanceAfter: number
  createdAt: string
}

// Kiadás kategóriánként egy időszakban (csak a kategóriába sorolt kimenő tételek)
export interface CategorySpendingResponse {
  categoryId: number
  categoryName: string
  categoryIcon: string | null
  totalAmount: number
  transactionCount: number
}

// A hónap a folyó naptári hónap, budapesti idő szerint
export interface DashboardSummaryResponse {
  currency: string
  totalBalance: number
  savingsBalance: number
  monthIncome: number
  monthOutcome: number
  accountCount: number
  activeCardCount: number
  unreadNotificationCount: number
}

// A month "ÉÉÉÉ-HH" alakú
export interface MonthlySummaryResponse {
  month: string
  income: number
  outcome: number
  transactionCount: number
}

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
export type LoginStatus = 'SUCCESS' | 'FAILED'

export interface UpdateUserRequest {
  firstName: string
  lastName: string
  email: string
  phone?: string
  // Csak e-mail-cseréhez kötelező (ellopott tokennel se lehessen átvenni a fiókot)
  currentPassword?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface LoginLogListItem {
  status: LoginStatus
  ipAddress: string | null
  userAgent: string | null
  failureReason: string | null
  createdAt: string
}

// Admin: belépési napló bejegyzés a felhasználóval
export interface LoginLogResponse extends LoginLogListItem {
  id: number
  userId: number | null
  email: string
}

export interface CreateSupportTicketRequest {
  title: string
  message: string
}

export interface SupportTicketListItem {
  id: number
  title: string
  userFullName: string
  status: TicketStatus
  createdAt: string
  updatedAt: string
}

// Egy üzenet a ticketben; staff = az ügyfélszolgálat válasza
export interface TicketMessageResponse {
  id: number
  authorName: string
  staff: boolean
  message: string
  createdAt: string
}

// A nyitó üzenet a message mezőben, a válaszok a messages listában
export interface SupportTicketResponse {
  id: number
  userId: number
  userFullName: string
  userEmail: string
  title: string
  message: string
  status: TicketStatus
  messages: TicketMessageResponse[]
  createdAt: string
  updatedAt: string
}

// GET /settings: a felület ezekből dolgozik, nem saját konstansokból
export interface BankSettingsResponse {
  transferDailyLimit: number
  maxAccountsPerUser: number
  savingsInterestRate: number
  cardDefaultDailyLimit: number
  cardDefaultDailyLimitForeign: number
  cardMaxDailyLimit: number
  maxSavingsPerUser: number
  maxStandingOrdersPerUser: number
  maxBeneficiariesPerUser: number
  // Devizaárfolyamok: 1 egység = ennyi forint (pl. EUR: 395)
  exchangeRates: Record<string, number>
}

export interface AdminDashboardResponse {
  userCount: number
  blockedUserCount: number
  accountCount: number
  totalDepositsHuf: number
  transactionsToday: number
  transactionVolumeTodayHuf: number
  openTicketCount: number
  failedLoginsLast24h: number
}

// Egy ütemezett feladat kézi futtatásának eredménye
export interface JobRunResponse {
  job: string
  processed: number
  skipped: number
}

export interface UserListItem {
  id: number
  fullName: string
  email: string
  role: UserRole
  status: UserStatus
  createdAt: string
}

export interface AccountListItem {
  id: number
  name: string
  accountNumber: string
  balance: number
  currency: string
  accountType: AccountType
  status: AccountStatus
}

export interface UserDetailsResponse {
  user: UserResponse
  accounts: AccountListItem[]
  cards: CardResponse[]
  savingsAccounts: SavingsAccountResponse[]
}

// Az ikon a lucide ikonkészlet neve, legfeljebb 20 karakter
export interface CategoryRequest {
  name: string
  icon?: string
}

export interface NotificationResponse {
  id: number
  type: NotificationType
  title: string
  message: string
  read: boolean
  createdAt: string
}

export interface UnreadCountResponse {
  count: number
}

// Spring Data lapozás VIA_DTO módban: a lapozási adatok a page objektumban vannak
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export interface ExchangeRequest {
  fromAccountId: number
  toAccountId: number
  amount: number
}

export interface ExchangeResponse {
  transactionId: number
  fromAccountId: number
  toAccountId: number
  fromAmount: number
  fromCurrency: string
  toAmount: number
  toCurrency: string
  rate: number
  fromAccountNewBalance: number
  toAccountNewBalance: number
  createdAt: string
}

export type AuditAction =
  | 'USER_STATUS_CHANGED'
  | 'USER_ROLE_CHANGED'
  | 'ACCOUNT_STATUS_CHANGED'
  | 'ACCOUNT_DEPOSIT'
  | 'TRANSACTION_REVERSED'
  | 'CATEGORY_CREATED'
  | 'CATEGORY_UPDATED'
  | 'CATEGORY_DELETED'
  | 'TICKET_STATUS_CHANGED'
  | 'JOB_RUN'

export type AuditTargetType = 'USER' | 'ACCOUNT' | 'TRANSACTION' | 'CATEGORY' | 'SUPPORT_TICKET' | 'JOB'

export interface AuditLogResponse {
  id: number
  actorId: number | null
  actorEmail: string
  action: AuditAction
  targetType: AuditTargetType
  targetId: number | null
  details: string | null
  ipAddress: string | null
  createdAt: string
}

// Főkönyvi egyeztetés: a tárolt egyenlegek egyeznek-e a könyvelt tételekkel
export interface ReconciliationItem {
  kind: 'ACCOUNT' | 'SAVINGS'
  id: number
  label: string
  currency: string
  storedBalance: number
  ledgerBalance: number
  difference: number
}

export interface ReconciliationResponse {
  checkedAt: string
  accountCount: number
  savingsCount: number
  balanced: boolean
  mismatches: ReconciliationItem[]
}
