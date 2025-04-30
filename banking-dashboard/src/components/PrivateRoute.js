import { Navigate, Outlet } from 'react-router-dom';
import { isAuthenticated, isAdmin } from '../services/apiUtils';

/**
 * PrivateRoute component that redirects unauthenticated users to the login page
 * @param {Object} props - Component props
 * @param {boolean} props.requireAdmin - Whether the route requires admin privileges
 * @param {string} props.redirectTo - Where to redirect if authentication fails (default: /login)
 * @returns {JSX.Element} The protected route component
 */
const PrivateRoute = ({ 
  requireAdmin = false, 
  redirectTo = "/login" 
}) => {
  // Check if the user is authenticated
  const authenticated = isAuthenticated();
  
  // If admin route, check admin status
  if (requireAdmin && !isAdmin()) {
    // Redirect to dashboard if user is authenticated but not admin
    return authenticated ? <Navigate to="/dashboard" /> : <Navigate to={redirectTo} />;
  }
  
  // For regular protected routes, just check authentication
  return authenticated ? <Outlet /> : <Navigate to={redirectTo} />;
};

export default PrivateRoute; 