import { useEffect, useMemo, useRef } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { toBlob, toJpeg, toPng } from 'html-to-image';
import jsPDF from 'jspdf';
import * as XLSX from 'xlsx';
import {
  useApplications,
  useExecutions,
  useProjectExecutionStats,
} from '../hooks/useQueries';
import { useAuth } from '../hooks/useAuth';
import { Card, ErrorState, LoadingState } from '../components/Primitives';
import { SectionHeader } from '../components/StatCardButton';
import { KebabMenu, ExportScope } from '../components/KebabMenu';
import { ExecutionHistoryTable } from '../components/ExecutionHistoryTable';
import { formatDateTime } from '../lib/format';
import {
  RANGE_OPTIONS,
  rangeStartDate,
  type DateRangeKey,
} from '../lib/dateRange';
import { currentlyRunning } from '../lib/projectStats';
import { setCurrentProjectId } from '../lib/currentProject';
import { QuickNav } from '../components/QuickNav';

const DEFAULT_RANGE: DateRangeKey = '30D';

const EXPORT_IMAGE_OPTIONS = {
  cacheBust: true,
  pixelRatio: 2,
  backgroundColor: '#ffffff',
};

function safeFileName(value: string): string {
  return value
      .trim()
      .replace(/[^a-zA-Z0-9-_]+/g, '-')
      .replace(/^-+|-+$/g, '')
      .toLowerCase();
}

function downloadDataUrl(dataUrl: string, fileName: string) {
  const link = document.createElement('a');
  link.href = dataUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
}


function isValidRange(value: string | null): value is DateRangeKey {
  return value !== null && RANGE_OPTIONS.includes(value as DateRangeKey);
}

function rangeLabel(range: DateRangeKey): string {
  switch (range) {
    case '30D':
      return 'Last 30 days';
    case '3M':
      return 'Last 3 months';
    case '6M':
      return 'Last 6 months';
    case '1Y':
      return 'Last year';
    case 'ALL':
      return 'Lifetime';
  }
}

