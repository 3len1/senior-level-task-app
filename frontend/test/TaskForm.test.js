import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import TaskForm from '../src/components/TaskForm';
import renderWithProviders from './utils/renderWithProviders';

function renderWithRoute(route, preloadedState) {
  const { Wrapper } = renderWithProviders(<TaskForm />, { preloadedState });
  return render(
    <Wrapper>
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path="/projects/:projectId/tasks/new" element={<TaskForm />} />
        </Routes>
      </MemoryRouter>
    </Wrapper>
  );
}

describe('TaskForm', () => {
  test('renders new task form and allows input', () => {
    const preloadedState = {
      projects: { items: [], loading: false, error: null },
      auth: { token: null, user: null, loading: false, error: null },
      tasks: { byProject: { 1: { items: [], loading: false, error: null } }, lastEvent: null },
      users: { items: [], loading: false, error: null },
      ui: { snackbar: null },
    };

    renderWithRoute('/projects/1/tasks/new', preloadedState);

    expect(screen.getByText('New Task')).toBeInTheDocument();

    const title = screen.getByRole('textbox', { name: /title/i });
    fireEvent.change(title, { target: { value: 'Test Task' } });
    expect(title).toHaveValue('Test Task');

    const status = screen.getByLabelText(/status/i);
    expect(status).toBeInTheDocument();

    const saveBtn = screen.getByRole('button', { name: /save/i });
    expect(saveBtn).toBeEnabled();
  });
});
