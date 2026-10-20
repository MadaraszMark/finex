// A backend DTO-inak (hu.finex.main.dto) megfelelő típusok.
// A BigDecimal összegek számként, az Instant időpontok ISO-szövegként, a LocalDate dátumok "ÉÉÉÉ-HH-NN" alakban érkeznek.

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

// Regisztráció (CreateUserRequest): a szerepkör mindig USER, azt a backend állítja be
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

// A backend által támogatott devizanemek (CreateAccountRequest mintája: HUF|EUR|USD)
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

// Számlakivonat egy tétele: az előjeles összeg és a tétel utáni egyenleg (futó egyenleg)
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

// Számlakivonat egy időszakra (a from/to "ÉÉÉÉ-HH-NN", mindkét nap beleszámít)
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

// Demó kártyás fizetés (pl. bolti vagy online vásárlás)
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
}

// A tranzakciók keresőjének szűrői (TransactionSearchRequest); a dátumok "ÉÉÉÉ-HH-NN" alakúak
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

// Utalás (TransferRequest): FineX-es címzettnél azonnal jóváíródik, külső számlára csak a terhelés történik meg
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

// Megtakarítási cél; a pénzneme a megnyitáskor választott folyószámláé. A progressPercent csak célösszegnél van.
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

// A főoldal összesítője egy devizanemben (a hónap a folyó naptári hónap, budapesti idő szerint)
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

// Egy hónap bevétele és kiadása; a month "ÉÉÉÉ-HH" alakú
export interface MonthlySummaryResponse {
  month: string
  income: number
  outcome: number
  transactionCount: number
}

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED'
export type LoginStatus = 'SUCCESS' | 'FAILED'

// Saját adatok módosítása (UpdateUserRequest)
export interface UpdateUserRequest {
  firstName: string
  lastName: string
  email: string
  phone?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

// Egy belépési kísérlet a saját belépési naplóban (az eszközt a böngésző azonosítója mutatja)
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

// A ticket teljes beszélgetéssel (a nyitó üzenet a message mezőben, a válaszok a messages listában)
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

// Admin felület
// A bank üzleti szabályainak paraméterei (GET /settings): a felület ezekből dolgozik, nem saját másolatból
export interface BankSettingsResponse {
  transferDailyLimit: number
  maxAccountsPerUser: number
  savingsInterestRate: number
  cardDefaultDailyLimit: number
  cardDefaultDailyLimitForeign: number
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

// Egy ügyfél teljes áttekintése (admin)
export interface UserDetailsResponse {
  user: UserResponse
  accounts: AccountListItem[]
  cards: CardResponse[]
  savingsAccounts: SavingsAccountResponse[]
}

// Kategória létrehozása és módosítása (az ikon a lucide ikonkészlet neve, legfeljebb 20 karakter)
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

// Spring Data lapozott válasz (a backend VIA_DTO módban küldi: a lapozási adatok a "page" objektumban vannak)
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}
