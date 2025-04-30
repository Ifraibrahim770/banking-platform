import { authFetch } from './apiUtils';

/**
 * Get all notifications (admin only)
 * @returns {Promise<Object[]>} List of all notifications
 */
export const getAllNotifications = async () => {
  return authFetch('/notifications/v1', {
    method: 'GET'
  });
};

/**
 * Get notifications by user ID
 * @param {number} userId - The user ID
 * @returns {Promise<Object[]>} List of user's notifications
 */
export const getNotificationsByUserId = async (userId) => {
  return authFetch(`/notifications/v1/user/${userId}`, {
    method: 'GET'
  });
};

/**
 * Get notifications by transaction reference
 * @param {string} transactionReference - The transaction reference
 * @returns {Promise<Object[]>} List of notifications for the transaction
 */
export const getNotificationsByTransactionReference = async (transactionReference) => {
  return authFetch(`/notifications/v1/transaction/${transactionReference}`, {
    method: 'GET'
  });
}; 