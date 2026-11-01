import type { ReactNode } from 'react'
import type { UseFormRegister, FieldErrors } from 'react-hook-form'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import type { AddressFormValues } from '@/features/checkout/addressSchema'

type AddressFormProps = {
  register: UseFormRegister<AddressFormValues>
  errors: FieldErrors<AddressFormValues>
}

function Field({
  id,
  label,
  error,
  children,
}: {
  id: string
  label: string
  error?: string
  children: ReactNode
}) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={id}>{label}</Label>
      {children}
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  )
}

export function AddressForm({ register, errors }: AddressFormProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <Field id="recipientName" label="Recipient" error={errors.recipientName?.message}>
        <Input id="recipientName" autoComplete="name" {...register('recipientName')} />
      </Field>
      <Field id="phone" label="Phone" error={errors.phone?.message}>
        <Input id="phone" autoComplete="tel" {...register('phone')} />
      </Field>
      <Field id="country" label="Country" error={errors.country?.message}>
        <Input id="country" autoComplete="country-name" {...register('country')} />
      </Field>
      <Field id="city" label="City" error={errors.city?.message}>
        <Input id="city" autoComplete="address-level2" {...register('city')} />
      </Field>
      <Field id="street" label="Street" error={errors.street?.message}>
        <Input id="street" autoComplete="street-address" {...register('street')} />
      </Field>
      <Field id="postalCode" label="Postal code" error={errors.postalCode?.message}>
        <Input id="postalCode" autoComplete="postal-code" {...register('postalCode')} />
      </Field>
    </div>
  )
}
