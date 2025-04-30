import { authFetch } from './apiUtils';

/**
 * Process a payment between accounts
 * @param {string} fromAccount - Source account number
 * @param {string} toAccount - Destination account number
 * @param {number} amount - Payment amount
 * @returns {Promise<Object>} The payment result
 */
export const processPayment = async (fromAccount, toAccount, amount) => {
  return authFetch(`/payments/v1/process?fromAccount=${fromAccount}&toAccount=${toAccount}&amount=${amount}`, {
    method: 'POST'
  });
};

/**
 * Get payment details by transaction ID
 * @param {string} transactionId - The transaction ID
 * @returns {Promise<Object>} The payment details
 */
export const getPaymentDetails = async (transactionId) => {
  return authFetch(`/payments/v1/${transactionId}`, {
    method: 'GET'
  });
}; 