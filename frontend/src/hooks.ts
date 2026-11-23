import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from './api'

interface ApiState<T> {
  data: T | null
  error: string | null
  loading: boolean
  reload: () => void
  setData: (value: T | null) => void
}

/**
 * Loads data whenever `key` changes. The loader is kept in a ref so callers can
 * write it inline without re-running on every render.
 */
export function useApi<T>(key: string, load: () => Promise<T>): ApiState<T> {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [tick, setTick] = useState(0)

  const loadRef = useRef(load)

  // Keep the latest loader without making it an effect dependency.
  useEffect(() => {
    loadRef.current = load
  })

  useEffect(() => {
    let alive = true
    setLoading(true)
    loadRef
      .current()
      .then((value) => {
        if (!alive) return
        setData(value)
        setError(null)
      })
      .catch((cause: unknown) => {
        if (!alive) return
        setError(cause instanceof Error ? cause.message : 'Could not load this screen.')
      })
      .finally(() => {
        if (alive) setLoading(false)
      })
    return () => {
      alive = false
    }
  }, [key, tick])

  const reload = useCallback(() => setTick((value) => value + 1), [])

  return { data, error, loading, reload, setData }
}

/** Delays a fast-changing value (search boxes) before it is sent to the API. */
export function useDebounced<T>(value: T, delay = 250): T {
  const [debounced, setDebounced] = useState(value)

  useEffect(() => {
    const timer = window.setTimeout(() => setDebounced(value), delay)
    return () => window.clearTimeout(timer)
  }, [value, delay])

  return debounced
}

export function errorMessage(cause: unknown): string {
  if (cause instanceof ApiError) return cause.message
  if (cause instanceof Error) return cause.message
  return 'Something went wrong. Please try again.'
}

export function errorField(cause: unknown): string | null {
  return cause instanceof ApiError ? cause.field : null
}
