import axios from 'axios'

const baseUrl = '/api/admin/risk-control'

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
    return axios.get<RiskOverviewVO>(`${baseUrl}/overview`)
  },

  getCircuitBreakers() {
    return axios.get<CircuitBreakerVO[]>(`${baseUrl}/circuit-breakers`)
  },

  getRateLimiters() {
    return axios.get<RateLimiterVO[]>(`${baseUrl}/rate-limiters`)
  }
}
