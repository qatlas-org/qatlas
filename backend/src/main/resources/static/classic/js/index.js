/* =============================================================
   UPGRADED index.js — uses Chart.js 4.x (NOT Highcharts)
   If you see 'Highcharts is not defined', you are running the
   OLD index.js. Replace it with THIS file from the upgraded zip.
   ============================================================= */
if (typeof Highcharts !== 'undefined') {
  console.warn('[index.js] WARNING: Old Highcharts library detected. This file uses Chart.js — Highcharts is no longer needed.');
}

/* ============================================================
   AUTH CONFIG  –  change these credentials as needed
   ============================================================ */
const AUTH_CONFIG = {
  username : 'admin',
  password : 'admin@123'
};

// Session flag: once verified per page load, no re-prompt
let authVerified = false;

// Callback stored while auth modal is open
let authSuccessCallback = null;

/**
 * requireAuth(callback)
 * If already authenticated this session, run callback immediately.
 * Otherwise, show the auth modal and run callback on success.
 */
function requireAuth(callback) {
  if (authVerified) { callback(); return; }
  authSuccessCallback = callback;
  // Reset modal state
  $('#authUsername').val('');
  $('#authPassword').val('');
  $('#authError').hide();
  $('#authSubmitBtn').prop('disabled', false);
  $('#authBtnText').text('Verify');
  $('#authModal').modal('show');
  setTimeout(function() { $('#authUsername').focus(); }, 400);
}

function submitAuth() {
  const u = ($('#authUsername').val() || '').trim();
  const p = ($('#authPassword').val() || '');
  if (u === AUTH_CONFIG.username && p === AUTH_CONFIG.password) {
    authVerified = true;
    $('#authModal').modal('hide');
    $('#authError').hide();
    if (typeof authSuccessCallback === 'function') {
      const cb = authSuccessCallback;
      authSuccessCallback = null;
      cb();
    }
  } else {
    $('#authError').show();
    $('#authPassword').val('').focus();
    // Shake the modal
    const mc = document.querySelector('#authModal .modal-content');
    mc.style.animation = 'none';
    mc.offsetHeight; // reflow
    mc.style.animation = 'authShake 0.4s ease';
  }
}

function cancelAuth() {
  authSuccessCallback = null;
  $('#authModal').modal('hide');
}

function toggleAuthPwd() {
  const inp = document.getElementById('authPassword');
  const icon = document.getElementById('eyeIcon');
  if (inp.type === 'password') {
    inp.type = 'text';
    icon.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>';
  } else {
    inp.type = 'password';
    icon.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
  }
}

/* Inject shake keyframe once */
(function() {
  const s = document.createElement('style');
  s.textContent = '@keyframes authShake{0%,100%{transform:translateX(0)}20%{transform:translateX(-8px)}40%{transform:translateX(8px)}60%{transform:translateX(-5px)}80%{transform:translateX(5px)}}';
  document.head.appendChild(s);
})();

/* ============================================================
   index.js  –  Modern Chart.js replacements for all dashboard charts
   ============================================================ */

// ── chart instances (kept so we can destroy & re-draw) ──────────────────────
let barChartInstance      = null;
let pieChartInstance      = null;
let lineChartInstance     = null;
let scenarioChartInstance = null;

// ── palette ─────────────────────────────────────────────────────────────────
const COLORS = {
  passed    : { solid: '#22c55e', light: 'rgba(34,197,94,0.12)'   },
  failed    : { solid: '#ef4444', light: 'rgba(239,68,68,0.12)'   },
  inprogress: { solid: '#f59e0b', light: 'rgba(245,158,11,0.12)'  },
  targeted  : { solid: '#4b7bec', light: 'rgba(75,123,236,0.12)'  },
  executed  : { solid: '#7c3aed', light: 'rgba(124,58,237,0.12)'  },
  skipped   : { solid: '#94a3b8', light: 'rgba(148,163,184,0.12)' },
};

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1,3),16), g = parseInt(hex.slice(3,5),16), b = parseInt(hex.slice(5,7),16);
  return `rgba(${r},${g},${b},${alpha})`;
}

function applyGlobalDefaults() {
  Chart.defaults.font.family = "'DM Sans', sans-serif";
  Chart.defaults.font.size   = 12;
  Chart.defaults.color       = '#64748b';
  Chart.defaults.plugins.legend.labels.usePointStyle = true;
  Chart.defaults.plugins.legend.labels.padding       = 16;
}

const tooltipPlugin = {
  backgroundColor: '#1e293b', titleColor: '#f8fafc', bodyColor: '#cbd5e1',
  borderColor: 'rgba(255,255,255,0.08)', borderWidth: 1,
  padding: 12, cornerRadius: 10, displayColors: true, boxPadding: 4,
};

// ── ensureCanvas ─────────────────────────────────────────────────────────────
function ensureCanvas(containerId, height) {
  const container = document.getElementById(containerId);
  if (!container) return null;
  if (container.tagName === 'CANVAS') return container;
  container.innerHTML = '';
  const canvas = document.createElement('canvas');
  canvas.id = containerId + '_canvas';
  container.style.cssText += ';position:relative;';
  container.appendChild(canvas);
  return canvas;
}

