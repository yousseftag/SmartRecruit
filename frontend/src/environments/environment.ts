export const environment = {
  production: true,
  keycloak: {
    url: 'http://localhost:8081',
    realm: 'smartrecruit',
    clientId: 'smartrecruit-frontend',
    // Seconds before expiry to attempt a refresh (safety cushion)
    tokenRefreshThresholdSeconds: 30,
  },
  apiUrl: 'http://localhost:8080',
  polling: {
    intervalMs: 3000,
    backoffMultiplier: 1.5,
    maxIntervalMs: 15_000,
    stalledAfterAttempts: 10,
    maxAttempts: 30,
    maxRestorePollAgeMs: 180_000,
  },
};
