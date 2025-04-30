// Authentication Service for the Banking System API
const API_URL = "REACT_APP_API_URL_PLACEHOLDER" + "/api/auth/v1";

/**
 * Register a new user
 * @param {Object} userData - User registration data
 * @returns {Promise} - Promise with registration response
 */
export const signup = async (userData) => {
  try {
    const response = await fetch(`${API_URL}/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: userData.username,
        email: `${userData.username}@example.com`, // Generate a default email
        password: userData.password,
        firstName: "User", // Default first name
        lastName: "Account", // Default last name 
        roles: ["user"] // Default role
      }),
    });

    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.message || "Registration failed");
    }
    
    return data;
  } catch (error) {
    console.error("Signup error:", error);
    throw error;
  }
};

/**
 * Sign in a user
 * @param {string} username - Username or email
 * @param {string} password - User password
 * @returns {Promise} - Promise with authentication response including JWT token
 */
export const signin = async (username, password) => {
  try {
    const response = await fetch(`${API_URL}/signin`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username,
        password
      }),
    });

    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.message || "Authentication failed");
    }
    
    // Store the token and user data in localStorage for later use
    if (data.token) {
      // Store token with the type (Bearer)
      localStorage.setItem("accessToken", data.token);
      localStorage.setItem("tokenType", data.type || "Bearer");
      localStorage.setItem("user", JSON.stringify(data));
      
      // Explicitly store the user ID for easy access
      if (data.id) {
        localStorage.setItem("userId", data.id.toString());
      }
    }
    
    return data;
  } catch (error) {
    console.error("Signin error:", error);
    throw error;
  }
};

/**
 * Sign out the current user
 */
export const signout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("user");
};

/**
 * Get the current user from local storage
 * @returns {Object|null} - The current user or null
 */
export const getCurrentUser = () => {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}; 