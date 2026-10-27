import { z } from 'zod'

// A backend validációját követi (CreateCategoryRequest, UpdateCategoryRequest)
export const categorySchema = z.object({
  name: z.string().trim().min(1, 'Add meg a kategória nevét.').max(100, 'Legfeljebb 100 karakter lehet.'),
  icon: z.string().min(1, 'Válassz ikont.').max(20),
})

export type CategoryForm = z.infer<typeof categorySchema>