// ── 1. BAR CHART ─────────────────────────────────────────────────────────────
function loadBarChart(testExecution) {
  if (barChartInstance) { barChartInstance.destroy(); barChartInstance = null; }
  const canvas = ensureCanvas('chartContainer', 300);
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  barChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Passed', 'Failed', 'In Progress'],
      datasets: [{
        label: 'Count',
        data : [testExecution.passedTestCaseCount||0, testExecution.failedTestCaseCount||0, testExecution.inProgressTestCaseCount||0],
        backgroundColor: [hexToRgba(COLORS.passed.solid,0.85), hexToRgba(COLORS.failed.solid,0.85), hexToRgba(COLORS.inprogress.solid,0.85)],
        borderColor    : [COLORS.passed.solid, COLORS.failed.solid, COLORS.inprogress.solid],
        borderWidth: 0, borderRadius: 10, borderSkipped: false, barPercentage: 0.5,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      animation: { duration: 900, easing: 'easeOutQuart' },
      onClick(evt, elements) {
        if (elements.length > 0) {
          const idx = elements[0].index;
          const statusMap = ['PASSED', 'FAILED', 'PROGRESS'];
          const url = "ExecutionSummary.html?executionId="+executionId+"&page="+page+"&suite="+suite+"&status="+statusMap[idx];
          location.href = url;
        }
      },
      onHover: (evt, elements) => {
        evt.native.target.style.cursor = elements.length ? 'pointer' : 'default';
      },
      plugins: {
        legend: { display: false },
        tooltip: { ...tooltipPlugin, callbacks: { label: c => `  ${c.parsed.y} test cases` } }
      },
      scales: {
        x: { grid: { display: false }, border: { display: false }, ticks: { font: { weight:'600', size:12 }, color:'#475569' } },
        y: { grid: { color:'rgba(0,0,0,0.04)', drawTicks: false }, border: { display: false }, ticks: { padding: 8, stepSize: 1 }, beginAtZero: true }
      }
    }
  });
}

// ── 2. DONUT CHART ───────────────────────────────────────────────────────────
function loadPieChart(testExecution) {
  if (pieChartInstance) { pieChartInstance.destroy(); pieChartInstance = null; }
  const canvas = ensureCanvas('morris', 300);
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const total  = testExecution.targetedTestCaseCount || 1;
  const passed = testExecution.passedTestCaseCount   || 0;
  const failed = testExecution.failedTestCaseCount   || 0;
  const inprog = testExecution.inProgressTestCaseCount || 0;
  const pct = v => +((v/total)*100).toFixed(1);

  pieChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Passed','Failed','In Progress'],
      datasets: [{
        data: [passed, failed, inprog],
        backgroundColor: [hexToRgba(COLORS.passed.solid,0.88), hexToRgba(COLORS.failed.solid,0.88), hexToRgba(COLORS.inprogress.solid,0.88)],
        borderColor: ['#fff','#fff','#fff'], borderWidth: 3, hoverOffset: 8,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false, cutout: '68%',
      animation: { animateRotate: true, duration: 900, easing: 'easeOutQuart' },
      onClick(evt, elements) {
        if (elements.length > 0) {
          const idx = elements[0].index;
          const statusMap = ['PASSED', 'FAILED', 'PROGRESS'];
          const url = "ExecutionSummary.html?executionId="+executionId+"&page="+page+"&suite="+suite+"&status="+statusMap[idx];
          location.href = url;
        }
      },
      onHover: (evt, elements) => {
        evt.native.target.style.cursor = elements.length ? 'pointer' : 'default';
      },
      plugins: {
        legend: { position:'bottom', labels:{ padding:18, font:{ weight:'500' } } },
        tooltip: { ...tooltipPlugin, callbacks: { label: c => `  ${c.label}: ${pct(c.raw)}%  (${c.raw})` } }
      }
    },
    plugins: [{
      id: 'centreLabel',
      afterDraw(chart) {
        const { ctx, chartArea: { width, height, left, top } } = chart;
        const cx = left + width/2, cy = top + height/2 - 14;
        const passRate = pct(passed);
        ctx.save();
        ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
        ctx.font = "700 26px 'DM Sans', sans-serif";
        ctx.fillStyle = passed/total >= 0.7 ? COLORS.passed.solid : COLORS.failed.solid;
        ctx.fillText(passRate + '%', cx, cy);
        ctx.font = "500 11px 'DM Sans', sans-serif"; ctx.fillStyle = '#94a3b8';
        ctx.fillText('pass rate', cx, cy + 22);
        ctx.restore();
      }
    }]
  });
}

