import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
});

export interface Position {
  id: string;
  title: string;
  department: string;
  openDate: string;
  createdAt: string;
  updatedAt: string;
}

export type PositionPayload = Pick<Position, 'title' | 'department' | 'openDate'>;

export const getPositions = () => api.get<Position[]>('/positions');
export const getPosition = (id: string) => api.get<Position>(`/positions/${id}`);
export const createPosition = (data: PositionPayload) => api.post<Position>('/positions', data);
export const updatePosition = (id: string, data: PositionPayload) => api.put<Position>(`/positions/${id}`, data);
export const deletePosition = (id: string) => api.delete<void>(`/positions/${id}`);
