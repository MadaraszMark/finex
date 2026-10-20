import { Pencil, Plus, Search, Send, Trash2, UsersRound } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { BeneficiaryResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import ButtonLink from '../../../components/ui/ButtonLink'
import { Card } from '../../../components/ui/Card'
import { cardSurface } from '../../../components/ui/cardStyles'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import CopyButton from '../../../components/ui/CopyButton'
import EmptyState from '../../../components/ui/EmptyState'
import IconButton from '../../../components/ui/IconButton'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { formatIban, nameInitials } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import BeneficiariesSkeleton from '../components/BeneficiariesSkeleton'
import BeneficiaryDialog from '../components/BeneficiaryDialog'
import { useBeneficiaries, useDeleteBeneficiary } from '../queries'

function transferLink(beneficiary: BeneficiaryResponse): string {
  return `/transfer?${new URLSearchParams({ to: beneficiary.accountNumber, name: beneficiary.name })}`
}

function searchable(value: string): string {
  return value.toLowerCase().replace(/\s/g, '')
}

// Kedvezményezettek: a gyakori címzettek kártyákon, egy érintéses utalással, szerkesztéssel és törléssel
export default function BeneficiariesPage() {
  const beneficiariesQuery = useBeneficiaries()
  const deleteBeneficiary = useDeleteBeneficiary()
  const [editor, setEditor] = useState<{ open: boolean; beneficiary?: BeneficiaryResponse; key: number }>({ open: false, key: 0 })
  const [deleting, setDeleting] = useState<BeneficiaryResponse>()
  const [query, setQuery] = useState('')

  const beneficiaries = beneficiariesQuery.data ?? []
  const filtered = beneficiaries.filter((beneficiary) =>
    searchable(`${beneficiary.name}${beneficiary.accountNumber}${beneficiary.note ?? ''}`).includes(searchable(query)),
  )

  function openEditor(beneficiary?: BeneficiaryResponse) {
    setEditor((current) => ({ open: true, beneficiary, key: current.key + 1 }))
  }

  function confirmDelete() {
    if (!deleting) {
      return
    }
    deleteBeneficiary.mutate(deleting.id, {
      onSuccess: () => {
        toast.success('Töröltük a kedvezményezettet', { description: deleting.name })
        setDeleting(undefined)
      },
    })
  }

  return (
    <>
      <PageHeader
        title="Kedvezményezettek"
        description="A gyakori címzettjeid – egy érintéssel utalhatsz nekik."
        actions={
          <Button variant="glass" leftIcon={<Plus />} onClick={() => openEditor()}>
            Új kedvezményezett
          </Button>
        }
      />

      {beneficiariesQuery.isPending ? (
        <BeneficiariesSkeleton />
      ) : beneficiariesQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(beneficiariesQuery.error)} retrying={beneficiariesQuery.isFetching} onRetry={() => void beneficiariesQuery.refetch()} />
        </Card>
      ) : beneficiaries.length === 0 ? (
        <Card>
          <EmptyState
            icon={<UsersRound />}
            title="Még nincs kedvezményezetted"
            description="Mentsd el azokat, akiknek gyakran utalsz (pl. albérlet, rezsi), így nem kell mindig beírni a számlaszámukat."
            action={
              <Button leftIcon={<Plus />} onClick={() => openEditor()}>
                Kedvezményezett hozzáadása
              </Button>
            }
          />
        </Card>
      ) : (
        <>
          {beneficiaries.length > 6 && (
            <TextField
              label="Keresés"
              type="search"
              placeholder="Név, számlaszám vagy megjegyzés"
              leftIcon={<Search />}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className="mb-6 max-w-md"
            />
          )}

          <motion.ul variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
            {filtered.map((beneficiary) => (
              <motion.li key={beneficiary.id} variants={staggerItem} className={cn(cardSurface, 'spotlight flex flex-col p-5')}>
                <div className="flex items-start gap-4">
                  <span aria-hidden className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-brand-gradient font-bold text-white shadow-glow">
                    {nameInitials(beneficiary.name)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-lg font-bold text-foreground">{beneficiary.name}</p>
                    {/* Megjegyzés nélkül is megtartja a sor magasságát, így a szomszédos kártyák számlaszáma egy vonalban marad */}
                    <p className="min-h-5 truncate text-sm text-muted-foreground">{beneficiary.note}</p>
                  </div>
                  {beneficiary.internal && (
                    <span className="shrink-0 rounded-full bg-brand-100 px-2.5 py-0.5 text-xs font-bold text-brand-700 dark:bg-brand-950 dark:text-brand-300" title="FineX-es számla: az utalás azonnal megérkezik">
                      FineX
                    </span>
                  )}
                </div>

                <div className="mt-4 flex items-center gap-1 rounded-2xl bg-muted/70 py-1.5 pr-1.5 pl-3.5">
                  <span className="min-w-0 flex-1 font-mono text-sm tracking-wide break-all text-foreground">{formatIban(beneficiary.accountNumber)}</span>
                  <CopyButton
                    value={beneficiary.accountNumber}
                    label={`${beneficiary.name} számlaszámának másolása`}
                    successMessage="A számlaszámot a vágólapra másoltuk."
                    className="size-9 shrink-0 border-transparent bg-transparent"
                  />
                </div>

                <div className="mt-auto flex items-center gap-2 pt-5">
                  <ButtonLink to={transferLink(beneficiary)} size="sm" leftIcon={<Send />} className="flex-1">
                    Utalás
                  </ButtonLink>
                  <IconButton label={`${beneficiary.name} szerkesztése`} onClick={() => openEditor(beneficiary)} className="size-9">
                    <Pencil />
                  </IconButton>
                  <IconButton label={`${beneficiary.name} törlése`} onClick={() => setDeleting(beneficiary)} className="size-9 hover:border-danger/50 hover:text-danger">
                    <Trash2 />
                  </IconButton>
                </div>
              </motion.li>
            ))}
          </motion.ul>
          {filtered.length === 0 && <p className="py-10 text-center text-muted-foreground">Nincs a keresésnek megfelelő kedvezményezett.</p>}
        </>
      )}

      <BeneficiaryDialog key={editor.key} open={editor.open} beneficiary={editor.beneficiary} onClose={() => setEditor((current) => ({ ...current, open: false }))} />

      <ConfirmDialog
        open={deleting !== undefined}
        onClose={() => {
          setDeleting(undefined)
          deleteBeneficiary.reset()
        }}
        onConfirm={confirmDelete}
        loading={deleteBeneficiary.isPending}
        title="Törlöd a kedvezményezettet?"
        description={deleting ? `${deleting.name} lekerül a listáról. A korábbi utalásaid megmaradnak.` : undefined}
        confirmLabel="Törlés"
      >
        {deleteBeneficiary.isError && <p className="text-sm text-danger">{getErrorMessage(deleteBeneficiary.error)}</p>}
      </ConfirmDialog>
    </>
  )
}