// ── 3. LINE CHART ────────────────────────────────────────────────────────────
function loadLineChart(testRuns) {
  if (lineChartInstance) { lineChartInstance.destroy(); lineChartInstance = null; }
  const canvas = ensureCanvas('LineHighChart', 300);
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  const last10 = testRuns.slice(0,10).reverse();

  const mkDataset = (label, key, colorKey, hidden) => ({
    label, hidden: !!hidden,
    data: last10.map(r => parseInt(r[key])||0),
    borderColor: COLORS[colorKey].solid,
    backgroundColor: 'transparent',
    borderWidth: 2.5,
    pointRadius: 4, pointHoverRadius: 6,
    pointBackgroundColor: COLORS[colorKey].solid,
    pointBorderColor: '#fff', pointBorderWidth: 2,
    tension: 0.4, fill: false,
  });

  lineChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: last10.map(r => r.name || ''),
      datasets: [
        mkDataset('Targeted',    'targetedTestCaseCount',   'targeted'),
        mkDataset('Executed',    'executedTestCaseCount',   'executed'),
        mkDataset('Passed',      'passedTestCaseCount',     'passed'),
        mkDataset('Failed',      'failedTestCaseCount',     'failed'),
        mkDataset('Skipped',     'skippedTestCaseCount',    'skipped',     true),
        mkDataset('In Progress', 'inProgressTestCaseCount', 'inprogress',  true),
      ]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      animation: { duration: 1000, easing: 'easeOutQuart' },
      plugins: {
        legend: { position: 'bottom', labels: { padding: 14, font: { size: 11, weight: '500' } } },
        tooltip: {
          ...tooltipPlugin,
          callbacks: {
            title: items => {
              const run = last10[items[0].dataIndex];
              return run ? new Date(run.startTime).toLocaleDateString() : items[0].label;
            },
            afterTitle: items => {
              const run = last10[items[0].dataIndex];
              return run ? 'Run: ' + run.name : '';
            }
          }
        }
      },
      scales: {
        x: { grid: { display: false }, border: { display: false }, ticks: { maxRotation: 30, font: { size: 11 } } },
        y: { grid: { color: 'rgba(0,0,0,0.04)', drawTicks: false }, border: { display: false }, ticks: { padding: 8 }, beginAtZero: true }
      }
    }
  });
}

// ── 4. SCENARIO BAR CHART ────────────────────────────────────────────────────
function loadTestCasesBarChart(testCases) {
  if (scenarioChartInstance) { scenarioChartInstance.destroy(); scenarioChartInstance = null; }
  const container = document.getElementById('container');
  if (!container) return;
  container.innerHTML = '<div style="position:relative;height:320px;width:100%;"><canvas id="scenarioCanvas"></canvas></div>';
  const canvas = document.getElementById('scenarioCanvas');
  const ctx = canvas.getContext('2d');

  const bgColors = testCases.map(e => {
    const s = statuses.get(e.executionStatus);
    return s ? hexToRgba(s.colorCode, 0.82) : hexToRgba('#94a3b8', 0.82);
  });

  scenarioChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: testCases.map(e => '#' + e.id),
      datasets: [{
        label: 'Execution Time (s)',
        data : testCases.map(e => +(e.executionTime/1000).toFixed(2)),
        backgroundColor: bgColors,
        borderColor    : testCases.map(e => { const s = statuses.get(e.executionStatus); return s ? s.colorCode : '#94a3b8'; }),
        borderWidth: 0, borderRadius: 8, borderSkipped: false, barPercentage: 0.65,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      animation: { duration: 800, easing: 'easeOutQuart' },
      onClick(evt, elements) {
        if (elements.length > 0) {
          const tc = testCases[elements[0].index];
          if (tc) location.href = '/classic/DetailReport.html?testCaseId=' + tc.id;
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          ...tooltipPlugin,
          callbacks: {
            title: items => { const tc = testCases[items[0].dataIndex]; return tc ? '#' + tc.id + ' – ' + tc.name : items[0].label; },
            label : c => {
              const tc = testCases[c.dataIndex];
              const st = statuses.get(tc && tc.executionStatus);
              return ['  Time: ' + c.parsed.y + ' s', '  Status: ' + (st ? st.name : (tc ? tc.executionStatus : '—'))];
            }
          }
        }
      },
      scales: {
        x: { grid: { display: false }, border: { display: false }, ticks: { font: { size: 10 }, maxRotation: 45 } },
        y: {
          grid: { color: 'rgba(0,0,0,0.04)', drawTicks: false }, border: { display: false }, ticks: { padding: 8 },
          beginAtZero: true, title: { display: true, text: 'Seconds', font: { size: 11 }, color: '#94a3b8' }
        }
      }
    }
  });

  // Hide old 3D slider panel
  const sliders = document.getElementById('sliders');
  if (sliders) sliders.style.display = 'none';
}

// ── Data & grid helpers (identical logic to original) ────────────────────────
function getFolderPath(filePath) {
  return filePath.substring(filePath.indexOf("/")+1, filePath.lastIndexOf("/"));
}
function dateFormatter(cellValue) { return new Date(cellValue).toLocaleString(); }

/* Precisely measures rendered text width using the grid header's actual
   font, via an off-screen canvas (fast, exact, no DOM insertion needed). */
