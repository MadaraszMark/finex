import { Pencil, Plus, Tags, Trash2 } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { CategoryResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import { cardSurface } from '../../../components/ui/cardStyles'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import EmptyState from '../../../components/ui/EmptyState'
import IconButton from '../../../components/ui/IconButton'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import { categoryIcon } from '../../transactions/categoryIcons'
import { useCategories } from '../../transactions/queries'
import { AdminCategoriesSkeleton } from '../components/AdminSkeletons'
import CategoryDialog from '../components/CategoryDialog'
import { useDeleteCategory } from '../queries'

// Tranzakciókategóriák kezelése: létrehozás, átnevezés, ikoncsere és törlés
// (tranzakcióhoz már hozzárendelt kategória nem törölhető – ezt a backend jelzi)
export default function AdminCategoriesPage() {
  const categoriesQuery = useCategories()
  const deleteCategory = useDeleteCategory()
  const [editing, setEditing] = useState<CategoryResponse | null>()
  const [deleting, setDeleting] = useState<CategoryResponse>()

  const categories = categoriesQuery.data ?? []

  function confirmDelete() {
    if (!deleting) {
      return
    }
    deleteCategory.mutate(deleting.id, {
      onSuccess: () => toast.success('Törölted a kategóriát', { description: deleting.name }),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setDeleting(undefined),
    })
  }

  return (
    <>
      <PageHeader
        eyebrow="Adminisztráció"
        title="Kategóriák"
        description="A tranzakciók kategóriái, ikonnal."
        actions={
          <Button variant="glass" leftIcon={<Plus />} onClick={() => setEditing(null)}>
            Új kategória
          </Button>
        }
      />

      {categoriesQuery.isPending ? (
        <AdminCategoriesSkeleton />
      ) : categoriesQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(categoriesQuery.error)} retrying={categoriesQuery.isFetching} onRetry={() => void categoriesQuery.refetch()} />
        </Card>
      ) : categories.length === 0 ? (
        <Card>
          <EmptyState
            icon={<Tags />}
            title="Még nincs kategória"
            action={
              <Button leftIcon={<Plus />} onClick={() => setEditing(null)}>
                Új kategória
              </Button>
            }
          />
        </Card>
      ) : (
        <>
          <p className="mb-3 text-sm font-semibold text-muted-foreground">{categories.length} kategória</p>
          <motion.ul variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-[repeat(auto-fill,minmax(min(18rem,100%),1fr))] gap-3">
            {categories.map((category) => (
              <motion.li key={category.id} variants={staggerItem} className={cn(cardSurface, 'spotlight group flex items-center gap-3 p-4')}>
                <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-brand-100 text-brand-600 transition group-hover:bg-brand-gradient group-hover:text-white dark:bg-brand-950 dark:text-brand-300">
                  {categoryIcon(category.icon, 'size-5')}
                </span>
                <span title={category.name} className="min-w-0 flex-1 truncate font-semibold text-foreground">
                  {category.name}
                </span>
                <span className="flex shrink-0 gap-1">
                  <IconButton label={`${category.name} szerkesztése`} onClick={() => setEditing(category)} className="size-9">
                    <Pencil />
                  </IconButton>
                  <IconButton label={`${category.name} törlése`} onClick={() => setDeleting(category)} className="size-9 hover:border-danger/50 hover:text-danger">
                    <Trash2 />
                  </IconButton>
                </span>
              </motion.li>
            ))}
          </motion.ul>
        </>
      )}

      <CategoryDialog category={editing} onClose={() => setEditing(undefined)} />

      <ConfirmDialog
        open={deleting !== undefined}
        onClose={() => setDeleting(undefined)}
        onConfirm={confirmDelete}
        loading={deleteCategory.isPending}
        title="Törlöd a kategóriát?"
        description={deleting ? `„${deleting.name}” végleg törlődik. Tranzakcióhoz már hozzárendelt kategória nem törölhető.` : undefined}
        confirmLabel="Törlés"
      />
    </>
  )
}
