import React, { useState } from 'react';
import { 
  Container, 
  Grid, 
  Paper, 
  Typography, 
  Box, 
  Button, 
  IconButton, 
  Tabs, 
  Tab, 
  Avatar, 
  Badge,
  Card,
  CardContent,
  Divider,
  TextField,
  MenuItem,
  Menu,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  ListItemSecondaryAction,
  Chip,
  useTheme,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  Switch,
  FormControlLabel,
  InputAdornment,
  ToggleButtonGroup,
  ToggleButton
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { 
  Notifications, 
  Search, 
  Person, 
  AccountBalance, 
  MoreVert, 
  CheckCircle, 
  Cancel, 
  Group,
  Settings,
  Dashboard,
  AccountBalanceWallet,
  Receipt,
  Add,
  Close,
  FilterList,
  CreditCard
} from '@mui/icons-material';
import { format, parseISO } from 'date-fns';

// Mock data for users
const mockUsers = [
  { id: 101, name: 'John Doe', email: 'john.doe@example.com', status: 'active', accounts: 2, lastLogin: '2023-04-30T15:42:00Z' },
  { id: 102, name: 'Jane Smith', email: 'jane.smith@example.com', status: 'active', accounts: 3, lastLogin: '2023-04-30T12:22:00Z' },
  { id: 103, name: 'Robert Johnson', email: 'robert.j@example.com', status: 'inactive', accounts: 1, lastLogin: '2023-04-15T09:30:00Z' },
  { id: 104, name: 'Emily Davis', email: 'emily.d@example.com', status: 'active', accounts: 2, lastLogin: '2023-04-29T14:15:00Z' },
  { id: 105, name: 'Michael Wilson', email: 'michael.w@example.com', status: 'active', accounts: 1, lastLogin: '2023-04-28T11:10:00Z' }
];

// Add mock data for accounts
const mockAccounts = [
  { 
    id: 1001, 
    accountNumber: "1234567890", 
    type: "CHECKING", 
    balance: 2580.45, 
    currency: "KES", 
    status: "ACTIVE", 
    userId: 101, 
    userName: "John Doe",
    createdAt: "2022-01-15T09:30:00Z" 
  },
  { 
    id: 1002, 
    accountNumber: "9876543210", 
    type: "SAVINGS", 
    balance: 15750.2, 
    currency: "KES", 
    status: "ACTIVE", 
    userId: 101, 
    userName: "John Doe",
    createdAt: "2022-03-10T14:20:00Z" 
  },
  { 
    id: 1003, 
    accountNumber: "5678901234", 
    type: "FIXED_DEPOSIT", 
    balance: 50000.0, 
    currency: "KES", 
    status: "ACTIVE", 
    userId: 102, 
    userName: "Jane Smith",
    createdAt: "2022-06-22T11:15:00Z" 
  },
  { 
    id: 1004, 
    accountNumber: "2468013579", 
    type: "CHECKING", 
    balance: 3200.75, 
    currency: "KES", 
    status: "INACTIVE", 
    userId: 103, 
    userName: "Robert Johnson",
    createdAt: "2022-08-05T10:45:00Z" 
  },
  { 
    id: 1005, 
    accountNumber: "1357924680", 
    type: "SAVINGS", 
    balance: 8900.30, 
    currency: "KES", 
    status: "ACTIVE", 
    userId: 104, 
    userName: "Emily Davis",
    createdAt: "2022-09-18T13:20:00Z" 
  },
  { 
    id: 1006, 
    accountNumber: "9753102468", 
    type: "CHECKING", 
    balance: 1500.00, 
    currency: "KES", 
    status: "ACTIVE", 
    userId: 105, 
    userName: "Michael Wilson",
    createdAt: "2022-10-30T09:10:00Z" 
  }
];

// Account Action Cell Component
const AccountActionCell = ({ params, onActivate, onDeactivate, onEdit }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  
  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };
  
  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleActivate = () => {
    onActivate(params.row);
    handleClose();
  };

  const handleDeactivate = () => {
    onDeactivate(params.row);
    handleClose();
  };

  const handleEdit = () => {
    onEdit(params.row);
    handleClose();
  };
  
  return (
    <>
      <IconButton onClick={handleClick} size="small">
        <MoreVert />
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem onClick={handleEdit}>Edit Account</MenuItem>
        {params.row.status === 'ACTIVE' ? (
          <MenuItem onClick={handleDeactivate}>Deactivate</MenuItem>
        ) : (
          <MenuItem onClick={handleActivate}>Activate</MenuItem>
        )}
      </Menu>
    </>
  );
};