function measureTextWidth(text, font) {
  const canvas = measureTextWidth._canvas || (measureTextWidth._canvas = document.createElement('canvas'));
  const ctx = canvas.getContext('2d');
  ctx.font = font;
  return ctx.measureText(text).width;
}

/* Computes exact column widths on the fly:
   - Targeted/Executed/Passed/Failed/Skipped/In-Progress/Actions: sized
     to exactly fit their own header label text, nothing more.
   - Date: sized to exactly fit the widest realistic rendered date/time
     string (worst case: 2-digit month/day/hour, e.g. "12/31/2026,
     12:59:59 PM"), so real values never truncate.
   - Application/Release/Run: whatever space is left in the grid's
     container after the above (plus the checkbox column jqGrid manages
     itself) is split evenly three ways between them. */
function computeGridColumnWidths() {
  const headerFont =
      '600 12px "Lucida Grande", "Lucida Sans", Arial, sans-serif';
  const cellChrome = 12 + 1 + 4;
  const fit = label =>
      Math.ceil(measureTextWidth(label, headerFont)) + cellChrome;
  const widestDateSample =
      new Date(2026, 11, 31, 12, 59, 59).toLocaleString();
  const widths = {
    date:       fit(widestDateSample),
    targetted:  fit('Targeted'),
    executed:   fit('Executed'),
    passed:     fit('Passed'),
    failed:     fit('Failed'),
    skipped:    fit('Skipped'),
    inProgress: fit('In-Progress'),
    // The Actions column renders 3 icons per row (archive,
    // download attachments, export report) — not just the word
    // "Actions". Sizing it to fit only the header text left the
    // third icon (download) clipped/cut off, as seen on screen.
    // ~26px per icon (icon glyph + its own margin) covers all 3
    // comfortably; still take whichever is larger in case the
    // header text itself ever needs more room.
    actions:    Math.max(fit('Actions'), (26 * 3) + cellChrome)
  };
  /*
   * jqGrid creates this checkbox column automatically.
   */
  const checkboxWidth = 25;
  /*
   * Get the width available to the grid.
   */
  let containerWidth =
      $('#chartHistory').parent().innerWidth();
  if (!containerWidth) {
    containerWidth = $(window).width();
  }
  /*
   * Fixed-width columns.
   */
  const fixedWidth =
      checkboxWidth +
      widths.date +
      widths.targetted +
      widths.executed +
      widths.passed +
      widths.failed +
      widths.skipped +
      widths.inProgress +
      widths.actions;
  /*
   * Application gets a smaller share than Release/Run; the width
   * freed up by shrinking it is split evenly between Release and
   * Run. Adjust APPLICATION_WIDTH_SHARE to make Application
   * narrower/wider (it's a fraction of the combined
   * Application+Release+Run width).
   */
  const remainingWidth =
      Math.max(0, containerWidth - fixedWidth);
  const APPLICATION_WIDTH_SHARE = 0.20;
  widths.application =
      Math.max(90, Math.floor(remainingWidth * APPLICATION_WIDTH_SHARE));
  const releaseRunWidth =
      Math.max(100, Math.floor((remainingWidth - widths.application) / 2));
  widths.release = releaseRunWidth;
  widths.run = releaseRunWidth;
  /*
   * Total width of the complete grid.
   */
  widths.total =
      checkboxWidth +
      widths.date +
      widths.application +
      widths.release +
      widths.run +
      widths.targetted +
      widths.executed +
      widths.passed +
      widths.failed +
      widths.skipped +
      widths.inProgress +
      widths.actions;
  return widths;
}

/* ── FORCE HEADER/BODY COLUMN SYNC ────────────────────────────────────────
   jqGrid renders the column headers and the data rows as two entirely
   separate <table> elements. Normally jqGrid keeps their column widths
   identical internally, but various things (custom CSS, font loading,
   filterToolbar() rebuilding the header row's DOM after initial layout,
   version-specific quirks, etc.) can throw that off, leaving the header
   grid lines visually offset from the data underneath.

   Rather than trying to predict the correct width in advance, this reads
   the ACTUAL rendered width of each header cell and each body cell for
   the same column — paired safely by jqGrid's own id/aria-describedby
   attributes rather than raw position, so it's unaffected by any extra
   spacer columns — and forces both to one identical pixel value. Because
   it's copying real, already-rendered values instead of predicting them,
   it stays correct regardless of what caused the drift. ─────────────────── */
function forceHeaderBodyColumnSync() {
  var $bodyTable = $('#chartHistory');
  var $headerRow = $('#gview_chartHistory').find('tr.ui-jqgrid-labels').first();
  if (!$headerRow.length) return;

  $headerRow.children('th').each(function() {
    var $th = $(this);
    var id = $th.attr('id');
    if (!id) return;
    var $tds = $bodyTable.find('td[aria-describedby="' + id + '"]');
    if (!$tds.length) return;
    var w = Math.max($th.outerWidth(), $tds.first().outerWidth());
    $th.add($tds).css({ width: w, minWidth: w, maxWidth: w, boxSizing: 'border-box' });
  });
}

