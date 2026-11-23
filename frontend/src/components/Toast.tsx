import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { Icon } from './Icons'

type Tone = 'success' | 'error' | 'info'

interface ToastItem {
  id: number
  message: string
  tone: Tone
}

type PushToast = (message: string, tone?: Tone) => void

const ToastContext = createContext<PushToast>(() => {})

const toneClasses: Record<Tone, string> = {
  success: 'border-brand/20 bg-brand-soft text-brand-dark',
  error: 'border-danger/25 bg-danger-soft text-danger',
  info: 'border-line bg-surface text-ink',
}

const toneIcons = {
  success: 'check',
  error: 'alert',
  info: 'info',
} as const

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])

  const push = useCallback<PushToast>((message, tone = 'success') => {
    const id = Date.now() + Math.random()
    setItems((current) => [...current, { id, message, tone }])
    window.setTimeout(() => {
      setItems((current) => current.filter((item) => item.id !== id))
    }, 4500)
  }, [])

  return (
    <ToastContext.Provider value={push}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed inset-x-0 bottom-5 z-[60] flex flex-col items-center gap-2 px-4 sm:items-end sm:pr-6"
      >
        {items.map((item) => (
          <div
            key={item.id}
            className={`animate-slide pointer-events-auto flex max-w-sm items-start gap-2.5 rounded-lg border px-4 py-3 text-[13px] leading-snug shadow-sm ${toneClasses[item.tone]}`}
          >
            <Icon
              name={toneIcons[item.tone]}
              className="mt-0.5 h-4 w-4 shrink-0"
            />
            <span>{item.message}</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): PushToast {
  return useContext(ToastContext)
}
