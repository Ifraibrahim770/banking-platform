import { authFetch } from './apiUtils';

/**
 * Create a new account (admin only)
 * @param {Object} accountData - Account creation data
 * @returns {Promise<Object>} The created account
 */
export const createAccount = async (accountData) => {
  return authFetch('/accounts/v1', {
    method: 'POST',
    body: JSON.stringify(accountData)
  });
};

/**
 * Update an account (admin only)
 * @param {string} accountNumber - The account number
 * @param {Object} accountData - Account update data
 * @returns {Promise<Object>} The updated account
 */
export const updateAccount = async (accountNumber, accountData) => {
  return authFetch(`/accounts/v1/${accountNumber}`, {
    method: 'PUT',
    body: JSON.stringify(accountData)
  });
};

/**
 * Get account balance
 * @param {string} accountNumber - The account number
 * @returns {Promise<Object>} The account balance
 */
export const getAccountBalance = async (accountNumber) => {
  return authFetch(`/accounts/v1/${accountNumber}/balance`, {
    method: 'GET'
  });
};

/**
 * Activate an account (admin only)
 * @param {string} accountNumber - The account number
 * @returns {Promise<Object>} The activation result
 */
export const activateAccount = async (accountNumber) => {
  return authFetch(`/accounts/v1/${accountNumber}/activate`, {
    method: 'PATCH'
  });
};

/**
 * Deactivate an account (admin only)
 * @param {string} accountNumber - The account number
 * @returns {Promise<Object>} The deactivation result
 */
export const deactivateAccount = async (accountNumber) => {
  return authFetch(`/accounts/v1/${accountNumber}/deactivate`, {
    method: 'PATCH'
  });
};

/**
 * Credit an account (admin only)
 * @param {string} accountNumber - The account number
 * @param {number} amount - The amount to credit
 * @returns {Promise<Object>} The credit result
 */
export const creditAccount = async (accountNumber, amount) => {
  return authFetch(`/accounts/v1/${accountNumber}/credit`, {
    method: 'POST',
    body: JSON.stringify({ amount })
  });
};

/**
 * Debit an account (admin only)
 * @param {string} accountNumber - The account number
 * @param {number} amount - The amount to debit
 * @returns {Promise<Object>} The debit result
 */
export const debitAccount = async (accountNumber, amount) => {
  return authFetch(`/accounts/v1/${accountNumber}/debit`, {
    method: 'POST',
    body: JSON.stringify({ amount })
  });
};

/**
 * Get account status (admin only)
 * @param {string} accountNumber - The account number
 * @returns {Promise<Object>} The account status
 */
export const getAccountStatus = async (accountNumber) => {
  return authFetch(`/accounts/v1/${accountNumber}/status`, {
    method: 'GET'
  });
};

/**
 * Get user accounts by profile ID
 * @param {string|number} profileId - The profile ID
 * @returns {Promise<Array>} The user's accounts
 */
export const getUserAccounts = async (profileId) => {
  return authFetch(`/accounts/v1/user/${profileId}`, {
    method: 'GET'
  });
}; 