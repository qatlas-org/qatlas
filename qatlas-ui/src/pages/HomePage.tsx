import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
    Search,
    Download,
    LogOut,
    RefreshCw,
} from 'lucide-react';
import {
    BarChart,
    Bar,
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
} from 'recharts';

import {
    useDashboardStats,
    useDashboardProjects,
} from '../hooks/useQueries';

import { useAuth } from '../hooks/useAuth';
import { LoadingState, ErrorState, Card } from '../components/Primitives';
import { ProjectCard } from '../components/ProjectCard';

import {
    buildDashboardProjectCards,
    sortProjectCards,
} from '../lib/portfolio';

import {
    RANGE_OPTIONS,
    RANGE_LABELS,
    type DateRangeKey,
} from '../lib/dateRange';

import { downloadCsv } from '../lib/csvExport';
import { QuickNav } from '../components/QuickNav';


const PAGE_SIZE = 9;

type SortMode =
    | 'priority'
    | 'recent'
    | 'alpha';


export function HomePage() {

    const {
        isAdmin,
        username,
        logout,
    } = useAuth();


    const [range, setRange] =
        useState<DateRangeKey>('30D');

    const [executor, setExecutor] =
        useState<string>('All');

    const [search, setSearch] =
        useState('');

    const [sortMode, setSortMode] =
        useState<SortMode>('recent');

    const [page, setPage] =
        useState(1);


    const {
        data: dashboardStats,
        isLoading: loadingDashboard,
        isError: errorDashboard,
        isFetching: fetchingDashboard,
        refetch: refetchDashboard,
    } = useDashboardStats(range);
    const {
        data: executorProjects,
        isLoading: loadingExecutorProjects,
        isError: errorExecutorProjects,
        isFetching: fetchingExecutorProjects,
        refetch: refetchExecutorProjects,
    } = useDashboardProjects(
        range,
        executor
    );

    /*
     * Project cards now come from /dashboard.
     *
     * They therefore respect:
     * 30D / 3M / 6M / 1Y / ALL
     *
     * and are not limited by /test-execution's latest 100 rows.
     */
    const cards = useMemo(
        () =>
            buildDashboardProjectCards(
                executor === 'All'
                    ? dashboardStats?.projects ?? []
                    : executorProjects ?? []
            ),
        [
            dashboardStats,
            executorProjects,
            executor,
        ]
    );


    /*
     * Executor buttons use the same aggregate data as the
     * "Executions by machine" dashboard chart.
     *
     * This means the selector always represents the currently
     * selected dashboard range.
     */
    const executors = useMemo(
        () =>
            (dashboardStats?.executionsByMachine ?? [])
                .map((machine) => machine.machine)
                .filter(Boolean),
        [dashboardStats]
    );


    /*
     * Project cards are already executor-filtered by the backend.
     *
     * This block only applies client-side search and sorting.
     */
    const filteredSorted = useMemo(() => {

        let list = cards;

        if (search.trim()) {

            const query =
                search
                    .trim()
                    .toLowerCase();

            list = list.filter(
                (card) =>
                    card.application.name
                        .toLowerCase()
                        .includes(query)
            );
        }


        return sortProjectCards(
            list,
            sortMode
        );

    }, [
        cards,
        search,
        sortMode,
    ]);


    const totalPages =
        Math.max(
            1,
            Math.ceil(
                filteredSorted.length /
                PAGE_SIZE
            )
        );


    const pageItems =
        filteredSorted.slice(
            (page - 1) * PAGE_SIZE,
            page * PAGE_SIZE
        );


    const kpis =
        dashboardStats
            ? {
                activeProjects:
                dashboardStats.activeProjects,

                executionsInRange:
                dashboardStats.executions,

                passRate:
                dashboardStats.passRate,

                currentlyRunning:
                dashboardStats.currentlyRunning,
            }
            : null;


    const dailyTrend =
        dashboardStats?.dailyTrend ?? [];


    const machineData = useMemo(
        () =>
            (
                dashboardStats
                    ?.executionsByMachine ??
                []
            ).map(
                (machine) => ({
                    name: machine.machine,
                    count: machine.executions,
                })
            ),
        [dashboardStats]
    );
    const refreshingDashboard =
        fetchingDashboard ||
        (
            executor !== 'All' &&
            fetchingExecutorProjects
        );
    async function handleRefreshDashboard() {

        if (executor === 'All') {
            await refetchDashboard();
            return;
        }

        await Promise.all([
            refetchDashboard(),
            refetchExecutorProjects(),
        ]);
    }


    function handleExportKpis() {

        if (!kpis) {
            return;
        }


        downloadCsv(
            `qatlas-kpis-${range}.csv`,
            [
                [
                    'Metric',
                    'Value',
                ],

                [
                    'Active projects',
                    kpis.activeProjects,
                ],

                [
                    `Executions (${RANGE_LABELS[range]})`,
                    kpis.executionsInRange,
                ],

                [
                    `Pass rate (${RANGE_LABELS[range]})`,
                    kpis.passRate != null
                        ? `${kpis.passRate}%`
                        : 'N/A',
                ],

                [
                    'Currently running',
                    kpis.currentlyRunning,
                ],

                [],

                [
                    'Executed by',
                    'Executions',
                ],

                ...machineData.map(
                    (machine) => [
                        machine.name,
                        machine.count,
                    ]
                ),
            ]
        );
    }


    if (loadingDashboard) {

        return (
            <LoadingState
                label="Loading portfolio…"
            />
        );
    }


    if (errorDashboard) {

        return (
            <ErrorState
                message="Could not reach the QAtlas backend."
            />
        );
    }


    return (
        <div className="mx-auto max-w-[1280px] px-8 py-6">

            {/* Header */}

            <div className="mb-6 flex items-center justify-between">

                <h1 className="text-xl font-semibold tracking-tight text-slate-900">
                    QAtlas
                </h1>


                <div className="flex items-center gap-3">

                    <QuickNav />

                    <button
                        onClick={handleRefreshDashboard}
                        disabled={refreshingDashboard}
                        title="Refresh dashboard"
                        className="flex items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        <RefreshCw
                            className={
                                `h-3.5 w-3.5 ${
                                    refreshingDashboard
                                        ? 'animate-spin'
                                        : ''
                                }`
                            }
                        />

                        {refreshingDashboard
                            ? 'Refreshing…'
                            : 'Refresh'}
                    </button>

                    <button
                        onClick={handleExportKpis}
                        className="flex items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
                    >
                        <Download className="h-3.5 w-3.5" />

                        Export KPIs
                    </button>


                    {isAdmin ? (

                        <div className="flex items-center gap-2">

              <span className="text-xs text-[var(--color-ink-muted)]">
                Signed in as {username}
              </span>


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

                    <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">
                        Active projects
                    </p>

                    <p className="mt-1 text-2xl font-semibold text-slate-900">
                        {kpis?.activeProjects ?? '—'}
                    </p>

                </Card>


                <Card className="px-4 py-3">

                    <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">
                        Executions ({range.toLowerCase()})
                    </p>

                    <p className="mt-1 text-2xl font-semibold text-slate-900">
                        {kpis?.executionsInRange ?? '—'}
                    </p>

                </Card>


                <Card className="px-4 py-3">

                    <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">
                        Pass rate ({range.toLowerCase()})
                    </p>

                    <p className="mt-1 text-2xl font-semibold text-slate-900">
                        {
                            kpis?.passRate != null
                                ? `${kpis.passRate}%`
                                : '—'
                        }
                    </p>

                </Card>


                <Card className="px-4 py-3">

                    <p className="text-[11px] font-medium text-[var(--color-ink-muted)]">
                        Currently running
                    </p>

                    <p className="mt-1 text-2xl font-semibold text-slate-900">
                        {kpis?.currentlyRunning ?? '—'}
                    </p>

                </Card>

            </div>


            {/* Executed-by tabs + date range */}

            <div className="mb-6 flex items-center justify-between">

                <div>

                    <p className="mb-1.5 text-[11px] text-[var(--color-ink-muted)]">
                        Executed by
                    </p>


                    <div className="flex flex-wrap gap-2">

                        {['All', ...executors].map(
                            (name) => (

                                <button
                                    key={name}
                                    onClick={() => {
                                        setExecutor(name);
                                        setPage(1);
                                    }}
                                    className={
                                        `rounded-full border px-3 py-1 text-xs font-medium ${
                                            executor === name
                                                ? 'border-slate-900 bg-slate-900 text-white'
                                                : 'border-[var(--color-border)] bg-white text-[var(--color-ink-muted)] hover:bg-slate-50'
                                        }`
                                    }
                                >
                                    {name}
                                </button>

                            )
                        )}

                    </div>

                </div>


                <div className="flex items-center gap-2">

          <span className="text-[11px] text-[var(--color-ink-muted)]">
            Showing: {RANGE_LABELS[range]}
          </span>


                    <div className="flex gap-1">

                        {RANGE_OPTIONS.map(
                            (rangeOption) => (

                                <button
                                    key={rangeOption}
                                    onClick={() => {

                                        setRange(rangeOption);

                                        /*
                                         * Reset executor because available machines can
                                         * change when the dashboard date range changes.
                                         */
                                        setExecutor('All');

                                        setPage(1);
                                    }}
                                    className={
                                        `rounded-full border px-3 py-1 text-xs font-medium ${
                                            range === rangeOption
                                                ? 'border-slate-900 bg-slate-900 text-white'
                                                : 'border-[var(--color-border)] bg-white text-[var(--color-ink-muted)] hover:bg-slate-50'
                                        }`
                                    }
                                >
                                    {rangeOption}
                                </button>

                            )
                        )}

                    </div>

                </div>

            </div>


            {/* Projects header */}

            <div className="mb-3 flex items-center justify-between">

                <h2 className="text-base font-semibold text-slate-900">
                    Projects
                </h2>


                <div className="flex gap-2">

                    <div className="relative">

                        <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-ink-muted)]" />


                        <input
                            value={search}
                            onChange={(event) => {

                                setSearch(
                                    event.target.value
                                );

                                setPage(1);
                            }}
                            placeholder="Search projects"
                            className="w-64 rounded-lg border border-[var(--color-border)] bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-400"
                        />

                    </div>


                    <select
                        value={sortMode}
                        onChange={(event) =>
                            setSortMode(
                                event.target.value as SortMode
                            )
                        }
                        className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700"
                    >

                        <option value="priority">
                            Sort: Running first
                        </option>

                        <option value="recent">
                            Sort: Most recent
                        </option>

                        <option value="alpha">
                            Sort: Alphabetical
                        </option>

                    </select>

                </div>

            </div>


            {/* Project grid */}

            {loadingExecutorProjects ? (

                <Card className="py-16 text-center text-sm text-[var(--color-ink-muted)]">
                    Loading projects…
                </Card>

            ) : errorExecutorProjects ? (

                <Card className="py-16 text-center text-sm text-red-600">
                    Could not load projects for the selected executor.
                </Card>

            ) : pageItems.length === 0 ? (

                <Card className="py-16 text-center text-sm text-[var(--color-ink-muted)]">
                    No projects match your filters.
                </Card>

            ) : (

                <div className="grid grid-cols-3 gap-4">

                    {pageItems.map(
                        (card) => (

                            <ProjectCard
                                key={card.application.id}
                                data={card}
                            />

                        )
                    )}

                </div>

            )}


            <div className="mt-3 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">

        <span>

          Showing {pageItems.length} of{' '}
            {filteredSorted.length}{' '}

            project
            {
                filteredSorted.length === 1
                    ? ''
                    : 's'
            }

        </span>


                {totalPages > 1 && (

                    <div className="flex items-center gap-2">

                        <button
                            disabled={page <= 1}
                            onClick={() =>
                                setPage(
                                    (currentPage) =>
                                        currentPage - 1
                                )
                            }
                            className="rounded-md border border-[var(--color-border)] px-2 py-1 disabled:opacity-40"
                        >
                            Prev
                        </button>


                        <span>
              Page {page} of {totalPages}
            </span>


                        <button
                            disabled={page >= totalPages}
                            onClick={() =>
                                setPage(
                                    (currentPage) =>
                                        currentPage + 1
                                )
                            }
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

                    <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">
                        Total executions per day
                    </p>


                    <ResponsiveContainer
                        width="100%"
                        height={200}
                    >

                        <BarChart data={dailyTrend}>

                            <CartesianGrid
                                strokeDasharray="3 3"
                                stroke="#e2e8f0"
                                vertical={false}
                            />

                            <XAxis
                                dataKey="date"
                                tick={{ fontSize: 9 }}
                                tickFormatter={
                                    (date) =>
                                        date.slice(5)
                                }
                            />

                            <YAxis
                                tick={{ fontSize: 10 }}
                                allowDecimals={false}
                            />

                            <Tooltip />

                            <Bar
                                dataKey="executions"
                                fill="var(--color-chart-neutral)"
                                radius={[3, 3, 0, 0]}
                            />

                        </BarChart>

                    </ResponsiveContainer>

                </Card>


                <Card className="p-4">

                    <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">
                        Pass vs fail trend
                    </p>


                    <ResponsiveContainer
                        width="100%"
                        height={200}
                    >

                        <LineChart data={dailyTrend}>

                            <CartesianGrid
                                strokeDasharray="3 3"
                                stroke="#e2e8f0"
                                vertical={false}
                            />

                            <XAxis
                                dataKey="date"
                                tick={{ fontSize: 9 }}
                                tickFormatter={
                                    (date) =>
                                        date.slice(5)
                                }
                            />

                            <YAxis
                                tick={{ fontSize: 10 }}
                                allowDecimals={false}
                            />

                            <Tooltip />

                            <Line
                                type="monotone"
                                dataKey="passed"
                                stroke="var(--color-status-passed)"
                                strokeWidth={2}
                                dot={false}
                                name="Passed"
                            />

                            <Line
                                type="monotone"
                                dataKey="failed"
                                stroke="var(--color-status-failed)"
                                strokeWidth={2}
                                dot={false}
                                name="Failed"
                            />

                        </LineChart>

                    </ResponsiveContainer>

                </Card>


                <Card className="p-4">

                    <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">
                        Executions by machine
                    </p>


                    {machineData.length === 0 ? (

                        <div className="flex h-[200px] items-center justify-center text-xs text-[var(--color-ink-muted)]">
                            No data in range
                        </div>

                    ) : (

                        <ResponsiveContainer
                            width="100%"
                            height={200}
                        >

                            <BarChart
                                data={machineData}
                                layout="vertical"
                                margin={{ left: 10 }}
                            >

                                <XAxis
                                    type="number"
                                    hide
                                />

                                <YAxis
                                    type="category"
                                    dataKey="name"
                                    tick={{ fontSize: 10 }}
                                    width={110}
                                    interval={0}
                                />

                                <Tooltip />

                                <Bar
                                    dataKey="count"
                                    fill="var(--color-chart-neutral)"
                                    radius={[0, 3, 3, 0]}
                                />

                            </BarChart>

                        </ResponsiveContainer>

                    )}

                </Card>

            </div>

        </div>
    );
}