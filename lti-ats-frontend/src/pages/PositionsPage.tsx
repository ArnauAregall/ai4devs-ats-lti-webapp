import type { TableColumnsType } from 'antd';
import { Alert, Button, Space, Spin, Table, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPositions, type Position } from '../services/api';

const { Title } = Typography;

export default function PositionsPage() {
  const [positions, setPositions] = useState<Position[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    getPositions()
      .then((response) => setPositions(response.data))
      .catch((err: Error) => setError(err.message || 'Failed to load positions'))
      .finally(() => setLoading(false));
  }, []);

  const columns: TableColumnsType<Position> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      render: (text: string, record: Position) => (
        <a onClick={() => navigate(`/positions/${record.id}`)}>{text}</a>
      ),
    },
    {
      title: 'Department',
      dataIndex: 'department',
      key: 'department',
    },
    {
      title: 'Open Since',
      dataIndex: 'openDate',
      key: 'openDate',
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Title level={2} style={{ margin: 0 }}>
          Positions
        </Title>
        <Button type="primary" onClick={() => console.log('New Position clicked')}>
          New Position
        </Button>
      </Space>
      {loading && <Spin />}
      {error && <Alert type="error" message={error} showIcon />}
      {!loading && !error && (
        <Table dataSource={positions} columns={columns} rowKey="id" />
      )}
    </div>
  );
}
