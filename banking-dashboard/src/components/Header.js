import React, { useState } from 'react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Box, 
  IconButton, 
  Menu, 
  MenuItem, 
  useMediaQuery, 
  useTheme, 
  Drawer,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import AccessibilityNewIcon from '@mui/icons-material/AccessibilityNew';
import { Link } from 'react-router-dom';

const Header = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const navItems = [
    { label: 'Locate Us', href: '#' },
    { label: 'Talk to Us', href: '#' },
    { label: 'DTB 24/7 App', href: '#' },
  ];

  const drawer = (
    <Box onClick={handleDrawerToggle} sx={{ textAlign: 'center' }}>
      <Box sx={{ py: 2 }}>
        <Typography variant="h6" component="div">
          DTB
        </Typography>
      </Box>
      <Divider />
      <List>
        {navItems.map((item) => (
          <ListItem button key={item.label} component="a" href={item.href}>
            <ListItemText primary={item.label} />
          </ListItem>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static" color="primary">
        <Toolbar>
          {isMobile && (
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          
          <Typography
            variant="h6"
            component="div"
            sx={{ 
              flexGrow: 1,
              display: 'flex',
              justifyContent: isMobile ? 'center' : 'flex-start',
              alignItems: 'center'
            }}
          >
            <Box 
              component="img"
              sx={{
                height: 40,
                maxHeight: { xs: 35, md: 40 },
                mr: 1
              }}
              alt="DTB Logo"
              src="/dtb-logo.png"
            />
            <Box component={Link} to="/" sx={{ textDecoration: 'none', color: 'inherit' }}>
              DIAMOND TRUST BANK
            </Box>
          </Typography>

          {!isMobile && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              {navItems.map((item) => (
                <Button 
                  key={item.label} 
                  color="inherit" 
                  sx={{ mx: 1 }}
                  component="a"
                  href={item.href}
                >
                  {item.label}
                </Button>
              ))}
            </Box>
          )}

          <Box sx={{ ml: 2, display: 'flex', alignItems: 'center' }}>
            <IconButton color="inherit" aria-label="accessibility">
              <AccessibilityNewIcon />
            </IconButton>
            <Button
              variant="contained"
              color="secondary"
              sx={{ 
                ml: 1,
                fontWeight: 'bold',
                display: { xs: 'none', sm: 'block' }
              }}
              component={Link}
              to="/login"
            >
              Online Banking
            </Button>
          </Box>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile
        }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
        }}
      >
        {drawer}
      </Drawer>
    </Box>
  );
};

export default Header; 