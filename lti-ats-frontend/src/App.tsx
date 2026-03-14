import { Layout, Menu, Typography } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import AppRouter from './router';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

const menuItems = [
  { key: 'positions', label: 'Positions' },
];

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();

  const selectedKey = location.pathname.replace('/', '') || 'positions';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <Title level={3} style={{ color: 'white', margin: 0 }}>
          LTI Applicant Tracking System
        </Title>
      </Header>
      <Layout>
        <Sider width={220}>
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            items={menuItems}
            style={{ height: '100%' }}
            onClick={({ key }) => navigate(`/${key}`)}
          />
        </Sider>
        <Content style={{ padding: 24 }}>
          <AppRouter />
        </Content>
      </Layout>
    </Layout>
  );
}
