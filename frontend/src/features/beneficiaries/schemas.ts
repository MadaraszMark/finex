import { z } from 'zod'
import { ibanField, messageField, partnerNameField } from '../transfer/schemas'

// A backend szabályai (CreateBeneficiaryRequest / UpdateBeneficiaryRequest): név, érvényes IBAN, rövid megjegyzés
export const beneficiarySchema = z.object({
  name: partnerNameField,
  accountNumber: ibanField,
  note: messageField,
})

export type BeneficiaryForm = z.infer<typeof beneficiarySchema>
