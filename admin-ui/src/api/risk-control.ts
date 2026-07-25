import request from './request'

export interface CircuitBreakerVO {
  name: string
  state: string
  failureRate: number
  numberOfBufferedCalls: number
  numberOfFailedCalls: number
  numberOfSuccessfulCalls: number
}

export interface RateLimiterVO {
  name: string
  availablePermissions: number
  numberOfWaitingThreads: number
}

export interface RiskOverviewVO {
  circuitBreakers: CircuitBreakerVO[]
  rateLimiters: RateLimiterVO[]
}

export const riskControlApi = {
  getOverview() {
    return request.get<RiskOverviewVO>('/admin/risk-control/overview')
  },

  getCircuitBreakers() {
    return request.get<CircuitBreakerVO[]>('/admin/risk-control/circuit-breakers')
  },

  getRateLimiters() {
    return request.get<RateLimiterVO[]>('/admin/risk-control/rate-limiters')
  }
}
