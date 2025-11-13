import React from 'react';
import { render, screen } from '@testing-library/react';
import TaskList from '../src/components/TaskList';
import renderWithProviders from './utils/renderWithProviders';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

function renderWithRoute(ui, route) {
  const preloadedState = {
    projects: { items: [], loading: false, error: null },
    auth: { token: null, user: null, loading: false, error: null },
    tasks: { byProject: { 1: { items: [{ id: 10, title: 'T1', status: 'TODO' }], loading: false, error: null } }, lastEvent: null },
    users: { items: [], loading: false, error: null },
    ui: { snackbar: null },
  };
  const { Wrapper } = renderWithProviders(ui, { preloadedState });
  return render(
    <Wrapper>
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path="/projects/:projectId/tasks" element={<TaskList />} />
        </Routes>
      </MemoryRouter>
    </Wrapper>
  );
}

describe('TaskList', () => {
  test('renders tasks for project', () => {
    renderWithRoute(<TaskList />, '/projects/1/tasks');
    expect(screen.getByText('Tasks')).toBeInTheDocument();
    expect(screen.getByText('T1')).toBeInTheDocument();
    expect(screen.getByText('New Task')).toBeInTheDocument();
  });
});