function loadTestRunHistoryTable(testRuns) {

  const colWidths = computeGridColumnWidths();

  $("#chartHistory").jqGrid({

    datatype: 'local',
    data: testRuns,

    /*
     * IMPORTANT:
     * Do not allow jqGrid to automatically resize
     * the columns.
     */
    autowidth: false,
    width: colWidths.total,
    shrinkToFit: false,

    multiselect: true,
    multiboxonly: false,

    /*
     * COLUMN ORDER:
     *
     * [checkbox]
     * Date
     * Application
     * Release
     * Run
     * Targeted
     * Executed
     * Passed
     * Failed
     * Skipped
     * In-Progress
     * Actions
     */

    colModel: [

      // 1. Date
      {
        label: "Date",
        name: "startTime",
        align: "center",
        width: colWidths.date,
        formatter: dateFormatter,
        search: false
      },

      // 2. Application
      {
        label: "Application",
        name: "applicationName",
        align: "center",
        width: colWidths.application,
        stype: "select",

        searchoptions: {
          defaultValue: application,
          autosearch: true,

          dataEvents: [{
            type: "change",

            fn: function(e) {

              application =
                  e.currentTarget.value;

              resetReleaseOptions(testRuns);
              resetRunOptions(testRuns);
              updateTabsURLs();
            }
          }],

          dataInit: function(el) {

            var current = application;
            var $el = $(el);
            var $options = $el.find("option");
            var $match = $options.filter(function() {
              var val = $(this).val();
              var text = $(this).text();
              if (!current || current === "ALL" || current === "[ALL]") {
                return val === "" || text === "ALL" || text === "[ALL]";
              }
              return val === current || text === current;
            });
            if ($match.length) {
              $options.prop("selected", false);
              $match.first().prop("selected", true);
            }

            setTimeout(function() {
              $el.trigger("change");
            }, 500);
          },

          value:
              ":[ALL];" +
              Array.from(
                  new Set(
                      getApplicationOptions(testRuns)
                          .map(function(e) {
                            return e.applicationName +
                                ":" +
                                e.applicationName;
                          })
                  )
              ).join(";")
        }
      },

      // 3. Release
      {
        label: "Release",
        name: "applicationVersion",
        align: "center",
        width: colWidths.release,
        stype: "select",

        searchoptions: {
          defaultValue: release,
          autosearch: true,

          dataEvents: [{
            type: "change",

            fn: function(e) {

              release =
                  e.currentTarget.value;

              resetRunOptions(testRuns);
              updateTabsURLs();
            }
          }],

          dataInit: function(el) {

            var current = release;
            var $el = $(el);
            var $options = $el.find("option");
            var $match = $options.filter(function() {
              var val = $(this).val();
              var text = $(this).text();
              if (!current || current === "ALL" || current === "[ALL]") {
                return val === "" || text === "ALL" || text === "[ALL]";
              }
              return val === current || text === current;
            });
            if ($match.length) {
              $options.prop("selected", false);
              $match.first().prop("selected", true);
            }

            setTimeout(function() {
              $el.trigger("change");
            }, 500);
          },

          value:
              ":[ALL];" +
              Array.from(
                  new Set(
                      getReleaseOptions(testRuns)
                          .map(function(e) {
                            return e.applicationVersion +
                                ":" +
                                e.applicationVersion;
                          })
                  )
              ).join(";")
        }
      },

      // 4. Run
      {
        label: "Run",
        name: "name",
        align: "center",
        width: colWidths.run,
        stype: "select",

        searchoptions: {
          defaultValue: execution,
          autosearch: true,

          dataEvents: [{
            type: "change",

            fn: function(e) {

              execution =
                  e.currentTarget.value;

              updateTabsURLs();
            }
          }],

          dataInit: function(el) {

            var current = execution;
            var $el = $(el);
            var $options = $el.find("option");
            var $match = $options.filter(function() {
              var val = $(this).val();
              var text = $(this).text();
              if (!current || current === "ALL" || current === "[ALL]") {
                return val === "" || text === "ALL" || text === "[ALL]";
              }
              return val === current || text === current;
            });
            if ($match.length) {
              $options.prop("selected", false);
              $match.first().prop("selected", true);
            }

            setTimeout(function() {
              $el.trigger("change");
            }, 500);
          },

          value:
              ":[ALL];" +
              Array.from(
                  new Set(
                      getRunOptions(testRuns)
                          .map(function(e) {
                            return e.name + ":" + e.name;
                          })
                  )
              ).join(";")
        }
      },

      // 5. Targeted
      {
        label: "Targeted",
        name: "targetedTestCaseCount",
        align: "center",
        width: colWidths.targetted,
        search: false
      },

      // 6. Executed
      {
        label: "Executed",
        name: "executedTestCaseCount",
        align: "center",
        width: colWidths.executed,
        search: false
      },

      // 7. Passed
      {
        label: "Passed",
        name: "passedTestCaseCount",
        align: "center",
        width: colWidths.passed,
        search: false
      },

      // 8. Failed
      {
        label: "Failed",
        name: "failedTestCaseCount",
        align: "center",
        width: colWidths.failed,
        search: false
      },

      // 9. Skipped
      {
        label: "Skipped",
        name: "skippedTestCaseCount",
        align: "center",
        width: colWidths.skipped,
        search: false
      },

      // 10. In-Progress
      {
        label: "In-Progress",
        name: "inProgressTestCaseCount",
        align: "center",
        width: colWidths.inProgress,
        search: false
      },

      // 11. Actions
      {
        label: "Actions",
        name: "actions",
        align: "center",
        width: colWidths.actions,
        search: false,
        formatter: historyActionsFormatter
      }
    ],

    loadonce: true,
    viewrecords: true,
    align: "center",
    height: 390,
    rowNum: 10,
    pager: "#gridpager",
    multiPageSelection: true,

    loadComplete: function() {

      /*
       * Create the filter row.
       */
      $("#chartHistory").jqGrid("filterToolbar");

      /*
       * Existing functionality.
       */
      initNavGrid();
      initActions();
      injectSelectAllCheckbox();

      /*
       * IMPORTANT:
       * Keep the column widths calculated by colModel.
       * Do NOT apply table-layout: fixed here.
       *
       * The header and body are separate jqGrid tables.
       * jqGrid itself will synchronize their column widths.
       */
      $("#chartHistory").jqGrid(
          "setGridWidth",
          colWidths.total,
          false
      );

      /*
       * FINAL ALIGNMENT CORRECTION:
       * Even with colModel widths and setGridWidth applied above,
       * jqGrid's header <table> and body <table> can still end up
       * with mismatched actual rendered widths (browser rounding,
       * filter dropdowns, font metrics, etc). Rather than predict
       * the correct width, forceHeaderBodyColumnSync() reads the
       * REAL rendered width of each header cell and each body
       * cell and forces them to match exactly. This must run on
       * initial load, not just on resize.
       */
      setTimeout(forceHeaderBodyColumnSync, 0);
      if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(forceHeaderBodyColumnSync);
      }
    },

    onSelectRow: function(id) {

      if (
          !event.target ||
          (
              event.target.type !== "checkbox" &&
              !$(event.target).hasClass("fas")
          )
      ) {

        location.href =
            "/classic/index.html?executionId=" + id;

      } else if (
          $(event.target).hasClass("fas")
      ) {

        $("#chartHistory").resetSelection();
      }
    }
  });
}

