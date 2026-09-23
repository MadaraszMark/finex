import { useQuery } from '@tanstack/react-query'
import { getBackendStatus } from '../api/system'

export function useBackendStatus() {
  return useQuery({ queryKey: ['backend-status'], queryFn: getBackendStatus })
}
