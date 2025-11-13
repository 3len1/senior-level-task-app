import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { within } from '@testing-library/react';
import ProjectDashboard from '../src/components/ProjectDashboard';
import renderWithProviders from './utils/renderWithProviders';

describe('ProjectDashboard', () => {
  test('renders projects and create form', () => {
    const preloadedState = {
      projects: { items: [{ id: 1, name: 'Alpha', description: 'First' }], loading: false, error: null },
      auth: { token: null, user: null, loading: false, error: null },
      tasks: { byProject: {}, lastEvent: null },
      users: { items: [], loading: false, error: null },
      ui: { snackbar: null },
    };
    const { Wrapper } = renderWithProviders(<ProjectDashboard />, { preloadedState });
    render(
      <Wrapper>
        <MemoryRouter>
          <ProjectDashboard />
        </MemoryRouter>
      </Wrapper>
    );

    expect(screen.getByText('Projects')).toBeInTheDocument();
    expect(screen.getByText('Alpha')).toBeInTheDocument();

    const section = screen.getByText('Create Project').closest('div');
    const textboxes = within(section).getAllByRole('textbox');
    const nameInput = textboxes[0];
    fireEvent.change(nameInput, { target: { value: 'New Project' } });
    expect(nameInput).toHaveValue('New Project');
  });
});
