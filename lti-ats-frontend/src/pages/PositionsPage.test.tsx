import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import * as api from '../services/api';

vi.mock('../services/api');

const routerFuture = { v7_startTransition: true, v7_relativeSplatPath: true };

const seedPositions: api.Position[] = [
  { id: '1', title: 'Backend Engineer', department: 'Engineering', openDate: '2025-01-15', createdAt: '', updatedAt: '' },
  { id: '2', title: 'Product Designer', department: 'Design', openDate: '2025-02-01', createdAt: '', updatedAt: '' },
  { id: '3', title: 'Engineering Manager', department: 'Engineering', openDate: '2025-03-10', createdAt: '', updatedAt: '' },
];

describe('PositionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the LTI Applicant Tracking System heading', async () => {
    vi.mocked(api.getPositions).mockResolvedValue({ data: seedPositions } as never);

    render(
      <MemoryRouter initialEntries={['/positions']} future={routerFuture}>
        <App />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('LTI Applicant Tracking System')).toBeInTheDocument();
    });
  });

  it('displays the three seed positions when the API resolves successfully', async () => {
    vi.mocked(api.getPositions).mockResolvedValue({ data: seedPositions } as never);

    render(
      <MemoryRouter initialEntries={['/positions']} future={routerFuture}>
        <App />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Backend Engineer')).toBeInTheDocument();
      expect(screen.getByText('Product Designer')).toBeInTheDocument();
      expect(screen.getByText('Engineering Manager')).toBeInTheDocument();
      expect(screen.getAllByText('Engineering').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Design')).toBeInTheDocument();
      expect(screen.getByText('2025-01-15')).toBeInTheDocument();
    });
  });

  it('shows an error alert when the API call fails', async () => {
    vi.mocked(api.getPositions).mockRejectedValue(new Error('Network error'));

    render(
      <MemoryRouter initialEntries={['/positions']} future={routerFuture}>
        <App />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
  });

  it('shows the New Position button', async () => {
    vi.mocked(api.getPositions).mockResolvedValue({ data: seedPositions } as never);

    render(
      <MemoryRouter initialEntries={['/positions']} future={routerFuture}>
        <App />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /new position/i })).toBeInTheDocument();
    });
  });
});
