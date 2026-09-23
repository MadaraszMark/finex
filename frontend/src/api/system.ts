import { api } from './client'

const HTTP_METHODS = new Set(['get', 'post', 'put', 'patch', 'delete'])

export interface BackendStatus {
  endpointCount: number
}

// A backend nyilvános OpenAPI-leírása (Swagger): ha ez válaszol, fut a backend és működik a proxy
export async function getBackendStatus(): Promise<BackendStatus> {
  const { data } = await api.get<{ paths: Record<string, Record<string, unknown>> }>('/v3/api-docs')
  const endpointCount = Object.values(data.paths).reduce(
    (count, operations) => count + Object.keys(operations).filter((method) => HTTP_METHODS.has(method)).length,
    0,
  )
  return { endpointCount }
}
