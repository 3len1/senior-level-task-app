import { normalizeTask } from '../src/services/api';

describe('normalizeTask', () => {
  it('normalizes DTO shape', () => {
    const raw = {
      id: 10,
      title: 'T',
      description: 'D',
      status: 'TODO',
      deadline: '2025-01-01T00:00:00Z',
      projectId: 5,
      assigneeId: 2,
      assigneeUsername: 'eleni',
      expired: true,
    };
    const n = normalizeTask(raw);
    expect(n).toEqual({
      id: 10,
      title: 'T',
      description: 'D',
      status: 'TODO',
      deadline: '2025-01-01T00:00:00Z',
      projectId: 5,
      assigneeId: 2,
      assigneeUsername: 'eleni',
      expired: true,
    });
  });

  it('normalizes entity shape with nested project/assignee', () => {
    const raw = {
      id: 11,
      title: 'N',
      status: 'DONE',
      project: { id: 7 },
      assignee: { id: 3, username: 'maria' },
    };
    const n = normalizeTask(raw);
    expect(n).toEqual({
      id: 11,
      title: 'N',
      description: '',
      status: 'DONE',
      deadline: null,
      projectId: 7,
      assigneeId: 3,
      assigneeUsername: 'maria',
      expired: false,
    });
  });
});