// Account statistics card
const AccountStatCard = ({ title, value, icon, color }) => (
  <Card sx={{ height: '100%' }}>
    <CardContent>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography color="textSecondary" variant="subtitle2">
          {title}
        </Typography>
        <Box sx={{ 
          backgroundColor: `${color}.light`, 
          color: `${color}.main`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 40,
          height: 40,
          borderRadius: '50%'
        }}>
          {icon}
        </Box>
      </Box>
      <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
        {value}
      </Typography>
    </CardContent>
  </Card>
);

// Account table columns
const getAccountColumns = (onActivate, onDeactivate, onEdit) => [
  { field: 'id', headerName: 'ID', width: 70 },
  { field: 'accountNumber', headerName: 'Account Number', width: 150 },
  { 
    field: 'type', 
    headerName: 'Type', 
    width: 130,
    renderCell: (params) => (
      <Chip 
        label={params.value} 
        color={
          params.value === 'CHECKING' 
            ? 'primary' 
            : params.value === 'SAVINGS' 
              ? 'secondary' 
              : 'info'
        } 
        size="small"
      />
    )
  },
  { 
    field: 'balance', 
    headerName: 'Balance', 
    width: 120,
    renderCell: (params) => (
      <Typography>
        {params.row?.currency || 'KES'} {params.value?.toLocaleString()}
      </Typography>
    )
  },
  { 
    field: 'userName', 
    headerName: 'Account Owner', 
    width: 180,
    renderCell: (params) => (
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <Avatar sx={{ width: 32, height: 32, mr: 1 }}>{params.value?.charAt(0) || ''}</Avatar>
        {params.value}
      </Box>
    )
  },
  { 
    field: 'status', 
    headerName: 'Status', 
    width: 120,
    renderCell: (params) => (
      <Chip 
        label={params.value} 
        color={params.value === 'ACTIVE' ? 'success' : 'error'} 
        size="small"
      />
    )
  },
  { 
    field: 'createdAt', 
    headerName: 'Created', 
    width: 150,
    valueFormatter: (params) => {
      if (!params.value) return '';
      return format(parseISO(params.value), 'MMM dd, yyyy');
    }
  },
  {
    field: 'actions',
    headerName: 'Actions',
    width: 100,
    renderCell: (params) => (
      <AccountActionCell 
        params={params} 
        onActivate={onActivate}
        onDeactivate={onDeactivate}
        onEdit={onEdit}
      />
    )
  }
];

