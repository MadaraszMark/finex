import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { ArrowRight, Lock, Mail, Phone } from 'lucide-react'
import { AnimatePresence, motion, useAnimate, useReducedMotion } from 'motion/react'
import { useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
import { toast } from 'sonner'
import { register as registerUser } from '../../../api/auth'
import { getErrorMessage, getFieldErrors } from '../../../api/client'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import PasswordField from '../../../components/ui/PasswordField'
import TextField from '../../../components/ui/TextField'
import { shakeKeyframes, staggerContainer, staggerItem } from '../../../lib/motion'
import { startSession } from '../../../session/session'
import BackToWelcomeLink from '../components/BackToWelcomeLink'
import PasswordChecklist from '../components/PasswordChecklist'
import { isRegisterField, registerSchema, type RegisterForm } from '../schemas'

// A regisztrációs űrlap (a keretet, hátteret és címet az AuthLayout adja)
export default function RegisterPage() {
  const [scope, animate] = useAnimate()
  const reduceMotion = useReducedMotion()

  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    mode: 'onTouched',
    defaultValues: { lastName: '', firstName: '', email: '', phone: '', password: '', passwordConfirm: '' },
  })

  const password = useWatch({ control, name: 'password' })

  // Sikeres regisztráció után a felhasználó rögtön be is van jelentkezve (a backend számlát és kártyát nyitott neki)
  const registerMutation = useMutation({
    mutationFn: registerUser,
    onSuccess: (response) => {
      startSession(response)
      toast.success(`Üdv a FineX-ben, ${response.user.firstName}!`, {
        description: 'Nyitottunk neked egy forint folyószámlát bankkártyával.',
      })
    },
    // A backend mezőhibáit (violations) a megfelelő mező alá írjuk
    onError: (error) => {
      Object.entries(getFieldErrors(error)).forEach(([field, message]) => {
        if (isRegisterField(field)) {
          setError(field, { message })
        }
      })
      if (!reduceMotion) {
        animate(scope.current, shakeKeyframes, { duration: 0.45 })
      }
    },
  })

  function onSubmit(values: RegisterForm) {
    registerMutation.mutate({
      lastName: values.lastName,
      firstName: values.firstName,
      email: values.email,
      phone: values.phone || undefined,
      password: values.password,
    })
  }

  return (
    <motion.div ref={scope} variants={staggerContainer} initial="hidden" animate="show">
      <motion.div variants={staggerItem}>
        <h2 className="text-2xl font-extrabold tracking-tight sm:text-3xl">Regisztráció</h2>
        <p className="mt-1.5 text-muted-foreground">Néhány adat, és már használhatod is a számládat.</p>
      </motion.div>

      <form noValidate onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-5">
        <motion.div variants={staggerItem} className="grid gap-5 sm:grid-cols-2">
          <TextField label="Vezetéknév" autoComplete="family-name" placeholder="Kovács" error={errors.lastName?.message} {...register('lastName')} />
          <TextField label="Keresztnév" autoComplete="given-name" placeholder="Anna" error={errors.firstName?.message} {...register('firstName')} />
        </motion.div>
        <motion.div variants={staggerItem}>
          <TextField
            label="E-mail cím"
            type="email"
            autoComplete="email"
            placeholder="pelda@email.hu"
            leftIcon={<Mail />}
            error={errors.email?.message}
            {...register('email')}
          />
        </motion.div>
        <motion.div variants={staggerItem}>
          <TextField
            label="Telefonszám (nem kötelező)"
            type="tel"
            autoComplete="tel"
            placeholder="+36 30 123 4567"
            leftIcon={<Phone />}
            error={errors.phone?.message}
            {...register('phone')}
          />
        </motion.div>
        <motion.div variants={staggerItem}>
          <PasswordField label="Jelszó" autoComplete="new-password" leftIcon={<Lock />} error={errors.password?.message} {...register('password')} />
          <PasswordChecklist password={password} />
        </motion.div>
        <motion.div variants={staggerItem}>
          <PasswordField
            label="Jelszó még egyszer"
            autoComplete="new-password"
            leftIcon={<Lock />}
            error={errors.passwordConfirm?.message}
            {...register('passwordConfirm')}
          />
        </motion.div>

        <AnimatePresence initial={false}>
          {registerMutation.isError && (
            <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}>
              <Alert tone="danger">{getErrorMessage(registerMutation.error)}</Alert>
            </motion.div>
          )}
        </AnimatePresence>

        <motion.div variants={staggerItem} className="flex items-center gap-3 pt-2">
          <BackToWelcomeLink />
          <Button type="submit" size="lg" loading={registerMutation.isPending} rightIcon={<ArrowRight />} className="min-w-0 flex-1 justify-between">
            Fiók nyitása
          </Button>
        </motion.div>
      </form>

      <motion.p variants={staggerItem} className="mt-8 border-t border-border pt-6 text-center text-sm text-muted-foreground">
        Már van fiókod?{' '}
        <Link to="/login" className="font-semibold text-primary hover:underline">
          Jelentkezz be
        </Link>
      </motion.p>
    </motion.div>
  )
}
