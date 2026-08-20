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
  // Unified polling configuration (3 seconds interval, 90 seconds max timeout)
  pollingIntervalMs: 3000,
  pollingMaxAttempts: 30,
};

