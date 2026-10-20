import { z } from 'zod'

// A kategória űrlapja a backend validációját követi (CreateCategoryRequest, UpdateCategoryRequest):
// a név kötelező és legfeljebb 100 karakter, az ikon az ikonkészlet egyik neve
export const categorySchema = z.object({
  name: z.string().trim().min(1, 'Add meg a kategória nevét.').max(100, 'Legfeljebb 100 karakter lehet.'),
  icon: z.string().min(1, 'Válassz ikont.').max(20),
})

export type CategoryForm = z.infer<typeof categorySchema>
