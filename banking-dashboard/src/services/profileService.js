import { authFetch } from './apiUtils';

/**
 * Get the current user's profile
 * @returns {Promise<Object>} The user profile
 */
export const getCurrentUserProfile = async () => {
  return authFetch('/profile/v1/me', {
    method: 'GET'
  });
};

/**
 * Update the current user's profile
 * @param {Object} profileData - The profile data to update
 * @returns {Promise<Object>} The updated profile
 */
export const updateUserProfile = async (profileData) => {
  return authFetch('/profile/v1/me', {
    method: 'PUT',
    body: JSON.stringify(profileData)
  });
};

/**
 * Get a user profile by ID (admin only)
 * @param {number} userId - The user ID
 * @returns {Promise<Object>} The user profile
 */
export const getUserProfileById = async (userId) => {
  return authFetch(`/profile/v1/${userId}`, {
    method: 'GET'
  });
}; 