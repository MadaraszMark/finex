import { zodResolver } from '@hookform/resolvers/zod'
import { Pencil, Tags } from 'lucide-react'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { CategoryResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { categoryIcon, categoryIconLabels, categoryIconNames } from '../../transactions/categoryIcons'
import { useSaveCategory } from '../queries'
import { categorySchema, type CategoryForm } from '../schemas'

interface CategoryDialogProps {
  // undefined: zárva; null: új kategória; egyébként a szerkesztett kategória
  category: CategoryResponse | null | undefined
  onClose: () => void
}

// Kategória létrehozása vagy szerkesztése: név és ikon, felül élő előnézettel (így jelenik meg a tranzakcióknál)
export default function CategoryDialog({ category, onClose }: CategoryDialogProps) {
  // Bezáráskor a tartalom a kiúszó animáció alatt is a korábbi kategóriát mutatja
  const [shown, setShown] = useState(category)
  if (category !== undefined && category !== shown) {
    setShown(category)
  }
  const editing = shown ?? undefined

  return (
    <Dialog
      open={category !== undefined}
      onClose={onClose}
      icon={editing ? <Pencil /> : <Tags />}
      title={editing ? 'Kategória szerkesztése' : 'Új kategória'}
      description="A tranzakciók kategorizálásánál és a statisztikában jelenik meg."
    >
      <CategoryFormBody key={editing?.id ?? 'new'} category={editing} onDone={onClose} />
    </Dialog>
  )
}

function CategoryFormBody({ category, onDone }: { category: CategoryResponse | undefined; onDone: () => void }) {
  const saveCategory = useSaveCategory()
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<CategoryForm>({
    resolver: zodResolver(categorySchema),
    defaultValues: { name: category?.name ?? '', icon: category?.icon ?? 'circle-ellipsis' },
  })
  const name = useWatch({ control, name: 'name' })
  const icon = useWatch({ control, name: 'icon' })

  function onSubmit(values: CategoryForm) {
    saveCategory.mutate(
      { id: category?.id, request: { name: values.name, icon: values.icon } },
      {
        onSuccess: (saved) => {
          toast.success(category ? 'Mentettük a kategóriát' : 'Létrehoztuk a kategóriát', { description: saved.name })
          onDone()
        },
      },
    )
  }

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <div aria-hidden className="flex items-center gap-3 rounded-2xl bg-muted/70 p-3">
        <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-brand-gradient text-white shadow-glow">{categoryIcon(icon, 'size-5')}</span>
        <span className="min-w-0">
          <span className="block truncate font-semibold text-foreground">{name.trim() || 'Kategória neve'}</span>
          <span className="block text-xs text-muted-foreground">Előnézet</span>
        </span>
      </div>

      <TextField label="Név" placeholder="pl. Háztartás" maxLength={100} error={errors.name?.message} {...register('name')} />

      <fieldset>
        <legend className="mb-2 text-sm font-medium text-foreground">Ikon</legend>
        <div className="grid grid-cols-6 gap-2 sm:grid-cols-8">
          {categoryIconNames.map((iconName) => (
            <label key={iconName} title={categoryIconLabels[iconName]} className="cursor-pointer">
              <input type="radio" value={iconName} aria-label={categoryIconLabels[iconName]} className="peer sr-only" {...register('icon')} />
              <span
                className={cn(
                  'flex aspect-square items-center justify-center rounded-xl border border-border text-muted-foreground transition duration-200',
                  'hover:border-brand-300 hover:text-foreground peer-focus-visible:ring-4 peer-focus-visible:ring-ring/40',
                  'peer-checked:border-transparent peer-checked:bg-brand-gradient peer-checked:text-white peer-checked:shadow-glow',
                )}
              >
                {categoryIcon(iconName, 'size-5')}
              </span>
            </label>
          ))}
        </div>
        {errors.icon && <p className="mt-2 text-sm text-danger">{errors.icon.message}</p>}
      </fieldset>

      {saveCategory.isError && <Alert tone="danger">{getErrorMessage(saveCategory.error)}</Alert>}

      <DialogActions>
        <Button variant="ghost" onClick={onDone}>
          Mégse
        </Button>
        <Button type="submit" loading={saveCategory.isPending}>
          {category ? 'Mentés' : 'Létrehozás'}
        </Button>
      </DialogActions>
    </form>
  )
}
