import '@testing-library/jest-dom';

// Mock MUI Tooltip to avoid portal/Popper issues in jsdom
jest.mock('@mui/material/Tooltip', () => ({ __esModule: true, default: ({ children }) => children }));

// Additionally patch the named export Tooltip from the barrel module
jest.mock('@mui/material', () => {
  const actual = jest.requireActual('@mui/material');
  return {
    ...actual,
    Tooltip: ({ children }) => children,
    Dialog: ({ open, children }) => (open ? <div data-testid="dialog">{children}</div> : null),
    DialogTitle: ({ children }) => <div>{children}</div>,
    DialogContent: ({ children }) => <div>{children}</div>,
    DialogContentText: ({ children }) => <div>{children}</div>,
    DialogActions: ({ children }) => <div>{children}</div>,
  };
});