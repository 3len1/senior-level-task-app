import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../src/App';
import renderWithProviders from './utils/renderWithProviders';

describe('App routing', () => {
  test('renders header and routes', () => {
    const preloadedState = {
      projects: { items: [], loading: false, error: null },
      auth: { token: null, user: null, loading: false, error: null },
      tasks: { byProject: {}, lastEvent: null },
      users: { items: [], loading: false, error: null },
      ui: { snackbar: null },
    };
    const { Wrapper } = renderWithProviders(<App />, { preloadedState });
    render(
      <Wrapper>
        <MemoryRouter>
          <App />
        </MemoryRouter>
      </Wrapper>
    );

    expect(screen.getByText('Senior Task Manager')).toBeInTheDocument();
    // There are multiple "Projects" occurrences (header link and page title). Ensure at least one link named Projects exists.
    const link = screen.getByRole('link', { name: /Projects/i });
    expect(link).toBeInTheDocument();
  });
});
