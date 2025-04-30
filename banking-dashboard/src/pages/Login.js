import { useState } from "react"
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Paper,
    Link,
    IconButton,
    InputAdornment,
    CircularProgress,
    Divider,
    useMediaQuery,
    Alert,
    Snackbar
} from "@mui/material"
import { useTheme } from "@mui/material/styles"
import { Visibility, VisibilityOff, LockOutlined, Person as PersonIcon, ArrowForward } from "@mui/icons-material"
import { Link as RouterLink, useNavigate } from "react-router-dom"
import { signin } from "../services/authService"

const Login = () => {
    const navigate = useNavigate()
    const [formData, setFormData] = useState({
        username: "",
        password: "",
    })
    const [showPassword, setShowPassword] = useState(false)
    const [isLoading, setIsLoading] = useState(false)
    const [errors, setErrors] = useState({})
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: '',
        severity: 'success'
    })
    const theme = useTheme()
    const isMobile = useMediaQuery(theme.breakpoints.down("md"))

    const handleChange = (e) => {
        const { name, value } = e.target
        setFormData({
            ...formData,
            [name]: value,
        })
        // Clear error when user types
        if (errors[name]) {
            setErrors({
                ...errors,
                [name]: "",
            })
        }
    }

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword)
    }

    const validate = () => {
        const newErrors = {}
        if (!formData.username) newErrors.username = "Username is required"
        
        if (!formData.password) newErrors.password = "Password is required"
        else if (formData.password.length < 6) newErrors.password = "Password must be at least 6 characters"

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (validate()) {
            setIsLoading(true)
            try {
                const response = await signin(formData.username, formData.password)
                console.log("Login successful", response)
                setSnackbar({
                    open: true,
                    message: 'Login successful! Redirecting...',
                    severity: 'success'
                })
                // Redirect to dashboard after a short delay
                setTimeout(() => {
                    if (response.roles && response.roles.includes('ROLE_ADMIN')) {
                        navigate('/admin-dashboard')
                    } else {
                        navigate('/dashboard')
                    }
                }, 1500)
            } catch (error) {
                console.error("Login failed", error)
                setSnackbar({
                    open: true,
                    message: error.message || 'Authentication failed. Please check your credentials.',
                    severity: 'error'
                })
            } finally {
                setIsLoading(false)
            }
        }
    }

    return (
        <Container
            component="main"
            maxWidth="lg"
            sx={{
                height: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                p: 0,
            }}
        >
            <Paper
                elevation={5}
                sx={{
                    width: "100%",
                    overflow: "hidden",
                    borderRadius: 3,
                    display: "flex",
                    flexDirection: { xs: "column", md: "row" },
                    height: { xs: "auto", md: "600px" },
                }}
            >
                {/* Left Column - Login Form */}
                <Box
                    sx={{
                        width: { xs: "100%", md: "50%" },
                        p: { xs: 3, sm: 4, md: 5 },
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "center",
                    }}
                >
                    <Box sx={{ mb: 4, display: "flex", justifyContent: "center" }}>
                        <img src="/placeholder.svg" alt="DTB Logo" style={{ height: 50, marginBottom: 16 }} />
                    </Box>

                    <Typography
                        component="h1"
                        variant="h4"
                        color="primary"
                        sx={{
                            mb: 3,
                            fontWeight: "bold",
                            textAlign: "center",
                        }}
                    >
                        Welcome Back
                    </Typography>

                    <Typography
                        variant="body1"
                        sx={{
                            mb: 4,
                            textAlign: "center",
                            color: "text.secondary",
                        }}
                    >
                        Sign in to access your DTB Online Banking account
                    </Typography>

                    <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="username"
                            label="Username"
                            name="username"
                            autoComplete="username"
                            autoFocus
                            value={formData.username}
                            onChange={handleChange}
                            error={!!errors.username}
                            helperText={errors.username}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <PersonIcon color="primary" />
                                    </InputAdornment>
                                ),
                            }}
                            sx={{ mb: 2 }}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="password"
                            label="Password"
                            type={showPassword ? "text" : "password"}
                            id="password"
                            autoComplete="current-password"
                            value={formData.password}
                            onChange={handleChange}
                            error={!!errors.password}
                            helperText={errors.password}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <LockOutlined color="primary" />
                                    </InputAdornment>
                                ),
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton aria-label="toggle password visibility" onClick={togglePasswordVisibility} edge="end">
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                            sx={{ mb: 1 }}
                        />

                        <Box sx={{ mt: 1, mb: 3, textAlign: "right" }}>
                            <Link
                                component={RouterLink}
                                to="/forgot-password"
                                variant="body2"
                                color="primary"
                                sx={{ fontWeight: 500 }}
                            >
                                Forgot password?
                            </Link>
                        </Box>

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            color="primary"
                            size="large"
                            sx={{
                                mt: 1,
                                mb: 3,
                                py: 1.5,
                                fontSize: "1rem",
                            }}
                            disabled={isLoading}
                            endIcon={!isLoading && <ArrowForward />}
                        >
                            {isLoading ? <CircularProgress size={24} color="inherit" /> : "Sign In"}
                        </Button>

                        <Divider sx={{ my: 2 }}>
                            <Typography variant="body2" color="text.secondary">
                                OR
                            </Typography>
                        </Divider>

                        <Box sx={{ textAlign: "center", mt: 2 }}>
                            <Typography variant="body2" sx={{ color: "text.secondary" }}>
                                Don't have an account?{" "}
                                <Link component={RouterLink} to="/signup" color="primary" sx={{ fontWeight: "bold" }}>
                                    Sign Up
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </Box>

                {/* Right Column - Promotional Content */}
                <Box
                    sx={{
                        width: { xs: "100%", md: "50%" },
                        bgcolor: "primary.main",
                        color: "white",
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "center",
                        alignItems: "center",
                        p: { xs: 3, sm: 4, md: 5 },
                        textAlign: "center",
                        position: "relative",
                        overflow: "hidden",
                    }}
                >
                    <Box
                        sx={{
                            position: "absolute",
                            top: 0,
                            left: 0,
                            width: "100%",
                            height: "100%",
                            opacity: 0.1,
                            backgroundImage: 'url("https://placeholder.svg?height=600&width=600")',
                            backgroundSize: "cover",
                            backgroundPosition: "center",
                        }}
                    />

                    <Box
                        sx={{
                            position: "relative",
                            maxWidth: "400px",
                            mx: "auto",
                        }}
                    >
                        <Typography
                            variant="h3"
                            gutterBottom
                            sx={{
                                fontWeight: "bold",
                                mb: 3,
                                fontSize: { xs: "1.75rem", sm: "2.25rem", md: "2.5rem" },
                            }}
                        >
                            Ibrahim Diba
                        </Typography>

                        <Typography
                            variant="body1"
                            sx={{
                                mb: 4,
                                fontSize: "1.1rem",
                                lineHeight: 1.6,
                            }}
                        >
                            Thank you for the opportunity to showcase my skills with this assessment. I'm excited to demonstrate my
                            abilities in creating scalable microservice architectures.
                        </Typography>

                        <Box sx={{ mt: 4, mb: 2, textAlign: "left", bgcolor: "rgba(0,0,0,0.2)", p: 3, borderRadius: 2 }}>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: "bold" }}>
                                Login Credentials:
                            </Typography>

                            <Typography variant="subtitle1" sx={{ fontWeight: "bold", mt: 2 }}>
                                User Role:
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                Username: <strong>testuser</strong>
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 3 }}>
                                Password: <strong>password123</strong>
                            </Typography>

                            <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
                                Admin Role:
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                Username: <strong>testadmin</strong>
                            </Typography>
                            <Typography variant="body2">
                                Password: <strong>password123</strong>
                            </Typography>
                        </Box>
                    </Box>
                </Box>
            </Paper>
            
            <Snackbar 
                open={snackbar.open} 
                autoHideDuration={6000} 
                onClose={() => setSnackbar({...snackbar, open: false})}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert 
                    onClose={() => setSnackbar({...snackbar, open: false})} 
                    severity={snackbar.severity}
                    sx={{ width: '100%' }}
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    )
}

export default Login
