import { authFetch } from './apiUtils';

/**
 * Create a deposit transaction
 * @param {Object} transactionData - Deposit transaction data
 * @returns {Promise<Object>} The created transaction
 */
export const createDepositTransaction = async (transactionData) => {
  return authFetch('/transactions/v1/deposit', {
    method: 'POST',
    body: JSON.stringify(transactionData)
  });
};

/**
 * Create a withdrawal transaction
 * @param {Object} transactionData - Withdrawal transaction data
 * @returns {Promise<Object>} The created transaction
 */
export const createWithdrawalTransaction = async (transactionData) => {
  return authFetch('/transactions/v1/withdrawal', {
    method: 'POST',
    body: JSON.stringify(transactionData)
  });
};

/**
 * Create a transfer transaction
 * @param {Object} transactionData - Transfer transaction data
 * @returns {Promise<Object>} The created transaction
 */
export const createTransferTransaction = async (transactionData) => {
  return authFetch('/transactions/v1/transfer', {
    method: 'POST',
    body: JSON.stringify(transactionData)
  });
};

/**
 * Get transaction by reference
 * @param {string} transactionReference - The transaction reference
 * @returns {Promise<Object>} The transaction details
 */
export const getTransactionByReference = async (transactionReference) => {
  return authFetch(`/transactions/v1/${transactionReference}`, {
    method: 'GET'
  });
};

/**
 * Create a deposit transaction for a specific account
 * @param {string} accountId - The account number to deposit to
 * @param {number} amount - The amount to deposit
 * @param {string} description - Optional transaction description
 * @returns {Promise<Object>} The transaction response
 */
export const depositToAccount = async (accountId, amount, description = "") => {
  return authFetch('/transactions/v1/deposit', {
    method: 'POST',
    body: JSON.stringify({
      accountId,
      amount,
      currency: "KES", // Default currency
      description,
      userId: localStorage.getItem("userId") // Get the current user ID
    })
  });
};

/**
 * Create a withdrawal transaction for a specific account
 * @param {string} accountId - The account number to withdraw from
 * @param {number} amount - The amount to withdraw
 * @param {string} description - Optional transaction description
 * @returns {Promise<Object>} The transaction response
 */
export const withdrawFromAccount = async (accountId, amount, description = "") => {
  return authFetch('/transactions/v1/withdraw', {
    method: 'POST',
    body: JSON.stringify({
      accountId,
      amount,
      currency: "KES", // Default currency
      description,
      userId: localStorage.getItem("userId") // Get the current user ID
    })
  });
};

/**
 * Create a transfer transaction between accounts
 * @param {string} sourceAccountId - The account number to transfer from
 * @param {string} destinationAccountId - The account number to transfer to
 * @param {number} amount - The amount to transfer
 * @param {string} description - Optional transaction description
 * @returns {Promise<Object>} The transaction response
 */
export const transferBetweenAccounts = async (sourceAccountId, destinationAccountId, amount, description = "") => {
  return authFetch('/transactions/v1/transfer', {
    method: 'POST',
    body: JSON.stringify({
      sourceAccountId,
      destinationAccountId,
      amount,
      currency: "KES", // Default currency
      description,
      userId: localStorage.getItem("userId") // Get the current user ID
    })
  });
};

/**
 * Get user transactions
 * @param {string|number} userId - The user ID
 * @returns {Promise<Array>} List of user transactions
 */
export const getUserTransactions = async (userId) => {
  return authFetch(`/transactions/v1/user/${userId}`, {
    method: 'GET'
  });
}; 