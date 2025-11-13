import React from 'react';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../src/App';
import renderWithProviders from './utils/renderWithProviders';

function renderApp(preloadedState, initialRoute = '/') {
  const { Wrapper } = renderWithProviders(null, { preloadedState });

  return render(
      <Wrapper>
        <MemoryRouter initialEntries={[initialRoute]}>
          <App />
        </MemoryRouter>
      </Wrapper>
  );
}

describe('App routing', () => {
  test('renders header navigation and auth links when logged out', () => {
    const preloadedState = {
      projects: { items: [], loading: false, error: null },
      auth: { token: null, user: null, loading: false, error: null },
      tasks: { byProject: {}, lastEvent: null },
      users: { items: [], loading: false, error: null },
      ui: { snackbar: null },
    };

    renderApp(preloadedState, '/');

    // Scope the query to the header (AppBar)
    const header = screen.getByRole('banner'); // <header> from MUI AppBar
    const headerWithin = within(header);

    // When not authenticated, header should show Login & Register
    expect(
        headerWithin.getByRole('link', { name: /login/i })
    ).toBeInTheDocument();

    expect(
        headerWithin.getByRole('link', { name: /register/i })
    ).toBeInTheDocument();
  });

  test('shows Projects link when authenticated', () => {
    const preloadedState = {
      projects: { items: [], loading: false, error: null },
      auth: {
        token: 'fake',
        user: { username: 'eleni', role: 'ROLE_USER' },
        loading: false,
        error: null,
      },
      tasks: { byProject: {}, lastEvent: null },
      users: { items: [], loading: false, error: null },
      ui: { snackbar: null },
    };

    renderApp(preloadedState, '/');

    const projectsLink = screen.getByRole('link', { name: /projects/i });
    expect(projectsLink).toBeInTheDocument();
  });
});