function getTestRunHistory(){
  $.get("/rs/test-execution",function(data){
    loadTestRunHistoryTable(data);loadLineChart(data);
    const latest=data&&data.length>0?data[0]:null;getTestRunDetails(latest);
  });
}
function getTestCases(testExecutionId){
  $.ajax({url:"/rs/test-execution/"+testExecutionId+"/test-cases?status=PASSED&status=FAILED&status=WARNING",method:"GET",context:document.body})
      .done(r=>loadTestCasesBarChart(r));
}
function loadTestRunDetails(testExecution){
  setExecutionDetails(testExecution);
  const url="ExecutionSummary.html?executionId="+executionId+"&page="+page+"&suite="+suite+"&status=";
  $("#targetted").html(parseInteger(testExecution.targetedTestCaseCount)).parent().attr("href",url);
  const p=parseInteger(testExecution.passedTestCaseCount);    $("#passed").html(p).parent().attr("href",p>0?url+"PASSED":"#");
  const f=parseInteger(testExecution.failedTestCaseCount);    $("#failed").html(f).parent().attr("href",f>0?url+"FAILED":"#");
  const ip=parseInteger(testExecution.inProgressTestCaseCount);$("#inprogress").html(ip).parent().attr("href",ip>0?url+"PROGRESS":"#");
  $(".inprog-icon").toggleClass("is-spinning", ip > 0);
  const ex=parseInteger(testExecution.executedTestCaseCount); $("#executed").html(ex).parent().attr("href",ex>0?url+"EXECUTED":"#");
  $("#skipped").html(parseInteger(testExecution.skippedTestCaseCount)).parent().attr("href","#");
  loadBarChart(testExecution);loadPieChart(testExecution);getTestCases(testExecution.id);
}
function getTestRunDetails(latestTestRun){
  if(!latestTestRun)return;
  if(executionId!=null&&executionId!=latestTestRun.id){
    $.ajax({url:"/rs/test-execution/"+executionId,method:"GET",context:document.body})
        .done(r=>loadTestRunDetails(r))
        .fail(()=>{
          // The execution the URL points to no longer exists (e.g. it was
          // archived/deleted). Fall back to the latest one instead of
          // leaving the page silently showing stale/empty details forever.
          executionId=latestTestRun.id;updateTabsURLs();loadTestRunDetails(latestTestRun);
        });
  }else{executionId=latestTestRun.id;updateTabsURLs();loadTestRunDetails(latestTestRun);}
}

