import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { ArrowRight, Lock, Mail } from 'lucide-react'
import { AnimatePresence, motion, useAnimate, useReducedMotion } from 'motion/react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { toast } from 'sonner'
import { login } from '../../../api/auth'
import { getErrorMessage } from '../../../api/client'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import PasswordField from '../../../components/ui/PasswordField'
import TextField from '../../../components/ui/TextField'
import { shakeKeyframes, staggerContainer, staggerItem } from '../../../lib/motion'
import { startSession } from '../../../session/session'
import BackToWelcomeLink from '../components/BackToWelcomeLink'
import { loginSchema, type LoginForm } from '../schemas'

// A belépő űrlap (a keretet, hátteret és címet az AuthLayout adja)
export default function LoginPage() {
  const [scope, animate] = useAnimate()
  const reduceMotion = useReducedMotion()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  // Sikeres belépés után a munkamenet elindul, és az útvonalvédelem (GuestOnly) továbbirányít
  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      startSession(response)
      toast.success(`Szia, ${response.user.firstName}!`)
    },
    onError: () => {
      if (!reduceMotion) {
        animate(scope.current, shakeKeyframes, { duration: 0.45 })
      }
    },
  })

  return (
    <motion.div ref={scope} variants={staggerContainer} initial="hidden" animate="show">
      <motion.div variants={staggerItem}>
        <h2 className="text-2xl font-extrabold tracking-tight sm:text-3xl">Bejelentkezés</h2>
        <p className="mt-1.5 text-muted-foreground">Add meg az e-mail címed és a jelszavad.</p>
      </motion.div>

      <form noValidate onSubmit={handleSubmit((values) => loginMutation.mutate(values))} className="mt-8 space-y-5">
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
          <PasswordField
            label="Jelszó"
            autoComplete="current-password"
            leftIcon={<Lock />}
            error={errors.password?.message}
            {...register('password')}
          />
        </motion.div>

        <AnimatePresence initial={false}>
          {loginMutation.isError && (
            <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}>
              <Alert tone="danger">{getErrorMessage(loginMutation.error)}</Alert>
            </motion.div>
          )}
        </AnimatePresence>

        <motion.div variants={staggerItem} className="flex items-center gap-3 pt-2">
          <BackToWelcomeLink />
          <Button type="submit" size="lg" loading={loginMutation.isPending} rightIcon={<ArrowRight />} className="min-w-0 flex-1 justify-between">
            Belépés
          </Button>
        </motion.div>
      </form>

      <motion.p variants={staggerItem} className="mt-8 border-t border-border pt-6 text-center text-sm text-muted-foreground">
        Még nincs fiókod?{' '}
        <Link to="/register" className="font-semibold text-primary hover:underline">
          Regisztrálj itt
        </Link>
      </motion.p>
    </motion.div>
  )
}
