import { api } from './client'
import type { CreateStandingOrderRequest, StandingOrderResponse, UpdateStandingOrderRequest } from './types'

// StandingOrderController (/standing-orders): rendszeres átutalások (a backend ütemezője óránként teljesíti az esedékeseket)

// A megszüntetett megbízások is benne vannak (active = false), a legújabb elöl
export async function getStandingOrders(): Promise<StandingOrderResponse[]> {
  const { data } = await api.get<StandingOrderResponse[]>('/standing-orders')
  return data
}

export async function createStandingOrder(request: CreateStandingOrderRequest): Promise<StandingOrderResponse> {
  const { data } = await api.post<StandingOrderResponse>('/standing-orders', request)
  return data
}

export async function updateStandingOrder(id: number, request: UpdateStandingOrderRequest): Promise<StandingOrderResponse> {
  const { data } = await api.put<StandingOrderResponse>(`/standing-orders/${id}`, request)
  return data
}

// Megszüntetés: a megbízás megmarad a listában, de többé nem teljesül
export async function cancelStandingOrder(id: number): Promise<void> {
  await api.delete(`/standing-orders/${id}`)
}