$(document).ready(function(){
  applyGlobalDefaults();
  setInterval(()=>window.location.reload(),300000);
  updateTabsURLs();getTestRunHistory();
  var totalJQGridPages=$("#chartHistory").jqGrid('getGridParam','lastpage');
  $("span.ui-icon").click(function(){
    var btn=$(this),oldPage=page;
    if(btn.hasClass('ui-icon-seek-first'))page=1;
    else if(btn.hasClass('ui-icon-seek-prev')&&page>1)page=parseInt(page)-1;
    else if(btn.hasClass('ui-icon-seek-next')&&page<totalJQGridPages)page=parseInt(page)+1;
    else if(btn.hasClass('ui-icon-seek-end'))page=totalJQGridPages;
    ChangeUrl('Page'+page,window.location.href.replace("page="+oldPage,"page="+page));
  });
  $('#MainReport').jqGrid('filterToolbar');
  $("span.glyphicon-trash").click(function(){
    var fp=$(this).attr("reportpath");
    $.ajax({url:"/delete-folder",method:"DELETE",context:document.body,data:{path:fp}}).done(()=>window.location.reload());
  });
  // Re-run the header/body width sync after resizes settle, since a
  // resize can reflow text and reintroduce drift the same way an
  // async font load can.
  var resizeSyncTimer = null;
  $(window).on('resize', function() {
    clearTimeout(resizeSyncTimer);
    resizeSyncTimer = setTimeout(forceHeaderBodyColumnSync, 200);
  });
});


/* ── SELECT-ALL HEADER CHECKBOX ─────────────────────────────────────────────
   jqGrid builds two header rows:
     tr.ui-jqgrid-labels  → column title row  (this has the empty multiselect th)
     tr.ui-search-toolbar → filter row
   The multiselect <th> in the labels row contains only an empty <div>.
   We replace that div's content with a real checkbox.
──────────────────────────────────────────────────────────────────────────── */
function injectSelectAllCheckbox() {
  // The multiselect header th is the FIRST th inside the ui-jqgrid-labels row
  var $labelRow = $('#gbox_chartHistory').find('tr.ui-jqgrid-labels');
  var $th = $labelRow.find('th:first-child');
  var $div = $th.find('div');

  // Already injected — skip
  if ($th.find('input[type=checkbox]').length > 0) return;

  var $cb = $('<input>', {
    type : 'checkbox',
    id   : 'cb_selectAll',
    title: 'Select / deselect all rows on this page'
  }).css({
    display      : 'block',
    margin       : '0 auto',
    cursor       : 'pointer',
    width        : '15px',
    height       : '15px',
    accentColor  : '#4b7bec'
  });

  $div.empty().append($cb);

  $cb.on('change', function () {
    var ids = [];
    $('#chartHistory').find('tbody tr[role=row]').each(function() {
      ids.push($(this).attr('id'));
    });
    if ($(this).is(':checked')) {
      ids.forEach(function(id) { $('#chartHistory').jqGrid('setSelection', id, false); });
    } else {
      $('#chartHistory').jqGrid('resetSelection');
    }
  });

  // Uncheck the header box whenever individual rows are deselected
  $('#chartHistory').on('jqGridSelectRow', function() {
    var total = $('#chartHistory').find('tbody tr[role=row]').length;
    var selected = $('#chartHistory').jqGrid('getGridParam', 'selarrrow').length;
    $('#cb_selectAll').prop('checked', selected === total);
  });
}

