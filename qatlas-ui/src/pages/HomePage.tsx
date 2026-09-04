import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Download, LogOut } from 'lucide-react';
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts';
import { useApplications, useExecutions } from '../hooks/useQueries';
import { useAuth } from '../hooks/useAuth';
import { LoadingState, ErrorState, Card } from '../components/Primitives';
import { ProjectCard } from '../components/ProjectCard';
import {
  buildProjectCards, sortProjectCards, computeKpis, computeDailyTrend,
  computeExecutionsByMachine, uniqueExecutors,
} from '../lib/portfolio';
import { RANGE_OPTIONS, RANGE_LABELS, type DateRangeKey } from '../lib/dateRange';
import { downloadCsv } from '../lib/csvExport';
import { QuickNav } from '../components/QuickNav';

const PAGE_SIZE = 9;

type SortMode = 'priority' | 'recent' | 'alpha';

export function HomePage() {
  const { data: applications, isLoading: loadingApps, isError: errorApps } = useApplications();
  const { data: executions, isLoading: loadingExecs, isError: errorExecs } = useExecutions();
  const { isAdmin, username, logout } = useAuth();

  const [range, setRange] = useState<DateRangeKey>('30D');
  const [executor, setExecutor] = useState<string>('All');
  const [search, setSearch] = useState('');
  const [sortMode, setSortMode] = useState<SortMode>('priority');
  const [page, setPage] = useState(1);

  const cards = useMemo(() => {
    if (!applications || !executions) return [];
    return buildProjectCards(applications, executions);
  }, [applications, executions]);

  const executors = useMemo(() => (executions ? uniqueExecutors(executions) : []), [executions]);

  const filteredSorted = useMemo(() => {
    let list = cards;
    if (executor !== 'All') {
      list = list.filter((c) => (c.latest?.executedBy || c.latest?.systemName) === executor);
    }
    if (search.trim()) {
      const q = search.trim().toLowerCase();
      list = list.filter((c) => c.application.name.toLowerCase().includes(q));
    }
    return sortProjectCards(list, sortMode);
  }, [cards, executor, search, sortMode]);

  const totalPages = Math.max(1, Math.ceil(filteredSorted.length / PAGE_SIZE));
  const pageItems = filteredSorted.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const kpis = useMemo(
    () => (applications && executions ? computeKpis(applications, executions, range) : null),
    [applications, executions, range]
  );
  const dailyTrend = useMemo(() => (executions ? computeDailyTrend(executions, range) : []), [executions, range]);
  const machineData = useMemo(
    () => (executions ? computeExecutionsByMachine(executions, range) : []),
    [executions, range]
  );

  function handleExportKpis() {
    if (!kpis) return;
    downloadCsv(`qatlas-kpis-${range}.csv`, [
      ['Metric', 'Value'],
      ['Active projects', kpis.activeProjects],
      [`Executions (${RANGE_LABELS[range]})`, kpis.executionsInRange],
      [`Pass rate (${RANGE_LABELS[range]})`, kpis.passRate != null ? `${kpis.passRate}%` : 'N/A'],
      ['Currently running', kpis.currentlyRunning],
      [],
      ['Executed by', 'Executions'],
      ...machineData.map((m) => [m.name, m.count]),
    ]);
  }

  if (loadingApps || loadingExecs) return <LoadingState label="Loading portfolio…" />;
  if (errorApps || errorExecs) return <ErrorState message="Could not reach the QAtlas backend." />;

  return (
    <div className="mx-auto max-w-[1280px] px-8 py-6">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-semibold tracking-tight text-slate-900">QAtlas</h1>
        <div className="flex items-center gap-3">
          <QuickNav />
          <button
            onClick={handleExportKpis}
            className="flex items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
          >
            <Download className="h-3.5 w-3.5" />
            Export KPIs
          </button>
          {isAdmin ? (
            <div className="flex items-center gap-2">
              <span className="text-xs text-[var(--color-ink-muted)]">Signed in as {username}</span>
              <button
                onClick={logout}
                className="flex items-center gap-1 rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
              >
                <LogOut className="h-3.5 w-3.5" />
                Sign out
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
            >
              Sign in
            </Link>
          )}
        </div>
      </div>

      {/* KPI strip */}
      <div className="mb-4 grid grid-cols-4 gap-4">
        <Card className="px-4 py-3">
          <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">Active projects</p>
          <p className="mt-1 text-2xl font-semibold text-slate-900">{kpis?.activeProjects ?? '—'}</p>
        </Card>
        <Card className="px-4 py-3">
          <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">Executions ({range.toLowerCase()})</p>
          <p className="mt-1 text-2xl font-semibold text-slate-900">{kpis?.executionsInRange ?? '—'}</p>
        </Card>
        <Card className="px-4 py-3">
          <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">Pass rate ({range.toLowerCase()})</p>
          <p className="mt-1 text-2xl font-semibold text-slate-900">{kpis?.passRate != null ? `${kpis.passRate}%` : '—'}</p>
        </Card>
        <Card className="px-4 py-3">
          <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">Currently running</p>
          <p className="mt-1 text-2xl font-semibold text-slate-900">{kpis?.currentlyRunning ?? '—'}</p>
        </Card>
      </div>

      {/* Executed-by tabs + date range */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <p className="mb-1.5 text-[11px] text-[var(--color-ink-muted)]">Executed by</p>
          <div className="flex flex-wrap gap-2">
            {['All', ...executors].map((name) => (
              <button
                key={name}
                onClick={() => { setExecutor(name); setPage(1); }}
                className={`rounded-full border px-3 py-1 text-xs font-medium ${
                  executor === name
                    ? 'border-slate-900 bg-slate-900 text-white'
                    : 'border-[var(--color-border)] bg-white text-[var(--color-ink-muted)] hover:bg-slate-50'
                }`}
              >
                {name}
              </button>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-[11px] text-[var(--color-ink-muted)]">Showing: {RANGE_LABELS[range]}</span>
          <div className="flex gap-1">
            {RANGE_OPTIONS.map((r) => (
              <button
                key={r}
                onClick={() => setRange(r)}
                className={`rounded-full border px-3 py-1 text-xs font-medium ${
                  range === r
                    ? 'border-slate-900 bg-slate-900 text-white'
                    : 'border-[var(--color-border)] bg-white text-[var(--color-ink-muted)] hover:bg-slate-50'
                }`}
              >
                {r}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Projects header: title, search, sort */}
      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-base font-semibold text-slate-900">Projects</h2>
        <div className="flex gap-2">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-ink-muted)]" />
            <input
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(1); }}
              placeholder="Search projects"
              className="w-64 rounded-lg border border-[var(--color-border)] bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-400"
            />
          </div>
          <select
            value={sortMode}
            onChange={(e) => setSortMode(e.target.value as SortMode)}
            className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700"
          >
            <option value="priority">Sort: Running first</option>
            <option value="recent">Sort: Most recent</option>
            <option value="alpha">Sort: Alphabetical</option>
          </select>
        </div>
      </div>

      {/* Project grid */}
      {pageItems.length === 0 ? (
        <Card className="py-16 text-center text-sm text-[var(--color-ink-muted)]">
          No projects match your filters.
        </Card>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {pageItems.map((c) => (
            <ProjectCard key={c.application.id} data={c} />
          ))}
        </div>
      )}

      <div className="mt-3 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">
        <span>
          Showing {pageItems.length} of {filteredSorted.length} project{filteredSorted.length === 1 ? '' : 's'}
        </span>
        {totalPages > 1 && (
          <div className="flex items-center gap-2">
            <button
              disabled={page <= 1}
              onClick={() => setPage((p) => p - 1)}
              className="rounded-md border border-[var(--color-border)] px-2 py-1 disabled:opacity-40"
            >
              Prev
            </button>
            <span>Page {page} of {totalPages}</span>
            <button
              disabled={page >= totalPages}
              onClick={() => setPage((p) => p + 1)}
              className="rounded-md border border-[var(--color-border)] px-2 py-1 disabled:opacity-40"
            >
              Next
            </button>
          </div>
        )}
      </div>

      {/* Aggregate charts */}
      <h2 className="mb-3 mt-8 text-base font-semibold text-slate-900">
        Company-wide, {RANGE_LABELS[range]}
      </h2>
      <div className="grid grid-cols-3 gap-4">
        <Card className="p-4">
          <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Total executions per day</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={dailyTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={(d) => d.slice(5)} />
              <YAxis tick={{ fontSize: 10 }} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="executions" fill="var(--color-chart-neutral)" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card className="p-4">
          <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Pass vs fail trend</p>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={dailyTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={(d) => d.slice(5)} />
              <YAxis tick={{ fontSize: 10 }} allowDecimals={false} />
              <Tooltip />
              <Line type="monotone" dataKey="passed" stroke="var(--color-status-passed)" strokeWidth={2} dot={false} name="Passed" />
              <Line type="monotone" dataKey="failed" stroke="var(--color-status-failed)" strokeWidth={2} dot={false} name="Failed" />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card className="p-4">
          <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Executions by machine</p>
          {machineData.length === 0 ? (
            <div className="flex h-[200px] items-center justify-center text-xs text-[var(--color-ink-muted)]">
              No data in range
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={machineData} layout="vertical" margin={{ left: 10 }}>
                <XAxis type="number" hide />
                <YAxis type="category" dataKey="name" tick={{ fontSize: 10 }} width={90} />
                <Tooltip />
                <Bar dataKey="count" fill="var(--color-chart-neutral)" radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </Card>
      </div>
    </div>
  );
}
