"use client"

import React, { useState, useEffect } from "react"
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  Card,
  CardContent,
  IconButton,
  Tab,
  Tabs,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Divider,
  Avatar,
  Badge,
  Chip,
  useTheme,
  AppBar,
  Toolbar,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  InputAdornment,
  alpha,
  CircularProgress,
  Alert,
  Snackbar,
  Tooltip,
  CardHeader,
  Stack
} from "@mui/material"
import { DataGrid } from "@mui/x-data-grid"
import {
  AccountBalanceWallet,
  CreditCard,
  ArrowUpward,
  ArrowDownward,
  Sync,
  Receipt,
  Notifications,
  Close,
  AttachMoney,
  Money,
  CompareArrows,
  AccountBalance,
  Search,
  Dashboard,
  Payment,
  Settings,
  Logout,
  Menu as MenuIcon,
  ArrowForward,
  CalendarMonth,
  CreditScore,
  SupportAgent,
  Refresh,
  SwapHoriz
} from "@mui/icons-material"
import {
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from "recharts"
import { format, parseISO, subDays } from "date-fns"

// Import account service
import { getUserAccounts } from "../services/accountService"
import { 
  depositToAccount, 
  getUserTransactions,
  withdrawFromAccount,
  transferBetweenAccounts
} from "../services/transactionService"

// Mock data for accounts
const mockAccounts = [
  {
    id: 1001,
    accountNumber: "1234567890",
    type: "CHECKING",
    balance: 2580.45,
    currency: "KES",
    status: "ACTIVE",
    createdAt: "2022-01-15T09:30:00Z",
  },
  {
    id: 1002,
    accountNumber: "9876543210",
    type: "SAVINGS",
    balance: 15750.2,
    currency: "KES",
    status: "ACTIVE",
    createdAt: "2022-03-10T14:20:00Z",
  },
  {
    id: 1003,
    accountNumber: "5678901234",
    type: "FIXED_DEPOSIT",
    balance: 50000.0,
    currency: "KES",
    status: "ACTIVE",
    createdAt: "2022-06-22T11:15:00Z",
  },
]

// Transaction types and status enums based on your Java model
const TransactionType = {
  DEPOSIT: "DEPOSIT",
  WITHDRAWAL: "WITHDRAWAL",
  TRANSFER: "TRANSFER",
  PAYMENT: "PAYMENT",
  REFUND: "REFUND",
}

const TransactionStatus = {
  PENDING: "PENDING",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
}

// Mock data for transactions based on your Java model
const mockTransactions = [
  {
    id: 1,
    transactionReference: "TRX123456789",
    type: TransactionType.DEPOSIT,
    amount: 5000.0,
    sourceAccountId: 1001,
    destinationAccountId: null,
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "Salary deposit",
    createdAt: "2023-04-15T08:30:00Z",
    completedAt: "2023-04-15T08:32:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 2,
    transactionReference: "TRX987654321",
    type: TransactionType.WITHDRAWAL,
    amount: 1000.0,
    sourceAccountId: 1001,
    destinationAccountId: null,
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "ATM withdrawal",
    createdAt: "2023-04-16T14:20:00Z",
    completedAt: "2023-04-16T14:22:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 3,
    transactionReference: "TRX456789123",
    type: TransactionType.TRANSFER,
    amount: 2500.0,
    sourceAccountId: 1001,
    destinationAccountId: 1002,
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "Transfer to savings",
    createdAt: "2023-04-18T10:15:00Z",
    completedAt: "2023-04-18T10:17:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 4,
    transactionReference: "TRX789123456",
    type: TransactionType.PAYMENT,
    amount: 1200.0,
    sourceAccountId: 1001,
    destinationAccountId: 2001, // Merchant account
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "Electricity bill payment",
    createdAt: "2023-04-20T09:45:00Z",
    completedAt: "2023-04-20T09:47:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 5,
    transactionReference: "TRX321654987",
    type: TransactionType.TRANSFER,
    amount: 3000.0,
    sourceAccountId: 1001,
    destinationAccountId: 3001, // Another user
    status: TransactionStatus.FAILED,
    currency: "KES",
    description: "Transfer to John",
    createdAt: "2023-04-22T16:30:00Z",
    completedAt: null,
    userId: 101,
    failureReason: "Insufficient funds",
    metadata: null,
  },
  {
    id: 6,
    transactionReference: "TRX654987321",
    type: TransactionType.DEPOSIT,
    amount: 10000.0,
    sourceAccountId: null,
    destinationAccountId: 1002,
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "Bonus deposit",
    createdAt: "2023-04-25T11:20:00Z",
    completedAt: "2023-04-25T11:22:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 7,
    transactionReference: "TRX741852963",
    type: TransactionType.WITHDRAWAL,
    amount: 5000.0,
    sourceAccountId: 1002,
    destinationAccountId: null,
    status: TransactionStatus.PENDING,
    currency: "KES",
    description: "Large withdrawal request",
    createdAt: "2023-04-28T13:10:00Z",
    completedAt: null,
    userId: 101,
    failureReason: null,
    metadata: null,
  },
  {
    id: 8,
    transactionReference: "TRX963852741",
    type: TransactionType.REFUND,
    amount: 500.0,
    sourceAccountId: 2002, // Merchant
    destinationAccountId: 1001,
    status: TransactionStatus.COMPLETED,
    currency: "KES",
    description: "Refund for returned item",
    createdAt: "2023-04-30T15:40:00Z",
    completedAt: "2023-04-30T15:42:00Z",
    userId: 101,
    failureReason: null,
    metadata: null,
  },
]

// Mock spending categories data for chart
const spendingData = [
  { name: "Bills", value: 3500 },
  { name: "Shopping", value: 2200 },
  { name: "Food", value: 1800 },
  { name: "Transport", value: 1200 },
  { name: "Entertainment", value: 800 },
]

// Mock data for balance history chart
const generateBalanceHistory = () => {
  const data = []
  let balance = 15000

  for (let i = 30; i >= 0; i--) {
    const date = subDays(new Date(), i)
    const variation = Math.random() * 1000 - 500 // Random variation between -500 and 500
    balance += variation

    data.push({
      date: format(date, "MMM dd"),
      balance: Math.max(balance, 0).toFixed(2),
    })
  }

  return data
}

const balanceHistory = generateBalanceHistory()

// Transaction columns for the data grid
const transactionColumns = [
  { 
    field: 'createdAt', 
    headerName: 'Date', 
    width: 180,
    valueFormatter: (params) => {
      const date = new Date(params.value);
      return date.toLocaleString();
    },
  },
  { 
    field: 'type', 
    headerName: 'Type', 
    width: 150,
    renderCell: (params) => {
      let color = '';
      let icon = null;
      
      switch (params.value) {
        case 'DEPOSIT':
          color = 'success.main';
          icon = <ArrowUpward fontSize="small" />;
          break;
        case 'WITHDRAWAL':
          color = 'error.main';
          icon = <ArrowDownward fontSize="small" />;
          break;
        case 'TRANSFER':
          color = 'info.main';
          icon = <SwapHoriz fontSize="small" />;
          break;
        default:
          color = 'text.primary';
      }
      
      return (
        <Box sx={{ display: 'flex', alignItems: 'center', color }}>
          {icon}
          <Typography variant="body2" sx={{ ml: 1 }}>
            {params.value.charAt(0) + params.value.slice(1).toLowerCase()}
          </Typography>
        </Box>
      );
    }
  },
  { 
    field: 'amount', 
    headerName: 'Amount', 
    width: 150,
    valueFormatter: (params) => {
      return `$${parseFloat(params.value).toFixed(2)}`;
    },
    renderCell: (params) => {
      const isCredit = params.row.type === 'DEPOSIT' || 
        (params.row.type === 'TRANSFER' && params.row.recipientAccountId === params.row.accountId);
      
      return (
        <Typography
          variant="body2"
          sx={{ color: isCredit ? 'success.main' : 'error.main' }}
        >
          {isCredit ? '+' : '-'}${parseFloat(params.value).toFixed(2)}
        </Typography>
      );
    }
  },
  { 
    field: 'description', 
    headerName: 'Description', 
    width: 300,
    flex: 1
  },
  { 
    field: 'status', 
    headerName: 'Status', 
    width: 150,
    renderCell: (params) => {
      let color;
      
      switch (params.value) {
        case 'COMPLETED':
          color = theme => theme.palette.success.main;
          break;
        case 'PENDING':
          color = theme => theme.palette.warning.main;
          break;
        case 'FAILED':
          color = theme => theme.palette.error.main;
          break;
        default:
          color = theme => theme.palette.text.primary;
      }
      
      return (
        <Chip
          label={params.value.charAt(0) + params.value.slice(1).toLowerCase()}
          size="small"
          sx={{ 
            bgcolor: theme => alpha(color(theme), 0.1),
            color,
            borderRadius: '4px'
          }}
        />
      );
    }
  },
  { 
    field: 'transactionReference', 
    headerName: 'Reference', 
    width: 220,
    renderCell: (params) => (
      <Tooltip title={params.value}>
        <Typography variant="body2" sx={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {params.value}
        </Typography>
      </Tooltip>
    )
  }
];

// Transaction operation tabs
function TransactionOperations({ onOperationSelect }) {
  const [value, setValue] = useState(0)
  const theme = useTheme()

  const handleChange = (event, newValue) => {
    setValue(newValue)
    onOperationSelect(newValue)
  }

  return (
      <Box sx={{ width: "100%" }}>
        <Tabs
            value={value}
            onChange={handleChange}
            aria-label="transaction operations"
            variant="fullWidth"
            indicatorColor="primary"
            sx={{
              "& .MuiTab-root": {
                minHeight: "72px",
                transition: "all 0.2s ease",
                "&:hover": {
                  backgroundColor: alpha(theme.palette.primary.main, 0.05),
                },
              },
            }}
        >
          <Tab
              icon={
                <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                  <Box
                      sx={{
                        backgroundColor: theme.palette.success.light,
                        borderRadius: "50%",
                        p: 1,
                        mb: 1,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                  >
                    <AttachMoney sx={{ color: theme.palette.success.main }} />
                  </Box>
                  <Typography variant="body2">Deposit</Typography>
                </Box>
              }
              sx={{ "&.Mui-selected": { color: theme.palette.success.main } }}
          />
          <Tab
              icon={
                <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                  <Box
                      sx={{
                        backgroundColor: theme.palette.error.light,
                        borderRadius: "50%",
                        p: 1,
                        mb: 1,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                  >
                    <Money sx={{ color: theme.palette.error.main }} />
                  </Box>
                  <Typography variant="body2">Withdraw</Typography>
                </Box>
              }
              sx={{ "&.Mui-selected": { color: theme.palette.error.main } }}
          />
          <Tab
              icon={
                <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                  <Box
                      sx={{
                        backgroundColor: theme.palette.primary.light,
                        borderRadius: "50%",
                        p: 1,
                        mb: 1,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                  >
                    <CompareArrows sx={{ color: theme.palette.primary.main }} />
                  </Box>
                  <Typography variant="body2">Transfer</Typography>
                </Box>
              }
              sx={{ "&.Mui-selected": { color: theme.palette.primary.main } }}
          />
          <Tab
              icon={
                <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                  <Box
                      sx={{
                        backgroundColor: theme.palette.warning.light,
                        borderRadius: "50%",
                        p: 1,
                        mb: 1,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                  >
                    <Payment sx={{ color: theme.palette.warning.main }} />
                  </Box>
                  <Typography variant="body2">Pay Bills</Typography>
                </Box>
              }
              sx={{ "&.Mui-selected": { color: theme.palette.warning.main } }}
          />
        </Tabs>
      </Box>
  )
}

// User Dashboard Component
const UserDashboard = () => {
  const theme = useTheme()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [transactionDialogOpen, setTransactionDialogOpen] = useState(false)
  const [transactionType, setTransactionType] = useState("")
  const [selectedAccount, setSelectedAccount] = useState(null)
  const [transactionData, setTransactionData] = useState({
    amount: "",
    accountId: "",
    description: "",
  })
  
  // New states for user accounts
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  })

  const [isProcessing, setIsProcessing] = useState(false);
  const [transactionResult, setTransactionResult] = useState(null);

  // Add state for transactions
  const [transactions, setTransactions] = useState([]);
  const [loadingTransactions, setLoadingTransactions] = useState(true);
  const [transactionError, setTransactionError] = useState(null);

  const [searchTerm, setSearchTerm] = useState("");
  
  // Add state for user
  const [user, setUser] = useState(null);
  
  // Get user from localStorage when component mounts
  useEffect(() => {
    const userId = localStorage.getItem("userId");
    if (userId) {
      setUser({ userId });
    }
  }, []);

  // Transaction fetching function
  const fetchTransactions = async () => {
    if (!user?.userId) {
      setTransactionError("User not authenticated. Please login again.");
      return;
    }
    
    setLoadingTransactions(true);
    setTransactionError(null);
    
    try {
      const data = await getUserTransactions(user.userId);
      
      // Sort transactions by date (newest first)
      const sortedTransactions = data.sort((a, b) => {
        return new Date(b.createdAt) - new Date(a.createdAt);
      });
      
      setTransactions(sortedTransactions);
    } catch (error) {
      console.error("Error fetching transactions:", error);
      setTransactionError("Failed to load transactions. Please try again.");
    } finally {
      setLoadingTransactions(false);
    }
  };

  // Function to refresh transactions
  const refreshTransactions = () => {
    fetchTransactions();
  };
  
  // Fetch user accounts function - moved outside useEffect
  const fetchUserAccounts = async () => {
    try {
      const userId = localStorage.getItem("userId")
      
      if (!userId) {
        throw new Error("User ID not found. Please login again.")
      }
      
      const userAccounts = await getUserAccounts(userId)
      setAccounts(userAccounts)
      
      // Set the first account as selected if available
      if (userAccounts.length > 0) {
        setSelectedAccount(userAccounts[0])
      }
      
      setSnackbar({
        open: true,
        message: `Successfully loaded ${userAccounts.length} account(s)`,
        severity: 'success'
      })
    } catch (error) {
      console.error("Failed to fetch user accounts:", error)
      setError(error.message || "Failed to load accounts")
      setSnackbar({
        open: true,
        message: error.message || "Failed to load accounts",
        severity: 'error'
      })
    } finally {
      setLoading(false)
    }
  }

  // Fetch data when user is available
  useEffect(() => {
    if (user) {
      fetchUserAccounts();
      fetchTransactions();
    }
  }, [user]);

  useEffect(() => {
    // Fetch user accounts when component mounts
    fetchUserAccounts()
  }, [])

  // Colors for charts
  const COLORS = [theme.palette.primary.main, theme.palette.secondary.main, "#2196f3", "#4caf50", "#ff9800"]

  const totalBalance = accounts.reduce((sum, account) => sum + account.balance, 0)

  const handleOpenTransactionDialog = (type) => {
    setTransactionType(type)
    setTransactionDialogOpen(true)
  }

  const handleCloseTransactionDialog = () => {
    setTransactionDialogOpen(false)
  }

  const handleTransactionOperation = (tabIndex) => {
    const types = ["deposit", "withdraw", "transfer", "payment"]
    handleOpenTransactionDialog(types[tabIndex])
  }

  const handleAccountSelect = (account) => {
    setSelectedAccount(account)
    setTransactionData({
      ...transactionData,
      amount: "",
      accountId: account.id,
      description: "",
    })
  }

  const handleTransactionChange = (e) => {
    const { name, value } = e.target
    setTransactionData({
      ...transactionData,
      [name]: value,
    })
  }

  const handleSubmitTransaction = async () => {
    setIsProcessing(true);
    try {
      let response;
      
      if (transactionType === "deposit") {
        // For deposit, we need accountId, amount, and optional description
        response = await depositToAccount(
          selectedAccount.accountNumber, 
          parseFloat(transactionData.amount), 
          transactionData.description
        );
      } else if (transactionType === "withdraw") {
        // For withdrawal
        response = await withdrawFromAccount(
          selectedAccount.accountNumber,
          parseFloat(transactionData.amount),
          transactionData.description
        );
      } else if (transactionType === "transfer") {
        // For transfer, hardcode the destination account as requested
        // In a real app, this would be selected by the user
        const destinationAccountId = "12345672"; // Hardcoded destination account
        
        response = await transferBetweenAccounts(
          selectedAccount.accountNumber,
          destinationAccountId,
          parseFloat(transactionData.amount),
          transactionData.description
        );
      } else {
        // Handle other transaction types (not implemented yet)
        throw new Error("Transaction type not implemented yet");
      }
      
      console.log("Transaction response:", response);
      setTransactionResult(response);
      
      // Show success message
      setSnackbar({
        open: true,
        message: `${transactionType.charAt(0).toUpperCase() + transactionType.slice(1)} initiated successfully! Reference: ${response.transactionReference}`,
        severity: 'success'
      });
      
      // Refresh accounts and transactions after a short delay
      setTimeout(() => {
        refreshAccounts();
        refreshTransactions();
      }, 2000);
      
    } catch (error) {
      console.error("Transaction failed:", error);
      setSnackbar({
        open: true,
        message: error.message || "Transaction failed",
        severity: 'error'
      });
    } finally {
      setIsProcessing(false);
    }

    // Close dialog
    handleCloseTransactionDialog();

    // Reset form
    setTransactionData({
      amount: "",
      accountId: "",
      description: "",
    });
  };
  
  // Function to refresh accounts
  const refreshAccounts = async () => {
    try {
      const userId = localStorage.getItem("userId");
      
      if (!userId) {
        throw new Error("User ID not found. Please login again.");
      }
      
      const userAccounts = await getUserAccounts(userId);
      setAccounts(userAccounts);
      
      // Keep the same selected account if it exists in the refreshed list
      if (selectedAccount) {
        const updatedSelectedAccount = userAccounts.find(
          account => account.accountNumber === selectedAccount.accountNumber
        );
        if (updatedSelectedAccount) {
          setSelectedAccount(updatedSelectedAccount);
        } else if (userAccounts.length > 0) {
          setSelectedAccount(userAccounts[0]);
        }
      }
    } catch (error) {
      console.error("Failed to refresh accounts:", error);
    }
  };

  // Sidebar menu items
  const menuItems = [
    { text: "Dashboard", icon: <Dashboard /> },
    { text: "Accounts", icon: <AccountBalance /> },
    { text: "Transactions", icon: <Receipt /> },
    { text: "Payments", icon: <Payment /> },
    { text: "Cards", icon: <CreditCard /> },
    { text: "Loans", icon: <CreditScore /> },
    { text: "Scheduled", icon: <CalendarMonth /> },
    { text: "Support", icon: <SupportAgent /> },
    { text: "Settings", icon: <Settings /> },
  ]

  // Render account card - redesigned to be more compact for horizontal layout
  const renderAccountCard = (account) => {
    // Format balance to 2 decimal places
    const formattedBalance = parseFloat(account.balance).toFixed(2)
    
    // Format date to a more readable format
    const createdDate = new Date(account.createdAt).toLocaleDateString()
    
    return (
      <Card
        key={account.id}
        sx={{
          mb: 2,
          borderRadius: 2,
          boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
          transition: "transform 0.3s ease",
          cursor: "pointer",
          "&:hover": {
            transform: "translateY(-5px)",
            boxShadow: "0 6px 15px rgba(0,0,0,0.2)",
          },
          border: selectedAccount?.id === account.id ? `2px solid ${theme.palette.primary.main}` : "none",
        }}
        onClick={() => handleAccountSelect(account)}
      >
        <CardContent>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Avatar
                sx={{
                  bgcolor:
                    account.accountType === "SAVINGS"
                      ? theme.palette.primary.main
                      : account.accountType === "CHECKING"
                      ? theme.palette.secondary.main
                      : theme.palette.success.main,
                  mr: 2,
                }}
              >
                {account.accountType === "SAVINGS" ? (
                  <AccountBalanceWallet />
                ) : account.accountType === "CHECKING" ? (
                  <CreditCard />
                ) : (
                  <AccountBalance />
                )}
              </Avatar>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  {account.accountType === "SAVINGS"
                    ? "Savings Account"
                    : account.accountType === "CHECKING"
                    ? "Checking Account"
                    : "Fixed Deposit"}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {account.accountNumber}
                </Typography>
              </Box>
            </Box>
            <Chip
              label={account.status}
              color={account.status === "ACTIVE" ? "success" : "error"}
              size="small"
            />
          </Box>
          <Box sx={{ mt: 3 }}>
            <Typography variant="body2" color="text.secondary">
              Balance
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              KES {formattedBalance}
            </Typography>
          </Box>
          <Box sx={{ mt: 2, display: "flex", justifyContent: "space-between" }}>
            <Typography variant="caption" color="text.secondary">
              Created on {createdDate}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    )
  }

  // Enhanced quick action tabs
  const EnhancedTransactionOperations = ({ onOperationSelect }) => {
    const [value, setValue] = useState(0)
    const theme = useTheme()
    
    const handleChange = (event, newValue) => {
      setValue(newValue)
      onOperationSelect(newValue)
    }
  
    // Direct click handlers for each action button
    const handleDepositClick = () => {
      setValue(0);
      onOperationSelect(0);
    }
    
    const handleWithdrawClick = () => {
      setValue(1);
      onOperationSelect(1);
    }
    
    const handleTransferClick = () => {
      setValue(2);
      onOperationSelect(2);
    }
    
    const handlePayBillsClick = () => {
      setValue(3);
      onOperationSelect(3);
    }
  
    return (
      <Box sx={{ display: 'flex', justifyContent: 'space-around', p: 2 }}>
        <Box 
          onClick={handleDepositClick}
          sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            cursor: 'pointer',
            p: 1,
            borderRadius: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: alpha(theme.palette.success.main, 0.1),
              transform: 'translateY(-4px)',
            }
          }}
        >
          <Box
            sx={{
              backgroundColor: theme.palette.success.light,
              borderRadius: '50%',
              p: 1.5,
              mb: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <AttachMoney sx={{ color: theme.palette.success.main, fontSize: '2rem' }} />
          </Box>
          <Typography variant="body1" fontWeight="medium" sx={{ color: theme.palette.success.main }}>Deposit</Typography>
        </Box>
        
        <Box 
          onClick={handleWithdrawClick}
          sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            cursor: 'pointer',
            p: 1,
            borderRadius: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: alpha(theme.palette.error.main, 0.1),
              transform: 'translateY(-4px)',
            }
          }}
        >
          <Box
            sx={{
              backgroundColor: theme.palette.error.light,
              borderRadius: '50%',
              p: 1.5,
              mb: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Money sx={{ color: theme.palette.error.main, fontSize: '2rem' }} />
          </Box>
          <Typography variant="body1" fontWeight="medium" sx={{ color: theme.palette.error.main }}>Withdraw</Typography>
        </Box>
        
        <Box 
          onClick={handleTransferClick}
          sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            cursor: 'pointer',
            p: 1,
            borderRadius: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.1),
              transform: 'translateY(-4px)',
            }
          }}
        >
          <Box
            sx={{
              backgroundColor: theme.palette.primary.light,
              borderRadius: '50%',
              p: 1.5,
              mb: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <CompareArrows sx={{ color: theme.palette.primary.main, fontSize: '2rem' }} />
          </Box>
          <Typography variant="body1" fontWeight="medium" sx={{ color: theme.palette.primary.main }}>Transfer</Typography>
        </Box>
        
        <Box 
          onClick={handlePayBillsClick}
          sx={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            cursor: 'pointer',
            p: 1,
            borderRadius: 2,
            transition: 'all 0.2s',
            '&:hover': {
              backgroundColor: alpha(theme.palette.warning.main, 0.1),
              transform: 'translateY(-4px)',
            }
          }}
        >
          <Box
            sx={{
              backgroundColor: theme.palette.warning.light,
              borderRadius: '50%',
              p: 1.5,
              mb: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Payment sx={{ color: theme.palette.warning.main, fontSize: '2rem' }} />
          </Box>
          <Typography variant="body1" fontWeight="medium" sx={{ color: theme.palette.warning.main }}>Pay Bills</Typography>
        </Box>
      </Box>
    )
  }

  // Render transaction dialog
  const renderTransactionDialog = () => {
    const dialogTitle =
        transactionType === "deposit"
            ? "Make a Deposit"
            : transactionType === "withdraw"
                ? "Make a Withdrawal"
                : transactionType === "transfer"
                    ? "Make a Transfer"
                    : "Pay Bills"

    const dialogIcon =
        transactionType === "deposit" ? (
            <AttachMoney />
        ) : transactionType === "withdraw" ? (
            <Money />
        ) : transactionType === "transfer" ? (
            <CompareArrows />
        ) : (
            <Payment />
        )

    const dialogColor =
        transactionType === "deposit"
            ? "success.main"
            : transactionType === "withdraw"
                ? "error.main"
                : transactionType === "transfer"
                    ? "primary.main"
                    : "warning.main"

    return (
        <Dialog
            open={transactionDialogOpen}
            onClose={handleCloseTransactionDialog}
            maxWidth="sm"
            fullWidth
            PaperProps={{
              sx: {
                borderRadius: 3,
                overflow: "hidden",
              },
            }}
        >
          <DialogTitle
              sx={{
                bgcolor: "primary.main",
                color: "white",
                position: "relative",
                p: 3,
                display: "flex",
                alignItems: "center",
                gap: 1.5,
              }}
          >
            <Box
                sx={{
                  backgroundColor: "white",
                  borderRadius: "50%",
                  p: 1,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
            >
              {React.cloneElement(dialogIcon, { sx: { color: dialogColor } })}
            </Box>
            <Typography variant="h6">{dialogTitle}</Typography>
            <IconButton
                aria-label="close"
                onClick={handleCloseTransactionDialog}
                sx={{
                  position: "absolute",
                  right: 16,
                  top: 16,
                  color: "white",
                }}
            >
              <Close />
            </IconButton>
          </DialogTitle>

          <DialogContent sx={{ p: 3, mt: 1 }}>
            {/* Show transaction result if available */}
            {transactionResult && (
              <Box sx={{ mb: 3 }}>
                <Alert severity="success" sx={{ mb: 2 }}>
                  {transactionResult.message}
                </Alert>
                <Typography variant="subtitle2">
                  Transaction Reference: {transactionResult.transactionReference}
                </Typography>
                <Typography variant="subtitle2">
                  Status: <Chip size="small" label={transactionResult.status} color="warning" />
                </Typography>
              </Box>
            )}
          
            <Grid container spacing={3}>
              {/* Account Selection */}
              {(transactionType === "deposit" || transactionType === "withdraw") && (
                <Grid item xs={12}>
                  <FormControl fullWidth>
                    <InputLabel>Select Account</InputLabel>
                    <Select
                      name="accountId"
                      value={selectedAccount ? selectedAccount.id : ""}
                      onChange={(e) => {
                        const account = accounts.find(acc => acc.id === e.target.value);
                        if (account) {
                          setSelectedAccount(account);
                          setTransactionData({
                            ...transactionData,
                            accountId: account.id
                          });
                        }
                      }}
                    >
                      {accounts
                        .filter(account => account.status === "ACTIVE")
                        .map(account => (
                          <MenuItem key={account.id} value={account.id}>
                            {account.accountType === "CHECKING" 
                              ? "Checking Account" 
                              : account.accountType === "SAVINGS" 
                                ? "Savings Account" 
                                : "Fixed Deposit"}{" "}
                            - {account.accountNumber} 
                            (KES {parseFloat(account.balance).toFixed(2)})
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                </Grid>
              )}
              
              {/* Source Account for Transfer */}
              {transactionType === "transfer" && (
                <Grid item xs={12}>
                  <FormControl fullWidth>
                    <InputLabel>From Account</InputLabel>
                    <Select
                      name="accountId"
                      value={selectedAccount ? selectedAccount.id : ""}
                      onChange={(e) => {
                        const account = accounts.find(acc => acc.id === e.target.value);
                        if (account) {
                          setSelectedAccount(account);
                          setTransactionData({
                            ...transactionData,
                            accountId: account.id
                          });
                        }
                      }}
                    >
                      {accounts
                        .filter(account => account.status === "ACTIVE")
                        .map(account => (
                          <MenuItem key={account.id} value={account.id}>
                            {account.accountType === "CHECKING" 
                              ? "Checking Account" 
                              : account.accountType === "SAVINGS" 
                                ? "Savings Account" 
                                : "Fixed Deposit"}{" "}
                            - {account.accountNumber} 
                            (KES {parseFloat(account.balance).toFixed(2)})
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                </Grid>
              )}
            
              <Grid item xs={12}>
                <TextField
                    label="Amount"
                    name="amount"
                    type="number"
                    fullWidth
                    value={transactionData.amount}
                    onChange={handleTransactionChange}
                    InputProps={{
                      startAdornment: <InputAdornment position="start">KES</InputAdornment>,
                    }}
                />
              </Grid>

              {/* Destination Account for Transfer (disabled and set to hardcoded account) */}
              {transactionType === "transfer" && (
                <Grid item xs={12}>
                  <TextField
                    label="To Account"
                    fullWidth
                    value="12345672"
                    disabled
                    InputProps={{
                      readOnly: true,
                    }}
                  />
                </Grid>
              )}

              <Grid item xs={12}>
                <TextField
                    label="Description"
                    name="description"
                    fullWidth
                    multiline
                    rows={2}
                    value={transactionData.description}
                    onChange={handleTransactionChange}
                    placeholder={
                      transactionType === "deposit" 
                        ? "Purpose of deposit" 
                        : transactionType === "withdraw"
                          ? "Purpose of withdrawal"
                          : transactionType === "transfer"
                            ? "Purpose of transfer"
                            : "Transaction description"
                    }
                />
              </Grid>
            </Grid>
          </DialogContent>

          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button
                onClick={handleCloseTransactionDialog}
                variant="outlined"
                color="inherit"
                sx={{ borderRadius: 28, px: 3 }}
            >
              Cancel
            </Button>
            <Button
                onClick={handleSubmitTransaction}
                variant="contained"
                color="primary"
                disabled={isProcessing || !transactionData.amount || !selectedAccount}
                endIcon={isProcessing ? <CircularProgress size={24} color="inherit" /> : <ArrowForward />}
                sx={{ borderRadius: 28, px: 3 }}
            >
              {isProcessing ? "Processing..." : "Confirm"}
            </Button>
          </DialogActions>
        </Dialog>
    )
  }

  const filteredTransactions = transactions.filter(
    (transaction) =>
      transaction.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Transaction history card
  const renderTransactionHistoryCard = () => {
    return (
      <Card sx={{ mb: 4, p: 0 }}>
        <CardHeader
          title="Transaction History"
          action={
            <Button 
              startIcon={<Refresh />} 
              onClick={refreshTransactions}
              disabled={loadingTransactions}
            >
              Refresh
            </Button>
          }
        />
        <Divider />
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ p: 2 }}>
            <TextField
              fullWidth
              size="small"
              label="Search transactions"
              variant="outlined"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
              }}
            />
          </Box>
          <Box sx={{ height: 400, width: '100%' }}>
            {loadingTransactions ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <CircularProgress />
              </Box>
            ) : transactionError ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%', color: 'error.main' }}>
                <Typography>{transactionError}</Typography>
              </Box>
            ) : (
              <DataGrid
                rows={transactions.filter(transaction => 
                  transaction.transactionReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  transaction.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                  transaction.type?.toLowerCase().includes(searchTerm.toLowerCase())
                )}
                columns={transactionColumns}
                pageSize={5}
                rowsPerPageOptions={[5, 10, 20]}
                disableSelectionOnClick
                getRowId={(row) => row.transactionReference}
                sx={{
                  border: 'none',
                  '& .MuiDataGrid-cell:focus': {
                    outline: 'none',
                  },
                }}
              />
            )}
          </Box>
        </CardContent>
      </Card>
    );
  };

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: alpha(theme.palette.primary.main, 0.02) }}>
      {/* Sidebar */}
      <Drawer
          variant="permanent"
          sx={{
            width: 240,
            flexShrink: 0,
            display: { xs: "none", md: "block" },
            "& .MuiDrawer-paper": {
              width: 240,
              boxSizing: "border-box",
              borderRight: "1px solid",
              borderColor: "divider",
              bgcolor: "white",
            },
          }}
      >
        <Toolbar
            sx={{
              py: 2,
              display: "flex",
              justifyContent: "center",
              borderBottom: "1px solid",
              borderColor: "divider",
            }}
        >
          <Typography
              variant="h6"
              component="div"
              color="primary"
              sx={{ fontWeight: "bold", display: "flex", alignItems: "center" }}
          >
            <AccountBalanceWallet sx={{ mr: 1 }} /> DTB Banking
          </Typography>
        </Toolbar>
        <Box sx={{ overflow: "auto", py: 2 }}>
          <List>
            {menuItems.map((item, index) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                      sx={{
                        py: 1.5,
                        px: 2,
                        borderRadius: 1,
                        mx: 1,
                        mb: 0.5,
                        "&.Mui-selected": {
                          bgcolor: "primary.light",
                          "&:hover": {
                            bgcolor: "primary.light",
                          },
                        },
                        "&:hover": {
                          bgcolor: alpha(theme.palette.primary.main, 0.05),
                        },
                      }}
                      selected={index === 0}
                  >
                    <ListItemIcon sx={{ minWidth: 40, color: index === 0 ? "primary.main" : "inherit" }}>
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText
                        primary={item.text}
                        primaryTypographyProps={{
                          fontWeight: index === 0 ? "bold" : "regular",
                          color: index === 0 ? "primary.main" : "inherit",
                        }}
                    />
                  </ListItemButton>
                </ListItem>
            ))}
          </List>
          <Divider sx={{ my: 2 }} />
          <List>
            <ListItem disablePadding>
              <ListItemButton
                  sx={{
                    py: 1.5,
                    px: 2,
                    borderRadius: 1,
                    mx: 1,
                    mb: 0.5,
                    "&:hover": {
                      bgcolor: alpha(theme.palette.error.main, 0.05),
                    },
                  }}
              >
                <ListItemIcon sx={{ minWidth: 40, color: "error.main" }}>
                  <Logout />
                </ListItemIcon>
                <ListItemText
                    primary="Logout"
                    primaryTypographyProps={{
                      color: "error.main",
                    }}
                />
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>

      {/* Mobile drawer */}
      <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={setMobileOpen}
          sx={{
            display: { xs: "block", md: "none" },
            "& .MuiDrawer-paper": {
              width: 240,
              boxSizing: "border-box",
            },
          }}
      >
        <Toolbar sx={{ py: 2, display: "flex", justifyContent: "center" }}>
          <Typography variant="h6" component="div" color="primary" sx={{ fontWeight: "bold" }}>
            <AccountBalanceWallet sx={{ mr: 1 }} /> DTB Banking
          </Typography>
        </Toolbar>
        <Box sx={{ overflow: "auto", py: 2 }}>
          <List>
            {menuItems.map((item, index) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                      sx={{
                        py: 1.5,
                        borderRadius: 1,
                        mx: 1,
                        mb: 0.5,
                        "&.Mui-selected": {
                          bgcolor: "primary.light",
                        },
                      }}
                      selected={index === 0}
                  >
                    <ListItemIcon sx={{ minWidth: 40, color: index === 0 ? "primary.main" : "inherit" }}>
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText
                        primary={item.text}
                        primaryTypographyProps={{
                          fontWeight: index === 0 ? "bold" : "regular",
                          color: index === 0 ? "primary.main" : "inherit",
                        }}
                    />
                  </ListItemButton>
                </ListItem>
            ))}
          </List>
          <Divider sx={{ my: 2 }} />
          <List>
            <ListItem disablePadding>
              <ListItemButton sx={{ py: 1.5 }}>
                <ListItemIcon sx={{ minWidth: 40, color: "error.main" }}>
                  <Logout />
                </ListItemIcon>
                <ListItemText primary="Logout" primaryTypographyProps={{ color: "error.main" }} />
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>

      {/* Main content */}
      <Box component="main" sx={{ flexGrow: 1, overflow: "hidden" }}>
        {/* App Bar */}
        <AppBar
            position="static"
            color="default"
            elevation={0}
            sx={{
              borderBottom: "1px solid",
              borderColor: "divider",
              bgcolor: "white",
            }}
        >
          <Toolbar>
            <IconButton
                edge="start"
                color="inherit"
                aria-label="menu"
                onClick={setMobileOpen}
                sx={{ mr: 2, display: { md: "none" } }}
            >
              <MenuIcon />
            </IconButton>
            <Box sx={{ flexGrow: 1 }}>
              <TextField
                  placeholder="Search..."
                  size="small"
                  InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                          <Search fontSize="small" sx={{ color: "text.secondary" }} />
                        </InputAdornment>
                    ),
                    sx: {
                      borderRadius: 28,
                      bgcolor: alpha(theme.palette.common.black, 0.04),
                      "&:hover": {
                        bgcolor: alpha(theme.palette.common.black, 0.06),
                      },
                      "& fieldset": { border: "none" },
                    },
                  }}
                  sx={{ width: { xs: 150, sm: 250 } }}
              />
            </Box>
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <IconButton sx={{ mr: 2 }}>
                <Badge badgeContent={3} color="error">
                  <Notifications />
                </Badge>
              </IconButton>
              <Box sx={{ display: "flex", alignItems: "center" }}>
                <Avatar
                    alt="Ibrahim Diba"
                    src="/static/avatar.jpg"
                    sx={{
                      width: 40,
                      height: 40,
                      border: "2px solid",
                      borderColor: "primary.main",
                    }}
                />
                <Box sx={{ ml: 1.5, display: { xs: "none", sm: "block" } }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: "bold" }}>
                    Ibrahim Diba
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Premium Customer
                  </Typography>
                </Box>
              </Box>
            </Box>
          </Toolbar>
        </AppBar>

        {/* Dashboard Content - REDESIGNED */}
        <Container maxWidth="xl" sx={{ py: 2 }}>
          {/* Welcome Banner with Total Balance */}
          <Paper
            elevation={0}
            sx={{
              p: 2,
              mb: 2,
              bgcolor: "primary.main",
              color: "white",
              borderRadius: 2,
              backgroundImage: `linear-gradient(to right, ${theme.palette.primary.main}, ${theme.palette.primary.dark})`,
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box>
                <Typography variant="h5" fontWeight="bold">Welcome Back, Ibrahim</Typography>
                <Box
                  sx={{
                    display: "inline-flex",
                    alignItems: "center",
                    mt: 1,
                    bgcolor: alpha(theme.palette.common.white, 0.2),
                    px: 2,
                    py: 0.5,
                    borderRadius: 28,
                  }}
                >
                  <Typography variant="subtitle2" sx={{ mr: 1 }}>Total Balance:</Typography>
                  <Typography variant="subtitle1" fontWeight="bold">
                    KES {totalBalance.toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </Typography>
                </Box>
              </Box>
            </Box>
          </Paper>
          
          {/* Accounts section */}
          <Typography variant="h4" sx={{ mb: 4, fontWeight: 700, color: theme.palette.text.primary }}>
            My Accounts
          </Typography>
          
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : error ? (
            <Alert severity="error" sx={{ mb: 4 }}>
              {error}
            </Alert>
          ) : accounts.length === 0 ? (
            <Alert severity="info" sx={{ mb: 4 }}>
              You don't have any accounts yet. Please contact bank support to open an account.
            </Alert>
          ) : (
            <Grid container spacing={3}>
              {/* Accounts cards */}
              <Grid item xs={12} md={8}>
                {accounts.map((account) => renderAccountCard(account))}
              </Grid>
              
              {/* Transaction operations */}
              <Grid item xs={12} md={4}>
                <Paper
                  elevation={0}
                  sx={{
                    mb: 2,
                    p: 0,
                    borderRadius: 2,
                    border: "1px solid",
                    borderColor: "divider",
                  }}
                >
                  <Typography variant="subtitle1" sx={{ px: 2, pt: 2, pb: 1, fontWeight: "bold" }}>
                    Quick Actions
                  </Typography>
                  <EnhancedTransactionOperations onOperationSelect={handleTransactionOperation} />
                </Paper>
              </Grid>
            </Grid>
          )}
          
          {/* Transactions Section - Updated to use real data */}
          {renderTransactionHistoryCard()}
        </Container>
      </Box>

      {/* Transaction Dialog */}
      {renderTransactionDialog()}

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
    </Box>
  )
}

export default UserDashboard