const initNavGrid=function(){
  if($("#gridpager .navtable .fas").length===0){
    $("#chartHistory")
        .navGrid('#gridpager',{edit:false,add:false,del:false,search:false,refresh:false})
        .navButtonAdd('#gridpager',{title:'archive test execution(s)',          caption:'',buttonicon:"fas fa-archive",           onClickButton:showArchiveModalBulk,   position:"last"})
        .navButtonAdd('#gridpager',{title:'download test step attachment(s)',   caption:'',buttonicon:"fas fa-cloud-download-alt",onClickButton:downloadAttachmentsBulk,position:"last"});
    $("#gridpager .navtable .fas").removeClass('ui-icon');
  }
};
// NOTE on the fix below: the previous implementation determined "is this
// the pager's bulk action or a single row's icon?" by inspecting the
// global `event` object *inside* the requireAuth() callback. Since
// requireAuth() shows a login modal and waits for the user to type
// credentials and click Verify, the global `event` object has by then
// been overwritten by that Verify button's own click - not the original
// archive/download icon click - so the wrong branch got taken and
// garbage/undefined ended up as the "execution id", which serialized to
// `null` in the JSON sent to the backend. Fix: capture the correct ids
// synchronously, at the moment of the original click, and pass them
// through explicitly - never re-derive them later from a stale global.
const showArchiveModalBulk=function(){
  const ids=$("#chartHistory").jqGrid('getGridParam','selarrrow');
  requireAuth(function(){ _showArchiveModal(ids); });
};
const showArchiveModal=function(executionId){
  requireAuth(function(){ _showArchiveModal([executionId]); });
};
const _showArchiveModal=function(ids){
  $('#archiveModal .modal-footer .btn-primary').unbind();
  if(ids.length===0){$('#archiveModal .content').text('Please select test execution(s)!');$('#archiveModal .modal-footer .btn-primary').css('display','none').prop('disabled',true);}
  else{
    const cd=$('#archiveModal .content');cd.empty().text('Test step attachment(s) are deleted permanently. Do you want to proceed?').append('<br/><br/>');
    cd.append($('<input>',{type:'checkbox',id:'deleteAttachmentsOnly'})).append(' Delete attachments only');
    cd.append($('<input>',{type:'hidden',id:'executionIdsToArchive'}).val(ids.join(',')));
    $('#archiveModal .modal-footer .btn-primary').css('display','').prop('disabled',false).click(archiveExecutions);
  }
  $("#archiveModal .modal-footer .btn-secondary").prop('disabled',false);$('#archiveModalTitle').text('Archive');$('#archiveModal').modal('show');
};
const archiveExecutions=function(){
  $("#archiveModal .modal-footer .btn-primary,#archiveModal .modal-footer .btn-secondary").prop('disabled',true);
  const ids=$("#archiveModal .content #executionIdsToArchive").val();
  if(ids.length>0){
    const del=$('#deleteAttachmentsOnly').prop('checked');
    $.ajax({url:"/rs/test-execution/archive/"+del,method:'PUT',contentType:'application/json',data:JSON.stringify(ids.split(',').map(Number))})
        .done(()=>{toggleClassName($('#archiveModal .modal-body'),'successful',()=>$('#archiveModal').modal('hide'));if(!del){getTestRunHistory();reloadTestExecutionHistory();}})
        .fail(()=>toggleClassName($('#archiveModal .modal-body'),'failure'));
  }
};
const downloadAttachmentsBulk=function(){
  const sel=$("#chartHistory").jqGrid('getGridParam','selarrrow');
  requireAuth(function(){ _downloadAttachments(sel); });
};
const downloadAttachments=function(executionId){
  requireAuth(function(){ _downloadAttachments([executionId]); });
};
const _downloadAttachments=function(sel){
  if(sel.length===0){
    const cd=$('#archiveModal .content');cd.empty().text('Please select test execution(s)!');
    $('#archiveModal .modal-footer .btn-primary').css('display','none').off("click","**").prop('disabled',true);
    $('#archiveModalTitle').text('Download attachments(s)');$('#archiveModal').modal('show');
  }else{window.open("/rs/test-execution/download-attachments?executionId="+sel.join(','));}
};
const reloadTestExecutionHistory=function(){
  $('#chartHistory').jqGrid('clearGridData');
  $.get("/rs/test-execution",function(d){$('#chartHistory').jqGrid('setGridParam',{data:d}).trigger('reloadGrid');});
};
const historyActionsFormatter=function(cellValue,options,rowObject){
  return '<i class="fas fa-archive" title="archive test execution" execution-id="'+rowObject.id+'"></i>'+
      '<i class="fas fa-cloud-download-alt" title="download test step attachment(s)" execution-id="'+rowObject.id+'"></i>'+
      '<i class="fas fa-file-download" title="export offline report (HTML + screenshots, no server needed)" execution-id="'+rowObject.id+'"></i>';
};
const initActions=function(){
  $('.ui-jqgrid tr td[role="gridcell"] .fa-archive').click(function(){showArchiveModal($(this).attr('execution-id'));});
  $('.ui-jqgrid tr td[role="gridcell"] .fa-cloud-download-alt').click(function(){downloadAttachments($(this).attr('execution-id'));});
  $('.ui-jqgrid tr td[role="gridcell"] .fa-file-download').click(function(){exportStaticReport($(this).attr('execution-id'));});
};
const exportStaticReport=function(executionId){
  requireAuth(function(){ window.open('/rs/test-execution/'+executionId+'/export'); });
};
const getApplicationOptions=t=>t;
const getReleaseOptions=t=>t.filter(r=>(application==='ALL'||application==='')?true:r.applicationName===application);
const getRunOptions=t=>t.filter(r=>(application==='ALL'||application==='')?true:r.applicationName===application&&(release==='ALL'||release==='')?true:r.applicationVersion===release);
const resetReleaseOptions=t=>{const s=$('#gs_applicationVersion');s.empty().append('<option value=""'+((release==='ALL'||release==='')?'selected':'')+'>ALL</option>');new Set(getReleaseOptions(t).map(e=>e.applicationVersion)).forEach(e=>s.append('<option value="'+e+'"'+(release===e?' selected':'')+'>'+e+'</option>'));};
const resetRunOptions=t=>{const s=$('#gs_name');s.empty().append('<option value=""'+((execution==='ALL'||execution==='')?'selected':'')+'>ALL</option>');new Set(getRunOptions(t).map(e=>e.name)).forEach(e=>s.append('<option value="'+e+'"'+(execution===e?' selected':'')+'>'+e+'</option>'));};