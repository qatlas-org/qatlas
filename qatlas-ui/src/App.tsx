import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { ProjectWorkspacePage } from './pages/ProjectWorkspacePage';
import { ExecutionsPage } from './pages/ExecutionsPage';
import { ExecutionDetailPage } from './pages/ExecutionDetailPage';
import { TestCaseDetailPage } from './pages/TestCaseDetailPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { EnvironmentsPage } from './pages/EnvironmentsPage';
import { AuthProvider } from './hooks/useAuth';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* These pages have their own full-width header (no sidebar),
                matching the finalized Figma design for the whole drill-down chain:
                Home -> Project Workspace -> Execution Detail -> Test Case Detail */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/projects/:applicationId" element={<ProjectWorkspacePage />} />
            <Route path="/executions/:id" element={<ExecutionDetailPage />} />
            <Route path="/test-cases/:id" element={<TestCaseDetailPage />} />

            {/* Utility pages keep the sidebar shell */}
            <Route element={<AppShell />}>
              <Route path="/executions" element={<ExecutionsPage />} />
              <Route path="/applications" element={<ApplicationsPage />} />
              <Route path="/environments" element={<EnvironmentsPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
