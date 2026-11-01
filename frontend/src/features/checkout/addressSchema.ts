import { z } from 'zod'

export const addressSchema = z.object({
  recipientName: z.string().trim().min(1, 'Recipient is required').max(200),
  phone: z.string().trim().min(1, 'Phone is required').max(50),
  country: z.string().trim().min(1, 'Country is required').max(100),
  city: z.string().trim().min(1, 'City is required').max(100),
  street: z.string().trim().min(1, 'Street is required').max(255),
  postalCode: z.string().trim().min(1, 'Postal code is required').max(30),
})

export type AddressFormValues = z.infer<typeof addressSchema>
