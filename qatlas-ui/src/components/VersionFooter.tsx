export function VersionFooter() {
    const uiVersion =
        import.meta.env.VITE_QATLAS_UI_VERSION ??
        'dev';

    const apiClientVersion =
        import.meta.env.VITE_QATLAS_API_CLIENT_VERSION ??
        'dev';

    return (
        <div
            className="
        pointer-events-none
        fixed
        bottom-2
        right-3
        z-50
        text-[9px]
        text-slate-400
        sm:right-4
      "
        >
      <span className="hidden sm:inline">
        QAtlas UI v{uiVersion}
          {' · '}
          API Client v{apiClientVersion}
      </span>

            <span className="sm:hidden">
        UI v{uiVersion}
                {' · '}
                API v{apiClientVersion}
      </span>
        </div>
    );
}