import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import UserManagement from '../src/components/UserManagement';
import renderWithProviders from './utils/renderWithProviders';

describe('UserManagement', () => {
  test('renders list and add user form', () => {
    const preloadedState = {
      projects: { items: [], loading: false, error: null },
      auth: { token: null, user: null, loading: false, error: null },
      tasks: { byProject: {}, lastEvent: null },
      users: { items: [{ id: 1, username: 'alice', role: 'ROLE_USER' }], loading: false, error: null },
      ui: { snackbar: null },
    };
    const { Wrapper } = renderWithProviders(<UserManagement />, { preloadedState });

    render(
      <Wrapper>
        <MemoryRouter>
          <UserManagement />
        </MemoryRouter>
      </Wrapper>
    );

    expect(screen.getByText('User Management')).toBeInTheDocument();
    expect(screen.getByText('alice')).toBeInTheDocument();

    const username = screen.getByRole('textbox', { name: /username/i });
    fireEvent.change(username, { target: { value: 'bob' } });
    expect(username).toHaveValue('bob');

    const password = screen.getByLabelText(/password/i);
    fireEvent.change(password, { target: { value: 'secret' } });
    expect(password).toHaveValue('secret');

    // While users are being fetched on mount, loading=true disables the button
    expect(screen.getByRole('button', { name: /add user/i })).toBeDisabled();
  });
});