export function ProjectWorkspacePage() {
  const { applicationId } = useParams();
  const appId = Number(applicationId);
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { isAdmin, username, logout } = useAuth();
  const exportScopeRef = useRef<HTMLDivElement>(null);

  const requestedRange = searchParams.get('range');
  const range: DateRangeKey = isValidRange(requestedRange)
      ? requestedRange
      : DEFAULT_RANGE;

  useEffect(() => {
    if (!isValidRange(requestedRange)) {
      setSearchParams({ range: DEFAULT_RANGE }, { replace: true });
    }
  }, [requestedRange, setSearchParams]);

  const { data: applications, isLoading: loadingApps } = useApplications();
  const {
    data: allExecutions,
    isLoading: loadingExecs,
    isError: executionsError,
  } = useExecutions();

  const {
    data: projectStats,
    isLoading: loadingStats,
    isError: statsError,
  } = useProjectExecutionStats(
      Number.isFinite(appId) && appId > 0 ? appId : undefined,
      range
  );

  useEffect(() => {
    if (appId) {
      setCurrentProjectId(appId);
    }
  }, [appId]);

  const application = applications?.find((a) => a.id === appId);

  const projectExecutions = useMemo(() => {
    const executions = (allExecutions ?? []).filter(
        (execution) => execution.applicationId === appId
    );

    if (range === 'ALL') {
      return executions;
    }

    const from = rangeStartDate(range).getTime();

    return executions.filter(
        (execution) => new Date(execution.startTime).getTime() >= from
    );
  }, [allExecutions, appId, range]);

  const running = useMemo(
      () => currentlyRunning(projectExecutions),
      [projectExecutions]
  );

  const barData = useMemo(
      () =>
          (projectStats?.dailyExecutions ?? []).map((day) => ({
            date: day.date,
            passed: day.passed,
            failed: day.failed,
            running: day.running,
            total: day.passed + day.failed + day.running,
          })),
      [projectStats]
  );

  const passRateTrend = projectStats?.passRateTrend ?? [];

  const averagePassRate = useMemo(() => {
    const values = passRateTrend
        .map((point) => point.passRate)
        .filter((value): value is number => value != null);

    if (values.length === 0) {
      return null;
    }

    const average =
        values.reduce((sum, value) => sum + value, 0) / values.length;

    return Math.round(average * 10) / 10;
  }, [passRateTrend]);


  function getExportNode(): HTMLDivElement {
    if (!exportScopeRef.current) {
      throw new Error('Export scope is not available.');
    }

    return exportScopeRef.current;
  }

  async function handleCopyToClipboard() {
    try {
      const blob = await toBlob(getExportNode(), EXPORT_IMAGE_OPTIONS);

      if (!blob) {
        throw new Error('Could not create export image.');
      }

      if (!navigator.clipboard?.write || typeof ClipboardItem === 'undefined') {
        throw new Error('Image clipboard is not supported by this browser.');
      }

      await navigator.clipboard.write([
        new ClipboardItem({
          'image/png': blob,
        }),
      ]);

      alert('Project execution overview copied to clipboard.');
    } catch (error) {
      console.error(error);
      alert(
          error instanceof Error
              ? error.message
              : 'Could not copy the project execution overview.'
      );
    }
  }

  async function handleExportJpeg() {
    try {
      const dataUrl = await toJpeg(getExportNode(), {
        ...EXPORT_IMAGE_OPTIONS,
        quality: 0.95,
      });

      downloadDataUrl(
          dataUrl,
          `${safeFileName(application?.name ?? 'project')}-${range.toLowerCase()}-overview.jpg`
      );
    } catch (error) {
      console.error(error);
      alert('Could not export the project execution overview as JPEG.');
    }
  }

  async function handleExportPdf() {
    try {
      const dataUrl = await toPng(getExportNode(), EXPORT_IMAGE_OPTIONS);
      const pdf = new jsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4',
      });

      const margin = 8;
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const contentWidth = pageWidth - margin * 2;
      const contentHeight = pageHeight - margin * 2;

      const image = pdf.getImageProperties(dataUrl);
      const renderedHeight =
          (image.height * contentWidth) / image.width;

      let remainingHeight = renderedHeight;
      let positionY = margin;

      pdf.addImage(
          dataUrl,
          'PNG',
          margin,
          positionY,
          contentWidth,
          renderedHeight
      );

      remainingHeight -= contentHeight;

      while (remainingHeight > 0) {
        pdf.addPage();

        positionY =
            margin - (renderedHeight - remainingHeight);

        pdf.addImage(
            dataUrl,
            'PNG',
            margin,
            positionY,
            contentWidth,
            renderedHeight
        );

        remainingHeight -= contentHeight;
      }

      pdf.save(
          `${safeFileName(application?.name ?? 'project')}-${range.toLowerCase()}-overview.pdf`
      );
    } catch (error) {
      console.error(error);
      alert('Could not export the project execution overview as PDF.');
    }
  }

  function handleExportExcel() {
    try {
      const workbook = XLSX.utils.book_new();

      const summarySheet = XLSX.utils.aoa_to_sheet([
        ['Project', application?.name ?? 'Project'],
        ['Range', rangeLabel(range)],
        [],
        ['Metric', 'Value'],
        ['Total executions', projectStats?.totalExecutions ?? 0],
        ['Passed executions', projectStats?.passedExecutions ?? 0],
        ['Failed executions', projectStats?.failedExecutions ?? 0],
        ['In progress', projectStats?.inProgressExecutions ?? 0],
      ]);

      const dailySheet = XLSX.utils.json_to_sheet(
          barData.map((day) => ({
            Date: day.date,
            Passed: day.passed,
            Failed: day.failed,
            Running: day.running,
            Total: day.total,
          }))
      );

      const passRateSheet = XLSX.utils.json_to_sheet(
          passRateTrend.map((point) => ({
            Date: point.date,
            'Pass rate (%)': point.passRate,
          }))
      );

      XLSX.utils.book_append_sheet(workbook, summarySheet, 'Summary');
      XLSX.utils.book_append_sheet(
          workbook,
          dailySheet,
          'Executions per day'
      );
      XLSX.utils.book_append_sheet(
          workbook,
          passRateSheet,
          'Pass rate trend'
      );

      XLSX.writeFile(
          workbook,
          `${safeFileName(application?.name ?? 'project')}-${range.toLowerCase()}-overview.xlsx`
      );
    } catch (error) {
      console.error(error);
      alert('Could not export the project execution overview as Excel.');
    }
  }

  function changeRange(nextRange: DateRangeKey) {
    setSearchParams({ range: nextRange });
  }

  function changeProject(nextProjectId: string) {
    navigate(`/projects/${nextProjectId}?range=${range}`);
  }

  if (loadingApps || loadingExecs || loadingStats) {
    return <LoadingState label="Loading project…" />;
  }

  if (
      executionsError ||
      statsError ||
      !application ||
      !projectStats
  ) {
    return <ErrorState message="Could not load this project." />;
  }

  return (
      <div className="mx-auto max-w-[1280px] px-8 py-6">
        <div className="mb-1 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">
          <div>
            <Link to="/" className="hover:underline">
              Projects
            </Link>{' '}
            / {application.name}
          </div>
          <QuickNav />
        </div>

        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-semibold tracking-tight text-slate-900">
              {application.name}
            </h1>

            <select
                value={appId}
                onChange={(e) => changeProject(e.target.value)}
                className="rounded-lg border border-[var(--color-border)] bg-white px-2.5 py-1 text-xs font-medium text-slate-700"
            >
              {applications?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.name}
                  </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2">
            <KebabMenu
                items={[
                  {
                    label: 'Copy to clipboard',
                    onClick: handleCopyToClipboard,
                  },
                  {
                    label: 'Export as JPEG',
                    onClick: handleExportJpeg,
                  },
                  {
                    label: 'Export as PDF',
                    onClick: handleExportPdf,
                  },
                  {
                    label: 'Export as Excel',
                    onClick: handleExportExcel,
                  },
                ]}
            />

            {isAdmin ? (
                <div className="flex items-center gap-2">
              <span className="text-xs text-[var(--color-ink-muted)]">
                Signed in as {username}
              </span>
                  <button
                      onClick={logout}
                      className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
                  >
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

        <ExportScope>
          <div ref={exportScopeRef} className="bg-white">
            <div className="mb-4 flex items-center justify-between">
              <SectionHeader title="Executions overview" />

              <div className="flex rounded-lg border border-[var(--color-border)] bg-white p-0.5 text-xs">
                {RANGE_OPTIONS.map((option) => (
                    <button
                        key={option}
                        type="button"
                        onClick={() => changeRange(option)}
                        className={`rounded-md px-2.5 py-1 font-medium ${
                            range === option
                                ? 'bg-slate-900 text-white'
                                : 'text-[var(--color-ink-muted)] hover:bg-slate-50'
                        }`}
                    >
                      {option}
                    </button>
                ))}
              </div>
            </div>

            <p className="-mt-2 mb-4 text-xs text-[var(--color-ink-muted)]">
              {rangeLabel(range)}
            </p>

            <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
              <ProjectKpiCard
                  label="Total executions"
                  value={projectStats.totalExecutions}
              />
              <ProjectKpiCard
                  label="Passed executions"
                  value={projectStats.passedExecutions}
                  tone="text-[var(--color-status-passed)]"
              />
              <ProjectKpiCard
                  label="Failed executions"
                  value={projectStats.failedExecutions}
                  tone="text-[var(--color-status-failed)]"
              />
              <ProjectKpiCard
                  label="In progress"
                  value={projectStats.inProgressExecutions}
                  tone="text-[var(--color-status-progress)]"
              />
            </div>

            <div className="mb-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
              <Card className="bg-slate-50 p-4">
                <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">
                  Executions per day
                </p>

                {barData.length === 0 ? (
                    <p className="py-16 text-center text-xs text-[var(--color-ink-muted)]">
                      No executions in this range
                    </p>
                ) : (
                    <ResponsiveContainer width="100%" height={180}>
                      <BarChart data={barData}>
                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="#e2e8f0"
                            vertical={false}
                        />
                        <XAxis
                            dataKey="date"
                            tick={{ fontSize: 9 }}
                            tickFormatter={(date) => date.slice(5)}
                        />
                        <YAxis tick={{ fontSize: 10 }} allowDecimals={false} />
                        <Tooltip />
                        <Legend
                            verticalAlign="bottom"
                            align="left"
                            iconType="circle"
                            iconSize={7}
                            wrapperStyle={{ fontSize: 10 }}
                        />
                        <Bar
                            dataKey="passed"
                            name="Passed"
                            stackId="executions"
                            fill="var(--color-status-passed)"
                        />
                        <Bar
                            dataKey="failed"
                            name="Failed"
                            stackId="executions"
                            fill="var(--color-status-failed)"
                        />
                        <Bar
                            dataKey="running"
                            name="Running"
                            stackId="executions"
                            fill="var(--color-status-progress)"
                        />
                      </BarChart>
                    </ResponsiveContainer>
                )}
              </Card>

              <Card className="bg-slate-50 p-4">
                <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">
                  Pass rate trend
                </p>

                {passRateTrend.length === 0 ? (
                    <p className="py-16 text-center text-xs text-[var(--color-ink-muted)]">
                      No executions in this range
                    </p>
                ) : (
                    <ResponsiveContainer width="100%" height={180}>
                      <LineChart data={passRateTrend}>
                        <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="#e2e8f0"
                            vertical={false}
                        />
                        <XAxis
                            dataKey="date"
                            tick={{ fontSize: 9 }}
                            tickFormatter={(date) => date.slice(5)}
                        />
                        <YAxis
                            tick={{ fontSize: 10 }}
                            domain={[0, 100]}
                        />
                        <Tooltip />
                        <Line
                            type="monotone"
                            dataKey="passRate"
                            stroke="var(--color-status-passed)"
                            strokeWidth={2}
                            dot={false}
                            connectNulls
                        />
                      </LineChart>
                    </ResponsiveContainer>
                )}

                {averagePassRate != null && (
                    <p className="mt-1 text-[10px] font-medium text-[var(--color-status-passed)]">
                      {averagePassRate}% average
                    </p>
                )}
              </Card>
            </div>
          </div>
        </ExportScope>

        <div className="mt-6">
          <SectionHeader title="Currently running" />

          {running.length === 0 ? (
              <Card className="py-8 text-center text-sm text-[var(--color-ink-muted)]">
                No executions currently running for {application.name}.
              </Card>
          ) : (
              <div className="mb-2 space-y-2">
                {running.map((execution) => (
                    <Link
                        key={execution.id}
                        to={`/executions/${execution.id}`}
                        className="flex items-center justify-between rounded-xl border border-blue-300 bg-white px-4 py-3 hover:bg-slate-50"
                    >
                      <div>
                        <p className="text-sm font-medium text-blue-700">
                          {execution.name}
                        </p>
                        <p className="mt-0.5 text-xs text-[var(--color-ink-muted)]">
                          {execution.environmentName ?? '—'} · started{' '}
                          {formatDateTime(execution.startTime)} ·{' '}
                          {execution.executedTestCaseCount ?? 0} of{' '}
                          {execution.targetedTestCaseCount ?? 0} executed
                        </p>
                      </div>

                      <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">
                  In Progress
                </span>
                    </Link>
                ))}
              </div>
          )}
        </div>

        <div className="mt-6">
          <SectionHeader title="All executions" />
          <ExecutionHistoryTable executions={projectExecutions} />
        </div>
      </div>
  );
}

function ProjectKpiCard({
                          label,
                          value,
                          tone = 'text-slate-900',
                        }: {
  label: string;
  value: number;
  tone?: string;
}) {
  return (
      <Card className="p-4">
        <p className="text-xs font-medium text-[var(--color-ink-muted)]">
          {label}
        </p>
        <p className={`mt-2 text-2xl font-semibold tracking-tight ${tone}`}>
          {value}
        </p>
      </Card>
  );
}