// Admin Dashboard Component
const AdminDashboard = () => {
  const theme = useTheme();
  const [accounts, setAccounts] = useState(mockAccounts);
  const [accountSearchTerm, setAccountSearchTerm] = useState("");
  const [openAddAccountDialog, setOpenAddAccountDialog] = useState(false);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [statusFilter, setStatusFilter] = useState("all");
  const [newAccount, setNewAccount] = useState({
    accountNumber: "",
    type: "CHECKING",
    balance: 0,
    currency: "KES",
    status: "ACTIVE",
    userId: ""
  });
  
  // Calculate account statistics
  const totalAccounts = accounts.length;
  const activeAccounts = accounts.filter(acc => acc.status === 'ACTIVE').length;
  const inactiveAccounts = accounts.filter(acc => acc.status === 'INACTIVE').length;
  
  // Handle account activation
  const handleActivateAccount = (account) => {
    setAccounts(accounts.map(acc => 
      acc.id === account.id ? { ...acc, status: 'ACTIVE' } : acc
    ));
  };

  // Handle account deactivation
  const handleDeactivateAccount = (account) => {
    setAccounts(accounts.map(acc => 
      acc.id === account.id ? { ...acc, status: 'INACTIVE' } : acc
    ));
  };

  // Handle account editing
  const handleEditAccount = (account) => {
    setSelectedAccount(account);
    setNewAccount({
      accountNumber: account.accountNumber,
      type: account.type,
      balance: account.balance,
      currency: account.currency,
      status: account.status,
      userId: account.userId
    });
    setOpenAddAccountDialog(true);
  };

  // Handle add account dialog open
  const handleOpenAddAccountDialog = () => {
    setSelectedAccount(null);
    setNewAccount({
      accountNumber: "",
      type: "CHECKING",
      balance: 0,
      currency: "KES",
      status: "ACTIVE",
      userId: ""
    });
    setOpenAddAccountDialog(true);
  };

  // Handle add account dialog close
  const handleCloseAddAccountDialog = () => {
    setOpenAddAccountDialog(false);
  };

  // Handle status filter change
  const handleStatusFilterChange = (event, newFilter) => {
    if (newFilter !== null) {
      setStatusFilter(newFilter);
    }
  };

  // Handle account creation/update
  const handleSaveAccount = () => {
    const selectedUser = mockUsers.find(user => user.id === parseInt(newAccount.userId));
    
    if (selectedAccount) {
      // Update existing account
      setAccounts(accounts.map(acc => 
        acc.id === selectedAccount.id 
          ? { 
              ...acc, 
              ...newAccount, 
              userName: selectedUser ? selectedUser.name : 'Unknown User'
            } 
          : acc
      ));
    } else {
      // Create new account
      const newId = Math.max(...accounts.map(a => a.id)) + 1;
      setAccounts([
        ...accounts, 
        {
          id: newId,
          ...newAccount,
          userName: selectedUser ? selectedUser.name : 'Unknown User',
          createdAt: new Date().toISOString()
        }
      ]);
    }
    
    handleCloseAddAccountDialog();
  };

  // Handle account input change
  const handleAccountInputChange = (e) => {
    const { name, value } = e.target;
    setNewAccount({
      ...newAccount,
      [name]: name === 'balance' ? parseFloat(value) || 0 : value
    });
  };

  // Filter accounts based on search term and status filter
  const filteredAccounts = accounts.filter(account => {
    // Filter by search term
    const matchesSearch = 
      account.accountNumber.toLowerCase().includes(accountSearchTerm.toLowerCase()) ||
      account.userName.toLowerCase().includes(accountSearchTerm.toLowerCase()) ||
      account.type.toLowerCase().includes(accountSearchTerm.toLowerCase());
    
    // Filter by status
    const matchesStatus = 
      statusFilter === "all" || 
      (statusFilter === "active" && account.status === "ACTIVE") ||
      (statusFilter === "inactive" && account.status === "INACTIVE");
    
    return matchesSearch && matchesStatus;
  });
  
  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Grid container spacing={3}>
        {/* Header Section */}
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Box>
              <Typography variant="h4" gutterBottom>Account Management</Typography>
              <Typography variant="body1" color="textSecondary">
                Add, edit, activate or deactivate accounts
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <IconButton sx={{ mr: 2 }}>
                <Badge badgeContent={5} color="error">
                  <Notifications />
                </Badge>
              </IconButton>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar alt="Admin" sx={{ bgcolor: 'primary.main', mr: 1 }}>A</Avatar>
                <Typography variant="subtitle1" sx={{ fontWeight: 'medium' }}>Admin</Typography>
              </Box>
            </Box>
          </Box>
        </Grid>
        
        {/* Account Statistics */}
        <Grid item xs={12}>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <AccountStatCard 
                title="Total Accounts" 
                value={totalAccounts} 
                icon={<AccountBalance />} 
                color="primary"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AccountStatCard 
                title="Active Accounts" 
                value={activeAccounts} 
                icon={<CheckCircle />} 
                color="success"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <AccountStatCard 
                title="Inactive Accounts" 
                value={inactiveAccounts} 
                icon={<Cancel />} 
                color="error"
              />
            </Grid>
          </Grid>
        </Grid>
        
        {/* Accounts Management */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3, mt: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6" fontWeight="medium">Accounts</Typography>
              <Button
                variant="contained"
                color="primary"
                startIcon={<Add />}
                onClick={handleOpenAddAccountDialog}
              >
                Add New Account
              </Button>
            </Box>
            
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <ToggleButtonGroup
                value={statusFilter}
                exclusive
                onChange={handleStatusFilterChange}
                aria-label="account status filter"
                size="small"
              >
                <ToggleButton value="all" aria-label="all accounts">
                  <Typography variant="body2">All</Typography>
                </ToggleButton>
                <ToggleButton value="active" aria-label="active accounts">
                  <Typography variant="body2" color="success.main">Active Only</Typography>
                </ToggleButton>
                <ToggleButton value="inactive" aria-label="inactive accounts">
                  <Typography variant="body2" color="error.main">Inactive Only</Typography>
                </ToggleButton>
              </ToggleButtonGroup>
              
              <TextField
                placeholder="Search accounts..."
                size="small"
                value={accountSearchTerm}
                onChange={(e) => setAccountSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search fontSize="small" sx={{ color: 'text.secondary' }} />
                    </InputAdornment>
                  )
                }}
                sx={{ width: 300 }}
              />
            </Box>
            
            <Box sx={{ height: 500 }}>
              <DataGrid
                rows={filteredAccounts}
                columns={getAccountColumns(
                  handleActivateAccount, 
                  handleDeactivateAccount, 
                  handleEditAccount
                )}
                pageSize={8}
                rowsPerPageOptions={[8, 16, 24]}
                disableSelectionOnClick
                sx={{
                  '& .MuiDataGrid-columnHeaders': {
                    backgroundColor: theme.palette.grey[100],
                    color: theme.palette.grey[900],
                    fontWeight: 'bold'
                  }
                }}
              />
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Add/Edit Account Dialog */}
      <Dialog open={openAddAccountDialog} onClose={handleCloseAddAccountDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          {selectedAccount ? 'Edit Account' : 'Add New Account'}
          <IconButton onClick={handleCloseAddAccountDialog} size="small">
            <Close />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                label="Account Number"
                name="accountNumber"
                value={newAccount.accountNumber}
                onChange={handleAccountInputChange}
                fullWidth
                required
                margin="normal"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Account Type</InputLabel>
                <Select
                  name="type"
                  value={newAccount.type}
                  onChange={handleAccountInputChange}
                  label="Account Type"
                >
                  <MenuItem value="CHECKING">CHECKING</MenuItem>
                  <MenuItem value="SAVINGS">SAVINGS</MenuItem>
                  <MenuItem value="FIXED_DEPOSIT">FIXED DEPOSIT</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Currency</InputLabel>
                <Select
                  name="currency"
                  value={newAccount.currency}
                  onChange={handleAccountInputChange}
                  label="Currency"
                >
                  <MenuItem value="KES">KES</MenuItem>
                  <MenuItem value="USD">USD</MenuItem>
                  <MenuItem value="EUR">EUR</MenuItem>
                  <MenuItem value="GBP">GBP</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Initial Balance"
                name="balance"
                type="number"
                value={newAccount.balance}
                onChange={handleAccountInputChange}
                fullWidth
                margin="normal"
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Account Owner</InputLabel>
                <Select
                  name="userId"
                  value={newAccount.userId}
                  onChange={handleAccountInputChange}
                  label="Account Owner"
                  required
                >
                  {mockUsers.map(user => (
                    <MenuItem key={user.id} value={user.id}>
                      {user.name} ({user.email})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={newAccount.status === 'ACTIVE'}
                    onChange={(e) => setNewAccount({
                      ...newAccount,
                      status: e.target.checked ? 'ACTIVE' : 'INACTIVE'
                    })}
                    color="success"
                  />
                }
                label={`Account Status: ${newAccount.status === 'ACTIVE' ? 'Active' : 'Inactive'}`}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseAddAccountDialog}>Cancel</Button>
          <Button 
            variant="contained" 
            color="primary" 
            onClick={handleSaveAccount}
            disabled={!newAccount.accountNumber || !newAccount.userId}
          >
            {selectedAccount ? 'Update Account' : 'Create Account'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminDashboard;