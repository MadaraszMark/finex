import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { AccountResponse, BankSettingsResponse, BeneficiaryResponse, TransferResponse } from '../../../api/types'
import TransferPage from './TransferPage'

// A backend-hívások helyett tesztadat (a modulok többi függvénye az eredeti marad)
const transferMock = vi.hoisted(() => vi.fn())

vi.mock('../../../api/accounts', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../../api/accounts')>()),
  getMyAccounts: vi.fn(async () => [account]),
}))
vi.mock('../../../api/beneficiaries', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../../api/beneficiaries')>()),
  getBeneficiaries: vi.fn(async () => [beneficiary]),
}))
vi.mock('../../../api/categories', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../../api/categories')>()),
  getCategories: vi.fn(async () => []),
}))
vi.mock('../../../api/settings', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../../api/settings')>()),
  getBankSettings: vi.fn(async () => settings),
}))
vi.mock('../../../api/transactions', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../../api/transactions')>()),
  transfer: transferMock,
}))

const account = {
  id: 1,
  name: 'Fő számla',
  accountNumber: 'HU15117730161111101800000001',
  balance: 100000,
  currency: 'HUF',
  status: 'ACTIVE',
  accountType: 'CURRENT',
} as AccountResponse

const beneficiary: BeneficiaryResponse = {
  id: 5,
  name: 'Nagy Bence',
  accountNumber: 'HU28104000950000521700000003',
  note: null,
  internal: true,
  createdAt: '2026-01-01T10:00:00Z',
} as BeneficiaryResponse

const settings = { transferDailyLimit: 5_000_000, exchangeRates: { HUF: 1 } } as unknown as BankSettingsResponse

const receipt: TransferResponse = {
  transactionId: 166,
  fromAccountId: 1,
  toAccountNumber: beneficiary.accountNumber,
  partnerName: 'Nagy Bence',
  amount: 2500,
  currency: 'HUF',
  message: null,
  categories: [],
  fromAccountNewBalance: 97500,
  internal: true,
  createdAt: '2026-10-26T20:47:00Z',
} as TransferResponse

function wait(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function renderTransferPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const router = createMemoryRouter([{ path: '/transfer', element: <TransferPage /> }], { initialEntries: ['/transfer'] })
  render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

// Az első három lépés: forrásszámla → kedvezményezett → összeg (a "Tovább" nélkül)
async function fillUntilAmount(user: ReturnType<typeof userEvent.setup>) {
  await screen.findByText('Melyik számládról utalsz?')
  await user.click(screen.getByRole('button', { name: 'Tovább' }))

  await user.click(await screen.findByRole('radio', { name: /Nagy Bence/ }))
  await user.click(screen.getByRole('button', { name: 'Tovább' }))

  await user.type(await screen.findByPlaceholderText('25 000'), '2500')
}

describe('Utalás varázsló', () => {
  beforeEach(() => {
    transferMock.mockReset()
    transferMock.mockResolvedValue(receipt)
  })

  it('végigvezet a lépéseken, és az áttekintés után egyszer, idempotencia-kulccsal utal', async () => {
    const user = userEvent.setup()
    renderTransferPage()

    await fillUntilAmount(user)
    await user.click(screen.getByRole('button', { name: 'Tovább' }))
    expect(await screen.findByText('Minden stimmel?')).toBeInTheDocument()

    // A felhasználó elolvassa az összegzést, majd elindítja az utalást
    await wait(700)
    await user.click(screen.getByRole('button', { name: /Utalás indítása/ }))

    expect(await screen.findByText('Sikeres utalás')).toBeInTheDocument()
    expect(transferMock).toHaveBeenCalledTimes(1)
    expect(transferMock.mock.calls[0][0]).toEqual({
      request: {
        fromAccountId: 1,
        toAccountNumber: 'HU28104000950000521700000003',
        partnerName: 'Nagy Bence',
        amount: 2500,
        message: undefined,
        categoryIds: undefined,
      },
      idempotencyKey: expect.stringMatching(/^[A-Za-z0-9_-]{8,100}$/),
    })
  })

  // Regresszió: a dupla kattintás második fele az áttekintésen elindította az utalást
  it('dupla kattintásra nem indítja el az utalást az áttekintés nélkül', async () => {
    const user = userEvent.setup()
    renderTransferPage()

    await fillUntilAmount(user)
    await user.click(screen.getByRole('button', { name: 'Tovább' }))
    await screen.findByText('Minden stimmel?')
    await wait(150)
    await user.click(screen.getByRole('button', { name: /Utalás indítása/ }))

    await wait(300)
    expect(transferMock).not.toHaveBeenCalled()
    expect(screen.getByText('Minden stimmel?')).toBeInTheDocument()
  })

  it('a fedezetnél nagyobb összeggel nem enged tovább', async () => {
    const user = userEvent.setup()
    renderTransferPage()

    await screen.findByText('Melyik számládról utalsz?')
    await user.click(screen.getByRole('button', { name: 'Tovább' }))
    await user.click(await screen.findByRole('radio', { name: /Nagy Bence/ }))
    await user.click(screen.getByRole('button', { name: 'Tovább' }))
    await user.type(await screen.findByPlaceholderText('25 000'), '150000')
    await user.click(screen.getByRole('button', { name: 'Tovább' }))

    await waitFor(() => expect(screen.getByText(/Nincs elég fedezet/)).toBeInTheDocument())
    expect(screen.queryByText('Minden stimmel?')).not.toBeInTheDocument()
    expect(transferMock).not.toHaveBeenCalled()
  })
})
