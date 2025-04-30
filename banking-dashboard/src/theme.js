import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#e4173e', // DTB red (approximated from image)
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#ffda2d', // DTB yellow (approximated from image)
      contrastText: '#000000',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
    },
    h2: {
      fontWeight: 600,
    },
    button: {
      textTransform: 'none',
      fontWeight: 500,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 25,
          padding: '10px 20px',
        },
        containedPrimary: {
          '&:hover': {
            backgroundColor: '#c9102d',
          },
        },
        containedSecondary: {
          '&:hover': {
            backgroundColor: '#e5c426',
          },
        },
      },
    },
  },
});

export default theme; 