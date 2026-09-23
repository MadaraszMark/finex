import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { ArrowRight, Eye, EyeOff, LoaderCircle } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate } from 'react-router'
import { z } from 'zod'
import { login } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { startSession } from '../auth/session'
import { useAuthToken } from '../auth/useAuthToken'
import Logo from '../components/Logo'
import SystemCheck from '../components/SystemCheck'
import { useBackendStatus } from '../hooks/useBackendStatus'
import { queryCheck, type Check } from '../lib/checks'

const loginSchema = z.object({
  email: z.email('Érvényes e-mail címet adj meg.'),
  password: z.string().min(1, 'Add meg a jelszavad.'),
})

type LoginForm = z.infer<typeof loginSchema>

const inputClass =
  'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 transition placeholder:text-slate-400 focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100 focus:outline-hidden'

export default function LoginPage() {
  const token = useAuthToken()
  const backendStatus = useBackendStatus()
  const [showPassword, setShowPassword] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  // Sikeres belépés után a token eltárolódik, és a lenti <Navigate> átirányít a főoldalra
  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (response) => startSession(response.token),
  })

  if (token) {
    return <Navigate to="/" replace />
  }

  const checks: Check[] = [
    { label: 'Frontend (React + Vite)', status: 'ok', detail: 'Ez az oldal fut, a Tailwind stílusok betöltődtek.' },
    queryCheck('Backend kapcsolat (proxy → :8080)', backendStatus, (status) => `Elérhető, ${status.endpointCount} API-végpont`),
    { label: 'Adatbázis', status: 'idle', detail: 'Bejelentkezés után ellenőrizhető.' },
  ]

  return (
    <div className="flex min-h-screen flex-col lg:flex-row">
      <div className="relative flex flex-col justify-between overflow-hidden bg-linear-to-br from-brand-400 via-brand-500 to-brand-700 px-8 py-10 text-white lg:w-1/2 lg:px-16 lg:py-14">
        <div aria-hidden className="absolute -top-24 -right-24 size-80 rounded-full bg-white/10" />
        <div aria-hidden className="absolute -bottom-32 -left-20 size-96 rounded-full bg-white/5" />

        <Logo light />
        <div className="relative mt-16 lg:mt-0">
          <h1 className="text-4xl leading-tight font-bold lg:text-5xl">
            Örülünk, hogy
            <br />
            újra itt vagy!
          </h1>
          <p className="mt-4 max-w-sm text-white/80">Lépj be a FineX fiókodba, és kezeld a pénzügyeidet egy helyen.</p>
        </div>
        <p className="relative mt-10 text-sm text-white/60">FineX · szakdolgozati projekt</p>
      </div>

      <div className="flex flex-1 items-center justify-center px-6 py-10">
        <div className="w-full max-w-md space-y-6">
          <div className="rounded-3xl bg-white p-8 shadow-xl ring-1 ring-slate-200">
            <h2 className="text-2xl font-semibold text-slate-900">Bejelentkezés</h2>

            <form noValidate onSubmit={handleSubmit((values) => loginMutation.mutate(values))} className="mt-6 space-y-5">
              <div>
                <label htmlFor="email" className="text-sm font-medium text-slate-700">
                  E-mail cím
                </label>
                <div className="mt-1.5">
                  <input
                    id="email"
                    type="email"
                    autoComplete="email"
                    placeholder="pelda@email.hu"
                    className={inputClass}
                    {...register('email')}
                  />
                </div>
                {errors.email && <p className="mt-1.5 text-sm text-red-600">{errors.email.message}</p>}
              </div>

              <div>
                <label htmlFor="password" className="text-sm font-medium text-slate-700">
                  Jelszó
                </label>
                <div className="relative mt-1.5">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    className={`${inputClass} pr-12`}
                    {...register('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((visible) => !visible)}
                    aria-label={showPassword ? 'Jelszó elrejtése' : 'Jelszó megjelenítése'}
                    className="absolute inset-y-0 right-0 flex items-center px-4 text-slate-400 transition hover:text-brand-600"
                  >
                    {showPassword ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
                  </button>
                </div>
                {errors.password && <p className="mt-1.5 text-sm text-red-600">{errors.password.message}</p>}
              </div>

              {loginMutation.isError && (
                <div role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-red-200">
                  {getErrorMessage(loginMutation.error)}
                </div>
              )}

              <button
                type="submit"
                disabled={loginMutation.isPending}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-4 py-3 font-semibold text-white shadow-lg shadow-brand-600/30 transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {loginMutation.isPending ? (
                  <LoaderCircle className="size-5 animate-spin" />
                ) : (
                  <>
                    Belépés
                    <ArrowRight className="size-5" />
                  </>
                )}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-slate-500">
              Még nincs fiókod? A regisztráció egyelőre a{' '}
              <a
                href="http://localhost:8080/swagger-ui.html"
                target="_blank"
                rel="noreferrer"
                className="font-medium text-brand-600 hover:underline"
              >
                Swaggerben
              </a>{' '}
              érhető el.
            </p>
          </div>

          <SystemCheck title="Rendszerállapot" checks={checks} />
        </div>
      </div>
    </div>
  )
}
