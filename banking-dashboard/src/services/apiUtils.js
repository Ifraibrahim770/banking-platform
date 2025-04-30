/**
 * API Utilities for making authenticated requests
 */

// Check if the environment variable has been replaced, if not use a default for local development
const API_BASE_URL = (() => {
  const placeholder = "REACT_APP_API_URL_PLACEHOLDER";
  // If the placeholder is still in the code, it means the environment variable wasn't injected
  if (placeholder === "REACT_APP_API_URL_PLACEHOLDER") {
    return "http://localhost:8080/api";
  }
  return placeholder + "/api";
})();

/**
 * Get the auth token from local storage
 * @returns {string|null} - The authentication token or null
 */
export const getAuthToken = () => {
  return localStorage.getItem("accessToken");
};

/**
 * Make an authenticated API request
 * @param {string} endpoint - API endpoint (without base URL)
 * @param {Object} options - Fetch options
 * @returns {Promise} - Fetch promise
 */
export const authFetch = async (endpoint, options = {}) => {
  const token = getAuthToken();
  const tokenType = localStorage.getItem("tokenType") || "Bearer";
  
  const defaultHeaders = {
    "Content-Type": "application/json",
  };
  
  if (token) {
    defaultHeaders.Authorization = `${tokenType} ${token}`;
  }
  
  const fetchOptions = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };
  
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, fetchOptions);
    const data = await response.json();
    
    if (!response.ok) {
      // Handle 401 Unauthorized - token might be expired
      if (response.status === 401) {
        // Clear the token and user from storage
        localStorage.removeItem("accessToken");
        localStorage.removeItem("tokenType");
        localStorage.removeItem("user");
        
        // Force redirect to login page
        window.location.href = "/login";
      }
      
      throw new Error(data.message || `Request failed with status ${response.status}`);
    }
    
    return data;
  } catch (error) {
    console.error("API request failed:", error);
    throw error;
  }
};

/**
 * Check if the user is authenticated
 * @returns {boolean} - Whether the user is authenticated
 */
export const isAuthenticated = () => {
  return !!getAuthToken();
};

/**
 * Check if the user has admin role
 * @returns {boolean} - Whether the user is an admin
 */
export const isAdmin = () => {
  try {
    const user = JSON.parse(localStorage.getItem("user"));
    return user && user.roles && user.roles.includes("ROLE_ADMIN");
  } catch (error) {
    return false;
  }
}; 